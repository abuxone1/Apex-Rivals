package com.example.ui.components

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.local.PerformanceMetrics
import com.example.util.RaceSpeechService
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Individual recorded lap entry with delta to the fastest lap.
 */
data class LapRecord(
  val lapNumber: Int,
  val lapTimeMillis: Long,
  val deltaToBestMillis: Long? = null,
  val isBestLap: Boolean = false,
  val deltaToGhostMillis: Long? = null
) {
  val formattedTime: String
    get() = RaceTimerState.formatDuration(lapTimeMillis)
}

/**
 * Immutable UI State representing the real-time live race lap timer.
 */
data class RaceTimerState(
  val totalElapsedMillis: Long = 0L,
  val currentLapElapsedMillis: Long = 0L,
  val isRunning: Boolean = false,
  val currentLapNumber: Int = 1,
  val lapRecords: List<LapRecord> = emptyList(),
  val bestLapMillis: Long? = null,
  val ghostLap: PerformanceMetrics? = null,
  val isGhostModeEnabled: Boolean = true
) {
  // Minutes, seconds, and milliseconds breakdown of current lap time
  val minutes: Int
    get() = ((currentLapElapsedMillis / 60000) % 60).toInt()

  val seconds: Int
    get() = ((currentLapElapsedMillis / 1000) % 60).toInt()

  val milliseconds: Int
    get() = (currentLapElapsedMillis % 1000).toInt()

  val formattedMinutes: String
    get() = String.format(Locale.US, "%02d", minutes)

  val formattedSeconds: String
    get() = String.format(Locale.US, "%02d", seconds)

  val formattedMilliseconds: String
    get() = String.format(Locale.US, "%03d", milliseconds)

  val formattedCurrentLapTime: String
    get() = String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, milliseconds)

  val formattedTotalTime: String
    get() = formatDuration(totalElapsedMillis)

  // Backward-compatibility properties
  val elapsedMillis: Long
    get() = totalElapsedMillis

  val currentLapStartTimeMillis: Long
    get() = totalElapsedMillis - currentLapElapsedMillis

  val laps: List<Long>
    get() = lapRecords.map { it.lapTimeMillis }

  val formattedTime: String
    get() = formattedTotalTime

  val formattedLapTime: String
    get() = formattedCurrentLapTime

  companion object {
    fun formatDuration(millis: Long): String {
      val totalSeconds = millis / 1000
      val minutes = (totalSeconds / 60) % 60
      val seconds = totalSeconds % 60
      val milliseconds = millis % 1000
      return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, milliseconds)
    }
  }
}

/**
 * ViewModel managing the RaceTimer StateFlow, monotonic clock, and lap splitting.
 */
class RaceTimerViewModel : ViewModel() {

  private val _timerState = MutableStateFlow(RaceTimerState())
  val timerState: StateFlow<RaceTimerState> = _timerState.asStateFlow()

  private var timerJob: Job? = null
  private var sessionStartSystemTime: Long = 0L
  private var accumulatedSessionTime: Long = 0L
  private var lapStartSystemTime: Long = 0L
  private var accumulatedLapTime: Long = 0L

  /**
   * Starts tracking current lap time and total session elapsed time in real-time.
   */
  fun start() {
    if (_timerState.value.isRunning) return

    val now = SystemClock.elapsedRealtime()
    sessionStartSystemTime = now
    lapStartSystemTime = now

    _timerState.update { it.copy(isRunning = true) }

    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (isActive) {
        val currentNow = SystemClock.elapsedRealtime()
        val totalElapsed = accumulatedSessionTime + (currentNow - sessionStartSystemTime)
        val lapElapsed = accumulatedLapTime + (currentNow - lapStartSystemTime)

        _timerState.update {
          it.copy(
            totalElapsedMillis = totalElapsed,
            currentLapElapsedMillis = lapElapsed
          )
        }
        delay(16L) // ~60fps smooth precision ticker
      }
    }
  }

  /**
   * Stops live time tracking.
   */
  fun stop() {
    if (!_timerState.value.isRunning) return

    timerJob?.cancel()
    timerJob = null

    val now = SystemClock.elapsedRealtime()
    accumulatedSessionTime += (now - sessionStartSystemTime)
    accumulatedLapTime += (now - lapStartSystemTime)

    _timerState.update {
      it.copy(
        isRunning = false,
        totalElapsedMillis = accumulatedSessionTime,
        currentLapElapsedMillis = accumulatedLapTime
      )
    }
  }

  /**
   * Records completed lap split time and resets current lap clock for the next lap.
   */
  fun recordLap(): LapRecord? {
    val currentState = _timerState.value
    if (!currentState.isRunning && currentState.currentLapElapsedMillis == 0L) return null

    val now = SystemClock.elapsedRealtime()
    val completedLapDuration = accumulatedLapTime + (now - lapStartSystemTime)
    if (completedLapDuration <= 0L) return null

    // Reset current lap clock for the next lap
    lapStartSystemTime = now
    accumulatedLapTime = 0L

    val currentBest = currentState.bestLapMillis
    val isBest = (currentBest == null || completedLapDuration < currentBest)
    val newBest = if (isBest) completedLapDuration else currentBest

    val deltaToBest = if (currentBest != null) completedLapDuration - currentBest else 0L

    val deltaToGhost = if (currentState.isGhostModeEnabled && currentState.ghostLap != null) {
      completedLapDuration - currentState.ghostLap.lapTimeMs
    } else null

    val newRecord = LapRecord(
      lapNumber = currentState.currentLapNumber,
      lapTimeMillis = completedLapDuration,
      deltaToBestMillis = deltaToBest,
      isBestLap = isBest,
      deltaToGhostMillis = deltaToGhost
    )

    // Re-evaluate previous records if a new fastest lap was set
    val updatedLaps = currentState.lapRecords.map { lap ->
      if (isBest && lap.isBestLap) {
        lap.copy(isBestLap = false, deltaToBestMillis = lap.lapTimeMillis - newBest)
      } else {
        lap
      }
    } + newRecord

    _timerState.update {
      it.copy(
        currentLapNumber = it.currentLapNumber + 1,
        currentLapElapsedMillis = 0L,
        lapRecords = updatedLaps,
        bestLapMillis = newBest
      )
    }

    return newRecord
  }

  /**
   * Resets all session and lap timing data to standby while preserving ghost settings.
   */
  fun reset() {
    timerJob?.cancel()
    timerJob = null
    accumulatedSessionTime = 0L
    accumulatedLapTime = 0L
    sessionStartSystemTime = 0L
    lapStartSystemTime = 0L

    val ghost = _timerState.value.ghostLap
    val ghostEnabled = _timerState.value.isGhostModeEnabled
    _timerState.value = RaceTimerState(ghostLap = ghost, isGhostModeEnabled = ghostEnabled)
  }

  /**
   * Sets or updates the target Ghost Lap from PerformanceMetrics.
   */
  fun selectGhost(metric: PerformanceMetrics?) {
    _timerState.update { it.copy(ghostLap = metric) }
  }

  /**
   * Clears active ghost lap benchmark.
   */
  fun clearGhost() {
    _timerState.update { it.copy(ghostLap = null) }
  }

  /**
   * Toggles Ghost Mode overlay on or off.
   */
  fun toggleGhostMode(enabled: Boolean? = null) {
    _timerState.update { it.copy(isGhostModeEnabled = enabled ?: !it.isGhostModeEnabled) }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
  }
}

/**
 * Live Race Timer Composable.
 * Tracks current lap time in real-time, displaying minutes, seconds, and milliseconds,
 * with start, stop, and lap functionality.
 */
@Composable
fun RaceTimer(
  timerStateFlow: StateFlow<RaceTimerState>,
  onStart: () -> Unit,
  onStop: () -> Unit,
  onReset: () -> Unit,
  onLap: () -> Unit = {},
  onSelectGhost: ((PerformanceMetrics) -> Unit)? = null,
  onClearGhost: (() -> Unit)? = null,
  onToggleGhostMode: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val state by timerStateFlow.collectAsStateWithLifecycle()
  var showGhostSelector by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(16.dp)
      .testTag("race_timer_container"),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Header Bar: Title, Lap Number Badge & Running Status Indicator
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Timer,
          contentDescription = "Live Race Timer",
          tint = NeonCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "LIVE RACE TIMER",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Current Lap Indicator Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(NeonCyan.copy(alpha = 0.15f))
            .border(0.8.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "LAP ${state.currentLapNumber}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Action Row: TTS Audio Toggle + Timing State Status Badge
      Row(verticalAlignment = Alignment.CenterVertically) {
        val context = LocalContext.current
        val speechService = remember { RaceSpeechService.getInstance(context) }
        val speechState by speechService.state.collectAsStateWithLifecycle()

        // Quick Voice Comms Toggle
        IconButton(
          onClick = { speechService.toggleAudioEnabled() },
          modifier = Modifier
            .size(32.dp)
            .testTag("race_timer_voice_comms_toggle")
        ) {
          Icon(
            imageVector = if (speechState.isEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
            contentDescription = if (speechState.isEnabled) "Voice Comms Active" else "Voice Comms Muted",
            tint = if (speechState.isEnabled) NeonCyan else TextTertiary,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Ghost Mode Toggle / Selector Button
        IconButton(
          onClick = { showGhostSelector = true },
          modifier = Modifier
            .size(32.dp)
            .testTag("race_timer_ghost_mode_toggle")
        ) {
          Icon(
            imageVector = if (state.isGhostModeEnabled && state.ghostLap != null) Icons.Default.Visibility else Icons.Default.VisibilityOff,
            contentDescription = if (state.isGhostModeEnabled && state.ghostLap != null) "Ghost Mode Active" else "Select Ghost Lap",
            tint = if (state.isGhostModeEnabled && state.ghostLap != null) PurpleDelta else TextTertiary,
            modifier = Modifier.size(16.dp)
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Timing State Status Badge
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
          .background(
            when {
              state.isRunning -> ApexGreen.copy(alpha = 0.2f)
              state.totalElapsedMillis > 0 -> NeonAmber.copy(alpha = 0.2f)
              else -> CarbonSurfaceVariant
            }
          )
          .border(
            1.dp,
            when {
              state.isRunning -> ApexGreen
              state.totalElapsedMillis > 0 -> NeonAmber
              else -> CarbonBorder
            },
            RoundedCornerShape(6.dp)
          )
          .padding(horizontal = 8.dp, vertical = 3.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(
                when {
                  state.isRunning -> ApexGreen
                  state.totalElapsedMillis > 0 -> NeonAmber
                  else -> TextTertiary
                }
              )
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = when {
              state.isRunning -> "LIVE TIMING"
              state.totalElapsedMillis > 0 -> "PAUSED"
              else -> "STANDBY"
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = when {
              state.isRunning -> ApexGreen
              state.totalElapsedMillis > 0 -> NeonAmber
              else -> TextSecondary
            },
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }

    Spacer(modifier = Modifier.height(10.dp))

    // Real-Time Ghost Mode Overlay & Benchmark Comparison Card
    GhostLapComparisonCard(
      ghostLap = state.ghostLap,
      isGhostModeEnabled = state.isGhostModeEnabled,
      currentLapElapsedMillis = state.currentLapElapsedMillis,
      isRunning = state.isRunning,
      lastCompletedLapMillis = state.lapRecords.lastOrNull()?.lapTimeMillis,
      onOpenSelector = { showGhostSelector = true },
      onToggleGhostMode = { onToggleGhostMode?.invoke() },
      onClearGhost = { onClearGhost?.invoke() }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Real-Time Primary Digital Time Display: Minutes, Seconds, and Milliseconds
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(CarbonBlack)
        .border(
          1.dp,
          if (state.isRunning) NeonCyan.copy(alpha = 0.6f) else CarbonBorder,
          RoundedCornerShape(12.dp)
        )
        .padding(vertical = 14.dp, horizontal = 12.dp)
        .testTag("race_timer_display"),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Label indicating current lap time tracking
        Text(
          text = "CURRENT LAP TIME",
          fontSize = 9.sp,
          fontWeight = FontWeight.Black,
          color = if (state.isRunning) NeonCyan else TextTertiary,
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Time Units: Minutes, Seconds, Milliseconds
        Row(
          verticalAlignment = Alignment.Bottom,
          horizontalArrangement = Arrangement.Center
        ) {
          // Minutes
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = state.formattedMinutes,
              fontSize = 38.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = if (state.isRunning) NeonCyan else TextPrimary,
              letterSpacing = 1.sp
            )
            Text(
              text = "MIN",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary,
              letterSpacing = 1.sp
            )
          }

          // Colon Separator
          Text(
            text = ":",
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (state.isRunning) NeonCyan.copy(alpha = 0.8f) else TextTertiary,
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 10.dp)
          )

          // Seconds
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = state.formattedSeconds,
              fontSize = 38.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = if (state.isRunning) NeonCyan else TextPrimary,
              letterSpacing = 1.sp
            )
            Text(
              text = "SEC",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary,
              letterSpacing = 1.sp
            )
          }

          // Dot Separator
          Text(
            text = ".",
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = if (state.isRunning) NeonCyan.copy(alpha = 0.8f) else TextTertiary,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 10.dp)
          )

          // Milliseconds
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = state.formattedMilliseconds,
              fontSize = 30.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = if (state.isRunning) NeonAmber else TextSecondary,
              letterSpacing = 1.sp,
              modifier = Modifier.padding(bottom = 3.dp)
            )
            Text(
              text = "MS",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary,
              letterSpacing = 1.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Total Session Elapsed Time Footer
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = "SESSION TOTAL: ",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary
          )
          Text(
            text = state.formattedTotalTime,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Best Lap Indicator (if recorded)
    if (state.bestLapMillis != null) {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CarbonSurfaceVariant)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = PurpleDelta,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "SESSION BEST LAP",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
          )
        }

        Text(
          text = RaceTimerState.formatDuration(state.bestLapMillis!!),
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = PurpleDelta,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Primary Control Buttons: Start, Stop, Lap, Reset
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Start / Stop Toggle Button
      Button(
        onClick = {
          if (state.isRunning) onStop() else onStart()
        },
        modifier = Modifier
          .weight(1.3f)
          .height(48.dp)
          .testTag("race_timer_start_stop_button"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (state.isRunning) RedlineRed else NeonCyan,
          contentColor = if (state.isRunning) Color.White else CarbonBlack
        )
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (state.isRunning) "Stop Timer" else "Start Timer",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = if (state.isRunning) "STOP" else "START",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
          )
        }
      }

      // Record Lap Split Button
      Button(
        onClick = onLap,
        enabled = state.isRunning,
        modifier = Modifier
          .weight(1.1f)
          .height(48.dp)
          .testTag("race_timer_lap_button"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonAmber,
          contentColor = CarbonBlack,
          disabledContainerColor = CarbonSurfaceVariant,
          disabledContentColor = TextTertiary
        )
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = "Record Lap",
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "LAP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
          )
        }
      }

      // Reset Button
      OutlinedButton(
        onClick = onReset,
        enabled = (state.totalElapsedMillis > 0L && !state.isRunning) || state.lapRecords.isNotEmpty(),
        modifier = Modifier
          .weight(0.9f)
          .height(48.dp)
          .testTag("race_timer_reset_button"),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = TextPrimary,
          disabledContentColor = TextTertiary.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (state.totalElapsedMillis > 0L && !state.isRunning) CarbonBorder else CarbonSurfaceVariant
        )
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Reset Timer",
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "RESET",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Recorded Laps Split History List
    AnimatedVisibility(visible = state.lapRecords.isNotEmpty()) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "RECORDED LAP SPLITS (${state.lapRecords.size})",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.5.sp
          )
          if (state.lapRecords.isNotEmpty()) {
            val avgLap = state.lapRecords.map { it.lapTimeMillis }.average().toLong()
            Text(
              text = "AVG: ${RaceTimerState.formatDuration(avgLap)}",
              fontSize = 9.5.sp,
              color = TextTertiary,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height((state.lapRecords.size.coerceAtMost(4) * 38).dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          itemsIndexed(state.lapRecords.reversed()) { _, lap ->
            val isFastest = lap.isBestLap

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isFastest) PurpleDelta.copy(alpha = 0.12f) else CarbonSurfaceVariant)
                .border(
                  0.8.dp,
                  if (isFastest) PurpleDelta.copy(alpha = 0.5f) else Color.Transparent,
                  RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "Lap ${lap.lapNumber}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isFastest) PurpleDelta else TextPrimary
                )
                if (isFastest) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(3.dp))
                      .background(PurpleDelta)
                      .padding(horizontal = 4.dp, vertical = 1.dp)
                  ) {
                    Text(
                      text = "BEST",
                      fontSize = 8.sp,
                      fontWeight = FontWeight.Black,
                      color = CarbonBlack,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                } else if (lap.deltaToBestMillis != null && lap.deltaToBestMillis > 0) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "+${String.format(Locale.US, "%.3f", lap.deltaToBestMillis / 1000f)}s",
                    fontSize = 9.sp,
                    color = RedlineRed,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }

              Text(
                text = lap.formattedTime,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFastest) PurpleDelta else TextPrimary,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }
  }

  // Ghost Lap Selection Dialog from PerformanceMetrics DB
  if (showGhostSelector) {
    GhostLapSelectorDialog(
      currentGhostLap = state.ghostLap,
      onSelectGhost = { metric ->
        onSelectGhost?.invoke(metric)
        showGhostSelector = false
      },
      onClearGhost = {
        onClearGhost?.invoke()
        showGhostSelector = false
      },
      onDismiss = { showGhostSelector = false }
    )
  }
}

/**
 * Convenience overload that instantiates a scoped RaceTimerViewModel automatically
 * and integrates with RaceSpeechService for lap announcements and Ghost comparison.
 */
@Composable
fun RaceTimer(
  modifier: Modifier = Modifier,
  viewModel: RaceTimerViewModel = viewModel()
) {
  val context = LocalContext.current
  val speechService = remember { RaceSpeechService.getInstance(context) }
  val state by viewModel.timerState.collectAsStateWithLifecycle()

  RaceTimer(
    timerStateFlow = viewModel.timerState,
    onStart = {
      viewModel.start()
      val ghostNotice = if (state.isGhostModeEnabled && state.ghostLap != null) {
        " Ghost target: ${state.ghostLap!!.trackName} in ${state.ghostLap!!.lapTimeMs / 1000} seconds."
      } else ""
      speechService.speak("Race timer started.$ghostNotice", flush = true)
    },
    onStop = {
      viewModel.stop()
      speechService.speak("Session stopped.", flush = true)
    },
    onReset = viewModel::reset,
    onLap = {
      val record = viewModel.recordLap()
      if (record != null) {
        speechService.announceLapTime(
          lapNumber = record.lapNumber,
          lapTimeMillis = record.lapTimeMillis,
          isBestLap = record.isBestLap,
          deltaToBestMillis = if (!record.isBestLap) record.deltaToBestMillis else null
        )

        val ghostDelta = record.deltaToGhostMillis
        if (ghostDelta != null) {
          if (ghostDelta <= 0) {
            val s = String.format(Locale.US, "%.1f", Math.abs(ghostDelta) / 1000f)
            speechService.speak("Outstanding! You beat the ghost lap by $s seconds!", flush = false)
          } else {
            val s = String.format(Locale.US, "%.1f", ghostDelta / 1000f)
            speechService.speak("$s seconds behind ghost benchmark.", flush = false)
          }
        }
      }
    },
    onSelectGhost = { metric ->
      viewModel.selectGhost(metric)
      speechService.speak("Ghost set: ${metric.driverName}, ${metric.lapTimeMs / 1000} seconds.", flush = true)
    },
    onClearGhost = {
      viewModel.clearGhost()
      speechService.speak("Ghost benchmark cleared.", flush = true)
    },
    onToggleGhostMode = {
      viewModel.toggleGhostMode()
    },
    modifier = modifier
  )
}
