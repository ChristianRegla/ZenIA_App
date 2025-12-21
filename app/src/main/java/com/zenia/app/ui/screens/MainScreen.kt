package com.zenia.app.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zenia.app.ui.components.ZeniaBottomBar
import com.zenia.app.ui.navigation.BottomNavItem
import com.zenia.app.ui.screens.diary.DiarioRoute
import com.zenia.app.ui.screens.home.HomeRoute
import com.zenia.app.ui.screens.recursos.RecursosRoute
import com.zenia.app.ui.screens.relax.RelajacionScreen
import com.zenia.app.ui.screens.zenia.ZeniaBotRoute
import com.zenia.app.ui.theme.ZenIATheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToDiaryEntry: (LocalDate) -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isChatScreen = currentRoute == BottomNavItem.Zenia.route

    ZenIATheme {
        Scaffold(
            bottomBar = {
                if (!isChatScreen) {
                    ZeniaBottomBar(navController = bottomNavController)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = innerPadding.calculateBottomPadding(),
                        top = 0.dp
                    )
            ) {
                NavHost(
                    navController = bottomNavController,
                    startDestination = BottomNavItem.Inicio.route,
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() },
                    popEnterTransition = { fadeIn() },
                    popExitTransition = { fadeOut() }
                ) {
                    composable(BottomNavItem.Inicio.route) {
                        HomeRoute(
                            onSignOut = onSignOut,
                            onNavigateToAccount = onNavigateToAccount,
                            onNavigateToPremium = { /* TODO */ },
                            onNavigateToSettings = onNavigateToSettings,
                            onNotificationClick = onNotificationClick,
                            onNavigateToSOS = onNavigateToSOS,
                        )
                    }

                    composable(BottomNavItem.Relajacion.route) {
                        RelajacionScreen()
                    }

                    composable(BottomNavItem.Zenia.route) {
                        ZeniaBotRoute(
                            onNavigateBack = { bottomNavController.popBackStack() }
                        )
                    }

                    composable(BottomNavItem.Diario.route) {
                        DiarioRoute()
                    }

                    composable(BottomNavItem.Recursos.route) {
                        RecursosRoute()
                    }
                }
            }
        }
    }
}