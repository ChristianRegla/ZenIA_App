package com.zenia.app.ui.screens.zenia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.ZeniaRepository
import com.zenia.app.model.MensajeChatbot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(val mensajes: List<MensajeChatbot>) : ChatUiState
    data class Error(val mensaje: String) : ChatUiState
}

class ZeniaChatViewModel(
    private val repositorio: ZeniaRepository
) : ViewModel() {
    val uiState: StateFlow<ChatUiState> = repositorio.getHistorialChat()
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
            val mensajeUsuario = MensajeChatbot(
                emisor = "usuario",
                texto = texto
            )

            repositorio.addChatMessage(mensajeUsuario)
            _isTyping.value = true
            simularRespuestaIA()
        }
    }

    fun eliminarHistorial() {
        viewModelScope.launch {
            try{
                repositorio.deleteChatHistory()
            } catch (e: Exception) {
                // Manejar el error
            }
        }
    }

    private suspend fun simularRespuestaIA() {
        delay(2000)
        val respuestasRandom = listOf(
            "¡Qué interesante! Cuéntame más.",
            "Entiendo cómo te sientes. Estoy aquí para apoyarte.",
            "Esa es una excelente pregunta. En mi base de datos encuentro que...",
            "Soy una versión temprana de ZenIA, pero pronto seré mucho más lista 🧠"
        )
        val mensajeIA = MensajeChatbot(
            emisor = "ia",
            texto = respuestasRandom.random()
        )
        _isTyping.value = false
        repositorio.addChatMessage(mensajeIA)
    }
}