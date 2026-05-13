package com.nayak.nammayantara.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.model.Equipment
import com.nayak.nammayantara.utils.Constants
import kotlinx.coroutines.tasks.await

class EquipmentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Returns all equipment EXCEPT those owned by the current user.
     * This ensures owners don't see their own vehicles in customer browse view.
     */
    suspend fun getAllEquipment(): List<Equipment> {
        val myUid = auth.currentUser?.uid ?: ""
        return try {
            val snapshot = db.collection(Constants.EQUIPMENT).get().await()
            snapshot.documents.mapNotNull {
                it.toObject(Equipment::class.java)?.copy(id = it.id)
            }.filter { it.ownerId != myUid }  // hide owner's own vehicles
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addEquipment(equipment: Equipment): Boolean {
        return try {
            db.collection(Constants.EQUIPMENT).add(equipment).await()
            true
        } catch (e: Exception) { false }
    }

    suspend fun addSampleData() {
        val myUid = auth.currentUser?.uid ?: "unknown"
        val samples = listOf(
            Equipment(
                ownerId = myUid,
                name = "Mahindra 575 DI",
                type = "Tractor",
                hourlyRate = 350.0,
                dailyRate = 2500.0,
                latitude = 12.9716,
                longitude = 77.5946,
                status = "Available",
                conditionRating = 4.5f,
                fuelType = "Diesel",
                lastServiceDate = "2024-01-15"
            ),
            Equipment(
                ownerId = myUid,
                name = "John Deere 5050D",
                type = "Tractor",
                hourlyRate = 400.0,
                dailyRate = 3000.0,
                latitude = 12.9800,
                longitude = 77.6100,
                status = "Available",
                conditionRating = 5f,
                fuelType = "Diesel",
                lastServiceDate = "2024-02-10"
            ),
            Equipment(
                ownerId = myUid,
                name = "Kubota Harvester",
                type = "Harvester",
                hourlyRate = 600.0,
                dailyRate = 4500.0,
                latitude = 12.9600,
                longitude = 77.5800,
                status = "Available",
                conditionRating = 4f,
                fuelType = "Diesel",
                lastServiceDate = "2023-12-05"
            ),
            Equipment(
                ownerId = myUid,
                name = "VST Shakti Sprayer",
                type = "Sprayer",
                hourlyRate = 200.0,
                dailyRate = 1500.0,
                latitude = 12.9900,
                longitude = 77.6200,
                status = "Available",
                conditionRating = 3.5f,
                fuelType = "Petrol",
                lastServiceDate = "2024-01-20"
            )
        )
        samples.forEach { addEquipment(it) }
    }
}