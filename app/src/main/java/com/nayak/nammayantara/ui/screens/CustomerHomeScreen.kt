package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayak.nammayantara.data.model.Equipment
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.ui.viewmodel.BookingViewModel
import com.nayak.nammayantara.ui.viewmodel.EquipmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen() {
    val equipmentViewModel: EquipmentViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()

    var selectedEquipment by remember { mutableStateOf<Equipment?>(null) }
    var showChat by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(equipmentViewModel.isLoading) {
        if (!equipmentViewModel.isLoading) isRefreshing = false
    }

    when {
        showChat -> {
            ChatScreen(onBack = { showChat = false })
        }
        bookingViewModel.isSuccess -> {
            BookingSuccessScreen(
                bookingId = bookingViewModel.bookingId,
                onGoHome = {
                    bookingViewModel.reset()
                    selectedEquipment = null
                }
            )
        }
        showMap -> {
            EquipmentMapScreen(
                equipmentList = equipmentViewModel.filteredList,
                onEquipmentClick = { equipment ->
                    bookingViewModel.reset()
                    selectedEquipment = equipment
                    showMap = false
                },
                onBack = { showMap = false }
            )
        }
        selectedEquipment != null -> {
            DetailScreen(
                equipment = selectedEquipment!!,
                bookingViewModel = bookingViewModel,
                onBack = {
                    selectedEquipment = null
                    bookingViewModel.reset()
                },
                onRequestSent = { bookingViewModel.sendRequest(selectedEquipment!!) }
            )
        }
        else -> {
            Scaffold(
                containerColor = YantraAsphalt,
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // ── Tractor logo (Fallback emoji) ──────────
                                Text(
                                    text = "🚜",
                                    fontSize = 28.sp
                                )
                                Column {
                                    Text(
                                        "Namma Yantra",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = YantraWhite,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        "Find equipment near you",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = YantraGrey60,
                                    )
                                }
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                isRefreshing = true
                                equipmentViewModel.loadEquipment()
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Refresh",
                                    tint = YantraGreen,
                                )
                            }
                            IconButton(onClick = { showMap = !showMap }) {
                                Icon(
                                    imageVector = if (showMap) Icons.Rounded.ViewList else Icons.Rounded.Map,
                                    contentDescription = if (showMap) "List view" else "Map view",
                                    tint = YantraAmber,
                                )
                            }
                            IconButton(onClick = { showChat = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = "AI Chat",
                                    tint = YantraTeal,
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = YantraSurface,
                        )
                    )
                }
            ) { padding ->
                PullToRefreshBox(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        equipmentViewModel.loadEquipment()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            state = pullRefreshState,
                            isRefreshing = isRefreshing,
                            color = YantraGreen,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {

                        if (equipmentViewModel.equipmentList.isEmpty() && !equipmentViewModel.isLoading) {
                            EmptyStateCard(onAddSample = { equipmentViewModel.addSampleData() })
                        }

                        LazyRow(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(equipmentViewModel.types) { type ->
                                YantraFilterChip(
                                    label = type,
                                    selected = equipmentViewModel.selectedType == type,
                                    onClick = { equipmentViewModel.selectedType = type }
                                )
                            }
                        }

                        if (equipmentViewModel.isLoading) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = YantraAmber,
                                    strokeWidth = 2.5.dp,
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(equipmentViewModel.filteredList) { equipment ->
                                    EquipmentCard(
                                        equipment = equipment,
                                        onClick = {
                                            bookingViewModel.reset()
                                            selectedEquipment = equipment
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateCard(onAddSample: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = YantraSurface,
        border = BorderStroke(1.dp, YantraAmber.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🚜", fontSize = 36.sp)
            Text(
                "No equipment listed yet",
                style = MaterialTheme.typography.titleMedium,
                color = YantraWhite,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Pull down or tap 🔄 to refresh",
                style = MaterialTheme.typography.bodySmall,
                color = YantraGrey60,
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onAddSample,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YantraAmber,
                    contentColor = YantraAsphalt,
                ),
            ) {
                Text(
                    "Add Sample Equipment",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Filter chip ───────────────────────────────────────────────────────────────

@Composable
private fun YantraFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg          = if (selected) YantraAmber else YantraSurface
    val textColor   = if (selected) YantraAsphalt else YantraGrey60
    val borderColor = if (selected) YantraAmber else YantraGrey30

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

// ── Equipment card ────────────────────────────────────────────────────────────

@Composable
fun EquipmentCard(equipment: Equipment, onClick: () -> Unit) {
    val (statusColor, statusBg) = when (equipment.status) {
        "Available" -> YantraGreen to YantraGreen.copy(alpha = 0.12f)
        "Booked"    -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.12f)
        else        -> YantraRed to YantraRed.copy(alpha = 0.12f)
    }
    val typeIcon = when (equipment.type) {
        "Tractor"   -> "🚜"
        "Harvester" -> "🌾"
        "Sprayer"   -> "💧"
        else        -> "🚜"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = YantraSurface,
        border = BorderStroke(1.dp, YantraGrey30),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(YantraSurfaceHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(typeIcon, fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            equipment.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = YantraWhite,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            equipment.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = YantraGrey60,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        equipment.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatCell(label = "Hourly", value = "₹${equipment.hourlyRate.toInt()}", unit = "/hr")
                StatCell(label = "Daily",  value = "₹${equipment.dailyRate.toInt()}",  unit = "/day")
                StatCell(label = "Rating", value = "⭐ ${equipment.conditionRating}",  unit = "")
                StatCell(label = "Fuel",   value = equipment.fuelType,                unit = "")
            }

            if (equipment.locationName.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    "📍 ${equipment.locationName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = YantraGrey60,
                )
            }
        }
    }
}

@Composable
private fun StatCell(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = YantraAmber,
                fontWeight = FontWeight.Bold,
            )
            if (unit.isNotEmpty()) {
                Text(unit, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
            }
        }
    }
}