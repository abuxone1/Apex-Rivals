package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TachometerGauge(
  rpm: Int,
  maxRpm: Int,
  redlineRpm: Int,
  speedKmh: Float,
  gear: Int,
  isMph: Boolean,
  onSpeedUnitToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val rpmRatio = (rpm.toFloat() / maxRpm).coerceIn(0f, 1f)
  val isRedlining = rpm >= redlineRpm

  val infiniteTransition = rememberInfiniteTransition(label = "redlineFlash")
  val flashAlpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(90),
      repeatMode = RepeatMode.Reverse
    ),
    label = "flash"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, if (isRedlining) RedlineRed else CarbonBorder, RoundedCornerShape(16.dp))
      .padding(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Top F1-Style Progressive Shift Lights
    ShiftLightsBar(rpm = rpm, maxRpm = maxRpm, redlineRpm = redlineRpm)

    Spacer(modifier = Modifier.height(10.dp))

    // Circular Arc Tachometer Dial & Central Speed / Gear readout
    Box(
      modifier = Modifier
        .size(220.dp)
        .testTag("tachometer_gauge"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 14.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val arcSize = Size(diameter, diameter)

        val startAngle = 140f
        val sweepAngle = 260f

        // Background Track Arc
        drawArc(
          color = CarbonSurfaceVariant,
          startAngle = startAngle,
          sweepAngle = sweepAngle,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Active RPM Arc
        val activeSweep = sweepAngle * rpmRatio
        val arcBrush = Brush.sweepGradient(
          0.0f to NeonCyan,
          0.6f to ApexGreen,
          0.85f to NeonAmber,
          1.0f to RedlineRed
        )

        drawArc(
          brush = arcBrush,
          startAngle = startAngle,
          sweepAngle = activeSweep,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Redline marker notch
        val redlineRatio = redlineRpm.toFloat() / maxRpm
        val redlineAngle = Math.toRadians((startAngle + sweepAngle * redlineRatio).toDouble())
        val rCenter = size.minDimension / 2
        val innerR = rCenter - strokeWidth
        val outerR = rCenter + 2.dp.toPx()

        val notchStart = Offset(
          (rCenter + innerR * cos(redlineAngle)).toFloat(),
          (rCenter + innerR * sin(redlineAngle)).toFloat()
        )
        val notchEnd = Offset(
          (rCenter + outerR * cos(redlineAngle)).toFloat(),
          (rCenter + outerR * sin(redlineAngle)).toFloat()
        )
        drawLine(
          color = RedlineRed,
          start = notchStart,
          end = notchEnd,
          strokeWidth = 3.dp.toPx(),
          cap = StrokeCap.Round
        )
      }

      // Center Display (Gear, Speed, RPM numbers)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Gear Badge
        Box(
          modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(if (isRedlining) RedlineRed.copy(alpha = flashAlpha) else CarbonSurfaceVariant)
            .border(1.5.dp, if (isRedlining) RedlineRed else NeonCyan, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (gear == 0) "N" else gear.toString(),
            color = if (isRedlining) CarbonBlack else TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Speed Display with unit toggle
        val displayedSpeed = if (isMph) (speedKmh * 0.621371f).toInt() else speedKmh.toInt()
        Row(
          verticalAlignment = Alignment.Bottom,
          modifier = Modifier.clickable { onSpeedUnitToggle() }
        ) {
          Text(
            text = "$displayedSpeed",
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = if (isMph) " MPH" else " KM/H",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
          )
        }

        // Digital RPM readout
        Text(
          text = "$rpm RPM",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (isRedlining) RedlineRed else TextSecondary,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

@Composable
fun ShiftLightsBar(
  rpm: Int,
  maxRpm: Int,
  redlineRpm: Int
) {
  val totalLights = 10
  val redlineThreshold = (redlineRpm.toFloat() / maxRpm) * totalLights
  val activeCount = ((rpm.toFloat() / maxRpm) * totalLights).toInt().coerceIn(0, totalLights)

  Row(
    modifier = Modifier
      .fillMaxWidth(0.9f)
      .height(10.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    for (i in 0 until totalLights) {
      val isActive = i < activeCount
      val color = when {
        !isActive -> CarbonSurfaceVariant
        i < 3 -> ApexGreen
        i < 6 -> NeonAmber
        i < 9 -> RedlineRed
        else -> PurpleDelta
      }
      Box(
        modifier = Modifier
          .weight(1f)
          .height(8.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(color)
          .border(0.5.dp, CarbonBorder, RoundedCornerShape(2.dp))
      )
    }
  }
}
