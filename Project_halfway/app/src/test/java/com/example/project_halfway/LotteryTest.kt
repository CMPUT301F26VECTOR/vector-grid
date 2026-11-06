package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Lottery model.
 *
 * What they check:
 * - isActive: open only between the start and end date
 * - joinWaitingList: adds an entrant, but not twice, not when closed, and not with a blank id
 * - leaveWaitingList: removes only that entrant, and the entrant can join again later
 * - getDaysLeft: counts full days left, and 0 once closed
 * - removeCampsite: only before registration opens, and never the last campsite
 *
 * Used for US 01.01.06, 01.02.01, 01.02.02 and 02.04.02.
 */

class LotteryTest {

    private val oneDay = 24 * 60 * 60 * 1000L

    private val today = 1_780_000_000_000L

    private fun openLottery(): Lottery {
        return Lottery(
            id = "lottery_1",
            campsiteId = "camp_001",
            startDate = today - 2 * oneDay,
            endDate = today + 5 * oneDay
        )
    }

    // lottery that already ended
    private fun closedLottery(): Lottery {
        return Lottery(
            id = "lottery_2",
            campsiteId = "camp_001",
            startDate = today - 10 * oneDay,
            endDate = today - 3 * oneDay
        )
    }

    @Test
    fun isActive_trueWhileOpen() {
        assertTrue(openLottery().isActive(today))
    }

    @Test
    fun isActive_falseAfterItEnds() {
        assertFalse(closedLottery().isActive(today))
    }

    @Test
    fun isActive_falseBeforeItStarts() {
        val lottery = Lottery("lottery_3", "camp_001", today + oneDay, today + 7 * oneDay)
        assertFalse(lottery.isActive(today))
    }

    @Test
    fun join_addsEntrant() {
        val lottery = openLottery()

        assertTrue(lottery.joinWaitingList("entrant_parneet", today))
        assertTrue(lottery.isOnWaitingList("entrant_parneet"))
        assertEquals(1, lottery.getWaitingListSize())
    }

    @Test
    fun join_twice_onlyAddsOnce() {
        val lottery = openLottery()
        lottery.joinWaitingList("entrant_parneet", today)

        assertFalse(lottery.joinWaitingList("entrant_parneet", today))
        assertEquals(1, lottery.getWaitingListSize())
    }

    @Test
    fun join_closedLottery_notAllowed() {
        val lottery = closedLottery()

        assertFalse(lottery.joinWaitingList("entrant_parneet", today))
        assertEquals(0, lottery.getWaitingListSize())
    }

    @Test
    fun join_blankId_notAllowed() {
        val lottery = openLottery()
        assertFalse(lottery.joinWaitingList("", today))
    }

    @Test
    fun leave_removesEntrant() {
        val lottery = openLottery()
        lottery.joinWaitingList("entrant_parneet", today)
        lottery.joinWaitingList("entrant_deepali", today)

        assertTrue(lottery.leaveWaitingList("entrant_parneet"))
        assertFalse(lottery.isOnWaitingList("entrant_parneet"))
        assertTrue(lottery.isOnWaitingList("entrant_deepali"))
        assertEquals(1, lottery.getWaitingListSize())
    }

    @Test
    fun leave_whenNotOnList_returnsFalse() {
        val lottery = openLottery()
        assertFalse(lottery.leaveWaitingList("entrant_parneet"))
    }

    @Test
    fun canJoinAgainAfterLeaving() {
        val lottery = openLottery()
        lottery.joinWaitingList("entrant_parneet", today)
        lottery.leaveWaitingList("entrant_parneet")

        assertTrue(lottery.joinWaitingList("entrant_parneet", today))
    }

    @Test
    fun getDaysLeft_countsDays() {
        assertEquals(5, openLottery().getDaysLeft(today))
        assertEquals(0, closedLottery().getDaysLeft(today))
    }

    @Test
    fun removeCampsite_beforeRegistrationOpens_removesIt() {
        val lottery = Lottery(
            id = "l1", campsiteId = "a", startDate = 1000L, endDate = 2000L,
            campsiteIds = listOf("a", "b")
        )

        assertTrue(lottery.removeCampsite("b", now = 500L))
        assertEquals(listOf("a"), lottery.campsiteIds)
    }

    @Test
    fun removeCampsite_afterRegistrationOpens_isRefused() {
        val lottery = Lottery(
            id = "l1", campsiteId = "a", startDate = 1000L, endDate = 2000L,
            campsiteIds = listOf("a", "b")
        )

        assertFalse(lottery.removeCampsite("b", now = 1500L))
        assertEquals(listOf("a", "b"), lottery.campsiteIds)
    }

    @Test
    fun removeCampsite_lastOne_isRefused() {
        val lottery = Lottery(id = "l1", campsiteId = "a", startDate = 1000L, endDate = 2000L)

        assertFalse(lottery.removeCampsite("a", now = 500L))
        assertEquals(listOf("a"), lottery.campsiteIds)
    }
}
