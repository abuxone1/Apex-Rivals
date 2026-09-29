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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetrySnapshot
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleDelta
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TelemetryTraceGraph(
  frames: List<TelemetrySnapshot>,
  currentScrubProgress: Float? = null,
  ghostFrames: List<TelemetrySnapshot>? = null,
  maxSpeedKmh: Float = 360f,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "TELEMETRY SPEED & INPUT TRACE",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp
      )

      // Legend
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(NeonCyan))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = "SPD", fontSize = 9.sp, color = NeonCyan, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ApexGreen))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = "THR", fontSize = 9.sp, color = ApexGreen, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(RedlineRed))
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = "BRK", fontSize = 9.sp, color = RedlineRed, fontWeight = FontWeight.Bold)

        if (ghostFrames != null) {
          Spacer(modifier = Modifier.width(6.dp))
          Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(PurpleDelta))
          Spacer(modifier = Modifier.width(3.dp))
          Text(text = "GHOST", fontSize = 9.sp, color = PurpleDelta, fontWeight = FontWeight.Bold)
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(110.dp)
        .padding(top = 8.dp)
        .testTag("telemetry_trace_graph")
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Grid lines (0%, 50%, 100%)
        drawLine(
          color = CarbonSurfaceVariant,
          start = Offset(0f, h * 0.25f),
          end = Offset(w, h * 0.25f),
          strokeWidth = 1.dp.toPx()
        )
        drawLine(
          color = CarbonSurfaceVariant,
          start = Offset(0f, h * 0.5f),
          end = Offset(w, h * 0.5f),
          strokeWidth = 1.dp.toPx()
        )
        drawLine(
          color = CarbonSurfaceVariant,
          start = Offset(0f, h * 0.75f),
          end = Offset(w, h * 0.75f),
          strokeWidth = 1.dp.toPx()
        )

        if (frames.size < 2) return@Canvas

        // Ghost Speed line (if available)
        ghostFrames?.let { ghost ->
          if (ghost.size >= 2) {
            val ghostPath = Path()
            ghost.forEachIndexed { idx, snap ->
              val x = (snap.trackProgress.coerceIn(0f, 1f)) * w
              val y = h - ((snap.speedKmh / maxSpeedKmh).coerceIn(0f, 1f) * h)
              if (idx == 0) ghostPath.moveTo(x, y) else ghostPath.lineTo(x, y)
            }
            drawPath(
              path = ghostPath,
              color = PurpleDelta.copy(alpha = 0.65f),
              style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            )
          }
        }

        // Speed Path
        val speedPath = Path()
        frames.forEachIndexed { idx, snap ->
          val x = (snap.trackProgress.coerceIn(0f, 1f)) * w
          val y = h - ((snap.speedKmh / maxSpeedKmh).coerceIn(0f, 1f) * h)
          if (idx == 0) speedPath.moveTo(x, y) else speedPath.lineTo(x, y)
        }
        drawPath(
          path = speedPath,
          color = NeonCyan,
          style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Throttle line (green, height 0..40%)
        val throttlePath = Path()
        frames.forEachIndexed { idx, snap ->
          val x = (snap.trackProgress.coerceIn(0f, 1f)) * w
          val y = h - (snap.throttle * 30.dp.toPx())
          if (idx == 0) throttlePath.moveTo(x, y) else throttlePath.lineTo(x, y)
        }
        drawPath(
          path = throttlePath,
          color = ApexGreen.copy(alpha = 0.8f),
          style = Stroke(width = 1.5.dp.toPx())
        )

        // Brake line (red, height 0..40%)
        val brakePath = Path()
        frames.forEachIndexed { idx, snap ->
          val x = (snap.trackProgress.coerceIn(0f, 1f)) * w
          val y = h - (snap.brake * 30.dp.toPx())
          if (idx == 0) brakePath.moveTo(x, y) else brakePath.lineTo(x, y)
        }
        drawPath(
          path = brakePath,
          color = RedlineRed.copy(alpha = 0.8f),
          style = Stroke(width = 1.5.dp.toPx())
        )

        // Scrubber needle position
        currentScrubProgress?.let { prog ->
          val scrubX = prog.coerceIn(0f, 1f) * w
          drawLine(
            color = Color.White,
            start = Offset(scrubX, 0f),
            end = Offset(scrubX, h),
            strokeWidth = 2.dp.toPx()
          )
          drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(scrubX, 4.dp.toPx()))
        }
      }
    }
  }
}
