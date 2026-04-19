package com.zenia.app.ui.screens.relax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BodyScanScreenState { INTRO, EXERCISING, FINISHED }

enum class MuscleGroup(val titleRes: Int) {
    HANDS(R.string.bodyscan_part_hands),
    SHOULDERS(R.string.bodyscan_part_shoulders),
    FACE(R.string.bodyscan_part_face)
}

enum class ScanPhase(val actionRes: Int, val durationMs: Int) {
    TENSE(R.string.bodyscan_action_tense, 4000),
    HOLD(R.string.bodyscan_action_hold, 3000),
    RELAX(R.string.bodyscan_action_relax, 6000)
}

data class BodyScanUiState(
    val screenState: BodyScanScreenState = BodyScanScreenState.INTRO,
    val currentMuscle: MuscleGroup = MuscleGroup.HANDS,
    val currentPhase: ScanPhase = ScanPhase.TENSE
)

@HiltViewModel
class BodyScanViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(BodyScanUiState())
    val uiState = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun startExercise() {
        _uiState.update {
            it.copy(
                screenState = BodyScanScreenState.EXERCISING,
                currentMuscle = MuscleGroup.HANDS,
                currentPhase = ScanPhase.TENSE
            )
        }

        scanJob = viewModelScope.launch {
            val muscles = MuscleGroup.entries.toTypedArray()

            for (muscle in muscles) {
                _uiState.update { it.copy(currentMuscle = muscle) }

                runPhase(ScanPhase.TENSE)
                runPhase(ScanPhase.HOLD)
                runPhase(ScanPhase.RELAX)
            }

            // Al terminar todos los músculos, finalizamos
            _uiState.update { it.copy(screenState = BodyScanScreenState.FINISHED) }
        }
    }

    private suspend fun runPhase(phase: ScanPhase) {
        if (_uiState.value.screenState != BodyScanScreenState.EXERCISING) return

        _uiState.update { it.copy(currentPhase = phase) }
        delay(phase.durationMs.toLong())
    }

    fun stopExercise() {
        scanJob?.cancel()
        _uiState.update { BodyScanUiState() }
    }

    override fun onCleared() {
        super.onCleared()
        stopExercise()
    }
}