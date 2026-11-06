package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.messaging.FirebaseMessaging

private val EntriesGreen = Color(0xFF566C11)

/**
 * The "My Reservations & Entries" screen from our mockups (entrant screen 9).
 * It has three tabs: Reservations, Waitlisted and Lottery.
 *
 * The Waitlisted tab is done: it lists every lottery waiting list the entrant
 * is on, how many days are left, and a Leave Waitlist button. Unsubscribes user from FCM
 * topic when a waitlist is left.
 * Used for US 01.02.01 (see the waiting lists I joined) and
 * US 01.02.02 (leave a waiting list).
 *
 * The Lottery tab (LotteryResultsTab) shows the entrant's draw results, and
 * lets a winner reserve dates inside the booking period (US 02.05.01,
 * US 02.04.04).
 *
 * Outstanding issues: the Reservations tab (US 01.06.01, 01.06.02, 01.07.01)
 * is not done yet, it just shows a message for now.
 *
 * @param onBackClick called when the back arrow is pressed
 */
@Composable
fun MyEntriesScreen(onBackClick: () -> Unit) {
    // start on the Waitlisted tab since that's the one that works
    var selectedTab by remember { mutableStateOf(1) }
    val tabs = listOf("Reservations", "Waitlisted", "Lottery")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EntriesGreen)
    ) {
        // back arrow + title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 16.dp)
                .clickable { onBackClick() }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.padding(start = 12.dp))
            Text(
                text = "My Reservations & Entries",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        TabRow(selectedTabIndex = selectedTab) {
            for (i in tabs.indices) {
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = { Text(tabs[i]) }
                )
            }
        }

        when (selectedTab) {
            1 -> WaitlistedTab()
            // TODO: Reservations tab (US 01.06.01, 01.06.02, 01.07.01)
            0 -> NotDoneYetText("Your reservations will show here")
            // Draw results, and reserving dates for a win (US 02.05.01, 02.04.04)
            else -> LotteryResultsTab()
        }
    }
}

// placeholder text for the tabs other people are still working on
@Composable
private fun NotDoneYetText(text: String) {
    Text(text = text, color = Color.White, modifier = Modifier.padding(20.dp))
}

/**
 * The Waitlisted tab. Loads the lotteries the signed in entrant has joined
 * and shows a WaitlistCard for each one.
 */
@Composable
fun WaitlistedTab() {
    val entrantId = FirebaseSecurity.auth.currentUser?.uid ?: ""

    val waitlists = remember { mutableStateListOf<Lottery>() }
    // campsite id -> campsite name, filled in as the campsites load
    val campsiteNames = remember { mutableStateMapOf<String, String>() }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(entrantId) {
        if (entrantId == "") {
            loading = false
            message = "Please sign in first"
        } else {
            // every lottery whose waiting list has this entrant in it
            FirebaseSecurity.db.collection("lotteries")
                .whereArrayContains("waitingList", entrantId)
                .get()
                .addOnSuccessListener { result ->
                    val all = mutableListOf<Lottery>()
                    for (document in result.documents) {
                        val data = document.data
                        if (data != null) {
                            all.add(LotteryRepository.fromMap(document.id, data))
                        }
                    }
                    waitlists.clear()
                    waitlists.addAll(WaitlistHelper.waitlistsFor(entrantId, all))

                    // look up each campsite's name for the cards
                    for (lottery in waitlists) {
                        CampsiteRepository.getById(lottery.campsiteId) { campsite ->
                            campsiteNames[lottery.campsiteId] = campsite.getCampsiteName()
                        }
                    }
                    loading = false
                }
                .addOnFailureListener {
                    loading = false
                    message = "Could not load your waitlists"
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (loading) {
            Text("Loading...", color = Color.White)
        } else if (waitlists.isEmpty() && message == "") {
            Text("You haven't joined any waitlists yet", color = Color.White)
        }

        for (lottery in waitlists.toList()) {
            WaitlistCard(
                campsiteName = campsiteNames[lottery.campsiteId] ?: "Campsite",
                daysText = WaitlistHelper.daysLeftText(lottery, System.currentTimeMillis()),
                waitingListSize = lottery.getWaitingListSize(),
                onLeave = {
                    LotteryRepository.leaveWaitingList(lottery.id, entrantId) { worked ->
                        if (worked) {
                            waitlists.remove(lottery)
                            message = "You left the waitlist"

                            // Remove entrant from the FCM topic:
                            FirebaseMessaging.getInstance().subscribeToTopic("WAITLIST_CAMPSITE_" +
                                    lottery.getLotteryId())

                        } else {
                            message = "Could not leave, try again"
                        }
                    }
                }
            )
        }

        if (message != "") {
            Text(message, color = Color.White, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/**
 * One waitlist card: campsite name, days left, how many people joined and
 * a Leave Waitlist button. Does not talk to Firestore, so it can be tested
 * on its own.
 *
 * @param campsiteName name of the campsite
 * @param daysText text like "Lottery closes in 3 days"
 * @param waitingListSize how many people are on the waiting list
 * @param onLeave called when Leave Waitlist is pressed
 */
@Composable
fun WaitlistCard(
    campsiteName: String,
    daysText: String,
    waitingListSize: Int,
    onLeave: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(campsiteName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(daysText, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
            Text("People on waiting list: $waitingListSize", color = Color.DarkGray)
            Button(
                onClick = onLeave,
                modifier = Modifier.padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Leave Waitlist")
            }
        }
    }
}

/**
 * Small helper functions for the Waitlisted tab. Kept apart from the UI so
 * they can be unit tested.
 */
object WaitlistHelper {

    /**
     * Picks the lotteries the entrant is still waiting on (on the waiting
     * list and not drawn yet), closing soonest first.
     *
     * @param entrantId the entrant's id
     * @param lotteries the lotteries to look through
     * @return the entrant's waitlists, sorted by end date
     */
    fun waitlistsFor(entrantId: String, lotteries: List<Lottery>): List<Lottery> {
        val result = mutableListOf<Lottery>()
        for (lottery in lotteries) {
            if (lottery.isOnWaitingList(entrantId) && !lottery.isDrawn) {
                result.add(lottery)
            }
        }
        return result.sortedBy { it.endDate }
    }

    /**
     * The booking period as text, e.g. "2026-11-10 to 2026-11-24", or
     * "any dates" when the owner didn't set one (US 02.04.04).
     *
     * @param lottery the lottery
     * @return the text for the reserve calendar
     */
    fun bookingPeriodText(lottery: Lottery): String {
        if (lottery.bookingStartDate <= 0L || lottery.bookingEndDate <= 0L) return "any dates"
        return CampsiteForm.millisToDate(lottery.bookingStartDate) + " to " +
            CampsiteForm.millisToDate(lottery.bookingEndDate)
    }

    /**
     * Text for a winner's reservation, e.g. "Reserved: 2026-11-10 to 2026-11-12".
     *
     * @param reservation the reservation
     * @return the text for the result card
     */
    fun reservedText(reservation: Reservation): String {
        return "Reserved: ${reservation.startDate} to ${reservation.endDate}"
    }

    /**
     * Text for how long is left before the lottery closes.
     *
     * @param lottery the lottery
     * @param now the current time in milliseconds
     * @return e.g. "Lottery closes in 3 days", "Lottery closes today" or "Lottery has closed"
     */
    fun daysLeftText(lottery: Lottery, now: Long): String {
        if (now > lottery.endDate) {
            return "Lottery has closed"
        }
        val days = lottery.getDaysLeft(now)
        if (days == 0) {
            return "Lottery closes today"
        }
        if (days == 1) {
            return "Lottery closes in 1 day"
        }
        return "Lottery closes in $days days"
    }
}

/** Android Studio preview of a waitlist card. */
@Preview(showBackground = true)
@Composable
fun WaitlistCardPreview() {
    WaitlistCard("Site 56A", "Lottery closes in 3 days", 12, {})
}
