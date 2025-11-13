package com.zenia.app.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

interface AppContainer {
    val zeniaRepository: ZeniaRepository
    val userPreferencesRepository: UserPreferencesRepository
    val firebaseAuth: FirebaseAuth
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val zeniaRepository: ZeniaRepository by lazy {
        ZeniaRepository()
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val firebaseAuth: FirebaseAuth by lazy {
        Firebase.auth
    }
}