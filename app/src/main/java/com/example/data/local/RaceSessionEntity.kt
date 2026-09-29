package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.PlatformType
import com.example.model.TelemetrySnapshot
import com.example.model.VehicleType

@Entity(tableName = "race_sessions")
data class RaceSessionEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val vehicleId: String,
  val vehicleName: String,
  val vehicleType: VehicleType,
  val trackId: String,
  val trackName: String,
  val lapTimeMs: Long,
  val topSpeedKmh: Float,
  val peakLateralG: Float,
  val sector1Ms: Long,
  val sector2Ms: Long,
  val sector3Ms: Long,
  val timestamp: Long = System.currentTimeMillis(),
  val platform: PlatformType = PlatformType.ANDROID,
  val telemetryJson: String // Serialized list of key telemetry checkpoints for ghost replay
)
