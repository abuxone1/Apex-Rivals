package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.RacePerformanceMetric
import com.example.data.repository.RacePerformanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Sorting options for race performance telemetry data.
 */
enum class PerformanceSortOption(val label: String) {
  NEWEST("Newest Runs"),
  FASTEST_LAP("Fastest Lap Time"),
  TOP_SPEED("Highest Speed"),
  TRACK_NAME("Track Name")
}

/**
 * Aggregate telemetry statistics computed across all recorded race sessions.
 */
data class PerformanceSummaryStats(
  val bestLapTimeMs: Long = 0L,
  val maxVehicleSpeedKmh: Float = 0f,
  val avgVehicleSpeedKmh: Float = 0f,
  val totalLapsRecorded: Int = 0,
  val fastestTrackName: String = ""
) {
  val formattedBestLap: String
    get() {
      if (bestLapTimeMs <= 0L) return "--:--.---"
      val minutes = bestLapTimeMs / 60000L
      val seconds = (bestLapTimeMs % 60000L) / 1000L
      val millis = bestLapTimeMs % 1000L
      return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
    }

  val formattedMaxSpeed: String
    get() = String.format(Locale.US, "%.1f km/h", maxVehicleSpeedKmh)

  val formattedAvgSpeed: String
    get() = String.format(Locale.US, "%.1f km/h", avgVehicleSpeedKmh)
}

/**
 * UI State for race performance telemetry management.
 */
data class RacePerformanceUiState(
  val allMetrics: List<RacePerformanceMetric> = emptyList(),
  val displayedMetrics: List<RacePerformanceMetric> = emptyList(),
  val availableTracks: List<String> = listOf("All Tracks"),
  val selectedTrackFilter: String = "All Tracks",
  val sortOption: PerformanceSortOption = PerformanceSortOption.NEWEST,
  val searchQuery: String = "",
  val stats: PerformanceSummaryStats = PerformanceSummaryStats(),
  val isLoading: Boolean = false,
  val statusMessage: String? = null
)

/**
 * ViewModel managing Room race performance telemetry data:
 * lap times, track names, vehicle speeds, dynamic filtering, sorting, and stats.
 */
class RacePerformanceViewModel(
  application: Application,
  private val repository: RacePerformanceRepository = RacePerformanceRepository(
    AppDatabase.getDatabase(application).racePerformanceMetricDao()
  ),
  coroutineScope: kotlinx.coroutines.CoroutineScope? = null
) : AndroidViewModel(application) {

  private val scope = coroutineScope ?: viewModelScope
  private val _selectedTrackFilter = MutableStateFlow("All Tracks")
  private val _sortOption = MutableStateFlow(PerformanceSortOption.NEWEST)
  private val _searchQuery = MutableStateFlow("")
  private val _statusMessage = MutableStateFlow<String?>(null)

  val uiState: StateFlow<RacePerformanceUiState> = combine(
    repository.allMetrics,
    _selectedTrackFilter,
    _sortOption,
    _searchQuery,
    _statusMessage
  ) { metrics, trackFilter, sortOpt, query, statusMsg ->
    // 1. Compute distinct available tracks
    val tracks = mutableListOf("All Tracks")
    tracks.addAll(metrics.map { it.trackName }.distinct().sorted())

    // 2. Filter by track and search query
    val filtered = metrics.filter { metric ->
      val matchesTrack = trackFilter == "All Tracks" || metric.trackName.equals(trackFilter, ignoreCase = true)
      val matchesQuery = query.isBlank() ||
          metric.trackName.contains(query, ignoreCase = true) ||
          metric.vehicleName.contains(query, ignoreCase = true) ||
          metric.driverName.contains(query, ignoreCase = true) ||
          metric.raceMode.contains(query, ignoreCase = true)
      matchesTrack && matchesQuery
    }

    // 3. Sort filtered results
    val sorted = when (sortOpt) {
      PerformanceSortOption.NEWEST -> filtered.sortedByDescending { it.timestamp }
      PerformanceSortOption.FASTEST_LAP -> filtered.sortedWith(
        compareBy<RacePerformanceMetric> { if (it.lapTimeMs > 0) it.lapTimeMs else Long.MAX_VALUE }
      )
      PerformanceSortOption.TOP_SPEED -> filtered.sortedByDescending { it.vehicleSpeed }
      PerformanceSortOption.TRACK_NAME -> filtered.sortedBy { it.trackName }
    }

    // 4. Calculate summary telemetry statistics
    val validLaps = metrics.filter { it.lapTimeMs > 0 }
    val bestLap = validLaps.minByOrNull { it.lapTimeMs }
    val maxSpeed = metrics.maxOfOrNull { it.vehicleSpeed } ?: 0f
    val speedsWithValues = metrics.map { it.vehicleSpeed }.filter { it > 0f }
    val avgSpeed = if (speedsWithValues.isNotEmpty()) speedsWithValues.average().toFloat() else 0f

    val stats = PerformanceSummaryStats(
      bestLapTimeMs = bestLap?.lapTimeMs ?: 0L,
      maxVehicleSpeedKmh = maxSpeed,
      avgVehicleSpeedKmh = avgSpeed,
      totalLapsRecorded = metrics.size,
      fastestTrackName = bestLap?.trackName ?: ""
    )

    RacePerformanceUiState(
      allMetrics = metrics,
      displayedMetrics = sorted,
      availableTracks = tracks,
      selectedTrackFilter = trackFilter,
      sortOption = sortOpt,
      searchQuery = query,
      stats = stats,
      isLoading = false,
      statusMessage = statusMsg
    )
  }.stateIn(
    scope = scope,
    started = SharingStarted.Eagerly,
    initialValue = RacePerformanceUiState(isLoading = true)
  )

  init {
    scope.launch {
      repository.seedInitialBenchmarkMetricsIfEmpty()
    }
  }

  /**
   * Records a new race performance metric into the Room database.
   */
  fun recordRacePerformance(
    trackName: String,
    lapTimeMs: Long,
    vehicleSpeed: Float,
    vehicleName: String = "Apex GT3-R Twin-Turbo",
    driverName: String = "Apex Pilot",
    lapNumber: Int = 1,
    avgSpeedKmh: Float = 0f,
    peakAccelerationG: Float = 0f,
    maxLateralG: Float = 0f,
    sector1Ms: Long = 0L,
    sector2Ms: Long = 0L,
    sector3Ms: Long = 0L,
    raceMode: String = "Time Trial",
    weather: String = "Dry Asphalt"
  ) {
    scope.launch {
      val id = repository.recordTelemetry(
        trackName = trackName,
        lapTimeMs = lapTimeMs,
        vehicleSpeed = vehicleSpeed,
        vehicleName = vehicleName,
        driverName = driverName,
        lapNumber = lapNumber,
        topSpeedKmh = vehicleSpeed,
        avgSpeedKmh = avgSpeedKmh,
        peakAccelerationG = peakAccelerationG,
        maxLateralG = maxLateralG,
        sector1Ms = sector1Ms,
        sector2Ms = sector2Ms,
        sector3Ms = sector3Ms,
        raceMode = raceMode,
        weather = weather
      )
      _statusMessage.value = "Telemetry saved (Lap ID: $id)"
    }
  }

  fun insertMetric(metric: RacePerformanceMetric) {
    scope.launch {
      repository.insertMetric(metric)
      _statusMessage.value = "Performance metric recorded"
    }
  }

  fun deleteMetric(id: Long) {
    scope.launch {
      repository.deleteMetricById(id)
      _statusMessage.value = "Metric deleted"
    }
  }

  fun clearAllMetrics() {
    scope.launch {
      repository.clearAllMetrics()
      _statusMessage.value = "All telemetry cleared"
    }
  }

  fun setTrackFilter(trackName: String) {
    _selectedTrackFilter.value = trackName
  }

  fun setSortOption(option: PerformanceSortOption) {
    _sortOption.value = option
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun dismissStatusMessage() {
    _statusMessage.value = null
  }
}
