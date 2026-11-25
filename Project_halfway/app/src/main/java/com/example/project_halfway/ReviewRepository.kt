package com.example.project_halfway

/**
 * Saves reviews in the Firestore "reviews" collection (US 01.13.02).
 *
 * What a review document looks like:
 *
 *     campsiteId : "abc123"     the campsite being reviewed
 *     entrantId  : "uid1"       who wrote it
 *     text       : "Lovely."    what they wrote
 *     createdAt  : 1700000000   when it was posted, in milliseconds
 *
 * Every review is its own document, so one review can be removed without
 * touching the others (US 03.10.01). Uses the entrant login, FirebaseSecurity.db.
 *
 * Outstanding issues: reviews can only be saved so far; loading them for a
 * campsite comes with US 03.10.01.
 */
object ReviewRepository {

    private fun reviews() = FirebaseSecurity.db.collection("reviews")

    /**
     * Saves a review. If it has no id, Firestore makes a new one.
     *
     * @param review the review to save; call review.firstError() first
     * @param onDone gets true once it is saved, false if it failed
     */
    fun save(review: Review, onDone: (Boolean) -> Unit = {}) {
        val document = if (review.id == "") reviews().document() else reviews().document(review.id)

        document.set(toMap(review))
            .addOnSuccessListener {
                println("Saved review ${document.id}")
                onDone(true)
            }
            .addOnFailureListener {
                println("Save review failed: ${it.message}")
                onDone(false)
            }
    }

    /**
     * Turns a review into a map that Firestore can store.
     * The text is saved without the spaces at its ends.
     *
     * @param review the review to store
     * @return the review's fields (the id is the document's name, not a field)
     */
    fun toMap(review: Review): Map<String, Any> {
        return mapOf(
            "campsiteId" to review.campsiteId,
            "entrantId" to review.entrantId,
            "text" to review.text.trim(),
            "createdAt" to review.createdAt
        )
    }

    /**
     * Turns a Firestore document back into a review. Missing fields become
     * blanks or 0.
     *
     * @param id the document's name, used as the review id
     * @param m the document's data
     * @return the review
     */
    fun fromMap(id: String, m: Map<String, Any>): Review {
        val created = m["createdAt"]
        return Review(
            id = id,
            campsiteId = m["campsiteId"] as? String ?: "",
            entrantId = m["entrantId"] as? String ?: "",
            text = m["text"] as? String ?: "",
            createdAt = if (created is Number) created.toLong() else 0L
        )
    }
}