package com.zenia.app.data

import android.util.Log
import com.zenia.app.model.MensajeChatbot
import com.zenia.app.model.NiaResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

class NiaApiRepository @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val url =
        "https://api-zenia.onrender.com/chat"

    suspend fun enviarMensaje(historial: List<MensajeChatbot>): Result<NiaResponse> =
        withContext(Dispatchers.IO) {

            try {
                val historyArray = org.json.JSONArray()
                for (msg in historial) {
                    val msgObject = JSONObject().apply {
                        put("role", if (msg.emisor == "usuario") "user" else "model")
                        put("text", msg.texto)
                    }
                    historyArray.put(msgObject)
                }

                val json = JSONObject().apply {
                    put("history", historyArray)
                }

                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: "{}"

                Log.d("NIA_API", "HTTP Status: ${response.code}")
                Log.d("NIA_API", "Cuerpo recibido: $responseBody")

                val jsonResponse = JSONObject(responseBody)

                if (!jsonResponse.has("mensaje_nia")) {
                    Log.e("NIA_API", "¡ERROR! El JSON no trae la llave 'mensaje_nia'. Trae esto: $responseBody")
                    val errorServidor = jsonResponse.optString("error", "Error desconocido en el servidor")
                    return@withContext Result.failure(Exception(errorServidor))
                }

                val mensajeNia = jsonResponse.getString("mensaje_nia")
                val trigger = jsonResponse.optString("trigger", "none")

                val niaResponse = NiaResponse(
                    mensaje_nia = mensajeNia,
                    trigger = trigger
                )

                Result.success(niaResponse)

            } catch (e: Exception) {
                Log.e("NIA_API", "Error llamando Worker", e)
                Result.failure(e)
            }
        }
}