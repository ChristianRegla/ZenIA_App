package com.zenia.app.ui.screens.home

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zenia.app.R
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
    isHealthAvailable: Boolean,
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onConnectSmartwatch: () -> Unit,
    onNavigateToPremium: () -> Unit,
) {
    Scaffold { paddingValues ->
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

            if (isHealthAvailable) {
                if (esPremium) {
                    if (!hasPermission) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onConnectSmartwatch) {
                            Text(stringResource(R.string.home_connect_watch))
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
            isHealthAvailable = true,
            onSignOut = { },
            onNavigateToAccount = { },
            onConnectSmartwatch = { },
            onNavigateToPremium = { }
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
            isHealthAvailable = true,
            onSignOut = { },
            onNavigateToAccount = { },
            onConnectSmartwatch = { },
            onNavigateToPremium = { }
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
            isHealthAvailable = true,
            onSignOut = { },
            onNavigateToAccount = { },
            onConnectSmartwatch = { },
            onNavigateToPremium = { }
        )
    }
}