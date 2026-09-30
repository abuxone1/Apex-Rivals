package com.example

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.RacePerformanceMetric
import com.example.data.repository.RacePerformanceRepository
import com.example.ui.viewmodel.PerformanceSortOption
import com.example.ui.viewmodel.RacePerformanceViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RacePerformanceTelemetryTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: RacePerformanceRepository
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = RacePerformanceRepository(db.racePerformanceMetricDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `room schema correctly stores and retrieves lap times, track name, and vehicle speed`() = runBlocking {
    val metric = RacePerformanceMetric(
      trackName = "Monza Speed Autodrome",
      lapTimeMs = 77340L,
      vehicleSpeed = 356.4f,
      vehicleName = "Formula Apex Hybrid",
      driverName = "Apex Driver",
      lapNumber = 5
    )

    val id = db.racePerformanceMetricDao().insertMetric(metric)
    assertTrue("Inserted ID should be positive", id > 0)

    val retrieved = db.racePerformanceMetricDao().getMetricById(id)
    assertNotNull("Retrieved metric should not be null", retrieved)
    assertEquals("Monza Speed Autodrome", retrieved?.trackName)
    assertEquals(77340L, retrieved?.lapTimeMs)
    assertEquals(356.4f, retrieved?.vehicleSpeed ?: 0f, 0.01f)
    assertEquals("01:17.340", retrieved?.formattedLapTime)
    assertEquals("356.4 km/h", retrieved?.formattedSpeed)
  }

  @Test
  fun `dao queries sort correctly by fastest lap time and highest vehicle speed`() = runBlocking {
    val metric1 = RacePerformanceMetric(
      trackName = "Suzuka Grand GP",
      lapTimeMs = 88120L,
      vehicleSpeed = 330.0f
    )
    val metric2 = RacePerformanceMetric(
      trackName = "Suzuka Grand GP",
      lapTimeMs = 85400L, // Faster lap
      vehicleSpeed = 345.5f // Higher speed
    )
    val metric3 = RacePerformanceMetric(
      trackName = "Monza Speed Autodrome",
      lapTimeMs = 76900L, // Fastest lap
      vehicleSpeed = 360.2f // Highest speed
    )

    db.racePerformanceMetricDao().insertAll(listOf(metric1, metric2, metric3))

    // Fastest laps order (ascending lap time)
    val fastest = db.racePerformanceMetricDao().getFastestLaps().first()
    assertEquals(3, fastest.size)
    assertEquals(76900L, fastest[0].lapTimeMs)
    assertEquals(85400L, fastest[1].lapTimeMs)
    assertEquals(88120L, fastest[2].lapTimeMs)

    // Top speed laps order (descending vehicle speed)
    val topSpeeds = db.racePerformanceMetricDao().getTopSpeedLaps().first()
    assertEquals(3, topSpeeds.size)
    assertEquals(360.2f, topSpeeds[0].vehicleSpeed, 0.01f)
    assertEquals(345.5f, topSpeeds[1].vehicleSpeed, 0.01f)
    assertEquals(330.0f, topSpeeds[2].vehicleSpeed, 0.01f)
  }

  @Test
  fun `repository records telemetry and filters by track name`() = runBlocking {
    repository.recordTelemetry(
      trackName = "Tokyo Midnight Expressway",
      lapTimeMs = 93420L,
      vehicleSpeed = 328.6f,
      vehicleName = "Apex GT3-R Twin-Turbo",
      driverName = "Midnight Racer"
    )

    repository.recordTelemetry(
      trackName = "Monza Speed Autodrome",
      lapTimeMs = 77100L,
      vehicleSpeed = 355.0f,
      vehicleName = "Formula Apex Hybrid",
      driverName = "Apex Driver"
    )

    val tokyoMetrics = repository.getMetricsForTrack("Tokyo Midnight Expressway").first()
    assertEquals(1, tokyoMetrics.size)
    assertEquals("Tokyo Midnight Expressway", tokyoMetrics[0].trackName)
    assertEquals(93420L, tokyoMetrics[0].lapTimeMs)
    assertEquals(328.6f, tokyoMetrics[0].vehicleSpeed, 0.01f)
  }

  @Test
  fun `performance summary stats calculates personal bests and maximum speeds accurately`() {
    val metric1 = RacePerformanceMetric(
      trackName = "Spa-Francorchamps",
      lapTimeMs = 104250L,
      vehicleSpeed = 338.9f,
      vehicleName = "Formula Apex Hybrid"
    )
    val metric2 = RacePerformanceMetric(
      trackName = "Spa-Francorchamps",
      lapTimeMs = 101800L,
      vehicleSpeed = 345.2f,
      vehicleName = "Formula Apex Hybrid"
    )

    val validLaps = listOf(metric1, metric2).filter { it.lapTimeMs > 0 }
    val bestLap = validLaps.minByOrNull { it.lapTimeMs }
    val maxSpeed = listOf(metric1, metric2).maxOfOrNull { it.vehicleSpeed } ?: 0f
    val avgSpeed = listOf(metric1, metric2).map { it.vehicleSpeed }.average().toFloat()

    val stats = com.example.ui.viewmodel.PerformanceSummaryStats(
      bestLapTimeMs = bestLap?.lapTimeMs ?: 0L,
      maxVehicleSpeedKmh = maxSpeed,
      avgVehicleSpeedKmh = avgSpeed,
      totalLapsRecorded = 2,
      fastestTrackName = bestLap?.trackName ?: ""
    )

    assertEquals(101800L, stats.bestLapTimeMs)
    assertEquals("01:41.800", stats.formattedBestLap)
    assertEquals(345.2f, stats.maxVehicleSpeedKmh, 0.01f)
    assertEquals("345.2 km/h", stats.formattedMaxSpeed)
    assertEquals(2, stats.totalLapsRecorded)
    assertEquals("Spa-Francorchamps", stats.fastestTrackName)
  }

  @Test
  fun `telemetry entity calculates speed in mph correctly`() {
    val metric = RacePerformanceMetric(
      trackName = "Monza Speed Autodrome",
      lapTimeMs = 77340L,
      vehicleSpeed = 350.0f
    )

    // 350 km/h * 0.621371 = 217.48 mph
    assertEquals(217.48f, metric.speedMph, 0.1f)
    assertTrue("formattedSpeedMph should contain mph", metric.formattedSpeedMph.contains("mph"))
    assertEquals("01:17.340", metric.formattedLapTime)
  }
}
