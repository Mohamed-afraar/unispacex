package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.model.CampusFeedPost
import com.example.model.CampusPlanet
import com.example.model.FeedCategory
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.UniSpaceUiState

@Composable
fun CampusUniverseScreen(
    uiState: UniSpaceUiState,
    onSelectPlanet: (CampusPlanet) -> Unit,
    onLikePost: (String) -> Unit,
    onToggleRegisterEvent: ((String) -> Unit)? = null,
    onCreatePostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFeedCategory by remember { mutableStateOf(FeedCategory.ALL) }

    val filteredFeed = uiState.feedPosts.filter { post ->
        selectedFeedCategory == FeedCategory.ALL || post.category == selectedFeedCategory
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gridColumns = if (maxWidth >= 720.dp) 2 else 1

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("campus_universe_screen"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
        // Campus Planets Banner
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = CosmicCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Campus Universe",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                }
                Text(
                    text = "Each college operates as a self-contained campus planet with active commerce",
                    fontSize = 12.sp,
                    color = SoftLavender
                )
            }
        }

        // Horizontal Carousel of Campus Planets
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.campusPlanets) { planet ->
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(Color(planet.colorHex).copy(alpha = 0.25f), CosmicSecondary)
                                )
                            )
                            .border(
                                1.dp,
                                Color(planet.accentHex).copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { onSelectPlanet(planet) }
                            .padding(16.dp)
                            .testTag("planet_card_${planet.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color(planet.colorHex).copy(alpha = 0.4f), CircleShape)
                                            .border(1.2.dp, Color(planet.accentHex), CircleShape)
                                    ) {
                                        Text(text = "🪐", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = planet.shortName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                        Text(
                                            text = "${planet.studentCount} Students",
                                            fontSize = 11.sp,
                                            color = CosmicCyan
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(planet.accentHex).copy(alpha = 0.2f))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Planet",
                                        fontSize = 10.sp,
                                        color = Color(planet.accentHex),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = planet.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StarWhite
                            )

                            Text(
                                text = planet.description,
                                fontSize = 11.sp,
                                color = SoftLavender,
                                maxLines = 2
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 4 Key Planet Metrics
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0C1220))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "${planet.businessCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                    Text(text = "Businesses", fontSize = 9.sp, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "${planet.serviceCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                                    Text(text = "Services", fontSize = 9.sp, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "${planet.productCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CometBlue)
                                    Text(text = "Products", fontSize = 9.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Campus Events & Hackathons Hub
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Campus Events & Hackathons 🎪",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Text(
                            text = "Flagship collegiate symposiums, hackathons & pitch days",
                            fontSize = 11.sp,
                            color = SoftLavender
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.campusEvents) { event ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .testTag("event_card_${event.id}"),
                            colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(ElectricViolet.copy(alpha = 0.2f))
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = event.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CosmicCyan
                                        )
                                    }
                                    event.prizePool?.let { prize ->
                                        Text(
                                            text = "🏆 $prize",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = event.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite,
                                    maxLines = 2,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "🎓 ${event.college} · 📍 ${event.venue}",
                                    fontSize = 11.sp,
                                    color = SoftLavender,
                                    maxLines = 1
                                )

                                Text(
                                    text = "📅 ${event.date} · ⏰ ${event.time}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${event.registeredCount} registered",
                                        fontSize = 11.sp,
                                        color = CosmicCyan,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    androidx.compose.material3.Button(
                                        onClick = { onToggleRegisterEvent?.invoke(event.id) },
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = if (event.isRegistered) CosmicSurfaceElevated else CosmicPurple,
                                            contentColor = if (event.isRegistered) Color(0xFF10B981) else StarWhite
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = if (event.isRegistered) "Pass Confirmed ✓" else "RSVP Pass 🎟️",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Campus Feed Section
        item {
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "What's Happening in Your Space?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "Real-time drops, hardware milestones & venture updates",
                        fontSize = 11.sp,
                        color = SoftLavender
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicPurple)
                        .clickable { onCreatePostClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+ Broadcast",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feed Category Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedCategory.values().forEach { cat ->
                    val isSelected = selectedFeedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) ElectricViolet else CosmicSurfaceCard)
                            .border(
                                0.8.dp,
                                if (isSelected) CosmicCyan else CosmicBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFeedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = cat.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) CosmicCyan else SoftLavender
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Feed Post Items
        if (filteredFeed.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(CosmicBorder, CometBlue.copy(alpha = 0.3f)))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📡", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Broadcasts in Orbit Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Share campus milestone updates, project drops, or tech breakthroughs with your fellow students!",
                            fontSize = 12.sp,
                            color = SoftLavender,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        androidx.compose.material3.Button(
                            onClick = onCreatePostClick,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("+ Create First Broadcast", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (gridColumns == 1) {
            items(filteredFeed) { post ->
                CampusFeedPostCard(
                    post = post,
                    onLikePost = onLikePost,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        } else {
            items(filteredFeed.chunked(gridColumns)) { rowChunk ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (post in rowChunk) {
                        Box(modifier = Modifier.weight(1f)) {
                            CampusFeedPostCard(
                                post = post,
                                onLikePost = onLikePost,
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

@Composable
private fun CampusFeedPostCard(
    post: CampusFeedPost,
    onLikePost: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("feed_post_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, ElectricViolet.copy(alpha = 0.2f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(CosmicPurple, CometBlue)),
                                shape = CircleShape
                            )
                    ) {
                        Text(
                            text = post.authorName.take(1),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = post.authorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Text(
                            text = "${post.authorRole} · 🎓 ${post.college}",
                            fontSize = 11.sp,
                            color = SoftLavender
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CosmicSurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = post.timestamp,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.content,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = StarWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.tag,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = CosmicCyan
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onLikePost(post.id) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color(0xFFF43F5E) else SoftLavender,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${post.likes}",
                        fontSize = 12.sp,
                        color = if (post.isLiked) Color(0xFFF43F5E) else SoftLavender
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = SoftLavender,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.commentsCount}",
                        fontSize = 12.sp,
                        color = SoftLavender
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CosmicPurple.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = post.category.label,
                        fontSize = 10.sp,
                        color = ElectricViolet,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
