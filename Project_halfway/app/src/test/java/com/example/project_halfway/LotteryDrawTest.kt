package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

// Tests for drawing lottery winners (US 02.05.01).
// Random(seed) makes the "random" picks the same every run, so tests are repeatable.

class LotteryDrawTest {

    private val oneDay = 24 * 60 * 60 * 1000L

    // pretend "today" is this time so the tests always give the same answer
    private val today = 1_780_000_000_000L

    private val fiveEntrants = listOf("ent_amy", "ent_ben", "ent_cara", "ent_dev", "ent_eli")

    // registration closed yesterday
    private fun closedLottery(waitingList: List<String>, winners: Int): Lottery {
        return Lottery(
            id = "lottery_1",
            campsiteId = "camp_001",
            startDate = today - 7 * oneDay,
            endDate = today - oneDay,
            waitingList = waitingList,
            numberOfWinners = winners
        )
    }

    @Test
    fun draw_whileRegistrationOpen_doesNothing() {
        val open = Lottery("lottery_2", "camp_001", today - oneDay, today + oneDay, fiveEntrants, 2)

        assertFalse(open.drawWinners(today, Random(1)))
        assertFalse(open.isDrawn)
        assertTrue(open.winners.isEmpty())
    }

    @Test
    fun draw_picksTheRightNumberOfWinners() {
        val lottery = closedLottery(fiveEntrants, winners = 2)

        assertTrue(lottery.drawWinners(today, Random(1)))

        assertTrue(lottery.isDrawn)
        assertEquals(2, lottery.winners.size)
        assertEquals(3, lottery.notSelected.size)
    }

    @Test
    fun draw_everyoneEndsUpInExactlyOneList() {
        val lottery = closedLottery(fiveEntrants, winners = 2)
        lottery.drawWinners(today, Random(7))

        // nobody is both a winner and not selected, and nobody is lost
        assertTrue(lottery.winners.none { it in lottery.notSelected })
        assertEquals(fiveEntrants.toSet(), (lottery.winners + lottery.notSelected).toSet())
    }

    @Test
    fun draw_fewerEntrantsThanSpots_everyoneWins() {
        val lottery = closedLottery(listOf("ent_amy", "ent_ben"), winners = 5)
        lottery.drawWinners(today, Random(1))

        assertEquals(setOf("ent_amy", "ent_ben"), lottery.winners.toSet())
        assertTrue(lottery.notSelected.isEmpty())
    }

    @Test
    fun draw_emptyWaitlist_isMarkedDrawnWithNoWinners() {
        val lottery = closedLottery(emptyList(), winners = 3)

        assertTrue(lottery.drawWinners(today, Random(1)))
        assertTrue(lottery.isDrawn)
        assertTrue(lottery.winners.isEmpty())
    }

    @Test
    fun draw_secondTime_doesNothing() {
        val lottery = closedLottery(fiveEntrants, winners = 2)
        lottery.drawWinners(today, Random(1))
        val firstWinners = lottery.winners.toList()

        assertFalse(lottery.drawWinners(today, Random(99)))
        assertEquals(firstWinners, lottery.winners)
    }

    @Test
    fun draw_sameSeed_givesSameWinners() {
        val a = closedLottery(fiveEntrants, winners = 2)
        val b = closedLottery(fiveEntrants, winners = 2)

        a.drawWinners(today, Random(42))
        b.drawWinners(today, Random(42))

        assertEquals(a.winners, b.winners)
    }

    @Test
    fun isDrawDue_onlyAfterRegistrationCloses() {
        val lottery = closedLottery(fiveEntrants, winners = 2)

        assertFalse(lottery.isDrawDue(today - 3 * oneDay)) // still open then
        assertTrue(lottery.isDrawDue(today))
    }
}
