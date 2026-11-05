package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project_halfway.ui.theme.Project_halfwayTheme
import java.util.Locale

private val CardGreen = Color(0xFF566C11)
private val CardGrey = Color(0xFF5F6368)

/**
 * The campsite summary card: name, rating, campground and location, number of
 * guests, price, available dates and, when there is one, the open lottery
 * (US 01.01.03, US 01.01.04). Shared by the entrant's Browse screen and the
 * owner's Registered Campsites list. The card fills the width it is given, so
 * the list decides the spacing around it.
 *
 * Outstanding issues: the rating is the stored userRating value, because
 * entrants can't rate campsites yet (US 01.13.01).
 *
 * @param campsite the campsite to show
 * @param onClick called when the card is tapped. The screen showing the card
 * decides what opens: Browse opens the entrant's details, My Campsites opens
 * the owner's details.
 * @param lotteryText text for the lottery tag, e.g. "Lottery closes in 5 days",
 * or null to show no tag (no open lottery, or a screen that doesn't load it)
 */
@Composable
fun CampsiteDescriptionCard(
    campsite: CampsiteModel,
    onClick: () -> Unit = {},
    lotteryText: String? = null
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Name on the left, a compact rating on the right
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    campsite.getCampsiteName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                CompactRating(campsite.getUserRating())
            }

            // "Whistlers Campground · Jasper, Alberta"
            InfoLine(
                icon = Icons.Filled.Place,
                text = placeText(campsite),
                style = MaterialTheme.typography.bodyMedium
            )

            // Guests and price on one line
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoLine(
                    icon = Icons.Filled.People,
                    text = guestsText(campsite.getOccupancy())
                )
                if (campsite.getPricePerNight() > 0.0) {
                    Text(
                        priceText(campsite.getPricePerNight()),
                        style = MaterialTheme.typography.labelLarge,
                        color = CardGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            InfoLine(
                icon = Icons.Filled.CalendarMonth,
                text = datesText(campsite.getAvailableDateRanges()),
                style = MaterialTheme.typography.bodySmall
            )

            if (lotteryText != null) {
                Text(
                    lotteryText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(CardGreen, RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("lottery_tag")
                )
            }
        }
    }
}

// One star and the rating ("4.0"), or "New" when the campsite has no rating yet
@Composable
private fun CompactRating(rating: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.Star,
            contentDescription = null,
            tint = Color(0xFFFFC107),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(2.dp))
        Text(
            ratingText(rating),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.testTag("rating_text")
        )
    }
}

// A small grey icon followed by one line of text
@Composable
private fun InfoLine(
    icon: ImageVector,
    text: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelLarge
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = CardGrey, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = style, color = CardGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * Text for the compact rating on the card.
 *
 * @param rating the campsite's rating, 0.0 when it has none
 * @return "New" for no rating, otherwise the rating with one decimal, e.g. "4.0"
 */
fun ratingText(rating: Double): String =
    if (rating <= 0.0) "New" else String.format(Locale.US, "%.1f", rating)

/**
 * The campground and location on one line, e.g. "Whistlers Campground · Jasper, AB".
 *
 * @param campsite the campsite to describe
 * @return just the location when there is no campground name
 */
fun placeText(campsite: CampsiteModel): String {
    val campground = campsite.getCampgroundName().trim()
    val location = campsite.getCampsiteLocation().trim()
    return when {
        campground.isEmpty() -> location
        location.isEmpty() -> campground
        else -> "$campground · $location"
    }
}

/**
 * How many guests the campsite holds, e.g. "1 guest" or "6 guests".
 *
 * @param occupancy the campsite's maximum occupancy
 * @return the text for the card
 */
fun guestsText(occupancy: Int): String =
    if (occupancy == 1) "1 guest" else "$occupancy guests"

/**
 * The price per night, e.g. "$38.50 / night".
 *
 * @param price the price in dollars
 * @return the text for the card
 */
fun priceText(price: Double): String =
    String.format(Locale.US, "$%.2f / night", price)

/**
 * The first available date range, plus how many more there are, e.g.
 * "2027-07-02 to 2027-07-09 (+1 more)".
 *
 * @param ranges the campsite's available date ranges, as "start" and "end" maps
 * @return "No dates yet" when there are none
 */
fun datesText(ranges: List<Map<String, String>>): String {
    if (ranges.isEmpty()) return "No dates yet"
    val first = ranges.first()
    val text = "${first["start"]} to ${first["end"]}"
    return if (ranges.size > 1) "$text (+${ranges.size - 1} more)" else text
}

// Preview of a full card and a bare one
@Preview(showBackground = true, backgroundColor = 0xFF526B0D)
@Composable
private fun CampsiteDescriptionCardPreview() {
    val full = CampsiteModel(
        campsiteName = "Site 42 – Lakeview",
        campgroundName = "Whistlers Campground",
        location = "Jasper, Alberta",
        longitude = -118.077,
        latitude = 52.8507,
        occupancy = 6,
        userRating = 4.0,
        description = "",
        imageUrl = "",
        pricePerNight = 38.5,
        availableDateRanges = listOf(
            mapOf("start" to "2027-07-02", "end" to "2027-07-09"),
            mapOf("start" to "2027-08-13", "end" to "2027-08-20")
        )
    )
    val bare = CampsiteModel("Site 3", "", "Elk Island National Park, AB", 0.0, 0.0, 2, 0.0, "", "")
    Project_halfwayTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CampsiteDescriptionCard(full, lotteryText = "Lottery closes in 5 days")
            CampsiteDescriptionCard(bare)
        }
    }
}
