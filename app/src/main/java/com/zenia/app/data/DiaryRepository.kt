package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.zenia.app.model.DiarioEntrada
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiaryRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")

    /**
     * Función de ayuda genérica para crear un Flow que escucha una subcolección del usuario.
     * Se reconecta automáticamente si el usuario cambia (login/logout).
     *
     * @param T El tipo de objeto a deserializar desde Firestore.
     * @param subcollection El nombre de la subcolección (ej. "diario").
     * @param queryBuilder Un lambda para personalizar la consulta (ej. añadir filtros u ordenación).
     * @return Un Flow que emite la lista de objetos de la subcolección.
     */
    private inline fun <reified T> getUserSubcollectionFlow(
        subcollection: String,
        crossinline queryBuilder: (Query) -> Query = { it }
    ): Flow<List<T>> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUserId = firebaseAuth.currentUser?.uid
            firestoreListener?.remove()

            if (currentUserId.isNullOrBlank()) {
                trySend(emptyList())
            } else {
                val baseQuery = db.collection(FirestoreCollections.USERS)
                    .document(currentUserId)
                    .collection(subcollection)

                firestoreListener = queryBuilder(baseQuery).addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val items = snapshot?.toObjects(T::class.java) ?: emptyList()
                    trySend(items)
                }
            }
        }

        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }

    /**
     * Obtiene un flujo de todas las entradas del diario del usuario.
     */
    fun getDiaryEntriesStream(): Flow<List<DiarioEntrada>> = getUserSubcollectionFlow(FirestoreCollections.DIARY)

    /**
     * Obtiene un flujo de entradas del diario a partir de una fecha mínima.
     *
     * @param minDate La fecha mínima en formato AAAA-MM-DD.
     */
    fun getEntriesFromDate(minDate: String): Flow<List<DiarioEntrada>> = getUserSubcollectionFlow(FirestoreCollections.DIARY) { query ->
        query.whereGreaterThanOrEqualTo("fecha", minDate)
    }

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

    suspend fun getAllEntriesOnce(): List<DiarioEntrada> {
        return try {
            val snapshot = db.collection(FirestoreCollections.USERS).document(userId)
                .collection(FirestoreCollections.DIARY)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.toObjects(DiarioEntrada::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Calcula la racha actual de forma eficiente.
     * Descarga todas las fechas ordenadas, pero no el contenido pesado.
     */
    suspend fun calculateCurrentStreak(userId: String): Int {
        return try {
            val snapshot = db.collection(FirestoreCollections.USERS)
                .document(userId)
                .collection(FirestoreCollections.DIARY)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            if (snapshot.isEmpty) return 0

            val dates = snapshot.documents.mapNotNull { doc ->
                val dateStr = doc.getString("fecha")
                try {
                    if (dateStr != null) LocalDate.parse(dateStr) else null
                } catch (e: Exception) {
                    null
                }
            }.distinct()

            if (dates.isEmpty()) return 0

            var streak = 0
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            val lastEntryDate = dates.first()
            if (!lastEntryDate.isEqual(today) && !lastEntryDate.isEqual(yesterday)) {
                return 0
            }

            var checkDate = if (dates.contains(today)) today else yesterday

            for (date in dates) {
                if (date.isEqual(checkDate)) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else if (date.isAfter(checkDate)) {
                    continue
                } else {
                    break
                }
            }

            streak
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }


    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}
