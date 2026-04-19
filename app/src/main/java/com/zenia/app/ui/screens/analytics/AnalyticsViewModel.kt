package com.zenia.app.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val totalEntries: Int = 0,
    val mainInsight: String = "Analizando tus tendencias semanales...",

    val averageMood: Float = 0f,
    val moodDistribution: Map<String, Int> = emptyMap(),
    val topActivities: List<AnalysisUtils.Insight> = emptyList(),

    val averageSleepHours: Float = 0f,

    val averageSteps: Int = 0,
    val averageHeartRate: Int = 0
)

private data class ProcessedAnalytics(
    val averageMood: Float,
    val totalEntries: Int,
    val moodDistribution: Map<String, Int>,
    val topActivities: List<AnalysisUtils.Insight>,
    val chartEntries: List<ChartEntry>,
    val averageSleepHours: Float,
    val averageSteps: Int,
    val averageHeartRate: Int
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    sessionManager: UserSessionManager
): ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedRange = MutableStateFlow(TimeRange.WEEK)
    val selectedRange = _selectedRange.asStateFlow()

    val isPremium = sessionManager.isPremium

    val lineChartProducer = ChartEntryModelProducer()

    val sleepChartProducer = ChartEntryModelProducer()
    val physicalChartProducer = ChartEntryModelProducer()

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
                if (entries.isEmpty()) {
                    _uiState.update { AnalyticsUiState(isLoading = false) }
                    lineChartProducer.setEntries(emptyList<ChartEntry>())
                    return@collect
                }

                val processedData = processEntries(entries)
                lineChartProducer.setEntries(processedData.chartEntries)
                _uiState.update {
                    it.copy(
                        averageMood = processedData.averageMood,
                        totalEntries = processedData.totalEntries,
                        moodDistribution = processedData.moodDistribution,
                        topActivities = processedData.topActivities,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun processEntries(entries: List<DiarioEntrada>): ProcessedAnalytics {
        val validMoodEntries = entries.filter { !it.estadoAnimo.isNullOrBlank() }
        val moodValues = validMoodEntries.map { ChartUtils.mapMoodToValue(it.estadoAnimo) }
        val average = if (moodValues.isNotEmpty()) moodValues.average().toFloat() else 0f

        val distribution = validMoodEntries
            .groupingBy {
                when (ChartUtils.mapMoodToValue(it.estadoAnimo).toInt()) {
                    5 -> MOOD_EXCELENTE
                    4 -> MOOD_BUENO
                    3 -> MOOD_NORMAL
                    2 -> MOOD_MALO
                    1 -> MOOD_TERRIBLE
                    else -> MOOD_OTRO
                }
            }
            .eachCount()

        val sleepEntriesValid = entries.filter { it.hcMinutosSueno != null && it.hcMinutosSueno > 0 }
        val averageSleep = if (sleepEntriesValid.isNotEmpty()) {
            sleepEntriesValid.mapNotNull { it.hcMinutosSueno }.average().toFloat() / 60f
        } else 0f

        val stepEntriesValid = entries.filter { it.hcPasos != null && it.hcPasos > 0 }
        val hrEntriesValid = entries.filter { it.hcRitmoCardiaco != null && it.hcRitmoCardiaco > 0 }

        val avgSteps = if (stepEntriesValid.isNotEmpty()) {
            stepEntriesValid.mapNotNull { it.hcPasos }.average().toInt()
        } else 0

        val avgHR = if (hrEntriesValid.isNotEmpty()) {
            hrEntriesValid.mapNotNull { it.hcRitmoCardiaco }.average().toInt()
        } else 0

        val chartEntries = mutableListOf<ChartEntry>()
        val sleepChartEntries = mutableListOf<ChartEntry>()
        val stepsChartEntries = mutableListOf<ChartEntry>()

        if (_selectedRange.value == TimeRange.WEEK) {
            val last7Days = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
            last7Days.forEach { date ->
                val entryForDate = entries.find { it.fecha == date.toString() }
                val xValue = date.toEpochDay().toFloat()

                if (entryForDate != null && !entryForDate.estadoAnimo.isNullOrBlank()) {
                    val yValue = ChartUtils.mapMoodToValue(entryForDate.estadoAnimo)
                    if (yValue > 0) chartEntries.add(entryOf(xValue, yValue))
                }

                val minutosSueno = entryForDate?.hcMinutosSueno
                if (minutosSueno != null && minutosSueno > 0) {
                    sleepChartEntries.add(entryOf(xValue, minutosSueno / 60f))
                }

                val pasos = entryForDate?.hcPasos
                if (pasos != null && pasos > 0) {
                    stepsChartEntries.add(entryOf(xValue, pasos.toFloat()))
                }
            }
        } else {
            entries.forEach { entry ->
                try {
                    val date = LocalDate.parse(entry.fecha)
                    val xValue = date.toEpochDay().toFloat()

                    if (!entry.estadoAnimo.isNullOrBlank()) {
                        val yValue = ChartUtils.mapMoodToValue(entry.estadoAnimo)
                        if (yValue > 0) chartEntries.add(entryOf(xValue, yValue))
                    }
                    if (entry.hcMinutosSueno != null && entry.hcMinutosSueno > 0) {
                        sleepChartEntries.add(entryOf(xValue, entry.hcMinutosSueno / 60f))
                    }
                    if (entry.hcPasos != null && entry.hcPasos > 0) {
                        stepsChartEntries.add(entryOf(xValue, entry.hcPasos.toFloat()))
                    }
                } catch (e: Exception) { }
            }
            chartEntries.sortBy { it.x }
            sleepChartEntries.sortBy { it.x }
            stepsChartEntries.sortBy { it.x }
        }

        sleepChartProducer.setEntries(sleepChartEntries)
        physicalChartProducer.setEntries(stepsChartEntries)

        val insightsPair = AnalysisUtils.analyzePatterns(entries)
        val insightsList = listOfNotNull(insightsPair.first, insightsPair.second)

        return ProcessedAnalytics(
            averageMood = average,
            totalEntries = entries.size,
            moodDistribution = distribution,
            topActivities = insightsList,
            chartEntries = chartEntries,
            averageSleepHours = averageSleep,
            averageSteps = avgSteps,
            averageHeartRate = avgHR
        )
    }

    companion object {
        private const val MOOD_EXCELENTE = "Excelente"
        private const val MOOD_BUENO = "Bueno"
        private const val MOOD_NORMAL = "Normal"
        private const val MOOD_MALO = "Malo"
        private const val MOOD_TERRIBLE = "Terrible"
        private const val MOOD_OTRO = "Otro"
    }
}
