package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import java.util.Locale

/**
 * Displays successful reservation details (US 01.05.01).
 *
 *
 */
@Composable
fun ReservationConfirmationDialog(
    reservation: Reservation,
    campsiteName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Reservation confirmed!")
        },
        text = {
            Column {
                Text("Campsite: $campsiteName")
                Text("Start date: ${reservation.startDate}")
                Text("End date: ${reservation.endDate}")
                Text("Number of guests: ${reservation.guests}")
                Text(
                    "Total cost: $" +
                            String.format(
                                Locale.CANADA,
                                "%.2f",
                                reservation.totalCost
                            )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

