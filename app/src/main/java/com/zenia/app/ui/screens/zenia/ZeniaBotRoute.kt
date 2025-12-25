package com.zenia.app.ui.screens.zenia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ZeniaBotRoute(
    onNavigateBack: () -> Unit
) {
    val viewModel: ZeniaChatViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    ZeniaBotScreen(
        uiState = uiState,
        isTyping = isTyping,
        onSendMessage = { viewModel.enviarMensaje(it) },
        onClearChat = { viewModel.eliminarHistorial() },
        onNavigateBack = onNavigateBack
    )
}