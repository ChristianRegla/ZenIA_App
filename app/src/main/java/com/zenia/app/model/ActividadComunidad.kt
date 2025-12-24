package com.zenia.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class ActividadComunidad(
    @DocumentId val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val fechaProgramada: Timestamp = Timestamp.now(),
    val participantes: Int = 0
)
