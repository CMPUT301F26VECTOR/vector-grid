package com.example.project_halfway

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the extra campsite details on the Register Campsite screen
 * (US 02.01.02). Like RegisterCampsiteScreenTest, these stop before anything
 * is saved, so they do not need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class RegisterCampsiteDetailsTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Starts the screen with no owner signed in, and fills the required boxes
    private fun startWithRequiredFields() {
        composeRule.setContent {
            RegisterCampsiteScreen()
        }
        composeRule.onNodeWithTag("name_field").performTextInput("Site 56A")
        composeRule.onNodeWithTag("location_field").performTextInput("Edmonton, AB")
        composeRule.onNodeWithTag("occupancy_field").performTextInput("3")
    }

    @Test
    fun badPrice_showsPriceError() {
        startWithRequiredFields()

        composeRule.onNodeWithTag("price_field").performScrollTo().performTextInput("-10")
        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text")
            .assertTextEquals("Please enter a price of 0 or more.")
    }

    @Test
    fun onlyLatitude_showsMapLocationError() {
        startWithRequiredFields()

        composeRule.onNodeWithTag("latitude_field").performScrollTo().performTextInput("53.5")
        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text")
            .assertTextEquals("Please enter both latitude and longitude, or leave both empty.")
    }

    @Test
    fun goodForm_withNoOwner_asksToSignIn() {
        startWithRequiredFields()

        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text")
            .assertTextEquals("Please sign in as a campsite owner first.")
    }

    @Test
    fun noDatesYet_showsNoDatesText() {
        composeRule.setContent {
            RegisterCampsiteScreen()
        }

        composeRule.onNodeWithTag("no_dates_text").performScrollTo()
            .assertTextEquals("No dates added yet.")
    }
}
