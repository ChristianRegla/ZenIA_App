package com.zenia.app.ui.navigation

import com.zenia.app.R

sealed class BottomNavItem(val route: String, val titleRes: Int, val iconRes: Int) {
    object Inicio : BottomNavItem(
        Destinations.HOME_ROUTE,
        titleRes = R.string.nav_home,
        iconRes = R.drawable.ic_home
    )
    object Diario : BottomNavItem(
        Destinations.DIARY_ROUTE,
        titleRes = R.string.nav_diary,
        iconRes = R.drawable.ic_journal
    )
    object Zenia : BottomNavItem(
        Destinations.ZENIA_ROUTE,
        titleRes = R.string.nav_bot,
        iconRes = R.drawable.ic_chat
    )
    object Relajacion : BottomNavItem(
        Destinations.RELAX_ROUTE,
        titleRes = R.string.nav_relax,
        iconRes = R.drawable.ic_relax
    )
    object Recursos : BottomNavItem(
        Destinations.RECURSOS_ROUTE,
        titleRes = R.string.nav_resources,
        iconRes = R.drawable.ic_resources
    )
}