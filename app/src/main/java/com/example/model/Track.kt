package com.example.model

data class TrackPoint(
  val x: Float, // normalized 0..1
  val y: Float, // normalized 0..1
  val speedSuggestedKmh: Float,
  val sector: Int,
  val turnName: String? = null
)

data class Track(
  val id: String,
  val name: String,
  val location: String,
  val totalLengthMeters: Int,
  val turns: Int,
  val sector1EndProgress: Float = 0.33f,
  val sector2EndProgress: Float = 0.68f,
  val referenceLapTimeMs: Long,
  val pathPoints: List<TrackPoint>
)

val TRACK_MONZA_APEX = Track(
  id = "track_monza",
  name = "Monza Speed Autodrome",
  location = "Monza, Italy",
  totalLengthMeters = 5793,
  turns = 11,
  sector1EndProgress = 0.32f,
  sector2EndProgress = 0.67f,
  referenceLapTimeMs = 78400L, // 1:18.400
  pathPoints = listOf(
    TrackPoint(0.15f, 0.85f, 320f, 1, "Main Straight"),
    TrackPoint(0.40f, 0.85f, 335f, 1),
    TrackPoint(0.65f, 0.85f, 310f, 1, "Prima Variante Braking"),
    TrackPoint(0.72f, 0.78f, 95f, 1, "T1 Chicane Right"),
    TrackPoint(0.70f, 0.70f, 110f, 1, "T2 Chicane Left"),
    TrackPoint(0.75f, 0.58f, 240f, 1, "Curva Grande"),
    TrackPoint(0.85f, 0.45f, 260f, 1),
    TrackPoint(0.85f, 0.32f, 130f, 2, "Variante della Roggia"),
    TrackPoint(0.80f, 0.28f, 140f, 2),
    TrackPoint(0.70f, 0.25f, 210f, 2, "Lesmo 1"),
    TrackPoint(0.55f, 0.20f, 180f, 2, "Lesmo 2"),
    TrackPoint(0.35f, 0.22f, 290f, 2, "Curva del Serraglio"),
    TrackPoint(0.20f, 0.30f, 160f, 3, "Variante Ascari 1"),
    TrackPoint(0.18f, 0.42f, 190f, 3, "Variante Ascari 2"),
    TrackPoint(0.15f, 0.55f, 280f, 3, "Back Straight"),
    TrackPoint(0.12f, 0.72f, 215f, 3, "Curva Parabolica Apex"),
    TrackPoint(0.15f, 0.85f, 320f, 3, "Start / Finish")
  )
)

val TRACK_SUZUKA_APEX = Track(
  id = "track_suzuka",
  name = "Suzuka Grand GP",
  location = "Mie Prefecture, Japan",
  totalLengthMeters = 5807,
  turns = 18,
  sector1EndProgress = 0.35f,
  sector2EndProgress = 0.70f,
  referenceLapTimeMs = 89200L, // 1:29.200
  pathPoints = listOf(
    TrackPoint(0.20f, 0.80f, 290f, 1, "Start Line"),
    TrackPoint(0.35f, 0.80f, 180f, 1, "First Curve"),
    TrackPoint(0.48f, 0.72f, 170f, 1, "S-Curves 1"),
    TrackPoint(0.42f, 0.62f, 175f, 1, "S-Curves 2"),
    TrackPoint(0.50f, 0.52f, 165f, 1, "Dunlop Curve"),
    TrackPoint(0.65f, 0.48f, 140f, 2, "Degner 1"),
    TrackPoint(0.72f, 0.42f, 110f, 2, "Degner 2"),
    TrackPoint(0.70f, 0.32f, 85f, 2, "Hairpin Corner"),
    TrackPoint(0.60f, 0.25f, 240f, 2, "Spoon Curve Entry"),
    TrackPoint(0.45f, 0.20f, 190f, 2, "Spoon Apex"),
    TrackPoint(0.25f, 0.35f, 310f, 3, "Back Straight 130R"),
    TrackPoint(0.15f, 0.52f, 295f, 3, "130R Apex"),
    TrackPoint(0.12f, 0.68f, 75f, 3, "Casio Triangle Chicane"),
    TrackPoint(0.20f, 0.80f, 290f, 3, "Final Straight")
  )
)

val TRACK_TOKYO_EXPRESSWAY = Track(
  id = "track_tokyo",
  name = "Tokyo Midnight Expressway",
  location = "Tokyo, Japan (Night)",
  totalLengthMeters = 6420,
  turns = 14,
  sector1EndProgress = 0.30f,
  sector2EndProgress = 0.65f,
  referenceLapTimeMs = 94100L, // 1:34.100
  pathPoints = listOf(
    TrackPoint(0.15f, 0.85f, 330f, 1, "Rainbow Bridge"),
    TrackPoint(0.45f, 0.85f, 340f, 1),
    TrackPoint(0.75f, 0.80f, 210f, 1, "Shibaura Ramp"),
    TrackPoint(0.85f, 0.65f, 160f, 1, "Tunnel Entry"),
    TrackPoint(0.82f, 0.45f, 250f, 2, "Underground Tunnel"),
    TrackPoint(0.70f, 0.30f, 190f, 2, "Ginza Curve"),
    TrackPoint(0.50f, 0.25f, 280f, 2, "Elevated Viaduct"),
    TrackPoint(0.30f, 0.20f, 140f, 2, "Harumi Hairpin"),
    TrackPoint(0.18f, 0.35f, 270f, 3, "Bayside Link"),
    TrackPoint(0.12f, 0.60f, 220f, 3, "Odaiba Chicane"),
    TrackPoint(0.15f, 0.85f, 330f, 3, "Finish Line")
  )
)

val AVAILABLE_TRACKS = listOf(
  TRACK_MONZA_APEX,
  TRACK_SUZUKA_APEX,
  TRACK_TOKYO_EXPRESSWAY
)
