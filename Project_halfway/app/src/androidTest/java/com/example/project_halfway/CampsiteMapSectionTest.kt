package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the map on the campsite details card (US 01.01.05, 02.01.04): a campsite
 * with a pin shows Directions and Open in Google Maps, and one without a pin says
 * "No location yet". Real maps are turned off with LocalShowRealMaps.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteMapSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun campsiteAt(latitude: Double, longitude: Double) = CampsiteModel(
        campsiteName = "Whistlers",
        campgroundName = "Jasper",
        location = "Jasper, AB",
        longitude = longitude,
        latitude = latitude,
        occupancy = 6,
        description = "",
        imageUrl = ""
    )

    private fun showMapSection(campsite: CampsiteModel) {
        composeRule.setContent {
            CompositionLocalProvider(LocalShowRealMaps provides false) {
                Column { CampsiteMapSection(campsite) }
            }
        }
    }

    @Test
    fun campsiteWithPin_showsMapAndButtons() {
        showMapSection(campsiteAt(52.8507, -118.077))

        composeRule.onNodeWithTag("campsite_map").assertIsDisplayed()
        composeRule.onNodeWithTag("directions_button").assertIsDisplayed()
        composeRule.onNodeWithTag("open_in_maps_button").assertIsDisplayed()
        composeRule.onNodeWithTag("no_location_placeholder").assertDoesNotExist()
    }

    @Test
    fun campsiteWithoutPin_saysNoLocationYet() {
        showMapSection(campsiteAt(0.0, 0.0))

        composeRule.onNodeWithTag("no_location_placeholder").assertIsDisplayed()
        composeRule.onNodeWithTag("directions_button").assertDoesNotExist()
        composeRule.onNodeWithTag("open_in_maps_button").assertDoesNotExist()
    }
}
