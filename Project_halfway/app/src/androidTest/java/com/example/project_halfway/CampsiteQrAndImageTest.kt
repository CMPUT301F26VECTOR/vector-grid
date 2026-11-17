package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the campsite QR code screen (US 02.02.01) and the image section
 * of Register / Edit Campsite (US 02.01.02, 02.01.03). No Firestore is needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
@RunWith(AndroidJUnit4::class)
class CampsiteQrAndImageTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun campsite(imageUrl: String = "", ownerId: String = "owner1") = CampsiteModel(
        campsiteName = "Site 56A", campgroundName = "Elk Island", location = "Edmonton, AB",
        longitude = 0.0, latitude = 0.0, occupancy = 4, description = "Shady",
        imageUrl = imageUrl, id = "camp1", ownerId = ownerId
    )

    @Test
    fun qrScreenShowsTheCodeAndItsLink() {
        composeRule.setContent { CampsiteQrScreen(campsite = campsite()) }

        composeRule.onNodeWithTag("campsite_qr_image").assertIsDisplayed()
        composeRule.onNodeWithTag("campsite_qr_link").assertTextEquals("campermax://campsite/camp1")
        composeRule.onNodeWithTag("campsite_qr_share").assertIsDisplayed()
    }

    @Test
    fun aNewCampsiteHasNoImageAndAnUploadButton() {
        composeRule.setContent { RegisterCampsiteScreen(ownerId = "owner1") }

        composeRule.onNodeWithTag("no_image_text").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("upload_image_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun removingTheImageClearsIt() {
        composeRule.setContent {
            RegisterCampsiteScreen(ownerId = "owner1", existing = campsite(imageUrl = "https://example.com/site.jpg"))
        }

        composeRule.onNodeWithTag("image_added_text").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("remove_image_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("no_image_text").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun anotherOwnerCantSaveChanges() {
        composeRule.setContent { RegisterCampsiteScreen(ownerId = "owner2", existing = campsite()) }

        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text").assertTextEquals("You can only edit your own campsites.")
    }
}
