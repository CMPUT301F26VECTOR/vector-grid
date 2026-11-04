package com.example.project_halfway

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

/**
 * Opens the Google Maps app for things this app doesn't build by itself: directions to a
 * campsite, and showing its pin in Google Maps (US 01.01.05, 02.01.04).
 *
 * Uses Google's documented "Maps URLs" format, so it needs no extra API or billing,
 * and it switches to a browser when the Maps app isn't installed.
 * Based on GoogleMapsLinks in the team's vector-maps-demo project.
 *
 * Outstanding issues: directions always use driving mode.
 */
object GoogleMapsLinks {

    private const val MAPS_PACKAGE = "com.google.android.apps.maps"

    /**
     * Builds a link that asks Google Maps for a route from the user's position to a place.
     *
     * @param destLat latitude of the destination
     * @param destLng longitude of the destination
     * @param travelMode "driving", "walking", "bicycling" or "transit"
     * @return the Maps URL
     */
    fun directionsUrl(destLat: Double, destLng: Double, travelMode: String = "driving"): String =
        "https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng&travelmode=$travelMode"

    /**
     * Builds a link that shows a pin at a place in Google Maps.
     *
     * @param lat latitude of the place
     * @param lng longitude of the place
     * @return the Maps URL
     */
    fun pinUrl(lat: Double, lng: Double): String =
        "https://www.google.com/maps/search/?api=1&query=$lat,$lng"

    /**
     * Opens Google Maps with directions to a place.
     *
     * @param context used to start the Maps app or browser
     * @param destination where to go
     */
    fun openDirections(context: Context, destination: LatLng) =
        open(context, directionsUrl(destination.latitude, destination.longitude))

    /**
     * Opens Google Maps with a pin at a place.
     *
     * @param context used to start the Maps app or browser
     * @param position where to put the pin
     */
    fun openPin(context: Context, position: LatLng) =
        open(context, pinUrl(position.latitude, position.longitude))

    // Tries the Maps app first, then any browser, then tells the user nothing could open it.
    private fun open(context: Context, url: String) {
        val inMapsApp = Intent(Intent.ACTION_VIEW, url.toUri()).setPackage(MAPS_PACKAGE)
        try {
            context.startActivity(inMapsApp)
        } catch (e: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No app can open Google Maps links", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

/**
 * Writes coordinates as text, for example "51.19130, -115.53670". It always uses a dot
 * for decimals, whatever the phone's language is.
 *
 * @param lat the latitude
 * @param lng the longitude
 * @return the coordinates with 5 decimal places
 */
fun formatLatLng(lat: Double, lng: Double): String =
    String.format(Locale.US, "%.5f, %.5f", lat, lng)
