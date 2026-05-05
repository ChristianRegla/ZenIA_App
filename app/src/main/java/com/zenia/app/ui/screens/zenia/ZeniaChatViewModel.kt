package com.zenia.app.ui.screens.zenia

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.R
import com.zenia.app.data.ChatRepository
import com.zenia.app.data.DiaryRepository
import com.zenia.app.data.HealthConnectRepository
import com.zenia.app.data.NiaApiRepository
import com.zenia.app.data.UserPreferencesRepository
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.di.ApplicationScope
import com.zenia.app.model.MensajeChatbot
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(val mensajes: List<MensajeChatbot>) : ChatUiState
    data class Error(val mensaje: String) : ChatUiState
}

sealed interface ChatUiEvent {
    data class ShowError(val message: String) : ChatUiEvent
}

enum class EmergencyDisplayState {
    NONE,
    BANNER,
    MINIMIZED
}

@HiltViewModel
class ZeniaChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val chatRepository: ChatRepository,
    private val niaRepository: NiaApiRepository,
    private val healthConnectRepository: HealthConnectRepository,
    private val userPreferences: UserPreferencesRepository,
    private val diaryRepository: DiaryRepository,
    sessionManager: UserSessionManager,
    @ApplicationScope private val externalScope: CoroutineScope
) : ViewModel() {

    private val _uiEvent = Channel<ChatUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<ChatUiState> = chatRepository.getHistorialChat()
        .map { ChatUiState.Success(it) as ChatUiState }
        .catch { emit(ChatUiState.Error(it.message ?: "Error desconocido")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatUiState.Loading
        )

    private val _isTyping = MutableStateFlow(false)
    val isTyping = _isTyping.asStateFlow()

    private val _emergencyType = MutableStateFlow<String?>(null)
    val emergencyType = _emergencyType.asStateFlow()

    private val _emergencyDisplay = MutableStateFlow(EmergencyDisplayState.NONE)
    val emergencyDisplay = _emergencyDisplay.asStateFlow()

    val isPremium = sessionManager.isPremium

    val nickname: StateFlow<String> = sessionManager.nickname

    private val _selectedDiaryDate = MutableStateFlow<String?>(null)
    val selectedDiaryDate = _selectedDiaryDate.asStateFlow()

    private val _selectedDiaryEntry = MutableStateFlow<String?>(null)
    val selectedDiaryEntry = _selectedDiaryEntry.asStateFlow()

    val shareHealthData = userPreferences.shareHealthDataWithNia.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    private fun generarResumenDiario(entry: com.zenia.app.model.DiarioEntrada, fechaIso: String): String {
        val moodString = when (entry.estadoAnimo) {
            "1" -> context.getString(R.string.mood_bad)
            "2" -> context.getString(R.string.mood_regular)
            "3" -> context.getString(R.string.mood_good)
            "4" -> context.getString(R.string.mood_excellent)
            else -> context.getString(R.string.unspecified)
        }

        val sleepString = when (entry.calidadSueno) {
            "1" -> context.getString(R.string.sleep_bad)
            "2" -> context.getString(R.string.sleep_regular)
            "3" -> context.getString(R.string.sleep_good)
            "4" -> context.getString(R.string.sleep_excellent)
            else -> context.getString(R.string.sleep_unspecified)
        }

        return StringBuilder().apply {
            append(context.getString(R.string.summary_base, fechaIso, moodString, sleepString))

            if (entry.isFavorite) {
                append(context.getString(R.string.summary_favorite))
            }

            if (entry.actividades.isNotEmpty()) {
                append(context.getString(R.string.summary_activities, entry.actividades.joinToString(", ")))
            }

            if (entry.notas.isNotBlank()) {
                append(context.getString(R.string.summary_notes, entry.notas))
            }
        }.toString()
    }

    fun seleccionarFechaDiario(fechaIso: String) {
        viewModelScope.launch {
            try {
                val entry = diaryRepository.getDiaryEntryByDate(fechaIso)

                if (entry != null) {
                    _selectedDiaryDate.value = fechaIso
                    _selectedDiaryEntry.value = generarResumenDiario(entry, fechaIso)
                } else {
                    _selectedDiaryDate.value = null
                    _selectedDiaryEntry.value = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun limpiarEntradaSeleccionada() {
        _selectedDiaryDate.value = null
        _selectedDiaryEntry.value = null
    }

    fun toggleHealthDataSharing(share: Boolean) {
        viewModelScope.launch {
            userPreferences.setShareHealthDataWithNia(share)
        }
    }

    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return

        viewModelScope.launch {
            var textoFinal = texto

            if (texto == context.getString(R.string.suggestion_analyze)) {
                val hoy = LocalDate.now().toString()
                try {
                    val entry = diaryRepository.getDiaryEntryByDate(hoy)
                    if (entry != null) {
                        val resumen = generarResumenDiario(entry, hoy)
                        textoFinal = "${context.getString(R.string.suggestion_analyze)}\n\n(Contexto del $hoy: $resumen)"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val mensajeUsuario = MensajeChatbot(
                emisor = "usuario",
                texto = textoFinal
            )

            val historialActual = (uiState.value as? ChatUiState.Success)?.mensajes ?: emptyList()

            val historialCompleto = historialActual + mensajeUsuario

            val historialParaMandar = historialCompleto.takeLast(20)

            try {
                chatRepository.addChatMessage(mensajeUsuario)
                _isTyping.value = true

                obtenerRespuestaIA(historialParaMandar)
            } catch (e: Exception) {
                _uiEvent.send(ChatUiEvent.ShowError(context.getString(R.string.error_send_message)))
            }
        }
    }

    fun eliminarHistorial() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState is ChatUiState.Success) {
                val todosLosIds = currentState.mensajes.map { it.id }.toSet()

                if (todosLosIds.isNotEmpty()) {
                    val result = chatRepository.deleteMessagesByIds(todosLosIds)

                    if (result.isFailure) {
                        _uiEvent.send(ChatUiEvent.ShowError(context.getString(R.string.error_delete_history)))
                        android.util.Log.e("ZeniaChatVM", "Error deleting history", result.exceptionOrNull())
                    }
                }
            }
        }
    }

    fun eliminarMensajesSeleccionados(ids: Set<String>) {
        viewModelScope.launch {
            val result = chatRepository.deleteMessagesByIds(ids)

            if (result.isFailure) {
                _uiEvent.send(ChatUiEvent.ShowError(context.getString(R.string.error_delete_messages)))
            }
        }
    }

    private fun obtenerRespuestaIA(historial: List<MensajeChatbot>) {
        externalScope.launch {

            val fechaActual = LocalDate.now().toString()

            var contextString = "El usuario prefiere que lo llames ${nickname.value}.\n\n" +
                    "REGLAS CRÍTICAS DE TIEMPO Y CONTEXTO:\n" +
                    "1. La fecha actual (HOY) es $fechaActual.\n" +
                    "2. Si en el mensaje el usuario incluye un texto entre paréntesis como '(Contexto del YYYY-MM-DD: ...)', significa que te adjuntó una memoria de su diario.\n" +
                    "3. Si el usuario usa palabras como 'este día', 'ese día', 'ayer', o 'cómo me fue', y existe un contexto adjunto en ese mensaje, se refiere ESTRICTAMENTE a la fecha del diario adjunto, NUNCA a hoy.\n" +
                    "4. Analiza y responde sobre ese día en tiempo pasado. Ignora por completo que es hoy."

            val userWantsToShare = shareHealthData.value

            if (isPremium.value && userWantsToShare) {
                try {
                    val summary = healthConnectRepository.getHealthSummary()
                    val ritmo = summary.heartRateAvg?.let { "$it bpm" } ?: "Desconocido"
                    val sueno = String.format(java.util.Locale.US, "%.1f", summary.sleepHours)

                    contextString += "Contexto biológico actual: " +
                            "Pasos hoy: ${summary.steps}. " +
                            "Ritmo cardíaco: $ritmo. " +
                            "Horas de sueño anoche: $sueno hrs. " +
                            "Nivel de estrés (HRV): ${summary.stressLevel}."
                } catch (e: Exception) {
                    android.util.Log.e("ZeniaChatVM", "Error leyendo Health Connect", e)
                }
            }

            val result = niaRepository.enviarMensaje(historial, contextString)

            _isTyping.value = false

            result.onSuccess { response ->
                val mensajeIA = MensajeChatbot(
                    emisor = "ia",
                    texto = response.mensaje_nia
                )

                chatRepository.addChatMessage(mensajeIA)
                manejarTrigger(response.trigger)
            }.onFailure {
                _uiEvent.send(
                    ChatUiEvent.ShowError(context.getString(R.string.error_nia_connection))
                )
            }
        }
    }

    private fun manejarTrigger(trigger: String?) {
        when(trigger){
            "physical_risk", "mental_health_emergency" -> {
                _emergencyType.value = trigger
                _emergencyDisplay.value = EmergencyDisplayState.BANNER

                viewModelScope.launch {
                    delay(12000)
                    if (_emergencyDisplay.value == EmergencyDisplayState.BANNER) {
                        _emergencyDisplay.value = EmergencyDisplayState.MINIMIZED
                    }
                }
            }
            "none" -> clearEmergency()
            else -> {}
        }
    }

    fun dismissBannerToIcon() {
        _emergencyDisplay.value = EmergencyDisplayState.MINIMIZED
    }

    fun restoreBanner() {
        _emergencyDisplay.value = EmergencyDisplayState.BANNER
    }

    fun clearEmergency() {
        _emergencyType.value = null
        _emergencyDisplay.value = EmergencyDisplayState.NONE
    }
}