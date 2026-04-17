package com.zenia.app.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenia.app.viewmodel.SettingsViewModel
import java.util.Locale

@Composable
fun MoreSettingsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
    onChangelogClick: () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val isBiometricEnabled by settingsViewModel.isBiometricEnabled.collectAsState()
    val allowWeakBiometrics by settingsViewModel.allowWeakBiometrics.collectAsState()

    val currentLocale = AppCompatDelegate.getApplicationLocales()[0] ?: Locale.getDefault()
    val currentLanguage = currentLocale.language

    val isNotificationsEnabled by settingsViewModel.isNotificationsEnabled.collectAsStateWithLifecycle()
    val isStreakEnabled by settingsViewModel.isStreakEnabled.collectAsStateWithLifecycle()

    val streakHour by settingsViewModel.streakReminderHour.collectAsStateWithLifecycle()
    val streakMinute by settingsViewModel.streakReminderMinute.collectAsStateWithLifecycle()

    val isAdviceEnabled by settingsViewModel.isAdviceEnabled.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            settingsViewModel.setNotificationsEnabled(true)
        } else {
            settingsViewModel.setNotificationsEnabled(false)
        }
    }

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
        onNavigateBack = onNavigateBack,
        onNavigateToExport = onNavigateToExport,
        isNotificationsEnabled = isNotificationsEnabled,
        isStreakEnabled = isStreakEnabled,
        streakHour = streakHour,
        streakMinute = streakMinute,
        isAdviceEnabled = isAdviceEnabled,
        onToggleNotifications = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                settingsViewModel.setNotificationsEnabled(enabled)
            }
        },
        onToggleStreak = { settingsViewModel.setStreakReminderEnabled(it) },
        onTimeChange = { hour, minute -> settingsViewModel.setStreakReminderTime(hour, minute) },
        onToggleAdvice = { settingsViewModel.setMorningAdviceEnabled(it) },
        onChangelogClick = onChangelogClick
    )
}