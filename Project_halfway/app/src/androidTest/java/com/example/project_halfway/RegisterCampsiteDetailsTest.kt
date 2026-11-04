package com.example.project_halfway

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
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
 * (US 02.01.02), including the map location picked in LocationPickerDialog.
 * Like RegisterCampsiteScreenTest, these stop before anything is saved, so they
 * do not need Firestore. The map is FakePickerMap, so they don't need Google Maps.
 */
@RunWith(AndroidJUnit4::class)
class RegisterCampsiteDetailsTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Starts the screen with no owner signed in, and fills the required boxes
    private fun startWithRequiredFields() {
        composeRule.setContent {
            // A fake map for the location pop-up: one button that "drops a pin" at Jasper
            CompositionLocalProvider(LocalLocationPickerMap provides FakePickerMap.map) {
                RegisterCampsiteScreen()
            }
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
    fun noPin_saysNoMapLocationYet() {
        startWithRequiredFields()

        composeRule.onNodeWithTag("map_location_text").performScrollTo()
            .assertTextEquals("No map location yet.")
    }

    @Test
    fun pickingAPin_showsItsCoordinatesOnTheForm() {
        startWithRequiredFields()

        composeRule.onNodeWithTag("set_location_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("fake_map_pick").performClick()
        composeRule.onNodeWithTag("location_save_button").performClick()

        composeRule.onNodeWithTag("map_location_text").performScrollTo()
            .assert(hasText("52.85070, -118.07700", substring = true))
        composeRule.onNodeWithTag("remove_location_button").assertExists()
    }

    @Test
    fun removingThePin_clearsIt() {
        startWithRequiredFields()

        composeRule.onNodeWithTag("set_location_button").performScrollTo().performClick()
        composeRule.onNodeWithTag("fake_map_pick").performClick()
        composeRule.onNodeWithTag("location_save_button").performClick()
        composeRule.onNodeWithTag("remove_location_button").performScrollTo().performClick()

        composeRule.onNodeWithTag("map_location_text").assertTextEquals("No map location yet.")
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
