package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * A box where the entrant writes a message to the campsite owner about a
 * reservation, with a Send button (US 01.14.01).
 *
 * [entrantId] and [send] have defaults that use Firebase, so tests can pass a
 * fake [send] and need no Firebase.
 *
 * @param reservationId the reservation the message is about
 * @param campsiteId the campsite the reservation is for
 * @param entrantId id of the signed-in entrant
 * @param send saves the message and reports true or false when it is done
 */
@Composable
fun MessageOwnerSection(
    reservationId: String,
    campsiteId: String,
    entrantId: String = FirebaseSecurity.auth.currentUser?.uid ?: "",
    send: (OwnerMessage, (Boolean) -> Unit) -> Unit = { message, onDone ->
        OwnerMessageRepository.save(message, onDone)
    }
) {
    var text by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("Message the owner")
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().testTag("message_text")
        )
        Text("${text.length}/${OwnerMessage.MAX_LENGTH}")
        Button(
            onClick = {
                if (entrantId.isBlank()) {
                    status = "Please sign in first"
                    return@Button
                }
                val message = OwnerMessage(
                    reservationId = reservationId,
                    campsiteId = campsiteId,
                    entrantId = entrantId,
                    text = text,
                    createdAt = System.currentTimeMillis()
                )
                val error = message.firstError()
                if (error != null) {
                    status = error
                    return@Button
                }
                status = "Sending..."
                send(message) { worked ->
                    if (worked) {
                        text = ""
                        status = "Message sent"
                    } else {
                        status = "Could not send, please try again."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("message_send")
        ) { Text("Send message") }
        if (status.isNotEmpty()) {
            Text(status, modifier = Modifier.testTag("message_status"))
        }
    }
}