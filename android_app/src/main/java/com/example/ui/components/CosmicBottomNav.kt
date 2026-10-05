package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceGlass
import com.example.ui.theme.GradientGlassBorder
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.viewmodel.NavDestination

private data class NavItemData(
    val destination: NavDestination,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Magnificent Executive Navigation Dock for UniSpaceX.
 * Features:
 * - Floating Frosted Obsidian capsule with crisp 1px hairline border
 * - 5 balanced high-touch-target destinations (Home, Explore, Crew, Chat, Hub)
 * - Animated active pill highlight with high-contrast sapphire glow
 * - Full edge-to-edge support with navigationBarsPadding
 */
@Composable
fun CosmicBottomNav(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItemData(NavDestination.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItemData(NavDestination.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
        NavItemData(NavDestination.COLLABORATE, "Crew", Icons.Filled.Groups, Icons.Outlined.Groups),
        NavItemData(NavDestination.MESSAGES, "Chat", Icons.Filled.Forum, Icons.Outlined.Forum),
        NavItemData(NavDestination.MISSION_CONTROL, "Hub", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        val isNarrowNav = maxWidth < 380.dp

        // Floating Island Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(horizontal = if (isNarrowNav) 8.dp else 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CosmicSurfaceGlass)
                .border(
                    width = 1.dp,
                    brush = GradientGlassBorder,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = if (isNarrowNav) 4.dp else 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = (currentDestination == item.destination)

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) StarWhite else TextMuted,
                        animationSpec = tween(durationMillis = 150),
                        label = "icon_color"
                    )

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) ElectricViolet.copy(alpha = 0.25f) else Color.Transparent,
                        animationSpec = tween(durationMillis = 150),
                        label = "pill_bg"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(item.destination) }
                            .padding(vertical = 2.dp)
                            .testTag("nav_item_${item.destination.name.lowercase()}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(26.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(pillBgColor)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) CosmicBorderGlow else Color.Transparent,
                                    shape = RoundedCornerShape(13.dp)
                                )
                                .padding(horizontal = if (isNarrowNav) 6.dp else 10.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = iconTint,
                                modifier = Modifier.size(if (isNarrowNav) 17.dp else 19.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = item.label,
                            fontSize = if (isNarrowNav) 10.sp else 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) StarWhite else TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-End Cosmic Navigation Rail for Tablets, Foldables (Unfolded) and Landscape Mode.
 * Adheres strictly to Material Design 3 Canonical Adaptive layouts:
 * - 80dp wide obsidian column pinned to the left edge
 * - Branded top avatar / USX monogram
 * - Touch targets >= 48dp with accessible labels and active glowing pills
 * - Action cluster: Cart counter badge, Create FAB button, and System Hub
 */
@Composable
fun CosmicNavigationRail(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onCreateClick: () -> Unit,
    onCartClick: () -> Unit = {},
    cartItemCount: Int = 0,
    userRoleBadge: String = "🎓",
    onDatabaseClick: () -> Unit = {},
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItemData(NavDestination.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItemData(NavDestination.EXPLORE, "Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
        NavItemData(NavDestination.COLLABORATE, "Crew", Icons.Filled.Groups, Icons.Outlined.Groups),
        NavItemData(NavDestination.CAMPUS, "Universe", Icons.Filled.Public, Icons.Outlined.Public),
        NavItemData(NavDestination.MESSAGES, "Chat", Icons.Filled.Forum, Icons.Outlined.Forum),
        NavItemData(NavDestination.MISSION_CONTROL, "Hub", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    )

    Column(
        modifier = modifier
            .width(84.dp)
            .fillMaxHeight()
            .background(CosmicSurfaceGlass)
            .border(
                width = 1.dp,
                color = CosmicBorderSubtle,
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top: UniSpaceX Monogram & Role
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ElectricViolet, CosmicPurple, Color(0xFF0E1424))
                        )
                    )
                    .border(1.5.dp, CosmicCyan, CircleShape)
                    .clickable { onNavigate(NavDestination.HOME) }
                    .testTag("rail_brand_logo")
            ) {
                Text(
                    text = "USX",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = StarWhite,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userRoleBadge,
                fontSize = 12.sp
            )
        }

        // Center: Navigation Items (Scrollable if height is constrained)
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items.forEach { item ->
                val isSelected = (currentDestination == item.destination)

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) StarWhite else TextMuted,
                    animationSpec = tween(durationMillis = 150),
                    label = "rail_icon_color"
                )

                val pillBgColor by animateColorAsState(
                    targetValue = if (isSelected) ElectricViolet.copy(alpha = 0.35f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 150),
                    label = "rail_pill_bg"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 56.dp, minHeight = 50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.destination) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("rail_item_${item.destination.name.lowercase()}")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(30.dp)
                            .width(52.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(pillBgColor)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) CosmicBorderGlow else Color.Transparent,
                                shape = RoundedCornerShape(15.dp)
                            )
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) StarWhite else TextMuted
                    )
                }
            }
        }

        // Bottom Action Cluster: Cart, Create FAB & Database Inspector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            // Cart with Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(CosmicSurfaceCard)
                    .border(1.dp, CosmicBorderSubtle, CircleShape)
                    .clickable { onCartClick() }
                    .testTag("rail_cart_button")
            ) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = CosmicCyan,
                                contentColor = Color(0xFF070B14)
                            ) {
                                Text(
                                    text = "$cartItemCount",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Shopping Bag",
                        tint = if (cartItemCount > 0) CosmicCyan else StarWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Quick Create Floating Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(ElectricViolet, CometBlue)
                        )
                    )
                    .border(1.dp, CosmicBorderGlow, CircleShape)
                    .clickable { onCreateClick() }
                    .testTag("rail_create_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create",
                    tint = StarWhite,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Sync / Database Tool
            IconButton(
                onClick = onDatabaseClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("rail_db_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Database Inspector",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (onSettingsClick != null) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("rail_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

