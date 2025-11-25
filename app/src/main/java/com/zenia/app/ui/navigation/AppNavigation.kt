package com.zenia.app.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zenia.app.ui.screens.MainScreen
import com.zenia.app.ui.screens.account.AccountRoute
import com.zenia.app.ui.screens.auth.AuthRoute
import com.zenia.app.ui.screens.home.HomeRoute
import com.zenia.app.ui.screens.lock.LockRoute
import com.zenia.app.viewmodel.AppViewModelProvider
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.auth.ForgotPasswordScreen
import com.zenia.app.ui.screens.settings.DonationsRoute
import com.zenia.app.ui.screens.settings.HelpCenterRoute
import com.zenia.app.ui.screens.settings.PrivacyRoute
import com.zenia.app.ui.screens.settings.SettingsRoute
import com.zenia.app.viewmodel.SettingsViewModel

/**
 * Composable principal que gestiona la navegación de toda la aplicación.
 * Utiliza un [NavHost] para definir todas las rutas (pantallas) posibles.
 *
 * Obtiene los ViewModels de autenticación ([AuthViewModel]) y configuración ([SettingsViewModel])
 * para determinar la pantalla de inicio correcta.
 */
@RequiresApi(Build.VERSION_CODES.P)
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
            AuthRoute(
                authViewModel = authViewModel,
                onNavigateToForgotPassword = {
                    navController.navigate(Destinations.FORGOT_PASSWORD_ROUTE)
                }
            )
        }

        composable(Destinations.FORGOT_PASSWORD_ROUTE) {
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    authViewModel.resetForgotPasswordState() // Limpiamos al salir
                    navController.popBackStack()
                }
            )
        }

        /**
         * Pantalla Principal (Home).
         * La lógica de esta pantalla está contenida en [HomeRoute].
         */
        composable(Destinations.HOME_ROUTE) {
            MainScreen(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToAccount = {
                    navController.navigate(Destinations.ACCOUNT_ROUTE)
                },
                onNavigateToSettings = {
                    navController.navigate(Destinations.SETTINGS_ROUTE)
                }
            )
        }

        composable(Destinations.SETTINGS_ROUTE) {
            // Instanciamos el ViewModel (asumiendo que tienes el Factory configurado)
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = AppViewModelProvider.Factory
            )

            SettingsRoute(
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = {
                    navController.navigate(Destinations.ACCOUNT_ROUTE)
                },
                onNavigateToPremium = {

                },
                onNavigateToHelp = {
                    navController.navigate(Destinations.HELP_CENTER_ROUTE)
                },
                onNavigateToDonations = {
                    navController.navigate(Destinations.DONATIONS_ROUTE)
                },
                onNavigateToPrivacy = {
                    navController.navigate(Destinations.PRIVACY_POLICY_ROUTE)
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                },
            )
        }

        composable(Destinations.HELP_CENTER_ROUTE) {
            HelpCenterRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.DONATIONS_ROUTE) {
            DonationsRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.PRIVACY_POLICY_ROUTE) {
            PrivacyRoute(
                onNavigateBack = { navController.popBackStack() }
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