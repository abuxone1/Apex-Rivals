package com.example.game.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.InteractiveRacingEngine
import com.example.game.model.CameraPerspective
import com.example.game.model.GameMode
import com.example.game.model.RaceFinishSummary
import com.example.game.model.RaceState
import com.example.game.model.RacingGameHudState
import com.example.model.Track
import com.example.model.Vehicle
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
import com.example.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun RacingGameScreen(
  vehicle: Vehicle,
  track: Track,
  onNavigateBack: () -> Unit,
  onNavigateToProfile: () -> Unit = {},
  isMph: Boolean = false,
  modifier: Modifier = Modifier
) {
  BackHandler { onNavigateBack() }

  val context = LocalContext.current
  val engine = remember(vehicle, track) {
    InteractiveRacingEngine(context, vehicle, track)
  }

  val raceState by engine.raceState.collectAsState()
  val hudState by engine.hudState.collectAsState()
  val finishSummary by engine.finishSummary.collectAsState()

  DisposableEffect(Unit) {
    onDispose {
      engine.resetRace()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .testTag("racing_game_screen")
  ) {
    // 1. 3D Perspective Racing Canvas
    RacingGameCanvas(
      engine = engine,
      modifier = Modifier.fillMaxSize()
    )

    // 2. Top In-Game Racing HUD
    TopRacingHudOverlay(
      hudState = hudState,
      track = track,
      vehicle = vehicle,
      perspective = engine.cameraPerspective,
      onSwitchCamera = { engine.switchCamera() },
      onBack = onNavigateBack,
      isMph = isMph
    )

    // 3. Dynamic Center Notification Banners
    AnimatedVisibility(
      visible = hudState.activeAlertMessage != null,
      enter = fadeIn() + scaleIn(),
      exit = fadeOut() + scaleOut(),
      modifier = Modifier
        .align(Alignment.Center)
        .padding(bottom = 60.dp)
    ) {
      hudState.activeAlertMessage?.let { msg ->
        val alertColor = Color(hudState.activeAlertColor)
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonBlack.copy(alpha = 0.88f))
            .border(1.5.dp, alertColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Text(
            text = msg,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = alertColor,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
          )
        }
      }
    }

    // 4. Starting Grid 5 Red Lights Overlay
    if (raceState == RaceState.LIGHTS_COUNTDOWN) {
      StartLightsOverlay(lightsCount = hudState.countdownLights)
    }

    // 5. Pre-Grid Launch Button
    if (raceState == RaceState.PRE_GRID) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(CarbonBlack.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.padding(24.dp)
        ) {
          Text(
            text = "GRAND PRIX GRID START",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 2.sp
          )
          Text(
            text = "${track.name.uppercase()} • ${track.totalLengthMeters}M CIRCUIT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 1.sp
          )
          Text(
            text = "8-Car grid, dynamic slipstream drafting, KERS nitro boost & live telemetry.",
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = { engine.startGridCountdown() },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .height(54.dp)
              .testTag("launch_race_countdown_btn")
          ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = CarbonBlack, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "START RACE • LIGHTS OUT",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = CarbonBlack,
              letterSpacing = 1.sp
            )
          }
        }
      }
    }

    // 6. Interactive In-Game Driving Controls (Steering, Pedals, Boost & DRS)
    if (raceState == RaceState.RACING || raceState == RaceState.FINAL_LAP) {
      InGameDrivingControls(
        engine = engine,
        hudState = hudState,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 12.dp)
      )
    }

    // 7. Post-Race Podium & Rewards Modal
    if (raceState == RaceState.FINISHED_PODIUM && finishSummary != null) {
      PostRacePodiumDialog(
        summary = finishSummary!!,
        onRaceAgain = { engine.resetRace() },
        onViewProfile = onNavigateToProfile,
        onExit = onNavigateBack,
        isMph = isMph
      )
    }
  }
}

// -------------------------------------------------------------------
// TOP RACING HUD OVERLAY
// -------------------------------------------------------------------
@Composable
fun TopRacingHudOverlay(
  hudState: RacingGameHudState,
  track: Track,
  vehicle: Vehicle,
  perspective: CameraPerspective,
  onSwitchCamera: () -> Unit,
  onBack: () -> Unit,
  isMph: Boolean
) {
  val speedConverted = if (isMph) hudState.speedKmh * 0.621371f else hudState.speedKmh
  val speedUnit = if (isMph) "MPH" else "KM/H"

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(CarbonBlack.copy(alpha = 0.95f), CarbonBlack.copy(alpha = 0.5f), Color.Transparent)
        )
      )
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Back button & Position Badge
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onBack,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(CarbonSurfaceVariant)
            .testTag("racing_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Position Badge (e.g. P1 / 8)
        val rankColor = when (hudState.currentRank) {
          1 -> Color(0xFFFFD700) // Gold
          2 -> Color(0xFFC0C0C0) // Silver
          3 -> Color(0xFFCD7F32) // Bronze
          else -> NeonCyan
        }
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurface)
            .border(1.5.dp, rankColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("hud_position_badge")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "P${hudState.currentRank}",
              fontSize = 16.sp,
              fontWeight = FontWeight.Black,
              color = rankColor,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "/ ${hudState.totalRivals}",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary
            )
          }
        }
      }

      // Lap Counter (e.g. LAP 2 / 3)
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(CarbonSurface)
          .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("hud_lap_counter")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "LAP ${hudState.currentLap}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = if (hudState.currentLap == hudState.totalLaps) RedlineRed else TextPrimary,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = " / ${hudState.totalLaps}",
            fontSize = 9.sp,
            color = TextSecondary
          )
        }
      }

      // Camera Switcher
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(CarbonSurfaceVariant)
          .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
          .clickable(onClick = onSwitchCamera)
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .testTag("hud_camera_toggle")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = "Switch Camera",
            tint = NeonCyan,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = perspective.label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Real-Time Speedometer & Lap Delta Strip
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Speed, Gear & RPM
      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = String.format(Locale.US, "%.0f", speedConverted),
          fontSize = 24.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.testTag("hud_speed_value")
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = speedUnit,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = NeonAmber,
          modifier = Modifier.padding(bottom = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))

        // Gear
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(NeonCyan.copy(alpha = 0.15f))
            .border(1.dp, NeonCyan, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "GEAR ${hudState.gear}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Interval to Car Ahead / Behind
      Column(horizontalAlignment = Alignment.End) {
        if (hudState.intervalAheadSeconds != null) {
          Text(
            text = String.format(Locale.US, "+%.2fs AHEAD", hudState.intervalAheadSeconds),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = ApexGreen,
            fontFamily = FontFamily.Monospace
          )
        }
        if (hudState.intervalBehindSeconds != null) {
          Text(
            text = String.format(Locale.US, "-%.2fs BEHIND", hudState.intervalBehindSeconds),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = RedlineRed,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------------
// 4. START LIGHTS COUNTDOWN OVERLAY
// -------------------------------------------------------------------
@Composable
fun StartLightsOverlay(lightsCount: Int) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CarbonBlack.copy(alpha = 0.55f)),
    contentAlignment = Alignment.Center
  ) {
    Card(
      colors = CardDefaults.cardColors(containerColor = CarbonSurface),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(2.dp, CarbonBorder)
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = if (lightsCount == 6) "LIGHTS OUT AND AWAY WE GO!" else "GRID START SEQUENCE",
          fontSize = 14.sp,
          fontWeight = FontWeight.Black,
          color = if (lightsCount == 6) ApexGreen else TextPrimary,
          letterSpacing = 1.sp
        )

        // 5 Starting Lights
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          for (i in 1..5) {
            val isOn = lightsCount in i..5
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isOn) RedlineRed else CarbonBlack)
                .border(2.dp, if (isOn) RedlineRed else CarbonBorder, CircleShape)
            )
          }
        }

        Text(
          text = if (lightsCount == 6) "FULL THROTTLE!" else "HOLD REVS • AWAIT GREEN",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = if (lightsCount == 6) ApexGreen else NeonAmber
        )
      }
    }
  }
}

// -------------------------------------------------------------------
// 5. IN-GAME DRIVING CONTROLS (STEERING, PEDALS, NITRO & DRS)
// -------------------------------------------------------------------
@Composable
fun InGameDrivingControls(
  engine: InteractiveRacingEngine,
  hudState: RacingGameHudState,
  modifier: Modifier = Modifier
) {
  // Steer Button Pressed States
  val steerLeftSource = remember { MutableInteractionSource() }
  val isSteerLeftPressed by steerLeftSource.collectIsPressedAsState()

  val steerRightSource = remember { MutableInteractionSource() }
  val isSteerRightPressed by steerRightSource.collectIsPressedAsState()

  // Gas & Brake Pedal Pressed States
  val throttleSource = remember { MutableInteractionSource() }
  val isThrottlePressed by throttleSource.collectIsPressedAsState()

  val brakeSource = remember { MutableInteractionSource() }
  val isBrakePressed by brakeSource.collectIsPressedAsState()

  LaunchedEffect(isSteerLeftPressed, isSteerRightPressed) {
    engine.steerInput = when {
      isSteerLeftPressed && !isSteerRightPressed -> -1.0f
      isSteerRightPressed && !isSteerLeftPressed -> 1.0f
      else -> 0.0f
    }
  }

  LaunchedEffect(isThrottlePressed) {
    engine.throttleInput = if (isThrottlePressed) 1.0f else 0.0f
  }

  LaunchedEffect(isBrakePressed) {
    engine.brakeInput = if (isBrakePressed) 1.0f else 0.0f
  }

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Bottom
  ) {
    // Left Side: Steer Left & Steer Right Buttons
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.Bottom
    ) {
      // Steer Left Button
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(if (isSteerLeftPressed) NeonCyan else CarbonSurfaceVariant.copy(alpha = 0.85f))
          .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp))
          .clickable(
            interactionSource = steerLeftSource,
            indication = null,
            onClick = {}
          )
          .testTag("steer_left_btn"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowLeft,
          contentDescription = "Steer Left",
          tint = if (isSteerLeftPressed) CarbonBlack else NeonCyan,
          modifier = Modifier.size(38.dp)
        )
      }

      // Steer Right Button
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(if (isSteerRightPressed) NeonCyan else CarbonSurfaceVariant.copy(alpha = 0.85f))
          .border(1.5.dp, NeonCyan, RoundedCornerShape(16.dp))
          .clickable(
            interactionSource = steerRightSource,
            indication = null,
            onClick = {}
          )
          .testTag("steer_right_btn"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.KeyboardArrowRight,
          contentDescription = "Steer Right",
          tint = if (isSteerRightPressed) CarbonBlack else NeonCyan,
          modifier = Modifier.size(38.dp)
        )
      }
    }

    // Center Booster Controls: NITRO BOOST & DRS FLAP
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // NITRO / ERS BOOST BUTTON
      val nitroAvailable = hudState.nitroChargePercent >= 0.25f
      Box(
        modifier = Modifier
          .size(56.dp)
          .clip(CircleShape)
          .background(if (hudState.isNitroActive) NeonCyan else if (nitroAvailable) CarbonSurfaceVariant else CarbonBlack)
          .border(2.dp, if (nitroAvailable) NeonCyan else CarbonBorder, CircleShape)
          .clickable(enabled = nitroAvailable) { engine.triggerNitroBoost() }
          .testTag("nitro_boost_btn"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.ElectricBolt,
            contentDescription = "Nitro Boost",
            tint = if (hudState.isNitroActive) CarbonBlack else if (nitroAvailable) NeonCyan else TextTertiary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "NITRO",
            fontSize = 7.5.sp,
            fontWeight = FontWeight.Black,
            color = if (hudState.isNitroActive) CarbonBlack else if (nitroAvailable) NeonCyan else TextTertiary
          )
        }
      }

      // DRS Flap Button
      if (hudState.isDrsAvailable) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (hudState.isDrsActive) ApexGreen else CarbonSurfaceVariant)
            .border(1.dp, ApexGreen, RoundedCornerShape(6.dp))
            .clickable { engine.toggleDrs() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("drs_flap_btn")
        ) {
          Text(
            text = if (hudState.isDrsActive) "DRS OPEN" else "DRS ACTIVE",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            color = if (hudState.isDrsActive) CarbonBlack else ApexGreen
          )
        }
      }
    }

    // Right Side: Progressive Brake & Gas Pedals
    Row(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.Bottom
    ) {
      // Brake Pedal
      Box(
        modifier = Modifier
          .width(62.dp)
          .height(86.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(if (isBrakePressed) RedlineRed else CarbonSurfaceVariant.copy(alpha = 0.85f))
          .border(2.dp, RedlineRed, RoundedCornerShape(12.dp))
          .clickable(
            interactionSource = brakeSource,
            indication = null,
            onClick = {}
          )
          .testTag("brake_pedal_btn"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "BRAKE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (isBrakePressed) Color.White else RedlineRed
          )
          Spacer(modifier = Modifier.height(4.dp))
          Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = if (isBrakePressed) Color.White else RedlineRed,
            modifier = Modifier.size(24.dp)
          )
        }
      }

      // Gas / Throttle Pedal
      Box(
        modifier = Modifier
          .width(68.dp)
          .height(106.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(if (isThrottlePressed) ApexGreen else CarbonSurfaceVariant.copy(alpha = 0.85f))
          .border(2.dp, ApexGreen, RoundedCornerShape(14.dp))
          .clickable(
            interactionSource = throttleSource,
            indication = null,
            onClick = {}
          )
          .testTag("gas_pedal_btn"),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "GAS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = if (isThrottlePressed) CarbonBlack else ApexGreen
          )
          Spacer(modifier = Modifier.height(6.dp))
          Icon(
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = if (isThrottlePressed) CarbonBlack else ApexGreen,
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }
  }
}

// -------------------------------------------------------------------
// 6. POST-RACE PODIUM & REWARDS DIALOG
// -------------------------------------------------------------------
@Composable
fun PostRacePodiumDialog(
  summary: RaceFinishSummary,
  onRaceAgain: () -> Unit,
  onViewProfile: () -> Unit,
  onExit: () -> Unit,
  isMph: Boolean
) {
  val speedConverted = if (isMph) summary.topSpeedKmh * 0.621371f else summary.topSpeedKmh
  val speedUnit = if (isMph) "MPH" else "KM/H"

  AlertDialog(
    onDismissRequest = onExit,
    shape = RoundedCornerShape(16.dp),
    containerColor = CarbonSurface,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = null,
          tint = if (summary.finishingPosition == 1) Color(0xFFFFD700) else NeonCyan,
          modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (summary.finishingPosition == 1) "VICTORY! P1 CHAMPION" else "RACE FINISHED • P${summary.finishingPosition}",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary
        )
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // Room Database Save Notice
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ApexGreen.copy(alpha = 0.15f))
            .border(1.dp, ApexGreen, RoundedCornerShape(8.dp))
            .padding(6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ApexGreen, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "SAVED TO ROOM DATABASE TELEMETRY",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Black,
              color = ApexGreen,
              letterSpacing = 0.5.sp
            )
          }
        }

        // Stats Matrix
        Card(
          colors = CardDefaults.cardColors(containerColor = CarbonBlack),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PodiumStatRow("Finishing Rank", "P${summary.finishingPosition} of ${summary.totalRacers}")
            PodiumStatRow("Best Lap Time", String.format(Locale.US, "%.3fs", summary.bestLapMs / 1000f))
            PodiumStatRow("Top Speed", String.format(Locale.US, "%.1f %s", speedConverted, speedUnit))
            PodiumStatRow("Overtakes Made", "${summary.overtakesCount} Cars")
            PodiumStatRow("XP Earned", "+${summary.xpEarned} XP")
          }
        }

        // Newly Unlocked Badges (if any)
        if (summary.newlyUnlockedBadges.isNotEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(NeonAmber.copy(alpha = 0.15f))
              .border(1.dp, NeonAmber, RoundedCornerShape(8.dp))
              .padding(6.dp)
              .testTag("new_trophies_unlocked_box")
          ) {
            Column {
              Text(
                text = "NEW TROPHIES UNLOCKED",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NeonAmber
              )
              Text(
                text = summary.newlyUnlockedBadges.take(3).joinToString(" • "),
                fontSize = 8.5.sp,
                color = TextPrimary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onRaceAgain,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        modifier = Modifier.testTag("race_again_button")
      ) {
        Text("Race Again", color = CarbonBlack, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onViewProfile,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
      ) {
        Text("Driver Profile", color = TextPrimary)
      }
    }
  )
}

@Composable
private fun PodiumStatRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, fontSize = 9.5.sp, color = TextSecondary)
    Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan, fontFamily = FontFamily.Monospace)
  }
}
