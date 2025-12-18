package com.zenia.app.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zenia.app.ui.screens.MainScreen
import com.zenia.app.ui.screens.account.AccountRoute
import com.zenia.app.ui.screens.auth.AuthRoute
import com.zenia.app.ui.screens.lock.LockRoute
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.auth.ForgotPasswordScreen
import com.zenia.app.ui.screens.diary.DiarioRoute
import com.zenia.app.ui.screens.diary.DiaryEntryScreen
import com.zenia.app.ui.screens.notifications.NotificationsRoute
import com.zenia.app.ui.screens.premium.PremiumRoute
import com.zenia.app.ui.screens.settings.DonationsRoute
import com.zenia.app.ui.screens.settings.HelpCenterRoute
import com.zenia.app.ui.screens.settings.MoreSettingsRoute
import com.zenia.app.ui.screens.settings.PrivacyRoute
import com.zenia.app.ui.screens.settings.SettingsRoute
import com.zenia.app.viewmodel.MainViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import java.time.LocalDate

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

    val mainViewModel: MainViewModel = hiltViewModel()

    val authViewModel: AuthViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    /**
     * Lógica clave para determinar la pantalla de inicio de la app (startDestination).
     * 1. Si el usuario está logueado Y tiene biometría activada -> Va a [Destinations.LOCK_ROUTE].
     * 2. Si está logueado pero SIN biometría -> Va directo a [Destinations.HOME_ROUTE].
     * 3. Si no está logueado (en cualquier otro caso) -> Va a [Destinations.AUTH_ROUTE].
     */
    val startDestination by mainViewModel.startDestinationState.collectAsState()
    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
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
                    authViewModel.resetForgotPasswordState()
                    navController.popBackStack()
                }
            )
        }

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
                },
                onNotificationClick = {
                    navController.navigate(Destinations.NOTIFICATIONS_ROUTE)
                },
                onNavigateToDiaryEntry = { date ->
                    navController.navigate(Destinations.createDiaryEntryRoute(date))
                }
            )
        }

        composable(Destinations.DIARY_ROUTE) {
            DiarioRoute()
        }

        composable(
            route = Destinations.DIARY_ENTRY_ROUTE,
            arguments = listOf(navArgument(NavArgs.DATE) { type = NavType.StringType })
        ) { backStackEntry ->
            val dateString = backStackEntry.arguments?.getString(NavArgs.DATE)
            val date = LocalDate.parse(dateString)

            DiaryEntryScreen(
                date = date,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.NOTIFICATIONS_ROUTE) {
            NotificationsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDestination = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = Destinations.SETTINGS_ROUTE,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
        ) {
            SettingsRoute(
                settingsViewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = {
                    navController.navigate(Destinations.ACCOUNT_ROUTE)
                },
                onNavigateToPremium = {
                    navController.navigate(Destinations.PREMIUM_ROUTE)
                },
                onNavigateToMoreSettings = {
                    navController.navigate(Destinations.MORE_SETTINGS_ROUTE)
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

        composable(Destinations.MORE_SETTINGS_ROUTE) {
            MoreSettingsRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.PREMIUM_ROUTE) {
            PremiumRoute(
                onNavigateBack = { navController.popBackStack() }
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

        composable(Destinations.ACCOUNT_ROUTE) {
            AccountRoute(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAuth = {
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                }
            )
        }

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