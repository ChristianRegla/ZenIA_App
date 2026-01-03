package com.zenia.app.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AnalyticsUiState(
    val averageMood: Float = 0f,
    val totalEntries: Int = 0,
    val moodDistribution: Map<String, Int> = emptyMap(),
    val topActivities: List<AnalysisUtils.Insight> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val authRepository: AuthRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedRange = MutableStateFlow(TimeRange.WEEK)
    val selectedRange = _selectedRange.asStateFlow()

    val isPremium = authRepository.getUsuarioFlow()
        .map { it?.suscripcion == "premium"  }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val lineChartProducer = ChartEntryModelProducer()

    init {
        viewModelScope.launch {
            _selectedRange.collect { range ->
                loadDataForRange(range)
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _selectedRange.value = range
    }

    private fun loadDataForRange(range: TimeRange) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val startDate = LocalDate.now().minusDays(range.days.toLong()).toString()

            diaryRepository.getEntriesFromDate(startDate).collect { entries ->
                processEntries(entries)
            }
        }
    }

    private fun processEntries(entries: List<DiarioEntrada>) {
        if (entries.isEmpty()) {
            _uiState.update { AnalyticsUiState(isLoading = false) }
            lineChartProducer.setEntries(emptyList<ChartEntry>())
            return
        }

        val validMoodEntries = entries.filter { !it.estadoAnimo.isNullOrBlank() }
        val moodValues = validMoodEntries.map { ChartUtils.mapMoodToValue(it.estadoAnimo) }
        val average = if (moodValues.isNotEmpty()) moodValues.average().toFloat() else 0f

        val distribution = validMoodEntries
            .groupingBy {
                when (ChartUtils.mapMoodToValue(it.estadoAnimo).toInt()) {
                    5 -> "Excelente"
                    4 -> "Bueno"
                    3 -> "Normal"
                    2 -> "Malo"
                    1 -> "Terrible"
                    else -> "Otro"
                }
            }
            .eachCount()

        val chartEntries = validMoodEntries
            .mapNotNull { entry ->
                try {
                    val date = LocalDate.parse(entry.fecha)
                    val xValue = date.toEpochDay().toFloat()
                    val yValue = ChartUtils.mapMoodToValue(entry.estadoAnimo)
                    if (yValue > 0) entryOf(xValue, yValue) else null
                } catch (e: Exception) { null }
            }
            .sortedBy { it.x }

        lineChartProducer.setEntries(chartEntries)

        val insightsPair = AnalysisUtils.analyzePatterns(entries)
        val insightsList = listOfNotNull(insightsPair.first, insightsPair.second)

        _uiState.update {
            it.copy(
                averageMood = average,
                totalEntries = entries.size,
                moodDistribution = distribution,
                topActivities = insightsList,
                isLoading = false
            )
        }
    }
}