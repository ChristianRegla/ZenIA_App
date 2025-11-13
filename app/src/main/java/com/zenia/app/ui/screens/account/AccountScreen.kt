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
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.zenia.app.R
import com.zenia.app.viewmodel.AuthUiState
import com.zenia.app.viewmodel.AuthViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import com.zenia.app.ui.screens.lock.canAuthenticate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAuth: () -> Unit
    ) {
    val uiState by authViewModel.uiState.collectAsState()
    val userEmail = authViewModel.userEmail
    val isVerified = authViewModel.isUserVerified

    val context = LocalContext.current
    val canUseBiometrics = remember { canAuthenticate(context) }
    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                        (userEmail ?: stringResource(R.string.common_not_available))
            )

            Text(
                stringResource(R.string.account_verified_label) +
                        stringResource(if (isVerified) R.string.common_yes else R.string.common_no)
            )

            Spacer(modifier = Modifier.height(24.dp))
            if (canUseBiometrics) {
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
                        checked = isBiometricEnabled,
                        onCheckedChange = { isEnabled ->
                            settingsViewModel.setBiometricEnabled(isEnabled)
                        }
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

            if (uiState == AuthUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.account_delete_button))
                }

                if  (!isVerified) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { authViewModel.resendVerificationEmail() }) {
                        Text(stringResource(R.string.account_resend_verification_button))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    authViewModel.sendPasswordResetEmail(userEmail ?: "")
                }) {
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

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.account_delete_dialog_title)) },
            text = { Text(stringResource(R.string.account_delete_dialog_text)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        authViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}