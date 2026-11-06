package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Unit tests for the logic added to finish the partly working half-way stories:
 * average ratings (US 01.01.04), editing a campsite (US 02.01.04), the booking
 * period check (US 02.04.04), draw notifications (US 02.05.01) and choosing a
 * lottery's waiting list (US 02.09.01). No Android or Firestore needed.
 */
class HalfwayFixesTest {

    private fun day(text: String): Long =
        LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    private fun campsite(ratings: List<Double> = emptyList(), lat: Double = 0.0, lng: Double = 0.0) = CampsiteModel(
        campsiteName = "Site 7",
        campgroundName = "Upper Waterton",
        location = "Waterton, AB",
        longitude = lng,
        latitude = lat,
        occupancy = 4,
        userRating = 0.0,
        description = "Lakeside tent site",
        imageUrl = "https://example.com/site7.jpg",
        id = "camp1",
        ownerId = "owner1",
        type = "Tent site",
        services = listOf("Power", "Water"),
        pricePerNight = 45.0,
        availableDateRanges = listOf(mapOf("start" to "2027-06-18", "end" to "2027-06-25")),
        tags = listOf("Lakeside"),
        ratings = ratings
    )

    // A drawn lottery: e1 won, e2 and e3 were not selected; booking Nov 10 to Nov 20
    private fun drawnLottery() = Lottery(
        id = "lot1",
        campsiteId = "camp1",
        startDate = day("2026-11-01"),
        endDate = day("2026-11-05"),
        waitingList = listOf("e1", "e2", "e3"),
        numberOfWinners = 1,
        winners = listOf("e1"),
        notSelected = listOf("e2", "e3"),
        isDrawn = true,
        bookingStartDate = day("2026-11-10"),
        bookingEndDate = day("2026-11-20")
    )

    // US 01.01.04: rating is the average of the entrants' ratings

    @Test
    fun averageRating_isTheMeanOfTheRatings() {
        val c = campsite(ratings = listOf(5.0, 4.0, 3.0))
        assertEquals(4.0, c.getAverageRating(), 0.0001)
        assertEquals(3, c.getRatingCount())
    }

    @Test
    fun averageRating_isZeroWithNoRatings_soCardSaysNoRatingsYet() {
        val c = campsite()
        assertEquals(0.0, c.getAverageRating(), 0.0)
        assertEquals("No ratings yet", ratingText(c.getAverageRating()))
    }

    @Test
    fun ratings_surviveSavingAndLoading() {
        val c = campsite(ratings = listOf(5.0, 4.5))
        val copy = CampsiteRepository.fromMap("camp1", CampsiteRepository.toMap(c))
        assertEquals(listOf(5.0, 4.5), copy.getRatings())
        assertEquals(4.75, copy.getAverageRating(), 0.0001)
    }

    // US 02.01.04: editing a campsite

    @Test
    fun fromCampsite_fillsTheFormIn() {
        val form = CampsiteForm.fromCampsite(campsite(lat = 49.05, lng = -113.9))
        assertEquals("Site 7", form.name)
        assertEquals("4", form.occupancy)
        assertEquals("Power, Water", form.services)
        assertEquals("45.0", form.pricePerNight)
        assertEquals("49.05", form.latitude)
        assertEquals("-113.9", form.longitude)
    }

    @Test
    fun fromCampsite_leavesPinEmptyWithNoMapLocation() {
        val form = CampsiteForm.fromCampsite(campsite())
        assertEquals("", form.latitude)
        assertEquals("", form.longitude)
    }

    @Test
    fun toCampsite_whenEditing_keepsIdPhotoAndRatings() {
        val original = campsite(ratings = listOf(4.0))
        val edited = CampsiteForm.fromCampsite(original).copy(name = "Site 7B").toCampsite("owner1", original)
        assertEquals("camp1", edited.getId())
        assertEquals("Site 7B", edited.getCampsiteName())
        assertEquals("https://example.com/site7.jpg", edited.getImageUrl())
        assertEquals(listOf(4.0), edited.getRatings())
    }

    @Test
    fun toCampsite_forANewCampsite_hasNoId() {
        val new = CampsiteForm.fromCampsite(campsite()).toCampsite("owner1")
        assertEquals("", new.getId())
        assertEquals("", new.getImageUrl())
    }

    // US 02.04.04: winners can only reserve inside the booking period

    @Test
    fun stayInsideBookingPeriod_isAllowed() {
        assertTrue(drawnLottery().isWithinBookingPeriod(day("2026-11-10"), day("2026-11-20")))
        assertTrue(drawnLottery().isWithinBookingPeriod(day("2026-11-12"), day("2026-11-14")))
    }

    @Test
    fun stayOutsideBookingPeriod_isRefused() {
        assertFalse(drawnLottery().isWithinBookingPeriod(day("2026-11-09"), day("2026-11-12")))
        assertFalse(drawnLottery().isWithinBookingPeriod(day("2026-11-18"), day("2026-11-21")))
    }

    @Test
    fun stayEndingBeforeItStarts_isRefused() {
        assertFalse(drawnLottery().isWithinBookingPeriod(day("2026-11-14"), day("2026-11-12")))
    }

    @Test
    fun bookableDay_onlyInsideBookingPeriod() {
        assertTrue(drawnLottery().isBookableDay(day("2026-11-15")))
        assertFalse(drawnLottery().isBookableDay(day("2026-11-21")))
    }

    @Test
    fun noBookingPeriod_allowsAnyStay() {
        val open = Lottery("lot2", "camp1", day("2026-11-01"), day("2026-11-05"))
        assertTrue(open.isWithinBookingPeriod(day("2027-01-01"), day("2027-01-03")))
        assertEquals("any dates", WaitlistHelper.bookingPeriodText(open))
    }

    @Test
    fun bookingPeriodText_showsBothDays() {
        assertEquals("2026-11-10 to 2026-11-20", WaitlistHelper.bookingPeriodText(drawnLottery()))
    }

    @Test
    fun isWinner_onlyForWinnersAfterTheDraw() {
        assertTrue(drawnLottery().isWinner("e1"))
        assertFalse(drawnLottery().isWinner("e2"))
        val notDrawn = Lottery("lot3", "camp1", 0L, 1L, waitingList = listOf("e1"))
        assertFalse(notDrawn.isWinner("e1"))
    }

    @Test
    fun reservation_survivesSavingAndLoading() {
        val r = Reservation("", "e1", "lot1", "camp1", "2026-11-12", "2026-11-14", 99L)
        val copy = Reservation.fromMap("res1", Reservation.toMap(r))
        assertEquals(r.copy(id = "res1"), copy)
        assertEquals("Reserved: 2026-11-12 to 2026-11-14", WaitlistHelper.reservedText(copy))
    }

    // US 02.05.01: winners and non-winners are notified

    @Test
    fun forDraw_notifiesEveryWinnerAndNonWinner() {
        val notes = LotteryResultNotification.forDraw(drawnLottery(), 123L)
        assertEquals(3, notes.size)
        assertEquals(listOf("e1"), notes.filter { it.isWin() }.map { it.entrantId })
        assertEquals(listOf("e2", "e3"), notes.filter { !it.isWin() }.map { it.entrantId })
        assertTrue(notes.all { it.lotteryId == "lot1" && it.campsiteId == "camp1" && it.sentAt == 123L })
    }

    @Test
    fun forDraw_isEmptyBeforeTheDraw() {
        val notDrawn = Lottery("lot3", "camp1", 0L, 1L, waitingList = listOf("e1"))
        assertTrue(LotteryResultNotification.forDraw(notDrawn, 1L).isEmpty())
    }

    @Test
    fun drawWinners_thenForDraw_coversEveryoneOnTheList() {
        val lottery = Lottery("lot4", "camp1", 0L, 10L, waitingList = listOf("a", "b", "c", "d"), numberOfWinners = 2)
        assertTrue(lottery.drawWinners(now = 11L))
        val notes = LotteryResultNotification.forDraw(lottery, 11L)
        assertEquals(setOf("a", "b", "c", "d"), notes.map { it.entrantId }.toSet())
        assertEquals(2, notes.count { it.isWin() })
    }

    @Test
    fun notification_survivesSavingAndLoading() {
        val n = LotteryResultNotification.forDraw(drawnLottery(), 5L).first()
        val copy = LotteryResultNotification.fromMap("n1", LotteryResultNotification.toMap(n))
        assertEquals(n.copy(id = "n1"), copy)
    }

    // US 02.09.01: the owner picks which lottery's waiting list to see

    @Test
    fun newestFirst_putsTheLatestClosingLotteryFirst() {
        val older = Lottery("old", "camp1", day("2026-10-01"), day("2026-10-10"))
        val newer = Lottery("new", "camp1", day("2026-11-01"), day("2026-11-21"))
        assertEquals(listOf("new", "old"), newestFirst(listOf(older, newer)).map { it.id })
    }

    @Test
    fun lotteryLabel_showsTheRegistrationPeriod() {
        val lottery = Lottery("x", "camp1", day("2026-11-01"), day("2026-11-21"))
        assertEquals("Nov 1 to Nov 21, 2026", lotteryLabel(lottery, ZoneId.of("UTC")))
    }
}
