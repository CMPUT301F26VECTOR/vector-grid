package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests for checking an entrant's message to the owner (US 01.14.01). */
class OwnerMessageTest {

    private fun message(
        text: String,
        reservationId: String = "res1",
        campsiteId: String = "camp1",
        entrantId: String = "entrant1"
    ) = OwnerMessage(
        reservationId = reservationId,
        campsiteId = campsiteId,
        entrantId = entrantId,
        text = text,
        createdAt = 1L
    )

    @Test
    fun normalMessage_hasNoError() {
        assertNull(message("Is early check-in possible?").firstError())
    }

    @Test
    fun blankMessage_isRejected() {
        assertEquals("Please write a message first.", message("   ").firstError())
    }

    @Test
    fun messageOf500Characters_isAllowed() {
        assertNull(message("a".repeat(OwnerMessage.MAX_LENGTH)).firstError())
    }

    @Test
    fun messageOver500Characters_isRejected() {
        assertEquals(
            "A message can be at most 500 characters.",
            message("a".repeat(OwnerMessage.MAX_LENGTH + 1)).firstError()
        )
    }

    @Test
    fun missingReservationCampsiteOrEntrant_isRejected() {
        val expected = "Could not tell which reservation or entrant this message is for."
        assertEquals(expected, message("Hi", reservationId = "").firstError())
        assertEquals(expected, message("Hi", campsiteId = "").firstError())
        assertEquals(expected, message("Hi", entrantId = "").firstError())
    }
}