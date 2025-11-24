package com.zenia.app.ui.screens.settings

import androidx.compose.runtime.Composable
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    SettingsScreen(
        viewModel = settingsViewModel,
        onNavigateBack = onNavigateBack,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToDonations = onNavigateToDonations,
        onNavigateToPrivacy = onNavigateToPrivacy,
    )
}