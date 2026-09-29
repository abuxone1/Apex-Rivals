package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceResultDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRaceResult(raceResult: RaceResult): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResult(result: RaceResult): Long

  @Query("SELECT * FROM race_results ORDER BY lapTimeMs ASC")
  fun getAllResults(): Flow<List<RaceResult>>

  @Query("SELECT MIN(lapTimeMs) FROM race_results WHERE trackName = :trackName")
  suspend fun getBestLapTime(trackName: String): Long?

  @Query("SELECT MIN(lapTimeMs) FROM race_results")
  suspend fun getBestLapTime(): Long?

  @Query("SELECT * FROM race_results WHERE trackName = :trackName ORDER BY lapTimeMs ASC")
  fun getResultsForTrack(trackName: String): Flow<List<RaceResult>>

  @Query("SELECT * FROM race_results WHERE id = :id LIMIT 1")
  suspend fun getResultById(id: Long): RaceResult?

  @Query("DELETE FROM race_results WHERE id = :id")
  suspend fun deleteResultById(id: Long)

  @Query("DELETE FROM race_results")
  suspend fun deleteAllResults()
}
