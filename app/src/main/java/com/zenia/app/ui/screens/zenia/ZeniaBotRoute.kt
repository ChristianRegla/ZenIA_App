package com.zenia.app.ui.screens.zenia

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenia.app.viewmodel.AppViewModelProvider

@Composable
fun ZeniaBotRoute() {
    val viewModel: ZeniaChatViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()

    ZeniaBotScreen(
        uiState = uiState,
        onSendMessage = { viewModel.enviarMensaje(it) },
        onClearChat = { viewModel.eliminarHistorial() }
    )
}