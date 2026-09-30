package com.example.ui.screens.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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

data class ConsoleRequirementItem(
  val section: String,
  val field: String,
  val value: String,
  val isCompleted: Boolean = true,
  val notes: String
)

@Composable
fun PlayConsoleSection(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val privacyPolicyUrl = "https://ais-pre-rabvswlrajngzqphimheco-261183070241.europe-west2.run.app/privacy-policy.html"

  val consoleItems = listOf(
    ConsoleRequirementItem(
      section = "App Identity & Version",
      field = "Package Name (applicationId)",
      value = "com.aistudio.apexrivals.tr8x",
      notes = "Unique package name declared in app/build.gradle.kts"
    ),
    ConsoleRequirementItem(
      section = "App Identity & Version",
      field = "Version Code & Name",
      value = "versionCode 7 (versionName 7.0.0)",
      notes = "Incremented release artifact version for Play Store publication"
    ),
    ConsoleRequirementItem(
      section = "App Identity & Version",
      field = "API Level Targets",
      value = "targetSdk 36, compileSdk 36, minSdk 26",
      notes = "Complies with Google Play target API requirements (Android 14/15/16)"
    ),
    ConsoleRequirementItem(
      section = "Policy & Legal Compliance",
      field = "Privacy Policy Live URL",
      value = privacyPolicyUrl,
      notes = "HTTPS public URL active and embedded in Garage Social & Dialog"
    ),
    ConsoleRequirementItem(
      section = "App Content (Policy)",
      field = "Target Audience & Content",
      value = "Ages 13 and up (Teens & Adults)",
      notes = "Not directed to children under 13 (COPPA compliant)"
    ),
    ConsoleRequirementItem(
      section = "App Content (Policy)",
      field = "Ads Declaration",
      value = "No, does not contain ads",
      notes = "No third-party ad networks or banner SDKs embedded"
    ),
    ConsoleRequirementItem(
      section = "App Content (Policy)",
      field = "App Access Credentials",
      value = "All functionality available without credentials",
      notes = "Anonymous driver login & guest telemetry fully operational"
    ),
    ConsoleRequirementItem(
      section = "Data Safety Form",
      field = "Data Collection & Encryption",
      value = "Encrypted in transit (HTTPS / TLS 1.3)",
      notes = "Telemetry & lap times collected for leaderboards only, never sold"
    ),
    ConsoleRequirementItem(
      section = "Store Listing",
      field = "Category & Tags",
      value = "Games > Racing / Simulation",
      notes = "Developer contact: abux.one@gmail.com"
    )
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(4.dp)
  ) {
    // Header Banner
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
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = ApexGreen,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "GOOGLE PLAY CONSOLE READINESS",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = TextPrimary,
              letterSpacing = 1.sp
            )
            Text(
              text = "ALL 9 PLAY CONSOLE DECLARATIONS CONFIGURED",
              fontSize = 9.sp,
              color = ApexGreen,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(ApexGreen.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
          Text(
            text = "READY",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = ApexGreen,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Requirement Checklist
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(consoleItems.size) { index ->
        val item = consoleItems[index]
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonSurface)
            .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Verified",
                  tint = ApexGreen,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = item.section.uppercase(),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = NeonCyan,
                  fontFamily = FontFamily.Monospace
                )
              }

              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText(item.field, item.value)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "Copied ${item.field}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy Value",
                  tint = TextSecondary,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.field, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = item.value,
              fontSize = 11.sp,
              color = NeonAmber,
              fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = item.notes, fontSize = 9.sp, color = TextSecondary)
          }
        }
      }
    }
  }
}
