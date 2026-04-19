package com.zenia.app.model

import androidx.annotation.DrawableRes
import com.google.firebase.firestore.DocumentId

data class EjercicioGuiado(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val tipo: String = "",
    val duracionMin: Int = 0,
    val esPremium: Boolean = false,
    val urlAudio: String? = null,

    val urlImagen: String? = null,

    @DrawableRes val imagenLocalRes: Int? = null
)