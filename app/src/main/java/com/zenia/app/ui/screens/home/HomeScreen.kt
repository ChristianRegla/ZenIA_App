package com.zenia.app.ui.screens.home

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenia.app.viewmodel.AppViewModelProvider
import com.zenia.app.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val dummyContract = object : androidx.activity.result.contract.ActivityResultContract<Set<String>, Set<String>>() {
        override fun createIntent(context: android.content.Context, input: Set<String>) = android.content.Intent()
        override fun parseResult(resultCode: Int, intent: android.content.Intent?) = emptySet<String>()
    }

    val realContract = homeViewModel.permissionRequestContract
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = realContract ?: dummyContract,
        onResult = { grantedPermissions ->
            if (grantedPermissions.isNotEmpty()) {
                homeViewModel.checkHealthPermissions()
            }
        }
    )

    val hasPermission by homeViewModel.hasHealthPermissions.collectAsState()
    val isHealthAvailable = homeViewModel.isHealthConnectAvailable
    val esPremium by homeViewModel.esPremium.collectAsState()

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
                text = "¡Bienvenido a ZenIA!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isHealthAvailable) {
                if (esPremium) {
                    if (!hasPermission) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            permissionLauncher.launch(homeViewModel.healthConnectPermissions)
                        }) {
                            Text("Conectar Smartwatch") // (TODO: Mover a strings.xml)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reloj Conectado ✔\uFE0F") // (TODO: Mover a strings.xml)
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* TODO: Navegar a pantalla de suscripción */ }) {
                        Text("Conectar Smartwatch (Función Premium)")
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNavigateToAccount) {
                Text("Mi Cuenta")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onSignOut) {
                Text("Cerrar Sesión")
            }
        }
    }
}