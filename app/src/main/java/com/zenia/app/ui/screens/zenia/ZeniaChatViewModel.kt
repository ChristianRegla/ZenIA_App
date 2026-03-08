package com.zenia.app.ui.screens.zenia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.ChatRepository
import com.zenia.app.data.NiaApiRepository
import com.zenia.app.model.MensajeChatbot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(val mensajes: List<MensajeChatbot>) : ChatUiState
    data class Error(val mensaje: String) : ChatUiState
}

sealed interface ChatUiEvent {
    data class ShowError(val message: String) : ChatUiEvent
}

@HiltViewModel
class ZeniaChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val niaRepository: NiaApiRepository
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

    fun enviarMensaje(texto: String) {
        if (texto.isBlank()) return

        viewModelScope.launch {
            val mensajeUsuario = MensajeChatbot(emisor = "usuario", texto = texto)

            try {
                chatRepository.addChatMessage(mensajeUsuario)
                _isTyping.value = true
                obtenerRespuestaIA(texto)
            } catch (e: Exception) {
                _uiEvent.send(ChatUiEvent.ShowError("No se pudo enviar el mensaje"))
            }
        }
    }

    fun eliminarHistorial() {
        viewModelScope.launch {
            try {
                chatRepository.deleteChatHistory()
            } catch (e: Exception) {
                _uiEvent.send(ChatUiEvent.ShowError("No se pudo borrar el historial"))
                android.util.Log.e("ZeniaChatVM", "Error deleting", e)
            }
        }
    }

    fun eliminarMensajesSeleccionados(ids: Set<String>) {
        viewModelScope.launch {
            try {
                chatRepository.deleteMessagesByIds(ids)
            } catch (e: Exception) {
                _uiEvent.send(
                    ChatUiEvent.ShowError("No se pudieron eliminar los mensajes")
                )
            }
        }
    }

    private suspend fun obtenerRespuestaIA(mensaje: String) {

        val result = niaRepository.enviarMensaje(mensaje)

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
                ChatUiEvent.ShowError("Error al conectar con Nia")
            )
        }
    }

    private fun manejarTrigger(trigger: String?) {

        when(trigger){

            "breathing" -> {
                // iniciar ejercicio respiración
            }

            "grounding" -> {
                // ejercicio grounding
            }

            "emergency" -> {
                // mostrar ayuda urgente
            }

            else -> {}
        }
    }
}