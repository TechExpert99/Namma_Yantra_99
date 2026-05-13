package com.nayak.nammayantara.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nayak.nammayantara.data.model.Review
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.utils.Constants
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OwnerRatingsViewModel : ViewModel() {
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var reviews       = mutableStateListOf<Review>()
    var isLoading     by mutableStateOf(false)
    var averageRating by mutableFloatStateOf(0f)

    init { loadRatings() }

    fun loadRatings() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val equipIds = db.collection(Constants.EQUIPMENT)
                    .whereEqualTo("ownerId", uid).get().await().documents.map { it.id }
                val allReviews = mutableListOf<Review>()
                equipIds.forEach { equipId ->
                    allReviews.addAll(
                        db.collection("reviews").whereEqualTo("equipmentId", equipId).get().await()
                            .documents.mapNotNull { it.toObject(Review::class.java)?.copy(id = it.id) }
                    )
                }
                reviews.clear(); reviews.addAll(allReviews)
                averageRating = if (allReviews.isNotEmpty())
                    allReviews.map { it.rating }.average().toFloat() else 0f
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerRatingsScreen() {
    val viewModel: OwnerRatingsViewModel = viewModel()

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = { Text("My Ratings", style = MaterialTheme.typography.titleLarge,
                    color = YantraWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        when {
            viewModel.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = YantraAmber, strokeWidth = 2.5.dp)
            }
            viewModel.reviews.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("⭐", fontSize = 52.sp)
                    Text("No ratings yet", style = MaterialTheme.typography.titleMedium,
                        color = YantraWhite, fontWeight = FontWeight.SemiBold)
                    Text("Ratings from renters will appear here",
                        style = MaterialTheme.typography.bodySmall, color = YantraGrey60,
                        textAlign = TextAlign.Center)
                }
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // ── Overall rating card ──────────────────────────────────────
                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                        color = YantraSurface) {
                        Column(modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Overall Rating", style = MaterialTheme.typography.labelMedium,
                                color = YantraGrey60)
                            Spacer(Modifier.height(8.dp))

                            // Big score
                            Text("%.1f".format(viewModel.averageRating),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                                color = YantraAmber, fontWeight = FontWeight.Bold)

                            Spacer(Modifier.height(8.dp))
                            // Stars
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                (1..5).forEach { star ->
                                    Icon(imageVector = if (star <= viewModel.averageRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint     = if (star <= viewModel.averageRating) Color(0xFFFFC107) else YantraGrey30,
                                        modifier = Modifier.size(26.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("${viewModel.reviews.size} reviews total",
                                style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                        }
                    }
                }

                item {
                    Text("All Reviews", style = MaterialTheme.typography.titleMedium,
                        color = YantraWhite, fontWeight = FontWeight.SemiBold)
                }

                items(viewModel.reviews) { review ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        color = YantraSurface, border = BorderStroke(0.5.dp, YantraGrey30)) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically) {
                                Column {
                                    Text(review.equipmentName, style = MaterialTheme.typography.titleMedium,
                                        color = YantraWhite, fontWeight = FontWeight.SemiBold)
                                    val masked = review.reviewerPhone.let {
                                        if (it.length > 5) it.dropLast(5) + "•••••" else it
                                    }
                                    Text(masked, style = MaterialTheme.typography.labelSmall, color = YantraGrey60)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    (1..5).forEach { star ->
                                        Icon(imageVector = if (star <= review.rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                            contentDescription = null,
                                            tint     = if (star <= review.rating) Color(0xFFFFC107) else YantraGrey30,
                                            modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(review.comment, style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
                        }
                    }
                }
            }
        }
    }
}