package com.zenia.app.ui.screens.relax

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GroundingRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: GroundingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    GroundingScreen(
        uiState = uiState,
        onStartExercise = { viewModel.startExercise() },
        onItemChecked = { viewModel.onItemChecked() },
        onNavigateBack = {
            viewModel.stopExercise()
            onNavigateBack()
        }
    )
}