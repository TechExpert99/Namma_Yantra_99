package com.nayak.nammayantara.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.*
import com.nayak.nammayantara.data.model.Equipment
import com.nayak.nammayantara.ui.theme.*
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentMapScreen(
    equipmentList:    List<Equipment>,
    onEquipmentClick: (Equipment) -> Unit,
    onBack: () -> Unit,
) {
    val context          = LocalContext.current
    val defaultLatLng    = LatLng(12.9716, 77.5946)   // Bengaluru
    var userLocation     by remember { mutableStateOf<LatLng?>(null) }
    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    var locationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION]   == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            val cts = CancellationTokenSource()
            try {
                val loc = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
                loc?.let { userLocation = LatLng(it.latitude, it.longitude) }
            } catch (_: Exception) {}
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation ?: defaultLatLng, 12f)
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 13f))
        }
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Equipment Map",
                            style      = MaterialTheme.typography.titleLarge,
                            color      = YantraWhite,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "${equipmentList.size} vehicles nearby",
                            style = MaterialTheme.typography.labelSmall,
                            color = YantraGrey60,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = YantraAmber)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (locationPermissionGranted) {
                            val cts = CancellationTokenSource()
                            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                                .addOnSuccessListener { loc ->
                                    loc?.let { userLocation = LatLng(it.latitude, it.longitude) }
                                }
                        } else {
                            permissionLauncher.launch(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                            )
                        }
                    }) {
                        Icon(Icons.Rounded.MyLocation, contentDescription = "My Location", tint = YantraTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Map ──────────────────────────────────────────────────────────
            GoogleMap(
                modifier            = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties          = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                uiSettings          = MapUiSettings(myLocationButtonEnabled = false),
            ) {
                equipmentList.forEach { equipment ->
                    val position    = LatLng(equipment.latitude, equipment.longitude)
                    val markerColor = when (equipment.status) {
                        "Available" -> BitmapDescriptorFactory.HUE_GREEN
                        "Booked"    -> BitmapDescriptorFactory.HUE_ORANGE
                        else        -> BitmapDescriptorFactory.HUE_RED
                    }
                    Marker(
                        state   = MarkerState(position = position),
                        title   = "${equipment.name}",
                        snippet = "₹${equipment.hourlyRate.toInt()}/hr · ${equipment.status}",
                        icon    = BitmapDescriptorFactory.defaultMarker(markerColor),
                        onClick = { selectedEquipment = equipment; false },
                    )
                }
            }

            // ── Legend ───────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                shape    = RoundedCornerShape(12.dp),
                color    = YantraSurface.copy(alpha = 0.95f),
                border   = BorderStroke(0.5.dp, YantraGrey30),
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    LegendItem("🟢", "Available")
                    LegendItem("🟠", "Booked")
                    LegendItem("🔴", "Unavailable")
                    if (userLocation != null) LegendItem("📍", "You")
                }
            }

            // ── Bottom sheet for selected equipment ──────────────────────────
            selectedEquipment?.let { equipment ->
                val typeIcon = when (equipment.type) {
                    "Tractor" -> "🚜"; "Harvester" -> "🌾"; "Sprayer" -> "💧"; else -> "🚜"
                }
                val (statusColor, _) = when (equipment.status) {
                    "Available" -> YantraGreen to YantraGreen.copy(alpha = 0.12f)
                    "Booked"    -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.12f)
                    else        -> YantraRed to YantraRed.copy(alpha = 0.12f)
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape  = RoundedCornerShape(20.dp),
                    color  = YantraSurface,
                    border = BorderStroke(1.dp, YantraGrey30),
                    shadowElevation = 12.dp,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                                        .background(YantraSurfaceHigh),
                                    contentAlignment = Alignment.Center,
                                ) { Text(typeIcon, fontSize = 24.sp) }

                                Column {
                                    Text(equipment.name, style = MaterialTheme.typography.titleMedium, color = YantraWhite, fontWeight = FontWeight.SemiBold)
                                    Text(equipment.type, style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                                }
                            }
                            // Dismiss
                            IconButton(onClick = { selectedEquipment = null }) {
                                Text("✕", color = YantraGrey60, fontSize = 16.sp)
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
                        Spacer(Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MapStatCell("Hourly", "₹${equipment.hourlyRate.toInt()}/hr")
                            MapStatCell("Daily",  "₹${equipment.dailyRate.toInt()}/day")
                            MapStatCell("Rating", "⭐ ${equipment.conditionRating}")
                            MapStatCell("Fuel",   equipment.fuelType)
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick   = { onEquipmentClick(equipment) },
                            modifier  = Modifier.fillMaxWidth().height(50.dp),
                            shape     = RoundedCornerShape(12.dp),
                            colors    = ButtonDefaults.buttonColors(containerColor = YantraAmber, contentColor = YantraAsphalt),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                        ) {
                            Text("View Details & Book", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(dot: String, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(dot, fontSize = 12.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = YantraWhite)
    }
}

@Composable
private fun MapStatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = YantraAmber, fontWeight = FontWeight.SemiBold)
    }
}