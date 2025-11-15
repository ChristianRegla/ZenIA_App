package com.zenia.app.ui.screens.account

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.zenia.app.R
import com.zenia.app.viewmodel.AuthUiState
import com.zenia.app.viewmodel.AuthViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import com.zenia.app.ui.screens.lock.canAuthenticate
import com.zenia.app.ui.theme.ZenIATheme
import java.util.Locale

/**
 * Clase de datos que agrupa todo el estado necesario para la UI de AccountScreen.
 */
data class AccountScreenState(
    val uiState: AuthUiState,
    val userEmail: String?,
    val isVerified: Boolean,
    val canUseBiometrics: Boolean,
    val isBiometricEnabled: Boolean,
    val currentLanguage: String,
    val showDeleteDialog: Boolean,
    val snackbarHostState: SnackbarHostState
)

/**
 * Clase de datos que agrupa todas las acciones (lambdas) que la UI puede disparar.
 */
data class AccountScreenActions(
    val onNavigateBack: () -> Unit,
    val onBiometricToggle: (Boolean) -> Unit,
    val onLanguageChange: (String) -> Unit,
    val onDeleteAccountClick: () -> Unit,
    val onResendVerificationClick: () -> Unit,
    val onChangePasswordClick: () -> Unit,
    val onConfirmDelete: () -> Unit,
    val onDismissDeleteDialog: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    state: AccountScreenState,
    actions: AccountScreenActions
    ) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = state.snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.account_info_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.account_email_label) +
                        (state.userEmail ?: stringResource(R.string.common_not_available))
            )

            Text(
                stringResource(R.string.account_verified_label) +
                        stringResource(if (state.isVerified) R.string.common_yes else R.string.common_no)
            )

            Spacer(modifier = Modifier.height(24.dp))
            if (state.canUseBiometrics) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.account_biometrics_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = state.isBiometricEnabled,
                        onCheckedChange = actions.onBiometricToggle
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.account_biometrics_not_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.account_language_label),
                    style = MaterialTheme.typography.bodyLarge
                )

                val currentLanguage = (AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()).language
                val isEsSelected = currentLanguage == "es"

                Row {
                    val esOnClick = {
                        val appLocale = LocaleListCompat.forLanguageTags("es")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                    if (isEsSelected) {
                        Button(onClick = esOnClick) { Text("ES") }
                    } else {
                        OutlinedButton(onClick = esOnClick) { Text("ES") }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val enOnClick = {
                        val appLocale = LocaleListCompat.forLanguageTags("en")
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    }
                    if (isEsSelected) {
                        OutlinedButton(onClick = enOnClick) { Text("EN") }
                    } else {
                        Button(onClick = enOnClick) { Text("EN") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (state.uiState == AuthUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = actions.onDeleteAccountClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.account_delete_button))
                }

                if (!state.isVerified) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = actions.onResendVerificationClick) {
                        Text(stringResource(R.string.account_resend_verification_button))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = actions.onChangePasswordClick) {
                    Text(stringResource(R.string.account_change_password_button))
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.account_delete_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = actions.onDismissDeleteDialog,
            title = { Text(stringResource(R.string.account_delete_dialog_title)) },
            text = { Text(stringResource(R.string.account_delete_dialog_text)) },
            confirmButton = {
                Button(
                    onClick = actions.onConfirmDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissDeleteDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(name = "Cuenta Verificada", showBackground = true)
@Composable
fun AccountScreenPreview_Verified() {
    val state = AccountScreenState(
        uiState = AuthUiState.Idle,
        userEmail = "test@zenia.app",
        isVerified = true,
        canUseBiometrics = true,
        isBiometricEnabled = true,
        currentLanguage = "es",
        showDeleteDialog = false,
        snackbarHostState = SnackbarHostState()
    )
    val actions = AccountScreenActions({}, {}, {}, {}, {}, {}, {}, {})

    ZenIATheme {
        AccountScreen(state = state, actions = actions)
    }
}

@Preview(name = "Cuenta No Verificada", showBackground = true)
@Composable
fun AccountScreenPreview_NotVerified() {
    val state = AccountScreenState(
        uiState = AuthUiState.Idle,
        userEmail = "test@zenia.app",
        isVerified = false,
        canUseBiometrics = true,
        isBiometricEnabled = false,
        currentLanguage = "en",
        showDeleteDialog = false,
        snackbarHostState = SnackbarHostState()
    )
    val actions = AccountScreenActions({}, {}, {}, {}, {}, {}, {}, {})

    ZenIATheme {
        AccountScreen(state = state, actions = actions)
    }
}

@Preview(name = "Diálogo de Borrado", showBackground = true)
@Composable
fun AccountScreenPreview_DeleteDialog() {
    val state = AccountScreenState(
        uiState = AuthUiState.Idle,
        userEmail = "test@zenia.app",
        isVerified = true,
        canUseBiometrics = true,
        isBiometricEnabled = true,
        currentLanguage = "es",
        showDeleteDialog = true,
        snackbarHostState = SnackbarHostState()
    )
    val actions = AccountScreenActions({}, {}, {}, {}, {}, {}, {}, {})

    ZenIATheme {
        AccountScreen(state = state, actions = actions)
    }
}