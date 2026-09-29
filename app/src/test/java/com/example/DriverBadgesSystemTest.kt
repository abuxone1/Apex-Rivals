package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.local.PerformanceMetrics
import com.example.model.BadgeCategory
import com.example.model.BadgeEvaluator
import com.example.ui.components.DriverBadgesSection
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
class DriverBadgesSystemTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `speed demon unlocks when top speed meets or exceeds 320 kmh`() {
    val lowSpeedRun = listOf(
      PerformanceMetrics(
        raceId = "r1",
        driverName = "Apex Pilot",
        trackName = "Monza GP",
        vehicleName = "Apex GT3-R",
        topSpeedKmh = 295.0f,
        lapTimeMs = 82000L
      )
    )

    val lockedBadges = BadgeEvaluator.evaluateBadges(lowSpeedRun, isMph = false)
    val lockedSpeedDemon = lockedBadges.firstOrNull { it.id == "speed_demon" }
    assertNotNull(lockedSpeedDemon)
    assertFalse("Speed Demon should be locked under 320 km/h", lockedSpeedDemon!!.isUnlocked)
    assertTrue("Progress should be proportional", lockedSpeedDemon.progress in 0.90f..0.95f)

    // Add high-speed run >= 320 km/h
    val highSpeedRun = lowSpeedRun + PerformanceMetrics(
      raceId = "r2",
      driverName = "Apex Pilot",
      trackName = "Spa-Francorchamps",
      vehicleName = "Formula Apex Hybrid",
      topSpeedKmh = 344.2f,
      lapTimeMs = 103250L
    )

    val unlockedBadges = BadgeEvaluator.evaluateBadges(highSpeedRun, isMph = false)
    val unlockedSpeedDemon = unlockedBadges.firstOrNull { it.id == "speed_demon" }
    assertNotNull(unlockedSpeedDemon)
    assertTrue("Speed Demon should unlock when topSpeed >= 320 km/h", unlockedSpeedDemon!!.isUnlocked)
    assertEquals(1.0f, unlockedSpeedDemon.progress, 0.001f)
    assertNotNull(unlockedSpeedDemon.unlockedDetail)
  }

  @Test
  fun `consistency king unlocks when 5 or more telemetry sessions are stored in Room`() {
    val threeRuns = (1..3).map { i ->
      PerformanceMetrics(
        raceId = "run_$i",
        driverName = "Apex Pilot",
        trackName = "Monza GP",
        vehicleName = "Apex GT3-R",
        lapTimeMs = 79000L + (i * 100),
        topSpeedKmh = 310f,
        trackDistanceMeters = 5793
      )
    }

    val badges3 = BadgeEvaluator.evaluateBadges(threeRuns)
    val consistency3 = badges3.firstOrNull { it.id == "consistency_king" }
    assertNotNull(consistency3)
    assertFalse("Consistency King should be locked with only 3 sessions", consistency3!!.isUnlocked)
    assertEquals(0.6f, consistency3.progress, 0.001f)
    assertEquals("3 / 5 Sessions", consistency3.progressText)

    val fiveRuns = threeRuns + (4..5).map { i ->
      PerformanceMetrics(
        raceId = "run_$i",
        driverName = "Apex Pilot",
        trackName = "Monza GP",
        vehicleName = "Apex GT3-R",
        lapTimeMs = 79200L,
        topSpeedKmh = 312f,
        trackDistanceMeters = 5793
      )
    }

    val badges5 = BadgeEvaluator.evaluateBadges(fiveRuns)
    val consistency5 = badges5.firstOrNull { it.id == "consistency_king" }
    assertNotNull(consistency5)
    assertTrue("Consistency King should unlock with 5 sessions", consistency5!!.isUnlocked)
    assertEquals(1.0f, consistency5.progress, 0.001f)
    assertEquals("5 / 5 Sessions", consistency5.progressText)
  }

  @Test
  fun `rainmaster and g-force monster evaluate correctly from telemetry metrics`() {
    val metrics = listOf(
      PerformanceMetrics(
        raceId = "run_dry",
        trackName = "Monza GP",
        vehicleName = "Apex GT3-R",
        maxLateralG = 2.45f,
        weatherCondition = "Dry Asphalt",
        lapTimeMs = 78400L
      ),
      PerformanceMetrics(
        raceId = "run_wet",
        trackName = "Spa-Francorchamps",
        vehicleName = "Formula Apex Hybrid",
        maxLateralG = 3.85f,
        weatherCondition = "Wet Surface",
        lapTimeMs = 103250L
      )
    )

    val badges = BadgeEvaluator.evaluateBadges(metrics)

    val rainmaster = badges.first { it.id == "rainmaster" }
    assertTrue("Rainmaster should unlock with Wet Surface run", rainmaster.isUnlocked)

    val gForceMonster = badges.first { it.id == "g_force_monster" }
    assertTrue("G-Force Monster should unlock when lateral G exceeds 3.0G", gForceMonster.isUnlocked)
    assertEquals(1.0f, gForceMonster.progress, 0.001f)
  }

  @Test
  fun `driver badges section renders counter, filters and opens detail dialog`() {
    val sampleMetrics = listOf(
      PerformanceMetrics(
        raceId = "m1",
        driverName = "Apex Pilot",
        trackName = "Monza GP",
        vehicleName = "Apex GT3-R Twin-Turbo",
        topSpeedKmh = 330.0f, // unlocks Speed Demon
        maxLateralG = 3.2f, // unlocks G-Force Monster
        lapTimeMs = 78000L, // unlocks Apex Predator (<80s)
        zeroToHundredKmhSeconds = 2.4f, // unlocks 0-100 (<2.5s)
        trackDistanceMeters = 5793,
        weatherCondition = "Dry Asphalt"
      ),
      PerformanceMetrics(
        raceId = "m2",
        driverName = "Apex Pilot",
        trackName = "Spa-Francorchamps",
        vehicleName = "Formula Apex Hybrid",
        topSpeedKmh = 345.0f,
        maxLateralG = 3.9f,
        lapTimeMs = 104000L,
        zeroToHundredKmhSeconds = 2.1f,
        trackDistanceMeters = 7004,
        weatherCondition = "Wet Surface" // unlocks Rainmaster
      )
    )

    val badges = BadgeEvaluator.evaluateBadges(sampleMetrics)

    composeTestRule.setContent {
      DriverBadgesSection(badges = badges)
    }

    // Section title and Room DB subtitle
    composeTestRule.onNodeWithText("DRIVER AWARDS & BADGES").assertIsDisplayed()
    composeTestRule.onNodeWithText("ROOM DATABASE TELEMETRY MILESTONES").assertIsDisplayed()

    // Unlocked counter chip
    composeTestRule.onNodeWithTag("badges_unlocked_counter").assertIsDisplayed()

    // Category filter chips
    composeTestRule.onNodeWithTag("badge_filter_all").assertIsDisplayed()
    composeTestRule.onNodeWithTag("badge_filter_speed").assertIsDisplayed()
    composeTestRule.onNodeWithTag("badge_filter_consistency").assertIsDisplayed()

    // Speed Demon card
    composeTestRule.onNodeWithTag("badge_item_speed_demon").assertIsDisplayed()
    composeTestRule.onNodeWithText("Speed Demon").assertIsDisplayed()

    // Consistency King card
    composeTestRule.onNodeWithTag("badge_item_consistency_king").assertIsDisplayed()
    composeTestRule.onNodeWithText("Consistency King").assertIsDisplayed()

    // Click on Speed Demon to open detail dialog
    composeTestRule.onNodeWithTag("badge_item_speed_demon").performClick()
    composeTestRule.onNodeWithText("GOLD AWARD").assertIsDisplayed()
    composeTestRule.onNodeWithText("Done").assertIsDisplayed()

    // Dismiss dialog
    composeTestRule.onNodeWithText("Done").performClick()
  }
}
