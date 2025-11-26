package com.zenia.app.ui.screens.diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate

@Composable
fun DiarioRoute(
    viewModel: DiarioViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    DiarioScreen(
        uiState = uiState,
        onDateSelected = viewModel::onDateSelected,
        onBackToCalendar = viewModel::onBackToCalendar
    )
}