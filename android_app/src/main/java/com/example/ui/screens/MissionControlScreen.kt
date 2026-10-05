package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Business
import com.example.model.OrderProcess
import com.example.model.OrderProgressStep
import com.example.model.Student
import com.example.model.UserRole
import com.example.model.VerificationType
import com.example.ui.components.CosmicBadgeRow
import com.example.ui.components.CosmicOrderProgress
import com.example.ui.components.SkillConstellation
import com.example.ui.theme.*
import com.example.ui.theme.CelestialGold
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.viewmodel.CreateType
import com.example.viewmodel.UniSpaceUiState

@Composable
fun MissionControlScreen(
    uiState: UniSpaceUiState,
    onAdvanceOrderStep: (String) -> Unit,
    onOpenCreate: (CreateType) -> Unit,
    onSignOut: () -> Unit = {},
    onSyncClick: () -> Unit = {},
    onOpenDatabaseInspector: () -> Unit = {},
    onAdminToggleVerifyBusiness: (String) -> Unit = {},
    onAdminToggleVerifyStudent: (String) -> Unit = {},
    onAdminEditBusiness: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onAdminEditStudent: (String, String, String, String, String) -> Unit = { _, _, _, _, _ -> },
    onSetProfilePicture: (String) -> Unit = {},
    onDeleteCurrentUserProfile: () -> Unit = {},
    onAdminDeleteStudent: (String) -> Unit = {},
    onAdminDeleteBusiness: (String) -> Unit = {},
    onOpenVerificationSheet: () -> Unit = {},
    onOpenSellerVerification: () -> Unit = {},
    onOpenSkillEditorSheet: () -> Unit = {},
    onOpenReviewSheet: (OrderProcess) -> Unit = {},
    onAdminApproveVerification: (String) -> Unit = {},
    onAdminRejectVerification: (String) -> Unit = {},
    onUpdateApplicationStatus: (String, com.example.model.ApplicationStatus) -> Unit = { _, _ -> },
    onNavigateToProfileHub: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSelectCandidateRole: (UserRole, String, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val userStudent = uiState.userProfile
    var editingBusiness by remember { mutableStateOf<Business?>(null) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showDeleteSelfDialog by remember { mutableStateOf(false) }
    var deleteBusinessCandidate by remember { mutableStateOf<Business?>(null) }
    var deleteStudentCandidate by remember { mutableStateOf<Student?>(null) }
    var showAvatarPickerOptions by remember { mutableStateOf(false) }
    var customAvatarUrlInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onSetProfilePicture(uri.toString())
        }
    }

    // Dialog for Admin editing a Seller / Business
    if (editingBusiness != null) {
        val biz = editingBusiness!!
        var editName by remember(biz.id) { mutableStateOf(biz.name) }
        var editCollege by remember(biz.id) { mutableStateOf(biz.college) }
        var editTagline by remember(biz.id) { mutableStateOf(biz.tagline) }
        var editAbout by remember(biz.id) { mutableStateOf(biz.about) }

        AlertDialog(
            onDismissRequest = { editingBusiness = null },
            containerColor = CosmicSurfaceCard,
            title = {
                Text(
                    text = "🛡️ Admin: Edit Seller Venture",
                    color = StarWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Business / Venture Name", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCollege,
                        onValueChange = { editCollege = it },
                        label = { Text("College Planet", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTagline,
                        onValueChange = { editTagline = it },
                        label = { Text("Tagline / Headline", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAbout,
                        onValueChange = { editAbout = it },
                        label = { Text("About Bio", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdminEditBusiness(biz.id, editName, editCollege, editTagline, editAbout)
                        editingBusiness = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                ) {
                    Text("Save Changes", color = Color(0xFF1E1400), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingBusiness = null }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    // Dialog for Admin editing a Student / User
    if (editingStudent != null) {
        val st = editingStudent!!
        var editName by remember(st.id) { mutableStateOf(st.name) }
        var editRoleTitle by remember(st.id) { mutableStateOf(st.roleTitle) }
        var editCollege by remember(st.id) { mutableStateOf(st.college) }
        var editBio by remember(st.id) { mutableStateOf(st.bio) }

        AlertDialog(
            onDismissRequest = { editingStudent = null },
            containerColor = CosmicSurfaceCard,
            title = {
                Text(
                    text = "🛡️ Admin: Edit Student Profile",
                    color = StarWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Student Full Name", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editRoleTitle,
                        onValueChange = { editRoleTitle = it },
                        label = { Text("Specialty / Major", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCollege,
                        onValueChange = { editCollege = it },
                        label = { Text("College Planet", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio", color = SoftLavender, fontSize = 12.sp) },
                        colors = adminTextFieldColors(),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdminEditStudent(st.id, editName, editRoleTitle, editCollege, editBio)
                        editingStudent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                ) {
                    Text("Save Changes", color = Color(0xFF1E1400), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingStudent = null }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    // Dialog for Self-Profile Deletion (Only Profile Owner)
    if (showDeleteSelfDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelfDialog = false },
            containerColor = Color(0xFF1F0D15),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Your Cosmic Profile?",
                        color = StarWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently delete your profile for '${userStudent.name}'? This action is permanent and only accessible by you and mission control admins. Your products, listings, and credentials will be removed.",
                    color = SoftLavender,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteSelfDialog = false
                        onDeleteCurrentUserProfile()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Permanently Delete Profile", color = StarWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelfDialog = false }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    // Dialog for Admin Deleting a Business
    if (deleteBusinessCandidate != null) {
        val biz = deleteBusinessCandidate!!
        AlertDialog(
            onDismissRequest = { deleteBusinessCandidate = null },
            containerColor = Color(0xFF1F0D15),
            title = {
                Text(
                    text = "🛡️ Admin: Delete Seller Venture",
                    color = Color(0xFFFDA4AF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Permanently delete business listing '${biz.name}' and all associated product drops from UniSpaceX? (Authorized for Admins & Owner only).",
                    color = SoftLavender,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdminDeleteBusiness(biz.id)
                        deleteBusinessCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Delete Venture", color = StarWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteBusinessCandidate = null }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    // Dialog for Admin Deleting a Student Profile
    if (deleteStudentCandidate != null) {
        val st = deleteStudentCandidate!!
        AlertDialog(
            onDismissRequest = { deleteStudentCandidate = null },
            containerColor = Color(0xFF1F0D15),
            title = {
                Text(
                    text = "🛡️ Admin: Delete Student Profile",
                    color = Color(0xFFFDA4AF),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Permanently remove student '${st.name}' (${st.college}) from the campus galaxy? (Authorized for Admins & User only).",
                    color = SoftLavender,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdminDeleteStudent(st.id)
                        deleteStudentCandidate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Delete Student", color = StarWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStudentCandidate = null }) {
                    Text("Cancel", color = SoftLavender)
                }
            }
        )
    }

    // Dialog for Changing Profile Picture
    if (showAvatarPickerOptions) {
        AlertDialog(
            onDismissRequest = { showAvatarPickerOptions = false },
            containerColor = CosmicSurfaceCard,
            title = {
                Text(
                    text = "Set Profile Picture",
                    color = StarWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Choose an image from your device gallery, select a cosmic avatar style, or provide an image link.",
                        color = SoftLavender,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Gallery Picker Button
                    Button(
                        onClick = {
                            showAvatarPickerOptions = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("📷 Upload from Gallery", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Cosmic Presets:", color = CosmicCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val presets = listOf(
                        "🚀 Cyber Astronaut" to "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=300&auto=format&fit=crop&q=80",
                        "🎨 Nebula Artisan" to "https://images.unsplash.com/photo-1544717305-2782549b5136?w=300&auto=format&fit=crop&q=80",
                        "⚡ Silicon Hacker" to "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300&auto=format&fit=crop&q=80",
                        "🛍️ Campus Founder" to "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop&q=80"
                    )

                    presets.forEach { (label, url) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicSurfaceElevated)
                                .border(0.6.dp, CosmicBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onSetProfilePicture(url)
                                    showAvatarPickerOptions = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, fontSize = 11.sp, color = StarWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = customAvatarUrlInput,
                        onValueChange = { customAvatarUrlInput = it },
                        label = { Text("Or paste image URL", color = SoftLavender, fontSize = 11.sp) },
                        colors = adminTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                if (customAvatarUrlInput.isNotBlank()) {
                    Button(
                        onClick = {
                            onSetProfilePicture(customAvatarUrlInput.trim())
                            showAvatarPickerOptions = false
                            customAvatarUrlInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CometBlue)
                    ) {
                        Text("Apply URL", color = StarWhite, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAvatarPickerOptions = false }) {
                    Text("Close", color = SoftLavender)
                }
            }
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isNarrow = maxWidth < 440.dp
        val isVeryNarrow = maxWidth < 350.dp
        val isWide = maxWidth >= 700.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 840.dp)
                .align(Alignment.TopCenter)
                .testTag("mission_control_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 100.dp)
        ) {
        // Mission Control Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛰️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mission Control",
                            fontSize = if (isVeryNarrow) 18.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = StarWhite
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Venture telemetry & student commerce hub",
                        fontSize = 11.sp,
                        color = SoftLavender,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (uiState.currentUserRole) {
                                    UserRole.ADMIN -> Color(0xFF5D2E0B)
                                    UserRole.SELLER -> Color(0xFF0F3156)
                                    UserRole.STUDENT -> ElectricViolet.copy(alpha = 0.25f)
                                }
                            )
                            .border(
                                0.8.dp,
                                when (uiState.currentUserRole) {
                                    UserRole.ADMIN -> Color(0xFFFFD54F)
                                    UserRole.SELLER -> CometBlue
                                    UserRole.STUDENT -> CosmicCyan
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${uiState.currentUserRole.badge} ${uiState.currentUserRole.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (uiState.currentUserRole) {
                                UserRole.ADMIN -> Color(0xFFFFD54F)
                                UserRole.SELLER -> CometBlue
                                UserRole.STUDENT -> CosmicCyan
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CosmicSurfaceCard)
                            .border(1.dp, CosmicBorderSubtle, CircleShape)
                            .testTag("mission_control_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = StarWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Student Profile Header & Badges
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF151D32), Color(0xFF0F1626))
                        )
                    )
                    .border(1.dp, CosmicBorderGlow.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Column {
                    if (isNarrow) {
                        // Narrow screen (mobile): Avatar + Info on top
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(listOf(CosmicPurple, CometBlue)),
                                        shape = CircleShape
                                    )
                                    .border(1.8.dp, CosmicCyan, CircleShape)
                                    .clickable { showAvatarPickerOptions = true }
                                    .testTag("profile_avatar_box")
                            ) {
                                if (!userStudent.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = userStudent.avatarUrl,
                                        contentDescription = "Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = if (userStudent.name.isNotBlank()) userStudent.name.take(2).uppercase() else "ME",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userStudent.name.ifBlank { "Campus Explorer" },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = userStudent.roleTitle.ifBlank { "Student Innovator" },
                                    fontSize = 11.sp,
                                    color = CosmicCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "🎓 ${userStudent.college.ifBlank { "Campus Universe" }}",
                                    fontSize = 10.sp,
                                    color = SoftLavender,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compact Actions Row: 3 balanced buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showAvatarPickerOptions = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet.copy(alpha = 0.25f)),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(CosmicBorder, CosmicCyan.copy(alpha = 0.4f)))
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).testTag("set_profile_picture_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Photo", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = StarWhite, maxLines = 1)
                            }

                            Button(
                                onClick = onSignOut,
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(CosmicBorder, ElectricViolet))
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Exit Orbit", fontSize = 10.5.sp, color = SoftLavender, maxLines = 1)
                            }

                            Button(
                                onClick = { showDeleteSelfDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38121C)),
                                border = ButtonDefaults.outlinedButtonBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C)))
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).testTag("delete_profile_button")
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFFDA4AF), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(text = "Delete", fontSize = 10.5.sp, color = Color(0xFFFDA4AF), fontWeight = FontWeight.SemiBold, maxLines = 1)
                            }
                        }
                    } else {
                        // Standard / Wide layout
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(listOf(CosmicPurple, CometBlue)),
                                        shape = CircleShape
                                    )
                                    .border(1.8.dp, CosmicCyan, CircleShape)
                                    .clickable { showAvatarPickerOptions = true }
                                    .testTag("profile_avatar_box")
                            ) {
                                if (!userStudent.avatarUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = userStudent.avatarUrl,
                                        contentDescription = "Profile Picture",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = if (userStudent.name.isNotBlank()) userStudent.name.take(2).uppercase() else "ME",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = userStudent.name.ifBlank { "Campus Explorer" },
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = userStudent.roleTitle.ifBlank { "Student Innovator" },
                                    fontSize = 11.sp,
                                    color = CosmicCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "🎓 ${userStudent.college.ifBlank { "Campus Universe" }}",
                                    fontSize = 10.5.sp,
                                    color = SoftLavender,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = { showAvatarPickerOptions = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                        modifier = Modifier.testTag("set_profile_picture_button")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "Photo", fontSize = 10.sp, color = StarWhite)
                                    }

                                    Button(
                                        onClick = onSignOut,
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(text = "Exit Orbit", fontSize = 10.sp, color = SoftLavender)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = { showDeleteSelfDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38121C)),
                                    border = ButtonDefaults.outlinedButtonBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C)))
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 3.dp),
                                    modifier = Modifier.testTag("delete_profile_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFFDA4AF), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "Delete Profile", fontSize = 9.5.sp, color = Color(0xFFFDA4AF), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onNavigateToProfileHub,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("hub_open_profile_hub_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = StarWhite, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Details in Profile Hub ➔", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    CosmicBadgeRow(badges = userStudent.badges)

                    Spacer(modifier = Modifier.height(12.dp))
                    val isSheerIdVerified = userStudent.isSheerIdVerified || userStudent.badges.contains(VerificationType.STUDENT_VERIFIED)
                    val hasPendingSheerIdReq = uiState.verificationRequests.any { it.studentId == userStudent.id && it.type == "STUDENT_SHEERID" && it.status == "PENDING" }
                    val isSellerVerified = userStudent.isSellerVerified
                    val pendingSellerReq = uiState.verificationRequests.firstOrNull { it.studentId == userStudent.id && it.type == "SELLER_GOVT_ID" && it.status == "PENDING" }
                    val userBusinessName = uiState.businesses.firstOrNull { it.ownerName == userStudent.name || it.name in userStudent.businesses }?.name ?: userStudent.businesses.firstOrNull() ?: ""

                    // Card 1: SheerID Student Verification
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSheerIdVerified) CosmicPurple.copy(alpha = 0.25f) else CosmicSurfaceCard)
                            .border(1.dp, if (isSheerIdVerified) CosmicCyan else CosmicBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { if (!isSheerIdVerified && !hasPendingSheerIdReq) onOpenVerificationSheet() }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (isSheerIdVerified) "🎓" else "🛡️", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when {
                                            isSheerIdVerified -> "SheerID Student Identity Verified ✓"
                                            hasPendingSheerIdReq -> "SheerID Verification Processing ⏳"
                                            else -> "Verify Student Status via SheerID"
                                        },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSheerIdVerified) CosmicCyan else StarWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = when {
                                            isSheerIdVerified -> "${userStudent.universityEmailStatus.label} · ${userStudent.sheerIdValidityExpiry ?: "Valid through academic cycle"}"
                                            hasPendingSheerIdReq -> "Validating institutional domain against SheerID policy"
                                            else -> "Verify campus institutional email & accept SheerID terms"
                                        },
                                        fontSize = 10.5.sp,
                                        color = if (isSheerIdVerified) Color(userStudent.universityEmailStatus.badgeColorHex) else TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (!isSheerIdVerified && !hasPendingSheerIdReq) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CosmicPurple)
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(text = "Verify SheerID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Card 2: Campus Seller Verification & Store Status
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSellerVerified) Color(0xFF1E1430) else CosmicSurfaceCard)
                            .border(1.dp, if (isSellerVerified) Color(0xFFFFD54F) else CosmicBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable {
                                if (!isSellerVerified && pendingSellerReq == null) {
                                    if (isSheerIdVerified) {
                                        onSelectCandidateRole(
                                            UserRole.SELLER,
                                            userBusinessName,
                                            userStudent.sellerWhatsappNumber ?: ""
                                        )
                                    } else {
                                        onOpenSellerVerification()
                                    }
                                }
                            }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = if (isSellerVerified) "🛍️" else "🏪", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = when {
                                            isSellerVerified -> "Verified Campus Seller & Creator ✓"
                                            isSheerIdVerified -> "Seller Privileges Available (Student Verified)"
                                            pendingSellerReq != null -> "Seller Verification Under Review ⏳"
                                            else -> "Become a Verified Campus Seller"
                                        },
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSellerVerified) Color(0xFFFFD54F) else StarWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = when {
                                            isSellerVerified -> "Store active · WhatsApp: ${userStudent.sellerWhatsappNumber ?: "Connected"} · ID: ${userStudent.sellerGovtIdType ?: "Govt ID"}"
                                            isSheerIdVerified -> "Collegiate identity verified! Click to activate Seller Mode and launch your campus storefront."
                                            pendingSellerReq != null -> "Government ID (${pendingSellerReq.governmentIdType ?: "Govt ID"}) & sample products under review by Admin"
                                            else -> "Submit Govt ID, WhatsApp number & product images for Admin approval"
                                        },
                                        fontSize = 10.5.sp,
                                        color = if (isSellerVerified) Color(0xFFFFD54F) else TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (!isSellerVerified && pendingSellerReq == null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFD97706))
                                        .clickable {
                                            if (isSheerIdVerified) {
                                                onSelectCandidateRole(
                                                    UserRole.SELLER,
                                                    userBusinessName,
                                                    userStudent.sellerWhatsappNumber ?: ""
                                                )
                                            } else {
                                                onOpenSellerVerification()
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (isSheerIdVerified) "Activate Seller 🛍️" else "Apply to Sell",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                }
                            }
                        }
                    }

                    // Card 3: Post-Approval Candidate Role Selection (Student vs Seller)
                    if (isSheerIdVerified) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131B2E))
                                .border(1.dp, CosmicCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "✨", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Verified Member Orbit: Choose Your Role",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(CosmicCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Live Admin Sync ⚡",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CosmicCyan
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "As an approved student, choose to participate as a Student (innovator/buyer) or Seller (creator/store owner). Changes update lively to Admin.",
                                    fontSize = 10.sp,
                                    color = SoftLavender
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isCurrentStudent = uiState.currentUserRole == UserRole.STUDENT
                                    val isCurrentSeller = uiState.currentUserRole == UserRole.SELLER

                                    // Option 1: Student Mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrentStudent) CosmicPurple.copy(alpha = 0.35f) else CosmicSurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (isCurrentStudent) CosmicCyan else CosmicBorderSubtle,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                onSelectCandidateRole(UserRole.STUDENT, "", "")
                                            }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "🎓", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Student Mode",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentStudent) CosmicCyan else StarWhite
                                            )
                                            Text(
                                                text = if (isCurrentStudent) "Active ✓" else "Buyer / Peer",
                                                fontSize = 9.sp,
                                                fontWeight = if (isCurrentStudent) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrentStudent) Color(0xFF10B981) else TextMuted
                                            )
                                        }
                                    }

                                    // Option 2: Seller Mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrentSeller) Color(0xFF2C1E3D) else CosmicSurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (isCurrentSeller) Color(0xFFFFD54F) else CosmicBorderSubtle,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                onSelectCandidateRole(
                                                    UserRole.SELLER,
                                                    userBusinessName,
                                                    userStudent.sellerWhatsappNumber ?: ""
                                                )
                                            }
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = "🛍️", fontSize = 18.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Seller Mode",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrentSeller) Color(0xFFFFD54F) else StarWhite
                                            )
                                            Text(
                                                text = if (isCurrentSeller) "Active ✓" else "Store Owner",
                                                fontSize = 9.sp,
                                                fontWeight = if (isCurrentSeller) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isCurrentSeller) Color(0xFFFFD54F) else TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4 KPI Orbit Cards
        item {
            if (isWide) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Cosmic Revenue",
                        value = "₹0",
                        subtitle = "Tracked in real-time",
                        accentColor = CosmicCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Orders Done",
                        value = "${uiState.businesses.firstOrNull()?.completedOrders ?: 0}",
                        subtitle = "100% response rate",
                        accentColor = ElectricViolet,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Active Requests",
                        value = "${uiState.orders.size}",
                        subtitle = "In orbit pipelines",
                        accentColor = CometBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Portfolio Views",
                        value = "0",
                        subtitle = "Campus visits",
                        accentColor = CosmicSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Cosmic Revenue",
                        value = "₹0",
                        subtitle = "Tracked in real-time",
                        accentColor = CosmicCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Orders Done",
                        value = "${uiState.businesses.firstOrNull()?.completedOrders ?: 0}",
                        subtitle = "100% response rate",
                        accentColor = ElectricViolet,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Active Requests",
                        value = "${uiState.orders.size}",
                        subtitle = "In orbit pipelines",
                        accentColor = CometBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Portfolio Views",
                        value = "0",
                        subtitle = "Campus visits",
                        accentColor = CosmicSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Quick Launch Actions
        item {
            Text(
                text = "Quick Launchpad",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StarWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onOpenCreate(CreateType.PRODUCT) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "+ Product", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onOpenCreate(CreateType.SERVICE) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "+ Service", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onOpenCreate(CreateType.COLLAB) },
                    colors = ButtonDefaults.buttonColors(containerColor = CometBlue),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "+ Crew", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToProfileHub,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicCyan),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = Brush.horizontalGradient(listOf(CosmicBorder, CosmicCyan.copy(alpha = 0.5f)))
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("quick_launch_profile_hub")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Profile Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToSettings,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftLavender),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = Brush.horizontalGradient(listOf(CosmicBorder, CosmicBorderSubtle))
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("quick_launch_settings")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(22.dp))
        }

        // Active Orders & Order Progress Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Orbit Pipelines",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "Tap to advance ➔",
                    fontSize = 11.sp,
                    color = CosmicCyan
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(uiState.orders) { order ->
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                CosmicOrderProgress(
                    order = order,
                    onAdvanceStep = { onAdvanceOrderStep(order.orderId) }
                )
                if (order.currentStep == OrderProgressStep.COMPLETED) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onOpenReviewSheet(order) },
                        colors = ButtonDefaults.buttonColors(containerColor = CelestialGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("leave_order_review_${order.orderId}")
                    ) {
                        Text(
                            text = "⭐ Rate Experience & Submit Review for ${order.providerName}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F1400)
                        )
                    }
                }
            }
        }

        // Student Skill Constellation
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SkillConstellation(
                skills = userStudent.skills,
                title = "My Skill Constellation",
                subtitle = "${userStudent.name} · ${userStudent.roleTitle}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onOpenSkillEditorSheet,
                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
                border = BorderStroke(1.dp, CosmicCyan.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("open_skill_editor_button")
            ) {
                Text(text = "+ Edit My Skill Constellation 🌌", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
            }
            Spacer(modifier = Modifier.height(18.dp))
        }



        // Admin Operations Center (Visible when logged in as ADMIN)
        if (uiState.currentUserRole == UserRole.ADMIN) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                AdminOperationsCenter(
                    uiState = uiState,
                    isNarrow = isNarrow,
                    onAdminToggleVerifyBusiness = onAdminToggleVerifyBusiness,
                    onAdminToggleVerifyStudent = onAdminToggleVerifyStudent,
                    onAdminApproveVerification = onAdminApproveVerification,
                    onAdminRejectVerification = onAdminRejectVerification,
                    onUpdateApplicationStatus = onUpdateApplicationStatus,
                    onEditBusiness = { editingBusiness = it },
                    onDeleteBusiness = { deleteBusinessCandidate = it },
                    onEditStudent = { editingStudent = it },
                    onDeleteStudent = { deleteStudentCandidate = it }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(CosmicBorder, accentColor.copy(alpha = 0.4f)))
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = SoftLavender,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CosmicSurface,
    unfocusedContainerColor = CosmicSurface,
    focusedBorderColor = Color(0xFFFFD54F),
    unfocusedBorderColor = CosmicBorder,
    focusedTextColor = StarWhite,
    unfocusedTextColor = StarWhite
)
