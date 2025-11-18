package com.zenia.app.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenia.app.viewmodel.AppViewModelProvider
import com.zenia.app.viewmodel.HomeViewModel
import androidx.core.net.toUri

/**
 * Composable "inteligente" (Smart Composable) para la ruta principal (Home).
 * Obtiene el estado de [HomeViewModel], maneja la lógica de Health Connect
 * y pasa el estado y las acciones a [HomeScreen].
 */
@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun HomeRoute(
    onNavigateToAccount: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onSignOut: () -> Unit
) {
    // 1. Obtiene el ViewModel
    val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val context = LocalContext.current

    // 2. Define el Launcher de Permisos
    val dummyContract = object : ActivityResultContract<Set<String>, Set<String>>() {
        override fun createIntent(context: android.content.Context, input: Set<String>) = Intent()
        override fun parseResult(resultCode: Int, intent: Intent?) = emptySet<String>()
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

    // 3. Recolecta todos los estados necesarios
    val esPremium by homeViewModel.esPremium.collectAsState()
    val hasPermission by homeViewModel.hasHealthPermissions.collectAsState()
    val healthConnectStatus = homeViewModel.healthConnectStatus

    // 4. Pasa los estados y las lambdas a la HomeScreen "tonta"
    HomeScreen(
        esPremium = esPremium,
        hasPermission = hasPermission,
        healthConnectStatus = healthConnectStatus,
        onSignOut = onSignOut,
        onNavigateToAccount = onNavigateToAccount,
        onConnectSmartwatch = {
            permissionLauncher.launch(homeViewModel.healthConnectPermissions)
        },
        onNavigateToPremium = onNavigateToPremium,
        onNavigateToManualPermission = {
            val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)

            val oldPackage = "com.google.android.apps.healthdata"
            val newPackage = "com.google.android.healthconnect.controller"

            try {
                intent.setPackage(oldPackage)
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.w("HomeRoute", "No se encontró el paquete antiguo de Health Connect. Intentando con el nuevo...")
                try {
                    intent.setPackage(newPackage)
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    Log.e("HomeRoute", "No se pudo abrir Health Connect con ninguno de los paquetes.", e2)
                }
            }
        },
        onInstallHealthConnect = {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "market://details?id=com.google.android.apps.healthdata".toUri()
                    setPackage("com.android.vending")
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri())
                context.startActivity(webIntent)
            }
        }
    )
}