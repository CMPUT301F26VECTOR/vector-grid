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
     * Declines a winner's reservation offer (US 01.04.03).
     *
     * Updates the offer status in Firestore without changing the lottery result.
     * Only the signed-in winner can decline their own pending offer.
     *
     * @param notificationId the Firestore document id of the winning notification
     * @param entrantId the entrant declining the offer
     * @param onDone true if the offer was declined, false otherwise
     */
    fun declineOffer(
        notificationId: String,
        entrantId: String,
        onDone: (Boolean) -> Unit
    ) {
        val currentUserId = FirebaseSecurity.auth.currentUser?.uid

        if (notificationId.isBlank() || entrantId.isBlank() || currentUserId != entrantId) {
            onDone(false)
            return
        }

        val document = notifications().document(notificationId)

        FirebaseSecurity.db.runTransaction { transaction ->
            val snapshot = transaction.get(document)

            val validOffer =
                snapshot.exists() &&
                        snapshot.getString("entrantId") == entrantId &&
                        snapshot.getString("type") == LotteryResultNotification.TYPE_WON &&
                        (snapshot.getString("offerStatus")
                            ?: LotteryResultNotification.OFFER_PENDING) ==
                        LotteryResultNotification.OFFER_PENDING

            if (!validOffer) {
                false
            } else {
                transaction.update(
                    document,
                    "offerStatus",
                    LotteryResultNotification.OFFER_DECLINED
                )
                true
            }
        }.addOnSuccessListener { declined ->
            onDone(declined)
        }.addOnFailureListener {
            println("Declining the reservation offer failed: ${it.message}")
            onDone(false)
        }
    }

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
