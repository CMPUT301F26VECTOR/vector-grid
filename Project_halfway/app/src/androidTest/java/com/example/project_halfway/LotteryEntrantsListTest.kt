
package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the owner's Waitlist / Winners tabs.
 * US 02.09.01, US 02.05.01, and US 01.04.03.
 *
 * Uses LotteryEntrantsList directly, so no Firestore is needed.
 */
@RunWith(AndroidJUnit4::class)
class LotteryEntrantsListTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyWaitlist_showsEmptyMessage() {
        composeRule.setContent {
            LotteryEntrantsList(
                waitlistNames = emptyList(),
                winnerNames = emptyList(),
                isDrawn = false
            )
        }

        composeRule.onNodeWithTag("waitlist_count")
            .assertTextEquals("People on waitlist: 0")

        composeRule.onNodeWithTag("empty_waitlist")
            .assertIsDisplayed()
    }

    @Test
    fun waitlist_showsEveryNameAndTheCount() {
        composeRule.setContent {
            LotteryEntrantsList(
                waitlistNames = listOf("Priya Shah", "Tom Nguyen"),
                winnerNames = emptyList(),
                isDrawn = false
            )
        }

        composeRule.onNodeWithTag("waitlist_count")
            .assertTextEquals("People on waitlist: 2")

        composeRule.onNodeWithText("Priya Shah")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Tom Nguyen")
            .assertIsDisplayed()
    }

    @Test
    fun winnersTab_beforeDraw_saysNotDrawnYet() {
        composeRule.setContent {
            LotteryEntrantsList(
                waitlistNames = listOf("Priya Shah"),
                winnerNames = emptyList(),
                isDrawn = false
            )
        }

        composeRule.onNodeWithTag("winners_tab")
            .performClick()

        composeRule.onNodeWithTag("winners_message")
            .assertTextEquals(
                "Winners are drawn when registration closes."
            )
    }

    @Test
    fun winnersTab_afterDraw_showsWinners() {
        composeRule.setContent {
            LotteryEntrantsList(
                waitlistNames = listOf("Priya Shah", "Tom Nguyen"),
                winnerNames = listOf("Tom Nguyen"),
                isDrawn = true
            )
        }

        composeRule.onNodeWithTag("winners_tab")
            .performClick()

        composeRule.onNodeWithTag("winners_list")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Tom Nguyen")
            .assertIsDisplayed()
    }

    /**
     * US 01.04.03:
     * The owner sees when a winner has declined an offer.
     */
    @Test
    fun winnersTab_declinedOffer_showsDeclinedStatus() {
        composeRule.setContent {
            LotteryEntrantsList(
                waitlistNames = listOf("Priya Shah", "Tom Nguyen"),
                winnerNames = listOf("Tom Nguyen"),
                isDrawn = true,
                winnerStatuses = listOf(
                    LotteryResultNotification.OFFER_DECLINED
                )
            )
        }

        composeRule.onNodeWithTag("winners_tab")
            .performClick()

        composeRule.onNodeWithText("Tom Nguyen")
            .assertIsDisplayed()

        composeRule.onNodeWithTag("offer_declined_0")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Offer declined")
            .assertIsDisplayed()
    }

    /**
     * US 01.04.03:
     * A pending offer must not appear as declined.
     */
    @Test
    fun winnersTab_pendingOffer_doesNotShowDeclinedStatus() {
        composeRule.setContent {
            LotteryEntrantsList(
                waitlistNames = listOf("Priya Shah"),
                winnerNames = listOf("Priya Shah"),
                isDrawn = true,
                winnerStatuses = listOf(
                    LotteryResultNotification.OFFER_PENDING
                )
            )
        }

        composeRule.onNodeWithTag("winners_tab")
            .performClick()

        composeRule.onNodeWithText("Priya Shah")
            .assertIsDisplayed()

        assertTrue(
            composeRule.onAllNodesWithTag("offer_declined_0")
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    /**
     * US 01.04.03:
     * Only the declined winner receives the declined label.
     * All winners remain visible.
     */
    @Test
    fun winnersTab_onlyDeclinedWinnerShowsStatus() {
        composeRule.setContent {
            LotteryEntrantsList(
                waitlistNames = listOf("Priya Shah", "Tom Nguyen"),
                winnerNames = listOf("Priya Shah", "Tom Nguyen"),
                isDrawn = true,
                winnerStatuses = listOf(
                    LotteryResultNotification.OFFER_DECLINED,
                    LotteryResultNotification.OFFER_PENDING
                )
            )
        }

        composeRule.onNodeWithTag("winners_tab")
            .performClick()

        // Both winners remain visible.
        composeRule.onNodeWithText("Priya Shah")
            .assertIsDisplayed()

        composeRule.onNodeWithText("Tom Nguyen")
            .assertIsDisplayed()

        // Priya declined.
        composeRule.onNodeWithTag("offer_declined_0")
            .assertIsDisplayed()

        // Tom did not decline.
        assertTrue(
            composeRule.onAllNodesWithTag("offer_declined_1")
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }
}
