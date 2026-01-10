package com.zenia.app.ui.navigation

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

    composable(Destinations.ONBOARDING_ROUTE) {
        OnboardingRoute(
            onNavigateToAuth = {
                navController.navigate(Destinations.AUTH_ROUTE) {
                    popUpTo(Destinations.ONBOARDING_ROUTE) { inclusive = true }
                }
            }
        )
    }
}