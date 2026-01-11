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
import com.zenia.app.ui.screens.analytics.AnalyticsRoute
import com.zenia.app.ui.screens.analytics.AnalyticsViewModel
import com.zenia.app.ui.screens.community.CommunityRoute
import com.zenia.app.ui.screens.diary.DiarioRoute
import com.zenia.app.ui.screens.diary.DiaryEntryScreen
import com.zenia.app.ui.screens.lock.LockRoute
import com.zenia.app.ui.screens.notifications.NotificationsRoute
import com.zenia.app.ui.screens.recursos.RecursosRoute
import com.zenia.app.ui.screens.relax.RelaxRoute
import com.zenia.app.ui.screens.sos.HelplineRoute
import com.zenia.app.ui.screens.zenia.ZeniaBotRoute
import com.zenia.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Composable principal que gestiona la navegación de toda la aplicación.
 * Utiliza un [NavHost] para definir todas las rutas (pantallas) posibles.
 */
@Composable
fun AppNavigation(pendingDeepLink: Uri? = null) {
    val navController = rememberNavController()

    val mainViewModel: MainViewModel = hiltViewModel()
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
        authGraph(navController)

        settingsGraph(navController, mainViewModel)

        composable(
            route = "${Destinations.HOME_ROUTE}?tab={tab}",
            arguments = listOf(
                navArgument("tab") {
                    defaultValue = Destinations.HOME_ROUTE
                    nullable = true
                }
            ),
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
            ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: Destinations.HOME_ROUTE

            MainScreen(
                startTab = tab,
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
                onNavigateToAnalytics = { navController.safeNavigate(Destinations.ANALYTICS_ROUTE) },
                onNavigateToCommunity = { navController.safeNavigate(Destinations.COMMUNITY_ROUTE) }
            )
        }

        composable(Destinations.SOS) {
            HelplineRoute(
                onNavigateToChat = {
                    navController.navigate(Destinations.homeWithTab(BottomNavItem.Zenia.route)) {
                        popUpTo(Destinations.SOS) { inclusive = true }
                    }
                },
                onNavigateToContacts = { },
                onNavigateToExercises = {
                    navController.navigate(Destinations.homeWithTab(BottomNavItem.Relajacion.route)) {
                        popUpTo(Destinations.SOS) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.CHAT_ROUTE) {
            ZeniaBotRoute(
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
            val analyticsViewModel: AnalyticsViewModel = hiltViewModel()
            AnalyticsRoute(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) },
                viewModel = analyticsViewModel
            )
        }

        composable(
            route = Destinations.COMMUNITY_ROUTE,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            CommunityRoute(
                onNavigateBack = { navController.popBackStack() }
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
