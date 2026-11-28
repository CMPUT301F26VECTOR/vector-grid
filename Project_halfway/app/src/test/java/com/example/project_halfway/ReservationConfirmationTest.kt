package com.example.project_halfway

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for reservation confirmation (US 01.05.01).
 *
 */

class ReservationConfirmationTest {

    private fun createReservation(): Reservation {
        return Reservation(
            id = "reservation1",
            entrantId = "entrant1",
            lotteryId = "lottery1",
            campsiteId = "campsite1",
            startDate = "2026-12-10",
            endDate = "2026-12-12",
            createdAt = 1000L,
            guests = 2,
            rulesAgreed = true,
            totalCost = 135.0
        )
    }

    @Test
    fun reservationStoresGuestCount() {
        val reservation = createReservation()

        assertEquals(2, reservation.guests)
    }

    @Test
    fun reservationStoresTotalCost() {
        val reservation = createReservation()

        assertEquals(135.0, reservation.totalCost, 0.001)
    }

    @Test
    fun reservationDetailsSavedToMap() {
        val reservation = createReservation()
        val map = Reservation.toMap(reservation)

        assertEquals("campsite1", map["campsiteId"])
        assertEquals("2026-12-10", map["startDate"])
        assertEquals("2026-12-12", map["endDate"])
        assertEquals(2, map["guests"])
        assertEquals(135.0, map["totalCost"])
    }

    @Test
    fun reservationDetailsRetrievedFromMap() {
        val reservation = createReservation()
        val map = Reservation.toMap(reservation)

        val restored = Reservation.fromMap(
            reservation.id,
            map
        )

        assertEquals(reservation.id, restored.id)
        assertEquals(reservation.campsiteId, restored.campsiteId)
        assertEquals(reservation.startDate, restored.startDate)
        assertEquals(reservation.endDate, restored.endDate)
        assertEquals(reservation.guests, restored.guests)
        assertEquals(
            reservation.totalCost,
            restored.totalCost,
            0.001
        )
    }

    @Test
    fun activeReservationIsIncluded() {
        val reservation = createReservation()

        assertTrue(reservation.isActive("2026-12-01"))
    }

    @Test
    fun cancelledReservationIsExcluded() {
        val reservation = createReservation().copy(
            status = Reservation.STATUS_CANCELLED
        )

        assertFalse(reservation.isActive("2026-12-01"))
    }

    @Test
    fun pastReservationIsExcluded() {
        val reservation = createReservation()

        assertFalse(reservation.isActive("2026-12-15"))
    }
}