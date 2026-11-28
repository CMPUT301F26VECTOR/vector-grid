package com.example.project_halfway

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays the entrant's active reservations (US 01.05.01).
 *
 */

@Composable
fun ActiveReservationsTab() {
    val entrantId = FirebaseSecurity.auth.currentUser?.uid ?: ""

    val reservations = remember {
        mutableStateListOf<Reservation>()
    }

    val campsiteNames = remember {
        mutableStateMapOf<String, String>()
    }

    var loading by remember { mutableStateOf(true) }

    val today = SimpleDateFormat(
        "yyyy-MM-dd", Locale.CANADA
    ).format(Date())

    LaunchedEffect(entrantId) {
        if (entrantId.isBlank()) {
            loading = false
        } else {
            ReservationRepository.getForEntrant(entrantId) { found ->
                reservations.clear()
                reservations.addAll(
                    found.filter { it.isActive(today) }
                )

                for (reservation in reservations) {
                    CampsiteRepository.getById(
                        reservation.campsiteId
                    ) { campsite ->
                        campsiteNames[reservation.campsiteId] =
                            campsite.getCampsiteName()
                    }
                }

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
        Text(
            "Active reservations",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (entrantId.isBlank()) {
            Text("Please sign in first", color = Color.White)
        } else if (loading) {
            Text("Loading...", color = Color.White)
        } else if (reservations.isEmpty()) {
            Text(
                "You have no active reservations",
                color = Color.White
            )
        }

        for (reservation in reservations) {
            ActiveReservationCard(
                reservation = reservation,
                campsiteName = campsiteNames[reservation.campsiteId]
                    ?: "Campsite"
            )
        }
    }
}

@Composable
fun ActiveReservationCard(
    reservation: Reservation,
    campsiteName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                campsiteName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text("Start date: ${reservation.startDate}")
            Text("End date: ${reservation.endDate}")
            Text("Guests: ${reservation.guests}")

            Text(
                "Total cost: $" + String.format(
                    Locale.CANADA,
                    "%.2f",
                    reservation.totalCost
                )
            )
        }
    }
}