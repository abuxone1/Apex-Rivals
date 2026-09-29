package com.example.ui.screens.ai

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RaceUiState
import com.example.ui.viewmodel.RaceViewModel

@Composable
fun AiLiveRadioSection(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier
) {
  var driverSpeechInput by remember { mutableStateOf("") }

  val radioQuickCommands = listOf(
    "Radio check, how do you read?",
    "Box this lap or stay out?",
    "What is my lap delta to leader?",
    "Report front-right tire temperature",
    "Switching engine map to overtake mode"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "radio_wave")
  val waveHeight by infiniteTransition.animateFloat(
    initialValue = 8f,
    targetValue = 28f,
    animationSpec = infiniteRepeatable(
      animation = tween(400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "wave_h"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(4.dp)
  ) {
    // Radio Channel Status Card
    Box(
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
              .background(if (uiState.isLiveVoiceActive) NeonAmber.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Radio,
              contentDescription = null,
              tint = if (uiState.isLiveVoiceActive) NeonAmber else NeonCyan,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "LIVE PIT-WALL RADIO (gemini-3.8-live)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
            Text(
              text = if (uiState.isLiveVoiceActive) "LIVE AUDIO RECEIVING..." else "LIVE COMMS LINK READY",
              fontSize = 9.sp,
              color = if (uiState.isLiveVoiceActive) NeonAmber else ApexGreen,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Live Audio Frequency Visualizer
        Row(
          horizontalArrangement = Arrangement.spacedBy(3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(5) { i ->
            val h = if (uiState.isLiveVoiceActive) (waveHeight * (1f - (i * 0.15f))).coerceAtLeast(4f) else 6f
            Box(
              modifier = Modifier
                .width(4.dp)
                .height(h.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (uiState.isLiveVoiceActive) NeonCyan else TextSecondary)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Radio Conversation Stream
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (uiState.liveVoiceMessages.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CarbonSurfaceVariant)
              .padding(14.dp)
          ) {
            Column {
              Text(
                text = "Two-Way Spoken Race Comms",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Use model gemini-3.8-live for real-time live conversations with your chief race engineer. Transmit quick pit radio calls below or type your custom radio message.",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 15.sp
              )
            }
          }
        }
      }

      items(uiState.liveVoiceMessages) { msg ->
        val isDriver = msg.sender.startsWith("Driver")
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = if (isDriver) Alignment.End else Alignment.Start
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isDriver) CarbonSurfaceVariant else NeonCyan.copy(alpha = 0.15f))
              .border(1.dp, if (isDriver) CarbonBorder else NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .padding(10.dp)
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (isDriver) Icons.Default.Mic else Icons.AutoMirrored.Filled.VolumeUp,
                  contentDescription = null,
                  tint = if (isDriver) NeonAmber else NeonCyan,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = msg.sender,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isDriver) NeonAmber else NeonCyan,
                  fontFamily = FontFamily.Monospace
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = msg.text,
                fontSize = 11.sp,
                color = TextPrimary,
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      if (uiState.isLiveVoiceActive) {
        item {
          Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = NeonCyan, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Receiving pit transmission from gemini-3.8-live...",
              fontSize = 10.sp,
              color = TextSecondary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Quick Radio Buttons
    Text(
      text = "QUICK PIT-WALL RADIO TRANSMISSIONS",
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )
    Spacer(modifier = Modifier.height(4.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(radioQuickCommands) { cmd ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurface)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { viewModel.sendLiveVoiceComms(cmd) }
            .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Text(text = cmd, fontSize = 9.sp, color = NeonAmber, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Custom Radio Transmission Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(CarbonSurface)
        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
        .padding(6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = driverSpeechInput,
        onValueChange = { driverSpeechInput = it },
        placeholder = { Text("Speak to Race Engineer...", fontSize = 11.sp, color = TextSecondary) },
        modifier = Modifier.weight(1f),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
          unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        singleLine = true
      )

      Button(
        onClick = {
          if (driverSpeechInput.isNotBlank()) {
            val text = driverSpeechInput
            driverSpeechInput = ""
            viewModel.sendLiveVoiceComms(text)
          }
        },
        enabled = driverSpeechInput.isNotBlank() && !uiState.isLiveVoiceActive,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CarbonBlack)
      ) {
        Text("Transmit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
