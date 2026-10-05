package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.model.UserRole
import com.example.model.VerificationType
import com.example.ui.components.CosmicBadgeRow
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileHubScreen(
    uiState: UniSpaceUiState,
    viewModel: UniSpaceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalCosmicTheme.current
    val initialProfile = uiState.userProfile

    BackHandler {
        onBack()
    }

    // Form fields
    var name by remember(initialProfile.id) { mutableStateOf(initialProfile.name) }
    var roleTitle by remember(initialProfile.id) { mutableStateOf(initialProfile.roleTitle) }
    var college by remember(initialProfile.id) { mutableStateOf(initialProfile.college) }
    var departmentYear by remember(initialProfile.id) { mutableStateOf(initialProfile.departmentYear ?: "") }
    var rollNumber by remember(initialProfile.id) { mutableStateOf(initialProfile.rollNumber ?: "") }
    var collegeEmail by remember(initialProfile.id) { mutableStateOf(initialProfile.collegeEmail ?: uiState.loggedInEmail) }
    var bio by remember(initialProfile.id) { mutableStateOf(initialProfile.bio) }
    var statusMessage by remember(initialProfile.id) { mutableStateOf(initialProfile.statusMessage ?: "") }
    var avatarUrl by remember(initialProfile.id) { mutableStateOf(initialProfile.avatarUrl) }
    var githubUrl by remember(initialProfile.id) { mutableStateOf(initialProfile.githubUrl ?: "") }
    var linkedinUrl by remember(initialProfile.id) { mutableStateOf(initialProfile.linkedinUrl ?: "") }
    var portfolioUrl by remember(initialProfile.id) { mutableStateOf(initialProfile.portfolioUrl ?: "") }

    // Achievements dynamic list
    val achievements = remember(initialProfile.id) {
        mutableStateListOf<String>().apply { addAll(initialProfile.achievements) }
    }
    var newAchievementInput by remember { mutableStateOf("") }

    // Quick Skill addition
    var newSkillName by remember { mutableStateOf("") }
    var newSkillLevel by remember { mutableStateOf("Advanced") }

    // Seller / Venture fields
    val sellerBiz = uiState.businesses.firstOrNull { it.ownerName == initialProfile.name }
    var bizName by remember(sellerBiz?.id) { mutableStateOf(sellerBiz?.name ?: "") }
    var bizTagline by remember(sellerBiz?.id) { mutableStateOf(sellerBiz?.tagline ?: "") }
    var bizAbout by remember(sellerBiz?.id) { mutableStateOf(sellerBiz?.about ?: "") }

    var customAvatarInput by remember { mutableStateOf("") }
    var showCustomAvatarUrlInput by remember { mutableStateOf(false) }
    var showSavedBanner by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUrl = uri.toString()
        }
    }

    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&auto=format&fit=crop&q=80"
    )

    val campusSuggestions = listOf(
        "S.A. Engineering College",
        "IIT Madras Research Park",
        "Anna University Guindy",
        "SRM University Tech Park",
        "Venture Orbit Campus"
    )

    fun saveAllDetails() {
        viewModel.updateUserProfileDetails(
            name = name,
            roleTitle = roleTitle,
            college = college,
            departmentYear = departmentYear,
            rollNumber = rollNumber,
            collegeEmail = collegeEmail,
            bio = bio,
            statusMessage = statusMessage,
            githubUrl = githubUrl,
            linkedinUrl = linkedinUrl,
            portfolioUrl = portfolioUrl,
            avatarUrl = avatarUrl
        )
        viewModel.updateStudentAchievements(achievements.toList())
        if (sellerBiz != null) {
            viewModel.adminEditBusiness(
                sellerBiz.id,
                newName = bizName,
                newCollege = college,
                newTagline = bizTagline,
                newAbout = bizAbout
            )
        }
        showSavedBanner = true
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isNarrow = maxWidth < 480.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter)
                .testTag("profile_hub_screen"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar with Back and Save
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CosmicSurfaceCard)
                                .border(1.dp, CosmicBorderSubtle, CircleShape)
                                .testTag("profile_hub_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = StarWhite,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🛰️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Profile Hub",
                                    fontSize = if (isNarrow) 18.sp else 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                            }
                            Text(
                                text = "Edit your campus identity, credentials & superpowers",
                                fontSize = 11.5.sp,
                                color = SoftLavender
                            )
                        }
                    }

                    // Primary Save Changes Button
                    Button(
                        onClick = { saveAllDetails() },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("save_profile_hub_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF07090E), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF07090E)
                        )
                    }
                }
            }

            // Saved Confirmation Banner
            if (showSavedBanner) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D2818)),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Profile successfully updated across Campus Orbit!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD1FAE5)
                                )
                            }
                            IconButton(onClick = { showSavedBanner = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = SoftLavender, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // 1. Interactive Live Cosmic Preview Card
            item {
                Text(
                    text = "Live Campus Card Preview",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftLavender
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(CosmicBorderGlow, ElectricViolet.copy(alpha = 0.5f)))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(listOf(CosmicPurple, CometBlue)),
                                        shape = CircleShape
                                    )
                                    .border(2.dp, CosmicCyan, CircleShape)
                            ) {
                                if (!avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = name.take(2).uppercase().ifBlank { "US" },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = name.ifBlank { "Student Name" },
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "⭐ 5.0", fontSize = 11.sp, color = CelestialGold, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = roleTitle.ifBlank { "Headline / Role Title" },
                                    fontSize = 12.sp,
                                    color = CosmicCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "🎓 ${college.ifBlank { "Campus" }} · $departmentYear",
                                    fontSize = 11.sp,
                                    color = SoftLavender,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (statusMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricViolet.copy(alpha = 0.2f))
                                    .border(0.6.dp, CosmicCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = statusMessage,
                                    fontSize = 11.5.sp,
                                    color = CosmicCyan
                                )
                            }
                        }

                        if (bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = bio,
                                fontSize = 12.sp,
                                color = SoftLavender,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        CosmicBadgeRow(badges = initialProfile.badges)
                    }
                }
            }

            // 2. Avatar & Photo Studio
            item {
                ProfileSectionCard(title = "Cosmic Photo & Avatar Studio", icon = Icons.Default.CameraAlt) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("hub_gallery_pick_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Photo", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showCustomAvatarUrlInput = !showCustomAvatarUrlInput },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicCyan),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(CosmicBorder, CosmicCyan.copy(alpha = 0.5f)))
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("hub_custom_url_button")
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Image URL", fontSize = 11.5.sp)
                            }

                            if (!avatarUrl.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = { avatarUrl = null },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFDA4AF)),
                                    border = ButtonDefaults.outlinedButtonBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C)))
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("hub_reset_avatar_button")
                                ) {
                                    Text("Reset", fontSize = 11.5.sp)
                                }
                            }
                        }

                        // Google OAuth Sync Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF13192B))
                                .border(1.dp, Color(0xFF4285F4).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable {
                                    if (uiState.isGoogleConnected) {
                                        if (uiState.googleAccountName.isNotBlank()) name = uiState.googleAccountName
                                        if (uiState.googleAccountEmail.isNotBlank()) collegeEmail = uiState.googleAccountEmail
                                        uiState.googleAccountAvatarUrl?.let { avatarUrl = it }
                                        viewModel.syncProfileWithGoogle()
                                        showSavedBanner = true
                                    } else {
                                        viewModel.linkGoogleAccount()
                                        if (uiState.googleAccountName.isNotBlank()) name = uiState.googleAccountName
                                        if (uiState.googleAccountEmail.isNotBlank()) collegeEmail = uiState.googleAccountEmail
                                        showSavedBanner = true
                                    }
                                }
                                .padding(10.dp)
                                .testTag("hub_google_oauth_sync_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    ) {
                                        Text(text = "G", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFF4285F4))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "Auto-fill with Google OAuth 2.0", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                        Text(text = if (uiState.googleAccountEmail.isNotBlank()) "Imports ${uiState.googleAccountEmail}" else "Imports verified Google identity", fontSize = 10.sp, color = SoftLavender)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF4285F4).copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "Sync Now", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF93C5FD))
                                }
                            }
                        }

                        AnimatedVisibility(visible = showCustomAvatarUrlInput) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customAvatarInput,
                                    onValueChange = { customAvatarInput = it },
                                    placeholder = { Text("https://example.com/avatar.jpg", color = TextMuted, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f).testTag("custom_avatar_url_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CosmicCyan,
                                        unfocusedBorderColor = CosmicBorderSubtle,
                                        focusedTextColor = StarWhite,
                                        unfocusedTextColor = StarWhite,
                                        focusedContainerColor = CosmicSurfaceElevated,
                                        unfocusedContainerColor = CosmicSurfaceElevated
                                    ),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        if (customAvatarInput.isNotBlank()) {
                                            avatarUrl = customAvatarInput
                                            showCustomAvatarUrlInput = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Set", color = Color(0xFF07090E), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }

                        // Preset Cosmic Avatars
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Or choose a Cosmic Orbit Preset:", fontSize = 11.5.sp, color = TextMuted)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                presetAvatars.forEachIndexed { idx, url ->
                                    val isSelected = avatarUrl == url
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) CosmicCyan else CosmicBorderSubtle,
                                                shape = CircleShape
                                            )
                                            .clickable { avatarUrl = url }
                                            .testTag("preset_avatar_$idx")
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Preset Avatar $idx",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Personal & Academic Credentials
            item {
                ProfileSectionCard(title = "Personal & Academic Credentials", icon = Icons.Default.School) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Full Name
                        HubFormField(
                            label = "Full Name",
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "e.g. Alex Morgan",
                            testTag = "input_full_name"
                        )

                        // Role Title / Headline
                        HubFormField(
                            label = "Role Title / Campus Headline",
                            value = roleTitle,
                            onValueChange = { roleTitle = it },
                            placeholder = "e.g. Software & Hardware Engineer · Co-Founder",
                            testTag = "input_role_title"
                        )

                        // College Planet Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "College / University Planet", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
                            OutlinedTextField(
                                value = college,
                                onValueChange = { college = it },
                                modifier = Modifier.fillMaxWidth().testTag("input_college"),
                                shape = RoundedCornerShape(10.dp),
                                colors = defaultHubTextFieldColors(),
                                singleLine = true
                            )

                            // Campus Quick Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                campusSuggestions.forEach { camp ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (college == camp) CosmicCyan.copy(alpha = 0.2f) else CosmicSurfaceElevated)
                                            .border(
                                                width = 0.8.dp,
                                                color = if (college == camp) CosmicCyan else CosmicBorderSubtle,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { college = camp }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = camp, fontSize = 10.5.sp, color = if (college == camp) CosmicCyan else SoftLavender)
                                    }
                                }
                            }
                        }

                        // Department & Year + Roll Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1.2f)) {
                                HubFormField(
                                    label = "Department & Year",
                                    value = departmentYear,
                                    onValueChange = { departmentYear = it },
                                    placeholder = "e.g. B.Tech Computer Science · 3rd Year",
                                    testTag = "input_department_year"
                                )
                            }
                            Box(modifier = Modifier.weight(0.8f)) {
                                HubFormField(
                                    label = "Roll / Student ID",
                                    value = rollNumber,
                                    onValueChange = { rollNumber = it },
                                    placeholder = "e.g. 23CS042",
                                    testTag = "input_roll_number"
                                )
                            }
                        }

                        // Campus Institutional Email
                        HubFormField(
                            label = "Institutional College Email",
                            value = collegeEmail,
                            onValueChange = { collegeEmail = it },
                            placeholder = "e.g. student@university.edu",
                            testTag = "input_college_email"
                        )
                    }
                }
            }

            // 3.5. Institutional & Marketplace Verification Status
            item {
                val isSheerIdActive = initialProfile.isSheerIdVerified || initialProfile.badges.contains(com.example.model.VerificationType.STUDENT_VERIFIED)
                val isSellerActive = initialProfile.isSellerVerified || uiState.currentUserRole == com.example.model.UserRole.SELLER
                val pendingSeller = uiState.verificationRequests.firstOrNull { it.studentId == initialProfile.id && it.type == "SELLER_GOVT_ID" && it.status == "PENDING" }

                ProfileSectionCard(title = "Campus Verification & Credentials", icon = Icons.Default.Security) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // SheerID Student Status Row
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isSheerIdActive) CosmicPurple.copy(alpha = 0.2f) else theme.surfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSheerIdActive) CosmicCyan else theme.borderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = if (isSheerIdActive) "🎓" else "🛡️", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isSheerIdActive) "SheerID Student Verified ✓" else "SheerID Student Verification",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSheerIdActive) CosmicCyan else StarWhite
                                        )
                                    }
                                    if (isSheerIdActive) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(initialProfile.universityEmailStatus.badgeColorHex).copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = initialProfile.universityEmailStatus.label,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(initialProfile.universityEmailStatus.badgeColorHex)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = initialProfile.sheerIdValidityExpiry ?: "Active Enrollment",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isSheerIdActive) {
                                        "Institutional email confirmed in Firestore. Validity dates synchronized with user Auth profile."
                                    } else {
                                        "Activate official student verified badge by confirming your college email under SheerID policy & terms."
                                    },
                                    fontSize = 10.5.sp,
                                    color = SoftLavender
                                )
                                if (isSheerIdActive) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            if (!initialProfile.collegeEmail.isNullOrBlank()) {
                                                Text(
                                                    text = "📧 ${initialProfile.collegeEmail}",
                                                    fontSize = 10.sp,
                                                    color = StarWhite
                                                )
                                            }
                                            if (!initialProfile.rollNumber.isNullOrBlank()) {
                                                Text(
                                                    text = "🆔 Roll: ${initialProfile.rollNumber}",
                                                    fontSize = 10.sp,
                                                    color = SoftLavender
                                                )
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.extendStudentValidity("2028") },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicCyan),
                                            border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text("Extend Validity ⏳", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (!isSheerIdActive) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.openVerificationSheet() },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Verify Student ID via SheerID 🛡️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                    }
                                }
                            }
                        }

                        // 1. Role Mode Selection Card (When Student Verification Approved)
                        if (isSheerIdActive) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, CosmicBorderGlow)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = "✨", fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Verified Member Orbit: Choose Your Role",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CelestialGold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CosmicPurple.copy(alpha = 0.3f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (uiState.currentUserRole == com.example.model.UserRole.SELLER) "Active: SELLER" else "Active: STUDENT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = CosmicCyan
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Student verification approved! You can operate as a Campus Student (buyer/innovator) or Campus Seller (storefront owner/creator). Updates sync lively to Admin.",
                                        fontSize = 10.5.sp,
                                        color = SoftLavender
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Option 1: Student Mode
                                        val isCurrentStudent = uiState.currentUserRole == com.example.model.UserRole.STUDENT
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.selectCandidateRole(com.example.model.UserRole.STUDENT) },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isCurrentStudent) CosmicPurple.copy(alpha = 0.35f) else theme.surfaceElevated
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(if (isCurrentStudent) 1.5.dp else 1.dp, if (isCurrentStudent) CosmicCyan else theme.borderSubtle)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("🎓", fontSize = 20.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Student Mode", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isCurrentStudent) CosmicCyan else StarWhite)
                                                Text("Buyer & Peer", fontSize = 9.5.sp, color = TextMuted)
                                                if (isCurrentStudent) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Active ✓", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                                }
                                            }
                                        }

                                        // Option 2: Seller Mode
                                        val isCurrentSeller = uiState.currentUserRole == com.example.model.UserRole.SELLER
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.selectCandidateRole(com.example.model.UserRole.SELLER, businessName = bizName, whatsappNumber = initialProfile.sellerWhatsappNumber ?: "") },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isCurrentSeller) Color(0xFF2C1E3D) else theme.surfaceElevated
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(if (isCurrentSeller) 1.5.dp else 1.dp, if (isCurrentSeller) CelestialGold else theme.borderSubtle)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("🛍️", fontSize = 20.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Seller Mode", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isCurrentSeller) CelestialGold else StarWhite)
                                                Text("Store & Creator", fontSize = 9.5.sp, color = TextMuted)
                                                if (isCurrentSeller) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Active ✓", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = CelestialGold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Seller Verification Status Row
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = if (isSellerActive) Color(0xFF1E1430) else theme.surfaceElevated),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isSellerActive) Color(0xFFFFD54F) else theme.borderSubtle)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = if (isSellerActive) "🛍️" else "🏪", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = when {
                                                isSellerActive -> "Verified Campus Seller ✓"
                                                isSheerIdActive -> "Seller Privileges Available (Student Verified)"
                                                pendingSeller != null -> "Seller Application Under Review ⏳"
                                                else -> "Campus Seller Verification"
                                            },
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSellerActive) Color(0xFFFFD54F) else StarWhite
                                        )
                                    }
                                    if (isSellerActive) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFD97706).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Admin Approved", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = when {
                                        isSellerActive -> "Store active. WhatsApp: ${initialProfile.sellerWhatsappNumber ?: "Connected"} · Status: Verified Campus Seller"
                                        isSheerIdActive -> "Your collegiate identity is already approved! You can switch to Seller Mode anytime to create listings and launch your storefront."
                                        pendingSeller != null -> "Government ID (${pendingSeller.governmentIdType ?: "Govt ID"}), WhatsApp & product samples are awaiting Admin approval."
                                        else -> "To become an approved seller, complete student verification first or submit seller verification."
                                    },
                                    fontSize = 10.5.sp,
                                    color = SoftLavender
                                )
                                if (!isSellerActive) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (isSheerIdActive) {
                                                viewModel.selectCandidateRole(
                                                    com.example.model.UserRole.SELLER,
                                                    businessName = bizName,
                                                    whatsappNumber = initialProfile.sellerWhatsappNumber ?: ""
                                                )
                                            } else {
                                                viewModel.openSellerVerificationModal()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isSheerIdActive) "Activate Seller Mode 🛍️" else "Apply for Seller Verification ➔",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Bio & Mission Statement
            item {
                ProfileSectionCard(title = "Mission Statement & Bio", icon = Icons.Default.Visibility) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HubFormField(
                            label = "Status / Availability Badge",
                            value = statusMessage,
                            onValueChange = { statusMessage = it },
                            placeholder = "e.g. ⚡ Available for freelance robotics & UI design",
                            testTag = "input_status_message"
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Elevator Pitch / Bio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
                                Text(text = "${bio.length}/350", fontSize = 10.sp, color = if (bio.length > 300) CelestialGold else TextMuted)
                            }
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { if (it.length <= 350) bio = it },
                                placeholder = { Text("Describe your craft, ventures founded, and what you love building on campus...", color = TextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("input_bio"),
                                shape = RoundedCornerShape(10.dp),
                                colors = defaultHubTextFieldColors(),
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                    }
                }
            }

            // 5. Skills Constellation & Superpowers
            item {
                ProfileSectionCard(title = "Skills & Superpowers", icon = Icons.Default.Psychology) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Currently Mastered Skills:", fontSize = 12.sp, color = SoftLavender)

                        // Chips of existing skills
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            initialProfile.skills.forEach { skill ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ElectricViolet.copy(alpha = 0.25f))
                                        .border(0.8.dp, CosmicCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${skill.name} · ${skill.level}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove skill",
                                        tint = Color(0xFFFDA4AF),
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.removeSkillNode(skill.id) }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = CosmicBorderSubtle)

                        // Add new skill row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSkillName,
                                onValueChange = { newSkillName = it },
                                placeholder = { Text("Add skill (e.g. Flutter, Kotlin, PCB)", color = TextMuted, fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f).testTag("input_new_skill"),
                                shape = RoundedCornerShape(10.dp),
                                colors = defaultHubTextFieldColors(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (newSkillName.isNotBlank()) {
                                        viewModel.addSkillNode(newSkillName.trim(), "Tech", newSkillLevel)
                                        newSkillName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_skill_hub_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Quick recommendations
                        val suggestions = listOf("Jetpack Compose", "Hardware PCB", "React Native", "Figma Design", "Embedded C", "Python AI")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            suggestions.forEach { sug ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CosmicSurfaceElevated)
                                        .border(0.6.dp, CosmicBorderSubtle, RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.addSkillNode(sug, "Tech", "Advanced")
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = "+ $sug", fontSize = 10.5.sp, color = CosmicCyan)
                                }
                            }
                        }
                    }
                }
            }

            // 6. Campus Achievements & Milestones
            item {
                ProfileSectionCard(title = "Campus Honors & Achievements", icon = Icons.Default.EmojiEvents) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        achievements.forEachIndexed { index, ach ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CosmicSurfaceElevated)
                                    .border(0.6.dp, CosmicBorderSubtle, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🏆", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ach,
                                    fontSize = 12.sp,
                                    color = StarWhite,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { achievements.removeAt(index) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color(0xFFFDA4AF), modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newAchievementInput,
                                onValueChange = { newAchievementInput = it },
                                placeholder = { Text("e.g. 1st Place Smart Campus Hackathon", color = TextMuted, fontSize = 11.5.sp) },
                                modifier = Modifier.weight(1f).testTag("input_new_achievement"),
                                shape = RoundedCornerShape(10.dp),
                                colors = defaultHubTextFieldColors(),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    if (newAchievementInput.isNotBlank()) {
                                        achievements.add(newAchievementInput.trim())
                                        newAchievementInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CelestialGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_achievement_hub_button")
                            ) {
                                Text("+ Add", color = Color(0xFF07090E), fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                    }
                }
            }

            // 7. Social Profiles & Portfolio Showcase
            item {
                ProfileSectionCard(title = "Social Profiles & Portfolio Showcase", icon = Icons.Default.Link) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HubFormField(
                            label = "GitHub Profile URL",
                            value = githubUrl,
                            onValueChange = { githubUrl = it },
                            placeholder = "https://github.com/yourhandle",
                            testTag = "input_github_url"
                        )
                        HubFormField(
                            label = "LinkedIn Profile URL",
                            value = linkedinUrl,
                            onValueChange = { linkedinUrl = it },
                            placeholder = "https://linkedin.com/in/yourhandle",
                            testTag = "input_linkedin_url"
                        )
                        HubFormField(
                            label = "Portfolio / Personal Website URL",
                            value = portfolioUrl,
                            onValueChange = { portfolioUrl = it },
                            placeholder = "https://yourportfolio.dev",
                            testTag = "input_portfolio_url"
                        )
                    }
                }
            }

            // 8. Creator / Seller Venture Details (If active seller or founder)
            item {
                ProfileSectionCard(title = "Venture Founder & Creator Details", icon = Icons.Default.Storefront) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "If you sell products or freelance on UniSpaceX, configure your venture profile:",
                            fontSize = 11.5.sp,
                            color = SoftLavender
                        )
                        HubFormField(
                            label = "Campus Venture / Studio Name",
                            value = bizName,
                            onValueChange = { bizName = it },
                            placeholder = "e.g. Apex Hardware Lab",
                            testTag = "input_venture_name"
                        )
                        HubFormField(
                            label = "Venture Tagline",
                            value = bizTagline,
                            onValueChange = { bizTagline = it },
                            placeholder = "e.g. Custom PCB Fabrication & Drone Hardware",
                            testTag = "input_venture_tagline"
                        )
                        HubFormField(
                            label = "About the Venture",
                            value = bizAbout,
                            onValueChange = { bizAbout = it },
                            placeholder = "Describe products, workshop gear and student warranty...",
                            testTag = "input_venture_about"
                        )
                    }
                }
            }

            // Bottom Save Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLavender),
                        border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(CosmicBorder, CosmicBorderSubtle))),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { saveAllDetails() },
                        modifier = Modifier.weight(1.5f).height(46.dp).testTag("bottom_save_profile_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF07090E), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Profile Changes", color = Color(0xFF07090E), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    val theme = LocalCosmicTheme.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = theme.surfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(theme.borderGlow, theme.borderSubtle))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun HubFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StarWhite)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 11.5.sp) },
            modifier = Modifier.fillMaxWidth().testTag(testTag),
            shape = RoundedCornerShape(10.dp),
            colors = defaultHubTextFieldColors(),
            singleLine = true
        )
    }
}

@Composable
private fun defaultHubTextFieldColors(): androidx.compose.material3.TextFieldColors {
    val theme = LocalCosmicTheme.current
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = theme.primary,
        unfocusedBorderColor = theme.borderSubtle,
        focusedTextColor = StarWhite,
        unfocusedTextColor = StarWhite,
        focusedContainerColor = theme.surfaceElevated,
        unfocusedContainerColor = theme.surfaceElevated
    )
}
