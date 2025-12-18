package com.zenia.app.model

import com.google.firebase.firestore.PropertyName

data class ZeniaNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    @get:PropertyName("is_read") @set:PropertyName("is_read")
    var isRead: Boolean = false,
    val route: String? = null,
    val type: String = "info"
)