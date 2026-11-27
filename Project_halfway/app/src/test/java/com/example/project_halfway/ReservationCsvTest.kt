package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for ReservationCsv: the header, one row per reservation, cancelled
 * reservations showing their status, and values with commas or quotes.
 *
 * Used for US 01.05.02 and US 01.07.02.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */
class ReservationCsvTest {

    private val active = Reservation(
        id = "r1", entrantId = "entrantA", lotteryId = "lot1", campsiteId = "jasper",
        startDate = "2027-05-14", endDate = "2027-05-16", createdAt = 0L, guests = 4
    )

    private val cancelled = Reservation(
        id = "r2", entrantId = "entrantA", lotteryId = "lot2", campsiteId = "banff",
        startDate = "2027-06-01", endDate = "2027-06-03", createdAt = 0L,
        status = Reservation.STATUS_CANCELLED, guests = 2
    )

    private val names = mapOf("jasper" to "Jasper Pines", "banff" to "Tunnel Mountain, Banff")

    @Test
    fun build_startsWithTheHeader() {
        val lines = ReservationCsv.build(listOf(active), names).trim().lines()
        assertEquals(ReservationCsv.HEADER, lines[0])
        assertEquals(2, lines.size)
    }

    @Test
    fun build_withNoReservations_isOnlyTheHeader() {
        assertEquals(ReservationCsv.HEADER + "\n", ReservationCsv.build(emptyList(), names))
    }

    @Test
    fun row_hasTheReservationDetails() {
        assertEquals("Jasper Pines,2027-05-14,2027-05-16,4,active,,", ReservationCsv.row(active, "Jasper Pines"))
    }

    @Test
    fun build_showsCancelledStatus_andQuotesNamesWithCommas() {
        val csv = ReservationCsv.build(listOf(cancelled, active), names)
        assertTrue(csv.contains("\"Tunnel Mountain, Banff\",2027-06-01,2027-06-03,2,cancelled"))
    }

    @Test
    fun build_sortsByStartDate() {
        val lines = ReservationCsv.build(listOf(cancelled, active), names).trim().lines()
        assertTrue(lines[1].startsWith("Jasper Pines"))
    }

    @Test
    fun build_usesTheIdIfTheNameIsMissing() {
        val csv = ReservationCsv.build(listOf(active), emptyMap())
        assertTrue(csv.contains("jasper,2027-05-14"))
    }

    @Test
    fun clean_doublesQuotes() {
        assertEquals("\"Bob's \"\"Best\"\" Site\"", ReservationCsv.clean("Bob's \"Best\" Site"))
        assertEquals("Plain", ReservationCsv.clean("Plain"))
    }

    @Test
    fun fileName_hasTheDate() {
        assertEquals("my_reservations_2026-11-23.csv", ReservationCsv.fileName("2026-11-23"))
    }
}
