package com.zenia.app.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimensions(
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val paddingExtraLarge: Dp = 32.dp,

    val buttonHeight: Dp = 48.dp,
    val buttonMaxWidth: Dp = 400.dp,

    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,

    val cornerRadiusNormal: Dp = 12.dp,
    val cardElevation: Dp = 4.dp
)

val compactDimensions = AppDimensions()

val mediumDimensions = AppDimensions(
    paddingSmall = 12.dp,
    paddingMedium = 24.dp,
    paddingLarge = 32.dp,
    paddingExtraLarge = 48.dp,
    buttonHeight = 56.dp,
    iconMedium = 28.dp
)

val expandedDimensions = AppDimensions(
    paddingSmall = 16.dp,
    paddingMedium = 32.dp,
    paddingLarge = 48.dp,
    paddingExtraLarge = 64.dp,
    buttonHeight = 56.dp,
    iconMedium = 32.dp
)

val LocalAppDimensions = compositionLocalOf { compactDimensions }