package com.example.model

data class RaceReplay(
  val id: String,
  val vehicleName: String,
  val vehicleType: VehicleType,
  val trackName: String,
  val trackId: String,
  val lapTimeMs: Long,
  val topSpeedKmh: Float,
  val maxLateralG: Float,
  val dateRecorded: Long,
  val playerName: String = "You",
  val platform: PlatformType = PlatformType.ANDROID,
  val telemetryFrames: List<TelemetrySnapshot>,
  val sector1Ms: Long? = null,
  val sector2Ms: Long? = null,
  val sector3Ms: Long? = null
) {
  val driverName: String get() = playerName
}

data class MultiplayerRacer(
  val id: String,
  val name: String,
  val platform: PlatformType,
  val vehicleName: String,
  val vehicleType: VehicleType,
  val pingMs: Int,
  val currentPos: Int,
  val trackProgress: Float,
  val speedKmh: Float,
  val gapToLeaderSeconds: Float,
  val lastLapTimeMs: Long,
  val isHost: Boolean = false,
  val isLocalPlayer: Boolean = false
)

data class MultiplayerRoom(
  val roomCode: String,
  val name: String,
  val track: Track,
  val vehicleTypeAllowed: String, // "Open / Any", "Cars Only", "Bikes Only"
  val maxPlayers: Int = 8,
  val currentLaps: Int = 3,
  val isCrossPlayEnabled: Boolean = true,
  val status: String, // "In Lobby", "Grid Countdown", "Racing", "Post-Race"
  val racers: List<MultiplayerRacer>
)

data class LeaderboardEntry(
  val rank: Int,
  val playerName: String,
  val platform: PlatformType,
  val vehicleName: String,
  val vehicleType: VehicleType,
  val lapTimeMs: Long,
  val sector1Ms: Long,
  val sector2Ms: Long,
  val sector3Ms: Long,
  val topSpeedKmh: Float,
  val inputDevice: String, // "Direct Drive Wheel", "DualSense Controller", "Touch / Motion Gyro"
  val dateAchieved: String,
  val hasReplay: Boolean = true
)

data class SocialTelemetryPost(
  val id: String,
  val authorName: String,
  val authorAvatarInitials: String,
  val platform: PlatformType,
  val trackName: String,
  val vehicleName: String,
  val vehicleType: VehicleType,
  val lapTimeMs: Long,
  val topSpeedKmh: Float,
  val peakG: Float,
  val likesCount: Int,
  val commentsCount: Int,
  val timeAgo: String,
  val caption: String,
  val challengeCode: String
)
