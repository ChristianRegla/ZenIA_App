package com.zenia.app.ui.navigation

import java.time.LocalDate

object Destinations {
    const val AUTH_ROUTE = "auth"
    const val MAIN_ROUTE = "main_screen"

    const val HOME_ROUTE = "home_tab"
    const val DIARY_ROUTE = "diary_tab"
    const val ZENIA_ROUTE = "zenia_tab"
    const val RELAX_ROUTE = "relax_tab"
    const val RECURSOS_ROUTE = "recursos_tab"

    const val SOS = "sos"
    const val ACCOUNT_ROUTE = "account"
    const val LOCK_ROUTE = "lock"
    const val SETTINGS_ROUTE = "settings"
    const val MORE_SETTINGS_ROUTE = "more_settings"
    const val ABOUT_ROUTE = "about"
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"
    const val HELP_CENTER_ROUTE = "help_center"
    const val DONATIONS_ROUTE = "donations"
    const val PRIVACY_POLICY_ROUTE = "privacy_policy"
    const val NOTIFICATIONS_ROUTE = "notifications"
    const val PREMIUM_ROUTE = "premium"
    const val HEALTH_SYNC_ROUTE = "health_sync"
    const val DIARY_ENTRY_ROUTE = "diary_entry/{date}"
    const val ANALYTICS_ROUTE = "analytics"
    const val CHAT_ROUTE = "chat"
    const val ONBOARDING_ROUTE = "onboarding_route"

    fun createDiaryEntryRoute(date: LocalDate) = "diary_entry/${date}"
    fun homeWithTab(tabRoute: String) = "$HOME_ROUTE?tab=$tabRoute"
}

object NavArgs {
    const val DATE = "date"
}