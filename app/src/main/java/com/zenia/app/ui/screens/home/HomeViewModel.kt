package com.zenia.app.ui.screens.home

import android.app.Application
import androidx.health.connect.client.HealthConnectClient
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.zenia.app.R
import com.zenia.app.data.AuthRepository
import com.zenia.app.data.ContentRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.model.RegistroBienestar
import com.zenia.app.util.ChartUtils
import com.zenia.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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

    // --- ESTADO DEL USUARIO ---
    val userName = authRepository.getUsuarioFlow()
        .map { it?.apodo ?: "Usuario" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Cargando...")

    // --- GRÁFICA DE EMOCIONES ---
    // Productor de datos para Vico
    val chartProducer = ChartEntryModelProducer()

    // Observamos los registros y actualizamos la gráfica automáticamente
    val registrosDiario = diaryRepository.diaryEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasEntryToday: StateFlow<Boolean> = registrosDiario.map { lista ->
        val todayStr = LocalDate.now().toString() // "2025-12-25"
        lista.any { it.fecha == todayStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
     * Expone el flujo de registros de bienestar del usuario desde Firestore,
     * ordenados por fecha descendente. Maneja errores de carga.
     */
    val registros = diaryRepository.getRegistrosBienestar()
        .catch {
            _uiState.value = HomeUiState.Error(UiText.StringResource(R.string.error_loading_records))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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
    /**
     * Bloque de inicialización.
     * Comprueba los permisos de Health Connect en cuanto se crea el ViewModel.
     */
    init {
        viewModelScope.launch {
            checkPermissions()
        }
    }

    fun processChartData(registros: List<DiarioEntrada>) {
        android.util.Log.d("ZENIA_CHART", "Procesando ${registros.size} entradas del diario")

        val entries = registros
            .filter { !it.estadoAnimo.isNullOrBlank() }
            .mapNotNull { entrada ->
                try {
                    val date = LocalDate.parse(entrada.fecha)
                    val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toFloat()
                    val moodValue = ChartUtils.mapMoodToValue(entrada.estadoAnimo)

                    if (moodValue > 0f) {
                        entryOf(millis, moodValue)
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            .sortedBy { it.x }
            .takeLast(7)

        if (entries.isNotEmpty()) {
            android.util.Log.d("ZENIA_CHART", "Graficando ${entries.size} puntos")
            chartProducer.setEntries(entries)
        } else {
            android.util.Log.d("ZENIA_CHART", "No hay datos válidos para graficar")
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
     * Guarda un nuevo registro de bienestar en Firestore.
     * Si el usuario es [esPremium] y [hasHealthPermissions], también intenta
     * leer el promedio de ritmo cardíaco del último día y lo adjunta al registro.
     *
     * @param estado El estado de ánimo seleccionado por el usuario.
     * @param notas Las notas de diario opcionales.
     */
    fun guardarRegistro(estado: String, notas: String) {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            try {
                var avgHeartRate: Int? = null

                if (isHealthConnectAvailable && hasHealthPermissions.value && esPremium.value) {
                    avgHeartRate = healthConnectRepository?.readDailyHeartRateAverage()
                }
                val nuevoRegistro = RegistroBienestar(
                    estadoAnimo = estado,
                    notas = notas,
                    frecuenciaCardiaca = avgHeartRate
                )
                diaryRepository.addRegistroBienestar(nuevoRegistro)
                _uiState.value = HomeUiState.Success
            } catch (e: Exception) {
                val errorText = e.message?.let { UiText.DynamicString(it) }
                    ?: UiText.StringResource(R.string.error_saving_record)

                _uiState.value = HomeUiState.Error(errorText)
            }
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