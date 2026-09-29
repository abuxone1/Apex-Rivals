package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.game.engine.InteractiveRacingEngine
import com.example.game.model.CameraPerspective
import com.example.game.model.GameMode
import com.example.game.model.RaceFinishSummary
import com.example.game.model.RaceState
import com.example.game.ui.InGameDrivingControls
import com.example.game.ui.PostRacePodiumDialog
import com.example.game.ui.RacingGameScreen
import com.example.game.ui.StartLightsOverlay
import com.example.game.ui.TopRacingHudOverlay
import com.example.model.AVAILABLE_TRACKS
import com.example.model.AVAILABLE_VEHICLES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RacingGameSystemTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  private val vehicle = AVAILABLE_VEHICLES[0] // Apex GT3-R Twin-Turbo
  private val track = AVAILABLE_TRACKS[0]     // Monza Speed Autodrome

  @Test
  fun `racing engine initializes with correct rivals, grid count and vehicle setup`() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val engine = InteractiveRacingEngine(context, vehicle, track)

    assertEquals(RaceState.PRE_GRID, engine.raceState.value)
    assertEquals(8, engine.hudState.value.totalRivals)
    assertEquals(7, engine.getRivalsList().size)
    assertEquals(1, engine.hudState.value.currentLap)
    assertEquals(vehicle.idleRpm, engine.rpm)
    assertEquals(1, engine.gear)
    assertTrue("Nitro should be fully charged at start", engine.nitroCharge >= 0.99f)
  }

  @Test
  fun `nitro boost engages and increases speed and consumption`() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val engine = InteractiveRacingEngine(context, vehicle, track)

    assertFalse(engine.isNitroActive)
    engine.triggerNitroBoost()
    assertTrue(engine.isNitroActive)
    assertNotNull(engine.hudState.value.activeAlertMessage)
  }

  @Test
  fun `camera perspective toggles seamlessly between chase, cockpit and hood`() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val engine = InteractiveRacingEngine(context, vehicle, track)

    assertEquals(CameraPerspective.CHASE_CAM, engine.cameraPerspective)
    engine.switchCamera()
    assertEquals(CameraPerspective.COCKPIT_CAM, engine.cameraPerspective)
    engine.switchCamera()
    assertEquals(CameraPerspective.HOOD_CAM, engine.cameraPerspective)
    engine.switchCamera()
    assertEquals(CameraPerspective.CHASE_CAM, engine.cameraPerspective)
  }

  @Test
  fun `racing game screen renders top hud, canvas and pre-grid launch button`() {
    var navigatedBack = false

    composeTestRule.setContent {
      RacingGameScreen(
        vehicle = vehicle,
        track = track,
        onNavigateBack = { navigatedBack = true }
      )
    }

    // Canvas container
    composeTestRule.onNodeWithTag("racing_game_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("racing_game_canvas").assertIsDisplayed()

    // Top HUD
    composeTestRule.onNodeWithTag("hud_position_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hud_lap_counter").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hud_camera_toggle").assertIsDisplayed()
    composeTestRule.onNodeWithTag("hud_speed_value").assertIsDisplayed()

    // Pre-Grid Launch Button
    composeTestRule.onNodeWithTag("launch_race_countdown_btn").assertIsDisplayed()
    composeTestRule.onNodeWithText("START RACE • LIGHTS OUT").assertIsDisplayed()

    // Test back button
    composeTestRule.onNodeWithTag("racing_back_button").performClick()
    assertTrue(navigatedBack)
  }

  @Test
  fun `in-game driving controls render steer, gas, brake and nitro buttons`() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val engine = InteractiveRacingEngine(context, vehicle, track)

    composeTestRule.setContent {
      InGameDrivingControls(
        engine = engine,
        hudState = engine.hudState.value
      )
    }

    // Steering buttons
    composeTestRule.onNodeWithTag("steer_left_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("steer_right_btn").assertIsDisplayed()

    // Pedals
    composeTestRule.onNodeWithTag("brake_pedal_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gas_pedal_btn").assertIsDisplayed()

    // Nitro button
    composeTestRule.onNodeWithTag("nitro_boost_btn").assertIsDisplayed()
  }

  @Test
  fun `start lights overlay renders countdown sequence`() {
    composeTestRule.setContent {
      StartLightsOverlay(lightsCount = 3)
    }

    composeTestRule.onNodeWithText("GRID START SEQUENCE").assertIsDisplayed()
    composeTestRule.onNodeWithText("HOLD REVS • AWAIT GREEN").assertIsDisplayed()
  }

  @Test
  fun `post-race podium dialog renders trophies, stats and room save verification`() {
    var raceAgainTriggered = false
    var profileTriggered = false

    val summary = RaceFinishSummary(
      finishingPosition = 1,
      totalRacers = 8,
      totalTimeMs = 156400L,
      bestLapMs = 78200L,
      topSpeedKmh = 338.4f,
      overtakesCount = 5,
      driftScore = 840,
      xpEarned = 1500,
      creditsEarned = 6000,
      trackName = track.name,
      vehicleName = vehicle.name,
      isNewPersonalBest = true,
      newlyUnlockedBadges = listOf("Speed Demon", "Apex Predator")
    )

    composeTestRule.setContent {
      PostRacePodiumDialog(
        summary = summary,
        onRaceAgain = { raceAgainTriggered = true },
        onViewProfile = { profileTriggered = true },
        onExit = {},
        isMph = false
      )
    }

    // Champion Title & Stats
    composeTestRule.onNodeWithText("VICTORY! P1 CHAMPION").assertIsDisplayed()
    composeTestRule.onNodeWithText("SAVED TO ROOM DATABASE TELEMETRY").assertIsDisplayed()
    composeTestRule.onNodeWithText("P1 of 8").assertIsDisplayed()
    composeTestRule.onNodeWithText("+1500 XP").assertIsDisplayed()

    // Unlocked awards notification
    composeTestRule.onNodeWithText("NEW TROPHIES UNLOCKED").assertIsDisplayed()

    // Test actions
    composeTestRule.onNodeWithTag("race_again_button").performClick()
    assertTrue(raceAgainTriggered)
  }
}
