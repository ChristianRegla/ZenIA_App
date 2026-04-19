package com.zenia.app.ui.screens.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AnalyticsRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val selectedRange by viewModel.selectedRange.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    AnalyticsScreen(
        uiState = uiState,
        selectedRange = selectedRange,
        isPremium = isPremium,
        lineChartProducer = viewModel.lineChartProducer,
        sleepChartProducer = viewModel.sleepChartProducer,
        physicalChartProducer = viewModel.physicalChartProducer,
        onNavigateBack = onNavigateBack,
        onNavigateToPremium = onNavigateToPremium,
        onTimeRangeSelected = viewModel::setTimeRange
    )
}