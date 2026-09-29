package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import com.example.data.firebase.DriverProfile
import com.example.data.firebase.FirebaseAuthAndFirestoreService
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.PerformanceMetrics
import com.example.model.BadgeEvaluator
import com.example.ui.components.DriverBadgesSection
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PlatformAndroid
import com.example.ui.theme.ShiftBlue
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
  onNavigateBack: () -> Unit,
  onNavigateToTelemetryDb: () -> Unit = {},
  onNavigateToPlayConsole: () -> Unit = {},
  initialIsMph: Boolean = false
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val db = remember { AppDatabase.getDatabase(context) }
  val dao = remember { db.performanceMetricsDao() }

  val metricsList by dao.getAllPerformanceMetrics().collectAsState(initial = emptyList())
  var isMph by remember { mutableStateOf(initialIsMph) }
  var showResetConfirmDialog by remember { mutableStateOf(false) }

  // Firebase Auth & Firestore Persistence
  val firebaseService = remember { FirebaseAuthAndFirestoreService(context) }
  val authProfile by firebaseService.observeAuthState().collectAsState(initial = null)
  var isGoogleSigningIn by remember { mutableStateOf(false) }
  var isFirestoreSyncing by remember { mutableStateOf(false) }
  var firestoreSyncStatus by remember { mutableStateOf<String?>(null) }

  // Seed default realistic telemetry data into Room DB if empty, ensuring instant profile stats
  LaunchedEffect(Unit) {
    launch(Dispatchers.IO) {
      val existing = dao.getAllList()
      if (existing.isEmpty()) {
        val samples = listOf(
          PerformanceMetrics(
            raceId = "race_monza_01",
            driverName = "Apex Pilot",
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
            timestamp = System.currentTimeMillis() - 3600_000 * 5
          ),
          PerformanceMetrics(
            raceId = "race_spa_02",
            driverName = "Apex Pilot",
            trackName = "Spa-Francorchamps",
            vehicleName = "Formula Apex Hybrid",
            lapTimeMs = 103250L,
            topSpeedKmh = 344.2f,
            peakAccelerationG = 2.40f,
            zeroToHundredKmhSeconds = 2.20f,
            maxLateralG = 3.85f,
            trackDistanceMeters = 7004,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Spa-Francorchamps", 103250L, 7004),
            raceType = "Championship",
            trackComplexity = "Flowing",
            weatherCondition = "Wet Surface",
            tags = listOf("Flowing", "Wet Surface", "P-Zero Inters"),
            timestamp = System.currentTimeMillis() - 3600_000 * 3
          ),
          PerformanceMetrics(
            raceId = "race_suzuka_03",
            driverName = "Apex Pilot",
            trackName = "Suzuka GP",
            vehicleName = "Apex GT3-R Twin-Turbo",
            lapTimeMs = 118400L,
            topSpeedKmh = 295.4f,
            peakAccelerationG = 1.95f,
            zeroToHundredKmhSeconds = 2.85f,
            maxLateralG = 2.65f,
            trackDistanceMeters = 5807,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Suzuka GP", 118400L, 5807),
            raceType = "Time Trial",
            trackComplexity = "Technical",
            weatherCondition = "Dry Asphalt",
            tags = listOf("Technical", "Figure 8", "High Downforce"),
            timestamp = System.currentTimeMillis() - 3600_000 * 2
          ),
          PerformanceMetrics(
            raceId = "race_silverstone_04",
            driverName = "Apex Pilot",
            trackName = "Silverstone Circuit",
            vehicleName = "Formula Apex Hybrid",
            lapTimeMs = 87620L,
            topSpeedKmh = 338.0f,
            peakAccelerationG = 2.30f,
            zeroToHundredKmhSeconds = 2.25f,
            maxLateralG = 4.10f,
            trackDistanceMeters = 5891,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Silverstone Circuit", 87620L, 5891),
            raceType = "Time Trial",
            trackComplexity = "High Speed",
            weatherCondition = "Dry Asphalt",
            tags = listOf("Copse Corner", "Maggotts-Becketts", "Aero Grip"),
            timestamp = System.currentTimeMillis() - 3600_000 * 1
          ),
          PerformanceMetrics(
            raceId = "race_nurburgring_05",
            driverName = "Apex Pilot",
            trackName = "Nürburgring GP",
            vehicleName = "Hypercar Proto LM",
            lapTimeMs = 94800L,
            topSpeedKmh = 312.5f,
            peakAccelerationG = 2.10f,
            zeroToHundredKmhSeconds = 2.50f,
            maxLateralG = 2.80f,
            trackDistanceMeters = 5148,
            avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed("Nürburgring GP", 94800L, 5148),
            raceType = "Time Trial",
            trackComplexity = "Technical",
            weatherCondition = "Night Run",
            tags = listOf("Night Run", "Technical", "Carbon Ceramics"),
            timestamp = System.currentTimeMillis() - 1800_000
          )
        )
        dao.insertAll(samples)
      }
    }
  }

  // --- Aggregate Stats from Room Database ---
  val totalDistanceMeters = metricsList.sumOf { it.getEffectiveDistanceMeters().toLong() }
  val totalDistanceKm = totalDistanceMeters / 1000.0f
  val totalDistanceMiles = totalDistanceKm * 0.621371f

  val totalLapTimeMs = metricsList.sumOf { it.lapTimeMs }
  val totalRacingHours = totalLapTimeMs / 3600000.0f

  val averageSpeedKmh = if (totalRacingHours > 0f && totalDistanceKm > 0f) {
    (totalDistanceKm / totalRacingHours)
  } else if (metricsList.isNotEmpty()) {
    metricsList.map { it.getComputedAvgSpeed() }.filter { it > 0f }.average().toFloat()
  } else 0f
  val averageSpeedMph = averageSpeedKmh * 0.621371f

  // Personal Records (PRs) from Room Database
  val bestLapRecord = metricsList.filter { it.lapTimeMs > 0 }.minByOrNull { it.lapTimeMs }
  val topSpeedRecord = metricsList.maxByOrNull { it.topSpeedKmh }
  val bestZeroToHundredRecord = metricsList.filter { it.zeroToHundredKmhSeconds > 0 }.minByOrNull { it.zeroToHundredKmhSeconds }
  val maxLateralGRecord = metricsList.maxByOrNull { it.maxLateralG }
  val maxPeakAccelRecord = metricsList.maxByOrNull { it.peakAccelerationG }

  // Track Breakdown Records
  val trackRecords = metricsList.groupBy { it.trackName }
  // Vehicle Breakdown
  val vehicleRecords = metricsList.groupBy { it.vehicleName }

  // Badges & Awards evaluated dynamically from Room Database Telemetry
  val driverBadges = remember(metricsList, isMph) {
    BadgeEvaluator.evaluateBadges(metricsList, isMph)
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("user_profile_screen"),
    containerColor = CarbonBlack,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = CarbonSurface,
          titleContentColor = TextPrimary
        ),
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("profile_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = NeonCyan
            )
          }
        },
        title = {
          Column {
            Text(
              text = "DRIVER CAREER PROFILE",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
            Text(
              text = "ROOM DATABASE TELEMETRY AGGREGATE",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              letterSpacing = 0.5.sp
            )
          }
        },
        actions = {
          // Unit Switcher (Metric KM/H <-> Imperial MPH)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(CarbonSurfaceVariant)
              .border(1.dp, NeonAmber.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
              .clickable { isMph = !isMph }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("profile_unit_toggle")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = null,
                tint = NeonAmber,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isMph) "IMPERIAL (MPH)" else "METRIC (KM/H)",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black,
                color = NeonAmber,
                fontFamily = FontFamily.Monospace
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // 1. Driver Identity & Status Hero Card
      item {
        DriverIdentityHeaderCard(
          driverName = authProfile?.displayName ?: "Apex Pilot",
          licenseTier = authProfile?.licenseGrade ?: "S-CLASS ESPORTS PRO",
          totalSessions = metricsList.size,
          dbStatusText = "Room Database Active • ${metricsList.size} Saved Runs",
          unlockedBadgesCount = driverBadges.count { it.isUnlocked },
          totalBadgesCount = driverBadges.size
        )
      }

      // 2. Firebase Cloud Authentication & Firestore Data Persistence Card
      item {
        FirebaseAuthFirestoreCard(
          profile = authProfile,
          isGoogleSigningIn = isGoogleSigningIn,
          isFirestoreSyncing = isFirestoreSyncing,
          syncStatusMessage = firestoreSyncStatus,
          onGoogleSignIn = {
            coroutineScope.launch {
              isGoogleSigningIn = true
              val p = firebaseService.signInWithGoogle(context)
              isGoogleSigningIn = false
              if (p != null) {
                firestoreSyncStatus = "Signed in with Google! Cloud profile active."
              }
            }
          },
          onSyncToFirestore = {
            coroutineScope.launch {
              isFirestoreSyncing = true
              val current = authProfile ?: DriverProfile(
                uid = "driver_cloud_${System.currentTimeMillis() % 10000}",
                displayName = "Apex Pilot",
                email = "abux.one@gmail.com",
                isAnonymous = false,
                isGoogleSignedIn = true
              )
              val success = firebaseService.syncUserTelemetryToFirestore(current, metricsList)
              isFirestoreSyncing = false
              firestoreSyncStatus = if (success) "Firestore: All ${metricsList.size} records synced to Cloud!" else "Firestore: Offline queue updated"
            }
          },
          onSignOut = {
            coroutineScope.launch {
              firebaseService.signOut()
              firestoreSyncStatus = "Signed out of Google account."
            }
          }
        )
      }

      // 3. Core Career Statistics (Total Distance, Average Speed, Total Sessions)
      item {
        Text(
          text = "CAREER PERFORMANCE TOTALS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        CareerTotalsSection(
          totalDistanceKm = totalDistanceKm,
          totalDistanceMiles = totalDistanceMiles,
          averageSpeedKmh = averageSpeedKmh,
          averageSpeedMph = averageSpeedMph,
          totalSessions = metricsList.size,
          totalLapTimeMs = totalLapTimeMs,
          isMph = isMph
        )
      }

      // 3. Personal Records (PRs) Dashboard
      item {
        Text(
          text = "ALL-TIME PERSONAL RECORDS (ROOM DB)",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        PersonalRecordsSection(
          bestLapRecord = bestLapRecord,
          topSpeedRecord = topSpeedRecord,
          bestZeroToHundredRecord = bestZeroToHundredRecord,
          maxLateralGRecord = maxLateralGRecord,
          maxPeakAccelRecord = maxPeakAccelRecord,
          isMph = isMph
        )
      }

      // 4. Driver Awards & Badges System (Room DB Telemetry Milestones)
      item {
        Text(
          text = "CAREER TROPHIES & UNLOCKED AWARDS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        DriverBadgesSection(
          badges = driverBadges
        )
      }

      // 5. Circuit PRs Breakdown
      item {
        Text(
          text = "CIRCUIT RECORDS & MILEAGE BREAKDOWN",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        CircuitRecordsList(
          trackRecords = trackRecords,
          isMph = isMph
        )
      }

      // 5. Vehicle Mileage Breakdown
      item {
        Text(
          text = "VEHICLE FLEET UTILIZATION",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        VehicleUtilizationCard(
          vehicleRecords = vehicleRecords,
          isMph = isMph
        )
      }

      // 6. Interactive Database Actions
      item {
        InteractiveDatabaseActionsCard(
          onSimulateNewLap = {
            coroutineScope.launch(Dispatchers.IO) {
              val sampleTracks = listOf(
                Pair("Monza GP", 5793),
                Pair("Spa-Francorchamps", 7004),
                Pair("Suzuka GP", 5807),
                Pair("Silverstone Circuit", 5891),
                Pair("Nürburgring GP", 5148)
              )
              val sampleVehicles = listOf(
                "Apex GT3-R Twin-Turbo",
                "Formula Apex Hybrid",
                "Hypercar Proto LM"
              )
              val selectedTrack = sampleTracks.random()
              val selectedVehicle = sampleVehicles.random()
              val lapMs = (75000L..120000L).random()
              val topSpeed = Random.nextDouble(290.0, 355.0).toFloat()
              val zeroTo100 = Random.nextDouble(2.1, 3.2).toFloat()
              val latG = Random.nextDouble(2.2, 4.3).toFloat()
              val peakG = Random.nextDouble(1.8, 2.9).toFloat()

              val newEntry = PerformanceMetrics(
                raceId = "sim_${System.currentTimeMillis()}",
                driverName = "Apex Pilot",
                trackName = selectedTrack.first,
                vehicleName = selectedVehicle,
                lapTimeMs = lapMs,
                topSpeedKmh = topSpeed,
                peakAccelerationG = peakG,
                zeroToHundredKmhSeconds = zeroTo100,
                maxLateralG = latG,
                trackDistanceMeters = selectedTrack.second,
                avgSpeedKmh = PerformanceMetrics.calculateAverageSpeed(selectedTrack.first, lapMs, selectedTrack.second),
                raceType = "Time Trial",
                trackComplexity = "Technical",
                weatherCondition = "Dry Asphalt",
                tags = listOf("Simulated Run", "Live Telemetry"),
                timestamp = System.currentTimeMillis()
              )
              dao.insert(newEntry)
            }
          },
          onViewAllEntries = onNavigateToTelemetryDb,
          onResetDatabase = { showResetConfirmDialog = true }
        )
      }

      // 7. Google Play Publishing & Console Hub
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = CarbonSurface),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, PlatformAndroid.copy(alpha = 0.5f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToPlayConsole() }
            .testTag("profile_play_console_card")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(PlatformAndroid.copy(alpha = 0.15f))
                  .border(1.dp, PlatformAndroid, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudUpload,
                  contentDescription = null,
                  tint = PlatformAndroid,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "GOOGLE PLAY CONSOLE HUB",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black,
                  color = TextPrimary
                )
                Text(
                  text = "Launch Console, view store listing, copy descriptions & pre-launch checklist.",
                  fontSize = 9.5.sp,
                  color = TextSecondary
                )
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = onNavigateToPlayConsole,
              colors = ButtonDefaults.buttonColors(containerColor = PlatformAndroid),
              shape = RoundedCornerShape(6.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "OPEN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = CarbonBlack
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Reset Confirmation Dialog
  if (showResetConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showResetConfirmDialog = false },
      title = {
        Text(
          text = "RESET PROFILE TELEMETRY",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black,
          color = NeonAmber
        )
      },
      text = {
        Text(
          text = "Are you sure you want to clear all performance metrics in the Room database? You can re-seed standard records at any time.",
          fontSize = 12.sp,
          color = TextSecondary
        )
      },
      confirmButton = {
        Button(
          onClick = {
            coroutineScope.launch(Dispatchers.IO) {
              dao.clearAll()
            }
            showResetConfirmDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
          Text(text = "CLEAR ALL", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmDialog = false }) {
          Text(text = "CANCEL", color = TextTertiary)
        }
      },
      containerColor = CarbonSurface,
      shape = RoundedCornerShape(12.dp)
    )
  }
}

// -------------------------------------------------------------
// UI SUBCOMPONENTS
// -------------------------------------------------------------

@Composable
private fun DriverIdentityHeaderCard(
  driverName: String,
  licenseTier: String,
  totalSessions: Int,
  dbStatusText: String,
  unlockedBadgesCount: Int = 0,
  totalBadgesCount: Int = 0
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("driver_identity_card"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.horizontalGradient(
            listOf(NeonCyan.copy(alpha = 0.08f), Color.Transparent)
          )
        )
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Driver Avatar / Emblem
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(NeonCyan.copy(alpha = 0.15f))
            .border(2.dp, NeonCyan, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Driver Avatar",
            tint = NeonCyan,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = driverName,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(ApexGreen.copy(alpha = 0.15f))
                .border(0.8.dp, ApexGreen, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "VERIFIED",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = ApexGreen
              )
            }

            if (totalBadgesCount > 0) {
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(NeonAmber.copy(alpha = 0.15f))
                  .border(0.8.dp, NeonAmber, RoundedCornerShape(4.dp))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
                  .testTag("driver_badges_header_chip")
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = NeonAmber,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "$unlockedBadgesCount / $totalBadgesCount AWARDS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = NeonAmber,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }

          Text(
            text = licenseTier,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = dbStatusText,
            fontSize = 9.sp,
            color = TextTertiary,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

@Composable
private fun CareerTotalsSection(
  totalDistanceKm: Float,
  totalDistanceMiles: Float,
  averageSpeedKmh: Float,
  averageSpeedMph: Float,
  totalSessions: Int,
  totalLapTimeMs: Long,
  isMph: Boolean
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // 2-Column Grid for Distance & Average Speed
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. TOTAL DISTANCE DRIVEN
      Card(
        modifier = Modifier
          .weight(1f)
          .testTag("profile_total_distance"),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "TOTAL DISTANCE",
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              color = TextTertiary,
              letterSpacing = 0.5.sp
            )
            Icon(
              imageVector = Icons.Default.Route,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Value
          val distFormatted = if (isMph) {
            String.format(Locale.US, "%.1f", totalDistanceMiles)
          } else {
            String.format(Locale.US, "%.1f", totalDistanceKm)
          }
          val unit = if (isMph) "MILES" else "KM"

          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = distFormatted,
              fontSize = 24.sp,
              fontWeight = FontWeight.Black,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = unit,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              modifier = Modifier.padding(bottom = 3.dp)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          val altDist = if (isMph) {
            String.format(Locale.US, "%.1f km", totalDistanceKm)
          } else {
            String.format(Locale.US, "%.1f mi", totalDistanceMiles)
          }
          Text(
            text = "Equiv: $altDist",
            fontSize = 9.sp,
            color = TextTertiary,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // 2. CAREER AVERAGE SPEED
      Card(
        modifier = Modifier
          .weight(1f)
          .testTag("profile_average_speed"),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "AVERAGE SPEED",
              fontSize = 10.sp,
              fontWeight = FontWeight.Black,
              color = TextTertiary,
              letterSpacing = 0.5.sp
            )
            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = null,
              tint = NeonAmber,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          val avgSpeedFormatted = if (isMph) {
            String.format(Locale.US, "%.1f", averageSpeedMph)
          } else {
            String.format(Locale.US, "%.1f", averageSpeedKmh)
          }
          val speedUnit = if (isMph) "MPH" else "KM/H"

          Row(verticalAlignment = Alignment.Bottom) {
            Text(
              text = avgSpeedFormatted,
              fontSize = 24.sp,
              fontWeight = FontWeight.Black,
              color = NeonAmber,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = speedUnit,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary,
              modifier = Modifier.padding(bottom = 3.dp)
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          val altSpeed = if (isMph) {
            String.format(Locale.US, "%.1f km/h", averageSpeedKmh)
          } else {
            String.format(Locale.US, "%.1f mph", averageSpeedMph)
          }
          Text(
            text = "Equiv: $altSpeed",
            fontSize = 9.sp,
            color = TextTertiary,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // Sessions Count & Total Racing Time Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("profile_total_sessions"),
      colors = CardDefaults.cardColors(containerColor = CarbonSurface),
      shape = RoundedCornerShape(10.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = null,
            tint = ApexGreen,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "TOTAL RECORDED SESSIONS",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary
            )
            Text(
              text = "$totalSessions Race Entries",
              fontSize = 14.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "TOTAL TRACK TIME",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary
          )
          Text(
            text = formatDuration(totalLapTimeMs),
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = ApexGreen,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

@Composable
private fun PersonalRecordsSection(
  bestLapRecord: PerformanceMetrics?,
  topSpeedRecord: PerformanceMetrics?,
  bestZeroToHundredRecord: PerformanceMetrics?,
  maxLateralGRecord: PerformanceMetrics?,
  maxPeakAccelRecord: PerformanceMetrics?,
  isMph: Boolean
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("personal_records_section"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. All-time Best Lap
      PersonalRecordItem(
        tag = "pr_best_lap",
        label = "FASTEST LAP RECORD",
        primaryValue = bestLapRecord?.let { formatLapTime(it.lapTimeMs) } ?: "--:--.---",
        subDetail = bestLapRecord?.let { "${it.trackName} • ${it.vehicleName}" } ?: "No lap recorded yet",
        icon = Icons.Default.Timer,
        accentColor = NeonCyan
      )

      // 2. All-time Top Speed
      val topSpeedValue = if (topSpeedRecord != null) {
        if (isMph) {
          String.format(Locale.US, "%.1f MPH", topSpeedRecord.topSpeedKmh * 0.621371f)
        } else {
          String.format(Locale.US, "%.1f KM/H", topSpeedRecord.topSpeedKmh)
        }
      } else "---"

      PersonalRecordItem(
        tag = "pr_top_speed",
        label = "TOP SPEED RECORD",
        primaryValue = topSpeedValue,
        subDetail = topSpeedRecord?.let { "${it.trackName} • ${it.vehicleName}" } ?: "No record yet",
        icon = Icons.Default.Speed,
        accentColor = NeonAmber
      )

      // 3. Best 0-100 km/h (0-60 mph)
      val zeroTo100Value = bestZeroToHundredRecord?.let {
        String.format(Locale.US, "%.2f SEC", it.zeroToHundredKmhSeconds)
      } ?: "--"

      PersonalRecordItem(
        tag = "pr_zero_to_hundred",
        label = if (isMph) "FASTEST 0-60 MPH ACCELERATION" else "FASTEST 0-100 KM/H ACCELERATION",
        primaryValue = zeroTo100Value,
        subDetail = bestZeroToHundredRecord?.let { "${it.vehicleName} • Launch Control" } ?: "No launch data",
        icon = Icons.Default.DirectionsCar,
        accentColor = ApexGreen
      )

      // 4. Max Lateral G-Force
      val lateralGValue = maxLateralGRecord?.let {
        String.format(Locale.US, "%.2f G", it.maxLateralG)
      } ?: "--"

      PersonalRecordItem(
        tag = "pr_lateral_g",
        label = "PEAK CORNERING LATERAL G",
        primaryValue = lateralGValue,
        subDetail = maxLateralGRecord?.let { "${it.trackName} • ${it.vehicleName}" } ?: "No telemetry",
        icon = Icons.Default.EmojiEvents,
        accentColor = ShiftBlue
      )
    }
  }
}

@Composable
private fun PersonalRecordItem(
  tag: String,
  label: String,
  primaryValue: String,
  subDetail: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CarbonSurfaceVariant)
      .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
      .padding(10.dp)
      .testTag(tag)
  ) {
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
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = TextTertiary,
            letterSpacing = 0.5.sp
          )
          Text(
            text = subDetail,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1
          )
        }
      }

      Text(
        text = primaryValue,
        fontSize = 15.sp,
        fontWeight = FontWeight.Black,
        color = accentColor,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
private fun CircuitRecordsList(
  trackRecords: Map<String, List<PerformanceMetrics>>,
  isMph: Boolean
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("circuit_records_list"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (trackRecords.isEmpty()) {
        Text(
          text = "No track sessions logged in database.",
          fontSize = 11.sp,
          color = TextTertiary,
          modifier = Modifier.padding(8.dp)
        )
      } else {
        trackRecords.forEach { (trackName, sessions) ->
          val bestLap = sessions.minByOrNull { it.lapTimeMs }
          val totalDistM = sessions.sumOf { it.getEffectiveDistanceMeters().toLong() }
          val distFormatted = if (isMph) {
            String.format(Locale.US, "%.1f mi", (totalDistM / 1000f) * 0.621371f)
          } else {
            String.format(Locale.US, "%.1f km", totalDistM / 1000f)
          }
          val maxSpeed = sessions.maxOfOrNull { it.topSpeedKmh } ?: 0f
          val speedFormatted = if (isMph) {
            String.format(Locale.US, "%.0f mph", maxSpeed * 0.621371f)
          } else {
            String.format(Locale.US, "%.0f km/h", maxSpeed)
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(CarbonSurfaceVariant)
              .border(0.8.dp, CarbonBorder, RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = trackName,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Black,
                  color = TextPrimary
                )
                Text(
                  text = "${sessions.size} sessions • $distFormatted driven • Top $speedFormatted",
                  fontSize = 9.5.sp,
                  color = TextSecondary
                )
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "BEST LAP",
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextTertiary
                )
                Text(
                  text = bestLap?.let { formatLapTime(it.lapTimeMs) } ?: "--:--.---",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Black,
                  color = NeonCyan,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun VehicleUtilizationCard(
  vehicleRecords: Map<String, List<PerformanceMetrics>>,
  isMph: Boolean
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("vehicle_utilization_card"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (vehicleRecords.isEmpty()) {
        Text(
          text = "No vehicle data available.",
          fontSize = 11.sp,
          color = TextTertiary,
          modifier = Modifier.padding(8.dp)
        )
      } else {
        vehicleRecords.forEach { (vehicleName, sessions) ->
          val totalDistM = sessions.sumOf { it.getEffectiveDistanceMeters().toLong() }
          val distFormatted = if (isMph) {
            String.format(Locale.US, "%.1f mi", (totalDistM / 1000f) * 0.621371f)
          } else {
            String.format(Locale.US, "%.1f km", totalDistM / 1000f)
          }
          val bestLap = sessions.minByOrNull { it.lapTimeMs }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(CarbonSurfaceVariant)
              .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = NeonAmber,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = vehicleName,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "${sessions.size} runs • Best ${bestLap?.let { formatLapTime(it.lapTimeMs) } ?: "--"}",
                  fontSize = 9.sp,
                  color = TextSecondary
                )
              }
            }

            Text(
              text = distFormatted,
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = NeonAmber,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
private fun InteractiveDatabaseActionsCard(
  onSimulateNewLap: () -> Unit,
  onViewAllEntries: () -> Unit,
  onResetDatabase: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("database_actions_card"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = "ROOM DATABASE CONTROLS",
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = TextTertiary,
        letterSpacing = 0.5.sp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Simulate & Save Run
        Button(
          onClick = onSimulateNewLap,
          colors = ButtonDefaults.buttonColors(containerColor = ApexGreen),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("profile_simulate_lap_button")
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = CarbonBlack,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "SIMULATE & LOG LAP",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CarbonBlack
          )
        }

        // View All Database Records
        OutlinedButton(
          onClick = onViewAllEntries,
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NeonCyan,
            containerColor = CarbonSurfaceVariant
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("profile_view_db_entries_button")
        ) {
          Icon(
            imageVector = Icons.Default.Assessment,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "VIEW RAW DB",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
        }
      }

      TextButton(
        onClick = onResetDatabase,
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .testTag("profile_clear_db_button")
      ) {
        Text(
          text = "Reset Profile Telemetry Database",
          fontSize = 10.sp,
          color = TextTertiary
        )
      }
    }
  }
}

// -------------------------------------------------------------
// HELPER FORMATTING
// -------------------------------------------------------------

private fun formatLapTime(timeMs: Long): String {
  if (timeMs <= 0L) return "--:--.---"
  val minutes = (timeMs / 60000).toInt()
  val seconds = ((timeMs % 60000) / 1000).toInt()
  val millis = (timeMs % 1000).toInt()
  return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis)
}

private fun formatDuration(durationMs: Long): String {
  if (durationMs <= 0L) return "0s"
  val hours = durationMs / 3600000
  val minutes = (durationMs % 3600000) / 60000
  val seconds = (durationMs % 60000) / 1000
  return if (hours > 0) {
    String.format(Locale.US, "%dh %dm %ds", hours, minutes, seconds)
  } else if (minutes > 0) {
    String.format(Locale.US, "%dm %ds", minutes, seconds)
  } else {
    String.format(Locale.US, "%ds", seconds)
  }
}

// -------------------------------------------------------------
// FIREBASE AUTH & FIRESTORE CLOUD DATA PERSISTENCE CARD
// -------------------------------------------------------------
@Composable
private fun FirebaseAuthFirestoreCard(
  profile: DriverProfile?,
  isGoogleSigningIn: Boolean,
  isFirestoreSyncing: Boolean,
  syncStatusMessage: String?,
  onGoogleSignIn: () -> Unit,
  onSyncToFirestore: () -> Unit,
  onSignOut: () -> Unit
) {
  val isGoogle = profile?.isGoogleSignedIn == true

  Card(
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isGoogle) ApexGreen.copy(alpha = 0.8f) else NeonCyan.copy(alpha = 0.5f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("firebase_auth_firestore_card")
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(if (isGoogle) ApexGreen.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isGoogle) Icons.Default.Verified else Icons.Default.AccountCircle,
              contentDescription = null,
              tint = if (isGoogle) ApexGreen else NeonCyan,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (isGoogle) "Firebase Auth: Google Account Connected" else "Firebase Auth: Guest Mode",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = if (isGoogle) (profile?.email ?: "Google Authenticated") else "Sign in with Google to sync telemetry",
              fontSize = 9.5.sp,
              color = TextSecondary
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isGoogle) ApexGreen.copy(alpha = 0.15f) else CarbonSurfaceVariant)
            .border(1.dp, if (isGoogle) ApexGreen.copy(alpha = 0.4f) else CarbonBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Text(
            text = if (isGoogle) "FIRESTORE SYNCED" else "OFFLINE LOCAL",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Black,
            color = if (isGoogle) ApexGreen else TextSecondary
          )
        }
      }

      // Firestore Database Persistence Details
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CarbonBlack)
          .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text("Firestore Cloud Collection", fontSize = 9.sp, color = TextSecondary)
            }
            Text("users/${profile?.uid ?: "guest"}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = NeonAmber)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ApexGreen,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text("Data Persistence", fontSize = 9.sp, color = TextSecondary)
            }
            Text("Active (Profile & Laps)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexGreen)
          }
        }
      }

      if (!syncStatusMessage.isNullOrBlank()) {
        Text(
          text = syncStatusMessage,
          fontSize = 9.sp,
          color = ApexGreen,
          fontFamily = FontFamily.Monospace
        )
      }

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (!isGoogle) {
          Button(
            onClick = onGoogleSignIn,
            enabled = !isGoogleSigningIn,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CarbonBlack),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("google_sign_in_button")
          ) {
            if (isGoogleSigningIn) {
              CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CarbonBlack, strokeWidth = 2.dp)
            } else {
              Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sign In with Google", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
          }
        } else {
          Button(
            onClick = onSyncToFirestore,
            enabled = !isFirestoreSyncing,
            colors = ButtonDefaults.buttonColors(containerColor = ApexGreen, contentColor = CarbonBlack),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("sync_to_firestore_button")
          ) {
            if (isFirestoreSyncing) {
              CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CarbonBlack, strokeWidth = 2.dp)
            } else {
              Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Sync Telemetry to Cloud", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
          }

          OutlinedButton(
            onClick = onSignOut,
            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("google_sign_out_button")
          ) {
            Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Sign Out", fontSize = 10.sp)
          }
        }
      }
    }
  }
}

