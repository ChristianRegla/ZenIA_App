package com.zenia.app.ui.navigation

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
import com.zenia.app.viewmodel.AuthViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val isLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()

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
            HomeScreen(
                onSignOut = {
                    authViewModel.signOut()
                },
                onNavigateToAccount = {
                    navController.navigate(Destinations.ACCOUNT_ROUTE)
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