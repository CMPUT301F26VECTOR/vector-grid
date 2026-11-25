package com.example.project_halfway

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Admin screen that shows the ratings of every campsite and lets the admin
 * remove one rating (US 03.09.01). Loads from [AdminRatingRepository]; the
 * drawing is in [AdminRatingsList].
 *
 * @param toback called when the admin taps Back
 */
@Composable
fun AdminRatingsScreen(toback: () -> Unit = {}) {
    var campsites by remember { mutableStateOf(listOf<CampsiteModel>()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    fun load() {
        AdminRatingRepository.getCampsites(
            onResult = { campsites = it; loading = false },
            onError = { message = it; loading = false }
        )
    }

    LaunchedEffect(Unit) { load() }

    Column {
        Button(onClick = toback, modifier = Modifier.padding(16.dp)) { Text("Back") }
        if (loading) {
            Text("Loading ratings...", modifier = Modifier.padding(16.dp))
        } else {
            AdminRatingsList(
                campsites = campsites,
                message = message,
                onRemove = { campsite, index ->
                    AdminRatingRepository.removeRating(campsite, index) { ok ->
                        if (ok) {
                            message = "Rating removed"
                            load()
                        } else {
                            message = "Could not remove the rating, please try again."
                        }
                    }
                }
            )
        }
    }
}

/** Only draws the campsites that have ratings, so tests can use fake data. */
@Composable
fun AdminRatingsList(
    campsites: List<CampsiteModel>,
    message: String,
    onRemove: (CampsiteModel, Int) -> Unit
) {
    val rated = campsites.filter { it.getRatings().isNotEmpty() }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ratings", style = MaterialTheme.typography.titleLarge)
        if (message.isNotEmpty()) {
            Text(message, modifier = Modifier.testTag("admin_rating_message"))
        }
        if (rated.isEmpty()) {
            Text("No ratings yet", modifier = Modifier.testTag("admin_rating_empty"))
        }
        LazyColumn {
            items(rated) { campsite ->
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(campsite.getCampsiteName(), style = MaterialTheme.typography.titleMedium)
                    campsite.getRatings().forEachIndexed { index, rating ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("$rating stars")
                            Button(
                                onClick = { onRemove(campsite, index) },
                                modifier = Modifier.testTag(
                                    "admin_rating_remove_${campsite.getId()}_$index"
                                )
                            ) { Text("Remove") }
                        }
                    }
                }
            }
        }
    }
}