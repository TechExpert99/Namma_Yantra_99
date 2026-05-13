package com.nayak.nammayantara.data.model

data class Review(
    val id: String = "",
    val equipmentId: String = "",
    val equipmentName: String = "",
    val reviewerId: String = "",
    val reviewerPhone: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val createdAt: Long = System.currentTimeMillis()
)