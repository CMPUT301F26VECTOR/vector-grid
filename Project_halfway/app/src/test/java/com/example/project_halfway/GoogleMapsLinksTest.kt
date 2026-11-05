package com.example.project_halfway

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

/**
 * Unit tests for GoogleMapsLinks and formatLatLng: the links the Directions and
 * Open in Google Maps buttons open (US 01.01.05), and how coordinates are shown.
 * Based on DistanceAndLinksTest in the team's vector-maps-demo project.
 */
class GoogleMapsLinksTest {

    @Test
    fun directionsUrl_usesMapsUrlsFormat() {
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=52.8507,-118.077&travelmode=driving",
            GoogleMapsLinks.directionsUrl(52.8507, -118.077)
        )
    }

    @Test
    fun directionsUrl_canWalk() {
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=51.1784,-115.5708&travelmode=walking",
            GoogleMapsLinks.directionsUrl(51.1784, -115.5708, travelMode = "walking")
        )
    }

    @Test
    fun pinUrl_usesMapsUrlsFormat() {
        assertEquals(
            "https://www.google.com/maps/search/?api=1&query=49.0491,-113.9089",
            GoogleMapsLinks.pinUrl(49.0491, -113.9089)
        )
    }

    @Test
    fun formatLatLng_usesFiveDecimalsAndADot() {
        assertEquals("51.19130, -115.53670", formatLatLng(51.1913, -115.5367))
    }

    @Test
    fun formatLatLng_usesADotEvenInFrench() {
        val before = Locale.getDefault()
        try {
            Locale.setDefault(Locale.FRANCE) // French writes decimals with a comma
            assertEquals("51.19130, -115.53670", formatLatLng(51.1913, -115.5367))
        } finally {
            Locale.setDefault(before)
        }
    }
}
