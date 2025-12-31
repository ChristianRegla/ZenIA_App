package com.zenia.app.ui.screens.analytics

import androidx.compose.runtime.Composable

@Composable
fun AnalyticsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    isPremium: Boolean
) {
    AnalyticsScreen(
        onNavigateBack = onNavigateBack,
        onNavigateToPremium = onNavigateToPremium,
        isPremium = isPremium
    )
}