package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

// Tests for the owner's lottery settings:
// US 02.04.03 registration period, US 02.04.04 booking period,
// US 02.04.05 number of winners, US 02.04.07 waiting list limit.
// Only uses the Lottery class, so it runs on the computer.

class LotterySettingsTest {

    private val oneDay = 24 * 60 * 60 * 1000L
    private val regStart = 1_780_000_000_000L
    private val regEnd = regStart + 7 * oneDay
    private val duringRegistration = regStart + oneDay
    private val beforeRegistration = regStart - oneDay

    // Checks settings as if it is the day before registration opens, in UTC,
    // so the tests don't depend on today's date or the computer's time zone.
    private fun errorOf(l: Lottery): String? = l.firstSettingsError(now = beforeRegistration, zone = ZoneOffset.UTC)

    // Midnight UTC of a day, the way the calendar picker gives dates.
    private fun calendarMillis(day: LocalDate): Long = day.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // A lottery with good settings; each test changes one thing.
    private fun lottery(
        startDate: Long = regStart,
        endDate: Long = regEnd,
        numberOfWinners: Int = 2,
        bookingStartDate: Long = regEnd + oneDay,
        bookingEndDate: Long = regEnd + 3 * oneDay,
        waitlistCapacity: Int? = 10,
        waitingList: List<String> = emptyList()
    ): Lottery {
        return Lottery(
            id = "lottery_1",
            campsiteId = "camp_001",
            startDate = startDate,
            endDate = endDate,
            waitingList = waitingList,
            numberOfWinners = numberOfWinners,
            bookingStartDate = bookingStartDate,
            bookingEndDate = bookingEndDate,
            waitlistCapacity = waitlistCapacity
        )
    }

    // ---- firstSettingsError ----

    @Test
    fun goodSettings_noError() {
        assertNull(errorOf(lottery()))
    }

    @Test
    fun noBookingPeriodAndNoLimit_noError() {
        // both are optional
        assertNull(errorOf(lottery(bookingStartDate = 0L, bookingEndDate = 0L, waitlistCapacity = null)))
    }

    @Test
    fun registrationNotSet_error() {
        assertEquals("Please set a registration period.", errorOf(lottery(startDate = 0L)))
    }

    @Test
    fun registrationEndsBeforeItStarts_error() {
        assertEquals(
            "Registration must end after it starts.",
            errorOf(lottery(endDate = regStart - oneDay))
        )
    }

    @Test
    fun onlyOneBookingDate_error() {
        assertEquals(
            "Please set both booking dates, or leave both empty.",
            errorOf(lottery(bookingEndDate = 0L))
        )
    }

    @Test
    fun bookingEndsBeforeItStarts_error() {
        assertEquals(
            "The booking period must end on or after it starts.",
            errorOf(lottery(bookingStartDate = regEnd + 3 * oneDay, bookingEndDate = regEnd + oneDay))
        )
    }

    @Test
    fun bookingStartsDuringRegistration_error() {
        assertEquals(
            "The booking period can't start before registration closes.",
            errorOf(lottery(bookingStartDate = duringRegistration))
        )
    }

    @Test
    fun zeroWinners_error() {
        assertEquals("Please pick at least 1 winner.", errorOf(lottery(numberOfWinners = 0)))
    }

    @Test
    fun zeroCapacity_error() {
        assertEquals(
            "The waiting list limit must be at least 1, or leave it empty.",
            errorOf(lottery(waitlistCapacity = 0))
        )
    }

    @Test
    fun capacitySmallerThanWinners_allowed() {
        // everyone on the list simply wins
        assertNull(errorOf(lottery(numberOfWinners = 5, waitlistCapacity = 3)))
    }

    @Test
    fun registrationAlreadyClosed_error() {
        val l = lottery()
        assertEquals(
            "Registration must close in the future.",
            l.firstSettingsError(now = regEnd + oneDay, zone = ZoneOffset.UTC)
        )
    }

    @Test
    fun bookingStartsOnTheDayRegistrationCloses_allowed() {
        val closeDay = LocalDate.of(2027, 6, 10)
        val l = lottery(
            endDate = closeDay.atTime(18, 0).toInstant(ZoneOffset.UTC).toEpochMilli(),
            bookingStartDate = calendarMillis(closeDay),
            bookingEndDate = calendarMillis(closeDay.plusDays(2))
        )
        assertNull(errorOf(l))
    }

    @Test
    fun bookingCheck_usesOwnersTimeZone() {
        // Registration closes 11:30 PM on June 10 in Edmonton, which is already June 11 in UTC.
        val edmonton = ZoneId.of("America/Edmonton")
        val close = ZonedDateTime.of(2027, 6, 10, 23, 30, 0, 0, edmonton).toInstant().toEpochMilli()
        val l = lottery(
            endDate = close,
            bookingStartDate = calendarMillis(LocalDate.of(2027, 6, 10)),
            bookingEndDate = calendarMillis(LocalDate.of(2027, 6, 12))
        )

        // For an owner in Edmonton, booking can start June 10 (the day registration closes)
        assertNull(l.firstSettingsError(now = beforeRegistration, zone = edmonton))
        // In UTC registration closes June 11, so June 10 would be too early
        assertEquals(
            "The booking period can't start before registration closes.",
            l.firstSettingsError(now = beforeRegistration, zone = ZoneOffset.UTC)
        )
    }

    // ---- waiting list limit (US 02.04.07) ----

    @Test
    fun noLimit_neverFull() {
        val l = lottery(waitlistCapacity = null, waitingList = List(500) { "entrant_$it" })
        assertFalse(l.isWaitlistFull())
        assertTrue(l.joinWaitingList("one_more", duringRegistration))
    }

    @Test
    fun join_refusedWhenFull() {
        val l = lottery(waitlistCapacity = 2)
        assertTrue(l.joinWaitingList("a", duringRegistration))
        assertTrue(l.joinWaitingList("b", duringRegistration))

        assertTrue(l.isWaitlistFull())
        assertFalse(l.joinWaitingList("c", duringRegistration))
        assertEquals(2, l.getWaitingListSize())
    }

    @Test
    fun leaveThenJoin_freesASpot() {
        val l = lottery(waitlistCapacity = 1, waitingList = listOf("a"))
        assertFalse(l.joinWaitingList("b", duringRegistration))

        l.leaveWaitingList("a")

        assertTrue(l.joinWaitingList("b", duringRegistration))
    }
}
