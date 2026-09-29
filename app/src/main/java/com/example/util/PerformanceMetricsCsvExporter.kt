package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.PerformanceMetrics
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PerformanceMetricsCsvExporter {

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

  /**
   * Generates a standard RFC 4180 compliant CSV string from the list of performance records.
   */
  fun generateCsvString(metrics: List<PerformanceMetrics>): String {
    val sb = StringBuilder()
    // CSV Header row
    sb.appendLine("Session ID,Driver Name,Track Name,Vehicle Name,Race Type,Track Complexity,Weather Condition,Tags,Lap Time (ms),Lap Time (formatted),Top Speed (km/h),0-100 km/h (s),Peak Accel (G),Max Lateral (G),Avg Speed (km/h),Date Recorded,Timestamp")

    // CSV Data rows
    metrics.forEach { m ->
      val escapedDriver = escapeCsv(m.driverName)
      val escapedTrack = escapeCsv(m.trackName)
      val escapedVehicle = escapeCsv(m.vehicleName)
      val escapedRaceType = escapeCsv(m.raceType)
      val escapedComplexity = escapeCsv(m.trackComplexity)
      val escapedWeather = escapeCsv(m.weatherCondition)
      val escapedTags = escapeCsv(m.getAllTags().joinToString("; "))
      val formattedLap = formatLapTimeStr(m.lapTimeMs)
      val dateFormatted = dateFormat.format(Date(m.timestamp))

      sb.appendLine(
        "${m.id}," +
        "$escapedDriver," +
        "$escapedTrack," +
        "$escapedVehicle," +
        "$escapedRaceType," +
        "$escapedComplexity," +
        "$escapedWeather," +
        "$escapedTags," +
        "${m.lapTimeMs}," +
        "$formattedLap," +
        "${String.format(Locale.US, "%.1f", m.topSpeedKmh)}," +
        "${String.format(Locale.US, "%.2f", m.zeroToHundredKmhSeconds)}," +
        "${String.format(Locale.US, "%.2f", m.peakAccelerationG)}," +
        "${String.format(Locale.US, "%.2f", m.maxLateralG)}," +
        "${String.format(Locale.US, "%.1f", m.getComputedAvgSpeed())}," +
        "\"$dateFormatted\"," +
        "${m.timestamp}"
      )
    }

    return sb.toString()
  }

  /**
   * Saves the generated CSV directly to device storage (Downloads or App Documents).
   * Uses Android MediaStore Downloads API on Android 10+ (Q+) without requiring storage permissions.
   */
  fun saveCsvToDeviceStorage(
    context: Context,
    metrics: List<PerformanceMetrics>,
    fileName: String = "apex_rivals_telemetry_${System.currentTimeMillis()}.csv"
  ): File? {
    if (metrics.isEmpty()) {
      Toast.makeText(context, "No performance metrics to export", Toast.LENGTH_SHORT).show()
      return null
    }

    return try {
      val csvContent = generateCsvString(metrics)
      var savedViaMediaStore = false

      // Try saving to public Downloads folder via MediaStore (Android 10+)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
          put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
          put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ApexRivals")
        }
        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
          context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(csvContent.toByteArray(Charsets.UTF_8))
          }
          savedViaMediaStore = true
          Toast.makeText(context, "Exported: Downloads/ApexRivals/$fileName", Toast.LENGTH_LONG).show()
        }
      }

      // App storage fallback
      val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
      val targetFile = File(targetDir, fileName)
      targetFile.writeText(csvContent, Charsets.UTF_8)

      if (!savedViaMediaStore) {
        Toast.makeText(context, "Exported CSV to: ${targetFile.name}", Toast.LENGTH_LONG).show()
      }
      targetFile
    } catch (e: Exception) {
      Toast.makeText(context, "Failed to save CSV: ${e.message}", Toast.LENGTH_SHORT).show()
      null
    }
  }

  /**
   * Writes the CSV file to the application cache and launches Android's system share sheet
   * with the .csv file attached for external analysis (Excel, Google Sheets, Python, etc.).
   */
  fun exportAndShareCsv(
    context: Context,
    metrics: List<PerformanceMetrics>,
    fileName: String = "apex_rivals_telemetry_${System.currentTimeMillis()}.csv"
  ) {
    if (metrics.isEmpty()) {
      Toast.makeText(context, "No performance metrics to export", Toast.LENGTH_SHORT).show()
      return
    }

    try {
      val csvContent = generateCsvString(metrics)
      val exportDir = File(context.cacheDir, "exports")
      if (!exportDir.exists()) exportDir.mkdirs()

      val csvFile = File(exportDir, fileName)
      csvFile.writeText(csvContent, Charsets.UTF_8)

      val authority = "${context.packageName}.fileprovider"
      val contentUri = FileProvider.getUriForFile(context, authority, csvFile)

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_SUBJECT, "🏎️ Apex Rivals Telemetry Export (${metrics.size} sessions)")
        putExtra(
          Intent.EXTRA_TEXT,
          "Attached is the Apex Rivals telemetry export containing ${metrics.size} race session records in CSV format."
        )
        putExtra(Intent.EXTRA_STREAM, contentUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, "Export Telemetry CSV").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

      context.startActivity(chooser)
    } catch (e: Exception) {
      // Fallback: copy to clipboard if file sharing fails
      copyCsvToClipboard(context, metrics, "File share error. Copied CSV data to clipboard instead.")
    }
  }

  /**
   * Copies the full CSV dataset to the device clipboard.
   */
  fun copyCsvToClipboard(
    context: Context,
    metrics: List<PerformanceMetrics>,
    toastMsg: String = "CSV telemetry copied to clipboard!"
  ) {
    try {
      val csvContent = generateCsvString(metrics)
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText("Apex Rivals Telemetry CSV", csvContent)
      clipboard.setPrimaryClip(clip)
      Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Toast.makeText(context, "Failed to copy CSV: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  private fun escapeCsv(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      "\"" + value.replace("\"", "\"\"") + "\""
    } else {
      value
    }
  }

  private fun formatLapTimeStr(ms: Long): String {
    val minutes = (ms / 60000)
    val seconds = (ms % 60000) / 1000
    val millis = ms % 1000
    return String.format(Locale.US, "%d:%02d.%03d", minutes, seconds, millis)
  }
}
