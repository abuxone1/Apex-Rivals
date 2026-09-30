package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.RacePerformanceMetric
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
import com.example.ui.viewmodel.PerformanceSortOption
import com.example.ui.viewmodel.RacePerformanceViewModel
import java.util.Locale

/**
 * Interactive UI panel managing Room-persisted race performance metrics:
 * lap times, track names, vehicle speeds, dynamic filtering, sorting, and manual record entry.
 */
@Composable
fun RacePerformanceTelemetryPanel(
  viewModel: RacePerformanceViewModel,
  modifier: Modifier = Modifier,
  isMph: Boolean = false
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("telemetry_panel"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(CarbonSurfaceVariant.copy(alpha = 0.6f), CarbonBlack)
          )
        )
        .padding(14.dp)
    ) {

      // 1. Header Title & Quick Action
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(NeonCyan.copy(alpha = 0.15f))
              .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Storage,
              contentDescription = "Room Database Telemetry",
              tint = NeonCyan,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "PERFORMANCE METRICS SCHEMA",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
            Text(
              text = "Room Persistence • Lap Times, Track Name & Speed",
              fontSize = 9.sp,
              color = TextSecondary
            )
          }
        }

        Button(
          onClick = { showAddDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(8.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("telemetry_record_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = CarbonBlack,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "LOG LAP",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CarbonBlack,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. Summary Statistics Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TelemetryStatBadge(
          title = "BEST LAP",
          value = uiState.stats.formattedBestLap,
          accentColor = ApexGreen,
          modifier = Modifier.weight(1f)
        )
        TelemetryStatBadge(
          title = "PEAK SPEED",
          value = if (isMph) {
            String.format(Locale.US, "%.0f MPH", uiState.stats.maxVehicleSpeedKmh * 0.621371f)
          } else {
            String.format(Locale.US, "%.0f KM/H", uiState.stats.maxVehicleSpeedKmh)
          },
          accentColor = NeonCyan,
          modifier = Modifier.weight(1f)
        )
        TelemetryStatBadge(
          title = "TOTAL LAPS",
          value = "${uiState.stats.totalLapsRecorded}",
          accentColor = NeonAmber,
          modifier = Modifier.weight(0.8f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Search & Track Filters
      OutlinedTextField(
        value = uiState.searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        placeholder = { Text("Search by track, vehicle or driver...", fontSize = 11.sp, color = TextTertiary) },
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
          focusedContainerColor = CarbonBlack,
          unfocusedContainerColor = CarbonBlack,
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = CarbonBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("telemetry_search_field")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Track Filter Chips Row
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(uiState.availableTracks) { trackName ->
          val isSelected = trackName.equals(uiState.selectedTrackFilter, ignoreCase = true)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) NeonCyan else CarbonSurfaceVariant)
              .border(0.8.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(6.dp))
              .clickable { viewModel.setTrackFilter(trackName) }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("telemetry_track_filter_${trackName.replace(" ", "_")}")
          ) {
            Text(
              text = trackName.uppercase(Locale.US),
              fontSize = 9.sp,
              fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
              color = if (isSelected) CarbonBlack else TextSecondary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Sort Option Chips Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SORT:",
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextTertiary,
          fontFamily = FontFamily.Monospace
        )
        PerformanceSortOption.entries.forEach { option ->
          val isSelected = uiState.sortOption == option
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (isSelected) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant.copy(alpha = 0.6f))
              .border(0.8.dp, if (isSelected) NeonAmber else CarbonBorder, RoundedCornerShape(4.dp))
              .clickable { viewModel.setSortOption(option) }
              .padding(horizontal = 6.dp, vertical = 3.dp)
              .testTag("telemetry_sort_${option.name}")
          ) {
            Text(
              text = option.label,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) NeonAmber else TextTertiary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 4. Metric Items List
      if (uiState.displayedMetrics.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No telemetry records found. Tap 'LOG LAP' to record your first lap!",
            fontSize = 11.sp,
            color = TextTertiary
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          uiState.displayedMetrics.take(8).forEach { metric ->
            TelemetryMetricItemCard(
              metric = metric,
              isMph = isMph,
              onDelete = { viewModel.deleteMetric(metric.id) }
            )
          }
        }
      }
    }
  }

  // Add / Log Telemetry Dialog
  if (showAddDialog) {
    AddTelemetryRecordDialog(
      onDismiss = { showAddDialog = false },
      onSave = { track, lapSeconds, speed, vehicle, driver ->
        val lapMs = (lapSeconds * 1000f).toLong()
        viewModel.recordRacePerformance(
          trackName = track,
          lapTimeMs = lapMs,
          vehicleSpeed = speed,
          vehicleName = vehicle,
          driverName = driver
        )
        showAddDialog = false
      }
    )
  }
}

@Composable
private fun TelemetryStatBadge(
  title: String,
  value: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBlack)
      .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(8.dp)
  ) {
    Column {
      Text(
        text = title,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary,
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        color = accentColor,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun TelemetryMetricItemCard(
  metric: RacePerformanceMetric,
  isMph: Boolean,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonBlack)
      .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(10.dp)
      .testTag("telemetry_item_${metric.id}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        // Track Name & Mode
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = metric.trackName.uppercase(Locale.US),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 0.5.sp
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(CarbonSurfaceVariant)
              .padding(horizontal = 4.dp, vertical = 1.dp)
          ) {
            Text(
              text = "LAP ${metric.lapNumber}",
              fontSize = 7.5.sp,
              fontWeight = FontWeight.Bold,
              color = NeonAmber,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Lap Time & Speed values
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = ApexGreen,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = metric.formattedLapTime,
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = ApexGreen
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (isMph) metric.formattedSpeedMph else metric.formattedSpeed,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = NeonCyan
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Vehicle & Driver Info
        Text(
          text = "${metric.vehicleName} • Driver: ${metric.driverName}",
          fontSize = 8.5.sp,
          color = TextTertiary
        )
      }

      // Delete Button (48dp touch target accessible)
      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(36.dp)
          .testTag("telemetry_delete_${metric.id}")
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete telemetry record",
          tint = RedlineRed.copy(alpha = 0.7f),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
private fun AddTelemetryRecordDialog(
  onDismiss: () -> Unit,
  onSave: (track: String, lapSeconds: Float, speed: Float, vehicle: String, driver: String) -> Unit
) {
  var trackName by remember { mutableStateOf("Monza Speed Autodrome") }
  var lapSecondsStr by remember { mutableStateOf("78.42") }
  var speedStr by remember { mutableStateOf("328.5") }
  var vehicleName by remember { mutableStateOf("Apex GT3-R Twin-Turbo") }
  var driverName by remember { mutableStateOf("Apex Pilot") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "LOG PERFORMANCE TELEMETRY",
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = NeonCyan
      )
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        OutlinedTextField(
          value = trackName,
          onValueChange = { trackName = it },
          label = { Text("Track Name", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_input_track")
        )

        OutlinedTextField(
          value = lapSecondsStr,
          onValueChange = { lapSecondsStr = it },
          label = { Text("Lap Time (Seconds, e.g. 78.42)", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_input_lap_seconds")
        )

        OutlinedTextField(
          value = speedStr,
          onValueChange = { speedStr = it },
          label = { Text("Vehicle Speed (km/h)", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("telemetry_input_speed")
        )

        OutlinedTextField(
          value = vehicleName,
          onValueChange = { vehicleName = it },
          label = { Text("Vehicle Name", fontSize = 10.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val seconds = lapSecondsStr.toFloatOrNull() ?: 80.0f
          val speed = speedStr.toFloatOrNull() ?: 300.0f
          onSave(trackName, seconds, speed, vehicleName, driverName)
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        modifier = Modifier.testTag("telemetry_add_dialog_save")
      ) {
        Text("SAVE TO ROOM", color = CarbonBlack, fontWeight = FontWeight.Black)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("telemetry_add_dialog_cancel")
      ) {
        Text("CANCEL", color = TextSecondary)
      }
    },
    containerColor = CarbonSurface,
    shape = RoundedCornerShape(12.dp)
  )
}
