package com.example.project_halfway

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Register Campsite screen for the campsite owner (US 02.01.01).
 * The owner fills in the name, location, description and occupancy, then
 * presses Save. The screen builds a CampsiteModel and gives it to
 * CampsiteRepository to store in Firestore.
 *
 * Outstanding issues: the Upload and Location buttons from the mockup are
 * not built yet (US 02.01.02), so latitude and longitude are saved as 0.0.
 * The ownerId has to be passed in by the screen that opens this one.
 */
@Composable
fun RegisterCampsiteScreen(ownerId: String = "") {

    // What the owner has typed so far
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }

    // Message shown under the form (an error, or "Listing created")
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Register Campsite")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Campsite Name") },
            modifier = Modifier.fillMaxWidth().testTag("name_field")
        )

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth().testTag("location_field")
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth().testTag("description_field")
        )

        OutlinedTextField(
            value = occupancy,
            onValueChange = { occupancy = it },
            label = { Text("Occupancy") },
            modifier = Modifier.fillMaxWidth().testTag("occupancy_field")
        )

        Button(
            modifier = Modifier.testTag("save_button"),
            onClick = {
                // Turn the typed text into a number (null if it is not a number)
                val occupancyNumber = occupancy.toIntOrNull()

                if (name.isBlank()) {
                    message = "Please enter a campsite name."
                } else if (location.isBlank()) {
                    message = "Please enter a location."
                } else if (occupancyNumber == null || occupancyNumber < 1) {
                    message = "Please enter an occupancy of 1 or more."
                } else {
                    // The form is good, so build the campsite and save it
                    val campsite = CampsiteModel(
                        campsiteName = name,
                        campgroundName = "",
                        location = location,
                        longitude = 0.0,
                        latitude = 0.0,
                        occupancy = occupancyNumber,
                        userRating = 0.0,
                        description = description,
                        imageUrl = "",
                        ownerId = ownerId
                    )
                    CampsiteRepository.save(campsite)
                    message = "Listing created"
                }
            }
        ) {
            Text("Save")
        }

        Text(message, modifier = Modifier.testTag("message_text"))
    }
}