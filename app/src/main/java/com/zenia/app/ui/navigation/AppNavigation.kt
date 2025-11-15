package com.zenia.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zenia.app.ui.screens.account.AccountRoute
import com.zenia.app.ui.screens.auth.AuthRoute
import com.zenia.app.ui.screens.home.HomeRoute
import com.zenia.app.ui.screens.lock.LockRoute
import com.zenia.app.viewmodel.AppViewModelProvider
import com.zenia.app.viewmodel.AuthViewModel
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
        /**
         * Pantalla de Autenticación (Login / Registro).
         * La lógica de esta pantalla está contenida en [AuthRoute].
         */
        composable(Destinations.AUTH_ROUTE) {
            AuthRoute(authViewModel = authViewModel)
        }

        /**
         * Pantalla Principal (Home).
         * La lógica de esta pantalla está contenida en [HomeRoute].
         */
        composable(Destinations.HOME_ROUTE) {
            HomeRoute(
                onSignOut = { authViewModel.signOut() },
                onNavigateToAccount = { navController.navigate(Destinations.ACCOUNT_ROUTE) },
                onNavigateToPremium = {
                    // TODO: Navegar a la futura pantalla de suscripción
                    // navController.navigate(Destinations.PREMIUM_ROUTE)
                }
            )
        }

        /**
         * Pantalla de Configuración de Cuenta.
         * La lógica de esta pantalla está contenida en [AccountRoute].
         */
        composable(Destinations.ACCOUNT_ROUTE) {
            AccountRoute(
                authViewModel = authViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAuth = {
                    // Navega a Auth y limpia todo el historial de atrás.
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        /**
         * Pantalla de Bloqueo Biométrico.
         * La lógica de esta pantalla está contenida en [LockRoute].
         */
        composable(Destinations.LOCK_ROUTE) {
            LockRoute(
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