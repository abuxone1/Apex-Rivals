package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "race_results")
data class RaceResult(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0L,
  val trackName: String,
  val vehicleName: String,
  val lapTimeMs: Long,
  val topSpeedKmh: Float = 0f,
  val timestamp: Long = System.currentTimeMillis()
)
