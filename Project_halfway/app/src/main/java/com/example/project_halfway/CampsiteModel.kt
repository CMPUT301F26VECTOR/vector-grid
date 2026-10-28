package com.example.project_halfway

class CampsiteModel (
    private val campsiteName : String,
    private val campgroundName : String,
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