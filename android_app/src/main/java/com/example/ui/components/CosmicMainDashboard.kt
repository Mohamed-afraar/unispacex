package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Business
import com.example.model.UserRole
import com.example.model.VerificationType
import com.example.ui.theme.*
import com.example.viewmodel.NavDestination
import com.example.viewmodel.UniSpaceUiState

/**
 * Main Cosmic Dashboard Component:
 * - Ambient Cosmic Orbital Header
 * - User Role Navigation Hub Cards (Student Orbit, Creator / Seller Orbit, Admin Mission Control)
 * - Featured Student Businesses in Orbit showcase with rich metrics, verified badges, and quick actions
 * - Fast Action Constellation for Launching ventures, posting services, exploring planets
 */
@Composable
fun CosmicMainDashboard(
    uiState: UniSpaceUiState,
    onNavigate: (NavDestination) -> Unit,
    onSelectBusiness: (Business) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cosmic_main_dashboard")
    ) {
        // 1. Cosmic Command Header & Role Badging Banner
        DashboardCosmicHeader(
            currentUserRole = uiState.currentUserRole,
            userProfileName = uiState.userProfile.name,
            collegeName = uiState.userProfile.college,
            isLoggedIn = uiState.isLoggedIn,
            onMissionControlClick = { onNavigate(NavDestination.MISSION_CONTROL) }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Featured Student Businesses in Orbit Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "☄️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Featured Student Businesses",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                }
                Text(
                    text = "Handcrafted tech, creative studios & campus ventures",
                    fontSize = 11.sp,
                    color = SoftLavender
                )
            }

            Text(
                text = "View All ➔",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicCyan,
                modifier = Modifier
                    .clickable { onNavigate(NavDestination.EXPLORE) }
                    .padding(4.dp)
                    .testTag("featured_businesses_view_all")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Featured Businesses Horizontal Orbit Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.businesses) { business ->
                FeaturedBusinessCosmicCard(
                    business = business,
                    onClick = { onSelectBusiness(business) },
                    onQuickOrder = { onSelectBusiness(business) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Quick Launch Constellation Card
        CosmicLaunchActionCard(
            currentUserRole = uiState.currentUserRole,
            onCreateClick = onCreateClick,
            onExploreClick = { onNavigate(NavDestination.EXPLORE) },
            onCollabClick = { onNavigate(NavDestination.COLLABORATE) }
        )
    }
}

@Composable
private fun DashboardCosmicHeader(
    currentUserRole: UserRole,
    userProfileName: String,
    collegeName: String,
    isLoggedIn: Boolean,
    onMissionControlClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF151D32),
                        Color(0xFF101728),
                        Color(0xFF0B101D)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        CosmicPurple.copy(alpha = glowAlpha),
                        CometBlue.copy(alpha = glowAlpha * 0.7f),
                        CosmicCyan.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(ElectricViolet, RoyalSapphire, Color(0xFF0C1220))
                                )
                            )
                            .border(1.2.dp, CosmicCyan, CircleShape)
                    ) {
                        Text(text = currentUserRole.badge, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isLoggedIn) "Welcome back, $userProfileName" else "UniSpaceX Campus Galaxy",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Text(
                            text = "$collegeName · ${currentUserRole.title}",
                            fontSize = 11.sp,
                            color = CosmicCyan
                        )
                    }
                }

                // Quick Mission Control Pill
                Surface(
                    onClick = onMissionControlClick,
                    shape = RoundedCornerShape(12.dp),
                    color = CosmicPurple.copy(alpha = 0.3f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CosmicCyan, CometBlue))),
                    modifier = Modifier.testTag("dashboard_mission_control_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🛰️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mission Control",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-status metrics banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CosmicHeaderStat(label = "Verified Creators", value = "420+")
                CosmicHeaderStat(label = "Campus Ventures", value = "85+")
                CosmicHeaderStat(label = "Avg Rating", value = "⭐ 4.9")
                CosmicHeaderStat(label = "Response Rate", value = "⚡ 98%")
            }
        }
    }
}

@Composable
private fun CosmicHeaderStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = StarWhite
        )
        Text(
            text = label,
            fontSize = 9.sp,
            color = SoftLavender
        )
    }
}

@Composable
private fun RoleNavigationCardsSection(
    activeRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    onNavigate: (NavDestination) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Student Orbit
        RolePortalCard(
            role = UserRole.STUDENT,
            title = "Student",
            subtitle = "Hire & Explore",
            badge = "🎓",
            accentColor = CosmicPurple,
            isActive = activeRole == UserRole.STUDENT,
            onClick = {
                onRoleSelected(UserRole.STUDENT)
                onNavigate(NavDestination.EXPLORE)
            },
            modifier = Modifier.weight(1f)
        )

        // 2. Creator / Seller Orbit
        RolePortalCard(
            role = UserRole.SELLER,
            title = "Creator",
            subtitle = "Sell & Deliver",
            badge = "🛍️",
            accentColor = CometBlue,
            isActive = activeRole == UserRole.SELLER,
            onClick = {
                onRoleSelected(UserRole.SELLER)
                onNavigate(NavDestination.MISSION_CONTROL)
            },
            modifier = Modifier.weight(1f)
        )

        // 3. Admin Mission Control Orbit
        RolePortalCard(
            role = UserRole.ADMIN,
            title = "Admin",
            subtitle = "Universe Hub",
            badge = "🛡️",
            accentColor = Color(0xFFFFB300),
            isActive = activeRole == UserRole.ADMIN,
            onClick = {
                onRoleSelected(UserRole.ADMIN)
                onNavigate(NavDestination.MISSION_CONTROL)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RolePortalCard(
    role: UserRole,
    title: String,
    subtitle: String,
    badge: String,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) CosmicSurfaceElevated else CosmicSurfaceCard
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                if (isActive) listOf(accentColor, CosmicBorderGlow)
                else listOf(CosmicBorder, Color.Transparent)
            )
        ),
        modifier = modifier
            .height(104.dp)
            .testTag("role_portal_${role.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.25f))
                        .border(1.dp, accentColor.copy(alpha = 0.5f), CircleShape)
                ) {
                    Text(text = badge, fontSize = 16.sp)
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accentColor.copy(alpha = 0.25f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = SoftLavender,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Featured Student Business Card with rich cosmic visuals,
 * glowing constellation accents, service tags, and instant order request buttons.
 */
@Composable
fun FeaturedBusinessCosmicCard(
    business: Business,
    onClick: () -> Unit,
    onQuickOrder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSecondary),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(CosmicBorderGlow.copy(alpha = 0.7f), CosmicBorder)
            )
        ),
        modifier = modifier
            .width(260.dp)
            .testTag("featured_business_${business.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Business Header with Category and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicPurple.copy(alpha = 0.2f))
                        .border(0.8.dp, CosmicCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = business.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD166),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${business.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = " (${business.reviewCount})",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Business Name & Tagline
            Text(
                text = business.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = StarWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = business.tagline,
                fontSize = 11.sp,
                color = SoftLavender,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Founder & College
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(ElectricViolet.copy(alpha = 0.4f))
                ) {
                    Text(text = "🚀", fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${business.ownerName} · ${business.college}",
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Badge Row
            CosmicBadgeRow(badges = business.badges)

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Metrics & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${business.completedOrders} orders done",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CometBlue
                    )
                    Text(
                        text = "${business.responseRate}% reply rate",
                        fontSize = 9.sp,
                        color = SoftLavender
                    )
                }

                Button(
                    onClick = onQuickOrder,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Explore ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmicLaunchActionCard(
    currentUserRole: UserRole,
    onCreateClick: () -> Unit,
    onExploreClick: () -> Unit,
    onCollabClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(
                listOf(CosmicCyan.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.6f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🚀 Launch Your Campus Venture",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = "Sell products, offer freelance services, or recruit a student team in minutes.",
                        fontSize = 11.sp,
                        color = SoftLavender,
                        lineHeight = 15.sp
                    )
                }

                Button(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("dashboard_launch_venture_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create",
                        tint = StarWhite,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Launch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Shortcut Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickShortcutButton(
                    title = "Browse Talent",
                    icon = Icons.Default.School,
                    onClick = onExploreClick,
                    modifier = Modifier.weight(1f)
                )

                QuickShortcutButton(
                    title = "Find Crews",
                    icon = Icons.Default.Handshake,
                    onClick = onCollabClick,
                    modifier = Modifier.weight(1f)
                )

                QuickShortcutButton(
                    title = "Shop Products",
                    icon = Icons.Default.ShoppingBag,
                    onClick = onExploreClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickShortcutButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = CosmicSurfaceElevated,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CosmicBorder, CosmicCyan.copy(alpha = 0.3f)))
        ),
        modifier = modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CosmicCyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = StarWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
