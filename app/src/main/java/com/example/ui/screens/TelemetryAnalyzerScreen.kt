package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVAILABLE_TRACKS
import com.example.model.CircuitCornerData
import com.example.model.CornerTelemetryPoint
import com.example.model.SectorDeltaSummary
import com.example.model.Track
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
import com.example.ui.viewmodel.RaceUiState
import com.example.ui.viewmodel.RaceViewModel
import java.util.Locale

/**
 * Screen providing Corner-by-Corner Apex Telemetry, Dual-Run Delta Comparison,
 * and Micro-Sector Optimization.
 */
@Composable
fun TelemetryAnalyzerScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedTabIndex by remember { mutableIntStateOf(0) }
  val tabs = listOf("CORNER APEX", "DUAL RUN DELTA", "SECTOR SPLITS")

  val currentTrack = uiState.activeTrack
  val corners = remember(currentTrack) { CircuitCornerData.getCornersForTrack(currentTrack.name) }
  var selectedCorner by remember(corners) { mutableStateOf(corners.firstOrNull() ?: CircuitCornerData.MONZA_CORNERS.first()) }
  var scrubProgress by remember { mutableFloatStateOf(0.45f) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .testTag("analyzer_screen")
  ) {
    // Header Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(CarbonSurface)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(48.dp)
            .testTag("analyzer_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Navigate Back",
            tint = NeonCyan
          )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "TELEMETRY ANALYZER",
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(NeonCyan.copy(alpha = 0.2f))
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text(
                text = "v7.0 PRO",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Text(
            text = "${currentTrack.name} • 60Hz Sector & Apex Telemetry",
            fontSize = 10.sp,
            color = TextSecondary
          )
        }
      }

      // Share Report Button
      IconButton(
        onClick = {
          val shareText = "Apex Rivals v7.0 Telemetry Analysis for ${currentTrack.name}:\n" +
              "Turn: ${selectedCorner.turnName} (${selectedCorner.cornerType})\n" +
              "Apex Speed: ${selectedCorner.formattedApexSpeedKmh}\n" +
              "Braking: ${selectedCorner.formattedBraking} | Gear: ${selectedCorner.recommendedGear}\n" +
              "Lateral Load: ${selectedCorner.peakLateralG}G\n" +
              "Advice: ${selectedCorner.coachAdvice}\n#ApexRivals #SimRacing"
          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
          }
          context.startActivity(Intent.createChooser(sendIntent, "Share Telemetry Report"))
        },
        modifier = Modifier
          .size(48.dp)
          .testTag("share_analysis_btn")
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share Analysis",
          tint = NeonAmber
        )
      }
    }

    // Circuit Selector Strip
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .background(CarbonSurfaceVariant)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(AVAILABLE_TRACKS) { track ->
        val isSelected = track.id == currentTrack.id
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) NeonCyan else CarbonSurface)
            .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(6.dp))
            .clickable { viewModel.selectTrack(track) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("analyzer_track_${track.id}")
        ) {
          Text(
            text = track.name.uppercase(Locale.US),
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) CarbonBlack else TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Material 3 Tabs
    TabRow(
      selectedTabIndex = selectedTabIndex,
      containerColor = CarbonSurface,
      contentColor = NeonCyan,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
          color = NeonCyan,
          height = 2.5.dp
        )
      }
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTabIndex == index,
          onClick = { selectedTabIndex = index },
          text = {
            Text(
              text = title,
              fontSize = 11.sp,
              fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Medium,
              fontFamily = FontFamily.Monospace,
              color = if (selectedTabIndex == index) NeonCyan else TextSecondary
            )
          },
          modifier = Modifier
            .height(44.dp)
            .testTag("analyzer_tab_$index")
        )
      }
    }

    // Content Based on Selected Tab
    when (selectedTabIndex) {
      0 -> CornerApexAnalysisTab(
        track = currentTrack,
        corners = corners,
        selectedCorner = selectedCorner,
        onSelectCorner = { selectedCorner = it },
        isMph = uiState.isMph
      )
      1 -> DualRunDeltaComparisonTab(
        track = currentTrack,
        scrubProgress = scrubProgress,
        onScrubChange = { scrubProgress = it },
        isMph = uiState.isMph
      )
      2 -> SectorSplitsOptimizationTab(
        track = currentTrack,
        uiState = uiState
      )
    }
  }
}

/**
 * Tab 1: Interactive Circuit Map & Corner-by-Corner Apex Analysis.
 */
@Composable
private fun CornerApexAnalysisTab(
  track: Track,
  corners: List<CornerTelemetryPoint>,
  selectedCorner: CornerTelemetryPoint,
  onSelectCorner: (CornerTelemetryPoint) -> Unit,
  isMph: Boolean
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Interactive Circuit Map Canvas with Speed Heatmap
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("track_canvas_card")
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "CIRCUIT APEX HEATMAP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
              )
            }
            Text(
              text = "Tap turns to inspect telemetry",
              fontSize = 9.sp,
              color = TextTertiary
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Circuit Canvas rendering 2D track line and corner nodes
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(CarbonBlack)
              .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val width = size.width
              val height = size.height
              val path = Path()

              // Draw stylized circuit loop
              val centerX = width * 0.5f
              val centerY = height * 0.5f
              val radiusX = width * 0.42f
              val radiusY = height * 0.36f

              val points = track.pathPoints
              if (points.isNotEmpty()) {
                val firstPoint = points[0]
                val startX = centerX + (firstPoint.x - 0.5f) * radiusX * 2f
                val startY = centerY + (firstPoint.y - 0.5f) * radiusY * 2f
                path.moveTo(startX, startY)

                for (i in 1 until points.size) {
                  val pt = points[i]
                  val px = centerX + (pt.x - 0.5f) * radiusX * 2f
                  val py = centerY + (pt.y - 0.5f) * radiusY * 2f
                  path.lineTo(px, py)
                }
                path.close()

                // Draw base circuit line (high-speed cyan)
                drawPath(
                  path = path,
                  color = CarbonSurfaceVariant,
                  style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                drawPath(
                  path = path,
                  color = NeonCyan.copy(alpha = 0.85f),
                  style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Render corner markers along circuit
                corners.forEach { corner ->
                  val targetIdx = ((points.size - 1) * corner.trackProgress).toInt().coerceIn(0, points.size - 1)
                  val pt = points[targetIdx]
                  val cx = centerX + (pt.x - 0.5f) * radiusX * 2f
                  val cy = centerY + (pt.y - 0.5f) * radiusY * 2f
                  val isSelected = corner.turnNumber == selectedCorner.turnNumber

                  // Draw apex point circle
                  drawCircle(
                    color = if (isSelected) NeonAmber else RedlineRed,
                    radius = if (isSelected) 9f else 5.5f,
                    center = Offset(cx, cy)
                  )
                  if (isSelected) {
                    drawCircle(
                      color = NeonAmber.copy(alpha = 0.4f),
                      radius = 16f,
                      center = Offset(cx, cy)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Corner Selector Pills
    item {
      Column {
        Text(
          text = "SELECT APEX / TURN:",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = TextSecondary,
          modifier = Modifier.padding(bottom = 6.dp)
        )
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(corners) { corner ->
            val isSelected = corner.turnNumber == selectedCorner.turnNumber
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) NeonAmber else CarbonSurface)
                .border(1.dp, if (isSelected) NeonAmber else CarbonBorder, RoundedCornerShape(8.dp))
                .clickable { onSelectCorner(corner) }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("corner_chip_${corner.turnNumber}")
            ) {
              Text(
                text = "T${corner.turnNumber}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (isSelected) CarbonBlack else TextPrimary
              )
            }
          }
        }
      }
    }

    // Detailed Selected Corner Card
    item {
      CornerDetailCard(
        corner = selectedCorner,
        isMph = isMph
      )
    }
  }
}

@Composable
private fun CornerDetailCard(
  corner: CornerTelemetryPoint,
  isMph: Boolean
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(CarbonSurfaceVariant.copy(alpha = 0.5f), CarbonBlack)
          )
        )
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "TURN ${corner.turnNumber}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = NeonAmber
          )
          Text(
            text = corner.turnName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PurpleDelta.copy(alpha = 0.2f))
            .border(1.dp, PurpleDelta.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "SECTOR ${corner.sector}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = PurpleDelta
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Corner Metrics Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        TelemetryMiniCard(
          label = "TARGET APEX",
          value = if (isMph) corner.formattedApexSpeedMph else corner.formattedApexSpeedKmh,
          accent = ApexGreen,
          modifier = Modifier.weight(1f)
        )
        TelemetryMiniCard(
          label = "BRAKING DIST",
          value = corner.formattedBraking,
          accent = RedlineRed,
          modifier = Modifier.weight(1f)
        )
        TelemetryMiniCard(
          label = "GEAR",
          value = "G${corner.recommendedGear}",
          accent = NeonCyan,
          modifier = Modifier.weight(0.7f)
        )
        TelemetryMiniCard(
          label = "LATERAL G",
          value = String.format(Locale.US, "%.2fG", corner.peakLateralG),
          accent = NeonAmber,
          modifier = Modifier.weight(0.9f)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // AI Coach Advice
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CarbonBlack)
          .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Row(verticalAlignment = Alignment.Top) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier
              .size(16.dp)
              .padding(top = 1.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "ENGINEERING APEX TACTICS",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = NeonCyan
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = corner.coachAdvice,
              fontSize = 11.5.sp,
              color = TextPrimary,
              lineHeight = 16.sp
            )
          }
        }
      }
    }
  }
}

/**
 * Tab 2: Dual-Run Side-by-Side Delta Comparison.
 */
@Composable
private fun DualRunDeltaComparisonTab(
  track: Track,
  scrubProgress: Float,
  onScrubChange: (Float) -> Unit,
  isMph: Boolean
) {
  val speedRunA = 285f - (Math.sin(scrubProgress * 14.0) * 110f).toFloat()
  val speedRunB = 292f - (Math.sin(scrubProgress * 14.0 + 0.1) * 105f).toFloat()
  val throttleRunA = (Math.cos(scrubProgress * 8.0) * 0.5f + 0.5f).toFloat().coerceIn(0f, 1f)
  val throttleRunB = (Math.cos(scrubProgress * 8.0 + 0.05) * 0.5f + 0.5f).toFloat().coerceIn(0f, 1f)
  val deltaMs = ((Math.sin(scrubProgress * 6.0) * 420)).toLong()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Delta Indicator Header
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "CUMULATIVE LAP DELTA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = if (deltaMs <= 0) String.format(Locale.US, "-%.3fs", Math.abs(deltaMs) / 1000f)
                  else String.format(Locale.US, "+%.3fs", deltaMs / 1000f),
                  fontSize = 24.sp,
                  fontWeight = FontWeight.Black,
                  fontFamily = FontFamily.Monospace,
                  color = if (deltaMs <= 0) ApexGreen else RedlineRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (deltaMs <= 0) ApexGreen.copy(alpha = 0.2f) else RedlineRed.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = if (deltaMs <= 0) "AHEAD OF BENCHMARK" else "BEHIND BENCHMARK",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (deltaMs <= 0) ApexGreen else RedlineRed
                  )
                }
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "TRACK PROGRESS",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = TextTertiary
              )
              Text(
                text = String.format(Locale.US, "%.1f%%", scrubProgress * 100f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = NeonCyan
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Interactive Progress Scrub Slider
          Slider(
            value = scrubProgress,
            onValueChange = onScrubChange,
            colors = SliderDefaults.colors(
              thumbColor = NeonCyan,
              activeTrackColor = NeonCyan,
              inactiveTrackColor = CarbonBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("delta_scrubber")
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("0m (Start/Finish)", fontSize = 8.5.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
            Text("Sector 2 Split", fontSize = 8.5.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
            Text("${track.totalLengthMeters}m", fontSize = 8.5.sp, color = TextTertiary, fontFamily = FontFamily.Monospace)
          }
        }
      }
    }

    // Side-by-Side Telemetry Gauges
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Run A Card
        RunTelemetryCompareCard(
          title = "YOUR RUN (A)",
          speedKmh = speedRunA,
          throttle = throttleRunA,
          brake = if (throttleRunA < 0.2f) 0.85f else 0f,
          accent = NeonCyan,
          isMph = isMph,
          modifier = Modifier.weight(1f)
        )

        // Run B Card
        RunTelemetryCompareCard(
          title = "BENCHMARK GHOST (B)",
          speedKmh = speedRunB,
          throttle = throttleRunB,
          brake = if (throttleRunB < 0.2f) 0.78f else 0f,
          accent = NeonAmber,
          isMph = isMph,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Where You Gained / Lost Time Breakdown
    item {
      Text(
        text = "WHERE YOU GAINED / LOST TIME",
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = TextPrimary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
      )
    }

    item {
      SectorDeltaItemCard(
        sectorNumber = 1,
        title = "Main Straight & First Chicane",
        deltaMs = -180L,
        explanation = "Gained 0.18s with deeper braking into Turn 1 (-15m later than ghost)."
      )
    }

    item {
      SectorDeltaItemCard(
        sectorNumber = 2,
        title = "Lesmo Curves & Serraglio",
        deltaMs = 240L,
        explanation = "Lost 0.24s due to excessive curb bouncing at Lesmo 2 apex exit."
      )
    }

    item {
      SectorDeltaItemCard(
        sectorNumber = 3,
        title = "Variante Ascari & Parabolica",
        deltaMs = -80L,
        explanation = "Gained 0.08s with full throttle acceleration out of the Parabolica exit."
      )
    }
  }
}

@Composable
private fun RunTelemetryCompareCard(
  title: String,
  speedKmh: Float,
  throttle: Float,
  brake: Float,
  accent: Color,
  isMph: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(CarbonSurface)
      .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Text(
        text = title,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = accent
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = if (isMph) String.format(Locale.US, "%.0f MPH", speedKmh * 0.621371f)
        else String.format(Locale.US, "%.0f KM/H", speedKmh),
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Throttle Bar
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("THR", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ApexGreen, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.width(4.dp))
        LinearProgressIndicator(
          progress = { throttle },
          color = ApexGreen,
          trackColor = CarbonBlack,
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Brake Bar
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("BRK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = RedlineRed, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.width(4.dp))
        LinearProgressIndicator(
          progress = { brake },
          color = RedlineRed,
          trackColor = CarbonBlack,
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
        )
      }
    }
  }
}

@Composable
private fun SectorDeltaItemCard(
  sectorNumber: Int,
  title: String,
  deltaMs: Long,
  explanation: String
) {
  val isAhead = deltaMs <= 0
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonSurface)
      .border(0.8.dp, if (isAhead) ApexGreen.copy(alpha = 0.4f) else RedlineRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (isAhead) ApexGreen.copy(alpha = 0.2f) else RedlineRed.copy(alpha = 0.2f))
              .padding(horizontal = 5.dp, vertical = 2.dp)
          ) {
            Text(
              text = "SECTOR $sectorNumber",
              fontSize = 8.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = if (isAhead) ApexGreen else RedlineRed
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = explanation,
          fontSize = 10.sp,
          color = TextSecondary,
          lineHeight = 14.sp
        )
      }

      Text(
        text = if (isAhead) String.format(Locale.US, "-%.3fs", Math.abs(deltaMs) / 1000f)
        else String.format(Locale.US, "+%.3fs", deltaMs / 1000f),
        fontSize = 13.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = if (isAhead) ApexGreen else RedlineRed,
        modifier = Modifier.padding(start = 8.dp)
      )
    }
  }
}

/**
 * Tab 3: Sector Splits & Theoretical Optimal Lap.
 */
@Composable
private fun SectorSplitsOptimizationTab(
  track: Track,
  uiState: RaceUiState
) {
  val s1Best = 25120L
  val s2Best = 26340L
  val s3Best = 25420L
  val theoreticalBestMs = s1Best + s2Best + s3Best

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Theoretical Optimal Lap Header
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleDelta.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                listOf(PurpleDelta.copy(alpha = 0.15f), CarbonBlack)
              )
            )
            .padding(14.dp)
        ) {
          Text(
            text = "THEORETICAL OPTIMAL LAP TIME",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = PurpleDelta
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = formatLapMs(theoreticalBestMs),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = PurpleDelta
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Aggregated from your fastest individual S1, S2, and S3 micro-sectors.",
            fontSize = 10.sp,
            color = TextSecondary
          )
        }
      }
    }

    // Sector 1
    item {
      SectorMicroSplitCard(
        sectorNumber = 1,
        sectorName = "Sector 1 (Variante del Rettifilo)",
        bestTimeMs = s1Best,
        actualTimeMs = 25300L,
        deltaMs = 180L,
        isPurple = false
      )
    }

    // Sector 2
    item {
      SectorMicroSplitCard(
        sectorNumber = 2,
        sectorName = "Sector 2 (Lesmo & Curva Grande)",
        bestTimeMs = s2Best,
        actualTimeMs = s2Best,
        deltaMs = 0L,
        isPurple = true
      )
    }

    // Sector 3
    item {
      SectorMicroSplitCard(
        sectorNumber = 3,
        sectorName = "Sector 3 (Variante Ascari & Parabolica)",
        bestTimeMs = s3Best,
        actualTimeMs = 25520L,
        deltaMs = 100L,
        isPurple = false
      )
    }
  }
}

@Composable
private fun SectorMicroSplitCard(
  sectorNumber: Int,
  sectorName: String,
  bestTimeMs: Long,
  actualTimeMs: Long,
  deltaMs: Long,
  isPurple: Boolean
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(CarbonSurface)
      .border(1.dp, if (isPurple) PurpleDelta else CarbonBorder, RoundedCornerShape(10.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "S$sectorNumber",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (isPurple) PurpleDelta else NeonCyan
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = sectorName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Best Sector: ${formatLapMs(bestTimeMs)}",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = TextSecondary
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = formatLapMs(actualTimeMs),
          fontSize = 14.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace,
          color = if (isPurple) PurpleDelta else TextPrimary
        )
        Text(
          text = if (isPurple) "PURPLE SECTOR" else String.format(Locale.US, "+%.3fs", deltaMs / 1000f),
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          color = if (isPurple) PurpleDelta else NeonAmber
        )
      }
    }
  }
}

@Composable
private fun TelemetryMiniCard(
  label: String,
  value: String,
  accent: Color,
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
        text = label,
        fontSize = 7.5.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color = TextTertiary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = accent
      )
    }
  }
}

private fun formatLapMs(ms: Long): String {
  val minutes = ms / 60000L
  val seconds = (ms % 60000L) / 1000L
  val millis = ms % 1000L
  return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
}
