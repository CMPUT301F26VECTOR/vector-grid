package com.example.project_halfway

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.Filter

/**
 * Saves and loads lotteries in the Firestore "lotteries" collection.
 *
 * What a lottery document looks like:
 *
 *     campsiteId       : "abc123"                 first campsite
 *     campsiteIds      : ["abc123", "def456"]     every campsite (US 02.04.02)
 *     startDate        : timestamp or ms          registration opens
 *     endDate          : timestamp or ms          registration closes
 *     waitingList      : ["uid1", "uid2", ...]
 *     numberOfWinners  : 2                        missing means 1
 *     winners          : ["uid2"]                 filled in by the draw
 *     notSelected      : ["uid1"]                 filled in by the draw
 *     drawn            : true                     missing means false
 *     bookingStartDate : timestamp or ms          missing means not set (US 02.04.04)
 *     bookingEndDate   : timestamp or ms          missing means not set (US 02.04.04)
 *     waitlistCapacity : 50                       missing means no limit (US 02.04.07)
 *
 * How two people at once are handled:
 * - Join and leave use arrayUnion / arrayRemove, so nobody overwrites anyone else.
 * - Joining a lottery with a waiting list limit uses a transaction, so the limit holds.
 * - The draw uses a transaction too, so two phones can't both draw the same lottery.
 * - The draw also writes a "won" or "notSelected" notification for every entrant (LotteryResultNotification).
 *
 * Which login is used:
 * - Entrant side (save, join, leave, getById, removeFromAllWaitingLists) uses FirebaseSecurity.db.
 * - Owner side (getLotteriesForCampsite, updateCampsites, drawIfDue, getEntrantNames) uses ownerDb.
 *
 * Used for US 01.01.06, 01.02.01, 01.02.02, 01.08.01 (entrant side) and
 * US 02.04.01, 02.04.02, 02.04.04, 02.04.07, 02.05.01, 02.09.01 (owner side).
 *
 * Outstanding issues: lotteries can't be deleted yet, and the draw only runs
 * when the owner opens the lottery's waitlist screen after registration
 * closes, not on a server timer (see drawIfDue).
 */

object LotteryRepository {

    private fun lotteries() = FirebaseSecurity.db.collection("lotteries")

    private fun ownerLotteries() = FirebaseSecurity.ownerDb.collection("lotteries")

    // ---------- entrant side ----------
 
    /**
     * Saves a lottery (US 02.04.01). If it has no id, Firestore makes a new one.
     *
     * @param lottery the lottery to save
     * @param onDone gets true once it is saved, false if it failed (optional, so save(lottery) still works)
     */
    fun save(lottery: Lottery, onDone: (Boolean) -> Unit = {}) {
        val document = if (lottery.id == "") lotteries().document() else lotteries().document(lottery.id)

        document.set(toMap(lottery))
            .addOnSuccessListener {
                println("Saved lottery ${document.id}")
                onDone(true)
            }
            .addOnFailureListener {
                println("Save lottery failed: ${it.message}")
                onDone(false)
            }
    }

    /**
     * Finds the lottery that is open right now for a campsite (US 01.01.06).
     *
     * Looks at lotteries whose campsiteId or campsiteIds has this campsite.
     *
     * @param campsiteId the campsite's id
     * @param onResult gets the open lottery, or null if there is none or loading failed
     */
    fun getActiveLottery(campsiteId: String, onResult: (Lottery?) -> Unit) {
        lotteries().where(
            Filter.or(
                Filter.equalTo("campsiteId", campsiteId),
                Filter.arrayContains("campsiteIds", campsiteId)
            )
        ).get()
            .addOnSuccessListener { result ->
                val now = System.currentTimeMillis()
                var active: Lottery? = null

                for (document in result.documents) {
                    val data = document.data
                    if (data != null) {
                        val lottery = fromMap(document.id, data)
                        if (lottery.isActive(now)) {
                            active = lottery
                        }
                    }
                }
                onResult(active)
            }
            .addOnFailureListener {
                println("Load lottery failed: ${it.message}")
                onResult(null)
            }
    }


    /**
     * Adds the entrant to the waiting list in Firestore (US 01.02.01).
     *
     * - No waiting list limit: a plain arrayUnion. This also works offline (sent when the phone reconnects).
     * - With a limit (US 02.04.07): a transaction, so the limit holds even if two people press Join at once. This needs a connection.
     *
     * The phone's saved copy of the lottery tells which kind it is. With no saved
     * copy, the transaction is used to be safe.
     *
     * @param lotteryId the lottery's id
     * @param entrantId the entrant's id
     * @param onDone gets true if they joined, false if it failed or they can't join
     * (closed, already joined, or the list is full)
     */
    fun joinWaitingList(lotteryId: String, entrantId: String, onDone: (Boolean) -> Unit) {
        val document = lotteries().document(lotteryId)

        document.get(Source.CACHE)
            .addOnSuccessListener { cached ->
                val data = cached.data
                if (data != null && fromMap(lotteryId, data).waitlistCapacity == null) {
                    joinWithoutLimit(document, entrantId, onDone)
                } else {
                    joinWithLimit(document, lotteryId, entrantId, onDone)
                }
            }
            .addOnFailureListener {
                joinWithLimit(document, lotteryId, entrantId, onDone)
            }
    }

    private fun joinWithoutLimit(document: DocumentReference, entrantId: String, onDone: (Boolean) -> Unit) {
        document.update("waitingList", FieldValue.arrayUnion(entrantId))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener {
                println("Join failed: ${it.message}")
                onDone(false)
            }
    }

    private fun joinWithLimit(
        document: DocumentReference,
        lotteryId: String,
        entrantId: String,
        onDone: (Boolean) -> Unit
    ) {
        FirebaseSecurity.db.runTransaction { transaction ->
            val data = transaction.get(document).data
            if (data == null) {
                false
            } else {
                val lottery = fromMap(lotteryId, data)
                if (lottery.isOnWaitingList(entrantId)) {
                    true
                } else {
                    val allowed = lottery.joinWaitingList(entrantId, System.currentTimeMillis())
                    if (allowed) {
                        transaction.update(document, "waitingList", FieldValue.arrayUnion(entrantId))
                    }
                    allowed
                }
            }
        }
            .addOnSuccessListener { joined -> onDone(joined) }
            .addOnFailureListener {
                println("Join failed: ${it.message}")
                onDone(false)
            }
    }

  /**
     * Removes the entrant from the waiting list in Firestore (US 01.02.02).
     *
     * @param lotteryId the lottery's id
     * @param entrantId the entrant's id
     * @param onDone gets true if it worked, false if it failed
     */
    fun leaveWaitingList(lotteryId: String, entrantId: String, onDone: (Boolean) -> Unit) {
        lotteries().document(lotteryId)
            .update("waitingList", FieldValue.arrayRemove(entrantId))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener {
                println("Leave failed: ${it.message}")
                onDone(false)
            }
    }

     /**
     * Loads one lottery, for example so a winner can see its booking period (US 02.04.04).
     *
     * @param lotteryId the lottery's id
     * @param onResult gets the lottery, or null if it couldn't be loaded
     */
    fun getById(lotteryId: String, onResult: (Lottery?) -> Unit) {
        lotteries().document(lotteryId).get()
            .addOnSuccessListener { document ->
                val data = document.data
                onResult(if (data == null) null else fromMap(document.id, data))
            }
            .addOnFailureListener {
                println("Load lottery failed: ${it.message}")
                onResult(null)
            }
    }

    /**
     * Takes an entrant off every waiting list they are on, when they delete
     * their profile (US 01.08.01).
     *
     * All the changes are sent together in one batch.
     *
     * @param entrantId the entrant's id
     * @param onDone gets true once every list is updated (or there were none), false if it failed
     */
    fun removeFromAllWaitingLists(entrantId: String, onDone: (Boolean) -> Unit) {
        lotteries().whereArrayContains("waitingList", entrantId).get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    onDone(true)
                    return@addOnSuccessListener
                }
                val batch = FirebaseSecurity.db.batch()
                for (document in result.documents) {
                    batch.update(document.reference, "waitingList", FieldValue.arrayRemove(entrantId))
                }
                batch.commit()
                    .addOnSuccessListener { onDone(true) }
                    .addOnFailureListener {
                        println("Removing from waiting lists failed: ${it.message}")
                        onDone(false)
                    }
            }
            .addOnFailureListener {
                println("Loading the entrant's waiting lists failed: ${it.message}")
                onDone(false)
            }
    }

    /**
     * Loads every lottery for a campsite, open or closed, for the owner's screens (US 02.09.01).
     *
     * @param campsiteId the campsite's id
     * @param onResult gets the lotteries, or an empty list if there are none or loading failed
     */
    fun getLotteriesForCampsite(campsiteId: String, onResult: (List<Lottery>) -> Unit) {
        ownerLotteries().where(
            Filter.or(
                Filter.equalTo("campsiteId", campsiteId),
                Filter.arrayContains("campsiteIds", campsiteId)
            )
        ).get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<Lottery>()
                for (document in result.documents) {
                    val data = document.data
                    if (data != null) {
                        list.add(fromMap(document.id, data))
                    }
                }
                onResult(list)
            }
            .addOnFailureListener {
                println("Load lotteries failed: ${it.message}")
                onResult(emptyList())
            }
    }
    
    /**
     * Saves which campsites a lottery is for, for example after the owner removes one (US 02.04.02).
     *
     * Only the campsite fields are written, so the waiting list and the draw stay the same.
     *
     * @param lotteryId the lottery's id
     * @param campsiteIds the campsites the lottery has now; the first one is the main one
     * @param onDone gets true once it is saved, false if it failed
     */
    fun updateCampsites(lotteryId: String, campsiteIds: List<String>, onDone: (Boolean) -> Unit) {
        ownerLotteries().document(lotteryId)
            .update(
                mapOf(
                    "campsiteId" to (campsiteIds.firstOrNull() ?: ""),
                    "campsiteIds" to campsiteIds
                )
            )
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener {
                println("Update campsites failed: ${it.message}")
                onDone(false)
            }
    }

   /**
     * Draws the winners if registration has closed and nobody has drawn yet (US 02.05.01).
     *
     * Safe to call every time a lottery screen opens. It runs in a transaction,
     * so if two phones try at the same moment, the second one sees drawn = true
     * and does nothing. The "won" / "notSelected" notifications are written in
     * the same transaction.
     *
     * @param lotteryId the lottery's id
     * @param onDone gets the lottery after this (drawn or not), or null if it couldn't be loaded
     */
    fun drawIfDue(lotteryId: String, onDone: (Lottery?) -> Unit) {
        val document = ownerLotteries().document(lotteryId)

        FirebaseSecurity.ownerDb.runTransaction { transaction ->
            val snapshot = transaction.get(document)
            val data = snapshot.data
            if (data == null) {
                null
            } else {
                val lottery = fromMap(snapshot.id, data)
                val now = System.currentTimeMillis()
                if (lottery.drawWinners(now)) {
                    transaction.update(
                        document,
                        mapOf(
                            "winners" to lottery.winners,
                            "notSelected" to lottery.notSelected,
                            "drawn" to true
                        )
                    )
 
                    for (notification in LotteryResultNotification.forDraw(lottery, now)) {
                        transaction.set(
                            FirebaseSecurity.ownerDb.collection("notifications").document(),
                            LotteryResultNotification.toMap(notification)
                        )
                    }
                }
                lottery
            }
        }
            .addOnSuccessListener { lottery -> onDone(lottery) }
            .addOnFailureListener {
                println("Draw failed: ${it.message}")
                onDone(null)
            }
    }

    /**
     * Looks up entrants' names in the "Users" collection, for the owner's waitlist screen (US 02.09.01).
     *
     * Anyone without a name yet is shown as "Entrant" plus the start of their id.
     *
     * @param entrantIds the entrants to look up
     * @param onResult gets a map of entrant id to the name to show, once every lookup is back
     */
    fun getEntrantNames(entrantIds: List<String>, onResult: (Map<String, String>) -> Unit) {
        if (entrantIds.isEmpty()) {
            onResult(emptyMap())
            return
        }
        val names = mutableMapOf<String, String>()
        var answered = 0

        for (id in entrantIds) {
            FirebaseSecurity.ownerDb.collection("Users").document(id).get()
                .addOnSuccessListener { document ->
                    names[id] = nameToShow(id, document.getString("Name"))
                    answered++
                    if (answered == entrantIds.size) onResult(names)
                }
                .addOnFailureListener {
                    names[id] = nameToShow(id, null)
                    answered++
                    if (answered == entrantIds.size) onResult(names)
                }
        }
    }

      /**
     * Picks the name to show for an entrant on the owner's lists.
     *
     * @param entrantId the entrant's id
     * @param name the name saved in their profile, or null if there is none
     * @return the name, or "Entrant" plus the first 6 letters of their id if it is blank
     */
    fun nameToShow(entrantId: String, name: String?): String {
        return if (name.isNullOrBlank()) "Entrant ${entrantId.take(6)}" else name
    }

   // ---------- changing a Lottery to Firestore data and back ----------
 
    /**
     * Turns a lottery into a map that Firestore can store.
     *
     * Dates are saved as milliseconds. Booking dates of 0 and a capacity of null
     * mean "not set", so they are left out.
     *
     * @param lottery the lottery to store
     * @return the lottery's fields (the id is the document's name, not a field)
     */
    fun toMap(lottery: Lottery): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "campsiteId" to (lottery.campsiteIds.firstOrNull() ?: lottery.campsiteId),
            "campsiteIds" to lottery.campsiteIds,
            "startDate" to lottery.startDate,
            "endDate" to lottery.endDate,
            "waitingList" to lottery.waitingList,
            "numberOfWinners" to lottery.numberOfWinners,
            "winners" to lottery.winners,
            "notSelected" to lottery.notSelected,
            "drawn" to lottery.isDrawn
        )
        if (lottery.bookingStartDate > 0L) {
            map["bookingStartDate"] = lottery.bookingStartDate
        }
        if (lottery.bookingEndDate > 0L) {
            map["bookingEndDate"] = lottery.bookingEndDate
        }
        val capacity = lottery.waitlistCapacity
        if (capacity != null) {
            map["waitlistCapacity"] = capacity
        }
        return map
    }

  /**
     * Turns a Firestore document back into a lottery.
     *
     * Fields that older lotteries don't have get safe defaults.
     *
     * @param id the document's name, used as the lottery id
     * @param m the document's data
     * @return the lottery
     */
    fun fromMap(id: String, m: Map<String, Any>): Lottery {
        val campsiteId = m["campsiteId"] as? String ?: ""
        val campsiteIds = readIds(m["campsiteIds"]).ifEmpty { listOf(campsiteId) }

        val list = mutableListOf<String>()
        val value = m["waitingList"]
        if (value is List<*>) {
            for (item in value) {
                if (item is String) {
                    list.add(item)
                }
            }
        }

        val winnerCount = m["numberOfWinners"]
        val capacity = m["waitlistCapacity"]

        return Lottery(
            id = id,
            campsiteId = campsiteIds.first(),
            startDate = readDate(m["startDate"]),
            endDate = readDate(m["endDate"]),
            waitingList = list,
            numberOfWinners = if (winnerCount is Number) winnerCount.toInt() else 1,
            winners = readIds(m["winners"]),
            notSelected = readIds(m["notSelected"]),
            isDrawn = m["drawn"] == true,
            bookingStartDate = readDate(m["bookingStartDate"]),
            bookingEndDate = readDate(m["bookingEndDate"]),
            waitlistCapacity = if (capacity is Number && capacity.toInt() > 0) capacity.toInt() else null,
            campsiteIds = campsiteIds
        )
    }

    private fun readIds(value: Any?): List<String> {
        if (value is List<*>) {
            return value.filterIsInstance<String>()
        }
        return emptyList()
    }

    private fun readDate(value: Any?): Long {
        if (value is Timestamp) {
            return value.toDate().time
        }
        if (value is Number) {
            return value.toLong()
        }
        return 0L
    }
}
