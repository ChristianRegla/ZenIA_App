package com.zenia.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zenia.app.util.ProfanityFilter
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class ZeniaApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            ProfanityFilter.loadFromCsv(applicationContext)

            clearOldCache()
        }
    }

    private fun clearOldCache() {
        try {
            val pdfsDir = File(cacheDir, "pdfs")
            val imagesDir = File(cacheDir, "images")

            if (pdfsDir.exists()) {
                pdfsDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
            if (imagesDir.exists()) {
                imagesDir.listFiles()?.forEach { file ->
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}