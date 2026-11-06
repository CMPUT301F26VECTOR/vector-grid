package com.example.project_halfway

/**
 * Model for one campsite listing.
 * Holds the campsite's data, read through the getter functions.
 * Latitude and longitude come from the pin the owner drops on a Google Map
 * (US 02.01.02); both are 0.0 when there is no pin (see hasMapLocation).
 * Saving and loading is done in CampsiteRepository.
 *
 * Outstanding issues: image upload is not built yet, and entrants can't give
 * a rating yet (US 01.13.01), so ratings only come from Firestore.
 *
 * @property availableDateRanges the dates the campsite can be booked, as maps
 * with "start" and "end" in YYYY-MM-DD form (not lottery registration dates)
 */
class CampsiteModel (
    private val campsiteName : String,
    private val campgroundName : String,
    private val location : String, // City, county, etc.
    private val longitude: Double,
    private val latitude: Double,
    private val occupancy: Int,
    private val userRating: Double = 0.0,
    private val description: String,
    private val imageUrl : String,

    // New fields. Each has a default so existing code that creates a
    // CampsiteModel with the original nine values still compiles.
    private val id: String = "",
    private val ownerId: String = "",
    private val type: String = "",
    private val services: List<String> = emptyList(),
    private val pricePerNight: Double = 0.0,

    // Available campsite dates, not lottery registration dates.
    private val availableDateRanges: List<Map<String, String>> = emptyList(),

    // Short labels the owner picks, e.g. "Lakeside", "Pet friendly" (US 02.01.02).
    private val tags: List<String> = emptyList(),

    // Every rating entrants have given, from 1 to 5 stars (US 01.01.04, 01.13.01).
    private val ratings: List<Double> = emptyList()
) {
    /**
     * Returns the single rating number stored on the campsite. Older campsites
     * were made with one; the cards now show getAverageRating() instead.
     */
    public fun getUserRating() : Double {
        return userRating
    }

    /** Returns every rating entrants have given this campsite. Empty if none. */
    public fun getRatings() : List<Double> {
        return ratings
    }

    /** Returns how many entrants have rated this campsite. */
    public fun getRatingCount() : Int {
        return ratings.size
    }

    /**
     * Returns the average of the entrants' ratings (US 01.01.04), or 0.0 when
     * nobody has rated the campsite yet. The summary card and the details page
     * both use this, so they always show the same rating.
     */
    public fun getAverageRating() : Double {
        if (ratings.isEmpty()) return 0.0
        return ratings.sum() / ratings.size
    }
    /** Returns the campsite's name, for example "Site 56A". */
    public fun getCampsiteName() : String {
        return campsiteName
    }
    /** Returns the town or area where the campsite is, for example "Edmonton, AB". */
    public fun getCampsiteLocation() : String {
        return location
    }
    /** Returns the name of the campground this campsite belongs to. */

    public fun getCampgroundName() : String {
        return campgroundName
    }
    /** Returns the maximum number of people the campsite holds. */
    public fun getOccupancy() : Int {
        return occupancy
    }
    /** Returns the owner's description of the campsite. */
    public fun getDescription() : String {
        return description
    }
    /** Returns the link to the campsite's picture, or an empty string if there is none yet. */
    public fun getImageUrl() : String {
        return imageUrl
    }
    /** Returns the latitude of the campsite's map pin. It is 0.0 until the owner sets the pin. */
    public fun getLatitude() : Double {
        return latitude
    }
    /** Returns the longitude of the campsite's map pin. It is 0.0 until the owner sets the pin. */
    public fun getLongitude() : Double {
        return longitude
    }

    //Added extra getters for detailed view
    /** Returns the campsite's Firestore document id, or "" if it hasn't been saved yet. */
    public fun getId() : String {
        return id
    }

    /** Returns the user id of the owner who listed the campsite. */
    public fun getOwnerId() : String {
        return ownerId
    }

    /** Returns the kind of site, for example "Tent site". */
    public fun getType() : String {
        return type
    }

    /** Returns the services the site offers, for example "Power" or "Non-potable water". */
    public fun getServices() : List<String> {
        return services
    }

    /** Returns the price per night in dollars. It is 0.0 if the owner didn't set one. */
    public fun getPricePerNight() : Double {
        return pricePerNight
    }

    /** Returns the owner's tags for this campsite, e.g. "Lakeside". Empty if none. */
    public fun getTags() : List<String> {
        return tags
    }

    /** Returns a string that contains all the ranges in which a campsite is available to be
     * booked. This depends on the map keys in availableDateRanges being called "start"
     * and "end". Returns "No available dates found." if list is empty. */
    public fun getAvailableDateRangesString() : String {
        if (availableDateRanges.isEmpty()) return "No available dates found."
        return availableDateRanges.joinToString(", ") { "${it["start"]} - ${it["end"]}" }
    }

    /** Returns availableDateRanges attribute. */
    public fun getAvailableDateRanges() : List<Map<String, String>> {
        return availableDateRanges
    }

    /**
     * Tells whether the owner has put this campsite on the map. A campsite saved
     * without a map pin has latitude and longitude both 0.0, so that counts as no
     * location (no real campsite is at 0, 0, which is in the Atlantic Ocean).
     *
     * @return true if the campsite has a map location
     */
    public fun hasMapLocation() : Boolean {
        return latitude != 0.0 || longitude != 0.0
    }

}
