package com.example.project_halfway

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.Filter

/**
 * Saves and loads lotteries in the Firestore "lotteries" collection.
 * Each lottery document looks like this:
 *   campsiteId : "abc123"
 *  campsiteIds: ["abc123", "def456"]  (every campsite in the lottery, US 02.04.02)
 *   startDate  : timestamp (or a number in milliseconds)
 *   endDate    : timestamp (or a number in milliseconds)
 *   waitingList: ["entrantUid1", "entrantUid2", ...]
 *   numberOfWinners: 2               (missing means 1)
 *   winners    : ["entrantUid2"]     (filled in by the draw)
 *   notSelected: ["entrantUid1"]     (filled in by the draw)
 *   drawn      : true                (missing means false)
 *   bookingStartDate: timestamp or ms (missing means not set, US 02.04.04)
 *   bookingEndDate  : timestamp or ms (missing means not set, US 02.04.04)
 *   waitlistCapacity: 50              (missing means no limit, US 02.04.07)
 *
 * For joining and leaving we use arrayUnion / arrayRemove so two people at the
 * same time don't overwrite each other. Joining a lottery with a waiting list
 * limit uses a transaction instead, so the limit holds. The draw also uses a
 * transaction so two phones can't both draw the same lottery.
 *
 * Entrant-side functions use FirebaseSecurity.db (the entrant's login).
 * The owner-side functions added for US 02.05.01 and 02.09.01
 * (getLotteriesForCampsite, drawIfDue, getEntrantNames) and for US 02.04.02
 * (updateCampsites) use ownerDb.
 * Outstanding issues: no delete yet. Lotteries are created through save from
 * CreateLotteryScreen (US 02.04.01, US 02.04.02).
 * The draw runs when the owner opens the lottery's waitlist screen after
 * registration closes, not on a server timer (see drawIfDue).
 */
object LotteryRepository {

    private fun lotteries() = FirebaseSecurity.db.collection("lotteries")

    // The same collection through the owner's login (like SignUpScreenUI uses ownerDb),
    // for the owner-side functions below.
    private fun ownerLotteries() = FirebaseSecurity.ownerDb.collection("lotteries")

    /**
     * Saves a lottery. If it has no id, Firestore makes a new one.
     *
     * @param lottery the lottery to save
     * @param onDone gets true once Firestore has saved it, or false if it failed.
     * It is optional, so older calls like save(lottery) still work.
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
     * Finds the lottery that is open right now for a campsite.
     * Gives back null if there is none (or if loading failed).
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
     * Adds the entrant to the waiting list in Firestore.
     * onDone gets true if it worked, or false if it failed or the entrant
     * can't join (closed, already joined, or the waiting list is full).
     *
     * Lotteries with no waiting list limit use a plain arrayUnion, which also
     * works offline (Firestore sends it when the phone reconnects).
     *
     * Lotteries with a limit (US 02.04.07) use a transaction instead, so the
     * limit holds even if two people press Join at the same moment: Firestore
     * re-reads the list and starts over if someone else changed it first.
     * Transactions need a connection, so joining a limited lottery offline fails.
     *
     * The phone's saved copy of the lottery (loaded when the screen opened) is
     * used to tell which kind it is. If there is no saved copy, the transaction
     * is used to be safe.
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
                // nothing saved on the phone yet
                joinWithLimit(document, lotteryId, entrantId, onDone)
            }
    }

    // No limit: add the entrant straight away (the original way of joining).
    private fun joinWithoutLimit(document: DocumentReference, entrantId: String, onDone: (Boolean) -> Unit) {
        document.update("waitingList", FieldValue.arrayUnion(entrantId))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener {
                println("Join failed: ${it.message}")
                onDone(false)
            }
    }

    // With a limit: check it and add the entrant inside one transaction.
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
                    // already joined (e.g. on another phone), so there is nothing to do
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
     * Removes the entrant from the waiting list in Firestore.
     * onDone gets true if it worked.
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
     * Loads every lottery for a campsite, open or closed (for the owner's screens).
     * Gives back an empty list if there are none (or if loading failed).
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
     * Saves the list of campsites a lottery is for (US 02.04.02), for example after
     * the owner removes one. Only the campsite fields are written, so the waiting
     * list and the draw are left as they are.
     *
     * @param lotteryId the Firestore id of the lottery
     * @param campsiteIds the campsites the lottery has now; the first is the main one
     * @param onDone gets true once Firestore has saved it, or false if it failed
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
     * Draws the winners if registration has closed and nobody has drawn yet
     * (US 02.05.01). Safe to call every time a lottery screen opens.
     *
     * Runs inside a Firestore transaction: Firestore re-reads the lottery,
     * and if another phone changed it at the same moment, it starts over.
     * So the second phone sees drawn = true and does nothing.
     *
     * onDone gets the lottery as it is after this (drawn or not),
     * or null if it could not be loaded.
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
                if (lottery.drawWinners(System.currentTimeMillis())) {
                    // only write the draw fields, so nothing else in the document is touched
                    transaction.update(
                        document,
                        mapOf(
                            "winners" to lottery.winners,
                            "notSelected" to lottery.notSelected,
                            "drawn" to true
                        )
                    )
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
     * Looks up the names of entrants from the "Users" collection (US 02.09.01).
     * Anyone without a name yet (device sign-in starts with a blank name)
     * is shown as "Entrant" plus the start of their id.
     *
     * onResult gets a map of entrant id -> name to show, once every lookup is back.
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
     * The name to show for an entrant on the owner's lists.
     *
     * @param entrantId the entrant's uid
     * @param name the name saved in their profile, or null if there was none
     * @return the name, or "Entrant" plus the first 6 letters of their id if it is blank
     */
    fun nameToShow(entrantId: String, name: String?): String {
        return if (name.isNullOrBlank()) "Entrant ${entrantId.take(6)}" else name
    }

    /**
     * Lottery -> map so Firestore can store it. Dates are saved as milliseconds.
     * A booking date of 0 and a capacity of null mean "not set", so they are left out.
     *
     * @param lottery the lottery to store
     * @return the lottery's fields as a map (the id is the document's name, not a field)
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
     * map -> Lottery. The id is the document name. Fields that older lotteries
     * don't have get safe defaults.
     *
     * @param id the Firestore document's name, used as the lottery id
     * @param m the document's data
     * @return the lottery
     */
    fun fromMap(id: String, m: Map<String, Any>): Lottery {
        val campsiteId = m["campsiteId"] as? String ?: ""
        // Newer lotteries list every campsite; older ones only have campsiteId.
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

        // Older lotteries don't have these fields, so use safe defaults.
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
            // 0 or less (e.g. typed in the console) would block everyone, so treat it as no limit
            waitlistCapacity = if (capacity is Number && capacity.toInt() > 0) capacity.toInt() else null,
            campsiteIds = campsiteIds
        )
    }

    // A list of entrant ids from Firestore, skipping anything that isn't text.
    private fun readIds(value: Any?): List<String> {
        if (value is List<*>) {
            return value.filterIsInstance<String>()
        }
        return emptyList()
    }

    // Dates might be a Firestore timestamp (if added in the console) or a number
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
