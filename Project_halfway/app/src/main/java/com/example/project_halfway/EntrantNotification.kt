package com.example.project_halfway

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A notification saved in the Firestore "notifications" collection.
 *
 * The lottery draw already saves "won" and "notSelected" notifications.
 * This class can read those and also the other types: "reserved",
 * "cancelled", "rejected" and "ownerMessage". The entrant's Notifications
 * screen, the phone pop-ups and the admin logs all use this class.
 *
 * Used for US 01.03.01, US 01.03.02, US 01.05.02, US 01.07.02, US 03.08.01 and US 03.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: owner messages (US 02.10.01) need type "ownerMessage" and the owner's id in "senderId" to show in the admin message log.
 *
 * @property id the Firestore document id ("" if not saved yet)
 * @property entrantId the entrant who gets the notification
 * @property campsiteId the campsite it is about
 * @property type the kind of notification, like "won" or "reserved"
 * @property message the text that was saved (can be empty)
 * @property sentAt when it was sent, in milliseconds
 * @property senderId the owner who sent it (only for owner messages)
 */
data class EntrantNotification(
    val id: String = "",
    val entrantId: String,
    val campsiteId: String,
    val type: String,
    val message: String,
    val sentAt: Long,
    val senderId: String = ""
) {
    /** True if a campsite owner sent this message. */
    fun isOwnerMessage(): Boolean {
        return type == TYPE_OWNER_MESSAGE
    }

    /** The title shown on top of the notification. */
    fun title(): String {
        return titleFor(type)
    }

    /** The text shown to the user. If no text was saved, a default text is used. */
    fun text(): String {
        if (message != "") {
            return message
        }
        return defaultTextFor(type)
    }

    /** The time it was sent, like "2026-11-23 14:05". */
    fun sentAtText(): String {
        return timeText(sentAt)
    }

    /** The notification types and some helper functions. */
    companion object {
        /** The entrant won the lottery. */
        const val TYPE_WON = "won"

        /** The entrant was not picked in the lottery. */
        const val TYPE_NOT_SELECTED = "notSelected"

        /** The entrant reserved dates at a campsite. */
        const val TYPE_RESERVED = "reserved"

        /** The entrant's reservation was cancelled. */
        const val TYPE_CANCELLED = "cancelled"

        /** The campsite owner cancelled the reservation. */
        const val TYPE_REJECTED = "rejected"

        /** A message from a campsite owner. */
        const val TYPE_OWNER_MESSAGE = "ownerMessage"

        /**
         * Gets the title for a type of notification.
         *
         * @param type the notification type
         * @return the title, or "Notification" if the type is unknown
         */
        fun titleFor(type: String): String {
            if (type == TYPE_WON) return "You won the lottery!"
            if (type == TYPE_NOT_SELECTED) return "Lottery result"
            if (type == TYPE_RESERVED) return "Reservation confirmed"
            if (type == TYPE_CANCELLED) return "Reservation cancelled"
            if (type == TYPE_REJECTED) return "Reservation cancelled by owner"
            if (type == TYPE_OWNER_MESSAGE) return "Message from campsite owner"
            return "Notification"
        }

        /**
         * Gets the default text for a type, used when no text was saved.
         *
         * @param type the notification type
         * @return the default text, or "" if there is none
         */
        fun defaultTextFor(type: String): String {
            if (type == TYPE_WON) return LotteryResultNotification.WON_MESSAGE
            if (type == TYPE_NOT_SELECTED) return LotteryResultNotification.NOT_SELECTED_MESSAGE
            if (type == TYPE_RESERVED) return "Your reservation is confirmed."
            if (type == TYPE_CANCELLED) return "Your reservation has been cancelled."
            if (type == TYPE_REJECTED) return "The campsite owner cancelled your reservation."
            return ""
        }

        /**
         * Makes a "reserved" notification for a new reservation (US 01.05.02).
         *
         * @param reservation the new reservation
         * @param now the current time in milliseconds
         * @return the notification
         */
        fun forReservation(reservation: Reservation, now: Long): EntrantNotification {
            return EntrantNotification(
                entrantId = reservation.entrantId,
                campsiteId = reservation.campsiteId,
                type = TYPE_RESERVED,
                message = "Your reservation from " + reservation.startDate + " to " + reservation.endDate + " is confirmed.",
                sentAt = now
            )
        }

        /**
         * Makes a "cancelled" notification to confirm a cancellation (US 01.07.02).
         *
         * @param reservation the cancelled reservation
         * @param now the current time in milliseconds
         * @return the notification
         */
        fun forCancellation(reservation: Reservation, now: Long): EntrantNotification {
            return EntrantNotification(
                entrantId = reservation.entrantId,
                campsiteId = reservation.campsiteId,
                type = TYPE_CANCELLED,
                message = "Your reservation from " + reservation.startDate + " to " + reservation.endDate + " has been cancelled.",
                sentAt = now
            )
        }

        /**
         * Changes a time in milliseconds into text like "2026-11-23 14:05".
         *
         * @param millis the time in milliseconds
         * @return the time as text, or "" if the time is 0
         */
        fun timeText(millis: Long): String {
            if (millis <= 0L) {
                return ""
            }
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CANADA)
            return format.format(Date(millis))
        }

        /**
         * Puts the notification into a map so it can be saved in Firestore.
         * The id is not saved because it is the document name.
         *
         * @param n the notification
         * @return the map to save
         */
        fun toMap(n: EntrantNotification): Map<String, Any> {
            return mapOf(
                "entrantId" to n.entrantId,
                "campsiteId" to n.campsiteId,
                "type" to n.type,
                "message" to n.message,
                "sentAt" to n.sentAt,
                "senderId" to n.senderId
            )
        }

        /**
         * Makes a notification from a Firestore document.
         * Anything missing becomes "" or 0.
         *
         * @param id the document id
         * @param m the document data
         * @return the notification
         */
        fun fromMap(id: String, m: Map<String, Any>): EntrantNotification {
            var sentAt = 0L
            val time = m["sentAt"]
            if (time is Number) {
                sentAt = time.toLong()
            }
            return EntrantNotification(
                id = id,
                entrantId = m["entrantId"] as? String ?: "",
                campsiteId = m["campsiteId"] as? String ?: "",
                type = m["type"] as? String ?: "",
                message = m["message"] as? String ?: "",
                sentAt = sentAt,
                senderId = m["senderId"] as? String ?: ""
            )
        }
    }
}

/**
 * Helper functions for lists of notifications: which ones should pop up on
 * the phone, sorting them, and picking the owner messages for the admin.
 *
 * Used for US 01.03.01, US 01.03.02, US 01.10.01, US 03.08.01 and US 03.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: none known.
 */
object NotificationHelper {

    /**
     * Finds the notifications that should pop up on the phone. These are the
     * ones sent after the last one we showed. If the entrant turned
     * notifications off, nothing pops up (US 01.10.01).
     *
     * @param list the entrant's notifications
     * @param lastShown the time of the last notification we showed
     * @param optedOut true if the entrant turned notifications off
     * @return the new notifications, oldest first
     */
    fun toPopUp(list: List<EntrantNotification>, lastShown: Long, optedOut: Boolean): List<EntrantNotification> {
        if (optedOut) {
            return emptyList()
        }
        val newOnes = mutableListOf<EntrantNotification>()
        for (n in list) {
            if (n.sentAt > lastShown) {
                newOnes.add(n)
            }
        }
        return newOnes.sortedBy { it.sentAt }
    }

    /**
     * Sorts notifications so the newest one is first.
     *
     * @param list the notifications
     * @return the sorted list
     */
    fun newestFirst(list: List<EntrantNotification>): List<EntrantNotification> {
        return list.sortedByDescending { it.sentAt }
    }

    /**
     * Keeps only the messages sent by campsite owners (US 03.12.01).
     *
     * @param list all notifications
     * @return only the owner messages, newest first
     */
    fun ownerMessagesOnly(list: List<EntrantNotification>): List<EntrantNotification> {
        val messages = mutableListOf<EntrantNotification>()
        for (n in list) {
            if (n.isOwnerMessage()) {
                messages.add(n)
            }
        }
        return newestFirst(messages)
    }

    /**
     * Gets the name for an id. If there is no name, the id is shown instead.
     * Used by the admin logs.
     *
     * @param names a map of id to name
     * @param id the id to look up
     * @return the name, the id, or "-" if the id is empty
     */
    fun nameOrId(names: Map<String, String>, id: String): String {
        if (id == "") {
            return "-"
        }
        val name = names[id]
        if (name == null || name == "") {
            return id
        }
        return name
    }
}
