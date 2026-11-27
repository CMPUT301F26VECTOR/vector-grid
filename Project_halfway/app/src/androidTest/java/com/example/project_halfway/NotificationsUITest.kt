package com.example.project_halfway

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Intent tests for the notification screens. They run on an emulator or phone.
 *
 * What they check: a win, a loss, a reservation and a cancellation each show
 * the right title and text; the opt-out switch turns notifications off and
 * on; and the admin log lists every notification or only owner messages,
 * with names instead of ids.
 *
 * Used for US 01.03.01, US 01.03.02, US 01.05.02, US 01.07.02, US 01.10.01, US 03.08.01 and US 03.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
@RunWith(AndroidJUnit4::class)
class NotificationsUITest {

    @get:Rule
    val composeRule = createComposeRule()

    private val reservation = Reservation(
        entrantId = "entrantA", lotteryId = "lot1", campsiteId = "jasper",
        startDate = "2027-05-14", endDate = "2027-05-16", createdAt = 0L
    )

    private fun note(type: String, sentAt: Long, message: String = "", senderId: String = "") =
        EntrantNotification(
            id = "n$sentAt", entrantId = "entrantA", campsiteId = "jasper",
            type = type, message = message, sentAt = sentAt, senderId = senderId
        )

    @Test
    fun winNotification_showsTitleCampsiteAndText() {
        val n = note(EntrantNotification.TYPE_WON, 0L)
        composeRule.setContent {
            NotificationCard(title = n.title(), campsiteName = "Jasper Pines", text = n.text(), time = n.sentAtText())
        }
        composeRule.onNodeWithText("You won the lottery!").assertIsDisplayed()
        composeRule.onNodeWithText("Jasper Pines").assertIsDisplayed()
        composeRule.onNodeWithText(LotteryResultNotification.WON_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun lossNotification_showsNotSelectedText() {
        val n = note(EntrantNotification.TYPE_NOT_SELECTED, 0L)
        composeRule.setContent {
            NotificationCard(title = n.title(), campsiteName = "", text = n.text(), time = "")
        }
        composeRule.onNodeWithText("Lottery result").assertIsDisplayed()
        composeRule.onNodeWithText(LotteryResultNotification.NOT_SELECTED_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun reservationAndCancellation_showTheirDates() {
        val reserved = EntrantNotification.forReservation(reservation, 0L)
        val cancelled = EntrantNotification.forCancellation(reservation, 0L)
        composeRule.setContent {
            androidx.compose.foundation.layout.Column {
                NotificationCard(reserved.title(), "Jasper Pines", reserved.text(), "")
                NotificationCard(cancelled.title(), "Jasper Pines", cancelled.text(), "")
            }
        }
        composeRule.onNodeWithText("Reservation confirmed").assertIsDisplayed()
        composeRule.onNodeWithText("Your reservation from 2027-05-14 to 2027-05-16 is confirmed.").assertIsDisplayed()
        composeRule.onNodeWithText("Reservation cancelled").assertIsDisplayed()
        composeRule.onNodeWithText("Your reservation from 2027-05-14 to 2027-05-16 has been cancelled.").assertIsDisplayed()
    }

    @Test
    fun optOutSwitch_turnsNotificationsOffAndOn() {
        var lastChoice: Boolean? = null
        composeRule.setContent {
            var optedOut by remember { mutableStateOf(false) }
            OptOutRow(optedOut = optedOut, onChange = { off ->
                optedOut = off
                lastChoice = off
            })
        }
        composeRule.onNodeWithTag("opt_out_switch").assertIsOn()

        // turn notifications off
        composeRule.onNodeWithTag("opt_out_switch").performClick()
        composeRule.onNodeWithTag("opt_out_switch").assertIsOff()
        composeRule.onNodeWithText("Off: you won't get pop-ups").assertIsDisplayed()
        assertEquals(true, lastChoice)

        // turn them back on
        composeRule.onNodeWithTag("opt_out_switch").performClick()
        composeRule.onNodeWithTag("opt_out_switch").assertIsOn()
        assertEquals(false, lastChoice)
    }

    @Test
    fun adminLog_listsEveryNotificationWithNames() {
        val logs = listOf(
            note(EntrantNotification.TYPE_WON, 300),
            note(EntrantNotification.TYPE_RESERVED, 200, "Your reservation is confirmed."),
            note(EntrantNotification.TYPE_OWNER_MESSAGE, 100, "Fire ban this weekend", "ownerB")
        )
        val names = mapOf("entrantA" to "Priya Singh", "jasper" to "Jasper Pines", "ownerB" to "Sam Lee")
        var backPressed = false
        composeRule.setContent {
            NotificationLogList("Notification Logs", logs, names, loading = false, message = "", toback = { backPressed = true })
        }
        composeRule.onNodeWithText("Notification Logs").assertIsDisplayed()
        composeRule.onNodeWithText("3 total").assertIsDisplayed()
        assertEquals(3, composeRule.onAllNodesWithTag("log_row").fetchSemanticsNodes().size)
        composeRule.onNodeWithText("From: Sam Lee").assertIsDisplayed()

        composeRule.onNodeWithText("Go Back").performClick()
        assertTrue(backPressed)
    }

    @Test
    fun ownerMessageLog_showsOnlyOwnerMessages() {
        val all = listOf(
            note(EntrantNotification.TYPE_WON, 300),
            note(EntrantNotification.TYPE_OWNER_MESSAGE, 100, "Gate code is 4521", "ownerB")
        )
        composeRule.setContent {
            NotificationLogList(
                "Owner Message Logs", NotificationHelper.ownerMessagesOnly(all), emptyMap(),
                loading = false, message = "", toback = {}
            )
        }
        composeRule.onNodeWithText("1 total").assertIsDisplayed()
        composeRule.onNodeWithText("Gate code is 4521").assertIsDisplayed()
        composeRule.onNodeWithText("You won the lottery!").assertDoesNotExist()
    }
}
