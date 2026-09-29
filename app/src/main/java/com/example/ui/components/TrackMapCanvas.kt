package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleDelta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TrackMapCanvas(
  track: Track,
  playerProgress: Float,
  ghostProgress: Float? = null,
  rivalProgressList: List<Float> = emptyList(),
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = track.name.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        letterSpacing = 1.sp,
        modifier = Modifier.weight(1f)
      )

      // Legend
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonCyan))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "YOU", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(NeonAmber))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "RIVALS", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(140.dp)
        .padding(top = 8.dp)
        .testTag("track_map_canvas"),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val points = track.pathPoints
        if (points.isEmpty()) return@Canvas

        val padding = 16.dp.toPx()
        val drawW = size.width - (padding * 2)
        val drawH = size.height - (padding * 2)

        val circuitPath = Path()
        val screenPoints = points.map { pt ->
          Offset(
            padding + (pt.x * drawW),
            padding + (pt.y * drawH)
          )
        }

        circuitPath.moveTo(screenPoints.first().x, screenPoints.first().y)
        for (i in 1 until screenPoints.size) {
          circuitPath.lineTo(screenPoints[i].x, screenPoints[i].y)
        }
        circuitPath.close()

        // Draw track outline (asphalt base)
        drawPath(
          path = circuitPath,
          color = CarbonSurfaceVariant,
          style = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw track inner centerline
        drawPath(
          path = circuitPath,
          color = CarbonBorder,
          style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw start/finish line indicator
        val sPt = screenPoints.first()
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = sPt)

        // Draw Rivals on track
        rivalProgressList.forEach { rProg ->
          val normProg = rProg.coerceIn(0f, 0.999f)
          val rIdx = (normProg * (screenPoints.size - 1)).toInt()
          val nextIdx = (rIdx + 1) % screenPoints.size
          val localT = (normProg * (screenPoints.size - 1)) - rIdx
          val pt1 = screenPoints[rIdx]
          val pt2 = screenPoints[nextIdx]
          val rivalPos = Offset(
            pt1.x + (pt2.x - pt1.x) * localT,
            pt1.y + (pt2.y - pt1.y) * localT
          )
          drawCircle(color = NeonAmber, radius = 5.dp.toPx(), center = rivalPos)
        }

        // Draw Ghost position (if present)
        ghostProgress?.let { gProg ->
          val normProg = gProg.coerceIn(0f, 0.999f)
          val gIdx = (normProg * (screenPoints.size - 1)).toInt()
          val nextIdx = (gIdx + 1) % screenPoints.size
          val localT = (normProg * (screenPoints.size - 1)) - gIdx
          val pt1 = screenPoints[gIdx]
          val pt2 = screenPoints[nextIdx]
          val ghostPos = Offset(
            pt1.x + (pt2.x - pt1.x) * localT,
            pt1.y + (pt2.y - pt1.y) * localT
          )
          drawCircle(color = PurpleDelta.copy(alpha = 0.5f), radius = 8.dp.toPx(), center = ghostPos)
          drawCircle(color = PurpleDelta, radius = 4.dp.toPx(), center = ghostPos)
        }

        // Draw Player position
        val normProg = playerProgress.coerceIn(0f, 0.999f)
        val pIdx = (normProg * (screenPoints.size - 1)).toInt()
        val nextIdx = (pIdx + 1) % screenPoints.size
        val localT = (normProg * (screenPoints.size - 1)) - pIdx
        val pt1 = screenPoints[pIdx]
        val pt2 = screenPoints[nextIdx]
        val playerPos = Offset(
          pt1.x + (pt2.x - pt1.x) * localT,
          pt1.y + (pt2.y - pt1.y) * localT
        )

        // Player glow ring and dot
        drawCircle(color = NeonCyan.copy(alpha = 0.4f), radius = 10.dp.toPx(), center = playerPos)
        drawCircle(color = NeonCyan, radius = 6.dp.toPx(), center = playerPos)
      }
    }
  }
}
