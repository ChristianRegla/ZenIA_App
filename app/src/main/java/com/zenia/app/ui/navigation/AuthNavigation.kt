package com.zenia.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zenia.app.ui.screens.auth.AuthRoute
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.auth.ForgotPasswordScreen
import com.zenia.app.ui.screens.onboarding.OnboardingRoute

/**
 * Gráfico de navegación anidado para las pantallas de autenticación.
 */
fun NavGraphBuilder.authGraph(navController: NavController) {
    composable(
        route = Destinations.AUTH_ROUTE,
        enterTransition = {
            if (initialState.destination.route == Destinations.ONBOARDING_ROUTE) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500)
                ) + fadeIn(animationSpec = tween(500))
            } else {
                null
            }
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500))
        }
    ) {
        val authViewModel: AuthViewModel = hiltViewModel()
        AuthRoute(
            authViewModel = authViewModel,
            onNavigateToForgotPassword = {
                navController.navigate(Destinations.FORGOT_PASSWORD_ROUTE)
            }
        )
    }

    composable(
        route = Destinations.FORGOT_PASSWORD_ROUTE,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(500)
            ) + fadeIn(animationSpec = tween(500))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(500)
            ) + fadeOut(animationSpec = tween(500))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(500)
            ) + fadeOut(animationSpec = tween(500))
        }
    ) {
        val authViewModel: AuthViewModel = hiltViewModel()
        ForgotPasswordScreen(
            viewModel = authViewModel,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    }

    composable(
        route = Destinations.ONBOARDING_ROUTE,
        exitTransition = {
            if (targetState.destination.route == Destinations.AUTH_ROUTE) {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            } else {
                null
            }
        }
    ) {
        OnboardingRoute(
            onNavigateToAuth = {
                navController.navigate(Destinations.AUTH_ROUTE) {
                    popUpTo(Destinations.ONBOARDING_ROUTE) { inclusive = true }
                }
            }
        )
    }
}