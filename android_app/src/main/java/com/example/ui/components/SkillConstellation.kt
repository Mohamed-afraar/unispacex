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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SkillNode
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite

@Composable
fun SkillConstellation(
    skills: List<SkillNode>,
    modifier: Modifier = Modifier,
    title: String = "Skill Constellation",
    subtitle: String = "Interactive neural orbital map"
) {
    var selectedSkillId by remember { mutableStateOf(skills.firstOrNull()?.id ?: 1) }

    val infiniteTransition = rememberInfiniteTransition(label = "constellation_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "node_pulse"
    )

    val selectedSkill = skills.firstOrNull { it.id == selectedSkillId } ?: skills.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CosmicSurface)
            .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
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
                    .background(CosmicPurple.copy(alpha = 0.25f))
                    .border(0.8.dp, CosmicCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${skills.size} Nodes",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CosmicCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Constellation Interactive Canvas with Dynamic Responsive Box Constraints
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070A1E))
                .border(0.8.dp, CosmicBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        ) {
            val canvasWidthPx = maxWidth
            val canvasHeightPx = 210.dp

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(skills) {
                        detectTapGestures { tapOffset ->
                            val width = size.width
                            val height = size.height
                            // Find closest skill
                            var closest: SkillNode? = null
                            var minDistance = Float.MAX_VALUE
                            skills.forEach { skill ->
                                val sx = skill.xRatio * width
                                val sy = skill.yRatio * height
                                val dist = kotlin.math.hypot((sx - tapOffset.x), (sy - tapOffset.y))
                                if (dist < 80f && dist < minDistance) {
                                    minDistance = dist
                                    closest = skill
                                }
                            }
                            closest?.let { selectedSkillId = it.id }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw background ambient constellation lines
                skills.forEach { skill ->
                    val start = Offset(skill.xRatio * width, skill.yRatio * height)
                    skill.connections.forEach { connId ->
                        val target = skills.firstOrNull { it.id == connId }
                        if (target != null && target.id > skill.id) {
                            val end = Offset(target.xRatio * width, target.yRatio * height)
                            val isSelectedConn = (skill.id == selectedSkillId || target.id == selectedSkillId)

                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = if (isSelectedConn) {
                                        listOf(CosmicCyan, ElectricViolet)
                                    } else {
                                        listOf(CometBlue.copy(alpha = 0.25f), CosmicPurple.copy(alpha = 0.2f))
                                    }
                                ),
                                start = start,
                                end = end,
                                strokeWidth = if (isSelectedConn) 2.2f else 1.0f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }

                // Draw Nodes
                skills.forEach { skill ->
                    val center = Offset(skill.xRatio * width, skill.yRatio * height)
                    val isSelected = (skill.id == selectedSkillId)

                    // Outer halo glow
                    drawCircle(
                        color = if (isSelected) CosmicCyan.copy(alpha = 0.35f * pulse) else ElectricViolet.copy(alpha = 0.15f),
                        radius = if (isSelected) 18f * pulse else 10f,
                        center = center
                    )

                    // Core star
                    drawCircle(
                        color = if (isSelected) StarWhite else CometBlue,
                        radius = if (isSelected) 6.5f else 4f,
                        center = center
                    )
                }
            }

            // Overlay Node Pills (Dynamically positioned relative to container width)
            skills.forEach { skill ->
                val isSelected = (skill.id == selectedSkillId)
                val maxW = (canvasWidthPx.value - 60f).coerceAtLeast(10f)
                val maxH = (canvasHeightPx.value - 36f).coerceAtLeast(10f)
                val pillX = (skill.xRatio * maxW).coerceIn(6f, maxW).dp
                val pillY = (skill.yRatio * maxH).coerceIn(6f, maxH).dp

                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = pillX, top = pillY)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CosmicPurple.copy(alpha = 0.85f) else Color(0xCC0E1332))
                        .border(
                            0.8.dp,
                            if (isSelected) CosmicCyan else CosmicBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedSkillId = skill.id }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = skill.name,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StarWhite else SoftLavender,
                        maxLines = 1
                    )
                }
            }
        }

        // Active Selected Node Card
        if (selectedSkill != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F1538))
                    .border(0.8.dp, CosmicCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(CosmicCyan, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedSkill.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Domain: ${selectedSkill.category}",
                            fontSize = 11.sp,
                            color = SoftLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricViolet.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = selectedSkill.level,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CometBlue
                    )
                }
            }
        }
    }
}
