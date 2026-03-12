package com.zenia.app.ui.screens.recursos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import com.zenia.app.data.RecursosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecursoUiModel(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val imageRes: Int,
    val isPremium: Boolean,
    val isFavorite: Boolean,
    val progress: Int
)

sealed interface RecursosUiState {
    object Loading : RecursosUiState
    data class Success(val recursos: List<RecursoUiModel>) : RecursosUiState
    data class Error(val message: String) : RecursosUiState
}

@HiltViewModel
class RecursosViewModel @Inject constructor(
    private val recursosRepository: RecursosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecursosUiState>(RecursosUiState.Loading)
    val uiState: StateFlow<RecursosUiState> = _uiState.asStateFlow()

    init {
        cargarRecursos()
    }

    fun cargarRecursos() {
        viewModelScope.launch {
            _uiState.value = RecursosUiState.Loading

            recursosRepository.getRecursosConInteracciones()
                .catch { e ->
                    _uiState.value = RecursosUiState.Error(e.message ?: "Error desconocido")
                }
                .collect { listaPares ->
                    val uiModels = listaPares.map { (recurso, interaccion) ->
                        RecursoUiModel(
                            id = recurso.id,
                            title = recurso.titulo,
                            description = recurso.descripcion.takeIf { it.isNotBlank() }
                                ?: (recurso.contenido.take(80).replace("#", "")
                                    .replace("*", "") + "..."),
                            category = recurso.tipo,
                            imageRes = R.drawable.placeholder_resource_1,
                            isPremium = recurso.esPremium ?: false,
                            isFavorite = interaccion?.isFavorite ?: false,
                            progress = interaccion?.progress ?: 0
                        )
                    }
                    _uiState.value = RecursosUiState.Success(uiModels)
                }
        }
    }

    fun toggleFavorite(recursoId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            recursosRepository.toggleFavorite(recursoId, currentStatus)
        }
    }
}