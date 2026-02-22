package com.zenia.app.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.zenia.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiRepository @Inject constructor() {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generarRespuesta(mensajeUsuario: String): Result<String> {
        return try {

            val prompt = """
                Eres ZenIA, un asistente emocional empático.
                No reemplazas ayuda profesional.
                Responde breve, clara y cálidamente.
                
                Usuario: $mensajeUsuario
            """.trimIndent()

            val response = model.generateContent(
                content { text(prompt) }
            )

            val texto = response.text

            if (texto != null) {
                Result.success(texto)
            } else {
                Result.failure(Exception("Respuesta vacía"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}