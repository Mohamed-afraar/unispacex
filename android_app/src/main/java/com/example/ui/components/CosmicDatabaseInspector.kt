package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessListingEntity
import com.example.data.local.UniSpaceDatabase
import com.example.data.local.UserProfileEntity
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CosmicDatabaseInspectorSheet(
    onDismiss: () -> Unit,
    onTriggerSync: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { UniSpaceDatabase.getInstance(context) }

    // Live data collection from Room DAOs
    val businessesFlow = remember { db.businessListingDao().getAllBusinesses() }
    val profilesFlow = remember { db.userProfileDao().getAllProfiles() }

    val businesses by businessesFlow.collectAsState(initial = emptyList())
    val profiles by profilesFlow.collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var filterUnsyncedOnly by remember { mutableStateOf(false) }
    var expandedRowId by remember { mutableStateOf<String?>(null) }
    var isOperating by remember { mutableStateOf(false) }

    val tabs = listOf(
        "🏢 business_listings (${businesses.size})",
        "🎓 user_profiles (${profiles.size})",
        "⚡ SQLite Schema & SQL"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag("database_inspector_sheet")
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 760.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header with DB Badge & Close Button
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F1E3D))
                                .border(1.dp, CosmicCyan, RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Database",
                                tint = CosmicCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "UniSpace SQLite Database",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF133420))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4ADE80)
                                    )
                                }
                            }
                            Text(
                                text = "Engine: Room 2.7.0 • File: ${UniSpaceDatabase.DATABASE_NAME}",
                                fontSize = 11.sp,
                                color = SoftLavender
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = StarWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Database Metadata Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A0F24))
                        .border(1.dp, Color(0xFF1D2854), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "TABLE COUNTS", fontSize = 10.sp, color = SoftLavender, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${businesses.size} Ventures  •  ${profiles.size} Profiles",
                                    fontSize = 13.sp,
                                    color = StarWhite,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "STORAGE PATH", fontSize = 10.sp, color = SoftLavender, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "data/databases/${UniSpaceDatabase.DATABASE_NAME}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CosmicCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        isOperating = true
                                        try {
                                            // Insert a test row to demonstrate real-time Room persistence
                                            val randomId = "biz_test_${System.currentTimeMillis() % 10000}"
                                            val testEntity = BusinessListingEntity(
                                                id = randomId,
                                                name = "Cosmic Test Lab #$randomId",
                                                ownerName = "Student Inspector",
                                                college = "S.A. Engineering College",
                                                category = "Hardware & Robotics",
                                                rating = 5.0,
                                                reviewCount = 1,
                                                tagline = "Real-time inserted via In-App DB Inspector",
                                                about = "This record was directly persisted into SQLite through Room Database Inspector.",
                                                completedOrders = 12,
                                                responseRate = 99,
                                                servicesOffered = "DB Verification, SQLite Inspection",
                                                productsOffered = "Test Module v1.0",
                                                isSyncedWithFirestore = false,
                                                lastUpdated = System.currentTimeMillis()
                                            )
                                            db.businessListingDao().insertBusiness(testEntity)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "✅ Added record $randomId into Room!", Toast.LENGTH_SHORT).show()
                                            }
                                        } finally {
                                            isOperating = false
                                        }
                                    }
                                },
                                enabled = !isOperating,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = CosmicCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Insert Test Row", fontSize = 11.sp, color = CosmicCyan, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        isOperating = true
                                        try {
                                            db.businessListingDao().clearAllBusinesses()
                                            db.userProfileDao().clearAllProfiles()
                                            db.productDao().clearAllProducts()
                                            db.serviceDao().clearAllServices()
                                            db.collaborationDao().clearAllCollaborations()
                                            db.feedPostDao().clearAllPosts()
                                            db.userSessionDao().clearSession()
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "🧹 All local Room tables purged clean", Toast.LENGTH_SHORT).show()
                                            }
                                        } finally {
                                            isOperating = false
                                        }
                                    }
                                },
                                enabled = !isOperating,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = StarWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Purge Tables", fontSize = 11.sp, color = StarWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Table Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = CosmicCyan,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = CosmicCyan,
                            height = 2.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) StarWhite else SoftLavender
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search & Filter controls for tables
                if (selectedTabIndex < 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search records in table...", fontSize = 12.sp, color = SoftLavender.copy(alpha = 0.6f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftLavender, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F1535),
                                unfocusedContainerColor = Color(0xFF0F1535),
                                focusedTextColor = StarWhite,
                                unfocusedTextColor = StarWhite,
                                focusedBorderColor = CosmicCyan,
                                unfocusedBorderColor = Color(0xFF223060)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )

                        FilterChip(
                            selected = filterUnsyncedOnly,
                            onClick = { filterUnsyncedOnly = !filterUnsyncedOnly },
                            label = { Text("Unsynced", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ElectricViolet,
                                selectedLabelColor = StarWhite,
                                containerColor = Color(0xFF131D42),
                                labelColor = SoftLavender
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(44.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Table Content Display
                when (selectedTabIndex) {
                    0 -> {
                        // business_listings table
                        val filteredBusinesses = businesses.filter { b ->
                            val queryMatch = searchQuery.isBlank() ||
                                    b.name.contains(searchQuery, ignoreCase = true) ||
                                    b.college.contains(searchQuery, ignoreCase = true) ||
                                    b.category.contains(searchQuery, ignoreCase = true) ||
                                    b.id.contains(searchQuery, ignoreCase = true)
                            val syncMatch = !filterUnsyncedOnly || !b.isSyncedWithFirestore
                            queryMatch && syncMatch
                        }

                        if (filteredBusinesses.isEmpty()) {
                            EmptyTableState(
                                tableName = "business_listings"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredBusinesses, key = { it.id }) { biz ->
                                    BusinessTableRowCard(
                                        biz = biz,
                                        isExpanded = expandedRowId == biz.id,
                                        onToggleExpand = {
                                            expandedRowId = if (expandedRowId == biz.id) null else biz.id
                                        },
                                        onDelete = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                db.businessListingDao().deleteBusiness(biz.id)
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Deleted ${biz.id} from Room", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // user_profiles table
                        val filteredProfiles = profiles.filter { p ->
                            val queryMatch = searchQuery.isBlank() ||
                                    p.name.contains(searchQuery, ignoreCase = true) ||
                                    p.college.contains(searchQuery, ignoreCase = true) ||
                                    p.roleTitle.contains(searchQuery, ignoreCase = true) ||
                                    p.id.contains(searchQuery, ignoreCase = true)
                            val syncMatch = !filterUnsyncedOnly || !p.isSyncedWithFirestore
                            queryMatch && syncMatch
                        }

                        if (filteredProfiles.isEmpty()) {
                            EmptyTableState(
                                tableName = "user_profiles"
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredProfiles, key = { it.id }) { prof ->
                                    ProfileTableRowCard(
                                        profile = prof,
                                        isExpanded = expandedRowId == prof.id,
                                        onToggleExpand = {
                                            expandedRowId = if (expandedRowId == prof.id) null else prof.id
                                        },
                                        onDelete = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                db.userProfileDao().deleteProfile(prof.id)
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Deleted profile ${prof.id}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    2 -> {
                        // SQLite Schema & Query Presets
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "SQLITE DDL SCHEMAS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            SqlSchemaSnippet(
                                tableName = "business_listings",
                                ddl = """CREATE TABLE business_listings (
  id TEXT PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  ownerName TEXT NOT NULL,
  college TEXT NOT NULL,
  category TEXT NOT NULL,
  rating REAL NOT NULL,
  reviewCount INTEGER NOT NULL,
  tagline TEXT NOT NULL,
  about TEXT NOT NULL,
  completedOrders INTEGER NOT NULL,
  responseRate INTEGER NOT NULL,
  servicesOffered TEXT NOT NULL,
  productsOffered TEXT NOT NULL,
  isSyncedWithFirestore INTEGER NOT NULL,
  lastUpdated INTEGER NOT NULL
);"""
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SqlSchemaSnippet(
                                tableName = "user_profiles",
                                ddl = """CREATE TABLE user_profiles (
  id TEXT PRIMARY KEY NOT NULL,
  name TEXT NOT NULL,
  roleTitle TEXT NOT NULL,
  college TEXT NOT NULL,
  bio TEXT NOT NULL,
  rating REAL NOT NULL,
  completedProjects INTEGER NOT NULL,
  responseRate INTEGER NOT NULL,
  businesses TEXT NOT NULL,
  achievements TEXT NOT NULL,
  isSyncedWithFirestore INTEGER NOT NULL,
  lastUpdated INTEGER NOT NULL
);"""
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "ACTIVE DATABASE SPECS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0C132B))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    SpecRow("Database Name", UniSpaceDatabase.DATABASE_NAME)
                                    SpecRow("Room Version", "2.7.0")
                                    SpecRow("Journal Mode", "WAL (Write-Ahead Logging)")
                                    SpecRow("Primary DAOs", "UserProfileDao, BusinessListingDao")
                                    SpecRow("Cloud Sync Target", "Firebase Cloud Firestore")
                                    SpecRow("Total SQLite Records", "${businesses.size + profiles.size} Rows")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BusinessTableRowCard(
    biz: BusinessListingEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F1535))
            .border(1.dp, if (isExpanded) CosmicCyan else Color(0xFF1E2850), RoundedCornerShape(10.dp))
            .clickable { onToggleExpand() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = biz.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (biz.isSyncedWithFirestore) Color(0xFF163E24) else Color(0xFF3E2D12))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (biz.isSyncedWithFirestore) "SYNCED" else "UNSYNCED",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (biz.isSyncedWithFirestore) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: ${biz.id}  •  ${biz.category}  •  ${biz.college}",
                        fontSize = 10.5.sp,
                        color = SoftLavender
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐ ${biz.rating}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD166)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = SoftLavender,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF090D22))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            SpecRow("Owner", biz.ownerName)
                            SpecRow("Tagline", biz.tagline)
                            SpecRow("About", biz.about)
                            SpecRow("Orders Done", "${biz.completedOrders} orders")
                            SpecRow("Services", biz.servicesOffered.ifBlank { "None listed" })
                            SpecRow("Products", biz.productsOffered.ifBlank { "None listed" })
                            SpecRow("Last Modified", dateFormat.format(Date(biz.lastUpdated)))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = NeonPink, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Row", fontSize = 10.sp, color = NeonPink)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTableRowCard(
    profile: UserProfileEntity,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F1535))
            .border(1.dp, if (isExpanded) CosmicCyan else Color(0xFF1E2850), RoundedCornerShape(10.dp))
            .clickable { onToggleExpand() }
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (profile.isSyncedWithFirestore) Color(0xFF163E24) else Color(0xFF3E2D12))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = if (profile.isSyncedWithFirestore) "SYNCED" else "UNSYNCED",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.isSyncedWithFirestore) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ID: ${profile.id}  •  ${profile.roleTitle}  •  ${profile.college}",
                        fontSize = 10.5.sp,
                        color = SoftLavender
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⭐ ${profile.rating}",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD166)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = SoftLavender,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF090D22))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            SpecRow("Bio", profile.bio)
                            SpecRow("Completed Projects", "${profile.completedProjects}")
                            SpecRow("Response Rate", "${profile.responseRate}%")
                            SpecRow("Associated Ventures", profile.businesses.ifBlank { "None" })
                            SpecRow("Achievements", profile.achievements.ifBlank { "None" })
                            SpecRow("Last Modified", dateFormat.format(Date(profile.lastUpdated)))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = NeonPink, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Profile", fontSize = 10.sp, color = NeonPink)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SqlSchemaSnippet(tableName: String, ddl: String) {
    Column {
        Text(text = "TABLE: $tableName", fontSize = 11.sp, color = StarWhite, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF050818))
                .border(1.dp, Color(0xFF1E2850), RoundedCornerShape(6.dp))
                .horizontalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            Text(
                text = ddl,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = CosmicCyan
            )
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, color = SoftLavender, modifier = Modifier.weight(0.4f))
        Text(
            text = value,
            fontSize = 10.sp,
            color = StarWhite,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun EmptyTableState(
    tableName: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0A0F24))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📭", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Table '$tableName' is clean and empty", fontSize = 13.sp, color = StarWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Zero pre-feeded records. Ready for new user entries.", fontSize = 11.sp, color = SoftLavender)
        }
    }
}
