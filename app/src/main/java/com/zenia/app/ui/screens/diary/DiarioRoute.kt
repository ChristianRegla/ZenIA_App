package com.zenia.app.ui.screens.diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DiarioRoute(
    viewModel: DiarioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DiarioScreen(
        uiState = uiState
    )
}