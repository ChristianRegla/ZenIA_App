package com.zenia.app.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zenia.app.model.CommunityPost
import com.zenia.app.util.ProfanityFilter
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val profanityFilter: ProfanityFilter
) {
    private val postsCollection = firestore.collection(FirestoreCollections.POSTS)

    suspend fun getPosts(
        lastVisible: DocumentSnapshot?,
        limit: Long = 10
    ): Pair<List<CommunityPost>, DocumentSnapshot?> {
        var query = postsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        val snapshot = query.get().await()
        val posts = snapshot.toObjects(CommunityPost::class.java)
        val newLastVisible = if (snapshot.documents.isNotEmpty()) snapshot.documents.last() else null

        return Pair(posts, newLastVisible)
    }

    suspend fun createPost(
        userId: String,
        apodo: String,
        avatarIndex: Int,
        isPremium: Boolean,
        content: String,
        category: String
    ): Result<Unit> {
        return try {
            if (profanityFilter.hasProfanity(content)) {
                return Result.failure(Exception("El contenido contiene palabras prohibidas"))
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
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(postId: String, userId: String, isLiking: Boolean) {
        val postRef = postsCollection.document(postId)
        val likeRef = postRef.collection("likes").document(userId)

        firestore.runTransaction { transaction ->
            if (isLiking) {
                transaction.set(likeRef, mapOf("timestamp" to FieldValue.serverTimestamp()))
                transaction.update(postRef, "likesCount", FieldValue.increment(1))
            } else {
                transaction.delete(likeRef)
                transaction.update(postRef, "likesCount", FieldValue.increment(-1))
            }
        }.await()
    }
}