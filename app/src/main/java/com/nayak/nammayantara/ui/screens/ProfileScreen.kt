package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.model.User
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    var user         by mutableStateOf<User?>(null)
    var isLoading    by mutableStateOf(false)
    var photoBase64  by mutableStateOf<String?>(null)

    init { loadProfile() }

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val doc = db.collection(Constants.USERS).document(uid).get().await()
                user        = doc.toObject(User::class.java)
                photoBase64 = doc.getString("photoBase64")?.takeIf { it.isNotEmpty() }
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfile  by remember { mutableStateOf(false) }

    if (showEditProfile) {
        EditProfileScreen(onBack = {
            showEditProfile = false
            viewModel.loadProfile()
        })
        return
    }

    // ── Logout dialog ────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor   = YantraSurface,
            title = {
                Text("Log out?", color = YantraWhite, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("You'll need to verify your phone number again to log back in.",
                    color = YantraGrey60,
                    style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Log out", color = YantraRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = YantraGrey60)
                }
            }
        )
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile",
                        style    = MaterialTheme.typography.titleLarge,
                        color    = YantraWhite,
                        fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { showEditProfile = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = YantraAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YantraAmber, strokeWidth = 2.5.dp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                // ── Avatar ───────────────────────────────────────────────────
                Box(contentAlignment = Alignment.Center) {
                    // Gradient ring
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(YantraAmber, YantraTeal))
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(98.dp)
                            .clip(CircleShape)
                            .background(YantraAsphalt),
                        contentAlignment = Alignment.Center,
                    ) {
                        val bitmap = remember(viewModel.photoBase64) {
                            viewModel.photoBase64?.let { decodeBase64ToBitmap(it) }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                if (viewModel.user?.role == "owner") "🚜" else "🌾",
                                fontSize = 40.sp,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                val displayName = viewModel.user?.name?.takeIf { it.isNotEmpty() } ?: "No name set"
                Text(
                    displayName,
                    style      = MaterialTheme.typography.headlineSmall,
                    color      = YantraWhite,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(Modifier.height(6.dp))

                // Role badge
                val isOwner = viewModel.user?.role == "owner"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(YantraAmber.copy(alpha = 0.12f))
                        .border(1.dp, YantraAmber.copy(alpha = 0.3f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (isOwner) "Equipment Owner" else "Equipment Renter",
                        color      = YantraAmber,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ── Info card ────────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    color    = YantraSurface,
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        ProfileRow("Phone",      viewModel.user?.phone ?: "—",                              icon = "📱")
                        RowDivider()
                        ProfileRow("Birth Date", viewModel.user?.birthDate?.takeIf { it.isNotEmpty() } ?: "Not set", icon = "🎂")
                        RowDivider()
                        ProfileRow("Gender",     viewModel.user?.gender?.takeIf { it.isNotEmpty() } ?: "Not set",    icon = "👤")
                        RowDivider()
                        ProfileRow("Address",    viewModel.user?.address?.takeIf { it.isNotEmpty() } ?: "Not set",   icon = "📍")
                        RowDivider()
                        ProfileRow(
                            label = "User ID",
                            value = FirebaseAuth.getInstance().currentUser?.uid?.take(12)?.uppercase() ?: "—",
                            icon  = "🆔",
                            mono  = true,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Edit button ──────────────────────────────────────────────
                OutlinedButton(
                    onClick  = { showEditProfile = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(1.dp, YantraAmber),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = YantraAmber),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(8.dp))

                // ── About card ───────────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    color    = YantraSurface,
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text("About", style = MaterialTheme.typography.labelMedium, color = YantraGrey60)
                        Spacer(Modifier.height(4.dp))
                        Text("Namma Yantra Share", style = MaterialTheme.typography.bodyMedium, color = YantraWhite, fontWeight = FontWeight.Medium)
                        Text("Version 404 - Team TechExpert99", style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                    }
                }

                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(24.dp))

                // ── Logout ───────────────────────────────────────────────────
                OutlinedButton(
                    onClick  = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(1.dp, YantraRed.copy(alpha = 0.6f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = YantraRed),
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(vertical = 10.dp),
        color     = YantraGrey30,
        thickness = 0.5.dp,
    )
}

@Composable
fun ProfileRow(label: String, value: String, icon: String = "", mono: Boolean = false) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment   = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            if (icon.isNotEmpty()) Text(icon, fontSize = 15.sp)
            Text(label, style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
        }
        Text(
            value,
            style      = if (mono) MaterialTheme.typography.labelSmall.copy(
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ) else MaterialTheme.typography.bodySmall,
            color      = YantraWhite,
            fontWeight = FontWeight.Medium,
            modifier   = Modifier.widthIn(max = 200.dp),
        )
    }
}