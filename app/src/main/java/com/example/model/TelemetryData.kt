package com.example.model

data class TelemetrySnapshot(
  val timestampMs: Long,
  val speedKmh: Float,
  val rpm: Int,
  val gear: Int, // 0 = Neutral, 1..8
  val throttle: Float, // 0..1
  val brake: Float, // 0..1
  val steerAngleDeg: Float, // -45..+45
  val leanAngleDeg: Float, // -64..+64 (for motorbike)
  val lateralG: Float,
  val longitudinalG: Float,
  val trackProgress: Float, // 0..1
  val lapTimeMs: Long,
  val sector: Int,
  val deltaVsGhostMs: Long = 0L,
  val tireTempC: Float = 88f
)

data class LiveRaceMetrics(
  val speedKmh: Float = 0f,
  val speedMph: Float = 0f,
  val rpm: Int = 1200,
  val maxRpm: Int = 9000,
  val redlineRpm: Int = 8500,
  val gear: Int = 1,
  val throttle: Float = 0f,
  val brake: Float = 0f,
  val steerAngleDeg: Float = 0f,
  val leanAngleDeg: Float = 0f, // active for motorbikes
  val lateralG: Float = 0f,
  val longitudinalG: Float = 0f,
  val peakLateralG: Float = 0f,
  val topSpeedKmh: Float = 0f,
  val trackProgress: Float = 0f, // 0..1
  val lapDistanceMeters: Float = 0f,
  val currentLap: Int = 1,
  val currentLapTimeMs: Long = 0L,
  val bestLapTimeMs: Long? = null,
  val lastLapTimeMs: Long? = null,
  val deltaVsGhostMs: Long = 0L,
  val currentSector: Int = 1,
  val sector1TimeMs: Long? = null,
  val sector2TimeMs: Long? = null,
  val sector3TimeMs: Long? = null,
  val isDrsOrTuckInActive: Boolean = false,
  val tireTempFL: Float = 88f,
  val tireTempFR: Float = 91f,
  val tireTempRL: Float = 86f,
  val tireTempRR: Float = 87f,
  val tirePressurePsi: Float = 24.5f,
  val isDriftingOrKneeDown: Boolean = false,
  val isPitLimiterActive: Boolean = false,
  val activeVehicle: Vehicle = AVAILABLE_VEHICLES[0],
  val activeTrack: Track = TRACK_MONZA_APEX
)
