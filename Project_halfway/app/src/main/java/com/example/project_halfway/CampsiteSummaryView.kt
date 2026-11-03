package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The small campsite summary card: rating, name, location, available dates and
 * a "days left in lottery" badge. Shared by the entrant's browse screen and
 * the owner's Registered Campsites list.
 *
 * Outstanding issues: the available dates line is a placeholder, and the
 * days-left badge always shows 6.
 *
 * @param campsite the campsite to show
 * @param onClick called when the card is tapped. The screen showing the card
 * decides what opens: Browse opens the entrant's details, My Campsites opens
 * the owner's details.
 */
@Composable
fun CampsiteDescriptionCard(campsite : CampsiteModel, onClick: () -> Unit = {}) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.padding(20.dp),
        onClick = onClick
    ) {
        Column() {
            RatingStars(campsite)

            // Campsite name
            Text(
                campsite.getCampsiteName(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                campsite.getCampsiteLocation(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                campsite.getOccupancy().toString() + " guests",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                campsite.getAvailableDateRangesString(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            
            Button(
                enabled = false,
                onClick = {}, // We don't want to be able to click the "Days left in lottery" flag
                content = { Text("Days left in lottery: 6") }
            )

        }

    }
}

