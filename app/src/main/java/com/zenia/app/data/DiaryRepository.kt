package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.zenia.app.model.DiarioEntrada
import com.zenia.app.model.RegistroBienestar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DiaryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val diaryEntries: Flow<List<DiarioEntrada>> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUserId = firebaseAuth.currentUser?.uid
            firestoreListener?.remove()

            if (currentUserId.isNullOrBlank()) {
                trySend(emptyList())
            } else {
                firestoreListener = db.collection(FirestoreCollections.USERS).document(currentUserId)
                    .collection(FirestoreCollections.DIARY)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val entradas = snapshot?.toObjects(DiarioEntrada::class.java) ?: emptyList()
                        trySend(entradas)
                    }
            }
        }

        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }

        .shareIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    fun getEntriesFromDate(minDate: String): Flow<List<DiarioEntrada>> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUserId = firebaseAuth.currentUser?.uid
            firestoreListener?.remove()

            if (currentUserId.isNullOrBlank()) {
                trySend(emptyList())
            } else {
                firestoreListener = db.collection(FirestoreCollections.USERS).document(currentUserId)
                    .collection(FirestoreCollections.DIARY)
                    .whereGreaterThanOrEqualTo("fecha", minDate)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val entradas = snapshot?.toObjects(DiarioEntrada::class.java) ?: emptyList()
                        trySend(entradas)
                    }
            }
        }

        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }

    fun getDiaryEntriesStream(): Flow<List<DiarioEntrada>> = diaryEntries

    suspend fun saveDiaryEntry(entry: DiarioEntrada) {
        db.collection(FirestoreCollections.USERS).document(userId)
            .collection(FirestoreCollections.DIARY)
            .document(entry.fecha)
            .set(entry)
            .await()
    }

    suspend fun deleteDiaryEntry(date: String) {
        db.collection(FirestoreCollections.USERS).document(userId)
            .collection(FirestoreCollections.DIARY)
            .document(date)
            .delete()
            .await()
    }

    suspend fun getDiaryEntryByDate(date: String): DiarioEntrada? {
        val snapshot = db.collection(FirestoreCollections.USERS).document(userId)
            .collection(FirestoreCollections.DIARY)
            .document(date)
            .get()
            .await()

        return if (snapshot.exists()) snapshot.toObject(DiarioEntrada::class.java) else null
    }

    fun getRegistrosBienestar(): Flow<List<RegistroBienestar>> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUserId = firebaseAuth.currentUser?.uid

            firestoreListener?.remove()

            if (currentUserId.isNullOrBlank()) {
                trySend(emptyList())
            } else {
                firestoreListener = db.collection(FirestoreCollections.USERS).document(currentUserId)
                    .collection(FirestoreCollections.WELNESS_LOGS)
                    .orderBy(FirestoreCollections.FIELD_DATE, Query.Direction.ASCENDING)
                    .limit(30)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val registros = snapshot?.toObjects(RegistroBienestar::class.java) ?: emptyList()
                        trySend(registros)
                    }
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }

    suspend fun addRegistroBienestar(registro: RegistroBienestar) {
        db.collection(FirestoreCollections.USERS).document(userId)
            .collection(FirestoreCollections.WELNESS_LOGS)
            .add(registro)
            .await()
    }

    /**
     * Obtiene todas las entradas ordenadas por fecha para generar reportes PDF.
     * Es una función suspendida de una sola ejecución (no Flow).
     */
    suspend fun getAllEntriesOnce(): List<DiarioEntrada> {
        return try {
            val snapshot = db.collection(FirestoreCollections.USERS).document(userId)
                .collection(FirestoreCollections.DIARY)
                .orderBy("fecha", Query.Direction.DESCENDING) // Del más reciente al más antiguo
                .get()
                .await()

            snapshot.toObjects(DiarioEntrada::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Devuelve el ID actual de forma segura (nullable) para que el ViewModel
     * pueda verificar si hay sesión antes de intentar guardar.
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}