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
 * Admin screen that lists every review and lets the admin remove one (US 03.10.01).
 * Loads from [AdminReviewRepository]; the drawing is in [AdminReviewsList].
 *
 * @param toback called when the admin taps Back
 */
@Composable
fun AdminReviewsScreen(toback: () -> Unit = {}) {
    var reviews by remember { mutableStateOf(listOf<Review>()) }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        AdminReviewRepository.getAll(
            onResult = { reviews = it; loading = false },
            onError = { message = it; loading = false }
        )
    }

    Column {
        Button(onClick = toback, modifier = Modifier.padding(16.dp)) { Text("Back") }
        if (loading) {
            Text("Loading reviews...", modifier = Modifier.padding(16.dp))
        } else {
            AdminReviewsList(
                reviews = reviews,
                message = message,
                onRemove = { review ->
                    AdminReviewRepository.delete(review.id) { ok ->
                        if (ok) {
                            reviews = reviews.filter { it.id != review.id }
                            message = "Review removed"
                        } else {
                            message = "Could not remove the review, please try again."
                        }
                    }
                }
            )
        }
    }
}

/** Only draws the reviews, so tests can use fake data. */
@Composable
fun AdminReviewsList(
    reviews: List<Review>,
    message: String,
    onRemove: (Review) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Reviews", style = MaterialTheme.typography.titleLarge)
        if (message.isNotEmpty()) {
            Text(message, modifier = Modifier.testTag("admin_review_message"))
        }
        if (reviews.isEmpty()) {
            Text("No reviews yet", modifier = Modifier.testTag("admin_review_empty"))
        }
        LazyColumn {
            items(reviews) { review ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(review.text, modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onRemove(review) },
                        modifier = Modifier.testTag("admin_review_remove_${review.id}")
                    ) { Text("Remove") }
                }
            }
        }
    }
}