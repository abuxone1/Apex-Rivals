package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BadgeCategory
import com.example.model.BadgeTier
import com.example.model.DriverBadge
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

@Composable
fun DriverBadgesSection(
  badges: List<DriverBadge>,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf(BadgeCategory.ALL) }
  var selectedBadgeForDetail by remember { mutableStateOf<DriverBadge?>(null) }

  val unlockedCount = badges.count { it.isUnlocked }
  val totalCount = badges.size
  val overallUnlockRatio = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

  val filteredBadges = remember(badges, selectedCategory) {
    if (selectedCategory == BadgeCategory.ALL) badges
    else badges.filter { it.category == selectedCategory }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
    modifier = modifier
      .fillMaxWidth()
      .testTag("driver_badges_section")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

      // Header with Title & Overall Unlock Progress
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
              .background(NeonAmber.copy(alpha = 0.15f))
              .border(1.dp, NeonAmber, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.EmojiEvents,
              contentDescription = null,
              tint = NeonAmber,
              modifier = Modifier.size(20.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "DRIVER AWARDS & BADGES",
              fontSize = 13.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 0.5.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(10.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "ROOM DATABASE TELEMETRY MILESTONES",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                letterSpacing = 0.5.sp
              )
            }
          }
        }

        // Summary Counter Chip
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (unlockedCount > 0) ApexGreen.copy(alpha = 0.15f) else CarbonSurfaceVariant)
            .border(1.dp, if (unlockedCount > 0) ApexGreen.copy(alpha = 0.7f) else CarbonBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("badges_unlocked_counter")
        ) {
          Text(
            text = "$unlockedCount / $totalCount UNLOCKED",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = if (unlockedCount > 0) ApexGreen else TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Overall Progress Bar
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CAREER TROPHY COMPLETION",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "${(overallUnlockRatio * 100).toInt()}%",
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Black,
            color = NeonCyan,
            fontFamily = FontFamily.Monospace
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { overallUnlockRatio },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = if (overallUnlockRatio >= 0.75f) ApexGreen else NeonCyan,
          trackColor = CarbonBlack
        )
      }

      // Category Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
        modifier = Modifier.testTag("badges_category_filters")
      ) {
        items(BadgeCategory.values()) { category ->
          val isSelected = selectedCategory == category
          val chipBg = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CarbonSurfaceVariant
          val chipBorder = if (isSelected) NeonCyan else CarbonBorder
          val chipTextColor = if (isSelected) NeonCyan else TextSecondary

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(chipBg)
              .border(1.dp, chipBorder, RoundedCornerShape(6.dp))
              .clickable { selectedCategory = category }
              .padding(horizontal = 10.dp, vertical = 5.dp)
              .testTag("badge_filter_${category.name.lowercase()}")
          ) {
            val countForCat = if (category == BadgeCategory.ALL) badges.size else badges.count { it.category == category }
            val unlockedForCat = if (category == BadgeCategory.ALL) unlockedCount else badges.count { it.category == category && it.isUnlocked }
            Text(
              text = "${category.label} ($unlockedForCat/$countForCat)",
              fontSize = 9.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = chipTextColor
            )
          }
        }
      }

      // Badges List / Grid
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        filteredBadges.forEach { badge ->
          BadgeItemCard(
            badge = badge,
            onClick = { selectedBadgeForDetail = badge }
          )
        }
      }
    }
  }

  // Interactive Badge Detail Dialog
  if (selectedBadgeForDetail != null) {
    val badge = selectedBadgeForDetail!!
    BadgeDetailDialog(
      badge = badge,
      onDismiss = { selectedBadgeForDetail = null }
    )
  }
}

@Composable
fun BadgeItemCard(
  badge: DriverBadge,
  onClick: () -> Unit
) {
  val tierColor = badge.tier.composeColor
  val cardBorderColor = if (badge.isUnlocked) {
    tierColor.copy(alpha = 0.6f)
  } else {
    CarbonBorder
  }

  val cardBg = if (badge.isUnlocked) {
    CarbonSurfaceVariant
  } else {
    CarbonBlack.copy(alpha = 0.85f)
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = cardBg),
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("badge_item_${badge.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Circular Badge Emblem
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(
                if (badge.isUnlocked) tierColor.copy(alpha = 0.18f)
                else CarbonSurfaceVariant
              )
              .border(
                1.5.dp,
                if (badge.isUnlocked) tierColor else CarbonBorder,
                CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (badge.isUnlocked) badge.icon else Icons.Default.Lock,
              contentDescription = badge.title,
              tint = if (badge.isUnlocked) tierColor else TextTertiary,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = badge.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (badge.isUnlocked) TextPrimary else TextSecondary
              )
              Spacer(modifier = Modifier.width(6.dp))
              // Tier Tag Chip
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(tierColor.copy(alpha = 0.15f))
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = badge.tier.label,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Black,
                  color = tierColor,
                  fontFamily = FontFamily.Monospace
                )
              }
            }

            Text(
              text = badge.description,
              fontSize = 9.sp,
              color = TextSecondary,
              lineHeight = 12.sp,
              maxLines = 2
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Status Tag (UNLOCKED / LOCKED)
        if (badge.isUnlocked) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(ApexGreen.copy(alpha = 0.15f))
              .border(1.dp, ApexGreen.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = ApexGreen,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "UNLOCKED",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                color = ApexGreen
              )
            }
          }
        } else {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(CarbonSurface)
              .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 3.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(10.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "LOCKED",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary
              )
            }
          }
        }
      }

      // Progress bar & Live Metric Label
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (badge.isUnlocked) "ROOM TELEMETRY VERIFIED" else "PROGRESS TO UNLOCK",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (badge.isUnlocked) ApexGreen else TextTertiary,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = badge.progressText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (badge.isUnlocked) NeonCyan else TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        LinearProgressIndicator(
          progress = { badge.progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp)),
          color = if (badge.isUnlocked) ApexGreen else tierColor,
          trackColor = CarbonBlack
        )
      }

      // Unlock detail or Requirement hint
      if (badge.isUnlocked && badge.unlockedDetail != null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(CarbonBlack.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = ApexGreen,
              modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = badge.unlockedDetail,
              fontSize = 8.5.sp,
              color = TextSecondary,
              fontFamily = FontFamily.Monospace,
              maxLines = 1
            )
          }
        }
      } else if (!badge.isUnlocked) {
        Text(
          text = "Requires: ${badge.requirementText}",
          fontSize = 8.5.sp,
          color = TextTertiary,
          lineHeight = 11.sp
        )
      }
    }
  }
}

@Composable
fun BadgeDetailDialog(
  badge: DriverBadge,
  onDismiss: () -> Unit
) {
  val tierColor = badge.tier.composeColor

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(16.dp),
    containerColor = CarbonSurface,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(tierColor.copy(alpha = 0.18f))
            .border(2.dp, tierColor, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (badge.isUnlocked) badge.icon else Icons.Default.Lock,
            contentDescription = badge.title,
            tint = tierColor,
            modifier = Modifier.size(26.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = badge.title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = TextPrimary
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(tierColor.copy(alpha = 0.2f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${badge.tier.label} AWARD",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                color = tierColor
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = badge.category.label.uppercase(),
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = TextSecondary
            )
          }
        }
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        // Status Alert Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (badge.isUnlocked) ApexGreen.copy(alpha = 0.12f) else CarbonSurfaceVariant)
            .border(1.dp, if (badge.isUnlocked) ApexGreen.copy(alpha = 0.5f) else CarbonBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (badge.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
              contentDescription = null,
              tint = if (badge.isUnlocked) ApexGreen else TextTertiary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (badge.isUnlocked) "AWARD UNLOCKED & RECORDED IN ROOM DB" else "AWARD LOCKED • RUN TELEMETRY TO UNLOCK",
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Black,
              color = if (badge.isUnlocked) ApexGreen else TextTertiary,
              letterSpacing = 0.5.sp
            )
          }
        }

        // Lore & Description
        Text(
          text = badge.description,
          fontSize = 11.sp,
          color = TextSecondary,
          lineHeight = 16.sp
        )

        // Threshold & Progress Block
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonBlack)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "TELEMETRY THRESHOLD REQUIREMENT",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextTertiary,
              letterSpacing = 0.5.sp
            )
            Text(
              text = badge.requirementText,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "CURRENT PROGRESS",
                fontSize = 8.5.sp,
                color = TextTertiary
              )
              Text(
                text = badge.progressText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace
              )
            }

            LinearProgressIndicator(
              progress = { badge.progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = if (badge.isUnlocked) ApexGreen else tierColor,
              trackColor = CarbonSurfaceVariant
            )
          }
        }

        // Unlocked Details (if present)
        if (badge.isUnlocked && badge.unlockedDetail != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(CarbonSurfaceVariant)
              .border(1.dp, ApexGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "VERIFIED TELEMETRY EVIDENCE",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                color = ApexGreen,
                letterSpacing = 0.5.sp
              )
              Text(
                text = badge.unlockedDetail,
                fontSize = 10.sp,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // Pro Tip Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NeonAmber.copy(alpha = 0.1f))
            .border(1.dp, NeonAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = Icons.Default.Lightbulb,
              contentDescription = null,
              tint = NeonAmber,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "RACE ENGINEER PRO TIP",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                color = NeonAmber,
                letterSpacing = 0.5.sp
              )
              Text(
                text = badge.proTip,
                fontSize = 9.5.sp,
                color = TextPrimary,
                lineHeight = 14.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
      ) {
        Text("Done", color = CarbonBlack, fontWeight = FontWeight.Bold)
      }
    }
  )
}
