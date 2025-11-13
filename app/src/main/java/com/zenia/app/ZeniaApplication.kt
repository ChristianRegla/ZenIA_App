package com.zenia.app

import android.app.Application
import com.zenia.app.data.AppContainer
import com.zenia.app.data.AppDataContainer

class ZeniaApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}