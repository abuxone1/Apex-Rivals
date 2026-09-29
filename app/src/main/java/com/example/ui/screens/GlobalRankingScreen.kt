package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVAILABLE_TRACKS
import com.example.model.AVAILABLE_VEHICLES
import com.example.model.GlobalRaceRanking
import com.example.model.PlatformType
import com.example.model.VehicleType
import com.example.ui.components.MultiplayerPlatformBadge
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PurpleDelta
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RaceUiState
import com.example.ui.viewmodel.RaceViewModel

@Composable
fun GlobalRankingScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  onNavigateToReplay: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showUploadDialog by remember { mutableStateOf(false) }

  // Pulsing Live Indicator for Firestore Connection
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  val trackOptions = listOf(
    "All Tracks",
    "Monza GP",
    "Suzuka Circuit",
    "Nürburgring Nordschleife",
    "Silverstone GP",
    "Spa-Francorchamps",
    "Circuit de Monaco",
    "Red Bull Ring"
  )

  val vehicleClassOptions = listOf(
    "All Classes",
    "Cars Only",
    "Bikes Only"
  )

  val rankings = uiState.globalRankings
  val top3 = rankings.take(3)

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    // Sync Success Toast / Notification Banner
    uiState.firestoreSyncSuccessMessage?.let { message ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(ApexGreen.copy(alpha = 0.15f))
          .border(1.dp, ApexGreen, RoundedCornerShape(8.dp))
          .clickable { viewModel.dismissSyncMessage() }
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CloudDone,
              contentDescription = null,
              tint = ApexGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = message,
              fontSize = 11.sp,
              color = ApexGreen,
              fontWeight = FontWeight.Bold
            )
          }
          Text(text = "DISMISS", fontSize = 9.sp, color = TextSecondary)
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    // Top Header: Title & Live Firestore Badge
    Box(
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
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Cloud,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "GLOBAL FIRESTORE RANKINGS",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
          }
          Text(
            text = "SORTED BY BEST LAP TIMES • REAL-TIME LEADERBOARD",
            fontSize = 9.sp,
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Live Cloud Sync Indicator
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(ApexGreen.copy(alpha = 0.15f))
              .border(1.dp, ApexGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .padding(horizontal = 7.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(ApexGreen)
                  .alpha(pulseAlpha)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "FIRESTORE LIVE",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = ApexGreen,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Refresh Button
          IconButton(
            onClick = { viewModel.loadGlobalRankings() },
            modifier = Modifier
              .size(32.dp)
              .testTag("refresh_firestore_rankings_button")
          ) {
            if (uiState.isLoadingGlobalRankings) {
              CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 2.dp,
                modifier = Modifier.size(16.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Search and Upload Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = uiState.rankingSearchQuery,
        onValueChange = { viewModel.setRankingSearchQuery(it) },
        placeholder = { Text("Search driver, track or car...", fontSize = 11.sp, color = TextSecondary) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
          )
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = CarbonSurface,
          unfocusedContainerColor = CarbonSurface,
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = CarbonBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .height(48.dp)
          .testTag("ranking_search_field")
      )

      Button(
        onClick = { showUploadDialog = true },
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonCyan,
          contentColor = CarbonBlack
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .height(48.dp)
          .testTag("submit_lap_to_firestore_button")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "SYNC LAP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Track Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(trackOptions) { track ->
        val isSelected = uiState.selectedRankingTrack == track
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonCyan else CarbonSurface)
            .border(
              1.dp,
              if (isSelected) NeonCyan else CarbonBorder,
              RoundedCornerShape(8.dp)
            )
            .clickable { viewModel.setSelectedRankingTrack(track) }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("ranking_track_chip_$track"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = track,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
            color = if (isSelected) CarbonBlack else TextPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Vehicle Class Filter Chips
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      vehicleClassOptions.forEach { vClass ->
        val isSelected = uiState.selectedRankingVehicleClass == vClass
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant)
            .border(
              1.dp,
              if (isSelected) NeonAmber else CarbonBorder,
              RoundedCornerShape(6.dp)
            )
            .clickable { viewModel.setSelectedRankingVehicleClass(vClass) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = vClass,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) NeonAmber else TextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Podium Top 3 Showcase (if available)
    if (top3.isNotEmpty() && uiState.rankingSearchQuery.isBlank()) {
      Text(
        text = "TOP PODIUM CONTENDERS",
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = TextSecondary,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        top3.forEach { contender ->
          PodiumCard(
            ranking = contender,
            isMph = uiState.isMph,
            modifier = Modifier.weight(1f)
          )
        }
      }
      Spacer(modifier = Modifier.height(10.dp))
    }

    // Main Ranking List Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "GLOBAL LEADERBOARD (${rankings.size} ENTRIES)",
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = TextSecondary,
        letterSpacing = 1.sp
      )
      Text(
        text = "SORTED: FASTEST FIRST",
        fontSize = 9.sp,
        color = ApexGreen,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Rankings LazyColumn
    if (rankings.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .clip(RoundedCornerShape(12.dp))
          .background(CarbonSurface)
          .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "No Rankings Found",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Try clearing filters or sync your own lap record to Firestore!",
            fontSize = 11.sp,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = {
              viewModel.setSelectedRankingTrack("All Tracks")
              viewModel.setSelectedRankingVehicleClass("All Classes")
              viewModel.setRankingSearchQuery("")
            },
            colors = ButtonDefaults.buttonColors(containerColor = CarbonSurfaceVariant)
          ) {
            Text(text = "Reset All Filters", fontSize = 11.sp, color = NeonCyan)
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(rankings) { ranking ->
          GlobalRankingRow(
            ranking = ranking,
            isMph = uiState.isMph,
            onLoadGhost = {
              val vehicle = AVAILABLE_VEHICLES.find { it.name == ranking.vehicleName }
                ?: AVAILABLE_VEHICLES[0]
              val track = AVAILABLE_TRACKS.find { it.name == ranking.trackName }
                ?: AVAILABLE_TRACKS[0]
              viewModel.challengeLeaderboardRival(
                com.example.model.LeaderboardEntry(
                  rank = ranking.rank,
                  playerName = ranking.driverName,
                  platform = ranking.platform,
                  vehicleName = ranking.vehicleName,
                  vehicleType = ranking.vehicleType,
                  lapTimeMs = ranking.lapTimeMs,
                  sector1Ms = ranking.lapTimeMs / 3,
                  sector2Ms = ranking.lapTimeMs / 3,
                  sector3Ms = ranking.lapTimeMs / 3,
                  topSpeedKmh = ranking.topSpeedKmh,
                  inputDevice = ranking.inputDevice,
                  dateAchieved = ranking.formattedDate
                )
              )
              Toast.makeText(context, "Loaded ${ranking.driverName}'s ghost line!", Toast.LENGTH_SHORT).show()
              onNavigateToReplay()
            }
          )
        }
      }
    }
  }

  // Submit Lap to Firestore Dialog
  if (showUploadDialog) {
    SubmitLapDialog(
      activeTrack = uiState.activeTrack.name,
      activeVehicle = uiState.activeVehicle.name,
      suggestedLapTimeMs = uiState.liveMetrics.bestLapTimeMs ?: uiState.activeTrack.referenceLapTimeMs,
      topSpeed = uiState.liveMetrics.topSpeedKmh,
      onDismiss = { showUploadDialog = false },
      onSubmit = { driverName, trackName, vehicleName, lapTimeMs, topSpeedKmh ->
        viewModel.submitLapToFirestore(
          driverName = driverName,
          trackName = trackName,
          vehicleName = vehicleName,
          lapTimeMs = lapTimeMs,
          topSpeedKmh = topSpeedKmh
        )
        showUploadDialog = false
      }
    )
  }
}

@Composable
fun PodiumCard(
  ranking: GlobalRaceRanking,
  isMph: Boolean,
  modifier: Modifier = Modifier
) {
  val (podiumColor, badgeText) = when (ranking.rank) {
    1 -> Pair(PurpleDelta, "P1 GOLD")
    2 -> Pair(Color(0xFFE0E0E0), "P2 SILVER")
    3 -> Pair(Color(0xFFCD7F32), "P3 BRONZE")
    else -> Pair(NeonCyan, "P${ranking.rank}")
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(CarbonSurface)
      .border(1.dp, podiumColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
      .padding(8.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = badgeText,
          fontSize = 8.sp,
          fontWeight = FontWeight.Black,
          color = podiumColor,
          fontFamily = FontFamily.Monospace
        )
        Icon(
          imageVector = if (ranking.vehicleType == VehicleType.MOTORBIKE) Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
          contentDescription = null,
          tint = TextSecondary,
          modifier = Modifier.size(12.dp)
        )
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = ranking.driverName,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = ranking.formattedLapTime,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        color = if (ranking.rank == 1) PurpleDelta else NeonCyan,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = ranking.trackName,
        fontSize = 9.sp,
        color = TextSecondary,
        maxLines = 1
      )
    }
  }
}

@Composable
fun GlobalRankingRow(
  ranking: GlobalRaceRanking,
  isMph: Boolean,
  onLoadGhost: () -> Unit,
  modifier: Modifier = Modifier
) {
  val rankColor = when (ranking.rank) {
    1 -> PurpleDelta
    2 -> Color(0xFFE0E0E0)
    3 -> Color(0xFFCD7F32)
    else -> NeonCyan
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CarbonSurface)
      .border(
        1.dp,
        if (ranking.isLocalDriver) ApexGreen else CarbonBorder,
        RoundedCornerShape(12.dp)
      )
      .padding(10.dp)
      .testTag("ranking_row_${ranking.rank}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Position Badge & Driver Details
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // Rank Box
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(rankColor.copy(alpha = 0.15f))
            .border(1.dp, rankColor, RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "P${ranking.rank}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = rankColor,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Driver Info
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = ranking.driverName,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            if (ranking.isLocalDriver) {
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(ApexGreen.copy(alpha = 0.2f))
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "YOU",
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Black,
                  color = ApexGreen
                )
              }
            }
            Spacer(modifier = Modifier.width(6.dp))
            MultiplayerPlatformBadge(platform = ranking.platform)
          }

          Spacer(modifier = Modifier.height(2.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (ranking.vehicleType == VehicleType.MOTORBIKE) Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = TextSecondary,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "${ranking.trackName} • ${ranking.vehicleName}",
              fontSize = 9.sp,
              color = TextSecondary
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = ApexGreen,
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "Firestore Verified • ${ranking.formattedDate}",
              fontSize = 8.sp,
              color = TextSecondary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      // Right: Timing & Actions
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = ranking.formattedLapTime,
          fontSize = 14.sp,
          fontWeight = FontWeight.Black,
          color = if (ranking.rank == 1) PurpleDelta else NeonCyan,
          fontFamily = FontFamily.Monospace
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = ranking.formattedGap,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (ranking.rank == 1) PurpleDelta else NeonAmber,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.width(6.dp))
          val speedVal = if (isMph) (ranking.topSpeedKmh * 0.621371f).toInt() else ranking.topSpeedKmh.toInt()
          val speedUnit = if (isMph) "mph" else "km/h"
          Text(
            text = "$speedVal $speedUnit",
            fontSize = 9.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Ghost Challenge Button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
            .clickable { onLoadGhost() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
              text = "GHOST",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
fun SubmitLapDialog(
  activeTrack: String,
  activeVehicle: String,
  suggestedLapTimeMs: Long,
  topSpeed: Float,
  onDismiss: () -> Unit,
  onSubmit: (driverName: String, trackName: String, vehicleName: String, lapTimeMs: Long, topSpeedKmh: Float) -> Unit
) {
  var driverName by remember { mutableStateOf("Apex Racer") }
  var trackName by remember { mutableStateOf(activeTrack) }
  var vehicleName by remember { mutableStateOf(activeVehicle) }

  val minutes = (suggestedLapTimeMs / 60000)
  val seconds = (suggestedLapTimeMs % 60000) / 1000
  val millis = (suggestedLapTimeMs % 1000)
  val formattedTime = String.format(java.util.Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = CarbonSurface,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CloudUpload,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "SYNC TO FIRESTORE",
          fontSize = 14.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Upload your best telemetry lap to the Firebase Firestore global leaderboard for world ranking verification.",
          fontSize = 11.sp,
          color = TextSecondary
        )

        OutlinedTextField(
          value = driverName,
          onValueChange = { driverName = it },
          label = { Text("Driver Gamer Tag", fontSize = 11.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CarbonBlack,
            unfocusedContainerColor = CarbonBlack,
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CarbonBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonBlack)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(text = "CIRCUIT: $trackName", fontSize = 10.sp, color = TextSecondary)
            Text(text = "VEHICLE: $vehicleName", fontSize = 10.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "LAP TIME: $formattedTime",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "${topSpeed.toInt()} KM/H",
                fontSize = 11.sp,
                color = NeonAmber,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSubmit(driverName, trackName, vehicleName, suggestedLapTimeMs, topSpeed)
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonCyan,
          contentColor = CarbonBlack
        )
      ) {
        Text(text = "UPLOAD RECORD", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = "CANCEL", color = TextSecondary)
      }
    }
  )
}
