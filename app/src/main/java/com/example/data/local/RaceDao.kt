package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceDao {

  @Query("SELECT * FROM race_sessions ORDER BY timestamp DESC")
  fun getAllSessions(): Flow<List<RaceSessionEntity>>

  @Query("SELECT * FROM race_sessions WHERE trackId = :trackId ORDER BY lapTimeMs ASC")
  fun getSessionsForTrack(trackId: String): Flow<List<RaceSessionEntity>>

  @Query("SELECT * FROM race_sessions WHERE trackId = :trackId AND vehicleId = :vehicleId ORDER BY lapTimeMs ASC LIMIT 1")
  suspend fun getBestLap(trackId: String, vehicleId: String): RaceSessionEntity?

  @Query("SELECT * FROM race_sessions WHERE id = :id LIMIT 1")
  suspend fun getSessionById(id: Long): RaceSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: RaceSessionEntity): Long

  @Query("DELETE FROM race_sessions WHERE id = :id")
  suspend fun deleteSessionById(id: Long)
}
