package com.zenia.app.ui.screens.recursos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RecursoDetailRoute(
    recursoId: String,
    onNavigateBack: () -> Unit,
    viewModel: RecursoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    RecursoDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onMarkAsCompleted = {
            viewModel.markAsCompleted()
            onNavigateBack()
        }
    )
}