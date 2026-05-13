package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayak.nammayantara.data.model.Booking
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.ui.viewmodel.OwnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRequestsScreen() {
    val viewModel: OwnerViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Pending", "Accepted", "Declined")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.actionMessage) {
        if (viewModel.actionMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(viewModel.actionMessage)
            viewModel.actionMessage = ""
        }
    }
    LaunchedEffect(Unit) { viewModel.loadIncomingRequests() }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rental Requests", style = MaterialTheme.typography.titleLarge,
                            color = YantraWhite, fontWeight = FontWeight.Bold)
                        Text("Manage incoming requests",
                            style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadIncomingRequests() }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = YantraAmber)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Summary chips ─────────────────────────────────────────────────
            if (viewModel.incomingRequests.isNotEmpty()) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val pending  = viewModel.incomingRequests.count { it.status == "Pending" }
                    val accepted = viewModel.incomingRequests.count { it.status == "Accepted" }
                    val declined = viewModel.incomingRequests.count { it.status == "Declined" }
                    SummaryChip("⏳", pending,  Color(0xFFFF9800), Modifier.weight(1f))
                    SummaryChip("✅", accepted, YantraGreen,       Modifier.weight(1f))
                    SummaryChip("❌", declined, YantraRed,         Modifier.weight(1f))
                }
            }

            // ── Tab row ───────────────────────────────────────────────────────
            TabRow(selectedTabIndex = selectedTab,
                containerColor = YantraSurface,
                contentColor   = YantraAmber,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = YantraAmber,
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelMedium,
                            color = if (selectedTab == index) YantraAmber else YantraGrey60) })
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YantraAmber, strokeWidth = 2.5.dp)
                }
            } else {
                val filtered = when (selectedTab) {
                    0 -> viewModel.incomingRequests.filter { it.status == "Pending" }
                    1 -> viewModel.incomingRequests.filter { it.status == "Accepted" }
                    else -> viewModel.incomingRequests.filter { it.status == "Declined" }
                }

                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📭", fontSize = 52.sp)
                            Text("No ${tabs[selectedTab].lowercase()} requests",
                                style = MaterialTheme.typography.titleMedium,
                                color = YantraWhite, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filtered) { booking ->
                            OwnerRequestCard(booking = booking,
                                onAccept  = { viewModel.acceptRequest(booking.id) },
                                onDecline = { viewModel.declineRequest(booking.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryChip(icon: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp),
        color  = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically) {
            Text(icon, fontSize = 16.sp)
            Spacer(Modifier.width(6.dp))
            Text("$count", style = MaterialTheme.typography.titleMedium,
                color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OwnerRequestCard(booking: Booking, onAccept: () -> Unit, onDecline: () -> Unit) {
    val isPending = booking.status == "Pending"
    val (statusColor, statusBg) = when (booking.status) {
        "Accepted" -> YantraGreen to YantraGreen.copy(alpha = 0.12f)
        "Declined" -> YantraRed   to YantraRed.copy(alpha = 0.12f)
        else       -> Color(0xFFFF9800) to Color(0xFFFF9800).copy(alpha = 0.12f)
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        color  = YantraSurface,
        border = BorderStroke(if (isPending) 1.5.dp else 1.dp,
            if (isPending) Color(0xFFFF9800).copy(alpha = 0.4f) else YantraGrey30)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically) {
                Column {
                    Text(booking.equipmentName, style = MaterialTheme.typography.titleMedium,
                        color = YantraWhite, fontWeight = FontWeight.SemiBold)
                    Text("Request #${booking.id.take(8).uppercase()}",
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
                RequestInfoCell("Date",     booking.startDate)
                RequestInfoCell("Duration",
                    if (booking.totalDays > 0) "${booking.totalDays} days" else "${booking.totalHours} hrs")
                RequestInfoCell("Amount",   "₹${booking.totalPrice.toInt()}")
            }

            if (isPending) {
                Spacer(Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f).height(44.dp),
                        shape  = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, YantraRed.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = YantraRed)) {
                        Text("Decline", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    }
                    Button(onClick = onAccept, modifier = Modifier.weight(1f).height(44.dp),
                        shape  = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = YantraGreen, contentColor = YantraAsphalt),
                        elevation = ButtonDefaults.buttonElevation(0.dp)) {
                        Text("Accept", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestInfoCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleMedium,
            color = YantraAmber, fontWeight = FontWeight.Bold)
    }
}