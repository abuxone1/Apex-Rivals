package com.example.util

import com.example.data.local.PerformanceMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceMetricsCsvExporterTest {

  @Test
  fun generateCsvString_containsStandardHeaderRow() {
    val result = PerformanceMetricsCsvExporter.generateCsvString(emptyList())
    val lines = result.trim().lines()
    assertEquals(1, lines.size)
    assertTrue(lines[0].contains("Session ID"))
    assertTrue(lines[0].contains("Driver Name"))
    assertTrue(lines[0].contains("Track Name"))
    assertTrue(lines[0].contains("Vehicle Name"))
    assertTrue(lines[0].contains("Lap Time (ms)"))
    assertTrue(lines[0].contains("Avg Speed (km/h)"))
  }

  @Test
  fun generateCsvString_escapesAndFormatsMetricsProperly() {
    val sample = PerformanceMetrics(
      id = 101,
      driverName = "Max, The Quick",
      trackName = "Monza \"GP\"",
      vehicleName = "F1-75",
      raceType = "Circuit",
      lapTimeMs = 78420L,
      topSpeedKmh = 345.2f,
      zeroToHundredKmhSeconds = 2.45f,
      peakAccelerationG = 4.2f,
      maxLateralG = 3.8f,
      weatherCondition = "Dry / Sunny",
      trackComplexity = "High Speed",
      tags = listOf("Setup:LowDownforce", "P-Zero Soft"),
      timestamp = 1710000000000L
    )

    val csv = PerformanceMetricsCsvExporter.generateCsvString(listOf(sample))
    val lines = csv.trim().lines()
    assertEquals(2, lines.size)

    val dataLine = lines[1]
    // Driver name with comma should be escaped in quotes
    assertTrue(dataLine.contains("\"Max, The Quick\""))
    // Track name with quotes should be escaped
    assertTrue(dataLine.contains("\"Monza \"\"GP\"\"\""))
    // Formatted lap time
    assertTrue(dataLine.contains("78420"))
    assertTrue(dataLine.contains("1:18.420"))
    // Top speed
    assertTrue(dataLine.contains("345.2"))
  }
}
