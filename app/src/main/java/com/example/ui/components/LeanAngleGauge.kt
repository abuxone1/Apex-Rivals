package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LeanAngleGauge(
  leanAngleDeg: Float,
  maxLeanDeg: Float = 64f,
  modifier: Modifier = Modifier
) {
  val absLean = abs(leanAngleDeg)
  val isKneeDown = absLean >= 55f
  val isWarningLean = absLean >= 60f

  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, if (isKneeDown) NeonAmber else CarbonBorder, RoundedCornerShape(16.dp))
      .padding(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "MOTO LEAN ANGLE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp
      )

      if (isKneeDown) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isWarningLean) RedlineRed else NeonAmber)
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "KNEE DOWN!",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = CarbonBlack
          )
        }
      }
    }

    Box(
      modifier = Modifier
        .size(130.dp)
        .padding(vertical = 4.dp)
        .testTag("lean_angle_gauge"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = (size.minDimension / 2) - 8.dp.toPx()

        // Draw semicircular horizon arc (-65° to +65°)
        val startAngle = 180f + 25f
        val sweepAngle = 130f
        drawArc(
          color = CarbonSurfaceVariant,
          startAngle = startAngle,
          sweepAngle = sweepAngle,
          useCenter = false,
          style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // Center upright indicator (0°)
        drawLine(
          color = TextSecondary,
          start = Offset(center.x, center.y - radius + 4.dp.toPx()),
          end = Offset(center.x, center.y - radius - 6.dp.toPx()),
          strokeWidth = 2.dp.toPx(),
          cap = StrokeCap.Round
        )

        // 55° Knee scrape markers on left & right
        val markerAngleL = Math.toRadians((270 - 55).toDouble())
        val markerAngleR = Math.toRadians((270 + 55).toDouble())

        drawLine(
          color = NeonAmber,
          start = Offset(
            (center.x + (radius - 5.dp.toPx()) * cos(markerAngleL)).toFloat(),
            (center.y + (radius - 5.dp.toPx()) * sin(markerAngleL)).toFloat()
          ),
          end = Offset(
            (center.x + (radius + 5.dp.toPx()) * cos(markerAngleL)).toFloat(),
            (center.y + (radius + 5.dp.toPx()) * sin(markerAngleL)).toFloat()
          ),
          strokeWidth = 2.dp.toPx()
        )

        drawLine(
          color = NeonAmber,
          start = Offset(
            (center.x + (radius - 5.dp.toPx()) * cos(markerAngleR)).toFloat(),
            (center.y + (radius - 5.dp.toPx()) * sin(markerAngleR)).toFloat()
          ),
          end = Offset(
            (center.x + (radius + 5.dp.toPx()) * cos(markerAngleR)).toFloat(),
            (center.y + (radius + 5.dp.toPx()) * sin(markerAngleR)).toFloat()
          ),
          strokeWidth = 2.dp.toPx()
        )

        // Dynamic tilted bike needle
        val tiltAngleRad = Math.toRadians((270 + leanAngleDeg).toDouble())
        val needleColor = when {
          isWarningLean -> RedlineRed
          isKneeDown -> NeonAmber
          else -> NeonCyan
        }

        val needleTip = Offset(
          (center.x + radius * cos(tiltAngleRad)).toFloat(),
          (center.y + radius * sin(tiltAngleRad)).toFloat()
        )

        drawLine(
          color = needleColor,
          start = center,
          end = needleTip,
          strokeWidth = 4.dp.toPx(),
          cap = StrokeCap.Round
        )

        drawCircle(color = CarbonSurface, radius = 8.dp.toPx(), center = center)
        drawCircle(color = needleColor, radius = 5.dp.toPx(), center = center)
      }
    }

    // Readout Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      Text(
        text = if (leanAngleDeg < -1f) "LEFT" else if (leanAngleDeg > 1f) "RIGHT" else "UPRIGHT",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary
      )
      Text(
        text = String.format(Locale.US, "%.1f°", absLean),
        fontSize = 16.sp,
        fontWeight = FontWeight.Black,
        color = if (isKneeDown) NeonAmber else NeonCyan,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "MAX ${maxLeanDeg.toInt()}°",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary
      )
    }
  }
}
