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

enum class BreathPhase(val instruction: String, val durationSeconds: Int) {
    IDLE("Listo para empezar", 0),
    INHALE("Inhala lentamente...", 4),
    HOLD("Mantén el aire...", 7),
    EXHALE("Exhala suavemente...", 8)
}

data class BreathingUiState(
    val phase: BreathPhase = BreathPhase.IDLE,
    val timeLeft: Int = 0,
    val isPlaying: Boolean = false,
    val cyclesCompleted: Int = 0
)

@HiltViewModel
class BreathingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(BreathingUiState())
    val uiState = _uiState.asStateFlow()

    private var breathJob: Job? = null

    fun toggleExercise() {
        if (_uiState.value.isPlaying) {
            stopExercise()
        } else {
            startExercise()
        }
    }

    private fun startExercise() {
        _uiState.update { it.copy(isPlaying = true, cyclesCompleted = 0) }

        breathJob = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                runPhase(BreathPhase.INHALE)
                runPhase(BreathPhase.HOLD)
                runPhase(BreathPhase.EXHALE)

                _uiState.update { it.copy(cyclesCompleted = it.cyclesCompleted + 1) }
            }
        }
    }

    fun stopExercise() {
        breathJob?.cancel()
        _uiState.update {
            it.copy(
                isPlaying = false,
                phase = BreathPhase.IDLE,
                timeLeft = 0
            )
        }
    }

    private suspend fun runPhase(phase: BreathPhase) {
        if (!_uiState.value.isPlaying) return

        for (i in phase.durationSeconds downTo 1) {
            if (!_uiState.value.isPlaying) return
            _uiState.update { it.copy(phase = phase, timeLeft = i) }
            delay(1000L)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopExercise()
    }
}