package com.nayak.nammayantara.ui.theme

import androidx.compose.ui.graphics.Color

// ── NammaYantra Palette ──────────────────────────────────────────────────────
// Inspired by Bengaluru's night roads: asphalt, neon amber, and instrument glow

// Brand / Primary — amber-gold like headlights cutting through night
val YantraAmber        = Color(0xFFFFB300)   // vivid amber — primary CTA, highlights
val YantraAmberDim     = Color(0xFFFF8F00)   // deep amber — pressed / variant

// Surface / Background — layered dark like dashboard and asphalt
val YantraAsphalt      = Color(0xFF0D0D0F)   // near-black background
val YantraSurface      = Color(0xFF18181C)   // card / sheet surface
val YantraSurfaceHigh  = Color(0xFF242429)   // elevated surface (dialogs, nav)

// Accent — teal glow like instrument cluster LEDs
val YantraTeal         = Color(0xFF00C9B1)   // accent / secondary
val YantraTealMuted    = Color(0xFF009E8E)   // variant

// Neutral text / icon
val YantraWhite        = Color(0xFFF5F5F5)
val YantraGrey60       = Color(0xFF9E9E9E)
val YantraGrey30       = Color(0xFF424242)

// Status
val YantraGreen        = Color(0xFF4CAF50)
val YantraRed          = Color(0xFFEF5350)

// Legacy aliases kept for generated theme compatibility
val YantraPrimaryLight = YantraAmber
val YantraSecLight     = YantraTeal
val YantraTerLight     = YantraAmberDim

val YantraPrimaryDark  = YantraAmber
val YantraSecDark      = YantraTeal
val YantraTerDark      = YantraTealMuted