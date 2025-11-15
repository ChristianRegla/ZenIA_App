package com.zenia.app.data

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.ListenerRegistration
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

/**
 * Repositorio principal de la aplicación ZenIA.
 * Se encarga de toda la comunicación con la base de datos (Cloud Firestore)
 * y la autenticación (Firebase Auth) para obtener y escribir datos.
 */
class ZeniaRepository {
    /**
     * Instancia privada de Cloud Firestore.
     */
    private val db = Firebase.firestore
    /**
     * Instancia privada de Firebase Authentication.
     */
    private val auth = Firebase.auth

    /**
     * Verifica si existe un documento de usuario en Firestore al iniciar sesión o registrarse.
     * Si no existe, lo crea automáticamente con la suscripción por defecto ("free").
     * Esto previene el crash de PERMISSION_DENIED al intentar leer un documento inexistente.
     *
     * @param userId El UID del usuario de Firebase Auth.
     * @param email El email del usuario (opcional, para guardarlo en el documento).
     */
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

    /**
     * Obtiene un Flow en tiempo real del documento del usuario actual ([Usuario]).
     * Este Flow emite `null` si el usuario cierra sesión.
     *
     * @return Un [Flow] que emite un objeto [Usuario?] nulable.
     */
    fun getUsuarioFlow(): Flow<Usuario?> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUserId = firebaseAuth.currentUser?.uid

            firestoreListener?.remove()

            if (currentUserId.isNullOrBlank()) {
                trySend(null)
            } else {
                firestoreListener = db.collection("usuarios").document(currentUserId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            trySend(null)
                            return@addSnapshotListener
                        }
                        trySend(snapshot?.toObject(Usuario::class.java))
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
     * Obtiene un Flow con la lista de [RegistroBienestar] del usuario actual.
     * Emite una lista vacía si el usuario cierra sesión.
     *
     * @return Un [Flow] que emite la lista de [RegistroBienestar].
     */
    fun getRegistrosBienestar(): Flow<List<RegistroBienestar>> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUserId = firebaseAuth.currentUser?.uid

            firestoreListener?.remove()

            if (currentUserId.isNullOrBlank()) {
                trySend(emptyList())
            } else {
                firestoreListener = db.collection("usuarios").document(currentUserId)
                    .collection("registrosBienestar")
                    .orderBy("fecha", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val registros =
                            snapshot?.toObjects(RegistroBienestar::class.java) ?: emptyList()
                        trySend(registros)
                    }
            }
        }

        auth.addAuthStateListener(authListener)

        // Cuando el Flow se cancela, limpia ambos listeners
        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }

    /**
     * Obtiene un Flow con la lista de [Recurso] públicos.
     * Esta función solo emite recursos que han sido marcados como `validado = true`
     * en Firestore, cumpliendo con el requisito RF_APP_3.6.
     *
     * @return Un [Flow] que emite la lista de [Recurso] validados.
     */
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

    /**
     * Obtiene un Flow con la lista de todos los [EjercicioGuiado] de la base de datos.
     *
     * @return Un [Flow] que emite la lista de [EjercicioGuiado].
     */
    fun getEjerciciosGuiados(): Flow<List<EjercicioGuiado>> = callbackFlow {
        val listener = db.collection("ejerciciosGuiados")
            .addSnapshotListener { snapshot, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                val ejercicios = snapshot?.toObjects(EjercicioGuiado::class.java) ?: emptyList()
                trySend(ejercicios)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Obtiene un Flow con la lista de [ActividadComunidad] públicas.
     * Las actividades se emiten en tiempo real y están ordenadas por fecha programada descendente.
     *
     * @return Un [Flow] que emite la lista de [ActividadComunidad].
     */
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

    /**
     * Obtiene el historial de chat ([MensajeChatbot]) del usuario actual.
     * Los mensajes se emiten en tiempo real y están ordenados por fecha ascendente.
     *
     * @return Un [Flow] que emite la lista de [MensajeChatbot]. Emite lista vacía si no está logueado.
     */
    fun getHistorialChat(): Flow<List<MensajeChatbot>> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val currentUserId = firebaseAuth.currentUser?.uid

            firestoreListener?.remove()

            if (currentUserId.isNullOrBlank()) {
                trySend(emptyList())
            } else {
                firestoreListener = db.collection("usuarios").document(currentUserId)
                    .collection("chatHistory")
                    .orderBy("fecha", Query.Direction.ASCENDING)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            trySend(emptyList())
                            return@addSnapshotListener
                        }
                        val mensajes = snapshot?.toObjects(MensajeChatbot::class.java) ?: emptyList()
                        trySend(mensajes)
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
     * Añade un nuevo [RegistroBienestar] a la subcolección del usuario actual.
     * Es una función suspendida (asíncrona).
     *
     * @param registro El objeto [RegistroBienestar] a guardar.
     * @throws IllegalStateException Si el usuario no está autenticado.
     */
    suspend fun addRegistroBienestar(registro: RegistroBienestar) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            throw IllegalStateException("Usuario no autenticado. No se puede guardar el registro.")
        }

        db.collection("usuarios").document(currentUserId)
            .collection("registrosBienestar").add(registro).await()
    }

    /**
     * Añade un nuevo [MensajeChatbot] a la subcolección del usuario actual.
     * Es una función suspendida (asíncrona).
     *
     * @param mensaje El objeto [MensajeChatbot] a guardar.
     * @throws IllegalStateException Si el usuario no está autenticado.
     */
    suspend fun addChatMessage(mensaje: MensajeChatbot) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            throw IllegalStateException("Usuario no autenticado. No se puede guardar el mensaje.")
        }

        db.collection("usuarios").document(currentUserId)
            .collection("chatHistory").add(mensaje).await()
    }
}