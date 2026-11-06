package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for changing a Lottery to Firestore data and back
 * (LotteryRepository.toMap and fromMap), plus nameToShow.
 *
 * They don't use the real database, so they run on the computer.
 *
 * What they check:
 * - Every field comes back the same after toMap then fromMap.
 * - Older lotteries that are missing fields get safe defaults.
 * - Booking dates and a waiting list limit that aren't set are left out of the map.
 * - A waiting list limit of 0 or less means no limit.
 * - An entrant with no name is shown as "Entrant" plus the start of their id.
 *
 * Used for US 01.01.06, 01.02.01, 01.02.02, 02.04.02, 02.04.04, 02.04.07, 02.05.01 and 02.09.01.
 */
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
    @Test
    fun toMapAndFromMap_keepEveryCampsite() {
        val lottery = Lottery(
            id = "l1", campsiteId = "a", startDate = 1000L, endDate = 2000L,
            campsiteIds = listOf("a", "b")
        )

        val copy = LotteryRepository.fromMap("l1", LotteryRepository.toMap(lottery))

        assertEquals(listOf("a", "b"), copy.campsiteIds)
    }

    @Test
    fun fromMap_oldLotteryWithoutList_usesCampsiteId() {
        val map: Map<String, Any> = mapOf(
            "campsiteId" to "camp_001",
            "startDate" to 1000L,
            "endDate" to 2000L
        )

        val lottery = LotteryRepository.fromMap("l1", map)

        assertEquals(listOf("camp_001"), lottery.campsiteIds)
    }
}
