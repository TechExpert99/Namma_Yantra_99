package com.nayak.nammayantara.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Color Schemes ────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    // Primary — amber, the brand signature
    primary            = YantraAmber,
    onPrimary          = YantraAsphalt,          // dark text on amber buttons
    primaryContainer   = YantraAmberDim,
    onPrimaryContainer = YantraWhite,

    // Secondary — teal instrument glow
    secondary          = YantraTeal,
    onSecondary        = YantraAsphalt,
    secondaryContainer = YantraTealMuted,
    onSecondaryContainer = YantraWhite,

    // Tertiary — muted amber variant
    tertiary           = YantraAmberDim,
    onTertiary         = YantraAsphalt,
    tertiaryContainer  = Color(0xFF3E2900),
    onTertiaryContainer = YantraAmber,

    // Backgrounds
    background         = YantraAsphalt,
    onBackground       = YantraWhite,
    surface            = YantraSurface,
    onSurface          = YantraWhite,
    surfaceVariant     = YantraSurfaceHigh,
    onSurfaceVariant   = YantraGrey60,

    // Outline / dividers
    outline            = YantraGrey30,
    outlineVariant     = Color(0xFF2C2C31),

    // Error
    error              = YantraRed,
    onError            = YantraWhite,
    errorContainer     = Color(0xFF4B0000),
    onErrorContainer   = Color(0xFFFFB4AB),

    // Scrim / overlay
    scrim              = Color(0xCC0D0D0F),
    inverseSurface     = YantraWhite,
    inverseOnSurface   = YantraAsphalt,
    inversePrimary     = YantraAmberDim,
)

private val LightColorScheme = lightColorScheme(
    // In light mode keep amber as primary but on a warm white base
    primary            = YantraAmberDim,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFFFECC2),
    onPrimaryContainer = Color(0xFF2D1600),

    secondary          = Color(0xFF006B5F),
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFFBCEDE6),
    onSecondaryContainer = Color(0xFF00201C),

    tertiary           = Color(0xFFBF6900),
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFFFFDDB8),
    onTertiaryContainer = Color(0xFF2C1600),

    background         = Color(0xFFFFFBF5),
    onBackground       = Color(0xFF1C1B1A),
    surface            = Color(0xFFFFFBF5),
    onSurface          = Color(0xFF1C1B1A),
    surfaceVariant     = Color(0xFFF1E8D8),
    onSurfaceVariant   = Color(0xFF504539),

    outline            = Color(0xFF837568),
    outlineVariant     = Color(0xFFD4C4B4),

    error              = Color(0xFFBA1A1A),
    onError            = Color.White,
    errorContainer     = Color(0xFFFFDAD6),
    onErrorContainer   = Color(0xFF410002),

    scrim              = Color(0xFF000000),
    inverseSurface     = Color(0xFF312F2D),
    inverseOnSurface   = Color(0xFFF5EFE7),
    inversePrimary     = YantraAmber,
)

// ── Theme Composable ─────────────────────────────────────────────────────────

/**
 * NammaYantra app theme.
 *
 * Dynamic colour is opt-in; the curated [DarkColorScheme] / [LightColorScheme]
 * are used by default so the amber brand identity is always preserved.
 *
 * @param darkTheme      Force dark/light; defaults to system setting.
 * @param dynamicColor   Use Android 12+ Material You wallpaper colours.
 *                       Disabled by default to keep brand colours.
 */
@Composable
fun NammaYantraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,          // off by default — keep brand amber
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    // Make status bar transparent and match dark/light icon tint
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}