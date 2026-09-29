package com.example.ui.screens.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gemini.GeneratedVideoResult
import com.example.data.gemini.VeoAspectRatio
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
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Composable
fun AiVideoSection(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var videoPrompt by remember { mutableStateOf("") }
  var selectedAspectRatio by remember { mutableStateOf(VeoAspectRatio.LANDSCAPE_16_9) }
  var isAnimateImageMode by remember { mutableStateOf(false) }

  var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
  var selectedPhotoBase64 by remember { mutableStateOf<String?>(null) }
  var selectedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      selectedPhotoUri = uri
      try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        if (bitmap != null) {
          selectedPhotoBitmap = bitmap
          val outputStream = ByteArrayOutputStream()
          bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
          selectedPhotoBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
      } catch (e: Exception) {
        selectedPhotoBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
      }
    }
  }

  val presets = listOf(
    "Cinematic 280km/h GT3 Monza Parabolica exit with tire smoke",
    "F1 Night Race Marina Bay pitstop under stadium lights",
    "MotoGP 64° knee scrape cornering slow-motion transition",
    "Suzuka Esses high downforce aerodynamic tracking shot"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(4.dp)
  ) {
    // Header Card
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
              imageVector = Icons.Default.Videocam,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "VEO 3 VIDEO GENERATION (veo-3.1-fast-generate-preview)",
              fontSize = 11.sp,
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
              text = "VEO 3",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mode: Text-to-Video vs Animate Image
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!isAnimateImageMode) NeonCyan.copy(alpha = 0.2f) else CarbonSurfaceVariant)
              .border(1.dp, if (!isAnimateImageMode) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable { isAnimateImageMode = false }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "Text to Video",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (!isAnimateImageMode) NeonCyan else TextPrimary
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isAnimateImageMode) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant)
              .border(1.dp, if (isAnimateImageMode) NeonAmber else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable { isAnimateImageMode = true }
              .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = if (isAnimateImageMode) NeonAmber else TextSecondary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Animate Image to Video",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAnimateImageMode) NeonAmber else TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Aspect Ratio Selector: 16:9 or 9:16
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          VeoAspectRatio.values().forEach { ar ->
            val isSelected = selectedAspectRatio == ar
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) CarbonSurfaceVariant else CarbonBlack)
                .border(1.dp, if (isSelected) ApexGreen else CarbonBorder, RoundedCornerShape(8.dp))
                .clickable { selectedAspectRatio = ar }
                .padding(vertical = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = ar.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) ApexGreen else TextSecondary
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Photo selection UI for Animate Image Mode
    if (isAnimateImageMode) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(CarbonSurfaceVariant)
          .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
          .padding(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (selectedPhotoBitmap != null) {
              Image(
                bitmap = selectedPhotoBitmap!!.asImageBitmap(),
                contentDescription = "Selected photo",
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .border(1.dp, NeonAmber, RoundedCornerShape(6.dp))
              )
            } else {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(CarbonBlack)
                  .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  tint = TextSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (selectedPhotoUri != null) "Photo Ready to Animate" else "Choose Photo to Animate",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = if (selectedPhotoUri != null) "Veo 3: 720p 60fps motion synthesis" else "Zero-permission Photo Picker",
                fontSize = 8.5.sp,
                color = TextSecondary
              )
            }
          }

          Button(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = CarbonBlack),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("pick_photo_for_veo_btn")
          ) {
            Text(
              text = if (selectedPhotoUri != null) "Change" else "Select Photo",
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }

    // Presets
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
            .clickable { videoPrompt = preset }
            .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Text(text = preset, fontSize = 9.sp, color = NeonCyan)
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Input Bar
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
        value = videoPrompt,
        onValueChange = { videoPrompt = it },
        placeholder = {
          Text(
            if (isAnimateImageMode) "Motion instructions for photo..." else "Describe 4K racing cinematic...",
            fontSize = 11.sp,
            color = TextSecondary
          )
        },
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
          if (videoPrompt.isNotBlank()) {
            val p = videoPrompt
            if (isAnimateImageMode) {
              val photoBase64 = selectedPhotoBase64 ?: "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
              viewModel.animateVeoImage(prompt = p, imageBase64 = photoBase64, aspectRatio = selectedAspectRatio)
            } else {
              viewModel.generateVeoVideo(prompt = p, aspectRatio = selectedAspectRatio)
            }
          }
        },
        enabled = videoPrompt.isNotBlank() && !uiState.isGeneratingVideo,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = CarbonBlack)
      ) {
        if (uiState.isGeneratingVideo) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CarbonBlack, strokeWidth = 2.dp)
        } else {
          Text("Render", fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Video Output List
    Text(
      text = "VEO 3 GENERATED RACING CINEMATICS",
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
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (uiState.generatedVideos.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CarbonSurface)
              .padding(14.dp)
          ) {
            Text(
              text = "No Veo videos rendered yet. Choose 16:9 (Landscape) or 9:16 (Portrait) and generate dynamic telemetry race replays or animate garage photos with Veo 3.",
              fontSize = 11.sp,
              color = TextSecondary,
              lineHeight = 16.sp
            )
          }
        }
      }

      items(uiState.generatedVideos) { video ->
        VideoCard(video = video)
      }
    }
  }
}

@Composable
fun VideoCard(video: GeneratedVideoResult) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CarbonSurface)
      .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      // Mock video frame preview with play badge
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(if (video.aspectRatio == VeoAspectRatio.LANDSCAPE_16_9) 16f / 9f else 9f / 16f)
          .clip(RoundedCornerShape(8.dp))
          .background(CarbonBlack)
          .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.PlayCircle,
            contentDescription = "Play Video",
            tint = NeonCyan,
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "VEO 3 CINEMATIC REPLAY (${video.aspectRatio.ratioString})",
            fontSize = 9.sp,
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(text = video.prompt, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "${video.modelUsed} • ${video.aspectRatio.label}",
        fontSize = 9.sp,
        color = ApexGreen,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
