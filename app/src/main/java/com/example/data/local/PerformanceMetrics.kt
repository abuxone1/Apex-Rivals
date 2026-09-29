package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "performance_metrics")
data class PerformanceMetrics(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val raceId: String = "",
  val driverName: String = "Apex Pilot",
  val trackName: String = "",
  val vehicleName: String = "",
  val lapTimeMs: Long = 0L,
  val topSpeedKmh: Float = 0f,
  val peakAccelerationG: Float = 0f,
  val zeroToHundredKmhSeconds: Float = 0f,
  val maxLateralG: Float = 0f,
  val avgSpeedKmh: Float = 0f,
  val trackDistanceMeters: Int = 0,
  val raceType: String = "Time Trial",
  val trackComplexity: String = "Technical",
  val weatherCondition: String = "Dry Asphalt",
  val tags: List<String> = emptyList(),
  val timestamp: Long = System.currentTimeMillis()
) {

  /**
   * Returns a deduplicated list of all category tags including track complexity,
   * weather conditions, and custom user tags.
   */
  fun getAllTags(): List<String> {
    val list = LinkedHashSet<String>()
    if (trackComplexity.isNotBlank()) list.add(trackComplexity.trim())
    if (weatherCondition.isNotBlank()) list.add(weatherCondition.trim())
    tags.forEach { if (it.isNotBlank()) list.add(it.trim()) }
    return list.toList()
  }

  /**
   * Checks if this record has a specific tag or matches track complexity or weather condition.
   */
  fun matchesTag(tag: String): Boolean {
    if (tag.isBlank() || tag.equals("All Tags", ignoreCase = true)) return true
    val clean = tag.trim().lowercase(Locale.US)
    return trackComplexity.lowercase(Locale.US) == clean ||
           weatherCondition.lowercase(Locale.US) == clean ||
           tags.any { it.trim().lowercase(Locale.US) == clean }
  }

  /**
   * Calculates the real average speed in km/h: (distance_km / lap_time_hours).
   * If avgSpeedKmh is pre-stored and > 0, returns it, otherwise calculates dynamically.
   */
  fun getComputedAvgSpeed(): Float {
    if (avgSpeedKmh > 0f) return avgSpeedKmh
    return calculateAverageSpeed(trackName, lapTimeMs, trackDistanceMeters)
  }

  fun getEffectiveDistanceMeters(): Int {
    return if (trackDistanceMeters > 0) trackDistanceMeters else getEstimatedTrackDistance(trackName)
  }

  companion object {
    val TRACK_COMPLEXITIES = listOf("Technical", "High Speed", "Flowing", "Extreme")
    val WEATHER_CONDITIONS = listOf("Dry Asphalt", "Wet Surface", "Heavy Rain", "Night Run")

    /**
     * Standard track distances (in meters) for official circuits.
     */
    fun getEstimatedTrackDistance(trackName: String): Int {
      return when {
        trackName.contains("Monza", ignoreCase = true) -> 5793
        trackName.contains("Spa", ignoreCase = true) -> 7004
        trackName.contains("Suzuka", ignoreCase = true) -> 5807
        trackName.contains("Silverstone", ignoreCase = true) -> 5891
        trackName.contains("Nürburgring", ignoreCase = true) || trackName.contains("Nurburgring", ignoreCase = true) -> 20832
        trackName.contains("Tokyo", ignoreCase = true) -> 6420
        else -> 5500
      }
    }

    /**
     * Calculates average speed in km/h based on track distance and lap time in milliseconds.
     * Speed = Distance (km) / Time (hours) = (Distance / 1000) / (lapTimeMs / 3,600,000)
     */
    fun calculateAverageSpeed(trackName: String, lapTimeMs: Long, distanceMeters: Int = 0): Float {
      if (lapTimeMs <= 0L) return 0f
      val dist = if (distanceMeters > 0) distanceMeters else getEstimatedTrackDistance(trackName)
      val distKm = dist / 1000f
      val hours = lapTimeMs / 3600000f
      return if (hours > 0f) distKm / hours else 0f
    }
  }
}
