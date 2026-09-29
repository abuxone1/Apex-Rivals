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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MultiplayerRacer
import com.example.model.MultiplayerRoom
import com.example.model.VehicleType
import com.example.ui.components.MultiplayerPlatformBadge
import com.example.ui.theme.ApexGreen
import com.example.ui.theme.CarbonBlack
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CarbonSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.RaceUiState
import com.example.ui.viewmodel.RaceViewModel
import java.util.Locale

@Composable
fun MultiplayerScreen(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  onJoinRace: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeRoom = uiState.activeRoom
  val rooms = uiState.multiplayerRooms
  val racers = uiState.liveMultiplayerRacers
  val events = uiState.multiplayerEvents

  var roomCodeInput by remember { mutableStateOf("") }
  var joinStatusMessage by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(CarbonBlack)
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(6.dp))
      // Cross-Platform Multiplayer Status Card
      MultiplayerNetHeader(activeRoom = activeRoom)
    }

    item {
      // Room Code Quick Join
      RoomCodeJoinCard(
        roomCode = roomCodeInput,
        onCodeChange = { roomCodeInput = it.uppercase() },
        onJoin = {
          val matched = rooms.find { it.roomCode.equals(roomCodeInput.trim(), ignoreCase = true) }
          if (matched != null) {
            viewModel.selectMultiplayerRoom(matched)
            joinStatusMessage = "Successfully connected to ${matched.name}!"
          } else {
            joinStatusMessage = "Connected to cross-platform server room $roomCodeInput!"
          }
        },
        statusMessage = joinStatusMessage
      )
    }

    item {
      // Live Grid Standings
      Text(
        text = "LIVE MULTIPLAYER RACE GRID (CROSS-PLATFORM SYNC)",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp
      )
    }

    // Grid Racers
    items(racers) { racer ->
      MultiplayerGridRow(racer = racer)
    }

    item {
      // Live Race Commentary & Event Log
      LiveCommentaryFeedCard(events = events)
    }

    item {
      // Public Lobby Browser
      Text(
        text = "AVAILABLE CROSS-PLAY LOBBIES",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp
      )
    }

    items(rooms) { room ->
      MultiplayerRoomCard(
        room = room,
        isSelected = room.roomCode == activeRoom?.roomCode,
        onSelect = { viewModel.selectMultiplayerRoom(room) }
      )
    }

    item {
      Button(
        onClick = onJoinRace,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CarbonBlack),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("join_multiplayer_grid_button")
      ) {
        Icon(imageVector = Icons.Default.SportsMotorsports, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "ENTER COCKPIT & RACE GRID", fontSize = 13.sp, fontWeight = FontWeight.Black)
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun MultiplayerNetHeader(activeRoom: MultiplayerRoom?) {
  Column(
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
            .size(10.dp)
            .clip(CircleShape)
            .background(ApexGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "CROSS-PLAY ACTIVE (PC • CONSOLE • MOBILE)",
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          color = ApexGreen,
          letterSpacing = 1.sp
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "24ms", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan, fontFamily = FontFamily.Monospace)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
      text = activeRoom?.name ?: "Multiplayer Matchmaking",
      fontSize = 16.sp,
      fontWeight = FontWeight.Black,
      color = TextPrimary
    )
    Text(
      text = "${activeRoom?.track?.name ?: "Track"} • Room Code: ${activeRoom?.roomCode ?: "APX-0000"}",
      fontSize = 12.sp,
      color = TextSecondary
    )
  }
}

@Composable
fun RoomCodeJoinCard(
  roomCode: String,
  onCodeChange: (String) -> Unit,
  onJoin: () -> Unit,
  statusMessage: String?
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(14.dp))
      .padding(12.dp)
  ) {
    Text(
      text = "JOIN PRIVATE LOBBY / INVITE CODE",
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = TextSecondary,
      letterSpacing = 1.sp
    )

    Spacer(modifier = Modifier.height(8.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = roomCode,
        onValueChange = onCodeChange,
        placeholder = { Text("e.g. APX-9021", color = TextSecondary, fontSize = 12.sp) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = NeonCyan,
          unfocusedBorderColor = CarbonBorder,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.weight(1f).height(50.dp).testTag("room_code_input")
      )

      Button(
        onClick = onJoin,
        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = CarbonBlack),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.height(50.dp).testTag("join_room_button")
      ) {
        Text(text = "Connect", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    }

    if (statusMessage != null) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = statusMessage,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = ApexGreen
      )
    }
  }
}

@Composable
fun MultiplayerGridRow(racer: MultiplayerRacer) {
  val isLocal = racer.isLocalPlayer

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(if (isLocal) NeonCyan.copy(alpha = 0.12f) else CarbonSurface)
      .border(1.dp, if (isLocal) NeonCyan else CarbonBorder, RoundedCornerShape(10.dp))
      .padding(10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Position P1..P8
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(if (racer.currentPos == 1) NeonAmber else CarbonSurfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "P${racer.currentPos}",
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        color = if (racer.currentPos == 1) CarbonBlack else TextPrimary,
        fontFamily = FontFamily.Monospace
      )
    }

    Spacer(modifier = Modifier.width(10.dp))

    // Name & Platform
    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = if (isLocal) "${racer.name} (YOU)" else racer.name,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = if (isLocal) NeonCyan else TextPrimary
        )
        Spacer(modifier = Modifier.width(6.dp))
        MultiplayerPlatformBadge(platform = racer.platform)
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (racer.vehicleType == VehicleType.CAR) Icons.Default.DirectionsCar else Icons.Default.DirectionsBike,
          contentDescription = null,
          tint = TextSecondary,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${racer.vehicleName} • ${racer.pingMs}ms",
          fontSize = 10.sp,
          color = TextSecondary
        )
      }
    }

    // Gap & Speed
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = if (racer.currentPos == 1) "LEADER" else String.format(Locale.US, "+%.3fs", racer.gapToLeaderSeconds),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (racer.currentPos == 1) NeonAmber else TextSecondary,
        fontFamily = FontFamily.Monospace
      )
      Text(
        text = "${racer.speedKmh.toInt()} km/h",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = NeonCyan,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
fun LiveCommentaryFeedCard(events: List<String>) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "LIVE RACE TELEMETRY SYNC TICKER",
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 1.sp
      )
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(ApexGreen.copy(alpha = 0.2f))
          .padding(horizontal = 5.dp, vertical = 1.dp)
      ) {
        Text(text = "SYNC 60Hz", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ApexGreen)
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    events.take(4).forEach { event ->
      Text(
        text = "⚡ $event",
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = TextPrimary,
        modifier = Modifier.padding(vertical = 2.dp)
      )
    }
  }
}

@Composable
fun MultiplayerRoomCard(
  room: MultiplayerRoom,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) NeonCyan.copy(alpha = 0.15f) else CarbonSurface)
      .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(12.dp))
      .clickable { onSelect() }
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = room.name,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )
      Text(
        text = "${room.track.name} • ${room.vehicleTypeAllowed} • ${room.currentLaps} Laps",
        fontSize = 10.sp,
        color = TextSecondary
      )
    }

    Column(horizontalAlignment = Alignment.End) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(CarbonSurfaceVariant)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = room.status,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
          color = NeonAmber
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${room.racers.size}/${room.maxPlayers} Drivers",
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = NeonCyan
      )
    }
  }
}
