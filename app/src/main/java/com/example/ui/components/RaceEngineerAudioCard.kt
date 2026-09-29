package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.util.RaceSpeechService
import java.util.Locale

/**
 * Race Engineer Radio Comms Card.
 * Manages Text-To-Speech lap time announcements, live speed alerts,
 * alert threshold configuration, and voice test comms.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RaceEngineerAudioCard(
  speechService: RaceSpeechService,
  currentSpeedKmh: Float,
  modifier: Modifier = Modifier
) {
  val state by speechService.state.collectAsStateWithLifecycle()

  // Pulsing animation when voice audio is actively transmitting
  val infiniteTransition = rememberInfiniteTransition(label = "radioPulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(400),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("race_engineer_audio_card"),
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(14.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (state.isSpeaking) NeonCyan else CarbonBorder
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header: Radio Icon, Title, Speaking Indicator & Master Mute Toggle
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
              .background(if (state.isEnabled) NeonCyan.copy(alpha = 0.15f) else CarbonSurfaceVariant)
              .border(
                1.dp,
                if (state.isEnabled) NeonCyan.copy(alpha = 0.6f) else CarbonBorder,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (state.isEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
              contentDescription = if (state.isEnabled) "Radio On" else "Radio Muted",
              tint = if (state.isEnabled) NeonCyan else TextTertiary,
              modifier = Modifier.size(17.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "RACE ENGINEER RADIO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.width(6.dp))

              // On Air / Transmitting Indicator
              if (state.isSpeaking) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(RedlineRed.copy(alpha = pulseAlpha))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "TRANSMITTING",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                  )
                }
              } else if (state.isEnabled) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(ApexGreen.copy(alpha = 0.2f))
                    .border(0.6.dp, ApexGreen, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "TTS ACTIVE",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = ApexGreen,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
            Text(
              text = "Android Text-to-Speech voice telemetry & speed alerts",
              fontSize = 9.5.sp,
              color = TextSecondary
            )
          }
        }

        // Master Enable / Mute Switch
        Switch(
          checked = state.isEnabled,
          onCheckedChange = { speechService.setAudioEnabled(it) },
          colors = SwitchDefaults.colors(
            checkedThumbColor = CarbonBlack,
            checkedTrackColor = NeonCyan,
            uncheckedThumbColor = TextTertiary,
            uncheckedTrackColor = CarbonSurfaceVariant
          ),
          modifier = Modifier.testTag("tts_master_switch")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Live Radio Comms Readout Box (Displays latest spoken race message)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CarbonBlack)
          .border(
            1.dp,
            if (state.isSpeaking) NeonCyan.copy(alpha = 0.8f) else CarbonBorder,
            RoundedCornerShape(8.dp)
          )
          .padding(horizontal = 10.dp, vertical = 8.dp)
          .testTag("tts_transcript_box")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.RecordVoiceOver,
            contentDescription = null,
            tint = if (state.isSpeaking) NeonCyan else NeonAmber,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "COMMS TRANSCRIPT",
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "\"${state.lastSpokenMessage}\"",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = if (state.isEnabled) TextPrimary else TextTertiary,
              fontFamily = FontFamily.Monospace,
              maxLines = 2
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Feature Toggles Row: Lap Announcements & Speed Alerts
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Lap Times Toggle Pill
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (state.lapAnnouncementsEnabled) PurpleDelta.copy(alpha = 0.15f) else CarbonSurfaceVariant)
            .border(
              1.dp,
              if (state.lapAnnouncementsEnabled) PurpleDelta else CarbonBorder,
              RoundedCornerShape(8.dp)
            )
            .clickable { speechService.setLapAnnouncementsEnabled(!state.lapAnnouncementsEnabled) }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("tts_toggle_lap_announcements")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = if (state.lapAnnouncementsEnabled) PurpleDelta else TextTertiary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Lap Times",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.lapAnnouncementsEnabled) PurpleDelta else TextSecondary
              )
            }
            Text(
              text = if (state.lapAnnouncementsEnabled) "ON" else "OFF",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = if (state.lapAnnouncementsEnabled) PurpleDelta else TextTertiary
            )
          }
        }

        // Speed Alerts Toggle Pill
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (state.speedAlertsEnabled) RedlineRed.copy(alpha = 0.15f) else CarbonSurfaceVariant)
            .border(
              1.dp,
              if (state.speedAlertsEnabled) RedlineRed else CarbonBorder,
              RoundedCornerShape(8.dp)
            )
            .clickable { speechService.setSpeedAlertsEnabled(!state.speedAlertsEnabled) }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("tts_toggle_speed_alerts")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = if (state.speedAlertsEnabled) RedlineRed else TextTertiary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Speed Alerts",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.speedAlertsEnabled) RedlineRed else TextSecondary
              )
            }
            Text(
              text = if (state.speedAlertsEnabled) "ON" else "OFF",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = if (state.speedAlertsEnabled) RedlineRed else TextTertiary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Speed Alert Threshold Selector
      AnimatedVisibility(visible = state.speedAlertsEnabled) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SPEED ALERT TRIGGER",
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "Threshold: ${state.speedAlertThresholdKmh.toInt()} km/h",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = RedlineRed,
              fontFamily = FontFamily.Monospace
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Presets: 180, 220, 250, 280, 300, 320 km/h
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            RaceSpeechService.SPEED_ALERT_PRESETS.forEach { preset ->
              val isSelected = (state.speedAlertThresholdKmh == preset)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) RedlineRed else CarbonSurfaceVariant)
                  .border(
                    1.dp,
                    if (isSelected) RedlineRed else CarbonBorder,
                    RoundedCornerShape(6.dp)
                  )
                  .clickable { speechService.setSpeedAlertThreshold(preset) }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
                  .testTag("speed_preset_${preset.toInt()}")
              ) {
                Text(
                  text = "${preset.toInt()} km/h",
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                  color = if (isSelected) Color.White else TextPrimary,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }
      }

      // Interactive Action Buttons: Test Voice & Call Current Speed
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Test Voice Comms Button
        OutlinedButton(
          onClick = { speechService.testRadioVoice() },
          enabled = state.isEnabled,
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = NeonCyan,
            containerColor = CarbonSurfaceVariant
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("tts_test_radio_button")
        ) {
          Icon(
            imageVector = Icons.Default.Hearing,
            contentDescription = "Test Radio",
            tint = NeonCyan,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "RADIO CHECK",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = NeonCyan
          )
        }

        // Call Current Speed Button
        Button(
          onClick = { speechService.announceCurrentSpeed(currentSpeedKmh) },
          enabled = state.isEnabled,
          colors = ButtonDefaults.buttonColors(
            containerColor = NeonAmber,
            contentColor = CarbonBlack
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .weight(1f)
            .height(38.dp)
            .testTag("tts_call_speed_button")
        ) {
          Icon(
            imageVector = Icons.Default.Campaign,
            contentDescription = "Call Speed",
            tint = CarbonBlack,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "CALL SPEED",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = CarbonBlack
          )
        }
      }
    }
  }
}
