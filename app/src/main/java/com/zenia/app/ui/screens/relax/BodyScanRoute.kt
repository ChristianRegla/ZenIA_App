package com.zenia.app.ui.screens.relax

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BodyScanRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: BodyScanViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    BodyScanScreen(
        uiState = uiState,
        onStartExercise = { viewModel.startExercise() },
        onNavigateBack = {
            viewModel.stopExercise()
            onNavigateBack()
        }
    )
}