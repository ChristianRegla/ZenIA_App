package com.zenia.app.ui.screens.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.zenia.app.R
import com.zenia.app.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar toda la lógica de autenticación de usuarios.
 * Maneja el registro, inicio de sesión, cierre de sesión y la gestión de la cuenta
 * (verificación, reseteo de contraseña, eliminación).
 *
 * @param auth La instancia de FirebaseAuth.
 * @param authRepository El repositorio para interactuar con Firestore (ej. crear documentos de usuario).
 * @param application La instancia de la Aplicación para acceder a recursos (como strings).
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val authRepository: AuthRepository,
    private val application: Application
) : AndroidViewModel(application) {
    /**
     * Expone el email del usuario actualmente autenticado.
     */
    val userEmail: String?
        get() = auth.currentUser?.email

    val isUserVerified: Boolean
        get() = auth.currentUser?.isEmailVerified ?: false

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer = _resendTimer.asStateFlow()

    private val _emailSentSuccess = MutableStateFlow(false)
    val emailSentSuccess = _emailSentSuccess.asStateFlow()

    private val _isResending = MutableStateFlow(false)
    val isResending = _isResending.asStateFlow()

    private var timerJob: Job? = null

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _isUserLoggedIn.value = (user != null && user.isEmailVerified)
    }

    /**
     * Bloque de inicialización. Se registra el [authStateListener] cuando el ViewModel se crea.
     */
    init {
        auth.addAuthStateListener(authStateListener)
    }

    /**
     * Se llama cuando el ViewModel está a punto de ser destruido.
     * Limpia él [authStateListener] para evitar memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        timerJob?.cancel()
        stopVerificationCheck()
    }

    /**
     * Validador simple para el formato de email.
     */
    private fun isValidEmail(email: String): Boolean = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    private fun isValidPassword(password: String): Boolean = password.isNotBlank() && password.length >= 6

    /**
     * Inicia sesión con Correo y Contraseña.
     * Si tiene éxito y el email está verificado, comprueba y crea el documento de usuario en Firestore.
     * Si el email no está verificado, cierra sesión y muestra un error.
     */
    fun signInWithEmail(email: String, password: String) {
        val cleanEmail = email.trim()

        if (_uiState.value is AuthUiState.Loading) return

        if (!isValidEmail(cleanEmail)) {
            _uiState.value = AuthUiState.Error(getString(R.string.auth_error_invalid_email))
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState.Error(getString(R.string.auth_error_fill_password_empty))
            return
        }

        launchCatching {
            _uiState.value = AuthUiState.Loading
            val result = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val user = result.user

            if (user != null) {
                if (user.isEmailVerified) {
                    authRepository.createUserIfNew(user.uid, user.email, isNewUser = false)
                    _uiState.value = AuthUiState.Idle
                } else {
                    _uiState.value = AuthUiState.VerificationRequired(cleanEmail)
                    startVerificationCheck()
                }
            } else {
                _uiState.value = AuthUiState.Error(getString(R.string.auth_error_unknown))
            }
        }
    }

    /**
     * Crea una nueva cuenta de usuario.
     * Si tiene éxito, crea el documento de usuario en Firestore, envía un correo de verificación,
     * y cierra la sesión para forzar al usuario a verificar su email.
     */
    fun createUser(email: String, password: String, confirmPassword: String) {
        val cleanEmail = email.trim()

        if (_uiState.value is AuthUiState.Loading) return

        if (!isValidEmail(cleanEmail)) {
            _uiState.value = AuthUiState.Error(getString(R.string.auth_error_invalid_email))
            return
        }
        if (!isValidPassword(password)) {
            _uiState.value = AuthUiState.Error(getString(R.string.auth_error_weak_password))
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error(getString(R.string.auth_error_passwords_no_match))
            return
        }

        launchCatching {
            _uiState.value = AuthUiState.Loading
            val result = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val newUser = result.user

            if (newUser != null) {
                authRepository.createUserIfNew(newUser.uid, cleanEmail, isNewUser = true)

                newUser.sendEmailVerification().await()

                _uiState.value = AuthUiState.VerificationRequired(cleanEmail)
                startVerificationCheck()
            }
        }
    }

    fun resendVerification() {
        if (_resendTimer.value > 0) return

        val user = auth.currentUser ?: return

        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.VerificationRequired(user.email ?: "")

                _isResending.value = true
                user.sendEmailVerification().await()
                _isResending.value = false

                startResendTimer()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    private var verificationPollJob: Job? = null

    fun startVerificationCheck() {
        if (verificationPollJob?.isActive == true) return

        verificationPollJob = viewModelScope.launch {
            while (true) {
                val user = auth.currentUser
                user?.reload()?.await()

                if (user?.isEmailVerified == true) {
                    _uiState.value = AuthUiState.Idle
                    _isUserLoggedIn.value = true

                    break
                }
                delay(3000)
            }
        }
    }

    fun stopVerificationCheck() {
        verificationPollJob?.cancel()
    }

    /**
     * Inicia sesión o registra al usuario usando una Credencial de Google (Sign-In with Google).
     * Si tiene éxito, comprueba y crea el documento de usuario en Firestore.
     */
    fun signInWithGoogle(credential: AuthCredential) {
        if (_uiState.value == AuthUiState.Loading) return

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false

                if (user != null) {
                    authRepository.createUserIfNew(user.uid, user.email, isNewUser)
                }
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    /**
     * Envía un correo de restablecimiento de contraseña a la dirección de email proporcionada.
     */
    fun sendPasswordResetEmail(email: String) {
        val cleanEmail = email.trim()

        if (_resendTimer.value > 0) return

        if (!isValidEmail(cleanEmail)) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_invalid_email))
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(cleanEmail).await()
                _uiState.value = AuthUiState.PasswordResetSent
                _emailSentSuccess.value = true
                startResendTimer()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    // Limpia el estado cuando salimos de la pantalla
    fun resetForgotPasswordState() {
        _resendTimer.value = 0
        _emailSentSuccess.value = false
        _uiState.value = AuthUiState.Idle
    }

    /**
     * Reenvía el correo de verificación al usuario actual (si no está verificado).
     */
    fun resendVerificationEmail() {
        if (_uiState.value == AuthUiState.Loading) return

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                authRepository.sendEmailVerification()
                _uiState.value = AuthUiState.VerificationSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    /**
     * Elimina permanentemente la cuenta del usuario actual de Firebase Authentication.
     * Las reglas de Firestore (o Cloud Functions) deberían encargarse de limpiar sus datos.
     */
    fun deleteAccount() {
        if (_uiState.value == AuthUiState.Loading) return

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val userId = user?.uid

                if (userId != null) {
                    authRepository.deleteUserData(userId)
                }

                user?.delete()?.await()

                _uiState.value = AuthUiState.AccountDeleted
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _resendTimer.value = 60
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    /**
     * Resetea el estado de la UI a [AuthUiState.Idle].
     * Útil para limpiar un mensaje de error después de que el usuario lo haya visto.
     */
    fun resetState() {
        if (_uiState.value !is AuthUiState.Loading) {
            _uiState.value = AuthUiState.Idle
        }
    }

    private fun launchCatching(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _isUserLoggedIn.value = false
    }

    /**
     * Mapea las excepciones de Firebase Auth a mensajes de error legibles para el usuario.
     * @param e La excepción capturada.
     * @return Un String (desde strings.xml) que describe el error.
     */
    private fun mapFirebaseAuthException(e: Exception): String {
        return when (e) {
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> application.getString(R.string.auth_error_invalid_email)
                "ERROR_WRONG_PASSWORD" -> application.getString(R.string.auth_error_wrong_password)
                "ERROR_USER_NOT_FOUND" -> application.getString(R.string.auth_error_user_not_found)
                "ERROR_EMAIL_ALREADY_IN_USE" -> application.getString(R.string.auth_error_email_in_use)
                "ERROR_WEAK_PASSWORD" -> application.getString(R.string.auth_error_weak_password)
                "ERROR_REQUIRES_RECENT_LOGIN" -> application.getString(R.string.auth_error_requires_recent_login)
                else -> application.getString(R.string.auth_error_default, e.message ?: "Unknown")
            }
            else -> e.message?.let { application.getString(R.string.auth_error_unexpected, it) } ?: application.getString(
                R.string.auth_error_unexpected, "Unknown")
        }
    }

    private fun getString(resId: Int, vararg formatArgs: Any): String {
        return application.getString(resId, *formatArgs)
    }
}