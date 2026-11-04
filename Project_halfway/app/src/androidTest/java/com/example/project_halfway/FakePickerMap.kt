package com.example.project_halfway

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.google.android.gms.maps.model.LatLng

/**
 * A stand-in for the Google Map in LocationPickerDialog, for UI tests. Tests give it
 * to the picker with CompositionLocalProvider(LocalLocationPickerMap provides
 * FakePickerMap.map), so they can "drop a pin" without Google Play services or an
 * API key. It is one button (tag "fake_map_pick") that picks [JASPER].
 */
object FakePickerMap {

    /** The point the fake map picks: Whistlers Campground, Jasper National Park. */
    val JASPER = LatLng(52.8507, -118.077)

    /** The fake map: pressing its button picks [JASPER]. */
    val map: @Composable (LatLng?, (LatLng) -> Unit) -> Unit = { _, onPick ->
        Button(onClick = { onPick(JASPER) }, modifier = Modifier.testTag("fake_map_pick")) {
            Text("Drop pin at Jasper")
        }
    }
}
