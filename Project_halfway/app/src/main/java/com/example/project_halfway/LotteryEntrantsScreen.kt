package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// The owner's Winners / Waitlist screen for one campsite's lottery
// (mockup Owner screen 6).
// US 02.05.01 - winners are drawn automatically once registration closes
// US 02.09.01 - the owner sees who joined the waitlist
//
// LotteryEntrantsScreen talks to Firestore, LotteryEntrantsList is just the
// looks (split up so LotteryEntrantsList can be tested without Firebase, the
// same way LotterySection and LotteryButton are split).
//
// Outstanding issues: shows the campsite's newest lottery only. The owner
// home screen still needs a button that opens this screen.

/**
 * Loads the newest lottery for a campsite, draws its winners if registration
 * has closed, looks up the entrants' names and shows them.
 *
 * Used for US 02.05.01 and US 02.09.01.
 *
 * Outstanding issues: shows the campsite's newest lottery only, and nothing in
 * the app opens this screen yet (the owner home screen needs a button for it).
 *
 * @param campsiteId id of the owner's campsite
 */
@Composable
fun LotteryEntrantsScreen(campsiteId: String) {
    var loading by remember { mutableStateOf(true) }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var names by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(campsiteId) {
        LotteryRepository.getLotteriesForCampsite(campsiteId) { lotteries ->
            // the lottery that closes last is the newest one
            val newest = lotteries.maxByOrNull { it.endDate }
            if (newest == null) {
                loading = false
            } else {
                // US 02.05.01: draws now if registration closed and nobody drew yet
                LotteryRepository.drawIfDue(newest.id) { afterDraw ->
                    val current = afterDraw ?: newest
                    lottery = current
                    LotteryRepository.getEntrantNames(current.waitingList) { found ->
                        names = found
                        loading = false
                    }
                }
            }
        }
    }

    if (loading) {
        Text("Loading lottery...", modifier = Modifier.padding(16.dp))
        return
    }

    val current = lottery
    if (current == null) {
        Text("This campsite has no lottery yet.", modifier = Modifier.padding(16.dp))
        return
    }

    LotteryEntrantsList(
        waitlistNames = current.waitingList.map { names[it] ?: it },
        winnerNames = current.winners.map { names[it] ?: it },
        isDrawn = current.isDrawn
    )
}

/**
 * Shows the WAITLIST and WINNERS tabs from the mockup.
 * Does not talk to Firestore, so it can be tested on its own.
 *
 * @param waitlistNames names of everyone who joined the waitlist
 * @param winnerNames names of the entrants the draw picked
 * @param isDrawn true once the draw has happened
 * Users on Waitlist" button on waitlist tab
 */
@Composable
fun LotteryEntrantsList(
    waitlistNames: List<String>,
    winnerNames: List<String>,
    isDrawn: Boolean,
) {
    // which tab is open: false = WAITLIST, true = WINNERS
    var showWinners by remember { mutableStateOf(false) }
    var showSendNotificationScreen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

        Row {
            TabButton("WAITLIST", selected = !showWinners, tag = "waitlist_tab") { showWinners = false }
            Spacer(modifier = Modifier.width(8.dp))
            TabButton("WINNERS", selected = showWinners, tag = "winners_tab") { showWinners = true }
        }

        if (showWinners) {
            if (!isDrawn) {
                Text(
                    "Winners are drawn when registration closes.",
                    modifier = Modifier.padding(top = 12.dp).testTag("winners_message")
                )
            } else if (winnerNames.isEmpty()) {
                Text(
                    "Nobody joined, so there are no winners.",
                    modifier = Modifier.padding(top = 12.dp).testTag("winners_message")
                )
            } else {
                NameList(winnerNames, "winners_list")
            }
        } else {    // Show the waitlist

            if(!showSendNotificationScreen) {
                Button(onClick = { showSendNotificationScreen = true }) {
                    Text("Send Notification to Users on Waitlist")
                }

                Text(
                    "People on waitlist: ${waitlistNames.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 12.dp).testTag("waitlist_count")
                )
                if (waitlistNames.isEmpty()) {
                    Text(
                        "No entrants have joined yet.",
                        modifier = Modifier.testTag("empty_waitlist")
                    )
                } else {
                    NameList(waitlistNames, "waitlist_list")
                }
            }

            else {
                OwnerNotifyEntrantsScreen()
            }
        }
    }
}

// A filled green button for the open tab, an outlined one for the other tab
@Composable
private fun TabButton(text: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = Modifier.testTag(tag),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff566C11))
        ) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.testTag(tag)) { Text(text) }
    }
}

// One line per name
@Composable
private fun NameList(names: List<String>, tag: String) {
    LazyColumn(modifier = Modifier.testTag(tag)) {
        items(names) { name ->
            Text(name, modifier = Modifier.padding(vertical = 6.dp))
        }
    }
}
