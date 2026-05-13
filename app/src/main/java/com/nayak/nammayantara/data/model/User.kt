package com.nayak.nammayantara.data.model

data class User(
    val uid: String = "",
    val phone: String = "",
    val name: String = "",
    val role: String = "",       // "owner" or "renter"
    val location: String = "",
    val rating: Float = 0f,
    val birthDate: String = "",  // "yyyy-MM-dd"
    val gender: String = "",     // "Male" / "Female" / "Other"
    val address: String = "",
    val photoBase64: String = ""
)