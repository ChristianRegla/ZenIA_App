package com.zenia.app.model

import com.google.firebase.firestore.DocumentId

data class Recurso(
    @DocumentId val id: String = "",
    val titulo: String = "",
    val contenido: String = "",
    val tipo: String = "",
    val validado: Boolean = false,
    val esPremium: Boolean = false,
    val telefono: String? = null,
    val sitioWeb: String? = null,
    val urlAudio: String? = null
)