package com.example.project_halfway


// Saves and loads campsites in the Firestore "campsites" collection.
// Uses the shared connection in FirebaseSecurity.

// Outstanding issues: no delete or update yet; errors are only printed;
// getById gives no answer if the id doesn't exist; not tested against the
//real database

object CampsiteRepository {

    // The "campsites" collection in Firestore (like a drawer of campsite folders).
    private fun campsites() = FirebaseSecurity.db.collection("campsites")

    // Save one campsite. A campsite with no id gets a new one from Firestore.
    fun save(campsite: CampsiteModel) {
        val document = if (campsite.id == "") campsites().document() else campsites().document(campsite.id)

        document.set(toMap(campsite))
            .addOnSuccessListener { println("Saved campsite ${document.id}") }
            .addOnFailureListener { println("Save failed: ${it.message}") }
    }

    // Load every campsite. Firestore answers later, so we pass in what to do with the list.
    fun getAll(onResult: (List<CampsiteModel>) -> Unit) {
        campsites().get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<CampsiteModel>()
                for (document in result.documents) {
                    val data = document.data
                    if (data != null) {
                        list.add(fromMap(document.id, data))
                    }
                }
                onResult(list)
            }
            .addOnFailureListener { println("Load failed: ${it.message}") }
    }

    // Load only the campsites that belong to one owner (for "my campsites").
    fun getByOwner(ownerId: String, onResult: (List<CampsiteModel>) -> Unit) {
        campsites().whereEqualTo("ownerId", ownerId).get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<CampsiteModel>()
                for (document in result.documents) {
                    val data = document.data
                    if (data != null) {
                        list.add(fromMap(document.id, data))
                    }
                }
                onResult(list)
            }
            .addOnFailureListener { println("Load failed: ${it.message}") }
    }

    // Load one campsite by its id.
    fun getById(id: String, onResult: (CampsiteModel) -> Unit) {
        campsites().document(id).get()
            .addOnSuccessListener { document ->
                val data = document.data
                if (data != null) {
                    onResult(fromMap(document.id, data))
                } else {
                    println("No campsite with id $id")
                }
            }
            .addOnFailureListener { println("Load failed: ${it.message}") }
    }

    // Turn a campsite into name -> value pairs, which is how Firestore stores it.
    fun toMap(c: CampsiteModel): Map<String, Any> {
        return mapOf(
            "ownerId" to c.ownerId,
            "campsiteName" to c.getCampsiteName(),
            "campgroundName" to c.getCampgroundName(),
            "location" to c.getCampsiteLocation(),
            "latitude" to c.getLatitude(),
            "longitude" to c.getLongitude(),
            "occupancy" to c.getOccupancy(),
            "type" to c.type,
            "services" to c.services,
            "pricePerNight" to c.pricePerNight,
            "userRating" to c.getUserRating(),
            "description" to c.getDescription(),
            "imageUrl" to c.getImageUrl(),
            "availableDateRanges" to c.availableDateRanges

        )
    }

    // Build a campsite back from name -> value pairs. The id is the document's name.
    fun fromMap(id: String, m: Map<String, Any>): CampsiteModel {
        return CampsiteModel(
            campsiteName = text(m, "campsiteName"),
            campgroundName = text(m, "campgroundName"),
            location = text(m, "location"),
            longitude = number(m, "longitude"),
            latitude = number(m, "latitude"),
            occupancy = number(m, "occupancy").toInt(),
            userRating = number(m, "userRating"),
            description = text(m, "description"),
            imageUrl = text(m, "imageUrl"),
            id = id,
            ownerId = text(m, "ownerId"),
            type = text(m, "type"),
            services = textList(m, "services"),
            pricePerNight = number(m, "pricePerNight"),
            availableDateRanges = dateRanges(m, "availableDateRanges")
        )
    }

    // Helpers: read a value, and use a blank or zero if it's missing.
    private fun text(m: Map<String, Any>, key: String): String {
        val value = m[key]
        return if (value is String) value else ""
    }

    private fun number(m: Map<String, Any>, key: String): Double {
        val value = m[key]
        return if (value is Number) value.toDouble() else 0.0
    }

    private fun textList(m: Map<String, Any>, key: String): List<String> {
        val value = m[key]
        if (value is List<*>) {
            return value.filterIsInstance<String>()
        }
        return listOf()
    }
       // Reads available date ranges stored in Firestore.

    private fun dateRanges(
        m: Map<String, Any>,
        key: String
    ): List<Map<String, String>> {

        val value = m[key]
        val ranges = mutableListOf<Map<String, String>>()

        if (value is List<*>) {
            for (item in value) {
                if (item is Map<*, *>) {
                    val start = item["start"]
                    val end = item["end"]

                    if (start is String && end is String) {
                        ranges.add(
                            mapOf("start" to start, "end" to end)
                        )
                    }
                }
            }
        }

        return ranges
    }
}