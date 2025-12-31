package com.zenia.app.ui.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zenia.app.ui.screens.MainScreen
import com.zenia.app.ui.screens.account.AccountRoute
import com.zenia.app.ui.screens.analytics.AnalyticsRoute
import com.zenia.app.ui.screens.auth.AuthRoute
import com.zenia.app.ui.screens.lock.LockRoute
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.auth.ForgotPasswordScreen
import com.zenia.app.ui.screens.diary.DiarioRoute
import com.zenia.app.ui.screens.diary.DiaryEntryScreen
import com.zenia.app.ui.screens.notifications.NotificationsRoute
import com.zenia.app.ui.screens.premium.PremiumRoute
import com.zenia.app.ui.screens.recursos.RecursosRoute
import com.zenia.app.ui.screens.relax.RelaxRoute
import com.zenia.app.ui.screens.settings.DonationsRoute
import com.zenia.app.ui.screens.settings.HelpCenterRoute
import com.zenia.app.ui.screens.settings.MoreSettingsRoute
import com.zenia.app.ui.screens.settings.PrivacyRoute
import com.zenia.app.ui.screens.settings.SettingsRoute
import com.zenia.app.ui.screens.sos.HelplineRoute
import com.zenia.app.viewmodel.MainViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Composable principal que gestiona la navegación de toda la aplicación.
 * Utiliza un [NavHost] para definir todas las rutas (pantallas) posibles.
 *
 * Obtiene los ViewModels de autenticación ([AuthViewModel]) y configuración ([SettingsViewModel])
 * para determinar la pantalla de inicio correcta.
 */
@Composable
fun AppNavigation(pendingDeepLink: Uri? = null) {
    val navController = rememberNavController()

    val mainViewModel: MainViewModel = hiltViewModel()
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

    LaunchedEffect(pendingDeepLink, startDestination) {
        if (pendingDeepLink != null) {
            delay(100)

            when (pendingDeepLink.toString()) {
                "zenia://diary/new" -> {
                    val today = LocalDate.now()
                    navController.navigate(Destinations.createDiaryEntryRoute(today))
                }
                "zenia://sos" -> {
                    navController.navigate(Destinations.SOS)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Destinations.AUTH_ROUTE) {
            val authViewModel: AuthViewModel = hiltViewModel()
            AuthRoute(
                authViewModel = authViewModel,
                onNavigateToForgotPassword = {
                    navController.navigate(Destinations.FORGOT_PASSWORD_ROUTE)
                }
            )
        }

        composable(Destinations.FORGOT_PASSWORD_ROUTE) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ForgotPasswordScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Destinations.HOME_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
            ) {
            MainScreen(
                onSignOut = {
                    mainViewModel.signOut()
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToAccount = { navController.safeNavigate(Destinations.ACCOUNT_ROUTE) },
                onNavigateToSettings = { navController.safeNavigate(Destinations.SETTINGS_ROUTE) },
                onNotificationClick = { navController.safeNavigate(Destinations.NOTIFICATIONS_ROUTE) },
                onNavigateToSOS = { navController.safeNavigate(Destinations.SOS) },
                onNavigateToDiaryEntry = { date ->
                    navController.safeNavigate(Destinations.createDiaryEntryRoute(date))
                },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) },
                onNavigateToAnalytics = { navController.safeNavigate(Destinations.ANALYTICS_ROUTE) }
            )
        }

        composable(Destinations.SOS) {
            HelplineRoute(
                onNavigateBack = { navController.popBackStack() }
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

        composable(
            route = Destinations.NOTIFICATIONS_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            NotificationsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDestination = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = Destinations.SETTINGS_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            val settingsVM: SettingsViewModel = hiltViewModel()
            SettingsRoute(
                settingsViewModel = settingsVM,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.safeNavigate(Destinations.ACCOUNT_ROUTE) },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) },
                onNavigateToMoreSettings = { navController.safeNavigate(Destinations.MORE_SETTINGS_ROUTE) },
                onNavigateToHelp = { navController.safeNavigate(Destinations.HELP_CENTER_ROUTE) },
                onNavigateToDonations = { navController.safeNavigate(Destinations.DONATIONS_ROUTE) },
                onNavigateToPrivacy = { navController.safeNavigate(Destinations.PRIVACY_POLICY_ROUTE) },
                onSignOut = {
                    mainViewModel.signOut()
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Destinations.MORE_SETTINGS_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            MoreSettingsRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.PREMIUM_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            PremiumRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.HELP_CENTER_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
            ) {
            HelpCenterRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.DONATIONS_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
            ) {
            DonationsRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.PRIVACY_POLICY_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
            ) {
            PrivacyRoute(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.ACCOUNT_ROUTE) {
            val authViewModel: AuthViewModel = hiltViewModel()
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

        composable(Destinations.RELAX_ROUTE) {
            RelaxRoute(
                onNavigateToPlayer = { /* TODO: Player */ },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) }
            )
        }

        composable(Destinations.RECURSOS_ROUTE) {
            RecursosRoute(
                onNavigateToDetail = { /* TODO: Detail */ },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) }
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
                    mainViewModel.signOut()
                    navController.navigate(Destinations.AUTH_ROUTE) {
                        popUpTo(Destinations.LOCK_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Destinations.ANALYTICS_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            AnalyticsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) },
                isPremium = true
            )
        }
    }
}

fun AnimatedContentTransitionScope<*>.slideIn() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))

fun AnimatedContentTransitionScope<*>.slideOut() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))

fun AnimatedContentTransitionScope<*>.popSlideIn() =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))

fun AnimatedContentTransitionScope<*>.popSlideOut() =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))

/**
 * FUNCIÓN DE EXTENSIÓN PARA EVITAR DOBLE CLIC / NAVEGACIÓN MÚLTIPLE
 * Solo navega si la pantalla actual está en estado RESUMED (Activa y lista).
 * Si ya se inició otra navegación, el estado cambia y esto evita el segundo clic.
 */
fun androidx.navigation.NavController.safeNavigate(route: String) {
    val lifecycle = this.currentBackStackEntry?.lifecycle
    if (lifecycle != null && lifecycle.currentState == Lifecycle.State.RESUMED) {
        this.navigate(route) {
            launchSingleTop = true
        }
    }
}