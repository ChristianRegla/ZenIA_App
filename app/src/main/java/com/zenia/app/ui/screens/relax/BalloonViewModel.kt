package com.zenia.app.ui.screens.relax

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BalloonScreenState { INTRO, TYPING, RELEASING, FINISHED }

data class BalloonUiState(
    val screenState: BalloonScreenState = BalloonScreenState.INTRO
)

@HiltViewModel
class BalloonViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(BalloonUiState())
    val uiState = _uiState.asStateFlow()

    fun startExercise() {
        _uiState.update { it.copy(screenState = BalloonScreenState.TYPING) }
    }

    fun releaseThought() {
        _uiState.update { it.copy(screenState = BalloonScreenState.RELEASING) }

        viewModelScope.launch {
            delay(4000)
            _uiState.update { it.copy(screenState = BalloonScreenState.FINISHED) }
        }
    }

    fun stopExercise() {
        _uiState.update { BalloonUiState() }
    }
}