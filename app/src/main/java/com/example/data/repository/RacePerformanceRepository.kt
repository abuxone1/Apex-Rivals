package com.example.data.repository

import com.example.data.local.RacePerformanceMetric
import com.example.data.local.RacePerformanceMetricDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository abstracting data access for race performance telemetry.
 * Serves as the single source of truth between the Room database DAO and ViewModels.
 */
class RacePerformanceRepository(
  private val dao: RacePerformanceMetricDao
) {

  val allMetrics: Flow<List<RacePerformanceMetric>> = dao.getAllMetrics()
  val fastestLaps: Flow<List<RacePerformanceMetric>> = dao.getFastestLaps()
  val topSpeedLaps: Flow<List<RacePerformanceMetric>> = dao.getTopSpeedLaps()
  val bestOverallLapTime: Flow<Long?> = dao.getBestOverallLapTime()
  val maxVehicleSpeed: Flow<Float?> = dao.getMaxVehicleSpeed()
  val avgVehicleSpeed: Flow<Float?> = dao.getAvgVehicleSpeed()
  val totalLapsCount: Flow<Int> = dao.getTotalLapsCount()

  fun getMetricsForTrack(trackName: String): Flow<List<RacePerformanceMetric>> {
    return dao.getMetricsForTrack(trackName)
  }

  suspend fun getMetricById(id: Long): RacePerformanceMetric? {
    return dao.getMetricById(id)
  }

  suspend fun recordTelemetry(
    trackName: String,
    lapTimeMs: Long,
    vehicleSpeed: Float,
    vehicleName: String = "Apex GT3-R Twin-Turbo",
    driverName: String = "Apex Pilot",
    lapNumber: Int = 1,
    topSpeedKmh: Float = vehicleSpeed,
    avgSpeedKmh: Float = 0f,
    peakAccelerationG: Float = 0f,
    maxLateralG: Float = 0f,
    sector1Ms: Long = 0L,
    sector2Ms: Long = 0L,
    sector3Ms: Long = 0L,
    raceMode: String = "Time Trial",
    weather: String = "Dry Asphalt"
  ): Long {
    val metric = RacePerformanceMetric(
      trackName = trackName,
      lapTimeMs = lapTimeMs,
      vehicleSpeed = vehicleSpeed,
      vehicleName = vehicleName,
      driverName = driverName,
      lapNumber = lapNumber,
      topSpeedKmh = topSpeedKmh,
      avgSpeedKmh = avgSpeedKmh,
      peakAccelerationG = peakAccelerationG,
      maxLateralG = maxLateralG,
      sector1Ms = sector1Ms,
      sector2Ms = sector2Ms,
      sector3Ms = sector3Ms,
      raceMode = raceMode,
      weather = weather,
      timestamp = System.currentTimeMillis()
    )
    return dao.insertMetric(metric)
  }

  suspend fun insertMetric(metric: RacePerformanceMetric): Long {
    return dao.insertMetric(metric)
  }

  suspend fun insertAll(metrics: List<RacePerformanceMetric>) {
    dao.insertAll(metrics)
  }

  suspend fun updateMetric(metric: RacePerformanceMetric) {
    dao.updateMetric(metric)
  }

  suspend fun deleteMetricById(id: Long) {
    dao.deleteById(id)
  }

  suspend fun clearAllMetrics() {
    dao.clearAll()
  }

  /**
   * Seeds realistic benchmark performance metrics if the database table is empty.
   */
  suspend fun seedInitialBenchmarkMetricsIfEmpty() {
    val current = dao.getAllMetricsSnapshot()
    if (current.isEmpty()) {
      val benchmarks = listOf(
        RacePerformanceMetric(
          trackName = "Monza Speed Autodrome",
          lapTimeMs = 77340L,
          vehicleSpeed = 356.4f,
          vehicleName = "Formula Apex Hybrid",
          driverName = "Apex Pilot",
          lapNumber = 5,
          topSpeedKmh = 356.4f,
          avgSpeedKmh = 269.8f,
          peakAccelerationG = 1.85f,
          maxLateralG = 3.65f,
          sector1Ms = 25100L,
          sector2Ms = 26400L,
          sector3Ms = 25840L,
          raceMode = "Time Trial",
          weather = "Dry Asphalt"
        ),
        RacePerformanceMetric(
          trackName = "Suzuka Grand GP",
          lapTimeMs = 88120L,
          vehicleSpeed = 331.2f,
          vehicleName = "Pulse V4R Superbike",
          driverName = "Apex Pilot",
          lapNumber = 4,
          topSpeedKmh = 331.2f,
          avgSpeedKmh = 237.2f,
          peakAccelerationG = 1.95f,
          maxLateralG = 2.15f,
          sector1Ms = 29800L,
          sector2Ms = 29100L,
          sector3Ms = 29220L,
          raceMode = "Grand Prix",
          weather = "Dry Asphalt"
        ),
        RacePerformanceMetric(
          trackName = "Tokyo Midnight Expressway",
          lapTimeMs = 93420L,
          vehicleSpeed = 328.6f,
          vehicleName = "Apex GT3-R Twin-Turbo",
          driverName = "Apex Pilot",
          lapNumber = 3,
          topSpeedKmh = 328.6f,
          avgSpeedKmh = 247.1f,
          peakAccelerationG = 1.62f,
          maxLateralG = 2.42f,
          sector1Ms = 28550L,
          sector2Ms = 32700L,
          sector3Ms = 32170L,
          raceMode = "Sprint",
          weather = "Night Run"
        ),
        RacePerformanceMetric(
          trackName = "Spa-Francorchamps",
          lapTimeMs = 104250L,
          vehicleSpeed = 338.9f,
          vehicleName = "Formula Apex Hybrid",
          driverName = "Apex Pilot",
          lapNumber = 2,
          topSpeedKmh = 338.9f,
          avgSpeedKmh = 241.8f,
          peakAccelerationG = 1.78f,
          maxLateralG = 3.82f,
          sector1Ms = 31200L,
          sector2Ms = 42100L,
          sector3Ms = 30950L,
          raceMode = "Time Trial",
          weather = "Dry Asphalt"
        )
      )
      dao.insertAll(benchmarks)
    }
  }
}
