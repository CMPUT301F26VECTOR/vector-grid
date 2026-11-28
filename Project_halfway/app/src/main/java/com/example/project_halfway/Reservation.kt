package com.example.project_halfway

/**
 * A lottery winner's reservation: the campsite and the nights they picked
 * (US 02.04.04, and the start of US 01.04.01). The dates must be inside the
 * lottery's booking period; Lottery.isWithinBookingPeriod checks that.
 *
 * Stored in the Firestore "reservations" collection by ReservationRepository.
 *
 * The owner sorts reservations into three lists (US 02.07.01, 02.07.02,
 * 02.07.03) with isActive, isPast and isCancelled, and the booked dates
 * calendar (US 02.06.01) leaves cancelled ones out.
 *
 * How status works: every reservation starts as "active". When an entrant
 * cancels (US 01.07.01) the code that does it should set status to "cancelled",
 * and when the owner rejects it (US 02.08.01) status should be "rejected".
 * Both should also set cancelledAt to the time it happened.
 *
 * Outstanding issues: the cancel (US 01.07.01) and reject (US 02.08.01) buttons
 * are other stories, so until they are built no reservation is ever cancelled.
 *
 * **AI assistance (Samarth Sathya Karthik):** the status, cancelledAt and guests
 * fields and the isActive, isPast and isCancelled helpers were implemented using
 * Claude AI; Samarth tested them and checked that they work.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * @property id the Firestore document id, or "" before it is saved
 * @property entrantId the winner who reserved
 * @property lotteryId the lottery they won
 * @property campsiteId the campsite they reserved
 * @property startDate first night, as YYYY-MM-DD
 * @property endDate last day, as YYYY-MM-DD
 * @property createdAt when it was made, in milliseconds
 * @property status "active", "cancelled" (the entrant cancelled) or "rejected" (the owner rejected it)
 * @property cancelledAt when it was cancelled or rejected, in milliseconds, or 0 if it wasn't
 * @property guests how many people are coming, or 0 if the entrant didn't say
 * @property rulesAgreed true if entrant agrees to the campsite rules, false if they do not agree
 */
data class Reservation(
    val id: String = "",
    val entrantId: String,
    val lotteryId: String,
    val campsiteId: String,
    val startDate: String,
    val endDate: String,
    val createdAt: Long,
    val status: String = STATUS_ACTIVE,
    val cancelledAt: Long = 0L,
    val guests: Int = 0,
    val rulesAgreed: Boolean = false,
    val totalCost: Double = 0.0
) {

    /**
     * Tells whether the reservation was called off, either by the entrant
     * cancelling it or by the owner rejecting it (US 02.07.03).
     *
     * @return true if the status is "cancelled" or "rejected"
     */
    fun isCancelled(): Boolean {
        return status == STATUS_CANCELLED || status == STATUS_REJECTED
    }

    /**
     * Tells whether the stay is over (US 02.07.02): it was not cancelled and
     * its last day is before today.
     *
     * Dates are YYYY-MM-DD text, so comparing the text is the same as
     * comparing the dates ("2026-06-03" < "2026-06-10").
     *
     * @param today today's date as YYYY-MM-DD
     * @return true if it is a past stay
     */
    fun isPast(today: String): Boolean {
        if (isCancelled()) {
            return false
        }
        return endDate < today
    }

    /**
     * Tells whether the stay is still coming up or happening now
     * (US 02.07.01): it was not cancelled and its last day is today or later.
     *
     * @param today today's date as YYYY-MM-DD
     * @return true if it is an active reservation
     */
    fun isActive(today: String): Boolean {
        if (isCancelled()) {
            return false
        }
        return endDate >= today
    }

    /** Helpers for storing reservations in Firestore. */
    companion object {
        /** A reservation that is still on. Missing status in Firestore means this. */
        const val STATUS_ACTIVE = "active"

        /** The entrant cancelled the reservation (US 01.07.01). */
        const val STATUS_CANCELLED = "cancelled"

        /** The owner rejected the reservation (US 02.08.01). */
        const val STATUS_REJECTED = "rejected"

        /** Turns a reservation into Firestore name -> value pairs (without the id). */
        fun toMap(r: Reservation): Map<String, Any> = mapOf(
            "entrantId" to r.entrantId,
            "lotteryId" to r.lotteryId,
            "campsiteId" to r.campsiteId,
            "startDate" to r.startDate,
            "endDate" to r.endDate,
            "createdAt" to r.createdAt,
            "status" to r.status,
            "cancelledAt" to r.cancelledAt,
            "guests" to r.guests,
            "rulesAgreed" to r.rulesAgreed,
            "totalCost" to r.totalCost
        )

        /**
         * Builds a reservation back from a Firestore document's data.
         * Older reservations have no status, cancelledAt or guests, so those
         * become "active", 0 and 0.
         */
        fun fromMap(id: String, m: Map<String, Any>): Reservation = Reservation(
            id = id,
            entrantId = m["entrantId"] as? String ?: "",
            lotteryId = m["lotteryId"] as? String ?: "",
            campsiteId = m["campsiteId"] as? String ?: "",
            startDate = m["startDate"] as? String ?: "",
            endDate = m["endDate"] as? String ?: "",
            createdAt = (m["createdAt"] as? Number)?.toLong() ?: 0L,
            status = m["status"] as? String ?: STATUS_ACTIVE,
            cancelledAt = (m["cancelledAt"] as? Number)?.toLong() ?: 0L,
            guests = (m["guests"] as? Number)?.toInt() ?: 0,
            rulesAgreed =  m["agreedToRules"] as? Boolean?: false,
            totalCost = (m["totalCost"] as? Number)?.toDouble() ?: 0.0
        )
    }
}
