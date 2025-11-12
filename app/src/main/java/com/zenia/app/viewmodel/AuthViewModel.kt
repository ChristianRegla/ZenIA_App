package com.zenia.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    val userEmail: String?
        get() = auth.currentUser?.email

    val isUserVerified: Boolean
        get() = auth.currentUser?.isEmailVerified ?: false

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _isUserLoggedIn.value = (user != null && user.isEmailVerified)
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor ingrese un correo electrónico y una contraseña.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null && user.isEmailVerified) {
                } else {
                    auth.signOut()
                    _uiState.value = AuthUiState.Error("El correo electrónico no ha sido verificado.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun createUser(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val newUser = result.user
                newUser?.sendEmailVerification()
                auth.signOut()
                _uiState.value = AuthUiState.VerificationSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor ingrese un correo electrónico.")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _uiState.value = AuthUiState.PasswordResetSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun resendVerificationEmail() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null && !user.isEmailVerified) {
                    user.sendEmailVerification().await()
                    _uiState.value = AuthUiState.VerificationSent
                } else {
                    _uiState.value = AuthUiState.Error("El correo electrónico ya ha sido verificado.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun deleteAccount() {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                user?.delete()?.await()
                _uiState.value = AuthUiState.AccountDeleted
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    private fun mapFirebaseAuthException(e: Exception): String {
        return when (e) {
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "Formato de correo inválido."
                "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta."
                "ERROR_USER_NOT_FOUND" -> "No se encontró usuario con este correo."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya está registrado."
                "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil (mínimo 6 caracteres)."
                "ERROR_REQUIRES_RECENT_LOGIN" -> "Esta operación es sensible. Por favor, cierra sesión y vuelve a iniciarla para completarla."
                else -> "Error de autenticación: ${e.message}"
            }
            else -> "Ocurrió un error inesperado: ${e.message}"
        }
    }
}