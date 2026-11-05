package com.example.project_halfway

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
/*
For UI components common to each campsite view
 */

/**
 * Shows a campsite's user rating as stars, on both the summary and detailed cards. Displays 5
 * stars in total, and fully fills stars based on the floor of the campsite's rating. Displays
 * a half star if the fractional component of the rating is 0.5 or higher, otherwise it stays
 * empty.
 *
 * Outstanding issues: None known.
 * yet.
 *
 * @param campsite the campsite whose rating is to shown
 */
@Composable
fun RatingStars(campsite : CampsiteModel) {
    val rating = campsite.getUserRating()
    val maxStarRating = 5

    // Indicates no user ratings. Style better later
    if (rating == 0.0) {
        Text(
            "No user ratings.",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        return
    }

    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
        for (i in 1..maxStarRating) {
            val icon = when {
                rating >= i -> Icons.Filled.Star // Fully filled yellow star

                rating >= i - 0.5 -> Icons.AutoMirrored.Filled.StarHalf // Half filled star

                else -> Icons.Filled.StarBorder // Empty star
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFC107)
            )
        }
    }
}