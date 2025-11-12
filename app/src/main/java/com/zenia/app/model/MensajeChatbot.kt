package com.zenia.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class MensajeChatbot(
    @DocumentId val id: String = "",
    val fecha: Timestamp = Timestamp.now(),
    val emisor: String = "usuario",
    val texto: String = ""
)
