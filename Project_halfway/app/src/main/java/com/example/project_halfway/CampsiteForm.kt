package com.example.project_halfway

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Everything the owner has typed on the Register Campsite screen
 * (US 02.01.01, US 02.01.02).
 *
 * The screen keeps each field as plain text while the owner types. This class
 * checks that text and turns it into a CampsiteModel. It has no Android or
 * Firebase code in it, so it can be unit tested on the computer.
 *
 * It is also used to edit a saved campsite (US 02.01.04): fromCampsite fills
 * the form in, and toCampsite keeps the campsite's id, photo and ratings.
 *
 * The optional campsite image (US 02.01.02) is uploaded by
 * CampsiteImageUploader; the form only keeps the link it gives back.
 * canEdit makes sure an owner only edits their own campsites (US 02.01.03).
 *
 * Outstanding issues: none known.
 *
 * **AI assistance (Samarth Sathya Karthik):** the imageUrl field and canEdit
 * were implemented using Claude AI; Samarth tested them and checked that they work.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * @property name the campsite's name, e.g. "Site 56A" (required)
 * @property campgroundName the campground the site belongs to
 * @property location town or area, e.g. "Edmonton, AB" (required)
 * @property description the owner's description of the campsite
 * @property occupancy most people the site holds, typed as text (required, 1 or more)
 * @property type the kind of site, e.g. "Tent site"
 * @property services comma separated, e.g. "Power, Non-potable water"
 * @property tags comma separated, e.g. "Lakeside, Pet friendly"
 * @property pricePerNight price per night in dollars, typed as text (optional, 0 or more)
 * @property latitude map pin latitude, typed as text (optional, -90 to 90)
 * @property longitude map pin longitude, typed as text (optional, -180 to 180)
 * @property availableDates date ranges as maps with "start" and "end" in
 * YYYY-MM-DD form, the same shape CampSearch.filterByDates reads
 * @property imageUrl link to the uploaded campsite image (US 02.01.02), "" for
 * no image, or null to keep whatever image the campsite already has
 */
data class CampsiteForm(
    val name: String = "",
    val campgroundName: String = "",
    val location: String = "",
    val description: String = "",
    val occupancy: String = "",
    val type: String = "",
    val services: String = "",
    val tags: String = "",
    val pricePerNight: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val availableDates: List<Map<String, String>> = emptyList(),
    val imageUrl: String? = null
) {

    /**
     * Checks the form, top to bottom.
     *
     * @return the message to show for the first problem found,
     * or null if the form is good to save
     */
    fun firstError(): String? {
        // The first three messages are the ones RegisterCampsiteScreenTest expects.
        if (name.isBlank()) {
            return "Please enter a campsite name."
        }
        if (location.isBlank()) {
            return "Please enter a location."
        }
        val occupancyNumber = occupancy.trim().toIntOrNull()
        if (occupancyNumber == null || occupancyNumber < 1) {
            return "Please enter an occupancy of 1 or more."
        }

        // Price is optional, but if it is typed it has to be a real price.
        if (pricePerNight.isNotBlank()) {
            val price = pricePerNight.trim().toDoubleOrNull()
            if (price == null || price < 0) {
                return "Please enter a price of 0 or more."
            }
        }

        // The map location is optional, but it needs both numbers or neither.
        if (latitude.isNotBlank() || longitude.isNotBlank()) {
            if (latitude.isBlank() || longitude.isBlank()) {
                return "Please enter both latitude and longitude, or leave both empty."
            }
            val lat = latitude.trim().toDoubleOrNull()
            if (lat == null || lat < -90 || lat > 90) {
                return "Latitude must be a number from -90 to 90."
            }
            val lon = longitude.trim().toDoubleOrNull()
            if (lon == null || lon < -180 || lon > 180) {
                return "Longitude must be a number from -180 to 180."
            }
        }

        // Each date range must end on or after the day it starts.
        for (range in availableDates) {
            val start = range["start"]
            val end = range["end"]
            if (start == null || end == null || LocalDate.parse(end).isBefore(LocalDate.parse(start))) {
                return "Each date range must end on or after its start date."
            }
        }

        return null
    }

    /**
     * Builds the campsite to save. Only call this when firstError() is null.
     *
     * @param ownerId uid of the signed in campsite owner
     * @param existing the campsite being edited (US 02.01.04), or null for a new
     * one. Its id, photo and ratings are kept, since the form doesn't change them.
     * @return the campsite to save; a new one has no id yet (Firestore gives it one)
     */
    fun toCampsite(ownerId: String, existing: CampsiteModel? = null): CampsiteModel {
        check(firstError() == null) { "The form still has a problem: ${firstError()}" }

        return CampsiteModel(
            campsiteName = name.trim(),
            campgroundName = campgroundName.trim(),
            location = location.trim(),
            // Careful: CampsiteModel takes longitude BEFORE latitude, so we use names here.
            longitude = longitude.trim().toDoubleOrNull() ?: 0.0,
            latitude = latitude.trim().toDoubleOrNull() ?: 0.0,
            occupancy = occupancy.trim().toInt(),
            userRating = existing?.getUserRating() ?: 0.0, // no ratings yet for a new campsite
            description = description.trim(),
            // The new image if the owner uploaded or removed one, otherwise the old one
            imageUrl = imageUrl ?: existing?.getImageUrl() ?: "",
            id = existing?.getId() ?: "",
            ownerId = ownerId,
            type = type.trim(),
            services = splitList(services),
            pricePerNight = pricePerNight.trim().toDoubleOrNull() ?: 0.0,
            availableDateRanges = availableDates,
            tags = splitList(tags),
            ratings = existing?.getRatings() ?: emptyList()
        )
    }

    /**
     * Helpers for turning the screen's input (comma separated text, calendar
     * dates) into the values a CampsiteModel stores.
     */
    companion object {

        /**
         * Tells whether an owner may edit a campsite (US 02.01.03): only the
         * owner who registered it can.
         *
         * @param campsite the campsite to edit
         * @param ownerId user id of the signed in owner
         * @return true if ownerId is not blank and is the campsite's owner
         */
        fun canEdit(campsite: CampsiteModel, ownerId: String): Boolean {
            if (ownerId.isBlank()) return false
            return campsite.getOwnerId() == ownerId
        }

        /**
         * Fills the form in from a saved campsite, so the owner can edit it
         * (US 02.01.04). Numbers become text the way the owner would type them,
         * and a campsite without a map pin gets empty latitude and longitude.
         *
         * @param campsite the campsite to edit
         * @return the form with the campsite's current details
         */
        fun fromCampsite(campsite: CampsiteModel): CampsiteForm {
            val price = campsite.getPricePerNight()
            return CampsiteForm(
                name = campsite.getCampsiteName(),
                campgroundName = campsite.getCampgroundName(),
                location = campsite.getCampsiteLocation(),
                description = campsite.getDescription(),
                occupancy = campsite.getOccupancy().toString(),
                type = campsite.getType(),
                services = campsite.getServices().joinToString(", "),
                tags = campsite.getTags().joinToString(", "),
                pricePerNight = if (price > 0.0) price.toString() else "",
                latitude = if (campsite.hasMapLocation()) campsite.getLatitude().toString() else "",
                longitude = if (campsite.hasMapLocation()) campsite.getLongitude().toString() else "",
                availableDates = campsite.getAvailableDateRanges()
            )
        }

        /**
         * Splits comma separated text into a clean list.
         * "Power, ,Water " becomes ["Power", "Water"].
         */
        fun splitList(text: String): List<String> {
            return text.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }

        /**
         * Turns a date picked on the Material 3 calendar into YYYY-MM-DD.
         * The calendar gives midnight UTC in milliseconds, so we read it in UTC
         * (the same way BrowseCampsitesScreenUI does for the entrant filter).
         */
        fun millisToDate(millis: Long): String {
            return Instant.ofEpochMilli(millis)
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .toString()
        }

        /**
         * Builds one available date range in the shape Firestore and CampSearch use.
         */
        fun dateRange(startMillis: Long, endMillis: Long): Map<String, String> {
            return mapOf(
                "start" to millisToDate(startMillis),
                "end" to millisToDate(endMillis)
            )
        }
    }
}
