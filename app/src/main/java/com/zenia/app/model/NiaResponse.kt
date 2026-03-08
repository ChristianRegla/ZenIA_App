package com.zenia.app.model

data class NiaResponse(
    val mensaje_nia: String,
    val trigger: String? = "none"
)

data class ChatRequest(
    val message: String
)