package com.example.project_halfway

/**
 * Model for one campsite listing.
 * Holds the campsite's data, read through the getter functions.
 * Latitude and longitude come from the owner's map pin (US 02.01.02).
 * Saving and loading is done in CampsiteRepository.
 *
 * Outstanding issues: image upload is not built yet.
 */
class CampsiteModel (
    private val campsiteName : String,
    private val campgroundName : String,
    private val location : String, // City, county, etc.
    private val longitude: Double,
    private val latitude: Double,
    private val occupancy: Int,
    private val userRating: Double,
    private val description: String,
    private val imageUrl : String,

    // New fields. Each has a default so existing code that creates a
    // CampsiteModel with the original nine values still compiles.
    val id: String = "",
    val ownerId: String = "",
    val type: String = "",
    val services: List<String> = emptyList(),
    
    val pricePerNight: Double = 0.0,
   // Available campsite dates, not lottery registration dates.
    val availableDateRanges: List<Map<String, String>> = emptyList()
   
    
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
}