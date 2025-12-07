package com.zenia.app.ui.screens.diary

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import com.zenia.app.data.DiaryRepository
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.ui.theme.ZeniaDream
import com.zenia.app.ui.theme.ZeniaExercise
import com.zenia.app.ui.theme.ZeniaFeelings
import com.zenia.app.ui.theme.ZeniaMind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface DiaryEntryUiState {
    object Idle : DiaryEntryUiState
    object Loading : DiaryEntryUiState
    object Success : DiaryEntryUiState
    object Deleted : DiaryEntryUiState
    data class Error(val msgRes: Int) : DiaryEntryUiState
}

data class FeelingData(val id: Int, val iconRes: Int, val labelRes: Int, val dbValue: String, val color: Color)
data class ActivityData(val labelRes: Int, val dbValue: String)

class DiaryEntryViewModel(
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiaryEntryUiState>(DiaryEntryUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _existingEntry = MutableStateFlow<DiarioEntrada?>(null)
    val existingEntry = _existingEntry.asStateFlow()

    val allEntries = diaryRepository.getDiaryEntriesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val feelings = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, R.string.mood_good, "Bien", ZeniaFeelings),
        FeelingData(1, R.drawable.ic_sol_feli, R.string.mood_happy, "Feliz", ZeniaFeelings),
        FeelingData(2, R.drawable.ic_nube_tite, R.string.mood_discouraged, "Desanimado", ZeniaFeelings),
        FeelingData(3, R.drawable.ic_sol_feli, R.string.mood_joyful, "Alegre", ZeniaFeelings)
    )

    val dreamQuality = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, R.string.sleep_rested, "Descansado", ZeniaDream),
        FeelingData(1, R.drawable.ic_sol_feli, R.string.sleep_energetic, "Energético", ZeniaDream),
        FeelingData(2, R.drawable.ic_nube_tite, R.string.sleep_tired, "Cansado", ZeniaDream),
        FeelingData(3, R.drawable.ic_sol_feli, R.string.sleep_very_good, "Muy bien", ZeniaDream)
    )

    val mind = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, R.string.mind_calm, "Tranquilidad", ZeniaMind),
        FeelingData(1, R.drawable.ic_sol_feli, R.string.mind_clarity, "Claridad", ZeniaMind),
        FeelingData(2, R.drawable.ic_nube_tite, R.string.mind_unmotivated, "Sin motivación", ZeniaMind),
        FeelingData(3, R.drawable.ic_sol_feli, R.string.mind_stressed, "Estresado", ZeniaMind)
    )

    val exercise = listOf(
        FeelingData(0, R.drawable.ic_nube_feli, R.string.exercise_walk, "Caminata", ZeniaExercise),
        FeelingData(1, R.drawable.ic_sol_feli, R.string.exercise_intense, "Intenso", ZeniaExercise),
        FeelingData(2, R.drawable.ic_nube_tite, R.string.exercise_none, "Nada", ZeniaExercise),
        FeelingData(3, R.drawable.ic_sol_feli, R.string.exercise_light, "Ligero", ZeniaExercise)
    )

    val activitiesList = listOf(
        ActivityData(R.string.activity_work, "Trabajo"),
        ActivityData(R.string.activity_exercise, "Ejercicio"),
        ActivityData(R.string.activity_reading, "Lectura"),
        ActivityData(R.string.activity_gaming, "Gaming"),
        ActivityData(R.string.activity_family, "Familia"),
        ActivityData(R.string.activity_friends, "Amigos"),
        ActivityData(R.string.activity_date, "Cita"),
        ActivityData(R.string.activity_trip, "Viaje"),
        ActivityData(R.string.activity_rest, "Descanso")
    )

    fun resetState() { _uiState.value = DiaryEntryUiState.Idle }

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

            val currentUserId = diaryRepository.getCurrentUserId() ?: ""

            if (currentUserId.isBlank()) {
                _uiState.value = DiaryEntryUiState.Error(R.string.diary_error_user_id)
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
                diaryRepository.saveDiaryEntry(nuevaEntrada)
                _uiState.value = DiaryEntryUiState.Success
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = DiaryEntryUiState.Error(R.string.diary_error_save)
                e.printStackTrace()
            }
        }
    }

    fun cargarEntrada(date: LocalDate) {
        _existingEntry.value = null
        viewModelScope.launch {
            _uiState.value = DiaryEntryUiState.Loading
            try {
                val entry = diaryRepository.getDiaryEntryByDate(date.toString())
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
                diaryRepository.deleteDiaryEntry(date.toString())
                _uiState.value = DiaryEntryUiState.Deleted
            } catch (e: Exception) {
                _uiState.value = DiaryEntryUiState.Error(R.string.diary_error_delete)
                e.printStackTrace()
            }
        }
    }

    fun findIndexByDbValue(list: List<FeelingData>, dbValue: String?): Int? {
        return list.find { it.dbValue == dbValue }?.id
    }
}