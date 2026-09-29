package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.RaceResult
import com.example.engine.MultiplayerSyncEngine
import com.example.engine.RacingPhysicsEngine
import com.example.model.AVAILABLE_TRACKS
import com.example.model.AVAILABLE_VEHICLES
import com.example.model.MultiplayerRacer
import com.example.model.MultiplayerRoom
import com.example.model.PlatformType
import com.example.model.TelemetrySnapshot
import com.example.model.VehicleType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context verifies app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Apex Rivals", appName)
  }

  @Test
  fun `room database stores and retrieves RaceResult entity`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()

    val result = RaceResult(
      trackName = "Autodromo Nazionale Monza",
      vehicleName = "Apex GT3-R Twin-Turbo",
      lapTimeMs = 78120L,
      topSpeedKmh = 318.5f
    )
    val insertedId = db.raceResultDao().insertResult(result)
    assertTrue(insertedId > 0)

    val fetched = db.raceResultDao().getResultById(insertedId)
    assertNotNull(fetched)
    assertEquals("Autodromo Nazionale Monza", fetched?.trackName)
    assertEquals("Apex GT3-R Twin-Turbo", fetched?.vehicleName)
    assertEquals(78120L, fetched?.lapTimeMs)
    assertEquals(318.5f, fetched?.topSpeedKmh ?: 0f, 0.01f)

    // Test insertRaceResult and getBestLapTime
    val fasterResult = RaceResult(
      trackName = "Autodromo Nazionale Monza",
      vehicleName = "Apex GT3-R Twin-Turbo",
      lapTimeMs = 76500L,
      topSpeedKmh = 324.0f
    )
    val secondId = db.raceResultDao().insertRaceResult(fasterResult)
    assertTrue(secondId > 0)

    val bestLapTrack = db.raceResultDao().getBestLapTime("Autodromo Nazionale Monza")
    assertEquals(76500L, bestLapTrack)

    val overallBest = db.raceResultDao().getBestLapTime()
    assertEquals(76500L, overallBest)

    db.close()
  }

  @Test
  fun `racing physics engine accelerates and shifts gears`() {
    val car = AVAILABLE_VEHICLES.first { it.type == VehicleType.CAR }
    val track = AVAILABLE_TRACKS.first()
    val engine = RacingPhysicsEngine(car, track)

    // Initial state: 0 speed, gear 1
    val initialMetrics = engine.getMetrics()
    assertEquals(0f, initialMetrics.speedKmh, 0.01f)
    assertEquals(1, initialMetrics.gear)

    // Apply 100% throttle for 2 seconds
    var metrics = initialMetrics
    for (i in 0 until 60) {
      metrics = engine.update(dtSeconds = 0.033f, throttle = 1.0f, brake = 0f, steer = 0f)
    }

    // Vehicle should have accelerated and increased RPM
    assertTrue("Speed should be greater than 20 km/h", metrics.speedKmh > 20f)
    assertTrue("RPM should exceed idle RPM", metrics.rpm > car.idleRpm)

    // Shift to gear 2
    engine.shiftUp()
    val shiftedMetrics = engine.getMetrics()
    assertEquals(2, shiftedMetrics.gear)

    // Shift down back to gear 1
    engine.shiftDown()
    assertEquals(1, engine.getMetrics().gear)
  }

  @Test
  fun `motorbike physics simulates lean angle and tuck in aero`() {
    val bike = AVAILABLE_VEHICLES.first { it.type == VehicleType.MOTORBIKE }
    val track = AVAILABLE_TRACKS.first()
    val engine = RacingPhysicsEngine(bike, track)

    // Accelerate then steer hard left (-1.0)
    for (i in 0 until 30) {
      engine.update(dtSeconds = 0.033f, throttle = 0.8f, brake = 0f, steer = 0f)
    }
    val turnMetrics = engine.update(dtSeconds = 0.033f, throttle = 0.5f, brake = 0f, steer = -1.0f)

    // Motorbike should lean into the turn
    assertTrue("Lean angle should be negative for left turn", turnMetrics.leanAngleDeg < -10f)

    // Toggle aero tuck-in
    engine.toggleDrsOrTuckIn()
    assertTrue("Tuck-in should be active", engine.getMetrics().isDrsOrTuckInActive)
  }

  @Test
  fun `multiplayer sync engine tracks positions and computes gaps`() {
    val track = AVAILABLE_TRACKS.first()
    val racers = listOf(
      MultiplayerRacer(
        id = "p1",
        name = "Max_Apex",
        platform = PlatformType.PC,
        vehicleName = "Apex GT3-R",
        vehicleType = VehicleType.CAR,
        pingMs = 22,
        currentPos = 1,
        trackProgress = 0.5f,
        speedKmh = 280f,
        gapToLeaderSeconds = 0f,
        lastLapTimeMs = 82000L,
        isLocalPlayer = true
      ),
      MultiplayerRacer(
        id = "p2",
        name = "Pecco_63",
        platform = PlatformType.PLAYSTATION,
        vehicleName = "Pulse V4R Superbike",
        vehicleType = VehicleType.MOTORBIKE,
        pingMs = 35,
        currentPos = 2,
        trackProgress = 0.48f,
        speedKmh = 275f,
        gapToLeaderSeconds = 0.5f,
        lastLapTimeMs = 82500L,
        isLocalPlayer = false
      )
    )
    val room = MultiplayerRoom(
      roomCode = "APX-TEST",
      name = "Test Lobby",
      track = track,
      vehicleTypeAllowed = "Open / Any",
      status = "Racing",
      racers = racers
    )
    val syncEngine = MultiplayerSyncEngine(room)
    val updated = syncEngine.updateCompetitors(dtSeconds = 0.1f, localPlayerProgress = 0.52f, localPlayerSpeed = 290f)

    assertNotNull(updated)
    assertEquals(2, updated.size)
    val localRacer = updated.first { it.isLocalPlayer }
    assertEquals(0.52f, localRacer.trackProgress, 0.001f)
  }
}
