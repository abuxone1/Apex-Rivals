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

data class LiveVoiceMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val sender: String, // "Driver" or "Race Engineer (gemini-3.8-live)"
  val text: String,
  val isAudioPlaying: Boolean = false,
  val timestamp: Long = System.currentTimeMillis()
)

class GeminiLiveVoiceService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val mediaType = "application/json; charset=utf-8".toMediaType()

  private val systemInstruction = """
    You are the Chief Race Engineer on the team pit-wall radio speaking in real-time over live audio with your racing driver using gemini-3.8-live.
    Be fast, precise, calm, and assertive like an F1 or MotoGP race engineer (e.g. 'Understood, copy that', 'Delta is plus 0.4', 'Watch tire surface temps in turn 4', 'Box box for soft slicks').
    Keep spoken radio transmissions to 1-2 sharp, tactical sentences.
  """.trimIndent()

  suspend fun conductLiveVoiceTurn(
    driverSpokenPrompt: String,
    conversationHistory: List<LiveVoiceMessage>
  ): LiveVoiceMessage = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      val simulatedReplies = listOf(
        "Copy that driver, your sector 2 delta is -0.18s. Keep pushing on the exit of Ascari.",
        "Understood. Tires are in optimal window at 95°C. You have 3 laps of battery deploy remaining.",
        "Box box box at the end of this lap for medium compound slicks, front wing angle +1.5 degrees.",
        "Radio check loud and clear. Traffic ahead is 4.2 seconds back, clear air ahead.",
        "Good recovery out of turn 1! Manage rear traction on throttle application."
      )
      return@withContext LiveVoiceMessage(
        sender = "Race Engineer (gemini-3.8-live Simulation)",
        text = simulatedReplies.random()
      )
    }

    try {
      val rootJson = JSONObject()

      // System instruction
      val sysObj = JSONObject()
      sysObj.put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
      rootJson.put("systemInstruction", sysObj)

      // Conversation turns
      val contentsArray = JSONArray()
      for (msg in conversationHistory.takeLast(6)) {
        val msgObj = JSONObject()
        msgObj.put("role", if (msg.sender.startsWith("Driver")) "user" else "model")
        msgObj.put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
        contentsArray.put(msgObj)
      }

      val currentObj = JSONObject()
      currentObj.put("role", "user")
      currentObj.put("parts", JSONArray().put(JSONObject().put("text", driverSpokenPrompt)))
      contentsArray.put(currentObj)
      rootJson.put("contents", contentsArray)

      val genConfig = JSONObject()
      genConfig.put("temperature", 0.6)
      rootJson.put("generationConfig", genConfig)

      // Use gemini-3.8-live model
      val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-live:generateContent?key=$apiKey"
      val requestBody = rootJson.toString().toRequestBody(mediaType)
      val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        Log.w("GeminiLiveVoice", "Live API error ${response.code}: $responseString")
        return@withContext LiveVoiceMessage(
          sender = "Race Engineer (gemini-3.8-live)",
          text = "Copy driver, radio interference on pit straight. Delta is stable, maintain current rhythm."
        )
      }

      val responseJson = JSONObject(responseString)
      val candidates = responseJson.optJSONArray("candidates")
      val firstCand = candidates?.optJSONObject(0)
      val contentObj = firstCand?.optJSONObject("content")
      val parts = contentObj?.optJSONArray("parts")

      val replyText = buildString {
        if (parts != null) {
          for (i in 0 until parts.length()) {
            val p = parts.optJSONObject(i)
            val t = p?.optString("text").orEmpty()
            append(t)
          }
        }
      }

      LiveVoiceMessage(
        sender = "Race Engineer (gemini-3.8-live)",
        text = if (replyText.isNotBlank()) replyText.trim() else "Copy, pit radio confirmed."
      )
    } catch (e: Exception) {
      Log.e("GeminiLiveVoice", "Live API voice exception", e)
      LiveVoiceMessage(
        sender = "Race Engineer (gemini-3.8-live)",
        text = "Understood. Pit comms online, telemetry telemetry is green."
      )
    }
  }
}
