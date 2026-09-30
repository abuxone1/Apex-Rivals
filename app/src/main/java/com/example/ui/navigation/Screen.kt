package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
  object LiveDrive : Screen("live_drive", "Drive HUD", Icons.Default.DirectionsCar)
  object RacingGame : Screen("racing_game", "Play Game", Icons.Default.Speed)
  object AiCoach : Screen("ai_coach", "AI Coach", Icons.Default.AutoAwesome)
  object Replay : Screen("replay", "Replay Engine", Icons.Default.PlayCircleFilled)
  object Leaderboards : Screen("leaderboards", "Leaderboard", Icons.Default.Leaderboard)
  object GlobalRankings : Screen("global_rankings", "Global Live", Icons.Default.Public)
  object PerformanceHistory : Screen("performance_metrics", "Telemetry DB", Icons.Default.Speed)
  object TelemetryAnalyzer : Screen("telemetry_analyzer", "Corner & Delta", Icons.Default.Speed)
  object Multiplayer : Screen("multiplayer", "Multiplayer", Icons.Default.Group)
  object GarageSocial : Screen("garage_social", "Garage & Social", Icons.Default.Tune)
  object Profile : Screen("profile", "Driver Profile", Icons.Default.AccountCircle)
  object GooglePlayPublish : Screen("play_publish", "Play Console", Icons.Default.CloudUpload)
}

val NAV_ITEMS = listOf(
  Screen.LiveDrive,
  Screen.AiCoach,
  Screen.Replay,
  Screen.Leaderboards,
  Screen.Multiplayer,
  Screen.GarageSocial
)
