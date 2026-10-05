package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderProcess
import com.example.model.OrderProgressStep
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSuccess
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite

@Composable
fun CosmicOrderProgress(
    order: OrderProcess,
    onAdvanceStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = OrderProgressStep.values()
    val activeIndex = order.currentStep.index

    val infiniteTransition = rememberInfiniteTransition(label = "comet_step_orbit")
    val cometSparkle by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "comet_sparkle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Order Info Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "☄️", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = order.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${order.orderId} · ${order.providerName} → ${order.clientName}",
                    fontSize = 11.sp,
                    color = SoftLavender,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricViolet.copy(alpha = 0.2f))
                    .border(0.8.dp, CosmicCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "₹${order.amount}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Orbital Comet Path Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val padX = 14.dp.toPx()
                val usableWidth = size.width - (padX * 2)
                val height = size.height / 2f
                val stepWidth = usableWidth / (steps.size - 1).coerceAtLeast(1)

                // Draw background orbital line
                drawLine(
                    color = Color(0x336C3BFF),
                    start = Offset(padX, height),
                    end = Offset(size.width - padX, height),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Draw active comet trajectory
                val currentX = padX + (activeIndex * stepWidth)
                if (activeIndex > 0) {
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                CosmicPurple.copy(alpha = 0.4f),
                                ElectricViolet,
                                CometBlue,
                                CosmicCyan
                            ),
                            startX = padX,
                            endX = currentX
                        ),
                        start = Offset(padX, height),
                        end = Offset(currentX, height),
                        strokeWidth = 4.5f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw Step Nodes
                steps.forEachIndexed { index, step ->
                    val x = padX + (index * stepWidth)
                    val isPast = index < activeIndex
                    val isCurrent = index == activeIndex

                    val nodeColor = when {
                        isCurrent -> CosmicCyan
                        isPast -> ElectricViolet
                        else -> Color(0xFF282F58)
                    }

                    // Outer halo
                    if (isCurrent) {
                        drawCircle(
                            color = CosmicCyan.copy(alpha = 0.3f * cometSparkle),
                            radius = 14f * cometSparkle,
                            center = Offset(x, height)
                        )
                    }

                    // Center node circle
                    drawCircle(
                        color = nodeColor,
                        radius = if (isCurrent) 7.5f else 5f,
                        center = Offset(x, height)
                    )

                    if (isCurrent || isPast) {
                        drawCircle(
                            color = StarWhite,
                            radius = if (isCurrent) 3f else 1.8f,
                            center = Offset(x, height)
                        )
                    }
                }
            }
        }

        // Step Label Pills Row (Weight-distributed so it never wraps or overflows)
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            steps.forEachIndexed { index, step ->
                val isCurrent = index == activeIndex
                val isPast = index < activeIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = step.title,
                        fontSize = 8.5.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isCurrent -> CosmicCyan
                            isPast -> StarWhite
                            else -> SoftLavender.copy(alpha = 0.6f)
                        },
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress Details & Action Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (order.currentStep == OrderProgressStep.COMPLETED) CosmicSuccess else CometBlue, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Status: ${order.currentStep.title}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StarWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAdvanceStep,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CosmicPurple,
                    contentColor = StarWhite
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (order.currentStep == OrderProgressStep.REVIEW) "Restart Orbit" else "Advance ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
