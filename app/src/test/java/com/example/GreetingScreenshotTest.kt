package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.model.PlatformType
import com.example.model.VehicleType
import com.example.ui.components.EsportsShareCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun esports_share_card_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        EsportsShareCard(
          playerName = "Apex_GhostRacer",
          platform = PlatformType.PC,
          vehicleName = "Apex GT3-R Twin-Turbo",
          vehicleType = VehicleType.CAR,
          trackName = "Autodromo Nazionale Monza",
          lapTimeMs = 82410L,
          topSpeedKmh = 318.5f,
          peakG = 2.45f,
          challengeCode = "APX-GT3-2026",
          onShareClick = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
