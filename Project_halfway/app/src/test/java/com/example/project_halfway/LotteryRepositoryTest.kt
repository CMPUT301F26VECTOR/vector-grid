package com.example.project_halfway

import org.junit.Assert.assertEquals
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
}
