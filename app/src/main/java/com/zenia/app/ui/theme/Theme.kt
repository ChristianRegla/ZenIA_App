package com.zenia.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ZeniaDeepTeal,
    onPrimary = ZeniaWhite,

    primaryContainer = ZeniaIceBlue,
    onPrimaryContainer = ZeniaDark,

    tertiary = ZeniaTeal,

    secondary = ZeniaSoftBlue,
    onSecondary = ZeniaDark,

    background = ZeniaWhite,
    onBackground = ZeniaDark,

    surface = ZeniaWhite,
    onSurface = ZeniaDark,

    surfaceVariant = ZeniaLightGrey,
    onSurfaceVariant = ZeniaSlateGrey,

    error = androidx.compose.ui.graphics.Color(0xFFBA1A1A),
    onError = androidx.compose.ui.graphics.Color.White
)

@Composable
fun ZenIATheme(
    windowSizeClass: WindowWidthSizeClass,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if(!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val dimensions = when (windowSizeClass) {
        WindowWidthSizeClass.Compact -> compactDimensions
        WindowWidthSizeClass.Medium -> mediumDimensions
        WindowWidthSizeClass.Expanded -> expandedDimensions
        else -> compactDimensions
    }

    CompositionLocalProvider(
        LocalAppDimensions provides dimensions
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object ZenIATheme {
    val dimensions: AppDimensions
    @Composable
    @ReadOnlyComposable
    get() = LocalAppDimensions.current
}