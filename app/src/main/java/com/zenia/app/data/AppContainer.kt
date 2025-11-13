package com.zenia.app.data

import android.content.Context

interface AppContainer {
    val zeniaRepository: ZeniaRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val zeniaRepository: ZeniaRepository by lazy {
        ZeniaRepository()
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }
}