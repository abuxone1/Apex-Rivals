package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for race performance telemetry data.
 * All write queries are suspend functions and all read queries return Flow<T> for reactive UI updates.
 */
@Dao
interface RacePerformanceMetricDao {

  @Query("SELECT * FROM race_performance_telemetry ORDER BY timestamp DESC")
  fun getAllMetrics(): Flow<List<RacePerformanceMetric>>

  @Query("SELECT * FROM race_performance_telemetry ORDER BY timestamp DESC")
  suspend fun getAllMetricsSnapshot(): List<RacePerformanceMetric>

  @Query("SELECT * FROM race_performance_telemetry WHERE trackName = :trackName ORDER BY lapTimeMs ASC")
  fun getMetricsForTrack(trackName: String): Flow<List<RacePerformanceMetric>>

  @Query("SELECT * FROM race_performance_telemetry ORDER BY lapTimeMs ASC")
  fun getFastestLaps(): Flow<List<RacePerformanceMetric>>

  @Query("SELECT * FROM race_performance_telemetry ORDER BY vehicleSpeed DESC")
  fun getTopSpeedLaps(): Flow<List<RacePerformanceMetric>>

  @Query("SELECT * FROM race_performance_telemetry WHERE id = :id LIMIT 1")
  suspend fun getMetricById(id: Long): RacePerformanceMetric?

  @Query("SELECT MIN(lapTimeMs) FROM race_performance_telemetry WHERE lapTimeMs > 0")
  fun getBestOverallLapTime(): Flow<Long?>

  @Query("SELECT MAX(vehicleSpeed) FROM race_performance_telemetry")
  fun getMaxVehicleSpeed(): Flow<Float?>

  @Query("SELECT AVG(vehicleSpeed) FROM race_performance_telemetry WHERE vehicleSpeed > 0")
  fun getAvgVehicleSpeed(): Flow<Float?>

  @Query("SELECT COUNT(*) FROM race_performance_telemetry")
  fun getTotalLapsCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMetric(metric: RacePerformanceMetric): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(metrics: List<RacePerformanceMetric>)

  @Update
  suspend fun updateMetric(metric: RacePerformanceMetric)

  @Query("DELETE FROM race_performance_telemetry WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM race_performance_telemetry")
  suspend fun clearAll()
}
