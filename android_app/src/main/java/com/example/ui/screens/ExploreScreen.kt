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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.model.Business
import com.example.model.Product
import com.example.model.Service
import com.example.model.Student
import com.example.model.VerificationType
import com.example.ui.components.CosmicBadgeRow
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSuccess
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.CosmicSurfaceHigh
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.UniSpaceUiState

@Composable
fun ExploreScreen(
    uiState: UniSpaceUiState,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onCollegeChange: (String) -> Unit,
    onSortChange: (String) -> Unit,
    onToggleVerifiedOnly: () -> Unit,
    onSelectBusiness: (Business) -> Unit,
    onSelectStudent: (Student) -> Unit,
    onSelectProduct: (Product) -> Unit,
    onSelectService: (Service) -> Unit,
    onToggleSave: (String) -> Unit,
    onSubTabChange: (String) -> Unit = {},
    onPriceRangeChange: (String) -> Unit = {},
    onToggleInStockOnly: () -> Unit = {},
    onResetFilters: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(uiState.activeSubTab) }

    val collegeOptions = listOf(
        "All Colleges",
        "S.A. Engineering College",
        "VIT Chennai",
        "SRM Institute of Science and Technology",
        "SSN College of Engineering",
        "Saveetha Engineering College",
        "Rajalakshmi Engineering College"
    )

    val categoryOptions = listOf(
        "All", "Pre-Loved", "Tech", "Creative", "Fashion", "Academic", "Food", "Events"
    )

    val sortOptions = listOf("Recommended", "Price: Low to High", "Price: High to Low", "Highest Rated", "Most Popular", "Newest")
    val priceOptions = listOf("All Prices", "< ₹300", "₹300 - ₹600", "₹600 - ₹1000", "₹1000+")

    val hasActiveFilters = uiState.verifiedOnly || uiState.inStockOnly || uiState.priceRange != "All" || uiState.activeCategory != "All" || uiState.activeCollege != "All" || uiState.sortBy != "Recommended" || uiState.searchQuery.isNotBlank()

    // Multi-keyword tokenized precise search
    val queryTokens = remember(uiState.searchQuery) {
        uiState.searchQuery.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
    }

    // Precise Filtered and Sorted lists
    val filteredBusinesses = remember(uiState.businesses, queryTokens, uiState.activeCollege, uiState.activeCategory, uiState.verifiedOnly, uiState.sortBy) {
        uiState.businesses.filter { biz ->
            val matchesQuery = queryTokens.isEmpty() || queryTokens.all { token ->
                biz.name.lowercase().contains(token) ||
                biz.tagline.lowercase().contains(token) ||
                biz.category.lowercase().contains(token) ||
                biz.ownerName.lowercase().contains(token) ||
                biz.college.lowercase().contains(token) ||
                biz.about.lowercase().contains(token) ||
                biz.productsOffered.any { it.lowercase().contains(token) } ||
                biz.servicesOffered.any { it.lowercase().contains(token) }
            }
            val matchesCollege = uiState.activeCollege == "All" || uiState.activeCollege == "All Colleges" || biz.college == uiState.activeCollege
            val matchesCategory = uiState.activeCategory == "All" || biz.category.contains(uiState.activeCategory, ignoreCase = true)
            val matchesVerified = !uiState.verifiedOnly || biz.badges.isNotEmpty()
            matchesQuery && matchesCollege && matchesCategory && matchesVerified
        }.let { list ->
            when (uiState.sortBy) {
                "Highest Rated" -> list.sortedWith(compareByDescending<Business> { it.rating }.thenByDescending { it.reviewCount })
                "Most Popular" -> list.sortedByDescending { it.completedOrders }
                "Newest" -> list.reversed()
                else -> list.sortedByDescending { (it.rating * 20) + it.completedOrders }
            }
        }
    }

    val filteredProducts = remember(uiState.products, queryTokens, uiState.activeCollege, uiState.activeCategory, uiState.verifiedOnly, uiState.inStockOnly, uiState.priceRange, uiState.sortBy) {
        uiState.products.filter { prod ->
            val matchesQuery = queryTokens.isEmpty() || queryTokens.all { token ->
                prod.title.lowercase().contains(token) ||
                prod.businessName.lowercase().contains(token) ||
                prod.description.lowercase().contains(token) ||
                prod.category.lowercase().contains(token) ||
                prod.college.lowercase().contains(token) ||
                prod.tags.any { it.lowercase().contains(token) }
            }
            val matchesCollege = uiState.activeCollege == "All" || uiState.activeCollege == "All Colleges" || prod.college == uiState.activeCollege
            val matchesCategory = uiState.activeCategory == "All" || prod.category.contains(uiState.activeCategory, ignoreCase = true)
            val matchesInStock = !uiState.inStockOnly || prod.inStock
            val matchesPrice = when (uiState.priceRange) {
                "< ₹300" -> prod.price < 300
                "₹300 - ₹600" -> prod.price in 300..600
                "₹600 - ₹1000" -> prod.price in 600..1000
                "₹1000+" -> prod.price >= 1000
                else -> true
            }
            val seller = uiState.businesses.firstOrNull { it.id == prod.businessId }
            val matchesVerified = !uiState.verifiedOnly || (seller?.badges?.isNotEmpty() == true)
            matchesQuery && matchesCollege && matchesCategory && matchesInStock && matchesPrice && matchesVerified
        }.let { list ->
            when (uiState.sortBy) {
                "Price: Low to High" -> list.sortedBy { it.price }
                "Price: High to Low" -> list.sortedByDescending { it.price }
                "Highest Rated" -> list.sortedWith(compareByDescending<Product> { it.rating }.thenByDescending { it.reviewCount })
                "Most Popular" -> list.sortedWith(compareByDescending<Product> { it.likesCount }.thenByDescending { it.reviewCount })
                "Newest" -> list.reversed()
                else -> list.sortedByDescending { (it.rating * 10) + it.likesCount }
            }
        }
    }

    val filteredServices = remember(uiState.services, queryTokens, uiState.activeCollege, uiState.activeCategory, uiState.verifiedOnly, uiState.priceRange, uiState.sortBy) {
        uiState.services.filter { serv ->
            val matchesQuery = queryTokens.isEmpty() || queryTokens.all { token ->
                serv.title.lowercase().contains(token) ||
                serv.description.lowercase().contains(token) ||
                serv.providerName.lowercase().contains(token) ||
                serv.category.lowercase().contains(token) ||
                serv.college.lowercase().contains(token) ||
                serv.tags.any { it.lowercase().contains(token) }
            }
            val matchesCollege = uiState.activeCollege == "All" || uiState.activeCollege == "All Colleges" || serv.college == uiState.activeCollege
            val matchesCategory = uiState.activeCategory == "All" || serv.category.contains(uiState.activeCategory, ignoreCase = true)
            val matchesPrice = when (uiState.priceRange) {
                "< ₹300" -> serv.startingPrice < 300
                "₹300 - ₹600" -> serv.startingPrice in 300..600
                "₹600 - ₹1000" -> serv.startingPrice in 600..1000
                "₹1000+" -> serv.startingPrice >= 1000
                else -> true
            }
            val matchesVerified = !uiState.verifiedOnly || uiState.students.any { it.name == serv.providerName && it.badges.isNotEmpty() } || uiState.businesses.any { it.name == serv.providerName && it.badges.isNotEmpty() }
            matchesQuery && matchesCollege && matchesCategory && matchesPrice && matchesVerified
        }.let { list ->
            when (uiState.sortBy) {
                "Price: Low to High" -> list.sortedBy { it.startingPrice }
                "Price: High to Low" -> list.sortedByDescending { it.startingPrice }
                "Highest Rated" -> list.sortedByDescending { it.rating }
                "Most Popular" -> list.sortedByDescending { it.completedCount }
                "Newest" -> list.reversed()
                else -> list.sortedByDescending { (it.rating * 10) + it.completedCount }
            }
        }
    }

    val filteredStudents = remember(uiState.students, queryTokens, uiState.activeCollege, uiState.verifiedOnly, uiState.sortBy) {
        uiState.students.filter { stud ->
            val matchesQuery = queryTokens.isEmpty() || queryTokens.all { token ->
                stud.name.lowercase().contains(token) ||
                stud.roleTitle.lowercase().contains(token) ||
                stud.bio.lowercase().contains(token) ||
                stud.college.lowercase().contains(token) ||
                stud.skills.any { it.name.lowercase().contains(token) || it.category.lowercase().contains(token) } ||
                stud.businesses.any { it.lowercase().contains(token) } ||
                stud.achievements.any { it.lowercase().contains(token) }
            }
            val matchesCollege = uiState.activeCollege == "All" || uiState.activeCollege == "All Colleges" || stud.college == uiState.activeCollege
            val matchesVerified = !uiState.verifiedOnly || stud.badges.isNotEmpty()
            matchesQuery && matchesCollege && matchesVerified
        }.let { list ->
            when (uiState.sortBy) {
                "Highest Rated" -> list.sortedByDescending { it.rating }
                "Most Popular" -> list.sortedByDescending { it.completedProjects }
                "Newest" -> list.reversed()
                else -> list.sortedByDescending { (it.rating * 20) + it.completedProjects }
            }
        }
    }

    val matchCount = when (activeSubTab) {
        "Businesses" -> filteredBusinesses.size
        "Services" -> filteredServices.size
        "Products" -> filteredProducts.size
        "Students" -> filteredStudents.size
        else -> filteredBusinesses.size + filteredServices.size + filteredProducts.size + filteredStudents.size
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gridColumns = if (maxWidth >= 880.dp) 3 else if (maxWidth >= 580.dp) 2 else 1

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("explore_screen"),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
        // Search Input Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Discover the Universe",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "Explore verified businesses, student talents, products & campus nodes",
                    fontSize = 12.sp,
                    color = SoftLavender
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search the universe...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CosmicCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = SoftLavender,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CosmicSurface,
                        unfocusedContainerColor = CosmicSurface,
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = CosmicBorder,
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("explore_search_input")
                )
            }
        }

        // Sub-Tabs: All | Businesses | Services | Products | Students
        item {
            val subTabs = listOf("All", "Businesses", "Services", "Products", "Students")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subTabs.forEach { tab ->
                    val isSelected = activeSubTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricViolet else CosmicSurfaceCard)
                            .border(
                                1.dp,
                                if (isSelected) CosmicBorderGlow else CosmicBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { activeSubTab = tab }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) StarWhite else SoftLavender
                        )
                    }
                }
            }
        }

        // Filter Controls Row
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Verified Only, In Stock Only & Reset Filters Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Verified Only Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (uiState.verifiedOnly) CosmicSuccess.copy(alpha = 0.2f) else CosmicSurfaceCard
                            )
                            .border(
                                0.8.dp,
                                if (uiState.verifiedOnly) CosmicSuccess else CosmicBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleVerifiedOnly() }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("verified_only_toggle")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.verifiedOnly) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Checked",
                                    tint = CosmicSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "🎓 Verified",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.verifiedOnly) CosmicSuccess else SoftLavender
                            )
                        }
                    }

                    // In Stock Only Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (uiState.inStockOnly) CosmicCyan.copy(alpha = 0.2f) else CosmicSurfaceCard
                            )
                            .border(
                                0.8.dp,
                                if (uiState.inStockOnly) CosmicCyan else CosmicBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleInStockOnly() }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("in_stock_toggle")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (uiState.inStockOnly) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Checked",
                                    tint = CosmicCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "📦 In Stock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.inStockOnly) CosmicCyan else SoftLavender
                            )
                        }
                    }

                    // Reset Filters if active
                    if (hasActiveFilters) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSurfaceCard)
                                .border(0.8.dp, Color(0xFFF43F5E).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable { onResetFilters() }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                                .testTag("reset_filters_button")
                        ) {
                            Text(
                                text = "↺ Reset",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFDA4AF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sort Options Carousel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sort:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sortOptions.forEach { sort ->
                            val isSelected = (uiState.sortBy == sort)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricViolet.copy(alpha = 0.35f) else CosmicSurfaceCard)
                                    .border(
                                        0.8.dp,
                                        if (isSelected) CometBlue else CosmicBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSortChange(sort) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = sort,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CosmicCyan else TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryOptions.forEach { cat ->
                        val isSelected = (uiState.activeCategory == cat)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CosmicPurple.copy(alpha = 0.6f) else CosmicSurfaceCard)
                                .border(
                                    0.8.dp,
                                    if (isSelected) CosmicCyan else CosmicBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onCategoryChange(cat) }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) StarWhite else SoftLavender
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Price Range Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priceOptions.forEach { price ->
                        val isSelected = (uiState.priceRange == price)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CosmicCyan.copy(alpha = 0.25f) else CosmicSurfaceCard)
                                .border(
                                    0.6.dp,
                                    if (isSelected) CosmicCyan else CosmicBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onPriceRangeChange(price) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = price,
                                fontSize = 10.sp,
                                color = if (isSelected) CosmicCyan else TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // College Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    collegeOptions.forEach { college ->
                        val isSelected = (uiState.activeCollege == college)
                        val shortTitle = if (college == "All Colleges") "All" else college.split(" ").first()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CometBlue.copy(alpha = 0.25f) else CosmicSurfaceCard)
                                .border(
                                    0.6.dp,
                                    if (isSelected) CometBlue else CosmicBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onCollegeChange(college) }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🎓 $shortTitle",
                                fontSize = 10.sp,
                                color = if (isSelected) StarWhite else SoftLavender
                            )
                        }
                    }
                }
            }
        }

        // Results Count Header
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Orbiting Matches",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "$matchCount result${if (matchCount != 1) "s" else ""}",
                    fontSize = 11.sp,
                    color = CosmicCyan
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Empty Results Card
        if (matchCount == 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
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
                        Text(text = "🔭", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Listings in This Orbit",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (hasActiveFilters)
                                "No items match your active search or filters. Try resetting filters."
                            else "No items have been listed in this universe yet. Be the first to drop a product or offer a service!",
                            fontSize = 12.sp,
                            color = SoftLavender,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        if (hasActiveFilters) {
                            Button(
                                onClick = onResetFilters,
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reset Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Render Results according to sub-tab
        if (activeSubTab == "All" || activeSubTab == "Businesses") {
            if (gridColumns == 1) {
                items(filteredBusinesses) { biz ->
                    ExploreBusinessCard(
                        business = biz,
                        onClick = { onSelectBusiness(biz) },
                        onSave = { onToggleSave(biz.id) },
                        isSaved = uiState.savedItems.contains(biz.id)
                    )
                }
            } else {
                items(filteredBusinesses.chunked(gridColumns)) { rowChunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (biz in rowChunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                ExploreBusinessCard(
                                    business = biz,
                                    onClick = { onSelectBusiness(biz) },
                                    onSave = { onToggleSave(biz.id) },
                                    isSaved = uiState.savedItems.contains(biz.id),
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

        if (activeSubTab == "All" || activeSubTab == "Services") {
            if (gridColumns == 1) {
                items(filteredServices) { serv ->
                    ExploreServiceCard(
                        service = serv,
                        onClick = { onSelectService(serv) },
                        onSave = { onToggleSave(serv.id) },
                        isSaved = uiState.savedItems.contains(serv.id)
                    )
                }
            } else {
                items(filteredServices.chunked(gridColumns)) { rowChunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (serv in rowChunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                ExploreServiceCard(
                                    service = serv,
                                    onClick = { onSelectService(serv) },
                                    onSave = { onToggleSave(serv.id) },
                                    isSaved = uiState.savedItems.contains(serv.id),
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

        if (activeSubTab == "All" || activeSubTab == "Products") {
            if (gridColumns == 1) {
                items(filteredProducts) { prod ->
                    ExploreProductCard(
                        product = prod,
                        onClick = { onSelectProduct(prod) },
                        onSave = { onToggleSave(prod.id) },
                        isSaved = uiState.savedItems.contains(prod.id)
                    )
                }
            } else {
                items(filteredProducts.chunked(gridColumns)) { rowChunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (prod in rowChunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                ExploreProductCard(
                                    product = prod,
                                    onClick = { onSelectProduct(prod) },
                                    onSave = { onToggleSave(prod.id) },
                                    isSaved = uiState.savedItems.contains(prod.id),
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

        if (activeSubTab == "All" || activeSubTab == "Students") {
            if (gridColumns == 1) {
                items(filteredStudents) { stud ->
                    ExploreStudentCard(
                        student = stud,
                        onClick = { onSelectStudent(stud) }
                    )
                }
            } else {
                items(filteredStudents.chunked(gridColumns)) { rowChunk ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (stud in rowChunk) {
                            Box(modifier = Modifier.weight(1f)) {
                                ExploreStudentCard(
                                    student = stud,
                                    onClick = { onSelectStudent(stud) },
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
}

@Composable
private fun ExploreBusinessCard(
    business: Business,
    onClick: () -> Unit,
    onSave: () -> Unit,
    isSaved: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, CosmicBorderGlow.copy(alpha = 0.25f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CosmicPurple.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .border(1.dp, ElectricViolet, RoundedCornerShape(10.dp))
                    ) {
                        if (!business.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = business.avatarUrl,
                                contentDescription = business.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "☄️", fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = business.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Text(
                            text = "🎓 ${business.college}",
                            fontSize = 11.sp,
                            color = SoftLavender
                        )
                    }
                }

                IconButton(
                    onClick = onSave,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) CosmicCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CosmicBadgeRow(badges = business.badges, compact = true)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = business.tagline,
                fontSize = 12.sp,
                color = StarWhite.copy(alpha = 0.85f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                        text = "⭐ ${business.rating}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                    Text(
                        text = " (${business.reviewCount})",
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " · ${business.completedOrders} orders",
                        fontSize = 11.sp,
                        color = CosmicCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElectricViolet.copy(alpha = 0.25f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "View Biz ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CometBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreServiceCard(
    service: Service,
    onClick: () -> Unit,
    onSave: () -> Unit,
    isSaved: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, CometBlue.copy(alpha = 0.25f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CosmicSurfaceElevated)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "💼 ${service.category}",
                            fontSize = 10.sp,
                            color = CometBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = service.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )

                    Text(
                        text = "By ${service.providerName} · ${service.college}",
                        fontSize = 11.sp,
                        color = SoftLavender
                    )
                }

                IconButton(
                    onClick = onSave,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (isSaved) CosmicCyan else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = service.description,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${service.startingPrice} onwards",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicCyan
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicPurple)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Request Service",
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
private fun ExploreProductCard(
    product: Product,
    onClick: () -> Unit,
    onSave: () -> Unit,
    isSaved: Boolean,
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, ElectricViolet.copy(alpha = 0.3f))
            )
        )
    ) {
        Column {
            // Full Width Image with Category Chip and Save Button
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
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = "🛍️", fontSize = 32.sp)
                    }
                }

                // Category Tag
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xDD090E1A))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = product.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicCyan
                    )
                }

                // Save Bookmark Icon
                IconButton(
                    onClick = onSave,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(36.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xB3000000))
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) CosmicCyan else StarWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${product.businessName} · 🎓 ${product.college}",
                            fontSize = 11.sp,
                            color = SoftLavender,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${product.rating}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

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
                            text = "₹${product.price}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CosmicCyan
                        )
                        if (product.originalPrice != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "₹${product.originalPrice}",
                                fontSize = 11.sp,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricViolet)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "View & Buy ➔",
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

@Composable
private fun ExploreStudentCard(
    student: Student,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(CosmicBorder, CosmicCyan.copy(alpha = 0.25f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(ElectricViolet, CometBlue)
                            ),
                            shape = CircleShape
                        )
                ) {
                    if (!student.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = student.avatarUrl,
                            contentDescription = student.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = student.name.take(1),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = student.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                    Text(
                        text = student.roleTitle,
                        fontSize = 11.sp,
                        color = CosmicCyan
                    )
                    Text(
                        text = "🎓 ${student.college}",
                        fontSize = 10.sp,
                        color = SoftLavender
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            CosmicBadgeRow(badges = student.badges, compact = true)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = student.bio,
                fontSize = 11.sp,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skills: ${student.skills.take(3).joinToString(", ") { it.name }}",
                    fontSize = 11.sp,
                    color = SoftLavender
                )

                Text(
                    text = "View Constellation ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CometBlue
                )
            }
        }
    }
}
