package com.zenia.app

import android.app.Application
import com.zenia.app.util.ProfanityFilter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class ZeniaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            ProfanityFilter.loadFromCsv(this@ZeniaApplication)
        }
    }
}