package com.zenia.app.ui.navigation

import java.time.LocalDate

object Destinations {
    const val AUTH_ROUTE = "auth"
    const val HOME_ROUTE = "home"
    const val ACCOUNT_ROUTE = "account"
    const val LOCK_ROUTE = "lock"
    const val SETTINGS_ROUTE = "settings"
    const val ABOUT_ROUTE = "about"
    const val FORGOT_PASSWORD_ROUTE = "forgot_password"
    const val HELP_CENTER_ROUTE = "help_center"
    const val DONATIONS_ROUTE = "donations"
    const val PRIVACY_POLICY_ROUTE = "privacy_policy"
    const val NOTIFICATIONS_ROUTE = "notifications"
    const val PREMIUM_ROUTE = "premium"
    const val DIARY_ROUTE = "diary"
    const val DIARY_ENTRY_ROUTE = "diary_entry/{date}"

    fun createDiaryEntryRoute(date: LocalDate) = "diary_entry/${date}"
}