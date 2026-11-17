package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/**
 * Reservations screen for the campsite owner, with three tabs:
 * - Active: stays that are coming up or happening now (US 02.07.01)
 * - Past: stays that are over (US 02.07.02)
 * - Cancelled: stays the entrant cancelled or the owner rejected (US 02.07.03)
 *
 * It loads the owner's campsites, then every reservation for them, then the
 * entrants' names (see loadOwnerReservations). Each tab is a filter on the
 * same list, using Reservation.isActive, isPast and isCancelled. The list is
 * loaded again every time the screen opens, so it shows new reservations and
 * cancellations.
 *
 * Outstanding issues: the Cancelled tab stays empty until entrants can cancel
 * (US 01.07.01) and owners can reject (US 02.08.01). Guests only show when the
 * reservation has a guest count.
 *
 * **AI assistance (Samarth Sathya Karthik):** this code was implemented using
 * Claude AI; Samarth tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * @param ownerId user id of the signed in campsite owner
 * @param onBackClick called when the back arrow is pressed
 * @param loadReservations loads the rows to show; loadOwnerReservations by
 * default, and replaced in tests so they don't need Firestore
 */
@Composable
fun OwnerReservationsScreen(
    ownerId: String,
    onBackClick: () -> Unit = {},
    loadReservations: (String, (List<OwnerReservationRow>) -> Unit) -> Unit = { id, onResult ->
        loadOwnerReservations(id, onResult)
    }
) {
    // The rows loaded so far, and whether Firestore has answered yet
    var rows by remember { mutableStateOf(listOf<OwnerReservationRow>()) }
    var loading by remember { mutableStateOf(true) }

    // Runs once when the screen opens
    LaunchedEffect(ownerId) {
        loadReservations(ownerId) { result ->
            rows = result
            loading = false
        }
    }

    OwnerReservationsList(
        rows = rows,
        loading = loading,
        onBackClick = onBackClick
    )
}

/**
 * One reservation as the owner sees it: the reservation plus the names to
 * show for the entrant and the campsite.
 *
 * @property reservation the reservation from Firestore
 * @property entrantName the entrant's name from their profile
 * @property campsiteName the name of the reserved campsite
 */
data class OwnerReservationRow(
    val reservation: Reservation,
    val entrantName: String,
    val campsiteName: String
)

/** The three tabs on the Reservations screen. */
enum class ReservationTab(val label: String) {
    /** Stays that are coming up or happening now (US 02.07.01). */
    ACTIVE("Active"),

    /** Stays that are over (US 02.07.02). */
    PAST("Past"),

    /** Stays that were cancelled or rejected (US 02.07.03). */
    CANCELLED("Cancelled")
}

/**
 * Picks the rows that belong on one tab, in the order to show them.
 * - Active: soonest first
 * - Past: most recent first
 * - Cancelled: most recently cancelled first
 *
 * It has no Android code, so it is unit tested in OwnerReservationsTest.
 *
 * @param rows every reservation for the owner's campsites
 * @param tab which tab is open
 * @param today today's date as YYYY-MM-DD
 * @return the rows to show on that tab
 */
fun reservationsForTab(
    rows: List<OwnerReservationRow>,
    tab: ReservationTab,
    today: String
): List<OwnerReservationRow> {
    return when (tab) {
        ReservationTab.ACTIVE -> rows
            .filter { it.reservation.isActive(today) }
            .sortedBy { it.reservation.startDate }

        ReservationTab.PAST -> rows
            .filter { it.reservation.isPast(today) }
            .sortedByDescending { it.reservation.endDate }

        ReservationTab.CANCELLED -> rows
            .filter { it.reservation.isCancelled() }
            .sortedByDescending { it.reservation.cancelledAt }
    }
}

/**
 * Says who called off a cancelled reservation (US 02.07.03).
 *
 * @param reservation a cancelled or rejected reservation
 * @return "Rejected by owner" or "Cancelled by entrant"
 */
fun cancelledByText(reservation: Reservation): String {
    return if (reservation.status == Reservation.STATUS_REJECTED) {
        "Rejected by owner"
    } else {
        "Cancelled by entrant"
    }
}

/**
 * Draws the Reservations screen: the green header, the three tab buttons and
 * the cards for the open tab. It does not talk to Firestore, so it can be
 * tested on its own (OwnerReservationsScreenTest).
 *
 * @param rows every reservation for the owner's campsites
 * @param loading true until Firestore has answered
 * @param onBackClick called when the back arrow is pressed
 * @param today today's date as YYYY-MM-DD; tests pass a fixed date
 */
@Composable
fun OwnerReservationsList(
    rows: List<OwnerReservationRow>,
    loading: Boolean = false,
    onBackClick: () -> Unit = {},
    today: String = LocalDate.now().toString()
) {
    // Which tab is open; Active is first, like the mockup
    var tab by remember { mutableStateOf(ReservationTab.ACTIVE) }

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 8.dp, end = 16.dp, bottom = 16.dp)
    ) {
        GreenScreenHeader(title = "Reservations", onBackClick = onBackClick)
        Spacer(Modifier.height(HeaderGap))

        // The three tab buttons. The open one is white, the others are outlined.
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (option in ReservationTab.entries) {
                val open = option == tab
                Button(
                    onClick = { tab = option },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reservations_tab_${option.name.lowercase()}"),
                    colors = if (open) {
                        whiteButtonColors()
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.18f),
                            contentColor = Color.White
                        )
                    }
                ) {
                    Text(option.label, fontWeight = if (open) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Only the rows for the open tab
        val shown = reservationsForTab(rows, tab, today)

        if (loading) {
            Text("Loading reservations...", color = Color.White, modifier = Modifier.padding(start = 8.dp))
        } else if (shown.isEmpty()) {
            Text(
                "No ${tab.label.lowercase()} reservations.",
                color = Color.White,
                modifier = Modifier.padding(start = 8.dp).testTag("reservations_empty")
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(start = 8.dp).testTag("reservations_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shown) { row ->
                    ReservationCard(row)
                }
            }
        }
    }
}

/**
 * One white card on the Reservations screen: the entrant, the campsite, the
 * dates and guests, and for a cancelled one who cancelled it and when.
 *
 * @param row the reservation to show
 */
@Composable
private fun ReservationCard(row: OwnerReservationRow) {
    val r = row.reservation
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().testTag("reservation_card_${r.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(row.entrantName, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(row.campsiteName, color = AppGreen, fontWeight = FontWeight.SemiBold)
            Text("${r.startDate} to ${r.endDate}", color = Color(0xFF5F6368))

            // Guests are optional, so only show them when we know them
            if (r.guests > 0) {
                Text("Guests: ${r.guests}", color = Color(0xFF5F6368))
            }

            // Cancelled cards also say who cancelled and when
            if (r.isCancelled()) {
                Text(cancelledByText(r), color = Color(0xFFB3261E), fontWeight = FontWeight.SemiBold)
                if (r.cancelledAt > 0L) {
                    val format = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
                    Text("On ${format.format(Date(r.cancelledAt))}", color = Color(0xFF5F6368))
                }
            }
        }
    }
}

/**
 * Loads everything the Reservations screen shows, in three steps:
 * 1. the owner's campsites (for their ids and names)
 * 2. every reservation for those campsites
 * 3. the names of the entrants who made them
 *
 * @param ownerId user id of the signed in campsite owner
 * @param onResult gets one row per reservation; empty if there are none
 */
fun loadOwnerReservations(ownerId: String, onResult: (List<OwnerReservationRow>) -> Unit) {
    // Step 1: the owner's campsites
    CampsiteRepository.getByOwner(ownerId) { campsites ->
        val campsiteNames = mutableMapOf<String, String>()
        for (campsite in campsites) {
            campsiteNames[campsite.getId()] = campsite.getCampsiteName()
        }

        // Step 2: their reservations
        ReservationRepository.getForCampsites(campsiteNames.keys.toList()) { reservations ->
            // Step 3: the entrants' names
            addNames(reservations, campsiteNames, onResult)
        }
    }
}

/**
 * Looks up the entrants' names and pairs each reservation with its names.
 * Shared by the Reservations screen and the booked dates screen.
 *
 * @param reservations the reservations to name
 * @param campsiteNames campsite id -> campsite name
 * @param onResult gets one row per reservation
 */
fun addNames(
    reservations: List<Reservation>,
    campsiteNames: Map<String, String>,
    onResult: (List<OwnerReservationRow>) -> Unit
) {
    // Each entrant only needs to be looked up once
    val entrantIds = reservations.map { it.entrantId }.distinct()

    LotteryRepository.getEntrantNames(entrantIds) { names ->
        val rows = mutableListOf<OwnerReservationRow>()
        for (r in reservations) {
            rows.add(
                OwnerReservationRow(
                    reservation = r,
                    entrantName = names[r.entrantId] ?: LotteryRepository.nameToShow(r.entrantId, null),
                    campsiteName = campsiteNames[r.campsiteId] ?: "Campsite"
                )
            )
        }
        onResult(rows)
    }
}
