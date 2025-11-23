package com.zenia.app.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zenia.app.ui.navigation.BottomNavItem

@Composable
fun ZeniaBottomBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Inicio,
        BottomNavItem.Relajacion,
        BottomNavItem.Zenia,
        BottomNavItem.Diario,
        BottomNavItem.Recursos
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(id = item.titleRes)
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = item.titleRes),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        style = if (isSelected) {
                            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.labelSmall
                        }
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}