package com.example.game.sound

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameSoundManager private constructor() {

  private var toneGenerator: ToneGenerator? = null
  private val scope = CoroutineScope(Dispatchers.Default)
  private var isMuted: Boolean = false

  init {
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 75)
    } catch (e: Exception) {
      Log.e("GameSoundManager", "Failed to initialize ToneGenerator", e)
    }
  }

  companion object {
    @Volatile
    private var instance: GameSoundManager? = null

    fun getInstance(): GameSoundManager {
      return instance ?: synchronized(this) {
        instance ?: GameSoundManager().also { instance = it }
      }
    }
  }

  fun toggleMute(): Boolean {
    isMuted = !isMuted
    return isMuted
  }

  fun isAudioMuted(): Boolean = isMuted

  /**
   * Plays a single red light beep for grid countdown (1..5)
   */
  fun playCountdownLightBeep() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  /**
   * Plays the high-pitched start chime when lights go out!
   */
  fun playLightsOutGo() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 300)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  /**
   * Plays gear shift sound effect pop
   */
  fun playGearShift() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 70)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  /**
   * Plays nitro boost ignition rush
   */
  fun playNitroBoost() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 220)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  /**
   * Plays slipstream drafting whistle
   */
  fun playSlipstreamDraft() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  /**
   * Plays chime when an overtake is achieved
   */
  fun playOvertakeChime() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 140)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  /**
   * Plays purple sector celebration sound
   */
  fun playSectorRecord() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 180)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  /**
   * Plays victory fanfare when crossing the checkered finish line!
   */
  fun playCheckeredFlagVictory() {
    if (isMuted) return
    scope.launch {
      try {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        delay(140)
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 140)
        delay(160)
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400)
      } catch (e: Exception) {
        // Safe fallback
      }
    }
  }

  fun release() {
    try {
      toneGenerator?.release()
      toneGenerator = null
    } catch (e: Exception) {
      // Safe fallback
    }
  }
}
