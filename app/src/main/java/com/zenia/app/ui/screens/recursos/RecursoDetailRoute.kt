package com.zenia.app.ui.screens.recursos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RecursoDetailRoute(
    recursoId: String,
    onNavigateBack: () -> Unit,
    viewModel: RecursoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var isNavigating by remember { mutableStateOf(false) }

    RecursoDetailScreen(
        uiState = uiState,
        onNavigateBack = {
            if (!isNavigating) {
                isNavigating = true
                onNavigateBack()
            }
        },
        onMarkAsCompleted = {
            if (!isNavigating) {
                isNavigating = true
                viewModel.markAsCompleted()
                onNavigateBack()
            }
        }
    )
}