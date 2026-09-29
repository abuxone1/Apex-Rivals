package com.example.engine

import com.example.model.LiveRaceMetrics
import com.example.model.TelemetrySnapshot
import com.example.model.Track
import com.example.model.Vehicle
import com.example.model.VehicleType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class RacingPhysicsEngine(
  private var vehicle: Vehicle,
  private var track: Track
) {

  private var speedKmh = 0f
  private var rpm = vehicle.idleRpm
  private var gear = 1
  private var trackProgress = 0f // 0..1
  private var lapDistanceMeters = 0f
  private var currentLap = 1
  private var currentLapTimeMs = 0L
  private var bestLapTimeMs: Long? = null
  private var lastLapTimeMs: Long? = null
  private var peakLateralG = 0f
  private var topSpeedKmh = 0f

  private var sector1TimeMs: Long? = null
  private var sector2TimeMs: Long? = null
  private var sector3TimeMs: Long? = null
  private var currentSector = 1

  private var tireTempFL = 85f
  private var tireTempFR = 85f
  private var tireTempRL = 83f
  private var tireTempRR = 83f

  private var isDrsOrTuckIn = false
  private var isAutoGears = true

  // Current recorded telemetry frames for active lap
  private val currentLapFrames = mutableListOf<TelemetrySnapshot>()
  private var ghostFrames: List<TelemetrySnapshot>? = null

  fun setVehicleAndTrack(newVehicle: Vehicle, newTrack: Track) {
    vehicle = newVehicle
    track = newTrack
    resetSession()
  }

  fun setGhost(frames: List<TelemetrySnapshot>?) {
    ghostFrames = frames
  }

  fun toggleDrsOrTuckIn() {
    isDrsOrTuckIn = !isDrsOrTuckIn
  }

  fun toggleAutoGears() {
    isAutoGears = !isAutoGears
  }

  fun shiftUp(): Boolean {
    if (gear < vehicle.gears) {
      gear++
      rpm = (rpm * 0.72f).toInt().coerceAtLeast(vehicle.idleRpm)
      return true
    }
    return false
  }

  fun shiftDown(): Boolean {
    if (gear > 1) {
      gear--
      rpm = (rpm * 1.35f).toInt().coerceAtMost(vehicle.maxRpm)
      return true
    }
    return false
  }

  fun resetSession() {
    speedKmh = 0f
    rpm = vehicle.idleRpm
    gear = 1
    trackProgress = 0f
    lapDistanceMeters = 0f
    currentLap = 1
    currentLapTimeMs = 0L
    peakLateralG = 0f
    topSpeedKmh = 0f
    sector1TimeMs = null
    sector2TimeMs = null
    sector3TimeMs = null
    currentSector = 1
    tireTempFL = 85f
    tireTempFR = 85f
    tireTempRL = 83f
    tireTempRR = 83f
    currentLapFrames.clear()
  }

  /**
   * Updates physics tick
   * @param dtSeconds time delta in seconds (e.g. 0.016 for 60fps)
   * @param throttle 0..1f
   * @param brake 0..1f
   * @param steer -1..+1f
   */
  fun update(
    dtSeconds: Float,
    throttle: Float,
    brake: Float,
    steer: Float
  ): LiveRaceMetrics {
    currentLapTimeMs += (dtSeconds * 1000).toLong()

    val isCar = vehicle.type == VehicleType.CAR

    // Gear ratios speed limit approximation
    val gearMaxSpeeds = when (vehicle.gears) {
      8 -> floatArrayOf(0f, 95f, 150f, 205f, 255f, 300f, 335f, 360f, 380f)
      7 -> floatArrayOf(0f, 90f, 145f, 200f, 250f, 295f, 335f, 365f)
      else -> floatArrayOf(0f, 105f, 165f, 220f, 275f, 315f, 345f)
    }

    val currentGearMaxSpeed = gearMaxSpeeds.getOrElse(gear) { 320f }

    // Auto transmission handling if enabled
    if (isAutoGears) {
      if (rpm > vehicle.redlineRpm * 0.96f && gear < vehicle.gears) {
        shiftUp()
      } else if (rpm < vehicle.idleRpm * 1.5f && gear > 1 && speedKmh < currentGearMaxSpeed * 0.45f) {
        shiftDown()
      }
    }

    // Power, acceleration and drag calculation
    val drsBonus = if (isDrsOrTuckIn) 1.08f else 1.0f
    val powerKw = (vehicle.horsepower * 0.7457f) * drsBonus
    val aeroDragCoeff = if (isCar) 0.00035f else 0.00028f
    val dragForce = speedKmh * speedKmh * aeroDragCoeff

    // Net acceleration force calibrated to vehicle 0-100 km/h spec
    val driveForce = if (throttle > 0.05f) {
      val targetLaunchAccel = (27.78f / vehicle.acceleration0to100) * 1.2f
      val speedFalloff = (1.0f - (speedKmh / vehicle.topSpeedKmh) * 0.65f).coerceIn(0.15f, 1.0f)
      val rpmRatio = (rpm.toFloat() / vehicle.maxRpm).coerceIn(0.15f, 1.0f)
      val torqueCurve = sin((rpmRatio * 0.8f + 0.2f) * (Math.PI.toFloat() / 2f)).coerceIn(0.5f, 1.0f)
      (targetLaunchAccel * torqueCurve * speedFalloff * throttle * drsBonus).coerceAtLeast(0f)
    } else {
      0f
    }

    val brakeForce = brake * (if (isCar) 65f else 55f) // deceleration m/s^2 equivalent
    val rollingResistance = 1.2f

    val netAccelMps2 = (driveForce - (dragForce + rollingResistance) - brakeForce)
    val deltaSpeedKmh = netAccelMps2 * dtSeconds * 3.6f

    speedKmh = (speedKmh + deltaSpeedKmh).coerceIn(0f, vehicle.topSpeedKmh * (if (isDrsOrTuckIn) 1.05f else 1.0f))

    if (speedKmh > topSpeedKmh) {
      topSpeedKmh = speedKmh
    }

    // Engine RPM calculation with rev limiter bounce
    val calculatedRpm = if (gear in 1..vehicle.gears) {
      val gearRatioSpeed = (speedKmh / currentGearMaxSpeed).coerceIn(0f, 1.1f)
      (vehicle.idleRpm + gearRatioSpeed * (vehicle.redlineRpm - vehicle.idleRpm)).toInt()
    } else {
      vehicle.idleRpm
    }

    rpm = if (calculatedRpm >= vehicle.maxRpm) {
      // Rev limiter bounce
      if ((currentLapTimeMs / 60) % 2 == 0L) vehicle.maxRpm else vehicle.redlineRpm - 200
    } else {
      calculatedRpm.coerceIn(vehicle.idleRpm, vehicle.maxRpm)
    }

    // Lateral G and Lean Angle
    val steerAngleDeg = steer * 35f // -35..+35 degrees
    val absSteer = abs(steer)
    val speedRatio = (speedKmh / 200f).coerceIn(0.1f, 1.8f)
    val computedLateralG = (absSteer * speedRatio * 1.8f * (vehicle.peakCorneringG / 2.2f))
      .coerceIn(0f, vehicle.peakCorneringG * 1.15f)

    if (computedLateralG > peakLateralG) {
      peakLateralG = computedLateralG
    }

    val computedLongitudinalG = (netAccelMps2 / 9.81f).coerceIn(-2.5f, 1.8f)

    // Motorbike specific Lean Angle
    val leanAngleDeg = if (!isCar) {
      (steer * vehicle.maxLeanAngleDeg * (speedKmh / 80f).coerceIn(0.2f, 1.0f))
        .coerceIn(-vehicle.maxLeanAngleDeg, vehicle.maxLeanAngleDeg)
    } else {
      0f
    }

    val isDriftingOrKneeDown = if (isCar) {
      absSteer > 0.65f && speedKmh > 75f
    } else {
      abs(leanAngleDeg) > vehicle.maxLeanAngleDeg * 0.85f
    }

    // Tire temperatures based on load & friction
    val heatDelta = (computedLateralG * 0.4f + brake * 0.6f) * dtSeconds
    val coolingDelta = 0.15f * dtSeconds
    tireTempFL = (tireTempFL + heatDelta - coolingDelta).coerceIn(75f, 120f)
    tireTempFR = (tireTempFR + heatDelta - coolingDelta).coerceIn(75f, 120f)
    tireTempRL = (tireTempRL + (throttle * 0.3f) * dtSeconds - coolingDelta).coerceIn(75f, 120f)
    tireTempRR = (tireTempRR + (throttle * 0.3f) * dtSeconds - coolingDelta).coerceIn(75f, 120f)

    // Track progression
    val distanceStepMeters = (speedKmh / 3.6f) * dtSeconds
    lapDistanceMeters += distanceStepMeters
    trackProgress = (lapDistanceMeters / track.totalLengthMeters)

    // Sector calculation
    val oldSector = currentSector
    currentSector = when {
      trackProgress < track.sector1EndProgress -> 1
      trackProgress < track.sector2EndProgress -> 2
      else -> 3
    }

    if (oldSector == 1 && currentSector == 2 && sector1TimeMs == null) {
      sector1TimeMs = currentLapTimeMs
    } else if (oldSector == 2 && currentSector == 3 && sector2TimeMs == null) {
      sector2TimeMs = currentLapTimeMs - (sector1TimeMs ?: 0L)
    }

    // Lap Completion
    if (trackProgress >= 1.0f) {
      sector3TimeMs = currentLapTimeMs - (sector1TimeMs ?: 0L) - (sector2TimeMs ?: 0L)
      lastLapTimeMs = currentLapTimeMs
      if (bestLapTimeMs == null || currentLapTimeMs < bestLapTimeMs!!) {
        bestLapTimeMs = currentLapTimeMs
      }

      // Reset for next lap
      trackProgress = 0f
      lapDistanceMeters = 0f
      currentLap++
      currentLapTimeMs = 0L
      sector1TimeMs = null
      sector2TimeMs = null
      sector3TimeMs = null
      currentSector = 1
      currentLapFrames.clear()
    }

    // Ghost Delta calculation
    var deltaVsGhostMs = 0L
    ghostFrames?.let { ghost ->
      val ghostIndex = ((ghost.size - 1) * trackProgress).toInt().coerceIn(0, ghost.size - 1)
      val ghostSnapshot = ghost[ghostIndex]
      deltaVsGhostMs = currentLapTimeMs - ghostSnapshot.lapTimeMs
    } ?: run {
      // Benchmark delta against track reference
      val expectedTimeMs = (track.referenceLapTimeMs * trackProgress).toLong()
      deltaVsGhostMs = currentLapTimeMs - expectedTimeMs
    }

    // Record snapshot every ~50ms
    if (currentLapFrames.isEmpty() || (currentLapTimeMs - currentLapFrames.last().timestampMs) >= 50L) {
      currentLapFrames.add(
        TelemetrySnapshot(
          timestampMs = currentLapTimeMs,
          speedKmh = speedKmh,
          rpm = rpm,
          gear = gear,
          throttle = throttle,
          brake = brake,
          steerAngleDeg = steerAngleDeg,
          leanAngleDeg = leanAngleDeg,
          lateralG = computedLateralG,
          longitudinalG = computedLongitudinalG,
          trackProgress = trackProgress,
          lapTimeMs = currentLapTimeMs,
          sector = currentSector,
          deltaVsGhostMs = deltaVsGhostMs,
          tireTempC = (tireTempFL + tireTempFR) / 2f
        )
      )
    }

    return LiveRaceMetrics(
      speedKmh = speedKmh,
      speedMph = speedKmh * 0.621371f,
      rpm = rpm,
      maxRpm = vehicle.maxRpm,
      redlineRpm = vehicle.redlineRpm,
      gear = gear,
      throttle = throttle,
      brake = brake,
      steerAngleDeg = steerAngleDeg,
      leanAngleDeg = leanAngleDeg,
      lateralG = computedLateralG,
      longitudinalG = computedLongitudinalG,
      peakLateralG = peakLateralG,
      topSpeedKmh = topSpeedKmh,
      trackProgress = trackProgress,
      lapDistanceMeters = lapDistanceMeters,
      currentLap = currentLap,
      currentLapTimeMs = currentLapTimeMs,
      bestLapTimeMs = bestLapTimeMs,
      lastLapTimeMs = lastLapTimeMs,
      deltaVsGhostMs = deltaVsGhostMs,
      currentSector = currentSector,
      sector1TimeMs = sector1TimeMs,
      sector2TimeMs = sector2TimeMs,
      sector3TimeMs = sector3TimeMs,
      isDrsOrTuckInActive = isDrsOrTuckIn,
      tireTempFL = tireTempFL,
      tireTempFR = tireTempFR,
      tireTempRL = tireTempRL,
      tireTempRR = tireTempRR,
      isDriftingOrKneeDown = isDriftingOrKneeDown,
      activeVehicle = vehicle,
      activeTrack = track
    )
  }

  fun getRecordedLapFrames(): List<TelemetrySnapshot> = currentLapFrames.toList()

  fun getMetrics(): LiveRaceMetrics {
    return LiveRaceMetrics(
      speedKmh = speedKmh,
      speedMph = speedKmh * 0.621371f,
      rpm = rpm,
      maxRpm = vehicle.maxRpm,
      redlineRpm = vehicle.redlineRpm,
      gear = gear,
      throttle = 0f,
      brake = 0f,
      steerAngleDeg = 0f,
      leanAngleDeg = 0f,
      lateralG = 0f,
      longitudinalG = 0f,
      peakLateralG = peakLateralG,
      topSpeedKmh = topSpeedKmh,
      trackProgress = trackProgress,
      lapDistanceMeters = lapDistanceMeters,
      currentLap = currentLap,
      currentLapTimeMs = currentLapTimeMs,
      bestLapTimeMs = bestLapTimeMs,
      lastLapTimeMs = lastLapTimeMs,
      deltaVsGhostMs = 0L,
      currentSector = currentSector,
      sector1TimeMs = sector1TimeMs,
      sector2TimeMs = sector2TimeMs,
      sector3TimeMs = sector3TimeMs,
      isDrsOrTuckInActive = isDrsOrTuckIn,
      tireTempFL = tireTempFL,
      tireTempFR = tireTempFR,
      tireTempRL = tireTempRL,
      tireTempRR = tireTempRR,
      isDriftingOrKneeDown = false,
      activeVehicle = vehicle,
      activeTrack = track
    )
  }
}
