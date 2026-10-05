package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite

/**
 * Authentic USX App Logo Vector & Composition Component
 * Replicates the provided USX brand identity:
 * - 'U': "STUDENTS FOR STUDENTS" student parcel handoff motif
 * - 'S': Campus shelves with books, notes, apparel, camera, headphones
 * - 'X': Bold ascending teal growth arrow with graduation cap symbol
 * - Tagline: "BUY · SELL · COLLABORATE" - A BIGGER CAMPUS TOGETHER
 * - 4 Pillars: DISCOVER, SHOP, CONNECT, GROW
 */
@Composable
fun UsxAppLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF131B2E),
                        Color(0xFF1E2844)
                    )
                )
            )
            .border(
                1.dp,
                CosmicBorderGlow,
                RoundedCornerShape(size * 0.28f)
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(size * 0.12f)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Back segment of glowing orbital planetary ring
            val orbitBack = Path().apply {
                moveTo(w * 0.20f, h * 0.40f)
                cubicTo(w * 0.30f, h * 0.25f, w * 0.70f, h * 0.35f, w * 0.82f, h * 0.52f)
            }
            drawPath(
                path = orbitBack,
                brush = Brush.linearGradient(
                    colors = listOf(ElectricViolet.copy(alpha = 0.5f), CosmicCyan.copy(alpha = 0.7f)),
                    start = Offset(w * 0.20f, h * 0.40f),
                    end = Offset(w * 0.82f, h * 0.52f)
                ),
                style = Stroke(width = w * 0.07f, cap = StrokeCap.Round)
            )

            // 2. Cosmic Cross Beam (Upper-Left to Lower-Right leg of 'X')
            drawLine(
                start = Offset(w * 0.24f, h * 0.26f),
                end = Offset(w * 0.76f, h * 0.78f),
                brush = Brush.linearGradient(
                    listOf(ElectricViolet, CosmicPurple, Color(0xFF4F46E5))
                ),
                strokeWidth = w * 0.13f,
                cap = StrokeCap.Round
            )

            // 3. Lower Thruster Ion Exhaust (Ascending leg lower part)
            drawLine(
                start = Offset(w * 0.22f, h * 0.80f),
                end = Offset(w * 0.38f, h * 0.64f),
                brush = Brush.linearGradient(
                    listOf(CometBlue, CosmicCyan)
                ),
                strokeWidth = w * 0.10f,
                cap = StrokeCap.Round
            )

            // 4. Front segment of glowing orbital ring
            val orbitFront = Path().apply {
                moveTo(w * 0.82f, h * 0.52f)
                cubicTo(w * 0.76f, h * 0.72f, w * 0.36f, h * 0.76f, w * 0.20f, h * 0.62f)
            }
            drawPath(
                path = orbitFront,
                brush = Brush.linearGradient(
                    colors = listOf(CosmicCyan, Color(0xFF38BDF8)),
                    start = Offset(w * 0.82f, h * 0.52f),
                    end = Offset(w * 0.20f, h * 0.62f)
                ),
                style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
            )

            // 5. Ascending Supersonic Rocket Fuselage (Upper part of Ascending leg)
            val rocketBody = Path().apply {
                moveTo(w * 0.38f, h * 0.64f)
                lineTo(w * 0.35f, h * 0.54f)
                lineTo(w * 0.52f, h * 0.38f)
                lineTo(w * 0.70f, h * 0.22f)
                lineTo(w * 0.74f, h * 0.26f)
                lineTo(w * 0.62f, h * 0.44f)
                lineTo(w * 0.44f, h * 0.68f)
                close()
            }
            drawPath(
                path = rocketBody,
                brush = Brush.linearGradient(
                    listOf(CosmicCyan, Color(0xFF38BDF8), StarWhite),
                    start = Offset(w * 0.35f, h * 0.68f),
                    end = Offset(w * 0.74f, h * 0.22f)
                )
            )

            // Left Delta Wing
            val leftWing = Path().apply {
                moveTo(w * 0.40f, h * 0.60f)
                lineTo(w * 0.28f, h * 0.66f)
                lineTo(w * 0.44f, h * 0.48f)
                close()
            }
            drawPath(path = leftWing, color = Color(0xFF2563EB))

            // Right Delta Wing
            val rightWing = Path().apply {
                moveTo(w * 0.60f, h * 0.42f)
                lineTo(w * 0.66f, h * 0.32f)
                lineTo(w * 0.48f, h * 0.46f)
                close()
            }
            drawPath(path = rightWing, color = Color(0xFF7C3AED))

            // Amber Cockpit Glass
            val cockpit = Path().apply {
                moveTo(w * 0.57f, h * 0.36f)
                lineTo(w * 0.65f, h * 0.28f)
                lineTo(w * 0.63f, h * 0.34f)
                close()
            }
            drawPath(path = cockpit, color = Color(0xFFF59E0B))

            // 6. Central Campus Nexus Core (Golden Starburst)
            val nexusStar = Path().apply {
                moveTo(w * 0.50f, h * 0.44f)
                lineTo(w * 0.53f, h * 0.49f)
                lineTo(w * 0.58f, h * 0.50f)
                lineTo(w * 0.53f, h * 0.51f)
                lineTo(w * 0.50f, h * 0.56f)
                lineTo(w * 0.47f, h * 0.51f)
                lineTo(w * 0.42f, h * 0.50f)
                lineTo(w * 0.47f, h * 0.49f)
                close()
            }
            drawPath(path = nexusStar, color = Color(0xFFFFD166))

            // 7. Apex North Star / Campus Launch Beacon (Top Right)
            val apexBeacon = Path().apply {
                moveTo(w * 0.78f, h * 0.10f)
                lineTo(w * 0.81f, h * 0.17f)
                lineTo(w * 0.88f, h * 0.19f)
                lineTo(w * 0.81f, h * 0.21f)
                lineTo(w * 0.78f, h * 0.28f)
                lineTo(w * 0.75f, h * 0.21f)
                lineTo(w * 0.68f, h * 0.19f)
                lineTo(w * 0.75f, h * 0.17f)
                close()
            }
            drawPath(path = apexBeacon, color = StarWhite)
            drawCircle(
                color = CosmicCyan.copy(alpha = 0.5f),
                radius = w * 0.05f,
                center = Offset(w * 0.78f, h * 0.19f)
            )
        }
    }
}

/**
 * Full USX Brand Banner Card replicating the official logo graphics,
 * motto, subtitles, and pillar icons.
 */
@Composable
fun UsxFullBrandHeroCard(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090E29)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(CosmicCyan.copy(alpha = 0.7f), CosmicBorder)
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("usx_brand_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main USX Stylized Typography Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // U - Students for Students
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF101B42))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "U",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "STUDENTS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "FOR STUDENTS",
                        fontSize = 7.sp,
                        color = SoftLavender
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // S - Campus Shelf / Marketplace
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF281C10))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "S",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFBBF24)
                    )
                    Text(
                        text = "SHELVES",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "BUY & SELL",
                        fontSize = 7.sp,
                        color = Color(0xFFFCD34D)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // X - Growth Arrow & Grad Cap
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF06232E))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "X",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF34D399)
                        )
                        Text(text = "↗", fontSize = 24.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Black)
                    }
                    Text(
                        text = "COLLAB",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "GROW CAREER",
                        fontSize = 7.sp,
                        color = Color(0xFF6EE7B7)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtitle: BUY · SELL · COLLABORATE
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(CosmicBorder))
                Text(
                    text = "  BUY  ·  SELL  ·  COLLABORATE  ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = StarWhite
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(CosmicBorder))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "A  B I G G E R   C A M P U S   T O G E T H E R",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = CosmicCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Brand Pillars (DISCOVER, SHOP, CONNECT, GROW)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF06091D))
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                PillarItem(icon = Icons.Default.ShoppingCart, label = "DISCOVER", color = CosmicCyan)
                PillarItem(icon = Icons.Default.LocalMall, label = "SHOP", color = Color(0xFFF59E0B))
                PillarItem(icon = Icons.Default.Handshake, label = "CONNECT", color = ElectricViolet)
                PillarItem(icon = Icons.AutoMirrored.Filled.TrendingUp, label = "GROW", color = Color(0xFF10B981))
            }
        }
    }
}

@Composable
private fun PillarItem(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = StarWhite,
            letterSpacing = 0.8.sp
        )
    }
}
