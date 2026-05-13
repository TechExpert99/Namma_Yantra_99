package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.nayak.nammayantara.data.model.Equipment
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.ui.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    equipment: Equipment,
    bookingViewModel: BookingViewModel,
    onBack: () -> Unit,
    onRequestSent: () -> Unit,
) {
    val totalPrice = bookingViewModel.getTotalPrice(equipment)

    val typeIcon = when (equipment.type) {
        "Tractor"   -> "🚜"
        "Harvester" -> "🌾"
        "Sprayer"   -> "💧"
        else        -> "🚜"
    }
    val (statusColor, statusBg) = when (equipment.status) {
        "Available" -> YantraGreen to YantraGreen.copy(alpha = 0.12f)
        "Booked"    -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.12f)
        else        -> YantraRed to YantraRed.copy(alpha = 0.12f)
    }

    val hasLocation = equipment.latitude != 0.0 && equipment.longitude != 0.0
    val equipLocation = LatLng(
        if (hasLocation) equipment.latitude else 12.9716,
        if (hasLocation) equipment.longitude else 77.5946,
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(equipLocation, 14f)
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        equipment.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = YantraWhite,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = YantraAmber,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        },
        bottomBar = {
            Surface(
                color = YantraSurface,
                shadowElevation = 12.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .navigationBarsPadding()
                ) {
                    if (bookingViewModel.isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp)),
                            color = YantraAmber,
                            trackColor = YantraGrey30,
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    if (bookingViewModel.errorMessage.isNotEmpty()) {
                        Text(
                            bookingViewModel.errorMessage,
                            color = YantraRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }

                    val isAvailable = equipment.status == "Available"
                    Button(
                        onClick = onRequestSent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = isAvailable && !bookingViewModel.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = YantraAmber,
                            contentColor = YantraAsphalt,
                            disabledContainerColor = YantraGrey30,
                            disabledContentColor = YantraGrey60,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Text(
                            text = if (isAvailable) "Send Rental Request  ·  ₹${totalPrice.toInt()}"
                            else "Not Available",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Hero card ────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = YantraSurface,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Icon on gradient pill
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(YantraAmber.copy(alpha = 0.18f), YantraTeal.copy(alpha = 0.10f))
                                )
                            )
                            .border(1.dp, YantraAmber.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(typeIcon, fontSize = 48.sp)
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        equipment.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = YantraWhite,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        equipment.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = YantraGrey60,
                    )
                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            equipment.status,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // ── Machine health ───────────────────────────────────────────────
            SectionCard(title = "Machine Health") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    HealthItem(label = "Rating",   value = "${equipment.conditionRating}/5", icon = "⭐")
                    HealthDivider()
                    HealthItem(label = "Fuel",     value = equipment.fuelType,               icon = "⛽")
                    HealthDivider()
                    HealthItem(label = "Serviced", value = equipment.lastServiceDate,         icon = "🔩")
                }
            }

            // ── Price calculator ─────────────────────────────────────────────
            SectionCard(title = "Price Calculator") {
                // Rate toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RateToggleChip(
                        label = "Hourly",
                        sublabel = "₹${equipment.hourlyRate.toInt()}/hr",
                        selected = !bookingViewModel.useDaily,
                        modifier = Modifier.weight(1f),
                        onClick = { bookingViewModel.useDaily = false },
                    )
                    RateToggleChip(
                        label = "Daily",
                        sublabel = "₹${equipment.dailyRate.toInt()}/day",
                        selected = bookingViewModel.useDaily,
                        modifier = Modifier.weight(1f),
                        onClick = { bookingViewModel.useDaily = true },
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Slider
                if (!bookingViewModel.useDaily) {
                    SliderRow(
                        label = "Hours",
                        value = bookingViewModel.hours,
                        onValueChange = { bookingViewModel.hours = it },
                        range = 1f..12f,
                        steps = 10,
                    )
                } else {
                    SliderRow(
                        label = "Days",
                        value = bookingViewModel.days,
                        onValueChange = { bookingViewModel.days = it },
                        range = 1f..30f,
                        steps = 28,
                    )
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            if (!bookingViewModel.useDaily)
                                "${bookingViewModel.hours} hr × ₹${equipment.hourlyRate.toInt()}"
                            else
                                "${bookingViewModel.days} day × ₹${equipment.dailyRate.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = YantraGrey60,
                        )
                        Text(
                            "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = YantraGrey60,
                        )
                    }
                    Text(
                        "₹${totalPrice.toInt()}",
                        style = MaterialTheme.typography.displayMedium,
                        color = YantraAmber,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // ── Location map ─────────────────────────────────────────────────
            SectionCard(title = "Equipment Location") {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        scrollGesturesEnabled = false,
                        zoomGesturesEnabled = false,
                    )
                ) {
                    Marker(
                        state = MarkerState(position = equipLocation),
                        title = equipment.name,
                        snippet = equipment.status,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (equipment.status == "Available") BitmapDescriptorFactory.HUE_YELLOW
                            else BitmapDescriptorFactory.HUE_ORANGE
                        )
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "${"%.4f".format(equipLocation.latitude)}, ${"%.4f".format(equipLocation.longitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = YantraGrey60,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Shared section card ───────────────────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = YantraSurface,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = YantraWhite,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

// ── Health row items ──────────────────────────────────────────────────────────

@Composable
fun HealthItem(label: String, value: String, icon: String = "") {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = YantraWhite, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
    }
}

@Composable
private fun HealthDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(YantraGrey30)
    )
}

// ── Rate toggle chip ──────────────────────────────────────────────────────────

@Composable
private fun RateToggleChip(
    label: String,
    sublabel: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (selected) YantraAmber.copy(alpha = 0.14f) else YantraSurfaceHigh
    val border = if (selected) YantraAmber else YantraGrey30
    val labelColor = if (selected) YantraAmber else YantraGrey60

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, border),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = labelColor, fontWeight = FontWeight.SemiBold)
            Text(sublabel, style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
        }
    }
}

// ── Slider row ────────────────────────────────────────────────────────────────

@Composable
private fun SliderRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = YantraGrey60)
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = YantraAmber,
            fontWeight = FontWeight.Bold,
        )
    }
    Slider(
        value = value.toFloat(),
        onValueChange = { onValueChange(it.toInt()) },
        valueRange = range,
        steps = steps,
        colors = SliderDefaults.colors(
            thumbColor = YantraAmber,
            activeTrackColor = YantraAmber,
            inactiveTrackColor = YantraGrey30,
        )
    )
}