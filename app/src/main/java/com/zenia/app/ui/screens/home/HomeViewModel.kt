package com.zenia.app.ui.screens.home

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entriesOf
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.R
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.ContentRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.model.RegistroBienestar
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import com.zenia.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Define los posibles estados de la UI para la pantalla principal,
 * específicamente para operaciones asíncronas como guardar un registro.
 */
sealed interface HomeUiState {
    object Idle : HomeUiState
    object Loading : HomeUiState
    object Success : HomeUiState
    data class Error(val message: UiText) : HomeUiState
}
/**
 * ViewModel para la [HomeScreen].
 * Se encarga de gestionar el estado de la UI, obtener los registros de bienestar,
 * manejar la lógica de Health Connect (permisos y lectura) y el estado de suscripción del usuario.
 *
 * @param repositorio El repositorio para interactuar con Firestore (registros, datos de usuario).
 * @param healthConnectRepository Repositorio para interactuar con la API de Health Connect. Es nulable si el SDK no está disponible.
 * @param application La instancia de la aplicación para acceder a recursos (strings).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val contentRepository: ContentRepository,
    private val diaryRepository: DiaryRepository,
    private val healthConnectRepository: HealthConnectRepository?,
) : ViewModel() {

    val userName = authRepository.getUsuarioFlow()
        .map { it?.apodo ?: "Usuario" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")

    private val sevenDaysAgo = LocalDate.now().minusDays(7).toString()

    val registrosDiario = diaryRepository.getEntriesFromDate(sevenDaysAgo)
        .onEach { entradas -> processChartData(entradas) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasEntryToday: StateFlow<Boolean> = registrosDiario.map { lista ->
        val todayStr = LocalDate.now().toString()
        lista.any { it.fecha == todayStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // --- GRÁFICA DE EMOCIONES ---
    // Productor de datos para Vico
    val chartProducer = ChartEntryModelProducer()

    // Estado derivado: Actividades de comunidad (Mock o Repo)
    val communityActivities = contentRepository.getActividadesComunidad()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    /**
     * StateFlow interno para el estado de la UI (Cargando, Éxito, Error) al guardar un registro.
     */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState = _uiState.asStateFlow()
    /**
     * Expone un boolean que indica si el usuario actual tiene una suscripción "premium".
     * Se actualiza en tiempo real observando el documento del usuario en Firestore.
     */
    val esPremium: StateFlow<Boolean> = authRepository.getUsuarioFlow()
        .map { it?.suscripcion == "premium" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /**
     * StateFlow interno para rastrear si se tienen los permisos de Health Connect.
     */
    private val _hasHealthPermissions = MutableStateFlow(false)
    /**
     * Expone si la app tiene (o no) los permisos necesarios de Health Connect.
     */
    val hasHealthPermissions: StateFlow<Boolean> = _hasHealthPermissions.asStateFlow()
    // Exponemos el estado completo del SDK.
    val healthConnectStatus: Int
        get() = healthConnectRepository?.getAvailabilityStatus() ?: HealthConnectClient.SDK_UNAVAILABLE
    // Helper para saber si podemos intentar operaciones (solo si está instalado Y disponible)
    val isHealthConnectFullyAvailable: Boolean
        get() = healthConnectStatus == HealthConnectClient.SDK_AVAILABLE
    /**
     * Indica si el SDK de Health Connect está disponible en el dispositivo.
     * Se basa en si [healthConnectRepository] pudo ser inicializado.
     */
    val isHealthConnectAvailable: Boolean = healthConnectRepository != null
    /**
     * Expone el conjunto de permisos de Health Connect que la app requiere (ej. leer ritmo cardíaco).
     */
    val healthConnectPermissions: Set<String>
        get() = healthConnectRepository?.permissions ?: emptySet()
    /**
     * Expone el 'contrato' de ActivityResult que la UI debe usar para solicitar
     * los permisos de Health Connect.
     */
    val permissionRequestContract
        get() = healthConnectRepository?.getPermissionRequestContract()

    val healthConnectAvailability = healthConnectRepository?.getAvailabilityStatus()
        ?: HealthConnectClient.SDK_UNAVAILABLE

    val currentStreak: StateFlow<Int> = diaryRepository.getEntriesFromDate(
        LocalDate.now().minusDays(30).toString()
    ).map { entries ->
        calculateStreak(entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val moodInsights = diaryRepository.getEntriesFromDate(
        LocalDate.now().minusDays(30).toString()
    ).map { entries ->
        AnalysisUtils.analyzePatterns(entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, null))

    /**
     * Bloque de inicialización.
     * Comprueba los permisos de Health Connect en cuanto se crea el ViewModel.
     */
    init {
        checkHealthPermissions()
    }

    private fun calculateStreak(entries: List<DiarioEntrada>): Int {
        if (entries.isEmpty()) return 0

        val recordedDates = entries.map { it.fecha }.toSet()
        var streak = 0
        var checkDate = LocalDate.now()

        if (!recordedDates.contains(checkDate.toString())) {
            checkDate = checkDate.minusDays(1)
        }

        while (recordedDates.contains(checkDate.toString())) {
            streak++
            checkDate = checkDate.minusDays(1)
        }
        return streak
    }

    private fun processChartData(registros: List<DiarioEntrada>) {
        val entries = registros
            .filter { !it.estadoAnimo.isNullOrBlank() }
            .mapNotNull { entrada ->
                try {
                    val date = LocalDate.parse(entrada.fecha)
                    val xValue = date.toEpochDay().toFloat()
                    val moodValue = ChartUtils.mapMoodToValue(entrada.estadoAnimo)

                    if (moodValue > 0f) {
                        entryOf(xValue, moodValue)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.x }
            .takeLast(7)

        if (entries.isNotEmpty()) {
            chartProducer.setEntries(entries)
        }
    }

    suspend fun checkPermissions(): Boolean {
        return healthConnectRepository?.hasPermissions() ?: false
    }
    /**
     * Comprueba si la app tiene actualmente los permisos de Health Connect
     * y actualiza el [hasHealthPermissions] StateFlow.
     */
    fun checkHealthPermissions() {
        if (!isHealthConnectFullyAvailable || healthConnectRepository == null) {
            _hasHealthPermissions.value = false
            return
        }
        viewModelScope.launch {
            _hasHealthPermissions.value = healthConnectRepository.hasPermissions()
        }
    }

    /**
     * Resetea el estado de la UI a [HomeUiState.Idle].
     * Útil para ocultar un Snackbar de error o éxito después de un tiempo.
     */
    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}