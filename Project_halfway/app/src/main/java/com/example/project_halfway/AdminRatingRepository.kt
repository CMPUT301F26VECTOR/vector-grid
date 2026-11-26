package com.example.project_halfway

/**
 * Lets the admin load campsites and remove one rating from a campsite (US 03.09.01).
 *
 * Ratings are stored as a list of numbers on the campsite document, so removing
 * one means saving the list again without that entry.
 *
 * Outstanding issues: ratings do not record who gave them, so the admin removes
 * a rating by its place in the list; if a rating is added at the same moment,
 * the list is overwritten.
 */
object AdminRatingRepository {

    /** Returns the ratings without the one at [index]. A bad index changes nothing. */
    fun withoutRating(ratings: List<Double>, index: Int): List<Double> {
        if (index !in ratings.indices) return ratings
        return ratings.filterIndexed { i, _ -> i != index }
    }

    /** Loads every campsite using the admin database. */
    fun getCampsites(onResult: (List<CampsiteModel>) -> Unit, onError: (String) -> Unit) {
        FirebaseSecurity.adminDB.collection("campsites").get()
            .addOnSuccessListener { snapshot ->
                val found = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data == null) null else CampsiteRepository.fromMap(doc.id, data)
                }
                onResult(found)
            }
            .addOnFailureListener { onError("Could not load campsites") }
    }

    /** Saves the campsite's ratings list again without the rating at [index]. */
    fun removeRating(campsite: CampsiteModel, index: Int, onDone: (Boolean) -> Unit) {
        val newRatings = withoutRating(campsite.getRatings(), index)
        FirebaseSecurity.adminDB.collection("campsites").document(campsite.getId())
            .update("ratings", newRatings)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }
}