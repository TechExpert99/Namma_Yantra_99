package com.nayak.nammayantara.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import com.nayak.nammayantara.data.model.Equipment
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── ViewModel ─────────────────────────────────────────────────────────────────

class OwnerFleetViewModel : ViewModel() {
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var myEquipment   = mutableStateListOf<Equipment>()
    var isLoading     by mutableStateOf(false)
    var isSaving      by mutableStateOf(false)
    var saveSuccess   by mutableStateOf(false)
    var errorMessage  by mutableStateOf("")
    var deleteSuccess by mutableStateOf(false)

    init { loadMyEquipment() }

    fun loadMyEquipment() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val snap = db.collection(Constants.EQUIPMENT).whereEqualTo("ownerId", uid).get().await()
                myEquipment.clear()
                myEquipment.addAll(snap.documents.mapNotNull { it.toObject(Equipment::class.java)?.copy(id = it.id) })
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    fun addEquipment(name: String, type: String, hourlyRate: Double, dailyRate: Double,
                     conditionRating: Float, fuelType: String, availableDates: List<String>,
                     latitude: Double, longitude: Double, locationName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isSaving = true; errorMessage = ""
            try {
                db.collection(Constants.EQUIPMENT).add(Equipment(ownerId = uid, name = name, type = type,
                    hourlyRate = hourlyRate, dailyRate = dailyRate, conditionRating = conditionRating,
                    fuelType = fuelType, availableDates = availableDates, latitude = latitude,
                    longitude = longitude, locationName = locationName, status = "Available")).await()
                saveSuccess = true; loadMyEquipment()
            } catch (e: Exception) { errorMessage = e.message ?: "Failed to add" }
            isSaving = false
        }
    }

    fun updateEquipment(equipmentId: String, name: String, type: String, hourlyRate: Double,
                        dailyRate: Double, conditionRating: Float, fuelType: String,
                        availableDates: List<String>, latitude: Double, longitude: Double,
                        locationName: String, status: String) {
        viewModelScope.launch {
            isSaving = true; errorMessage = ""
            try {
                db.collection(Constants.EQUIPMENT).document(equipmentId).update(mapOf(
                    "name" to name, "type" to type, "hourlyRate" to hourlyRate,
                    "dailyRate" to dailyRate, "conditionRating" to conditionRating,
                    "fuelType" to fuelType, "availableDates" to availableDates,
                    "latitude" to latitude, "longitude" to longitude,
                    "locationName" to locationName, "status" to status)).await()
                saveSuccess = true; loadMyEquipment()
            } catch (e: Exception) { errorMessage = e.message ?: "Failed to update" }
            isSaving = false
        }
    }

    fun deleteEquipment(equipmentId: String) {
        viewModelScope.launch {
            try {
                db.collection(Constants.EQUIPMENT).document(equipmentId).delete().await()
                myEquipment.removeAll { it.id == equipmentId }
                deleteSuccess = true
            } catch (e: Exception) { errorMessage = e.message ?: "Failed to delete" }
        }
    }

    fun resetState() { saveSuccess = false; deleteSuccess = false; errorMessage = "" }
}

// ── Geocoder helper ───────────────────────────────────────────────────────────

suspend fun reverseGeocode(context: android.content.Context, lat: Double, lng: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                listOfNotNull(addr.subLocality?.takeIf { it.isNotBlank() },
                    addr.locality?.takeIf { it.isNotBlank() },
                    addr.adminArea?.takeIf { it.isNotBlank() })
                    .take(2).joinToString(", ").ifBlank { "Selected location" }
            } else "Selected location"
        } catch (_: Exception) { "Selected location" }
    }
}

// ── Fleet screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerFleetScreen() {
    val viewModel: OwnerFleetViewModel = viewModel()
    var showAddForm    by remember { mutableStateOf(false) }
    var editEquipment  by remember { mutableStateOf<Equipment?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.deleteSuccess) {
        if (viewModel.deleteSuccess) {
            snackbarHostState.showSnackbar("Vehicle removed")
            viewModel.resetState()
        }
    }

    if (showAddForm || editEquipment != null) {
        AddEquipmentForm(
            viewModel          = viewModel,
            existingEquipment  = editEquipment,
            onBack             = { showAddForm = false; editEquipment = null; viewModel.resetState() }
        )
        return
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("My Fleet", style = MaterialTheme.typography.titleLarge,
                            color = YantraWhite, fontWeight = FontWeight.Bold)
                        Text("${viewModel.myEquipment.size} vehicles listed",
                            style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddForm = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add", tint = YantraAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddForm = true },
                containerColor = YantraAmber, contentColor = YantraAsphalt) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Vehicle")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when {
            viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YantraAmber, strokeWidth = 2.5.dp)
            }
            viewModel.myEquipment.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🚜", fontSize = 64.sp)
                    Text("No vehicles listed yet", style = MaterialTheme.typography.titleMedium,
                        color = YantraWhite, fontWeight = FontWeight.SemiBold)
                    Text("Tap + to add your first vehicle",
                        style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { showAddForm = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YantraAmber, contentColor = YantraAsphalt),
                        elevation = ButtonDefaults.buttonElevation(0.dp)) {
                        Text("Add Vehicle", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(viewModel.myEquipment, key = { it.id }) { equipment ->
                    FleetCard(equipment = equipment,
                        onEdit   = { editEquipment = equipment },
                        onDelete = { viewModel.deleteEquipment(equipment.id) })
                }
            }
        }
    }
}

// ── Fleet card ────────────────────────────────────────────────────────────────

@Composable
fun FleetCard(equipment: Equipment, onEdit: () -> Unit, onDelete: () -> Unit) {
    val (statusColor, statusBg) = when (equipment.status) {
        "Available" -> YantraGreen to YantraGreen.copy(alpha = 0.12f)
        "Booked"    -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.12f)
        else        -> YantraRed to YantraRed.copy(alpha = 0.12f)
    }
    val typeIcon = when (equipment.type) {
        "Tractor" -> "🚜"; "Harvester" -> "🌾"; "Sprayer" -> "💧"; else -> "🚜"
    }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = YantraSurface,
            title = { Text("Remove Vehicle", color = YantraWhite, fontWeight = FontWeight.Bold) },
            text  = { Text("Remove \"${equipment.name}\" from your fleet?",
                color = YantraGrey60, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Remove", color = YantraRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = YantraGrey60)
                }
            }
        )
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = YantraSurface, border = BorderStroke(1.dp, YantraGrey30)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                        .background(YantraSurfaceHigh), contentAlignment = Alignment.Center) {
                        Text(typeIcon, fontSize = 22.sp)
                    }
                    Column {
                        Text(equipment.name, style = MaterialTheme.typography.titleMedium,
                            color = YantraWhite, fontWeight = FontWeight.SemiBold)
                        Text(equipment.type, style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                    }
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(equipment.status, color = statusColor,
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                FleetStatCell("Hourly", "₹${equipment.hourlyRate.toInt()}/hr")
                FleetStatCell("Daily",  "₹${equipment.dailyRate.toInt()}/day")
                FleetStatCell("Rating", "⭐ ${equipment.conditionRating}")
                FleetStatCell("Fuel",   equipment.fuelType)
            }

            if (equipment.locationName.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("📍 ${equipment.locationName}",
                    style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
            } else if (equipment.latitude != 0.0) {
                Spacer(Modifier.height(8.dp))
                Text("📍 ${"%.4f".format(equipment.latitude)}, ${"%.4f".format(equipment.longitude)}",
                    style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
            }

            if (equipment.availableDates.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("📅 ${equipment.availableDates.size} available date(s)",
                    style = MaterialTheme.typography.bodySmall, color = YantraTeal)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, YantraAmber),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YantraAmber),
                    contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, YantraRed.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YantraRed),
                    contentPadding = PaddingValues(horizontal = 8.dp)) {
                    Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Remove", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun FleetStatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = YantraAmber, fontWeight = FontWeight.Bold)
    }
}

// ── Add / Edit form ───────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEquipmentForm(
    viewModel: OwnerFleetViewModel,
    existingEquipment: Equipment? = null,
    onBack: () -> Unit,
) {
    val isEditMode = existingEquipment != null
    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()

    var name             by remember { mutableStateOf(existingEquipment?.name ?: "") }
    var type             by remember { mutableStateOf(existingEquipment?.type ?: "Tractor") }
    var hourlyRate       by remember { mutableStateOf(existingEquipment?.hourlyRate?.toInt()?.toString() ?: "") }
    var dailyRate        by remember { mutableStateOf(existingEquipment?.dailyRate?.toInt()?.toString() ?: "") }
    var conditionRating  by remember { mutableStateOf(existingEquipment?.conditionRating ?: 4f) }
    var fuelType         by remember { mutableStateOf(existingEquipment?.fuelType ?: "Diesel") }
    var status           by remember { mutableStateOf(existingEquipment?.status ?: "Available") }
    var selectedDates    by remember { mutableStateOf(existingEquipment?.availableDates?.toSet() ?: setOf()) }
    var calendarMonth    by remember { mutableStateOf(YearMonth.now()) }
    var showLocationPicker by remember { mutableStateOf(false) }

    val initialLatLng = if ((existingEquipment?.latitude ?: 0.0) != 0.0)
        LatLng(existingEquipment!!.latitude, existingEquipment.longitude) else null

    var pickedLocation by remember { mutableStateOf<LatLng?>(initialLatLng) }
    var locationName   by remember { mutableStateOf(existingEquipment?.locationName ?: "") }
    var locationLabel  by remember {
        mutableStateOf(when {
            existingEquipment?.locationName?.isNotBlank() == true -> existingEquipment.locationName
            initialLatLng != null -> "${"%.5f".format(initialLatLng.latitude)}, ${"%.5f".format(initialLatLng.longitude)}"
            else -> "Not set — tap to pick on map"
        })
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var locationPermissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        locationPermissionGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(viewModel.saveSuccess) { if (viewModel.saveSuccess) onBack() }

    if (showLocationPicker) {
        LocationPickerScreen(
            initialLocation  = pickedLocation ?: LatLng(12.9716, 77.5946),
            onLocationPicked = { latLng ->
                pickedLocation = latLng; showLocationPicker = false
                scope.launch {
                    val resolved = reverseGeocode(context, latLng.latitude, latLng.longitude)
                    locationName = resolved; locationLabel = resolved
                }
            },
            onBack = { showLocationPicker = false }
        )
        return
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Vehicle" else "Add Vehicle",
                    style = MaterialTheme.typography.titleLarge, color = YantraWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = YantraAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)
            .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // Name
            FleetTextField(value = name, onValueChange = { name = it },
                label = "Equipment Name", placeholder = "e.g. Mahindra 575 DI")

            // Type
            FormLabel("Equipment Type")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Tractor" to "🚜", "Harvester" to "🌾", "Sprayer" to "💧").forEach { (t, icon) ->
                    YantraTypeChip(label = "$icon $t", selected = type == t, onClick = { type = t })
                }
            }

            // Rates
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FleetTextField(value = hourlyRate, onValueChange = { hourlyRate = it },
                    label = "Hourly (₹)", placeholder = "e.g. 300",
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
                FleetTextField(value = dailyRate, onValueChange = { dailyRate = it },
                    label = "Daily (₹)", placeholder = "e.g. 2000",
                    keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
            }

            // Condition
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                FormLabel("Condition Rating")
                Text("${conditionRating.toInt()}/5", style = MaterialTheme.typography.titleMedium,
                    color = YantraAmber, fontWeight = FontWeight.Bold)
            }
            Slider(value = conditionRating, onValueChange = { conditionRating = it },
                valueRange = 1f..5f, steps = 3,
                colors = SliderDefaults.colors(thumbColor = YantraAmber, activeTrackColor = YantraAmber, inactiveTrackColor = YantraGrey30))

            // Fuel
            FormLabel("Fuel Type")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Diesel", "Petrol", "Electric").forEach { fuel ->
                    YantraTypeChip(label = fuel, selected = fuelType == fuel, onClick = { fuelType = fuel })
                }
            }

            // Status (edit only)
            if (isEditMode) {
                HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
                FormLabel("Vehicle Status")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Available" to YantraGreen, "Booked" to Color(0xFFFF9800), "In-Use" to YantraRed).forEach { (s, color) ->
                        val selected = status == s
                        Box(modifier = Modifier.clip(RoundedCornerShape(50.dp))
                            .background(if (selected) color.copy(alpha = 0.18f) else YantraSurface)
                            .border(if (selected) 1.5.dp else 1.dp, if (selected) color else YantraGrey30, RoundedCornerShape(50.dp))
                            .clickable { status = s }.padding(horizontal = 14.dp, vertical = 8.dp)) {
                            Text(s, style = MaterialTheme.typography.labelMedium,
                                color = if (selected) color else YantraGrey60,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
            }

            // Calendar
            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
            FormLabel("Availability Calendar")
            Text("Tap dates when this vehicle is available",
                style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
            if (selectedDates.isNotEmpty()) {
                Text("${selectedDates.size} date(s) selected",
                    style = MaterialTheme.typography.labelMedium, color = YantraTeal, fontWeight = FontWeight.Medium)
            }
            AvailabilityCalendar(currentMonth = calendarMonth, selectedDates = selectedDates,
                onDateToggle = { date -> selectedDates = if (selectedDates.contains(date)) selectedDates - date else selectedDates + date },
                onMonthChange = { calendarMonth = it })

            // Location
            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
            FormLabel("Vehicle Location")
            Text("Set where this vehicle is parked",
                style = MaterialTheme.typography.bodySmall, color = YantraGrey60)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (locationPermissionGranted) {
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                            .addOnSuccessListener { loc -> loc?.let { l ->
                                pickedLocation = LatLng(l.latitude, l.longitude)
                                scope.launch {
                                    val r = reverseGeocode(context, l.latitude, l.longitude)
                                    locationName = r; locationLabel = r
                                }
                            }}
                    } else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = YantraTeal.copy(alpha = 0.15f), contentColor = YantraTeal),
                    elevation = ButtonDefaults.buttonElevation(0.dp)) {
                    Icon(Icons.Rounded.MyLocation, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("My Location", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(onClick = { showLocationPicker = true }, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, YantraAmber),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YantraAmber)) {
                    Text("Pick on Map", style = MaterialTheme.typography.labelMedium)
                }
            }

            // Location result
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                color = if (pickedLocation != null) YantraGreen.copy(alpha = 0.08f) else YantraSurface,
                border = BorderStroke(1.dp, if (pickedLocation != null) YantraGreen.copy(alpha = 0.3f) else YantraGrey30)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (pickedLocation != null) "✅" else "⚠️", fontSize = 16.sp)
                    Column {
                        Text(locationLabel, style = MaterialTheme.typography.bodySmall,
                            color = if (pickedLocation != null) YantraGreen else YantraGrey60,
                            fontWeight = FontWeight.Medium)
                        if (pickedLocation != null && locationName.isNotBlank()) {
                            Text("${"%.5f".format(pickedLocation!!.latitude)}, ${"%.5f".format(pickedLocation!!.longitude)}",
                                style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                        }
                    }
                }
            }

            if (viewModel.errorMessage.isNotEmpty()) {
                Text(viewModel.errorMessage, color = YantraRed, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    val lat     = pickedLocation?.latitude  ?: existingEquipment?.latitude  ?: 12.9716
                    val lng     = pickedLocation?.longitude ?: existingEquipment?.longitude ?: 77.5946
                    val locName = locationName.ifBlank { existingEquipment?.locationName ?: "" }
                    if (isEditMode) viewModel.updateEquipment(existingEquipment!!.id, name.trim(), type,
                        hourlyRate.toDoubleOrNull() ?: 0.0, dailyRate.toDoubleOrNull() ?: 0.0,
                        conditionRating, fuelType, selectedDates.sorted(), lat, lng, locName, status)
                    else viewModel.addEquipment(name.trim(), type,
                        hourlyRate.toDoubleOrNull() ?: 0.0, dailyRate.toDoubleOrNull() ?: 0.0,
                        conditionRating, fuelType, selectedDates.sorted(), lat, lng, locName)
                },
                modifier  = Modifier.fillMaxWidth().height(54.dp),
                enabled   = name.isNotBlank() && hourlyRate.isNotBlank() && !viewModel.isSaving,
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = YantraAmber, contentColor = YantraAsphalt,
                    disabledContainerColor = YantraGrey30, disabledContentColor = YantraGrey60),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                if (viewModel.isSaving) CircularProgressIndicator(color = YantraAsphalt, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text(if (isEditMode) "Save Changes" else "Add to My Fleet",
                    style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Location picker screen ────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(initialLocation: LatLng, onLocationPicked: (LatLng) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var markerPosition by remember { mutableStateOf(initialLocation) }
    var resolvedName   by remember { mutableStateOf("") }
    var locationPermissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val permissionLauncher  = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
        locationPermissionGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 14f)
    }
    LaunchedEffect(markerPosition) { resolvedName = reverseGeocode(context, markerPosition.latitude, markerPosition.longitude) }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Pick Vehicle Location", style = MaterialTheme.typography.titleLarge,
                            color = YantraWhite, fontWeight = FontWeight.Bold)
                        Text("Tap on the map to set location",
                            style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = YantraAmber) }
                },
                actions = {
                    IconButton(onClick = {
                        if (locationPermissionGranted) {
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                                .addOnSuccessListener { loc -> loc?.let {
                                    markerPosition = LatLng(it.latitude, it.longitude)
                                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(markerPosition, 15f))
                                }}
                        } else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    }) { Icon(Icons.Rounded.MyLocation, null, tint = YantraTeal) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        },
        bottomBar = {
            Surface(color = YantraSurface, shadowElevation = 12.dp) {
                Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        color = YantraGreen.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, YantraGreen.copy(alpha = 0.3f))) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (resolvedName.isNotBlank())
                                Text("📍 $resolvedName", style = MaterialTheme.typography.bodyMedium,
                                    color = YantraGreen, fontWeight = FontWeight.SemiBold)
                            Text("${"%.5f".format(markerPosition.latitude)}, ${"%.5f".format(markerPosition.longitude)}",
                                style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                        }
                    }
                    Button(onClick = { onLocationPicked(markerPosition) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YantraAmber, contentColor = YantraAsphalt),
                        elevation = ButtonDefaults.buttonElevation(0.dp)) {
                        Text("Confirm Location", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        GoogleMap(modifier = Modifier.fillMaxSize().padding(padding),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
            uiSettings = MapUiSettings(myLocationButtonEnabled = false),
            onMapClick = { markerPosition = it }) {
            Marker(state = MarkerState(position = markerPosition),
                title = resolvedName.ifBlank { "Vehicle Location" },
                icon  = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        }
    }
}

// ── Availability calendar ─────────────────────────────────────────────────────

@Composable
fun AvailabilityCalendar(currentMonth: YearMonth, selectedDates: Set<String>,
                         onDateToggle: (String) -> Unit, onMonthChange: (YearMonth) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today     = LocalDate.now()

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = YantraSurface) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                    Icon(Icons.Rounded.ChevronLeft, null, tint = YantraAmber)
                }
                Text(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium, color = YantraWhite, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                    Icon(Icons.Rounded.ChevronRight, null, tint = YantraAmber)
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su","Mo","Tu","We","Th","Fr","Sa").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                }
            }
            Spacer(Modifier.height(4.dp))
            val firstDay      = currentMonth.atDay(1)
            val startOffset   = firstDay.dayOfWeek.value % 7
            val daysInMonth   = currentMonth.lengthOfMonth()
            val rows          = (startOffset + daysInMonth + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayIndex = row * 7 + col - startOffset + 1
                        if (dayIndex < 1 || dayIndex > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            val date      = currentMonth.atDay(dayIndex)
                            val dateStr   = date.format(formatter)
                            val isPast    = date.isBefore(today)
                            val isSelected = selectedDates.contains(dateStr)
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                .clip(CircleShape)
                                .background(when { isSelected -> YantraAmber; else -> Color.Transparent })
                                .clickable(enabled = !isPast) { if (!isPast) onDateToggle(dateStr) },
                                contentAlignment = Alignment.Center) {
                                Text(dayIndex.toString(), style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when { isSelected -> YantraAsphalt; isPast -> YantraGrey30; else -> YantraWhite })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Form helpers ──────────────────────────────────────────────────────────────

@Composable
private fun FormLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = YantraGrey60)
}

@Composable
private fun YantraTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(modifier = Modifier.clip(RoundedCornerShape(50.dp))
        .background(if (selected) YantraAmber else YantraSurface)
        .border(if (selected) 0.dp else 1.dp, YantraGrey30, RoundedCornerShape(50.dp))
        .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = if (selected) YantraAsphalt else YantraGrey60,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
private fun FleetTextField(value: String, onValueChange: (String) -> Unit,
                           label: String, placeholder: String,
                           keyboardType: KeyboardType = KeyboardType.Text,
                           modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(value = value, onValueChange = onValueChange,
        label       = { Text(label,       color = YantraGrey60) },
        placeholder = { Text(placeholder, color = YantraGrey30) },
        modifier    = modifier, shape = RoundedCornerShape(12.dp), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = YantraAmber,  unfocusedBorderColor = YantraGrey30,
            focusedTextColor   = YantraWhite,  unfocusedTextColor   = YantraWhite,
            cursorColor        = YantraAmber,  focusedLabelColor    = YantraAmber,
            focusedContainerColor = YantraSurfaceHigh, unfocusedContainerColor = YantraSurfaceHigh))
}