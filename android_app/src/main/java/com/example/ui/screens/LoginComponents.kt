package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.Dimens
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted

@Composable
fun RoleSelectionCard(
    role: UserRole,
    isSelected: Boolean,
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF162544) else CosmicSurfaceCard
        ),
        shape = RoundedCornerShape(Dimens.RadiusCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isSelected) listOf(CosmicCyan, ElectricViolet) else listOf(CosmicBorder, CosmicBorder)
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(Dimens.Spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CosmicCyan.copy(alpha = 0.2f) else Color(0xFF131B2E)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(Dimens.Spacing14))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftLavender
                )
            }
        }
    }
}

@Composable
fun LoginProgressStepper(
    currentStep: Int,
    totalSteps: Int = 2,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            val isActive = i <= currentStep
            Box(
                modifier = Modifier
                    .size(if (i == currentStep) 28.dp else 22.dp)
                    .clip(CircleShape)
                    .background(if (isActive) CosmicCyan else CosmicBorder),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$i",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color(0xFF090D16) else SoftLavender
                )
            }
            if (i < totalSteps) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(2.dp)
                        .background(if (currentStep > i) CosmicCyan else CosmicBorder)
                )
            }
        }
    }
}

@Composable
fun authTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CosmicSurface,
    unfocusedContainerColor = CosmicSurface,
    focusedBorderColor = CosmicCyan,
    unfocusedBorderColor = CosmicBorder,
    focusedTextColor = StarWhite,
    unfocusedTextColor = StarWhite,
    cursorColor = CosmicCyan
)

@Preview
@Composable
fun RoleSelectionCardPreview() {
    RoleSelectionCard(
        role = UserRole.STUDENT,
        isSelected = true,
        title = "Student Innovator",
        subtitle = "Discover products, join crews, and freelance",
        icon = "🎓",
        onClick = {}
    )
}
