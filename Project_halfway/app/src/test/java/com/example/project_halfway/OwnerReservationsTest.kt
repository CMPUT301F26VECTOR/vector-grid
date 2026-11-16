package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the owner's reservation lists (US 02.07.01, 02.07.02, 02.07.03):
 * the status helpers on Reservation and the tab filter in OwnerReservationsScreen.
 * No Android or Firestore needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class OwnerReservationsTest {

    // "Today" for every test, so the results never depend on the real date
    private val today = "2026-07-15"

    // Makes a reservation with only the parts these tests care about
    private fun reservation(
        id: String,
        start: String,
        end: String,
        status: String = Reservation.STATUS_ACTIVE,
        cancelledAt: Long = 0L
    ) = Reservation(
        id = id, entrantId = "entrant_$id", lotteryId = "lottery1", campsiteId = "camp1",
        startDate = start, endDate = end, createdAt = 1L, status = status, cancelledAt = cancelledAt
    )

    private fun row(r: Reservation) = OwnerReservationRow(r, "Alex Smith", "Site 56A")

    @Test
    fun aStayEndingTodayIsStillActive() {
        val r = reservation("a", "2026-07-13", today)
        assertTrue(r.isActive(today))
        assertFalse(r.isPast(today))
    }

    @Test
    fun aStayThatEndedYesterdayIsPast() {
        val r = reservation("a", "2026-07-10", "2026-07-14")
        assertTrue(r.isPast(today))
        assertFalse(r.isActive(today))
    }

    @Test
    fun cancelledAndRejectedAreNeitherActiveNorPast() {
        val cancelled = reservation("a", "2026-08-01", "2026-08-03", Reservation.STATUS_CANCELLED)
        val rejected = reservation("b", "2026-06-01", "2026-06-03", Reservation.STATUS_REJECTED)
        assertTrue(cancelled.isCancelled())
        assertTrue(rejected.isCancelled())
        assertFalse(cancelled.isActive(today))
        assertFalse(rejected.isPast(today))
    }

    @Test
    fun anOldReservationWithNoStatusLoadsAsActive() {
        val r = Reservation.fromMap(
            "r1",
            mapOf(
                "entrantId" to "e1", "lotteryId" to "l1", "campsiteId" to "c1",
                "startDate" to "2026-08-01", "endDate" to "2026-08-03", "createdAt" to 5L
            )
        )
        assertEquals(Reservation.STATUS_ACTIVE, r.status)
        assertEquals(0L, r.cancelledAt)
        assertEquals(0, r.guests)
    }

    @Test
    fun statusCancelledAtAndGuestsSurviveASaveAndLoad() {
        val original = reservation("r1", "2026-08-01", "2026-08-03", Reservation.STATUS_REJECTED, 99L).copy(guests = 4)
        val loaded = Reservation.fromMap("r1", Reservation.toMap(original))
        assertEquals(original, loaded)
    }

    @Test
    fun eachTabShowsOnlyItsOwnReservationsInOrder() {
        val rows = listOf(
            row(reservation("later", "2026-09-01", "2026-09-03")),
            row(reservation("sooner", "2026-07-20", "2026-07-22")),
            row(reservation("old", "2026-05-01", "2026-05-03")),
            row(reservation("older", "2026-04-01", "2026-04-03")),
            row(reservation("cancelledFirst", "2026-08-01", "2026-08-03", Reservation.STATUS_CANCELLED, 100L)),
            row(reservation("cancelledLast", "2026-08-05", "2026-08-07", Reservation.STATUS_REJECTED, 200L))
        )

        val active = reservationsForTab(rows, ReservationTab.ACTIVE, today).map { it.reservation.id }
        val past = reservationsForTab(rows, ReservationTab.PAST, today).map { it.reservation.id }
        val cancelled = reservationsForTab(rows, ReservationTab.CANCELLED, today).map { it.reservation.id }

        assertEquals(listOf("sooner", "later"), active)
        assertEquals(listOf("old", "older"), past)
        assertEquals(listOf("cancelledLast", "cancelledFirst"), cancelled)
    }

    @Test
    fun cancelledCardsSayWhoCancelled() {
        assertEquals("Rejected by owner", cancelledByText(reservation("a", today, today, Reservation.STATUS_REJECTED)))
        assertEquals("Cancelled by entrant", cancelledByText(reservation("a", today, today, Reservation.STATUS_CANCELLED)))
    }
}
