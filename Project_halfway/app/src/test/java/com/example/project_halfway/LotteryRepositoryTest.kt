package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Tests that a lottery is turned into Firestore data and back correctly.
// Doesn't use the real database so it runs on the computer.

class LotteryRepositoryTest {

    @Test
    fun toMapThenFromMap_keepsValues() {
        val original = Lottery(
            id = "lottery_1",
            campsiteId = "camp_001",
            startDate = 1_780_000_000_000L,
            endDate = 1_780_600_000_000L,
            waitingList = listOf("entrant_a", "entrant_b")
        )

        val map = LotteryRepository.toMap(original)
        val copy = LotteryRepository.fromMap("lottery_1", map)

        assertEquals("lottery_1", copy.id)
        assertEquals("camp_001", copy.campsiteId)
        assertEquals(1_780_000_000_000L, copy.startDate)
        assertEquals(1_780_600_000_000L, copy.endDate)
        assertEquals(listOf("entrant_a", "entrant_b"), copy.waitingList)
    }

    @Test
    fun fromMap_missingWaitingList_givesEmptyList() {
        val map = mapOf<String, Any>(
            "campsiteId" to "camp_001",
            "startDate" to 1_780_000_000_000L,
            "endDate" to 1_780_600_000_000L
        )

        val lottery = LotteryRepository.fromMap("lottery_1", map)

        assertEquals(0, lottery.getWaitingListSize())
    }

    @Test
    fun toMapThenFromMap_keepsDrawResults() {
        // US 02.05.01: a drawn lottery must still be drawn after loading it again
        val original = Lottery(
            id = "lottery_3",
            campsiteId = "camp_001",
            startDate = 1_780_000_000_000L,
            endDate = 1_780_600_000_000L,
            waitingList = listOf("entrant_a", "entrant_b", "entrant_c"),
            numberOfWinners = 2,
            winners = listOf("entrant_c", "entrant_a"),
            notSelected = listOf("entrant_b"),
            isDrawn = true
        )

        val copy = LotteryRepository.fromMap("lottery_3", LotteryRepository.toMap(original))

        assertEquals(2, copy.numberOfWinners)
        assertEquals(listOf("entrant_c", "entrant_a"), copy.winners)
        assertEquals(listOf("entrant_b"), copy.notSelected)
        assertEquals(true, copy.isDrawn)
    }

    @Test
    fun fromMap_oldLotteryWithoutDrawFields_usesDefaults() {
        // lotteries added in the console before the draw existed
        val map = mapOf<String, Any>(
            "campsiteId" to "camp_001",
            "startDate" to 1_780_000_000_000L,
            "endDate" to 1_780_600_000_000L
        )

        val lottery = LotteryRepository.fromMap("lottery_1", map)

        assertEquals(1, lottery.numberOfWinners)
        assertEquals(false, lottery.isDrawn)
        assertEquals(0, lottery.winners.size)
    }

    @Test
    fun toMapThenFromMap_keepsBookingPeriodAndCapacity() {
        // US 02.04.04 and US 02.04.07
        val original = Lottery(
            id = "lottery_4",
            campsiteId = "camp_001",
            startDate = 1_780_000_000_000L,
            endDate = 1_780_600_000_000L,
            bookingStartDate = 1_781_000_000_000L,
            bookingEndDate = 1_781_500_000_000L,
            waitlistCapacity = 25
        )

        val copy = LotteryRepository.fromMap("lottery_4", LotteryRepository.toMap(original))

        assertEquals(1_781_000_000_000L, copy.bookingStartDate)
        assertEquals(1_781_500_000_000L, copy.bookingEndDate)
        assertEquals(25, copy.waitlistCapacity)
    }

    @Test
    fun toMap_unsetBookingAndCapacity_areLeftOut() {
        val lottery = Lottery("lottery_5", "camp_001", 1_780_000_000_000L, 1_780_600_000_000L)

        val map = LotteryRepository.toMap(lottery)

        assertEquals(false, map.containsKey("bookingStartDate"))
        assertEquals(false, map.containsKey("bookingEndDate"))
        assertEquals(false, map.containsKey("waitlistCapacity"))
    }

    @Test
    fun fromMap_oldLotteryWithoutSettings_hasNoBookingAndNoLimit() {
        val map = mapOf<String, Any>(
            "campsiteId" to "camp_001",
            "startDate" to 1_780_000_000_000L,
            "endDate" to 1_780_600_000_000L
        )

        val lottery = LotteryRepository.fromMap("lottery_1", map)

        assertEquals(0L, lottery.bookingStartDate)
        assertEquals(0L, lottery.bookingEndDate)
        assertNull(lottery.waitlistCapacity)
    }

    @Test
    fun fromMap_capacityZeroOrLess_meansNoLimit() {
        // a 0 typed in the Firestore console must not block everyone from joining
        for (bad in listOf(0L, -5L)) {
            val map = mapOf<String, Any>(
                "campsiteId" to "camp_001",
                "startDate" to 1_780_000_000_000L,
                "endDate" to 1_780_600_000_000L,
                "waitlistCapacity" to bad
            )
            assertNull(LotteryRepository.fromMap("lottery_1", map).waitlistCapacity)
        }
    }

    @Test
    fun nameToShow_blankName_usesStartOfId() {
        assertEquals("Entrant abc123", LotteryRepository.nameToShow("abc123xyz", ""))
        assertEquals("Entrant abc123", LotteryRepository.nameToShow("abc123xyz", null))
        assertEquals("Priya", LotteryRepository.nameToShow("abc123xyz", "Priya"))
    }
}
