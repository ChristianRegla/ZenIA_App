package com.zenia.app.ui.screens.evaluacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenia.app.data.EvaluacionRepository
import com.zenia.app.data.TestProvider
import com.zenia.app.data.session.UserSessionManager
import com.zenia.app.model.ResultadoEvaluacion
import com.zenia.app.model.TestPsicologico
import com.zenia.app.model.TipoTest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EvaluacionUiState(
    val test: TestPsicologico? = null,
    val indiceActual: Int = 0,
    val respuestas: Map<String, Int> = emptyMap(),
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null
)
@HiltViewModel
class EvaluacionViewModel @Inject constructor(
    private val evaluacionRepository: EvaluacionRepository,
    private val sessionManager: UserSessionManager
): ViewModel() {

    private val _uiState = MutableStateFlow(EvaluacionUiState())
    val uiState = _uiState.asStateFlow()

    fun cargarTest(tipoTestId: String) {
        val testSeleccionado = when (tipoTestId) {
            TipoTest.GAD7.name -> TestProvider.gad7
            TipoTest.PHQ9.name -> TestProvider.phq9
            else -> null
        }

        _uiState.update { it.copy(test = testSeleccionado, indiceActual = 0, respuestas = emptyMap()) }
    }

    fun seleccionarRespuesta(preguntaId: String, puntaje: Int) {
        _uiState.update { state ->
            val nuevasRespuestas = state.respuestas.toMutableMap()
            nuevasRespuestas[preguntaId] = puntaje
            state.copy(respuestas = nuevasRespuestas)
        }
    }

    fun avanzarSiguiente() {
        _uiState.update { state ->
            val totalPreguntas = state.test?.preguntas?.size ?: 0
            if (state.indiceActual < totalPreguntas - 1) {
                state.copy(indiceActual = state.indiceActual + 1)
            } else {
                state
            }
        }
    }

    fun retrocederAnterior() {
        _uiState.update { state ->
            if (state.indiceActual > 0) {
                state.copy(indiceActual = state.indiceActual - 1)
            } else {
                state
            }
        }
    }

    fun finalizarTest() {
        val state = _uiState.value
        val test = state.test ?: return
        val userId = sessionManager.currentUserId

        if (userId == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val puntajeTotal = state.respuestas.values.sum()

            val interpretacion = when (test.tipo) {
                TipoTest.GAD7 -> TestProvider.interpretarGAD7(puntajeTotal)
                TipoTest.PHQ9 -> TestProvider.interpretarPHQ9(puntajeTotal)
                else -> "Evaluación completada"
            }

            val resultado = ResultadoEvaluacion(
                userId = userId,
                tipoTest = test.tipo.name,
                puntajeTotal = puntajeTotal,
                interpretacion = interpretacion,
                respuestas = state.respuestas
            )

            val result = evaluacionRepository.guardarResultado(resultado)

            if (result.isSuccess) {
                _uiState.update { it.copy(isSubmitting = false, isCompleted = true) }
            } else {
                _uiState.update { it.copy(isSubmitting = false, error = "Error al guardar el resultado") }
            }
        }
    }
}