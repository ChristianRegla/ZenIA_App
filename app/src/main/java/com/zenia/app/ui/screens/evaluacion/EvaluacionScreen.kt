package com.zenia.app.ui.screens.evaluacion

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.lazy.LazyColumn
import com.zenia.app.ui.components.ZeniaTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluacionScreen(
    uiState: EvaluacionUiState,
    onRespuestaSeleccionada: (String, Int) -> Unit,
    onSiguiente: () -> Unit,
    onAnterior: () -> Unit,
    onFinalizar: () -> Unit,
    onBackClick: () -> Unit
) {

    val test = uiState.test

    if (test == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val preguntaActual = test.preguntas[uiState.indiceActual]
    val respuestaSeleccionada = uiState.respuestas[preguntaActual.id]
    val progreso = (uiState.indiceActual + 1).toFloat() / test.preguntas.size

    Scaffold(
        topBar = {
            ZeniaTopBar(
                title = test.tipo.titulo,
                onNavigateBack = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxSize()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = "Pregunta ${uiState.indiceActual + 1} de ${test.preguntas.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(targetState = preguntaActual, label = "pregunta_anim") { pregunta ->
                Text(
                    text = pregunta.texto,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(test.opciones) { opcion ->
                    val isSelected = respuestaSeleccionada == opcion.puntaje

                    Surface(
                        onClick = { onRespuestaSeleccionada(preguntaActual.id, opcion.puntaje) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = opcion.texto,
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onAnterior,
                    enabled = uiState.indiceActual > 0
                ) {
                    Text("Anterior")
                }

                if (uiState.indiceActual < test.preguntas.size - 1) {
                    Button(
                        onClick = onSiguiente,
                        enabled = respuestaSeleccionada != null // Obliga a responder
                    ) {
                        Text("Siguiente")
                    }
                } else {
                    Button(
                        onClick = onFinalizar,
                        enabled = respuestaSeleccionada != null && !uiState.isSubmitting
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Text("Finalizar")
                        }
                    }
                }
            }
        }
    }
}