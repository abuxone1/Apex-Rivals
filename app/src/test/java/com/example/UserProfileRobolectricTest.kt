package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.PerformanceMetrics
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
class UserProfileRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `room database aggregates total distance average speed and personal records`() = runBlocking {
    val dao = db.performanceMetricsDao()

    val sample1 = PerformanceMetrics(
      raceId = "run_1",
      driverName = "Apex Pilot",
      trackName = "Monza GP",
      vehicleName = "Apex GT3-R Twin-Turbo",
      lapTimeMs = 78000L,
      topSpeedKmh = 320.0f,
      peakAccelerationG = 2.10f,
      zeroToHundredKmhSeconds = 2.80f,
      maxLateralG = 2.50f,
      trackDistanceMeters = 5793,
      avgSpeedKmh = 267.3f
    )

    val sample2 = PerformanceMetrics(
      raceId = "run_2",
      driverName = "Apex Pilot",
      trackName = "Spa-Francorchamps",
      vehicleName = "Formula Apex Hybrid",
      lapTimeMs = 104000L,
      topSpeedKmh = 345.5f,
      peakAccelerationG = 2.40f,
      zeroToHundredKmhSeconds = 2.20f,
      maxLateralG = 3.90f,
      trackDistanceMeters = 7004,
      avgSpeedKmh = 242.4f
    )

    dao.insertAll(listOf(sample1, sample2))

    // Verify session count
    val count = dao.getTotalSessionsCount().first()
    assertEquals(2, count)

    // Verify total distance (5793 + 7004 = 12797 meters)
    val totalDistanceM = dao.getTotalDistanceMeters().first()
    assertNotNull(totalDistanceM)
    assertEquals(12797L, totalDistanceM)

    val totalKm = (totalDistanceM ?: 0L) / 1000f
    assertEquals(12.797f, totalKm, 0.001f)
    val totalMiles = totalKm * 0.621371f
    assertEquals(7.9517f, totalMiles, 0.01f)

    // Verify average speed query
    val avgSpeed = dao.getAverageSpeedKmh().first()
    assertNotNull(avgSpeed)
    val expectedAvg = (267.3f + 242.4f) / 2f
    assertEquals(expectedAvg, avgSpeed ?: 0f, 0.1f)

    // Verify personal records
    val bestLap = dao.getBestLapTimeMs().first()
    assertEquals(78000L, bestLap)

    val maxTopSpeed = dao.getMaxTopSpeedKmh().first()
    assertEquals(345.5f, maxTopSpeed ?: 0f, 0.01f)

    val best0to100 = dao.getBestZeroToHundred().first()
    assertEquals(2.20f, best0to100 ?: 0f, 0.01f)

    val maxLatG = dao.getMaxLateralG().first()
    assertEquals(3.90f, maxLatG ?: 0f, 0.01f)
  }

  @Test
  fun `track personal bests correctly identified per circuit`() = runBlocking {
    val dao = db.performanceMetricsDao()

    val monzaLap1 = PerformanceMetrics(
      raceId = "m1",
      trackName = "Monza GP",
      vehicleName = "Apex GT3-R",
      lapTimeMs = 80000L,
      topSpeedKmh = 310f,
      trackDistanceMeters = 5793,
      avgSpeedKmh = 260f
    )
    val monzaLap2 = PerformanceMetrics(
      raceId = "m2",
      trackName = "Monza GP",
      vehicleName = "Formula Apex",
      lapTimeMs = 74500L, // Better lap
      topSpeedKmh = 335f,
      trackDistanceMeters = 5793,
      avgSpeedKmh = 280f
    )

    dao.insertAll(listOf(monzaLap1, monzaLap2))

    val allRuns = dao.getAllList()
    val monzaRuns = allRuns.filter { it.trackName == "Monza GP" }
    val personalBestMonza = monzaRuns.minByOrNull { it.lapTimeMs }

    assertNotNull(personalBestMonza)
    assertEquals(74500L, personalBestMonza?.lapTimeMs)
    assertEquals("Formula Apex", personalBestMonza?.vehicleName)
    assertEquals(335f, personalBestMonza?.topSpeedKmh ?: 0f, 0.01f)
    assertEquals(5793 * 2, monzaRuns.sumOf { it.getEffectiveDistanceMeters() })
  }
}
