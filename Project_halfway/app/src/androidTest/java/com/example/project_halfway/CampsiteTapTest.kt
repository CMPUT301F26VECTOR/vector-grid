package com.example.project_halfway

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
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
 * UI tests for the campsite cards (US 01.01.04, US 01.01.05, US 02.01.04):
 * what the summary card shows and its tap, the owner's list passing on the tapped campsite,
 * and the details card's action area. They give the screens campsites
 * directly, so they do not need Firestore.
 */
@RunWith(AndroidJUnit4::class)
class CampsiteTapTest {

    @get:Rule
    val composeRule = createComposeRule()

    // A campsite like the ones on our mockup
    private fun makeCampsite(id: String, name: String): CampsiteModel {
        return CampsiteModel(
            campsiteName = name,
            campgroundName = "Elk Island",
            location = "Fort Saskatchewan, AB",
            longitude = -112.86,
            latitude = 53.61,
            occupancy = 4,
            userRating = 0.0,
            description = "Walk-in tent site by the lake",
            imageUrl = "",
            id = id,
            type = "Tent site",
            services = listOf("Fire pit", "Non-potable water"),
            pricePerNight = 32.0
        )
    }

    @Test
    fun summaryCard_tap_callsOnClick() {
        var tapped = false
        composeRule.setContent {
            CampsiteDescriptionCard(makeCampsite("camp_012", "Site 12"), onClick = { tapped = true })
        }

        composeRule.onNodeWithText("Site 12").performClick()

        assertTrue(tapped)
    }

    // The card is clickable, so Compose merges its texts into one node. The tagged
    // texts below are looked up in the unmerged tree to find them on their own.

    @Test
    fun summaryCard_withLotteryText_showsTheTag() {
        composeRule.setContent {
            CampsiteDescriptionCard(
                makeCampsite("camp_012", "Site 12"),
                lotteryText = "Lottery closes in 5 days"
            )
        }

        composeRule.onNodeWithTag("lottery_tag", useUnmergedTree = true).assertTextEquals("Lottery closes in 5 days")
    }

    @Test
    fun summaryCard_withoutLotteryText_showsNoTag() {
        composeRule.setContent {
            CampsiteDescriptionCard(makeCampsite("camp_012", "Site 12"))
        }

        composeRule.onNodeWithTag("lottery_tag", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun summaryCard_noRatingYet_saysNoRatingsYet() {
        composeRule.setContent {
            // makeCampsite has no entrant ratings, so there is no average yet (US 01.01.04)
            CampsiteDescriptionCard(makeCampsite("camp_012", "Site 12"))
        }

        composeRule.onNodeWithTag("rating_text", useUnmergedTree = true).assertTextEquals("No ratings yet")
    }

    @Test
    fun summaryCard_showsGuestsAndPrice() {
        composeRule.setContent {
            CampsiteDescriptionCard(makeCampsite("camp_012", "Site 12"))
        }

        composeRule.onNodeWithText("4 guests").assertIsDisplayed()
        composeRule.onNodeWithText("$32.00 / night").assertIsDisplayed()
    }

    @Test
    fun ownerList_tap_givesTheTappedCampsite() {
        var picked: CampsiteModel? = null
        composeRule.setContent {
            RegisteredCampsitesList(
                campsites = listOf(makeCampsite("camp_012", "Site 12"), makeCampsite("camp_056", "Site 56A")),
                onCampsiteClick = { picked = it }
            )
        }

        composeRule.onNodeWithText("Site 56A").performClick()

        assertEquals("camp_056", picked?.getId())
    }

    @Test
    fun detailsCard_withActionArea_showsItInsteadOfJoin() {
        composeRule.setContent {
            // A placeholder instead of a real Google Map, so no Play services are needed
            CompositionLocalProvider(LocalShowRealMaps provides false) {
                CampsiteDetailedCard(
                    campsite = makeCampsite("camp_012", "Site 12"),
                    isLotteryActive = true,
                    onBackClick = {},
                    onJoinClick = {},
                    actionArea = { Text("Lottery section goes here") }
                )
            }
        }

        composeRule.onNodeWithText("Lottery section goes here").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Join lottery").assertDoesNotExist()
    }

    @Test
    fun detailsCard_withoutActionArea_showsJoinButton() {
        composeRule.setContent {
            CompositionLocalProvider(LocalShowRealMaps provides false) {
                CampsiteDetailedCard(
                    campsite = makeCampsite("camp_012", "Site 12"),
                    isLotteryActive = true,
                    onBackClick = {},
                    onJoinClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Join lottery").performScrollTo().assertIsDisplayed()
    }
}
