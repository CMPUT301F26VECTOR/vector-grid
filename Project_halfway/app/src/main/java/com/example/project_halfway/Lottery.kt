package com.example.project_halfway

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.random.Random

/**
 * Model for a campsite lottery.
 *
 * How it works:
 * - An owner opens a lottery for one or more campsites, with a registration period.
 * - While registration is open, entrants can join or leave the waiting list.
 * - After registration closes, the winners are drawn at random from the waiting list.
 * - Winners then reserve their stay inside the booking period, if the owner set one.
 *
 * Used for US 01.01.06, 01.02.01, 01.02.02 (entrant side) and
 * US 02.04.02, 02.04.03, 02.04.04, 02.04.05, 02.04.07, 02.05.01 (owner side).
 *
 * Outstanding issues: the owner can't change a lottery after it is saved, and
 * winners and non-winners are not told the result of the draw yet.
 *
 * @property id the Firestore document id of this lottery
 * @property campsiteId id of the first campsite in this lottery
 * @property campsiteIds ids of every campsite in this lottery, starting with campsiteId (US 02.04.02)
 * @property startDate when registration opens, in milliseconds (US 02.04.03)
 * @property endDate when registration closes, in milliseconds (US 02.04.03)
 * @property numberOfWinners how many entrants the draw picks (US 02.04.05)
 * @property bookingStartDate first day winners can stay, in milliseconds, or 0 if not set (US 02.04.04)
 * @property bookingEndDate last day winners can stay, in milliseconds, or 0 if not set (US 02.04.04)
 * @property waitlistCapacity most entrants allowed on the waiting list, or null for no limit (US 02.04.07)
 * @param waitingList ids of the entrants already on the waiting list (empty for a new lottery)
 * @param winners ids picked by the draw (empty until the draw happens)
 * @param notSelected ids that joined but were not picked (empty until the draw happens)
 * @param isDrawn true if the winners were already drawn
 *
 *
 * **AI assistance (Parneet Kaur):** part of this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
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
    val waitlistCapacity: Int? = null,
    campsiteIds: List<String> = listOf(campsiteId)
) {
    private val _waitingList = waitingList.toMutableList()

    private val _winners = winners.toMutableList()
    private val _notSelected = notSelected.toMutableList()

    private val _campsiteIds = campsiteIds.toMutableList()

    var isDrawn: Boolean = isDrawn
        private set

    val winners: List<String>
        get() = _winners

    val notSelected: List<String>
        get() = _notSelected

    val waitingList: List<String>
        get() = _waitingList
   
    val campsiteIds: List<String>
        get() = _campsiteIds

    /**
     * Checks if registration is open right now.
     *
     * @param now the current time in milliseconds
     * @return true if now is between startDate and endDate
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
     * Adds an entrant to the waiting list (US 01.02.01).
     *
     * Nothing is added if the id is blank, registration is not open, the
     * entrant already joined, or the waiting list is full (US 02.04.07).
     *
     * @param entrantId the entrant's id
     * @param now the current time in milliseconds
     * @return true if they were added, false if not
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
     * @return true if there is a limit and the list is at it, false if there is no limit
     */
    fun isWaitlistFull(): Boolean {
        val capacity = waitlistCapacity ?: return false
        return _waitingList.size >= capacity
    }

    /**
     * Checks the owner's lottery settings, so the create-lottery screen can say
     * what is wrong before saving.
     *
     * Checked in this order:
     * - registration period is set, ends after it starts, and closes in the future
     * - booking period has both dates or neither, ends on or after it starts, and starts no earlier than the day registration closes
     * - at least 1 winner
     * - waiting list limit is at least 1, or empty
     *
     * Booking dates come from the calendar as midnight UTC, so they are compared
     * as calendar days. A waiting list limit smaller than the number of winners
     * is allowed: everyone on the list simply wins.
     *
     * @param now the current time in milliseconds; registration must close after it
     * @param zone the owner's time zone, used to find the day registration closes
     * @return the message for the first problem found, or null if the settings are good
     */
    fun firstSettingsError(
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): String? {

        if (startDate <= 0L || endDate <= 0L) {
            return "Please set a registration period."
        }
        if (endDate <= startDate) {
            return "Registration must end after it starts."
        }
        if (endDate <= now) {
            return "Registration must close in the future."
        }

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
     
            val registrationCloseDay = Instant.ofEpochMilli(endDate).atZone(zone).toLocalDate()
            if (bookingStartDay.isBefore(registrationCloseDay)) {
                return "The booking period can't start before registration closes."
            }
        }

        if (numberOfWinners < 1) {
            return "Please pick at least 1 winner."
        }

        val capacity = waitlistCapacity
        if (capacity != null && capacity < 1) {
            return "The waiting list limit must be at least 1, or leave it empty."
        }

        return null
    }


    private fun calendarDay(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
    }

    /**
     * Checks if an entrant won this lottery's draw.
     *
     * @param entrantId the entrant's id
     * @return true once the draw has happened and they are one of the winners
     */
    fun isWinner(entrantId: String): Boolean {
        return isDrawn && _winners.contains(entrantId)
    }

    /**
     * Checks that a winner's chosen stay is inside the booking period (US 02.04.04).
     *
     * Whole calendar days are compared. With no booking period set, any stay is allowed.
     *
     * @param stayStart the first night, in milliseconds (midnight UTC)
     * @param stayEnd the last day, in milliseconds (midnight UTC)
     * @return true if the stay doesn't end before it starts and fits inside the booking period
     */
     
    fun isWithinBookingPeriod(stayStart: Long, stayEnd: Long): Boolean {
        val start = calendarDay(stayStart)
        val end = calendarDay(stayEnd)
        if (end.isBefore(start)) return false
        if (bookingStartDate <= 0L || bookingEndDate <= 0L) return true
        return !start.isBefore(calendarDay(bookingStartDate)) && !end.isAfter(calendarDay(bookingEndDate))
    }

    /**
     * Checks if one calendar day can be picked when reserving (US 02.04.04).
     * The calendar uses it to grey out days outside the booking period.
     *
     * @param day a day from the calendar, in milliseconds (midnight UTC)
     * @return true if it's inside the booking period, or there is no period
     */
    fun isBookableDay(day: Long): Boolean {
        return isWithinBookingPeriod(day, day)
    }

    /**
     * Removes an entrant from the waiting list (US 01.02.02).
     *
     * @param entrantId the entrant's id
     * @return true if they were removed, false if they weren't on the list
     */
    fun leaveWaitingList(entrantId: String): Boolean {
        return _waitingList.remove(entrantId)
    }

    /**
     * Takes a campsite out of the lottery (US 02.04.02).
     *
     * Only allowed before registration opens, and the lottery must keep at
     * least one campsite.
     *
     * @param idToRemove the id of the campsite to remove
     * @param now the current time in milliseconds
     * @return true if it was removed, false if not
     */
    fun removeCampsite(idToRemove: String, now: Long): Boolean {
        if (now >= startDate) {
            return false
        }
        if (_campsiteIds.size <= 1) {
            return false
        }
        return _campsiteIds.remove(idToRemove)
    }


    /**
     * Counts the entrants on the waiting list.
     *
     * @return how many entrants are on the waiting list
     */
    fun getWaitingListSize(): Int {
        return _waitingList.size
    }

    /**
     * Works out how many full days are left before registration closes.
     *
     * Shown on the details screen (LotteryButton) and on the Waitlisted tab
     * (WaitlistHelper.daysLeftText).
     *
     * @param now the current time in milliseconds
     * @return full days left, or 0 if registration has closed
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
     *
     * Everyone else goes to notSelected. If fewer people joined than there are
     * winner spots, everyone wins. Does nothing if the draw is not due, so it
     * can never run twice.
     *
     * @param now the current time in milliseconds
     * @param random where the randomness comes from; tests pass Random(seed) to get the same winners
     * @return true if a draw happened, false if it was not due
     */
    fun drawWinners(now: Long, random: Random = Random.Default): Boolean {
        if (!isDrawDue(now)) {
            return false
        }
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
