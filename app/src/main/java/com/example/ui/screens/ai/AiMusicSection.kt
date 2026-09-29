package com.example.ui.screens.ai

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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.gemini.GeneratedMusicTrack
import com.example.data.gemini.LyriaMusicModel
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
fun AiMusicSection(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier
) {
  var musicPrompt by remember { mutableStateOf("") }
  var selectedModel by remember { mutableStateOf(LyriaMusicModel.CLIP) }
  var currentlyPlayingTrackId by remember { mutableStateOf<String?>(null) }

  val presets = listOf(
    "140 BPM High-Octane Synthwave Monza Apex",
    "Tokyo Expressway Midnight Chill Lofi Beats",
    "Podium Victory Anthem with Electric Guitar",
    "Pre-Race Grid Tension with Deep Bass Pulses"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(4.dp)
  ) {
    // Header & Model Selector
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(CarbonSurface)
        .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
        .padding(14.dp)
    ) {
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MusicNote,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "LYRIA 3 RACING SOUNDTRACK STUDIO",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonCyan.copy(alpha = 0.15f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "AI AUDIO",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Model selector buttons: Lyria 3 Clip vs Lyria 3 Pro
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LyriaMusicModel.values().forEach { model ->
            val isSelected = selectedModel == model
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CarbonSurfaceVariant)
                .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(10.dp))
                .clickable { selectedModel = model }
                .padding(vertical = 8.dp, horizontal = 10.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = model.displayName,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) NeonCyan else TextPrimary
                )
                Text(
                  text = if (model == LyriaMusicModel.CLIP) "Short clip up to 30s" else "Full track up to 3m",
                  fontSize = 8.sp,
                  color = TextSecondary
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Preset prompts
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(presets) { preset ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { musicPrompt = preset }
            .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Text(text = preset, fontSize = 9.sp, color = NeonAmber)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Generation Input
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
        value = musicPrompt,
        onValueChange = { musicPrompt = it },
        placeholder = { Text("Describe soundtrack theme, BPM, or mood...", fontSize = 11.sp, color = TextSecondary) },
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
          if (musicPrompt.isNotBlank()) {
            val p = musicPrompt
            viewModel.generateMusicTrack(prompt = p, model = selectedModel)
          }
        },
        enabled = musicPrompt.isNotBlank() && !uiState.isGeneratingMusic,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CarbonBlack)
      ) {
        if (uiState.isGeneratingMusic) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CarbonBlack, strokeWidth = 2.dp)
        } else {
          Text("Generate", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Generated Tracks List
    Text(
      text = "GENERATED RACING SOUNDTRACKS",
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(6.dp))

    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (uiState.generatedMusicTracks.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CarbonSurface)
              .padding(14.dp)
          ) {
            Text(
              text = "No tracks generated yet. Select Lyria 3 Clip (up to 30s) or Lyria 3 Pro (full-length) above to generate your customized racing soundtrack.",
              fontSize = 11.sp,
              color = TextSecondary,
              lineHeight = 16.sp
            )
          }
        }
      }

      items(uiState.generatedMusicTracks) { track ->
        MusicTrackCard(
          track = track,
          isPlaying = currentlyPlayingTrackId == track.id,
          onTogglePlay = {
            currentlyPlayingTrackId = if (currentlyPlayingTrackId == track.id) null else track.id
          }
        )
      }
    }
  }
}

@Composable
fun MusicTrackCard(
  track: GeneratedMusicTrack,
  isPlaying: Boolean,
  onTogglePlay: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CarbonSurface)
      .border(1.dp, if (isPlaying) NeonCyan.copy(alpha = 0.6f) else CarbonBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onTogglePlay,
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isPlaying) NeonCyan else CarbonSurfaceVariant)
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = "Play/Pause",
            tint = if (isPlaying) CarbonBlack else NeonCyan,
            modifier = Modifier.size(18.dp)
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
          Text(text = track.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
          Text(
            text = "${track.modelUsed} • ${track.durationSeconds}s",
            fontSize = 9.sp,
            color = if (isPlaying) ApexGreen else TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(CarbonSurfaceVariant)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = if (track.isSimulated) "AUDIO READY" else "HIGH-RES",
          fontSize = 8.sp,
          color = ApexGreen,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}
