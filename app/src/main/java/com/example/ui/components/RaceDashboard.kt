package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleDelta
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.ShiftBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * RaceDashboard Component
 *
 * Professional esports digital steering wheel & cockpit telemetry dashboard.
 * Displays real-time telemetry: speed, gear, RPM with sequential shift lights,
 * throttle/brake trace, lap delta, ERS battery, and tire thermals.
 *
 * All parameters include production-ready placeholder defaults.
 */
@Composable
fun RaceDashboard(
  modifier: Modifier = Modifier,
  // Primary Telemetry Parameters (with realistic placeholder defaults)
  speedKmh: Float = 278.4f,
  gear: Int = 5,
  rpm: Int = 11850,
  maxRpm: Int = 14500,
  redlineRpm: Int = 13200,
  throttlePercent: Float = 0.92f,
  brakePercent: Float = 0.0f,
  isMph: Boolean = false,
  deltaLapTimeSeconds: Float = -0.284f,
  batteryPercent: Float = 0.82f,
  fuelPercent: Float = 0.65f,
  tireTemps: List<Float> = listOf(94.2f, 95.8f, 91.5f, 92.0f), // FL, FR, RL, RR
  drsAvailable: Boolean = true,
  ersMode: String = "HOTLAP",
  trackName: String = "Monza GP",
  lapNumber: Int = 8,
  totalLaps: Int = 25,
  allowInteractiveDemo: Boolean = true,
  onSpeedUnitToggle: () -> Unit = {},
  onGearUp: () -> Unit = {},
  onGearDown: () -> Unit = {}
) {
  var isDemoMode by remember { mutableStateOf(false) }

  // Interactive Demo State
  var demoRpm by remember { mutableIntStateOf(rpm) }
  var demoSpeed by remember { mutableFloatStateOf(speedKmh) }
  var demoGear by remember { mutableIntStateOf(gear) }
  var demoThrottle by remember { mutableFloatStateOf(throttlePercent) }
  var demoBrake by remember { mutableFloatStateOf(brakePercent) }

  // Animated telemetry when Demo mode is engaged
  LaunchedEffect(isDemoMode) {
    if (isDemoMode) {
      var currentG = demoGear.coerceIn(1, 7)
      var currentR = 9000
      var currentS = 180f
      while (isDemoMode) {
        // Accelerating loop through gears
        if (currentR < redlineRpm + 400) {
          currentR += 450
          currentS += 3.8f
          demoThrottle = 1.0f
          demoBrake = 0.0f
        } else {
          // Upshift
          if (currentG < 7) {
            currentG++
            currentR = 9200
          } else {
            // Reached top gear, simulate braking into chicane
            currentG = 2
            currentR = 6500
            currentS = 95f
            demoThrottle = 0.1f
            demoBrake = 0.85f
            delay(400)
          }
        }
        demoGear = currentG
        demoRpm = currentR
        demoSpeed = currentS
        delay(80)
      }
    }
  }

  // Resolved values based on active mode
  val activeSpeedKmh = if (isDemoMode) demoSpeed else speedKmh
  val activeGear = if (isDemoMode) demoGear else gear
  val activeRpm = if (isDemoMode) demoRpm else rpm
  val activeThrottle = if (isDemoMode) demoThrottle else throttlePercent
  val activeBrake = if (isDemoMode) demoBrake else brakePercent

  val rpmRatio = (activeRpm.toFloat() / maxRpm.coerceAtLeast(1)).coerceIn(0f, 1f)
  val isRedlining = activeRpm >= redlineRpm

  // Flashing animation for redline / shift warning
  val infiniteTransition = rememberInfiniteTransition(label = "redlineShift")
  val flashAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(120),
      repeatMode = RepeatMode.Reverse
    ),
    label = "redlineFlash"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("race_dashboard"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(16.dp),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isRedlining) 2.dp else 1.dp,
      color = if (isRedlining) RedlineRed.copy(alpha = flashAlpha) else CarbonBorder
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(
              CarbonSurfaceVariant.copy(alpha = 0.6f),
              CarbonBlack
            )
          )
        )
        .padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {

      // 1. TOP STATUS BAR (Track, Lap Counter, DRS, Demo Mode Toggle)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = trackName.uppercase(Locale.US),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(CarbonSurfaceVariant)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "LAP $lapNumber / $totalLaps",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // DRS Status Indicator
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (drsAvailable) ApexGreen.copy(alpha = 0.2f) else CarbonBorder.copy(alpha = 0.4f))
              .border(0.8.dp, if (drsAvailable) ApexGreen else CarbonBorder, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (drsAvailable) "DRS READY" else "DRS OFF",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Black,
              color = if (drsAvailable) ApexGreen else TextTertiary
            )
          }

          if (allowInteractiveDemo) {
            Spacer(modifier = Modifier.width(6.dp))
            // Interactive Demo Toggle
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isDemoMode) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant)
                .border(0.8.dp, if (isDemoMode) NeonAmber else CarbonBorder, RoundedCornerShape(4.dp))
                .clickable { isDemoMode = !isDemoMode }
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("dashboard_demo_toggle")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = null,
                  tint = if (isDemoMode) NeonAmber else TextSecondary,
                  modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = if (isDemoMode) "SIM RUN" else "DEMO",
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Black,
                  color = if (isDemoMode) NeonAmber else TextSecondary
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. SEQUENTIAL SHIFT LIGHT ARRAY (15 LEDs)
      ShiftLightLedArray(
        rpmRatio = rpmRatio,
        isRedlining = isRedlining,
        flashAlpha = flashAlpha
      )

      Spacer(modifier = Modifier.height(12.dp))

      // 3. MAIN DASHBOARD TELEMETRY ROW (Left Wing, Center Cluster, Right Wing)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {

        // LEFT WING: Pedals (Throttle / Brake) + Tire Temps
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(end = 8.dp),
          horizontalAlignment = Alignment.Start
        ) {
          // Throttle & Brake Dual Vertical/Bar Meter
          ThrottleBrakeMeter(
            throttlePercent = activeThrottle,
            brakePercent = activeBrake
          )

          Spacer(modifier = Modifier.height(8.dp))

          // 4-Corner Tire Temperatures
          TireTempsMiniGrid(tireTemps = tireTemps)
        }

        // CENTER CLUSTER: Large Gear Display & Speedometer
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonBlack)
            .border(
              width = if (isRedlining) 2.dp else 1.dp,
              color = if (isRedlining) RedlineRed else NeonCyan.copy(alpha = 0.4f),
              shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 18.dp, vertical = 10.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // GEAR INDICATOR (with up/down buttons)
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              if (allowInteractiveDemo) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowDown,
                  contentDescription = "Gear Down",
                  tint = TextTertiary,
                  modifier = Modifier
                    .size(18.dp)
                    .clickable { onGearDown() }
                )
              }

              // Gear text
              val gearText = when {
                activeGear <= -1 -> "R"
                activeGear == 0 -> "N"
                else -> activeGear.toString()
              }
              val gearColor by animateColorAsState(
                targetValue = if (isRedlining) RedlineRed else if (activeGear == 0) NeonAmber else TextPrimary,
                label = "gearColor"
              )

              Text(
                text = gearText,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = gearColor,
                modifier = Modifier
                  .padding(horizontal = 6.dp)
                  .testTag("dashboard_gear_value")
              )

              if (allowInteractiveDemo) {
                Icon(
                  imageVector = Icons.Default.KeyboardArrowUp,
                  contentDescription = "Gear Up",
                  tint = TextTertiary,
                  modifier = Modifier
                    .size(18.dp)
                    .clickable { onGearUp() }
                )
              }
            }

            // SPEEDOMETER VALUE & UNIT
            val displaySpeed = if (isMph) {
              (activeSpeedKmh * 0.621371f).toInt()
            } else {
              activeSpeedKmh.toInt()
            }
            val speedUnit = if (isMph) "MPH" else "KM/H"

            Row(
              verticalAlignment = Alignment.Bottom,
              modifier = Modifier
                .clickable { onSpeedUnitToggle() }
                .testTag("dashboard_speed_display")
            ) {
              Text(
                text = "$displaySpeed",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan,
                modifier = Modifier.testTag("dashboard_speed_value")
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = speedUnit,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonAmber,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                  .padding(bottom = 3.dp)
                  .testTag("dashboard_speed_unit")
              )
            }
          }
        }

        // RIGHT WING: Lap Delta, ERS Battery, Energy Mode
        Column(
          modifier = Modifier
            .weight(1f)
            .padding(start = 8.dp),
          horizontalAlignment = Alignment.End
        ) {
          // Lap Delta
          LapDeltaBadge(deltaSeconds = deltaLapTimeSeconds)

          Spacer(modifier = Modifier.height(8.dp))

          // ERS Battery & Hybrid Mode
          ErsHybridStatus(
            batteryPercent = batteryPercent,
            ersMode = ersMode,
            fuelPercent = fuelPercent
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. BOTTOM TACHOMETER RPM BAR & METRICS READOUT
      BottomRpmBar(
        currentRpm = activeRpm,
        maxRpm = maxRpm,
        redlineRpm = redlineRpm,
        rpmRatio = rpmRatio,
        isRedlining = isRedlining
      )
    }
  }
}

// -------------------------------------------------------------
// SUBCOMPONENTS
// -------------------------------------------------------------

/**
 * Sequential LED Shift Light Strip (15 LEDs total: 5 Green, 5 Amber, 5 Red/Blue)
 */
@Composable
private fun ShiftLightLedArray(
  rpmRatio: Float,
  isRedlining: Boolean,
  flashAlpha: Float
) {
  val totalLeds = 15
  val activeLedCount = (rpmRatio * totalLeds).toInt().coerceIn(0, totalLeds)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(6.dp))
      .background(CarbonBlack)
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("dashboard_led_array"),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until totalLeds) {
      val isLit = i < activeLedCount || (isRedlining && flashAlpha > 0.6f)
      val ledColor = when {
        i < 5 -> ApexGreen
        i < 10 -> NeonAmber
        i < 13 -> RedlineRed
        else -> PurpleDelta
      }

      Box(
        modifier = Modifier
          .size(width = 16.dp, height = 10.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(
            if (isLit) {
              if (isRedlining) ledColor.copy(alpha = flashAlpha) else ledColor
            } else {
              CarbonBorder.copy(alpha = 0.35f)
            }
          )
          .border(
            0.5.dp,
            if (isLit) ledColor.copy(alpha = 0.8f) else Color.Transparent,
            RoundedCornerShape(3.dp)
          )
      )
    }
  }
}

/**
 * Throttle & Brake Real-time Bars with live percentage readouts
 */
@Composable
private fun ThrottleBrakeMeter(
  throttlePercent: Float,
  brakePercent: Float
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBlack)
      .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(8.dp)
      .testTag("dashboard_throttle_brake_bars"),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Throttle Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "THR",
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = ApexGreen,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .weight(1f)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(CarbonSurfaceVariant)
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(throttlePercent.coerceIn(0f, 1f))
            .background(ApexGreen)
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "${(throttlePercent * 100).toInt()}%",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        fontFamily = FontFamily.Monospace
      )
    }

    // Brake Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "BRK",
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = RedlineRed,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .weight(1f)
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(CarbonSurfaceVariant)
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(brakePercent.coerceIn(0f, 1f))
            .background(RedlineRed)
        )
      }
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "${(brakePercent * 100).toInt()}%",
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

/**
 * 4-Corner Tire Temperatures (FL, FR, RL, RR)
 */
@Composable
private fun TireTempsMiniGrid(tireTemps: List<Float>) {
  val fl = tireTemps.getOrElse(0) { 92f }
  val fr = tireTemps.getOrElse(1) { 92f }
  val rl = tireTemps.getOrElse(2) { 90f }
  val rr = tireTemps.getOrElse(3) { 90f }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBlack)
      .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(6.dp)
      .testTag("dashboard_tire_temps")
  ) {
    Text(
      text = "TIRE THERMALS",
      fontSize = 8.sp,
      fontWeight = FontWeight.Black,
      color = TextTertiary,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      TireTempChip(label = "FL", temp = fl)
      TireTempChip(label = "FR", temp = fr)
    }
    Spacer(modifier = Modifier.height(3.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      TireTempChip(label = "RL", temp = rl)
      TireTempChip(label = "RR", temp = rr)
    }
  }
}

@Composable
private fun TireTempChip(label: String, temp: Float) {
  val tempColor = when {
    temp < 75f -> ShiftBlue
    temp in 75f..102f -> ApexGreen
    else -> RedlineRed
  }

  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = "$label ",
      fontSize = 8.sp,
      fontWeight = FontWeight.Bold,
      color = TextTertiary
    )
    Text(
      text = "${temp.toInt()}°C",
      fontSize = 9.sp,
      fontWeight = FontWeight.Black,
      color = tempColor,
      fontFamily = FontFamily.Monospace
    )
  }
}

/**
 * Lap Delta Indicator Badge (Apex Green = Ahead, Redline Red = Behind)
 */
@Composable
private fun LapDeltaBadge(deltaSeconds: Float) {
  val isAhead = deltaSeconds <= 0f
  val sign = if (isAhead) "-" else "+"
  val formattedDelta = String.format(Locale.US, "%s%.3fs", sign, kotlin.math.abs(deltaSeconds))
  val deltaColor = if (isAhead) ApexGreen else RedlineRed

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBlack)
      .border(0.8.dp, deltaColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("dashboard_delta_display"),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Timer,
          contentDescription = null,
          tint = deltaColor,
          modifier = Modifier.size(10.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "SPLIT DELTA",
          fontSize = 8.sp,
          fontWeight = FontWeight.Black,
          color = TextTertiary
        )
      }
      Text(
        text = formattedDelta,
        fontSize = 14.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = deltaColor
      )
    }
  }
}

/**
 * Hybrid ERS Battery Status & Fuel Gauge
 */
@Composable
private fun ErsHybridStatus(
  batteryPercent: Float,
  ersMode: String,
  fuelPercent: Float
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBlack)
      .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(6.dp)
      .testTag("dashboard_ers_display")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.ElectricBolt,
          contentDescription = null,
          tint = NeonAmber,
          modifier = Modifier.size(11.dp)
        )
        Text(
          text = "ERS",
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Black,
          color = NeonAmber
        )
      }
      Text(
        text = ersMode,
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        color = NeonCyan,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Battery Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(5.dp)
        .clip(RoundedCornerShape(2.5.dp))
        .background(CarbonSurfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(batteryPercent.coerceIn(0f, 1f))
          .background(NeonAmber)
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Fuel Remaining
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "FUEL",
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary
      )
      Text(
        text = "${(fuelPercent * 100).toInt()}%",
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

/**
 * Bottom Tachometer Bar with exact RPM numbers and redline indicator
 */
@Composable
private fun BottomRpmBar(
  currentRpm: Int,
  maxRpm: Int,
  redlineRpm: Int,
  rpmRatio: Float,
  isRedlining: Boolean
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBlack)
      .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 8.dp)
      .testTag("dashboard_rpm_bar")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Speed,
          contentDescription = null,
          tint = if (isRedlining) RedlineRed else NeonCyan,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "ENGINE TACHOMETER",
          fontSize = 9.sp,
          fontWeight = FontWeight.Black,
          color = TextTertiary,
          letterSpacing = 0.5.sp
        )
      }

      Text(
        text = "$currentRpm / $maxRpm RPM",
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = if (isRedlining) RedlineRed else TextPrimary,
        modifier = Modifier.testTag("dashboard_rpm_value")
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Gradient Progress Bar
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(CarbonSurfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(rpmRatio)
          .background(
            Brush.horizontalGradient(
              listOf(
                ApexGreen,
                NeonCyan,
                NeonAmber,
                RedlineRed
              )
            )
          )
      )
    }
  }
}
