package com.nayak.nammayantara.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.nayak.nammayantara.R

// ── Google Fonts Provider ──────────────────────────────────────────────────
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// ── Typefaces ────────────────────────────────────────────────────────────────
// Display / UI — Sora: geometric, modern, great at small & large sizes
val SoraFont = GoogleFont("Sora")

val SoraFontFamily = FontFamily(
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// Monospace — DM Mono: for data, distances, prices, ID numbers
val DmMonoFont = GoogleFont("DM Mono")

val DmMonoFontFamily = FontFamily(
    Font(googleFont = DmMonoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = DmMonoFont, fontProvider = provider, weight = FontWeight.Medium),
)

// ── Typography ────────────────────────────────────────────────────────────────
val Typography = Typography(
    // Large Headlines — Bold & Expressive
    displayLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),

    // Subtitles & Section Headers
    titleLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp
    ),

    // Body Text — Clean & Readable
    bodyLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // Labels — Used for buttons, chips, and small metadata
    labelLarge = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DmMonoFontFamily, // Using Mono for small IDs/Codes
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
