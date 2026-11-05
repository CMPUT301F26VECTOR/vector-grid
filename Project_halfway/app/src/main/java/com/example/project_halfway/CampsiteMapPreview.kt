package com.example.project_halfway

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.project_halfway.ui.theme.Project_halfwayTheme
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

/*
 * The small Google Map on the campsite details card, which shows where the campsite is
 * (US 01.01.05 for entrants, US 02.01.04 for owners). Also holds the map helpers that
 * LocationPickerDialog shares: ALBERTA_CENTER, MapPlaceholder and LocalShowRealMaps.
 * Based on MapPreview in the team's vector-maps-demo project.
 *
 * Outstanding issues: needs a Google Maps API key in local.properties (see README);
 * without one the map is an empty grey grid.
 */

/** Centre of Alberta, where maps start when there is no pin yet. */
val ALBERTA_CENTER = LatLng(53.9333, -116.5765)

private val MapGreen = Color(0xFF566C11)
private val MapBorder = Color(0xFF2E2E2E)
private val MapPlaceholderColor = Color(0xFFE8F0DC)

/**
 * Whether to draw real Google Maps. It is true in the app. UI tests set it to false
 * (with CompositionLocalProvider) so they draw MapPlaceholder instead, and don't
 * depend on Google Play services or an API key.
 */
val LocalShowRealMaps = staticCompositionLocalOf { true }

/**
 * Gives the campsite's map position, or null if the owner hasn't put it on the map.
 *
 * @return the campsite's latitude and longitude, or null
 */
fun CampsiteModel.mapPosition(): LatLng? =
    if (hasMapLocation()) LatLng(getLatitude(), getLongitude()) else null

/**
 * A small map that can't be moved, with one pin. It uses Google's Lite mode, a cheap
 * still image of the map. Shows "No location yet" when [position] is null.
 *
 * @param position where the pin goes, or null if there is no location
 * @param modifier sets the size of the map
 * @param zoom how close in the map starts (higher is closer)
 * @param onClick called when the map is tapped
 */
@Composable
fun CampsiteMapPreview(
    position: LatLng?,
    modifier: Modifier = Modifier,
    zoom: Float = 12f,
    onClick: () -> Unit = {},
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .clip(shape)
            .border(2.dp, MapBorder, shape)
            .testTag("campsite_map"),
    ) {
        when {
            position == null -> NoLocationPlaceholder(Modifier.fillMaxSize().clickable(onClick = onClick))
            LocalInspectionMode.current || !LocalShowRealMaps.current -> MapPlaceholder(Modifier.fillMaxSize())
            else -> LiteMap(position, zoom, onClick)
        }
    }
}

// The real Lite mode map, which follows the pin when it moves (after "Change location").
@Composable
private fun LiteMap(position: LatLng, zoom: Float, onClick: () -> Unit) {
    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, zoom)
    }
    LaunchedEffect(position, zoom) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(position, zoom)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = { GoogleMapOptions().liteMode(true) },
        uiSettings = MapUiSettings(mapToolbarEnabled = false, zoomControlsEnabled = false),
        onMapClick = { onClick() },
    ) {
        Marker(state = rememberUpdatedMarkerState(position = position))
    }
}

/**
 * Stands in for a map in Android Studio's preview pane and in UI tests, where real
 * maps can't be drawn.
 *
 * @param modifier sets the size of the box
 * @param label the text under the pin icon
 */
@Composable
fun MapPlaceholder(modifier: Modifier = Modifier, label: String = "Map preview") {
    Box(modifier.background(MapPlaceholderColor), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Filled.Place, contentDescription = null, tint = MapGreen, modifier = Modifier.size(40.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MapGreen)
        }
    }
}

// Shown instead of a map when the campsite has no location.
@Composable
private fun NoLocationPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier.background(Color(0xFFF4F4F4)).padding(8.dp).testTag("no_location_placeholder"),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Filled.LocationOff, contentDescription = null, tint = Color.Gray)
            Text(
                "No location yet",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

// Preview of the map with and without a location.
@Preview(showBackground = true)
@Composable
private fun CampsiteMapPreviewPreview() {
    Project_halfwayTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CampsiteMapPreview(LatLng(51.1784, -115.5708), Modifier.fillMaxWidth().height(150.dp))
            CampsiteMapPreview(null, Modifier.fillMaxWidth().height(150.dp))
        }
    }
}
