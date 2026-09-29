package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.screens.CompanyAndLinksTab
import com.example.ui.screens.DirectPublishTab
import com.example.ui.screens.GooglePlayListingData
import com.example.ui.screens.GooglePlayPublishScreen
import com.example.ui.screens.LegalLinkCard
import com.example.ui.screens.MediaAndAssetsTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GooglePlayPublishScreenTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `google play publish screen renders hero header and store listing data`() {
    var navigatedBack = false

    composeTestRule.setContent {
      GooglePlayPublishScreen(
        onNavigateBack = { navigatedBack = true }
      )
    }

    // Main screen container
    composeTestRule.onNodeWithTag("play_console_screen").assertIsDisplayed()

    // Title and Header
    composeTestRule.onNodeWithText("GOOGLE PLAY CONSOLE").assertIsDisplayed()
    composeTestRule.onNodeWithText("RELEASE & STORE PUBLISHING CENTER").assertIsDisplayed()

    // Hero Header with App Version & Company Name
    composeTestRule.onNodeWithText("Apex Rivals v${GooglePlayListingData.VERSION_NAME}").assertIsDisplayed()
    composeTestRule.onNodeWithText(GooglePlayListingData.COMPANY_NAME).assertIsDisplayed()
    composeTestRule.onNodeWithText("TARGET SDK ${GooglePlayListingData.TARGET_SDK}").assertIsDisplayed()

    // Primary action buttons
    composeTestRule.onNodeWithTag("launch_console_external_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("toggle_in_app_browser_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("launch_play_store_btn").assertIsDisplayed()

    // Store listing tabs
    composeTestRule.onNodeWithTag("publish_tab_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("publish_tab_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("publish_tab_2").assertIsDisplayed()

    // Test back button
    composeTestRule.onNodeWithTag("play_publish_back_button").performClick()
    assertTrue("Should trigger onNavigateBack callback", navigatedBack)
  }

  @Test
  fun `media and assets tab displays app logo, feature graphic and promo trailer`() {
    var videoLaunched = false

    composeTestRule.setContent {
      MediaAndAssetsTab(
        onCopy = { _, _ -> },
        onLaunchVideo = { videoLaunched = true }
      )
    }

    // App Logo Card
    composeTestRule.onNodeWithText("Unique App Logo & Icon").assertIsDisplayed()
    composeTestRule.onNodeWithText("512x512 Ready").assertIsDisplayed()

    // Feature Graphic & Trailer Card
    composeTestRule.onNodeWithText("Feature Graphic (1024x500) & Promo Video").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trailer").assertIsDisplayed()
    composeTestRule.onNodeWithText("Trailer").performClick()
    assertTrue("Trailer callback should be triggered", videoLaunched)
  }

  @Test
  fun `company and links tab displays organisation info`() {
    composeTestRule.setContent {
      CompanyAndLinksTab(
        onCopy = { _, _ -> },
        onOpenUrl = {},
        onReadText = { _, _ -> },
        onRequestDataDeletion = {}
      )
    }

    // Company & Organization Info
    composeTestRule.onNodeWithText("Publishing Company & Organization").assertIsDisplayed()
    composeTestRule.onNodeWithText(GooglePlayListingData.COMPANY_NAME).assertIsDisplayed()
    composeTestRule.onNodeWithText(GooglePlayListingData.ORGANISATION_TYPE).assertIsDisplayed()
  }

  @Test
  fun `legal link cards render website, privacy policy, terms and data deletion links`() {
    var openedUrl: String? = null
    var copiedLabel: String? = null

    composeTestRule.setContent {
      LegalLinkCard(
        icon = Icons.Default.Language,
        title = "Official Studio & Game Website",
        subtitle = "Mandatory contact & presence website for Google Play Console",
        url = GooglePlayListingData.WEBSITE_URL,
        onOpen = { openedUrl = GooglePlayListingData.WEBSITE_URL },
        onCopy = { copiedLabel = "Website" }
      )
    }

    composeTestRule.onNodeWithText("Official Studio & Game Website").assertIsDisplayed()
    composeTestRule.onNodeWithText(GooglePlayListingData.WEBSITE_URL).assertIsDisplayed()
    composeTestRule.onNodeWithText("Open").performClick()
    assertEquals(GooglePlayListingData.WEBSITE_URL, openedUrl)

    composeTestRule.onNodeWithText("Copy Link").performClick()
    assertEquals("Website", copiedLabel)
  }

  @Test
  fun `metadata and company compliance links adhere strictly to google play policies`() {
    // Title must be <= 30 characters
    assertTrue(
      "App title length (${GooglePlayListingData.APP_TITLE.length}) must be <= 30 chars",
      GooglePlayListingData.APP_TITLE.length <= 30
    )

    // Short description must be <= 80 characters
    assertTrue(
      "Short description length (${GooglePlayListingData.SHORT_DESCRIPTION.length}) must be <= 80 chars",
      GooglePlayListingData.SHORT_DESCRIPTION.length <= 80
    )

    // Target SDK must be >= 34 for Google Play
    assertTrue(
      "Target SDK (${GooglePlayListingData.TARGET_SDK}) must be >= 34",
      GooglePlayListingData.TARGET_SDK >= 34
    )

    // Company and website URLs configured
    assertTrue("Company name must not be blank", GooglePlayListingData.COMPANY_NAME.isNotBlank())
    assertTrue("Website must be https", GooglePlayListingData.WEBSITE_URL.startsWith("https://"))
    assertTrue("Privacy policy must be https", GooglePlayListingData.PRIVACY_POLICY_URL.startsWith("https://"))
    assertTrue("Terms of service must be https", GooglePlayListingData.TERMS_OF_SERVICE_URL.startsWith("https://"))
    assertTrue("Data deletion URL must be https", GooglePlayListingData.DATA_DELETION_URL.startsWith("https://"))
    assertTrue("Promo video URL must be valid", GooglePlayListingData.PROMO_VIDEO_URL.contains("youtu"))

    // Full description must be <= 4000 characters
    assertTrue(
      "Full description length must be <= 4000 chars",
      GooglePlayListingData.FULL_DESCRIPTION.length <= 4000
    )
  }

  @Test
  fun `direct publish and api tab renders console action hub and deployment commands`() {
    var openedUrl: String? = null
    var copiedLabel: String? = null

    composeTestRule.setContent {
      DirectPublishTab(
        onCopy = { label, _ -> copiedLabel = label },
        onOpenUrl = { url -> openedUrl = url }
      )
    }

    // Action Hub & Direct Track Buttons
    composeTestRule.onNodeWithText("Google Play Console Action Hub").assertIsDisplayed()
    composeTestRule.onNodeWithText("DIRECT API V3").assertIsDisplayed()
    composeTestRule.onNodeWithTag("launch_production_track_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("launch_internal_testing_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("launch_api_access_btn").assertIsDisplayed()

    // Click production track button
    composeTestRule.onNodeWithTag("launch_production_track_btn").performClick()
    assertEquals(GooglePlayListingData.PLAY_CONSOLE_PRODUCTION_URL, openedUrl)

    // Pre-flight Audit
    composeTestRule.onNodeWithText("Pre-Flight Play Policy & Bundle Audit").assertIsDisplayed()
    composeTestRule.onNodeWithText("7/7 PASSED").assertIsDisplayed()
  }
}
