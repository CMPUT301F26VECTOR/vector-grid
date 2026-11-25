package com.example.project_halfway

/**
 * A review an entrant writes for a campsite (US 01.13.02).
 *
 * Each review is its own document in the "reviews" collection, so a single
 * review can be removed later without touching the others (US 03.10.01).
 *
 * Outstanding issues: an entrant can post more than one review for the same
 * campsite, and a review can't be edited after it is posted.
 *
 * @property id the Firestore document id of this review, or "" before it is saved
 * @property campsiteId id of the campsite being reviewed
 * @property entrantId id of the entrant who wrote it
 * @property text what the entrant wrote
 * @property createdAt when it was posted, in milliseconds
 */
class Review(
    val id: String = "",
    val campsiteId: String,
    val entrantId: String,
    val text: String,
    val createdAt: Long
) {

    /**
     * Checks the review before it is saved, so the screen can say what is wrong.
     *
     * Checked in this order: the campsite and entrant are known, the text is
     * not blank, and the text is at most MAX_LENGTH characters.
     *
     * @return the message for the first problem found, or null if the review is good
     */
    fun firstError(): String? {
        if (campsiteId.isBlank() || entrantId.isBlank()) {
            return "Could not tell which campsite or entrant this review is for."
        }
        if (text.isBlank()) {
            return "Please write something before posting."
        }
        if (text.trim().length > MAX_LENGTH) {
            return "A review can be at most $MAX_LENGTH characters."
        }
        return null
    }

    companion object {
        /** The most characters a review can have. */
        const val MAX_LENGTH = 500
    }
}