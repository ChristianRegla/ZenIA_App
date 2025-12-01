package com.zenia.app.ui.screens.auth

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.zenia.app.R
import com.zenia.app.data.ZeniaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel encargado de gestionar toda la lógica de autenticación de usuarios.
 * Maneja el registro, inicio de sesión, cierre de sesión y la gestión de la cuenta
 * (verificación, reseteo de contraseña, eliminación).
 *
 * @param auth La instancia de FirebaseAuth.
 * @param repositorio El repositorio para interactuar con Firestore (ej. crear documentos de usuario).
 * @param application La instancia de la Aplicación para acceder a recursos (como strings).
 */
class AuthViewModel(
    private val auth: FirebaseAuth,
    private val repositorio: ZeniaRepository,
    private val application: Application
) : AndroidViewModel(application) {
    /**
     * Expone el email del usuario actualmente autenticado.
     */
    val userEmail: String?
        get() = auth.currentUser?.email

    /**
     * Expone si el usuario actual ha verificado su correo electrónico.
     */
    val isUserVerified: Boolean
        get() = auth.currentUser?.isEmailVerified ?: false

    // StateFlow interno para el estado de la UI (Cargando, Error, etc.)
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    /**
     * Expone el estado de la UI para operaciones asíncronas de autenticación.
     * La UI observa este Flow para mostrar loaders, snackbars de error, etc.
     */
    val uiState = _uiState.asStateFlow()

    // StateFlow interno para saber si el usuario está logueado.
    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    /**
     * Expone un boolean que indica si el usuario está logueado Y verificado.
     * Este es el "estado de verdad" principal para la navegación de la app.
     */
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    /**
     * Listener que se activa cada vez que el estado de autenticación de Firebase cambia (login/logout).
     * Actualiza [_isUserLoggedIn] basado en si el usuario no es nulo Y su email está verificado.
     */
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        // La app considera a un usuario "logueado" solo si existe Y ha verificado su email.
        _isUserLoggedIn.value = (user != null && user.isEmailVerified)
    }

    // Estado para el temporizador de reenvío (0 significa listo para enviar)
    private val _resendTimer = MutableStateFlow(0)
    val resendTimer = _resendTimer.asStateFlow()

    // Estado para saber si ya se envió al menos una vez (para mostrar el mensaje de Spam)
    private val _emailSentSuccess = MutableStateFlow(false)
    val emailSentSuccess = _emailSentSuccess.asStateFlow()

    /**
     * Bloque de inicialización. Se registra el [authStateListener] cuando el ViewModel se crea.
     */
    init {
        auth.addAuthStateListener(authStateListener)
    }

    /**
     * Se llama cuando el ViewModel está a punto de ser destruido.
     * Limpia el [authStateListener] para evitar memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    /**
     * Validador simple para el formato de email.
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validador simple para la contraseña.
     * Firebase requiere al menos 6 caracteres.
     */
    private fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }

    /**
     * Inicia sesión con Correo y Contraseña.
     * Si tiene éxito y el email está verificado, comprueba y crea el documento de usuario en Firestore.
     * Si el email no está verificado, cierra sesión y muestra un error.
     */
    fun signInWithEmail(email: String, password: String) {
        if (_uiState.value == AuthUiState.Loading) return

        if (!isValidEmail(email)) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_invalid_email))
            return
        }
        if (password.isBlank()) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_fill_password_empty))
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null && user.isEmailVerified) {
                    repositorio.checkAndCreateUserDocument(user.uid, user.email)
                    _uiState.value = AuthUiState.Idle
                } else {
                    auth.signOut()
                    _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_email_not_verified))
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    /**
     * Crea una nueva cuenta de usuario.
     * Si tiene éxito, crea el documento de usuario en Firestore, envía un correo de verificación,
     * y cierra la sesión para forzar al usuario a verificar su email.
     */
    fun createUser(email: String, password: String, confirmPassword: String) {
        if (_uiState.value == AuthUiState.Loading) return

        if (!isValidEmail(email)) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_invalid_email))
            return
        }
        if (!isValidPassword(password)) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_weak_password))
            return
        }
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_passwords_no_match))
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val newUser = result.user
                if (newUser != null) {
                    repositorio.checkAndCreateUserDocument(newUser.uid, email)
                    newUser.sendEmailVerification()
                }
                auth.signOut()
                _uiState.value = AuthUiState.VerificationSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
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

                if (user != null) {
                    repositorio.checkAndCreateUserDocument(user.uid, user.email)
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
        if (_resendTimer.value > 0) return

        if (!isValidEmail(email)) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_invalid_email))
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = AuthUiState.PasswordResetSent
                _emailSentSuccess.value = true
                startResendTimer()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    private fun startResendTimer() {
        viewModelScope.launch {
            _resendTimer.value = 60
            while (_resendTimer.value > 0) {
                kotlinx.coroutines.delay(1000)
                _resendTimer.value -= 1
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
                val user = auth.currentUser
                if (user != null && !user.isEmailVerified) {
                    user.sendEmailVerification().await()
                    _uiState.value = AuthUiState.VerificationSent
                } else {
                    _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_email_already_verified))
                }
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
                    repositorio.deleteUserData(userId)
                }

                user?.delete()?.await()

                _uiState.value = AuthUiState.AccountDeleted
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    /**
     * Cierra la sesión del usuario actual.
     * El [authStateListener] se encargará de actualizar [_isUserLoggedIn].
     */
    fun signOut() {
        if (_uiState.value == AuthUiState.Loading) return

        viewModelScope.launch {
            auth.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    /**
     * Resetea el estado de la UI a [AuthUiState.Idle].
     * Útil para limpiar un mensaje de error después de que el usuario lo haya visto.
     */
    fun resetState() {
        _uiState.value = AuthUiState.Idle
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
}