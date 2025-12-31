package com.zenia.app

import android.app.Application
import com.zenia.app.util.ProfanityFilter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ZeniaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Thread {
            ProfanityFilter.loadFromCsv(this)
        }.start()
    }
}