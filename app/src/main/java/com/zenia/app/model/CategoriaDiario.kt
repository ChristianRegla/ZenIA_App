package com.zenia.app.model

data class CategoriaDiario(
    val idCategoria: String = "",
    val tituloPersonalizado: String = "",
    val opciones: List<OpcionCategoria> = emptyList()
)

data class OpcionCategoria(
    val nivel: Int = 0,
    val nombrePersonalizado: String = "",
    val iconResName: String = "",
)