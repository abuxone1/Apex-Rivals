package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [RaceSessionEntity::class, RaceResult::class, PerformanceMetrics::class, RacePerformanceMetric::class],
  version = 8,
  exportSchema = false
)
@TypeConverters(TelemetryConverters::class)
abstract class AppDatabase : RoomDatabase() {

  abstract fun raceDao(): RaceDao
  abstract fun raceResultDao(): RaceResultDao
  abstract fun performanceMetricsDao(): PerformanceMetricsDao
  abstract fun racePerformanceMetricDao(): RacePerformanceMetricDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "apex_rivals_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
