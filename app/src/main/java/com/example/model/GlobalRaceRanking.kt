package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class GlobalRaceRanking(
  val id: String = "",
  val rank: Int = 1,
  val userId: String = "",
  val driverName: String = "Apex Racer",
  val trackName: String = "Monza GP",
  val vehicleName: String = "Apex GT3-R",
  val vehicleType: VehicleType = VehicleType.CAR,
  val lapTimeMs: Long = 78420L,
  val gapToLeaderMs: Long = 0L,
  val topSpeedKmh: Float = 294.5f,
  val platform: PlatformType = PlatformType.PC,
  val inputDevice: String = "Direct Drive Wheel",
  val timestamp: Long = System.currentTimeMillis(),
  val isVerifiedFirestore: Boolean = true,
  val isLocalDriver: Boolean = false
) {
  val formattedLapTime: String
    get() {
      val minutes = (lapTimeMs / 60000)
      val seconds = (lapTimeMs % 60000) / 1000
      val millis = (lapTimeMs % 1000)
      return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
    }

  val formattedGap: String
    get() = if (rank == 1 || gapToLeaderMs <= 0L) {
      "LEADER"
    } else {
      String.format(Locale.US, "+%.3fs", gapToLeaderMs / 1000.0)
    }

  val formattedDate: String
    get() {
      val diffMs = System.currentTimeMillis() - timestamp
      val diffMins = diffMs / (1000 * 60)
      val diffHours = diffMins / 60
      val diffDays = diffHours / 24

      return when {
        diffMins < 2 -> "Just now"
        diffMins < 60 -> "${diffMins}m ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays < 7 -> "${diffDays}d ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(timestamp))
      }
    }
}
