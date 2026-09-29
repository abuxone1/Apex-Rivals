package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.model.PlatformType
import com.example.model.VehicleType
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.RaceLapShareHelper
import com.example.util.ShareFormatPreset

@Composable
fun ShareLapTimeDialog(
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
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var selectedPreset by remember { mutableStateOf(ShareFormatPreset.SOCIAL_POST) }
  var customNote by remember { mutableStateOf("") }

  val previewText = remember(selectedPreset, customNote, lapTimeMs, trackName, vehicleName) {
    RaceLapShareHelper.buildShareMessage(
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
      customMessage = customNote,
      preset = selectedPreset
    )
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = CarbonSurface,
      border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .testTag("share_lap_time_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Dialog Header
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
                .background(NeonCyan.copy(alpha = 0.15f))
                .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share Icon",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SHARE RECORDED LAP",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Post to social feeds or community chats",
                fontSize = 10.sp,
                color = TextSecondary
              )
            }
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(28.dp).testTag("close_share_dialog")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextSecondary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Lap Highlight Summary Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (vehicleType == VehicleType.CAR) Icons.Default.DirectionsCar else Icons.Default.DirectionsBike,
                  contentDescription = null,
                  tint = NeonCyan,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "$trackName • $vehicleName",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextSecondary
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = RaceLapShareHelper.formatLapTime(lapTimeMs),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = ApexGreen,
                fontFamily = FontFamily.Monospace
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(NeonAmber.copy(alpha = 0.15f))
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = "${topSpeedKmh.toInt()} KM/H",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonAmber,
                  fontFamily = FontFamily.Monospace
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = platform.displayName,
                fontSize = 9.sp,
                color = TextSecondary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Format Preset Selector Tabs
        Text(
          text = "CHOOSE SHARE FORMAT",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          ShareFormatPreset.values().forEach { preset ->
            val isSelected = preset == selectedPreset
            val (icon, label) = when (preset) {
              ShareFormatPreset.SOCIAL_POST -> Icons.Default.Public to "Social Feed"
              ShareFormatPreset.COMMUNITY_CHAT -> Icons.Default.Chat to "Discord/Chat"
              ShareFormatPreset.FULL_TELEMETRY -> Icons.Default.QueryStats to "Dossier"
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CarbonSurfaceVariant)
                .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
                .clickable { selectedPreset = preset }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = if (isSelected) NeonCyan else TextSecondary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = label,
                  fontSize = 9.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) NeonCyan else TextSecondary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Optional Custom Note Input
        OutlinedTextField(
          value = customNote,
          onValueChange = { customNote = it },
          label = { Text("Add custom shoutout or challenge (optional)", fontSize = 11.sp) },
          placeholder = { Text("e.g. Broke personal record on soft slicks!", fontSize = 11.sp, color = TextSecondary) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = CarbonBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = CarbonBlack,
            unfocusedContainerColor = CarbonBlack
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("share_custom_note_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Preview Container
        Text(
          text = "MESSAGE PREVIEW",
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = TextSecondary,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonBlack)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Text(
            text = previewText,
            fontSize = 9.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace,
            lineHeight = 13.sp
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Share Intent & Copy to Clipboard
        Button(
          onClick = {
            RaceLapShareHelper.shareLapTime(
              context = context,
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
              customMessage = customNote,
              preset = selectedPreset
            )
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonCyan,
            contentColor = CarbonBlack
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("confirm_share_button")
        ) {
          Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Share",
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "SHARE TO SOCIAL MEDIA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
          onClick = {
            RaceLapShareHelper.copyToClipboard(context, previewText)
          },
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = TextPrimary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .testTag("copy_share_text_button")
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            tint = TextSecondary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "COPY TEXT TO CLIPBOARD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
          )
        }
      }
    }
  }
}
