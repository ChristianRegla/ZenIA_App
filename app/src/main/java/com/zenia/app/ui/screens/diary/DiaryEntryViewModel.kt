package com.zenia.app.ui.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import com.zenia.app.data.ZeniaRepository
import com.zenia.app.model.DiarioEntrada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface DiaryEntryUiState {
    object Idle : DiaryEntryUiState
    object Loading : DiaryEntryUiState
    object Success : DiaryEntryUiState
    object Deleted : DiaryEntryUiState
    data class Error(val msg: String) : DiaryEntryUiState
}

data class FeelingData(val id: Int, val iconRes: Int, val label: String)

class DiaryEntryViewModel(
    private val repository: ZeniaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<DiaryEntryUiState>(DiaryEntryUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _existingEntry = MutableStateFlow<DiarioEntrada?>(null)
    val existingEntry = _existingEntry.asStateFlow()

    val feelings = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Bien"),
        FeelingData(1, R.drawable.ic_sol_feli, "Feliz"),
        FeelingData(2, R.drawable.ic_nube_tite, "Desanimado"),
        FeelingData(3, R.drawable.ic_sol_feli, "Alegre")
    )

    val dreamQuality = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Descansado"),
        FeelingData(1, R.drawable.ic_sol_feli, "Energético"),
        FeelingData(2, R.drawable.ic_nube_tite, "Cansado"),
        FeelingData(3, R.drawable.ic_sol_feli, "Muy bien")
    )

    val mind = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Tranquilidad"),
        FeelingData(1, R.drawable.ic_sol_feli, "Claridad"),
        FeelingData(2, R.drawable.ic_nube_tite, "Sin motivación"),
        FeelingData(3, R.drawable.ic_sol_feli, "Estresado")
    )

    val exercise = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Caminata"),
        FeelingData(1, R.drawable.ic_sol_feli, "Intenso"),
        FeelingData(2, R.drawable.ic_nube_tite, "Nada"),
        FeelingData(3, R.drawable.ic_sol_feli, "Ligero")
    )

    val activitiesList = listOf(
        "Trabajo", "Ejercicio", "Lectura", "Gaming",
        "Familia", "Amigos", "Cita", "Viaje", "Descanso"
    )

    fun resetState() {
        _uiState.value = DiaryEntryUiState.Idle
    }

    fun guardarEntrada(
        date: LocalDate,
        estadoAnimo: String?,
        calidadSueno: String?,
        estadoMental: String?,
        ejercicio: String?,
        actividades: List<String>,
        notas: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = DiaryEntryUiState.Loading

            val currentUserId = repository.getCurrentUserId() ?: ""

            if (currentUserId.isBlank()) {
                _uiState.value = DiaryEntryUiState.Error("Usuario no identificado")
                return@launch
            }

            val nuevaEntrada = DiarioEntrada(
                userId = currentUserId,
                fecha = date.toString(),
                estadoAnimo = estadoAnimo,
                calidadSueno = calidadSueno,
                estadoMental = estadoMental,
                ejercicio = ejercicio,
                actividades = actividades,
                notas = notas
            )

            val entradaActual = _existingEntry.value
            if (entradaActual != null) {
                val entradaParaComparar = nuevaEntrada.copy(timestamp = entradaActual.timestamp)

                if (entradaActual == entradaParaComparar) {
                    _uiState.value = DiaryEntryUiState.Success
                    onSuccess()
                    return@launch
                }
            }

            try {
                repository.saveDiaryEntry(nuevaEntrada)
                _uiState.value = DiaryEntryUiState.Success
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = DiaryEntryUiState.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun cargarEntrada(date: LocalDate) {
        _existingEntry.value = null

        viewModelScope.launch {
            _uiState.value = DiaryEntryUiState.Loading
            try {
                val entry = repository.getDiaryEntryByDate(date.toString())
                _existingEntry.value = entry
                _uiState.value = DiaryEntryUiState.Idle
            } catch (e: Exception) {
                _existingEntry.value = null
                _uiState.value = DiaryEntryUiState.Idle
            }
        }
    }

    fun eliminarEntrada(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = DiaryEntryUiState.Loading
            try {
                repository.deleteDiaryEntry(date.toString())
                _uiState.value = DiaryEntryUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = DiaryEntryUiState.Error("Error al eliminar")
            }
        }
    }

    fun findIndexByLabel(list: List<FeelingData>, label: String?): Int? {
        return list.find { it.label == label }?.id
    }
}