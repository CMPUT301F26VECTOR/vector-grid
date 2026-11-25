package com.example.project_halfway

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Tests for the "Message the owner" box (US 01.14.01). They use a fake send, so no Firebase is needed. */
class MessageOwnerSectionTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun notSignedIn_asksToSignIn() {
        var sent = false
        rule.setContent {
            MessageOwnerSection("res1", "camp1", entrantId = "", send = { _, _ -> sent = true })
        }
        rule.onNodeWithTag("message_text").performTextInput("Hello")
        rule.onNodeWithTag("message_send").performClick()
        rule.onNodeWithTag("message_status").assertTextEquals("Please sign in first")
        assertTrue(!sent)
    }

    @Test
    fun emptyMessage_showsErrorAndDoesNotSend() {
        var sent = false
        rule.setContent {
            MessageOwnerSection("res1", "camp1", entrantId = "entrant1", send = { _, _ -> sent = true })
        }
        rule.onNodeWithTag("message_send").performClick()
        rule.onNodeWithTag("message_status").assertTextEquals("Please write a message first.")
        assertTrue(!sent)
    }

    @Test
    fun goodMessage_isSentWithTheRightIdsAndTheBoxClears() {
        var sentMessage: OwnerMessage? = null
        rule.setContent {
            MessageOwnerSection("res1", "camp1", entrantId = "entrant1", send = { message, onDone ->
                sentMessage = message
                onDone(true)
            })
        }
        rule.onNodeWithTag("message_text").performTextInput("Is early check-in possible?")
        rule.onNodeWithTag("message_send").performClick()
        rule.onNodeWithTag("message_status").assertTextEquals("Message sent")
        assertEquals("res1", sentMessage?.reservationId)
        assertEquals("camp1", sentMessage?.campsiteId)
        assertEquals("entrant1", sentMessage?.entrantId)
        assertEquals("Is early check-in possible?", sentMessage?.text)
        rule.onNodeWithTag("message_text").assertTextEquals("0/500".let { "" }.ifEmpty { "" })
    }

    @Test
    fun failedSend_showsTryAgain() {
        rule.setContent {
            MessageOwnerSection("res1", "camp1", entrantId = "entrant1", send = { _, onDone -> onDone(false) })
        }
        rule.onNodeWithTag("message_text").performTextInput("Hello")
        rule.onNodeWithTag("message_send").performClick()
        rule.onNodeWithTag("message_status").assertTextEquals("Could not send, please try again.")
    }
}