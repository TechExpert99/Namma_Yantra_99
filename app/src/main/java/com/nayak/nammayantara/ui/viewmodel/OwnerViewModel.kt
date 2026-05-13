package com.nayak.nammayantara.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nayak.nammayantara.data.model.Booking
import com.nayak.nammayantara.data.repository.BookingRepository
import kotlinx.coroutines.launch

class OwnerViewModel : ViewModel() {
    private val repository = BookingRepository()

    var incomingRequests by mutableStateOf<List<Booking>>(emptyList())
    var isLoading by mutableStateOf(false)
    var actionMessage by mutableStateOf("")

    fun loadIncomingRequests() {
        viewModelScope.launch {
            isLoading = true
            incomingRequests = repository.getIncomingRequests()
            isLoading = false
        }
    }

    fun acceptRequest(bookingId: String) {
        viewModelScope.launch {
            val success = repository.updateBookingStatus(bookingId, "Accepted")
            if (success) {
                actionMessage = "✅ Request Accepted!"
                loadIncomingRequests()
            } else {
                actionMessage = "❌ Failed. Try again."
            }
        }
    }

    fun declineRequest(bookingId: String) {
        viewModelScope.launch {
            val success = repository.updateBookingStatus(bookingId, "Declined")
            if (success) {
                actionMessage = "❌ Request Declined."
                loadIncomingRequests()
            } else {
                actionMessage = "❌ Failed. Try again."
            }
        }
    }
}