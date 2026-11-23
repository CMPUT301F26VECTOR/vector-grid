package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * Unit tests for the booked dates calendar (US 02.06.01): which days are
 * booked, and how a month is laid out. No Android or Firestore needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class BookedDatesTest {

    private fun reservation(start: String, end: String, status: String = Reservation.STATUS_ACTIVE) = Reservation(
        entrantId = "e1", lotteryId = "l1", campsiteId = "c1",
        startDate = start, endDate = end, createdAt = 1L, status = status
    )

    @Test
    fun everyDayFromStartToEndIsBooked() {
        val days = bookedDays(listOf(reservation("2026-06-29", "2026-07-02")))
        assertEquals(setOf("2026-06-29", "2026-06-30", "2026-07-01", "2026-07-02"), days)
    }

    @Test
    fun cancelledReservationsFreeTheirDays() {
        val days = bookedDays(
            listOf(
                reservation("2026-06-01", "2026-06-02"),
                reservation("2026-06-10", "2026-06-11", Reservation.STATUS_CANCELLED)
            )
        )
        assertEquals(setOf("2026-06-01", "2026-06-02"), days)
    }

    @Test
    fun brokenDatesAreSkippedInsteadOfCrashing() {
        assertTrue(bookedDays(listOf(reservation("", "2026-06-02"))).isEmpty())
    }

    @Test
    fun aMonthStartsOnTheRightWeekday() {
        // July 1, 2026 is a Wednesday, so Sunday to Tuesday are empty
        val weeks = monthWeeks(YearMonth.of(2026, 7))
        assertNull(weeks[0][2])
        assertEquals(LocalDate.of(2026, 7, 1), weeks[0][3])
        assertTrue(weeks.all { it.size == 7 })
        assertEquals(31, weeks.flatten().count { it != null })
    }
}
