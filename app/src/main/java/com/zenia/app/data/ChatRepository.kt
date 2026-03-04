package com.zenia.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.zenia.app.model.MensajeChatbot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

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

    suspend fun deleteMessagesByIds(ids: Set<String>): Result<Unit> {

        if (ids.isEmpty()) return Result.success(Unit)

        val uid = getUserIdOrNull()
            ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {

            val batch: WriteBatch = db.batch()

            val collectionRef = db.collection(FirestoreCollections.USERS)
                .document(uid)
                .collection(FirestoreCollections.CHAT_HISTORY)

            ids.forEach { id ->
                val docRef = collectionRef.document(id)
                batch.delete(docRef)
            }

            batch.commit().await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * IMPORTANTE: La eliminación masiva del historial de chat desde el cliente es insegura.
     * Ha sido deshabilitada para prevenir crashes por uso excesivo de memoria.
     *
     * SOLUCIÓN CORRECTA: Usar una Cloud Function que elimine la subcolección en el backend.
     * Se puede llamar a esta función desde el cliente con un solo comando.
     * @see https://firebase.google.com/docs/functions/callable
     */
    suspend fun deleteChatHistory() {
        val currentUserId = auth.currentUser?.uid ?: return
        Log.w(
            "ChatRepository",
            "La eliminación del historial de chat ($currentUserId) debe ser manejada por una Cloud Function. " +
            "La implementación del lado del cliente ha sido deshabilitada."
        )
    }
}
