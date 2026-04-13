package com.zenia.app.ui.screens.diary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.model.CategoriaDiario
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.model.OpcionCategoria
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface DiaryEntryUiState {
    object Idle : DiaryEntryUiState
    object Loading : DiaryEntryUiState
    data class Success(val messageRes: Int) : DiaryEntryUiState
    object Deleted : DiaryEntryUiState
    data class Error(val msgRes: Int) : DiaryEntryUiState
}

data class ActivityData(val labelRes: Int, val dbValue: String)
data class HealthDataResult(
    val pasos: Long? = null,
    val ritmoCardiaco: Int? = null,
    val minutosSueno: Int? = null,
    val hrv: Int? = null
)

@HiltViewModel
class DiaryEntryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val healthConnectRepository: HealthConnectRepository?,
    sessionManager: UserSessionManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiaryEntryUiState>(DiaryEntryUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _existingEntry = MutableStateFlow<DiarioEntrada?>(null)
    val existingEntry = _existingEntry.asStateFlow()

    private val _healthConnectData = MutableStateFlow<HealthDataResult?>(null)
    val healthConnectData = _healthConnectData.asStateFlow()

    val isPremium = sessionManager.isPremium

    val allEntries = diaryRepository.getDiaryEntriesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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

    val limiteCategorias = 7

    private val defaultsCategorias = listOf(
        CategoriaDiario("estadoAnimo", context.getString(R.string.category_mood), listOf(
            OpcionCategoria(4, context.getString(R.string.mood_incredible), "sol_muy_feliz"),
            OpcionCategoria(3, context.getString(R.string.mood_good), "sol_feliz"),
            OpcionCategoria(2, context.getString(R.string.mood_down), "sol_mid"),
            OpcionCategoria(1, context.getString(R.string.mood_terrible), "sol_triste")
        )),
        CategoriaDiario("calidadSueno", context.getString(R.string.category_sleep), listOf(
            OpcionCategoria(4, context.getString(R.string.sleep_very_good), "nube_muy_feliz"),
            OpcionCategoria(3, context.getString(R.string.sleep_rested), "nube_feliz"),
            OpcionCategoria(2, context.getString(R.string.sleep_tired), "nube_mid"),
            OpcionCategoria(1, context.getString(R.string.sleep_insomnia), "nube_triste")
        )),
        CategoriaDiario("estadoMental", context.getString(R.string.category_mental), listOf(
            OpcionCategoria(4, context.getString(R.string.mental_clarity), "superhappy"),
            OpcionCategoria(3, context.getString(R.string.mental_calm), "happyface"),
            OpcionCategoria(2, context.getString(R.string.mental_stress), "sadface"),
            OpcionCategoria(1, context.getString(R.string.mental_chaos), "supersad")
        )),
        CategoriaDiario("ejercicio", context.getString(R.string.category_exercise), listOf(
            OpcionCategoria(4, context.getString(R.string.exercise_intense), "happy1"),
            OpcionCategoria(3, context.getString(R.string.exercise_moderate), "happy2"),
            OpcionCategoria(2, context.getString(R.string.exercise_light), "sad"),
            OpcionCategoria(1, context.getString(R.string.exercise_none), "sad2")
        ))
    )

    val categoriasUsuario = combine(
        MutableStateFlow(defaultsCategorias),
        diaryRepository.getCustomCategoriesStream()
    ) { defs, customs ->
        val customsMap = customs.associateBy { it.idCategoria }
        val mergedDefaults = defs.map { customsMap[it.idCategoria] ?: it }
        val pureCustoms = customs.filter { it.idCategoria !in defs.map { d -> d.idCategoria } }

        mergedDefaults + pureCustoms
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultsCategorias)


    fun resetState() { _uiState.value = DiaryEntryUiState.Idle }

    fun recargarDatosDeSalud(date: LocalDate) {
        viewModelScope.launch { obtenerDatosDeSaludDelDia(date) }
    }

    fun guardarEntrada(
        date: LocalDate,
        selecciones: Map<String, String>,
        actividades: List<String>,
        notas: String,
        hcPasos: Int?,
        hcRitmoCardiaco: Int?,
        hcMinutosSueno: Int?,
        hcHrv: Int?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = DiaryEntryUiState.Loading

            val currentDbEntry = diaryRepository.getDiaryEntryByDate(date.toString())
            val isFav = currentDbEntry?.isFavorite ?: false

            val categoriasBase = listOf("estadoAnimo", "calidadSueno", "estadoMental", "ejercicio")
            val extras = selecciones.filterKeys { it !in categoriasBase }

            val nuevaEntrada = DiarioEntrada(
                userId = "",
                fecha = date.toString(),
                estadoAnimo = selecciones["estadoAnimo"],
                calidadSueno = selecciones["calidadSueno"],
                estadoMental = selecciones["estadoMental"],
                ejercicio = selecciones["ejercicio"],
                categoriasExtra = extras,
                actividades = actividades,
                notas = notas,
                hcPasos = hcPasos,
                hcRitmoCardiaco = hcRitmoCardiaco,
                hcMinutosSueno = hcMinutosSueno,
                hcHrv = hcHrv,
                isFavorite = isFav
            )

            val entradaActual = _existingEntry.value
            if (entradaActual != null) {
                val entradaParaComparar = nuevaEntrada.copy(timestamp = entradaActual.timestamp)
                if (entradaActual == entradaParaComparar) {
                    _uiState.value = DiaryEntryUiState.Success(R.string.diary_toast_saved)
                    onSuccess()
                    return@launch
                }
            }

            try {
                diaryRepository.saveDiaryEntry(nuevaEntrada)
                val feedbackMsg = when (selecciones["estadoAnimo"]?.lowercase()?.trim()) {
                    "feliz", "increíble", "excelente", "5", "4" -> R.string.feedback_happy
                    "bien", "contento", "3" -> R.string.feedback_good
                    "mal", "triste", "cansado", "2" -> R.string.feedback_sad
                    "terrible", "pésimo", "1" -> R.string.feedback_awful
                    else -> R.string.feedback_neutral
                }
                _uiState.value = DiaryEntryUiState.Success(feedbackMsg)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = DiaryEntryUiState.Error(R.string.diary_error_save)
                e.printStackTrace()
            }
        }
    }

    fun cargarEntrada(date: LocalDate) {
        _existingEntry.value = null
        _healthConnectData.value = null

        viewModelScope.launch {
            _uiState.value = DiaryEntryUiState.Loading
            try {
                val entry = diaryRepository.getDiaryEntryByDate(date.toString())
                _existingEntry.value = entry

                if (entry == null || (entry.hcPasos == null && entry.hcMinutosSueno == null)) {
                    obtenerDatosDeSaludDelDia(date)
                }

                _uiState.value = DiaryEntryUiState.Idle
            } catch (e: Exception) {
                _existingEntry.value = null
                _uiState.value = DiaryEntryUiState.Idle
                e.printStackTrace()
            }
        }
    }

    fun toggleFavoriteDirect(date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val existingEntry = diaryRepository.getDiaryEntryByDate(dateStr)

            if (existingEntry != null) {
                diaryRepository.saveDiaryEntry(existingEntry.copy(isFavorite = !existingEntry.isFavorite))
            } else {
                val newEntry = DiarioEntrada(fecha = dateStr, isFavorite = true)
                diaryRepository.saveDiaryEntry(newEntry)
            }
        }
    }

    fun agregarCategoriaPersonalizada(nuevaCategoria: CategoriaDiario) {
        viewModelScope.launch {
            if (categoriasUsuario.value.size < limiteCategorias) {
                try { diaryRepository.saveCustomCategory(nuevaCategoria) }
                catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    fun actualizarCategoria(categoriaModificada: CategoriaDiario) {
        viewModelScope.launch {
            try { diaryRepository.saveCustomCategory(categoriaModificada) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun eliminarCategoria(idCategoria: String) {
        viewModelScope.launch {
            try { diaryRepository.deleteCustomCategory(idCategoria) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun obtenerDatosDeSaludDelDia(date: LocalDate) {
        val hc = healthConnectRepository ?: return
        try {
            val pasos = hc.readStepsByDate(date)
            val sueno = hc.readLastNightSleepDurationByDate(date)

            _healthConnectData.value = HealthDataResult(
                pasos = pasos.takeIf { it > 0 },
                minutosSueno = sueno.totalMinutes.takeIf { it > 0 },
                ritmoCardiaco = null,
                hrv = null
            )
        } catch (e: Exception) { e.printStackTrace() }
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
}