package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.zenia.app.model.ZeniaNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Obtiene solo las últimas 20 notificaciones para ahorrar ancho de banda.
     * Escucha cambios en tiempo real.
     */
    fun getNotificationsStream(): Flow<List<ZeniaNotification>> = callbackFlow {
        val uid = getCurrentUserId()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = firestore.collection(FirestoreCollections.USERS)
            .document(uid)
            .collection(FirestoreCollections.NOTIFICATIONS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)

        var listenerRegistration: ListenerRegistration?= null

        listenerRegistration = query.addSnapshotListener { snapshots, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                val notifications = snapshots.documents.mapNotNull { doc ->
                    doc.toObject(ZeniaNotification::class.java)
                        ?.copy(id = doc.id)
                }

                trySend(notifications).isSuccess
            } else {
                trySend(emptyList()).isSuccess
            }
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }.distinctUntilChanged()

    suspend fun markAsRead(notificationId: String) {
        val uid = getCurrentUserId() ?: return

        firestore.collection(FirestoreCollections.USERS)
            .document(uid)
            .collection(FirestoreCollections.NOTIFICATIONS)
            .document(notificationId)
            .update("is_read", true)
            .await()
    }

    suspend fun deleteNotification(notificationId: String) {
        val uid = getCurrentUserId() ?: return

        firestore.collection(FirestoreCollections.USERS)
            .document(uid)
            .collection(FirestoreCollections.NOTIFICATIONS)
            .document(notificationId)
            .delete()
            .await()
    }

    suspend fun createDummyNotification(notification: ZeniaNotification) {
        val uid = getCurrentUserId() ?: return

        val docRef = firestore.collection(FirestoreCollections.USERS)
            .document(uid)
            .collection(FirestoreCollections.NOTIFICATIONS)
            .document()

        val notificationWithId = notification.copy(id = docRef.id)

        docRef.set(notificationWithId).await()
    }
}