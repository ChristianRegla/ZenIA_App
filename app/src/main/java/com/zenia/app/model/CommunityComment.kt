package com.zenia.app.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class CommunityComment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorApodo: String = "",
    val authorAvatarIndex: Int = 0,
    val authorIsPremium: Boolean = false,
    val content: String = "",
    val likesCount: Int = 0,

    @ServerTimestamp
    val timestamp: Date? = null,

    @get:Exclude
    var isLikedByCurrentUser: Boolean = false
)