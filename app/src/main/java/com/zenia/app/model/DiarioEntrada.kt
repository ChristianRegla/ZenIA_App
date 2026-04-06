package com.zenia.app.model

data class DiarioEntrada(
    val userId: String = "",
    val fecha: String = "",

    val estadoAnimo: String? = null,
    val calidadSueno: String? = null,
    val estadoMental: String? = null,
    val ejercicio: String? = null,

    val categoriasExtra: Map<String, String> = emptyMap(),

    val actividades: List<String> = emptyList(),
    val notas: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    val hcPasos: Int? = null,
    val hcRitmoCardiaco: Int? = null,
    val hcMinutosSueno: Int? = null,
    val hcHrv: Int? = null
)