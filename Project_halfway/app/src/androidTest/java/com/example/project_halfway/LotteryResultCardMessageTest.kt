package com.example.project_halfway

import androidx.compose.material3.Text
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.Modifier
import org.junit.Rule
import org.junit.Test

/**
 * Tests that the message area under a reservation only shows when the entrant
 * has reserved dates (US 01.14.01). They use fake data, so no Firebase is needed.
 */
class LotteryResultCardMessageTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun reservedCard_showsTheMessageArea() {
        rule.setContent {
            LotteryResultCard(
                campsiteName = "Site 56A",
                message = "You won!",
                isWin = true,
                reservedText = "Reserved: 2026-11-10 to 2026-11-12",
                messageArea = { Text("Message the owner", modifier = Modifier.testTag("fake_message_area")) }
            )
        }
        rule.onNodeWithTag("fake_message_area").assertIsDisplayed()
    }

    @Test
    fun winnerWithoutReservation_doesNotShowTheMessageArea() {
        rule.setContent {
            LotteryResultCard(
                campsiteName = "Site 56A",
                message = "You won!",
                isWin = true,
                reservedText = null,
                messageArea = { Text("Message the owner", modifier = Modifier.testTag("fake_message_area")) }
            )
        }
        rule.onNodeWithTag("fake_message_area").assertDoesNotExist()
    }

    @Test
    fun notSelected_doesNotShowTheMessageArea() {
        rule.setContent {
            LotteryResultCard(
                campsiteName = "Site 42",
                message = "Not selected",
                isWin = false,
                reservedText = null,
                messageArea = { Text("Message the owner", modifier = Modifier.testTag("fake_message_area")) }
            )
        }
        rule.onNodeWithTag("fake_message_area").assertDoesNotExist()
    }
}