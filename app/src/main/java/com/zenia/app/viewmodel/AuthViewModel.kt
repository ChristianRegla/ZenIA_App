package com.zenia.app.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.zenia.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val auth: FirebaseAuth,
    private val application: Application
) : AndroidViewModel(application) {
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

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }

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
                newUser?.sendEmailVerification()
                auth.signOut()
                _uiState.value = AuthUiState.VerificationSent
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun signInWithGoogle(credential: AuthCredential) {
        if (_uiState.value == AuthUiState.Loading) return

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                _uiState.value = AuthUiState.Idle
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseAuthException(e))
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        if (_uiState.value == AuthUiState.Loading) return

        if (!isValidEmail(email)) {
            _uiState.value = AuthUiState.Error(application.getString(R.string.auth_error_invalid_email))
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

    fun deleteAccount() {
        if (_uiState.value == AuthUiState.Loading) return

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
        if (_uiState.value == AuthUiState.Loading) return

        viewModelScope.launch {
            auth.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

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
            else -> e.message?.let { application.getString(R.string.auth_error_unexpected, it) } ?: application.getString(R.string.auth_error_unexpected, "Unknown")
        }
    }
}