package com.zenia.app.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zenia.app.ui.screens.account.AccountRoute
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.premium.PremiumRoute
import com.zenia.app.ui.screens.settings.DonationsRoute
import com.zenia.app.ui.screens.settings.HealthSyncRoute
import com.zenia.app.ui.screens.settings.HelpCenterRoute
import com.zenia.app.ui.screens.settings.MoreSettingsRoute
import com.zenia.app.ui.screens.settings.PrivacyRoute
import com.zenia.app.ui.screens.settings.SettingsRoute
import com.zenia.app.viewmodel.MainViewModel
import com.zenia.app.viewmodel.SettingsViewModel

fun NavGraphBuilder.settingsGraph(navController: NavController, mainViewModel: MainViewModel) {
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
            onNavigateToHealthSync = { navController.safeNavigate(Destinations.HEALTH_SYNC_ROUTE) },
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
        route = Destinations.HEALTH_SYNC_ROUTE,
        enterTransition = { slideIn() },
        exitTransition = { slideOut() },
        popEnterTransition = { popSlideIn() },
        popExitTransition = { popSlideOut() }
    ) {
        HealthSyncRoute(
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
                mainViewModel.signOut()
                navController.navigate(Destinations.AUTH_ROUTE) {
                    popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                }
            }
        )
    }
}