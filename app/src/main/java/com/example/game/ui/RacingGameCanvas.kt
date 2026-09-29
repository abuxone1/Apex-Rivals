package com.example.game.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.engine.InteractiveRacingEngine
import com.example.game.model.CameraPerspective
import com.example.game.model.RaceState
import com.example.model.VehicleType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RacingGameCanvas(
  engine: InteractiveRacingEngine,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "gameVisualPulse")
  val pulseAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "pulseAnim"
  )

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .testTag("racing_game_canvas")
  ) {
    val canvasW = size.width
    val canvasH = size.height

    val horizonY = canvasH * 0.44f
    val perspective = engine.cameraPerspective

    // Calculate curve offset based on track distance
    val segments = engine.getRoadSegments()
    val currentSegmentIdx = ((engine.playerTrackDistanceMeters / 60f).toInt()) % segments.size
    val currentSegment = segments[currentSegmentIdx]
    val curveShift = currentSegment.curvature * (canvasW * 0.22f)

    // 1. SKY & DISTANT HORIZON
    drawSkyAndEnvironment(canvasW, canvasH, horizonY, curveShift, engine.playerLaneX)

    // 2. 3D PROJECTED ASPHALT ROAD & RUMBLE STRIPS
    drawPerspectiveRoad(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      playerDistance = engine.playerTrackDistanceMeters,
      playerLaneX = engine.playerLaneX,
      curveShift = curveShift,
      currentSegment = currentSegment,
      isBrakingZone = currentSegment.isBrakingZone
    )

    // 3. TRACKSIDE GANTRY & DISTANCE BOARDS
    drawTracksideAssets(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      playerDistance = engine.playerTrackDistanceMeters,
      playerLaneX = engine.playerLaneX,
      curveShift = curveShift,
      speedKmh = engine.speedKmh
    )

    // 4. RIVAL AI OPPONENTS
    drawRivalVehicles(
      canvasW = canvasW,
      canvasH = canvasH,
      horizonY = horizonY,
      playerDistance = engine.playerTrackDistanceMeters,
      playerLaneX = engine.playerLaneX,
      curveShift = curveShift,
      engine = engine
    )

    // 5. PLAYER VEHICLE BASED ON CAMERA
    when (perspective) {
      CameraPerspective.CHASE_CAM -> {
        drawChaseCamPlayerVehicle(
          canvasW = canvasW,
          canvasH = canvasH,
          playerLaneX = engine.playerLaneX,
          steerInput = engine.steerInput,
          brakeInput = engine.brakeInput,
          isNitroActive = engine.isNitroActive,
          isDrsActive = engine.isDrsActive,
          speedKmh = engine.speedKmh,
          pulse = pulseAnim
        )
      }
      CameraPerspective.COCKPIT_CAM -> {
        drawCockpitCam(
          canvasW = canvasW,
          canvasH = canvasH,
          steerInput = engine.steerInput,
          rpm = engine.rpm,
          gear = engine.gear,
          speedKmh = engine.speedKmh,
          isDrsActive = engine.isDrsActive,
          rivals = engine.getRivalsList(),
          playerDistance = engine.playerTrackDistanceMeters
        )
      }
      CameraPerspective.HOOD_CAM -> {
        drawHoodCamOverlay(
          canvasW = canvasW,
          canvasH = canvasH,
          speedKmh = engine.speedKmh,
          brakeInput = engine.brakeInput,
          pulse = pulseAnim
        )
      }
    }

    // 6. SPEED WARP LINES & NITRO BOOST PARTICLES
    if (engine.isNitroActive || engine.speedKmh > 270f) {
      drawSpeedLines(canvasW, canvasH, horizonY, engine.isNitroActive, pulseAnim)
    }

    // 7. CHECKERED VICTORY CONFETTI
    if (engine.raceState.value == RaceState.FINISHED_PODIUM) {
      drawVictoryConfetti(canvasW, canvasH, pulseAnim)
    }
  }
}

// -------------------------------------------------------------------
// 1. SKY & DISTANT HORIZON
// -------------------------------------------------------------------
private fun DrawScope.drawSkyAndEnvironment(
  w: Float,
  h: Float,
  horizonY: Float,
  curveShift: Float,
  playerLaneX: Float
) {
  // Sky Gradient: Twilight Purple to Deep Midnight Carbon
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(
        Color(0xFF070B16), // Deep Night
        Color(0xFF0F1E36), // Electric Twilight
        Color(0xFF1B3252), // Sky Glow
        Color(0xFF27496D)  // Horizon Mist
      ),
      startY = 0f,
      endY = horizonY
    ),
    topLeft = Offset(0f, 0f),
    size = Size(w, horizonY)
  )

  // Distant Mountains / Circuit Floodlight Silhouettes
  val mountainPath = Path().apply {
    moveTo(0f, horizonY)
    val mountainOffset = -playerLaneX * 40f + curveShift * 0.25f
    lineTo(w * 0.15f + mountainOffset, horizonY - 45f)
    lineTo(w * 0.35f + mountainOffset, horizonY - 20f)
    lineTo(w * 0.55f + mountainOffset, horizonY - 65f)
    lineTo(w * 0.75f + mountainOffset, horizonY - 30f)
    lineTo(w * 0.92f + mountainOffset, horizonY - 50f)
    lineTo(w, horizonY)
    close()
  }
  drawPath(mountainPath, color = Color(0xFF0E1A2C))

  // Grass / Run-off terrain beneath horizon
  drawRect(
    brush = Brush.verticalGradient(
      colors = listOf(
        Color(0xFF142018), // Deep trackside turf
        Color(0xFF0C140E)
      ),
      startY = horizonY,
      endY = h
    ),
    topLeft = Offset(0f, horizonY),
    size = Size(w, h - horizonY)
  )
}

// -------------------------------------------------------------------
// 2. 3D PERSPECTIVE ROAD & RUMBLE STRIPS
// -------------------------------------------------------------------
private fun DrawScope.drawPerspectiveRoad(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  playerDistance: Float,
  playerLaneX: Float,
  curveShift: Float,
  currentSegment: Any,
  isBrakingZone: Boolean
) {
  val roadTopW = canvasW * 0.18f
  val roadBottomW = canvasW * 0.92f

  val vanishingX = (canvasW * 0.5f) + curveShift - (playerLaneX * canvasW * 0.12f)
  val roadCenterX = (canvasW * 0.5f) - (playerLaneX * canvasW * 0.35f)

  val roadTopLeft = vanishingX - (roadTopW * 0.5f)
  val roadTopRight = vanishingX + (roadTopW * 0.5f)
  val roadBottomLeft = roadCenterX - (roadBottomW * 0.5f)
  val roadBottomRight = roadCenterX + (roadBottomW * 0.5f)

  // Asphalt Road Polygon
  val roadPath = Path().apply {
    moveTo(roadTopLeft, horizonY)
    lineTo(roadTopRight, horizonY)
    lineTo(roadBottomRight, canvasH)
    lineTo(roadBottomLeft, canvasH)
    close()
  }
  drawPath(
    roadPath,
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF1E222B), Color(0xFF12141A)),
      startY = horizonY,
      endY = canvasH
    )
  )

  // Rumble Strips (Kerbs) - Left & Right
  val kerbTopW = roadTopW * 0.18f
  val kerbBottomW = roadBottomW * 0.08f

  val numStrips = 18
  val stripeOffset = (playerDistance * 0.25f) % 2f // Animates forward with speed

  for (i in 0 until numStrips) {
    val t0 = i.toFloat() / numStrips
    val t1 = (i + 1).toFloat() / numStrips

    // Perspective depth power
    val y0 = horizonY + (canvasH - horizonY) * (t0 * t0)
    val y1 = horizonY + (canvasH - horizonY) * (t1 * t1)

    val leftX0 = roadTopLeft + (roadBottomLeft - roadTopLeft) * (t0 * t0)
    val leftX1 = roadTopLeft + (roadBottomLeft - roadTopLeft) * (t1 * t1)

    val rightX0 = roadTopRight + (roadBottomRight - roadTopRight) * (t0 * t0)
    val rightX1 = roadTopRight + (roadBottomRight - roadTopRight) * (t1 * t1)

    val kerbColor = if (((i + stripeOffset.toInt()) % 2) == 0) Color(0xFFE53935) else Color(0xFFFAFAFA)

    // Left Kerb
    drawPath(
      Path().apply {
        moveTo(leftX0, y0)
        lineTo(leftX0 - kerbBottomW * t0, y0)
        lineTo(leftX1 - kerbBottomW * t1, y1)
        lineTo(leftX1, y1)
        close()
      },
      color = kerbColor
    )

    // Right Kerb
    drawPath(
      Path().apply {
        moveTo(rightX0, y0)
        lineTo(rightX0 + kerbBottomW * t0, y0)
        lineTo(rightX1 + kerbBottomW * t1, y1)
        lineTo(rightX1, y1)
        close()
      },
      color = kerbColor
    )

    // Center Dashed White Road Lines
    if (i % 2 == 0) {
      val midX0 = leftX0 + (rightX0 - leftX0) * 0.5f
      val midX1 = leftX1 + (rightX1 - leftX1) * 0.5f
      val dashW = 2.dp.toPx() + (8.dp.toPx() * t1)
      drawLine(
        color = Color(0xFFECEFF1).copy(alpha = 0.85f),
        start = Offset(midX0, y0),
        end = Offset(midX1, y1),
        strokeWidth = dashW
      )
    }
  }

  // Dynamic Racing Line (Green -> Yellow -> Red in Braking Zones)
  val racingLineColor = if (isBrakingZone) Color(0xFFFF1744).copy(alpha = 0.65f) else Color(0xFF00E676).copy(alpha = 0.55f)
  val idealLineT = 0.5f + (curveShift / canvasW * 0.8f) // Clings to apex
  val lineX0 = roadTopLeft + (roadTopRight - roadTopLeft) * idealLineT
  val lineX1 = roadBottomLeft + (roadBottomRight - roadBottomLeft) * idealLineT

  drawLine(
    color = racingLineColor,
    start = Offset(lineX0, horizonY),
    end = Offset(lineX1, canvasH),
    strokeWidth = 6.dp.toPx(),
    cap = StrokeCap.Round
  )
}

// -------------------------------------------------------------------
// 3. TRACKSIDE GANTRY & DISTANCE BOARDS
// -------------------------------------------------------------------
private fun DrawScope.drawTracksideAssets(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  playerDistance: Float,
  playerLaneX: Float,
  curveShift: Float,
  speedKmh: Float
) {
  // Checkered Start / Finish Gantry (appears at lap boundary)
  val distInLap = playerDistance % 1200f
  if (distInLap < 180f) {
    val t = 1f - (distInLap / 180f)
    val gantryY = horizonY + (canvasH - horizonY) * (t * t * 0.85f)
    val gantryW = canvasW * (0.35f + t * 0.55f)
    val gantryH = 26.dp.toPx() + (45.dp.toPx() * t)

    val centerX = (canvasW * 0.5f) + curveShift * (1f - t) - (playerLaneX * canvasW * 0.25f * t)

    // Overhead Bridge Structure
    drawRect(
      color = Color(0xFF1E2638),
      topLeft = Offset(centerX - gantryW * 0.5f, gantryY - gantryH),
      size = Size(gantryW, gantryH * 0.45f)
    )

    // Checkered pattern strip on gantry
    val checks = 12
    val checkW = gantryW / checks
    for (c in 0 until checks) {
      val checkCol = if (c % 2 == 0) Color.White else Color(0xFF111111)
      drawRect(
        color = checkCol,
        topLeft = Offset(centerX - gantryW * 0.5f + c * checkW, gantryY - gantryH),
        size = Size(checkW, gantryH * 0.22f)
      )
    }

    // Gantry Pillars
    val pillarW = 8.dp.toPx() + 14.dp.toPx() * t
    drawRect(
      color = Color(0xFF101622),
      topLeft = Offset(centerX - gantryW * 0.5f, gantryY - gantryH),
      size = Size(pillarW, gantryH)
    )
    drawRect(
      color = Color(0xFF101622),
      topLeft = Offset(centerX + gantryW * 0.5f - pillarW, gantryY - gantryH),
      size = Size(pillarW, gantryH)
    )
  }
}

// -------------------------------------------------------------------
// 4. RIVAL AI OPPONENTS
// -------------------------------------------------------------------
private fun DrawScope.drawRivalVehicles(
  canvasW: Float,
  canvasH: Float,
  horizonY: Float,
  playerDistance: Float,
  playerLaneX: Float,
  curveShift: Float,
  engine: InteractiveRacingEngine
) {
  val rivals = engine.getRivalsList()
  val roadTopW = canvasW * 0.18f
  val roadBottomW = canvasW * 0.92f
  val vanishingX = (canvasW * 0.5f) + curveShift - (playerLaneX * canvasW * 0.12f)
  val roadCenterX = (canvasW * 0.5f) - (playerLaneX * canvasW * 0.35f)

  // Render rivals ahead of player, sorted far-to-near for proper z-ordering
  val visibleRivals = rivals.filter {
    val deltaMeters = it.distanceMeters - playerDistance
    deltaMeters in 3f..260f
  }.sortedByDescending { it.distanceMeters - playerDistance }

  visibleRivals.forEach { rival ->
    val deltaMeters = rival.distanceMeters - playerDistance
    val depthNorm = (1f - (deltaMeters / 260f)).coerceIn(0.04f, 1f)

    // Perspective Y & Width
    val carY = horizonY + (canvasH - horizonY) * (depthNorm * depthNorm * 0.88f)
    val carW = (canvasW * 0.08f) + (canvasW * 0.18f * depthNorm)
    val carH = carW * 0.52f

    // Current road width at this Y
    val roadWAtY = roadTopW + (roadBottomW - roadTopW) * (depthNorm * depthNorm)
    val roadCenterAtY = vanishingX + (roadCenterX - vanishingX) * (depthNorm * depthNorm)

    val carX = roadCenterAtY + (rival.laneOffset * roadWAtY * 0.44f) - (carW * 0.5f)

    // Rival Shadow
    drawOval(
      color = Color.Black.copy(alpha = 0.65f),
      topLeft = Offset(carX, carY + carH * 0.82f),
      size = Size(carW, carH * 0.28f)
    )

    // Rival Body
    val bodyColor = rival.composeColor
    drawRoundRect(
      color = bodyColor,
      topLeft = Offset(carX, carY),
      size = Size(carW, carH * 0.75f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(carW * 0.1f, carW * 0.1f)
    )

    // Rear Windshield / Cockpit Canopy
    drawRoundRect(
      color = Color(0xFF0A0F18),
      topLeft = Offset(carX + carW * 0.18f, carY + carH * 0.08f),
      size = Size(carW * 0.64f, carH * 0.32f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(carW * 0.06f, carW * 0.06f)
    )

    // Rear Wing / Diffuser
    drawRect(
      color = Color(0xFF1A1A1A),
      topLeft = Offset(carX + carW * 0.08f, carY - carH * 0.08f),
      size = Size(carW * 0.84f, carH * 0.12f)
    )

    // Glowing Red Taillights
    val lightW = carW * 0.16f
    val lightH = carH * 0.14f
    drawRect(
      color = Color(0xFFFF1744),
      topLeft = Offset(carX + carW * 0.08f, carY + carH * 0.38f),
      size = Size(lightW, lightH)
    )
    drawRect(
      color = Color(0xFFFF1744),
      topLeft = Offset(carX + carW * 0.76f, carY + carH * 0.38f),
      size = Size(lightW, lightH)
    )

    // Slipstream Aero Trails if close
    if (deltaMeters < 35f && engine.hudState.value.isDrafting) {
      val trailAlpha = (1f - (deltaMeters / 35f)).coerceIn(0.2f, 0.85f)
      drawLine(
        color = Color(0xFF00E5FF).copy(alpha = trailAlpha),
        start = Offset(carX + carW * 0.1f, carY + carH * 0.5f),
        end = Offset(carX - carW * 0.2f, canvasH * 0.85f),
        strokeWidth = 2.dp.toPx()
      )
      drawLine(
        color = Color(0xFF00E5FF).copy(alpha = trailAlpha),
        start = Offset(carX + carW * 0.9f, carY + carH * 0.5f),
        end = Offset(carX + carW * 1.2f, canvasH * 0.85f),
        strokeWidth = 2.dp.toPx()
      )
    }
  }
}

// -------------------------------------------------------------------
// 5. CHASE CAM: PLAYER VEHICLE
// -------------------------------------------------------------------
private fun DrawScope.drawChaseCamPlayerVehicle(
  canvasW: Float,
  canvasH: Float,
  playerLaneX: Float,
  steerInput: Float,
  brakeInput: Float,
  isNitroActive: Boolean,
  isDrsActive: Boolean,
  speedKmh: Float,
  pulse: Float
) {
  val carW = canvasW * 0.44f
  val carH = carW * 0.54f

  val carCenterX = (canvasW * 0.5f) + (steerInput * 12.dp.toPx())
  val carY = canvasH * 0.64f + (brakeInput * 8.dp.toPx())

  val carLeft = carCenterX - (carW * 0.5f)

  // Dynamic Steering Body Roll
  val rollAngle = steerInput * 4.5f

  rotate(degrees = rollAngle, pivot = Offset(carCenterX, carY + carH * 0.5f)) {
    // Car Ground Shadow
    drawOval(
      color = Color.Black.copy(alpha = 0.75f),
      topLeft = Offset(carLeft + carW * 0.05f, carY + carH * 0.82f),
      size = Size(carW * 0.9f, carH * 0.28f)
    )

    // Wide Racing Wheels & Slicks (Left & Right)
    val wheelW = carW * 0.16f
    val wheelH = carH * 0.42f
    drawRoundRect(
      color = Color(0xFF14171E),
      topLeft = Offset(carLeft + 2.dp.toPx(), carY + carH * 0.42f),
      size = Size(wheelW, wheelH),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(wheelW * 0.2f, wheelW * 0.2f)
    )
    drawRoundRect(
      color = Color(0xFF14171E),
      topLeft = Offset(carLeft + carW - wheelW - 2.dp.toPx(), carY + carH * 0.42f),
      size = Size(wheelW, wheelH),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(wheelW * 0.2f, wheelW * 0.2f)
    )

    // Wide Hypercar Bodywork (Carbon Cyan / Redline Theme)
    val mainBodyPath = Path().apply {
      moveTo(carLeft + carW * 0.14f, carY + carH * 0.22f)
      lineTo(carLeft + carW * 0.28f, carY + carH * 0.04f)
      lineTo(carLeft + carW * 0.72f, carY + carH * 0.04f)
      lineTo(carLeft + carW * 0.86f, carY + carH * 0.22f)
      lineTo(carLeft + carW * 0.94f, carY + carH * 0.75f)
      lineTo(carLeft + carW * 0.06f, carY + carH * 0.75f)
      close()
    }
    drawPath(
      mainBodyPath,
      brush = Brush.verticalGradient(
        colors = listOf(
          Color(0xFF00E5FF), // Apex Neon Cyan
          Color(0xFF0091EA),
          Color(0xFF0D47A1),
          Color(0xFF0A121F)
        ),
        startY = carY,
        endY = carY + carH * 0.75f
      )
    )

    // Cockpit Roof & Dark Windshield
    drawRoundRect(
      color = Color(0xFF060911),
      topLeft = Offset(carLeft + carW * 0.28f, carY + carH * 0.08f),
      size = Size(carW * 0.44f, carH * 0.32f),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(carW * 0.08f, carW * 0.08f)
    )

    // Active Carbon Rear Wing (DRS rotates flat when active)
    val wingY = if (isDrsActive) carY - carH * 0.02f else carY - carH * 0.09f
    val wingH = if (isDrsActive) carH * 0.05f else carH * 0.10f
    drawRoundRect(
      color = Color(0xFF1B202A),
      topLeft = Offset(carLeft + carW * 0.05f, wingY),
      size = Size(carW * 0.9f, wingH),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )

    // Wing Struts
    drawRect(
      color = Color(0xFF0F131A),
      topLeft = Offset(carLeft + carW * 0.32f, wingY),
      size = Size(carW * 0.05f, carH * 0.14f)
    )
    drawRect(
      color = Color(0xFF0F131A),
      topLeft = Offset(carLeft + carW * 0.63f, wingY),
      size = Size(carW * 0.05f, carH * 0.14f)
    )

    // Rear Carbon Diffuser & Strakes
    drawRect(
      color = Color(0xFF0A0D14),
      topLeft = Offset(carLeft + carW * 0.16f, carY + carH * 0.72f),
      size = Size(carW * 0.68f, carH * 0.16f)
    )

    // Intense LED Taillight Bars (Glow red, flare super bright on braking!)
    val taillightColor = if (brakeInput > 0.1f) Color(0xFFFF1744) else Color(0xFFD50000)
    val glowHeight = if (brakeInput > 0.1f) carH * 0.12f else carH * 0.06f

    drawRoundRect(
      color = taillightColor,
      topLeft = Offset(carLeft + carW * 0.10f, carY + carH * 0.42f),
      size = Size(carW * 0.28f, glowHeight),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
    drawRoundRect(
      color = taillightColor,
      topLeft = Offset(carLeft + carW * 0.62f, carY + carH * 0.42f),
      size = Size(carW * 0.28f, glowHeight),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )

    // Dual Quad Exhaust Pipes
    val exhaustY = carY + carH * 0.74f
    val exhaustLeft1 = carLeft + carW * 0.38f
    val exhaustLeft2 = carLeft + carW * 0.44f
    val exhaustRight1 = carLeft + carW * 0.52f
    val exhaustRight2 = carLeft + carW * 0.58f

    listOf<Float>(exhaustLeft1, exhaustLeft2, exhaustRight1, exhaustRight2).forEach { exX ->
      drawCircle(
        color = Color(0xFF1E2636),
        radius = carW * 0.024f,
        center = Offset(exX, exhaustY)
      )
    }

    // Nitro Flame Bursts from Exhausts
    if (isNitroActive || speedKmh > 300f) {
      val flameLength = (carH * 0.35f) * (0.8f + (pulse * 0.4f))
      listOf<Float>(exhaustLeft1, exhaustLeft2, exhaustRight1, exhaustRight2).forEach { exX ->
        val flamePath = Path().apply {
          moveTo(exX - carW * 0.015f, exhaustY)
          lineTo(exX, exhaustY + flameLength)
          lineTo(exX + carW * 0.015f, exhaustY)
          close()
        }
        drawPath(
          flamePath,
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color.Transparent),
            startY = exhaustY,
            endY = exhaustY + flameLength
          )
        )
      }
    }
  }
}

// -------------------------------------------------------------------
// 6. COCKPIT CAM: STEERING WHEEL & WORKING CABIN
// -------------------------------------------------------------------
private fun DrawScope.drawCockpitCam(
  canvasW: Float,
  canvasH: Float,
  steerInput: Float,
  rpm: Int,
  gear: Int,
  speedKmh: Float,
  isDrsActive: Boolean,
  rivals: List<Any>,
  playerDistance: Float
) {
  // Lower Cockpit Dashboard Structure
  val dashY = canvasH * 0.62f
  val dashPath = Path().apply {
    moveTo(0f, dashY)
    lineTo(canvasW * 0.3f, dashY + canvasH * 0.08f)
    lineTo(canvasW * 0.7f, dashY + canvasH * 0.08f)
    lineTo(canvasW, dashY)
    lineTo(canvasW, canvasH)
    lineTo(0f, canvasH)
    close()
  }
  drawPath(
    dashPath,
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF161922), Color(0xFF0D0F14)),
      startY = dashY,
      endY = canvasH
    )
  )

  // Rotating Alcantara Racing Steering Wheel
  val wheelRadius = canvasW * 0.28f
  val wheelCenter = Offset(canvasW * 0.5f, canvasH * 0.88f)
  val wheelRotation = steerInput * 45f

  rotate(degrees = wheelRotation, pivot = wheelCenter) {
    // Outer Wheel Rim
    drawCircle(
      color = Color(0xFF232733),
      radius = wheelRadius,
      center = wheelCenter,
      style = Stroke(width = canvasW * 0.065f)
    )

    // Center 12 o'clock cyan positioning strip
    drawRect(
      color = Color(0xFF00E5FF),
      topLeft = Offset(wheelCenter.x - 4.dp.toPx(), wheelCenter.y - wheelRadius - 8.dp.toPx()),
      size = Size(8.dp.toPx(), 16.dp.toPx())
    )

    // Center Hub with Formula Apex Emblem
    drawCircle(
      color = Color(0xFF12151D),
      radius = wheelRadius * 0.45f,
      center = wheelCenter
    )
    drawCircle(
      color = Color(0xFF00E5FF),
      radius = wheelRadius * 0.12f,
      center = wheelCenter
    )
  }

  // Rev Limiter Shift LEDs on Dash (Green -> Yellow -> Red -> Blue)
  val ledsCount = 12
  val ledW = canvasW * 0.025f
  val startLedX = canvasW * 0.35f
  val rpmRatio = (rpm / 9200f).coerceIn(0f, 1f)

  for (i in 0 until ledsCount) {
    val activeRatio = (i.toFloat() / ledsCount)
    val isActive = rpmRatio >= activeRatio
    val ledCol = when {
      !isActive -> Color(0xFF2A2E3B)
      i < 4 -> Color(0xFF00E676)
      i < 8 -> Color(0xFFFFD600)
      i < 11 -> Color(0xFFFF1744)
      else -> Color(0xFF00E5FF)
    }
    drawRoundRect(
      color = ledCol,
      topLeft = Offset(startLedX + i * (ledW + 3.dp.toPx()), dashY + 12.dp.toPx()),
      size = Size(ledW, 8.dp.toPx()),
      cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
    )
  }

  // Rearview Mirror at Top Center
  val mirrorW = canvasW * 0.38f
  val mirrorH = mirrorW * 0.32f
  val mirrorLeft = (canvasW - mirrorW) * 0.5f

  drawRoundRect(
    color = Color(0xFF0C101A),
    topLeft = Offset(mirrorLeft, 8.dp.toPx()),
    size = Size(mirrorW, mirrorH),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
    style = Stroke(width = 2.dp.toPx())
  )
  drawRoundRect(
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF142033), Color(0xFF0A0F1A)),
      startY = 8.dp.toPx(),
      endY = 8.dp.toPx() + mirrorH
    ),
    topLeft = Offset(mirrorLeft + 2.dp.toPx(), 10.dp.toPx()),
    size = Size(mirrorW - 4.dp.toPx(), mirrorH - 4.dp.toPx()),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
  )
}

// -------------------------------------------------------------------
// 7. HOOD CAM OVERLAY
// -------------------------------------------------------------------
private fun DrawScope.drawHoodCamOverlay(
  canvasW: Float,
  canvasH: Float,
  speedKmh: Float,
  brakeInput: Float,
  pulse: Float
) {
  // Low-slung front aerodynamic nose cone
  val hoodH = canvasH * 0.16f
  val hoodPath = Path().apply {
    moveTo(canvasW * 0.28f, canvasH)
    lineTo(canvasW * 0.44f, canvasH - hoodH)
    lineTo(canvasW * 0.56f, canvasH - hoodH)
    lineTo(canvasW * 0.72f, canvasH)
    close()
  }
  drawPath(
    hoodPath,
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF00E5FF), Color(0xFF0B2545)),
      startY = canvasH - hoodH,
      endY = canvasH
    )
  )
}

// -------------------------------------------------------------------
// 8. SPEED WARP LINES
// -------------------------------------------------------------------
private fun DrawScope.drawSpeedLines(
  w: Float,
  h: Float,
  horizonY: Float,
  isNitro: Boolean,
  pulse: Float
) {
  val lines = 16
  val lineColor = if (isNitro) Color(0xFF00E5FF).copy(alpha = 0.55f) else Color.White.copy(alpha = 0.35f)
  val cx = w * 0.5f

  for (i in 0 until lines) {
    val angle = (i.toFloat() / lines) * 3.14159f
    val r0 = w * 0.25f + (pulse * w * 0.15f)
    val r1 = r0 + (w * 0.22f)

    val x0 = cx + cos(angle) * r0
    val y0 = horizonY + sin(angle) * r0 * 0.65f
    val x1 = cx + cos(angle) * r1
    val y1 = horizonY + sin(angle) * r1 * 0.65f

    drawLine(
      color = lineColor,
      start = Offset(x0, y0),
      end = Offset(x1, y1),
      strokeWidth = if (isNitro) 2.5.dp.toPx() else 1.5.dp.toPx()
    )
  }
}

// -------------------------------------------------------------------
// 9. VICTORY CONFETTI
// -------------------------------------------------------------------
private fun DrawScope.drawVictoryConfetti(w: Float, h: Float, pulse: Float) {
  val colors = listOf(Color(0xFFFFD700), Color(0xFF00E5FF), Color(0xFFFF1744), Color(0xFF00E676))
  for (i in 0 until 40) {
    val cX = ((i * 37f + pulse * 400f) % w)
    val cY = ((i * 53f + pulse * 700f) % h)
    val col = colors[i % colors.size]
    drawCircle(color = col, radius = 4.dp.toPx(), center = Offset(cX, cY))
  }
}
