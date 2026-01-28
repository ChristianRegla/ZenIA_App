package com.zenia.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zenia.app.ui.components.ZeniaBottomBar
import com.zenia.app.ui.navigation.BottomNavItem
import com.zenia.app.ui.navigation.Destinations
import com.zenia.app.ui.screens.diary.DiarioRoute
import com.zenia.app.ui.screens.home.HomeRoute
import com.zenia.app.ui.screens.recursos.RecursosRoute
import com.zenia.app.ui.screens.relax.RelaxRoute
import com.zenia.app.ui.screens.zenia.ZeniaBotRoute
import com.zenia.app.ui.theme.ZenIATheme
import java.time.LocalDate

/**
 * Pantalla principal de la aplicación que aloja la navegación inferior y las diferentes
 * secciones de la app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startTab: String? = null,
    onSignOut: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigateToDiaryEntry: (LocalDate) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCommunity: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Estado para saber si en la pantalla de diario estamos en una fecha, para ocultar la barra inferior pues
    var isDiaryEntryActive by remember { mutableStateOf(false) }

    val bottomBarRoutes = listOf(
        Destinations.HOME_ROUTE,
        Destinations.RELAX_ROUTE,
        Destinations.DIARY_ROUTE,
        Destinations.RECURSOS_ROUTE,
    )

    // La barra se muestra si la ruta es de las principales y pues lo del diario de arriba
    val showBottomBar = (currentRoute in bottomBarRoutes) && !isDiaryEntryActive

    LaunchedEffect(currentRoute) {
        if (currentRoute != Destinations.DIARY_ROUTE) {
            isDiaryEntryActive = false
        }
    }

    val items = listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Relajacion,
        BottomNavItem.Zenia,
        BottomNavItem.Diario,
        BottomNavItem.Recursos
    )
    
    LaunchedEffect(startTab) {
        if (startTab != null && startTab != BottomNavItem.Inicio.route) {
            bottomNavController.navigate(startTab) {
                popUpTo(BottomNavItem.Inicio.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ZenIATheme {
        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    ZeniaBottomBar(navController = bottomNavController, items = items)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp,
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
                            onNavigateToPremium = onNavigateToPremium,
                            onNavigateToSettings = onNavigateToSettings,
                            onNotificationClick = onNotificationClick,
                            onNavigateToSOS = onNavigateToSOS,
                            onNavigateToDiaryEntry = onNavigateToDiaryEntry,
                            onNavigateToAnalytics = onNavigateToAnalytics,
                            onNavigateToCommunity = onNavigateToCommunity
                        )
                    }

                    composable(BottomNavItem.Relajacion.route) {
                        RelaxRoute(
                            onNavigateToPlayer = { /* TODO */ },
                            onNavigateToPremium = onNavigateToPremium
                        )
                    }

                    composable(BottomNavItem.Zenia.route) {
                        ZeniaBotRoute(
                            onNavigateBack = { bottomNavController.popBackStack() }
                        )
                    }

                    composable(BottomNavItem.Diario.route) {
                        DiarioRoute(
                            onToggleBottomBar = { isVisible ->
                                isDiaryEntryActive = !isVisible
                            }
                        )
                    }

                    composable(BottomNavItem.Recursos.route) {
                        RecursosRoute(
                            onNavigateToDetail = { /* TODO */ },
                            onNavigateToPremium = onNavigateToPremium
                        )
                    }
                }
            }
        }
    }
}