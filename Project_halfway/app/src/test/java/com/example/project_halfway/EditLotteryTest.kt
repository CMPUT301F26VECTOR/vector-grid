package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Unit tests for editing a lottery (US 02.04.06): filling the form in from a
 * saved lottery, the checks before saving, and which fields get written.
 * No Android or Firestore needed.
 *
 * **AI assistance (Samarth Sathya Karthik):** these tests were implemented using
 * Claude AI; Samarth ran them and checked that they pass.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class EditLotteryTest {

    private val zone: ZoneId = ZoneId.of("America/Edmonton")

    // Midnight UTC of a day, which is what the date picker gives
    private fun picked(day: String) = LocalDate.parse(day).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    // A time long before every date below, used as "now"
    private val now = picked("2026-01-01")

    private val settings = LotterySettings(
        registrationStart = picked("2026-07-01"),
        registrationEnd = picked("2026-07-10"),
        bookingStart = picked("2026-08-01"),
        bookingEnd = picked("2026-08-31"),
        numberOfWinners = "3",
        waitlistCapacity = "20"
    )

    private fun lottery(drawn: Boolean = false, waiting: List<String> = emptyList()): Lottery {
        val base = settings.toLottery("camp1", id = "lot1", zone = zone, campsiteIds = listOf("camp1", "camp2"))
        return Lottery(
            id = base.id, campsiteId = base.campsiteId, startDate = base.startDate, endDate = base.endDate,
            waitingList = waiting, numberOfWinners = base.numberOfWinners, isDrawn = drawn,
            bookingStartDate = base.bookingStartDate, bookingEndDate = base.bookingEndDate,
            waitlistCapacity = base.waitlistCapacity, campsiteIds = base.campsiteIds
        )
    }

    @Test
    fun theFormStartsWithTheLotterysSavedValues() {
        assertEquals(settings, lotterySettingsFrom(lottery(), zone))
    }

    @Test
    fun aLotteryWithNoBookingPeriodOrLimitLeavesThoseEmpty() {
        val plain = Lottery("lot2", "camp1", lottery().startDate, lottery().endDate)
        val form = lotterySettingsFrom(plain, zone)
        assertNull(form.bookingStart)
        assertEquals("", form.waitlistCapacity)
    }

    @Test
    fun aGoodEditHasNoError() {
        assertNull(editLotteryError(lottery(), settings.copy(numberOfWinners = "5"), listOf("camp1"), now, zone))
    }

    @Test
    fun aDrawnLotteryCantBeEdited() {
        assertEquals(DRAWN_MESSAGE, editLotteryError(lottery(drawn = true), settings, listOf("camp1"), now, zone))
    }

    @Test
    fun atLeastOneCampsiteMustStayTicked() {
        assertEquals("Please pick at least one campsite.", editLotteryError(lottery(), settings, emptyList(), now, zone))
    }

    @Test
    fun theUsualSettingsRulesStillApply() {
        assertEquals(
            "Please pick at least 1 winner.",
            editLotteryError(lottery(), settings.copy(numberOfWinners = "0"), listOf("camp1"), now, zone)
        )
    }

    @Test
    fun theLimitCantGoBelowThePeopleAlreadyWaiting() {
        val full = lottery(waiting = listOf("a", "b", "c"))
        val error = editLotteryError(full, settings.copy(waitlistCapacity = "2"), listOf("camp1"), now, zone)
        assertEquals("3 entrants are already waiting, so the limit can't be lower than that.", error)
    }

    @Test
    fun onlyTheSettingsAreWrittenNotTheWaitingListOrWinners() {
        val fields = lotteryChanges(settings.copy(waitlistCapacity = "", bookingStart = null, bookingEnd = null), listOf("camp2"), zone)
        assertEquals("camp2", fields["campsiteId"])
        assertEquals(listOf("camp2"), fields["campsiteIds"])
        assertEquals(3, fields["numberOfWinners"])
        // Cleared values are null, which updateLottery turns into "delete this field"
        assertNull(fields["waitlistCapacity"])
        assertNull(fields["bookingStartDate"])
        assertEquals(false, fields.containsKey("waitingList"))
        assertEquals(false, fields.containsKey("winners"))
    }
}
