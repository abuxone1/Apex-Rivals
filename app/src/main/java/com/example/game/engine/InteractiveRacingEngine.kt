package com.example.game.engine

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.PerformanceMetrics
import com.example.data.local.RaceResult
import com.example.game.model.AirRival
import com.example.game.model.CameraPerspective
import com.example.game.model.GameMode
import com.example.game.model.RaceFinishSummary
import com.example.game.model.RaceState
import com.example.game.model.RacingGameHudState
import com.example.game.model.RoadSegment
import com.example.game.sound.GameSoundManager
import com.example.model.BadgeEvaluator
import com.example.model.Track
import com.example.model.Vehicle
import com.example.model.VehicleType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class InteractiveRacingEngine(
  private val context: Context,
  private var vehicle: Vehicle,
  private var track: Track
) {

  private val soundManager = GameSoundManager.getInstance()
  private val coroutineScope = CoroutineScope(Dispatchers.Default)
  private var gameLoopJob: Job? = null
  private var countdownJob: Job? = null

  // Game Configuration
  var gameMode: GameMode = GameMode.GRAND_PRIX
  var cameraPerspective: CameraPerspective = CameraPerspective.CHASE_CAM
  var totalLaps: Int = 2

  // Player State
  var playerLaneX: Float = 0f // -1f (left track edge) to +1f (right edge)
  var playerTrackDistanceMeters: Float = 0f
  var speedKmh: Float = 0f
  var rpm: Int = vehicle.idleRpm
  var gear: Int = 1
  var isAutoGears: Boolean = true

  var throttleInput: Float = 0f
  var brakeInput: Float = 0f
  var steerInput: Float = 0f

  // Nitro / ERS Boost
  var nitroCharge: Float = 1.0f // 0..1f
  var isNitroActive: Boolean = false
  var nitroDurationRemainingSec: Float = 0f

  // DRS Flap
  var isDrsActive: Boolean = false

  // Drift & Apex Score
  var driftScore: Int = 0
  var currentDriftAngle: Float = 0f

  // Lap Timers
  var currentLap: Int = 1
  var currentLapTimeMs: Long = 0L
  var bestLapTimeMs: Long? = null
  var lastLapTimeMs: Long? = null
  var sector1TimeMs: Long? = null
  var sector2TimeMs: Long? = null
  var sector3TimeMs: Long? = null
  var currentSector: Int = 1

  var topSpeedReachedKmh: Float = 0f
  var overtakesCount: Int = 0
  var peakLateralG: Float = 0f

  // Rivals
  private val rivals = mutableListOf<AirRival>()

  // Road geometry segments
  private val roadSegments = generateCircuitRoadSegments(track.totalLengthMeters)

  // State flows
  private val _raceState = MutableStateFlow(RaceState.PRE_GRID)
  val raceState: StateFlow<RaceState> = _raceState.asStateFlow()

  private val _hudState = MutableStateFlow(RacingGameHudState())
  val hudState: StateFlow<RacingGameHudState> = _hudState.asStateFlow()

  private val _finishSummary = MutableStateFlow<RaceFinishSummary?>(null)
  val finishSummary: StateFlow<RaceFinishSummary?> = _finishSummary.asStateFlow()

  init {
    initRivals()
  }

  fun setVehicleAndTrack(newVehicle: Vehicle, newTrack: Track) {
    vehicle = newVehicle
    track = newTrack
    resetRace()
  }

  private fun initRivals() {
    rivals.clear()
    val rivalTemplates = listOf(
      Triple("M. Verstappen", "Red Bull Apex", 0xFF0D47A1),
      Triple("L. Hamilton", "Mercedes Silver Arrow", 0xFF00E5FF),
      Triple("C. Leclerc", "Scuderia Corse Red", 0xFFD50000),
      Triple("L. Norris", "McLaren Papaya", 0xFFFF6D00),
      Triple("F. Alonso", "Aston Racing Green", 0xFF004D40),
      Triple("G. Russell", "Silver Lightning", 0xFF78909C),
      Triple("O. Piastri", "Papaya Stealth", 0xFFFF9100)
    )

    rivalTemplates.forEachIndexed { index, (name, team, color) ->
      // Position rivals along the starting grid ahead or behind player
      val gridDistance = (rivalTemplates.size - index) * 14f // 14m apart
      val lane = if (index % 2 == 0) -0.45f else 0.45f
      rivals.add(
        AirRival(
          id = "rival_$index",
          name = name,
          teamName = team,
          carName = "Apex Competitor GT",
          colorHex = color,
          laneOffset = lane,
          distanceMeters = gridDistance,
          speedKmh = 0f,
          currentLap = 1,
          currentRank = index + 1
        )
      )
    }
  }

  fun startGridCountdown() {
    countdownJob?.cancel()
    _raceState.value = RaceState.LIGHTS_COUNTDOWN
    _finishSummary.value = null

    countdownJob = coroutineScope.launch {
      // 5 Red Lights turning ON consecutively
      for (light in 1..5) {
        _hudState.update { it.copy(countdownLights = light) }
        soundManager.playCountdownLightBeep()
        delay(900)
      }

      // Random delay between 0.4s and 1.2s before lights out!
      delay(750)

      // LIGHTS OUT AND AWAY WE GO!
      _hudState.update { it.copy(countdownLights = 6) }
      soundManager.playLightsOutGo()
      _raceState.value = RaceState.RACING

      delay(400)
      _hudState.update { it.copy(countdownLights = 0) }

      startLoop()
    }
  }

  fun resetRace() {
    gameLoopJob?.cancel()
    countdownJob?.cancel()

    playerLaneX = 0f
    playerTrackDistanceMeters = 0f
    speedKmh = 0f
    rpm = vehicle.idleRpm
    gear = 1
    throttleInput = 0f
    brakeInput = 0f
    steerInput = 0f
    nitroCharge = 1.0f
    isNitroActive = false
    nitroDurationRemainingSec = 0f
    isDrsActive = false
    driftScore = 0
    currentDriftAngle = 0f
    currentLap = 1
    currentLapTimeMs = 0L
    bestLapTimeMs = null
    lastLapTimeMs = null
    sector1TimeMs = null
    sector2TimeMs = null
    sector3TimeMs = null
    currentSector = 1
    topSpeedReachedKmh = 0f
    overtakesCount = 0
    peakLateralG = 0f

    initRivals()
    _raceState.value = RaceState.PRE_GRID
    _finishSummary.value = null
    _hudState.value = RacingGameHudState(
      currentRank = rivals.size + 1,
      totalRivals = rivals.size + 1,
      currentLap = 1,
      totalLaps = totalLaps,
      rpm = vehicle.idleRpm,
      maxRpm = vehicle.maxRpm,
      redlineRpm = vehicle.redlineRpm,
      gear = 1,
      isAutoGears = isAutoGears
    )
  }

  fun triggerNitroBoost() {
    if (nitroCharge >= 0.25f && !isNitroActive) {
      isNitroActive = true
      nitroDurationRemainingSec = 3.5f
      soundManager.playNitroBoost()
      triggerAlert("NITRO / ERS BOOST ENGAGED!", 0xFF00E5FF)
    }
  }

  fun toggleDrs() {
    isDrsActive = !isDrsActive
    if (isDrsActive) {
      triggerAlert("DRS WING OPEN (+15 KM/H)", 0xFF00E676)
    }
  }

  fun toggleAutoGears() {
    isAutoGears = !isAutoGears
    _hudState.update { it.copy(isAutoGears = isAutoGears) }
  }

  fun shiftUp(): Boolean {
    if (gear < vehicle.gears) {
      gear++
      rpm = (rpm * 0.72f).toInt().coerceAtLeast(vehicle.idleRpm)
      soundManager.playGearShift()
      return true
    }
    return false
  }

  fun shiftDown(): Boolean {
    if (gear > 1) {
      gear--
      rpm = (rpm * 1.35f).toInt().coerceAtMost(vehicle.maxRpm)
      soundManager.playGearShift()
      return true
    }
    return false
  }

  fun switchCamera(): CameraPerspective {
    cameraPerspective = when (cameraPerspective) {
      CameraPerspective.CHASE_CAM -> CameraPerspective.COCKPIT_CAM
      CameraPerspective.COCKPIT_CAM -> CameraPerspective.HOOD_CAM
      CameraPerspective.HOOD_CAM -> CameraPerspective.CHASE_CAM
    }
    return cameraPerspective
  }

  fun triggerAlert(message: String, colorHex: Long = 0xFF00E5FF) {
    _hudState.update {
      it.copy(activeAlertMessage = message, activeAlertColor = colorHex)
    }
    coroutineScope.launch {
      delay(2200)
      if (_hudState.value.activeAlertMessage == message) {
        _hudState.update { it.copy(activeAlertMessage = null) }
      }
    }
  }

  private fun startLoop() {
    gameLoopJob?.cancel()
    gameLoopJob = coroutineScope.launch {
      var lastTimestamp = System.currentTimeMillis()
      while (isActive && _raceState.value != RaceState.FINISHED_PODIUM && _raceState.value != RaceState.PAUSED) {
        val now = System.currentTimeMillis()
        val dt = ((now - lastTimestamp) / 1000f).coerceIn(0.005f, 0.05f)
        lastTimestamp = now

        updateTick(dt)
        delay(16) // ~60fps
      }
    }
  }

  private fun updateTick(dt: Float) {
    if (_raceState.value != RaceState.RACING && _raceState.value != RaceState.FINAL_LAP) return

    currentLapTimeMs += (dt * 1000).toLong()

    // 1. NITRO BOOST LOGIC
    if (isNitroActive) {
      nitroDurationRemainingSec -= dt
      nitroCharge = (nitroCharge - (dt / 3.5f)).coerceAtLeast(0f)
      if (nitroDurationRemainingSec <= 0f || nitroCharge <= 0f) {
        isNitroActive = false
      }
    } else {
      // Recharges when braking or cruising
      if (brakeInput > 0.2f) {
        nitroCharge = (nitroCharge + dt * 0.15f).coerceAtMost(1f)
      } else {
        nitroCharge = (nitroCharge + dt * 0.04f).coerceAtMost(1f)
      }
    }

    // 2. DRAFTING / SLIPSTREAM DETECTION
    var isDrafting = false
    var slipstreamBonusKmh = 0f
    val nearestAhead = rivals.filter {
      it.distanceMeters > playerTrackDistanceMeters && (it.distanceMeters - playerTrackDistanceMeters) in 2f..24f
    }.minByOrNull { it.distanceMeters - playerTrackDistanceMeters }

    if (nearestAhead != null && abs(nearestAhead.laneOffset - playerLaneX) < 0.35f && speedKmh > 140f) {
      isDrafting = true
      slipstreamBonusKmh = 18f
      if (!_hudState.value.isDrafting) {
        soundManager.playSlipstreamDraft()
        triggerAlert("SLIPSTREAM DRAFTING • +18 KM/H", 0xFF00E5FF)
      }
    }

    // 3. STEERING & LATERAL G
    playerLaneX = (playerLaneX + steerInput * dt * 2.2f).coerceIn(-0.95f, 0.95f)

    // Current road segment
    val currentSegmentIdx = ((playerTrackDistanceMeters / 60f).toInt()) % roadSegments.size
    val currentSegment = roadSegments[currentSegmentIdx]
    val curvePull = currentSegment.curvature * (speedKmh / 200f) * dt
    playerLaneX = (playerLaneX - curvePull).coerceIn(-0.98f, 0.98f)

    val lateralG = abs(steerInput * (speedKmh / 160f) * vehicle.peakCorneringG).coerceAtMost(4.5f)
    if (lateralG > peakLateralG) peakLateralG = lateralG

    // Drift Detection
    if (lateralG > 2.0f && speedKmh > 100f && abs(steerInput) > 0.6f) {
      currentDriftAngle = abs(steerInput) * 35f
      val pointsGained = (lateralG * speedKmh * 0.15f * dt).toInt()
      driftScore += pointsGained
    } else {
      currentDriftAngle = 0f
    }

    // 4. ACCELERATION, BRAKING & TOP SPEED
    val baseTopSpeed = vehicle.topSpeedKmh.toFloat()
    val maxEffectiveSpeed = baseTopSpeed +
      (if (isDrsActive) 15f else 0f) +
      (if (isNitroActive) 35f else 0f) +
      slipstreamBonusKmh

    val nitroMultiplier = if (isNitroActive) 1.55f else 1.0f

    if (throttleInput > 0.05f) {
      val accelPower = (vehicle.horsepower / 220f) * (1f - (speedKmh / maxEffectiveSpeed).coerceIn(0f, 1f)) * nitroMultiplier
      speedKmh += (accelPower * 48f * throttleInput * dt)
    } else {
      // Natural aerodynamic drag & rolling resistance
      speedKmh -= (18f * dt + (speedKmh * speedKmh / 25000f) * dt)
    }

    if (brakeInput > 0.05f) {
      speedKmh -= (110f * brakeInput * dt)
    }

    speedKmh = speedKmh.coerceIn(0f, maxEffectiveSpeed)
    if (speedKmh > topSpeedReachedKmh) topSpeedReachedKmh = speedKmh

    // 5. GEARS & RPM
    val gearRatioSpeed = maxEffectiveSpeed / vehicle.gears
    if (isAutoGears) {
      val targetGear = ((speedKmh / gearRatioSpeed).toInt() + 1).coerceIn(1, vehicle.gears)
      if (targetGear != gear) {
        gear = targetGear
        soundManager.playGearShift()
      }
    }

    val speedInCurrentGear = speedKmh - (gear - 1) * gearRatioSpeed
    val rpmFraction = (speedInCurrentGear / gearRatioSpeed).coerceIn(0f, 1.15f)
    rpm = (vehicle.idleRpm + rpmFraction * (vehicle.maxRpm - vehicle.idleRpm)).toInt().coerceIn(vehicle.idleRpm, vehicle.maxRpm)

    // 6. DISTANCE & TRACK PROGRESS
    val distanceStepMeters = (speedKmh / 3.6f) * dt
    playerTrackDistanceMeters += distanceStepMeters

    // Lap Completion Check
    val totalTrackMeters = track.totalLengthMeters.toFloat()
    if (playerTrackDistanceMeters >= totalTrackMeters) {
      playerTrackDistanceMeters -= totalTrackMeters
      onLapCompleted()
    }

    // Sector Times
    val progress = playerTrackDistanceMeters / totalTrackMeters
    if (progress > 0.33f && sector1TimeMs == null) {
      sector1TimeMs = currentLapTimeMs
      soundManager.playSectorRecord()
      triggerAlert("SECTOR 1 • ${formatTime(sector1TimeMs ?: 0)}", 0xFF9C27B0)
    } else if (progress > 0.66f && sector2TimeMs == null) {
      sector2TimeMs = currentLapTimeMs - (sector1TimeMs ?: 0L)
      soundManager.playSectorRecord()
      triggerAlert("SECTOR 2 • ${formatTime(sector2TimeMs ?: 0)}", 0xFF9C27B0)
    }

    // 7. UPDATE AI RIVALS
    updateRivals(dt, totalTrackMeters)

    // 8. CALCULATE RANK & GAPS
    val allCars = (rivals.map { it.distanceMeters + (it.currentLap - 1) * totalTrackMeters } +
      listOf(playerTrackDistanceMeters + (currentLap - 1) * totalTrackMeters)).sortedDescending()

    val playerTotalDistance = playerTrackDistanceMeters + (currentLap - 1) * totalTrackMeters
    val playerRank = allCars.indexOf(playerTotalDistance) + 1

    // Check overtakes
    rivals.forEach { rival ->
      val rivalTotalDist = rival.distanceMeters + (rival.currentLap - 1) * totalTrackMeters
      if (!rival.isOvertaken && playerTotalDistance > rivalTotalDist) {
        rival.isOvertaken = true
        overtakesCount++
        soundManager.playOvertakeChime()
        triggerAlert("OVERTAKE! PASSED ${rival.name.uppercase()}", 0xFF00E676)
      }
    }

    // Interval ahead / behind
    val carAheadDist = allCars.filter { it > playerTotalDistance }.minOrNull()
    val intervalAhead = carAheadDist?.let { ((it - playerTotalDistance) / (speedKmh.coerceAtLeast(60f) / 3.6f)) }

    val carBehindDist = allCars.filter { it < playerTotalDistance }.maxOrNull()
    val intervalBehind = carBehindDist?.let { ((playerTotalDistance - it) / (speedKmh.coerceAtLeast(60f) / 3.6f)) }

    val rivalAhead = rivals.find {
      val dist = it.distanceMeters + (it.currentLap - 1) * totalTrackMeters
      dist > playerTotalDistance && (dist - playerTotalDistance) < 150f
    }

    _hudState.update {
      it.copy(
        currentRank = playerRank,
        totalRivals = rivals.size + 1,
        currentLap = currentLap,
        totalLaps = totalLaps,
        intervalAheadSeconds = intervalAhead,
        intervalBehindSeconds = intervalBehind,
        rivalAheadName = rivalAhead?.name,
        speedKmh = speedKmh,
        rpm = rpm,
        gear = gear,
        nitroChargePercent = nitroCharge,
        isNitroActive = isNitroActive,
        isDrsAvailable = currentSegment.isSpeedTrap || currentSegment.curvature == 0f,
        isDrsActive = isDrsActive,
        isDrafting = isDrafting,
        slipstreamBonusKmh = slipstreamBonusKmh,
        driftPoints = driftScore,
        currentLapTimeMs = currentLapTimeMs,
        bestLapTimeMs = bestLapTimeMs,
        lastLapTimeMs = lastLapTimeMs,
        sector1TimeMs = sector1TimeMs,
        sector2TimeMs = sector2TimeMs
      )
    }
  }

  private fun updateRivals(dt: Float, totalTrackMeters: Float) {
    rivals.forEachIndexed { i, rival ->
      // Rival speed behavior based on difficulty & curve
      val baseSpeed = vehicle.topSpeedKmh * (0.91f + (i * 0.012f))
      val currentSegmentIdx = ((rival.distanceMeters / 60f).toInt()) % roadSegments.size
      val seg = roadSegments[currentSegmentIdx]

      val targetSpeed = if (seg.isBrakingZone) baseSpeed * 0.58f else baseSpeed
      rival.speedKmh += ((targetSpeed - rival.speedKmh) * 1.5f * dt)

      rival.distanceMeters += (rival.speedKmh / 3.6f) * dt

      if (rival.distanceMeters >= totalTrackMeters) {
        rival.distanceMeters -= totalTrackMeters
        rival.currentLap++
      }

      // Smooth AI weaving/defending
      rival.laneOffset += sin((rival.distanceMeters / 50f) + i) * 0.08f * dt
      rival.laneOffset = rival.laneOffset.coerceIn(-0.8f, 0.8f)
    }
  }

  private fun onLapCompleted() {
    lastLapTimeMs = currentLapTimeMs
    val isBest = bestLapTimeMs == null || currentLapTimeMs < bestLapTimeMs!!
    if (isBest) {
      bestLapTimeMs = currentLapTimeMs
      soundManager.playCheckeredFlagVictory()
      triggerAlert("NEW PERSONAL BEST LAP! • ${formatTime(currentLapTimeMs)}", 0xFFFFD700)
    } else {
      triggerAlert("LAP $currentLap COMPLETE • ${formatTime(currentLapTimeMs)}", 0xFF00E5FF)
    }

    currentLapTimeMs = 0L
    sector1TimeMs = null
    sector2TimeMs = null

    if (currentLap < totalLaps) {
      currentLap++
      if (currentLap == totalLaps) {
        _raceState.value = RaceState.FINAL_LAP
        triggerAlert("FINAL LAP! PUSH TO THE APEX!", 0xFFFF1744)
      }
    } else {
      // Race Complete!
      finishRace()
    }
  }

  private fun finishRace() {
    gameLoopJob?.cancel()
    _raceState.value = RaceState.FINISHED_PODIUM
    soundManager.playCheckeredFlagVictory()

    val totalTrackMeters = track.totalLengthMeters.toFloat()
    val playerTotalDistance = playerTrackDistanceMeters + (currentLap - 1) * totalTrackMeters
    val allCars = (rivals.map { it.distanceMeters + (it.currentLap - 1) * totalTrackMeters } + listOf(playerTotalDistance)).sortedDescending()
    val finalRank = allCars.indexOf(playerTotalDistance) + 1

    val bestLap = bestLapTimeMs ?: lastLapTimeMs ?: 78000L
    val xp = (1000 / finalRank) + (driftScore / 10) + (overtakesCount * 150)
    val credits = (5000 / finalRank) + (driftScore / 5)

    // Save to Room DB asynchronously
    coroutineScope.launch(Dispatchers.IO) {
      try {
        val db = AppDatabase.getDatabase(context)
        val metricsDao = db.performanceMetricsDao()
        val raceResultDao = db.raceResultDao()

        val raceId = "gp_${System.currentTimeMillis()}"
        val metricsRecord = PerformanceMetrics(
          raceId = raceId,
          driverName = "Apex Driver",
          trackName = track.name,
          vehicleName = vehicle.name,
          lapTimeMs = bestLap,
          topSpeedKmh = topSpeedReachedKmh,
          maxLateralG = peakLateralG,
          weatherCondition = "Dry Asphalt",
          trackDistanceMeters = track.totalLengthMeters,
          zeroToHundredKmhSeconds = vehicle.acceleration0to100
        )
        metricsDao.insert(metricsRecord)

        val raceResult = RaceResult(
          trackName = track.name,
          vehicleName = vehicle.name,
          lapTimeMs = bestLap,
          topSpeedKmh = topSpeedReachedKmh,
          timestamp = System.currentTimeMillis()
        )
        raceResultDao.insertResult(raceResult)

        // Evaluate Badges
        val allMetrics = metricsDao.getAllList()
        val evaluatedBadges = BadgeEvaluator.evaluateBadges(allMetrics)
        val newlyUnlocked = evaluatedBadges.filter { it.isUnlocked }.map { it.title }

        _finishSummary.value = RaceFinishSummary(
          finishingPosition = finalRank,
          totalRacers = rivals.size + 1,
          totalTimeMs = (lastLapTimeMs ?: bestLap) * totalLaps,
          bestLapMs = bestLap,
          topSpeedKmh = topSpeedReachedKmh,
          overtakesCount = overtakesCount,
          driftScore = driftScore,
          xpEarned = xp,
          creditsEarned = credits,
          trackName = track.name,
          vehicleName = vehicle.name,
          isNewPersonalBest = true,
          newlyUnlockedBadges = newlyUnlocked
        )
      } catch (e: Exception) {
        // Fallback summary if DB save has transient error
        _finishSummary.value = RaceFinishSummary(
          finishingPosition = finalRank,
          totalRacers = rivals.size + 1,
          totalTimeMs = (lastLapTimeMs ?: bestLap) * totalLaps,
          bestLapMs = bestLap,
          topSpeedKmh = topSpeedReachedKmh,
          overtakesCount = overtakesCount,
          driftScore = driftScore,
          xpEarned = xp,
          creditsEarned = credits,
          trackName = track.name,
          vehicleName = vehicle.name,
          isNewPersonalBest = true
        )
      }
    }
  }

  fun getRivalsList(): List<AirRival> = rivals.toList()

  fun getRoadSegments(): List<RoadSegment> = roadSegments

  private fun formatTime(millis: Long): String {
    val mins = (millis / 60000)
    val secs = (millis % 60000) / 1000
    val ms = millis % 1000
    return String.format(Locale.US, "%d:%02d.%03d", mins, secs, ms)
  }

  private companion object {
    fun generateCircuitRoadSegments(totalDistanceMeters: Int): List<RoadSegment> {
      val segmentsCount = (totalDistanceMeters / 60).coerceAtLeast(40)
      return (0 until segmentsCount).map { i ->
        val progress = i.toFloat() / segmentsCount
        val curvature = when {
          progress in 0.15f..0.25f -> -0.75f // Curva Grande left
          progress in 0.35f..0.45f -> 0.85f  // Chicane right
          progress in 0.55f..0.65f -> -0.90f // Lesmo hairpin
          progress in 0.78f..0.92f -> 0.65f  // Parabolica sweeper
          else -> 0.0f // Long high-speed straightaway
        }

        val isBraking = progress in 0.32f..0.35f || progress in 0.52f..0.55f || progress in 0.75f..0.78f
        val isSpeedTrap = progress in 0.08f..0.12f

        val board = when {
          progress in 0.31f..0.32f -> 200
          progress in 0.33f..0.34f -> 100
          progress in 0.51f..0.52f -> 150
          progress in 0.74f..0.75f -> 100
          else -> null
        }

        RoadSegment(
          segmentIndex = i,
          curvature = curvature,
          elevation = sin(progress * 6.28f * 3f) * 0.3f,
          isBrakingZone = isBraking,
          isSpeedTrap = isSpeedTrap,
          hasDistanceBoard = board
        )
      }
    }
  }
}
