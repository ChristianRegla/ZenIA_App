package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.zenia.app.model.Usuario
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun createUserIfNew(userId: String, email: String?, isNewUser: Boolean) {
        val userRef = db.collection(FirestoreCollections.USERS).document(userId)

        if (isNewUser) {
            val nuevoUsuario = Usuario(
                id = userId,
                email = email ?: "",
                suscripcion = "free"
            )
            userRef.set(nuevoUsuario).await()
        } else {
            val datosActualizados = mapOf(
                "email" to (email ?: "")
            )
            userRef.set(datosActualizados, SetOptions.merge()).await()
        }
    }

    suspend fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()?.await()
    }

    fun getUsuarioFlow(): Flow<Usuario?> = callbackFlow {
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            if (userId == null) {
                trySend(null)
            } else {
                val listener = db.collection(FirestoreCollections.USERS).document(userId)
                    .addSnapshotListener { snapshot, _ ->
                        trySend(snapshot?.toObject(Usuario::class.java))
                    }
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose { auth.removeAuthStateListener(authListener) }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun deleteUserData(userId: String) {
        try {
            val userRef = db.collection(FirestoreCollections.USERS).document(userId)
            val batch = db.batch()

            val registrosRef = userRef.collection(FirestoreCollections.WELNESS_LOGS)
            val registrosSnapshot = registrosRef.get().await()
            for (doc in registrosSnapshot.documents) batch.delete(doc.reference)

            val chatRef = userRef.collection(FirestoreCollections.CHAT_HISTORY)
            val chatSnapshot = chatRef.get().await()
            for (doc in chatSnapshot.documents) batch.delete(doc.reference)

            val diaryRef = userRef.collection(FirestoreCollections.DIARY)
            val diarySnapshot = diaryRef.get().await()
            for(doc in diarySnapshot.documents) batch.delete(doc.reference)

            batch.delete(userRef)
            batch.commit().await()
        } catch (e: Exception) {
            throw e
        }
    }
}