package com.nayak.nammayantara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nayak.nammayantara.data.model.Booking
import com.nayak.nammayantara.data.model.Equipment
import com.nayak.nammayantara.data.repository.BookingRepository
import com.nayak.nammayantara.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookingViewModel : ViewModel() {

    private val repository = BookingRepository()

    var isLoading by mutableStateOf(false)
    var bookingId by mutableStateOf("")
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var myBookings by mutableStateOf<List<Booking>>(emptyList())

    // Selected duration
    var hours by mutableStateOf(1)
    var days by mutableStateOf(1)
    var useDaily by mutableStateOf(false)

    fun getTotalPrice(equipment: Equipment): Double {
        return if (useDaily) days * equipment.dailyRate
        else hours * equipment.hourlyRate
    }

    fun sendRequest(equipment: Equipment) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val booking = Booking(
                equipmentId = equipment.id,
                equipmentName = equipment.name,
                renterId = repository.getCurrentUserId(),
                ownerId = equipment.ownerId,
                startDate = today,
                endDate = today,
                totalHours = if (!useDaily) hours else 0,
                totalDays = if (useDaily) days else 0,
                totalPrice = getTotalPrice(equipment),
                status = Constants.STATUS_PENDING
            )
            val id = repository.createBooking(booking)
            if (id != null) {
                bookingId = id
                isSuccess = true
            } else {
                errorMessage = "Failed to send request. Try again."
            }
            isLoading = false
        }
    }

    fun loadMyBookings() {
        viewModelScope.launch {
            isLoading = true
            myBookings = repository.getMyBookings()
            isLoading = false
        }
    }

    fun reset() {
        isSuccess = false
        bookingId = ""
        errorMessage = ""
    }
}
