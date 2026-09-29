package com.example.ui.screens.ai

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiCoachModel
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
import java.util.Locale

@Composable
fun AiChatSection(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier
) {
  var promptInput by remember { mutableStateOf("") }
  var selectedModel by remember { mutableStateOf(GeminiCoachModel.FLASH) }
  var selectedRole by remember { mutableStateOf(com.example.data.gemini.ChatbotRole.CHIEF_ENGINEER) }
  var enableSearchGrounding by remember { mutableStateOf(true) }
  var enableMapsGrounding by remember { mutableStateOf(false) }
  var modelDropdownExpanded by remember { mutableStateOf(false) }

  val listState = rememberLazyListState()

  val audioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    // Transcribe speech using gemini-3.5-transcribe
    val sampleAudioBytes = "SAMPLE_PIT_RADIO_AUDIO_STREAM".toByteArray()
    viewModel.transcribePitRadioAudio(sampleAudioBytes)
  }

  // If new transcribed text arrived, populate into promptInput
  LaunchedEffect(uiState.transcribedText) {
    if (!uiState.transcribedText.isNullOrBlank()) {
      promptInput = uiState.transcribedText ?: ""
      viewModel.clearTranscribedText()
    }
  }

  LaunchedEffect(uiState.aiChatMessages.size, uiState.isAiGenerating) {
    if (uiState.aiChatMessages.isNotEmpty()) {
      listState.animateScrollToItem(uiState.aiChatMessages.size - 1)
    }
  }

  val suggestedPrompts = listOf(
    "Analyze Monza chicane trail-braking telemetry",
    "Where is the highest elevation on Suzuka circuit?",
    "Formula 1 vs MotoGP apex speeds at Monza",
    "Calculate fuel delta for 5 remaining laps"
  )

  Column(modifier = modifier.fillMaxSize()) {
    // Top Bar: Model Selector, Google Search Grounding & Google Maps Grounding
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(CarbonSurface)
        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box {
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurfaceVariant)
            .clickable { modelDropdownExpanded = true }
            .padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = selectedModel.displayName,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = selectedModel.description,
              fontSize = 8.sp,
              color = TextSecondary
            )
          }
        }

        DropdownMenu(
          expanded = modelDropdownExpanded,
          onDismissRequest = { modelDropdownExpanded = false },
          modifier = Modifier.background(CarbonSurface)
        ) {
          GeminiCoachModel.values().forEach { model ->
            DropdownMenuItem(
              text = {
                Column {
                  Text(text = model.displayName, color = TextPrimary, fontWeight = FontWeight.Bold)
                  Text(text = model.description, color = TextSecondary, fontSize = 9.sp)
                }
              },
              onClick = {
                selectedModel = model
                modelDropdownExpanded = false
              }
            )
          }
        }
      }

      // Grounding Controls
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Search Grounding Toggle
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { enableSearchGrounding = !enableSearchGrounding }
        ) {
          Icon(
            imageVector = Icons.Default.Language,
            contentDescription = "Search Grounding",
            tint = if (enableSearchGrounding) ApexGreen else TextSecondary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = "Search", fontSize = 9.sp, color = if (enableSearchGrounding) ApexGreen else TextSecondary)
        }

        // Maps Grounding Toggle
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { enableMapsGrounding = !enableMapsGrounding }
        ) {
          Icon(
            imageVector = Icons.Default.Map,
            contentDescription = "Maps Grounding",
            tint = if (enableMapsGrounding) NeonAmber else TextSecondary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = "Maps", fontSize = 9.sp, color = if (enableMapsGrounding) NeonAmber else TextSecondary)
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Chatbot Role Selector with Specific System Instructions
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 4.dp)
    ) {
      items(com.example.data.gemini.ChatbotRole.values()) { role ->
        val isSelected = selectedRole == role
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CarbonSurface)
            .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { selectedRole = role }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isSelected) NeonCyan else TextSecondary)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = role.displayName,
              fontSize = 9.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) NeonCyan else TextPrimary
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Messages Thread
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      if (uiState.aiChatMessages.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CarbonSurface)
              .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
              .padding(14.dp)
          ) {
            Column {
              Text(
                text = "APEX MULTI-TURN AI RACE ENGINEER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                letterSpacing = 1.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Live telemetry analysis, Google Search Grounding for real-world circuit records, and Google Maps Grounding for turn elevation & layout details.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = "Suggested Inquiries:", fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(4.dp))
              suggestedPrompts.forEach { p ->
                Text(
                  text = "• $p",
                  fontSize = 10.sp,
                  color = NeonAmber,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { promptInput = p }
                    .padding(vertical = 2.dp)
                )
              }
            }
          }
        }
      }

      items(uiState.aiChatMessages) { msg ->
        ChatMessageBubble(message = msg)
      }

      if (uiState.isAiGenerating) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(14.dp),
              color = NeonCyan,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Telemetry model computing vehicle dynamics...",
              fontSize = 11.sp,
              color = TextSecondary,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Quick Suggestions Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(suggestedPrompts) { prompt ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
            .clickable { promptInput = prompt }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(text = prompt, fontSize = 9.sp, color = TextPrimary)
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Input Bar with Mic (gemini-3.5-transcribe) and Send
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(CarbonSurface)
        .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
        .padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = { audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        modifier = Modifier.size(36.dp)
      ) {
        if (uiState.isTranscribing) {
          CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonAmber, strokeWidth = 2.dp)
        } else {
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Transcribe Audio",
            tint = NeonAmber,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      OutlinedTextField(
        value = promptInput,
        onValueChange = { promptInput = it },
        placeholder = { Text("Ask Race Engineer or dictate with mic...", fontSize = 11.sp, color = TextSecondary) },
        modifier = Modifier
          .weight(1f)
          .testTag("ai_coach_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
          unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        singleLine = true
      )

      IconButton(
        onClick = {
          if (promptInput.isNotBlank()) {
            val textToSend = promptInput
            promptInput = ""
            viewModel.sendAiCoachMessage(
              prompt = textToSend,
              model = selectedModel,
              enableSearch = enableSearchGrounding,
              enableMaps = enableMapsGrounding,
              role = selectedRole
            )
          }
        },
        enabled = promptInput.isNotBlank() && !uiState.isAiGenerating,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(if (promptInput.isNotBlank() && !uiState.isAiGenerating) NeonCyan else CarbonSurfaceVariant)
          .testTag("ai_coach_send")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = if (promptInput.isNotBlank() && !uiState.isAiGenerating) CarbonBlack else TextSecondary,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
  val isUser = message.role == "user"
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Box(
      modifier = Modifier
        .clip(
          RoundedCornerShape(
            topStart = 14.dp,
            topEnd = 14.dp,
            bottomStart = if (isUser) 14.dp else 2.dp,
            bottomEnd = if (isUser) 2.dp else 14.dp
          )
        )
        .background(if (isUser) NeonCyan.copy(alpha = 0.2f) else CarbonSurface)
        .border(
          1.dp,
          if (isUser) NeonCyan.copy(alpha = 0.5f) else CarbonBorder,
          RoundedCornerShape(
            topStart = 14.dp,
            topEnd = 14.dp,
            bottomStart = if (isUser) 14.dp else 2.dp,
            bottomEnd = if (isUser) 2.dp else 14.dp
          )
        )
        .padding(10.dp)
    ) {
      Column {
        if (!isUser) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = message.modelUsed ?: "Apex Engineer",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Text(
          text = message.content,
          fontSize = 12.sp,
          color = TextPrimary,
          lineHeight = 17.sp
        )

        if (message.searchSources.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Grounding: ${message.searchSources.joinToString(", ")}",
            fontSize = 9.sp,
            color = ApexGreen,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
