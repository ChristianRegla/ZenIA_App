package com.zenia.app

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.zenia.app.ui.navigation.AppNavigation
import com.zenia.app.ui.theme.ZenIATheme
import com.zenia.app.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels()
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            settingsViewModel.isBiometricEnabled.value == null
        }

        enableEdgeToEdge()
        setContent {
            ZenIATheme {
                AppNavigation()
            }
        }
    }
}