package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// The owner's screen for the campsites in one lottery (US 02.04.02).
// It lists every campsite in the campsite's newest lottery, and the owner can
// remove one until registration opens.
//
// LotteryCampsitesScreen talks to Firestore, LotteryCampsitesList is just the
// looks (split up so it can be tested without Firebase, the same way
// LotteryEntrantsScreen and LotteryEntrantsList are).
//
// Outstanding issues: shows the campsite's newest lottery only. If saving a
// removal fails, the owner has to leave the screen and open it again.

/**
 * Loads the newest lottery for a campsite and the names of its campsites, and
 * lets the owner remove one before registration opens (US 02.04.02).
 *
 * The rule (not after registration opens, never the last campsite) is in
 * Lottery.removeCampsite. The change is saved with LotteryRepository.updateCampsites.
 *
 * @param campsiteId id of the owner's campsite whose lottery is shown
 */
@Composable
fun LotteryCampsitesScreen(campsiteId: String) {
    var loading by remember { mutableStateOf(true) }
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    // each campsite in the lottery as (id, name)
    var campsites by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(campsiteId) {
        LotteryRepository.getLotteriesForCampsite(campsiteId) { lotteries ->
            // the lottery that closes last is the newest one
            val newest = lotteries.maxByOrNull { it.endDate }
            if (newest == null) {
                loading = false
            } else {
                lottery = newest
                loadCampsiteNames(newest.campsiteIds) { found ->
                    campsites = found
                    loading = false
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

    LotteryCampsitesList(
        campsites = campsites,
        registrationStarted = System.currentTimeMillis() >= current.startDate,
        message = message,
        onRemove = { id ->
            if (current.removeCampsite(id, System.currentTimeMillis())) {
                message = "Saving..."
                LotteryRepository.updateCampsites(current.id, current.campsiteIds) { worked ->
                    if (worked) {
                        campsites = campsites.filter { it.first != id }
                        message = "Campsite removed from the lottery"
                    } else {
                        message = "Could not save. Please leave this screen and open it again."
                    }
                }
            } else {
                message = "This campsite can't be removed."
            }
        }
    )
}

/**
 * Shows the campsites of a lottery, each with a Remove button.
 * Does not talk to Firestore, so it can be tested on its own.
 *
 * @param campsites every campsite in the lottery as (id, name)
 * @param registrationStarted true once registration has opened; Remove is turned off then
 * @param message what to show under the list ("Saving...", errors)
 * @param onRemove called with the campsite's id when the owner presses Remove
 */
@Composable
fun LotteryCampsitesList(
    campsites: List<Pair<String, String>>,
    registrationStarted: Boolean,
    message: String,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            "Campsites in this lottery",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("lottery_campsites_title")
        )

        if (registrationStarted) {
            Text(
                "Registration has started, so campsites can no longer be removed.",
                modifier = Modifier.padding(top = 8.dp).testTag("lottery_campsites_note")
            )
        } else if (campsites.size <= 1) {
            Text(
                "A lottery needs at least one campsite, so the last one can't be removed.",
                modifier = Modifier.padding(top = 8.dp).testTag("lottery_campsites_note")
            )
        }

        campsites.forEach { (id, name) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(name, modifier = Modifier.weight(1f).testTag("lottery_campsite_$id"))
                OutlinedButton(
                    onClick = { onRemove(id) },
                    enabled = !registrationStarted && campsites.size > 1,
                    modifier = Modifier.testTag("remove_campsite_$id")
                ) {
                    Text("Remove")
                }
            }
        }

        Text(message, modifier = Modifier.padding(top = 8.dp).testTag("lottery_campsites_message"))
    }
}

// Looks up each campsite's name; gives (id, name) in the lottery's order once
// every lookup is back. A campsite that can't be found is shown by its id.
private fun loadCampsiteNames(ids: List<String>, onResult: (List<Pair<String, String>>) -> Unit) {
    if (ids.isEmpty()) {
        onResult(emptyList())
        return
    }
    val names = mutableMapOf<String, String>()
    var answered = 0

    for (id in ids) {
        CampsiteRepository.getById(id) { campsite ->
            names[id] = campsite?.getCampsiteName() ?: id
            answered++
            if (answered == ids.size) {
                onResult(ids.map { it to (names[it] ?: it) })
            }
        }
    }
}