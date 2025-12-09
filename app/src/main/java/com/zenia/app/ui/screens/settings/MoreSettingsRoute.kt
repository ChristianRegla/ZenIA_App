package com.zenia.app.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun MoreSettingsRoute(
    onNavigateBack: () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val allowWeakBiometrics by settingsViewModel.allowWeakBiometrics.collectAsState()
    MoreSettingsScreen(
        isBiometricEnabled = isBiometricEnabled == true,
        allowWeakBiometrics = allowWeakBiometrics,
        onToggleBiometric = { settingsViewModel.setBiometricEnabled(it) },
        onToggleWeakBiometric = { settingsViewModel.setWeakBiometricsEnabled(it) },
        onNavigateBack = onNavigateBack
    )
}