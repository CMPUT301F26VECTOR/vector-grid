package com.example.project_halfway

/**
 * Saves and loads lottery winners' reservations in the Firestore
 * "reservations" collection (US 02.04.04), and loads the draw notifications
 * for an entrant from the "notifications" collection (US 02.05.01).
 * Uses the entrant's login (FirebaseSecurity.db).
 *
 * The owner loads the reservations for their campsites with getForCampsites
 * (US 02.06.01, 02.07.01, 02.07.02, 02.07.03).
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

    // ---------- Owner side (US 02.06.01, 02.07.01, 02.07.02, 02.07.03) ----------
    // Added at the end of the file on purpose, so it doesn't clash with
    // teammates' changes higher up.

    /**
     * Loads every reservation for a list of campsites, as the campsite owner
     * (US 02.06.01, 02.07.01, 02.07.02, 02.07.03).
     *
     * How it works: Firestore is asked once per campsite. Each answer is added
     * to one big list, and when the last campsite has answered, onResult gets
     * the whole list. A campsite that fails to load just adds nothing.
     *
     * **AI assistance (Samarth Sathya Karthik):** this function was implemented
     * using Claude AI; Samarth tested it and checked that it works.
     *
     * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
     *
     * @param campsiteIds ids of the owner's campsites
     * @param onResult gets every reservation for those campsites; empty if there are none
     */
    fun getForCampsites(campsiteIds: List<String>, onResult: (List<Reservation>) -> Unit) {
        // Nothing to ask for, so answer straight away
        if (campsiteIds.isEmpty()) {
            onResult(emptyList())
            return
        }

        val found = mutableListOf<Reservation>()
        var answered = 0 // how many campsites have answered so far

        for (campsiteId in campsiteIds) {
            FirebaseSecurity.ownerDb.collection("reservations")
                .whereEqualTo("campsiteId", campsiteId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result.documents) {
                        val data = document.data
                        if (data != null) {
                            found.add(Reservation.fromMap(document.id, data))
                        }
                    }
                    answered++
                    if (answered == campsiteIds.size) onResult(found)
                }
                .addOnFailureListener {
                    println("Loading reservations for $campsiteId failed: ${it.message}")
                    answered++
                    if (answered == campsiteIds.size) onResult(found)
                }
        }
    }
}
