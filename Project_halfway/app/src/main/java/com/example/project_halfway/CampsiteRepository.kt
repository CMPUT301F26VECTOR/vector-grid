package com.example.project_halfway


/**
 * Saves and loads campsites in the Firestore "campsites" collection.
 * Uses the shared connection in FirebaseSecurity.
 *
 * Used for US 02.01.01 (register a campsite), US 02.03.01 (owner's campsites)
 * and the entrant browse and search stories.
 *
 * Outstanding issues: no delete or update yet; only save and getAll report
 * failures, the other loads just print them; getById gives no answer if the
 * id doesn't exist; not tested against the real database.
 */
object CampsiteRepository {

    // The "campsites" collection in Firestore (like a drawer of campsite folders).
    private fun campsites() = FirebaseSecurity.db.collection("campsites")

    /**
     * Saves one campsite. A campsite with no id gets a new one from Firestore.
     *
     * @param campsite the campsite to save
     * @param onDone gets true once Firestore has saved it, or false if it failed.
     * It is optional, so older calls like save(campsite) still work.
     */
    fun save(campsite: CampsiteModel, onDone: (Boolean) -> Unit = {}) {
        val document = if (campsite.getId() == "") campsites().document() else campsites().document(campsite.getId())

        document.set(toMap(campsite))
            .addOnSuccessListener {
                println("Saved campsite ${document.id}")
                onDone(true)
            }
            .addOnFailureListener {
                println("Save failed: ${it.message}")
                onDone(false)
            }
    }

    /**
     * Loads every campsite. Firestore answers later, so we pass in what to do with the list.
     *
     * onFailure comes first on purpose: a block written after the brackets,
     * getAll { list -> ... }, always goes to the last parameter, so older calls
     * written that way still mean onResult.
     *
     * @param onFailure gets Firestore's error message if loading fails; optional
     * @param onResult gets every campsite; not called if loading fails
     */
    fun getAll(onFailure: (String) -> Unit = {}, onResult: (List<CampsiteModel>) -> Unit) {
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
            .addOnFailureListener {
                println("Load failed: ${it.message}")
                onFailure(it.message ?: "unknown error")
            }
    }

    /**
     * Loads only the campsites that belong to one owner (for "my campsites").
     *
     * @param ownerId the owner's user id
     * @param onResult gets that owner's campsites; not called if loading fails
     */
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

    /**
     * Loads one campsite by its id.
     *
     * @param id the campsite's Firestore document id
     * @param onResult gets the campsite; not called if it doesn't exist or loading fails
     */
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

    /**
     * Turns a campsite into name -> value pairs, which is how Firestore stores it.
     * The id is not included; it is the document's name.
     *
     * @param c the campsite
     * @return the campsite's fields as a map
     */
    fun toMap(c: CampsiteModel): Map<String, Any> {
        return mapOf(
            "ownerId" to c.getOwnerId(),
            "campsiteName" to c.getCampsiteName(),
            "campgroundName" to c.getCampgroundName(),
            "location" to c.getCampsiteLocation(),
            "latitude" to c.getLatitude(),
            "longitude" to c.getLongitude(),
            "occupancy" to c.getOccupancy(),
            "type" to c.getType(),
            "services" to c.getServices(),
            "pricePerNight" to c.getPricePerNight(),
            "userRating" to c.getUserRating(),
            "description" to c.getDescription(),
            "imageUrl" to c.getImageUrl(),
            "availableDateRanges" to c.availableDateRanges,
            "tags" to c.getTags()
        )
    }

    /**
     * Builds a campsite back from name -> value pairs. Missing values become
     * blanks or zeros.
     *
     * @param id the Firestore document's name, used as the campsite id
     * @param m the document's data
     * @return the campsite
     */
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
            availableDateRanges = dateRanges(m, "availableDateRanges"),
            tags = textList(m, "tags")
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