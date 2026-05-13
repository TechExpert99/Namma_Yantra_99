package com.nayak.nammayantara.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nayak.nammayantara.ui.theme.*

@Composable
fun BookingSuccessScreen(
    bookingId: String,
    onGoHome: () -> Unit,
) {
    // Pulsing glow behind the check icon
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.55f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    // Pop-in scale for the icon
    val iconScale by rememberInfiniteTransition(label = "iconScale").animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YantraAsphalt)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            YantraGreen.copy(alpha = pulse * 0.25f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width / 2f, size.height * 0.28f),
                        radius = size.width * 0.65f,
                    ),
                    radius = size.width * 0.65f,
                    center = Offset(size.width / 2f, size.height * 0.28f),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
        ) {

            // ── Check icon with glow ring ────────────────────────────────────
            Box(contentAlignment = Alignment.Center) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(YantraGreen.copy(alpha = pulse * 0.12f))
                        .border(1.dp, YantraGreen.copy(alpha = pulse * 0.4f), CircleShape)
                )
                // Inner icon
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = YantraGreen,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(iconScale),
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Request Sent!",
                style = MaterialTheme.typography.headlineMedium,
                color = YantraWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Your booking request has been sent to the owner.\nYou'll be notified once they accept.",
                style = MaterialTheme.typography.bodyMedium,
                color = YantraGrey60,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(32.dp))

            // ── Booking ID card ──────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = YantraSurface,
                border = BorderStroke(1.dp, YantraGreen.copy(alpha = 0.25f)),
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "BOOKING ID",
                        style = MaterialTheme.typography.labelSmall,
                        color = YantraGrey60,
                        letterSpacing = 1.5.sp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = bookingId,
                        style = MaterialTheme.typography.bodyLarge,
                        color = YantraAmber,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Status pill ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(YantraGreen.copy(alpha = 0.10f))
                    .border(1.dp, YantraGreen.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(YantraGreen)
                )
                Text(
                    "Awaiting owner confirmation",
                    style = MaterialTheme.typography.labelMedium,
                    color = YantraGreen,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(48.dp))

            // ── CTA button ───────────────────────────────────────────────────
            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = YantraAmber,
                    contentColor = YantraAsphalt,
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp),
            ) {
                Text(
                    "Back to Home",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}