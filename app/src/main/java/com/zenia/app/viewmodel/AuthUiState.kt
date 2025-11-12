package com.zenia.app.viewmodel

sealed interface AuthUiState {
    object Idle: AuthUiState
    object Loading: AuthUiState
    object VerificationSent: AuthUiState
    object PasswordResetSent: AuthUiState
    object AccountDeleted: AuthUiState
    data class Error(val message: String): AuthUiState
}