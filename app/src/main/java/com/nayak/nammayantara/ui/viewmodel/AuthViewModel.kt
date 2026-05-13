package com.nayak.nammayantara.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.repository.AuthRepository
import com.nayak.nammayantara.utils.Constants

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var phoneNumber by mutableStateOf("")
    var otp by mutableStateOf("")
    var verificationId by mutableStateOf("")
    var isOtpSent by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    // null = not checked yet, "" = not logged in, "owner"/"renter" = logged in with role
    var userRole by mutableStateOf<String?>(null)
    var isLoggedIn by mutableStateOf(false)

    init {
        // If already logged in, fetch their role from Firestore
        val uid = auth.currentUser?.uid
        if (uid != null) {
            fetchRole(uid)
        } else {
            userRole = ""
        }
    }

    private fun fetchRole(uid: String) {
        db.collection(Constants.USERS).document(uid).get()
            .addOnSuccessListener { doc ->
                userRole = doc.getString("role") ?: "renter"
                isLoggedIn = true
            }
            .addOnFailureListener {
                userRole = "renter"
                isLoggedIn = true
            }
    }

    fun sendOtp(activity: Activity) {
        if (phoneNumber.length != 10) {
            errorMessage = "Enter a valid 10-digit number"
            return
        }
        isLoading = true
        errorMessage = ""
        repository.sendOtp(
            phoneNumber = "+91$phoneNumber",
            activity = activity,
            onCodeSent = {
                verificationId = it
                isOtpSent = true
                isLoading = false
            },
            onFailed = {
                errorMessage = it
                isLoading = false
            }
        )
    }

    fun verifyOtp(role: String) {
        isLoading = true
        repository.verifyOtp(
            verificationId = verificationId,
            otp = otp,
            onSuccess = { uid ->
                repository.saveUserRole(uid, "+91$phoneNumber", role) {
                    userRole = role
                    isLoggedIn = true
                    isLoading = false
                }
            },
            onFailed = {
                errorMessage = it
                isLoading = false
            }
        )
    }
}