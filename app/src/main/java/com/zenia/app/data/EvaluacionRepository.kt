package com.zenia.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zenia.app.model.ResultadoEvaluacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EvaluacionRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun guardarResultado(resultado: ResultadoEvaluacion): Result<Unit> = withContext(
        Dispatchers.IO) {
        try {
            val docId = resultado.id.ifBlank { UUID.randomUUID().toString() }
            val resultadoConId = resultado.copy(id = docId)

            db.collection(FirestoreCollections.EVALUACIONES)
                .document(docId)
                .set(resultadoConId)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun obtenerHistorialTest(userId: String, tipoTest: String): Flow<List<ResultadoEvaluacion>> = callbackFlow {
        val listener = db.collection(FirestoreCollections.EVALUACIONES)
            .whereEqualTo("userId", userId)
            .whereEqualTo("tipoTest", tipoTest)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val resultados = snapshot.toObjects(ResultadoEvaluacion::class.java)
                    trySend(resultados)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { listener.remove() }
    }
}