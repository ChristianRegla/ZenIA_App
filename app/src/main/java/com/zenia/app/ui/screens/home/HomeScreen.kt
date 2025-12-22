package com.zenia.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import com.zenia.app.R
import com.zenia.app.ui.components.HomeTopBar
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.ui.theme.ZeniaTeal

/**
 * Pantalla principal "tonta" (Stateless Composable).
 * No recibe el ViewModel. Solo recibe el estado actual y eventos (lambdas)
 * desde [HomeRoute].
 *
 * @param esPremium Indica si el usuario tiene suscripción premium.
 * @param hasPermission Indica si ya se concedieron permisos de Health Connect.
 * @param healthConnectStatus Estado del SDK (Disponible, No instalado, etc.).
 * @param onSignOut Acción al pulsar cerrar sesión.
 * @param onNavigateToAccount Acción al pulsar ir a cuenta.
 * @param onConnectSmartwatch Acción para solicitar permisos de Health Connect.
 * @param onNavigateToPremium Acción para ir a la pantalla de pago.
 * @param onNavigateToManualPermission Acción para abrir ajustes de la app en Android.
 * @param onInstallHealthConnect Acción para ir a la Play Store a instalar Health Connect.
 * @param onSettingsClick Acción al pulsar el engranaje en la barra superior.
 * @param onNotificationClick Acción al pulsar la campana en la barra superior.
 * @param onNavigateToSOS Acción al pulsar el botón flotante.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    esPremium: Boolean,
    hasPermission: Boolean,
    healthConnectStatus: Int,
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onConnectSmartwatch: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToManualPermission: () -> Unit,
    onInstallHealthConnect: () -> Unit,
    onSettingsClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onResetState: () -> Unit,
    onNavigateToSOS: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    if (uiState is HomeUiState.Error) {
        val errorMessage = uiState.message.asString()

        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(errorMessage)
            onResetState()
        }
    }
    Scaffold(
        topBar = {
            HomeTopBar(
                onSettingsClick = onSettingsClick,
                onNotificationClick = onNotificationClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToSOS,
                shape = CircleShape,
                containerColor = ZeniaTeal,
                contentColor = Color.White
            ) {
                Icon(
                    painter = painterResource(R.drawable.telefono),
                    contentDescription = stringResource(R.string.sos_btn_lifeline)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState is HomeUiState.Loading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Lógica de UI basada en el estado de Health Connect
            when (healthConnectStatus) {
                HealthConnectClient.SDK_AVAILABLE -> {
                    if (esPremium) {
                        if (!hasPermission) {
                            // Usuario Premium pero sin permisos -> Botón de conectar
                            Button(onClick = onConnectSmartwatch) {
                                Text(stringResource(R.string.home_connect_watch))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Botón de ayuda por si el popup no sale (fuerza ir a ajustes)
                            TextButton(onClick = onNavigateToManualPermission) {
                                Text(
                                    text = stringResource(R.string.home_connect_watch_help),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            // Premium Y Conectado -> Mensaje de éxito
                            Text(
                                text = stringResource(R.string.home_watch_connected),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        // Usuario Free -> Botón para hacerse Premium
                        Button(onClick = onNavigateToPremium) {
                            Text(stringResource(R.string.home_connect_watch_premium))
                        }
                    }
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    // El SDK está soportado pero falta instalar la app de Google
                    Text(
                        text = stringResource(R.string.home_health_connect_missing),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onInstallHealthConnect) {
                        Text(stringResource(R.string.home_install_health_connect))
                    }
                }
                else -> {
                    // SDK_UNAVAILABLE o casos raros.
                    Text(
                        text = stringResource(R.string.account_biometrics_not_available),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botones de navegación generales
            Button(onClick = onNavigateToAccount) {
                Text(stringResource(R.string.home_my_account))
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(name = "Usuario Free", showBackground = true)
@Composable
fun HomeScreenPreview_Free() {
    ZenIATheme {
        HomeScreen(
            uiState = HomeUiState.Idle,
            esPremium = false,
            hasPermission = false,
            healthConnectStatus = HealthConnectClient.SDK_AVAILABLE,
            onSignOut = {},
            onNavigateToAccount = {},
            onConnectSmartwatch = {},
            onNavigateToPremium = {},
            onNavigateToManualPermission = {},
            onInstallHealthConnect = {},
            onSettingsClick = {},
            onNotificationClick = {},
            onResetState = {},
            onNavigateToSOS = {}
        )
    }
}

@Preview(name = "Usuario Premium Conectado", showBackground = true)
@Composable
fun HomeScreenPreview_Connected() {
    ZenIATheme {
        HomeScreen(
            uiState = HomeUiState.Idle,
            esPremium = true,
            hasPermission = true,
            healthConnectStatus = HealthConnectClient.SDK_AVAILABLE,
            onSignOut = {},
            onNavigateToAccount = {},
            onConnectSmartwatch = {},
            onNavigateToPremium = {},
            onNavigateToManualPermission = {},
            onInstallHealthConnect = {},
            onSettingsClick = {},
            onNotificationClick = {},
            onResetState = {},
            onNavigateToSOS = {}
        )
    }
}