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
    val pricePerNight: Double = 0.0
) {

    public fun getUserRating() : Double {
        return userRating
    }

    public fun getCampsiteName() : String {
        return campsiteName
    }

    public fun getCampsiteLocation() : String {
        return location
    }

    public fun getCampgroundName() : String {
        return campgroundName
    }

    public fun getOccupancy() : Int {
        return occupancy
    }

    public fun getDescription() : String {
        return description
    }

    public fun getImageUrl() : String {
        return imageUrl
    }

    public fun getLatitude() : Double {
        return latitude
    }

    public fun getLongitude() : Double {
        return longitude
    }
}