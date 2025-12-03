package com.zenia.app.ui.screens.diary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenia.app.viewmodel.AppViewModelProvider
import java.time.LocalDate

@Composable
fun DiarioRoute(
    viewModel: DiarioViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    DiarioScreen(
        uiState = uiState,
        onDateSelected = { date -> viewModel.selectDate(date) },
        onBackToCalendar = { viewModel.clearSelection() },
        onYearChange = { increment -> viewModel.changeYear(increment) },
        onJumpToToday = { viewModel.jumpToToday() },
        onScrollConsumed = { viewModel.resetScrollTarget() }
    )
}