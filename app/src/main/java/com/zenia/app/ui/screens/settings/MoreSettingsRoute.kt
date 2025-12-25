package com.zenia.app.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel
import java.util.Locale

@Composable
fun MoreSettingsRoute(
    onNavigateBack: () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val allowWeakBiometrics by settingsViewModel.allowWeakBiometrics.collectAsState()

    val currentLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    val currentLanguage = currentLocale.language

    MoreSettingsScreen(
        isBiometricEnabled = isBiometricEnabled == true,
        allowWeakBiometrics = allowWeakBiometrics,
        currentLanguage = currentLanguage,
        onToggleBiometric = { settingsViewModel.setBiometricEnabled(it) },
        onToggleWeakBiometric = { settingsViewModel.setWeakBiometricsEnabled(it) },
        onLanguageChange = { newLanguage ->
            val appLocale = LocaleListCompat.forLanguageTags(newLanguage)
            AppCompatDelegate.setApplicationLocales(appLocale)
        },
        onNavigateBack = onNavigateBack
    )
}