package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.LocalCosmicTheme

/**
 * Executive Ambient Backdrop for UniSpaceX.
 * Dynamically responds to active theme mode (Cosmic Dark, AMOLED Deep, Nebula Glow)
 * and accent glow hue (Electric Cyan, Cosmic Violet, Solar Gold, Supernova Rose),
 * providing ultra-clean, high-contrast atmospheric depth.
 */
@Composable
fun CosmicBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val theme = LocalCosmicTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Base Executive Gradient according to theme mode
            val baseGradient = when {
                theme.themeMode.contains("AMOLED", ignoreCase = true) -> {
                    listOf(
                        Color(0xFF000000),
                        Color(0xFF040406),
                        Color(0xFF000000)
                    )
                }
                theme.themeMode.contains("Nebula", ignoreCase = true) -> {
                    listOf(
                        Color(0xFF140B2E),
                        Color(0xFF0C071E),
                        Color(0xFF080414)
                    )
                }
                else -> { // Glassmorphism / Frosted Acrylic
                    listOf(
                        Color(0xFF060913),
                        Color(0xFF080E1C),
                        Color(0xFF050710)
                    )
                }
            }

            drawRect(
                brush = Brush.verticalGradient(colors = baseGradient)
            )

            // Dynamic glow alpha optimized for glassmorphic refraction
            val isAmoled = theme.themeMode.contains("AMOLED", ignoreCase = true)
            val primaryAlpha = if (isAmoled) 0.12f else 0.28f
            val secondaryAlpha = if (isAmoled) 0.09f else 0.22f
            val tertiaryAlpha = if (isAmoled) 0.07f else 0.18f

            // 2. Top-Center Radiant Ambient Cyan/Teal Glass Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        theme.primary.copy(alpha = primaryAlpha),
                        theme.primaryVariant.copy(alpha = primaryAlpha * 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.35f, height * 0.05f),
                    radius = width * 0.95f
                ),
                radius = width * 0.95f,
                center = Offset(width * 0.35f, height * 0.05f)
            )

            // 3. Top-Right Electric Sapphire & Cobalt Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        theme.secondary.copy(alpha = secondaryAlpha),
                        Color(0xFF1D4ED8).copy(alpha = secondaryAlpha * 0.5f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.92f, height * 0.22f),
                    radius = width * 0.75f
                ),
                radius = width * 0.75f,
                center = Offset(width * 0.92f, height * 0.22f)
            )

            // 4. Center-Left Cosmic Violet Mesh Diffuser (creates breathtaking glass refraction under cards)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.20f),
                        Color(0xFF6366F1).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.02f, height * 0.52f),
                    radius = width * 0.85f
                ),
                radius = width * 0.85f,
                center = Offset(width * 0.02f, height * 0.52f)
            )

            // 5. Mid-Right Mint Emerald / Sky Refraction Orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        theme.tertiary.copy(alpha = tertiaryAlpha),
                        Color(0xFF06B6D4).copy(alpha = tertiaryAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.98f, height * 0.72f),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(width * 0.98f, height * 0.72f)
            )

            // 6. Bottom Horizon Ambient Rose & Royal Sapphire Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2563EB).copy(alpha = 0.22f),
                        Color(0xFFEC4899).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.98f),
                    radius = width * 0.95f
                ),
                radius = width * 0.95f,
                center = Offset(width * 0.5f, height * 0.98f)
            )
        }

        content()
    }
}
