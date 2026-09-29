package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PerformanceMetricsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(metrics: PerformanceMetrics): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(metrics: List<PerformanceMetrics>)

  @androidx.room.Update
  suspend fun update(metric: PerformanceMetrics)

  @Query("SELECT * FROM performance_metrics ORDER BY timestamp DESC")
  fun getAllPerformanceMetrics(): Flow<List<PerformanceMetrics>>

  @Query("SELECT * FROM performance_metrics ORDER BY timestamp DESC")
  suspend fun getAllList(): List<PerformanceMetrics>

  @Query("SELECT * FROM performance_metrics WHERE trackName = :trackName ORDER BY lapTimeMs ASC")
  fun getMetricsForTrack(trackName: String): Flow<List<PerformanceMetrics>>

  @Query("SELECT * FROM performance_metrics WHERE raceType = :raceType ORDER BY timestamp DESC")
  fun getMetricsByRaceType(raceType: String): Flow<List<PerformanceMetrics>>

  @Query("SELECT * FROM performance_metrics WHERE driverName LIKE '%' || :query || '%' OR trackName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
  fun searchMetrics(query: String): Flow<List<PerformanceMetrics>>

  @Query("SELECT SUM(trackDistanceMeters) FROM performance_metrics")
  fun getTotalDistanceMeters(): Flow<Long?>

  @Query("SELECT AVG(avgSpeedKmh) FROM performance_metrics WHERE avgSpeedKmh > 0")
  fun getAverageSpeedKmh(): Flow<Float?>

  @Query("SELECT MIN(lapTimeMs) FROM performance_metrics WHERE lapTimeMs > 0")
  fun getBestLapTimeMs(): Flow<Long?>

  @Query("SELECT MAX(topSpeedKmh) FROM performance_metrics")
  fun getMaxTopSpeedKmh(): Flow<Float?>

  @Query("SELECT MIN(zeroToHundredKmhSeconds) FROM performance_metrics WHERE zeroToHundredKmhSeconds > 0")
  fun getBestZeroToHundred(): Flow<Float?>

  @Query("SELECT MAX(maxLateralG) FROM performance_metrics")
  fun getMaxLateralG(): Flow<Float?>

  @Query("SELECT COUNT(*) FROM performance_metrics")
  fun getTotalSessionsCount(): Flow<Int>

  @Query("DELETE FROM performance_metrics WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM performance_metrics")
  suspend fun clearAll()
}
