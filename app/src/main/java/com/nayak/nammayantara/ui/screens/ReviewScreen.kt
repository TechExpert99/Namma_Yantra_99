package com.nayak.nammayantara.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope
import com.nayak.nammayantara.data.model.Review
import com.nayak.nammayantara.data.repository.ReviewRepository
import com.nayak.nammayantara.ui.theme.*
import kotlinx.coroutines.launch

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ReviewViewModel : ViewModel() {
    private val repo = ReviewRepository()

    var reviews       = androidx.compose.runtime.mutableStateListOf<Review>()
    var isLoading     by androidx.compose.runtime.mutableStateOf(false)
    var isSubmitting  by androidx.compose.runtime.mutableStateOf(false)
    var submitSuccess by androidx.compose.runtime.mutableStateOf(false)
    var errorMessage  by androidx.compose.runtime.mutableStateOf("")

    fun loadReviews(equipmentId: String) {
        viewModelScope.launch {
            isLoading = true
            reviews.clear()
            reviews.addAll(repo.getReviewsForEquipment(equipmentId))
            isLoading = false
        }
    }

    fun submitReview(equipmentId: String, equipmentName: String, rating: Float, comment: String) {
        if (rating == 0f)     { errorMessage = "Please select a star rating"; return }
        if (comment.isBlank()) { errorMessage = "Please write a comment";      return }
        viewModelScope.launch {
            isSubmitting = true
            errorMessage = ""
            val result = repo.submitReview(equipmentId, equipmentName, rating, comment)
            if (result.isSuccess) {
                submitSuccess = true
                loadReviews(equipmentId)
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Failed to submit"
            }
            isSubmitting = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    equipmentId:   String,
    equipmentName: String,
    onBack: () -> Unit,
) {
    val viewModel: ReviewViewModel = viewModel()

    var userRating  by remember { mutableStateOf(0f) }
    var userComment by remember { mutableStateOf("") }
    var showForm    by remember { mutableStateOf(true) }

    LaunchedEffect(equipmentId)           { viewModel.loadReviews(equipmentId) }
    LaunchedEffect(viewModel.submitSuccess) { if (viewModel.submitSuccess) showForm = false }

    Scaffold(
        containerColor = YantraAsphalt,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Reviews",
                            style      = MaterialTheme.typography.titleLarge,
                            color      = YantraWhite,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            equipmentName,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YantraSurface),
            )
        }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Write / success ──────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = showForm,
                    enter   = fadeIn() + slideInVertically(),
                ) {
                    WriteReviewCard(
                        userRating    = userRating,
                        onRatingChange = { userRating = it },
                        userComment   = userComment,
                        onCommentChange = { userComment = it },
                        errorMessage  = viewModel.errorMessage,
                        isSubmitting  = viewModel.isSubmitting,
                        onSubmit      = {
                            viewModel.submitReview(equipmentId, equipmentName, userRating, userComment)
                        },
                    )
                }

                AnimatedVisibility(
                    visible = !showForm,
                    enter   = fadeIn() + slideInVertically(),
                ) {
                    SubmitSuccessBanner()
                }
            }

            // ── Section header ───────────────────────────────────────────────
            item {
                Text(
                    "All Reviews (${viewModel.reviews.size})",
                    style      = MaterialTheme.typography.titleMedium,
                    color      = YantraWhite,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // ── Loading / empty / list ───────────────────────────────────────
            when {
                viewModel.isLoading -> item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = YantraAmber, strokeWidth = 2.5.dp)
                    }
                }
                viewModel.reviews.isEmpty() -> item {
                    EmptyReviews()
                }
                else -> items(viewModel.reviews) { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

// ── Write review card ─────────────────────────────────────────────────────────

@Composable
private fun WriteReviewCard(
    userRating:      Float,
    onRatingChange:  (Float) -> Unit,
    userComment:     String,
    onCommentChange: (String) -> Unit,
    errorMessage:    String,
    isSubmitting:    Boolean,
    onSubmit:        () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = YantraSurface,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                "Write a Review",
                style      = MaterialTheme.typography.titleMedium,
                color      = YantraWhite,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))

            // Star selector
            Text("Your Rating", style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick  = { onRatingChange(star.toFloat()) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = if (star <= userRating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "$star stars",
                            tint     = if (star <= userRating) Color(0xFFFFC107) else YantraGrey30,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                if (userRating > 0) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${userRating.toInt()}/5",
                        style      = MaterialTheme.typography.labelMedium,
                        color      = Color(0xFFFFC107),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value         = userComment,
                onValueChange = onCommentChange,
                placeholder   = { Text("Describe your experience…", color = YantraGrey30) },
                modifier      = Modifier.fillMaxWidth(),
                minLines      = 3,
                maxLines      = 5,
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = YantraAmber,
                    unfocusedBorderColor = YantraGrey30,
                    focusedTextColor     = YantraWhite,
                    unfocusedTextColor   = YantraWhite,
                    cursorColor          = YantraAmber,
                    focusedContainerColor   = YantraSurfaceHigh,
                    unfocusedContainerColor = YantraSurfaceHigh,
                ),
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(errorMessage, color = YantraRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick  = onSubmit,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = !isSubmitting,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = YantraAmber,
                    contentColor   = YantraAsphalt,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color       = YantraAsphalt,
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Submit Review", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Submit success banner ─────────────────────────────────────────────────────

@Composable
private fun SubmitSuccessBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = YantraSurface,
        border   = BorderStroke(1.dp, YantraGreen.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(YantraGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Text("✅", fontSize = 22.sp) }
            Column {
                Text("Review submitted!", style = MaterialTheme.typography.titleMedium, color = YantraGreen, fontWeight = FontWeight.SemiBold)
                Text("Thank you for your feedback", style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyReviews() {
    Column(
        modifier              = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp),
    ) {
        Text("💬", fontSize = 48.sp)
        Text("No reviews yet", style = MaterialTheme.typography.titleMedium, color = YantraWhite, fontWeight = FontWeight.SemiBold)
        Text("Be the first to review!", style = MaterialTheme.typography.bodySmall, color = YantraGrey60, textAlign = TextAlign.Center)
    }
}

// ── Review card ───────────────────────────────────────────────────────────────

@Composable
fun ReviewCard(review: Review) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = YantraSurface,
        border   = BorderStroke(0.5.dp, YantraGrey30),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                // Masked phone
                val masked = review.reviewerPhone.let {
                    if (it.length > 5) it.dropLast(5) + "•••••" else it
                }
                Text(
                    masked,
                    style      = MaterialTheme.typography.labelMedium,
                    color      = YantraWhite,
                    fontWeight = FontWeight.Medium,
                )

                // Stars
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= review.rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint     = if (star <= review.rating) Color(0xFFFFC107) else YantraGrey30,
                            modifier = Modifier.size(15.dp),
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