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
    onNavigateToHealthSync: () -> Unit,
    onNavigateToMoreSettings: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onSignOut: () -> Unit
) {
    val nickname by settingsViewModel.nickname.collectAsState()
    val email by settingsViewModel.email.collectAsState()
    val avatarIndex by settingsViewModel.avatarIndex.collectAsState()
    val isPremium by settingsViewModel.isUserPremium.collectAsState()

    SettingsScreen(
        name = if (nickname == "Usuario") null else nickname,
        email = email ?: "",
        avatarIndex = avatarIndex,
        isPremium = isPremium,
        onUpdateProfile = { newName, newAvatarIdx ->
            settingsViewModel.updateProfile(newName, newAvatarIdx)
        },
        onNavigateBack = onNavigateBack,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToPremium = onNavigateToPremium,
        onNavigateToHealthSync = onNavigateToHealthSync,
        onNavigateToMoreSettings = onNavigateToMoreSettings,
        onNavigateToHelp = onNavigateToHelp,
        onNavigateToDonations = onNavigateToDonations,
        onNavigateToPrivacy = onNavigateToPrivacy,
        onSignOut = onSignOut
    )
}