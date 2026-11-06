package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the screens changed to finish the partly working half-way
 * stories: the rating and description on the details card (US 01.01.04,
 * 01.01.05, 02.01.04), editing a campsite (US 02.01.04), lottery results and
 * reserving (US 02.05.01, 02.04.04) and picking a lottery (US 02.09.01).
 * None of them need Firebase: they use the parts that only draw the screen.
 */
@RunWith(AndroidJUnit4::class)
class HalfwayFixesUITest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun campsite(description: String, ratings: List<Double>) = CampsiteModel(
        campsiteName = "Site 7",
        campgroundName = "Upper Waterton",
        location = "Waterton, AB",
        longitude = 0.0,
        latitude = 0.0,
        occupancy = 4,
        description = description,
        imageUrl = "",
        id = "camp1",
        ownerId = "owner1",
        ratings = ratings
    )

    private fun showDetails(c: CampsiteModel) {
        composeRule.setContent {
            CompositionLocalProvider(LocalShowRealMaps provides false) {
                CampsiteDetailedCard(campsite = c, isLotteryActive = false, onBackClick = {}, onJoinClick = {})
            }
        }
    }

    @Test
    fun details_showDescription() {
        showDetails(campsite("Lakeside tent site with a fire pit", listOf(4.0)))
        composeRule.onNodeWithTag("description_text")
            .performScrollTo()
            .assertTextEquals("Lakeside tent site with a fire pit")
    }

    @Test
    fun details_withoutDescription_sayNoDescriptionYet() {
        showDetails(campsite("", emptyList()))
        composeRule.onNodeWithTag("description_text").performScrollTo().assertTextEquals("No description yet.")
    }

    @Test
    fun details_showAverageRatingAndCount() {
        showDetails(campsite("x", listOf(5.0, 4.0, 3.0)))
        composeRule.onNodeWithText("4.0 (3 ratings)").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun details_withNoRatings_sayNoRatingsYet() {
        showDetails(campsite("x", emptyList()))
        composeRule.onNodeWithText("No ratings yet").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun summaryCard_showsTheSameAverage() {
        composeRule.setContent { CampsiteDescriptionCard(campsite("x", listOf(5.0, 4.0, 3.0))) }
        composeRule.onNodeWithTag("rating_text", useUnmergedTree = true).assertTextEquals("4.0")
    }

    @Test
    fun editCampsite_startsFilledIn() {
        composeRule.setContent {
            RegisterCampsiteScreen(ownerId = "owner1", existing = campsite("Lakeside", emptyList()), onBackClick = {})
        }
        composeRule.onNodeWithText("Edit Campsite").assertIsDisplayed()
        composeRule.onNodeWithTag("name_field").assertTextContains("Site 7")
        composeRule.onNodeWithTag("occupancy_field").assertTextContains("4")
    }

    @Test
    fun winnerWithoutReservation_seesReserveButton() {
        var pressed = false
        composeRule.setContent {
            LotteryResultCard("Site 7", LotteryResultNotification.WON_MESSAGE, true, null) { pressed = true }
        }
        composeRule.onNodeWithTag("result_status").assertTextEquals("You won!")
        composeRule.onNodeWithTag("reserve_button").performClick()
        assertTrue(pressed)
    }

    @Test
    fun winnerWithReservation_seesTheirDates() {
        composeRule.setContent {
            LotteryResultCard("Site 7", LotteryResultNotification.WON_MESSAGE, true, "Reserved: 2026-11-12 to 2026-11-14") {}
        }
        composeRule.onNodeWithTag("reserved_text").assertTextEquals("Reserved: 2026-11-12 to 2026-11-14")
        composeRule.onNodeWithTag("reserve_button").assertDoesNotExist()
    }

    @Test
    fun nonWinner_isToldAndCantReserve() {
        composeRule.setContent {
            LotteryResultCard("Site 7", LotteryResultNotification.NOT_SELECTED_MESSAGE, false, null) {}
        }
        composeRule.onNodeWithTag("result_status").assertTextEquals("Not selected")
        composeRule.onNodeWithTag("reserve_button").assertDoesNotExist()
    }

    @Test
    fun lotteryPicker_tellsWhichLotteryWasPicked() {
        var picked = -1
        composeRule.setContent {
            Column {
                LotteryPicker(listOf("Nov 1 to Nov 21, 2026", "Oct 1 to Oct 10, 2026"), selectedIndex = 0) { picked = it }
            }
        }
        composeRule.onNodeWithText("Oct 1 to Oct 10, 2026").performClick()
        assertEquals(1, picked)
    }
}
