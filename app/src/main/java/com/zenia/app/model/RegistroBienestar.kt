package com.zenia.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class RegistroBienestar(
    @DocumentId val id: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val estadoAnimo: String = "",
    val notas: String = "",
    val frecuenciaCardiaca: Int? = null
)