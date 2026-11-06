package com.example.project_halfway

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
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
For UI components common to each campsite view: the rating stars, the photo
and the description, used by the details card.
 */

/**
 * Shows a campsite's rating as stars on the details card: the average of the
 * entrants' ratings (US 01.01.04), the same number the summary card shows.
 * Displays 5 stars in total, and fully fills stars based on the floor of the
 * rating. Displays a half star if the fractional part is 0.5 or higher,
 * otherwise it stays empty. Shows "No ratings yet" when nobody has rated it.
 *
 * Outstanding issues: none known.
 *
 * @param campsite the campsite whose rating is to shown
 */
@Composable
fun RatingStars(campsite : CampsiteModel) {
    val rating = campsite.getAverageRating()
    val maxStarRating = 5

    // Nobody has rated this campsite yet
    if (rating == 0.0) {
        Text(
            "No ratings yet",
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
        // e.g. "4.3 (3 ratings)", so it matches the summary card's number
        Text(
            text = ratingText(rating) + " (" + ratingCountText(campsite.getRatingCount()) + ")",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

/**
 * How many ratings a campsite has, e.g. "1 rating" or "3 ratings".
 *
 * @param count the number of ratings
 * @return the text to show next to the stars
 */
fun ratingCountText(count: Int): String =
    if (count == 1) "1 rating" else "$count ratings"
/**
 * The campsite's photo, loaded from its imageUrl (US 01.01.05, 02.01.04).
 * Shows nothing when the campsite has no photo, or while it loads, or if it
 * can't be loaded, so the details card never shows a broken picture.
 *
 * Outstanding issues: owners can't upload a photo yet, so only campsites
 * with an imageUrl set in Firestore have one.
 *
 * @param url the photo's web address; blank means no photo
 * @param modifier layout options from the card that shows it
 */
@Composable
fun CampsiteImage(url: String, modifier: Modifier = Modifier) {
    if (url.isBlank()) return

    var bitmap by remember(url) { mutableStateOf<ImageBitmap?>(null) }

    // Download the picture away from the screen's thread, then show it
    LaunchedEffect(url) {
        bitmap = withContext(Dispatchers.IO) {
            try {
                URL(url).openStream().use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    val loaded = bitmap ?: return
    Image(
        bitmap = loaded,
        contentDescription = "Campsite photo",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("campsite_image")
    )
}

/**
 * The owner's description of the campsite under an "About this campsite"
 * heading, or "No description yet." when the owner left it empty
 * (US 01.01.05, 02.01.04).
 *
 * @param description the campsite's description
 */
@Composable
fun CampsiteDescription(description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "About this campsite",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Text(
            text = description.ifBlank { "No description yet." },
            style = MaterialTheme.typography.bodyLarge,
            color = if (description.isBlank()) Color.Gray else Color(0xFF424242),
            modifier = Modifier.padding(top = 4.dp).testTag("description_text")
        )
    }
}
