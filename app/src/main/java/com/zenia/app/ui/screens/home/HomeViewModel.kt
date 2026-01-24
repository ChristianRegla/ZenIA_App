package com.zenia.app.ui.screens.home

import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.ContentRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.util.AnalysisUtils
import com.zenia.app.util.ChartUtils
import com.zenia.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface HomeUiState {
    object Idle : HomeUiState
    object Loading : HomeUiState
    object Success : HomeUiState
    data class Error(val message: UiText) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val contentRepository: ContentRepository,
    private val diaryRepository: DiaryRepository,
    private val healthConnectRepository: HealthConnectRepository?,
) : ViewModel() {

    // --- ESTADO DE USUARIO ---
    val userName = authRepository.getUsuarioFlow()
        .map { it?.apodo ?: "Usuario" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")

    val esPremium: StateFlow<Boolean> = authRepository.isPremium
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    // --- DIARIO Y GRÁFICAS (Últimos 7 días) ---
    private val sevenDaysAgo = LocalDate.now().minusDays(7).toString()

    // Solo cargamos los últimos 7 días para la gráfica y el estado de "hoy"
    // Esto es muy ligero
    val registrosDiario = diaryRepository.getEntriesFromDate(sevenDaysAgo)
        .onEach { entradas -> processChartData(entradas) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasEntryToday: StateFlow<Boolean> = registrosDiario.map { lista ->
        val todayStr = LocalDate.now().toString()
        lista.any { it.fecha == todayStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val chartProducer = ChartEntryModelProducer()

    // Para los Insights (Patrones), necesitamos un poco más de contexto (ej. 30 días)
    // Pero lo hacemos en un Flow separado para no afectar la carga inicial de la UI
    val moodInsights = diaryRepository.getEntriesFromDate(
        LocalDate.now().minusDays(30).toString()
    ).map { entries ->
        AnalysisUtils.analyzePatterns(entries)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(null, null))

    // RACHA: Usamos un MutableStateFlow que actualizamos manualmente con la función eficiente del Repo
    private val _currentStreak = MutableStateFlow(0)
    val currentStreak = _currentStreak.asStateFlow()


    // --- COMUNIDAD ---
    val communityActivities = contentRepository.getActividadesComunidad()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- UI STATE GENERAL ---
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState = _uiState.asStateFlow()


    // --- HEALTH CONNECT ---
    private val _hasHealthPermissions = MutableStateFlow(false)
    val hasHealthPermissions: StateFlow<Boolean> = _hasHealthPermissions.asStateFlow()

    val healthConnectStatus: Int
        get() = healthConnectRepository?.getAvailabilityStatus() ?: HealthConnectClient.SDK_UNAVAILABLE

    val isHealthConnectFullyAvailable: Boolean
        get() = healthConnectStatus == HealthConnectClient.SDK_AVAILABLE

    val isHealthConnectAvailable: Boolean = healthConnectRepository != null

    val healthConnectPermissions: Set<String>
        get() = healthConnectRepository?.permissions ?: emptySet()

    val permissionRequestContract
        get() = healthConnectRepository?.getPermissionRequestContract()


    init {
        checkHealthPermissions()
        loadStreak()
    }

    /**
     * Carga la racha usando la función eficiente del repositorio.
     * Esto evita descargar todo el contenido de meses pasados.
     */
    private fun loadStreak() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId
            if (userId != null) {
                try {
                    val streak = diaryRepository.calculateCurrentStreak(userId)
                    _currentStreak.value = streak
                } catch (e: Exception) {
                    // Fallback silencioso o log
                    _currentStreak.value = 0
                }
            }
        }
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

    fun checkHealthPermissions() {
        if (!isHealthConnectFullyAvailable || healthConnectRepository == null) {
            _hasHealthPermissions.value = false
            return
        }
        viewModelScope.launch {
            _hasHealthPermissions.value = healthConnectRepository.hasPermissions()
        }
    }

    suspend fun checkPermissions(): Boolean {
        return healthConnectRepository?.hasPermissions() ?: false
    }

    fun resetState() {
        _uiState.value = HomeUiState.Idle
    }
}