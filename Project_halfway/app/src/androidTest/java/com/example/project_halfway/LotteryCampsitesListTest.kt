package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for LotteryCampsitesList (US 02.04.02): every campsite of the
 * lottery is shown, and the owner can remove one only before registration
 * opens and never the last one. Nothing is loaded from Firestore.
 */
@RunWith(AndroidJUnit4::class)
class LotteryCampsitesListTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val twoCampsites = listOf("camp1" to "Lakeside", "camp2" to "Hilltop")

    @Test
    fun showsEveryCampsiteInTheLottery() {
        composeRule.setContent {
            LotteryCampsitesList(
                campsites = twoCampsites,
                registrationStarted = false,
                message = "",
                onRemove = {}
            )
        }

        composeRule.onNodeWithTag("lottery_campsite_camp1").assertTextEquals("Lakeside")
        composeRule.onNodeWithTag("lottery_campsite_camp2").assertTextEquals("Hilltop")
    }

    @Test
    fun pressingRemove_givesBackThatCampsite() {
        var removed = ""
        composeRule.setContent {
            LotteryCampsitesList(
                campsites = twoCampsites,
                registrationStarted = false,
                message = "",
                onRemove = { removed = it }
            )
        }

        composeRule.onNodeWithTag("remove_campsite_camp2").assertIsEnabled()
        composeRule.onNodeWithTag("remove_campsite_camp2").performClick()

        assertEquals("camp2", removed)
    }

    @Test
    fun afterRegistrationOpens_removeIsTurnedOff() {
        composeRule.setContent {
            LotteryCampsitesList(
                campsites = twoCampsites,
                registrationStarted = true,
                message = "",
                onRemove = {}
            )
        }

        composeRule.onNodeWithTag("remove_campsite_camp1").assertIsNotEnabled()
        composeRule.onNodeWithTag("remove_campsite_camp2").assertIsNotEnabled()
        composeRule.onNodeWithTag("lottery_campsites_note")
            .assertTextEquals("Registration has started, so campsites can no longer be removed.")
    }

    @Test
    fun lastCampsite_cannotBeRemoved() {
        composeRule.setContent {
            LotteryCampsitesList(
                campsites = listOf("camp1" to "Lakeside"),
                registrationStarted = false,
                message = "",
                onRemove = {}
            )
        }

        composeRule.onNodeWithTag("remove_campsite_camp1").assertIsNotEnabled()
        composeRule.onNodeWithTag("lottery_campsites_note").assertIsDisplayed()
    }
}