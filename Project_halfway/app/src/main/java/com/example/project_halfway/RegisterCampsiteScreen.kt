package com.example.project_halfway

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng

/**
 * Register Campsite screen for the campsite owner (US 02.01.01, US 02.01.02).
 * The owner fills in the campsite's details and presses Save. CampsiteForm
 * checks what was typed and builds a CampsiteModel, then CampsiteRepository
 * stores it in Firestore so entrants can find it.
 *
 * The campsite's map location is chosen by dropping a pin on a Google Map in
 * LocationPickerDialog. It is optional: a campsite saved without a pin shows
 * "No location yet" on its details.
 *
 * Outstanding issues: the Upload button from the mockup is not built yet, so
 * no image is saved. The ownerId has to be passed in by the screen that
 * opens this one (the owner home screen).
 *
 * The screen uses the app's green look from Browse Campsites: a white title
 * with a back arrow, the boxes in a white panel, and a white Save button.
 *
 * @param ownerId user id of the signed in campsite owner; saving is refused if blank
 * @param onBackClick called when the back arrow is pressed; null shows no arrow
 */
@Composable
fun RegisterCampsiteScreen(ownerId: String = "", onBackClick: (() -> Unit)? = null) {

    // What the owner has typed so far, one variable per box
    var name by remember { mutableStateOf("") }
    var campgroundName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var occupancy by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var services by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    // The map pin, kept as text because CampsiteForm checks and converts it
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var placeName by remember { mutableStateOf("") }
    var availableDates by remember { mutableStateOf(listOf<Map<String, String>>()) }

    // true while the calendar pop-up is open
    var showDatePicker by remember { mutableStateOf(false) }

    // true while the map pop-up is open
    var showLocationPicker by remember { mutableStateOf(false) }

    // Message shown under the Save button (an error, "Saving..." or "Listing created")
    var message by remember { mutableStateOf("") }

    LightStatusBarIcons()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGreen)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {

        GreenScreenHeader(
            title = "Register Campsite",
            onBackClick = onBackClick,
            backButtonTag = "owner_back_button"
        )
        Spacer(modifier = Modifier.height(HeaderGap))

        // The boxes scroll; the Save button below them always stays on screen.
        WhitePanel(modifier = Modifier.weight(1f), scrollable = true) {
            PanelLabel("Campsite details", first = true)
            FormField("Campsite Name", name, { name = it }, "name_field")
            FormField("Location (e.g. Edmonton, AB)", location, { location = it }, "location_field")
            FormField("Description", description, { description = it }, "description_field", singleLine = false)
            FormField("Occupancy", occupancy, { occupancy = it }, "occupancy_field", KeyboardType.Number)
            FormField("Campground Name", campgroundName, { campgroundName = it }, "campground_field")
            FormField("Site Type (e.g. Tent site)", type, { type = it }, "type_field")
            FormField("Services, separated by commas", services, { services = it }, "services_field")
            FormField("Tags, separated by commas", tags, { tags = it }, "tags_field")
            FormField("Price per night ($)", price, { price = it }, "price_field", KeyboardType.Decimal)

            // Map location: a pin dropped on a Google Map (optional)
            PanelLabel("Map location (optional)")
            val lat = latitude.toDoubleOrNull()
            val lng = longitude.toDoubleOrNull()
            Text(
                if (lat == null || lng == null) "No map location yet."
                else "$placeName (${formatLatLng(lat, lng)})",
                color = Color.Gray,
                modifier = Modifier.testTag("map_location_text")
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { showLocationPicker = true },
                    border = BorderStroke(1.dp, AppGreen),
                    colors = greenOutlinedButtonColors(),
                    modifier = Modifier.testTag("set_location_button")
                ) {
                    Text(if (lat == null) "Set location on map" else "Change location")
                }
                if (lat != null) {
                    TextButton(
                        onClick = { latitude = ""; longitude = ""; placeName = "" },
                        modifier = Modifier.testTag("remove_location_button")
                    ) {
                        Text("Remove", color = AppGreen)
                    }
                }
            }

            // Available dates: one row per range, each with a Remove button
            PanelLabel("Available dates")
            if (availableDates.isEmpty()) {
                Text("No dates added yet.", color = Color.Gray, modifier = Modifier.testTag("no_dates_text"))
            }
            for (range in availableDates) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${range["start"]} to ${range["end"]}", modifier = Modifier.weight(1f))
                    TextButton(onClick = { availableDates = availableDates - range }) {
                        Text("Remove", color = AppGreen)
                    }
                }
            }
            OutlinedButton(
                onClick = { showDatePicker = true },
                border = BorderStroke(1.dp, AppGreen),
                colors = greenOutlinedButtonColors(),
                modifier = Modifier.testTag("add_dates_button")
            ) {
                Text("Add available dates")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Button(
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppGreen),
            onClick = {
                val form = CampsiteForm(
                    name = name,
                    campgroundName = campgroundName,
                    location = location,
                    description = description,
                    occupancy = occupancy,
                    type = type,
                    services = services,
                    tags = tags,
                    pricePerNight = price,
                    latitude = latitude,
                    longitude = longitude,
                    availableDates = availableDates
                )
                val error = form.firstError()

                if (error != null) {
                    message = error
                } else if (ownerId.isBlank()) {
                    // Without an owner id the campsite would never show in "My Campsites"
                    message = "Please sign in as a campsite owner first."
                } else {
                    message = "Saving..."
                    CampsiteRepository.save(form.toCampsite(ownerId)) { worked ->
                        message = if (worked) "Listing created" else "Could not save, please try again."
                    }
                }
            }
        ) {
            Text("Save", fontWeight = FontWeight.Bold)
        }

        Text(
            message,
            color = Color.White,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp).testTag("message_text")
        )
    }

    if (showLocationPicker) {
        val lat = latitude.toDoubleOrNull()
        val lng = longitude.toDoubleOrNull()
        LocationPickerDialog(
            initial = if (lat != null && lng != null) LatLng(lat, lng) else null,
            onDismiss = { showLocationPicker = false },
            onSave = { point, name ->
                latitude = point.latitude.toString()
                longitude = point.longitude.toString()
                placeName = name
                // Fill in the Location box from the pin if the owner left it empty and
                // a real place name was found (not just the coordinates).
                if (location.isBlank() && name != formatLatLng(point.latitude, point.longitude)) {
                    location = name
                }
                showLocationPicker = false
            }
        )
    }

    if (showDatePicker) {
        AddDatesDialog(
            onDismiss = { showDatePicker = false },
            onAdd = { range ->
                availableDates = availableDates + range
                showDatePicker = false
            }
        )
    }
}

/**
 * One labelled text box. Keeps the screen above short, since every box
 * looks the same apart from its label, value and test tag.
 */
@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = greenFieldColors(),
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp).testTag(tag)
    )
}

/**
 * Calendar pop-up for choosing one range of available dates.
 * Add only turns on once both a start and an end date are picked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDatesDialog(
    onDismiss: () -> Unit,
    onAdd: (Map<String, String>) -> Unit
) {
    val state = rememberDateRangePickerState()
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = start != null && end != null,
                onClick = {
                    if (start != null && end != null) {
                        onAdd(CampsiteForm.dateRange(start, end))
                    }
                }
            ) {
                Text("Add", color = if (start != null && end != null) AppGreen else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AppGreen) }
        }
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.height(480.dp),
            showModeToggle = false
        )
    }
}
