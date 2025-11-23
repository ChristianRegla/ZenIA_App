package com.zenia.app.ui.screens

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zenia.app.ui.components.ZeniaBottomBar
import com.zenia.app.ui.navigation.BottomNavItem
import com.zenia.app.ui.screens.diary.DiarioScreen
import com.zenia.app.ui.screens.home.HomeRoute
import com.zenia.app.ui.screens.relax.RelajacionScreen
import com.zenia.app.ui.screens.resources.RecursosScreen
import com.zenia.app.ui.screens.zenia.ZeniaBotScreen
import com.zenia.app.ui.theme.ZenIATheme

@Composable
fun MainScreen(
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit
) {
    val bottomNavController = rememberNavController()

    ZenIATheme {
        Scaffold(
            bottomBar = {
                ZeniaBottomBar(navController = bottomNavController)
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
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
                            onNavigateToPremium = { /* TODO */ }
                        )
                    }

                    composable(BottomNavItem.Relajacion.route) {
                        RelajacionScreen()
                    }

                    composable(BottomNavItem.Zenia.route) {
                        ZeniaBotScreen()
                    }

                    composable(BottomNavItem.Diario.route) {
                        DiarioScreen()
                    }

                    composable(BottomNavItem.Recursos.route) {
                        RecursosScreen()
                    }
                }
            }
        }
    }
}