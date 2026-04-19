package com.zenia.app.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.Locale

class ZeniaTranslator {
    private val languageIdentifier = LanguageIdentification.getClient()

    private val targetLanguageCode = Locale.getDefault().language

    suspend fun translateTextIfNeeded(text: String): String? {
        return try {
            val sourceLanguageCode = languageIdentifier.identifyLanguage(text).await()

            if (sourceLanguageCode == targetLanguageCode || sourceLanguageCode == "und") {
                return null
            }

            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguageCode)
                .setTargetLanguage(targetLanguageCode)
                .build()

            val translator = Translation.getClient(options)

            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()

            val translatedText = translator.translate(text).await()

            translator.close()

            translatedText
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}