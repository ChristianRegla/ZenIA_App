package com.zenia.app.ui.screens.sos

import androidx.compose.runtime.Composable

@Composable
fun HelplineRoute(
    onNavigateToChat: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateBack: () -> Unit
) {
    HelplineScreen(
        onNavigateToChat = onNavigateToChat,
        onNavigateToContacts = onNavigateToContacts,
        onNavigateToExercises = onNavigateToExercises,
        onNavigateBack = onNavigateBack
    )
}