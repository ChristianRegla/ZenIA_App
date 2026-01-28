package com.zenia.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zenia.app.model.MensajeChatbot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ChatRepository @Inject constructor(
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
