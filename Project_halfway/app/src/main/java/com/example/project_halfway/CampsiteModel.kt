package com.example.project_halfway

/**
 * Model for one campsite listing.
 * Holds the campsite's data, read through the getter functions.
 * Latitude and longitude come from the owner's map pin (US 02.01.02).
 * Saving and loading is done in CampsiteRepository.
 *
 * Outstanding issues: image upload is not built yet.
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
    val availableDateRanges: List<Map<String, String>> = emptyList(),

    // Short labels the owner picks, e.g. "Lakeside", "Pet friendly" (US 02.01.02).
    private val tags: List<String> = emptyList()
) {
    /** Returns the average rating given by users. */
    public fun getUserRating() : Double {
        return userRating
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
/*
    /** Returns the available date ranges for an entrant to register Empty if none. */
    public fun getAvailableDateRanges() : List<Map<String, String>> {
        return availableDateRanges
    }

 */

}