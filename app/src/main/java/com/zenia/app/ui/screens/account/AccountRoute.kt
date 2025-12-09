package com.zenia.app.ui.screens.account

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.zenia.app.R
import com.zenia.app.ui.screens.auth.AuthUiState
import com.zenia.app.ui.screens.auth.AuthViewModel

@Composable
fun AccountRoute(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val context = LocalContext.current

    val uiState by authViewModel.uiState.collectAsState()

    val userEmail = authViewModel.userEmail ?: ""
    val isVerified = authViewModel.isUserVerified

    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.AccountDeleted -> {
                onNavigateToAuth()
                authViewModel.resetState()
            }
            is AuthUiState.VerificationSent -> {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.account_verification_sent)
                )
                authViewModel.resetState()
            }
            is AuthUiState.PasswordResetSent -> {
                snackbarHostState.showSnackbar(
                    context.getString(R.string.account_password_reset_sent)
                )
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                val errorMsg = (uiState as AuthUiState.Error).message
                snackbarHostState.showSnackbar(errorMsg)
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    val state = AccountScreenState(
        isLoading = uiState is AuthUiState.Loading,
        userEmail = userEmail,
        isVerified = isVerified,
        showDeleteDialog = showDeleteDialog,
        snackbarHostState = snackbarHostState
    )

    val actions = AccountScreenActions(
        onNavigateBack = onNavigateBack,
        onResendVerification = {
            authViewModel.resendVerificationEmail()
        },
        onChangePassword = {
            if (userEmail.isNotBlank()) {
                authViewModel.sendPasswordResetEmail(userEmail)
            }
        },
        onDeleteAccountRequest = { showDeleteDialog = true },
        onConfirmDeleteAccount = {
            showDeleteDialog = false
            authViewModel.deleteAccount()
        },
        onDismissDeleteDialog = { showDeleteDialog = false }
    )

    AccountScreen(
        state = state,
        actions = actions
    )
}