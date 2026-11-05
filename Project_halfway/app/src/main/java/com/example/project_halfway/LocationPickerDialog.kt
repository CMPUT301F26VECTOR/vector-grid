package com.example.project_halfway

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/*
 * Full-screen "Select location" pop-up where a campsite owner drops a pin on a Google
 * Map (US 02.01.02 when registering, US 02.01.03 when changing a campsite's location).
 * The owner taps or long-presses the map to drop the pin, can drag it to adjust it,
 * then presses Save. The pin gets a place name from ReverseGeocoder.
 * It is a pop-up rather than its own screen, so the Register Campsite form keeps
 * what the owner already typed. Based on SelectLocationScreen in the team's
 * vector-maps-demo project.
 *
 * Outstanding issues: there is no address search and no "use my location" button yet.
 */

private val PickerGreen = Color(0xFF566C11)

/**
 * The map the picker draws: a function given the current pin (or null) and a callback
 * for when the owner picks a new point. In the app it is a real Google Map. UI tests
 * replace it (with CompositionLocalProvider) with a fake map, so they can "pick" a
 * point without Google Play services.
 */
val LocalLocationPickerMap = staticCompositionLocalOf<@Composable (LatLng?, (LatLng) -> Unit) -> Unit> {
    { picked, onPick -> GoogleLocationPickerMap(picked, onPick) }
}

/**
 * Full-screen pop-up for choosing a campsite's location on a map.
 *
 * @param initial the pin to start with, or null to start with no pin over Alberta
 * @param title the heading at the top, e.g. "Select location"
 * @param onSave called with the chosen point and its place name (or its coordinates
 * when no name was found) when the owner presses Save
 * @param onDismiss called when the owner goes back without saving
 */
@Composable
fun LocationPickerDialog(
    initial: LatLng?,
    title: String = "Select location",
    onSave: (LatLng, String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            LocationPickerContent(initial, title, onSave, onDismiss)
        }
    }
}

// What is inside the pop-up: heading, map, the picked place and the Save button.
@Composable
private fun LocationPickerContent(
    initial: LatLng?,
    title: String,
    onSave: (LatLng, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickerMap = LocalLocationPickerMap.current

    var picked by remember { mutableStateOf(initial) }
    var placeName by remember { mutableStateOf("") }
    var findingName by remember { mutableStateOf(false) }
    var nameJob by remember { mutableStateOf<Job?>(null) }

    // Look up a place name for the pin. If the owner moves the pin again before the
    // answer comes back, the old answer is thrown away.
    fun lookUpName(point: LatLng) {
        nameJob?.cancel()
        findingName = true
        placeName = ""
        nameJob = scope.launch {
            val name = ReverseGeocoder.describe(context, point)
            if (picked == point) {
                placeName = name ?: formatLatLng(point.latitude, point.longitude)
                findingName = false
            }
        }
    }

    LaunchedEffect(Unit) {
        initial?.let { lookUpName(it) }
    }

    Column(
        modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss, modifier = Modifier.testTag("location_back_button")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "Tap the map to drop the pin. Hold the pin to drag it.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )

        val shape = RoundedCornerShape(16.dp)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(shape)
                .border(2.dp, Color(0xFF2E2E2E), shape),
        ) {
            if (LocalInspectionMode.current) {
                MapPlaceholder(Modifier.fillMaxSize(), label = "Tap to drop a pin")
            } else {
                pickerMap(picked) { point ->
                    picked = point
                    lookUpName(point)
                }
            }
        }

        // The picked place, or a hint if there is no pin yet
        val current = picked
        if (current == null) {
            Text("No pin yet", modifier = Modifier.testTag("picked_location_text"))
        } else {
            Text(
                if (findingName) "Finding place name..." else placeName,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("picked_location_text"),
            )
            Text(
                formatLatLng(current.latitude, current.longitude),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag("picked_coordinates_text"),
            )
        }

        Button(
            onClick = {
                val point = picked ?: return@Button
                onSave(point, placeName.ifBlank { formatLatLng(point.latitude, point.longitude) })
            },
            enabled = picked != null,
            colors = ButtonDefaults.buttonColors(containerColor = PickerGreen),
            modifier = Modifier.fillMaxWidth().testTag("location_save_button"),
        ) {
            Text("Save location")
        }
    }
}

/**
 * The real Google Map used by the picker. Tapping or long-pressing moves the pin there,
 * and dragging the pin reports where it was let go.
 *
 * @param picked the current pin, or null for no pin
 * @param onPick called with the new point whenever the owner picks one
 */
@Composable
fun GoogleLocationPickerMap(picked: LatLng?, onPick: (LatLng) -> Unit) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(picked ?: ALBERTA_CENTER, if (picked != null) 14f else 5f)
    }
    // A pin being dragged owns its own position, so it is remembered once here.
    val markerState = rememberSaveable(saver = MarkerState.Saver) {
        MarkerState(position = picked ?: ALBERTA_CENTER)
    }
    val currentOnPick by rememberUpdatedState(onPick)
    var mapLoaded by remember { mutableStateOf(false) }

    // The pin changed from outside (a tap): move the marker there.
    LaunchedEffect(picked) {
        if (picked != null && !markerState.isDragging) markerState.position = picked
    }
    // When the map first loads with a pin, fly the camera to it.
    LaunchedEffect(mapLoaded) {
        if (mapLoaded && picked != null) {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(picked, 14f), 800)
        }
    }
    // When the owner lets go of a dragged pin, report where it ended up.
    LaunchedEffect(markerState) {
        snapshotFlow { markerState.isDragging }
            .drop(1)
            .filter { dragging -> !dragging }
            .collect { currentOnPick(markerState.position) }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false),
        onMapLoaded = { mapLoaded = true },
        onMapClick = { currentOnPick(it) },
        onMapLongClick = { currentOnPick(it) },
    ) {
        if (picked != null) {
            Marker(state = markerState, draggable = true, title = "Campsite location")
        }
    }
}
