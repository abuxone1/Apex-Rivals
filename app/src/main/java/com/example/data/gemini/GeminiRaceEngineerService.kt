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

enum class GeminiCoachModel(val modelId: String, val displayName: String, val description: String) {
  FLASH("gemini-3.5-flash", "Gemini 3.5 Flash", "Standard Race Strategy & Search Grounding"),
  PRO("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "Deep Telemetry & Vehicle Dynamics"),
  FLASH_LITE("gemini-3.1-flash-lite-preview", "Gemini 3.1 Flash Lite", "Instant Pit-Wall Advice")
}

enum class ChatbotRole(val displayName: String, val roleDescription: String, val systemInstruction: String) {
  CHIEF_ENGINEER(
    "Chief Race Engineer",
    "Pit-wall tactics & overall delta balance",
    "You are the Chief Race Engineer on the team pit wall. Your role is analyzing overall race strategy, driver deltas, sector splits, traffic management, and vehicle setup compromises for maximum race pace."
  ),
  AERODYNAMICIST(
    "Aero & CFD Specialist",
    "Downforce, drag & dirty air vortices",
    "You are the Lead Aerodynamicist and CFD Specialist. Your role is advising on front/rear wing angles, ground-effect venture tunnels, drag coefficient, high vs low downforce circuits, and dirty air wake control."
  ),
  TIRE_STRATEGIST(
    "Tire & Telemetry Strategist",
    "Thermal degradation & pit windows",
    "You are the Lead Tire and Telemetry Strategist. Your role is calculating tire surface and carcass temperatures, degradation curves, undercut/overcut windows, and compound selections (soft/medium/hard/wet)."
  )
}

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val role: String, // "user" or "model"
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val searchSources: List<String> = emptyList(),
  val modelUsed: String? = null
)

class GeminiRaceEngineerService {

  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val mediaType = "application/json; charset=utf-8".toMediaType()

  suspend fun sendMessage(
    history: List<ChatMessage>,
    newPrompt: String,
    selectedModel: GeminiCoachModel = GeminiCoachModel.FLASH,
    enableSearchGrounding: Boolean = true,
    enableMapsGrounding: Boolean = false,
    role: ChatbotRole = ChatbotRole.CHIEF_ENGINEER
  ): ChatMessage = withContext(Dispatchers.IO) {
    val apiKey = try {
      BuildConfig.GEMINI_API_KEY
    } catch (e: Throwable) {
      ""
    }

    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      // Graceful offline simulated response if API key is not yet set in Secrets
      return@withContext ChatMessage(
        role = "model",
        content = generateFallbackCoaching(newPrompt, selectedModel),
        modelUsed = "${selectedModel.displayName} [${role.displayName}] (Simulation Mode - Add GEMINI_API_KEY in Secrets for live AI)"
      )
    }

    try {
      val rootJson = JSONObject()

      // System instruction customized by ChatbotRole
      val systemObj = JSONObject()
      val systemParts = JSONArray().put(JSONObject().put("text", role.systemInstruction))
      systemObj.put("parts", systemParts)
      rootJson.put("systemInstruction", systemObj)

      // Contents (Multi-turn conversation history)
      val contentsArray = JSONArray()
      // Include last 8 turns of history
      val relevantHistory = history.takeLast(8)
      for (msg in relevantHistory) {
        val msgObj = JSONObject()
        msgObj.put("role", if (msg.role == "user") "user" else "model")
        val parts = JSONArray().put(JSONObject().put("text", msg.content))
        msgObj.put("parts", parts)
        contentsArray.put(msgObj)
      }
      // Add current user prompt
      val currentMsgObj = JSONObject()
      currentMsgObj.put("role", "user")
      currentMsgObj.put("parts", JSONArray().put(JSONObject().put("text", newPrompt)))
      contentsArray.put(currentMsgObj)
      rootJson.put("contents", contentsArray)

      // Generation config
      val genConfig = JSONObject()
      genConfig.put("temperature", 0.7)
      rootJson.put("generationConfig", genConfig)

      // Grounding with Google Search & Google Maps if using gemini-3.5-flash and enabled
      if (selectedModel == GeminiCoachModel.FLASH && (enableSearchGrounding || enableMapsGrounding)) {
        val toolsArray = JSONArray()
        if (enableSearchGrounding) {
          toolsArray.put(JSONObject().put("googleSearch", JSONObject()))
        }
        if (enableMapsGrounding) {
          toolsArray.put(JSONObject().put("googleMaps", JSONObject()))
        }
        rootJson.put("tools", toolsArray)
      }

      val url = "https://generativelanguage.googleapis.com/v1beta/models/${selectedModel.modelId}:generateContent?key=$apiKey"
      val requestBody = rootJson.toString().toRequestBody(mediaType)
      val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseString = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        Log.e("GeminiService", "API error: ${response.code} $responseString")
        return@withContext ChatMessage(
          role = "model",
          content = "Telemetry Telecommunication Link Error (${response.code}). Falling back to pit wall heuristics:\n\n${generateFallbackCoaching(newPrompt, selectedModel)}",
          modelUsed = selectedModel.displayName
        )
      }

      val responseJson = JSONObject(responseString)
      val candidates = responseJson.optJSONArray("candidates")
      val firstCandidate = candidates?.optJSONObject(0)
      val contentObj = firstCandidate?.optJSONObject("content")
      val partsArray = contentObj?.optJSONArray("parts")

      val replyText = buildString {
        if (partsArray != null) {
          for (i in 0 until partsArray.length()) {
            val part = partsArray.optJSONObject(i)
            val text = part?.optString("text").orEmpty()
            append(text)
          }
        }
      }

      // Extract search grounding metadata if available
      val searchSources = mutableListOf<String>()
      val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
      val webSearchQueries = groundingMetadata?.optJSONArray("webSearchQueries")
      if (webSearchQueries != null) {
        for (i in 0 until webSearchQueries.length()) {
          val query = webSearchQueries.optString(i)
          if (query.isNotBlank()) searchSources.add("Search: $query")
        }
      }

      ChatMessage(
        role = "model",
        content = if (replyText.isNotBlank()) replyText else "No response generated by telemetry model.",
        searchSources = searchSources,
        modelUsed = selectedModel.displayName
      )
    } catch (e: Exception) {
      Log.e("GeminiService", "Exception during Gemini request", e)
      ChatMessage(
        role = "model",
        content = "Pit communication interrupted: ${e.message ?: "Unknown error"}.\n\nHeuristic Telemetry Advice:\n${generateFallbackCoaching(newPrompt, selectedModel)}",
        modelUsed = selectedModel.displayName
      )
    }
  }

  private fun generateFallbackCoaching(prompt: String, model: GeminiCoachModel): String {
    val lower = prompt.lowercase()
    return when {
      lower.contains("brake") || lower.contains("braking") -> {
        """
        🏁 **Apex Brake Zone Analysis**:
        • **Threshold Braking**: Transition from 100% initial bite to progressive trail-braking down to 20% pressure as you turn into the apex.
        • **Brake Bias**: Current telemetry indicates high front lockup risk. Adjust brake bias 1-2% rearward (54% Front / 46% Rear) to stabilize entry.
        • **Marker Point**: On Monza Turn 1 (Prima Variante), start braking exactly at the 150m board before downshifting sequentially from 6th to 1st gear.
        """.trimIndent()
      }
      lower.contains("bike") || lower.contains("lean") || lower.contains("motorcycle") -> {
        """
        🏍️ **Superbike Lean Dynamics & Knee Contact**:
        • **Apex Transition**: When rolling into 60°+ lean angle, initiate counter-steering swiftly, keeping upper body tucked inside the fairing.
        • **Throttle Application**: Delay full throttle until the bike is stood up beyond 40° to prevent rear tire slide-spin and highside.
        • **Quickshifter Timing**: Shift at 14,800 RPM in 2nd and 3rd gear to maintain maximum torque transfer without disturbing chassis pitch.
        """.trimIndent()
      }
      lower.contains("tire") || lower.contains("tyre") || lower.contains("temp") -> {
        """
        🌡️ **Tire Degradation & Thermal Management**:
        • **Optimal Thermal Window**: Soft Slicks operate best between 85°C and 105°C. Front-right tire is currently running warm on high-load right-hand sweepers.
        • **Driving Line**: Avoid aggressive curb-hopping on corner exits to minimize tire carcass shearing and overheating.
        • **Strategy**: Medium compound will offer +35% stint longevity while yielding only 0.25s per lap compared to Softs.
        """.trimIndent()
      }
      else -> {
        """
        🏎️ **Race Engineer Telemetry Telemetry Report**:
        • **Apex Speed**: Carrying an average of 188 km/h through mid-corner. Shifting brake release 5 meters later will yield a projected -0.220s delta.
        • **Aero Balance**: For high-speed circuits, reduce rear wing angle to 55% to gain +7 km/h on straights while maintaining high-G cornering grip.
        • **Throttle Modulation**: Smooth out initial 0% to 50% pedal roll-on to avoid traction control intervention and wheelspin out of low-gear hairpins.
        """.trimIndent()
      }
    }
  }
}
