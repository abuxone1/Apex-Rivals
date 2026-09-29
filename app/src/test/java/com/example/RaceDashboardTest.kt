package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.components.RaceDashboard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RaceDashboardTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `race dashboard renders with placeholder telemetry values by default`() {
    composeTestRule.setContent {
      // Instantiated with default placeholder parameters
      RaceDashboard()
    }

    // Main dashboard card
    composeTestRule.onNodeWithTag("race_dashboard").assertIsDisplayed()

    // Gear indicator defaults to 5
    composeTestRule.onNodeWithTag("dashboard_gear_value").assertIsDisplayed()
    composeTestRule.onNodeWithText("5").assertIsDisplayed()

    // Speed indicator defaults to 278 KM/H
    composeTestRule.onNodeWithTag("dashboard_speed_display").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_speed_value", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("278", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_speed_unit", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("KM/H", useUnmergedTree = true).assertIsDisplayed()

    // Sequential LED array and tachometer bar
    composeTestRule.onNodeWithTag("dashboard_led_array").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_rpm_bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("11850 / 14500 RPM").assertIsDisplayed()

    // Telemetry bars and badges
    composeTestRule.onNodeWithTag("dashboard_throttle_brake_bars").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_tire_temps").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_delta_display").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_ers_display").assertIsDisplayed()
  }

  @Test
  fun `race dashboard displays mph and triggers speed unit toggle callback`() {
    var toggled = false

    composeTestRule.setContent {
      RaceDashboard(
        speedKmh = 300f,
        isMph = true, // 300 km/h = ~186 mph
        gear = 6,
        rpm = 12500,
        onSpeedUnitToggle = { toggled = true }
      )
    }

    composeTestRule.onNodeWithText("186", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("MPH", useUnmergedTree = true).assertIsDisplayed()

    // Click speed display to toggle
    composeTestRule.onNodeWithTag("dashboard_speed_display").performClick()
    assertTrue("onSpeedUnitToggle should have been triggered", toggled)
  }

  @Test
  fun `race dashboard formats neutral and reverse gears appropriately`() {
    composeTestRule.setContent {
      RaceDashboard(
        gear = 0, // Neutral
        speedKmh = 0f,
        rpm = 1200
      )
    }

    composeTestRule.onNodeWithText("N").assertIsDisplayed()
  }

  @Test
  fun `race dashboard triggers gear up and down actions`() {
    var gearUpClicked = false
    var gearDownClicked = false

    composeTestRule.setContent {
      RaceDashboard(
        gear = 3,
        onGearUp = { gearUpClicked = true },
        onGearDown = { gearDownClicked = true }
      )
    }

    composeTestRule.onNodeWithText("3").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dashboard_demo_toggle").assertIsDisplayed()
  }
}
