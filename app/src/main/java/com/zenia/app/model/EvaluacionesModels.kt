package com.zenia.app.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class TipoTest(val titulo: String) {
    GAD7("Ansiedad (GAD-7)"),
    PHQ9("Depresión (PHQ-9)"),
    ASRS("TDAH (ASRS v1.1)"),
    BIG5("Personalidad (Big Five)")
}

data class ResultadoEvaluacion(
    val id: String = "",
    val userId: String = "",
    val tipoTest: String = "",
    val puntajeTotal: Int = 0,
    val interpretacion: String = "",
    val respuestas: Map<String, Int> = emptyMap(),
    @ServerTimestamp
    val timestamp: Date? = null
)

data class PreguntaTest(
    val id: String,
    val texto: String
)

data class OpcionRespuesta(
    val texto: String,
    val puntaje: Int
)

data class TestPsicologico(
    val tipo: TipoTest,
    val descripcion: String,
    val preguntas: List<PreguntaTest>,
    val opciones: List<OpcionRespuesta>
)