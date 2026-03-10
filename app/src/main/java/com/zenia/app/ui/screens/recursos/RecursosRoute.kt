package com.zenia.app.ui.screens.recursos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenia.app.viewmodel.SettingsViewModel

@Composable
fun RecursosRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPremium: () -> Unit
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val recursosViewModel: RecursosViewModel = hiltViewModel()

    val isPremium by settingsViewModel.isUserPremium.collectAsState()
    val uiState by recursosViewModel.uiState.collectAsState()

    RecursosScreen(
        uiState = uiState,
        isUserPremium = isPremium,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToPremium = onNavigateToPremium,
        onToggleFavorite = { id, status -> recursosViewModel.toggleFavorite(id, status) }
    )
}