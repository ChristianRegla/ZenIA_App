package com.zenia.app.ui.screens.relax

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import com.zenia.app.R

enum class GroundingScreenState { INTRO, EXERCISING, FINISHED }

enum class GroundingPhase(val targetCount: Int, val titleRes: Int, val descRes: Int) {
    SEE(5, R.string.grounding_see_title, R.string.grounding_see_desc),
    TOUCH(4, R.string.grounding_touch_title, R.string.grounding_touch_desc),
    HEAR(3, R.string.grounding_hear_title, R.string.grounding_hear_desc),
    SMELL(2, R.string.grounding_smell_title, R.string.grounding_smell_desc),
    TASTE(1, R.string.grounding_taste_title, R.string.grounding_taste_desc),
    DONE(0, 0, 0)
}

data class GroundingUiState(
    val screenState: GroundingScreenState = GroundingScreenState.INTRO,
    val currentPhase: GroundingPhase = GroundingPhase.SEE,
    val itemsChecked: Int = 0
)

@HiltViewModel
class GroundingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(GroundingUiState())
    val uiState = _uiState.asStateFlow()

    fun startExercise() {
        _uiState.update {
            it.copy(
                screenState = GroundingScreenState.EXERCISING,
                currentPhase = GroundingPhase.SEE,
                itemsChecked = 0
            )
        }
    }

    fun onItemChecked() {
        val state = _uiState.value
        if (state.screenState != GroundingScreenState.EXERCISING) return

        val newCheckedCount = state.itemsChecked + 1

        if (newCheckedCount >= state.currentPhase.targetCount) {
            advancePhase(state.currentPhase)
        } else {
            _uiState.update { it.copy(itemsChecked = newCheckedCount) }
        }
    }

    private fun advancePhase(currentPhase: GroundingPhase) {
        val nextPhase = when (currentPhase) {
            GroundingPhase.SEE -> GroundingPhase.TOUCH
            GroundingPhase.TOUCH -> GroundingPhase.HEAR
            GroundingPhase.HEAR -> GroundingPhase.SMELL
            GroundingPhase.SMELL -> GroundingPhase.TASTE
            GroundingPhase.TASTE -> GroundingPhase.DONE
            GroundingPhase.DONE -> GroundingPhase.DONE
        }

        if (nextPhase == GroundingPhase.DONE) {
            _uiState.update {
                it.copy(screenState = GroundingScreenState.FINISHED, itemsChecked = 0)
            }
        } else {
            _uiState.update {
                it.copy(currentPhase = nextPhase, itemsChecked = 0)
            }
        }
    }

    fun stopExercise() {
        _uiState.update { GroundingUiState() }
    }
}