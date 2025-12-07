package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zenia.app.model.MensajeChatbot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

    fun getHistorialChat(): Flow<List<MensajeChatbot>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(FirestoreCollections.USERS).document(currentUserId)
            .collection(FirestoreCollections.CHAT_HISTORY)
            .orderBy(FirestoreCollections.FIELD_DATE, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val mensajes = snapshot?.toObjects(MensajeChatbot::class.java) ?: emptyList()
                trySend(mensajes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addChatMessage(mensaje: MensajeChatbot) {
        db.collection(FirestoreCollections.USERS).document(userId)
            .collection(FirestoreCollections.CHAT_HISTORY)
            .add(mensaje)
            .await()
    }

    suspend fun deleteChatHistory() {
        val currentUserId = auth.currentUser?.uid ?: return
        val chatCollection = db.collection(FirestoreCollections.USERS)
            .document(currentUserId)
            .collection(FirestoreCollections.CHAT_HISTORY)

        val snapshot = chatCollection.get().await()
        val batches = snapshot.documents.chunked(500)
        for (batchDocs in batches) {
            val batch = db.batch()
            for (document in batchDocs) {
                batch.delete(document.reference)
            }
            batch.commit().await()
        }
    }
}