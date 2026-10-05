package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphic Design Extensions for Jetpack Compose.
 * Provides authentic frosted acrylic look:
 * - Translucent background with soft refraction
 * - Specular gradient hairline border
 * - Top-edge subtle sheen
 */
object GlassmorphismDefaults {
    val DefaultShape = RoundedCornerShape(18.dp)
    val PillShape = RoundedCornerShape(24.dp)
    val BorderWidth = 1.dp

    val GlassCardBackground = Color(0x38152342) // Frosted translucent glass
    val GlassSurfaceBackground = Color(0x33101C34)
    val GlassDockBackground = Color(0x40101A30)
    val GlassElevatedBackground = Color(0x521C2E54)

    val SpecularBorder = Brush.linearGradient(
        colors = listOf(
            Color(0x80FFFFFF), // Bright specular highlight on top-left
            Color(0x3860A5FA), // Sapphire refraction
            Color(0x18FFFFFF), // Soft rim
            Color(0x06000000)  // Shadow transition on bottom-right
        )
    )

    val SpecularBorderSubtle = Brush.linearGradient(
        colors = listOf(
            Color(0x55FFFFFF),
            Color(0x1A60A5FA),
            Color(0x10FFFFFF)
        )
    )

    val GlassSheenGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0x28FFFFFF), // Frosted light refraction
            Color(0x06FFFFFF),
            Color(0x140F172A)
        )
    )

    @Composable
    fun cardColors(
        containerColor: Color = GlassCardBackground,
        contentColor: Color = StarWhite
    ): CardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor
    )

    fun borderStroke(
        width: Dp = BorderWidth,
        brush: Brush = SpecularBorder
    ): BorderStroke = BorderStroke(width = width, brush = brush)
}

/**
 * Modifier extension to apply frosted glass surface and specular border.
 */
fun Modifier.glassmorphic(
    shape: Shape = GlassmorphismDefaults.DefaultShape,
    backgroundColor: Color = GlassmorphismDefaults.GlassCardBackground,
    borderBrush: Brush = GlassmorphismDefaults.SpecularBorder,
    borderWidth: Dp = GlassmorphismDefaults.BorderWidth
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, brush = borderBrush, shape = shape)
