package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// The lottery part of the campsite details screen.
// US 01.01.06 - join an active lottery from the campsite details screen
// US 01.02.01 - join the waiting list
// US 01.02.02 - leave the waiting list
//
// LotterySection talks to Firestore, LotteryButton is just the looks
// (split up so LotteryButton can be tested without Firebase).

/**
 * Loads the active lottery for a campsite and lets the signed in entrant
 * join or leave its waiting list.
 *
 * Used for US 01.01.06, US 01.02.01 and US 01.02.02. Shown on the entrant's
 * campsite details: Browse puts it in CampsiteDetailedCard's action area.
 *
 * Outstanding issues: lotteries can only be added in the Firestore console
 * until the owner's create-lottery screen exists.
 *
 * @param campsiteId id of the campsite being looked at
 */
@Composable
fun LotterySection(campsiteId: String) {
    // the logged in user's id is used as the entrant id for now
    // (will change once device login US 01.09.01 is merged)
    val entrantId = FirebaseSecurity.auth.currentUser?.uid ?: ""

    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var loading by remember { mutableStateOf(true) }
    var onList by remember { mutableStateOf(false) }
    var count by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(campsiteId) {
        LotteryRepository.getActiveLottery(campsiteId) { found ->
            lottery = found
            if (found != null) {
                onList = found.isOnWaitingList(entrantId)
                count = found.getWaitingListSize()
            }
            loading = false
        }
    }

    if (loading) {
        Text("Loading lottery...", modifier = Modifier.padding(8.dp))
        return
    }

    val current = lottery
    if (current == null) {
        Text("No active lottery for this campsite right now", modifier = Modifier.padding(8.dp))
        return
    }

    LotteryButton(
        isOnWaitingList = onList,
        waitingListSize = count,
        daysLeft = current.getDaysLeft(System.currentTimeMillis()),
        message = message,
        onJoin = {
            if (entrantId == "") {
                message = "Please sign in first"
            } else if (current.joinWaitingList(entrantId, System.currentTimeMillis())) {
                LotteryRepository.joinWaitingList(current.id, entrantId) { worked ->
                    if (worked) {
                        onList = true
                        count = current.getWaitingListSize()
                        message = "You joined the waiting list!"
                    } else {
                        // undo it locally since firestore didn't save it
                        current.leaveWaitingList(entrantId)
                        message = "Could not join, try again"
                    }
                }
            } else {
                message = "You can't join this lottery"
            }
        },
        onLeave = {
            if (current.leaveWaitingList(entrantId)) {
                LotteryRepository.leaveWaitingList(current.id, entrantId) { worked ->
                    if (worked) {
                        onList = false
                        count = current.getWaitingListSize()
                        message = "You left the waiting list"
                    } else {
                        current.joinWaitingList(entrantId, System.currentTimeMillis())
                        message = "Could not leave, try again"
                    }
                }
            }
        }
    )
}

/**
 * Shows the lottery info and the join / leave button.
 * Does not talk to Firestore, so it can be tested on its own.
 *
 * @param isOnWaitingList true if the entrant already joined
 * @param waitingListSize how many people joined
 * @param daysLeft days until the lottery closes
 * @param message feedback text shown under the button
 * @param onJoin called when Join is pressed
 * @param onLeave called when Leave is pressed
 */
@Composable
fun LotteryButton(
    isOnWaitingList: Boolean,
    waitingListSize: Int,
    daysLeft: Int,
    message: String,
    onJoin: () -> Unit,
    onLeave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Lottery is open", style = MaterialTheme.typography.titleMedium)
        Text("Days left: $daysLeft")
        Text("People on waiting list: $waitingListSize")

        if (isOnWaitingList) {
            OutlinedButton(
                onClick = onLeave,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Leave Waiting List")
            }
        } else {
            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xff566C11))
            ) {
                Text("Join Waiting List")
            }
        }

        if (message != "") {
            Text(message, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Android Studio preview of LotteryButton for an entrant who hasn't joined yet. */
@Preview(showBackground = true)
@Composable
fun LotteryButtonPreview() {
    LotteryButton(
        isOnWaitingList = false,
        waitingListSize = 12,
        daysLeft = 3,
        message = "",
        onJoin = {},
        onLeave = {}
    )
}
