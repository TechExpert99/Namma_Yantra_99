package com.nayak.nammayantara.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.model.Booking
import com.nayak.nammayantara.utils.Constants
import kotlinx.coroutines.tasks.await

class BookingRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun createBooking(booking: Booking): String? {
        return try {
            val doc = db.collection(Constants.BOOKINGS).add(booking).await()
            doc.id
        } catch (e: Exception) { null }
    }

    suspend fun getMyBookings(): List<Booking> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection(Constants.BOOKINGS)
                .whereEqualTo("renterId", uid)
                .get().await()
            snapshot.documents.mapNotNull {
                it.toObject(Booking::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) { emptyList() }
    }

    // ── NEW: fetch all bookings where ownerId == current user ──
    suspend fun getIncomingRequests(): List<Booking> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection(Constants.BOOKINGS)
                .whereEqualTo("ownerId", uid)
                .get().await()
            snapshot.documents.mapNotNull {
                it.toObject(Booking::class.java)?.copy(id = it.id)
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): Boolean {
        return try {
            db.collection(Constants.BOOKINGS)
                .document(bookingId)
                .update("status", status).await()
            true
        } catch (e: Exception) { false }
    }

    fun getCurrentUserId() = auth.currentUser?.uid ?: ""
}