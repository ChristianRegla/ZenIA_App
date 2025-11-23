package com.zenia.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.zenia.app.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Inicio : BottomNavItem(
        route = "home_tab",
        titleRes = R.string.nav_home,
        icon = Icons.Default.Home
    )
    object Relajacion : BottomNavItem(
        route = "relax_tab",
        titleRes = R.string.nav_relax,
        icon = Icons.Default.Favorite
    )
    object Zenia : BottomNavItem(
        route = "zenia_bot_tab",
        titleRes = R.string.nav_bot,
        icon = Icons.AutoMirrored.Filled.Chat
    )
    object Diario : BottomNavItem(
        route = "diary_tab",
        titleRes = R.string.nav_diary,
        icon = Icons.Default.DateRange
    )
    object Recursos : BottomNavItem(
        route = "resources_tab",
        titleRes = R.string.nav_resources,
        icon = Icons.Default.Book
    )
}