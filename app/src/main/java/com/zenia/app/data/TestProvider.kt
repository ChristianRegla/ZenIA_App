package com.zenia.app.data

import com.zenia.app.model.OpcionRespuesta
import com.zenia.app.model.PreguntaTest
import com.zenia.app.model.TestPsicologico
import com.zenia.app.model.TipoTest

object TestProvider {

    private val opcionesFrecuencia = listOf(
        OpcionRespuesta("Para nada", 0),
        OpcionRespuesta("Varios días", 1),
        OpcionRespuesta("Más de la mitad de los días", 2),
        OpcionRespuesta("Casi todos los días", 3)
    )

    val gad7 = TestPsicologico(
        tipo = TipoTest.GAD7,
        descripcion = "Durante las últimas 2 semanas, ¿con qué frecuencia le han molestado los siguientes problemas?",
        opciones = opcionesFrecuencia,
        preguntas = listOf(
            PreguntaTest("gad_1", "Sentirse nervioso/a, intranquilo/a o con los nervios de punta."),
            PreguntaTest("gad_2", "No poder dejar de preocuparse o no poder controlar la preocupación."),
            PreguntaTest("gad_3", "Preocuparse demasiado por diferentes cosas."),
            PreguntaTest("gad_4", "Dificultad para relajarse."),
            PreguntaTest("gad_5", "Estar tan inquieto/a que es difícil permanecer sentado/a tranquilamente."),
            PreguntaTest("gad_6", "Molestarse o irritarse fácilmente."),
            PreguntaTest("gad_7", "Sentir miedo, como si algo terrible pudiera pasar.")
        )
    )

    val phq9 = TestPsicologico(
        tipo = TipoTest.PHQ9,
        descripcion = "Durante las últimas 2 semanas, ¿con qué frecuencia le han molestado los siguientes problemas?",
        opciones = opcionesFrecuencia,
        preguntas = listOf(
            PreguntaTest("phq_1", "Tener poco interés o placer en hacer las cosas."),
            PreguntaTest("phq_2", "Sentirse desanimado/a, deprimido/a o sin esperanza."),
            PreguntaTest("phq_3", "Problemas para dormir o mantenerse dormido/a, o dormir demasiado."),
            PreguntaTest("phq_4", "Sentirse cansado/a o tener poca energía."),
            PreguntaTest("phq_5", "Tener poco apetito o comer en exceso."),
            PreguntaTest("phq_6", "Sentir falta de amor propio — o que es un fracaso o que se ha decepcionado a sí mismo/a o a su familia."),
            PreguntaTest("phq_7", "Dificultad para concentrarse en cosas tales como leer el periódico o ver la televisión."),
            PreguntaTest("phq_8", "Moverse o hablar tan lentamente que otras personas podrían haberlo notado. O lo contrario — estar tan inquieto/a o agitado/a que se ha estado moviendo mucho más de lo normal."),
            PreguntaTest("phq_9", "Pensamientos de que estaría mejor muerto/a o de lastimarse de alguna manera.")
        )
    )

    fun interpretarGAD7(puntaje: Int): String = when (puntaje) {
        in 0..4 -> "Ansiedad Mínima"
        in 5..9 -> "Ansiedad Leve"
        in 10..14 -> "Ansiedad Moderada"
        else -> "Ansiedad Severa"
    }

    fun interpretarPHQ9(puntaje: Int): String = when (puntaje) {
        in 0..4 -> "Depresión Mínima"
        in 5..9 -> "Depresión Leve"
        in 10..14 -> "Depresión Moderada"
        in 15..19 -> "Depresión Moderadamente Severa"
        else -> "Depresión Severa"
    }
}