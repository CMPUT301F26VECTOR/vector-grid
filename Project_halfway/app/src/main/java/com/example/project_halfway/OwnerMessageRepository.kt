package com.example.project_halfway

/**
 * Saves entrant messages to campsite owners in the Firestore "messages"
 * collection (US 01.14.01). Uses the entrant's login (FirebaseSecurity.db).
 *
 * One document per message, so the owner's story can show or remove a single
 * message later.
 *
 * Outstanding issues: only saving is built; loading the messages is the
 * owner's story.
 */
object OwnerMessageRepository {

    private fun messages() = FirebaseSecurity.db.collection("messages")

    /**
     * Saves one message.
     *
     * @param message the message to save
     * @param onDone gets true once Firestore has saved it, or false if it failed
     */
    fun save(message: OwnerMessage, onDone: (Boolean) -> Unit = {}) {
        messages().add(toMap(message))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener {
                println("Sending message failed: ${it.message}")
                onDone(false)
            }
    }

    /** Turns a message into name -> value pairs. The id is the document's name, so it is left out. */
    fun toMap(m: OwnerMessage): Map<String, Any> {
        return mapOf(
            "reservationId" to m.reservationId,
            "campsiteId" to m.campsiteId,
            "entrantId" to m.entrantId,
            "text" to m.text.trim(),
            "createdAt" to m.createdAt
        )
    }

    /** Builds a message back from a Firestore document's data. Missing values become blanks or zero. */
    fun fromMap(id: String, m: Map<String, Any>): OwnerMessage {
        return OwnerMessage(
            id = id,
            reservationId = m["reservationId"] as? String ?: "",
            campsiteId = m["campsiteId"] as? String ?: "",
            entrantId = m["entrantId"] as? String ?: "",
            text = m["text"] as? String ?: "",
            createdAt = (m["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }
}