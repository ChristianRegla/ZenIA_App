package com.zenia.app.ui.screens.diary

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
    sessionManager: UserSessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<DiaryEntryUiState>(DiaryEntryUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _existingEntry = MutableStateFlow<DiarioEntrada?>(null)
    val existingEntry = _existingEntry.asStateFlow()

    private val _healthConnectData = MutableStateFlow<HealthDataResult?>(null)
    val healthConnectData = _healthConnectData.asStateFlow()

    private val _categoriasUsuario = MutableStateFlow<List<CategoriaDiario>>(emptyList())
    val categoriasUsuario = _categoriasUsuario.asStateFlow()

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

    init {
        cargarCategorias()
    }

    fun resetState() { _uiState.value = DiaryEntryUiState.Idle }

    private fun cargarCategorias() {
        viewModelScope.launch {
            val defaults = listOf(
                CategoriaDiario(
                    idCategoria = "estadoAnimo",
                    tituloPersonalizado = "Estado de Ánimo",
                    opciones = listOf(
                        OpcionCategoria(4, "Increíble", "ic_sol_feli"),
                        OpcionCategoria(3, "Bien", "ic_nube_feli"),
                        OpcionCategoria(2, "Desanimado", "ic_sol_tite"),
                        OpcionCategoria(1, "Terrible", "ic_nube_tite")
                    )
                ),
                CategoriaDiario(
                    idCategoria = "calidadSueno",
                    tituloPersonalizado = "Calidad del Sueño",
                    opciones = listOf(
                        OpcionCategoria(4, "Muy bien", "ic_sol_feli"),
                        OpcionCategoria(3, "Descansado", "ic_nube_feli"),
                        OpcionCategoria(2, "Cansado", "ic_nube_tite"),
                        OpcionCategoria(1, "Insomnio", "ic_sol_tite")
                    )
                ),
                CategoriaDiario(
                    idCategoria = "estadoMental",
                    tituloPersonalizado = "Estado Mental",
                    opciones = listOf(
                        OpcionCategoria(4, "Claridad", "ic_sol_feli"),
                        OpcionCategoria(3, "Tranquilidad", "ic_nube_feli"),
                        OpcionCategoria(2, "Estrés", "ic_nube_tite"),
                        OpcionCategoria(1, "Caos", "ic_sol_tite")
                    )
                ),
                CategoriaDiario(
                    idCategoria = "ejercicio",
                    tituloPersonalizado = "Intensidad del Ejercicio",
                    opciones = listOf(
                        OpcionCategoria(4, "Intenso", "ic_sol_feli"),
                        OpcionCategoria(3, "Moderado", "ic_nube_feli"),
                        OpcionCategoria(2, "Ligero", "ic_nube_tite"),
                        OpcionCategoria(1, "Nada", "ic_sol_tite")
                    )
                )
            )
            _categoriasUsuario.value = defaults
        }
    }

    fun recargarDatosDeSalud(date: LocalDate) {
        viewModelScope.launch {
            obtenerDatosDeSaludDelDia(date)
        }
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
                hcHrv = hcHrv
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
                    "feliz", "increíble", "excelente", "5" -> R.string.feedback_happy
                    "bien", "contento", "4" -> R.string.feedback_good
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

    val limiteCategorias = 7
    fun agregarCategoriaPersonalizada(nuevaCategoria: CategoriaDiario) {
        val actuales = _categoriasUsuario.value.toMutableList()
        if (actuales.size < limiteCategorias) {
            actuales.add(nuevaCategoria)
            _categoriasUsuario.value = actuales
        }
    }

    fun actualizarCategoria(categoriaModificada: CategoriaDiario) {
        val actuales = _categoriasUsuario.value.toMutableList()
        val index = actuales.indexOfFirst { it.idCategoria == categoriaModificada.idCategoria }
        if (index != -1) {
            actuales[index] = categoriaModificada
            _categoriasUsuario.value = actuales
        }
    }

    fun eliminarCategoria(idCategoria: String) {
        val actuales = _categoriasUsuario.value.toMutableList()
        actuales.removeAll { it.idCategoria == idCategoria }
        _categoriasUsuario.value = actuales
    }

    private suspend fun obtenerDatosDeSaludDelDia(date: LocalDate) {
        val hc = healthConnectRepository ?: return

        try {
            val pasos = hc.readStepsByDate(date)
            val sueno = hc.readLastNightSleepDurationByDate(date)

            val suenoMinutos = sueno.totalMinutes

            _healthConnectData.value = HealthDataResult(
                pasos = pasos.takeIf { it > 0 },
                minutosSueno = suenoMinutos.takeIf { it > 0 },
                ritmoCardiaco = null,
                hrv = null
            )
        } catch (e: Exception) {
            e.printStackTrace()
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
}