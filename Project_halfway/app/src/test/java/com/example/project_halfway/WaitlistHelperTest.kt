package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test

// Tests for the Waitlisted tab helpers (US 01.02.01, 01.02.02)

class WaitlistHelperTest {

    private val oneDay = 24 * 60 * 60 * 1000L

    // pretend "today" is this time so the tests always give the same answer
    private val today = 1_780_000_000_000L

    @Test
    fun waitlistsFor_onlyListsLotteriesTheEntrantJoined() {
        val joined = Lottery("lottery_1", "camp_001", today - oneDay, today + 3 * oneDay, listOf("parneet", "deepali"))
        val notJoined = Lottery("lottery_2", "camp_002", today - oneDay, today + 3 * oneDay, listOf("deepali"))

        val result = WaitlistHelper.waitlistsFor("parneet", listOf(joined, notJoined))

        assertEquals(1, result.size)
        assertEquals("lottery_1", result[0].id)
    }

    @Test
    fun waitlistsFor_skipsDrawnLotteries() {
        val drawn = Lottery("lottery_1", "camp_001", today - 5 * oneDay, today - oneDay, listOf("parneet"), isDrawn = true)

        val result = WaitlistHelper.waitlistsFor("parneet", listOf(drawn))

        assertEquals(0, result.size)
    }

    @Test
    fun waitlistsFor_closingSoonestFirst() {
        val later = Lottery("later", "camp_001", today - oneDay, today + 6 * oneDay, listOf("parneet"))
        val sooner = Lottery("sooner", "camp_002", today - oneDay, today + 2 * oneDay, listOf("parneet"))

        val result = WaitlistHelper.waitlistsFor("parneet", listOf(later, sooner))

        assertEquals("sooner", result[0].id)
        assertEquals("later", result[1].id)
    }

    @Test
    fun daysLeftText_examples() {
        val threeDays = Lottery("a", "camp_001", today - oneDay, today + 3 * oneDay)
        val oneDayLeft = Lottery("b", "camp_001", today - oneDay, today + oneDay + 1000)
        val closesToday = Lottery("c", "camp_001", today - oneDay, today + 1000)
        val closed = Lottery("d", "camp_001", today - 5 * oneDay, today - oneDay)

        assertEquals("Lottery closes in 3 days", WaitlistHelper.daysLeftText(threeDays, today))
        assertEquals("Lottery closes in 1 day", WaitlistHelper.daysLeftText(oneDayLeft, today))
        assertEquals("Lottery closes today", WaitlistHelper.daysLeftText(closesToday, today))
        assertEquals("Lottery has closed", WaitlistHelper.daysLeftText(closed, today))
    }
}
