package com.nayak.nammayantara.data.repository

import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.model.User
import com.nayak.nammayantara.utils.Constants
import java.util.concurrent.TimeUnit

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun sendOtp(
        phoneNumber: String,
        activity: android.app.Activity,
        onCodeSent: (String) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                override fun onVerificationFailed(e: FirebaseException) {
                    onFailed(e.message ?: "OTP Failed")
                }
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId)
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        verificationId: String,
        otp: String,
        onSuccess: (String) -> Unit,
        onFailed: (String) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid ?: ""
                onSuccess(uid)
            }
            .addOnFailureListener {
                onFailed(it.message ?: "Verification failed")
            }
    }

    fun saveUserRole(uid: String, phone: String, role: String, onDone: () -> Unit) {
        val user = User(uid = uid, phone = phone, role = role)
        db.collection(Constants.USERS).document(uid)
            .set(user)
            .addOnSuccessListener { onDone() }
    }

    fun isLoggedIn() = auth.currentUser != null
}