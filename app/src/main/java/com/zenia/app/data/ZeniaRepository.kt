package com.zenia.app.data

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.zenia.app.model.ActividadComunidad
import com.zenia.app.model.EjercicioGuiado
import com.zenia.app.model.MensajeChatbot
import com.zenia.app.model.Recurso
import com.zenia.app.model.RegistroBienestar
import com.zenia.app.model.Usuario
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ZeniaRepository {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    suspend fun checkAndCreateUserDocument(userId: String, email: String?) {
        val userDocRef = db.collection("usuarios").document(userId)
        val document = userDocRef.get().await()

        if (!document.exists()) {
            val nuevoUsuario = Usuario(
                id = userId,
                email = email ?: "",
                suscripcion = "free"
            )
            userDocRef.set(nuevoUsuario).await()
        }
    }

    fun getUsuarioFlow(): Flow<Usuario?> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = db.collection("usuarios").document(currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val usuario = snapshot?.toObject(Usuario::class.java)
                trySend(usuario)
            }
        awaitClose { listener.remove() }
    }

    fun getRegistrosBienestar(): Flow<List<RegistroBienestar>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("usuarios").document(currentUserId)
            .collection("registrosBienestar")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val registros = snapshot?.toObjects(RegistroBienestar::class.java) ?: emptyList()
                trySend(registros)
            }
        awaitClose { listener.remove() }
    }

    fun getRecursos(): Flow<List<Recurso>> = callbackFlow {
        val listener = db.collection("recursos")
            .whereEqualTo("validado", true)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val recursos = snapshot?.toObjects(Recurso::class.java) ?: emptyList()
                trySend(recursos)
            }
        awaitClose { listener.remove() }
    }

    fun getEjerciciosGuiados(): Flow<List<EjercicioGuiado>> = callbackFlow {
        val listener = db.collection("ejerciciosGuiados")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val ejercicios = snapshot?.toObjects(EjercicioGuiado::class.java) ?: emptyList()
                trySend(ejercicios)
            }
        awaitClose { listener.remove() }
    }

    fun getActividadesComunidad(): Flow<List<ActividadComunidad>> = callbackFlow {
        val listener = db.collection("actividadesComunidad")
            .orderBy("fechaProgramada", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val actividades = snapshot?.toObjects(ActividadComunidad::class.java) ?: emptyList()
                trySend(actividades)
            }
        awaitClose { listener.remove() }
    }

    fun getHistorialChat(): Flow<List<MensajeChatbot>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("usuarios").document(currentUserId)
            .collection("chatHistory")
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val mensajes = snapshot?.toObjects(MensajeChatbot::class.java) ?: emptyList()
                trySend(mensajes)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addRegistroBienestar(registro: RegistroBienestar) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            throw IllegalStateException("Usuario no autenticado. No se puede guardar el registro.")
        }

        db.collection("usuarios").document(currentUserId)
            .collection("registrosBienestar").add(registro).await()
    }

    suspend fun addChatMessage(mensaje: MensajeChatbot) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            throw IllegalStateException("Usuario no autenticado. No se puede guardar el mensaje.")
        }

        db.collection("usuarios").document(currentUserId)
            .collection("chatHistory").add(mensaje).await()
    }
}