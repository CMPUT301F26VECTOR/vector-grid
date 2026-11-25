package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * The review part of the entrant's campsite details screen (US 01.13.02).
 *
 * The entrant types a review and presses Post review. The review is checked
 * with Review.firstError and saved with ReviewRepository.save. Browse puts it
 * in CampsiteDetailedCard's action area, under the lottery section.
 *
 * Outstanding issues: the entrant can't see the reviews that are already
 * posted yet, and can't edit or delete their own.
 *
 * @param campsiteId id of the campsite being reviewed
 * @param entrantId id of the signed-in entrant; blank means nobody is signed in
 * @param save stores the review and reports whether it worked; ReviewRepository.save
 * by default, and replaced in tests so they don't need Firestore
 */
@Composable
fun ReviewSection(
    campsiteId: String,
    entrantId: String = FirebaseSecurity.auth.currentUser?.uid ?: "",
    save: (Review, (Boolean) -> Unit) -> Unit = { review, onDone -> ReviewRepository.save(review, onDone) }
) {
    var text by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var posting by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text("Write a review", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("review_text")
        )
        Text("${text.length}/${Review.MAX_LENGTH}", modifier = Modifier.padding(top = 2.dp))

        Button(
            onClick = {
                val review = Review(
                    campsiteId = campsiteId,
                    entrantId = entrantId,
                    text = text,
                    createdAt = System.currentTimeMillis()
                )
                val error = review.firstError()
                if (entrantId == "") {
                    message = "Please sign in first"
                } else if (error != null) {
                    message = error
                } else {
                    message = "Posting..."
                    posting = true
                    save(review) { worked ->
                        posting = false
                        if (worked) {
                            text = ""
                            message = "Review posted"
                        } else {
                            message = "Could not post, please try again."
                        }
                    }
                }
            },
            enabled = !posting,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff566C11)),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("review_post")
        ) {
            Text("Post review")
        }

        if (message != "") {
            Text(message, modifier = Modifier.padding(top = 4.dp).testTag("review_message"))
        }
    }
}