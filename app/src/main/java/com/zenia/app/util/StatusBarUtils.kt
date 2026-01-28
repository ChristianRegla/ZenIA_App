package com.zenia.app.util

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun DarkStatusIconsEffect() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)

            val originalStatus = controller.isAppearanceLightStatusBars

            controller.isAppearanceLightStatusBars = true

            onDispose {
                controller.isAppearanceLightStatusBars = originalStatus
            }
        }
    }
}

@Composable
fun LightStatusIconsEffect() {
    val view = LocalView.current
    if (!view.isInEditMode) {
        DisposableEffect(Unit) {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)

            val originalStatus = controller.isAppearanceLightStatusBars

            controller.isAppearanceLightStatusBars = false

            onDispose {
                controller.isAppearanceLightStatusBars = originalStatus
            }
        }
    }
}