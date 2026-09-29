package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PerformanceMetrics
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleDelta
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale
import kotlin.math.abs

/**
 * Real-time Ghost Mode Comparison Overlay Card.
 * Displays the selected ghost lap from PerformanceMetrics database,
 * showing live real-time delta, animated dual-runner track progress,
 * and post-lap delta comparison against the user's live race timer.
 */
@Composable
fun GhostLapComparisonCard(
  ghostLap: PerformanceMetrics?,
  isGhostModeEnabled: Boolean,
  currentLapElapsedMillis: Long,
  isRunning: Boolean,
  lastCompletedLapMillis: Long?,
  onOpenSelector: () -> Unit,
  onToggleGhostMode: () -> Unit,
  onClearGhost: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (ghostLap == null) {
    // Empty state: Invitation banner to select a Ghost lap
    Box(
      modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(10.dp))
        .background(PurpleDelta.copy(alpha = 0.08f))
        .border(1.dp, PurpleDelta.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
        .clickable(onClick = onOpenSelector)
        .padding(horizontal = 12.dp, vertical = 9.dp)
        .testTag("ghost_mode_enable_prompt")
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = null,
            tint = PurpleDelta,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "GHOST MODE OVERLAY",
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              color = PurpleDelta,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "Select a best lap from DB to race against in real time",
              fontSize = 9.sp,
              color = TextSecondary
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PurpleDelta.copy(alpha = 0.2f))
            .border(0.8.dp, PurpleDelta, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "SELECT GHOST",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleDelta
          )
        }
      }
    }
    return
  }

  // Active Ghost Benchmark HUD
  val ghostTargetMs = ghostLap.lapTimeMs
  val ghostTargetSeconds = ghostTargetMs / 1000f

  // Calculate live real-time progress (0f to 1f)
  val runnerProgress = if (ghostTargetMs > 0) {
    (currentLapElapsedMillis.toFloat() / ghostTargetMs).coerceIn(0f, 1f)
  } else 0f

  // Ghost progress advances in lockstep with ghost benchmark target pace
  val ghostProgress = if (ghostTargetMs > 0) {
    (currentLapElapsedMillis.toFloat() / ghostTargetMs).coerceIn(0f, 1f)
  } else 0f

  // Live real-time delta to target
  val liveDeltaMillis = currentLapElapsedMillis - ghostTargetMs
  val isAheadOfGhostTarget = liveDeltaMillis < 0L

  // Pulse animation when live timing against ghost
  val infiniteTransition = rememberInfiniteTransition(label = "ghost_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ghost_pulse_alpha"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("ghost_mode_active_card"),
    colors = CardDefaults.cardColors(containerColor = CarbonBlack),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isGhostModeEnabled) PurpleDelta.copy(alpha = 0.7f) else CarbonBorder
    )
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      // Header: Ghost Name, Lap Benchmark, Toggle & Change Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(if (isGhostModeEnabled) PurpleDelta.copy(alpha = 0.25f) else CarbonSurfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isGhostModeEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = "Ghost Mode Status",
              tint = if (isGhostModeEnabled) PurpleDelta else TextTertiary,
              modifier = Modifier.size(14.dp)
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "GHOST BENCHMARK",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = if (isGhostModeEnabled) PurpleDelta else TextTertiary,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "• ${ghostLap.trackName}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
              )
            }
            Text(
              text = "${ghostLap.driverName} • ${ghostLap.vehicleName}",
              fontSize = 9.5.sp,
              color = TextPrimary
            )
          }
        }

        // Target Time Display + Actions
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = formatDuration(ghostTargetMs),
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = if (isGhostModeEnabled) PurpleDelta else TextSecondary,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "TARGET LAP",
              fontSize = 7.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          IconButton(
            onClick = onOpenSelector,
            modifier = Modifier
              .size(26.dp)
              .testTag("change_ghost_button")
          ) {
            Icon(
              imageVector = Icons.Default.ChangeCircle,
              contentDescription = "Change Ghost Lap",
              tint = NeonCyan,
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = onClearGhost,
            modifier = Modifier
              .size(26.dp)
              .testTag("dismiss_ghost_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Clear Ghost",
              tint = TextTertiary,
              modifier = Modifier.size(15.dp)
            )
          }
        }
      }

      if (isGhostModeEnabled) {
        Spacer(modifier = Modifier.height(8.dp))

        // Real-Time Ghost Progress Track (Dual Runner Gauge)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
          Column {
            // Track Runner Labels & Delta
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) NeonCyan.copy(alpha = pulseAlpha) else NeonCyan)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "LIVE RUNNER",
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonCyan
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(PurpleDelta)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "GHOST",
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = PurpleDelta
                )
              }

              // Live Real-Time Delta Indicator
              if (isRunning && currentLapElapsedMillis > 0L) {
                val deltaText = if (isAheadOfGhostTarget) {
                  val remainingMs = -liveDeltaMillis
                  "-${formatDelta(remainingMs)} to target"
                } else {
                  "+${formatDelta(liveDeltaMillis)} over ghost"
                }

                Text(
                  text = deltaText,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Black,
                  color = if (isAheadOfGhostTarget) ApexGreen else RedlineRed,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.testTag("live_ghost_delta_text")
                )
              } else {
                Text(
                  text = "READY",
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextTertiary,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dual Progress Bars on Track
            // Runner Bar (Cyan)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(CarbonBlack)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(fraction = runnerProgress.coerceIn(0.01f, 1f))
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(if (isAheadOfGhostTarget) NeonCyan else RedlineRed)
              )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Ghost Reference Bar (Purple)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CarbonBlack)
            ) {
              Box(
                modifier = Modifier
                  .fillMaxWidth(fraction = ghostProgress.coerceIn(0.01f, 1f))
                  .height(4.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(PurpleDelta.copy(alpha = 0.8f))
              )
            }
          }
        }

        // Post-Lap Delta vs Ghost (if user completed previous lap in this session)
        if (lastCompletedLapMillis != null && lastCompletedLapMillis > 0L) {
          val completedDelta = lastCompletedLapMillis - ghostTargetMs
          val beatGhost = completedDelta <= 0L

          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(if (beatGhost) ApexGreen.copy(alpha = 0.15f) else RedlineRed.copy(alpha = 0.15f))
              .border(
                0.8.dp,
                if (beatGhost) ApexGreen.copy(alpha = 0.5f) else RedlineRed.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
              )
              .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = if (beatGhost) Icons.Default.FlashOn else Icons.Default.Flag,
                contentDescription = null,
                tint = if (beatGhost) ApexGreen else RedlineRed,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (beatGhost) "LAST LAP BEAT GHOST RECORD!" else "LAST LAP BEHIND GHOST",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                color = if (beatGhost) ApexGreen else RedlineRed
              )
            }

            Text(
              text = if (beatGhost) {
                "-${String.format(Locale.US, "%.3f", abs(completedDelta) / 1000f)}s"
              } else {
                "+${String.format(Locale.US, "%.3f", completedDelta / 1000f)}s"
              },
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Black,
              color = if (beatGhost) ApexGreen else RedlineRed,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Ghost Benchmark Telemetry Stats (Avg Speed, Top Speed, Setup)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Ghost Specs: Top ${ghostLap.topSpeedKmh.toInt()} km/h • Avg ${ghostLap.getComputedAvgSpeed().toInt()} km/h",
            fontSize = 8.5.sp,
            color = TextTertiary,
            fontFamily = FontFamily.Monospace
          )

          if (ghostLap.weatherCondition.isNotBlank()) {
            Text(
              text = "${ghostLap.weatherCondition} • ${ghostLap.trackComplexity}",
              fontSize = 8.5.sp,
              color = NeonAmber
            )
          }
        }
      }
    }
  }
}

private fun formatDuration(millis: Long): String {
  val totalSeconds = millis / 1000
  val minutes = (totalSeconds / 60) % 60
  val seconds = totalSeconds % 60
  val ms = millis % 1000
  return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, ms)
}

private fun formatDelta(millis: Long): String {
  val totalSeconds = millis / 1000
  val minutes = (totalSeconds / 60) % 60
  val seconds = totalSeconds % 60
  val ms = millis % 1000
  return if (minutes > 0) {
    String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, ms)
  } else {
    String.format(Locale.US, "%02d.%03ds", seconds, ms)
  }
}
