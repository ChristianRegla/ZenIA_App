package com.zenia.app.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.zenia.app.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int
) {
    object Inicio : BottomNavItem(
        route = "home_tab",
        titleRes = R.string.nav_home,
        iconRes = R.drawable.ic_home
    )
    object Relajacion : BottomNavItem(
        route = "relax_tab",
        titleRes = R.string.nav_relax,
        iconRes = R.drawable.ic_relax
    )
    object Zenia : BottomNavItem(
        route = "zenia_bot_tab",
        titleRes = R.string.nav_bot,
        iconRes = R.drawable.ic_chat
    )
    object Diario : BottomNavItem(
        route = "diary_tab",
        titleRes = R.string.nav_diary,
        iconRes = R.drawable.ic_journal
    )
    object Recursos : BottomNavItem(
        route = "resources_tab",
        titleRes = R.string.nav_resources,
        iconRes = R.drawable.ic_resources
    )
}