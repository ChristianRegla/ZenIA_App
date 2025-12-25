package com.zenia.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToMoreSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onSignOut: () -> Unit
) {
    val currentUser by settingsViewModel.currentUser.collectAsState(initial = null)

    SettingsScreen(
        name = currentUser?.apodo,
        email = currentUser?.email ?: "",
        avatarIndex = currentUser?.avatarIndex ?: 0,
        onUpdateProfile = { newName, newAvatarIdx ->
            settingsViewModel.updateProfile(newName, newAvatarIdx)
        },
        onNavigateBack = onNavigateBack,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToPremium = onNavigateToPremium,
        onNavigateToMoreSettings = onNavigateToMoreSettings,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToDonations = onNavigateToDonations,
        onNavigateToPrivacy = onNavigateToPrivacy,
        onSignOut = onSignOut
    )
}