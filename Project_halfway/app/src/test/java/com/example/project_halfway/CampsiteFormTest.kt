package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Tests for CampsiteForm, the checks behind the Register Campsite screen
// (US 02.01.01, US 02.01.02). No Android or Firestore needed.

class CampsiteFormTest {

    // A form filled in the way a real owner might, like our mockup's Site 56A.
    private fun goodForm(): CampsiteForm {
        return CampsiteForm(
            name = "Site 56A",
            campgroundName = "Campground A",
            location = "Edmonton, AB",
            description = "Quiet tent site near the lake",
            occupancy = "3",
            type = "Tent site",
            services = "Power, Non-potable water",
            tags = "Lakeside, Pet friendly",
            pricePerNight = "43.00",
            latitude = "53.5461",
            longitude = "-113.4938",
            availableDates = listOf(mapOf("start" to "2027-05-01", "end" to "2027-05-31"))
        )
    }

    @Test
    fun goodForm_hasNoError() {
        assertNull(goodForm().firstError())
    }

    @Test
    fun blankName_givesNameError() {
        val form = goodForm().copy(name = "  ")
        assertEquals("Please enter a campsite name.", form.firstError())
    }

    @Test
    fun blankLocation_givesLocationError() {
        val form = goodForm().copy(location = "")
        assertEquals("Please enter a location.", form.firstError())
    }

    @Test
    fun occupancyNotANumber_givesOccupancyError() {
        val form = goodForm().copy(occupancy = "three")
        assertEquals("Please enter an occupancy of 1 or more.", form.firstError())
    }

    @Test
    fun blankPrice_isAllowed() {
        assertNull(goodForm().copy(pricePerNight = "").firstError())
    }

    @Test
    fun negativePrice_givesPriceError() {
        val form = goodForm().copy(pricePerNight = "-5")
        assertEquals("Please enter a price of 0 or more.", form.firstError())
    }

    @Test
    fun onlyLatitude_givesBothError() {
        val form = goodForm().copy(longitude = "")
        assertEquals(
            "Please enter both latitude and longitude, or leave both empty.",
            form.firstError()
        )
    }

    @Test
    fun latitudeOutOfRange_givesLatitudeError() {
        val form = goodForm().copy(latitude = "120")
        assertEquals("Latitude must be a number from -90 to 90.", form.firstError())
    }

    @Test
    fun dateRangeEndingBeforeStart_givesDateError() {
        val form = goodForm().copy(
            availableDates = listOf(mapOf("start" to "2027-05-31", "end" to "2027-05-01"))
        )
        assertEquals("Each date range must end on or after its start date.", form.firstError())
    }

    @Test
    fun toCampsite_copiesEveryField() {
        val campsite = goodForm().toCampsite("owner_7")

        assertEquals("Site 56A", campsite.getCampsiteName())
        assertEquals("Campground A", campsite.getCampgroundName())
        assertEquals("Edmonton, AB", campsite.getCampsiteLocation())
        assertEquals("Quiet tent site near the lake", campsite.getDescription())
        assertEquals(3, campsite.getOccupancy())
        assertEquals("Tent site", campsite.getType())
        assertEquals(listOf("Power", "Non-potable water"), campsite.getServices())
        assertEquals(listOf("Lakeside", "Pet friendly"), campsite.getTags())
        assertEquals(43.0, campsite.getPricePerNight(), 0.001)
        assertEquals("owner_7", campsite.getOwnerId())
        assertEquals("", campsite.getId()) // Firestore picks the id when it saves
        assertEquals(1, campsite.availableDateRanges.size)
    }

    @Test
    fun toCampsite_putsLatitudeAndLongitudeTheRightWayRound() {
        val campsite = goodForm().toCampsite("owner_7")

        assertEquals(53.5461, campsite.getLatitude(), 0.0001)
        assertEquals(-113.4938, campsite.getLongitude(), 0.0001)
    }

    @Test
    fun toCampsite_blankOptionalNumbers_becomeZero() {
        val campsite = goodForm()
            .copy(pricePerNight = "", latitude = "", longitude = "")
            .toCampsite("owner_7")

        assertEquals(0.0, campsite.getPricePerNight(), 0.0)
        assertEquals(0.0, campsite.getLatitude(), 0.0)
        assertEquals(0.0, campsite.getLongitude(), 0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun toCampsite_withAnError_throws() {
        goodForm().copy(name = "").toCampsite("owner_7")
    }

    @Test
    fun splitList_trimsAndDropsEmptyItems() {
        assertEquals(listOf("Power", "Water"), CampsiteForm.splitList(" Power, ,Water ,"))
    }

    @Test
    fun dateRange_turnsCalendarMillisIntoDates() {
        // 2027-05-01 and 2027-05-31 at midnight UTC, the way the calendar gives them
        val range = CampsiteForm.dateRange(1_809_129_600_000L, 1_811_721_600_000L)

        assertEquals("2027-05-01", range["start"])
        assertEquals("2027-05-31", range["end"])
    }
}
