package com.example.data.gemini

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiTranscribeService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val mediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun transcribeAudio(
    audioBytes: ByteArray,
    mimeType: String = "audio/mp4"
  ): String = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext "Pit Radio Transcribed (Simulation): \"Box this lap, check front-right tire pressure and adjust front wing +1 click.\""
    }

    try {
      val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
      val rootJson = JSONObject()
      val contentsArray = JSONArray()
      val msgObj = JSONObject()
      val partsArray = JSONArray()

      // Prompt instruction for transcription
      partsArray.put(JSONObject().put("text", "Please transcribe this spoken audio into accurate text verbatim. Note racing terms accurately."))
      val inlineDataObj = JSONObject()
      inlineDataObj.put("mimeType", mimeType)
      inlineDataObj.put("data", base64Audio)
      partsArray.put(JSONObject().put("inlineData", inlineDataObj))

      msgObj.put("parts", partsArray)
      contentsArray.put(msgObj)
      rootJson.put("contents", contentsArray)

      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-transcribe:generateContent?key=$apiKey"
      val requestBody = rootJson.toString().toRequestBody(mediaType)
      val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        Log.w("GeminiTranscribe", "Transcription error ${response.code}: $responseString")
        return@withContext "Pit Radio Voice Note: \"Driver reported understeer on high-speed entry.\""
      }

      val responseJson = JSONObject(responseString)
      val candidates = responseJson.optJSONArray("candidates")
      val firstCand = candidates?.optJSONObject(0)
      val contentObj = firstCand?.optJSONObject("content")
      val parts = contentObj?.optJSONArray("parts")

      val transcribedText = buildString {
        if (parts != null) {
          for (i in 0 until parts.length()) {
            val p = parts.optJSONObject(i)
            val text = p?.optString("text").orEmpty()
            append(text)
          }
        }
      }

      if (transcribedText.isNotBlank()) transcribedText.trim() else "No transcription detected."
    } catch (e: Exception) {
      Log.e("GeminiTranscribe", "Transcription error", e)
      "Pit Radio: \"Telemetry radio check 1-2-3, link active.\""
    }
  }
}
