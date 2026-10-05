package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.Dimens
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted

/**
 * Universal list state for asynchronous loading, empty fallback, or error handling.
 */
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Empty(val message: String = "No items found in orbit") : UiState<Nothing>
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>
    data class Content<T>(val data: T) : UiState<T>
}

@Composable
fun EmptyState(
    icon: String = "🌌",
    title: String = "Nothing in this orbit yet",
    message: String = "Explore other quadrants or create a new campus listing.",
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusLarge))
            .background(CosmicSurfaceCard)
            .border(1.dp, CosmicBorder, RoundedCornerShape(Dimens.RadiusLarge))
            .padding(Dimens.Spacing24),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2846)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing16))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StarWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing6))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavender,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.Spacing16)
            )

            if (!actionLabel.isNullOrBlank() && onAction != null) {
                Spacer(modifier = Modifier.height(Dimens.Spacing16))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                    shape = RoundedCornerShape(Dimens.RadiusSmall),
                    modifier = Modifier.height(Dimens.CompactButtonHeight)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = StarWhite
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String = "Unable to connect to campus orbit.",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.RadiusLarge))
            .background(CosmicSurfaceCard)
            .border(1.dp, Color(0x33EF4444), RoundedCornerShape(Dimens.RadiusLarge))
            .padding(Dimens.Spacing20),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "⚠️", fontSize = 32.sp)

            Spacer(modifier = Modifier.height(Dimens.Spacing10))

            Text(
                text = "Orbit Connection Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StarWhite
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing4))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(Dimens.Spacing12))
                OutlinedButton(
                    onClick = onRetry,
                    shape = RoundedCornerShape(Dimens.RadiusSmall),
                    modifier = Modifier.height(Dimens.CompactButtonHeight)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = CosmicCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.size(Dimens.Spacing6))
                    Text(
                        text = "Retry Orbit",
                        style = MaterialTheme.typography.labelMedium,
                        color = CosmicCyan
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF090D16)
@Composable
fun ListStatesPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        EmptyState(actionLabel = "Clear Filters", onAction = {})
        Spacer(modifier = Modifier.height(12.dp))
        ErrorState(onRetry = {})
    }
}
