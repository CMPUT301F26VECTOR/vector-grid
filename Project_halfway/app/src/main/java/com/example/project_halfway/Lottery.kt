package com.example.project_halfway

/**
 * Model for a campsite lottery.
 *
 * An owner opens a lottery for one campsite for a period of time (startDate to endDate).
 * While it is open, entrants can join or leave its waiting list.
 * After it ends, the owner draws winners from the waiting list (not done yet).
 *
 * Used for US 01.01.06, US 01.02.01 and US 01.02.02.
 *
 * Outstanding issues: drawing winners is not done yet, and the owner side
 * (creating a lottery) is not done yet either, so for now test lotteries are
 * added by hand in the Firestore console.
 *
 * @property id the Firestore document id of this lottery
 * @property campsiteId id of the campsite this lottery is for
 * @property startDate when the lottery opens, in milliseconds
 * @property endDate when the lottery closes, in milliseconds
 */
class Lottery(
    val id: String,
    val campsiteId: String,
    val startDate: Long,
    val endDate: Long,
    waitingList: List<String> = emptyList()
) {
    // ids of the entrants on the waiting list
    private val _waitingList = waitingList.toMutableList()

    /**
     * The entrant ids on the waiting list. Read only, use join/leave to change it.
     */
    val waitingList: List<String>
        get() = _waitingList

    /**
     * Checks if the lottery is open right now.
     *
     * @param now the current time in milliseconds
     * @return true if now is between the start and end date
     */
    fun isActive(now: Long): Boolean {
        return now >= startDate && now <= endDate
    }

    /**
     * Checks if an entrant is already on the waiting list.
     *
     * @param entrantId the entrant's id
     * @return true if they are on the list
     */
    fun isOnWaitingList(entrantId: String): Boolean {
        return _waitingList.contains(entrantId)
    }

    /**
     * Adds an entrant to the waiting list.
     * You can't join if the lottery is not active, or if you already joined.
     *
     * @param entrantId the entrant's id
     * @param now the current time in milliseconds
     * @return true if they got added, false if not
     */
    fun joinWaitingList(entrantId: String, now: Long): Boolean {
        if (entrantId.isBlank()) {
            return false
        }
        if (!isActive(now)) {
            return false
        }
        if (isOnWaitingList(entrantId)) {
            return false
        }
        _waitingList.add(entrantId)
        return true
    }

    /**
     * Removes an entrant from the waiting list.
     *
     * @param entrantId the entrant's id
     * @return true if they got removed, false if they weren't on the list
     */
    fun leaveWaitingList(entrantId: String): Boolean {
        return _waitingList.remove(entrantId)
    }

    /**
     * @return how many entrants are on the waiting list
     */
    fun getWaitingListSize(): Int {
        return _waitingList.size
    }

    /**
     * Works out how many full days are left before the lottery closes.
     * Could be used for the "Days left in lottery" label on the summary card.
     *
     * @param now the current time in milliseconds
     * @return days left, or 0 if it's already closed
     */
    fun getDaysLeft(now: Long): Int {
        if (now > endDate) {
            return 0
        }
        val oneDay = 24 * 60 * 60 * 1000L
        return ((endDate - now) / oneDay).toInt()
    }
}
