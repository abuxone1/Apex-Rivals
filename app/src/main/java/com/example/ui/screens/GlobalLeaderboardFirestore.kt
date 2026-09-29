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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseAuthAndFirestoreService
import com.example.model.GlobalRaceRanking
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
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Composable function to display the global leaderboards,
 * fetching data from Firestore to show top race performers.
 */
@Composable
fun GlobalLeaderboardsFirestore(
  modifier: Modifier = Modifier,
  initialTrack: String = "All Tracks"
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val firestoreService = remember { FirebaseAuthAndFirestoreService(context) }

  var selectedTrack by remember { mutableStateOf(initialTrack) }
  var rankings by remember { mutableStateOf<List<GlobalRaceRanking>>(emptyList()) }
  var isLoading by remember { mutableStateOf(true) }

  fun loadRankings() {
    isLoading = true
    coroutineScope.launch {
      val result = firestoreService.fetchGlobalRankings(
        trackFilter = if (selectedTrack == "All Tracks") null else selectedTrack
      )
      rankings = result
      isLoading = false
    }
  }

  LaunchedEffect(selectedTrack) {
    loadRankings()
  }

  val tracks = listOf("All Tracks", "Monza GP", "Spa-Francorchamps", "Suzuka Circuit", "Nürburgring Nordschleife", "Silverstone GP")

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("global_leaderboard_firestore")
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(NeonCyan.copy(alpha = 0.15f))
            .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Cloud,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = "GLOBAL LEADERBOARDS",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 0.8.sp
          )
          Text(
            text = "Live Firestore Cloud Database",
            fontSize = 11.sp,
            color = ApexGreen
          )
        }
      }

      IconButton(
        onClick = { loadRankings() },
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Refresh from Firestore",
          tint = NeonCyan,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    // Track Filter Pills
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
    ) {
      items(tracks) { track ->
        val isSelected = track == selectedTrack
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) NeonCyan else CarbonSurface)
            .border(
              1.dp,
              if (isSelected) NeonCyan else CarbonBorder,
              RoundedCornerShape(16.dp)
            )
            .clickable { selectedTrack = track }
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          Text(
            text = track,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CarbonBlack else TextPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    if (isLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(top = 40.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(
            color = NeonCyan,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Syncing with Firestore...",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Top 3 Performers Podium
        if (rankings.size >= 3) {
          item {
            FirestorePodiumHeader(topPerformers = rankings.take(3))
          }
        }

        // Full List of Ranked Drivers
        itemsIndexed(rankings) { index, ranking ->
          FirestorePerformerRow(
            rank = index + 1,
            ranking = ranking
          )
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
        }
      }
    }
  }
}

@Composable
private fun FirestorePodiumHeader(topPerformers: List<GlobalRaceRanking>) {
  if (topPerformers.size < 3) return
  val first = topPerformers[0]
  val second = topPerformers[1]
  val third = topPerformers[2]

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(
        Brush.verticalGradient(
          colors = listOf(
            CarbonSurfaceVariant,
            CarbonSurface
          )
        )
      )
      .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = null,
          tint = NeonAmber,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "TOP RACE PERFORMERS",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = NeonAmber,
          letterSpacing = 1.sp
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
      ) {
        // 2nd Place
        PodiumPillar(ranking = second, rank = 2, accentColor = Color(0xFFC0C0C0), heightDp = 80)
        // 1st Place
        PodiumPillar(ranking = first, rank = 1, accentColor = Color(0xFFFFD700), heightDp = 100)
        // 3rd Place
        PodiumPillar(ranking = third, rank = 3, accentColor = Color(0xFFCD7F32), heightDp = 65)
      }
    }
  }
}

@Composable
private fun PodiumPillar(
  ranking: GlobalRaceRanking,
  rank: Int,
  accentColor: Color,
  heightDp: Int
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.width(96.dp)
  ) {
    Text(
      text = ranking.driverName,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary,
      maxLines = 1
    )
    Text(
      text = formatLapTimeStr(ranking.lapTimeMs),
      fontSize = 10.sp,
      fontWeight = FontWeight.Black,
      color = accentColor,
      fontFamily = FontFamily.Monospace
    )
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .width(80.dp)
        .height(heightDp.dp)
        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
        .background(accentColor.copy(alpha = 0.2f))
        .border(1.dp, accentColor, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "#$rank",
        fontSize = 20.sp,
        fontWeight = FontWeight.Black,
        color = accentColor
      )
    }
  }
}

@Composable
private fun FirestorePerformerRow(
  rank: Int,
  ranking: GlobalRaceRanking
) {
  val rankColor = when (rank) {
    1 -> Color(0xFFFFD700)
    2 -> Color(0xFFC0C0C0)
    3 -> Color(0xFFCD7F32)
    else -> TextSecondary
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Rank
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(rankColor.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "$rank",
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        color = rankColor
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Vehicle icon
    Icon(
      imageVector = if (ranking.vehicleType == VehicleType.MOTORBIKE) Icons.Default.DirectionsBike else Icons.Default.DirectionsCar,
      contentDescription = null,
      tint = NeonCyan,
      modifier = Modifier.size(16.dp)
    )

    Spacer(modifier = Modifier.width(8.dp))

    // Driver details
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = ranking.driverName,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        if (ranking.isVerifiedFirestore) {
          Spacer(modifier = Modifier.width(4.dp))
          Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Verified Firestore",
            tint = NeonCyan,
            modifier = Modifier.size(12.dp)
          )
        }
      }
      Text(
        text = "${ranking.trackName} • ${ranking.vehicleName}",
        fontSize = 10.sp,
        color = TextSecondary
      )
    }

    // Lap Time & Top Speed
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = formatLapTimeStr(ranking.lapTimeMs),
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        color = if (rank <= 3) ApexGreen else NeonCyan,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "${ranking.topSpeedKmh.toInt()} km/h",
        fontSize = 10.sp,
        color = TextTertiary,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

private fun formatLapTimeStr(ms: Long): String {
  val minutes = (ms / 60000)
  val seconds = (ms % 60000) / 1000
  val millis = ms % 1000
  return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis)
}
