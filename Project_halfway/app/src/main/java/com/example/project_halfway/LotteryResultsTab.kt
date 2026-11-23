package com.example.project_halfway

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.AlertDialog

/**
 * The Lottery tab of My Reservations & Entries: the entrant's draw results
 * (US 02.05.01). Each result says whether they won. A winner can press
 * Reserve dates and pick their stay, but only inside the lottery's booking
 * period (US 02.04.04); the reservation is then shown on the card.
 *
 * Outstanding issues: a reservation can't be changed or cancelled yet
 * (US 01.07.01), and multi-campsite lotteries aren't supported (US 01.04.02).
 */
@Composable
fun LotteryResultsTab() {
    val entrantId = FirebaseSecurity.auth.currentUser?.uid ?: ""

    val results = remember { mutableStateListOf<LotteryResultNotification>() }
    // campsite id -> name, and lottery id -> the entrant's reservation for it
    val campsiteNames = remember { mutableStateMapOf<String, String>() }
    val reservations = remember { mutableStateMapOf<String, Reservation>() }
    var loading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }
    // the win the entrant is reserving dates for, while the calendar is open
    var reserving by remember { mutableStateOf<LotteryResultNotification?>(null) }
    // The winning offer the entrant wants to decline (US 01.04.03).
    var declining by remember { mutableStateOf<LotteryResultNotification?>(null) }

    LaunchedEffect(entrantId) {
        if (entrantId == "") {
            loading = false
            message = "Please sign in first"
            return@LaunchedEffect
        }
        ReservationRepository.getNotifications(entrantId) { found ->
            results.clear()
            results.addAll(found)
            for (result in found) {
                CampsiteRepository.getById(result.campsiteId) { campsite ->
                    campsiteNames[result.campsiteId] = campsite.getCampsiteName()
                }
            }
            ReservationRepository.getForEntrant(entrantId) { list ->
                for (reservation in list) reservations[reservation.lotteryId] = reservation
                loading = false
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
        } else if (results.isEmpty() && message == "") {
            Text("No lottery results yet. They show here once a lottery you joined is drawn.", color = Color.White)
        }

        for (result in results.toList()) {
            val reservation = reservations[result.lotteryId]
            LotteryResultCard(
                campsiteName = campsiteNames[result.campsiteId] ?: "Campsite",
                message = result.message,
                isWin = result.isWin(),
                reservedText = reservation?.let { WaitlistHelper.reservedText(it) },
                offerStatus = result.offerStatus,
                onReserve = { reserving = result },
                onDecline = { declining = result }
            )
        }

        if (message != "") {
            Text(message, color = Color.White, modifier = Modifier.padding(top = 8.dp))
        }
    }

    val win = reserving
    if (win != null) {
        ReserveDatesDialog(
            entrantId = entrantId,
            lotteryId = win.lotteryId,
            onDismiss = { reserving = null },
            onReserved = { reservation ->
                reservations[reservation.lotteryId] = reservation
                reserving = null
                message = "Your dates are reserved"
            }
        )
    }
    // Confirms that the entrant wants to decline their winning offer (US 01.04.03).
    val offer = declining

    if (offer != null) {
        var decliningInProgress by remember { mutableStateOf(false) }


        DeclineOfferConfirmationDialog(
            decliningInProgress = decliningInProgress,
            onDismiss = {
                declining = null
            },
            onConfirm = {
                decliningInProgress = true

                ReservationRepository.declineOffer(
                    notificationId = offer.id,
                    entrantId = entrantId
                ) { success ->
                    decliningInProgress = false

                    if (success) {
                        val index = results.indexOfFirst {
                            it.id == offer.id
                        }

                        if (index != -1) {
                            results[index] = results[index].copy(
                                offerStatus =
                                    LotteryResultNotification.OFFER_DECLINED
                            )
                        }

                        declining = null
                        message = "Reservation offer declined"
                    } else {
                        declining = null
                        message = "Could not decline the offer. Please try again."
                    }
                }
            }
        )

    }
}


/**
 * Confirmation dialog for declining a winning lottery offer.
 * US 01.04.03 - Acceptance Criterion 2.
 *
 * @param decliningInProgress true while saving the decline
 * @param onDismiss called when the entrant keeps the offer
 * @param onConfirm called when the entrant confirms declining
 */
@Composable
fun DeclineOfferConfirmationDialog(
    decliningInProgress: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!decliningInProgress) onDismiss()
        },
        title = {
            Text("Decline reservation offer?")
        },
        text = {
            Text(
                "Are you sure you want to decline? " +
                        "You won't be able to reserve dates " +
                        "for this offer afterward."
            )
        },
        confirmButton = {
            TextButton(
                enabled = !decliningInProgress,
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_decline")
            ) {
                Text("Decline", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(
                enabled = !decliningInProgress,
                onClick = onDismiss,
                modifier = Modifier.testTag("keep_offer")
            ) {
                Text("Keep offer")
            }
        }
    )
}


/**
 * One draw result: the campsite, whether the entrant won, and for a winner
 * either their reserved dates or a Reserve dates button. Does not talk to
 * Firestore, so it can be tested on its own.
 *
 * @param campsiteName name of the lottery's campsite
 * @param message the notification text
 * @param isWin true if the entrant won
 * @param reservedText e.g. "Reserved: 2026-11-10 to 2026-11-12", or null if not reserved yet
 * @param offerStatus the winner's reservation offer status (US 01.04.03)
 * @param onReserve called when Reserve dates is pressed
 * @param onDecline called when Decline offer is pressed (US 01.04.03)
 */
@Composable
fun LotteryResultCard(
    campsiteName: String,
    message: String,
    isWin: Boolean,
    reservedText: String?,
    offerStatus: String = LotteryResultNotification.OFFER_PENDING,
    onDecline: () -> Unit = {},
    onReserve: () -> Unit = {}

) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(campsiteName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                text = if (isWin) "You won!" else "Not selected",
                fontWeight = FontWeight.Bold,
                color = if (isWin) AppGreen else Color.DarkGray,
                modifier = Modifier.padding(top = 4.dp).testTag("result_status")
            )
            Text(message, color = Color.DarkGray, modifier = Modifier.padding(top = 2.dp))
            if (isWin) {
                if (reservedText != null) {
                    Text(
                        reservedText,
                        fontWeight = FontWeight.Bold,
                        color = AppGreen,
                        modifier = Modifier.padding(top = 8.dp).testTag("reserved_text")
                    )
                } else if (offerStatus == LotteryResultNotification.OFFER_DECLINED) {

                    /** Shows that the winner has declined their reservation offer. */
                    Text(
                        text = "Offer declined",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag("offer_declined")
                    )

                } else {

                    /** Allows a winner to reserve dates or decline their offer (US 01.04.03). */
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onReserve,
                            colors = greenButtonColors(),
                            modifier = Modifier.testTag("reserve_button")
                        ) {
                            Text("Reserve dates")
                        }

                        OutlinedButton(
                            onClick = onDecline,
                            modifier = Modifier.testTag("decline_button")
                        ) {
                            Text("Decline offer")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Calendar pop-up for a winner to pick their stay (US 02.04.04). Loads the
 * lottery first, then only lets the winner pick days inside its booking
 * period; Reserve stays off until a valid stay is picked.
 *
 * @param entrantId the winner's id
 * @param lotteryId the lottery they won
 * @param onDismiss called when Cancel is pressed
 * @param onReserved called with the saved reservation
 */
@Composable
fun ReserveDatesDialog(
    entrantId: String,
    lotteryId: String,
    onDismiss: () -> Unit,
    onReserved: (Reservation) -> Unit
) {
    var lottery by remember { mutableStateOf<Lottery?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(lotteryId) {
        LotteryRepository.getById(lotteryId) { found ->
            if (found == null) loadFailed = true else lottery = found
        }
    }

    val loaded = lottery
    if (loaded == null) {
        // Nothing to pick from until the lottery (and its booking period) has loaded
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = AppGreen) } }
        ) {
            Text(
                if (loadFailed) "Could not load the lottery. Try again later." else "Loading the booking period...",
                modifier = Modifier.padding(24.dp)
            )
        }
    } else {
        BookingPeriodPicker(entrantId, loaded, onDismiss, onReserved)
    }
}

// The calendar itself, made once the lottery is known so it can grey out days
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingPeriodPicker(
    entrantId: String,
    lottery: Lottery,
    onDismiss: () -> Unit,
    onReserved: (Reservation) -> Unit
) {
    var campsite by remember { mutableStateOf<CampsiteModel?>(null) }
    LaunchedEffect(lottery.campsiteId) {
        CampsiteRepository.getById(lottery.campsiteId) { found ->
            campsite = found
        }
    }
    val rulesText = campsite?.getRules() ?: DEFAULT_CAMPSITE_RULES

    val bookable = remember(lottery) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = lottery.isBookableDay(utcTimeMillis)
        }
    }
    val state = rememberDateRangePickerState(
        initialDisplayedMonthMillis = if (lottery.bookingStartDate > 0L) lottery.bookingStartDate else null,
        selectableDates = bookable
    )
    var rulesAgreed by remember { mutableStateOf(false) }
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis
    val valid = start != null && end != null && lottery.isWithinBookingPeriod(start, end) && rulesAgreed
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = valid && !saving,
                onClick = {
                    if (start != null && end != null && lottery.isWithinBookingPeriod(start, end)) {
                        saving = true
                        val reservation = Reservation(
                            entrantId = entrantId,
                            lotteryId = lottery.id,
                            campsiteId = lottery.campsiteId,
                            startDate = CampsiteForm.millisToDate(start),
                            endDate = CampsiteForm.millisToDate(end),
                            createdAt = System.currentTimeMillis(),
                            rulesAgreed = rulesAgreed
                        )
                        ReservationRepository.save(reservation) { saved ->
                            saving = false
                            if (saved != null) onReserved(saved) else error = "Could not reserve, try again."
                        }
                    }
                }
            ) {
                Text("Reserve", color = if (valid) AppGreen else Color.Gray)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = AppGreen) } }
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Text(
                "Booking period: " + WaitlistHelper.bookingPeriodText(lottery),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp).testTag("booking_period_text")
            )
            if (error.isNotEmpty()) {
                Text(error, color = Color(0xFFB3261E), modifier = Modifier.padding(horizontal = 24.dp))
            }
            DateRangePicker(
                state = state,
                modifier = Modifier.height(460.dp),
                showModeToggle = false
            )

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    text = "Campsite Rules & Policies",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = rulesText,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rulesAgreed = !rulesAgreed }
                ) {
                    Checkbox(
                        checked = rulesAgreed,
                        onCheckedChange = { rulesAgreed = it },
                        modifier = Modifier.testTag("rules_checkbox")
                    )
                    Text(
                        text = "I have read and agree to the campsite rules",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

/** Android Studio preview of a winner's and a non-winner's result. */
@Preview(showBackground = true)
@Composable
private fun LotteryResultCardPreview() {
    Column(Modifier.padding(16.dp)) {
        LotteryResultCard(
            "Site 7 – Upper Waterton",
            LotteryResultNotification.WON_MESSAGE,
            true,
            null,
            LotteryResultNotification.OFFER_PENDING,
            {},
            {}
        )

        LotteryResultCard(
            "Site 42 - Lakeview",
            LotteryResultNotification.NOT_SELECTED_MESSAGE,
            false,
            null,
            LotteryResultNotification.OFFER_PENDING,
            {},
            {}
        )
    }
}
