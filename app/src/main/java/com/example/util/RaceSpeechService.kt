package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

/**
 * Configuration and state for the Race Audio Coach / Text-To-Speech engine.
 */
data class SpeechCoachState(
  val isReady: Boolean = false,
  val isEnabled: Boolean = true,
  val lapAnnouncementsEnabled: Boolean = true,
  val speedAlertsEnabled: Boolean = true,
  val speedAlertThresholdKmh: Float = 250f,
  val isSpeaking: Boolean = false,
  val lastSpokenMessage: String = "Radio standby",
  val speechRate: Float = 1.06f,
  val pitch: Float = 1.02f
)

/**
 * Manages the Android TextToSpeech API to deliver real-time race engineer radio comms:
 * - Reads out recorded lap times with minutes, seconds, tenths, and delta comparisons
 * - Announces speed alerts when vehicles cross critical speed thresholds
 * - Provides configurable thresholds, cooldown hysteresis, and voice controls
 */
class RaceSpeechService private constructor(context: Context) {

  private val appContext = context.applicationContext
  private var tts: TextToSpeech? = null

  private val _state = MutableStateFlow(SpeechCoachState())
  val state: StateFlow<SpeechCoachState> = _state.asStateFlow()

  // Cooldown & hysteresis tracking to prevent audio spamming
  private var lastSpeedAlertTimestamp: Long = 0L
  private var isSpeedAlertArmed: Boolean = true
  private var lastAnnouncedLapNumber: Int = -1

  companion object {
    private const val TAG = "RaceSpeechService"
    private const val SPEED_ALERT_COOLDOWN_MS = 10_000L // 10s cooldown
    private const val SPEED_HYSTERESIS_DELTA_KMH = 15f   // Must drop 15 km/h below threshold to re-arm

    val SPEED_ALERT_PRESETS = listOf(180f, 220f, 250f, 280f, 300f, 320f)

    @Volatile
    private var instance: RaceSpeechService? = null

    fun getInstance(context: Context): RaceSpeechService {
      return instance ?: synchronized(this) {
        instance ?: RaceSpeechService(context).also { instance = it }
      }
    }
  }

  init {
    initializeTts()
  }

  private fun initializeTts() {
    try {
      tts = TextToSpeech(appContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
          val result = tts?.setLanguage(Locale.US)
          if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale.getDefault())
          }
          tts?.setSpeechRate(_state.value.speechRate)
          tts?.setPitch(_state.value.pitch)

          tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
              _state.update { it.copy(isSpeaking = true) }
            }

            override fun onDone(utteranceId: String?) {
              _state.update { it.copy(isSpeaking = false) }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
              _state.update { it.copy(isSpeaking = false) }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
              _state.update { it.copy(isSpeaking = false) }
              Log.w(TAG, "TTS utterance error code: $errorCode")
            }
          })

          _state.update { it.copy(isReady = true) }
          Log.i(TAG, "Android TextToSpeech initialized successfully.")
        } else {
          Log.e(TAG, "Failed to initialize Android TextToSpeech, status: $status")
          _state.update { it.copy(isReady = false) }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Exception initializing TextToSpeech", e)
      _state.update { it.copy(isReady = false) }
    }
  }

  /**
   * Speak arbitrary race text through the Android TTS engine.
   */
  fun speak(text: String, flush: Boolean = false) {
    val current = _state.value
    if (!current.isEnabled || !current.isReady || tts == null) return

    _state.update { it.copy(lastSpokenMessage = text) }

    val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
    val utteranceId = "race_radio_${System.currentTimeMillis()}"

    val params = Bundle().apply {
      putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
    }

    try {
      tts?.speak(text, queueMode, params, utteranceId)
    } catch (e: Exception) {
      Log.e(TAG, "Error invoking speak()", e)
    }
  }

  /**
   * Formats and announces a completed lap time in natural race engineer terminology.
   * Example: "Lap 2. 1 minute, 18 point 4 seconds. New fastest lap!"
   */
  fun announceLapTime(
    lapNumber: Int,
    lapTimeMillis: Long,
    isBestLap: Boolean = false,
    deltaToBestMillis: Long? = null
  ) {
    if (!_state.value.isEnabled || !_state.value.lapAnnouncementsEnabled) return
    if (lapTimeMillis <= 0L) return

    val totalSeconds = lapTimeMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = (lapTimeMillis % 1000) / 100

    val timeSpeech = StringBuilder()
    if (minutes > 0) {
      timeSpeech.append("$minutes minute${if (minutes > 1) "s" else ""}, ")
    }
    timeSpeech.append("$seconds point $tenths seconds")

    val message = buildString {
      append("Lap $lapNumber: $timeSpeech. ")
      if (isBestLap) {
        append("New personal best lap! Outstanding pace.")
      } else if (deltaToBestMillis != null && deltaToBestMillis > 0) {
        val deltaSec = deltaToBestMillis / 1000
        val deltaTenths = (deltaToBestMillis % 1000) / 100
        append("Plus $deltaSec point $deltaTenths seconds to delta.")
      }
    }

    lastAnnouncedLapNumber = lapNumber
    speak(message, flush = true)
  }

  /**
   * Evaluates current vehicle speed against alert threshold with hysteresis and cooldown.
   */
  fun checkSpeedAlert(currentSpeedKmh: Float) {
    val current = _state.value
    if (!current.isEnabled || !current.speedAlertsEnabled || !current.isReady) return

    val threshold = current.speedAlertThresholdKmh
    val now = System.currentTimeMillis()

    // Re-arm when speed drops sufficiently below threshold
    if (!isSpeedAlertArmed && currentSpeedKmh < (threshold - SPEED_HYSTERESIS_DELTA_KMH)) {
      isSpeedAlertArmed = true
    }

    // Trigger alert when crossing threshold with cooldown protection
    if (isSpeedAlertArmed && currentSpeedKmh >= threshold) {
      if (now - lastSpeedAlertTimestamp > SPEED_ALERT_COOLDOWN_MS) {
        lastSpeedAlertTimestamp = now
        isSpeedAlertArmed = false

        val speedInt = currentSpeedKmh.toInt()
        val alertMessage = "Warning! Speed alert: $speedInt kilometers per hour reached!"
        speak(alertMessage, flush = true)
      }
    }
  }

  /**
   * Manual announcement of current top speed or milestone.
   */
  fun announceCurrentSpeed(speedKmh: Float) {
    val speedInt = speedKmh.toInt()
    speak("Current speed: $speedInt kilometers per hour.", flush = true)
  }

  /**
   * Tests race engineer voice radio link.
   */
  fun testRadioVoice() {
    speak("Race engineer radio check. Comms loud and clear. All telemetry systems active.", flush = true)
  }

  private var lastBatteryWarningTimestamp: Long = 0L

  /**
   * Delivers audio radio warning when device battery drops to low or critical levels.
   */
  fun announceBatteryWarning(batteryLevelPercent: Int, isCritical: Boolean) {
    val now = System.currentTimeMillis()
    if (now - lastBatteryWarningTimestamp < 45_000L) return // 45s cooldown
    lastBatteryWarningTimestamp = now
    val message = if (isCritical) {
      "Critical pit alert! Device power at $batteryLevelPercent percent. Plug in power now."
    } else {
      "Telemetry notice. Device battery running low at $batteryLevelPercent percent."
    }
    speak(message, flush = false)
  }

  /**
   * Toggle all voice announcements on/off.
   */
  fun toggleAudioEnabled() {
    val newEnabled = !_state.value.isEnabled
    _state.update { it.copy(isEnabled = newEnabled) }
    if (!newEnabled) {
      stopSpeaking()
    } else {
      speak("Race audio enabled.", flush = true)
    }
  }

  fun setAudioEnabled(enabled: Boolean) {
    _state.update { it.copy(isEnabled = enabled) }
    if (!enabled) stopSpeaking()
  }

  fun setLapAnnouncementsEnabled(enabled: Boolean) {
    _state.update { it.copy(lapAnnouncementsEnabled = enabled) }
  }

  fun setSpeedAlertsEnabled(enabled: Boolean) {
    _state.update { it.copy(speedAlertsEnabled = enabled) }
  }

  fun setSpeedAlertThreshold(thresholdKmh: Float) {
    _state.update { it.copy(speedAlertThresholdKmh = thresholdKmh) }
    isSpeedAlertArmed = true // Reset armed state for new threshold
  }

  fun stopSpeaking() {
    try {
      tts?.stop()
      _state.update { it.copy(isSpeaking = false) }
    } catch (e: Exception) {
      Log.w(TAG, "Error stopping TTS", e)
    }
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
      tts = null
      _state.update { it.copy(isReady = false, isSpeaking = false) }
    } catch (e: Exception) {
      Log.w(TAG, "Error shutting down TTS", e)
    }
  }
}
