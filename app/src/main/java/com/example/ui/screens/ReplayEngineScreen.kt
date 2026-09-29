package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RaceReplay
import com.example.model.TelemetrySnapshot
import com.example.model.VehicleType
import com.example.ui.components.GForceMeter
import com.example.ui.components.LeanAngleGauge
import com.example.ui.components.MultiplayerPlatformBadge
import com.example.ui.components.ShareLapTimeDialog
import com.example.ui.components.TelemetryTraceGraph
import com.example.ui.components.TrackMapCanvas
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleDelta
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RaceUiState
import com.example.ui.viewmodel.RaceViewModel
import java.util.Locale

@Composable
fun ReplayEngineScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val replay = uiState.activeReplay
  val progress = uiState.replayProgress
  var showShareDialog by remember { mutableStateOf(false) }

  if (showShareDialog && replay != null) {
    ShareLapTimeDialog(
      driverName = replay.driverName,
      trackName = replay.trackName,
      vehicleName = replay.vehicleName,
      vehicleType = replay.vehicleType,
      lapTimeMs = replay.lapTimeMs,
      topSpeedKmh = replay.topSpeedKmh,
      sector1Ms = replay.sector1Ms,
      sector2Ms = replay.sector2Ms,
      sector3Ms = replay.sector3Ms,
      peakG = replay.maxLateralG,
      platform = replay.platform,
      onDismiss = { showShareDialog = false }
    )
  }

  val currentFrame: TelemetrySnapshot? = replay?.telemetryFrames?.let { frames ->
    if (frames.isEmpty()) null
    else {
      val idx = (progress * (frames.size - 1)).toInt().coerceIn(0, frames.size - 1)
      frames[idx]
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(6.dp))
      // Replay Overview Header
      ReplayHeaderCard(replay = replay)
    }

    item {
      // Replay Video/Simulator Viewport Simulator
      ReplayViewportCard(
        replay = replay,
        frame = currentFrame,
        cameraMode = uiState.replayCamera,
        onCameraSelect = { viewModel.setReplayCamera(it) }
      )
    }

    item {
      // Interactive Scrubber & Playback Controls
      ReplayScrubberControls(
        progress = progress,
        onScrub = { viewModel.setReplayScrubProgress(it) },
        isPlaying = uiState.isReplayPlaying,
        onTogglePlay = { viewModel.toggleReplayPlay() },
        playbackSpeed = uiState.replayPlaybackSpeed,
        onSetSpeed = { viewModel.setReplaySpeed(it) },
        lapTimeMs = replay?.lapTimeMs ?: 0L,
        currentFrame = currentFrame
      )
    }

    item {
      // Telemetry Trace with Scrubber Needle
      replay?.let { rep ->
        TelemetryTraceGraph(
          frames = rep.telemetryFrames,
          currentScrubProgress = progress,
          maxSpeedKmh = rep.topSpeedKmh * 1.1f
        )
      }
    }

    item {
      // Mini Telemetry Gauges at current frame
      currentFrame?.let { frame ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          GForceMeter(
            lateralG = frame.lateralG,
            longitudinalG = frame.longitudinalG,
            peakLateralG = replay?.maxLateralG ?: 2.5f,
            modifier = Modifier.weight(1f)
          )

          if (replay?.vehicleType == VehicleType.MOTORBIKE) {
            LeanAngleGauge(
              leanAngleDeg = frame.leanAngleDeg,
              modifier = Modifier.weight(1f)
            )
          } else {
            ReplayInputBars(
              throttle = frame.throttle,
              brake = frame.brake,
              steer = frame.steerAngleDeg,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    item {
      // Track Map with Current Scrubber Position & Ghost
      TrackMapCanvas(
        track = uiState.activeTrack,
        playerProgress = progress,
        ghostProgress = (progress + 0.04f) % 1.0f
      )
    }

    item {
      // Social Sharing Action
      replay?.let { r ->
        Button(
          onClick = { showShareDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CarbonBlack),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("replay_share_button")
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "SHARE RECORDED LAP TIME", fontWeight = FontWeight.Black, fontSize = 13.sp)
        }
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun ReplayHeaderCard(replay: RaceReplay?) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
      .padding(14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = "INTERACTIVE REPLAY ANALYZER",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan,
          letterSpacing = 1.sp
        )
        Text(
          text = "${replay?.trackName ?: "Track"} • ${replay?.vehicleName ?: "Vehicle"}",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }

      replay?.platform?.let {
        MultiplayerPlatformBadge(platform = it)
      }
    }
  }
}

@Composable
fun ReplayViewportCard(
  replay: RaceReplay?,
  frame: TelemetrySnapshot?,
  cameraMode: String,
  onCameraSelect: (String) -> Unit
) {
  val cameraOptions = listOf("Cockpit HUD", "Chase Cam", "Trackside Heli", "Ghost Compare")

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(12.dp)
  ) {
    // Camera Selector Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(cameraOptions) { cam ->
        val isSelected = cam == cameraMode
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant)
            .border(1.dp, if (isSelected) NeonAmber else CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { onCameraSelect(cam) }
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Videocam,
              contentDescription = null,
              tint = if (isSelected) NeonAmber else TextSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = cam,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) NeonAmber else TextSecondary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Simulated Camera Viewport Screen
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(CarbonBlack)
        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
        .padding(12.dp),
      contentAlignment = Alignment.Center
    ) {
      // Overlay Telemetry HUD
      Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "CAM: ${cameraMode.uppercase()}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = NeonAmber,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "60 FPS • REC 4K",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = RedlineRed,
            fontFamily = FontFamily.Monospace
          )
        }

        // Center Viewport Simulation Graphics
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "${frame?.speedKmh?.toInt() ?: 0} KM/H",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "GEAR ${frame?.gear ?: 1} • ${frame?.rpm ?: 1200} RPM",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            fontFamily = FontFamily.Monospace
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "DELTA VS GHOST: ${String.format(Locale.US, "%+.3fs", (frame?.deltaVsGhostMs ?: 0L) / 1000f)}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if ((frame?.deltaVsGhostMs ?: 0L) <= 0) ApexGreen else RedlineRed,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "SECTOR ${frame?.sector ?: 1}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

@Composable
fun ReplayScrubberControls(
  progress: Float,
  onScrub: (Float) -> Unit,
  isPlaying: Boolean,
  onTogglePlay: () -> Unit,
  playbackSpeed: Float,
  onSetSpeed: (Float) -> Unit,
  lapTimeMs: Long,
  currentFrame: TelemetrySnapshot?
) {
  val currentTimeMs = (lapTimeMs * progress).toLong()
  val curMin = currentTimeMs / 60000
  val curSec = (currentTimeMs % 60000) / 1000
  val curMil = currentTimeMs % 1000
  val timeStr = String.format(Locale.US, "%02d:%02d.%03d", curMin, curSec, curMil)

  val totalMin = lapTimeMs / 60000
  val totalSec = (lapTimeMs % 60000) / 1000
  val totalMil = lapTimeMs % 1000
  val totalTimeStr = String.format(Locale.US, "%02d:%02d.%03d", totalMin, totalSec, totalMil)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(14.dp)
  ) {
    // Time Indicator
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = timeStr,
        fontSize = 14.sp,
        fontWeight = FontWeight.Black,
        color = NeonCyan,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = totalTimeStr,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        fontFamily = FontFamily.Monospace
      )
    }

    // Scrubber Slider
    Slider(
      value = progress,
      onValueChange = onScrub,
      colors = SliderDefaults.colors(
        thumbColor = NeonCyan,
        activeTrackColor = NeonCyan,
        inactiveTrackColor = CarbonSurfaceVariant
      ),
      modifier = Modifier.testTag("replay_scrubber")
    )

    // Playback Buttons & Speed Toggles
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Speed Selectors
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(0.5f, 1.0f, 2.0f).forEach { spd ->
          val isSel = playbackSpeed == spd
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSel) NeonCyan else CarbonSurfaceVariant)
              .clickable { onSetSpeed(spd) }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "${spd}x",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSel) CarbonBlack else TextSecondary
            )
          }
        }
      }

      // Play / Pause Main Button
      IconButton(
        onClick = onTogglePlay,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(NeonCyan)
          .testTag("replay_play_pause_button")
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
          contentDescription = "Play/Pause",
          tint = CarbonBlack,
          modifier = Modifier.size(28.dp)
        )
      }
    }
  }
}

@Composable
fun ReplayInputBars(
  throttle: Float,
  brake: Float,
  steer: Float,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(12.dp)
  ) {
    Text(
      text = "DRIVER INPUTS",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Throttle Bar
    Text(text = "THROTTLE ${(throttle * 100).toInt()}%", fontSize = 10.sp, color = ApexGreen, fontWeight = FontWeight.Bold)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(CarbonSurfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(throttle.coerceIn(0f, 1f))
          .height(8.dp)
          .background(ApexGreen)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Brake Bar
    Text(text = "BRAKE ${(brake * 100).toInt()}%", fontSize = 10.sp, color = RedlineRed, fontWeight = FontWeight.Bold)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(CarbonSurfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(brake.coerceIn(0f, 1f))
          .height(8.dp)
          .background(RedlineRed)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Steer angle
    Text(text = String.format(Locale.US, "STEER %.1f°", steer), fontSize = 10.sp, color = NeonCyan, fontWeight = FontWeight.Bold)
  }
}
