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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CustomerReviewsViewModel : ViewModel() {
    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var reviews   = mutableStateListOf<Review>()
    var isLoading by mutableStateOf(false)

    init { loadMyReviews() }

    fun loadMyReviews() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val snapshot = db.collection("reviews")
                    .whereEqualTo("reviewerId", uid)
                    .get().await()
                reviews.clear()
                reviews.addAll(snapshot.documents.mapNotNull {
                    it.toObject(Review::class.java)?.copy(id = it.id)
                })
            } catch (_: Exception) {}
            isLoading = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerReviewsScreen() {
    val viewModel: CustomerReviewsViewModel = viewModel()

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Reviews",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = YantraWhite,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        when {
            viewModel.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = YantraAmber, strokeWidth = 2.5.dp)
                }
            }
            viewModel.reviews.isEmpty() -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("⭐", fontSize = 52.sp)
                        Text(
                            "No reviews yet",
                            style      = MaterialTheme.typography.titleMedium,
                            color      = YantraWhite,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Reviews you write will appear here",
                            style       = MaterialTheme.typography.bodySmall,
                            color       = YantraGrey60,
                            textAlign   = TextAlign.Center,
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier        = Modifier.fillMaxSize().padding(padding),
                    contentPadding  = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(viewModel.reviews) { review ->
                        MyReviewCard(review)
                    }
                }
            }
        }
    }
}

@Composable
private fun MyReviewCard(review: Review) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = YantraSurface,
        border   = BorderStroke(0.5.dp, YantraGrey30),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Text(
                    review.equipmentName,
                    style      = MaterialTheme.typography.titleMedium,
                    color      = YantraWhite,
                    fontWeight = FontWeight.SemiBold,
                )
                // Stars
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= review.rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint     = if (star <= review.rating) Color(0xFFFFC107) else YantraGrey30,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodySmall,
                color = YantraGrey60,
            )
        }
    }
}