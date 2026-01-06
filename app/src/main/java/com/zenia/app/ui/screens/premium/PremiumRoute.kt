package com.zenia.app.ui.screens.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun PremiumRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val isPremium by viewModel.isUserPremium.collectAsState()

    PremiumScreen(
        onNavigateBack = onNavigateBack,
        isPremium = isPremium,
        viewModel = viewModel
    )
}