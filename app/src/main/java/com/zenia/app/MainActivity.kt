package com.zenia.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.zenia.app.ui.navigation.AppNavigation
import com.zenia.app.ui.theme.ZenIATheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenIATheme {
                AppNavigation()
            }
        }
    }
}