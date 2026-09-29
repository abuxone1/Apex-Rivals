package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.model.PlatformType
import com.example.model.VehicleType
import java.util.Locale

enum class ShareFormatPreset(val displayName: String, val description: String) {
  SOCIAL_POST(
    "Social Post (X / Threads)",
    "Short & punchy with hashtags, ideal for feeds and stories"
  ),
  COMMUNITY_CHAT(
    "Discord / WhatsApp",
    "Clean format with sector splits and challenge code"
  ),
  FULL_TELEMETRY(
    "Detailed Telemetry Dossier",
    "Comprehensive session statistics, peak G-force, and speed splits"
  )
}

object RaceLapShareHelper {

  fun formatLapTime(lapTimeMs: Long): String {
    val minutes = lapTimeMs / 60000
    val seconds = (lapTimeMs % 60000) / 1000
    val millis = lapTimeMs % 1000
    return String.format(Locale.US, "%02d:%02d.%03d", minutes, seconds, millis)
  }

  fun buildShareMessage(
    driverName: String,
    trackName: String,
    vehicleName: String,
    vehicleType: VehicleType,
    lapTimeMs: Long,
    topSpeedKmh: Float,
    sector1Ms: Long? = null,
    sector2Ms: Long? = null,
    sector3Ms: Long? = null,
    peakG: Float = 2.5f,
    platform: PlatformType = PlatformType.ANDROID,
    customMessage: String = "",
    preset: ShareFormatPreset = ShareFormatPreset.SOCIAL_POST,
    challengeCode: String = "APX-${(lapTimeMs % 9000 + 1000)}"
  ): String {
    val formattedTime = formatLapTime(lapTimeMs)
    val vehicleIcon = if (vehicleType == VehicleType.CAR) "🏎️" else "🏍️"

    return when (preset) {
      ShareFormatPreset.SOCIAL_POST -> {
        buildString {
          appendLine("$vehicleIcon NEW LAP RECORD SET IN APEX RIVALS!")
          if (customMessage.isNotBlank()) {
            appendLine("\"$customMessage\"")
          }
          appendLine("👤 Driver: $driverName")
          appendLine("🏁 Circuit: $trackName")
          appendLine("⚙️ Machine: $vehicleName")
          appendLine("⏱️ Lap Time: $formattedTime")
          appendLine("🚀 Top Speed: ${topSpeedKmh.toInt()} km/h")
          appendLine("🎮 Platform: ${platform.displayName}")
          appendLine("🔥 Can you beat my ghost line? Challenge code: $challengeCode")
          appendLine()
          append("#ApexRivals #SimRacing #TrackDay #TimeAttack #Motorsport #FastLap")
        }
      }

      ShareFormatPreset.COMMUNITY_CHAT -> {
        buildString {
          appendLine("**$vehicleIcon APEX RIVALS // OFFICIAL LAP TIME ENTRY**")
          appendLine("```")
          appendLine("Circuit:      $trackName")
          appendLine("Driver:       $driverName")
          appendLine("Vehicle:      $vehicleName")
          appendLine("LAP TIME:     $formattedTime")
          if (sector1Ms != null && sector1Ms > 0) {
            appendLine("Sector 1:     ${String.format(Locale.US, "%.3fs", sector1Ms / 1000f)}")
          }
          if (sector2Ms != null && sector2Ms > 0) {
            appendLine("Sector 2:     ${String.format(Locale.US, "%.3fs", sector2Ms / 1000f)}")
          }
          if (sector3Ms != null && sector3Ms > 0) {
            appendLine("Sector 3:     ${String.format(Locale.US, "%.3fs", sector3Ms / 1000f)}")
          }
          appendLine("Top Speed:    ${topSpeedKmh.toInt()} km/h")
          appendLine("Max Corner G: ${String.format(Locale.US, "%.2f G", peakG)}")
          appendLine("```")
          if (customMessage.isNotBlank()) {
            appendLine("> _${customMessage}_")
          }
          appendLine("Rival Challenge Passcode: **$challengeCode**")
          append("Platform: ${platform.displayName} • Verified Telemetry")
        }
      }

      ShareFormatPreset.FULL_TELEMETRY -> {
        buildString {
          appendLine("═══════════════════════════════════════")
          appendLine("🏆 APEX RIVALS RACING TELEMETRY DOSSIER")
          appendLine("═══════════════════════════════════════")
          appendLine("Driver:       $driverName")
          appendLine("Track:        $trackName")
          appendLine("Vehicle:      $vehicleName (${vehicleType.name})")
          appendLine("Recorded Lap: $formattedTime")
          appendLine("───────────────────────────────────────")
          appendLine("SECTOR SPLITS:")
          appendLine(" • Sector 1: ${sector1Ms?.let { String.format(Locale.US, "%.3fs", it / 1000f) } ?: "N/A"}")
          appendLine(" • Sector 2: ${sector2Ms?.let { String.format(Locale.US, "%.3fs", it / 1000f) } ?: "N/A"}")
          appendLine(" • Sector 3: ${sector3Ms?.let { String.format(Locale.US, "%.3fs", it / 1000f) } ?: "N/A"}")
          appendLine("───────────────────────────────────────")
          appendLine("DYNAMICS:")
          appendLine(" • Top Speed:     ${topSpeedKmh.toInt()} km/h")
          appendLine(" • Peak Lateral:  ${String.format(Locale.US, "%.2f G", peakG)}")
          appendLine(" • Platform:      ${platform.displayName}")
          appendLine(" • Session Code:  $challengeCode")
          if (customMessage.isNotBlank()) {
            appendLine("Note: $customMessage")
          }
          appendLine("═══════════════════════════════════════")
          append("Recorded with Apex Rivals High-Frequency Telemetry Engine")
        }
      }
    }
  }

  fun shareLapTime(
    context: Context,
    driverName: String,
    trackName: String,
    vehicleName: String,
    vehicleType: VehicleType,
    lapTimeMs: Long,
    topSpeedKmh: Float,
    sector1Ms: Long? = null,
    sector2Ms: Long? = null,
    sector3Ms: Long? = null,
    peakG: Float = 2.5f,
    platform: PlatformType = PlatformType.ANDROID,
    customMessage: String = "",
    preset: ShareFormatPreset = ShareFormatPreset.SOCIAL_POST,
    challengeCode: String = "APX-${(lapTimeMs % 9000 + 1000)}"
  ) {
    val shareText = buildShareMessage(
      driverName = driverName,
      trackName = trackName,
      vehicleName = vehicleName,
      vehicleType = vehicleType,
      lapTimeMs = lapTimeMs,
      topSpeedKmh = topSpeedKmh,
      sector1Ms = sector1Ms,
      sector2Ms = sector2Ms,
      sector3Ms = sector3Ms,
      peakG = peakG,
      platform = platform,
      customMessage = customMessage,
      preset = preset,
      challengeCode = challengeCode
    )

    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, shareText)
      putExtra(Intent.EXTRA_SUBJECT, "🏎️ Apex Rivals Lap Record: $trackName (${formatLapTime(lapTimeMs)})")
      type = "text/plain"
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    val chooser = Intent.createChooser(sendIntent, "Share Recorded Lap Time").apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
      context.startActivity(chooser)
    } catch (e: Exception) {
      Toast.makeText(context, "Unable to launch share: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  fun copyToClipboard(context: Context, text: String, toastMessage: String = "Lap time copied to clipboard!") {
    try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText("Apex Rivals Lap Record", text)
      clipboard.setPrimaryClip(clip)
      Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Toast.makeText(context, "Failed to copy: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }
}
