package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AVAILABLE_VEHICLES
import com.example.model.PlatformType
import com.example.model.SocialTelemetryPost
import com.example.model.Vehicle
import com.example.model.VehicleType
import com.example.ui.components.EsportsShareCard
import com.example.ui.components.MultiplayerPlatformBadge
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RaceUiState
import com.example.ui.viewmodel.RaceViewModel
import java.util.Locale

@Composable
fun GarageSocialScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier,
  onNavigateToProfile: () -> Unit = {}
) {
  val context = LocalContext.current
  val activeVehicle = uiState.activeVehicle
  val isCar = activeVehicle.type == VehicleType.CAR

  var selectedTab by remember { mutableStateOf("Garage") } // "Garage" or "Social"

  var downforceLevel by remember { mutableFloatStateOf(65f) }
  var brakeBias by remember { mutableFloatStateOf(54f) }
  var selectedTireCompound by remember { mutableStateOf("Soft Slick") }
  var showPrivacyDialog by remember { mutableStateOf(false) }

  val privacyPolicyUrl = "https://ais-pre-rabvswlrajngzqphimheco-261183070241.europe-west2.run.app/privacy-policy.html"

  if (showPrivacyDialog) {
    PrivacyPolicyDialog(
      onDismiss = { showPrivacyDialog = false },
      onOpenWeb = {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
      }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(6.dp))
      // Sub-Navigation Tabs: Garage & Tuning vs Social Telemetry Feed
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(CarbonSurface)
          .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
          .padding(4.dp)
      ) {
        listOf("Garage", "Social Feed").forEach { tab ->
          val isSel = tab == selectedTab
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSel) NeonCyan else CarbonSurface)
              .clickable { selectedTab = tab }
              .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = tab.uppercase(),
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = if (isSel) CarbonBlack else TextSecondary
            )
          }
        }
      }
    }

    if (selectedTab == "Garage") {
      item {
        // Firebase Auth & Driver Profile Card
        DriverProfileCard(
          profile = uiState.driverProfile,
          onSignIn = { viewModel.signInDriver() },
          onSignOut = { viewModel.signOutDriver() },
          onNavigateToProfile = onNavigateToProfile
        )
      }

      item {
        // Vehicle Switcher & Specs
        GarageVehicleCard(
          vehicles = AVAILABLE_VEHICLES,
          activeVehicle = activeVehicle,
          onSelectVehicle = { viewModel.selectVehicle(it) }
        )
      }

      item {
        // Gemini AI Livery & Helmet Studio (gemini-3.1-flash-image-preview)
        AiLiveryStudioCard(
          activeVehicle = activeVehicle,
          generatedLiveries = uiState.generatedLiveries,
          isGenerating = uiState.isGeneratingLivery,
          onGenerate = { prompt -> viewModel.generateLiveryConcept(prompt) }
        )
      }

      item {
        // Telemetry Setup Tuning
        TuningSetupCard(
          isCar = isCar,
          downforce = downforceLevel,
          onDownforceChange = { downforceLevel = it },
          brakeBias = brakeBias,
          onBrakeBiasChange = { brakeBias = it },
          selectedTire = selectedTireCompound,
          onSelectTire = { selectedTireCompound = it }
        )
      }

      item {
        // Esports Share Card Preview
        Text(
          text = "YOUR ESPORTS TELEMETRY CARD",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
      }

      item {
        EsportsShareCard(
          playerName = "Apex_GhostRacer",
          platform = PlatformType.ANDROID,
          vehicleName = activeVehicle.name,
          vehicleType = activeVehicle.type,
          trackName = uiState.activeTrack.name,
          lapTimeMs = uiState.liveMetrics.bestLapTimeMs ?: uiState.activeTrack.referenceLapTimeMs,
          topSpeedKmh = uiState.liveMetrics.topSpeedKmh.coerceAtLeast(activeVehicle.topSpeedKmh * 0.9f),
          peakG = uiState.liveMetrics.peakLateralG.coerceAtLeast(1.8f),
          challengeCode = "APX-${activeVehicle.id.take(3).uppercase()}-2026",
          onShareClick = {
            viewModel.shareTelemetryCard(
              context = context,
              lapTimeMs = uiState.liveMetrics.bestLapTimeMs ?: uiState.activeTrack.referenceLapTimeMs,
              topSpeedKmh = activeVehicle.topSpeedKmh.toFloat(),
              peakG = 2.4f,
              vehicleName = activeVehicle.name,
              trackName = uiState.activeTrack.name
            )
          }
        )
      }
    } else {
      // Social Feed Tab
      item {
        Text(
          text = "COMMUNITY TELEMETRY & LAP RECORD FEED",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 1.sp
        )
      }

      items(uiState.socialPosts) { post ->
        SocialPostCard(
          post = post,
          onLike = { viewModel.toggleLikePost(post.id) },
          onShare = {
            viewModel.shareTelemetryCard(
              context = context,
              lapTimeMs = post.lapTimeMs,
              topSpeedKmh = post.topSpeedKmh,
              peakG = 2.5f,
              vehicleName = post.vehicleName,
              trackName = post.trackName
            )
          }
        )
      }
    }

    item {
      PrivacyPolicyCard(
        onViewPolicy = { showPrivacyDialog = true },
        onOpenWeb = {
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
          }
          context.startActivity(intent)
        }
      )
    }

    item {
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun GarageVehicleCard(
  vehicles: List<Vehicle>,
  activeVehicle: Vehicle,
  onSelectVehicle: (Vehicle) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(14.dp)
  ) {
    Text(
      text = "SELECT VEHICLE IN GARAGE",
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(vehicles) { v ->
        val isSel = v.id == activeVehicle.id
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSel) NeonCyan.copy(alpha = 0.2f) else CarbonSurfaceVariant)
            .border(1.dp, if (isSel) NeonCyan else CarbonBorder, RoundedCornerShape(10.dp))
            .clickable { onSelectVehicle(v) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (v.type == VehicleType.CAR) Icons.Default.DirectionsCar else Icons.Default.DirectionsBike,
              contentDescription = null,
              tint = if (isSel) NeonCyan else TextSecondary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = v.name,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSel) TextPrimary else TextSecondary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Vehicle Specifications Grid
    Text(
      text = activeVehicle.name,
      fontSize = 18.sp,
      fontWeight = FontWeight.Black,
      color = TextPrimary
    )
    Text(
      text = if (activeVehicle.type == VehicleType.CAR) "FIA GT3 / Prototype Spec" else "FIM Superbike World Championship Spec",
      fontSize = 11.sp,
      color = NeonCyan
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      SpecBox(label = "HORSEPOWER", value = "${activeVehicle.horsepower} HP")
      SpecBox(label = "0-100 KM/H", value = "${activeVehicle.acceleration0to100}s")
      SpecBox(label = "TOP SPEED", value = "${activeVehicle.topSpeedKmh} km/h")
      SpecBox(label = "REDLINE", value = "${activeVehicle.redlineRpm} RPM")
    }

    if (activeVehicle.type == VehicleType.MOTORBIKE) {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SpecBox(label = "MAX LEAN ANGLE", value = "${activeVehicle.maxLeanAngleDeg.toInt()}°")
        SpecBox(label = "DRY WEIGHT", value = "${activeVehicle.weightKg} kg")
        SpecBox(label = "GEARBOX", value = "${activeVehicle.gears}-Speed Quickshifter")
        SpecBox(label = "AERO WING", value = "Carbon Biplane")
      }
    }
  }
}

@Composable
fun SpecBox(label: String, value: String) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(CarbonSurfaceVariant)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(text = label, fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
    Text(
      text = value,
      fontSize = 11.sp,
      fontWeight = FontWeight.Black,
      color = TextPrimary,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun TuningSetupCard(
  isCar: Boolean,
  downforce: Float,
  onDownforceChange: (Float) -> Unit,
  brakeBias: Float,
  onBrakeBiasChange: (Float) -> Unit,
  selectedTire: String,
  onSelectTire: (String) -> Unit
) {
  val tireCompounds = listOf("Soft Slick", "Medium Slick", "Hard Slick", "Wet Inter")

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(16.dp))
      .padding(14.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "RACE TELEMETRY SETUP & TUNING",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = NeonAmber,
        letterSpacing = 1.sp
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Tire Compound Selector
    Text(text = "TIRE COMPOUND", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      tireCompounds.forEach { comp ->
        val isSel = comp == selectedTire
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSel) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant)
            .border(1.dp, if (isSel) NeonAmber else CarbonBorder, RoundedCornerShape(6.dp))
            .clickable { onSelectTire(comp) }
            .padding(vertical = 6.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = comp,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSel) NeonAmber else TextSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Aero Wing / Downforce Slider
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = if (isCar) "AERODYNAMIC DOWNFORCE (WING)" else "FRONT AERO CANARDS",
        fontSize = 10.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "${downforce.toInt()}%",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = NeonCyan,
        fontFamily = FontFamily.Monospace
      )
    }
    Slider(
      value = downforce,
      onValueChange = onDownforceChange,
      valueRange = 0f..100f,
      colors = SliderDefaults.colors(
        thumbColor = NeonCyan,
        activeTrackColor = NeonCyan,
        inactiveTrackColor = CarbonSurfaceVariant
      )
    )

    // Brake Bias Slider
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "BRAKE BIAS (FRONT / REAR)",
        fontSize = 10.sp,
        color = TextSecondary,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "${brakeBias.toInt()}% FRONT",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = RedlineRed,
        fontFamily = FontFamily.Monospace
      )
    }
    Slider(
      value = brakeBias,
      onValueChange = onBrakeBiasChange,
      valueRange = 45f..65f,
      colors = SliderDefaults.colors(
        thumbColor = RedlineRed,
        activeTrackColor = RedlineRed,
        inactiveTrackColor = CarbonSurfaceVariant
      )
    )
  }
}

@Composable
fun SocialPostCard(
  post: SocialTelemetryPost,
  onLike: () -> Unit,
  onShare: () -> Unit
) {
  val minutes = (post.lapTimeMs / 60000)
  val seconds = (post.lapTimeMs % 60000) / 1000
  val millis = (post.lapTimeMs % 1000)
  val formattedTime = String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
      .padding(14.dp)
      .testTag("social_post_${post.id}")
  ) {
    // Post Header
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
            .background(CarbonSurfaceVariant),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (post.vehicleType == VehicleType.CAR) Icons.Default.DirectionsCar else Icons.Default.DirectionsBike,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(text = post.authorName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Text(text = post.timeAgo, fontSize = 10.sp, color = TextSecondary)
        }
      }

      MultiplayerPlatformBadge(platform = post.platform)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Caption / Description
    Text(
      text = post.caption,
      fontSize = 12.sp,
      color = TextPrimary
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Telemetry Lap Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(CarbonBlack)
        .padding(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(text = "${post.trackName} • ${post.vehicleName}", fontSize = 10.sp, color = TextSecondary)
          Text(
            text = formattedTime,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = ApexGreen,
            fontFamily = FontFamily.Monospace
          )
        }

        Text(
          text = "${post.topSpeedKmh.toInt()} km/h",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = NeonAmber,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Footer: Like & Share Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onLike() }
      ) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "Like",
          tint = RedlineRed,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "${post.likesCount} Kudos", fontSize = 11.sp, color = TextSecondary)
      }

      IconButton(
        onClick = onShare,
        modifier = Modifier.size(28.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Share,
          contentDescription = "Share",
          tint = NeonCyan,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun DriverProfileCard(
  profile: com.example.data.firebase.DriverProfile?,
  onSignIn: () -> Unit,
  onSignOut: () -> Unit,
  onNavigateToProfile: () -> Unit = {}
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
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(NeonCyan.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = profile?.displayName ?: "Apex Driver (Guest)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = if (profile?.isAnonymous == false) "Google Firebase Auth" else "Firebase Guest License",
            fontSize = 10.sp,
            color = TextSecondary
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CloudDone,
          contentDescription = "Cloud Synced",
          tint = ApexGreen,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Firestore Synced",
          fontSize = 9.sp,
          color = ApexGreen,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(text = "LICENSE GRADE", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(text = profile?.licenseGrade ?: "FIA Superlicense A", fontSize = 11.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
      }
      Column {
        Text(text = "SAFETY RATING", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(text = "${profile?.reputationScore ?: 1950} PTS", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
      Column {
        Text(text = "LAPS COMPLETED", fontSize = 8.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Text(text = "${profile?.totalLapsCompleted ?: 42}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    OutlinedButton(
      onClick = onNavigateToProfile,
      colors = ButtonDefaults.outlinedButtonColors(
        contentColor = NeonCyan,
        containerColor = CarbonSurfaceVariant
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("garage_view_profile_button")
    ) {
      Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = null,
        tint = NeonCyan,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = "VIEW CAREER STATS & PERSONAL RECORDS (ROOM DB)",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = NeonCyan
      )
    }
  }
}

@Composable
fun AiLiveryStudioCard(
  activeVehicle: Vehicle,
  generatedLiveries: List<com.example.data.gemini.GeneratedLiveryResult>,
  isGenerating: Boolean,
  onGenerate: (String) -> Unit
) {
  var liveryPrompt by remember { mutableStateOf("") }

  val liveryPresets = listOf(
    "Cyberpunk Tokyo Neon with cyan lightning decals",
    "Matte Carbon Stealth with gold aerodynamic winglets",
    "Vintage Gulf Racing Powder Blue and Tangerine Orange",
    "MotoGP Italian Red with aggressive shark aero fins"
  )

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
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.ColorLens,
          contentDescription = null,
          tint = NeonAmber,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "AI LIVERY & HELMET STUDIO",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          letterSpacing = 1.sp
        )
      }

      Text(
        text = "gemini-3.1-flash-image",
        fontSize = 8.sp,
        color = NeonAmber,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold
      )
    }

    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = "Design bespoke aerodynamics, vinyl decals, and helmet paint using text prompts with Gemini Flash Image.",
      fontSize = 10.sp,
      color = TextSecondary
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Preset chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(liveryPresets) { preset ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { liveryPrompt = preset }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(text = preset, fontSize = 9.sp, color = TextPrimary)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Prompt input and generate button
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = liveryPrompt,
        onValueChange = { liveryPrompt = it },
        placeholder = { Text("Describe your custom livery or helmet...", fontSize = 11.sp, color = TextSecondary) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          focusedBorderColor = NeonAmber,
          unfocusedBorderColor = CarbonBorder
        ),
        modifier = Modifier
          .weight(1f)
          .testTag("livery_prompt_input"),
        singleLine = true
      )

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = {
          if (liveryPrompt.isNotBlank()) {
            onGenerate(liveryPrompt)
          }
        },
        enabled = liveryPrompt.isNotBlank() && !isGenerating,
        colors = ButtonDefaults.buttonColors(
          containerColor = NeonAmber,
          contentColor = CarbonBlack
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("generate_livery_button")
      ) {
        if (isGenerating) {
          CircularProgressIndicator(color = CarbonBlack, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
        } else {
          Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
        }
      }
    }

    // Display generated livery concept if any
    if (generatedLiveries.isNotEmpty()) {
      Spacer(modifier = Modifier.height(10.dp))
      val latest = generatedLiveries.first()
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(CarbonSurfaceVariant)
          .border(1.dp, NeonAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
          .padding(10.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "LIVERY CONCEPT: ${latest.prompt.take(30)}...",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = NeonAmber
            )
            Text(
              text = if (latest.isSimulated) "STUDIO SPEC" else "RENDER READY",
              fontSize = 8.sp,
              color = ApexGreen,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(text = latest.description, fontSize = 10.sp, color = TextPrimary)
        }
      }
    }
  }
}

@Composable
fun PrivacyPolicyCard(
  onViewPolicy: () -> Unit,
  onOpenWeb: () -> Unit
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
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = "Privacy Policy",
          tint = NeonCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "LEGAL & PRIVACY POLICY",
          fontSize = 12.sp,
          fontWeight = FontWeight.Black,
          color = TextPrimary,
          letterSpacing = 1.sp
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(ApexGreen.copy(alpha = 0.15f))
          .border(1.dp, ApexGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = "PLAY STORE READY",
          fontSize = 8.sp,
          fontWeight = FontWeight.Bold,
          color = ApexGreen,
          fontFamily = FontFamily.Monospace
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Apex Rivals respects your privacy. Local lap records stay in sandbox storage. Optional cloud sync via Firebase. AI coaching prompts process via Google Gemini API with zero personal data transmission.",
      fontSize = 10.sp,
      color = TextSecondary,
      lineHeight = 14.sp
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedButton(
        onClick = onViewPolicy,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = NeonCyan
        )
      ) {
        Text(text = "Read Policy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      Button(
        onClick = onOpenWeb,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = CarbonSurfaceVariant,
          contentColor = TextPrimary
        )
      ) {
        Icon(
          imageVector = Icons.Default.OpenInBrowser,
          contentDescription = null,
          modifier = Modifier.size(14.dp),
          tint = NeonAmber
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "Web Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun PrivacyPolicyDialog(
  onDismiss: () -> Unit,
  onOpenWeb: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = CarbonSurface,
    titleContentColor = TextPrimary,
    textContentColor = TextSecondary,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Security,
          contentDescription = null,
          tint = NeonCyan,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Apex Rivals Privacy Policy",
          fontSize = 16.sp,
          fontWeight = FontWeight.Black
        )
      }
    },
    text = {
      val scrollState = rememberScrollState()
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(360.dp)
          .verticalScroll(scrollState)
      ) {
        Text(
          text = "Effective Date: September 22, 2026\nApplication: Apex Rivals (com.aistudio.apexrivals.tr8x)\nDeveloper: abux.one@gmail.com",
          fontSize = 11.sp,
          color = NeonAmber,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "1. Data Collection & Minimization\n" +
            "• Local Telemetry: Lap times, speed, G-forces, and split sectors are stored locally in private Room/SQLite databases.\n" +
            "• Driver Profile: Optional Google/Firebase Auth and anonymous credentials to save cloud leaderboards.\n" +
            "• AI Race Engineer: Telemetry statistics and livery prompts are sent to Google Gemini API servers. No personal identities are collected or transmitted.\n" +
            "\n2. Permissions\n" +
            "• INTERNET & ACCESS_NETWORK_STATE: Required for multiplayer lobbies and Gemini AI coaching.\n" +
            "• VIBRATE: Haptic gear-shift and tire lockup feedback.\n" +
            "• Zero sensitive permissions (no GPS location, camera, microphone, or file system access).\n" +
            "\n3. Data Retention & Deletion\n" +
            "• Users may clear local data anytime via device settings.\n" +
            "• Cloud data deletion requests can be sent to abux.one@gmail.com.\n" +
            "\n4. Children's Privacy (COPPA)\n" +
            "• The app complies with COPPA and Google Play Families guidelines, with no targeted tracking of children under 13.",
          fontSize = 11.sp,
          color = TextPrimary,
          lineHeight = 16.sp
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onOpenWeb,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CarbonBlack),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Open Full Web Policy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = TextSecondary, fontSize = 11.sp)
      }
    }
  )
}


