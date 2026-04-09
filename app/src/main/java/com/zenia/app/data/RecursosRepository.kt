package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zenia.app.model.Recurso
import com.zenia.app.model.RecursoInteraccion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecursosRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId: String?
        get() = auth.currentUser?.uid

    private fun getGlobalRecursos(): Flow<List<Recurso>> = callbackFlow {
        val listener = db.collection(FirestoreCollections.RESOURCES)
            .whereEqualTo(FirestoreCollections.FIELD_VALIDATED, true)
            .addSnapshotListener { snapshots, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val recursos = snapshots?.toObjects(Recurso::class.java) ?: emptyList()
                trySend(recursos)
            }
        awaitClose { listener.remove() }
    }

    private fun getUserInteracciones(): Flow<List<RecursoInteraccion>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.RESOURCES_INTERACTIONS)
            .addSnapshotListener { snapshots, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val interacciones = snapshots?.toObjects(RecursoInteraccion::class.java) ?: emptyList()
                trySend(interacciones)
            }
        awaitClose { listener.remove() }
    }

    fun getRecursosConInteracciones(): Flow<List<Pair<Recurso, RecursoInteraccion?>>> {
        return combine(getGlobalRecursos(), getUserInteracciones()) { recursos, interacciones ->
            recursos.map { recurso ->
                val interaccion = interacciones.find { it.recursoId == recurso.id }
                Pair(recurso, interaccion)
            }
        }
    }

    suspend fun toggleFavorite(recursoId: String, currentStatus: Boolean) {
        val uid = userId?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.RESOURCES_INTERACTIONS).document(recursoId)

        val data = mapOf(
            "recursoId" to recursoId,
            "isFavorite" to !currentStatus
        )

        ref.set(data, SetOptions.merge()).await()
    }

    suspend fun updateProgress(recursoId: String, progress: Int) {
        val uid = userId?: return
        val ref = db.collection(FirestoreCollections.USERS).document(uid)
            .collection(FirestoreCollections.RESOURCES_INTERACTIONS).document(recursoId)

        val data = mapOf(
            "recursoId" to recursoId,
            "progress" to progress
        )
        ref.set(data, SetOptions.merge()).await()
    }

    suspend fun getRecursoById(recursoId: String): Recurso? {
        return try {
            val snapshot = db.collection(FirestoreCollections.RESOURCES)
                .document(recursoId)
                .get()
                .await()
            snapshot.toObject(Recurso::class.java)
        } catch (e: Exception) {
            null
        }
    }
}