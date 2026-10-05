package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CampusPaymentMethod
import com.example.model.UserRole
import com.example.ui.theme.CelestialGold
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.LocalCosmicTheme
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.viewmodel.UniSpaceUiState
import com.example.viewmodel.UniSpaceViewModel

private enum class SettingsCategory(val title: String, val icon: String) {
    ALL("All", "⚡"),
    ACCOUNT("Role & Identity", "🎓"),
    APPEARANCE("Theme & Glow", "🎨"),
    NOTIFICATIONS("Alerts & Radar", "🔔"),
    TRADING("Meetup & Trade", "🤝"),
    PRIVACY("Privacy & Lock", "🛡️"),
    DATA("Database & Sync", "💾"),
    ABOUT("About & Rules", "📜")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: UniSpaceUiState,
    viewModel: UniSpaceViewModel,
    onBack: () -> Unit,
    onNavigateToProfileHub: () -> Unit,
    onOpenDatabaseInspector: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = uiState.appSettings
    val userStudent = uiState.userProfile
    val theme = LocalCosmicTheme.current

    BackHandler {
        onBack()
    }

    var activeCategory by remember { mutableStateOf(SettingsCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showResetCacheDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showGuidelinesDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showTermsOfServiceDialog by remember { mutableStateOf(false) }
    var showGoogleLinkDialog by remember { mutableStateOf(false) }
    var customGoogleLinkEmail by remember(uiState.loggedInEmail) { mutableStateOf(if (uiState.loggedInEmail.contains("@")) uiState.loggedInEmail else "") }
    var instructionsText by remember(settings.handoverInstructions) { mutableStateOf(settings.handoverInstructions) }

    // Dialogs
    if (showResetCacheDialog) {
        AlertDialog(
            onDismissRequest = { showResetCacheDialog = false },
            containerColor = theme.surfaceCard,
            title = {
                Text(
                    text = "Clear Local Cache & Reset Data?",
                    color = StarWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "This will clear transient cart items, reset search history, and refresh local caches. Your student identity in Room DB remains intact.",
                    color = SoftLavender,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetLocalCache()
                        showResetCacheDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Clear Cache", color = StarWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetCacheDialog = false }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = Color(0xFF1F0D15),
            title = {
                Text(
                    text = "Delete Profile & Exit Galaxy?",
                    color = Color(0xFFFDA4AF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete your student profile and all associated venture listings from the local database? This cannot be undone.",
                    color = SoftLavender,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        viewModel.deleteCurrentUserProfile()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Permanently Delete", color = StarWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    if (showGuidelinesDialog) {
        AlertDialog(
            onDismissRequest = { showGuidelinesDialog = false },
            containerColor = theme.surfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📜", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Campus Commerce Honor Code",
                        color = StarWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Verified Campus Handover: Always meet at designated campus landmarks during well-lit daylight hours.",
                        color = SoftLavender,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = "2. Student Integrity: Accurately declare item condition (Brand New vs Pre-Loved textbooks and electronics).",
                        color = SoftLavender,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = "3. Zero Platform Fee: All peer trades are 100% student-to-student with direct handover cash or UPI payment.",
                        color = SoftLavender,
                        fontSize = 12.5.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showGuidelinesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Understood", color = Color(0xFF07090E), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            containerColor = theme.surfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🛡️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Policy & Data Safety",
                        color = StarWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Student Data Collection: UNISpaceX collects institutional college emails (.edu, .ac.in), graduation years, and public marketplace listings strictly for verified campus networking.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "2. Secure Cloud Storage: All verification records and venture listings are securely stored in Google Cloud Firebase. We never sell or share student data with third-party ad networks.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "3. Complete Right to Deletion: You retain absolute ownership of your data. Tapping 'Delete Profile' in Settings permanently purges all profile data, listings, and Auth credentials from Google Cloud and local storage.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "4. Google Play Compliance: Fully compliant with Google Play Developer Policy §4.8 regarding User-Generated Content and personal data protection.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Close", color = Color(0xFF07090E), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showTermsOfServiceDialog) {
        AlertDialog(
            onDismissRequest = { showTermsOfServiceDialog = false },
            containerColor = theme.surfaceCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚖️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Terms of Service & UGC Rules",
                        color = StarWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. Verified Campus Membership: UNISpaceX is strictly for verified college students, student entrepreneurs, and campus communities.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "2. Prohibited Content: No counterfeit products, stolen goods, exam cheating services, weapons, or illegal items. All listings are monitored and violating items are immediately removed.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "3. Community Safety & Reporting: Students can report fraudulent listings or abusive users at any time. Our campus moderation team reviews reports within 24 hours.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "4. Physical Handover Safety: Physical meetups must take place at well-lit public campus landmarks. Inspect items thoroughly before completing payment.",
                        color = SoftLavender,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsOfServiceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.primary)
                ) {
                    Text("Accept & Close", color = Color(0xFF07090E), fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showGoogleLinkDialog) {
        val defaultLinkEmail = if (uiState.loggedInEmail.contains("@gmail.com")) uiState.loggedInEmail else uiState.userProfile.collegeEmail?.ifBlank { null } ?: "student@campus.edu"
        AlertDialog(
            onDismissRequest = { showGoogleLinkDialog = false },
            containerColor = theme.surfaceCard,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Text(text = "G", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color(0xFF4285F4))
                    }
                    Column {
                        Text("Link Google Account", color = StarWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Connect campus identity with Google OAuth", color = SoftLavender, fontSize = 11.sp)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showGoogleLinkDialog = false
                                val defaultName = uiState.userProfile.name.ifBlank { "Student Account" }
                                viewModel.linkGoogleAccount(defaultLinkEmail, defaultName)
                                viewModel.syncProfileWithGoogle()
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF13192B)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF4285F4).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                val currentName = uiState.userProfile.name.ifBlank { "Student Account" }
                                Text(currentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StarWhite)
                                Text(defaultLinkEmail, fontSize = 11.sp, color = SoftLavender)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF4285F4).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Connect ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                            }
                        }
                    }

                    Text("Or enter university Google Workspace / Gmail:", fontSize = 11.sp, color = TextMuted)
                    OutlinedTextField(
                        value = customGoogleLinkEmail,
                        onValueChange = { customGoogleLinkEmail = it },
                        placeholder = { Text("username@campus.edu / gmail.com", fontSize = 12.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite,
                            focusedBorderColor = Color(0xFF4285F4),
                            unfocusedBorderColor = CosmicBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val emailToUse = customGoogleLinkEmail.trim().ifBlank { defaultLinkEmail }
                        val nameToUse = emailToUse.substringBefore("@").replace(".", " ")
                            .split(" ")
                            .filter { it.isNotBlank() }
                            .joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
                        showGoogleLinkDialog = false
                        viewModel.linkGoogleAccount(emailToUse, nameToUse)
                        viewModel.syncProfileWithGoogle()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                ) {
                    Text("Link & Sync Profile", color = StarWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoogleLinkDialog = false }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 720.dp
        val isNarrow = maxWidth < 480.dp

        if (isWideScreen) {
            // ==========================================
            // CANONICAL WIDE LAYOUT (TABLET / DESKTOP)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Left Pane: Sticky Profile Card + Category Nav + Status Pill
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Back & Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceCard)
                                .border(1.dp, theme.borderSubtle, CircleShape)
                                .testTag("settings_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = "Settings & Orbit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }

                    // User Summary Mini Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.borderGlow, theme.primary.copy(alpha = 0.4f))))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(theme.primaryVariant, theme.secondary)))
                                        .border(1.5.dp, theme.primary, CircleShape)
                                ) {
                                    if (!userStudent.avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = userStudent.avatarUrl,
                                            contentDescription = "Avatar",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(text = userStudent.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = StarWhite, fontSize = 15.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = userStudent.name, fontWeight = FontWeight.Bold, color = StarWhite, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = "${uiState.currentUserRole.badge} ${uiState.currentUserRole.title}", fontSize = 11.sp, color = theme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onNavigateToProfileHub,
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(36.dp).testTag("wide_edit_profile_hub")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF07090E), modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Profile Hub ➔", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF07090E))
                            }
                        }
                    }

                    // Category Navigation Items
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SettingsCategory.entries.forEach { cat ->
                            val isSelected = activeCategory == cat
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) theme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { activeCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = cat.icon, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cat.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StarWhite else SoftLavender
                                )
                            }
                        }
                    }

                    // Reset to Defaults Button
                    OutlinedButton(
                        onClick = { viewModel.resetAppSettingsToDefaults() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLavender),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle))),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("wide_reset_defaults")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Defaults", fontSize = 11.sp)
                    }
                }

                // Right Pane: Active Category Content
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    SettingsContentColumn(
                        activeCategory = activeCategory,
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        settings = settings,
                        uiState = uiState,
                        viewModel = viewModel,
                        theme = theme,
                        instructionsText = instructionsText,
                        onInstructionsChange = {
                            instructionsText = it
                            viewModel.setHandoverInstructions(it)
                        },
                        onNavigateToProfileHub = onNavigateToProfileHub,
                        onOpenDatabaseInspector = onOpenDatabaseInspector,
                        onShowResetCacheDialog = { showResetCacheDialog = true },
                        onShowDeleteAccountDialog = { showDeleteAccountDialog = true },
                        onShowGuidelinesDialog = { showGuidelinesDialog = true },
                        onShowPrivacyPolicyDialog = { showPrivacyPolicyDialog = true },
                        onShowTermsOfServiceDialog = { showTermsOfServiceDialog = true },
                        onShowGoogleLinkDialog = { showGoogleLinkDialog = true },
                        onSignOut = onSignOut,
                        isNarrow = false
                    )
                }
            }
        } else {
            // ==========================================
            // COMPACT PHONE LAYOUT
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(theme.surfaceCard)
                                .border(1.dp, theme.borderSubtle, CircleShape)
                                .testTag("settings_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite, modifier = Modifier.size(17.dp))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚙️", fontSize = 17.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Settings & Orbit",
                                    fontSize = if (isNarrow) 17.sp else 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                            }
                            Text(text = "Preferences, theme & privacy", fontSize = 11.sp, color = SoftLavender)
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetAppSettingsToDefaults() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLavender),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle))),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.testTag("reset_defaults_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Reset", fontSize = 10.5.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Pills Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SettingsCategory.entries.forEach { cat ->
                        val isSelected = activeCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) theme.primary.copy(alpha = 0.25f) else theme.surfaceElevated)
                                .border(
                                    width = if (isSelected) 1.dp else 0.6.dp,
                                    color = if (isSelected) theme.primary else theme.borderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { activeCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("cat_chip_${cat.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = cat.icon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = cat.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) StarWhite else SoftLavender
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Settings Content
                SettingsContentColumn(
                    activeCategory = activeCategory,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    settings = settings,
                    uiState = uiState,
                    viewModel = viewModel,
                    theme = theme,
                    instructionsText = instructionsText,
                    onInstructionsChange = {
                        instructionsText = it
                        viewModel.setHandoverInstructions(it)
                    },
                    onNavigateToProfileHub = onNavigateToProfileHub,
                    onOpenDatabaseInspector = onOpenDatabaseInspector,
                    onShowResetCacheDialog = { showResetCacheDialog = true },
                    onShowDeleteAccountDialog = { showDeleteAccountDialog = true },
                    onShowGuidelinesDialog = { showGuidelinesDialog = true },
                    onShowPrivacyPolicyDialog = { showPrivacyPolicyDialog = true },
                    onShowTermsOfServiceDialog = { showTermsOfServiceDialog = true },
                    onShowGoogleLinkDialog = { showGoogleLinkDialog = true },
                    onSignOut = onSignOut,
                    isNarrow = isNarrow
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsContentColumn(
    activeCategory: SettingsCategory,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    settings: com.example.model.AppSettings,
    uiState: UniSpaceUiState,
    viewModel: UniSpaceViewModel,
    theme: com.example.ui.theme.CosmicThemeTokens,
    instructionsText: String,
    onInstructionsChange: (String) -> Unit,
    onNavigateToProfileHub: () -> Unit,
    onOpenDatabaseInspector: () -> Unit,
    onShowResetCacheDialog: () -> Unit,
    onShowDeleteAccountDialog: () -> Unit,
    onShowGuidelinesDialog: () -> Unit,
    onShowPrivacyPolicyDialog: () -> Unit = {},
    onShowTermsOfServiceDialog: () -> Unit = {},
    onShowGoogleLinkDialog: () -> Unit,
    onSignOut: () -> Unit,
    isNarrow: Boolean
) {
    val userStudent = uiState.userProfile
    val listState = rememberLazyListState()

    fun matchesFilter(category: SettingsCategory, sectionTitle: String, keywords: String = ""): Boolean {
        if (activeCategory != SettingsCategory.ALL && activeCategory != category) return false
        if (searchQuery.isBlank()) return true
        val query = searchQuery.trim().lowercase()
        return sectionTitle.lowercase().contains(query) || keywords.lowercase().contains(query)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_content_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Quick Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search settings (e.g. theme, haptic, sync, meetup)...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SoftLavender, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.primary,
                    unfocusedBorderColor = theme.borderSubtle,
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite,
                    focusedContainerColor = theme.surfaceElevated,
                    unfocusedContainerColor = theme.surfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // 1. Account & Profile Hub Card
        if (matchesFilter(SettingsCategory.ACCOUNT, "Profile Hub User Account", "email name role college avatar photo")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(theme.borderGlow, theme.primaryVariant.copy(alpha = 0.4f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(theme.primaryVariant, theme.secondary)))
                                    .border(1.8.dp, theme.primary, CircleShape)
                            ) {
                                if (!userStudent.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = userStudent.avatarUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(text = if (userStudent.name.isNotBlank()) userStudent.name.take(2).uppercase() else "ME", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = userStudent.name.ifBlank { "Campus Explorer" }, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = StarWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = userStudent.roleTitle.ifBlank { "Student Account" }, fontSize = 12.sp, color = theme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "🎓 ${userStudent.college.ifBlank { "Campus Universe" }}", fontSize = 11.sp, color = SoftLavender, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.primary.copy(alpha = 0.2f))
                                    .border(0.8.dp, theme.primary.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "${uiState.currentUserRole.badge} ${uiState.currentUserRole.title}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = theme.borderSubtle)
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onNavigateToProfileHub,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("goto_profile_hub_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF07090E), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Details in Profile Hub ➔", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF07090E))
                        }
                    }
                }
            }
        }

        // 1.5. Google OAuth 2.0 Authorization & Identity Card
        if (matchesFilter(SettingsCategory.ACCOUNT, "Google OAuth 2.0 Cloud Account Security", "oauth google openid email profile auth")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF4285F4), Color(0xFF34A853)))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color(0xFF4285F4), CircleShape)
                            ) {
                                Text(text = "G", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF4285F4))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Google OAuth 2.0",
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (uiState.isGoogleConnected) Color(0x3310B981) else Color(0x336B7280))
                                            .border(0.8.dp, if (uiState.isGoogleConnected) Color(0xFF10B981) else Color(0xFF9CA3AF), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (uiState.isGoogleConnected) "● Connected" else "○ Not Linked",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (uiState.isGoogleConnected) Color(0xFF34D399) else SoftLavender
                                        )
                                    }
                                }
                                Text(
                                    text = if (uiState.isGoogleConnected && uiState.googleAccountEmail.isNotBlank()) uiState.googleAccountEmail else "Link your campus Google workspace",
                                    fontSize = 12.sp,
                                    color = SoftLavender
                                )
                            }
                        }

                        HorizontalDivider(color = theme.borderSubtle)

                        // Granted Scopes
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Authorized Scopes (Project: ${uiState.googleProjectId}):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted
                            )
                            uiState.googleOAuthScopes.forEach { scope ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = scope,
                                        fontSize = 10.5.sp,
                                        color = SoftLavender,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.syncProfileWithGoogle() },
                                modifier = Modifier.weight(1f).testTag("sync_google_profile_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated),
                                border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFF4285F4), theme.primary))),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Sync Google Profile", fontSize = 11.5.sp, color = StarWhite, fontWeight = FontWeight.Bold)
                            }

                            if (uiState.isGoogleConnected) {
                                OutlinedButton(
                                    onClick = { viewModel.unlinkGoogleAccount() },
                                    modifier = Modifier.weight(0.7f).testTag("unlink_google_button"),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDA4AF)),
                                    border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFF9F1239)))),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Unlink", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = onShowGoogleLinkDialog,
                                    modifier = Modifier.weight(0.7f).testTag("link_google_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Link Account", fontSize = 11.sp, color = StarWhite, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Campus Role Switcher
        if (matchesFilter(SettingsCategory.ACCOUNT, "Role & Identity", "student seller creator admin")) {
            item {
                SettingsSectionHeader(title = "Campus Role & Identity Orbit", icon = Icons.Default.AccountCircle, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle)))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Choose your operational permission tier in UniSpaceX:", fontSize = 11.5.sp, color = SoftLavender)

                        UserRole.entries.forEach { role ->
                            val isSelected = uiState.currentUserRole == role
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) theme.primary.copy(alpha = 0.2f) else theme.surfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.2.dp else 0.8.dp,
                                        color = if (isSelected) theme.primary else theme.borderSubtle,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setUserRole(role) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("role_option_${role.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = role.badge, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = role.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) StarWhite else SoftLavender)
                                    Text(text = role.description, fontSize = 10.5.sp, color = TextMuted, lineHeight = 14.sp)
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier.size(22.dp).clip(CircleShape).background(theme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF07090E), modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Theme & Atmosphere Settings (ENHANCED LIVE PREVIEW)
        if (matchesFilter(SettingsCategory.APPEARANCE, "Theme Atmosphere Cosmic Dark AMOLED Nebula Glow", "theme color accent style dark black")) {
            item {
                SettingsSectionHeader(title = "Theme Atmosphere & Cosmic Glow", icon = Icons.Default.Palette, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.borderGlow, theme.borderSubtle)))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Theme Mode with Rich Palette Cards
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Visual Atmosphere", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
                                Text(text = settings.themeMode, fontSize = 11.sp, color = theme.primary, fontWeight = FontWeight.Bold)
                            }

                            // Theme Selector Cards (Responsive FlowRow or Columns)
                            val themeOptions = listOf(
                                Triple("Glassmorphism", "Frosted Acrylic & Ambient Aurora", listOf(Color(0x501E2E4E), Color(0x38FFFFFF), Color(0xFF38BDF8))),
                                Triple("Cosmic Dark", "Deep Midnight Obsidian", listOf(Color(0xFF090D16), Color(0xFF172138), theme.primary)),
                                Triple("AMOLED Deep", "Pure OLED True Black", listOf(Color(0xFF000000), Color(0xFF101016), StarWhite)),
                                Triple("Nebula Glow", "Cyberpunk Violet Stardust", listOf(Color(0xFF0C071E), Color(0xFF1E1347), Color(0xFFC084FC)))
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                themeOptions.forEach { (tName, tSub, palette) ->
                                    val isSelected = settings.themeMode == tName
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .widthIn(min = 140.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) theme.primary.copy(alpha = 0.2f) else theme.surfaceElevated)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.8.dp,
                                                color = if (isSelected) theme.primary else theme.borderSubtle,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.updateThemeMode(tName) }
                                            .padding(10.dp)
                                            .testTag("theme_option_${tName.replace(" ", "_").lowercase()}")
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = tName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) StarWhite else SoftLavender)
                                                // Mini visual palette swatches
                                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    palette.forEach { c ->
                                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(c))
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(text = tSub, fontSize = 9.5.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = theme.borderSubtle)

                        // Cosmic Accent Glow Hue Selector
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Accent Glow Hue", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
                                Text(text = settings.accentGlowColor, fontSize = 11.sp, color = theme.primary, fontWeight = FontWeight.Bold)
                            }

                            val hues = listOf(
                                "Electric Cyan" to CosmicCyan,
                                "Cosmic Violet" to Color(0xFF8B5CF6),
                                "Solar Gold" to CelestialGold,
                                "Supernova Rose" to NeonPink
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                hues.forEach { (name, color) ->
                                    val isSelected = settings.accentGlowColor == name
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
                                            .border(
                                                width = if (isSelected) 1.dp else 0.dp,
                                                color = if (isSelected) color else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { viewModel.updateAccentGlowColor(name) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("accent_color_${name.replace(" ", "_").lowercase()}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .border(
                                                    width = if (isSelected) 2.5.dp else 1.dp,
                                                    color = if (isSelected) StarWhite else Color.Transparent,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = StarWhite, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = name.split(" ").first(), fontSize = 10.5.sp, color = if (isSelected) StarWhite else TextMuted)
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = theme.borderSubtle)

                        // Density Control
                        SettingsToggleRow(
                            title = "High-Density Compact Layout",
                            subtitle = "Fit more products, campus ventures and crew bounty cards",
                            checked = settings.compactDensity,
                            onCheckedChange = { viewModel.toggleCompactDensity(it) },
                            testTag = "toggle_compact_density",
                            theme = theme
                        )
                    }
                }
            }
        }

        // 4. Alerts & Radar
        if (matchesFilter(SettingsCategory.NOTIFICATIONS, "Alerts Radar Notifications", "push order crew message drop haptic")) {
            item {
                SettingsSectionHeader(title = "Campus Alerts & Radar Telemetry", icon = Icons.Default.Notifications, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle)))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsToggleRow(
                            title = "Push Notifications Master",
                            subtitle = "Receive live mission updates when backgrounded",
                            checked = settings.pushNotificationsEnabled,
                            onCheckedChange = { viewModel.togglePushNotifications(it) },
                            testTag = "toggle_push_notifications",
                            theme = theme
                        )

                        AnimatedVisibility(visible = settings.pushNotificationsEnabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                HorizontalDivider(color = theme.borderSubtle)
                                SettingsToggleRow("Order & Quote Milestones", "Accepted quotes & handshake readiness", settings.orderAlertsEnabled, { viewModel.toggleOrderAlerts(it) }, "toggle_order_alerts", theme)
                                HorizontalDivider(color = theme.borderSubtle)
                                SettingsToggleRow("Crew Applications & Recruitment", "Pitches from prospective co-founders", settings.crewApplicationAlertsEnabled, { viewModel.toggleCrewAlerts(it) }, "toggle_crew_alerts", theme)
                                HorizontalDivider(color = theme.borderSubtle)
                                SettingsToggleRow("Direct Peer Messages", "Gigs, orders & project queries", settings.chatAlertsEnabled, { viewModel.toggleChatAlerts(it) }, "toggle_chat_alerts", theme)
                                HorizontalDivider(color = theme.borderSubtle)
                                SettingsToggleRow("Campus Drops & Event Radar", "Hackathons, symposiums & drops", settings.eventRadarAlertsEnabled, { viewModel.toggleEventRadarAlerts(it) }, "toggle_event_alerts", theme)
                            }
                        }

                        HorizontalDivider(color = theme.borderSubtle)
                        SettingsToggleRow("Haptic Tactile Feedback", "Vibration on button presses & quote confirmations", settings.hapticFeedbackEnabled, { viewModel.toggleHapticFeedback(it) }, "toggle_haptic_feedback", theme)
                    }
                }
            }
        }

        // 5. Meetup & Trading
        if (matchesFilter(SettingsCategory.TRADING, "Campus Meetup Handover Trading", "location landmark payment upi cash note")) {
            item {
                SettingsSectionHeader(title = "Campus Handover & Trading Hub", icon = Icons.Default.LocationOn, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle)))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Default Campus Meetup Landmark", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)

                        // Locations Grid
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.meetupLocations.forEach { loc ->
                                val isSelected = settings.defaultMeetupLocationId == loc.id
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .widthIn(min = 140.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) theme.primary.copy(alpha = 0.2f) else theme.surfaceElevated)
                                        .border(
                                            width = if (isSelected) 1.2.dp else 0.6.dp,
                                            color = if (isSelected) theme.primary else theme.borderSubtle,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { viewModel.setDefaultMeetupLocation(loc.id) }
                                        .padding(10.dp)
                                        .testTag("location_option_${loc.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = loc.icon, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = loc.name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StarWhite, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(text = loc.landmarkDesc, fontSize = 9.5.sp, color = SoftLavender, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = theme.primary, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = theme.borderSubtle)

                        // Payment Methods
                        Text(text = "Default Payment Method", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
                        CampusPaymentMethod.entries.forEach { method ->
                            val isSelected = settings.defaultPaymentMethod == method
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) theme.primary.copy(alpha = 0.2f) else theme.surfaceElevated)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.6.dp,
                                        color = if (isSelected) theme.primary else theme.borderSubtle,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setDefaultPaymentMethod(method) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("payment_method_${method.name.lowercase()}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = method.icon, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = method.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                    Text(text = method.subtitle, fontSize = 10.sp, color = SoftLavender)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = theme.borderSubtle)

                        // Handover Instructions Field
                        Text(text = "Handover Meetup Note", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
                        OutlinedTextField(
                            value = instructionsText,
                            onValueChange = onInstructionsChange,
                            placeholder = { Text("e.g. Meet outside central library foyer or main gate", color = TextMuted, fontSize = 11.5.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("handover_instructions_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = theme.primary,
                                unfocusedBorderColor = theme.borderSubtle,
                                focusedTextColor = StarWhite,
                                unfocusedTextColor = StarWhite,
                                focusedContainerColor = theme.surfaceElevated,
                                unfocusedContainerColor = theme.surfaceElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // 6. Privacy & Security
        if (matchesFilter(SettingsCategory.PRIVACY, "Privacy Security Visibility Lock", "visibility directory email message biometric lock")) {
            item {
                SettingsSectionHeader(title = "Privacy, Security & Visibility", icon = Icons.Default.Security, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle)))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SettingsToggleRow("Public Student Directory", "Discoverable in Explore & Crew recruitment", settings.isPublicProfileVisible, { viewModel.togglePublicProfile(it) }, "toggle_public_profile", theme)
                        HorizontalDivider(color = theme.borderSubtle)
                        SettingsToggleRow("Display College Email", "Show verified institutional email to campus peers", settings.showCollegeEmailOnProfile, { viewModel.toggleShowCollegeEmail(it) }, "toggle_show_email", theme)
                        HorizontalDivider(color = theme.borderSubtle)
                        SettingsToggleRow("Allow Direct Peer DMs", "Permit students without active orders to chat", settings.allowDirectMessages, { viewModel.toggleAllowDirectMessages(it) }, "toggle_allow_dms", theme)
                        HorizontalDivider(color = theme.borderSubtle)
                        SettingsToggleRow("Biometric / Campus App Lock", "Require fingerprint or screen lock to open UniSpaceX", settings.biometricAppLock, { viewModel.toggleBiometricAppLock(it) }, "toggle_biometric_lock", theme)
                    }
                }
            }
        }

        // 7. Local Database & Room Persistence
        if (matchesFilter(SettingsCategory.DATA, "Database Storage Room Cloud Sync Cache", "room sqlite firestore sync cache inspector")) {
            item {
                SettingsSectionHeader(title = "Local SQLite Cache & Cloud Persistence", icon = Icons.Default.Storage, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle)))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Room SQLite Database", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                Text(text = uiState.syncStatusText, fontSize = 11.sp, color = theme.primary)
                            }
                            if (uiState.isSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = theme.primary, strokeWidth = 2.dp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.triggerRoomToFirestoreSync() },
                                modifier = Modifier.weight(1f).testTag("trigger_sync_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !uiState.isSyncing
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF07090E), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sync Now", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF07090E))
                            }

                            OutlinedButton(
                                onClick = onOpenDatabaseInspector,
                                modifier = Modifier.weight(1f).testTag("open_db_inspector_button"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StarWhite),
                                border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderGlow))),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(14.dp), tint = theme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Inspector", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onShowResetCacheDialog,
                            modifier = Modifier.fillMaxWidth().testTag("clear_cache_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLavender),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(theme.borderSubtle, theme.border))),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Temporary Cache", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 8. About & Community Code
        if (matchesFilter(SettingsCategory.ABOUT, "About UniSpaceX Guidelines Honor Code", "about guidelines version rules terms")) {
            item {
                SettingsSectionHeader(title = "About UniSpaceX & Honor Code", icon = Icons.Default.Info, theme = theme)
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(theme.border, theme.borderSubtle)))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "App Edition", fontSize = 12.sp, color = SoftLavender)
                            Text(text = "v2.4.0 (Cosmic Campus Orbit)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                        }
                        HorizontalDivider(color = theme.borderSubtle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onShowGuidelinesDialog() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Campus Commerce Guidelines & Honor Code", fontSize = 12.sp, color = theme.primary)
                            Text(text = "➔", fontSize = 14.sp, color = theme.primary)
                        }
                        HorizontalDivider(color = theme.borderSubtle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onShowPrivacyPolicyDialog() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Privacy Policy & Student Data Protection", fontSize = 12.sp, color = theme.primary)
                            Text(text = "➔", fontSize = 14.sp, color = theme.primary)
                        }
                        HorizontalDivider(color = theme.borderSubtle)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onShowTermsOfServiceDialog() }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Terms of Service & UGC Community Rules", fontSize = 12.sp, color = theme.primary)
                            Text(text = "➔", fontSize = 14.sp, color = theme.primary)
                        }
                        HorizontalDivider(color = theme.borderSubtle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Verified Student Network", fontSize = 12.sp, color = SoftLavender)
                            Text(text = "Active & Encrypted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }
            }
        }

        // 9. Danger Zone
        if (matchesFilter(SettingsCategory.ACCOUNT, "Session Danger Zone Exit Delete", "signout logout delete exit orbit")) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0A10)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFF9F1239))))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(text = "Session & Danger Zone", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFDA4AF))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onSignOut,
                                modifier = Modifier.weight(1f).testTag("settings_sign_out_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoftLavender)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Exit Orbit", fontSize = 11.5.sp, color = StarWhite)
                            }

                            Button(
                                onClick = onShowDeleteAccountDialog,
                                modifier = Modifier.weight(1f).testTag("settings_delete_profile_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp), tint = StarWhite)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete Profile", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector,
    theme: com.example.ui.theme.CosmicThemeTokens
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = theme.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = StarWhite
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    theme: com.example.ui.theme.CosmicThemeTokens
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
            Text(text = subtitle, fontSize = 10.5.sp, color = TextMuted, lineHeight = 14.sp)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = StarWhite,
                checkedTrackColor = theme.primary,
                uncheckedThumbColor = SoftLavender,
                uncheckedTrackColor = theme.surfaceElevated
            )
        )
    }
}
