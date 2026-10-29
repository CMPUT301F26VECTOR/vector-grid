package com.example.project_halfway

/**
 * One campsite listing, saved in the Firestore "campsites" collection.
 * Every field has a default value so Firestore can rebuild it from a document.
 * Outstanding issues: location is text plus coordinates; no image upload yet.
 */
data class CampsiteClass(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val locationName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val maxOccupancy: Int = 0,
    val type: String = "",
    val services: List<String> = emptyList(),
    val pricePerNight: Double = 0.0,
    val averageRating: Double = 0.0,
    val description: String = "",
    val imageUrl: String = ""
)