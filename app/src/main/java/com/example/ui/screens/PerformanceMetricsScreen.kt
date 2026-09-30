package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.RacePerformanceTelemetryPanel
import com.example.ui.viewmodel.RacePerformanceViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.local.AppDatabase
import com.example.data.local.PerformanceMetrics
import com.example.model.VehicleType
import com.example.ui.components.PerformanceMetricsRechartsChart
import com.example.ui.components.ShareLapTimeDialog
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
import com.example.util.PerformanceMetricsCsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val RACE_TYPE_FILTERS = listOf("All Types", "Time Trial", "Circuit", "Sprint")

// Tag Categories for filtering & tagging
val TAG_FILTER_PRESETS = listOf(
  "All Tags",
  "Technical",
  "High Speed",
  "Flowing",
  "Extreme",
  "Dry Asphalt",
  "Wet Surface",
  "Heavy Rain",
  "Night Run"
)

@Composable
fun PerformanceMetricsScreen(
  modifier: Modifier = Modifier,
  telemetryViewModel: RacePerformanceViewModel = viewModel(),
  onNavigateToAnalyzer: () -> Unit = {},
  onNavigateBack: () -> Unit = {}
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val db = remember { AppDatabase.getDatabase(context) }
  val dao = remember { db.performanceMetricsDao() }

  val metricsList by dao.getAllPerformanceMetrics().collectAsState(initial = emptyList())
  var searchQuery by remember { mutableStateOf("") }
  var selectedRaceType by remember { mutableStateOf("All Types") }
  var selectedTagFilter by remember { mutableStateOf("All Tags") }
  var tagCategoryGroup by remember { mutableStateOf("All") } // "All", "Complexity", "Weather"
  var showExportDialog by remember { mutableStateOf(false) }
  var editingMetricForTags by remember { mutableStateOf<PerformanceMetrics?>(null) }
  var showNewRunDialog by remember { mutableStateOf(false) }
  var sharingMetric by remember { mutableStateOf<PerformanceMetrics?>(null) }

  // Seed initial sample telemetry records with rich tags, complexity, and weather conditions
  LaunchedEffect(Unit) {
    launch(Dispatchers.IO) {
      val existing = dao.getAllList()
      if (existing.isEmpty()) {
        val samples = listOf(
          PerformanceMetrics(
            raceId = "race_monza_01",
            driverName = "Max V.",
            trackName = "Monza GP",
            vehicleName = "Apex GT3-R Twin-Turbo",
            lapTimeMs = 78420L,
            topSpeedKmh = 328.6f,
            peakAccelerationG = 2.15f,
            zeroToHundredKmhSeconds = 2.75f,
            maxLateralG = 2.45f,
            trackDistanceMeters = 5793,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Monza GP", 78420L, 5793),
            raceType = "Time Trial",
            trackComplexity = "High Speed",
            weatherCondition = "Dry Asphalt",
            tags = listOf("High Speed", "Dry Asphalt", "Low Downforce"),
            timestamp = System.currentTimeMillis() - 3600_000 * 2
          ),
          PerformanceMetrics(
            raceId = "race_spa_02",
            driverName = "Lewis H.",
            trackName = "Spa-Francorchamps",
            vehicleName = "Formula Apex Hybrid",
            lapTimeMs = 104350L,
            topSpeedKmh = 345.2f,
            peakAccelerationG = 2.85f,
            zeroToHundredKmhSeconds = 2.20f,
            maxLateralG = 3.60f,
            trackDistanceMeters = 7004,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Spa-Francorchamps", 104350L, 7004),
            raceType = "Circuit",
            trackComplexity = "Technical",
            weatherCondition = "Wet Surface",
            tags = listOf("Technical", "Wet Surface", "Eau Rouge"),
            timestamp = System.currentTimeMillis() - 3600_000 * 6
          ),
          PerformanceMetrics(
            raceId = "race_nurb_03",
            driverName = "Valentino R.",
            trackName = "Nürburgring Nordschleife",
            vehicleName = "Pulse V4R Superbike",
            lapTimeMs = 398200L,
            topSpeedKmh = 312.4f,
            peakAccelerationG = 1.95f,
            zeroToHundredKmhSeconds = 2.65f,
            maxLateralG = 1.85f,
            trackDistanceMeters = 20832,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Nürburgring Nordschleife", 398200L, 20832),
            raceType = "Sprint",
            trackComplexity = "Extreme",
            weatherCondition = "Night Run",
            tags = listOf("Extreme", "Night Run", "Green Hell"),
            timestamp = System.currentTimeMillis() - 3600_000 * 18
          ),
          PerformanceMetrics(
            raceId = "race_suzuka_04",
            driverName = "Charles L.",
            trackName = "Suzuka Circuit",
            vehicleName = "Apex GT3-R Twin-Turbo",
            lapTimeMs = 119850L,
            topSpeedKmh = 298.7f,
            peakAccelerationG = 2.05f,
            zeroToHundredKmhSeconds = 2.80f,
            maxLateralG = 2.30f,
            trackDistanceMeters = 5807,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Suzuka Circuit", 119850L, 5807),
            raceType = "Time Trial",
            trackComplexity = "Technical",
            weatherCondition = "Dry Asphalt",
            tags = listOf("Technical", "Dry Asphalt", "Figure 8"),
            timestamp = System.currentTimeMillis() - 3600_000 * 24
          ),
          PerformanceMetrics(
            raceId = "race_silver_05",
            driverName = "Apex Pilot",
            trackName = "Silverstone GP",
            vehicleName = "Formula Apex Hybrid",
            lapTimeMs = 89200L,
            topSpeedKmh = 332.1f,
            peakAccelerationG = 2.70f,
            zeroToHundredKmhSeconds = 2.35f,
            maxLateralG = 3.40f,
            trackDistanceMeters = 5891,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Silverstone GP", 89200L, 5891),
            raceType = "Circuit",
            trackComplexity = "Flowing",
            weatherCondition = "Heavy Rain",
            tags = listOf("Flowing", "Heavy Rain", "Fast Sweepers"),
            timestamp = System.currentTimeMillis() - 3600_000 * 30
          ),
          PerformanceMetrics(
            raceId = "race_drag_06",
            driverName = "Francesco B.",
            trackName = "Monza GP",
            vehicleName = "Pulse V4R Superbike",
            lapTimeMs = 76100L,
            topSpeedKmh = 338.4f,
            peakAccelerationG = 2.95f,
            zeroToHundredKmhSeconds = 2.15f,
            maxLateralG = 2.10f,
            trackDistanceMeters = 5793,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Monza GP", 76100L, 5793),
            raceType = "Sprint",
            trackComplexity = "High Speed",
            weatherCondition = "Dry Asphalt",
            tags = listOf("High Speed", "Dry Asphalt", "Speed Traps"),
            timestamp = System.currentTimeMillis() - 3600_000 * 36
          )
        )
        dao.insertAll(samples)
      }
    }
  }

  // Filter metrics by driver name, track name, race type, and active tag filter
  val filteredMetrics = remember(metricsList, selectedRaceType, selectedTagFilter, searchQuery) {
    val q = searchQuery.trim()
    metricsList.filter { metric ->
      val matchesType = if (selectedRaceType == "All Types") true else metric.raceType.equals(selectedRaceType, ignoreCase = true)
      val matchesTag = if (selectedTagFilter == "All Tags") true else metric.matchesTag(selectedTagFilter)
      val matchesSearch = if (q.isEmpty()) true else {
        metric.driverName.contains(q, ignoreCase = true) ||
        metric.trackName.contains(q, ignoreCase = true) ||
        metric.getAllTags().any { it.contains(q, ignoreCase = true) }
      }
      matchesType && matchesTag && matchesSearch
    }
  }

  val bestLapMs = filteredMetrics.minOfOrNull { it.lapTimeMs }
  val maxTopSpeed = filteredMetrics.maxOfOrNull { it.topSpeedKmh } ?: 0f
  val maxAcceleration = filteredMetrics.maxOfOrNull { it.peakAccelerationG } ?: 0f
  val overallAvgSpeed = remember(filteredMetrics) {
    if (filteredMetrics.isNotEmpty()) {
      filteredMetrics.map { it.getComputedAvgSpeed() }.average().toFloat()
    } else 0f
  }

  // Collect all available tags across all sessions for dynamic tag filtering
  val allAvailableTags = remember(metricsList) {
    val set = linkedSetOf("All Tags")
    // Pre-populate standard presets first
    set.addAll(PerformanceMetrics.TRACK_COMPLEXITIES)
    set.addAll(PerformanceMetrics.WEATHER_CONDITIONS)
    metricsList.forEach { metric ->
      set.addAll(metric.getAllTags())
    }
    set.toList()
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .padding(horizontal = 14.dp, vertical = 8.dp)
      .testTag("performance_metrics_screen")
  ) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "RACE ENTRIES",
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan,
          letterSpacing = 1.sp
        )
        Text(
          text = "Telemetry DB • ${filteredMetrics.size} of ${metricsList.size} sessions",
          fontSize = 11.sp,
          color = TextSecondary
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        // Corner & Delta Analyzer Button
        Button(
          onClick = onNavigateToAnalyzer,
          colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
          shape = RoundedCornerShape(8.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("nav_to_analyzer_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = CarbonBlack,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "DELTA ANALYZER",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CarbonBlack,
            fontFamily = FontFamily.Monospace
          )
        }

        // Share Best Lap Run Button
        if (filteredMetrics.isNotEmpty()) {
          OutlinedButton(
            onClick = {
              val best = filteredMetrics.minByOrNull { it.lapTimeMs } ?: filteredMetrics.first()
              sharingMetric = best
            },
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = PurpleDelta,
              containerColor = CarbonSurfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleDelta.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("share_best_lap_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Lap Performance",
              tint = PurpleDelta,
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "SHARE",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = PurpleDelta
            )
          }
        }

        // Export CSV / Data Button
        OutlinedButton(
          onClick = { showExportDialog = true },
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NeonCyan,
            containerColor = CarbonSurfaceVariant
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .testTag("export_data_button")
            .testTag("export_csv_button")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Export Data",
            tint = NeonCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "EXPORT DATA",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
        }

        // Log Run Button with Tag & Category support
        Button(
          onClick = { showNewRunDialog = true },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("add_metric_button")
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Log Telemetry",
            tint = CarbonBlack,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "LOG RUN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = CarbonBlack
          )
        }
      }
    }

    // Search Bar for quick lookup by driver, track, or tags
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 3.dp)
        .testTag("race_entries_search_bar"),
      placeholder = {
        Text(
          text = "Search driver, track, or tag (e.g. Technical, Wet)...",
          fontSize = 12.sp,
          color = TextTertiary
        )
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Search",
          tint = if (searchQuery.isNotEmpty()) NeonCyan else TextTertiary,
          modifier = Modifier.size(18.dp)
        )
      },
      trailingIcon = {
        if (searchQuery.isNotEmpty()) {
          IconButton(
            onClick = { searchQuery = "" },
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Clear search",
              tint = TextSecondary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      },
      singleLine = true,
      shape = RoundedCornerShape(10.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = CarbonSurfaceVariant,
        unfocusedContainerColor = CarbonSurface,
        focusedBorderColor = NeonCyan,
        unfocusedBorderColor = CarbonBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        cursorColor = NeonCyan
      )
    )

    // Tag Filter Bar with Category Toggles (Complexity & Weather)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 3.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocalOffer,
            contentDescription = null,
            tint = NeonAmber,
            modifier = Modifier.size(12.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "TAG FILTERS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = TextTertiary,
            letterSpacing = 0.5.sp
          )
        }

        // Category Toggle Pills: All | Complexity | Weather
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.testTag("tag_category_toggle_group")
        ) {
          listOf("All", "Complexity", "Weather").forEach { cat ->
            val isCatSelected = (tagCategoryGroup == cat)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isCatSelected) NeonAmber.copy(alpha = 0.2f) else CarbonSurface)
                .border(0.8.dp, if (isCatSelected) NeonAmber else CarbonBorder, RoundedCornerShape(4.dp))
                .clickable {
                  tagCategoryGroup = cat
                  if (cat == "Complexity" && selectedTagFilter !in PerformanceMetrics.TRACK_COMPLEXITIES) {
                    selectedTagFilter = "All Tags"
                  } else if (cat == "Weather" && selectedTagFilter !in PerformanceMetrics.WEATHER_CONDITIONS) {
                    selectedTagFilter = "All Tags"
                  }
                }
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .testTag("tag_toggle_cat_${cat.lowercase(Locale.US)}")
            ) {
              Text(
                text = cat,
                fontSize = 9.sp,
                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isCatSelected) NeonAmber else TextSecondary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Filter Chips according to category
      val tagsToDisplay = remember(tagCategoryGroup, allAvailableTags) {
        when (tagCategoryGroup) {
          "Complexity" -> listOf("All Tags") + PerformanceMetrics.TRACK_COMPLEXITIES
          "Weather" -> listOf("All Tags") + PerformanceMetrics.WEATHER_CONDITIONS
          else -> allAvailableTags
        }
      }

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("tag_filters_row")
      ) {
        items(tagsToDisplay) { tag ->
          val isSelected = (selectedTagFilter == tag)
          val tagColor = getTagColor(tag)
          val tagIcon = getTagIcon(tag)

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) tagColor else CarbonSurface)
              .border(1.dp, if (isSelected) tagColor else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable {
                selectedTagFilter = if (isSelected && tag != "All Tags") "All Tags" else tag
              }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("tag_chip_${tag.lowercase(Locale.US).replace(" ", "_")}")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (tagIcon != null) {
                Icon(
                  imageVector = tagIcon,
                  contentDescription = null,
                  tint = if (isSelected) CarbonBlack else tagColor,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
              }
              Text(
                text = tag,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) CarbonBlack else TextPrimary
              )
              if (isSelected && tag != "All Tags") {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = CarbonBlack,
                  modifier = Modifier.size(10.dp)
                )
              }
            }
          }
        }
      }
    }

    // Race Type Filter Options (Time Trial, Circuit, Sprint, All)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "RACE TYPE",
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = TextTertiary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(end = 8.dp)
      )

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("race_type_filter_row")
      ) {
        items(RACE_TYPE_FILTERS) { raceType ->
          val isSelected = (selectedRaceType == raceType)
          val count = remember(metricsList, raceType, selectedTagFilter, searchQuery) {
            metricsList.count { m ->
              val matchesT = if (raceType == "All Types") true else m.raceType.equals(raceType, ignoreCase = true)
              val matchesTag = if (selectedTagFilter == "All Tags") true else m.matchesTag(selectedTagFilter)
              matchesT && matchesTag
            }
          }
          val accentColor = when (raceType) {
            "Time Trial" -> NeonCyan
            "Circuit" -> NeonAmber
            "Sprint" -> PurpleDelta
            else -> ApexGreen
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) accentColor else CarbonSurface)
              .border(1.dp, if (isSelected) accentColor else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable { selectedRaceType = raceType }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("filter_chip_${raceType.lowercase(Locale.US).replace(" ", "_")}")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = raceType,
                fontSize = 10.5.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                color = if (isSelected) CarbonBlack else TextPrimary
              )
              Spacer(modifier = Modifier.width(4.dp))
              Box(
                modifier = Modifier
                  .clip(CircleShape)
                  .background(if (isSelected) CarbonBlack.copy(alpha = 0.2f) else CarbonSurfaceVariant)
                  .padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "$count",
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) CarbonBlack else TextSecondary
                )
              }
            }
          }
        }
      }
    }

    // Summary Stats Cards (Filtered, including Overall Average Speed)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      MetricStatCard(
        label = "RECORDS",
        value = "${filteredMetrics.size}",
        accentColor = NeonCyan,
        modifier = Modifier.weight(1f)
      )
      MetricStatCard(
        label = "BEST LAP",
        value = bestLapMs?.let { formatLapTime(it) } ?: "--:--",
        accentColor = ApexGreen,
        modifier = Modifier.weight(1.2f)
      )
      MetricStatCard(
        label = "AVG SPEED",
        value = if (overallAvgSpeed > 0) "${overallAvgSpeed.toInt()} km/h" else "--",
        accentColor = NeonCyan,
        modifier = Modifier.weight(1.2f)
      )
      MetricStatCard(
        label = "TOP SPEED",
        value = if (maxTopSpeed > 0) "${maxTopSpeed.toInt()} km/h" else "--",
        accentColor = NeonAmber,
        modifier = Modifier.weight(1.1f)
      )
      MetricStatCard(
        label = "PEAK G",
        value = if (maxAcceleration > 0) String.format(Locale.US, "%.2fG", maxAcceleration) else "--",
        accentColor = RedlineRed,
        modifier = Modifier.weight(0.9f)
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Scrollable column displaying Recharts line chart & past race entries
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("performance_metrics_list"),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Room Database Performance Telemetry Panel (ViewModel + Room Schema)
      item {
        RacePerformanceTelemetryPanel(
          viewModel = telemetryViewModel,
          modifier = Modifier.padding(bottom = 6.dp)
        )
      }

      if (filteredMetrics.isNotEmpty()) {
        item {
          PerformanceMetricsRechartsChart(
            metrics = filteredMetrics,
            modifier = Modifier.padding(bottom = 4.dp)
          )
        }
      }

      if (filteredMetrics.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 30.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "No races match filter [Tag: $selectedTagFilter, Type: $selectedRaceType]",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
              )
              Spacer(modifier = Modifier.height(4.dp))
              TextButton(onClick = {
                selectedTagFilter = "All Tags"
                selectedRaceType = "All Types"
                searchQuery = ""
              }) {
                Text(text = "Reset All Filters", color = NeonCyan, fontSize = 12.sp)
              }
            }
          }
        }
      } else {
        itemsIndexed(filteredMetrics, key = { _, metric -> metric.id }) { index, metric ->
          // Identify previous session chronologically for speed trend comparison
          val prevMetric = remember(metric, filteredMetrics, index) {
            filteredMetrics.filter { it.timestamp < metric.timestamp }
              .maxByOrNull { it.timestamp }
              ?: if (index + 1 < filteredMetrics.size) filteredMetrics[index + 1] else null
          }

          val speedDelta = remember(metric, prevMetric) {
            prevMetric?.let { metric.getComputedAvgSpeed() - it.getComputedAvgSpeed() }
          }

          PerformanceMetricCard(
            metric = metric,
            isBestLap = (metric.lapTimeMs == bestLapMs),
            speedDelta = speedDelta,
            onShare = {
              sharingMetric = metric
            },
            onTagClick = { tag ->
              selectedTagFilter = tag
            },
            onEditTags = {
              editingMetricForTags = metric
            },
            onDelete = {
              coroutineScope.launch(Dispatchers.IO) {
                dao.deleteById(metric.id)
              }
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Edit Tags Dialog
  editingMetricForTags?.let { targetMetric ->
    EditTagsDialog(
      metric = targetMetric,
      onDismiss = { editingMetricForTags = null },
      onSave = { updatedMetric ->
        coroutineScope.launch(Dispatchers.IO) {
          dao.update(updatedMetric)
        }
        editingMetricForTags = null
      }
    )
  }

  // Log New Run Dialog with Tagging System
  if (showNewRunDialog) {
    LogNewRunDialog(
      onDismiss = { showNewRunDialog = false },
      onSave = { newEntry ->
        coroutineScope.launch(Dispatchers.IO) {
          dao.insert(newEntry)
        }
        showNewRunDialog = false
      }
    )
  }

  // Export CSV Dialog
  if (showExportDialog) {
    ExportTelemetryCsvDialog(
      filteredMetrics = filteredMetrics,
      allMetrics = metricsList,
      onDismiss = { showExportDialog = false },
      onExportSave = { listToExport ->
        PerformanceMetricsCsvExporter.saveCsvToDeviceStorage(context, listToExport)
        showExportDialog = false
      },
      onExportShare = { listToExport ->
        PerformanceMetricsCsvExporter.exportAndShareCsv(context, listToExport)
        showExportDialog = false
      },
      onExportCopy = { listToExport ->
        PerformanceMetricsCsvExporter.copyCsvToClipboard(context, listToExport)
        showExportDialog = false
      }
    )
  }

  // Social Media Share Dialog for Race Entries (Android Share Intent)
  sharingMetric?.let { metric ->
    val isMotorbike = metric.vehicleName.contains("Bike", ignoreCase = true) ||
                      metric.vehicleName.contains("Yamaha", ignoreCase = true) ||
                      metric.vehicleName.contains("Ducati", ignoreCase = true)
    ShareLapTimeDialog(
      driverName = metric.driverName,
      trackName = metric.trackName,
      vehicleName = metric.vehicleName,
      vehicleType = if (isMotorbike) VehicleType.MOTORBIKE else VehicleType.CAR,
      lapTimeMs = metric.lapTimeMs,
      topSpeedKmh = metric.topSpeedKmh,
      peakG = if (metric.peakAccelerationG > 0) metric.peakAccelerationG else 2.5f,
      onDismiss = { sharingMetric = null }
    )
  }
}

/**
 * Dialog allowing users to tag and categorize an existing race by track complexity,
 * weather condition, and custom tags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTagsDialog(
  metric: PerformanceMetrics,
  onDismiss: () -> Unit,
  onSave: (PerformanceMetrics) -> Unit
) {
  var selectedComplexity by remember { mutableStateOf(metric.trackComplexity) }
  var selectedWeather by remember { mutableStateOf(metric.weatherCondition) }
  var customTagInput by remember { mutableStateOf("") }
  var activeTags by remember { mutableStateOf(metric.tags.toMutableList()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.LocalOffer,
          contentDescription = null,
          tint = NeonAmber,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "CATEGORIZE RACE",
          fontSize = 15.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "${metric.trackName} • ${metric.driverName}",
          fontSize = 11.sp,
          color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Track Complexity Selector
        Text(
          text = "TRACK COMPLEXITY",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          PerformanceMetrics.TRACK_COMPLEXITIES.forEach { complexity ->
            val isSel = (selectedComplexity == complexity)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) NeonAmber else CarbonSurfaceVariant)
                .border(1.dp, if (isSel) NeonAmber else CarbonBorder, RoundedCornerShape(6.dp))
                .clickable { selectedComplexity = complexity }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("dialog_complexity_$complexity")
            ) {
              Text(
                text = complexity,
                fontSize = 11.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) CarbonBlack else TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Weather Conditions Selector
        Text(
          text = "WEATHER CONDITION",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          PerformanceMetrics.WEATHER_CONDITIONS.forEach { weather ->
            val isSel = (selectedWeather == weather)
            val weatherColor = when (weather) {
              "Dry Asphalt" -> NeonAmber
              "Wet Surface", "Heavy Rain" -> NeonCyan
              "Night Run" -> PurpleDelta
              else -> ApexGreen
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) weatherColor else CarbonSurfaceVariant)
                .border(1.dp, if (isSel) weatherColor else CarbonBorder, RoundedCornerShape(6.dp))
                .clickable { selectedWeather = weather }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("dialog_weather_$weather")
            ) {
              Text(
                text = weather,
                fontSize = 11.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) CarbonBlack else TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Tags Input
        Text(
          text = "CUSTOM TAGS",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customTagInput,
            onValueChange = { customTagInput = it },
            placeholder = { Text("Add custom tag...", fontSize = 11.sp, color = TextTertiary) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = CarbonSurfaceVariant,
              unfocusedContainerColor = CarbonSurfaceVariant,
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = CarbonBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = {
              val tag = customTagInput.trim()
              if (tag.isNotEmpty() && tag !in activeTags) {
                activeTags = (activeTags + tag).toMutableList()
                customTagInput = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(6.dp)
          ) {
            Text("ADD", fontSize = 11.sp, color = CarbonBlack, fontWeight = FontWeight.Bold)
          }
        }

        if (activeTags.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            activeTags.forEach { tag ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(CarbonSurfaceVariant)
                  .border(0.5.dp, CarbonBorder, RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = tag, fontSize = 10.sp, color = TextPrimary)
                  Spacer(modifier = Modifier.width(4.dp))
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove tag",
                    tint = TextTertiary,
                    modifier = Modifier
                      .size(10.dp)
                      .clickable {
                        activeTags = activeTags.filter { it != tag }.toMutableList()
                      }
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val updated = metric.copy(
            trackComplexity = selectedComplexity,
            weatherCondition = selectedWeather,
            tags = activeTags
          )
          onSave(updated)
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        shape = RoundedCornerShape(6.dp)
      ) {
        Text("SAVE TAGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CarbonBlack)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", fontSize = 11.sp, color = TextTertiary)
      }
    },
    containerColor = CarbonSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

/**
 * Dialog to log a new telemetry run with track complexity and weather categorization.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogNewRunDialog(
  onDismiss: () -> Unit,
  onSave: (PerformanceMetrics) -> Unit
) {
  var track by remember { mutableStateOf("Monza GP") }
  var raceType by remember { mutableStateOf("Time Trial") }
  var complexity by remember { mutableStateOf("High Speed") }
  var weather by remember { mutableStateOf("Dry Asphalt") }
  val availableTracks = listOf("Monza GP", "Spa-Francorchamps", "Suzuka Circuit", "Silverstone GP", "Nürburgring Nordschleife")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "LOG NEW RUN", fontSize = 16.sp, fontWeight = FontWeight.Black, color = NeonCyan)
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "SELECT CIRCUIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          availableTracks.forEach { t ->
            val isSel = (track == t)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) NeonCyan else CarbonSurfaceVariant)
                .clickable {
                  track = t
                  complexity = when {
                    t.contains("Monza") -> "High Speed"
                    t.contains("Spa") -> "Technical"
                    t.contains("Nürburgring") -> "Extreme"
                    t.contains("Silverstone") -> "Flowing"
                    else -> "Technical"
                  }
                }
                .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
              Text(
                text = t,
                fontSize = 10.5.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) CarbonBlack else TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "TRACK COMPLEXITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          PerformanceMetrics.TRACK_COMPLEXITIES.forEach { c ->
            val isSel = (complexity == c)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) NeonAmber else CarbonSurfaceVariant)
                .clickable { complexity = c }
                .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
              Text(
                text = c,
                fontSize = 10.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) CarbonBlack else TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "WEATHER CONDITION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextTertiary)
        Spacer(modifier = Modifier.height(4.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          PerformanceMetrics.WEATHER_CONDITIONS.forEach { w ->
            val isSel = (weather == w)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) NeonCyan else CarbonSurfaceVariant)
                .clickable { weather = w }
                .padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
              Text(
                text = w,
                fontSize = 10.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) CarbonBlack else TextPrimary
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val assignedLapTime = when {
            track.contains("Monza") -> (74000L..81000L).random()
            track.contains("Spa") -> (102000L..109000L).random()
            track.contains("Nürburgring") -> (390000L..420000L).random()
            else -> (88000L..115000L).random()
          }
          val distance = PerformanceMetrics.getEstimatedTrackDistance(track)
          val calculatedAvg = PerformanceMetrics.calculateAverageSpeed(track, assignedLapTime, distance)

          val entry = PerformanceMetrics(
            raceId = "race_${System.currentTimeMillis()}",
            driverName = listOf("Apex Pilot", "Max V.", "Lewis H.", "Charles L.", "Francesco B.").random(),
            trackName = track,
            vehicleName = listOf("Apex GT3-R Twin-Turbo", "Formula Apex Hybrid", "Pulse V4R Superbike").random(),
            lapTimeMs = assignedLapTime,
            topSpeedKmh = (2950..3450).random() / 10f,
            peakAccelerationG = (190..290).random() / 100f,
            zeroToHundredKmhSeconds = (210..310).random() / 100f,
            maxLateralG = (210..350).random() / 100f,
            trackDistanceMeters = distance,
            avgSpeedKmh = calculatedAvg,
            raceType = raceType,
            trackComplexity = complexity,
            weatherCondition = weather,
            tags = listOf(complexity, weather),
            timestamp = System.currentTimeMillis()
          )
          onSave(entry)
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        shape = RoundedCornerShape(6.dp)
      ) {
        Text("CREATE RUN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CarbonBlack)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("CANCEL", fontSize = 11.sp, color = TextTertiary)
      }
    },
    containerColor = CarbonSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
fun ExportTelemetryCsvDialog(
  filteredMetrics: List<PerformanceMetrics>,
  allMetrics: List<PerformanceMetrics>,
  onDismiss: () -> Unit,
  onExportSave: (List<PerformanceMetrics>) -> Unit,
  onExportShare: (List<PerformanceMetrics>) -> Unit,
  onExportCopy: (List<PerformanceMetrics>) -> Unit
) {
  var exportScopeAll by remember { mutableStateOf(false) }
  val targetList = if (exportScopeAll) allMetrics else filteredMetrics

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("export_data_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "EXPORT DATA (CSV)",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          color = NeonCyan
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Export telemetry performance metrics from Room database into standard RFC 4180 CSV format for Microsoft Excel, Google Sheets, or data analysis tools.",
          fontSize = 12.sp,
          color = TextSecondary,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Export Scope Selection
        Text(
          text = "SELECT DATASET SCOPE",
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          color = TextTertiary,
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!exportScopeAll) NeonCyan else CarbonSurfaceVariant)
              .border(1.dp, if (!exportScopeAll) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable { exportScopeAll = false }
              .padding(vertical = 8.dp, horizontal = 10.dp)
              .testTag("export_scope_filtered")
          ) {
            Column {
              Text(
                text = "FILTERED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (!exportScopeAll) CarbonBlack else TextPrimary
              )
              Text(
                text = "${filteredMetrics.size} sessions",
                fontSize = 10.sp,
                color = if (!exportScopeAll) CarbonBlack.copy(alpha = 0.8f) else TextSecondary
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (exportScopeAll) NeonCyan else CarbonSurfaceVariant)
              .border(1.dp, if (exportScopeAll) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable { exportScopeAll = true }
              .padding(vertical = 8.dp, horizontal = 10.dp)
              .testTag("export_scope_all")
          ) {
            Column {
              Text(
                text = "ALL SESSIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (exportScopeAll) CarbonBlack else TextPrimary
              )
              Text(
                text = "${allMetrics.size} total in DB",
                fontSize = 10.sp,
                color = if (exportScopeAll) CarbonBlack.copy(alpha = 0.8f) else TextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CSV Header Schema Preview Box
        Text(
          text = "INCLUDED CSV COLUMNS (17):",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
            .padding(8.dp)
        ) {
          Text(
            text = "Session ID, Driver Name, Track Name, Vehicle Name, Race Type, Track Complexity, Weather Condition, Tags, Lap Time, Top Speed, 0-100, Peak G, Lat G, Avg Speed, Date, Timestamp",
            fontSize = 8.5.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace,
            lineHeight = 12.sp
          )
        }

        if (targetList.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          val sample = targetList.first()
          Text(
            text = "SAMPLE ROW PREVIEW:",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary
          )
          Spacer(modifier = Modifier.height(3.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(CarbonBlack)
              .border(0.8.dp, CarbonBorder, RoundedCornerShape(6.dp))
              .padding(6.dp)
          ) {
            Text(
              text = "${sample.id},\"${sample.driverName}\",\"${sample.trackName}\",\"${sample.vehicleName}\",${sample.lapTimeMs}ms,${sample.topSpeedKmh.toInt()}km/h",
              fontSize = 8.sp,
              color = ApexGreen,
              fontFamily = FontFamily.Monospace,
              maxLines = 2
            )
          }
        }
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Save to Storage Button
        Button(
          onClick = { onExportSave(targetList) },
          colors = ButtonDefaults.buttonColors(containerColor = ApexGreen),
          shape = RoundedCornerShape(6.dp),
          enabled = targetList.isNotEmpty(),
          modifier = Modifier
            .weight(1f)
            .testTag("confirm_export_save_button")
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = CarbonBlack,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "SAVE FILE",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = CarbonBlack
          )
        }

        // Share File Button
        Button(
          onClick = { onExportShare(targetList) },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
          shape = RoundedCornerShape(6.dp),
          enabled = targetList.isNotEmpty(),
          modifier = Modifier
            .weight(1f)
            .testTag("confirm_export_share_button")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = CarbonBlack,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "SHARE FILE",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = CarbonBlack
          )
        }
      }
    },
    dismissButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = { onExportCopy(targetList) },
          enabled = targetList.isNotEmpty(),
          modifier = Modifier.testTag("confirm_export_copy_button")
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = "COPY CSV", fontSize = 11.sp, color = NeonCyan)
        }

        TextButton(onClick = onDismiss) {
          Text(text = "CANCEL", fontSize = 11.sp, color = TextTertiary)
        }
      }
    },
    containerColor = CarbonSurface,
    shape = RoundedCornerShape(12.dp)
  )
}

@Composable
private fun MetricStatCard(
  label: String,
  value: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(horizontal = 6.dp, vertical = 6.dp)
  ) {
    Column {
      Text(
        text = label,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary,
        letterSpacing = 0.5.sp
      )
      Text(
        text = value,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Black,
        color = accentColor,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

/**
 * Visual Trend Indicator displayed next to average speed values.
 */
@Composable
fun SpeedTrendIndicator(
  speedDelta: Float?,
  modifier: Modifier = Modifier
) {
  if (speedDelta == null) {
    Box(
      modifier = modifier
        .clip(RoundedCornerShape(4.dp))
        .background(CarbonSurface)
        .border(0.5.dp, CarbonBorder, RoundedCornerShape(4.dp))
        .padding(horizontal = 4.dp, vertical = 1.dp)
        .testTag("trend_indicator_baseline")
    ) {
      Text(
        text = "—",
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = TextTertiary
      )
    }
    return
  }

  val isFaster = speedDelta > 0.05f
  val isSlower = speedDelta < -0.05f

  val indicatorColor = when {
    isFaster -> ApexGreen
    isSlower -> RedlineRed
    else -> TextSecondary
  }

  val icon = when {
    isFaster -> Icons.Default.ArrowUpward
    isSlower -> Icons.Default.ArrowDownward
    else -> null
  }

  val testTag = when {
    isFaster -> "trend_indicator_faster"
    isSlower -> "trend_indicator_slower"
    else -> "trend_indicator_equal"
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(indicatorColor.copy(alpha = 0.16f))
      .border(0.8.dp, indicatorColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
      .padding(horizontal = 3.5.dp, vertical = 1.5.dp)
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = if (isFaster) "Faster than previous race" else "Slower than previous race",
          tint = indicatorColor,
          modifier = Modifier.size(10.dp)
        )
      }
      Text(
        text = if (isFaster) {
          "+${String.format(Locale.US, "%.1f", speedDelta)}"
        } else if (isSlower) {
          String.format(Locale.US, "%.1f", speedDelta)
        } else {
          "0.0"
        },
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Black,
        color = indicatorColor,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerformanceMetricCard(
  metric: PerformanceMetrics,
  isBestLap: Boolean,
  speedDelta: Float? = null,
  onShare: () -> Unit = {},
  onTagClick: (String) -> Unit = {},
  onEditTags: () -> Unit = {},
  onDelete: () -> Unit
) {
  val typeBadgeColor = when (metric.raceType) {
    "Circuit" -> NeonAmber
    "Sprint" -> PurpleDelta
    else -> NeonCyan
  }
  val typeIcon = when (metric.raceType) {
    "Circuit" -> Icons.Default.SportsScore
    "Sprint" -> Icons.Default.FlashOn
    else -> Icons.Default.Timer
  }

  val computedAvgSpeed = metric.getComputedAvgSpeed()
  val trackDistanceMeters = metric.getEffectiveDistanceMeters()
  val trackDistanceKm = trackDistanceMeters / 1000f

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("metric_card_${metric.id}"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isBestLap) ApexGreen.copy(alpha = 0.6f) else CarbonBorder
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Top header row: Track name, race type badge, best lap badge, and actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = metric.trackName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.width(6.dp))

          // Race Type Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(typeBadgeColor.copy(alpha = 0.15f))
              .border(1.dp, typeBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
              .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = typeIcon,
                contentDescription = null,
                tint = typeBadgeColor,
                modifier = Modifier.size(9.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = metric.raceType.uppercase(Locale.US),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = typeBadgeColor
              )
            }
          }

          if (isBestLap) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(ApexGreen.copy(alpha = 0.2f))
                .border(1.dp, ApexGreen, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "BEST",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = ApexGreen
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onShare,
            modifier = Modifier
              .size(24.dp)
              .testTag("share_metric_icon_${metric.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share to social media",
              tint = NeonCyan,
              modifier = Modifier.size(14.dp)
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          IconButton(
            onClick = onEditTags,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit tags & condition",
              tint = TextSecondary,
              modifier = Modifier.size(14.dp)
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete record",
              tint = TextTertiary,
              modifier = Modifier.size(15.dp)
            )
          }
        }
      }

      // Driver Name and Vehicle
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = metric.driverName,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = NeonCyan
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "• ${metric.vehicleName}",
          fontSize = 11.sp,
          color = TextSecondary
        )
      }

      // Categorization Tags Row: Complexity, Weather Condition, & Custom Tags
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
      ) {
        // Track Complexity Tag
        if (metric.trackComplexity.isNotBlank()) {
          TagChip(
            text = metric.trackComplexity,
            icon = Icons.Default.Tune,
            accentColor = NeonAmber,
            onClick = { onTagClick(metric.trackComplexity) }
          )
        }

        // Weather Condition Tag
        if (metric.weatherCondition.isNotBlank()) {
          val weatherIcon = when {
            metric.weatherCondition.contains("Wet", ignoreCase = true) ||
            metric.weatherCondition.contains("Rain", ignoreCase = true) -> Icons.Default.WaterDrop
            else -> Icons.Default.WbSunny
          }
          val weatherColor = when {
            metric.weatherCondition.contains("Rain", ignoreCase = true) ||
            metric.weatherCondition.contains("Wet", ignoreCase = true) -> NeonCyan
            metric.weatherCondition.contains("Night", ignoreCase = true) -> PurpleDelta
            else -> ApexGreen
          }
          TagChip(
            text = metric.weatherCondition,
            icon = weatherIcon,
            accentColor = weatherColor,
            onClick = { onTagClick(metric.weatherCondition) }
          )
        }

        // Additional Custom Tags
        metric.tags.filter { it != metric.trackComplexity && it != metric.weatherCondition }.forEach { customTag ->
          TagChip(
            text = customTag,
            icon = Icons.Default.LocalOffer,
            accentColor = TextSecondary,
            onClick = { onTagClick(customTag) }
          )
        }
      }

      // Telemetry Data Grid: Lap Time, Calculated Avg Speed + Trend, Top Speed, Accel
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CarbonSurfaceVariant)
          .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Lap Time
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Timer,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "LAP TIME", fontSize = 8.5.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
          }
          Text(
            text = formatLapTime(metric.lapTimeMs),
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = if (isBestLap) ApexGreen else NeonCyan,
            fontFamily = FontFamily.Monospace
          )
        }

        // Calculated Average Speed with Trend Indicator
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = ApexGreen,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "AVG SPEED", fontSize = 8.5.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
          }
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "${String.format(Locale.US, "%.1f", computedAvgSpeed)} km/h",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Black,
              color = ApexGreen,
              fontFamily = FontFamily.Monospace
            )
            SpeedTrendIndicator(
              speedDelta = speedDelta,
              modifier = Modifier.testTag("trend_indicator_${metric.id}")
            )
          }
        }

        // Top Speed
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = NeonAmber,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "TOP SPEED", fontSize = 8.5.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
          }
          Text(
            text = "${metric.topSpeedKmh.toInt()} km/h",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
          )
        }

        // Acceleration Peak
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.FlashOn,
              contentDescription = null,
              tint = RedlineRed,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = "PEAK G", fontSize = 8.5.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
          }
          Text(
            text = "${String.format(Locale.US, "%.2f", metric.peakAccelerationG)}G",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NeonAmber,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Calculated stats summary footer with trend note, track distance and timestamp
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        val trendStatusText = when {
          speedDelta == null -> "Baseline entry"
          speedDelta > 0.05f -> "▲ +${String.format(Locale.US, "%.1f", speedDelta)} km/h faster"
          speedDelta < -0.05f -> "▼ ${String.format(Locale.US, "%.1f", speedDelta)} km/h slower"
          else -> "= Pace matched"
        }
        val trendStatusColor = when {
          speedDelta == null -> TextTertiary
          speedDelta > 0.05f -> ApexGreen
          speedDelta < -0.05f -> RedlineRed
          else -> TextSecondary
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Track: ${String.format(Locale.US, "%.2f km", trackDistanceKm)} • ",
            fontSize = 9.5.sp,
            color = TextTertiary
          )
          Text(
            text = trendStatusText,
            fontSize = 9.5.sp,
            fontWeight = if (speedDelta != null && (speedDelta > 0.05f || speedDelta < -0.05f)) FontWeight.Bold else FontWeight.Normal,
            color = trendStatusColor
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OutlinedButton(
            onClick = onShare,
            modifier = Modifier
              .height(24.dp)
              .testTag("share_metric_button_${metric.id}"),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, NeonCyan.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = NeonCyan.copy(alpha = 0.08f),
              contentColor = NeonCyan
            )
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share",
              tint = NeonCyan,
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "SHARE",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = NeonCyan
            )
          }

          Text(
            text = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(metric.timestamp)),
            fontSize = 9.5.sp,
            color = TextTertiary
          )
        }
      }
    }
  }
}

@Composable
private fun TagChip(
  text: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(4.dp))
      .background(accentColor.copy(alpha = 0.12f))
      .border(0.6.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 5.dp, vertical = 2.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(9.dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = text,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = accentColor
      )
    }
  }
}

private fun getTagColor(tag: String): Color {
  return when {
    tag == "All Tags" -> ApexGreen
    tag in listOf("Technical", "High Speed", "Flowing", "Extreme") -> NeonAmber
    tag in listOf("Wet Surface", "Heavy Rain") -> NeonCyan
    tag == "Night Run" -> PurpleDelta
    tag == "Dry Asphalt" -> ApexGreen
    else -> NeonCyan
  }
}

private fun getTagIcon(tag: String): androidx.compose.ui.graphics.vector.ImageVector? {
  return when {
    tag == "All Tags" -> Icons.Default.FilterList
    tag in listOf("Technical", "High Speed", "Flowing", "Extreme") -> Icons.Default.Tune
    tag in listOf("Wet Surface", "Heavy Rain") -> Icons.Default.WaterDrop
    tag == "Night Run" -> Icons.Default.Timer
    tag == "Dry Asphalt" -> Icons.Default.WbSunny
    else -> Icons.Default.LocalOffer
  }
}

private fun formatLapTime(ms: Long): String {
  val minutes = (ms / 60000)
  val seconds = (ms % 60000) / 1000
  val millis = ms % 1000
  return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis)
}
