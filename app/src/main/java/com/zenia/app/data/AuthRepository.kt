package com.zenia.app.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio central encargado de la autenticación y la gestión de datos del usuario en Firestore.
 * Sigue el patrón "Single Source of Truth" combinando Firebase Auth y Firestore.
 *
 * @property auth Instancia de FirebaseAuth para gestión de sesión.
 * @property db Instancia de Firestore para base de datos.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    // Scope para mantener vivos los Flows compartidos (shareIn) mientras la app viva.
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Obtiene el ID único (UID) del usuario autenticado actualmente.
     * Retorna null si no hay sesión iniciada.
     */
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

    /**
     * Flujo compartido (Hot Flow) que expone los datos del usuario en tiempo real.
     *
     * Características clave:
     * 1. **Reactivo:** Usa [flatMapLatest] para cambiar la suscripción de Firestore automáticamente si cambia el UID.
     * 2. **Optimizado:** Usa [shareIn] con `replay = 1` para mantener el último dato en memoria caché.
     * 3. **Eficiente:** Evita lecturas redundantes a Firestore cuando múltiples pantallas observan al usuario.
     */
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
                                close(error)
                                return@addSnapshotListener
                            }
                            if (snapshot != null && snapshot.exists()) {
                                trySend(snapshot.toObject(Usuario::class.java))
                            }
                        }
                    awaitClose { registration.remove() }
                }
            }
        }
        .shareIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000), // Mantiene la conexión 5s después de perder suscriptores
            replay = 1 // Retiene el último valor para nuevos suscriptores inmediatos
        )

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
            // SetOptions.merge() evita sobrescribir campos existentes (como 'apodo' o 'avatar')
            userRef.set(datosActualizados, SetOptions.merge()).await()
        }
    }

    /**
     * Envía un correo electrónico de verificación al usuario actual mediante Firebase Auth.
     */
    suspend fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    /**
     * Obtiene el flujo observable del usuario actual.
     * @return Un [Flow] que emite objetos [Usuario] o null si no hay sesión.
     */
    fun getUsuarioFlow(): Flow<Usuario?> = _sharedUserFlow

    /**
     * Cierra la sesión actual en Firebase Auth.
     * Esto disparará automáticamente la actualización de [_sharedUserFlow] a null.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Elimina permanentemente los datos del usuario de Firestore.
     *
     * IMPORTANTE: La implementación original que borraba desde el cliente ha sido eliminada
     * por ser peligrosa y no escalable. Un borrado masivo desde el cliente puede fallar
     * y dejar datos inconsistentes o crashear la app por exceso de memoria.
     *
     * SOLUCIÓN CORRECTA: Utilizar una Cloud Function en Firebase que se active
     * al eliminar un usuario de Firebase Auth y borre sus subcolecciones de forma segura
     * en el backend.
     * @see https://firebase.google.com/products/extensions/firebase-delete-user-data
     *
     * @param userId El ID del usuario a eliminar (actualmente no se usa).
     */
    suspend fun deleteUserData(userId: String) {
        // Esta función ahora solo registra una advertencia. La lógica real debe estar en el backend.
        Log.w(
            "AuthRepository",
            "La eliminación de datos de Firestore ($userId) debe ser manejada por una Cloud Function. " +
            "La implementación del lado del cliente ha sido deshabilitada para evitar la pérdida de datos o crashes."
        )
        // La eliminación de la cuenta de Auth (auth.currentUser.delete()) se maneja por separado
        // en el ViewModel, ya que es una operación distinta.
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
