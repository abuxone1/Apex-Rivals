package com.example

import com.example.data.firebase.DriverProfile
import com.example.data.gemini.GeminiImageService
import com.example.data.gemini.GeminiLiveVoiceService
import com.example.data.gemini.GeminiMusicService
import com.example.data.gemini.GeminiVideoService
import com.example.data.gemini.LiveVoiceMessage
import com.example.data.gemini.LyriaMusicModel
import com.example.data.gemini.VeoAspectRatio
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiServicesUnitTest {

  @Test
  fun testLyriaMusicModelsConfiguration() {
    assertEquals("lyria-3-clip-preview", LyriaMusicModel.CLIP.modelId)
    assertEquals(30, LyriaMusicModel.CLIP.maxDurationSeconds)

    assertEquals("lyria-3-pro-preview", LyriaMusicModel.PRO.modelId)
    assertEquals(180, LyriaMusicModel.PRO.maxDurationSeconds)
  }

  @Test
  fun testVeoVideoAspectRatiosConfiguration() {
    assertEquals("16:9", VeoAspectRatio.LANDSCAPE_16_9.ratioString)
    assertEquals("9:16", VeoAspectRatio.PORTRAIT_9_16.ratioString)
  }

  @Test
  fun testGeminiMusicServiceGeneration() = runBlocking {
    val service = GeminiMusicService()
    val clipResult = service.generateRacingMusic("Intense Monza chicane theme", LyriaMusicModel.CLIP)
    assertNotNull(clipResult)
    assertEquals(30, clipResult.durationSeconds)
    assertTrue(clipResult.title.isNotEmpty())

    val proResult = service.generateRacingMusic("Full Grand Prix soundtrack", LyriaMusicModel.PRO)
    assertNotNull(proResult)
    assertTrue(proResult.durationSeconds >= 90)
  }

  @Test
  fun testGeminiVideoServiceGeneration() = runBlocking {
    val service = GeminiVideoService()
    val landscapeResult = service.generateVideoFromText(
      prompt = "Hypercar drafting on straight",
      aspectRatio = VeoAspectRatio.LANDSCAPE_16_9
    )
    assertNotNull(landscapeResult)
    assertEquals(VeoAspectRatio.LANDSCAPE_16_9, landscapeResult.aspectRatio)
    assertTrue(landscapeResult.modelUsed.contains("veo-3.1-fast-generate-preview"))

    val portraitResult = service.generateVideoFromText(
      prompt = "Pit stop crew changing tires",
      aspectRatio = VeoAspectRatio.PORTRAIT_9_16
    )
    assertNotNull(portraitResult)
    assertEquals(VeoAspectRatio.PORTRAIT_9_16, portraitResult.aspectRatio)
  }

  @Test
  fun testGeminiLiveVoiceServiceTurn() = runBlocking {
    val service = GeminiLiveVoiceService()
    val history = listOf(
      LiveVoiceMessage(sender = "Driver", text = "Checking tire temps into turn 3")
    )
    val reply = service.conductLiveVoiceTurn("Radio check, what is my delta?", history)
    assertNotNull(reply)
    assertTrue(reply.sender.contains("Race Engineer"))
    assertTrue(reply.text.isNotEmpty())
  }

  @Test
  fun testGeminiImageServiceCreationAndEditing() = runBlocking {
    val service = GeminiImageService()
    val creationResult = service.createImage("Stealth carbon matte livery", "1:1", "1K")
    assertNotNull(creationResult)
    assertEquals("1:1", creationResult.aspectRatio)
    assertTrue(creationResult.description.isNotEmpty())

    val editResult = service.editImage(
      prompt = "Add neon cyan racing stripes and #44 roundel",
      inputImageBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
      aspectRatio = "1:1"
    )
    assertNotNull(editResult)
    assertTrue(editResult.isEdited)
  }

  @Test
  fun testDriverProfileDataIntegrity() {
    val profile = DriverProfile(
      uid = "driver_test_99",
      displayName = "Apex Racer",
      email = "racer@example.com",
      isAnonymous = false,
      isGoogleSignedIn = true,
      licenseGrade = "FIA Superlicense S",
      reputationScore = 2100,
      totalLapsCompleted = 50
    )

    assertEquals("driver_test_99", profile.uid)
    assertEquals("Apex Racer", profile.displayName)
    assertEquals("racer@example.com", profile.email)
    assertTrue(profile.isGoogleSignedIn)
    assertEquals("users/driver_test_99", profile.firestoreDocPath)
  }
}
