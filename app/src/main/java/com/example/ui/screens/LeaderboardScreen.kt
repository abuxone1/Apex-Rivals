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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVAILABLE_TRACKS
import com.example.model.LeaderboardEntry
import com.example.model.PlatformType
import com.example.model.Track
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
import java.util.Locale

@Composable
fun LeaderboardScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  onNavigateToReplay: () -> Unit,
  onNavigateToGlobalRankings: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val entries = uiState.leaderboardEntries
  val currentTrack = uiState.selectedLeaderboardTrack

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
  ) {
    // Mode Switcher Tabs: Circuit Records vs Firestore Cloud Live vs Room DB
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(RoundedCornerShape(8.dp))
          .background(if (selectedTab == 0) NeonCyan else CarbonSurface)
          .border(1.dp, if (selectedTab == 0) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
          .clickable { selectedTab = 0 }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "CIRCUIT",
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          color = if (selectedTab == 0) CarbonBlack else TextSecondary,
          letterSpacing = 0.5.sp
        )
      }

      Box(
        modifier = Modifier
          .weight(1.1f)
          .clip(RoundedCornerShape(8.dp))
          .background(if (selectedTab == 1) NeonCyan else CarbonSurface)
          .border(1.dp, if (selectedTab == 1) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
          .clickable { selectedTab = 1 }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = if (selectedTab == 1) CarbonBlack else ApexGreen,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "FIRESTORE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (selectedTab == 1) CarbonBlack else TextPrimary,
            letterSpacing = 0.5.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .weight(1.1f)
          .clip(RoundedCornerShape(8.dp))
          .background(if (selectedTab == 2) NeonCyan else CarbonSurface)
          .border(1.dp, if (selectedTab == 2) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
          .clickable { selectedTab = 2 }
          .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            tint = if (selectedTab == 2) CarbonBlack else NeonAmber,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "ROOM DB",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (selectedTab == 2) CarbonBlack else TextPrimary,
            letterSpacing = 0.5.sp
          )
        }
      }
    }

    if (selectedTab == 1) {
      GlobalLeaderboardsFirestore(
        modifier = Modifier.fillMaxSize()
      )
    } else if (selectedTab == 2) {
      PerformanceMetricsScreen(
        modifier = Modifier.fillMaxSize()
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Spacer(modifier = Modifier.height(2.dp))
          // Title Header
          LeaderboardHeader(
            track = currentTrack,
            onNavigateToGlobalRankings = { selectedTab = 1 }
          )
        }

    item {
      // Track Selector Filter Pills
      TrackFilterPills(
        tracks = AVAILABLE_TRACKS,
        selectedTrack = currentTrack,
        onSelectTrack = { trk ->
          viewModel.filterLeaderboard(trk, uiState.selectedVehicleFilter, uiState.selectedPlatformFilter)
        }
      )
    }

    item {
      // Vehicle Type & Platform Filter Pills
      VehicleAndPlatformFilters(
        selectedVehicleType = uiState.selectedVehicleFilter,
        onSelectVehicleType = { vType ->
          viewModel.filterLeaderboard(currentTrack, vType, uiState.selectedPlatformFilter)
        },
        selectedPlatform = uiState.selectedPlatformFilter,
        onSelectPlatform = { plat ->
          viewModel.filterLeaderboard(currentTrack, uiState.selectedVehicleFilter, plat)
        }
      )
    }

    item {
      // Personal Best Lap Highlight
      uiState.liveMetrics.bestLapTimeMs?.let { bestMs ->
        PersonalBestLapBanner(
          lapTimeMs = bestMs,
          vehicleName = uiState.activeVehicle.name,
          vehicleType = uiState.activeVehicle.type
        )
      }
    }

    // Leaderboard Rows
    items(entries) { entry ->
      LeaderboardRowCard(
        entry = entry,
        onChallenge = {
          viewModel.challengeLeaderboardRival(entry)
          onNavigateToReplay()
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
      }
    }
  }
}

@Composable
fun LeaderboardHeader(
  track: Track,
  onNavigateToGlobalRankings: () -> Unit = {}
) {
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
          text = "GLOBAL CROSS-PLATFORM LEADERBOARDS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan,
          letterSpacing = 1.sp
        )
        Text(
          text = "${track.name} (${track.location})",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }

      Icon(
        imageVector = Icons.Default.EmojiEvents,
        contentDescription = "Trophy",
        tint = NeonAmber,
        modifier = Modifier.size(28.dp)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
      onClick = onNavigateToGlobalRankings,
      colors = ButtonDefaults.buttonColors(
        containerColor = NeonCyan.copy(alpha = 0.15f),
        contentColor = NeonCyan
      ),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
        .testTag("open_firestore_global_rankings_button")
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Cloud,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "VIEW FIRESTORE CLOUD GLOBAL RANKINGS",
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}

@Composable
fun TrackFilterPills(
  tracks: List<Track>,
  selectedTrack: Track,
  onSelectTrack: (Track) -> Unit
) {
  LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    items(tracks) { trk ->
      val isSel = trk.id == selectedTrack.id
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else CarbonSurface)
          .border(1.dp, if (isSel) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
          .clickable { onSelectTrack(trk) }
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Text(
          text = trk.name,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isSel) NeonCyan else TextSecondary
        )
      }
    }
  }
}

@Composable
fun VehicleAndPlatformFilters(
  selectedVehicleType: VehicleType?,
  onSelectVehicleType: (VehicleType?) -> Unit,
  selectedPlatform: PlatformType?,
  onSelectPlatform: (PlatformType?) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
      .padding(10.dp)
  ) {
    // Vehicle Type Filters
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      FilterChip(
        label = "All Classes",
        isSelected = selectedVehicleType == null,
        onClick = { onSelectVehicleType(null) },
        modifier = Modifier.weight(1f)
      )
      FilterChip(
        label = "Cars Only",
        isSelected = selectedVehicleType == VehicleType.CAR,
        onClick = { onSelectVehicleType(VehicleType.CAR) },
        modifier = Modifier.weight(1f)
      )
      FilterChip(
        label = "Bikes Only",
        isSelected = selectedVehicleType == VehicleType.MOTORBIKE,
        onClick = { onSelectVehicleType(VehicleType.MOTORBIKE) },
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Platform Filter Row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      item {
        FilterChip(
          label = "Cross-Play (All)",
          isSelected = selectedPlatform == null,
          onClick = { onSelectPlatform(null) }
        )
      }
      items(PlatformType.values()) { plat ->
        FilterChip(
          label = plat.displayName,
          isSelected = selectedPlatform == plat,
          onClick = { onSelectPlatform(plat) }
        )
      }
    }
  }
}

@Composable
fun FilterChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant)
      .border(1.dp, if (isSelected) NeonAmber else CarbonBorder, RoundedCornerShape(6.dp))
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = if (isSelected) NeonAmber else TextSecondary
    )
  }
}

@Composable
fun PersonalBestLapBanner(
  lapTimeMs: Long,
  vehicleName: String,
  vehicleType: VehicleType
) {
  val minutes = (lapTimeMs / 60000)
  val seconds = (lapTimeMs % 60000) / 1000
  val millis = (lapTimeMs % 1000)
  val formattedTime = String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ApexGreen.copy(alpha = 0.12f))
      .border(1.dp, ApexGreen.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.SportsScore,
          contentDescription = null,
          tint = ApexGreen,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(text = "YOUR PERSONAL BEST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ApexGreen)
          Text(text = vehicleName, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
        }
      }

      Text(
        text = formattedTime,
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        color = ApexGreen,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
fun LeaderboardRowCard(
  entry: LeaderboardEntry,
  onChallenge: () -> Unit
) {
  val minutes = (entry.lapTimeMs / 60000)
  val seconds = (entry.lapTimeMs % 60000) / 1000
  val millis = (entry.lapTimeMs % 1000)
  val formattedTime = String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)

  val rankColor = when (entry.rank) {
    1 -> Color(0xFFFFD700) // Gold
    2 -> Color(0xFFC0C0C0) // Silver
    3 -> Color(0xFFCD7F32) // Bronze
    else -> TextSecondary
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
      .testTag("leaderboard_row_${entry.rank}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Rank Badge
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(rankColor.copy(alpha = 0.2f))
          .border(1.dp, rankColor, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "P${entry.rank}",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = rankColor,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Driver Name & Platform Badge
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = entry.playerName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))
          MultiplayerPlatformBadge(platform = entry.platform)
        }

        // Vehicle & Controller
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (entry.vehicleType == VehicleType.CAR) Icons.Default.DirectionsCar else Icons.Default.DirectionsBike,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${entry.vehicleName} • ${entry.inputDevice}",
            fontSize = 10.sp,
            color = TextSecondary
          )
        }
      }

      // Lap Time & Challenge Button
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = formattedTime,
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan,
          fontFamily = FontFamily.Monospace
        )

        Button(
          onClick = onChallenge,
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonAmber.copy(alpha = 0.15f),
            contentColor = NeonAmber
          ),
          shape = RoundedCornerShape(6.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier.height(28.dp).testTag("race_ghost_button_${entry.rank}")
        ) {
          Icon(imageVector = Icons.Default.PlayCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "Race Ghost", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Sector Splits Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "S1: ${String.format(Locale.US, "%.3fs", entry.sector1Ms / 1000f)}",
        fontSize = 10.sp,
        color = TextSecondary,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "S2: ${String.format(Locale.US, "%.3fs", entry.sector2Ms / 1000f)}",
        fontSize = 10.sp,
        color = TextSecondary,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "S3: ${String.format(Locale.US, "%.3fs", entry.sector3Ms / 1000f)}",
        fontSize = 10.sp,
        color = TextSecondary,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "${entry.topSpeedKmh.toInt()} km/h",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = NeonAmber,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
