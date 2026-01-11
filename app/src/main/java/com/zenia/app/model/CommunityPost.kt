package com.zenia.app.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommunityPost(
    val id: String = "",
    val authorId: String = "",

    val authorApodo: String = "",
    val authorAvatarIndex: Int = 0,
    val authorIsPremium: Boolean = false,

    val content: String = "",
    val category: String = "General",

    val likesCount: Int = 0,
    val commentsCount: Int = 0,

    @ServerTimestamp
    val timestamp: Date? = null
)
