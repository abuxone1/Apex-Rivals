package com.example.data.gemini

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

enum class LyriaMusicModel(val modelId: String, val displayName: String, val maxDurationSeconds: Int) {
  CLIP("lyria-3-clip-preview", "Lyria 3 Clip (Up to 30s)", 30),
  PRO("lyria-3-pro-preview", "Lyria 3 Pro (Full Track)", 180)
}

data class GeneratedMusicTrack(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val prompt: String,
  val modelUsed: String,
  val durationSeconds: Int,
  val audioBase64: String? = null,
  val mimeType: String = "audio/mp3",
  val timestamp: Long = System.currentTimeMillis(),
  val isSimulated: Boolean = false
)

class GeminiMusicService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(90, TimeUnit.SECONDS)
    .readTimeout(90, TimeUnit.SECONDS)
    .writeTimeout(90, TimeUnit.SECONDS)
    .build()

  private val mediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun generateRacingMusic(
    prompt: String,
    model: LyriaMusicModel = LyriaMusicModel.CLIP
  ): GeneratedMusicTrack = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext GeneratedMusicTrack(
        title = "Apex Pitlane Soundtrack",
        prompt = prompt,
        modelUsed = "${model.displayName} (Simulation Mode - Set GEMINI_API_KEY for live audio)",
        durationSeconds = if (model == LyriaMusicModel.CLIP) 30 else 120,
        isSimulated = true
      )
    }

    try {
      val rootJson = JSONObject()
      val contentsArray = JSONArray()
      val userObj = JSONObject()
      val partsArray = JSONArray()

      val enhancedPrompt = "Motorsport high-adrenaline race soundtrack: $prompt. Dynamic driving tempo, rich synth bass and percussion."
      partsArray.put(JSONObject().put("text", enhancedPrompt))
      userObj.put("parts", partsArray)
      contentsArray.put(userObj)
      rootJson.put("contents", contentsArray)

      val genConfig = JSONObject()
      val modalities = JSONArray().put("AUDIO")
      genConfig.put("responseModalities", modalities)
      rootJson.put("generationConfig", genConfig)

      val url = "https://generativelanguage.googleapis.com/v1beta/models/${model.modelId}:generateContent?key=$apiKey"
      val requestBody = rootJson.toString().toRequestBody(mediaType)
      val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        Log.w("GeminiMusicService", "Music generation error ${response.code}: $responseString")
        return@withContext GeneratedMusicTrack(
          title = "Apex Track Sound: ${prompt.take(24)}",
          prompt = prompt,
          modelUsed = model.displayName,
          durationSeconds = if (model == LyriaMusicModel.CLIP) 30 else 90,
          isSimulated = true
        )
      }

      val responseJson = JSONObject(responseString)
      val candidates = responseJson.optJSONArray("candidates")
      val firstCand = candidates?.optJSONObject(0)
      val contentObj = firstCand?.optJSONObject("content")
      val parts = contentObj?.optJSONArray("parts")

      var audioBase64: String? = null
      var detectedMime = "audio/mp3"

      if (parts != null) {
        for (i in 0 until parts.length()) {
          val p = parts.optJSONObject(i)
          if (p != null && p.has("inlineData")) {
            val inlineData = p.optJSONObject("inlineData")
            audioBase64 = inlineData?.optString("data")
            detectedMime = inlineData?.optString("mimeType", "audio/mp3") ?: "audio/mp3"
            break
          }
        }
      }

      GeneratedMusicTrack(
        title = "Apex Theme: ${prompt.take(28)}",
        prompt = prompt,
        modelUsed = model.displayName,
        durationSeconds = if (model == LyriaMusicModel.CLIP) 30 else 150,
        audioBase64 = audioBase64,
        mimeType = detectedMime,
        isSimulated = audioBase64 == null
      )
    } catch (e: Exception) {
      Log.e("GeminiMusicService", "Error calling Lyria API", e)
      GeneratedMusicTrack(
        title = "Apex Soundtrack: ${prompt.take(20)}",
        prompt = prompt,
        modelUsed = model.displayName,
        durationSeconds = 30,
        isSimulated = true
      )
    }
  }
}
