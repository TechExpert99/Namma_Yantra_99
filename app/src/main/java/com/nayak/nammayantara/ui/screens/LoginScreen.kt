package com.nayak.nammayantara.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nayak.nammayantara.ui.theme.*
import com.nayak.nammayantara.ui.viewmodel.AuthViewModel

private enum class LegalPage { TERMS, PRIVACY }

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel: AuthViewModel = viewModel()
    val context      = LocalContext.current
    val activity     = context as Activity
    var selectedRole by remember { mutableStateOf("") }
    var showPage     by remember { mutableStateOf<LegalPage?>(null) }

    // Navigate to legal pages
    when (showPage) {
        LegalPage.TERMS   -> { TermsScreen(onBack   = { showPage = null }); return }
        LegalPage.PRIVACY -> { PrivacyScreen(onBack = { showPage = null }); return }
        null -> {}
    }

    if (viewModel.isLoggedIn) {
        LaunchedEffect(Unit) { onLoginSuccess() }
    }

    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.15f, targetValue = 0.30f,
        animationSpec = infiniteRepeatable(tween(2800, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glowAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YantraAsphalt)
            .drawBehind {
                drawCircle(
                    brush  = Brush.radialGradient(listOf(YantraAmber.copy(alpha = glowAlpha), Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.12f), radius = size.width * 0.55f),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.15f, size.height * 0.12f))
                drawCircle(
                    brush  = Brush.radialGradient(listOf(YantraTeal.copy(alpha = glowAlpha * 0.6f), Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.88f), radius = size.width * 0.5f),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.85f, size.height * 0.88f))
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            // ── Brand mark ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(YantraAmber, YantraAmberDim),
                        start = Offset(0f, 0f), end = Offset(80f, 80f))),
                contentAlignment = Alignment.Center,
            ) { Text("🚜", fontSize = 36.sp) }

            Spacer(Modifier.height(24.dp))
            Text("Namma Yantra", style = MaterialTheme.typography.headlineLarge,
                color = YantraWhite, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Farm equipment, on demand", style = MaterialTheme.typography.bodyMedium,
                color = YantraGrey60, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(48.dp))

            // ── Login card ───────────────────────────────────────────────────
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
                color = YantraSurface, tonalElevation = 0.dp) {
                Column(modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = viewModel.isOtpSent,
                        transitionSpec = {
                            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                        },
                        label = "loginStep",
                    ) { otpSent ->
                        if (!otpSent) PhoneStep(viewModel, activity)
                        else OtpStep(viewModel, selectedRole) { selectedRole = it }
                    }
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            AnimatedVisibility(visible = viewModel.errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(YantraRed.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("⚠", fontSize = 16.sp)
                    Text(viewModel.errorMessage, color = YantraRed, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.weight(1.2f))

            // ── Footer ─────────────────────────────────────────────────────────
            // "Terms of Service" and "Privacy Policy" are individually tappable
            val footerText = buildAnnotatedString {
                withStyle(SpanStyle(color = YantraGrey60)) { append("By continuing you agree to our ") }
                pushStringAnnotation("TERMS", "terms")
                withStyle(SpanStyle(color = YantraAmber, textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium)) { append("Terms of Service") }
                pop()
                withStyle(SpanStyle(color = YantraGrey60)) { append(" and ") }
                pushStringAnnotation("PRIVACY", "privacy")
                withStyle(SpanStyle(color = YantraAmber, textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Medium)) { append("Privacy Policy") }
                pop()
                withStyle(SpanStyle(color = YantraGrey60)) { append("\n— Team TechExpert99 —") }
            }

            androidx.compose.foundation.text.ClickableText(
                text     = footerText,
                style    = MaterialTheme.typography.labelSmall.copy(textAlign = TextAlign.Center),
                modifier = Modifier.padding(bottom = 24.dp),
                onClick  = { offset ->
                    footerText.getStringAnnotations("TERMS",   offset, offset).firstOrNull()?.let { showPage = LegalPage.TERMS }
                    footerText.getStringAnnotations("PRIVACY", offset, offset).firstOrNull()?.let { showPage = LegalPage.PRIVACY }
                },
            )
        }
    }
}

// ── Phone step ────────────────────────────────────────────────────────────────

@Composable
private fun PhoneStep(viewModel: AuthViewModel, activity: Activity) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Enter your mobile number", style = MaterialTheme.typography.titleMedium, color = YantraGrey60)
        OutlinedTextField(
            value = viewModel.phoneNumber, onValueChange = { viewModel.phoneNumber = it },
            placeholder = { Text("98765 43210", color = YantraGrey30) },
            prefix = { Text("+91  ", color = YantraAmber, fontWeight = FontWeight.SemiBold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = loginFieldColors())
        YantraPrimaryButton("Send OTP", viewModel.isLoading, viewModel.phoneNumber.length >= 10) { viewModel.sendOtp(activity) }
    }
}

// ── OTP + role step ───────────────────────────────────────────────────────────

@Composable
private fun OtpStep(viewModel: AuthViewModel, selectedRole: String, onRoleSelected: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("OTP sent to +91 ${viewModel.phoneNumber}",
            style = MaterialTheme.typography.bodySmall, color = YantraGrey60)
        OutlinedTextField(
            value = viewModel.otp, onValueChange = { viewModel.otp = it },
            placeholder = { Text("• • • • • •", color = YantraGrey30, letterSpacing = 6.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp),
            colors = loginFieldColors())
        Spacer(Modifier.height(4.dp))
        Text("I am a", style = MaterialTheme.typography.labelMedium, color = YantraGrey60)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("owner" to "🚜  Owner", "renter" to "🌾  Renter").forEach { (role, label) ->
                RoleChip(label, selectedRole == role, Modifier.weight(1f)) { onRoleSelected(role) }
            }
        }
        YantraPrimaryButton("Verify & Continue", viewModel.isLoading,
            selectedRole.isNotEmpty() && viewModel.otp.length >= 4) { viewModel.verifyOtp(selectedRole) }
    }
}

// ── Shared helpers ────────────────────────────────────────────────────────────

@Composable
private fun loginFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = YantraAmber,   unfocusedBorderColor = YantraGrey30,
    focusedTextColor   = YantraWhite,   unfocusedTextColor   = YantraWhite,
    cursorColor        = YantraAmber,
    focusedContainerColor   = YantraSurfaceHigh,
    unfocusedContainerColor = YantraSurfaceHigh,
)

@Composable
private fun YantraPrimaryButton(text: String, isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(52.dp),
        enabled = enabled && !isLoading, shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = YantraAmber, contentColor = YantraAsphalt,
            disabledContainerColor = YantraGrey30, disabledContentColor = YantraGrey60),
        elevation = ButtonDefaults.buttonElevation(0.dp)) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp),
            color = YantraAsphalt, strokeWidth = 2.5.dp, strokeCap = StrokeCap.Round)
        else Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RoleChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp))
        .background(if (selected) YantraAmber.copy(alpha = 0.12f) else YantraSurfaceHigh)
        .border(if (selected) 1.5.dp else 1.dp, if (selected) YantraAmber else YantraGrey30, RoundedCornerShape(12.dp))
        .clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center) {
        Text(label, style = MaterialTheme.typography.labelLarge,
            color = if (selected) YantraAmber else YantraGrey60,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}