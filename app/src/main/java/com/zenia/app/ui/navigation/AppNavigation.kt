package com.zenia.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zenia.app.model.CommunityPost
import com.zenia.app.ui.screens.MainScreen
import com.zenia.app.ui.screens.analytics.AnalyticsRoute
import com.zenia.app.ui.screens.analytics.AnalyticsViewModel
import com.zenia.app.ui.screens.community.CommunityRoute
import com.zenia.app.ui.screens.diary.DiarioRoute
import com.zenia.app.ui.screens.diary.DiaryEntryScreen
import com.zenia.app.ui.screens.evaluacion.EvaluacionRoute
import com.zenia.app.ui.screens.lock.LockRoute
import com.zenia.app.ui.screens.notifications.NotificationsRoute
import com.zenia.app.ui.screens.recursos.RecursoDetailRoute
import com.zenia.app.ui.screens.recursos.RecursosRoute
import com.zenia.app.ui.screens.relax.BalloonRoute
import com.zenia.app.ui.screens.relax.BodyScanRoute
import com.zenia.app.ui.screens.relax.BreathingRoute
import com.zenia.app.ui.screens.relax.GroundingRoute
import com.zenia.app.ui.screens.relax.RelaxRoute
import com.zenia.app.ui.screens.sos.HelplineRoute
import com.zenia.app.ui.screens.zenia.ZeniaBotRoute
import com.zenia.app.viewmodel.MainViewModel
import java.time.LocalDate

/**
 * Composable principal que gestiona la navegación de toda la aplicación.
 * Básicamente el mero mero de la navegación, de aquí se divide el resto.
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

    LaunchedEffect(startDestination) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        if (startDestination == Destinations.AUTH_ROUTE && currentRoute != Destinations.AUTH_ROUTE) {
            navController.navigate(Destinations.AUTH_ROUTE) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingDeepLink, startDestination) {
        if (pendingDeepLink != null && startDestination != null) {

            val targetRoute = when (pendingDeepLink.toString()) {
                "zenia://diary/new" -> {
                    val today = LocalDate.now()
                    Destinations.createDiaryEntryRoute(today)
                }
                "zenia://sos" -> Destinations.SOS
                else -> null
            }

            targetRoute?.let { route ->
                if (startDestination == Destinations.LOCK_ROUTE) {
                    mainViewModel.setPendingRoute(route)

                    navController.navigate(Destinations.LOCK_ROUTE) {
                        popUpTo(0)
                    }
                } else {
                    navController.navigate(route)
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
                onNavigateToSettings = { navController.safeNavigate(Destinations.SETTINGS_ROUTE) },
                onNotificationClick = { navController.safeNavigate(Destinations.NOTIFICATIONS_ROUTE) },
                onNavigateToSOS = { navController.safeNavigate(Destinations.SOS) },
                onNavigateToDiaryEntry = { date ->
                    navController.safeNavigate(Destinations.createDiaryEntryRoute(date))
                },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) },
                onNavigateToAnalytics = { navController.safeNavigate(Destinations.ANALYTICS_ROUTE) },
                onNavigateToCommunity = { navController.safeNavigate(Destinations.COMMUNITY_ROUTE) },
                onNavigateToRecursoDetail = { recursoId ->
                    navController.safeNavigate(Destinations.createRecursoDetailRoute(recursoId))
                },
                onNavigateToPlayer = { id ->
                    navController.safeNavigate(Destinations.createPlayerRoute(id))
                },
                onNavigateToPostDetail = { post ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("mainPost", post)
                    navController.safeNavigate(Destinations.createPostDetailRoute(post.id))
                }
            )
        }

        composable(
            route = Destinations.SOS,
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            HelplineRoute(
                onNavigateToChat = {
                    navController.navigate(Destinations.homeWithTab(BottomNavItem.Zenia.route)) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToContacts = { },
                onNavigateToExercises = {
                    navController.navigate(Destinations.homeWithTab(BottomNavItem.Relajacion.route)) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
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
                onNavigateToPlayer = { id ->
                    navController.safeNavigate(Destinations.createPlayerRoute(id))
                },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) }
            )
        }

        composable(Destinations.RECURSOS_ROUTE) {
            RecursosRoute(
                onNavigateToDetail = { /* TODO: Detail */ },
                onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) }
            )
        }

        composable(
            route = Destinations.EVALUACION_ROUTE,
            arguments = listOf(navArgument(NavArgs.TIPO_TEST_ID) { type = NavType.StringType }),
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) { backStackEntry ->
            val tipoTestId = backStackEntry.arguments?.getString(NavArgs.TIPO_TEST_ID) ?: return@composable

            EvaluacionRoute(
                tipoTestId = tipoTestId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Destinations.LOCK_ROUTE) {
            LockRoute(
                onUnlockSuccess = {
                    val pendingRoute = mainViewModel.consumePendingRoute()

                    navController.navigate(
                        pendingRoute ?: Destinations.HOME_ROUTE
                    ) {
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPostDetail = { post ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("mainPost", post)
                    navController.safeNavigate(Destinations.createPostDetailRoute(post.id))
                }
            )
        }

        composable(
            route = Destinations.POST_DETAIL_ROUTE,
            arguments = listOf(navArgument(NavArgs.POST_ID) { type = NavType.StringType }),
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) {
            val mainPost = remember {
                navController.previousBackStackEntry?.savedStateHandle?.get<CommunityPost>("mainPost")
            }

            if (mainPost != null) {
                com.zenia.app.ui.screens.community.PostDetailRoute(
                    mainPost = mainPost,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }

        composable(
            route = Destinations.RECURSO_DETAIL_ROUTE,
            arguments = listOf(navArgument(NavArgs.RECURSO_ID) { type = NavType.StringType }),
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) { backStackEntry ->
            val recursoId = backStackEntry.arguments?.getString(NavArgs.RECURSO_ID) ?: return@composable

            RecursoDetailRoute(
                recursoId = recursoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.PLAYER_ROUTE,
            arguments = listOf(navArgument("exerciseId") { type = NavType.IntType }),
            enterTransition = { slideIn() },
            exitTransition = { slideOut() },
            popEnterTransition = { popSlideIn() },
            popExitTransition = { popSlideOut() }
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getInt("exerciseId") ?: 1

            when (exerciseId) {
                1 -> {
                    BreathingRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                2 -> {
                    GroundingRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                3 -> {
                    BalloonRoute(onNavigateBack = { navController.popBackStack() })
                }
                4 -> {
                    BodyScanRoute(onNavigateBack = { navController.popBackStack() })
                }
                else -> {
                    BreathingRoute(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}