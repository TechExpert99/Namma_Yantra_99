package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayak.nammayantara.data.model.Booking
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.ui.viewmodel.BookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBookingsScreen() {
    val viewModel: BookingViewModel = viewModel()
    var selectedTab         by remember { mutableIntStateOf(0) }
    val tabs                = listOf("Pending", "Accepted", "Declined")
    var showReview          by remember { mutableStateOf(false) }
    var reviewEquipmentId   by remember { mutableStateOf("") }
    var reviewEquipmentName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadMyBookings() }

    if (showReview) {
        ReviewScreen(equipmentId = reviewEquipmentId, equipmentName = reviewEquipmentName,
            onBack = { showReview = false })
        return
    }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", style = MaterialTheme.typography.titleLarge,
                    color = YantraWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab, containerColor = YantraSurface, contentColor = YantraAmber,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = YantraAmber)
                }) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == index) YantraAmber else YantraGrey60) })
                }
            }

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YantraAmber, strokeWidth = 2.5.dp)
                }
            } else {
                val filtered = when (selectedTab) {
                    0 -> viewModel.myBookings.filter { it.status == "Pending" }
                    1 -> viewModel.myBookings.filter { it.status == "Accepted" }
                    else -> viewModel.myBookings.filter { it.status == "Declined" }
                }
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(when (selectedTab) { 0 -> "📭"; 1 -> "✅"; else -> "❌" }, fontSize = 52.sp)
                            Text("No ${tabs[selectedTab].lowercase()} bookings",
                                style = MaterialTheme.typography.titleMedium, color = YantraWhite, fontWeight = FontWeight.SemiBold)
                            Text("They'll appear here once you book equipment.",
                                style = MaterialTheme.typography.bodySmall, color = YantraGrey60, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtered) { booking ->
                            CustomerBookingCard(booking = booking, onWriteReview = {
                                reviewEquipmentId = booking.equipmentId
                                reviewEquipmentName = booking.equipmentName
                                showReview = true
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerBookingCard(booking: Booking, onWriteReview: () -> Unit = {}) {
    val (statusColor, statusBg) = when (booking.status) {
        "Accepted" -> YantraGreen to YantraGreen.copy(alpha = 0.12f)
        "Declined" -> YantraRed   to YantraRed.copy(alpha = 0.12f)
        else       -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.12f)
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color = YantraSurface, border = BorderStroke(1.dp, YantraGrey30)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(booking.equipmentName, style = MaterialTheme.typography.titleMedium,
                        color = YantraWhite, fontWeight = FontWeight.SemiBold)
                    Text("Booking #${booking.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(statusBg)
                    .padding(horizontal = 10.dp, vertical = 5.dp)) {
                    Text(booking.status, color = statusColor,
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = YantraGrey30, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BookingInfoCell("Date",     booking.startDate)
                BookingInfoCell("Duration",
                    if (booking.totalDays > 0) "${booking.totalDays} days" else "${booking.totalHours} hrs")
                BookingInfoCell("Total",    "₹${booking.totalPrice.toInt()}")
            }

            if (booking.status == "Accepted") {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onWriteReview, modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, YantraAmber),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = YantraAmber)) {
                    Icon(Icons.Rounded.Star, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Write a Review", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun BookingInfoCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium, color = YantraAmber, fontWeight = FontWeight.Bold)
    }
}