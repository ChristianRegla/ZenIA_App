package com.zenia.app.ui.navigation

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.zenia.app.ui.screens.account.AccountRoute
import com.zenia.app.ui.screens.auth.AuthViewModel
import com.zenia.app.ui.screens.premium.PremiumRoute
import com.zenia.app.ui.screens.settings.DonationsRoute
import com.zenia.app.ui.screens.settings.ExportSettingsRoute
import com.zenia.app.ui.screens.settings.HealthSyncRoute
import com.zenia.app.ui.screens.settings.HelpCenterRoute
import com.zenia.app.ui.screens.settings.MoreSettingsRoute
import com.zenia.app.ui.screens.settings.PrivacyRoute
import com.zenia.app.ui.screens.settings.SettingsRoute
import com.zenia.app.viewmodel.MainViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import com.zenia.app.ui.screens.settings.ChangelogScreen

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
            onNavigateBack = { navController.popBackStack() },
            onNavigateToExport = { navController.safeNavigate(Destinations.EXPORT_SETTINGS_ROUTE) },
            onChangelogClick = { navController.safeNavigate(Destinations.CHANGELOG_ROUTE) }
        )
    }

    composable(
        route = Destinations.CHANGELOG_ROUTE,
        enterTransition = { slideIn() },
        exitTransition = { slideOut() },
        popEnterTransition = { popSlideIn() },
        popExitTransition = { popSlideOut() }
    ) {
        ChangelogScreen(
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
        val context = LocalContext.current
        HealthSyncRoute(
            onNavigateBack = { navController.popBackStack() },
            onInstallOrUpdateHealthConnect = {
                val pkg = "com.google.android.apps.healthdata"
                val uriString = "market://details?id=$pkg&url=healthconnect%3A%2F%2Fonboarding"

                try {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            setPackage("com.android.vending")
                            data = uriString.toUri()
                            putExtra("overlay", true)
                            putExtra("callerId", context.packageName)
                        }
                    )
                } catch (e: Exception) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=$pkg".toUri())
                    )
                }
            },
            onNavigateToPremium = { navController.safeNavigate(Destinations.PREMIUM_ROUTE) },
            onManagePermissionClick = {
                val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
                context.startActivity(intent)
            }
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

    composable(
        route = Destinations.ACCOUNT_ROUTE,
        enterTransition = { slideIn() },
        exitTransition = { slideOut() },
        popEnterTransition = { popSlideIn() },
        popExitTransition = { popSlideOut() }
    ) {
        val authViewModel: AuthViewModel = hiltViewModel()
        AccountRoute(
            authViewModel = authViewModel,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAuth = {
                mainViewModel.signOut()
                navController.navigate(Destinations.AUTH_ROUTE) {
                    popUpTo(Destinations.HOME_ROUTE) { inclusive = true }
                }
            },
            onNavigateToBlockedUsers = { navController.safeNavigate(Destinations.BLOCKED_USERS_ROUTE) }
        )
    }

    composable(
        route = Destinations.BLOCKED_USERS_ROUTE,
        enterTransition = { slideIn() },
        exitTransition = { slideOut() },
        popEnterTransition = { popSlideIn() },
        popExitTransition = { popSlideOut() }
    ) {
        com.zenia.app.ui.screens.account.BlockedUsersRoute(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Destinations.EXPORT_SETTINGS_ROUTE,
        enterTransition = { slideIn() },
        exitTransition = { slideOut() },
        popEnterTransition = { popSlideIn() },
        popExitTransition = { popSlideOut() }
    ) {
        ExportSettingsRoute(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}