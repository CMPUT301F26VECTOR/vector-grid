package com.example.project_halfway

/**
 * Model for one campsite listing
 *
 * Holds the campsite's data and keeps a list of views to notify when it changes.
 *
 * Outstanding issues: notifyViews() is not implemented yet, and this class
 * does not extend ObservableModel yet.
 */
class CampsiteModel (
    private val campsiteName : String,
    private val campgroundName : String,
    private val longitude: Double,
    private val latitude: Double,
    private val occupancy: Int,
    private val userRating: Double,
    private val description: String,
    private val imageUrl : String,

    // New fields. Each has a default so existing code that creates a
    // CampsiteModel with the original eight values still compiles.
    val id: String = "",
    val ownerId: String = "",
    val locationName: String = "",
    val type: String = "",
    val services: List<String> = emptyList(),
    val pricePerNight: Double = 0.0
) {

    public fun getUserRating() : Double {
        return userRating
    }

    private val views = mutableListOf<CampsiteView>()

    fun addView(view : CampsiteView) {
        if (view !in views) {
            views.add(view)
        }
    }

    fun removeView(view : CampsiteView) {
        if (view in views) {
            views.remove(view)
        }
    }

    protected fun notifyViews(campsite : CampsiteModel) {
    }
}