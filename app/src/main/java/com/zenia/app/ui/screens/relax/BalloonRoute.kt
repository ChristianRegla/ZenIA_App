package com.zenia.app.ui.screens.relax

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BalloonRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: BalloonViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    BalloonScreen(
        uiState = uiState,
        onStartExercise = { viewModel.startExercise() },
        onReleaseThought = { viewModel.releaseThought() },
        onNavigateBack = {
            viewModel.stopExercise()
            onNavigateBack()
        }
    )
}