package com.zenia.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zenia.app.BuildConfig
import com.zenia.app.model.SubscriptionType
import com.zenia.app.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val baseUrl = BuildConfig.API_BASE_URL

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Flujo interno que monitorea los cambios de estado de autenticación (Login/Logout).
     * Emite el UID cuando hay usuario, o null cuando se cierra sesión.
     */
    private val _authState = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _sharedUserFlow: Flow<Usuario?> = _authState
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(null)
            } else {
                callbackFlow<Usuario?> {
                    val registration = db.collection(FirestoreCollections.USERS)
                        .document(uid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e("AuthRepository", "Error escuchando usuario en Firestore", error)
                                trySend(null)
                                return@addSnapshotListener
                            }
                            if (snapshot != null && snapshot.exists()) {
                                trySend(snapshot.toObject(Usuario::class.java))
                            } else {
                                trySend(null)
                            }
                        }
                    awaitClose { registration.remove() }
                }
            }
        }

    /**
     * Obtiene el documento del usuario actual una sola vez (Snapshot).
     * Útil para operaciones puntuales como "Crear Post" donde necesitamos los datos actuales.
     */
    suspend fun getCurrentUserSnapshot(): Usuario? {
        val uid = currentUserId ?: return null
        return try {
            val snapshot = db.collection(FirestoreCollections.USERS)
                .document(uid)
                .get()
                .await()

            snapshot.toObject(Usuario::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crea o actualiza el documento del usuario en Firestore tras el inicio de sesión.
     *
     * @param userId El UID del usuario.
     * @param email El correo electrónico del usuario.
     * @param isNewUser Si es true, crea un usuario nuevo con valores por defecto. Si es false, solo actualiza datos básicos (merge).
     */
    suspend fun createUserIfNew(userId: String, email: String?, isNewUser: Boolean) {
        val userRef = db.collection(FirestoreCollections.USERS).document(userId)

        if (isNewUser) {
            val nuevoUsuario = Usuario(
                id = userId,
                email = email ?: "",
                suscripcion = SubscriptionType.FREE
            )
            userRef.set(nuevoUsuario).await()
        } else {
            val datosActualizados = mapOf(
                "email" to (email ?: "")
            )
            userRef.set(datosActualizados, SetOptions.merge()).await()
        }
    }

    /**
     * Envía un correo electrónico de verificación al usuario actual mediante Firebase Auth.
     */
    suspend fun sendEmailVerification(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val currentUser = auth.currentUser ?: throw Exception("No hay usuario autenticado")
            val email = currentUser.email ?: throw Exception("El usuario no tiene correo electrónico")

            var nombre = "Usuario"
            try {
                val snapshot = db.collection(FirestoreCollections.USERS).document(currentUser.uid).get().await()
                nombre = snapshot.getString("apodo") ?: "Usuario"
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error al obtener apodo del usuario", e)
            }

            val json = JSONObject().apply {
                put("email", email)
                put("nombre", nombre)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/send-custom-verification")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("AuthRepository", "Correo de ZenIA enviado exitosamente")
                Result.success(Unit)
            } else {
                val errorBody = response.body?.string()
                Log.e("AuthRepository", "Error en Render: ${response.code} - $errorBody")
                Result.failure(Exception("Error del servidor: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al enviar correo de verificación", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
            }
            val body = json.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$baseUrl/send-password-reset")
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("AuthRepository", "Correo de recuperación enviado exitosamente")
                Result.success(Unit)
            } else {
                val errorBody = response.body?.string()
                Log.e("AuthRepository", "Error en Render al recuperar: ${response.code} - $errorBody")
                Result.failure(Exception("Error del servidor: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al enviar correo de recuperación", e)
            Result.failure(e)
        }
    }

    fun getUsuarioFlow(): Flow<Usuario?> = _sharedUserFlow

    fun signOut() {
        auth.signOut()
    }

    /**
     * Elimina permanentemente los datos del usuario de Firestore.
     */
    suspend fun deleteUserData(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            db.collection(FirestoreCollections.USERS).document(userId).delete().await()

            val request = Request.Builder()
                .url("$baseUrl/delete-account/$userId")
                .delete()
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                Log.d("AuthRepository", "Cuenta $userId eliminada de Firebase Auth")
                auth.signOut()
                Result.success(Unit)
            } else {
                val errorBody = response.body?.string()
                Log.e("AuthRepository", "Error borrando en Render: ${response.code} - $errorBody")
                Result.failure(Exception("Error al borrar cuenta: ${response.code}"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error eliminando usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el estado de la suscripción del usuario en Firestore.
     * Debería ser llamado desde el BillingRepository tras una compra exitosa.
     *
     * @param isPremium El nuevo estado de la suscripción.
     */
    suspend fun updateUserSubscription(isPremium: Boolean) {
        val userId = currentUserId ?: return
        val newStatus = if (isPremium) SubscriptionType.PREMIUM else SubscriptionType.FREE
        db.collection(FirestoreCollections.USERS)
            .document(userId)
            .update("suscripcion", newStatus)
            .await()
    }

    /**
     * Actualiza la información del perfil del usuario (Apodo y Avatar).
     *
     * @param userId El ID del usuario.
     * @param newNickname El nuevo apodo a guardar.
     * @param newAvatarIndex El índice del nuevo avatar seleccionado.
     */
    suspend fun updateProfile(userId: String, newNickname: String, newAvatarIndex: Int) {
        val updates = mapOf(
            "apodo" to newNickname,
            "avatarIndex" to newAvatarIndex
        )
        db.collection(FirestoreCollections.USERS)
            .document(userId)
            .update(updates)
            .await()
    }
}