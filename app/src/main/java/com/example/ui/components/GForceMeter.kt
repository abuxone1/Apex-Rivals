package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ApexGreen
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

@Composable
fun GForceMeter(
  lateralG: Float,
  longitudinalG: Float,
  peakLateralG: Float,
  maxScaleG: Float = 3.0f,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(12.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "G-FORCE FRICTION CIRCLE",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Box(
      modifier = Modifier
        .size(130.dp)
        .padding(vertical = 6.dp)
        .testTag("g_force_circle"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = (size.minDimension / 2) - 8.dp.toPx()

        // Concentric G-force rings: 1.0G, 2.0G, 3.0G
        val ringRatios = floatArrayOf(0.33f, 0.66f, 1.0f)
        ringRatios.forEach { ratio ->
          drawCircle(
            color = CarbonSurfaceVariant,
            radius = maxRadius * ratio,
            center = center,
            style = Stroke(width = 1.dp.toPx())
          )
        }

        // Crosshairs
        drawLine(
          color = CarbonBorder,
          start = Offset(center.x - maxRadius, center.y),
          end = Offset(center.x + maxRadius, center.y),
          strokeWidth = 1.dp.toPx()
        )
        drawLine(
          color = CarbonBorder,
          start = Offset(center.x, center.y - maxRadius),
          end = Offset(center.x, center.y + maxRadius),
          strokeWidth = 1.dp.toPx()
        )

        // Dynamic G position dot
        val normX = (lateralG / maxScaleG).coerceIn(-1f, 1f)
        val normY = (-longitudinalG / maxScaleG).coerceIn(-1f, 1f) // forward accel is up

        val targetX = center.x + (normX * maxRadius)
        val targetY = center.y + (normY * maxRadius)

        // Dynamic colored vector line from center to dot
        val gMagnitude = kotlin.math.sqrt((lateralG * lateralG + longitudinalG * longitudinalG).toDouble()).toFloat()
        val vectorColor = when {
          gMagnitude > 2.2f -> RedlineRed
          gMagnitude > 1.2f -> NeonAmber
          else -> NeonCyan
        }

        drawLine(
          color = vectorColor.copy(alpha = 0.6f),
          start = center,
          end = Offset(targetX, targetY),
          strokeWidth = 2.dp.toPx()
        )

        // Glow ring around dot
        drawCircle(
          color = vectorColor.copy(alpha = 0.35f),
          radius = 9.dp.toPx(),
          center = Offset(targetX, targetY)
        )
        drawCircle(
          color = vectorColor,
          radius = 5.dp.toPx(),
          center = Offset(targetX, targetY)
        )
      }
    }

    // Digital readout row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceAround
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "LATERAL", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(
          text = String.format(Locale.US, "%+.2f G", lateralG),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyan,
          fontFamily = FontFamily.Monospace
        )
      }
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "LONGIT.", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(
          text = String.format(Locale.US, "%+.2f G", longitudinalG),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NeonAmber,
          fontFamily = FontFamily.Monospace
        )
      }
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "PEAK", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(
          text = String.format(Locale.US, "%.2f G", peakLateralG),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = ApexGreen,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}
