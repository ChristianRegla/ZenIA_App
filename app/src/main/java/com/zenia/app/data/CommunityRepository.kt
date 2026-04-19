package com.zenia.app.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zenia.app.model.BlockedUserProfile
import com.zenia.app.model.CommunityComment
import com.zenia.app.model.CommunityPost
import com.zenia.app.util.ProfanityFilter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import com.zenia.app.R
import android.content.Context
import kotlinx.coroutines.flow.StateFlow

@Singleton
class CommunityRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val profanityFilter: ProfanityFilter
) {
    private val postsCollection = firestore.collection(FirestoreCollections.POSTS)

    private val _postUpdates = MutableSharedFlow<CommunityPost>(extraBufferCapacity = 1)
    val postUpdates = _postUpdates.asSharedFlow()

    suspend fun emitPostUpdate(post: CommunityPost) {
        _postUpdates.emit(post)
    }

    suspend fun getPosts(
        lastVisible: DocumentSnapshot?,
        limit: Long = 10,
        currentUserId: String? = null
    ): Pair<List<CommunityPost>, DocumentSnapshot?> {
        var query = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        val snapshot = query.get().await()
        var posts = snapshot.toObjects(CommunityPost::class.java)

        if (currentUserId != null && posts.isNotEmpty()) {
            posts = coroutineScope {
                posts.map { post ->
                    async {
                        val likeSnapshot = postsCollection.document(post.id)
                            .collection("likes").document(currentUserId)
                            .get().await()

                        post.copy(isLikedByCurrentUser = likeSnapshot.exists())
                    }
                }.awaitAll()
            }
        }

        val newLastVisible =
            if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null

        return Pair(posts, newLastVisible)
    }

    suspend fun createPost(
        userId: String,
        apodo: String,
        avatarIndex: Int,
        isPremium: Boolean,
        content: String,
        category: String
    ): Result<CommunityPost> {
        return try {
            if (profanityFilter.hasProfanity(content)) {
                return Result.failure(Exception(context.getString(R.string.error_profanity_content)))
            }

            val postId = postsCollection.document().id

            val post = CommunityPost(
                id = postId,
                authorId = userId,
                authorApodo = apodo,
                authorAvatarIndex = avatarIndex,
                authorIsPremium = isPremium,
                content = content,
                category = category
            )

            postsCollection.document(postId).set(post).await()

            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(
        postId: String,
        userId: String
    ): Result<Boolean> {
        val postRef = postsCollection.document(postId)
        val likeRef = postRef.collection("likes").document(userId)

        return try {
            val isLikedNow = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(likeRef)

                if (snapshot.exists()) {
                    transaction.delete(likeRef)
                    transaction.update(postRef, "likesCount", FieldValue.increment(-1))
                    false
                } else {
                    transaction.set(likeRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                    transaction.update(postRef, "likesCount", FieldValue.increment(1))
                    true
                }
            }.await()

            Result.success(isLikedNow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            postsCollection.document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportPost(content: String, authorId: String): Result<Unit> {
        return try {
            val reportData = mapOf(
                "content" to content,
                "reportedUserId" to authorId,
                "timestamp" to FieldValue.serverTimestamp()
            )
            firestore.collection("reported_posts").add(reportData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockUser(currentUserId: String, authorIdToBlock: String): Result<Unit> {
        return try {
            val blockRef = firestore.collection("usuarios")
                .document(currentUserId)
                .collection("blocked_users")
                .document(authorIdToBlock)

            blockRef.set(mapOf("timestamp" to FieldValue.serverTimestamp())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBlockedUsers(currentUserId: String): Result<List<String>> {
        return try {
            val snapshot = firestore.collection("usuarios")
                .document(currentUserId)
                .collection("blocked_users")
                .get()
                .await()

            val blockedIds = snapshot.documents.map { it.id }
            Result.success(blockedIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(currentUserId: String, authorId: String): Result<Unit> {
        return try {
            firestore.collection("usuarios").document(currentUserId)
                .collection("blocked_users").document(authorId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetailedBlockedUsers(currentUserId: String): Result<List<BlockedUserProfile>> {
        return try {
            val blockedIds = firestore.collection("usuarios").document(currentUserId)
                .collection("blocked_users").get().await().documents.map { it.id }

            if (blockedIds.isEmpty()) return Result.success(emptyList())

            val users = firestore.collection("usuarios")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), blockedIds)
                .get().await().toObjects(com.zenia.app.model.Usuario::class.java)

            val profiles = users.map {
                BlockedUserProfile(
                    it.id,
                    it.apodo ?: context.getString(R.string.default_username),
                    it.avatarIndex,
                    it.suscripcion == com.zenia.app.model.SubscriptionType.PREMIUM
                )
            }
            Result.success(profiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getComments(
        postId: String,
        lastVisible: DocumentSnapshot?,
        limit: Long = 15,
        currentUserId: String? = null
    ): Pair<List<CommunityComment>, DocumentSnapshot?> {
        var query = postsCollection.document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        val snapshot = query.get().await()
        var comments = snapshot.toObjects(CommunityComment::class.java)

        if (currentUserId != null && comments.isNotEmpty()) {
            comments = coroutineScope {
                comments.map { comment ->
                    async {
                        val likeSnapshot = postsCollection.document(postId)
                            .collection("comments").document(comment.id)
                            .collection("likes").document(currentUserId)
                            .get().await()

                        comment.copy(isLikedByCurrentUser = likeSnapshot.exists())
                    }
                }.awaitAll()
            }
        }

        val newLastVisible =
            if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null

        return Pair(comments, newLastVisible)
    }

    suspend fun createComment(
        postId: String,
        userId: String,
        apodo: String,
        avatarIndex: Int,
        isPremium: Boolean,
        content: String
    ): Result<CommunityComment> {
        return try {
            if (profanityFilter.hasProfanity(content)) {
                return Result.failure(Exception(context.getString(R.string.error_profanity_content)))
            }

            val postRef = postsCollection.document(postId)
            val commentRef = postRef.collection("comments").document()
            val commentId = commentRef.id

            val comment = CommunityComment(
                id = commentId,
                postId = postId,
                authorId = userId,
                authorApodo = apodo,
                authorAvatarIndex = avatarIndex,
                authorIsPremium = isPremium,
                content = content
            )

            firestore.runTransaction { transaction ->
                val postSnapshot = transaction.get(postRef)
                if (postSnapshot.exists()) {
                    transaction.set(commentRef, comment)
                    transaction.update(postRef, "commentsCount", FieldValue.increment(1))
                } else {
                    throw Exception(context.getString(R.string.error_post_not_found))
                }
            }.await()

            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        return try {
            val postRef = postsCollection.document(postId)
            val commentRef = postRef.collection("comments").document(commentId)

            firestore.runTransaction { transaction ->
                val commentSnapshot = transaction.get(commentRef)
                if (commentSnapshot.exists()) {
                    transaction.delete(commentRef)
                    transaction.update(postRef, "commentsCount", FieldValue.increment(-1))
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleCommentLike(
        postId: String,
        commentId: String,
        userId: String
    ): Result<Boolean> {
        val commentRef = postsCollection.document(postId).collection("comments").document(commentId)
        val likeRef = commentRef.collection("likes").document(userId)

        return try {
            val isLikedNow = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(likeRef)

                if (snapshot.exists()) {
                    transaction.delete(likeRef)
                    transaction.update(commentRef, "likesCount", FieldValue.increment(-1))
                    false
                } else {
                    transaction.set(likeRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                    transaction.update(commentRef, "likesCount", FieldValue.increment(1))
                    true
                }
            }.await()

            Result.success(isLikedNow)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAuthorProfileInCommunity(
        userId: String,
        newApodo: String,
        newAvatarIndex: Int,
        isPremium: Boolean
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()

            val userPosts = postsCollection.whereEqualTo("authorId", userId).get().await()
            for (doc in userPosts.documents) {
                batch.update(
                    doc.reference, mapOf(
                        "authorApodo" to newApodo,
                        "authorAvatarIndex" to newAvatarIndex,
                        "authorIsPremium" to isPremium
                    )
                )
            }

            val userComments = firestore.collectionGroup("comments")
                .whereEqualTo("authorId", userId).get().await()

            for (doc in userComments.documents) {
                batch.update(
                    doc.reference, mapOf(
                        "authorApodo" to newApodo,
                        "authorAvatarIndex" to newAvatarIndex,
                        "authorIsPremium" to isPremium
                    )
                )
            }

            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}