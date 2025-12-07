package com.zenia.app.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.zenia.app.model.ActividadComunidad
import com.zenia.app.model.EjercicioGuiado
import com.zenia.app.model.Recurso
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ContentRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    fun getRecursos(): Flow<List<Recurso>> = callbackFlow {
        val listener = db.collection(FirestoreCollections.RESOURCES)
            .whereEqualTo(FirestoreCollections.FIELD_VALIDATED, true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val recursos = snapshot?.toObjects(Recurso::class.java) ?: emptyList()
                trySend(recursos)
            }
        awaitClose { listener.remove() }
    }

    fun getEjerciciosGuiados(): Flow<List<EjercicioGuiado>> = callbackFlow {
        val listener = db.collection(FirestoreCollections.GUIDED_EXERCISES)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val ejercicios = snapshot?.toObjects(EjercicioGuiado::class.java) ?: emptyList()
                trySend(ejercicios)
            }
        awaitClose { listener.remove() }
    }

    fun getActividadesComunidad(): Flow<List<ActividadComunidad>> = callbackFlow {
        val listener = db.collection(FirestoreCollections.COMMUNITY_ACTIVITIES)
            .orderBy(FirestoreCollections.FIELD_SCHEDULED_DATE, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val actividades = snapshot?.toObjects(ActividadComunidad::class.java) ?: emptyList()
                trySend(actividades)
            }
        awaitClose { listener.remove() }
    }
}