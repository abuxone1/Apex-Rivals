package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.PerformanceMetrics
import java.util.Locale

enum class BadgeCategory(val label: String) {
  ALL("All"),
  SPEED("Speed"),
  CONSISTENCY("Consistency"),
  AGILITY("Agility"),
  ENDURANCE("Endurance"),
  MASTERY("Mastery")
}

enum class BadgeTier(val label: String, val colorHex: Long) {
  BRONZE("BRONZE", 0xFFCD7F32),
  SILVER("SILVER", 0xFFC0C0C0),
  GOLD("GOLD", 0xFFFFD700),
  PLATINUM("PLATINUM", 0xFF00E5FF),
  LEGENDARY("LEGENDARY", 0xFFFF1744);

  val composeColor: Color get() = Color(colorHex)
}

data class DriverBadge(
  val id: String,
  val title: String,
  val description: String,
  val category: BadgeCategory,
  val tier: BadgeTier,
  val icon: ImageVector,
  val isUnlocked: Boolean,
  val progress: Float, // 0.0f to 1.0f
  val progressText: String,
  val requirementText: String,
  val unlockedDetail: String? = null,
  val proTip: String
)

object BadgeEvaluator {

  /**
   * Evaluates performance data stored in the Room database (performance_metrics)
   * and computes the unlock status and progress for all awards.
   */
  fun evaluateBadges(metrics: List<PerformanceMetrics>, isMph: Boolean = false): List<DriverBadge> {
    val sessionCount = metrics.size
    val totalDistanceMeters = metrics.sumOf { it.getEffectiveDistanceMeters().toLong() }
    val totalDistanceKm = totalDistanceMeters / 1000.0f
    val totalDistanceMiles = totalDistanceKm * 0.621371f

    val topSpeedRecord = metrics.maxByOrNull { it.topSpeedKmh }
    val maxTopSpeedKmh = topSpeedRecord?.topSpeedKmh ?: 0f
    val maxTopSpeedConverted = if (isMph) maxTopSpeedKmh * 0.621371f else maxTopSpeedKmh
    val speedTargetKmh = 320.0f
    val speedTargetConverted = if (isMph) speedTargetKmh * 0.621371f else speedTargetKmh
    val speedUnit = if (isMph) "mph" else "km/h"

    val maxLateralGRecord = metrics.maxByOrNull { it.maxLateralG }
    val maxLateralG = maxLateralGRecord?.maxLateralG ?: 0f

    val best0to100Record = metrics.filter { it.zeroToHundredKmhSeconds > 0 }.minByOrNull { it.zeroToHundredKmhSeconds }
    val best0to100 = best0to100Record?.zeroToHundredKmhSeconds ?: 0f

    val maxAccelRecord = metrics.maxByOrNull { it.peakAccelerationG }
    val maxAccelG = maxAccelRecord?.peakAccelerationG ?: 0f

    val bestLapRecord = metrics.filter { it.lapTimeMs > 0 }.minByOrNull { it.lapTimeMs }
    val bestLapMs = bestLapRecord?.lapTimeMs ?: 0L

    val distinctTracks = metrics.map { it.trackName }.filter { it.isNotBlank() }.distinct()
    val distinctVehicles = metrics.map { it.vehicleName }.filter { it.isNotBlank() }.distinct()

    val wetSessions = metrics.filter {
      it.weatherCondition.contains("Wet", ignoreCase = true) ||
      it.weatherCondition.contains("Rain", ignoreCase = true) ||
      it.tags.any { tag -> tag.contains("Wet", ignoreCase = true) || tag.contains("Rain", ignoreCase = true) }
    }

    val maxAvgSpeedRecord = metrics.maxByOrNull { it.getComputedAvgSpeed() }
    val maxAvgSpeedKmh = maxAvgSpeedRecord?.getComputedAvgSpeed() ?: 0f

    // 1. SPEED DEMON (Speed > 320 km/h)
    val isSpeedDemonUnlocked = maxTopSpeedKmh >= speedTargetKmh
    val speedDemonProgress = if (maxTopSpeedKmh >= speedTargetKmh) 1f else (maxTopSpeedKmh / speedTargetKmh).coerceIn(0f, 1f)
    val speedDemon = DriverBadge(
      id = "speed_demon",
      title = "Speed Demon",
      description = "Surpass the 320 km/h (198.8 mph) threshold in verified Room telemetry.",
      category = BadgeCategory.SPEED,
      tier = BadgeTier.GOLD,
      icon = Icons.Default.Speed,
      isUnlocked = isSpeedDemonUnlocked,
      progress = speedDemonProgress,
      progressText = String.format(Locale.US, "%.1f / %.1f %s", maxTopSpeedConverted, speedTargetConverted, speedUnit),
      requirementText = "Achieve top speed >= 320 km/h (198.8 mph) in any session",
      unlockedDetail = if (isSpeedDemonUnlocked && topSpeedRecord != null) {
        String.format(Locale.US, "Unlocked on %s • %.1f %s (%s)", topSpeedRecord.trackName, maxTopSpeedConverted, speedUnit, topSpeedRecord.vehicleName)
      } else null,
      proTip = "Choose high-speed circuits like Monza GP or Spa Kemmel Straight with low downforce tuning."
    )

    // 2. CONSISTENCY KING (>= 5 sessions completed with steady pacing)
    val targetConsistencySessions = 5
    val isConsistencyKingUnlocked = sessionCount >= targetConsistencySessions
    val consistencyProgress = (sessionCount.toFloat() / targetConsistencySessions).coerceIn(0f, 1f)
    val consistencyKing = DriverBadge(
      id = "consistency_king",
      title = "Consistency King",
      description = "Demonstrate elite discipline by logging at least 5 completed telemetry sessions in Room DB.",
      category = BadgeCategory.CONSISTENCY,
      tier = BadgeTier.PLATINUM,
      icon = Icons.Default.EmojiEvents,
      isUnlocked = isConsistencyKingUnlocked,
      progress = consistencyProgress,
      progressText = "$sessionCount / $targetConsistencySessions Sessions",
      requirementText = "Log at least 5 telemetry sessions in the Room database",
      unlockedDetail = if (isConsistencyKingUnlocked) {
        "Unlocked with $sessionCount verified telemetry records stored in SQLite Room"
      } else null,
      proTip = "Maintain consistent braking markers and run full sessions across varied track layouts."
    )

    // 3. APEX PREDATOR (Sub-80s Lap Time)
    val targetLapTimeMs = 80000L // 1:20.000
    val isApexPredatorUnlocked = bestLapMs in 1..targetLapTimeMs
    val apexPredatorProgress = when {
      bestLapMs == 0L -> 0f
      bestLapMs <= targetLapTimeMs -> 1f
      else -> (targetLapTimeMs.toFloat() / bestLapMs).coerceIn(0f, 0.95f)
    }
    val apexPredator = DriverBadge(
      id = "apex_predator",
      title = "Apex Predator",
      description = "Clock a blazing qualifying lap under 1m 20.000s on any premier circuit.",
      category = BadgeCategory.SPEED,
      tier = BadgeTier.LEGENDARY,
      icon = Icons.Default.Bolt,
      isUnlocked = isApexPredatorUnlocked,
      progress = apexPredatorProgress,
      progressText = if (bestLapMs > 0) String.format(Locale.US, "%.3fs / 80.000s", bestLapMs / 1000f) else "0.000s / 80.000s",
      requirementText = "Set a lap time < 1:20.000 (80,000 ms)",
      unlockedDetail = if (isApexPredatorUnlocked && bestLapRecord != null) {
        String.format(Locale.US, "Unlocked on %s • %.3fs (%s)", bestLapRecord.trackName, bestLapRecord.lapTimeMs / 1000f, bestLapRecord.vehicleName)
      } else null,
      proTip = "Attack corner kerbs with Formula Apex Hybrid on low fuel load at Monza."
    )

    // 4. G-FORCE MONSTER (Lateral G >= 3.0G)
    val targetLateralG = 3.0f
    val isGForceMonsterUnlocked = maxLateralG >= targetLateralG
    val gForceProgress = (maxLateralG / targetLateralG).coerceIn(0f, 1f)
    val gForceMonster = DriverBadge(
      id = "g_force_monster",
      title = "G-Force Monster",
      description = "Sustain neck-snapping lateral acceleration exceeding 3.0G through sweeping bends.",
      category = BadgeCategory.AGILITY,
      tier = BadgeTier.GOLD,
      icon = Icons.Default.MilitaryTech,
      isUnlocked = isGForceMonsterUnlocked,
      progress = gForceProgress,
      progressText = String.format(Locale.US, "%.2fG / %.1fG", maxLateralG, targetLateralG),
      requirementText = "Achieve peak lateral G >= 3.0G",
      unlockedDetail = if (isGForceMonsterUnlocked && maxLateralGRecord != null) {
        String.format(Locale.US, "Unlocked on %s • %.2fG (%s)", maxLateralGRecord.trackName, maxLateralG, maxLateralGRecord.vehicleName)
      } else null,
      proTip = "Carry flat-out speed through Pouhon at Spa or 130R at Suzuka with high downforce."
    )

    // 5. ZERO-TO-HUNDRED SPECIALIST (0-100 <= 2.50s)
    val target0to100 = 2.50f
    val is0to100Unlocked = best0to100 in 0.01f..target0to100
    val zeroTo100Progress = when {
      best0to100 <= 0f -> 0f
      best0to100 <= target0to100 -> 1f
      else -> (target0to100 / best0to100).coerceIn(0f, 0.95f)
    }
    val zeroToHundredSpecialist = DriverBadge(
      id = "zero_to_hundred",
      title = "Zero-to-Hundred Specialist",
      description = "Launch off the grid and hit 100 km/h (62 mph) in 2.50 seconds or less.",
      category = BadgeCategory.SPEED,
      tier = BadgeTier.SILVER,
      icon = Icons.Default.Timer,
      isUnlocked = is0to100Unlocked,
      progress = zeroTo100Progress,
      progressText = if (best0to100 > 0) String.format(Locale.US, "%.2fs / %.2fs", best0to100, target0to100) else "0.00s / 2.50s",
      requirementText = "0-100 km/h acceleration <= 2.50 seconds",
      unlockedDetail = if (is0to100Unlocked && best0to100Record != null) {
        String.format(Locale.US, "Unlocked with %s • %.2fs", best0to100Record.vehicleName, best0to100)
      } else null,
      proTip = "Activate launch control and optimize differential lock for instant AWD hookup."
    )

    // 6. ENDURANCE PILOT (Total distance >= 25.0 km)
    val targetDistanceKm = 25.0f
    val targetDistanceMiles = targetDistanceKm * 0.621371f
    val isEnduranceUnlocked = totalDistanceKm >= targetDistanceKm
    val enduranceProgress = (totalDistanceKm / targetDistanceKm).coerceIn(0f, 1f)
    val endurancePilot = DriverBadge(
      id = "endurance_pilot",
      title = "Endurance Pilot",
      description = "Clock over 25 kilometers (15.5 miles) of cumulative track mileage in Room DB.",
      category = BadgeCategory.ENDURANCE,
      tier = BadgeTier.GOLD,
      icon = Icons.Default.Explore,
      isUnlocked = isEnduranceUnlocked,
      progress = enduranceProgress,
      progressText = if (isMph) {
        String.format(Locale.US, "%.1f / %.1f mi", totalDistanceMiles, targetDistanceMiles)
      } else {
        String.format(Locale.US, "%.1f / %.1f km", totalDistanceKm, targetDistanceKm)
      },
      requirementText = "Accumulate >= 25 km (15.5 mi) total logged track distance",
      unlockedDetail = if (isEnduranceUnlocked) {
        String.format(Locale.US, "Unlocked with %.1f km logged across %d sessions", totalDistanceKm, sessionCount)
      } else null,
      proTip = "Keep racing laps or simulate new sessions to build your lifetime telemetry odometer."
    )

    // 7. RAINMASTER (Wet Weather Mastery)
    val isRainmasterUnlocked = wetSessions.isNotEmpty()
    val rainmaster = DriverBadge(
      id = "rainmaster",
      title = "Rainmaster",
      description = "Conquer low-grip conditions by completing a recorded telemetry run in wet weather.",
      category = BadgeCategory.MASTERY,
      tier = BadgeTier.SILVER,
      icon = Icons.Default.WaterDrop,
      isUnlocked = isRainmasterUnlocked,
      progress = if (isRainmasterUnlocked) 1f else 0f,
      progressText = if (isRainmasterUnlocked) "${wetSessions.size} Wet Runs" else "0 / 1 Wet Run",
      requirementText = "Complete and save at least 1 session in Wet Surface / Rain conditions",
      unlockedDetail = if (isRainmasterUnlocked) {
        String.format(Locale.US, "Unlocked on %s (%s)", wetSessions.first().trackName, wetSessions.first().weatherCondition)
      } else null,
      proTip = "Soften anti-roll bars and modulate throttle inputs smoothly on damp asphalt."
    )

    // 8. TRACK CONQUEROR (>= 3 Distinct Circuits)
    val targetTracks = 3
    val isTrackConquerorUnlocked = distinctTracks.size >= targetTracks
    val trackConquerorProgress = (distinctTracks.size.toFloat() / targetTracks).coerceIn(0f, 1f)
    val trackConqueror = DriverBadge(
      id = "track_conqueror",
      title = "Track Conqueror",
      description = "Master the racing line across 3 or more premier international circuits.",
      category = BadgeCategory.MASTERY,
      tier = BadgeTier.PLATINUM,
      icon = Icons.Default.WorkspacePremium,
      isUnlocked = isTrackConquerorUnlocked,
      progress = trackConquerorProgress,
      progressText = "${distinctTracks.size} / $targetTracks Circuits",
      requirementText = "Log telemetry on at least 3 distinct tracks in Room DB",
      unlockedDetail = if (isTrackConquerorUnlocked) {
        "Unlocked across: " + distinctTracks.take(4).joinToString(", ")
      } else null,
      proTip = "Log sessions on Monza GP, Spa-Francorchamps, and Suzuka GP to master diverse geometries."
    )

    // 9. FLEET COMMANDER (>= 2 Distinct Vehicles)
    val targetVehicles = 2
    val isFleetCommanderUnlocked = distinctVehicles.size >= targetVehicles
    val fleetCommanderProgress = (distinctVehicles.size.toFloat() / targetVehicles).coerceIn(0f, 1f)
    val fleetCommander = DriverBadge(
      id = "fleet_commander",
      title = "Fleet Commander",
      description = "Pilot and record telemetry with at least 2 distinct competition vehicles.",
      category = BadgeCategory.MASTERY,
      tier = BadgeTier.BRONZE,
      icon = Icons.Default.DirectionsCar,
      isUnlocked = isFleetCommanderUnlocked,
      progress = fleetCommanderProgress,
      progressText = "${distinctVehicles.size} / $targetVehicles Vehicles",
      requirementText = "Record laps with 2 or more different vehicles",
      unlockedDetail = if (isFleetCommanderUnlocked) {
        "Unlocked with: " + distinctVehicles.take(3).joinToString(", ")
      } else null,
      proTip = "Switch between GT3-R Twin-Turbo and Formula Apex Hybrid in your garage."
    )

    // 10. TELEMETRY VETERAN (>= 3 Sessions)
    val targetVeteranSessions = 3
    val isVeteranUnlocked = sessionCount >= targetVeteranSessions
    val veteranProgress = (sessionCount.toFloat() / targetVeteranSessions).coerceIn(0f, 1f)
    val telemetryVeteran = DriverBadge(
      id = "telemetry_veteran",
      title = "Telemetry Veteran",
      description = "Establish a proven local database record with 3 or more logged sessions.",
      category = BadgeCategory.CONSISTENCY,
      tier = BadgeTier.BRONZE,
      icon = Icons.Default.CheckCircle,
      isUnlocked = isVeteranUnlocked,
      progress = veteranProgress,
      progressText = "$sessionCount / $targetVeteranSessions Runs",
      requirementText = "Save at least 3 sessions in local Room DB",
      unlockedDetail = if (isVeteranUnlocked) {
        "Unlocked • $sessionCount sessions stored in Room database"
      } else null,
      proTip = "Regular telemetry logging helps AI Race Engineer pinpoint throttle and braking gains."
    )

    // 11. SUPER-G ACCELERATOR (Peak Acceleration >= 2.3G)
    val targetAccelG = 2.30f
    val isSuperGUnlocked = maxAccelG >= targetAccelG
    val superGProgress = (maxAccelG / targetAccelG).coerceIn(0f, 1f)
    val superGAccelerator = DriverBadge(
      id = "super_g_accelerator",
      title = "Super-G Accelerator",
      description = "Unleash explosive longitudinal acceleration of 2.30G or higher.",
      category = BadgeCategory.AGILITY,
      tier = BadgeTier.LEGENDARY,
      icon = Icons.Default.Bolt,
      isUnlocked = isSuperGUnlocked,
      progress = superGProgress,
      progressText = String.format(Locale.US, "%.2fG / %.2fG", maxAccelG, targetAccelG),
      requirementText = "Achieve peak acceleration >= 2.30G",
      unlockedDetail = if (isSuperGUnlocked && maxAccelRecord != null) {
        String.format(Locale.US, "Unlocked on %s • %.2fG (%s)", maxAccelRecord.trackName, maxAccelG, maxAccelRecord.vehicleName)
      } else null,
      proTip = "Deploy full ERS battery boost on corner exits in Formula Apex Hybrid."
    )

    // 12. HIGH-SPEED CRUISER (Avg speed >= 260 km/h)
    val targetAvgSpeedKmh = 260.0f
    val targetAvgSpeedConverted = if (isMph) targetAvgSpeedKmh * 0.621371f else targetAvgSpeedKmh
    val maxAvgSpeedConverted = if (isMph) maxAvgSpeedKmh * 0.621371f else maxAvgSpeedKmh
    val isCruiserUnlocked = maxAvgSpeedKmh >= targetAvgSpeedKmh
    val cruiserProgress = (maxAvgSpeedKmh / targetAvgSpeedKmh).coerceIn(0f, 1f)
    val highSpeedCruiser = DriverBadge(
      id = "high_speed_cruiser",
      title = "High-Speed Cruiser",
      description = "Sustain a blistering average lap speed of 260 km/h (161.5 mph) or higher.",
      category = BadgeCategory.SPEED,
      tier = BadgeTier.SILVER,
      icon = Icons.Default.Speed,
      isUnlocked = isCruiserUnlocked,
      progress = cruiserProgress,
      progressText = String.format(Locale.US, "%.1f / %.1f %s", maxAvgSpeedConverted, targetAvgSpeedConverted, speedUnit),
      requirementText = "Average lap speed >= 260 km/h (161.5 mph)",
      unlockedDetail = if (isCruiserUnlocked && maxAvgSpeedRecord != null) {
        String.format(Locale.US, "Unlocked on %s • %.1f %s (%s)", maxAvgSpeedRecord.trackName, maxAvgSpeedConverted, speedUnit, maxAvgSpeedRecord.vehicleName)
      } else null,
      proTip = "Minimize steering angles through Parabolica and Curva Grande at Monza."
    )

    return listOf(
      speedDemon,
      consistencyKing,
      apexPredator,
      gForceMonster,
      zeroToHundredSpecialist,
      endurancePilot,
      rainmaster,
      trackConqueror,
      fleetCommander,
      telemetryVeteran,
      superGAccelerator,
      highSpeedCruiser
    )
  }
}
