package com.zenia.app.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.zenia.app.viewmodel.AuthUiState
import com.zenia.app.viewmodel.AuthViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import com.zenia.app.ui.screens.lock.canAuthenticate

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
                    message = "Cuenta eliminada exitosamente.",
                    duration = SnackbarDuration.Short
                )
                authViewModel.resetState()
                onNavigateToAuth()
            }
            is AuthUiState.VerificationSent -> {
                snackbarHostState.showSnackbar(
                    message = "Correo de verificación enviado.",
                    duration = SnackbarDuration.Short
                )
                authViewModel.resetState()
            }
            is AuthUiState.PasswordResetSent -> {
                snackbarHostState.showSnackbar(
                    message = "Correo enviado. Revisa tu bandeja de entrada para cambiar tu contraseña.",
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
                title = { Text("Mi Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
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
                text = "Información de la Cuenta",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Correo: ${userEmail ?: "No disponible"}")

            Text("Verificado: ${if (isVerified) "Sí" else "No"}")

            Spacer(modifier = Modifier.height(24.dp))
            if (canUseBiometrics) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Bloquear app con huella/rostro",
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
                    text = "No hay hardware biométrico disponible en este dispositivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState == AuthUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar Mi Cuenta")
                }

                if  (!isVerified) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { authViewModel.resendVerificationEmail() }) {
                        Text("Reenviar correo de verificación")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    authViewModel.sendPasswordResetEmail(userEmail ?: "")
                }) {
                    Text("Cambiar Contraseña")
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Advertencia: La eliminación de la cuenta es permanente y no se puede deshacer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar cuenta?") },
            text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción es irreversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        authViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}