package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CollaborationRequest
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSuccess
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.UniSpaceUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollaborateScreen(
    uiState: UniSpaceUiState,
    onToggleJoin: (String) -> Unit,
    onSelectCollab: (CollaborationRequest) -> Unit,
    onApplyCrew: ((CollaborationRequest) -> Unit)? = null,
    onPostRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val roles = listOf("All", "Video Editors", "Co-founders", "Developers", "Designers", "Hardware/PCB", "Photographers")

    val filteredList = uiState.collaborations.filter { collab ->
        if (selectedFilter == "All") true
        else when (selectedFilter) {
            "Video Editors" -> collab.skillsNeeded.any { it.contains("Video", ignoreCase = true) }
            "Co-founders" -> collab.title.contains("Co-founder", ignoreCase = true)
            "Developers" -> collab.skillsNeeded.any { it.contains("Kotlin") || it.contains("Node") || it.contains("Full-Stack") }
            "Designers" -> collab.skillsNeeded.any { it.contains("UI") || it.contains("Design") }
            "Hardware/PCB" -> collab.skillsNeeded.any { it.contains("PCB") || it.contains("Hardware") }
            "Photographers" -> collab.skillsNeeded.any { it.contains("Photo", ignoreCase = true) }
            else -> true
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth
        val gridColumns = if (availableWidth >= 720.dp) 2 else 1
        val isCompactBanner = availableWidth < 480.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("collaborate_screen"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
        // Banner Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF151D32), Color(0xFF0F1626))
                        )
                    )
                    .border(1.dp, CometBlue.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    if (isCompactBanner) {
                        Column {
                            Text(
                                text = "🤝 FIND YOUR CREW",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = CosmicCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Great ideas need great people.",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Assemble hackathon teams, recruit co-founders, or find specialized student freelancers.",
                                fontSize = 12.sp,
                                color = SoftLavender
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onPostRequestClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricViolet,
                                    contentColor = StarWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("post_crew_request_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Post Bounty", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🤝 FIND YOUR CREW",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CosmicCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Great ideas need great people.",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Assemble hackathon teams, recruit co-founders, or find specialized student freelancers.",
                                    fontSize = 12.sp,
                                    color = SoftLavender
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = onPostRequestClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricViolet,
                                    contentColor = StarWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("post_crew_request_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Post Bounty", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Filter Pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roles.forEach { role ->
                    val isSelected = selectedFilter == role
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricViolet else CosmicSurfaceCard)
                            .border(
                                0.8.dp,
                                if (isSelected) CosmicCyan else CosmicBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFilter = role }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = role,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) StarWhite else SoftLavender
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (uiState.crewApplications.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "MY CREW APPLICATIONS (${uiState.crewApplications.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftLavender,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.crewApplications.take(3).forEach { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.projectTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = StarWhite
                                    )
                                    Text(
                                        text = "Role: ${app.chosenSkill} · ${app.timestamp}",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(app.status.badgeColorHex).copy(alpha = 0.2f))
                                        .border(0.8.dp, Color(app.status.badgeColorHex), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = app.status.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(app.status.badgeColorHex)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Collaboration Cards
        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CosmicBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🤝", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Open Crew Recruits in Orbit",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Looking for co-founders, hackathon builders, or project collaborators? Start the first recruitment call!",
                            fontSize = 12.sp,
                            color = SoftLavender,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onPostRequestClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Post Collaboration Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (gridColumns == 1) {
            items(filteredList) { collab ->
                CollaborateCard(
                    collab = collab,
                    onSelectCollab = onSelectCollab,
                    onApplyCrew = onApplyCrew,
                    onToggleJoin = onToggleJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        } else {
            items(filteredList.chunked(gridColumns)) { rowChunk ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (collab in rowChunk) {
                        Box(modifier = Modifier.weight(1f)) {
                            CollaborateCard(
                                collab = collab,
                                onSelectCollab = onSelectCollab,
                                onApplyCrew = onApplyCrew,
                                onToggleJoin = onToggleJoin,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    repeat(gridColumns - rowChunk.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CollaborateCard(
    collab: CollaborationRequest,
    onSelectCollab: (CollaborationRequest) -> Unit,
    onApplyCrew: ((CollaborationRequest) -> Unit)?,
    onToggleJoin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onSelectCollab(collab) }
            .testTag("collab_card_${collab.id}"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, CometBlue.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricViolet.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = collab.projectType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricViolet
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = collab.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "By ${collab.organizer} · 🎓 ${collab.college}",
                        fontSize = 11.sp,
                        color = SoftLavender
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Budget Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicSurfaceElevated)
                        .border(0.8.dp, CosmicCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Budget", fontSize = 9.sp, color = TextMuted)
                        Text(
                            text = "₹${collab.budget}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = collab.description,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = StarWhite.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Skills Tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                collab.skillsNeeded.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CosmicSurfaceElevated)
                            .border(0.6.dp, CosmicBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 10.sp,
                            color = SoftLavender
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom info & Join Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${collab.deadlineDays}d",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = CosmicCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${collab.applicantsCount} in orbit",
                        fontSize = 11.sp,
                        color = CosmicCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (onApplyCrew != null && !collab.isJoined) {
                            onApplyCrew(collab)
                        } else {
                            onToggleJoin(collab.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (collab.isJoined) CosmicSurfaceElevated else ElectricViolet,
                        contentColor = if (collab.isJoined) CosmicSuccess else StarWhite
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    if (collab.isJoined) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "Applied", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text(text = "Apply 🚀", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
