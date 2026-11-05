package com.example.project_halfway

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.random.Random

/**
 * Model for a campsite lottery.
 *
 * An owner opens a lottery for one campsite for a period of time (startDate to endDate).
 * While it is open, entrants can join or leave its waiting list.
 * After it ends, winners are drawn at random from the waiting list.
 *
 * Used for US 01.01.06, US 01.02.01, US 01.02.02, US 02.04.03, US 02.04.04,
 * US 02.04.05, US 02.04.07 and US 02.05.01.
 *
 * Outstanding issues: the owner screen for creating a lottery is not done yet
 * (US 02.04.01, 02.04.02), so for now test lotteries are added by hand in the
 * Firestore console. That screen can check the owner's choices with
 * firstSettingsError(). Winners and non-winners are recorded but not notified
 * yet (US 01.03.01, 01.03.02).
 *
 * @property id the Firestore document id of this lottery
 * @property campsiteId id of the campsite this lottery is for
 * @property startDate when registration opens, in milliseconds (US 02.04.03)
 * @property endDate when registration closes, in milliseconds (US 02.04.03)
 * @property numberOfWinners how many entrants the draw picks (US 02.04.05)
 * @property bookingStartDate first day the winners can stay, in milliseconds,
 * or 0 if the owner has not set a booking period (US 02.04.04)
 * @property bookingEndDate last day the winners can stay, in milliseconds,
 * or 0 if not set (US 02.04.04)
 * @property waitlistCapacity most entrants allowed on the waiting list,
 * or null for no limit (US 02.04.07)
 */
class Lottery(
    val id: String,
    val campsiteId: String,
    val startDate: Long,
    val endDate: Long,
    waitingList: List<String> = emptyList(),
    val numberOfWinners: Int = 1,
    winners: List<String> = emptyList(),
    notSelected: List<String> = emptyList(),
    isDrawn: Boolean = false,
    val bookingStartDate: Long = 0L,
    val bookingEndDate: Long = 0L,
    val waitlistCapacity: Int? = null
) {
    // ids of the entrants on the waiting list
    private val _waitingList = waitingList.toMutableList()

    // results of the draw (both empty until the draw happens)
    private val _winners = winners.toMutableList()
    private val _notSelected = notSelected.toMutableList()

    /**
     * True once the winners have been drawn. Only drawWinners can change it.
     */
    var isDrawn: Boolean = isDrawn
        private set

    /**
     * Entrant ids picked by the draw. Read only.
     */
    val winners: List<String>
        get() = _winners

    /**
     * Entrant ids that joined but were not picked. Read only.
     */
    val notSelected: List<String>
        get() = _notSelected

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
     * You can't join if the lottery is not active, if you already joined,
     * or if the waiting list is full (US 02.04.07).
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
        if (isWaitlistFull()) {
            return false
        }
        _waitingList.add(entrantId)
        return true
    }

    /**
     * Checks if the waiting list has reached the owner's limit (US 02.04.07).
     *
     * @return true if there is a limit and the list is at it; always false with no limit
     */
    fun isWaitlistFull(): Boolean {
        val capacity = waitlistCapacity ?: return false
        return _waitingList.size >= capacity
    }

    /**
     * Checks the owner's lottery settings, so the create-lottery screen can show
     * what is wrong before saving (US 02.04.03, 02.04.04, 02.04.05, 02.04.07).
     *
     * Booking dates come from the calendar as midnight UTC, while registration
     * closes at an exact time, so the booking check compares calendar days
     * (registration's closing day in the owner's time zone) instead of
     * milliseconds. Booking may start on the day registration closes.
     *
     * If the waiting list limit is smaller than the number of winners, that is
     * allowed: everyone on the list simply wins.
     *
     * @param now the current time in milliseconds; registration must close after it
     * @param zone the owner's time zone, used to find the day registration closes
     * @return the message for the first problem found, or null if the settings are good
     */
    fun firstSettingsError(
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): String? {
        // US 02.04.03: registration period
        if (startDate <= 0L || endDate <= 0L) {
            return "Please set a registration period."
        }
        if (endDate <= startDate) {
            return "Registration must end after it starts."
        }
        if (endDate <= now) {
            return "Registration must close in the future."
        }

        // US 02.04.04: booking period is optional, but needs both dates or neither
        val bookingSet = bookingStartDate > 0L || bookingEndDate > 0L
        if (bookingSet) {
            if (bookingStartDate <= 0L || bookingEndDate <= 0L) {
                return "Please set both booking dates, or leave both empty."
            }
            val bookingStartDay = calendarDay(bookingStartDate)
            val bookingEndDay = calendarDay(bookingEndDate)
            if (bookingEndDay.isBefore(bookingStartDay)) {
                return "The booking period must end on or after it starts."
            }
            // winners are only known once registration closes
            val registrationCloseDay = Instant.ofEpochMilli(endDate).atZone(zone).toLocalDate()
            if (bookingStartDay.isBefore(registrationCloseDay)) {
                return "The booking period can't start before registration closes."
            }
        }

        // US 02.04.05: number of winners
        if (numberOfWinners < 1) {
            return "Please pick at least 1 winner."
        }

        // US 02.04.07: waiting list limit is optional
        val capacity = waitlistCapacity
        if (capacity != null && capacity < 1) {
            return "The waiting list limit must be at least 1, or leave it empty."
        }

        return null
    }

    // A calendar-picked date (midnight UTC in milliseconds) as a plain day.
    private fun calendarDay(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
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

    /**
     * Checks if it is time to draw: registration has closed and nobody
     * has drawn this lottery yet.
     *
     * @param now the current time in milliseconds
     * @return true if drawWinners would do a draw right now
     */
    fun isDrawDue(now: Long): Boolean {
        return !isDrawn && now > endDate
    }

    /**
     * Randomly picks numberOfWinners entrants from the waiting list (US 02.05.01).
     * If fewer people joined than there are winner spots, everyone wins.
     * Does nothing if the draw is not due yet, so it can never run twice.
     *
     * @param now the current time in milliseconds
     * @param random where the randomness comes from; tests pass Random(seed)
     * so they always get the same winners
     * @return true if a draw happened, false if it was not due
     */
    fun drawWinners(now: Long, random: Random = Random.Default): Boolean {
        if (!isDrawDue(now)) {
            return false
        }
        // shuffle a copy of the list, then the first N people are the winners
        val shuffled = _waitingList.shuffled(random)
        val spots = numberOfWinners.coerceAtLeast(0)

        _winners.clear()
        _winners.addAll(shuffled.take(spots))
        _notSelected.clear()
        _notSelected.addAll(shuffled.drop(spots))
        isDrawn = true
        return true
    }
}
