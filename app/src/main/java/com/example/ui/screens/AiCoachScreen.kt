package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ai.AiChatSection
import com.example.ui.screens.ai.AiImageStudioSection
import com.example.ui.screens.ai.AiLiveRadioSection
import com.example.ui.screens.ai.AiMusicSection
import com.example.ui.screens.ai.AiVideoSection
import com.example.ui.screens.ai.PlayConsoleSection
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RaceUiState
import com.example.ui.viewmodel.RaceViewModel

enum class AiHubTab(val title: String, val icon: ImageVector) {
  CHAT("Chat & Maps", Icons.Default.AutoAwesome),
  IMAGE("Images (3.1)", Icons.Default.Palette),
  LIVE_RADIO("Live Radio (3.8)", Icons.Default.Radio),
  MUSIC("Music (Lyria 3)", Icons.Default.MusicNote),
  VIDEO("Video (Veo 3)", Icons.Default.Videocam),
  PLAY_CONSOLE("Play Console", Icons.Default.VerifiedUser)
}

@Composable
fun AiCoachScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(AiHubTab.CHAT) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .padding(horizontal = 12.dp, vertical = 6.dp)
  ) {
    // Top Tab Navigation Bar
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp)
    ) {
      items(AiHubTab.values()) { tab ->
        val isSelected = selectedTab == tab
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CarbonSurface)
            .border(
              1.dp,
              if (isSelected) NeonCyan else CarbonBorder,
              RoundedCornerShape(10.dp)
            )
            .clickable { selectedTab = tab }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("ai_tab_${tab.name.lowercase()}"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = tab.icon,
              contentDescription = null,
              tint = if (isSelected) NeonCyan else TextSecondary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = tab.title,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) NeonCyan else TextPrimary
            )
          }
        }
      }
    }

    // Active Tab Screen Content
    when (selectedTab) {
      AiHubTab.CHAT -> AiChatSection(
        uiState = uiState,
        viewModel = viewModel
      )
      AiHubTab.IMAGE -> AiImageStudioSection(
        uiState = uiState,
        viewModel = viewModel
      )
      AiHubTab.LIVE_RADIO -> AiLiveRadioSection(
        uiState = uiState,
        viewModel = viewModel
      )
      AiHubTab.MUSIC -> AiMusicSection(
        uiState = uiState,
        viewModel = viewModel
      )
      AiHubTab.VIDEO -> AiVideoSection(
        uiState = uiState,
        viewModel = viewModel
      )
      AiHubTab.PLAY_CONSOLE -> PlayConsoleSection()
    }
  }
}
