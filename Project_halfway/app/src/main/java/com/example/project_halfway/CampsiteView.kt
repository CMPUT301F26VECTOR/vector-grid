package com.example.project_halfway

import androidx.compose.runtime.Composable
import androidx.media3.common.StarRating

/*
For UI components common to each campsite view class
 */
open class CampsiteView {
    @Composable
    fun RatingStars(campsite : CampsiteModel) {
        val starRating = StarRating(5, campsite.getUserRating().toFloat())
    }

}