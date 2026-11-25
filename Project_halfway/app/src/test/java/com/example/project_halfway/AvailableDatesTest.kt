package com.example.project_halfway

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Unit tests for entrant date availability.
 * US 01.04.01
 *
 * AI assistance (Deepali Ladhar):
 * OpenAI ChatGPT assisted with designing and
 * implementing the date availability test cases.
 *
 * OpenAI. (2026). ChatGPT [Large language model].
 * https://chatgpt.com/
 */
class AvailableDatesTest {

    private fun reservation(
        start: String,
        end: String,
        status: String = Reservation.STATUS_ACTIVE
    ) = Reservation(
        entrantId = "e1",
        lotteryId = "l1",
        campsiteId = "c1",
        startDate = start,
        endDate = end,
        createdAt = 1L,
        status = status
    )

    @Test
    fun availableDateCanBeSelected() {
        val reservations = listOf(
            reservation("2026-11-28", "2026-11-30")
        )

        assertTrue(
            isDateRangeAvailable(
                "2026-11-26", "2026-11-26", reservations
            )
        )
    }

    @Test
    fun bookedDateCannotBeSelected() {
        val reservations = listOf(
            reservation("2026-11-28", "2026-11-30")
        )

        assertFalse(
            isDateRangeAvailable(
                "2026-11-29", "2026-11-29", reservations
            )
        )
    }

    @Test
    fun overlappingDateRangeIsRejected() {
        val reservations = listOf(
            reservation("2026-11-28", "2026-11-30")
        )

        assertFalse(
            isDateRangeAvailable(
                "2026-11-27", "2026-11-29", reservations
            )
        )
    }

    @Test
    fun rangeContainingBookedDatesIsRejected() {
        val reservations = listOf(
            reservation("2026-11-28", "2026-11-30")
        )

        assertFalse(
            isDateRangeAvailable(
                "2026-11-26", "2026-12-02", reservations
            )
        )
    }

    @Test
    fun nonOverlappingRangeIsAvailable() {
        val reservations = listOf(
            reservation("2026-11-28", "2026-11-30")
        )

        assertTrue(
            isDateRangeAvailable(
                "2026-12-01", "2026-12-03", reservations
            )
        )
    }

    @Test
    fun cancelledReservationDoesNotBlockDates() {
        val reservations = listOf(
            reservation(
                "2026-11-28",
                "2026-11-30",
                Reservation.STATUS_CANCELLED
            )
        )

        assertTrue(
            isDateRangeAvailable(
                "2026-11-28", "2026-11-30", reservations
            )
        )
    }

    @Test
    fun availabilityChangesAfterNewReservation() {
        val reservations = mutableListOf<Reservation>()

        assertTrue(
            isDateRangeAvailable(
                "2026-11-28", "2026-11-30", reservations
            )
        )

        reservations.add(
            reservation("2026-11-28", "2026-11-30")
        )

        assertFalse(
            isDateRangeAvailable(
                "2026-11-28", "2026-11-30", reservations
            )
        )
    }

    @Test
    fun reversedDateRangeIsRejected() {
        assertFalse(
            isDateRangeAvailable(
                "2026-11-30", "2026-11-28", emptyList()
            )
        )
    }
}