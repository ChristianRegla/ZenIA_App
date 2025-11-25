package com.zenia.app.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zenia.app.ui.components.ZeniaBottomBar
import com.zenia.app.ui.navigation.BottomNavItem
import com.zenia.app.ui.screens.diary.DiarioRoute
import com.zenia.app.ui.screens.diary.DiarioScreen
import com.zenia.app.ui.screens.home.HomeRoute
import com.zenia.app.ui.screens.recursos.RecursosRoute
import com.zenia.app.ui.screens.relax.RelajacionScreen
import com.zenia.app.ui.screens.zenia.ZeniaBotRoute
import com.zenia.app.ui.theme.ZenIATheme
import java.time.LocalDate

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToDiaryEntry: (LocalDate) -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isChatScreen = currentRoute == BottomNavItem.Zenia.route

    ZenIATheme {
        Scaffold(
            bottomBar = {
                ZeniaBottomBar(navController = bottomNavController)
            }
        ) { innerPadding ->
            val density = LocalDensity.current
            val bottomBarHeight = innerPadding.calculateBottomPadding()
            val imeHeight = WindowInsets.ime.getBottom(density)

            val imeHeightDp = with(density) { imeHeight.toDp() }

            val bottomPadding = if (isChatScreen) {
                max(0.dp, bottomBarHeight - imeHeightDp)
            } else {
                bottomBarHeight
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomPadding)
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
                            onNotificationClick = onNotificationClick
                        )
                    }

                    composable(BottomNavItem.Relajacion.route) {
                        RelajacionScreen()
                    }

                    composable(BottomNavItem.Zenia.route) {
                        ZeniaBotRoute()
                    }

                    composable(BottomNavItem.Diario.route) {
                        DiarioRoute(
                            onNavigateToEntry = onNavigateToDiaryEntry
                        )
                    }

                    composable(BottomNavItem.Recursos.route) {
                        RecursosRoute()
                    }
                }
            }
        }
    }
}