package com.zenia.app.ui.screens.auth

sealed interface AuthUiState {
    object Idle: AuthUiState
    object Loading: AuthUiState
    object VerificationSent: AuthUiState
    object PasswordResetSent: AuthUiState
    object AccountDeleted: AuthUiState
    object Authenticated: AuthUiState
    data class Error(val message: String): AuthUiState
    data class VerificationRequired(val email: String): AuthUiState
}