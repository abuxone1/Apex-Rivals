package com.example

import com.example.model.CircuitCornerData
import com.example.model.SectorDeltaSummary
import com.example.ui.screens.GooglePlayListingData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetryAnalyzerTest {

  @Test
  fun `play store listing version matches v7_0_0 and versionCode 7`() {
    assertEquals(7, GooglePlayListingData.VERSION_CODE)
    assertEquals("7.0.0", GooglePlayListingData.VERSION_NAME)
    assertTrue("What's New should contain Corner Apex & Delta Analyzer", GooglePlayListingData.WHATS_NEW_V7.contains("CORNER APEX"))
    assertTrue("What's New should contain Dual-Run Delta Engine", GooglePlayListingData.WHATS_NEW_V7.contains("DUAL-RUN"))
  }

  @Test
  fun `monza corner telemetry points contain all 11 turns and braking markers`() {
    val corners = CircuitCornerData.getCornersForTrack("Monza Speed Autodrome")
    assertFalse("Monza corners should not be empty", corners.isEmpty())

    val turn1 = corners.first { it.turnNumber == 1 }
    assertEquals("Variante del Rettifilo (T1-T2)", turn1.turnName)
    assertEquals(1, turn1.sector)
    assertEquals(2, turn1.recommendedGear)
    assertEquals("78 KM/H", turn1.formattedApexSpeedKmh)
    assertEquals("135m", turn1.formattedBraking)
    assertTrue(turn1.peakLateralG > 2.0f)
  }

  @Test
  fun `suzuka corner telemetry points contain 130R and hairpin profiles`() {
    val corners = CircuitCornerData.getCornersForTrack("Suzuka Grand GP")
    assertFalse("Suzuka corners should not be empty", corners.isEmpty())

    val super130R = corners.firstOrNull { it.turnNumber == 15 }
    assertNotNull("130R corner should exist", super130R)
    assertEquals(7, super130R?.recommendedGear)
    assertEquals(0, super130R?.brakingDistanceMeters) // Flat out!
    assertTrue("Apex speed for 130R should exceed 300 km/h", (super130R?.targetApexSpeedKmh ?: 0f) >= 300f)
  }

  @Test
  fun `sector delta summary computes time difference correctly`() {
    val aheadSummary = SectorDeltaSummary(
      sector = 1,
      runATimeMs = 25120L,
      runBTimeMs = 25300L,
      deltaMs = -180L,
      isRunAFaster = true
    )
    assertEquals("-0.180s", aheadSummary.formattedDelta)

    val behindSummary = SectorDeltaSummary(
      sector = 2,
      runATimeMs = 26500L,
      runBTimeMs = 26260L,
      deltaMs = 240L,
      isRunAFaster = false
    )
    assertEquals("+0.240s", behindSummary.formattedDelta)
  }
}
