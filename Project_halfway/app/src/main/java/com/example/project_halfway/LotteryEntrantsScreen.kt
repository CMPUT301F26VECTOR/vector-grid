package com.example.project_halfway

import java.util.Locale
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.Instant
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
// The owner can pick any of the campsite's lotteries (LotteryPicker); the
// newest one shows first.
//
// Outstanding issues: none known.

/**
 * Loads every lottery for a campsite and shows the newest one first; when the
 * campsite has more than one, the owner can pick which to see (US 02.09.01).
 * Draws the chosen lottery's winners if registration has closed (US 02.05.01),
 * looks up the entrants' names and shows them. Opened from the owner's
 * campsite details (View waitlist and winners).
 *
 * Outstanding issues: none known.
 *
 * @param campsiteId id of the owner's campsite
 */
@Composable
fun LotteryEntrantsScreen(campsiteId: String) {
    var loading by remember { mutableStateOf(true) }
    // every lottery of this campsite, newest first, and the one being shown
    var lotteries by remember { mutableStateOf(listOf<Lottery>()) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var names by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(campsiteId) {
        LotteryRepository.getLotteriesForCampsite(campsiteId) { found ->
            lotteries = newestFirst(found)
            // start on the newest lottery
            selectedId = lotteries.firstOrNull()?.id
            if (selectedId == null) loading = false
        }
    }

    // Load the chosen lottery whenever the owner picks another one
    LaunchedEffect(selectedId) {
        val id = selectedId ?: return@LaunchedEffect
        val chosen = lotteries.firstOrNull { it.id == id } ?: return@LaunchedEffect
        loading = true
        // US 02.05.01: draws now if registration closed and nobody drew yet
        LotteryRepository.drawIfDue(chosen.id) { afterDraw ->
            val current = afterDraw ?: chosen
            lottery = current
            LotteryRepository.getEntrantNames(current.waitingList) { found ->
                names = found
                loading = false
            }
        }
    }

    if (lotteries.isEmpty()) {
        Text(
            if (loading) "Loading lottery..." else "This campsite has no lottery yet.",
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    Column {
        // US 02.09.01: one chip per lottery, so the owner can see any of its waiting lists
        if (lotteries.size > 1) {
            LotteryPicker(
                labels = lotteries.map { lotteryLabel(it) },
                selectedIndex = lotteries.indexOfFirst { it.id == selectedId },
                onPick = { index -> selectedId = lotteries[index].id }
            )
        }

        val current = lottery
        if (loading || current == null) {
            Text("Loading lottery...", modifier = Modifier.padding(16.dp))
        } else {
            LotteryEntrantsList(
                lottery = current,
                waitlistNames = current.waitingList.map { names[it] ?: it },
                winnerNames = current.winners.map { names[it] ?: it },
                isDrawn = current.isDrawn
            )
        }
    }
}

/**
 * A row of chips, one per lottery, for choosing which waiting list to show
 * (US 02.09.01). Does not talk to Firestore, so it can be tested on its own.
 *
 * @param labels text for each chip, e.g. "Nov 1 to Nov 21, 2026"
 * @param selectedIndex which chip is selected
 * @param onPick called with the index of the chip the owner taps
 */
@Composable
fun LotteryPicker(labels: List<String>, selectedIndex: Int, onPick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
        Text("Lottery (registration period)", style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            labels.forEachIndexed { index, label ->
                FilterChip(
                    selected = index == selectedIndex,
                    onClick = { onPick(index) },
                    label = { Text(label) },
                    modifier = Modifier.testTag("lottery_chip_$index")
                )
            }
        }
    }
}

/**
 * Puts a campsite's lotteries in order, the one that closes last first.
 *
 * @param lotteries the campsite's lotteries in any order
 * @return the same lotteries, newest first
 */
fun newestFirst(lotteries: List<Lottery>): List<Lottery> = lotteries.sortedByDescending { it.endDate }

/**
 * A short name for a lottery: its registration period, e.g.
 * "Nov 1 to Nov 21, 2026" (US 02.09.01).
 *
 * @param lottery the lottery
 * @param zone the time zone to show the dates in; the phone's by default
 * @return the label for the lottery's chip
 */
fun lotteryLabel(lottery: Lottery, zone: ZoneId = ZoneId.systemDefault()): String {
    val day = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    val dayYear = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)
    val start = Instant.ofEpochMilli(lottery.startDate).atZone(zone)
    val end = Instant.ofEpochMilli(lottery.endDate).atZone(zone)
    return start.format(day) + " to " + end.format(dayYear)
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
    lottery: Lottery,
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
                OwnerNotifyEntrantsScreen({}, lottery)
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
