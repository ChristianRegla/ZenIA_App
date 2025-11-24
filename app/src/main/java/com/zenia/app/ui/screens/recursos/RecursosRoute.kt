package com.zenia.app.ui.screens.recursos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenia.app.ui.screens.resources.RecursosScreen
import com.zenia.app.viewmodel.AppViewModelProvider

@Composable
fun RecursosRoute(
    // Aquí puedes pasar lambdas de navegación si al hacer click vas a un detalle
    // onRecursoClick: (String) -> Unit
) {
    val viewModel: RecursosViewModel = viewModel(factory = AppViewModelProvider.Factory)

    val uiState by viewModel.uiState.collectAsState()

    RecursosScreen(
        uiState = uiState,
        onRetry = { viewModel.cargarRecursos() }
    )
}