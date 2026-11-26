package com.example.project_halfway

/**
 * Lets the admin load and delete entrant reviews (US 03.10.01).
 *
 * It uses the admin database like the rest of the admin code. Reviews are
 * saved by [ReviewRepository] (US 01.13.02), one document per review, so
 * removing one review only deletes that one document.
 */
object AdminReviewRepository {

    /** Loads every review, newest first. */
    fun getAll(onResult: (List<Review>) -> Unit, onError: (String) -> Unit) {
        FirebaseSecurity.adminDB.collection("reviews").get()
            .addOnSuccessListener { snapshot ->
                val reviews = snapshot.documents.map { doc ->
                    ReviewRepository.fromMap(doc.id, doc.data ?: emptyMap())
                }
                onResult(reviews.sortedByDescending { it.createdAt })
            }
            .addOnFailureListener { onError("Could not load reviews") }
    }

    /** Deletes one review by its id. */
    fun delete(reviewId: String, onDone: (Boolean) -> Unit) {
        FirebaseSecurity.adminDB.collection("reviews").document(reviewId)
            .delete()
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }
}