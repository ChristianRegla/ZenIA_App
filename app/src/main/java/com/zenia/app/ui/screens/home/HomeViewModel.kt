package com.zenia.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.ContentRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import com.zenia.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeUiState {
    object Idle : HomeUiState
    object Loading : HomeUiState
    object Success : HomeUiState
    data class Error(val message: UiText) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val contentRepository: ContentRepository,
    private val diaryRepository: DiaryRepository,
) : ViewModel() {

    // --- ESTADO DE USUARIO ---
    val userName = authRepository.getUsuarioFlow()
        .map { it?.apodo ?: "Usuario" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")


    private val sevenDaysAgo = LocalDate.now().minusDays(7).toString()

    val registrosDiario = diaryRepository.getEntriesFromDate(sevenDaysAgo)
        .onEach { entradas ->
            processChartData(entradas)
            loadStreak()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasEntryToday: StateFlow<Boolean> = registrosDiario.map { lista ->
        val todayStr = LocalDate.now().toString()
        lista.any { it.fecha == todayStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val chartProducer = ChartEntryModelProducer()

    val moodInsights = diaryRepository.getEntriesFromDate(
        LocalDate.now().minusDays(30).toString()
    ).map { entries ->
        AnalysisUtils.analyzePatterns(entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, null))

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()

    val communityActivities = contentRepository.getActividadesComunidad()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState = _uiState.asStateFlow()

    /**
     * Carga la racha usando la función eficiente del repositorio.
     * Esto evita descargar todo el contenido de meses pasados.
     */
    private fun loadStreak() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId
            if (userId != null) {
                try {
                    val streak = diaryRepository.calculateCurrentStreak()
                    _currentStreak.value = streak
                } catch (e: Exception) {
                    _currentStreak.value = 0
                }
            }
        }
    }

    private fun processChartData(registros: List<DiarioEntrada>) {
        val entries = registros
            .filter { !it.estadoAnimo.isNullOrBlank() }
            .mapNotNull { entrada ->
                try {
                    val date = LocalDate.parse(entrada.fecha)
                    val xValue = date.toEpochDay().toFloat()
                    val moodValue = ChartUtils.mapMoodToValue(entrada.estadoAnimo)

                    if (moodValue > 0f) {
                        entryOf(xValue, moodValue)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.x }
            .takeLast(7)

        chartProducer.setEntries(entries)
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}