package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CampsiteDescriptionCard(campsite : CampsiteModel) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.padding(20.dp),
        onClick = { }  // Needs to link to the full view
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
                "IMPLEMENT GETTING AVAILABLE DATES",
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

