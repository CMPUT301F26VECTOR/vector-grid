package com.example.project_halfway

/**
 * One message an entrant sends to the campsite owner about a reservation
 * (US 01.14.01).
 *
 * Saved in the Firestore "messages" collection by OwnerMessageRepository, one
 * document per message. The owner is found through the campsite, so no owner id
 * is stored here.
 *
 * Outstanding issues: only sending is done. Showing the messages to the owner
 * is the owner's story, and there is no reply yet.
 *
 * @property id the Firestore document id, or "" before it is saved
 * @property reservationId the reservation the message is about
 * @property campsiteId the campsite the reservation is for
 * @property entrantId the entrant who wrote it
 * @property text what the entrant wrote
 * @property createdAt when it was written, in milliseconds
 */
class OwnerMessage(
    val id: String = "",
    val reservationId: String,
    val campsiteId: String,
    val entrantId: String,
    val text: String,
    val createdAt: Long
) {

    /**
     * Checks the message before it is sent.
     *
     * @return the first problem found, or null if the message is fine
     */
    fun firstError(): String? {
        if (reservationId.isBlank() || campsiteId.isBlank() || entrantId.isBlank()) {
            return "Could not tell which reservation or entrant this message is for."
        }
        if (text.isBlank()) {
            return "Please write a message first."
        }
        if (text.trim().length > MAX_LENGTH) {
            return "A message can be at most $MAX_LENGTH characters."
        }
        return null
    }

    companion object {
        /** The longest message allowed. */
        const val MAX_LENGTH = 500
    }
}