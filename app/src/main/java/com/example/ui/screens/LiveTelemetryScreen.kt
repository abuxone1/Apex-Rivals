package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.components.RaceEngineerAudioCard
import com.example.ui.components.RaceTimer
import com.example.ui.components.ShareLapTimeDialog
import com.example.ui.components.rememberDeviceBatteryState
import com.example.util.RaceSpeechService
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVAILABLE_TRACKS
import com.example.model.AVAILABLE_VEHICLES
import com.example.model.Track
import com.example.model.Vehicle
import com.example.model.VehicleType
import com.example.ui.components.GForceMeter
import com.example.ui.components.LeanAngleGauge
import com.example.ui.components.RaceDashboard
import com.example.ui.components.TachometerGauge
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
fun LiveTelemetryScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  onPlayGame: () -> Unit = {},
  onNavigateToAnalyzer: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showShareDialog by remember { mutableStateOf(false) }
  var showCockpitDashboard by remember { mutableStateOf(true) }
  val metrics = uiState.liveMetrics
  val vehicle = uiState.activeVehicle
  val isCar = vehicle.type == VehicleType.CAR

  val context = LocalContext.current
  val speechService = remember { RaceSpeechService.getInstance(context) }
  val deviceBatteryState = rememberDeviceBatteryState()

  // Notify driver via engineer radio if device battery drops into low power during race
  LaunchedEffect(deviceBatteryState.isLow, deviceBatteryState.isCritical, deviceBatteryState.levelPercent) {
    if (deviceBatteryState.isLow || deviceBatteryState.isCritical) {
      speechService.announceBatteryWarning(
        batteryLevelPercent = deviceBatteryState.levelPercent,
        isCritical = deviceBatteryState.isCritical
      )
    }
  }

  // Monitor vehicle speed in real-time to trigger voice speed alerts
  LaunchedEffect(metrics.speedKmh) {
    speechService.checkSpeedAlert(metrics.speedKmh)
  }

  // Monitor completed laps to automatically announce lap times via TextToSpeech
  LaunchedEffect(metrics.currentLap, metrics.lastLapTimeMs) {
    if (metrics.currentLap > 1 && metrics.lastLapTimeMs != null && metrics.lastLapTimeMs > 0L) {
      val isBest = (metrics.lastLapTimeMs == metrics.bestLapTimeMs)
      speechService.announceLapTime(
        lapNumber = metrics.currentLap - 1,
        lapTimeMillis = metrics.lastLapTimeMs,
        isBestLap = isBest,
        deltaToBestMillis = if (metrics.bestLapTimeMs != null && !isBest) metrics.lastLapTimeMs - metrics.bestLapTimeMs else null
      )
    }
  }

  if (showShareDialog) {
    val lapToShare = metrics.bestLapTimeMs ?: metrics.lastLapTimeMs ?: metrics.currentLapTimeMs
    ShareLapTimeDialog(
      driverName = "Apex Driver",
      trackName = uiState.activeTrack.name,
      vehicleName = uiState.activeVehicle.name,
      vehicleType = uiState.activeVehicle.type,
      lapTimeMs = if (lapToShare > 0) lapToShare else 78420L,
      topSpeedKmh = metrics.topSpeedKmh,
      sector1Ms = metrics.sector1TimeMs,
      sector2Ms = metrics.sector2TimeMs,
      sector3Ms = metrics.sector3TimeMs,
      peakG = metrics.peakLateralG,
      onDismiss = { showShareDialog = false }
    )
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
      // Vehicle & Track Selector Header
      VehicleAndTrackSelector(
        vehicles = AVAILABLE_VEHICLES,
        selectedVehicle = vehicle,
        onSelectVehicle = { viewModel.selectVehicle(it) },
        tracks = AVAILABLE_TRACKS,
        selectedTrack = uiState.activeTrack,
        onSelectTrack = { viewModel.selectTrack(it) }
      )
    }

    item {
      // Modern Racing Game Action Card (Launch 3D Game)
      Card(
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.8f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onPlayGame)
          .testTag("launch_racing_game_hero_card")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.15f))
                .border(1.5.dp, NeonCyan, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Racing Game",
                tint = NeonCyan,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "GRAND PRIX RACING GAME",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Black,
                  color = TextPrimary,
                  letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(RedlineRed.copy(alpha = 0.2f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "3D PERSPECTIVE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = RedlineRed,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "8-Car grid race vs AI rivals • Chase & Cockpit Cam • Nitro Boost & Slipstream",
                fontSize = 9.5.sp,
                color = TextSecondary,
                lineHeight = 13.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onPlayGame,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.testTag("launch_racing_game_button")
          ) {
            Text(
              text = "RACE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              color = CarbonBlack
            )
          }
        }
      }
    }

    item {
      // Telemetry Corner & Delta Analyzer Banner Card
      Card(
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.7f)),
        modifier = Modifier
          .fillMaxWidth()
          .clickable(onClick = onNavigateToAnalyzer)
          .testTag("open_telemetry_analyzer_card")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(NeonAmber.copy(alpha = 0.15f))
                .border(1.5.dp, NeonAmber, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Corner & Delta Analyzer",
                tint = NeonAmber,
                modifier = Modifier.size(22.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "CORNER APEX & DELTA ANALYZER",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black,
                  color = TextPrimary,
                  letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(NeonAmber.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "NEW v7.0",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonAmber,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Turn-by-turn apex speeds, braking zones, dual-run comparison & theoretical best lap",
                fontSize = 9.5.sp,
                color = TextSecondary,
                lineHeight = 13.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onNavigateToAnalyzer,
            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("open_analyzer_button")
          ) {
            Text(
              text = "ANALYZE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              color = CarbonBlack
            )
          }
        }
      }
    }

    item {
      // Real-Time Lap Timing Strip & Delta
      LapTimingHudStrip(
        lap = metrics.currentLap,
        currentLapMs = metrics.currentLapTimeMs,
        bestLapMs = metrics.bestLapTimeMs,
        deltaMs = metrics.deltaVsGhostMs,
        currentSector = metrics.currentSector,
        s1 = metrics.sector1TimeMs,
        s2 = metrics.sector2TimeMs,
        onShareLap = { showShareDialog = true }
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .padding(2.dp)
        ) {
          Row {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (showCockpitDashboard) NeonCyan else Color.Transparent)
                .clickable { showCockpitDashboard = true }
                .padding(horizontal = 12.dp, vertical = 5.dp)
                .testTag("dashboard_mode_cockpit")
            ) {
              Text(
                text = "RACE DASHBOARD",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                color = if (showCockpitDashboard) CarbonBlack else TextSecondary
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (!showCockpitDashboard) NeonCyan else Color.Transparent)
                .clickable { showCockpitDashboard = false }
                .padding(horizontal = 12.dp, vertical = 5.dp)
                .testTag("dashboard_mode_classic")
            ) {
              Text(
                text = "CLASSIC TACHO",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                color = if (!showCockpitDashboard) CarbonBlack else TextSecondary
              )
            }
          }
        }
      }
    }

    item {
      if (showCockpitDashboard) {
        val throttleEst = (metrics.longitudinalG / 1.8f).coerceIn(0f, 1f)
        val brakeEst = (-metrics.longitudinalG / 2.5f).coerceIn(0f, 1f)

        RaceDashboard(
          speedKmh = metrics.speedKmh,
          gear = metrics.gear,
          rpm = metrics.rpm,
          maxRpm = metrics.maxRpm,
          redlineRpm = metrics.redlineRpm,
          throttlePercent = if (metrics.speedKmh > 5f && metrics.longitudinalG >= 0f) (0.35f + throttleEst * 0.65f).coerceIn(0f, 1f) else throttleEst,
          brakePercent = brakeEst,
          isMph = uiState.isMph,
          deltaLapTimeSeconds = metrics.deltaVsGhostMs / 1000f,
          batteryPercent = 0.82f,
          fuelPercent = 0.68f,
          tireTemps = listOf(metrics.tireTempFL, metrics.tireTempFR, metrics.tireTempRL, metrics.tireTempRR),
          drsAvailable = metrics.isDrsOrTuckInActive,
          ersMode = "HOTLAP",
          trackName = uiState.activeTrack.name,
          lapNumber = metrics.currentLap,
          totalLaps = 20,
          allowInteractiveDemo = true,
          onSpeedUnitToggle = { viewModel.toggleSpeedUnit() },
          onGearUp = { viewModel.shiftUp() },
          onGearDown = { viewModel.shiftDown() }
        )
      } else {
        TachometerGauge(
          rpm = metrics.rpm,
          maxRpm = metrics.maxRpm,
          redlineRpm = metrics.redlineRpm,
          speedKmh = metrics.speedKmh,
          gear = metrics.gear,
          isMph = uiState.isMph,
          onSpeedUnitToggle = { viewModel.toggleSpeedUnit() }
        )
      }
    }

    item {
      // Dynamic Telemetry Row: G-Force Circle + (Lean Angle for Motorbike OR Tire Temps for Car)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        GForceMeter(
          lateralG = metrics.lateralG,
          longitudinalG = metrics.longitudinalG,
          peakLateralG = metrics.peakLateralG,
          modifier = Modifier.weight(1f)
        )

        if (!isCar) {
          LeanAngleGauge(
            leanAngleDeg = metrics.leanAngleDeg,
            maxLeanDeg = vehicle.maxLeanAngleDeg,
            modifier = Modifier.weight(1f)
          )
        } else {
          TireTelemetryPanel(
            tempFL = metrics.tireTempFL,
            tempFR = metrics.tireTempFR,
            tempRL = metrics.tireTempRL,
            tempRR = metrics.tireTempRR,
            pressurePsi = metrics.tirePressurePsi,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    item {
      // Live Track Map Canvas with circuit telemetry
      TrackMapCanvas(
        track = uiState.activeTrack,
        playerProgress = metrics.trackProgress,
        ghostProgress = uiState.activeReplay?.telemetryFrames?.let { frames ->
          val idx = ((frames.size - 1) * metrics.trackProgress).toInt().coerceIn(0, frames.size - 1)
          frames[idx].trackProgress
        },
        rivalProgressList = uiState.liveMultiplayerRacers.filter { !it.isLocalPlayer }.map { it.trackProgress }
      )
    }

    item {
      // Live Telemetry Speed & Throttle Trace Graph
      uiState.activeReplay?.let { replay ->
        TelemetryTraceGraph(
          frames = replay.telemetryFrames,
          currentScrubProgress = metrics.trackProgress,
          maxSpeedKmh = vehicle.topSpeedKmh.toFloat()
        )
      }
    }

    item {
      // Interactive Race Lap Timer with StateFlow
      RaceTimer(modifier = Modifier.fillMaxWidth())
    }

    item {
      // Race Engineer Text-To-Speech Radio Comms & Speed Alerts
      RaceEngineerAudioCard(
        speechService = speechService,
        currentSpeedKmh = metrics.speedKmh
      )
    }

    item {
      // Interactive Cockpit Controls: Throttle, Brake, Steering, Shifts & DRS
      InteractiveCockpitControls(
        throttle = uiState.playerThrottleInput,
        onThrottleChange = { viewModel.setThrottleInput(it) },
        brake = uiState.playerBrakeInput,
        onBrakeChange = { viewModel.setBrakeInput(it) },
        steer = uiState.playerSteerInput,
        onSteerChange = { viewModel.setSteerInput(it) },
        onShiftUp = { viewModel.shiftUp() },
        onShiftDown = { viewModel.shiftDown() },
        isDrsOrTuckIn = metrics.isDrsOrTuckInActive,
        onToggleDrs = { viewModel.toggleDrsOrTuckIn() },
        isCar = isCar,
        onReset = { viewModel.resetDriveSession() }
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun VehicleAndTrackSelector(
  vehicles: List<Vehicle>,
  selectedVehicle: Vehicle,
  onSelectVehicle: (Vehicle) -> Unit,
  tracks: List<Track>,
  selectedTrack: Track,
  onSelectTrack: (Track) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
      .padding(12.dp)
  ) {
    Text(
      text = "SELECT VEHICLE & CIRCUIT",
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Vehicle Selection Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(vehicles) { v ->
        val isSelected = v.id == selectedVehicle.id
        val isCar = v.type == VehicleType.CAR

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CarbonSurfaceVariant)
            .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(10.dp))
            .clickable { onSelectVehicle(v) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("select_vehicle_${v.id}")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isCar) Icons.Default.DirectionsCar else Icons.Default.DirectionsBike,
              contentDescription = null,
              tint = if (isSelected) NeonCyan else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = v.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) TextPrimary else TextSecondary
              )
              Text(
                text = "${v.horsepower} HP • ${v.topSpeedKmh} KM/H",
                fontSize = 9.sp,
                color = if (isSelected) NeonCyan else TextSecondary,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Track Selection Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(tracks) { trk ->
        val isSelected = trk.id == selectedTrack.id
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonAmber.copy(alpha = 0.15f) else CarbonSurfaceVariant)
            .border(1.dp, if (isSelected) NeonAmber else CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { onSelectTrack(trk) }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("select_track_${trk.id}")
        ) {
          Text(
            text = trk.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) NeonAmber else TextSecondary
          )
        }
      }
    }
  }
}

@Composable
fun LapTimingHudStrip(
  lap: Int,
  currentLapMs: Long,
  bestLapMs: Long?,
  deltaMs: Long,
  currentSector: Int,
  s1: Long?,
  s2: Long?,
  onShareLap: (() -> Unit)? = null
) {
  val curMin = currentLapMs / 60000
  val curSec = (currentLapMs % 60000) / 1000
  val curMil = currentLapMs % 1000
  val curLapStr = String.format(Locale.US, "%02d:%02d.%03d", curMin, curSec, curMil)

  val bestLapStr = bestLapMs?.let {
    val bMin = it / 60000
    val bSec = (it % 60000) / 1000
    val bMil = it % 1000
    String.format(Locale.US, "%02d:%02d.%03d", bMin, bSec, bMil)
  } ?: "--:--.---"

  val isDeltaAhead = deltaMs <= 0
  val deltaSec = Math.abs(deltaMs) / 1000.0f
  val deltaStr = String.format(Locale.US, "%s%.3fs", if (isDeltaAhead) "-" else "+", deltaSec)
  val deltaColor = if (isDeltaAhead) ApexGreen else RedlineRed

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Lap counter
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(NeonCyan.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(text = "LAP $lap", fontSize = 11.sp, fontWeight = FontWeight.Black, color = NeonCyan)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = curLapStr,
          fontSize = 20.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          fontFamily = FontFamily.Monospace
        )
      }

      // Delta vs Ghost badge
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(deltaColor.copy(alpha = 0.15f))
          .border(1.dp, deltaColor, RoundedCornerShape(6.dp))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Text(
          text = "DELTA $deltaStr",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = deltaColor,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Best Lap, Share Button & Sectors
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "BEST: ", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(
          text = bestLapStr,
          fontSize = 10.sp,
          color = PurpleDelta,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )

        if (onShareLap != null) {
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(NeonCyan.copy(alpha = 0.15f))
              .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
              .clickable { onShareLap() }
              .padding(horizontal = 6.dp, vertical = 2.dp)
              .testTag("hud_share_lap_button")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Lap",
                tint = NeonCyan,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "SHARE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }

      // Sector Splits
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SectorSplitPill(num = 1, isActive = currentSector == 1, timeMs = s1)
        SectorSplitPill(num = 2, isActive = currentSector == 2, timeMs = s2)
        SectorSplitPill(num = 3, isActive = currentSector == 3, timeMs = null)
      }
    }
  }
}

@Composable
fun SectorSplitPill(num: Int, isActive: Boolean, timeMs: Long?) {
  val label = timeMs?.let {
    String.format(Locale.US, "%.2fs", it / 1000f)
  } ?: "S$num"

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(if (isActive) NeonAmber else CarbonSurfaceVariant)
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(
      text = label,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = if (isActive) CarbonBlack else TextSecondary,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun TireTelemetryPanel(
  tempFL: Float,
  tempFR: Float,
  tempRL: Float,
  tempRR: Float,
  pressurePsi: Float,
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
      text = "4-TIRE TELEMETRY",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Car Tire Diagram
    Column(
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        TireTempBox(label = "FL", tempC = tempFL)
        TireTempBox(label = "FR", tempC = tempFR)
      }
      Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
        TireTempBox(label = "RL", tempC = tempRL)
        TireTempBox(label = "RR", tempC = tempRR)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = String.format(Locale.US, "PRESSURE: %.1f PSI", pressurePsi),
      fontSize = 10.sp,
      fontWeight = FontWeight.SemiBold,
      color = NeonCyan,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun TireTempBox(label: String, tempC: Float) {
  val color = when {
    tempC > 105f -> RedlineRed
    tempC > 85f -> ApexGreen
    else -> NeonCyan
  }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(CarbonSurfaceVariant)
      .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Text(text = label, fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
    Text(
      text = "${tempC.toInt()}°C",
      fontSize = 12.sp,
      fontWeight = FontWeight.Black,
      color = color,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun InteractiveCockpitControls(
  throttle: Float,
  onThrottleChange: (Float) -> Unit,
  brake: Float,
  onBrakeChange: (Float) -> Unit,
  steer: Float,
  onSteerChange: (Float) -> Unit,
  onShiftUp: () -> Unit,
  onShiftDown: () -> Unit,
  isDrsOrTuckIn: Boolean,
  onToggleDrs: () -> Unit,
  isCar: Boolean,
  onReset: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(14.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "RACING CONTROLS & PEDALS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp
      )

      IconButton(
        onClick = onReset,
        modifier = Modifier.size(28.dp).testTag("reset_telemetry_button")
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Reset",
          tint = TextSecondary,
          modifier = Modifier.size(18.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Pedals Row: Brake & Throttle
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Brake Slider / Pedal
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "BRAKE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RedlineRed)
          Text(
            text = "${(brake * 100).toInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RedlineRed,
            fontFamily = FontFamily.Monospace
          )
        }
        Slider(
          value = brake,
          onValueChange = onBrakeChange,
          colors = SliderDefaults.colors(
            thumbColor = RedlineRed,
            activeTrackColor = RedlineRed,
            inactiveTrackColor = CarbonSurfaceVariant
          ),
          modifier = Modifier.testTag("brake_slider")
        )
      }

      // Throttle Slider / Pedal
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = "THROTTLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ApexGreen)
          Text(
            text = "${(throttle * 100).toInt()}%",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ApexGreen,
            fontFamily = FontFamily.Monospace
          )
        }
        Slider(
          value = throttle,
          onValueChange = onThrottleChange,
          colors = SliderDefaults.colors(
            thumbColor = ApexGreen,
            activeTrackColor = ApexGreen,
            inactiveTrackColor = CarbonSurfaceVariant
          ),
          modifier = Modifier.testTag("throttle_slider")
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Steering / Lean Slider
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = if (isCar) "STEERING RACK (APEX ANGLE)" else "BODY LEAN & COUNTER-STEER",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyan
        )
        Text(
          text = if (steer < -0.05f) "LEFT ${(steer * -100).toInt()}%" else if (steer > 0.05f) "RIGHT ${(steer * 100).toInt()}%" else "CENTER",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyan,
          fontFamily = FontFamily.Monospace
        )
      }
      Slider(
        value = steer,
        onValueChange = onSteerChange,
        valueRange = -1f..1f,
        colors = SliderDefaults.colors(
          thumbColor = NeonCyan,
          activeTrackColor = NeonCyan,
          inactiveTrackColor = CarbonSurfaceVariant
        ),
        modifier = Modifier.testTag("steering_slider")
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Gear Shifting & DRS / Tuck-in Buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = onShiftDown,
        colors = ButtonDefaults.buttonColors(containerColor = CarbonSurfaceVariant, contentColor = TextPrimary),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f).height(44.dp).testTag("gear_down_button")
      ) {
        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Gear Down")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "SHIFT DOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = onShiftUp,
        colors = ButtonDefaults.buttonColors(containerColor = CarbonSurfaceVariant, contentColor = TextPrimary),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f).height(44.dp).testTag("gear_up_button")
      ) {
        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Gear Up")
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "SHIFT UP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = onToggleDrs,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isDrsOrTuckIn) NeonAmber else CarbonSurfaceVariant,
          contentColor = if (isDrsOrTuckIn) CarbonBlack else NeonAmber
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1.2f).height(44.dp).testTag("drs_boost_button")
      ) {
        Icon(
          imageVector = if (isCar) Icons.Default.Air else Icons.Default.FlashOn,
          contentDescription = "Aero Boost"
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (isCar) (if (isDrsOrTuckIn) "DRS OPEN" else "OPEN DRS") else (if (isDrsOrTuckIn) "TUCKED IN" else "TUCK IN"),
          fontSize = 11.sp,
          fontWeight = FontWeight.Black
        )
      }
    }
  }
}
