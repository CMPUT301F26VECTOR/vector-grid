package com.example.project_halfway

/**
 * A lottery winner's reservation: the campsite and the nights they picked
 * (US 02.04.04, and the start of US 01.04.01). The dates must be inside the
 * lottery's booking period; Lottery.isWithinBookingPeriod checks that.
 *
 * Stored in the Firestore "reservations" collection by ReservationRepository.
 *
 * Outstanding issues: reservations can't be cancelled or listed on the
 * Reservations tab yet (US 01.06.01, 01.07.01).
 *
 * @property id the Firestore document id, or "" before it is saved
 * @property entrantId the winner who reserved
 * @property lotteryId the lottery they won
 * @property campsiteId the campsite they reserved
 * @property startDate first night, as YYYY-MM-DD
 * @property endDate last day, as YYYY-MM-DD
 * @property createdAt when it was made, in milliseconds
 */
data class Reservation(
    val id: String = "",
    val entrantId: String,
    val lotteryId: String,
    val campsiteId: String,
    val startDate: String,
    val endDate: String,
    val createdAt: Long
) {
    /** Helpers for storing reservations in Firestore. */
    companion object {
        /** Turns a reservation into Firestore name -> value pairs (without the id). */
        fun toMap(r: Reservation): Map<String, Any> = mapOf(
            "entrantId" to r.entrantId,
            "lotteryId" to r.lotteryId,
            "campsiteId" to r.campsiteId,
            "startDate" to r.startDate,
            "endDate" to r.endDate,
            "createdAt" to r.createdAt
        )

        /** Builds a reservation back from a Firestore document's data. */
        fun fromMap(id: String, m: Map<String, Any>): Reservation = Reservation(
            id = id,
            entrantId = m["entrantId"] as? String ?: "",
            lotteryId = m["lotteryId"] as? String ?: "",
            campsiteId = m["campsiteId"] as? String ?: "",
            startDate = m["startDate"] as? String ?: "",
            endDate = m["endDate"] as? String ?: "",
            createdAt = (m["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }
}
