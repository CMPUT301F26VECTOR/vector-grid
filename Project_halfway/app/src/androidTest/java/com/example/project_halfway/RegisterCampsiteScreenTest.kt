package com.example.project_halfway

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Register Campsite screen (US 02.01.01).
 * These only test the checks the screen does before saving, so they do not
 * need a Firestore connection. Saving a good form is not tested here.
 */
@RunWith(AndroidJUnit4::class)
class RegisterCampsiteScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Starts the screen with nothing typed in
    private fun startScreen() {
        composeRule.setContent {
            RegisterCampsiteScreen()
        }
    }

    @Test
    fun emptyName_showsNameError() {
        startScreen()

        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text")
            .assertTextEquals("Please enter a campsite name.")
    }

    @Test
    fun emptyLocation_showsLocationError() {
        startScreen()

        composeRule.onNodeWithTag("name_field").performTextInput("Site 56A")
        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text")
            .assertTextEquals("Please enter a location.")
    }

    @Test
    fun emptyOccupancy_showsOccupancyError() {
        startScreen()

        composeRule.onNodeWithTag("name_field").performTextInput("Site 56A")
        composeRule.onNodeWithTag("location_field").performTextInput("Edmonton, AB")
        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text")
            .assertTextEquals("Please enter an occupancy of 1 or more.")
    }

    @Test
    fun zeroOccupancy_showsOccupancyError() {
        startScreen()

        composeRule.onNodeWithTag("name_field").performTextInput("Site 56A")
        composeRule.onNodeWithTag("location_field").performTextInput("Edmonton, AB")
        composeRule.onNodeWithTag("occupancy_field").performTextInput("0")
        composeRule.onNodeWithTag("save_button").performClick()

        composeRule.onNodeWithTag("message_text")
            .assertTextEquals("Please enter an occupancy of 1 or more.")
    }
}