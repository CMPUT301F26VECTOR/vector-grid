package com.example.project_halfway

/**
 * Saves and loads lottery winners' reservations in the Firestore
 * "reservations" collection (US 02.04.04), and loads the draw notifications
 * for an entrant from the "notifications" collection (US 02.05.01).
 * Uses the entrant's login (FirebaseSecurity.db).
 *
 * Outstanding issues: no cancel or delete yet (US 01.07.01).
 */
object ReservationRepository {

    private fun reservations() = FirebaseSecurity.db.collection("reservations")

    private fun notifications() = FirebaseSecurity.db.collection("notifications")

    /**
     * Saves a new reservation.
     *
     * @param reservation the reservation to save
     * @param onDone gets the saved reservation (with its id), or null if saving failed
     */
    fun save(reservation: Reservation, onDone: (Reservation?) -> Unit) {
        val document = reservations().document()
        document.set(Reservation.toMap(reservation))
            .addOnSuccessListener { onDone(reservation.copy(id = document.id)) }
            .addOnFailureListener {
                println("Saving the reservation failed: ${it.message}")
                onDone(null)
            }
    }

    /**
     * Loads every reservation an entrant has made.
     *
     * @param entrantId the entrant's id
     * @param onResult gets the reservations; empty if there are none or loading failed
     */
    fun getForEntrant(entrantId: String, onResult: (List<Reservation>) -> Unit) {
        reservations().whereEqualTo("entrantId", entrantId).get()
            .addOnSuccessListener { result ->
                onResult(result.documents.mapNotNull { d -> d.data?.let { Reservation.fromMap(d.id, it) } })
            }
            .addOnFailureListener {
                println("Loading reservations failed: ${it.message}")
                onResult(emptyList())
            }
    }

    /**
     * Loads an entrant's lottery result notifications, newest first.
     *
     * @param entrantId the entrant's id
     * @param onResult gets the notifications; empty if there are none or loading failed
     */
    fun getNotifications(entrantId: String, onResult: (List<LotteryResultNotification>) -> Unit) {
        notifications().whereEqualTo("entrantId", entrantId).get()
            .addOnSuccessListener { result ->
                val list = result.documents
                    .mapNotNull { d -> d.data?.let { LotteryResultNotification.fromMap(d.id, it) } }
                    .filter { it.type == LotteryResultNotification.TYPE_WON || it.type == LotteryResultNotification.TYPE_NOT_SELECTED }
                onResult(list.sortedByDescending { it.sentAt })
            }
            .addOnFailureListener {
                println("Loading notifications failed: ${it.message}")
                onResult(emptyList())
            }
    }
}
