package com.zenia.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import com.zenia.app.R
import com.zenia.app.ui.components.HomeTopBar
import com.zenia.app.ui.theme.ZenIATheme

/**
 * Pantalla principal "tonta" (Dumb Composable).
 * No contiene lógica de estado, solo recibe el estado actual y lambdas
 * para notificar eventos hacia arriba (al navegador).
 *
 * @param esPremium Si el usuario actual es premium.
 * @param hasPermission Si la app tiene permisos de Health Connect.
 * @param isHealthAvailable Si Health Connect está disponible en el dispositivo.
 * @param onSignOut Lambda que se invoca cuando el usuario pulsa "Cerrar Sesión".
 * @param onNavigateToAccount Lambda que se invoca cuando el usuario pulsa "Mi Cuenta".
 * @param onConnectSmartwatch Lambda que se invoca cuando el usuario pulsa "Conectar Smartwatch".
 * @param onNavigateToPremium Lambda que se invoca cuando el usuario pulsa el botón de "Premium".
 */
@Composable
fun HomeScreen(
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
    onNotificationClick: () -> Unit
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                onSettingsClick = onSettingsClick,
                onNotificationClick = onNotificationClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.home_welcome),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            when (healthConnectStatus) {
                HealthConnectClient.SDK_AVAILABLE -> {
                    if (esPremium) {
                        if (!hasPermission) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onConnectSmartwatch) {
                                Text(stringResource(R.string.home_connect_watch))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onNavigateToManualPermission) {
                                Text(
                                    text = stringResource(R.string.home_connect_watch_help),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToPremium) {
                            Text(stringResource(R.string.home_connect_watch_premium))
                        }
                    }
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    if (esPremium) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.home_health_connect_missing),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onInstallHealthConnect) {
                            Text(stringResource(R.string.home_install_health_connect))
                        }
                    } else {
                        // Si no es premium, mostramos el botón premium normal
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToPremium) {
                            Text(stringResource(R.string.home_connect_watch_premium))
                        }
                    }
                }
                else -> {

                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNavigateToAccount) {
                Text(stringResource(R.string.home_my_account))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onSignOut) {
                Text(stringResource(R.string.sign_out))
            }
        }
    }
}

/**
 * Vista previa para un usuario "Free".
 * Muestra el botón de "Función Premium".
 */
@Preview(name = "Usuario Gratuito", showBackground = true)
@Composable
fun HomeScreenPreview_FreeUser() {
    ZenIATheme {
        HomeScreen(
            esPremium = false,
            hasPermission = false,
            healthConnectStatus = 0,
            onSignOut = { },
            onNavigateToAccount = { },
            onConnectSmartwatch = { },
            onNavigateToPremium = { },
            onNavigateToManualPermission = { },
            onInstallHealthConnect = { },
            onSettingsClick = { },
            onNotificationClick = { }
        )
    }
}

/**
 * Vista previa para un usuario "Premium" que aún no ha dado permisos.
 * Muestra el botón normal de "Conectar Smartwatch".
 */
@Preview(name = "Premium (Sin Permisos)", showBackground = true)
@Composable
fun HomeScreenPreview_Premium_NeedsPermission() {
    ZenIATheme {
        HomeScreen(
            esPremium = true,
            hasPermission = false,
            healthConnectStatus =  0,
            onSignOut = { },
            onNavigateToAccount = { },
            onConnectSmartwatch = { },
            onNavigateToPremium = { },
            onNavigateToManualPermission = { },
            onInstallHealthConnect = { },
            onSettingsClick = { },
            onNotificationClick = { }
        )
    }
}

/**
 * Vista previa para un usuario "Premium" que ya conectó su reloj.
 * Muestra el texto "Reloj Conectado".
 */
@Preview(name = "Premium (Conectado)", showBackground = true)
@Composable
fun HomeScreenPreview_Premium_Connected() {
    ZenIATheme {
        HomeScreen(
            esPremium = true,
            hasPermission = true,
            healthConnectStatus = 0,
            onSignOut = { },
            onNavigateToAccount = { },
            onConnectSmartwatch = { },
            onNavigateToPremium = { },
            onNavigateToManualPermission = { },
            onInstallHealthConnect = { },
            onSettingsClick = { },
            onNotificationClick = { }
        )
    }
}