package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AppDatabase
import com.example.data.local.PerformanceMetrics
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
import com.example.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dialog allowing drivers to select a previous best lap from the PerformanceMetrics database
 * to use as their real-time Ghost comparison benchmark in the live Race Timer.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GhostLapSelectorDialog(
  currentGhostLap: PerformanceMetrics?,
  initialTrackFilter: String? = null,
  onSelectGhost: (PerformanceMetrics) -> Unit,
  onClearGhost: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val db = remember { AppDatabase.getDatabase(context) }
  val dao = remember { db.performanceMetricsDao() }
  val allMetrics by dao.getAllPerformanceMetrics().collectAsState(initial = emptyList())

  var selectedTrackFilter by remember {
    mutableStateOf(initialTrackFilter ?: "All Tracks")
  }

  // Derive unique tracks from recorded metrics
  val availableTracks = remember(allMetrics) {
    val tracks = linkedSetOf("All Tracks")
    allMetrics.forEach { if (it.trackName.isNotBlank()) tracks.add(it.trackName.trim()) }
    tracks.toList()
  }

  // Filter metrics according to track selection, sorted from fastest lap to slowest
  val filteredMetrics = remember(allMetrics, selectedTrackFilter) {
    val list = if (selectedTrackFilter == "All Tracks") {
      allMetrics
    } else {
      allMetrics.filter { it.trackName.equals(selectedTrackFilter, ignoreCase = true) }
    }
    list.sortedBy { it.lapTimeMs }
  }

  // Fastest lap overall
  val allTimeBestLap = remember(allMetrics) {
    allMetrics.minByOrNull { it.lapTimeMs }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = CarbonSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.85f)
        .testTag("ghost_lap_selector_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp)
      ) {
        // Dialog Header: Title, Ghost Icon, Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PurpleDelta.copy(alpha = 0.15f))
                .border(1.dp, PurpleDelta.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                tint = PurpleDelta,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "GHOST MODE BENCHMARK",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Select a best lap from DB to overlay on Live Timer",
                fontSize = 10.sp,
                color = TextSecondary
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close dialog",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Pick Best Lap Overall Banner
        if (allTimeBestLap != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(PurpleDelta.copy(alpha = 0.12f))
              .border(1.dp, PurpleDelta.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
              .clickable {
                onSelectGhost(allTimeBestLap)
                onDismiss()
              }
              .padding(10.dp)
              .testTag("quick_pick_fastest_ghost_button")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "⚡ FASTEST OVERALL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = PurpleDelta
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = allTimeBestLap.trackName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                }
                Text(
                  text = "${allTimeBestLap.driverName} • ${allTimeBestLap.vehicleName}",
                  fontSize = 10.sp,
                  color = TextSecondary
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = formatDuration(allTimeBestLap.lapTimeMs),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Black,
                  color = PurpleDelta,
                  fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = PurpleDelta,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        // Track Filter Chips Row
        Text(
          text = "FILTER BY TRACK",
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = TextTertiary,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(availableTracks) { track ->
            val isSelected = (track == selectedTrackFilter)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) NeonCyan else CarbonSurfaceVariant)
                .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(6.dp))
                .clickable { selectedTrackFilter = track }
                .padding(horizontal = 9.dp, vertical = 5.dp)
                .testTag("ghost_track_filter_${track.replace(" ", "_")}")
            ) {
              Text(
                text = track,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) CarbonBlack else TextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Performance Metrics List of candidate ghost laps
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .testTag("ghost_lap_candidates_list"),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (filteredMetrics.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "No saved laps found for $selectedTrackFilter",
                  fontSize = 11.sp,
                  color = TextTertiary
                )
              }
            }
          } else {
            items(filteredMetrics) { metric ->
              val isCurrentGhost = (currentGhostLap?.id == metric.id)
              val isBestInTrack = (filteredMetrics.firstOrNull()?.id == metric.id)

              Card(
                colors = CardDefaults.cardColors(
                  containerColor = if (isCurrentGhost) PurpleDelta.copy(alpha = 0.15f) else CarbonSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isCurrentGhost) PurpleDelta else if (isBestInTrack) ApexGreen.copy(alpha = 0.5f) else CarbonBorder
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onSelectGhost(metric)
                    onDismiss()
                  }
                  .testTag("ghost_candidate_${metric.id}")
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = if (isCurrentGhost) PurpleDelta else NeonCyan,
                        modifier = Modifier.size(14.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = metric.trackName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )

                      if (isBestInTrack) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(ApexGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                          Text(
                            text = "RECORD",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            color = ApexGreen
                          )
                        }
                      }

                      if (isCurrentGhost) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(PurpleDelta)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                          Text(
                            text = "ACTIVE GHOST",
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                          )
                        }
                      }
                    }

                    // Lap Time Display
                    Text(
                      text = formatDuration(metric.lapTimeMs),
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Black,
                      color = if (isCurrentGhost) PurpleDelta else if (isBestInTrack) ApexGreen else NeonCyan,
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  // Driver, Vehicle & Stats
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "${metric.driverName} • ${metric.vehicleName}",
                      fontSize = 10.sp,
                      color = TextSecondary
                    )

                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Text(
                        text = "Avg: ${String.format(Locale.US, "%.1f", metric.getComputedAvgSpeed())} km/h",
                        fontSize = 9.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                      )
                      Text(
                        text = "Top: ${metric.topSpeedKmh.toInt()} km/h",
                        fontSize = 9.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                      )
                    }
                  }

                  // Tags
                  if (metric.trackComplexity.isNotBlank() || metric.weatherCondition.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      if (metric.trackComplexity.isNotBlank()) {
                        Text(
                          text = "• ${metric.trackComplexity}",
                          fontSize = 8.5.sp,
                          color = NeonAmber
                        )
                      }
                      if (metric.weatherCondition.isNotBlank()) {
                        Text(
                          text = "• ${metric.weatherCondition}",
                          fontSize = 8.5.sp,
                          color = TextTertiary
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dialog Action Buttons: Clear Active Ghost, Cancel
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (currentGhostLap != null) {
            OutlinedButton(
              onClick = {
                onClearGhost()
                onDismiss()
              },
              colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RedlineRed
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("clear_ghost_button")
            ) {
              Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = RedlineRed,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "CLEAR GHOST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RedlineRed
              )
            }
          }

          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
              containerColor = CarbonSurfaceVariant,
              contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .testTag("close_ghost_selector_button")
          ) {
            Text(
              text = "CLOSE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
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
