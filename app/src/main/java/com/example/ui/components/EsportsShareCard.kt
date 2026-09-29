package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlatformType
import com.example.model.VehicleType
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun EsportsShareCard(
  playerName: String,
  platform: PlatformType,
  vehicleName: String,
  vehicleType: VehicleType,
  trackName: String,
  lapTimeMs: Long,
  topSpeedKmh: Float,
  peakG: Float,
  challengeCode: String,
  onShareClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val minutes = (lapTimeMs / 60000)
  val seconds = (lapTimeMs % 60000) / 1000
  val millis = (lapTimeMs % 1000)
  val formattedLapTime = String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.verticalGradient(
          listOf(CarbonSurfaceVariant, CarbonSurface)
        )
      )
      .border(1.5.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
      .padding(16.dp)
      .testTag("esports_share_card")
  ) {
    // Header Row: App Watermark & Platform
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(NeonCyan),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (vehicleType == VehicleType.CAR) Icons.Default.DirectionsCar else Icons.Default.DirectionsBike,
            contentDescription = "Vehicle Icon",
            tint = CarbonBlack,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "APEX RIVALS TELEMETRY",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan,
          letterSpacing = 1.sp
        )
      }

      MultiplayerPlatformBadge(platform = platform)
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Driver & Track Info
    Text(
      text = playerName,
      fontSize = 20.sp,
      fontWeight = FontWeight.Black,
      color = TextPrimary
    )
    Text(
      text = "$trackName • $vehicleName",
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Main Lap Time Highlight
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(CarbonBlack.copy(alpha = 0.6f))
        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = ApexGreen,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "OFFICIAL LAP TIME", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
          }
          Text(
            text = formattedLapTime,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = ApexGreen,
            fontFamily = FontFamily.Monospace
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = NeonAmber,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "TOP SPEED", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
          }
          Text(
            text = String.format(Locale.US, "%.1f km/h", topSpeedKmh),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NeonAmber,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = String.format(Locale.US, "Peak: %.2f G", peakG),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Challenge Code and Share Button
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(text = "RIVAL CHALLENGE PASSCODE", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(
          text = challengeCode,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyan,
          fontFamily = FontFamily.Monospace
        )
      }

      Button(
        onClick = onShareClick,
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonCyan,
          contentColor = CarbonBlack
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("share_telemetry_button")
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share",
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Share Card", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }
  }
}
