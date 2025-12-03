package com.zenia.app.model

data class DiarioEntrada(
    val id: String = "",
    val userId: String = "",
    val fecha: String = "",
    val estadoAnimo: String? = null,
    val calidadSueno: String? = null,
    val estadoMental: String? = null,
    val ejercicio: String? = null,
    val actividades: List<String> = emptyList(),
    val notas: String = "",
    val timestamp: Long = System.currentTimeMillis()
)