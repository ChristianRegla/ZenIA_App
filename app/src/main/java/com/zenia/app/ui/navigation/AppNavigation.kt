package com.zenia.app.ui.navigation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zenia.app.ui.screens.account.AccountScreen
import com.zenia.app.ui.screens.auth.AuthScreen
import com.zenia.app.ui.screens.home.HomeScreen
import com.zenia.app.ui.screens.lock.LockScreen
import com.zenia.app.viewmodel.AppViewModelProvider
import com.zenia.app.viewmodel.AuthViewModel
import com.zenia.app.viewmodel.HomeViewModel
import com.zenia.app.viewmodel.SettingsViewModel

/**
 * Composable principal que gestiona la navegación de toda la aplicación.
 * Utiliza un [NavHost] para definir todas las rutas (pantallas) posibles.
 *
 * Obtiene los ViewModels de autenticación ([AuthViewModel]) y configuración ([SettingsViewModel])
 * para determinar la pantalla de inicio correcta.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()

    /**
     * Lógica clave para determinar la pantalla de inicio de la app (startDestination).
     * 1. Si el usuario está logueado Y tiene biometría activada -> Va a [Destinations.LOCK_ROUTE].
     * 2. Si está logueado pero SIN biometría -> Va directo a [Destinations.HOME_ROUTE].
     * 3. Si no está logueado (en cualquier otro caso) -> Va a [Destinations.AUTH_ROUTE].
     */
    val startDestination = when {
        isLoggedIn && isBiometricEnabled -> Destinations.LOCK_ROUTE
        isLoggedIn && !isBiometricEnabled -> Destinations.HOME_ROUTE
        else -> Destinations.AUTH_ROUTE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destinations.AUTH_ROUTE) {
            AuthScreen(
                authViewModel = authViewModel
            )
        }
        composable(Destinations.HOME_ROUTE) {
            val homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)

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

            val esPremium by homeViewModel.esPremium.collectAsState()
            val hasPermission by homeViewModel.hasHealthPermissions.collectAsState()
            val isHealthAvailable = homeViewModel.isHealthConnectAvailable

            HomeScreen(
                esPremium = esPremium,
                hasPermission = hasPermission,
                isHealthAvailable = isHealthAvailable,
                onSignOut = {
                    authViewModel.signOut()
                },
                onNavigateToAccount = {
                    navController.navigate(Destinations.ACCOUNT_ROUTE)
                },
                onConnectSmartwatch = {
                    permissionLauncher.launch(homeViewModel.healthConnectPermissions)
                },
                onNavigateToPremium = {
                    // TODO: Navegar a la futura pantalla de suscripción
                    // navController.navigate(Destinations.PREMIUM_ROUTE)
                }
            )
        }
        composable(Destinations.ACCOUNT_ROUTE) {
            AccountScreen(
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAuth = {
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        composable(Destinations.LOCK_ROUTE) {
            LockScreen(
                onUnlockSuccess = {
                    navController.navigate(Destinations.HOME_ROUTE) {
                        popUpTo(Destinations.LOCK_ROUTE) { inclusive = true }
                    }
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.LOCK_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }
}