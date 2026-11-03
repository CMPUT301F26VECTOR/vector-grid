package com.example.project_halfway

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for the owner's Waitlist / Winners tabs (US 02.09.01, US 02.05.01).
 * Uses LotteryEntrantsList directly, so no Firestore is needed.
 */
@RunWith(AndroidJUnit4::class)
class LotteryEntrantsListTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyWaitlist_showsEmptyMessage() {
        composeRule.setContent {
            LotteryEntrantsList(waitlistNames = emptyList(), winnerNames = emptyList(), isDrawn = false)
        }

        composeRule.onNodeWithTag("waitlist_count").assertTextEquals("People on waitlist: 0")
        composeRule.onNodeWithTag("empty_waitlist").assertIsDisplayed()
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

        composeRule.onNodeWithTag("waitlist_count").assertTextEquals("People on waitlist: 2")
        composeRule.onNodeWithText("Priya Shah").assertIsDisplayed()
        composeRule.onNodeWithText("Tom Nguyen").assertIsDisplayed()
    }

    @Test
    fun winnersTab_beforeDraw_saysNotDrawnYet() {
        composeRule.setContent {
            LotteryEntrantsList(waitlistNames = listOf("Priya Shah"), winnerNames = emptyList(), isDrawn = false)
        }

        composeRule.onNodeWithTag("winners_tab").performClick()

        composeRule.onNodeWithTag("winners_message")
            .assertTextEquals("Winners are drawn when registration closes.")
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

        composeRule.onNodeWithTag("winners_tab").performClick()

        composeRule.onNodeWithTag("winners_list").assertIsDisplayed()
        composeRule.onNodeWithText("Tom Nguyen").assertIsDisplayed()
    }
}
