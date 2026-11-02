package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the Registered Campsites list (US 02.03.01).
 * They give the list some campsites directly, so they do not need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class RegisteredCampsitesListTest {

    @get:Rule
    val composeRule = createComposeRule()

    // Makes a simple campsite with the given name and location
    private fun makeCampsite(name: String, location: String): CampsiteModel {
        return CampsiteModel(
            campsiteName = name,
            campgroundName = "",
            location = location,
            longitude = 0.0,
            latitude = 0.0,
            occupancy = 4,
            userRating = 0.0,
            description = "",
            imageUrl = ""
        )
    }

    @Test
    fun emptyList_showsEmptyMessage() {
        composeRule.setContent {
            RegisteredCampsitesList(listOf())
        }

        composeRule.onNodeWithTag("empty_message").assertIsDisplayed()
    }

    @Test
    fun campsites_areShownWithTheirNames() {
        val campsites = listOf(
            makeCampsite("Site 56A", "Edmonton, AB"),
            makeCampsite("Lakeview", "Banff, AB")
        )

        composeRule.setContent {
            RegisteredCampsitesList(campsites)
        }

        composeRule.onNodeWithText("Site 56A").assertIsDisplayed()
        composeRule.onNodeWithText("Lakeview").assertIsDisplayed()
    }
}