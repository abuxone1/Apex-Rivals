package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.model.Track
import com.example.model.Vehicle
import com.example.model.VehicleType

enum class GameMode(val displayName: String, val description: String) {
  GRAND_PRIX("Grand Prix Race", "8-Car grid start, live positions, drafting & checkered flag!"),
  TIME_ATTACK("Time Attack & Ghost", "Qualifying sprint to beat lap records & ghost delta."),
  SPEED_TRAP("Speed Trap Sprint", "Blitz radar checkpoints on straightaways for max speed."),
  DRIFT_CHALLENGE("Apex Drift & Precision", "Slide through apexes and rack up drift score multipliers.")
}

enum class RaceState {
  PRE_GRID,
  LIGHTS_COUNTDOWN,
  RACING,
  FINAL_LAP,
  FINISHED_PODIUM,
  PAUSED
}

enum class CameraPerspective(val label: String) {
  CHASE_CAM("Chase Cam"),
  COCKPIT_CAM("Cockpit Cam"),
  HOOD_CAM("Hood Cam")
}

enum class DrivingAssistLevel(val label: String) {
  ROOKIE("Rookie (Auto-Gears, ABS, TCS)"),
  PRO("Pro (Manual Gears, ABS, Low TCS)"),
  SIMULATION("Simulation (Pure Raw Physics)")
}

data class AirRival(
  val id: String,
  val name: String,
  val teamName: String,
  val carName: String,
  val colorHex: Long,
  var laneOffset: Float, // -0.8f (left) to +0.8f (right)
  var distanceMeters: Float,
  var speedKmh: Float,
  var currentLap: Int = 1,
  var currentRank: Int = 2,
  var isOvertaken: Boolean = false
) {
  val composeColor: Color get() = Color(colorHex)
}

data class RoadSegment(
  val segmentIndex: Int,
  val curvature: Float, // -1.0 (hard left) to +1.0 (hard right)
  val elevation: Float, // hill up / down
  val isBrakingZone: Boolean = false,
  val isSpeedTrap: Boolean = false,
  val hasDistanceBoard: Int? = null, // e.g. 200, 150, 100, 50 meters
  val curbColor: Long = 0xFFE53935
)

data class RacingGameHudState(
  val currentRank: Int = 1,
  val totalRivals: Int = 8,
  val currentLap: Int = 1,
  val totalLaps: Int = 3,
  val intervalAheadSeconds: Float? = null,
  val intervalBehindSeconds: Float? = null,
  val rivalAheadName: String? = null,
  val speedKmh: Float = 0f,
  val rpm: Int = 1200,
  val maxRpm: Int = 9200,
  val redlineRpm: Int = 8500,
  val gear: Int = 1,
  val isAutoGears: Boolean = true,
  val nitroChargePercent: Float = 1.0f,
  val isNitroActive: Boolean = false,
  val isDrsAvailable: Boolean = false,
  val isDrsActive: Boolean = false,
  val isDrafting: Boolean = false,
  val slipstreamBonusKmh: Float = 0f,
  val driftPoints: Int = 0,
  val currentLapTimeMs: Long = 0L,
  val bestLapTimeMs: Long? = null,
  val lastLapTimeMs: Long? = null,
  val sector1TimeMs: Long? = null,
  val sector2TimeMs: Long? = null,
  val sector3TimeMs: Long? = null,
  val deltaVsBestSeconds: Float? = null,
  val activeAlertMessage: String? = null,
  val activeAlertColor: Long = 0xFF00E5FF,
  val countdownLights: Int = 0 // 0 = off, 1..5 = red lights, 6 = green / lights out!
)

data class RaceFinishSummary(
  val finishingPosition: Int,
  val totalRacers: Int,
  val totalTimeMs: Long,
  val bestLapMs: Long,
  val topSpeedKmh: Float,
  val overtakesCount: Int,
  val driftScore: Int,
  val xpEarned: Int,
  val creditsEarned: Int,
  val trackName: String,
  val vehicleName: String,
  val isNewPersonalBest: Boolean,
  val newlyUnlockedBadges: List<String> = emptyList()
)
