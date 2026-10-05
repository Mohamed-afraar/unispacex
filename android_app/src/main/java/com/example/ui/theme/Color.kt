package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// UniSpaceX Frosted Glassmorphism Theme
// Translucent frosted glass layers allowing vibrant ambient background aurora orbs to diffuse through
val CosmicBackground = Color(0xFF060913) // Deep rich cosmos space backdrop
val CosmicSecondary = Color(0xFF0A0F1E)
val CosmicSurface = Color(0x33101C34) // Frosted glass surface
val CosmicSurfaceCard = Color(0x3D152342) // Translucent glass card surface (24% alpha navy glass)
val CosmicSurfaceElevated = Color(0x521C2E54) // Highlighted glass panel (32% alpha)
val CosmicSurfaceGlass = Color(0x40101A30) // Translucent frosted dock and header glass
val CosmicSurfaceGlassLight = Color(0x28FFFFFF) // Specular highlight glass
val CosmicSurfaceHigh = Color(0x66223458)
val CosmicBorder = Color(0x38FFFFFF) // Top-edge white specular reflection
val CosmicBorderSubtle = Color(0x24FFFFFF) // Hairline glass rim
val CosmicBorderGlow = Color(0x6638BDF8) // Diffused sapphire edge glow
val CosmicBorderNeon = Color(0x992563EB)

// Accent Brand Colors - Electric Sapphire, Cobalt, Sky Teal & Mint
val CosmicPurple = Color(0xFF3B82F6) // Vibrant Royal Cobalt
val ElectricViolet = Color(0xFF2563EB) // Authoritative Electric Sapphire Blue
val CometBlue = Color(0xFF60A5FA) // Ice Blue
val CosmicCyan = Color(0xFF06B6D4) // Precision Teal
val StarWhite = Color(0xFFFFFFFF) // Pristine Pure White
val SoftLavender = Color(0xFFE2E8F0) // Clean Slate White
val TextMuted = Color(0xFF94A3B8) // Refined Slate Muted
val TextSubtle = Color(0xFF64748B) // Subtle Caption Slate

// Modern Luxury Accents
val RoyalSapphire = Color(0xFF1D4ED8) // Deep Executive Sapphire
val MintEmerald = Color(0xFF10B981) // Precision Mint Emerald (Verified & Success)
val NeonPink = Color(0xFFF43F5E) // Refined Rose / Coral Accent
val NeonEmerald = Color(0xFF10B981) // Precision Mint Emerald (Verified & Success)
val SolarGold = Color(0xFFF59E0B) // Champagne Gold (Ratings & Founder Seals)
val CelestialGold = Color(0xFFF59E0B) // Champagne Gold

// Status Colors
val CosmicSuccess = Color(0xFF10B981)
val CosmicWarning = Color(0xFFF59E0B)
val CosmicError = Color(0xFFEF4444)

// Gradient Brushes
val GradientComet = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF10B981))
)

val GradientVioletCyan = Brush.linearGradient(
    colors = listOf(Color(0xFF1D4ED8), Color(0xFF06B6D4))
)

val GradientBlueCyan = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF38BDF8))
)

val GradientAurora = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF06B6D4), Color(0xFF10B981))
)

// Frosted Glass Card Gradient: subtle top specular highlight fading into glass body
val GradientCosmicCard = Brush.verticalGradient(
    colors = listOf(
        Color(0x4D24365C), // Specular light catch
        Color(0x2E131E36)  // Ambient glass body
    )
)

// Signature Glassmorphism Hairline Specular Border:
// Catches light at the top-left and drops to a delicate semi-transparent tint at the bottom
val GradientGlassBorder = Brush.linearGradient(
    colors = listOf(
        Color(0x80FFFFFF), // Bright specular highlight on top-left
        Color(0x3860A5FA), // Sapphire refraction
        Color(0x18FFFFFF), // Soft rim
        Color(0x06000000)  // Shadow transition on bottom-right
    )
)

val GradientGlassSheen = Brush.verticalGradient(
    colors = listOf(
        Color(0x33FFFFFF), // Frosted top reflection
        Color(0x0AFFFFFF),
        Color(0x180F172A)
    )
)

val GradientCometTrail = Brush.horizontalGradient(
    colors = listOf(Color.Transparent, Color(0xFF2563EB), Color(0xFF06B6D4), StarWhite)
)

// Legacy compatibility
val Purple80 = SoftLavender
val PurpleGrey80 = TextMuted
val Pink80 = ElectricViolet
val Purple40 = CosmicPurple
val PurpleGrey40 = CosmicSurfaceHigh
val Pink40 = CometBlue
