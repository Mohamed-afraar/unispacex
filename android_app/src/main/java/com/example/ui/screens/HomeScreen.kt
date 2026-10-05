package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Business
import com.example.model.CampusPlanet
import com.example.model.CollaborationRequest
import com.example.model.Product
import com.example.model.Service
import com.example.model.Student
import com.example.ui.components.CosmicBadgeRow
import com.example.ui.components.UsxAppLogoBadge
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.NavDestination
import com.example.viewmodel.UniSpaceUiState

@Composable
fun HomeScreen(
    uiState: UniSpaceUiState,
    onNavigate: (NavDestination) -> Unit,
    onSearchSuggestion: (String) -> Unit,
    onSelectBusiness: (Business) -> Unit,
    onSelectStudent: (Student) -> Unit,
    onSelectProduct: (Product) -> Unit,
    onSelectService: (Service) -> Unit,
    onSelectCollab: (CollaborationRequest) -> Unit,
    onSelectPlanet: (CampusPlanet) -> Unit,
    onCreateClick: () -> Unit,
    onSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val quickSearches = listOf(
        "PCB Design",
        "Custom Tees",
        "Figma UI",
        "Brownies",
        "Python Tutor",
        "Quadcopter",
        "VLSI Core",
        "Video Editing"
    )

    var selectedCategoryFilter by remember { mutableStateOf("All") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Atmosphere Bento Header & Live Campus Telemetry
        item {
            CampusEcosystemHeroCard(
                uiState = uiState,
                onExploreClick = { onNavigate(NavDestination.EXPLORE) },
                onProfileClick = { onNavigate(NavDestination.MISSION_CONTROL) }
            )
        }

        // 2. High-Performance Tactile Search Capsule & Live Chips
        item {
            HeroSearchBar(
                quickSearches = quickSearches,
                onSearchClick = { onNavigate(NavDestination.EXPLORE) },
                onChipClick = onSearchSuggestion
            )
        }

        // 3. Category Spectrum Ribbon
        item {
            CategorySpectrumRibbon(
                selectedCategory = selectedCategoryFilter,
                onSelectCategory = { cat ->
                    selectedCategoryFilter = cat
                    if (cat != "All") {
                        onSearchSuggestion(cat)
                    }
                }
            )
        }

        // 4. Premier Student Studio Spotlight (Bento Feature)
        item {
            val spotlightBiz = uiState.businesses.firstOrNull()
            spotlightBiz?.let { biz ->
                PremierStudioSpotlight(
                    business = biz,
                    onExploreStore = { onSelectBusiness(biz) }
                )
            }
        }

        // 5. Trending Products in Orbit (High-Resolution Visual Cards)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Trending Products in Orbit",
                subtitle = "Student-engineered dev kits, custom campus apparel & artisan treats",
                actionText = if (uiState.products.isNotEmpty()) "Shop All ➔" else "+ Add",
                onAction = { if (uiState.products.isNotEmpty()) onNavigate(NavDestination.EXPLORE) else onCreateClick() }
            )
            if (uiState.products.isEmpty()) {
                EmptyOrbitSectionCard(
                    icon = Icons.Default.ShoppingBag,
                    title = "No Products in Orbit Yet",
                    subtitle = "Be the first student seller to launch custom apparel, electronics or project gear.",
                    actionText = "+ Drop Product",
                    onAction = onCreateClick
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.products) { product ->
                        ProductOrbitCard(
                            product = product,
                            onClick = { onSelectProduct(product) }
                        )
                    }
                }
            }
        }

        // 6. Top Campus Talent & Freelance Builders
        item {
            Spacer(modifier = Modifier.height(26.dp))
            SectionHeader(
                title = "Verified Campus Talent",
                subtitle = "Hire high-caliber student engineers, designers & tech tutors",
                actionText = if (uiState.services.isNotEmpty()) "Browse Talent ➔" else "+ Offer",
                onAction = { if (uiState.services.isNotEmpty()) onNavigate(NavDestination.EXPLORE) else onCreateClick() }
            )
            if (uiState.services.isEmpty()) {
                EmptyOrbitSectionCard(
                    icon = Icons.Default.FlashOn,
                    title = "No Freelance Services Yet",
                    subtitle = "Offer your skills in UI/UX design, PCB fabrication, code tutoring, or photography.",
                    actionText = "+ Offer Service",
                    onAction = onCreateClick
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.services) { service ->
                        ServiceOrbitCard(
                            service = service,
                            onClick = { onSelectService(service) }
                        )
                    }
                }
            }
        }

        // 7. Student-Founded Ventures in Orbit
        item {
            Spacer(modifier = Modifier.height(26.dp))
            SectionHeader(
                title = "Ventures in Orbit",
                subtitle = "Registered student studios shaping university commerce",
                actionText = if (uiState.businesses.isNotEmpty()) "View All ➔" else "+ Launch",
                onAction = { if (uiState.businesses.isNotEmpty()) onNavigate(NavDestination.EXPLORE) else onCreateClick() }
            )
            if (uiState.businesses.isEmpty()) {
                EmptyOrbitSectionCard(
                    icon = Icons.Default.RocketLaunch,
                    title = "No Registered Ventures Yet",
                    subtitle = "Register your campus startup, creative studio, or student business.",
                    actionText = "+ Launch Venture",
                    onAction = onCreateClick
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.businesses) { business ->
                        BusinessOrbitCard(
                            business = business,
                            onClick = { onSelectBusiness(business) }
                        )
                    }
                }
            }
        }

        // 8. Open Collaborations & Project Crew Recruitment
        item {
            Spacer(modifier = Modifier.height(26.dp))
            SectionHeader(
                title = "Find Your Project Crew",
                subtitle = "Join collegiate hackathon teams, research labs & venture spinouts",
                actionText = if (uiState.collaborations.isNotEmpty()) "Explore Crew ➔" else "+ Post",
                onAction = { if (uiState.collaborations.isNotEmpty()) onNavigate(NavDestination.COLLABORATE) else onCreateClick() }
            )
            if (uiState.collaborations.isEmpty()) {
                EmptyOrbitSectionCard(
                    icon = Icons.Default.Handshake,
                    title = "No Open Crew Recruits",
                    subtitle = "Assemble your hackathon team, find co-founders, or recruit student talent.",
                    actionText = "+ Post Collab",
                    onAction = onCreateClick
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.collaborations) { collab ->
                        CrewCollabCard(
                            collab = collab,
                            onClick = { onSelectCollab(collab) }
                        )
                    }
                }
            }
        }

        // 9. Collegiate Universe Hubs (Campus Planets)
        item {
            Spacer(modifier = Modifier.height(26.dp))
            SectionHeader(
                title = "Collegiate Hubs",
                subtitle = "Discover student ventures across partner universities",
                actionText = "All Campuses ➔",
                onAction = { onNavigate(NavDestination.CAMPUS) }
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.campusPlanets) { planet ->
                    CampusPlanetCard(
                        planet = planet,
                        onClick = { onSelectPlanet(planet) }
                    )
                }
            }
        }

        // 10. Elegant Launch Callout Banner
        item {
            Spacer(modifier = Modifier.height(30.dp))
            LaunchCalloutBanner(onCreateClick = onCreateClick)
        }
    }
}

/**
 * Atmospheric Bento-Grid Hero Card
 */
@Composable
private fun CampusEcosystemHeroCard(
    uiState: UniSpaceUiState,
    onExploreClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("campus_ecosystem_hero"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSecondary),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    CosmicBorderGlow.copy(alpha = 0.35f),
                    CosmicBorderSubtle
                )
            )
        )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrowHero = maxWidth < 400.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF131B2E),
                                Color(0xFF0D1424),
                                Color(0xFF090E1A)
                            )
                        )
                    )
                    .padding(if (isNarrowHero) 14.dp else 18.dp)
            ) {
                // Top Row: Collegiate Credential & Live Quad Node
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        if (uiState.isLoggedIn) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF13203C))
                                    .border(1.2.dp, Color(0x4038BDF8), CircleShape)
                                    .clickable { onProfileClick() }
                            ) {
                                if (!uiState.userProfile.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = uiState.userProfile.avatarUrl,
                                        contentDescription = uiState.userProfile.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = uiState.userProfile.name.take(2).uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "Welcome, ${uiState.userProfile.name}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = uiState.userProfile.college.ifBlank { "SA Engineering College" },
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x1F2563EB))
                                    .border(1.dp, Color(0x3360A5FA), RoundedCornerShape(12.dp))
                                    .clickable { onExploreClick() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = CosmicCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "UniSpaceX Campus Quad",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                                Text(
                                    text = "SA Engineering College Node",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Verified Badge Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (uiState.isLoggedIn) Color(0x1E10B981) else Color(0x1F2563EB),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                if (uiState.isLoggedIn) listOf(Color(0x4410B981), Color(0x2210B981))
                                else listOf(Color(0x4438BDF8), Color(0x222563EB))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (uiState.isLoggedIn) Icons.Default.Verified else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (uiState.isLoggedIn) MintEmerald else CosmicCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isLoggedIn) uiState.currentUserRole.title.uppercase() else "ACTIVE QUAD",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isLoggedIn) MintEmerald else CosmicCyan,
                                letterSpacing = 0.3.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subtitle Statement
                Text(
                    text = "Buy. Sell. Collaborate. Grow.",
                    fontSize = if (isNarrowHero) 19.sp else 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.4).sp,
                    color = StarWhite
                )
                Text(
                    text = "The premier collegiate commerce platform connecting verified student creators, builders & studios.",
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Live Ecosystem Telemetry (Bento Stats: Clean hairlines, pure numeric precision, no emojis)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x380C1424))
                        .border(0.8.dp, CosmicBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(vertical = 11.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelemetryMetricItem(title = "CREATORS", value = "420+")
                    Box(modifier = Modifier.width(0.8.dp).height(24.dp).background(Color(0x1AFFFFFF)))
                    TelemetryMetricItem(title = "STUDIOS", value = "85+")
                    Box(modifier = Modifier.width(0.8.dp).height(24.dp).background(Color(0x1AFFFFFF)))
                    TelemetryMetricItem(title = "RATING", value = "4.9 ★")
                    Box(modifier = Modifier.width(0.8.dp).height(24.dp).background(Color(0x1AFFFFFF)))
                    TelemetryMetricItem(title = "RESPONSE", value = "98%")
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = StarWhite,
            letterSpacing = (-0.2).sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            color = TextMuted
        )
    }
}

/**
 * Modern Tactile Search Capsule & Chips
 */
@Composable
private fun HeroSearchBar(
    quickSearches: List<String>,
    onSearchClick: () -> Unit,
    onChipClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CosmicSurfaceCard)
                .border(1.dp, CosmicBorderSubtle, RoundedCornerShape(14.dp))
                .clickable { onSearchClick() }
                .padding(horizontal = 14.dp)
                .testTag("hero_search_bar"),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Search hardware, dev kits, notes, apparel...",
                        fontSize = 12.5.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x18FFFFFF))
                        .border(0.6.dp, Color(0x28FFFFFF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Browse ➔",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CometBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Curated Discovery Chips without cheese/sparkles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickSearches.forEach { query ->
                Surface(
                    onClick = { onChipClick(query) },
                    shape = RoundedCornerShape(10.dp),
                    color = CosmicSurfaceElevated,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(CosmicBorderSubtle, Color(0x14FFFFFF)))
                    )
                ) {
                    Text(
                        text = query,
                        fontSize = 11.5.sp,
                        color = SoftLavender,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

private data class CategoryRibbonItem(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Category Ribbon with Unified Color & Clean Vector Icons
 */
@Composable
private fun CategorySpectrumRibbon(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    val categories = listOf(
        CategoryRibbonItem("All", Icons.Default.Explore),
        CategoryRibbonItem("Products", Icons.Default.ShoppingBag),
        CategoryRibbonItem("Services", Icons.Default.Handshake),
        CategoryRibbonItem("Tech", Icons.Default.Memory),
        CategoryRibbonItem("Creative", Icons.Default.Brush),
        CategoryRibbonItem("Apparel", Icons.Default.LocalMall),
        CategoryRibbonItem("Treats", Icons.Default.Cake),
        CategoryRibbonItem("Academic", Icons.Default.School)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { item ->
            val isSelected = (selectedCategory == item.name)
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) ElectricViolet else CosmicSurfaceCard,
                label = "cat_bg"
            )
            val iconTint = if (isSelected) StarWhite else TextMuted
            val textColor = if (isSelected) StarWhite else SoftLavender

            Surface(
                onClick = { onSelectCategory(item.name) },
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (isSelected) listOf(CometBlue, StarWhite.copy(alpha = 0.8f))
                        else listOf(CosmicBorderSubtle, Color(0x1AFFFFFF))
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = iconTint,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.name,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Premier Campus Studio Spotlight (Bento Feature)
 */
@Composable
private fun PremierStudioSpotlight(
    business: Business,
    onExploreStore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onExploreStore() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CosmicCyan.copy(alpha = 0.6f), ElectricViolet.copy(alpha = 0.4f)))
        )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrowSpotlight = maxWidth < 420.dp

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isNarrowSpotlight) 12.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(if (isNarrowSpotlight) 44.dp else 52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CosmicSurfaceElevated)
                                .border(1.2.dp, CosmicCyan, RoundedCornerShape(14.dp))
                        ) {
                            if (!business.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = business.avatarUrl,
                                    contentDescription = business.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = business.name.take(2).uppercase(),
                                    fontSize = if (isNarrowSpotlight) 16.sp else 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = business.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = CosmicCyan,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = "${business.college} · Founder: ${business.ownerName}",
                                fontSize = 10.5.sp,
                                color = SoftLavender,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x33F59E0B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "${business.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = business.tagline,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = StarWhite.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Micro stats & action button (Responsive stacked on narrow, row on regular)
                if (isNarrowSpotlight) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${business.completedOrders} orders delivered",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MintEmerald
                            )
                            Text(text = " • ", color = TextMuted, fontSize = 11.sp)
                            Text(
                                text = "${business.responseRate}% response",
                                fontSize = 11.sp,
                                color = SoftLavender
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElectricViolet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExploreStore() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Visit Store", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = StarWhite, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = "${business.completedOrders} orders delivered",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MintEmerald
                            )
                            Text(text = " • ", color = TextMuted, fontSize = 12.sp)
                            Text(
                                text = "${business.responseRate}% response",
                                fontSize = 11.5.sp,
                                color = SoftLavender
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ElectricViolet,
                            modifier = Modifier.clickable { onExploreStore() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Visit Store", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = StarWhite, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-Resolution Product Orbit Card
 */
@Composable
private fun ProductOrbitCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(CosmicBorderGlow.copy(alpha = 0.4f), CosmicBorderSubtle)
            )
        )
    ) {
        Column {
            // Real Image with Category & Heart Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF0C1220))
            ) {
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF141F36), Color(0xFF0A101D))
                                )
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = null,
                            tint = CometBlue.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Category Tag Top-Left
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xDD090E1A),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = product.category,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Discount Pill Top-Right (if available)
                if (product.originalPrice != null && product.originalPrice > product.price) {
                    val discountPercent = ((product.originalPrice - product.price) * 100) / product.originalPrice
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xEE10B981),
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = SoftLavender, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = product.businessName,
                        fontSize = 10.5.sp,
                        color = SoftLavender,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${product.price}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StarWhite
                        )
                        if (product.originalPrice != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹${product.originalPrice}",
                                fontSize = 11.sp,
                                textDecoration = TextDecoration.LineThrough,
                                color = TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(text = "${product.rating}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }
                }
            }
        }
    }
}

/**
 * Service / Talent Orbit Card
 */
@Composable
private fun ServiceOrbitCard(
    service: Service,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, CometBlue.copy(alpha = 0.35f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CosmicSurfaceElevated
                ) {
                    Text(
                        text = service.category,
                        fontSize = 10.sp,
                        color = CometBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "${service.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = service.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = StarWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "By ${service.providerName}",
                fontSize = 11.sp,
                color = SoftLavender
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.School, contentDescription = null, tint = TextMuted, modifier = Modifier.size(11.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = service.college,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Starting at", fontSize = 9.sp, color = TextMuted)
                    Text(text = "₹${service.startingPrice}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricViolet
                ) {
                    Text(
                        text = "${service.turnaroundDays}d Delivery",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Business Venture Orbit Card
 */
@Composable
private fun BusinessOrbitCard(
    business: Business,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, CosmicCyan.copy(alpha = 0.35f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicSurfaceElevated)
                        .border(1.dp, CosmicCyan, RoundedCornerShape(12.dp))
                ) {
                    if (!business.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = business.avatarUrl,
                            contentDescription = business.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = business.name.take(2).uppercase(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = business.name,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.School, contentDescription = null, tint = TextMuted, modifier = Modifier.size(10.5.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = business.college,
                            fontSize = 10.5.sp,
                            color = SoftLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = business.tagline,
                fontSize = 11.sp,
                color = StarWhite.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${business.completedOrders} orders",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintEmerald
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${business.rating} (${business.reviewCount})",
                        fontSize = 11.sp,
                        color = SolarGold,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Crew / Collab Request Card
 */
@Composable
private fun CrewCollabCard(
    collab: CollaborationRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(240.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CosmicBorder, ElectricViolet.copy(alpha = 0.35f)))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CosmicSurfaceElevated
                ) {
                    Text(
                        text = collab.projectType,
                        fontSize = 10.sp,
                        color = SoftLavender,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "₹${collab.budget} Stipend",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintEmerald
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = collab.title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = StarWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lead: ${collab.organizer}",
                    fontSize = 10.5.sp,
                    color = SoftLavender,
                    maxLines = 1
                )
                Text(text = " • ", color = TextMuted, fontSize = 10.5.sp)
                Text(
                    text = collab.college,
                    fontSize = 10.5.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skills chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                collab.skillsNeeded.take(3).forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF0E1424)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 9.sp,
                            color = CometBlue,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Campus Planet Hub Card
 */
@Composable
private fun CampusPlanetCard(
    planet: CampusPlanet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(Color(planet.colorHex).copy(alpha = 0.6f), CosmicBorderSubtle)
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = planet.shortName,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = StarWhite
            )
            Text(
                text = planet.name,
                fontSize = 10.sp,
                color = SoftLavender,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${planet.studentCount} students", fontSize = 10.sp, color = CosmicCyan)
                Text(text = "${planet.businessCount} biz", fontSize = 10.sp, color = CometBlue)
            }
        }
    }
}

/**
 * Universal Section Header
 */
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    actionText: String?,
    onAction: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StarWhite
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = SoftLavender.copy(alpha = 0.8f)
            )
        }

        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CosmicCyan,
                modifier = Modifier
                    .clickable { onAction() }
                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun EmptyOrbitSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CosmicBorder, CometBlue.copy(alpha = 0.3f)))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1F2563EB))
                        .border(1.dp, Color(0x3360A5FA), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CometBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, fontSize = 11.sp, color = TextMuted, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(text = actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarWhite)
            }
        }
    }
}

/**
 * Clean Launch CTA Banner at the bottom of the feed
 */
@Composable
private fun LaunchCalloutBanner(onCreateClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(CosmicBorderGlow.copy(alpha = 0.5f), CosmicBorderSubtle))
        )
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isNarrowBanner = maxWidth < 400.dp

            if (isNarrowBanner) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Launch into Orbit",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Sell products, offer services or recruit your project crew.",
                        fontSize = 11.5.sp,
                        color = SoftLavender
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onCreateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Launch Creation", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Launch into Orbit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Text(
                            text = "Sell products, offer services or recruit your project crew.",
                            fontSize = 11.5.sp,
                            color = SoftLavender
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onCreateClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Launch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
