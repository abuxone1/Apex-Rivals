package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

enum class ChartMetricType(val displayName: String, val unit: String, val color: Color) {
  LAP_TIME("Lap Time", "sec", NeonCyan),
  AVG_SPEED("Avg Speed", "km/h", ApexGreen),
  TOP_SPEED("Top Speed", "km/h", NeonAmber),
  ACCELERATION("Peak Accel", "G", PurpleDelta)
}

enum class ChartEngine(val displayName: String) {
  RECHARTS_WEB("Recharts (Web)"),
  COMPOSE_NATIVE("Compose (Native)")
}

/**
 * Visualizes 'PerformanceMetrics' data (lap times, speeds, and acceleration over sessions)
 * as a line chart using the Recharts library and a native Compose Canvas counterpart.
 */
@Composable
fun PerformanceMetricsRechartsChart(
  metrics: List<PerformanceMetrics>,
  modifier: Modifier = Modifier
) {
  var selectedMetric by remember { mutableStateOf(ChartMetricType.LAP_TIME) }
  var selectedEngine by remember { mutableStateOf(ChartEngine.RECHARTS_WEB) }
  var selectedIndex by remember { mutableIntStateOf(-1) }

  // Sort chronological for session progression
  val chronologicalMetrics = remember(metrics) {
    metrics.sortedBy { it.timestamp }
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("recharts_line_chart_card"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header & Engine Switcher
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Timeline,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "SESSION TRENDS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary,
            letterSpacing = 0.8.sp
          )
        }

        // Engine Toggle
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
            .padding(2.dp)
        ) {
          ChartEngine.values().forEach { engine ->
            val isSelected = (engine == selectedEngine)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isSelected) NeonCyan else Color.Transparent)
                .clickable { selectedEngine = engine }
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = engine.displayName,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) CarbonBlack else TextSecondary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Metric Filter Chips (Lap Time, Top Speed, Accel G)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        ChartMetricType.values().forEach { metricType ->
          val isSelected = (metricType == selectedMetric)
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (isSelected) metricType.color.copy(alpha = 0.2f) else CarbonSurfaceVariant)
              .border(
                1.dp,
                if (isSelected) metricType.color else CarbonBorder,
                RoundedCornerShape(6.dp)
              )
              .clickable {
                selectedMetric = metricType
                selectedIndex = -1
              }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(metricType.color)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = metricType.displayName,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                color = if (isSelected) metricType.color else TextSecondary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (chronologicalMetrics.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No race sessions recorded yet to chart",
            fontSize = 12.sp,
            color = TextTertiary
          )
        }
      } else {
        // Render either Recharts (via WebView) or Native Compose Canvas
        when (selectedEngine) {
          ChartEngine.RECHARTS_WEB -> {
            RechartsWebViewContainer(
              metrics = chronologicalMetrics,
              metricType = selectedMetric,
              modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
            )
          }

          ChartEngine.COMPOSE_NATIVE -> {
            NativeComposeLineChart(
              metrics = chronologicalMetrics,
              metricType = selectedMetric,
              selectedIndex = selectedIndex,
              onSelectIndex = { selectedIndex = it },
              modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Footer legend and info
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${chronologicalMetrics.size} sessions plotted • Line: ${selectedMetric.displayName}",
          fontSize = 10.sp,
          color = TextTertiary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(selectedMetric.color)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Recharts CartesianGrid",
            fontSize = 10.sp,
            color = selectedMetric.color,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

/**
 * Embedded Recharts Web Engine using React and Recharts library.
 * Renders ResponsiveContainer, LineChart, CartesianGrid, XAxis, YAxis, Tooltip, and smooth Monotone Line.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebViewContainer(
  metrics: List<PerformanceMetrics>,
  metricType: ChartMetricType,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  // Prepare JSON dataset for Recharts
  val jsonArray = remember(metrics, metricType) {
    val array = JSONArray()
    metrics.forEachIndexed { index, m ->
      val obj = JSONObject()
      obj.put("session", "S${index + 1}")
      obj.put("track", m.trackName)
      obj.put("vehicle", m.vehicleName)
      obj.put("lapTimeSec", String.format(Locale.US, "%.2f", m.lapTimeMs / 1000f).toDouble())
      obj.put("avgSpeed", String.format(Locale.US, "%.1f", m.getComputedAvgSpeed()).toDouble())
      obj.put("topSpeed", m.topSpeedKmh.toInt())
      obj.put("accelG", String.format(Locale.US, "%.2f", m.peakAccelerationG).toDouble())
      array.put(obj)
    }
    array.toString()
  }

  val dataKey = when (metricType) {
    ChartMetricType.LAP_TIME -> "lapTimeSec"
    ChartMetricType.AVG_SPEED -> "avgSpeed"
    ChartMetricType.TOP_SPEED -> "topSpeed"
    ChartMetricType.ACCELERATION -> "accelG"
  }

  val strokeColorHex = when (metricType) {
    ChartMetricType.LAP_TIME -> "#00E5FF"
    ChartMetricType.AVG_SPEED -> "#00E676"
    ChartMetricType.TOP_SPEED -> "#FF6D00"
    ChartMetricType.ACCELERATION -> "#B388FF"
  }

  val htmlContent = remember(jsonArray, dataKey, strokeColorHex) {
    """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
      <script src="https://unpkg.com/react@18/umd/react.production.min.js"></script>
      <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"></script>
      <script src="https://unpkg.com/recharts@2.12.7/umd/Recharts.min.js"></script>
      <style>
        * { box-sizing: border-box; }
        body {
          margin: 0;
          padding: 4px;
          background-color: #131822;
          color: #F0F4F8;
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
          overflow: hidden;
          user-select: none;
        }
        #chart-container {
          width: 100%;
          height: 195px;
        }
        .recharts-default-tooltip {
          background-color: #1B2332 !important;
          border: 1px solid #2B364C !important;
          border-radius: 8px !important;
          box-shadow: 0 4px 12px rgba(0,0,0,0.5) !important;
          padding: 6px 10px !important;
        }
        .recharts-tooltip-label {
          color: #00E5FF !important;
          font-weight: bold !important;
          font-size: 11px !important;
          margin-bottom: 2px !important;
        }
        .recharts-tooltip-item {
          color: #F0F4F8 !important;
          font-size: 12px !important;
          font-family: monospace !important;
        }
        /* Fallback SVG Chart styling */
        svg.fallback-chart {
          width: 100%;
          height: 100%;
        }
      </style>
    </head>
    <body>
      <div id="chart-container"></div>

      <script>
        const data = $jsonArray;
        const key = "$dataKey";
        const strokeColor = "$strokeColorHex";

        function renderFallbackSvg() {
          const container = document.getElementById('chart-container');
          if (!data || data.length === 0) {
            container.innerHTML = '<div style="text-align:center;padding-top:80px;color:#64748B;">No telemetry data</div>';
            return;
          }
          const w = container.clientWidth || 340;
          const h = container.clientHeight || 190;
          const padL = 36, padR = 20, padT = 16, padB = 26;
          const innerW = w - padL - padR;
          const innerH = h - padT - padB;

          const values = data.map(d => d[key]);
          const minVal = Math.min(...values) * 0.95;
          const maxVal = Math.max(...values) * 1.05 || 1;

          let points = data.map((d, i) => {
            const x = padL + (i / Math.max(1, data.length - 1)) * innerW;
            const y = padT + (1 - (d[key] - minVal) / (maxVal - minVal)) * innerH;
            return { x, y, val: d[key], session: d.session };
          });

          let pathD = "M " + points[0].x + " " + points[0].y;
          for (let i = 1; i < points.length; i++) {
            pathD += " L " + points[i].x + " " + points[i].y;
          }

          let gridLines = '';
          for (let i = 0; i <= 3; i++) {
            const gy = padT + (i / 3) * innerH;
            const gVal = (maxVal - (i / 3) * (maxVal - minVal)).toFixed(1);
            gridLines += '<line x1="' + padL + '" y1="' + gy + '" x2="' + (w - padR) + '" y2="' + gy + '" stroke="#2B364C" stroke-dasharray="3 3"/>';
            gridLines += '<text x="' + (padL - 4) + '" y="' + (gy + 3) + '" fill="#94A3B8" font-size="9" text-anchor="end">' + gVal + '</text>';
          }

          let dots = points.map(p => 
            '<circle cx="' + p.x + '" cy="' + p.y + '" r="4" fill="' + strokeColor + '" stroke="#131822" stroke-width="2"/>' +
            '<text x="' + p.x + '" y="' + (h - 8) + '" fill="#94A3B8" font-size="9" text-anchor="middle">' + p.session + '</text>'
          ).join('');

          container.innerHTML = '<svg class="fallback-chart">' +
            gridLines +
            '<path d="' + pathD + '" fill="none" stroke="' + strokeColor + '" stroke-width="3" stroke-linecap="round"/>' +
            dots +
          '</svg>';
        }

        try {
          if (window.Recharts && window.React && window.ReactDOM) {
            const { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip } = window.Recharts;
            const e = window.React.createElement;

            const App = () => {
              return e(ResponsiveContainer, { width: '100%', height: '100%' },
                e(LineChart, { data: data, margin: { top: 12, right: 16, left: -16, bottom: 4 } },
                  e(CartesianGrid, { strokeDasharray: '3 3', stroke: '#2B364C' }),
                  e(XAxis, { dataKey: 'session', stroke: '#94A3B8', fontSize: 10, tickLine: false }),
                  e(YAxis, { stroke: '#94A3B8', fontSize: 10, tickLine: false, domain: ['dataMin - 1', 'dataMax + 1'] }),
                  e(Tooltip, {
                    contentStyle: { backgroundColor: '#1B2332', borderColor: '#2B364C', borderRadius: '8px' },
                    labelStyle: { color: '#00E5FF', fontWeight: 'bold' }
                  }),
                  e(Line, {
                    type: 'monotone',
                    dataKey: key,
                    stroke: strokeColor,
                    strokeWidth: 3,
                    dot: { r: 4, fill: strokeColor, stroke: '#131822', strokeWidth: 2 },
                    activeDot: { r: 7, fill: '#FFFFFF', stroke: strokeColor, strokeWidth: 3 }
                  })
                )
              );
            };

            const root = window.ReactDOM.createRoot(document.getElementById('chart-container'));
            root.render(e(App));
          } else {
            renderFallbackSvg();
          }
        } catch (err) {
          console.error("Recharts render error, falling back to SVG", err);
          renderFallbackSvg();
        }
      </script>
    </body>
    </html>
    """.trimIndent()
  }

  AndroidView(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp)),
    factory = { ctx ->
      WebView(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(AndroidColor.parseColor("#131822"))
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        webViewClient = WebViewClient()
        loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
      }
    },
    update = { webView ->
      webView.loadDataWithBaseURL("https://unpkg.com", htmlContent, "text/html", "UTF-8", null)
    }
  )
}

/**
 * Native Jetpack Compose Canvas counterpart mirroring the exact Recharts Cartesian visual style.
 * Includes CartesianGrid, Monotone curved Line with gradient stroke, glow, and touch-to-inspect Tooltip.
 */
@Composable
fun NativeComposeLineChart(
  metrics: List<PerformanceMetrics>,
  metricType: ChartMetricType,
  selectedIndex: Int,
  onSelectIndex: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  if (metrics.isEmpty()) return

  val values = remember(metrics, metricType) {
    metrics.map {
      when (metricType) {
        ChartMetricType.LAP_TIME -> it.lapTimeMs / 1000f
        ChartMetricType.AVG_SPEED -> it.getComputedAvgSpeed()
        ChartMetricType.TOP_SPEED -> it.topSpeedKmh
        ChartMetricType.ACCELERATION -> it.peakAccelerationG
      }
    }
  }

  val minVal = (values.minOrNull() ?: 0f) * 0.92f
  val maxVal = ((values.maxOrNull() ?: 1f) * 1.08f).coerceAtLeast(minVal + 0.1f)

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonSurfaceVariant)
      .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .pointerInput(metrics) {
        detectTapGestures { offset ->
          val padLeft = 40.dp.toPx()
          val padRight = 16.dp.toPx()
          val chartWidth = size.width - padLeft - padRight
          if (chartWidth > 0 && offset.x >= padLeft && offset.x <= size.width - padRight) {
            val progress = (offset.x - padLeft) / chartWidth
            val index = (progress * (metrics.size - 1)).toInt().coerceIn(0, metrics.size - 1)
            onSelectIndex(index)
          }
        }
      }
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val padLeft = 40.dp.toPx()
      val padRight = 16.dp.toPx()
      val padTop = 16.dp.toPx()
      val padBottom = 28.dp.toPx()

      val chartW = size.width - padLeft - padRight
      val chartH = size.height - padTop - padBottom

      if (chartW <= 0 || chartH <= 0) return@Canvas

      // 1. Cartesian Grid Horizontal Lines & Y-Axis Labels
      val gridSteps = 4
      for (i in 0..gridSteps) {
        val y = padTop + (chartH / gridSteps) * i
        val labelVal = maxVal - (i.toFloat() / gridSteps) * (maxVal - minVal)

        // Dotted grid line
        drawLine(
          color = CarbonBorder,
          start = Offset(padLeft, y),
          end = Offset(size.width - padRight, y),
          strokeWidth = 1.dp.toPx()
        )

        // Y-axis label text
        val labelStr = String.format(Locale.US, "%.1f", labelVal)
        drawContext.canvas.nativeCanvas.drawText(
          labelStr,
          padLeft - 6.dp.toPx(),
          y + 4.dp.toPx(),
          android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#94A3B8")
            textSize = 9.sp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
          }
        )
      }

      // Compute Point Coordinates
      val points = values.mapIndexed { idx, v ->
        val x = padLeft + (idx.toFloat() / (values.size - 1).coerceAtLeast(1)) * chartW
        val normY = (v - minVal) / (maxVal - minVal)
        val y = padTop + (1f - normY) * chartH
        Offset(x, y)
      }

      // 2. Draw Area Gradient under the Curve
      if (points.size >= 2) {
        val areaPath = Path().apply {
          moveTo(points.first().x, size.height - padBottom)
          points.forEach { lineTo(it.x, it.y) }
          lineTo(points.last().x, size.height - padBottom)
          close()
        }

        drawPath(
          path = areaPath,
          brush = Brush.verticalGradient(
            colors = listOf(
              metricType.color.copy(alpha = 0.25f),
              metricType.color.copy(alpha = 0.02f)
            ),
            startY = padTop,
            endY = size.height - padBottom
          )
        )

        // 3. Draw Monotone Line Curve
        val linePath = Path().apply {
          moveTo(points.first().x, points.first().y)
          for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val cX = (prev.x + curr.x) / 2f
            cubicTo(cX, prev.y, cX, curr.y, curr.x, curr.y)
          }
        }

        drawPath(
          path = linePath,
          color = metricType.color,
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      // 4. Draw Data Marker Dots and X-Axis Labels
      points.forEachIndexed { i, pt ->
        val isSelected = (i == selectedIndex)
        // Session X label
        drawContext.canvas.nativeCanvas.drawText(
          "S${i + 1}",
          pt.x,
          size.height - 8.dp.toPx(),
          android.graphics.Paint().apply {
            color = if (isSelected) android.graphics.Color.parseColor("#00E5FF") else android.graphics.Color.parseColor("#94A3B8")
            textSize = 9.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = isSelected
            isAntiAlias = true
          }
        )

        // Dot
        drawCircle(
          color = CarbonSurface,
          radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx(),
          center = pt
        )
        drawCircle(
          color = if (isSelected) Color.White else metricType.color,
          radius = if (isSelected) 5.dp.toPx() else 3.dp.toPx(),
          center = pt
        )
      }

      // 5. Active Tooltip Overlay
      if (selectedIndex in metrics.indices) {
        val activePt = points[selectedIndex]
        val activeMetric = metrics[selectedIndex]
        val activeVal = values[selectedIndex]

        // Vertical scrubber line
        drawLine(
          color = metricType.color.copy(alpha = 0.6f),
          start = Offset(activePt.x, padTop),
          end = Offset(activePt.x, size.height - padBottom),
          strokeWidth = 1.dp.toPx()
        )
      }
    }

    // Interactive Floating Tooltip Badge
    if (selectedIndex in metrics.indices) {
      val activeMetric = metrics[selectedIndex]
      Box(
        modifier = Modifier
          .align(Alignment.TopCenter)
          .clip(RoundedCornerShape(8.dp))
          .background(CarbonSurface.copy(alpha = 0.95f))
          .border(1.dp, metricType.color.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Column {
            Text(
              text = "Session ${selectedIndex + 1}: ${activeMetric.trackName}",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan
            )
            Text(
              text = "${activeMetric.vehicleName} • ${formatLapTimeVal(activeMetric.lapTimeMs)} (${activeMetric.topSpeedKmh.toInt()} km/h)",
              fontSize = 9.sp,
              color = TextPrimary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

private fun formatLapTimeVal(ms: Long): String {
  val minutes = (ms / 60000)
  val seconds = (ms % 60000) / 1000
  val millis = ms % 1000
  return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis)
}
