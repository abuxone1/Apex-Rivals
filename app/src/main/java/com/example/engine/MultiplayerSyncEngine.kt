package com.example.engine

import com.example.model.MultiplayerRacer
import com.example.model.MultiplayerRoom
import com.example.model.PlatformType
import com.example.model.Track
import com.example.model.VehicleType
import kotlin.random.Random

class MultiplayerSyncEngine(
  private val room: MultiplayerRoom
) {

  private var racersState: MutableList<MultiplayerRacer> = room.racers.toMutableList()
  private val eventLog = mutableListOf<String>()

  init {
    eventLog.add("Cross-Platform NetSync Connected: Server Tick 60Hz")
    eventLog.add("Lobby initialized with ${room.racers.size} drivers across PC, PS5, Xbox, iOS, Android")
  }

  fun updateCompetitors(dtSeconds: Float, localPlayerProgress: Float, localPlayerSpeed: Float): List<MultiplayerRacer> {
    val updated = racersState.mapIndexed { index, racer ->
      if (racer.isLocalPlayer) {
        racer.copy(
          trackProgress = localPlayerProgress,
          speedKmh = localPlayerSpeed
        )
      } else {
        // Dynamic simulated competitor movement with slight randomized variance
        val speedVariation = (sin(System.currentTimeMillis() / 1000.0 + index) * 8.0).toFloat()
        val currentSpeed = (racer.speedKmh + speedVariation).coerceIn(120f, 340f)
        val progressDelta = (currentSpeed / 3.6f * dtSeconds) / room.track.totalLengthMeters
        val newProgress = (racer.trackProgress + progressDelta) % 1.0f

        racer.copy(
          trackProgress = newProgress,
          speedKmh = currentSpeed,
          pingMs = (racer.pingMs + Random.nextInt(-2, 3)).coerceIn(14, 85)
        )
      }
    }

    // Sort by progress to compute current grid positions
    val sorted = updated.sortedByDescending { it.trackProgress }
    val leaderProgress = sorted.firstOrNull()?.trackProgress ?: 0f

    val finalRacers = sorted.mapIndexed { pos, r ->
      val gapMeters = (leaderProgress - r.trackProgress) * room.track.totalLengthMeters
      val gapSec = if (gapMeters > 0 && r.speedKmh > 10f) gapMeters / (r.speedKmh / 3.6f) else 0f
      r.copy(
        currentPos = pos + 1,
        gapToLeaderSeconds = gapSec
      )
    }

    racersState = finalRacers.toMutableList()

    // Periodically add dynamic commentary events
    if (Random.nextInt(0, 300) == 42) {
      val randomRacer = racersState.random()
      val events = listOf(
        "${randomRacer.name} [${randomRacer.platform.shortCode}] set purple Sector 2 split!",
        "Drafting slipstream: ${randomRacer.name} clocked ${randomRacer.speedKmh.toInt()} km/h!",
        "Knee scrape at 62° lean by ${randomRacer.name}!",
        "Cross-platform packet sync: 0% packet loss on 60Hz tick."
      )
      eventLog.add(0, events.random())
      if (eventLog.size > 8) eventLog.removeAt(eventLog.lastIndex)
    }

    return finalRacers
  }

  fun getRecentEvents(): List<String> = eventLog.toList()
}

private fun sin(x: Double): Double = kotlin.math.sin(x)
