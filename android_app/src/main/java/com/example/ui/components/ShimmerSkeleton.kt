package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.Dimens

/**
 * Universal subtle shimmer brush for cosmic skeletons.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        Color(0xFF131D33),
        Color(0xFF243254),
        Color(0xFF131D33)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    this.background(brush)
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .height(height)
            .shimmerEffect()
    )
}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 160.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(Dimens.RadiusCard))
            .background(CosmicSurfaceCard)
            .border(1.dp, CosmicBorderSubtle, RoundedCornerShape(Dimens.RadiusCard))
            .padding(Dimens.Spacing12)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height * 0.55f)
                    .clip(RoundedCornerShape(Dimens.RadiusMedium))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(Dimens.Spacing8))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f), height = 14.dp)
            Spacer(modifier = Modifier.height(Dimens.Spacing6))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f), height = 12.dp)
        }
    }
}

@Composable
fun ShimmerItemRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusMedium))
            .background(CosmicSurface)
            .padding(Dimens.Spacing12),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.width(Dimens.Spacing12))
        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f), height = 14.dp)
            Spacer(modifier = Modifier.height(Dimens.Spacing6))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.35f), height = 12.dp)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF090D16)
@Composable
fun ShimmerCardPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        ShimmerCard(height = 140.dp)
        Spacer(modifier = Modifier.height(12.dp))
        ShimmerItemRow()
    }
}
