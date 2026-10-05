package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.CosmicSurfaceGlass
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted

/**
 * Magnificent Executive Top Bar for UniSpaceX.
 * Features:
 * - High-clarity frosted glass surface with subtle hair-line divider
 * - Distinctive USX brand lockup with crisp typography
 * - Tactile quick-search button with command styling
 * - Clean notification bell with precision counter
 * - Quick "Launch" venture pill with high-contrast sapphire styling
 * - Seamless profile & authentication triggers
 */
@Composable
fun CosmicHeader(
    onLogoClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onCartClick: (() -> Unit)? = null,
    cartItemCount: Int = 0,
    onSettingsClick: (() -> Unit)? = null,
    onSignOutClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onDatabaseClick: (() -> Unit)? = null,
    isLoggedIn: Boolean = true,
    userRoleBadge: String = "🎓",
    unreadNotificationsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(CosmicSurfaceGlass)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        CosmicBorderSubtle,
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
            )
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        val isNarrow = maxWidth < 440.dp
        val isVeryNarrow = maxWidth < 360.dp
        val showTagline = maxWidth >= 480.dp
        val showDbInspector = maxWidth >= 520.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand lockup: USX with UniSpace and collegiate node badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onLogoClick() }
                    .testTag("brand_logo_button")
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                UsxAppLogoBadge(size = 32.dp)

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "USX",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = StarWhite
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x1F2563EB))
                                .border(0.8.dp, Color(0x3360A5FA), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SAEC QUAD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicCyan,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                    if (showTagline) {
                        Text(
                            text = "Collegiate Commerce & Studios",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted,
                            letterSpacing = 0.2.sp
                        )
                    }
                }
            }

            // Right Action Constellation: Consolidated & Clean on Mobile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Secondary tools shown only on wide displays to prevent mobile overcrowding
                if (!isNarrow) {
                    // Search shortcut
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onSearchClick() }
                            .testTag("header_search_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CosmicSurfaceCard)
                                .border(1.dp, CosmicBorderSubtle, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = SoftLavender,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Create / Launch button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(ElectricViolet, CosmicPurple)
                                )
                            )
                            .clickable { onCreateClick() }
                            .padding(horizontal = 10.dp)
                            .testTag("header_create_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                tint = StarWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Launch",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarWhite
                            )
                        }
                    }

                    // Database inspector (Wide only)
                    if (onDatabaseClick != null && showDbInspector) {
                        IconButton(
                            onClick = onDatabaseClick,
                            modifier = Modifier.size(36.dp).testTag("header_db_inspector_button")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CosmicSurfaceCard)
                                    .border(1.dp, CosmicBorderSubtle, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Inspect SQLite Database",
                                    tint = CosmicCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // 1. Notification Bell (Essential on all screens)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onNotificationsClick() }
                        .testTag("header_notifications_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceCard)
                            .border(1.dp, CosmicBorderSubtle, CircleShape)
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = NeonPink,
                                        contentColor = StarWhite,
                                        modifier = Modifier.size(13.dp)
                                    ) {
                                        Text(
                                            text = unreadNotificationsCount.toString(),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = SoftLavender,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // 2. Campus Bag / Cart Button
                if (onCartClick != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onCartClick() }
                            .testTag("header_cart_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CosmicSurfaceCard)
                                .border(1.dp, CosmicBorderSubtle, CircleShape)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(
                                            containerColor = CosmicCyan,
                                            contentColor = Color(0xFF07090E),
                                            modifier = Modifier.size(13.dp)
                                        ) {
                                            Text(
                                                text = cartItemCount.toString(),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Campus Bag",
                                    tint = if (cartItemCount > 0) CosmicCyan else SoftLavender,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // 3. User Identity / Sign In Pill
                if (isLoggedIn) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick() }
                            .testTag("header_profile_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CosmicSurfaceElevated)
                                .border(1.2.dp, CosmicBorderGlow, CircleShape)
                        ) {
                            Text(
                                text = userRoleBadge.ifBlank { "🎓" },
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(ElectricViolet, CosmicPurple)
                                )
                            )
                            .clickable { onSignInClick() }
                            .padding(horizontal = 12.dp)
                            .testTag("header_login_button")
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    }
                }
            }
        }
    }
}
