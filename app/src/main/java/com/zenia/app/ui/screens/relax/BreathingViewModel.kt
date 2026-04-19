package com.zenia.app.ui.screens.relax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BreathingScreenState { INTRO, EXERCISING, FINISHED }

enum class BreathPhase(val durationMs: Int) {
    IDLE(0),
    INHALE(4000),
    HOLD(7000),
    EXHALE(8000)
}

data class BreathingUiState(
    val screenState: BreathingScreenState = BreathingScreenState.INTRO,
    val phase: BreathPhase = BreathPhase.IDLE,
    val cyclesCompleted: Int = 0,
    val totalCycles: Int = 4
)

@HiltViewModel
class BreathingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(BreathingUiState())
    val uiState = _uiState.asStateFlow()

    private var breathJob: Job? = null

    fun startExercise() {
        _uiState.update {
            it.copy(
                screenState = BreathingScreenState.EXERCISING,
                cyclesCompleted = 0,
                phase = BreathPhase.INHALE
            )
        }

        breathJob = viewModelScope.launch {
            while (_uiState.value.cyclesCompleted < _uiState.value.totalCycles) {
                runPhase(BreathPhase.INHALE)
                runPhase(BreathPhase.HOLD)
                runPhase(BreathPhase.EXHALE)

                _uiState.update { it.copy(cyclesCompleted = it.cyclesCompleted + 1) }
            }

            _uiState.update {
                it.copy(
                    screenState = BreathingScreenState.FINISHED,
                    phase = BreathPhase.IDLE
                )
            }
        }
    }

    fun stopExercise() {
        breathJob?.cancel()
        _uiState.update {
            it.copy(
                screenState = BreathingScreenState.INTRO,
                phase = BreathPhase.IDLE,
                cyclesCompleted = 0
            )
        }
    }

    private suspend fun runPhase(phase: BreathPhase) {
        if (_uiState.value.screenState != BreathingScreenState.EXERCISING) return

        _uiState.update { it.copy(phase = phase) }
        delay(phase.durationMs.toLong())
    }

    override fun onCleared() {
        super.onCleared()
        stopExercise()
    }
}