package com.zenia.app.ui.screens.relax

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BreathingRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: BreathingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    BreathingScreen(
        uiState = uiState,
        onToggleExercise = { viewModel.toggleExercise() },
        onNavigateBack = {
            viewModel.stopExercise()
            onNavigateBack()
        }
    )
}