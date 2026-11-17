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

/**
 * The lottery part of the entrant's campsite details screen.
 *
 * What it does:
 * - Loads the campsite's open lottery, or says there isn't one.
 * - Shows the days left and how many people are on the waiting list.
 * - Lets the signed-in entrant join or leave the waiting list.
 * - Shows a message after each join or leave.
 *
 * Browse puts it in CampsiteDetailedCard's action area. It talks to Firestore;
 * LotteryButton below only draws the info and buttons, so it can be tested on its own.
 *
 * Used for US 01.01.06 (join from the details screen), US 01.02.01 (join the
 * waiting list) and US 01.02.02 (leave the waiting list).
 *
 * Outstanding issues: the count doesn't update if someone else joins or
 * leaves while the screen is open.
 *
 * @param campsiteId id of the campsite being looked at
 *
 *
 * **AI assistance (Parneet Kaur):** part of this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 */

@Composable
fun LotterySection(campsiteId: String) {
 
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
 * Shows the lottery info and the Join or Leave button (US 01.02.01, 01.02.02).
 *
 * Shows "Join Waiting List" if the entrant hasn't joined, and "Leave Waiting List"
 * if they have. It has no Firestore code, so it is tested on its own (LotteryButtonTest).
 *
 * @param isOnWaitingList true if the entrant already joined
 * @param waitingListSize how many people are on the waiting list
 * @param daysLeft full days until registration closes
 * @param message text shown under the button, or "" for none
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
