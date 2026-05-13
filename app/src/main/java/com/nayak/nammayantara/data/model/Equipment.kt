package com.nayak.nammayantara.data.model

data class Equipment(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val type: String = "",              // Tractor / Harvester / Sprayer
    val hourlyRate: Double = 0.0,
    val dailyRate: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",      // Human-readable area name from reverse geocoding
    val status: String = "Available",   // Available / Booked / In-Use
    val conditionRating: Float = 0f,
    val fuelType: String = "",
    val lastServiceDate: String = "",
    val availableDates: List<String> = emptyList() // "yyyy-MM-dd" strings
)