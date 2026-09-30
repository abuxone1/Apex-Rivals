package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

/**
 * Room database entity storing telemetry and race performance metrics:
 * lap times, track name, vehicle speed, driver stats, and dynamics.
 */
@Entity(tableName = "race_performance_telemetry")
data class RacePerformanceMetric(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val trackName: String,
  val lapTimeMs: Long,
  val vehicleSpeed: Float, // Speed in km/h at checkpoint or peak
  val vehicleName: String = "Apex GT3-R Twin-Turbo",
  val driverName: String = "Apex Pilot",
  val lapNumber: Int = 1,
  val topSpeedKmh: Float = vehicleSpeed,
  val avgSpeedKmh: Float = 0f,
  val peakAccelerationG: Float = 0f,
  val maxLateralG: Float = 0f,
  val sector1Ms: Long = 0L,
  val sector2Ms: Long = 0L,
  val sector3Ms: Long = 0L,
  val raceMode: String = "Time Trial",
  val weather: String = "Dry Asphalt",
  val timestamp: Long = System.currentTimeMillis()
) {
  val formattedLapTime: String
    get() {
      if (lapTimeMs <= 0L) return "--:--.---"
      val minutes = lapTimeMs / 60000L
      val seconds = (lapTimeMs % 60000L) / 1000L
      val millis = lapTimeMs % 1000L
      return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
    }

  val formattedSpeed: String
    get() = String.format(Locale.US, "%.1f km/h", vehicleSpeed)

  val speedMph: Float
    get() = vehicleSpeed * 0.621371f

  val formattedSpeedMph: String
    get() = String.format(Locale.US, "%.1f mph", speedMph)
}
