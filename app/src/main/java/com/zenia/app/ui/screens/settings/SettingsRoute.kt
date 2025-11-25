package com.zenia.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.zenia.app.R
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onSignOut: () -> Unit
) {
    val userName = stringResource(R.string.settings_placeholder_name)
    val userEmail = stringResource(R.string.settings_placeholder_email)
    SettingsScreen(
        name = userName,
        email = userEmail,
        onNavigateBack = onNavigateBack,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToPremium = onNavigateToPremium,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToDonations = onNavigateToDonations,
        onNavigateToPrivacy = onNavigateToPrivacy,
        onSignOut = onSignOut
    )
}