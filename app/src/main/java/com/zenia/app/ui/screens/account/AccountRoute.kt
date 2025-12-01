package com.zenia.app.ui.screens.account

import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import com.zenia.app.R
import com.zenia.app.ui.screens.auth.AuthUiState
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import java.util.Locale

/**
 * Composable "inteligente" (Smart Composable) para la ruta de la cuenta de usuario.
 * Obtiene estado de [AuthViewModel] y [SettingsViewModel], maneja la lógica de UI
 * (diálogos, snackbars, biometría, idioma) y pasa el estado y las acciones
 * al Composable "tonto" [AccountScreen].
 */
@Composable
fun AccountRoute(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    // --- 1. Estado y Handlers ---
    val uiState by authViewModel.uiState.collectAsState()
    val userEmail = authViewModel.userEmail
    val isVerified = authViewModel.isUserVerified
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val allowWeakBiometrics by settingsViewModel.allowWeakBiometrics.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val biometricManager = remember { BiometricManager.from(context) }

    val canUseStrong = remember {
        biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    val canUseWeak = remember {
        biometricManager.canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }
    val currentLanguage = (AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()).language

    // --- 2. Efectos Secundarios (Snackbars y Navegación) ---
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.AccountDeleted -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.account_delete_success),
                    duration = SnackbarDuration.Short
                )
                authViewModel.resetState()
                onNavigateToAuth()
            }
            is AuthUiState.VerificationSent -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.account_verification_sent),
                    duration = SnackbarDuration.Short
                )
                authViewModel.resetState()
            }
            is AuthUiState.PasswordResetSent -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.account_password_reset_sent),
                    duration = SnackbarDuration.Long
                )
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                authViewModel.resetState()
            }
            else -> {}
        }
    }

    // --- 3. Definición de Estado y Acciones ---
    val screenState = AccountScreenState(
        uiState = uiState,
        userEmail = userEmail,
        isVerified = isVerified,
        isBiometricEnabled = isBiometricEnabled ?: false,
        allowWeakBiometrics = allowWeakBiometrics,
        canUseStrongBiometrics = canUseStrong,
        canUseWeakBiometrics = canUseWeak,
        currentLanguage = currentLanguage,
        showDeleteDialog = showDeleteDialog,
        snackbarHostState = snackbarHostState
    )

    val screenActions = AccountScreenActions(
        onNavigateBack = onNavigateBack,
        onBiometricToggle = { settingsViewModel.setBiometricEnabled(it) },
        onWeakBiometricToggle = { settingsViewModel.setWeakBiometricsEnabled(it) },
        onLanguageChange = { langTag ->
            val appLocale = LocaleListCompat.forLanguageTags(langTag)
            AppCompatDelegate.setApplicationLocales(appLocale)
        },
        onDeleteAccountClick = { showDeleteDialog = true },
        onResendVerificationClick = { authViewModel.resendVerificationEmail() },
        onChangePasswordClick = {
            authViewModel.sendPasswordResetEmail(userEmail ?: "")
        },
        onConfirmDelete = {
            showDeleteDialog = false
            authViewModel.deleteAccount()
        },
        onDismissDeleteDialog = { showDeleteDialog = false }
    )

    AccountScreen(
        state = screenState,
        actions = screenActions
    )
}