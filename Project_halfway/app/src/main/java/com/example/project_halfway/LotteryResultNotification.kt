package com.example.project_halfway

/**
 * A message telling an entrant how a lottery draw went for them: they won,
 * or they weren't selected (US 02.05.01, and the start of US 01.03.01 and
 * US 01.03.02). The draw makes one for every entrant on the waiting list,
 * and the entrant sees them on the Lottery tab of My Reservations & Entries.
 *
 * Stored in the Firestore "notifications" collection with type "won" or
 * "notSelected", so other kinds of notification (such as the owner's
 * messages, US 02.10.01) can share the collection with their own type.
 *
 * Outstanding issues: these are shown inside the app only; there are no
 * phone (push) notifications yet.
 *
 * @property id the Firestore document id, or "" before it is saved
 * @property entrantId who the notification is for
 * @property lotteryId the lottery that was drawn
 * @property campsiteId the lottery's campsite, so the screen can show its name
 * @property type "won" or "notSelected"
 * @property message the text the entrant reads
 * @property sentAt when it was made, in milliseconds
 */
data class LotteryResultNotification(
    val id: String = "",
    val entrantId: String,
    val lotteryId: String,
    val campsiteId: String,
    val type: String,
    val message: String,
    val sentAt: Long
) {
    /** True if this tells the entrant they won the lottery. */
    fun isWin(): Boolean = type == TYPE_WON

    /** Helpers for making and storing draw notifications. */
    companion object {
        /** Type of a notification for a winner. */
        const val TYPE_WON = "won"

        /** Type of a notification for an entrant who wasn't picked. */
        const val TYPE_NOT_SELECTED = "notSelected"

        /** Text for winners. */
        const val WON_MESSAGE = "You won the lottery! Reserve your dates below."

        /** Text for entrants who weren't picked. */
        const val NOT_SELECTED_MESSAGE = "You weren't selected in this lottery. Better luck next time!"

        /**
         * Makes one notification for every entrant in a drawn lottery: winners
         * get a "won" one and everyone else a "notSelected" one (US 02.05.01).
         *
         * @param lottery a lottery whose winners have been drawn
         * @param now the time to put on the notifications, in milliseconds
         * @return the notifications, or an empty list if the lottery isn't drawn
         */
        fun forDraw(lottery: Lottery, now: Long): List<LotteryResultNotification> {
            if (!lottery.isDrawn) return emptyList()
            val won = lottery.winners.map {
                LotteryResultNotification(
                    entrantId = it, lotteryId = lottery.id, campsiteId = lottery.campsiteId,
                    type = TYPE_WON, message = WON_MESSAGE, sentAt = now
                )
            }
            val lost = lottery.notSelected.map {
                LotteryResultNotification(
                    entrantId = it, lotteryId = lottery.id, campsiteId = lottery.campsiteId,
                    type = TYPE_NOT_SELECTED, message = NOT_SELECTED_MESSAGE, sentAt = now
                )
            }
            return won + lost
        }

        /**
         * Turns a notification into Firestore name -> value pairs (the id is
         * the document's name, so it isn't included).
         */
        fun toMap(n: LotteryResultNotification): Map<String, Any> = mapOf(
            "entrantId" to n.entrantId,
            "lotteryId" to n.lotteryId,
            "campsiteId" to n.campsiteId,
            "type" to n.type,
            "message" to n.message,
            "sentAt" to n.sentAt
        )

        /**
         * Builds a notification back from a Firestore document's data.
         * Missing values become blanks or zero.
         */
        fun fromMap(id: String, m: Map<String, Any>): LotteryResultNotification =
            LotteryResultNotification(
                id = id,
                entrantId = m["entrantId"] as? String ?: "",
                lotteryId = m["lotteryId"] as? String ?: "",
                campsiteId = m["campsiteId"] as? String ?: "",
                type = m["type"] as? String ?: "",
                message = m["message"] as? String ?: "",
                sentAt = (m["sentAt"] as? Number)?.toLong() ?: 0L
            )
    }
}
