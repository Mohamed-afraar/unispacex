package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dynamic design tokens for UniSpaceX theme system.
 * Allows theme mode (Cosmic Dark, AMOLED Deep, Nebula Glow) and
 * accent glow hue (Electric Cyan, Cosmic Violet, Solar Gold, Supernova Rose)
 * to be dynamically configured and propagated throughout all composables.
 */
data class CosmicThemeTokens(
    val themeMode: String = "Glassmorphism",
    val accentName: String = "Electric Cyan",
    val primary: Color = CosmicCyan,
    val primaryVariant: Color = ElectricViolet,
    val secondary: Color = CometBlue,
    val tertiary: Color = MintEmerald,
    val background: Color = CosmicBackground,
    val surface: Color = CosmicSurface,
    val surfaceCard: Color = CosmicSurfaceCard,
    val surfaceElevated: Color = CosmicSurfaceElevated,
    val surfaceGlass: Color = CosmicSurfaceGlass,
    val border: Color = CosmicBorder,
    val borderGlow: Color = CosmicBorderGlow,
    val borderSubtle: Color = CosmicBorderSubtle,
    val textPrimary: Color = StarWhite,
    val textMuted: Color = TextMuted,
    val textSubtle: Color = TextSubtle
)

val LocalCosmicTheme = staticCompositionLocalOf { CosmicThemeTokens() }

private data class ModeColors(
    val bg: Color,
    val surf: Color,
    val surfCard: Color,
    val surfElevated: Color,
    val surfGlass: Color,
    val border: Color,
    val borderGlow: Color
)

fun resolveCosmicTokens(themeMode: String, accentName: String): CosmicThemeTokens {
    val (primary, secondary, tertiary) = when (accentName) {
        "Cosmic Violet" -> Triple(Color(0xFF8B5CF6), Color(0xFFA855F7), Color(0xFFC084FC))
        "Solar Gold" -> Triple(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFFBBF24))
        "Supernova Rose" -> Triple(Color(0xFFF43F5E), Color(0xFFE11D48), Color(0xFFFB7185))
        else -> Triple(Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF10B981)) // Electric Cyan
    }

    val mode = when {
        themeMode.contains("AMOLED", ignoreCase = true) -> {
            ModeColors(
                bg = Color(0xFF000000), // Pure OLED black
                surf = Color(0x28101018),
                surfCard = Color(0x35161622),
                surfElevated = Color(0x481E1E2E),
                surfGlass = Color(0x330D0D14),
                border = Color(0x40FFFFFF),
                borderGlow = primary.copy(alpha = 0.55f)
            )
        }
        themeMode.contains("Nebula", ignoreCase = true) -> {
            ModeColors(
                bg = Color(0xFF090418), // Deep cyberpunk nebula
                surf = Color(0x331C0F40),
                surfCard = Color(0x3D281558),
                surfElevated = Color(0x52361B72),
                surfGlass = Color(0x38190B38),
                border = Color(0x40C084FC),
                borderGlow = Color(0x70C084FC)
            )
        }
        themeMode.contains("Cosmic Dark", ignoreCase = true) -> {
            ModeColors(
                bg = Color(0xFF090D16),
                surf = Color(0x30111827),
                surfCard = Color(0x3D172138),
                surfElevated = Color(0x4D1E2844),
                surfGlass = Color(0x3811182B),
                border = Color(0x333B82F6),
                borderGlow = Color(0x553B82F6)
            )
        }
        else -> { // "Glassmorphism" / "Frosted Glassmorphism" (Default signature glass)
            ModeColors(
                bg = Color(0xFF060913),
                surf = Color(0x30101C34),
                surfCard = Color(0x3D152342),
                surfElevated = Color(0x521C2E54),
                surfGlass = Color(0x3D101A30),
                border = Color(0x40FFFFFF),
                borderGlow = Color(0x6638BDF8)
            )
        }
    }

    return CosmicThemeTokens(
        themeMode = themeMode,
        accentName = accentName,
        primary = primary,
        primaryVariant = secondary,
        secondary = secondary,
        tertiary = tertiary,
        background = mode.bg,
        surface = mode.surf,
        surfaceCard = mode.surfCard,
        surfaceElevated = mode.surfElevated,
        surfaceGlass = mode.surfGlass,
        border = mode.border,
        borderGlow = mode.borderGlow,
        borderSubtle = Color(0x24FFFFFF)
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    themeMode: String = "Glassmorphism",
    accentColorName: String = "Electric Cyan",
    content: @Composable () -> Unit
) {
    val tokens = resolveCosmicTokens(themeMode, accentColorName)

    val colorScheme = darkColorScheme(
        primary = tokens.primary,
        onPrimary = StarWhite,
        primaryContainer = tokens.primaryVariant,
        onPrimaryContainer = StarWhite,
        secondary = tokens.secondary,
        onSecondary = tokens.background,
        secondaryContainer = tokens.surfaceElevated,
        onSecondaryContainer = tokens.primary,
        tertiary = tokens.tertiary,
        onTertiary = tokens.background,
        background = tokens.background,
        onBackground = StarWhite,
        surface = tokens.surface,
        onSurface = StarWhite,
        surfaceVariant = tokens.surfaceElevated,
        onSurfaceVariant = SoftLavender,
        outline = tokens.border,
        outlineVariant = tokens.borderGlow,
        error = CosmicError,
        onError = StarWhite
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = tokens.background.toArgb()
                window.navigationBarColor = tokens.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(LocalCosmicTheme provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
