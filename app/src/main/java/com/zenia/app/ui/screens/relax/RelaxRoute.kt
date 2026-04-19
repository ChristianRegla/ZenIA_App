package com.zenia.app.ui.screens.relax

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun RelaxRoute(
    onNavigateToPlayer: (Int) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val isPremium by viewModel.isUserPremium.collectAsState()

    RelaxScreen(
        onNavigateToPlayer = onNavigateToPlayer,
        onNavigateToPremium = onNavigateToPremium,
        isUserPremium = isPremium
    )
}