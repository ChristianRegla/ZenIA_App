package com.zenia.app.ui.screens.notifications

import androidx.compose.runtime.Composable

@Composable
fun NotificationsRoute(
    onNavigateBack: () -> Unit
) {
    NotificationsScreen(
        onNavigateBack = onNavigateBack
    )
}