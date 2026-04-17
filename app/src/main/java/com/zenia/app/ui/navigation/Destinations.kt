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
    const val BLOCKED_USERS_ROUTE = "blocked_users"
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
    const val COMMUNITY_ROUTE = "community_route"
    const val POST_DETAIL_ROUTE = "post_detail/{postId}"
    const val EXPORT_SETTINGS_ROUTE = "export_settings_route"
    const val CHANGELOG_ROUTE = "changelog_route"

    // Acá iré poniendo los ejercicios de relajación
    const val BREATHING_ROUTE = "breathing_exercise"

    const val RECURSO_DETAIL_ROUTE = "recurso_detail/{recursoId}"

    const val EVALUACION_ROUTE = "evaluacion/{tipoTestId}"

    fun createDiaryEntryRoute(date: LocalDate) = "diary_entry/${date}"
    fun homeWithTab(tabRoute: String) = "$HOME_ROUTE?tab=$tabRoute"
    fun createRecursoDetailRoute(recursoId: String) = "recurso_detail/$recursoId"
    fun createEvaluacionRoute(tipoTestId: String) = "evaluacion/$tipoTestId"
    fun createPostDetailRoute(postId: String) = "post_detail/$postId"
}

object NavArgs {
    const val DATE = "date"
    const val RECURSO_ID = "recursoId"
    const val TIPO_TEST_ID = "tipoTestId"
    const val POST_ID = "postId"
}