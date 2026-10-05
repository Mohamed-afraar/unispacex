package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VerificationType
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicSuccess
import com.example.ui.theme.CosmicWarning
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.StarWhite

@Composable
fun CosmicBadge(
    badge: VerificationType,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (icon, label, accentColor, bgColor) = when (badge) {
        VerificationType.STUDENT_VERIFIED -> Quad(
            "🎓",
            "Student Verified",
            CosmicSuccess,
            Color(0x2210B981)
        )
        VerificationType.BUSINESS_VERIFIED -> Quad(
            "🏪",
            "Business Verified",
            CometBlue,
            Color(0x2238BDF8)
        )
        VerificationType.TRUSTED_SELLER -> Quad(
            "⭐",
            "Trusted Seller",
            CosmicWarning,
            Color(0x22F59E0B)
        )
        VerificationType.RISING_ENTREPRENEUR -> Quad(
            "☄️",
            "Rising Entrepreneur",
            ElectricViolet,
            Color(0x259B5CFF)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.8.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = if (compact) 2.dp else 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = if (compact) 10.sp else 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CosmicBadgeRow(
    badges: List<VerificationType>,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        badges.forEach { badge ->
            CosmicBadge(badge = badge, compact = compact)
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
