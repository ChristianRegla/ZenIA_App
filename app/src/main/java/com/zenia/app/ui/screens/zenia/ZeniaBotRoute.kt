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

    val emergencyType by viewModel.emergencyType.collectAsState()
    val emergencyDisplay by viewModel.emergencyDisplay.collectAsState()

    ZeniaBotScreen(
        uiState = uiState,
        isTyping = isTyping,
        emergencyType = emergencyType,
        emergencyDisplay = emergencyDisplay,
        onSendMessage = { viewModel.enviarMensaje(it) },
        onClearChat = { viewModel.eliminarHistorial() },
        onDeleteSelected = { ids ->
            viewModel.eliminarMensajesSeleccionados(ids)
        },
        onDismissBanner = { viewModel.dismissBannerToIcon() },
        onRestoreBanner = { viewModel.restoreBanner() },
        onNavigateBack = onNavigateBack
    )
}