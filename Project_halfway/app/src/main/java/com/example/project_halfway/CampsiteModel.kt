package com.example.project_halfway

class CampsiteModel (
    private val campsiteName : String,
    private val campgroundName : String,
    private val location : String, // City, county, etc.
    private val longitude: Double,
    private val latitude: Double,
    private val occupancy: Int,
    private val userRating: Double,
    private val description: String,
    private val imageUrl : String
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

}