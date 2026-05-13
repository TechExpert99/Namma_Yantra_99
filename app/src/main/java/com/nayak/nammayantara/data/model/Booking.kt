package com.nayak.nammayantara.data.model

data class Booking(
    val id: String = "",
    val equipmentId: String = "",
    val equipmentName: String = "",
    val renterId: String = "",
    val ownerId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val totalHours: Int = 0,
    val totalDays: Int = 0,
    val totalPrice: Double = 0.0,
    val status: String = "Pending",
    val createdAt: Long = System.currentTimeMillis()
)