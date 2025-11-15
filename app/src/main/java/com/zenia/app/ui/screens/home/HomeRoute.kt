package com.zenia.app.ui.screens.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenia.app.viewmodel.AppViewModelProvider
import com.zenia.app.viewmodel.HomeViewModel

/**
 * Composable "inteligente" (Smart Composable) para la ruta principal (Home).
 * Obtiene el estado de [HomeViewModel], maneja la lógica de Health Connect
 * y pasa el estado y las acciones a [HomeScreen].
 */
@Composable
fun HomeRoute(
    onNavigateToAccount: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onSignOut: () -> Unit
) {
    // 1. Obtiene el ViewModel
    val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)

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
    val isHealthAvailable = homeViewModel.isHealthConnectAvailable

    // 4. Pasa los estados y las lambdas a la HomeScreen "tonta"
    HomeScreen(
        esPremium = esPremium,
        hasPermission = hasPermission,
        isHealthAvailable = isHealthAvailable,
        onSignOut = onSignOut,
        onNavigateToAccount = onNavigateToAccount,
        onConnectSmartwatch = {
            permissionLauncher.launch(homeViewModel.healthConnectPermissions)
        },
        onNavigateToPremium = onNavigateToPremium
    )
}