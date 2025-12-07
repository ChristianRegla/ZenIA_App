package com.zenia.app.ui.screens.diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DiarioRoute(
    viewModel: DiarioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val entries by viewModel.allEntries.collectAsState()

    DiarioScreen(
        uiState = uiState,
        entries = entries,
        onDateSelected = { date -> viewModel.selectDate(date) },
        onBackToCalendar = { viewModel.clearSelection() },
        onYearChange = { increment -> viewModel.changeYear(increment) },
        onJumpToToday = { viewModel.jumpToToday() },
        onScrollConsumed = { viewModel.resetScrollTarget() }
    )
}