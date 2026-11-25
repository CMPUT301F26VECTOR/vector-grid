package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for EntrantNotification and NotificationHelper: titles and texts for
 * each type, the reservation and cancellation notices, saving to and reading
 * from Firestore maps, the opt-out rule for pop-ups and the admin log filter.
 *
 * Used for US 01.03.01, US 01.03.02, US 01.05.02, US 01.07.02, US 01.10.01, US 03.08.01 and US 03.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class EntrantNotificationTest {

    private val reservation = Reservation(
        id = "res1",
        entrantId = "entrantA",
        lotteryId = "lot1",
        campsiteId = "jasperPines",
        startDate = "2027-05-14",
        endDate = "2027-05-16",
        createdAt = 1_800_000_000_000L,
        guests = 4
    )

    private fun note(type: String, sentAt: Long, message: String = "") =
        EntrantNotification(entrantId = "entrantA", campsiteId = "jasperPines", type = type, message = message, sentAt = sentAt)

    @Test
    fun title_isDifferentForEachType() {
        assertEquals("You won the lottery!", note(EntrantNotification.TYPE_WON, 1).title())
        assertEquals("Lottery result", note(EntrantNotification.TYPE_NOT_SELECTED, 1).title())
        assertEquals("Reservation confirmed", note(EntrantNotification.TYPE_RESERVED, 1).title())
        assertEquals("Reservation cancelled", note(EntrantNotification.TYPE_CANCELLED, 1).title())
        assertEquals("Notification", note("somethingNew", 1).title())
    }

    @Test
    fun text_usesSavedMessageIfThereIsOne() {
        val n = note(EntrantNotification.TYPE_WON, 1, "Pick your dates soon!")
        assertEquals("Pick your dates soon!", n.text())
    }

    @Test
    fun text_usesDefaultForWinAndLossFromTheDraw() {
        assertEquals(LotteryResultNotification.WON_MESSAGE, note(EntrantNotification.TYPE_WON, 1).text())
        assertEquals(LotteryResultNotification.NOT_SELECTED_MESSAGE, note(EntrantNotification.TYPE_NOT_SELECTED, 1).text())
    }

    @Test
    fun text_rejectedWithoutMessage_stillSaysWhatHappened() {
        // the owner's reject saves no message, so a default one is shown
        assertEquals("The campsite owner cancelled your reservation.", note(EntrantNotification.TYPE_REJECTED, 1).text())
    }

    @Test
    fun forReservation_hasTheDatesAndEntrant() {
        val n = EntrantNotification.forReservation(reservation, 5L)
        assertEquals("entrantA", n.entrantId)
        assertEquals("jasperPines", n.campsiteId)
        assertEquals(EntrantNotification.TYPE_RESERVED, n.type)
        assertTrue(n.text().contains("2027-05-14"))
        assertTrue(n.text().contains("2027-05-16"))
        assertEquals(5L, n.sentAt)
    }

    @Test
    fun forCancellation_confirmsTheCancel() {
        val n = EntrantNotification.forCancellation(reservation, 9L)
        assertEquals(EntrantNotification.TYPE_CANCELLED, n.type)
        assertTrue(n.text().contains("cancelled"))
    }

    @Test
    fun toMapThenFromMap_givesTheSameNotification() {
        val n = EntrantNotification(
            id = "n1", entrantId = "entrantA", campsiteId = "jasperPines",
            type = EntrantNotification.TYPE_OWNER_MESSAGE, message = "Bring bug spray!",
            sentAt = 1_800_000_000_000L, senderId = "ownerB"
        )
        val back = EntrantNotification.fromMap("n1", EntrantNotification.toMap(n))
        assertEquals(n, back)
    }

    @Test
    fun fromMap_readsAnOldDrawNotification() {
        // the draw saves more values (lotteryId, offerStatus); they are ignored here
        val data = mapOf<String, Any>(
            "entrantId" to "entrantA", "lotteryId" to "lot1", "campsiteId" to "jasperPines",
            "type" to "won", "message" to "You won!", "sentAt" to 1_800_000_000_000L, "offerStatus" to "pending"
        )
        val n = EntrantNotification.fromMap("n2", data)
        assertEquals("won", n.type)
        assertEquals("", n.senderId)
        assertFalse(n.isOwnerMessage())
    }

    @Test
    fun timeText_isBlankForZero() {
        assertEquals("", EntrantNotification.timeText(0L))
    }

    @Test
    fun toPopUp_onlyGivesNewOnesOldestFirst() {
        val list = listOf(note("won", 300), note("reserved", 100), note("cancelled", 200))
        val result = NotificationHelper.toPopUp(list, lastShown = 150, optedOut = false)
        assertEquals(listOf(200L, 300L), result.map { it.sentAt })
    }

    @Test
    fun toPopUp_givesNothingWhenOptedOut() {
        val list = listOf(note("won", 300), note("notSelected", 400))
        assertTrue(NotificationHelper.toPopUp(list, lastShown = 0, optedOut = true).isEmpty())
    }

    @Test
    fun ownerMessagesOnly_keepsOnlyOwnerMessagesNewestFirst() {
        val list = listOf(
            note(EntrantNotification.TYPE_OWNER_MESSAGE, 100, "Gate code is 4521"),
            note(EntrantNotification.TYPE_WON, 200),
            note(EntrantNotification.TYPE_OWNER_MESSAGE, 300, "Fire ban this weekend")
        )
        val result = NotificationHelper.ownerMessagesOnly(list)
        assertEquals(listOf("Fire ban this weekend", "Gate code is 4521"), result.map { it.message })
    }

    @Test
    fun nameOrId_fallsBackToTheId() {
        val names = mapOf("entrantA" to "Priya Singh", "ownerB" to "")
        assertEquals("Priya Singh", NotificationHelper.nameOrId(names, "entrantA"))
        assertEquals("ownerB", NotificationHelper.nameOrId(names, "ownerB"))
        assertEquals("-", NotificationHelper.nameOrId(names, ""))
    }
}
