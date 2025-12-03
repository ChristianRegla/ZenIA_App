package com.zenia.app.ui.screens.diary

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import com.zenia.app.data.ZeniaRepository
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.theme.ZeniaDream
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaMind
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

data class FeelingData(val id: Int, val iconRes: Int, val label: String, val color: Color)

class DiaryEntryViewModel(
    private val repository: ZeniaRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<DiaryEntryUiState>(DiaryEntryUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _existingEntry = MutableStateFlow<DiarioEntrada?>(null)
    val existingEntry = _existingEntry.asStateFlow()

    val feelings = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Bien", color = ZeniaFeelings),
        FeelingData(1, R.drawable.ic_sol_feli, "Feliz", color = ZeniaFeelings),
        FeelingData(2, R.drawable.ic_nube_tite, "Desanimado", color = ZeniaFeelings),
        FeelingData(3, R.drawable.ic_sol_feli, "Alegre", color = ZeniaFeelings)
    )

    val dreamQuality = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Descansado", color = ZeniaDream),
        FeelingData(1, R.drawable.ic_sol_feli, "Energético", color = ZeniaDream),
        FeelingData(2, R.drawable.ic_nube_tite, "Cansado", color = ZeniaDream),
        FeelingData(3, R.drawable.ic_sol_feli, "Muy bien", color = ZeniaDream)
    )

    val mind = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Tranquilidad", color = ZeniaMind),
        FeelingData(1, R.drawable.ic_sol_feli, "Claridad", color = ZeniaMind),
        FeelingData(2, R.drawable.ic_nube_tite, "Sin motivación", color = ZeniaMind),
        FeelingData(3, R.drawable.ic_sol_feli, "Estresado", color = ZeniaMind)
    )

    val exercise = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, "Caminata", color = ZeniaExercise),
        FeelingData(1, R.drawable.ic_sol_feli, "Intenso", color = ZeniaExercise),
        FeelingData(2, R.drawable.ic_nube_tite, "Nada", color = ZeniaExercise),
        FeelingData(3, R.drawable.ic_sol_feli, "Ligero", color = ZeniaExercise)
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
                e.printStackTrace()
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
                e.printStackTrace()
            }
        }
    }

    fun findIndexByLabel(list: List<FeelingData>, label: String?): Int? {
        return list.find { it.label == label }?.id
    }
}