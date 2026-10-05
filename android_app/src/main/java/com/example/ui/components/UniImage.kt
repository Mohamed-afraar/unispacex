package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.StarWhite

/**
 * Robust image loader for UniSpaceX with shimmer skeleton placeholder,
 * initials/icon fallback on error or blank URL, and crossfade transition.
 */
@Composable
fun UniImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackInitials: String? = null,
    fallbackIcon: String = "🪐"
) {
    val appliedModifier = if (shape != null) modifier.clip(shape) else modifier

    if (model == null || (model is String && model.isBlank())) {
        UniImageFallback(
            modifier = appliedModifier,
            initials = fallbackInitials,
            icon = fallbackIcon
        )
    } else {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(300)
                .build(),
            contentDescription = contentDescription,
            modifier = appliedModifier,
            contentScale = contentScale,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmerEffect()
                )
            },
            error = {
                UniImageFallback(
                    modifier = Modifier.fillMaxSize(),
                    initials = fallbackInitials,
                    icon = fallbackIcon
                )
            }
        )
    }
}

@Composable
private fun UniImageFallback(
    modifier: Modifier,
    initials: String?,
    icon: String
) {
    Box(
        modifier = modifier.background(Color(0xFF16213A)),
        contentAlignment = Alignment.Center
    ) {
        if (!initials.isNullOrBlank()) {
            Text(
                text = initials.take(2).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StarWhite
            )
        } else {
            Text(
                text = icon,
                fontSize = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UniImagePreview() {
    UniImage(
        model = null,
        contentDescription = "Avatar",
        fallbackInitials = "UA",
        shape = CircleShape
    )
}
