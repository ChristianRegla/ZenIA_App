package com.zenia.app.ui.screens.home

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.ui.navigation.Destinations
import java.time.LocalDate

/**
 * Composable "inteligente" (Smart Composable) para la ruta principal (Home).
 * Obtiene el estado de [HomeViewModel], maneja la lógica de Health Connect
 * y pasa el estado y las acciones a [HomeScreen].
 */
@Composable
fun HomeRoute(
    onNavigateToAccount: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToDiaryEntry: (LocalDate) -> Unit
) {
    // 1. Obtiene el ViewModel
    val homeViewModel: HomeViewModel = hiltViewModel()
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
    val uiState by homeViewModel.uiState.collectAsState()
    val registros by homeViewModel.registros.collectAsState()
    val esPremium by homeViewModel.esPremium.collectAsState()
    val hasPermission by homeViewModel.hasHealthPermissions.collectAsState()
    val healthConnectStatus = homeViewModel.healthConnectStatus
    val userName by homeViewModel.userName.collectAsState()
    val hasEntryToday by homeViewModel.hasEntryToday.collectAsState()
    val communityActivities by homeViewModel.communityActivities.collectAsState()

    LaunchedEffect(registros) {
        homeViewModel.processChartData(registros)
    }

    // 4. Pasa los estados y las lambdas a la HomeScreen "tonta"
    HomeScreen(
        uiState = uiState,
        userName = userName,
        registrosRecientes = registros,
        hasEntryToday = hasEntryToday,
        communityActivities = communityActivities,
        chartProducer = homeViewModel.chartProducer,
        onNavigateToDiaryEntry = onNavigateToDiaryEntry,
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
        },
        onSettingsClick = onNavigateToSettings,
        onNotificationClick = onNotificationClick,
        onResetState = { homeViewModel.resetState() },
        onNavigateToSOS = onNavigateToSOS,
    )
}