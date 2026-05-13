package com.nayak.nammayantara

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayak.nammayantara.service.MyFirebaseMessagingService
import com.nayak.nammayantara.ui.screens.CustomerApp
import com.nayak.nammayantara.ui.screens.LoginScreen
import com.nayak.nammayantara.ui.screens.OwnerApp
import com.nayak.nammayantara.ui.theme.NammaYantraTheme
import com.nayak.nammayantara.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            MyFirebaseMessagingService.getToken { token ->
                android.util.Log.e("FCMToken", "Token: $token")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()

        setContent {
            NammaYantraTheme {
                val authViewModel: AuthViewModel = viewModel()

                when {
                    // Still checking role — show spinner
                    authViewModel.userRole == null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        }
                    }
                    // Not logged in
                    !authViewModel.isLoggedIn -> {
                        LoginScreen(onLoginSuccess = { /* AuthViewModel handles state */ })
                    }
                    // Owner role — show owner app
                    authViewModel.userRole == "owner" -> {
                        OwnerApp(
                            onLogout = {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                authViewModel.isLoggedIn = false
                                authViewModel.userRole = ""
                            }
                        )
                    }
                    // Renter/customer role — show customer app
                    else -> {
                        CustomerApp(
                            onLogout = {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                authViewModel.isLoggedIn = false
                                authViewModel.userRole = ""
                            }
                        )
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    MyFirebaseMessagingService.getToken { token ->
                        android.util.Log.e("FCMToken", "Token: $token")
                    }
                }
                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            MyFirebaseMessagingService.getToken { token ->
                android.util.Log.e("FCMToken", "Token: $token")
            }
        }
    }
}