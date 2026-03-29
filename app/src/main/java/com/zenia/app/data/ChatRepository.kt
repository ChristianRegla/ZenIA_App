package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.zenia.app.model.MensajeChatbot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private fun getUserIdOrNull(): String? = auth.currentUser?.uid

    fun getHistorialChat(): Flow<List<MensajeChatbot>> = callbackFlow {
        val uid = getUserIdOrNull()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection(FirestoreCollections.USERS)
            .document(uid)
            .collection(FirestoreCollections.CHAT_HISTORY)
            .orderBy(FirestoreCollections.FIELD_DATE, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val mensajes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MensajeChatbot::class.java)
                        ?.copy(id = doc.id)
                } ?: emptyList()

                trySend(mensajes).isSuccess
            }

        awaitClose { listener.remove() }

    }.distinctUntilChanged()

    suspend fun addChatMessage(mensaje: MensajeChatbot): Result<Unit> {
        val uid = getUserIdOrNull() ?: return Result.failure(
            Exception("Usuario no autenticado")
        )

        return try {
            val docRef = db.collection(FirestoreCollections.USERS)
                .document(uid)
                .collection(FirestoreCollections.CHAT_HISTORY)
                .document()

            val messageWithId = mensaje.copy(id = docRef.id)

            docRef.set(messageWithId).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessagesByIds(ids: Set<String>): Result<Unit> = withContext(Dispatchers.IO) {

        if (ids.isEmpty()) return@withContext Result.success(Unit)

        val uid = getUserIdOrNull()
            ?: return@withContext Result.failure(Exception("Usuario no autenticado"))

        return@withContext try {
            val collectionRef = db.collection(FirestoreCollections.USERS)
                .document(uid)
                .collection(FirestoreCollections.CHAT_HISTORY)

            val chunkedIds = ids.chunked(500)

            chunkedIds.forEach { chunk ->
                val batch: WriteBatch = db.batch()
                chunk.forEach { id ->
                    val docRef = collectionRef.document(id)
                    batch.delete(docRef)
                }
                batch.commit().await()
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
