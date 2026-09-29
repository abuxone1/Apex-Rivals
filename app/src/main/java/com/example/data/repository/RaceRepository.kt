package com.example.data.repository

import com.example.data.local.RaceDao
import com.example.data.local.RaceResult
import com.example.data.local.RaceResultDao
import com.example.data.local.RaceSessionEntity
import com.example.data.local.TelemetryConverters
import com.example.model.AVAILABLE_TRACKS
import com.example.model.AVAILABLE_VEHICLES
import com.example.model.LeaderboardEntry
import com.example.model.MultiplayerRacer
import com.example.model.MultiplayerRoom
import com.example.model.PlatformType
import com.example.model.RaceReplay
import com.example.model.SocialTelemetryPost
import com.example.model.TRACK_MONZA_APEX
import com.example.model.TRACK_SUZUKA_APEX
import com.example.model.TRACK_TOKYO_EXPRESSWAY
import com.example.model.TelemetrySnapshot
import com.example.model.VehicleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RaceRepository(
  private val raceDao: RaceDao,
  private val raceResultDao: RaceResultDao? = null
) {

  val allSessions: Flow<List<RaceSessionEntity>> = raceDao.getAllSessions()
  val allRaceResults: Flow<List<RaceResult>>? = raceResultDao?.getAllResults()

  suspend fun saveRaceSession(entity: RaceSessionEntity): Long {
    return raceDao.insertSession(entity)
  }

  suspend fun saveRaceResult(result: RaceResult): Long {
    return raceResultDao?.insertResult(result) ?: -1L
  }

  fun getResultsForTrack(trackName: String): Flow<List<RaceResult>>? {
    return raceResultDao?.getResultsForTrack(trackName)
  }

  suspend fun deleteRaceResult(id: Long) {
    raceResultDao?.deleteResultById(id)
  }

  suspend fun getBestLap(trackId: String, vehicleId: String): RaceSessionEntity? {
    return raceDao.getBestLap(trackId, vehicleId)
  }

  suspend fun getSessionById(id: Long): RaceSessionEntity? {
    return raceDao.getSessionById(id)
  }

  // Pre-configured benchmark rival leaderboards with cross-platform competition
  fun getLeaderboard(
    trackId: String,
    vehicleTypeFilter: VehicleType? = null,
    platformFilter: PlatformType? = null
  ): List<LeaderboardEntry> {
    val rawList = when (trackId) {
      TRACK_SUZUKA_APEX.id -> listOf(
        LeaderboardEntry(1, "Apex_Kenshin", PlatformType.PC, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 88120L, 29800L, 29100L, 29220L, 331.2f, "Direct Drive Wheel", "12m ago"),
        LeaderboardEntry(2, "Verstappen_Gamer", PlatformType.PLAYSTATION, "Formula Apex Hybrid", VehicleType.CAR, 88450L, 29950L, 29200L, 29300L, 348.6f, "Direct Drive Wheel", "1h ago"),
        LeaderboardEntry(3, "DucatiRossi_46", PlatformType.XBOX, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 89040L, 30120L, 29400L, 29520L, 329.8f, "DualSense Controller", "3h ago"),
        LeaderboardEntry(4, "TurboChrono", PlatformType.ANDROID, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 89780L, 30400L, 29650L, 29730L, 314.5f, "Touch / Motion Gyro", "5h ago"),
        LeaderboardEntry(5, "NordSchleifeGhost", PlatformType.PC, "Formula Apex Hybrid", VehicleType.CAR, 90120L, 30600L, 29720L, 29800L, 350.2f, "Direct Drive Wheel", "8h ago"),
        LeaderboardEntry(6, "SpeedySamurai", PlatformType.IOS, "Torque 890 R Naked", VehicleType.MOTORBIKE, 91540L, 31100L, 30100L, 30340L, 258.9f, "Touch / Motion Gyro", "1d ago"),
        LeaderboardEntry(7, "Monza_Bullet", PlatformType.PLAYSTATION, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 92100L, 31400L, 30200L, 30500L, 312.0f, "DualSense Controller", "2d ago")
      )
      TRACK_TOKYO_EXPRESSWAY.id -> listOf(
        LeaderboardEntry(1, "MidnightDevil_Z", PlatformType.PC, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 92450L, 28100L, 32400L, 31950L, 322.4f, "Direct Drive Wheel", "25m ago"),
        LeaderboardEntry(2, "Wangan_Ghost", PlatformType.PLAYSTATION, "Formula Apex Hybrid", VehicleType.CAR, 92890L, 28300L, 32550L, 32040L, 352.0f, "DualSense Controller", "45m ago"),
        LeaderboardEntry(3, "NeoTokyoRider", PlatformType.ANDROID, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 93420L, 28550L, 32700L, 32170L, 328.6f, "Touch / Motion Gyro", "2h ago"),
        LeaderboardEntry(4, "CyberMoto_99", PlatformType.IOS, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 94120L, 28800L, 32900L, 32420L, 325.1f, "Touch / Motion Gyro", "6h ago"),
        LeaderboardEntry(5, "ShutoExpress", PlatformType.XBOX, "Torque 890 R Naked", VehicleType.MOTORBIKE, 95300L, 29200L, 33200L, 32900L, 259.4f, "DualSense Controller", "1d ago")
      )
      else -> listOf( // Monza Speedring
        LeaderboardEntry(1, "MonzaMaster_ITA", PlatformType.PC, "Formula Apex Hybrid", VehicleType.CAR, 77340L, 25100L, 26400L, 25840L, 356.4f, "Direct Drive Wheel", "5m ago"),
        LeaderboardEntry(2, "Bagnaia_Apex", PlatformType.PLAYSTATION, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 78100L, 25400L, 26700L, 26000L, 334.8f, "DualSense Controller", "18m ago"),
        LeaderboardEntry(3, "TelemetryGod", PlatformType.PC, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 78890L, 25700L, 26900L, 26290L, 319.2f, "Direct Drive Wheel", "1h ago"),
        LeaderboardEntry(4, "SpeedRacer_99", PlatformType.ANDROID, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 79420L, 25900L, 27100L, 26420L, 330.1f, "Touch / Motion Gyro", "3h ago"),
        LeaderboardEntry(5, "XboxChicane", PlatformType.XBOX, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 80210L, 26200L, 27400L, 26610L, 316.5f, "DualSense Controller", "4h ago"),
        LeaderboardEntry(6, "MotoSwift_iOS", PlatformType.IOS, "Torque 890 R Naked", VehicleType.MOTORBIKE, 81450L, 26700L, 27800L, 26950L, 261.0f, "Touch / Motion Gyro", "12h ago")
      )
    }

    return rawList.filter { entry ->
      (vehicleTypeFilter == null || entry.vehicleType == vehicleTypeFilter) &&
      (platformFilter == null || entry.platform == platformFilter)
    }
  }

  // Active Cross-Platform Multiplayer Rooms
  fun getMultiplayerRooms(): List<MultiplayerRoom> {
    return listOf(
      MultiplayerRoom(
        roomCode = "APX-9021",
        name = "Pro Tier 1 | Monza High-Speed Draft",
        track = TRACK_MONZA_APEX,
        vehicleTypeAllowed = "Open (Cars & Bikes)",
        maxPlayers = 8,
        currentLaps = 5,
        isCrossPlayEnabled = true,
        status = "Grid Countdown",
        racers = listOf(
          MultiplayerRacer("r1", "Verstappen_Gamer", PlatformType.PC, "Formula Apex Hybrid", VehicleType.CAR, 22, 1, 0.45f, 328f, 0.0f, 77340L, isHost = true),
          MultiplayerRacer("r2", "Bagnaia_Apex", PlatformType.PLAYSTATION, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 34, 2, 0.44f, 321f, 0.42f, 78100L),
          MultiplayerRacer("r3", "DriftDemon_XB", PlatformType.XBOX, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 29, 3, 0.42f, 305f, 1.15f, 78890L),
          MultiplayerRacer("r4", "MobileApex_Pro", PlatformType.ANDROID, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 41, 4, 0.40f, 312f, 2.30f, 79420L, isLocalPlayer = true),
          MultiplayerRacer("r5", "iOS_Slipstream", PlatformType.IOS, "Torque 890 R Naked", VehicleType.MOTORBIKE, 52, 5, 0.38f, 255f, 3.80f, 81450L)
        )
      ),
      MultiplayerRoom(
        roomCode = "MTO-4482",
        name = "MotoGP Superbike Cup | Suzuka",
        track = TRACK_SUZUKA_APEX,
        vehicleTypeAllowed = "Motorbikes Only",
        maxPlayers = 6,
        currentLaps = 4,
        isCrossPlayEnabled = true,
        status = "In Lobby (5/6)",
        racers = listOf(
          MultiplayerRacer("r10", "DucatiRossi_46", PlatformType.PLAYSTATION, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 28, 1, 0.0f, 0f, 0f, 89040L, isHost = true),
          MultiplayerRacer("r11", "Apex_Kenshin", PlatformType.PC, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 18, 2, 0.0f, 0f, 0f, 88120L),
          MultiplayerRacer("r12", "TokyoRider_99", PlatformType.ANDROID, "Torque 890 R Naked", VehicleType.MOTORBIKE, 45, 3, 0.0f, 0f, 0f, 91540L),
          MultiplayerRacer("r13", "BikerBoy_UK", PlatformType.PC, "Pulse V4R Superbike", VehicleType.MOTORBIKE, 31, 4, 0.0f, 0f, 0f, 90400L),
          MultiplayerRacer("r14", "LeanMachine", PlatformType.IOS, "Torque 890 R Naked", VehicleType.MOTORBIKE, 49, 5, 0.0f, 0f, 0f, 92100L)
        )
      ),
      MultiplayerRoom(
        roomCode = "TYO-8800",
        name = "Tokyo Midnight Wangan Sprint",
        track = TRACK_TOKYO_EXPRESSWAY,
        vehicleTypeAllowed = "Cars Only",
        maxPlayers = 8,
        currentLaps = 3,
        isCrossPlayEnabled = true,
        status = "In Paddock (3/8)",
        racers = listOf(
          MultiplayerRacer("r20", "MidnightDevil_Z", PlatformType.PC, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 24, 1, 0.0f, 0f, 0f, 92450L, isHost = true),
          MultiplayerRacer("r21", "Wangan_Ghost", PlatformType.PLAYSTATION, "Formula Apex Hybrid", VehicleType.CAR, 32, 2, 0.0f, 0f, 0f, 92890L),
          MultiplayerRacer("r22", "TurboXB_Racer", PlatformType.XBOX, "Apex GT3-R Twin-Turbo", VehicleType.CAR, 38, 3, 0.0f, 0f, 0f, 94200L)
        )
      )
    )
  }

  // Social community feed posts
  fun getSocialPosts(): List<SocialTelemetryPost> {
    return listOf(
      SocialTelemetryPost(
        id = "post_1",
        authorName = "Apex_Kenshin",
        authorAvatarInitials = "AK",
        platform = PlatformType.PC,
        trackName = "Suzuka Grand GP",
        vehicleName = "Pulse V4R Superbike",
        vehicleType = VehicleType.MOTORBIKE,
        lapTimeMs = 88120L,
        topSpeedKmh = 331.2f,
        peakG = 1.95f,
        likesCount = 342,
        commentsCount = 28,
        timeAgo = "18m ago",
        caption = "New world record at Suzuka! Pushed the lean angle to 63.8° through the 130R apex! Try to beat my ghost telemetry line.",
        challengeCode = "RIVAL-SUZ-8812"
      ),
      SocialTelemetryPost(
        id = "post_2",
        authorName = "Verstappen_Gamer",
        authorAvatarInitials = "VG",
        platform = PlatformType.PLAYSTATION,
        trackName = "Monza Speed Autodrome",
        vehicleName = "Formula Apex Hybrid",
        vehicleType = VehicleType.CAR,
        lapTimeMs = 77340L,
        topSpeedKmh = 356.4f,
        peakG = 3.65f,
        likesCount = 512,
        commentsCount = 45,
        timeAgo = "1h ago",
        caption = "Zero lift through Curva Grande and nailed the braking point for Variante Ascari at 110m. DRS opened full throttle!",
        challengeCode = "RIVAL-MNZ-7734"
      ),
      SocialTelemetryPost(
        id = "post_3",
        authorName = "TurboChrono",
        authorAvatarInitials = "TC",
        platform = PlatformType.ANDROID,
        trackName = "Tokyo Midnight Expressway",
        vehicleName = "Apex GT3-R Twin-Turbo",
        vehicleType = VehicleType.CAR,
        lapTimeMs = 93420L,
        topSpeedKmh = 328.6f,
        peakG = 2.42f,
        likesCount = 189,
        commentsCount = 14,
        timeAgo = "3h ago",
        caption = "Mobile gyroscope steering dialed in at 85% sensitivity. Sector 2 hairpin exit was ultra clean. Cross-play lobby tonight!",
        challengeCode = "RIVAL-TYO-9342"
      )
    )
  }

  // Generates rich synthetic replay telemetry for demonstration & ghost comparison
  fun generateSampleReplay(
    track: com.example.model.Track,
    vehicle: com.example.model.Vehicle,
    targetLapTimeMs: Long
  ): RaceReplay {
    val frames = mutableListOf<TelemetrySnapshot>()
    val totalFrames = 180
    val timeStepMs = targetLapTimeMs / totalFrames

    for (i in 0 until totalFrames) {
      val progress = i.toFloat() / totalFrames
      val pointIndex = ((track.pathPoints.size - 1) * progress).toInt()
      val baseSpeed = track.pathPoints[pointIndex].speedSuggestedKmh
      val speedFactor = vehicle.topSpeedKmh / 320f
      val speed = (baseSpeed * speedFactor * (0.95f + (i % 7) * 0.015f)).coerceIn(60f, vehicle.topSpeedKmh.toFloat())

      val isCar = vehicle.type == VehicleType.CAR
      val gear = when {
        speed < 90 -> 2
        speed < 145 -> 3
        speed < 195 -> 4
        speed < 245 -> 5
        speed < 290 -> 6
        else -> if (vehicle.gears >= 7) 7 else 6
      }

      val rpm = ((vehicle.idleRpm + (speed / vehicle.topSpeedKmh) * (vehicle.redlineRpm - vehicle.idleRpm)) * 0.9f).toInt()
        .coerceIn(vehicle.idleRpm, vehicle.maxRpm)

      val sector = when {
        progress < track.sector1EndProgress -> 1
        progress < track.sector2EndProgress -> 2
        else -> 3
      }

      val steer = (Math.sin(progress * 16.0) * 28.0).toFloat()
      val lean = if (!isCar) (Math.sin(progress * 16.0) * vehicle.maxLeanAngleDeg * 0.9f).toFloat() else 0f
      val latG = (Math.abs(steer) / 28f * vehicle.peakCorneringG).coerceIn(0.1f, vehicle.peakCorneringG)
      val throttle = if (Math.abs(steer) > 18f) 0.65f else 1.0f
      val brake = if (Math.abs(steer) > 22f && speed > 180f) 0.7f else 0.0f

      frames.add(
        TelemetrySnapshot(
          timestampMs = i * timeStepMs,
          speedKmh = speed,
          rpm = rpm,
          gear = gear,
          throttle = throttle,
          brake = brake,
          steerAngleDeg = steer,
          leanAngleDeg = lean,
          lateralG = latG,
          longitudinalG = if (brake > 0.1f) -1.8f else 0.8f,
          trackProgress = progress,
          lapTimeMs = i * timeStepMs,
          sector = sector,
          deltaVsGhostMs = (Math.sin(progress * 8.0) * 280).toLong(),
          tireTempC = 86f + (latG * 6f)
        )
      )
    }

    return RaceReplay(
      id = "replay_${System.currentTimeMillis()}",
      vehicleName = vehicle.name,
      vehicleType = vehicle.type,
      trackName = track.name,
      trackId = track.id,
      lapTimeMs = targetLapTimeMs,
      topSpeedKmh = vehicle.topSpeedKmh * 0.98f,
      maxLateralG = vehicle.peakCorneringG,
      dateRecorded = System.currentTimeMillis(),
      telemetryFrames = frames
    )
  }
}
