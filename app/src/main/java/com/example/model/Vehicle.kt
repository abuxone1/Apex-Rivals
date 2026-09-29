package com.example.model

enum class VehicleType {
  CAR,
  MOTORBIKE
}

enum class PlatformType(val displayName: String, val shortCode: String) {
  PC("Steam / PC", "PC"),
  PLAYSTATION("PlayStation 5", "PS5"),
  XBOX("Xbox Series X", "XBOX"),
  IOS("iOS", "iOS"),
  ANDROID("Android", "AND")
}

data class Vehicle(
  val id: String,
  val name: String,
  val type: VehicleType,
  val category: String, // e.g. "GT3", "Hypercar", "MotoGP 1000cc", "Supernaked"
  val horsepower: Int,
  val topSpeedKmh: Int,
  val weightKg: Int,
  val maxRpm: Int,
  val idleRpm: Int = 1200,
  val redlineRpm: Int,
  val gears: Int = 6,
  val maxLeanAngleDeg: Float = 0f, // > 0 for Motorbikes (e.g. 64°)
  val acceleration0to100: Float, // seconds
  val peakCorneringG: Float,
  val description: String,
  val tireCompound: String = "Soft Slick",
  val aeroDownforce: Int = 75, // percentage
  val brakeBiasFront: Int = 58 // percentage
)

val AVAILABLE_VEHICLES = listOf(
  Vehicle(
    id = "car_gt3_apex",
    name = "Apex GT3-R Twin-Turbo",
    type = VehicleType.CAR,
    category = "GT3 Racing",
    horsepower = 620,
    topSpeedKmh = 318,
    weightKg = 1240,
    maxRpm = 9200,
    redlineRpm = 8500,
    gears = 6,
    maxLeanAngleDeg = 0f,
    acceleration0to100 = 2.7f,
    peakCorneringG = 2.4f,
    description = "Twin-turbo V8 racecar optimized for high-downforce cornering stability and precision apex tracking."
  ),
  Vehicle(
    id = "bike_superbike_1000",
    name = "Pulse V4R Superbike",
    type = VehicleType.MOTORBIKE,
    category = "1000cc Superbike",
    horsepower = 224,
    topSpeedKmh = 332,
    weightKg = 172,
    maxRpm = 16500,
    redlineRpm = 15200,
    gears = 6,
    maxLeanAngleDeg = 64f,
    acceleration0to100 = 2.4f,
    peakCorneringG = 1.9f,
    description = "Ultra-lightweight liter-class track missile. Features extreme 64° MotoGP lean angle and active anti-wheelie aerodynamics."
  ),
  Vehicle(
    id = "car_formula_apex",
    name = "Formula Apex Hybrid",
    type = VehicleType.CAR,
    category = "Formula Single-Seater",
    horsepower = 1010,
    topSpeedKmh = 355,
    weightKg = 798,
    maxRpm = 12500,
    redlineRpm = 11800,
    gears = 8,
    maxLeanAngleDeg = 0f,
    acceleration0to100 = 1.9f,
    peakCorneringG = 3.6f,
    description = "Pinnacle open-wheel racecar with ground-effect venturi tunnels and DRS speed boosts."
  ),
  Vehicle(
    id = "bike_track_890",
    name = "Torque 890 R Naked",
    type = VehicleType.MOTORBIKE,
    category = "Middleweight Track",
    horsepower = 128,
    topSpeedKmh = 262,
    weightKg = 166,
    maxRpm = 11500,
    redlineRpm = 10500,
    gears = 6,
    maxLeanAngleDeg = 58f,
    acceleration0to100 = 3.1f,
    peakCorneringG = 1.7f,
    description = "Razor-sharp twin-cylinder with explosive midrange torque for tight chicane transitions."
  )
)
