package com.zenia.app.data

import android.content.Context
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

interface AppContainer {
    val authRepository: AuthRepository
    val diaryRepository: DiaryRepository
    val chatRepository: ChatRepository
    val contentRepository: ContentRepository

    val userPreferencesRepository: UserPreferencesRepository
    val firebaseAuth: FirebaseAuth
    val healthConnectRepository: HealthConnectRepository?
}

class AppDataContainer(private val context: Context) : AppContainer {

    private val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    override val firebaseAuth: FirebaseAuth by lazy { Firebase.auth }

    override val authRepository: AuthRepository by lazy {
        AuthRepository(firebaseAuth, firestore)
    }

    override val diaryRepository: DiaryRepository by lazy {
        DiaryRepository(firebaseAuth, firestore)
    }

    override val chatRepository: ChatRepository by lazy {
        ChatRepository(firebaseAuth, firestore)
    }

    override val contentRepository: ContentRepository by lazy {
        ContentRepository(firestore)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val healthConnectRepository: HealthConnectRepository? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HealthConnectRepository(context)
        } else {
            null
        }
    }
}