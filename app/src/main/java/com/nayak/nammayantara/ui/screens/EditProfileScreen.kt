package com.nayak.nammayantara.ui.screens

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.model.User
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

// ── Bitmap helpers ────────────────────────────────────────────────────────────

fun decodeBase64ToBitmap(base64: String): Bitmap? = try {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} catch (_: Exception) { null }

fun bitmapToBase64(bitmap: Bitmap): String {
    val out    = ByteArrayOutputStream()
    val scaled = if (bitmap.width > 400 || bitmap.height > 400) {
        val scale = 400f / maxOf(bitmap.width, bitmap.height)
        Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    } else bitmap
    scaled.compress(Bitmap.CompressFormat.JPEG, 70, out)
    return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class EditProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // Profile fields
    var name         by mutableStateOf("")
    var birthDate    by mutableStateOf("")
    var gender       by mutableStateOf("")
    var address      by mutableStateOf("")
    var photoBase64  by mutableStateOf<String?>(null)
    var isLoading    by mutableStateOf(false)
    var isSaving     by mutableStateOf(false)
    var saveSuccess  by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    // Phone change
    var newPhone        by mutableStateOf("")
    var phoneOtp        by mutableStateOf("")
    var isPhoneOtpSent  by mutableStateOf(false)
    var isPhoneUpdating by mutableStateOf(false)
    var phoneError      by mutableStateOf("")
    var phoneSuccess    by mutableStateOf(false)

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init { loadProfile() }

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val doc  = db.collection(Constants.USERS).document(uid).get().await()
                val user = doc.toObject(User::class.java)
                name        = user?.name      ?: ""
                birthDate   = user?.birthDate ?: ""
                gender      = user?.gender    ?: ""
                address     = user?.address   ?: ""
                photoBase64 = doc.getString("photoBase64")?.takeIf { it.isNotEmpty() }
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    // ── KEY FIX: call this every time EditProfileScreen opens ─────────────────
    fun resetSaveState() {
        saveSuccess  = false
        errorMessage = ""
    }

    fun saveProfile() {
        val uid = auth.currentUser?.uid ?: return
        if (name.isBlank()) { errorMessage = "Name cannot be empty"; return }
        viewModelScope.launch {
            isSaving = true; errorMessage = ""
            try {
                val updates = mutableMapOf<String, Any>(
                    "name"      to name.trim(),
                    "birthDate" to birthDate.trim(),
                    "gender"    to gender,
                    "address"   to address.trim(),
                )
                photoBase64?.let { updates["photoBase64"] = it }
                db.collection(Constants.USERS).document(uid).update(updates).await()
                saveSuccess = true
            } catch (e: Exception) {
                // Fallback: set with merge if doc doesn't exist yet
                try {
                    val updates2 = mutableMapOf<String, Any>(
                        "uid"       to uid,
                        "name"      to name.trim(),
                        "birthDate" to birthDate.trim(),
                        "gender"    to gender,
                        "address"   to address.trim(),
                    )
                    photoBase64?.let { updates2["photoBase64"] = it }
                    db.collection(Constants.USERS).document(uid)
                        .set(updates2, com.google.firebase.firestore.SetOptions.merge()).await()
                    saveSuccess = true
                } catch (e2: Exception) {
                    errorMessage = e2.message ?: "Failed to save"
                }
            }
            isSaving = false
        }
    }

    fun setPhoto(base64: String) { photoBase64 = base64 }

    // ── Phone change ──────────────────────────────────────────────────────────

    fun sendPhoneOtp(activity: Activity) {
        if (newPhone.length < 10) { phoneError = "Enter a valid 10-digit number"; return }
        phoneError = ""; isPhoneUpdating = true

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                updatePhoneWithCredential(credential)
            }
            override fun onVerificationFailed(e: FirebaseException) {
                phoneError = e.message ?: "Verification failed"; isPhoneUpdating = false
            }
            override fun onCodeSent(vid: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = vid; resendToken = token
                isPhoneOtpSent = true; isPhoneUpdating = false
            }
        }

        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber("+91${newPhone.trim()}")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .apply { resendToken?.let { setForceResendingToken(it) } }
                .build()
        )
    }

    fun verifyPhoneOtp() {
        val vid = verificationId ?: return
        if (phoneOtp.length < 6) { phoneError = "Enter the 6-digit OTP"; return }
        phoneError = ""; isPhoneUpdating = true
        updatePhoneWithCredential(PhoneAuthProvider.getCredential(vid, phoneOtp))
    }

    private fun updatePhoneWithCredential(credential: PhoneAuthCredential) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                user.updatePhoneNumber(credential).await()
                db.collection(Constants.USERS).document(user.uid)
                    .update("phone", "+91${newPhone.trim()}").await()
                phoneSuccess = true; isPhoneOtpSent = false
                phoneOtp = ""; newPhone = ""
            } catch (e: Exception) { phoneError = e.message ?: "Failed to update phone" }
            isPhoneUpdating = false
        }
    }

    fun resetPhoneFlow() {
        newPhone = ""; phoneOtp = ""; isPhoneOtpSent = false
        phoneError = ""; phoneSuccess = false; isPhoneUpdating = false
        verificationId = null
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val viewModel: EditProfileViewModel = viewModel()
    val context  = LocalContext.current
    val activity = context as Activity
    var showPhoneSheet by remember { mutableStateOf(false) }

    // ── KEY FIX: reset saveSuccess every time this screen is opened ───────────
    // Without this, the second time you open Edit Profile the LaunchedEffect
    // below sees saveSuccess=true immediately and calls onBack() right away.
    LaunchedEffect(Unit) {
        viewModel.resetSaveState()
        viewModel.loadProfile()   // also reload fresh data each time screen opens
    }

    // Only navigate back AFTER user actually taps Save and it succeeds
    LaunchedEffect(viewModel.saveSuccess) {
        if (viewModel.saveSuccess) onBack()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) viewModel.setPhoto(bitmapToBase64(bitmap))
            } catch (_: Exception) {}
        }
    }

    if (showPhoneSheet) {
        ChangePhoneSheet(
            viewModel = viewModel,
            activity  = activity,
            onDismiss = { viewModel.resetPhoneFlow(); showPhoneSheet = false },
        )
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        color = YantraWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = YantraAmber)
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
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {

            // ── Avatar ───────────────────────────────────────────────────────
            Text("Profile Photo", style = MaterialTheme.typography.labelMedium, color = YantraGrey60)
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(YantraAmber, YantraTeal)))
                )
                Box(
                    modifier = Modifier
                        .size(98.dp)
                        .clip(CircleShape)
                        .background(YantraAsphalt)
                        .clickable { imagePickerLauncher.launch("image/*") },
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
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📷", fontSize = 30.sp)
                            Text("Tap to upload", style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(YantraAmber)
                        .border(2.dp, YantraAsphalt, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CameraAlt, null, tint = YantraAsphalt, modifier = Modifier.size(16.dp))
                }
            }
            Text(
                "Tap photo to change · Gallery only",
                style = MaterialTheme.typography.labelSmall, color = YantraGrey60
            )

            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)

            // ── Change phone row ─────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(14.dp),
                color    = YantraSurface,
                border   = BorderStroke(1.dp, YantraGrey30),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPhoneSheet = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(YantraTeal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Phone, null, tint = YantraTeal, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                "Phone Number",
                                style = MaterialTheme.typography.bodyMedium,
                                color = YantraWhite, fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Tap to update via OTP",
                                style = MaterialTheme.typography.labelSmall, color = YantraGrey60
                            )
                        }
                    }
                    Icon(Icons.Rounded.Edit, null, tint = YantraAmber, modifier = Modifier.size(18.dp))
                }
            }

            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)

            // ── Fields ───────────────────────────────────────────────────────
            YantraTextField(
                viewModel.name, { viewModel.name = it }, "Full Name", "e.g. Nithin Nayak"
            )
            YantraTextField(
                viewModel.birthDate, { viewModel.birthDate = it }, "Birth Date", "yyyy-MM-dd  e.g. 2000-06-15"
            )

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gender", style = MaterialTheme.typography.labelMedium, color = YantraGrey60)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Male" to "👨", "Female" to "👩", "Other" to "🧑").forEach { (option, icon) ->
                        val selected = viewModel.gender == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(if (selected) YantraAmber.copy(alpha = 0.14f) else YantraSurface)
                                .border(
                                    if (selected) 1.5.dp else 1.dp,
                                    if (selected) YantraAmber else YantraGrey30,
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable { viewModel.gender = option }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "$icon $option",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) YantraAmber else YantraGrey60,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            YantraTextField(
                viewModel.address, { viewModel.address = it },
                "Address", "Village, Taluk, District, State",
                minLines = 3, maxLines = 4
            )

            if (viewModel.errorMessage.isNotEmpty()) {
                Text(viewModel.errorMessage, color = YantraRed, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick  = { viewModel.saveProfile() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled  = !viewModel.isSaving,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor        = YantraAmber,
                    contentColor          = YantraAsphalt,
                    disabledContainerColor = YantraGrey30,
                    disabledContentColor  = YantraGrey60,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(
                        color = YantraAsphalt, modifier = Modifier.size(22.dp), strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Profile", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Change Phone Bottom Sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePhoneSheet(
    viewModel: EditProfileViewModel,
    activity:  Activity,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = YantraSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(YantraGrey30)
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Change Phone Number",
                style = MaterialTheme.typography.titleMedium,
                color = YantraWhite, fontWeight = FontWeight.Bold
            )

            AnimatedVisibility(visible = viewModel.phoneSuccess, enter = fadeIn() + slideInVertically()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(14.dp),
                    color    = YantraGreen.copy(alpha = 0.10f),
                    border   = BorderStroke(1.dp, YantraGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✅", fontSize = 22.sp)
                        Column {
                            Text("Phone updated!", style = MaterialTheme.typography.titleMedium,
                                color = YantraGreen, fontWeight = FontWeight.SemiBold)
                            Text("Your new number is now active.",
                                style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                        }
                    }
                }
            }

            if (!viewModel.phoneSuccess) {
                if (!viewModel.isPhoneOtpSent) {
                    Text("Enter the new number you want to link.",
                        style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                    OutlinedTextField(
                        value = viewModel.newPhone,
                        onValueChange = { viewModel.newPhone = it.take(10) },
                        prefix = { Text("+91  ", color = YantraTeal, fontWeight = FontWeight.SemiBold) },
                        placeholder = { Text("98765 43210", color = YantraGrey30) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = phoneFieldColors(),
                    )
                    Button(
                        onClick  = { viewModel.sendPhoneOtp(activity) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled  = viewModel.newPhone.length == 10 && !viewModel.isPhoneUpdating,
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = YantraAmber, contentColor = YantraAsphalt,
                            disabledContainerColor = YantraGrey30, disabledContentColor = YantraGrey60
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        if (viewModel.isPhoneUpdating)
                            CircularProgressIndicator(color = YantraAsphalt, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Send OTP", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("OTP sent to +91 ${viewModel.newPhone}",
                        style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                    OutlinedTextField(
                        value = viewModel.phoneOtp,
                        onValueChange = { viewModel.phoneOtp = it.take(6) },
                        placeholder = { Text("• • • • • •", color = YantraGrey30, letterSpacing = 6.sp) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = phoneFieldColors(),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick  = { viewModel.isPhoneOtpSent = false; viewModel.phoneOtp = "" },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape    = RoundedCornerShape(12.dp),
                            border   = BorderStroke(1.dp, YantraGrey30),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = YantraGrey60),
                        ) { Text("← Change", style = MaterialTheme.typography.labelLarge) }

                        Button(
                            onClick  = { viewModel.verifyPhoneOtp() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            enabled  = viewModel.phoneOtp.length == 6 && !viewModel.isPhoneUpdating,
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = YantraAmber, contentColor = YantraAsphalt,
                                disabledContainerColor = YantraGrey30, disabledContentColor = YantraGrey60
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                        ) {
                            if (viewModel.isPhoneUpdating)
                                CircularProgressIndicator(color = YantraAsphalt, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Verify", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (viewModel.phoneError.isNotEmpty()) {
                    Text(viewModel.phoneError, color = YantraRed, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (viewModel.phoneSuccess) {
                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = YantraAmber, contentColor = YantraAsphalt),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) { Text("Done", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun phoneFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = YantraAmber,       unfocusedBorderColor   = YantraGrey30,
    focusedTextColor     = YantraWhite,        unfocusedTextColor     = YantraWhite,
    cursorColor          = YantraAmber,
    focusedContainerColor = YantraSurfaceHigh, unfocusedContainerColor = YantraSurfaceHigh,
)

@Composable
private fun YantraTextField(
    value: String, onValueChange: (String) -> Unit,
    label: String, placeholder: String,
    minLines: Int = 1, maxLines: Int = 1,
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label       = { Text(label,       color = YantraGrey60) },
        placeholder = { Text(placeholder, color = YantraGrey30) },
        modifier    = Modifier.fillMaxWidth(),
        shape       = RoundedCornerShape(14.dp),
        minLines    = minLines, maxLines = maxLines,
        colors      = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = YantraAmber,        unfocusedBorderColor    = YantraGrey30,
            focusedTextColor      = YantraWhite,        unfocusedTextColor      = YantraWhite,
            cursorColor           = YantraAmber,        focusedLabelColor       = YantraAmber,
            focusedContainerColor = YantraSurfaceHigh,  unfocusedContainerColor = YantraSurfaceHigh,
        ),
    )
}