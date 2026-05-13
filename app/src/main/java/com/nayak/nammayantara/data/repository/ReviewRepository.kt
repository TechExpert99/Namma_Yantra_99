package com.nayak.nammayantara.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.model.Review
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun submitReview(
        equipmentId: String,
        equipmentName: String,
        rating: Float,
        comment: String
    ): Result<String> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
            val review = Review(
                equipmentId = equipmentId,
                equipmentName = equipmentName,
                reviewerId = user.uid,
                reviewerPhone = user.phoneNumber ?: "Unknown",
                rating = rating,
                comment = comment
            )
            val ref = db.collection("reviews").add(review).await()
            // Update equipment's average rating
            updateEquipmentRating(equipmentId)
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewsForEquipment(equipmentId: String): List<Review> {
        return try {
            val snapshot = db.collection("reviews")
                .whereEqualTo("equipmentId", equipmentId)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun updateEquipmentRating(equipmentId: String) {
        try {
            val reviews = getReviewsForEquipment(equipmentId)
            if (reviews.isNotEmpty()) {
                val avg = reviews.map { it.rating }.average()
                db.collection("equipment").document(equipmentId)
                    .update("conditionRating", (Math.round(avg * 10) / 10.0))
                    .await()
            }
        } catch (_: Exception) {}
    }
}