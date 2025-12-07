package com.zenia.app.ui.screens.recursos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.ContentRepository
import com.zenia.app.model.Recurso
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed interface RecursosUiState {
    object Loading : RecursosUiState
    data class Success(val recursos: List<Recurso>) : RecursosUiState
    data class Error(val message: String) : RecursosUiState
}

class RecursosViewModel(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecursosUiState>(RecursosUiState.Loading)
    val uiState: StateFlow<RecursosUiState> = _uiState.asStateFlow()

    init {
        cargarRecursos()
    }

    fun cargarRecursos() {
        viewModelScope.launch {
            _uiState.value = RecursosUiState.Loading

            contentRepository.getRecursos()
                .catch { e ->
                    _uiState.value = RecursosUiState.Error(e.message ?: "Error desconocido")
                }
                .collect { listaRecursos ->
                    _uiState.value = RecursosUiState.Success(listaRecursos)
                }
        }
    }
}