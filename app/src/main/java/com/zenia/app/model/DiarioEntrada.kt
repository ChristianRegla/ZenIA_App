package com.zenia.app.model

data class DiarioEntrada(
    val userId: String = "",
    val fecha: String = "",

    val estadoAnimo: String? = null,
    val calidadSueno: String? = null,
    val estadoMental: String? = null,
    val ejercicio: String? = null,

    val actividades: List<String> = emptyList(),
    val notas: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    val hcPasos: Int? = null,
    val hcCaloriasActivas: Int? = null,
    val hcMinutosSueno: Int? = null,
    val hcMinutosEjercicio: Int? = null
)