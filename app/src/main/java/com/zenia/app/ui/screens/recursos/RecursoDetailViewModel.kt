package com.zenia.app.ui.screens.recursos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.RecursosRepository
import com.zenia.app.model.Recurso
import com.zenia.app.ui.navigation.NavArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RecursoDetailUiState {
    object Loading : RecursoDetailUiState
    data class Success(val recurso: Recurso) : RecursoDetailUiState
    data class Error(val message: String) : RecursoDetailUiState
}

@HiltViewModel
class RecursoDetailViewModel @Inject constructor(
    private val recursosRepository: RecursosRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recursoId: String = checkNotNull(savedStateHandle[NavArgs.RECURSO_ID])

    private val _uiState = MutableStateFlow<RecursoDetailUiState>(RecursoDetailUiState.Loading)
    val uiState: StateFlow<RecursoDetailUiState> = _uiState.asStateFlow()

    init {
        loadRecurso()
    }

    private fun loadRecurso() {
        viewModelScope.launch {
            _uiState.value = RecursoDetailUiState.Loading
            val recurso = recursosRepository.getRecursoById(recursoId)

            if (recurso != null) {
                _uiState.value = RecursoDetailUiState.Success(recurso)
                recursosRepository.updateProgress(recursoId, 10)
            } else {
                _uiState.value = RecursoDetailUiState.Error("No se encontró el recurso.")
            }
        }
    }

    fun markAsCompleted() {
        viewModelScope.launch {
            recursosRepository.updateProgress(recursoId, 100)
        }
    }
}