package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Unit tests for LotterySettings, the values behind LotterySettingsForm
 * (US 02.04.03, US 02.04.04, US 02.04.05, US 02.04.07).
 */
class LotterySettingsFormTest {

    private val edmonton = ZoneId.of("America/Edmonton")

    // what a Material date picker gives for a day: midnight UTC in milliseconds
    private fun picked(day: String): Long =
        LocalDate.parse(day).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // 9 AM in Edmonton on a day, as "now"
    private fun morningOf(day: String): Long =
        LocalDate.parse(day).atTime(9, 0).atZone(edmonton).toInstant().toEpochMilli()

    private val good = LotterySettings(
        registrationStart = picked("2026-11-10"),
        registrationEnd = picked("2026-11-17"),
        bookingStart = picked("2026-12-01"),
        bookingEnd = picked("2026-12-05"),
        numberOfWinners = "3",
        waitlistCapacity = "50"
    )

    @Test
    fun goodSettingsHaveNoError() {
        assertNull(good.firstError(morningOf("2026-11-02"), edmonton))
    }

    @Test
    fun registrationRunsFromStartOfFirstDayToEndOfLastDay() {
        val lottery = good.toLottery("camp1", zone = edmonton)
        assertEquals(
            LocalDate.parse("2026-11-10").atStartOfDay(edmonton).toInstant().toEpochMilli(),
            lottery.startDate
        )
        assertEquals(
            LocalDate.parse("2026-11-18").atStartOfDay(edmonton).toInstant().toEpochMilli() - 1,
            lottery.endDate
        )
    }

    @Test
    fun registrationEndingTodayIsStillAllowed() {
        val today = good.copy(
            registrationStart = picked("2026-11-02"),
            registrationEnd = picked("2026-11-02"),
            bookingStart = null,
            bookingEnd = null
        )
        assertNull(today.firstError(morningOf("2026-11-02"), edmonton))
    }

    @Test
    fun toLotteryCopiesTheOtherFields() {
        val lottery = good.toLottery("camp1", zone = edmonton)
        assertEquals("camp1", lottery.campsiteId)
        assertEquals("", lottery.id)
        assertEquals(3, lottery.numberOfWinners)
        assertEquals(50, lottery.waitlistCapacity)
        assertEquals(picked("2026-12-01"), lottery.bookingStartDate)
        assertEquals(picked("2026-12-05"), lottery.bookingEndDate)
        assertTrue(lottery.waitingList.isEmpty())
    }

    @Test
    fun emptyLimitMeansNoLimit() {
        assertNull(good.copy(waitlistCapacity = "  ").toLottery("camp1").waitlistCapacity)
    }

    @Test
    fun noBookingPeriodIsAllowed() {
        val noBooking = good.copy(bookingStart = null, bookingEnd = null)
        assertNull(noBooking.firstError(morningOf("2026-11-02"), edmonton))
        assertEquals(0L, noBooking.toLottery("camp1").bookingStartDate)
    }

    @Test
    fun missingRegistrationIsAnError() {
        val missing = good.copy(registrationStart = null, registrationEnd = null)
        assertEquals(
            "Please set a registration period.",
            missing.firstError(morningOf("2026-11-02"), edmonton)
        )
    }

    @Test
    fun bookingBeforeRegistrationClosesIsAnError() {
        val early = good.copy(bookingStart = picked("2026-11-15"))
        assertEquals(
            "The booking period can't start before registration closes.",
            early.firstError(morningOf("2026-11-02"), edmonton)
        )
    }

    @Test
    fun bookingOnTheDayRegistrationClosesIsAllowed() {
        val sameDay = good.copy(bookingStart = picked("2026-11-17"))
        assertNull(sameDay.firstError(morningOf("2026-11-02"), edmonton))
    }

    @Test
    fun winnersMustBeAWholeNumber() {
        assertEquals(
            "The number of winners must be a whole number.",
            good.copy(numberOfWinners = "two").firstError(morningOf("2026-11-02"), edmonton)
        )
    }

    @Test
    fun emptyOrZeroWinnersIsAnError() {
        val now = morningOf("2026-11-02")
        assertEquals("Please pick at least 1 winner.", good.copy(numberOfWinners = "").firstError(now, edmonton))
        assertEquals("Please pick at least 1 winner.", good.copy(numberOfWinners = "0").firstError(now, edmonton))
    }

    @Test
    fun limitMustBeAWholeNumberOfAtLeastOne() {
        val now = morningOf("2026-11-02")
        assertEquals(
            "The waiting list limit must be a whole number, or leave it empty.",
            good.copy(waitlistCapacity = "lots").firstError(now, edmonton)
        )
        assertEquals(
            "The waiting list limit must be at least 1, or leave it empty.",
            good.copy(waitlistCapacity = "0").firstError(now, edmonton)
        )
    }

    @Test
    fun describeShowsPickedDaysOrNotSet() {
        assertEquals("Not set", LotterySettings.describe(null, null))
        assertEquals(
            "Nov 10, 2026 to Nov 17, 2026",
            LotterySettings.describe(picked("2026-11-10"), picked("2026-11-17"))
        )
    }
}
