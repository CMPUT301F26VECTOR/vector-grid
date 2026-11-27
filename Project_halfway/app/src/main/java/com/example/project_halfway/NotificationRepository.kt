package com.example.project_halfway

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

/**
 * Saves and loads notifications in the Firestore "notifications" collection.
 * It also saves the entrant's "NotificationsOff" setting in their "Users" document.
 *
 * The entrant parts use FirebaseSecurity.db and the admin parts use
 * FirebaseSecurity.adminDB.
 *
 * Used for US 01.03.01, US 01.03.02, US 01.05.02, US 01.07.02, US 01.10.01, US 03.08.01 and US 03.12.01.
 *
 * **AI assistance (Parneet Kaur):** this code was written with Claude; Parneet tested it and checked that it works.
 *
 * Anthropic. (2026). *Claude* (Claude Opus 5.5) [Large language model]. https://claude.ai
 *
 * Outstanding issues: the admin log loads all notifications at once, which could be slow if there are a lot.
 */
object NotificationRepository {

    /**
     * Saves a new notification for an entrant.
     *
     * @param n the notification to save
     * @param onDone gets true if it was saved, false if not
     */
    fun send(n: EntrantNotification, onDone: (Boolean) -> Unit = {}) {
        FirebaseSecurity.db.collection("notifications")
            .add(EntrantNotification.toMap(n))
            .addOnSuccessListener {
                onDone(true)
            }
            .addOnFailureListener {
                println("Could not send notification: " + it.message)
                onDone(false)
            }
    }

    /**
     * Listens to an entrant's notifications. onChange is called right away,
     * and then again every time a notification is added.
     *
     * @param entrantId the entrant's id
     * @param onChange gets the entrant's notifications, newest first
     * @return the listener (call remove() on it to stop listening)
     */
    fun listenForEntrant(entrantId: String, onChange: (List<EntrantNotification>) -> Unit): ListenerRegistration {
        return FirebaseSecurity.db.collection("notifications")
            .whereEqualTo("entrantId", entrantId)
            .addSnapshotListener { result, error ->
                if (error != null || result == null) {
                    println("Could not load notifications: " + error?.message)
                } else {
                    val list = mutableListOf<EntrantNotification>()
                    for (doc in result.documents) {
                        val data = doc.data
                        if (data != null) {
                            list.add(EntrantNotification.fromMap(doc.id, data))
                        }
                    }
                    onChange(NotificationHelper.newestFirst(list))
                }
            }
    }

    /**
     * Checks if the entrant turned notifications off (US 01.10.01).
     *
     * @param uid the entrant's id
     * @param onResult gets true if notifications are off, false if they are on
     */
    fun isOptedOut(uid: String, onResult: (Boolean) -> Unit) {
        FirebaseSecurity.db.collection("Users").document(uid).get()
            .addOnSuccessListener { doc ->
                val off = doc.getBoolean("NotificationsOff")
                onResult(off == true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    /**
     * Saves if the entrant wants notifications or not (US 01.10.01).
     *
     * @param uid the entrant's id
     * @param off true to turn notifications off, false to turn them on
     * @param onDone gets true if it was saved, false if not
     */
    fun setOptedOut(uid: String, off: Boolean, onDone: (Boolean) -> Unit) {
        FirebaseSecurity.db.collection("Users").document(uid)
            .set(mapOf("NotificationsOff" to off), SetOptions.merge())
            .addOnSuccessListener {
                onDone(true)
            }
            .addOnFailureListener {
                println("Could not save the setting: " + it.message)
                onDone(false)
            }
    }

    /**
     * Loads every notification in the app for the admin log (US 03.08.01).
     * This is the app's notifications (lottery results, reservations,
     * cancellations) and the owner messages together.
     *
     * @param onResult gets all the notifications, newest first
     * @param onError gets an error message if loading failed
     */
    fun getAllForAdmin(onResult: (List<EntrantNotification>) -> Unit, onError: (String) -> Unit) {
        FirebaseSecurity.adminDB.collection("notifications").get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<EntrantNotification>()
                for (doc in result.documents) {
                    val data = doc.data
                    if (data != null) {
                        list.add(EntrantNotification.fromMap(doc.id, data))
                    }
                }
                // then add the owner messages
                getOwnerMessagesForAdmin(
                    onResult = { messages ->
                        list.addAll(messages)
                        onResult(NotificationHelper.newestFirst(list))
                    },
                    onError = {
                        // still show the app's notifications if the messages didn't load
                        onResult(NotificationHelper.newestFirst(list))
                    }
                )
            }
            .addOnFailureListener {
                onError("Could not load notifications: " + it.message)
            }
    }

    /**
     * Loads the messages campsite owners sent to entrants (US 03.12.01).
     * Owners' messages are saved in the "notification_logs" collection
     * (US 02.10.01), so each one is changed into an EntrantNotification
     * with type "ownerMessage".
     *
     * @param onResult gets the owner messages, newest first
     * @param onError gets an error message if loading failed
     */
    fun getOwnerMessagesForAdmin(onResult: (List<EntrantNotification>) -> Unit, onError: (String) -> Unit) {
        FirebaseSecurity.adminDB.collection("notification_logs").get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<EntrantNotification>()
                for (doc in result.documents) {
                    // the time is saved as a Firestore Timestamp, so change it to milliseconds
                    var sentAt = 0L
                    val time = doc.getTimestamp("timestamp")
                    if (time != null) {
                        sentAt = time.toDate().time
                    }

                    val title = doc.getString("title") ?: ""
                    val message = doc.getString("message") ?: ""
                    var text = message
                    if (title != "") {
                        text = title + ": " + message
                    }

                    list.add(
                        EntrantNotification(
                            id = doc.id,
                            entrantId = "",
                            campsiteId = "",
                            type = EntrantNotification.TYPE_OWNER_MESSAGE,
                            message = text,
                            sentAt = sentAt,
                            senderId = doc.getString("senderId") ?: "",
                            lotteryId = doc.getString("lotteryId") ?: ""
                        )
                    )
                }
                onResult(NotificationHelper.newestFirst(list))
            }
            .addOnFailureListener {
                onError("Could not load owner messages: " + it.message)
            }
    }

    /**
     * Loads the names of users and campsites, so the admin log can show
     * names instead of ids.
     *
     * @param onResult gets a map of id to name
     */
    fun loadNamesForAdmin(onResult: (Map<String, String>) -> Unit) {
        val names = mutableMapOf<String, String>()
        val db = FirebaseSecurity.adminDB

        // first load the users, then the campsites
        db.collection("Users").get().addOnCompleteListener { users ->
            if (users.isSuccessful) {
                for (doc in users.result.documents) {
                    names[doc.id] = doc.getString("Name") ?: ""
                }
            }
            db.collection("campsites").get().addOnCompleteListener { campsites ->
                if (campsites.isSuccessful) {
                    for (doc in campsites.result.documents) {
                        names[doc.id] = doc.getString("campsiteName") ?: ""
                    }
                }
                onResult(names)
            }
        }
    }
}
