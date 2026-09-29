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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.gemini.GeneratedLiveryResult
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
fun AiImageStudioSection(
  uiState: RaceUiState,
  viewModel: RaceViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var isEditMode by remember { mutableStateOf(false) }
  var imagePrompt by remember { mutableStateOf("") }
  var selectedAspectRatio by remember { mutableStateOf("1:1") }
  var selectedResolution by remember { mutableStateOf("1K") }

  var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
  var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
  var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

  // Android Photo Picker (zero-permission storage compliant with Google Play Policies)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      selectedImageUri = uri
      try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        if (bitmap != null) {
          selectedImageBitmap = bitmap
          val outputStream = ByteArrayOutputStream()
          bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
          selectedImageBase64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        }
      } catch (e: Exception) {
        // Fallback placeholder image base64
        selectedImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
      }
    }
  }

  val createPresets = listOf(
    "Cyberpunk neon livery with luminescent wheel rim decals",
    "Gulf Racing heritage light blue & orange endurance livery",
    "Matte stealth black carbon weave aerodynamic kit",
    "Redline Formula aero wings with titanium exhaust heat glow",
    "Weathered Le Mans 24h race-worn patina and tire marks"
  )

  val editPresets = listOf(
    "Add glowing neon cyan racing stripes and carbon fiber rear wing",
    "Apply matte midnight purple finish with gold Brembo calipers",
    "Add aggressive front aero canards and widebody fender flares",
    "Change sponsor decals to Apex Motorsport championship branding"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(4.dp)
  ) {
    // 1. Header Card
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
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = NeonCyan,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "IMAGE STUDIO (gemini-3.1-flash-image-preview)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Create and Edit Motorsport Liveries & Vehicle Concepts",
                fontSize = 9.sp,
                color = TextSecondary
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(NeonCyan.copy(alpha = 0.15f))
              .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "GEMINI 3.1",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = NeonCyan,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mode Switcher: Create vs Edit
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (!isEditMode) NeonCyan.copy(alpha = 0.2f) else CarbonSurfaceVariant)
              .border(1.dp, if (!isEditMode) NeonCyan else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable { isEditMode = false }
              .padding(vertical = 7.dp)
              .testTag("mode_create_image_btn"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = null,
                tint = if (!isEditMode) NeonCyan else TextSecondary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Create Image",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isEditMode) NeonCyan else TextPrimary
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isEditMode) NeonAmber.copy(alpha = 0.2f) else CarbonSurfaceVariant)
              .border(1.dp, if (isEditMode) NeonAmber else CarbonBorder, RoundedCornerShape(8.dp))
              .clickable { isEditMode = true }
              .padding(vertical = 7.dp)
              .testTag("mode_edit_image_btn"),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = if (isEditMode) NeonAmber else TextSecondary,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Edit Existing Image",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEditMode) NeonAmber else TextPrimary
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Aspect Ratio Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("1:1", "16:9", "4:3", "9:16").forEach { ar ->
            val isSelected = selectedAspectRatio == ar
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSelected) CarbonBlack else CarbonSurfaceVariant)
                .border(1.dp, if (isSelected) ApexGreen else CarbonBorder, RoundedCornerShape(6.dp))
                .clickable { selectedAspectRatio = ar }
                .padding(vertical = 5.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = ar,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) ApexGreen else TextSecondary
              )
            }
          }
        }

        if (!isEditMode) {
          Spacer(modifier = Modifier.height(8.dp))
          // Resolution Selector (512px, 1K, 2K)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("512px", "1K", "2K").forEach { res ->
              val isSelected = selectedResolution == res
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) CarbonBlack else CarbonSurfaceVariant)
                  .border(1.dp, if (isSelected) NeonCyan else CarbonBorder, RoundedCornerShape(6.dp))
                  .clickable { selectedResolution = res }
                  .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Res: $res",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) NeonCyan else TextSecondary
                )
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Photo Upload for Edit Mode
    if (isEditMode) {
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
            if (selectedImageBitmap != null) {
              Image(
                bitmap = selectedImageBitmap!!.asImageBitmap(),
                contentDescription = "Selected vehicle",
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .border(1.dp, NeonAmber, RoundedCornerShape(6.dp))
              )
            } else {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(CarbonBlack)
                  .border(1.dp, CarbonBorder, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  tint = TextSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = if (selectedImageUri != null) "Base Photo Selected" else "Select Vehicle Photo to Edit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = if (selectedImageUri != null) "Zero-permission Photo Picker" else "Tap Pick Photo below",
                fontSize = 9.sp,
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
            modifier = Modifier.testTag("pick_photo_for_edit_btn")
          ) {
            Text(
              text = if (selectedImageUri != null) "Change Photo" else "Pick Photo",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }

    // 3. Preset Ideas
    val currentPresets = if (isEditMode) editPresets else createPresets
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(currentPresets) { preset ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CarbonSurfaceVariant)
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
            .clickable { imagePrompt = preset }
            .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
          Text(
            text = preset,
            fontSize = 9.sp,
            color = if (isEditMode) NeonAmber else NeonCyan
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 4. Input Prompt & Action
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
        value = imagePrompt,
        onValueChange = { imagePrompt = it },
        placeholder = {
          Text(
            text = if (isEditMode) "Describe modifications (e.g. add stripes, carbon wing)..." else "Describe livery or concept art...",
            fontSize = 11.sp,
            color = TextSecondary
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("image_prompt_input"),
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
          if (imagePrompt.isNotBlank()) {
            if (isEditMode) {
              val base64 = selectedImageBase64 ?: "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
              viewModel.editLiveryImage(
                prompt = imagePrompt,
                inputImageBase64 = base64,
                aspectRatio = selectedAspectRatio
              )
            } else {
              viewModel.createLiveryImage(
                prompt = imagePrompt,
                aspectRatio = selectedAspectRatio,
                resolution = selectedResolution
              )
            }
          }
        },
        enabled = imagePrompt.isNotBlank() && !uiState.isGeneratingLivery,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isEditMode) NeonAmber else NeonCyan,
          contentColor = CarbonBlack
        ),
        modifier = Modifier.testTag("execute_image_action_btn")
      ) {
        if (uiState.isGeneratingLivery) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CarbonBlack, strokeWidth = 2.dp)
        } else {
          Text(
            text = if (isEditMode) "Apply Edit" else "Generate",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 5. Output List
    Text(
      text = "GEMINI 3.1 FLASH IMAGE CREATIONS & EDITS",
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
      if (uiState.generatedLiveries.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(CarbonSurface)
              .padding(16.dp)
          ) {
            Column {
              Text(
                text = "No images created yet.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Use text prompts to create photorealistic race car liveries or upload an existing photo to edit bodywork, colors, and aero parts with gemini-3.1-flash-image-preview.",
                fontSize = 10.sp,
                color = TextSecondary,
                lineHeight = 14.sp
              )
            }
          }
        }
      } else {
        items(uiState.generatedLiveries) { item ->
          LiveryResultCard(item = item)
        }
      }
    }
  }
}

@Composable
private fun LiveryResultCard(item: GeneratedLiveryResult) {
  val decodedBitmap = remember(item.base64Image) {
    if (!item.base64Image.isNullOrBlank()) {
      try {
        val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
      } catch (e: Exception) {
        null
      }
    } else null
  }

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
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(if (item.isEdited) NeonAmber.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (item.isEdited) "EDITED" else "CREATED",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Black,
              color = if (item.isEdited) NeonAmber else NeonCyan
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Ratio: ${item.aspectRatio}",
            fontSize = 9.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
          )
        }

        Text(
          text = "gemini-3.1-flash-image-preview",
          fontSize = 8.5.sp,
          color = TextSecondary,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (decodedBitmap != null) {
        Image(
          bitmap = decodedBitmap.asImageBitmap(),
          contentDescription = item.prompt,
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, CarbonBorder, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
      }

      Text(
        text = item.prompt,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = item.description,
        fontSize = 9.5.sp,
        color = TextSecondary,
        lineHeight = 13.sp
      )
    }
  }
}
