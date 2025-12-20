package com.zenia.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Usuario(
    @DocumentId val id: String = "",
    val email: String = "",
    val fechaCreacion: Timestamp = Timestamp.now(),
    val suscripcion: String = "free",
    val apodo: String? = null,
    val avatarIndex: Int = 0
)
