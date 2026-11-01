package com.example.project_halfway

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

// Saves and loads lotteries in the Firestore "lotteries" collection.
// Each lottery document looks like this:
//   campsiteId : "abc123"
//   startDate  : timestamp (or a number in milliseconds)
//   endDate    : timestamp (or a number in milliseconds)
//   waitingList: ["entrantUid1", "entrantUid2", ...]
//
// For joining and leaving we use arrayUnion / arrayRemove so two people
// joining at the same time don't overwrite each other.
//
// Outstanding issues: no delete yet, and creating a lottery is only here so the
// owner side can use it later.

object LotteryRepository {

    private fun lotteries() = FirebaseSecurity.db.collection("lotteries")

    /**
     * Saves a lottery. If it has no id, Firestore makes a new one.
     */
    fun save(lottery: Lottery) {
        val document = if (lottery.id == "") lotteries().document() else lotteries().document(lottery.id)

        document.set(toMap(lottery))
            .addOnSuccessListener { println("Saved lottery ${document.id}") }
            .addOnFailureListener { println("Save lottery failed: ${it.message}") }
    }

    /**
     * Finds the lottery that is open right now for a campsite.
     * Gives back null if there is none (or if loading failed).
     */
    fun getActiveLottery(campsiteId: String, onResult: (Lottery?) -> Unit) {
        lotteries().whereEqualTo("campsiteId", campsiteId).get()
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
     * onDone gets true if it worked.
     */
    fun joinWaitingList(lotteryId: String, entrantId: String, onDone: (Boolean) -> Unit) {
        lotteries().document(lotteryId)
            .update("waitingList", FieldValue.arrayUnion(entrantId))
            .addOnSuccessListener { onDone(true) }
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

    // Lottery -> map so Firestore can store it. Dates are saved as milliseconds.
    fun toMap(lottery: Lottery): Map<String, Any> {
        return mapOf(
            "campsiteId" to lottery.campsiteId,
            "startDate" to lottery.startDate,
            "endDate" to lottery.endDate,
            "waitingList" to lottery.waitingList
        )
    }

    // map -> Lottery. The id is the document name.
    fun fromMap(id: String, m: Map<String, Any>): Lottery {
        val campsiteId = m["campsiteId"] as? String ?: ""

        val list = mutableListOf<String>()
        val value = m["waitingList"]
        if (value is List<*>) {
            for (item in value) {
                if (item is String) {
                    list.add(item)
                }
            }
        }

        return Lottery(
            id = id,
            campsiteId = campsiteId,
            startDate = readDate(m["startDate"]),
            endDate = readDate(m["endDate"]),
            waitingList = list
        )
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
