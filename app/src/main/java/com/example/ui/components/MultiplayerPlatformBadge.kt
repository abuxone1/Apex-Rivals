package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlatformType
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.PlatformAndroid
import com.example.ui.theme.PlatformApple
import com.example.ui.theme.PlatformPc
import com.example.ui.theme.PlatformPs
import com.example.ui.theme.PlatformXbox
import com.example.ui.theme.TextPrimary

@Composable
fun MultiplayerPlatformBadge(
  platform: PlatformType,
  modifier: Modifier = Modifier
) {
  val (color, label) = when (platform) {
    PlatformType.PC -> PlatformPc to "PC"
    PlatformType.PLAYSTATION -> PlatformPs to "PS5"
    PlatformType.XBOX -> PlatformXbox to "XBOX"
    PlatformType.IOS -> PlatformApple to "iOS"
    PlatformType.ANDROID -> PlatformAndroid to "AND"
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(color.copy(alpha = 0.2f))
      .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(color)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = color
      )
    }
  }
}
