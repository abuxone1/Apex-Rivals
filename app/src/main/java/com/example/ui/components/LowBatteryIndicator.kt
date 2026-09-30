package com.example.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.example.ui.theme.RedlineRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * State representing device power levels and battery status during a live race session.
 */
data class DeviceBatteryState(
  val levelPercent: Int = 100,
  val isCharging: Boolean = false,
  val isLow: Boolean = false,
  val isCritical: Boolean = false,
  val estimatedSessionMinutesLeft: Int = 60
)

/**
 * Remembers and observes live Android device battery state.
 * Allows simulatedLevel overrides for interactive testing and demos.
 */
@Composable
fun rememberDeviceBatteryState(
  simulatedLevel: Int? = null,
  lowBatteryThreshold: Int = 20,
  criticalBatteryThreshold: Int = 10
): DeviceBatteryState {
  val context = LocalContext.current
  var liveBatteryState by remember {
    mutableStateOf(
      readDeviceBattery(context, lowBatteryThreshold, criticalBatteryThreshold)
    )
  }

  DisposableEffect(context, simulatedLevel, lowBatteryThreshold, criticalBatteryThreshold) {
    if (simulatedLevel != null) {
      val isLow = simulatedLevel <= lowBatteryThreshold
      val isCritical = simulatedLevel <= criticalBatteryThreshold
      val estMinutes = (simulatedLevel * 1.2f).toInt().coerceAtLeast(1)
      liveBatteryState = DeviceBatteryState(
        levelPercent = simulatedLevel,
        isCharging = false,
        isLow = isLow,
        isCritical = isCritical,
        estimatedSessionMinutesLeft = estMinutes
      )
      return@DisposableEffect onDispose {}
    }

    val receiver = object : BroadcastReceiver() {
      override fun onReceive(c: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
          val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
          val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
          val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
          val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
              status == BatteryManager.BATTERY_STATUS_FULL

          val pct = if (level >= 0 && scale > 0) {
            ((level / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
          } else {
            100
          }

          val isLow = pct <= lowBatteryThreshold && !isCharging
          val isCritical = pct <= criticalBatteryThreshold && !isCharging
          val estMinutes = (pct * 1.2f).toInt().coerceAtLeast(1)

          liveBatteryState = DeviceBatteryState(
            levelPercent = pct,
            isCharging = isCharging,
            isLow = isLow,
            isCritical = isCritical,
            estimatedSessionMinutesLeft = estMinutes
          )
        }
      }
    }

    val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val stickyIntent = context.registerReceiver(receiver, filter)
    if (stickyIntent != null) {
      val level = stickyIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
      val scale = stickyIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
      val status = stickyIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
      val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
          status == BatteryManager.BATTERY_STATUS_FULL

      val pct = if (level >= 0 && scale > 0) {
        ((level / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
      } else {
        100
      }

      val isLow = pct <= lowBatteryThreshold && !isCharging
      val isCritical = pct <= criticalBatteryThreshold && !isCharging
      val estMinutes = (pct * 1.2f).toInt().coerceAtLeast(1)

      liveBatteryState = DeviceBatteryState(
        levelPercent = pct,
        isCharging = isCharging,
        isLow = isLow,
        isCritical = isCritical,
        estimatedSessionMinutesLeft = estMinutes
      )
    }

    onDispose {
      try {
        context.unregisterReceiver(receiver)
      } catch (_: Exception) {
      }
    }
  }

  return liveBatteryState
}

private fun readDeviceBattery(
  context: Context,
  lowThreshold: Int,
  criticalThreshold: Int
): DeviceBatteryState {
  return try {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val pct = if (level >= 0 && scale > 0) {
      ((level / scale.toFloat()) * 100).toInt().coerceIn(0, 100)
    } else {
      100
    }

    val isLow = pct <= lowThreshold && !isCharging
    val isCritical = pct <= criticalThreshold && !isCharging
    val estMinutes = (pct * 1.2f).toInt().coerceAtLeast(1)

    DeviceBatteryState(
      levelPercent = pct,
      isCharging = isCharging,
      isLow = isLow,
      isCritical = isCritical,
      estimatedSessionMinutesLeft = estMinutes
    )
  } catch (_: Exception) {
    DeviceBatteryState()
  }
}

/**
 * Compact high-tech status badge for the top dashboard bar.
 * Pulses when battery is low or critical to immediately alert the driver.
 */
@Composable
fun LowBatteryCompactBadge(
  batteryState: DeviceBatteryState,
  modifier: Modifier = Modifier,
  onToggleDetail: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "batteryPulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (batteryState.isCritical) 250 else 600),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseAlpha"
  )

  val badgeColor = when {
    batteryState.isCharging -> ApexGreen
    batteryState.isCritical -> RedlineRed
    batteryState.isLow -> NeonAmber
    else -> NeonCyan
  }

  val backgroundColor = when {
    batteryState.isCritical -> RedlineRed.copy(alpha = 0.15f * pulseAlpha + 0.1f)
    batteryState.isLow -> NeonAmber.copy(alpha = 0.15f * pulseAlpha + 0.08f)
    batteryState.isCharging -> ApexGreen.copy(alpha = 0.12f)
    else -> CarbonSurfaceVariant.copy(alpha = 0.8f)
  }

  val borderColor = when {
    batteryState.isCritical -> RedlineRed.copy(alpha = pulseAlpha)
    batteryState.isLow -> NeonAmber.copy(alpha = pulseAlpha)
    batteryState.isCharging -> ApexGreen.copy(alpha = 0.6f)
    else -> CarbonBorder
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(backgroundColor)
      .border(0.8.dp, borderColor, RoundedCornerShape(6.dp))
      .clickable(onClickLabel = "View device battery details") { onToggleDetail() }
      .padding(horizontal = 7.dp, vertical = 3.dp)
      .testTag("dashboard_battery_badge"),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      if (batteryState.isCharging) {
        Icon(
          imageVector = Icons.Default.BatteryChargingFull,
          contentDescription = "Device charging",
          tint = ApexGreen,
          modifier = Modifier.size(12.dp)
        )
      } else if (batteryState.isLow || batteryState.isCritical) {
        Icon(
          imageVector = Icons.Default.BatteryAlert,
          contentDescription = "Low battery warning",
          tint = badgeColor,
          modifier = Modifier.size(12.dp)
        )
      } else {
        Icon(
          imageVector = Icons.Default.BatteryStd,
          contentDescription = "Device battery",
          tint = NeonCyan,
          modifier = Modifier.size(12.dp)
        )
      }

      Text(
        text = "${batteryState.levelPercent}%",
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Black,
        fontFamily = FontFamily.Monospace,
        color = badgeColor,
        modifier = Modifier.testTag("low_battery_percentage_text")
      )

      if (batteryState.isCritical) {
        Text(
          text = "CRIT",
          fontSize = 8.sp,
          fontWeight = FontWeight.Black,
          color = RedlineRed,
          fontFamily = FontFamily.Monospace
        )
      } else if (batteryState.isLow) {
        Text(
          text = "LOW",
          fontSize = 8.sp,
          fontWeight = FontWeight.Black,
          color = NeonAmber,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

/**
 * Prominent race dashboard notification banner alerting driver of low device power.
 * Provides estimated remaining race minutes, power saving recommendations, and dismiss/test actions.
 */
@Composable
fun LowBatteryWarningBanner(
  batteryState: DeviceBatteryState,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit = {},
  onCycleSimulation: () -> Unit = {}
) {
  val infiniteTransition = rememberInfiniteTransition(label = "bannerAlertGlow")
  val borderAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = if (batteryState.isCritical) 300 else 700),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bannerBorderAlpha"
  )

  val alertColor = if (batteryState.isCritical) RedlineRed else NeonAmber
  val alertHeader = if (batteryState.isCritical) {
    "CRITICAL DEVICE BATTERY (${batteryState.levelPercent}%)"
  } else {
    "LOW BATTERY ALERT (${batteryState.levelPercent}%)"
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("low_battery_banner"),
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = CarbonBlack),
    border = androidx.compose.foundation.BorderStroke(
      width = 1.2.dp,
      color = alertColor.copy(alpha = borderAlpha)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(
              alertColor.copy(alpha = 0.18f),
              CarbonBlack.copy(alpha = 0.95f)
            )
          )
        )
        .padding(10.dp)
    ) {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(alertColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.BatteryAlert,
              contentDescription = "Battery Warning Alert",
              tint = alertColor,
              modifier = Modifier.size(15.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Text(
              text = alertHeader,
              fontSize = 11.sp,
              fontWeight = FontWeight.Black,
              fontFamily = FontFamily.Monospace,
              color = alertColor,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "~${batteryState.estimatedSessionMinutesLeft} min estimated race time remaining",
              fontSize = 9.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
          }
        }

        // Dismiss Icon Button (at least 48dp touch target)
        IconButton(
          onClick = onDismiss,
          modifier = Modifier
            .size(36.dp)
            .testTag("low_battery_dismiss_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss low battery warning",
            tint = TextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Battery level telemetry progress bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "PWR LEVEL",
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextTertiary,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.width(55.dp)
        )

        Box(
          modifier = Modifier
            .weight(1f)
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(CarbonSurfaceVariant)
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth((batteryState.levelPercent / 100f).coerceIn(0f, 1f))
              .background(
                Brush.horizontalGradient(
                  listOf(RedlineRed, alertColor)
                )
              )
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = "${batteryState.levelPercent}%",
          fontSize = 9.sp,
          fontWeight = FontWeight.Black,
          color = alertColor,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Guidance Note
      Text(
        text = "Telemetry GPS sensors and screen brightness consume high power. Connect a charger to ensure complete session recording and avoid mid-race shutdown.",
        fontSize = 9.sp,
        lineHeight = 13.sp,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Action row: Acknowledge button and Simulate toggle for testing
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Test simulation button
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CarbonSurfaceVariant)
            .border(0.8.dp, CarbonBorder, RoundedCornerShape(4.dp))
            .clickable(onClickLabel = "Cycle battery test simulation") { onCycleSimulation() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("low_battery_sim_toggle")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "TEST SIM LEVEL",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Acknowledge Button
        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(
            containerColor = alertColor.copy(alpha = 0.2f),
            contentColor = alertColor
          ),
          shape = RoundedCornerShape(4.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier
            .height(28.dp)
            .testTag("low_battery_ack_button")
        ) {
          Text(
            text = "ACKNOWLEDGE",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
