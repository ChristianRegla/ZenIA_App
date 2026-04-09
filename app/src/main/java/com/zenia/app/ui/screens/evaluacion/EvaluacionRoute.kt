package com.zenia.app.ui.screens.evaluacion

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EvaluacionRoute(
    tipoTestId: String,
    onBackClick: () -> Unit,
    viewModel: EvaluacionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(tipoTestId) {
        viewModel.cargarTest(tipoTestId)
    }

    LaunchedEffect(uiState.isCompleted, uiState.error) {
        if (uiState.isCompleted) {
            Toast.makeText(context, "Evaluación completada con éxito", Toast.LENGTH_SHORT).show()
            onBackClick()
        }
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
        }
    }

    EvaluacionScreen(
        uiState = uiState,
        onRespuestaSeleccionada = viewModel::seleccionarRespuesta,
        onSiguiente = viewModel::avanzarSiguiente,
        onAnterior = viewModel::retrocederAnterior,
        onFinalizar = viewModel::finalizarTest,
        onBackClick = onBackClick
    )
}