package com.example.model

import java.util.Locale

/**
 * Data model for a circuit corner / turn telemetry profile.
 */
data class CornerTelemetryPoint(
  val turnNumber: Int,
  val turnName: String,
  val sector: Int, // 1, 2, or 3
  val trackProgress: Float, // 0.0 .. 1.0 along the circuit
  val recommendedGear: Int,
  val targetApexSpeedKmh: Float,
  val entrySpeedKmh: Float,
  val exitSpeedKmh: Float,
  val brakingDistanceMeters: Int,
  val peakLateralG: Float,
  val cornerType: String, // "Heavy Braking Chicane", "High-Speed Sweeper", "Hairpin", "Esses"
  val coachAdvice: String
) {
  val formattedApexSpeedKmh: String
    get() = String.format(Locale.US, "%.0f KM/H", targetApexSpeedKmh)

  val formattedApexSpeedMph: String
    get() = String.format(Locale.US, "%.0f MPH", targetApexSpeedKmh * 0.621371f)

  val formattedBraking: String
    get() = "${brakingDistanceMeters}m"
}

/**
 * Circuit-specific corner telemetry profiles for premier race tracks.
 */
object CircuitCornerData {

  val MONZA_CORNERS = listOf(
    CornerTelemetryPoint(
      turnNumber = 1,
      turnName = "Variante del Rettifilo (T1-T2)",
      sector = 1,
      trackProgress = 0.16f,
      recommendedGear = 2,
      targetApexSpeedKmh = 78f,
      entrySpeedKmh = 345f,
      exitSpeedKmh = 145f,
      brakingDistanceMeters = 135,
      peakLateralG = 2.45f,
      cornerType = "Heavy Braking Chicane",
      coachAdvice = "Brake hard at the 140m board. Attack the left curb gently and prioritize early throttle on exit."
    ),
    CornerTelemetryPoint(
      turnNumber = 3,
      turnName = "Curva Grande (Biassono)",
      sector = 1,
      trackProgress = 0.28f,
      recommendedGear = 7,
      targetApexSpeedKmh = 295f,
      entrySpeedKmh = 285f,
      exitSpeedKmh = 310f,
      brakingDistanceMeters = 0,
      peakLateralG = 3.65f,
      cornerType = "High-Speed Sweeper",
      coachAdvice = "Flat out in 7th gear. Hug the inside line tightly to minimize the racing distance."
    ),
    CornerTelemetryPoint(
      turnNumber = 4,
      turnName = "Variante della Roggia (T4-T5)",
      sector = 2,
      trackProgress = 0.45f,
      recommendedGear = 3,
      targetApexSpeedKmh = 118f,
      entrySpeedKmh = 328f,
      exitSpeedKmh = 195f,
      brakingDistanceMeters = 110,
      peakLateralG = 2.85f,
      cornerType = "Technical Chicane",
      coachAdvice = "Brake at the 100m marker. Trail-brake into turn 4, flick right, and watch traction control on exit."
    ),
    CornerTelemetryPoint(
      turnNumber = 6,
      turnName = "Curva di Lesmo 1 & 2 (T6-T7)",
      sector = 2,
      trackProgress = 0.58f,
      recommendedGear = 4,
      targetApexSpeedKmh = 168f,
      entrySpeedKmh = 260f,
      exitSpeedKmh = 230f,
      brakingDistanceMeters = 75,
      peakLateralG = 3.20f,
      cornerType = "Double Right Apex",
      coachAdvice = "Commit high speed through Lesmo 1, dab the brakes lightly, and carry maximum momentum onto the Serraglio straight."
    ),
    CornerTelemetryPoint(
      turnNumber = 8,
      turnName = "Variante Ascari (T8-T10)",
      sector = 3,
      trackProgress = 0.76f,
      recommendedGear = 4,
      targetApexSpeedKmh = 195f,
      entrySpeedKmh = 332f,
      exitSpeedKmh = 245f,
      brakingDistanceMeters = 105,
      peakLateralG = 3.75f,
      cornerType = "High-Speed Complex",
      coachAdvice = "Key sector! Brake firmly at 110m, aggressive left turn-in, dance across the right curb, and stay flat through exit."
    ),
    CornerTelemetryPoint(
      turnNumber = 11,
      turnName = "Curva Parabolica (Alboreto)",
      sector = 3,
      trackProgress = 0.92f,
      recommendedGear = 5,
      targetApexSpeedKmh = 215f,
      entrySpeedKmh = 338f,
      exitSpeedKmh = 280f,
      brakingDistanceMeters = 90,
      peakLateralG = 3.40f,
      cornerType = "Long Expanding Sweeper",
      coachAdvice = "Late apex! Turn in after the halfway mark, drift out to the exit curb, and open DRS early down the main straight."
    )
  )

  val SUZUKA_CORNERS = listOf(
    CornerTelemetryPoint(
      turnNumber = 1,
      turnName = "First Corner & Turn 2",
      sector = 1,
      trackProgress = 0.12f,
      recommendedGear = 5,
      targetApexSpeedKmh = 230f,
      entrySpeedKmh = 315f,
      exitSpeedKmh = 155f,
      brakingDistanceMeters = 80,
      peakLateralG = 3.10f,
      cornerType = "Decreasing Radius Right",
      coachAdvice = "Downshift from 7th to 5th smoothly while leaning into Turn 1, then hard braking for Turn 2 hairpin exit."
    ),
    CornerTelemetryPoint(
      turnNumber = 3,
      turnName = "The S-Curves & Dunlop",
      sector = 1,
      trackProgress = 0.28f,
      recommendedGear = 4,
      targetApexSpeedKmh = 190f,
      entrySpeedKmh = 225f,
      exitSpeedKmh = 210f,
      brakingDistanceMeters = 35,
      peakLateralG = 3.85f,
      cornerType = "Flowing Esses Rhythm",
      coachAdvice = "Rhythm is king here. Smooth steering inputs, delicate weight transfer, avoid hitting curbs too heavily."
    ),
    CornerTelemetryPoint(
      turnNumber = 8,
      turnName = "Degner 1 & Degner 2",
      sector = 2,
      trackProgress = 0.48f,
      recommendedGear = 3,
      targetApexSpeedKmh = 142f,
      entrySpeedKmh = 265f,
      exitSpeedKmh = 185f,
      brakingDistanceMeters = 65,
      peakLateralG = 3.45f,
      cornerType = "High-Risk Fast Rights",
      coachAdvice = "Carry maximum speed into Degner 1 with a quick brake tap. Hard on the brakes before Degner 2's harsh exit curb."
    ),
    CornerTelemetryPoint(
      turnNumber = 11,
      turnName = "Hairpin (Turn 11)",
      sector = 2,
      trackProgress = 0.62f,
      recommendedGear = 2,
      targetApexSpeedKmh = 68f,
      entrySpeedKmh = 235f,
      exitSpeedKmh = 135f,
      brakingDistanceMeters = 95,
      peakLateralG = 2.15f,
      cornerType = "Tight Hairpin",
      coachAdvice = "Trail brake deep towards the apex. Square off the corner for the fastest launch down towards Spoon."
    ),
    CornerTelemetryPoint(
      turnNumber = 13,
      turnName = "Spoon Curve (T13-T14)",
      sector = 2,
      trackProgress = 0.74f,
      recommendedGear = 4,
      targetApexSpeedKmh = 175f,
      entrySpeedKmh = 270f,
      exitSpeedKmh = 220f,
      brakingDistanceMeters = 85,
      peakLateralG = 3.35f,
      cornerType = "Double Left Apex",
      coachAdvice = "Sacrifice the first apex to position the vehicle for early full throttle out of the second apex."
    ),
    CornerTelemetryPoint(
      turnNumber = 15,
      turnName = "130R Super-Speedway",
      sector = 3,
      trackProgress = 0.88f,
      recommendedGear = 7,
      targetApexSpeedKmh = 308f,
      entrySpeedKmh = 318f,
      exitSpeedKmh = 312f,
      brakingDistanceMeters = 0,
      peakLateralG = 4.10f,
      cornerType = "Legendary Flat-Out Left",
      coachAdvice = "Keep the throttle pinned 100%! Trust aerodynamic downforce and hold the car tight along the apex line."
    ),
    CornerTelemetryPoint(
      turnNumber = 16,
      turnName = "Casio Triangle Chicane",
      sector = 3,
      trackProgress = 0.95f,
      recommendedGear = 2,
      targetApexSpeedKmh = 65f,
      entrySpeedKmh = 315f,
      exitSpeedKmh = 140f,
      brakingDistanceMeters = 130,
      peakLateralG = 2.50f,
      cornerType = "Final Stop Chicane",
      coachAdvice = "Extreme braking point at 120m. Mount the kerbs diagonally to slingshot down the pit straight."
    )
  )

  fun getCornersForTrack(trackName: String): List<CornerTelemetryPoint> {
    return when {
      trackName.contains("Suzuka", ignoreCase = true) -> SUZUKA_CORNERS
      else -> MONZA_CORNERS
    }
  }
}

/**
 * Side-by-side Telemetry Comparison Model.
 */
data class TelemetryComparisonPoint(
  val trackProgress: Float, // 0..1
  val distanceMeters: Float,
  val runASpeedKmh: Float,
  val runBSpeedKmh: Float,
  val runAThrottle: Float,
  val runBThrottle: Float,
  val runABrake: Float,
  val runBBrake: Float,
  val timeDeltaMs: Long // Positive: Run A is ahead, Negative: Run B is ahead
)

/**
 * Summary breakdown of time gained/lost across sectors.
 */
data class SectorDeltaSummary(
  val sector: Int,
  val runATimeMs: Long,
  val runBTimeMs: Long,
  val deltaMs: Long,
  val isRunAFaster: Boolean
) {
  val formattedDelta: String
    get() {
      val sign = if (deltaMs < 0) "-" else "+"
      val absMs = Math.abs(deltaMs)
      val sec = absMs / 1000L
      val millis = absMs % 1000L
      return String.format(Locale.US, "%s%d.%03ds", sign, sec, millis)
    }
}
