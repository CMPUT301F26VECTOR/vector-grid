package com.example.project_halfway

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Turns a dropped map pin into a readable place name, such as "Banff, Alberta", for the
 * location picker (US 02.01.02). Uses Android's built-in Geocoder, which is free and
 * needs no extra Google API.
 * Based on ReverseGeocoder in the team's vector-maps-demo project.
 *
 * Outstanding issues: the Geocoder sometimes finds nothing, especially on emulators,
 * so callers show the coordinates instead.
 */
object ReverseGeocoder {

    /**
     * Looks up a place name for a point on the map.
     *
     * @param context any context; only its application context is kept
     * @param latLng the point to look up
     * @return a name like "Banff, Alberta", or null if none was found
     */
    suspend fun describe(context: Context, latLng: LatLng): String? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context.applicationContext, Locale.getDefault())
        val address = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                firstAddressAsync(geocoder, latLng)
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.firstOrNull()
                }
            }
        } catch (e: java.io.IOException) {
            null // no network, or the geocoder service is unavailable
        } catch (e: IllegalArgumentException) {
            null
        }
        return address?.let(::format)
    }

    // Android 13 and newer: the listener version, which doesn't block a thread.
    private suspend fun firstAddressAsync(geocoder: Geocoder, latLng: LatLng): Address? =
        suspendCancellableCoroutine { cont ->
            geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (cont.isActive) cont.resume(addresses.firstOrNull())
                    }

                    override fun onError(errorMessage: String?) {
                        if (cont.isActive) cont.resume(null)
                    }
                },
            )
        }

    // "Banff, Alberta": the town (or county, or feature) and the province.
    private fun format(address: Address): String? {
        val place = address.locality ?: address.subAdminArea ?: address.featureName
        return listOfNotNull(place, address.adminArea)
            .distinct()
            .joinToString(", ")
            .ifBlank { null }
    }
}
