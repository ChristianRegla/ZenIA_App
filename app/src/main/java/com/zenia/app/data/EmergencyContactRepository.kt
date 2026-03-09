package com.zenia.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zenia.app.model.EmergencyContact
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyContactRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    private fun contactsCollection() =
        auth.currentUser?.uid?.let { uid ->
            db.collection(FirestoreCollections.USERS)
                .document(uid)
                .collection(FirestoreCollections.EMERGENCY_CONTACTS)
        }

    fun getContactsFlow(): Flow<List<EmergencyContact>> = callbackFlow {

        val collection = contactsCollection()

        if (collection == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = collection.addSnapshotListener { snapshots, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val contacts = snapshots?.documents?.map {
                EmergencyContact(
                    id = it.id,
                    name = it.getString("name") ?: "",
                    phone = it.getString("phone") ?: ""
                )
            } ?: emptyList()

            trySend(contacts)
        }

        awaitClose { registration.remove() }
    }

    suspend fun addContact(name: String, phone: String) {
        val data = mapOf(
            "name" to name,
            "phone" to phone
        )

        contactsCollection()
            ?.add(data)
            ?.await()
    }

    suspend fun deleteContact(contactId: String) {
        contactsCollection()
            ?.document(contactId)
            ?.delete()
            ?.await()
    }
}