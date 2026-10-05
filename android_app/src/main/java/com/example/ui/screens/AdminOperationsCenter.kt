package com.example.ui.screens

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApplicationStatus
import com.example.model.Business
import com.example.model.Student
import com.example.model.VerificationType
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MintEmerald
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.UniSpaceUiState

@Composable
fun AdminOperationsCenter(
    uiState: UniSpaceUiState,
    isNarrow: Boolean,
    onAdminToggleVerifyBusiness: (String) -> Unit,
    onAdminToggleVerifyStudent: (String) -> Unit,
    onAdminApproveVerification: (String) -> Unit,
    onAdminRejectVerification: (String) -> Unit,
    onUpdateApplicationStatus: (String, ApplicationStatus) -> Unit,
    onEditBusiness: (Business) -> Unit,
    onDeleteBusiness: (Business) -> Unit,
    onEditStudent: (Student) -> Unit,
    onDeleteStudent: (Student) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF2C1908), Color(0xFF140D04))
                )
            )
            .border(1.2.dp, Color(0xFFFFD54F).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Chief Admin Operations Center",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD54F)
                    )
                    Text(
                        text = "Full authority to edit and verify any student or seller venture",
                        fontSize = 10.sp,
                        color = SoftLavender
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 1: SELLER VENTURES VERIFICATION & EDITING
            Text(
                text = "SELLER / VENTURE DIRECTORY (${uiState.businesses.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.businesses.isEmpty()) {
                Text(
                    text = "No seller ventures currently registered in campus registry.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                uiState.businesses.forEach { biz ->
                val isVerified = biz.badges.contains(VerificationType.BUSINESS_VERIFIED)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                if (isVerified) MintEmerald.copy(alpha = 0.5f) else CosmicBorderSubtle,
                                CosmicBorder
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (isNarrow) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = biz.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                    if (isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = CosmicCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Founder: ${biz.ownerName} • 🎓 ${biz.college}",
                                    fontSize = 10.5.sp,
                                    color = SoftLavender
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${biz.completedOrders} orders",
                                        fontSize = 10.sp,
                                        color = CosmicCyan
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditBusiness(biz) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Venture",
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteBusiness(biz) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Venture",
                                                tint = Color(0xFFF43F5E),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        Button(
                                            onClick = { onAdminToggleVerifyBusiness(biz.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isVerified) CosmicCyan else Color(0xFF3B2808)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isVerified) "✓ Verified" else "+ Verify",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isVerified) Color(0xFF070B1E) else Color(0xFFFFD54F)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = biz.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                        if (isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = CosmicCyan,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Founder: ${biz.ownerName} • 🎓 ${biz.college}",
                                        fontSize = 10.sp,
                                        color = SoftLavender
                                    )
                                    Text(
                                        text = "Category: ${biz.category} • Orders: ${biz.completedOrders}",
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onEditBusiness(biz) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Venture",
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteBusiness(biz) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Venture",
                                            tint = Color(0xFFF43F5E),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    Button(
                                        onClick = { onAdminToggleVerifyBusiness(biz.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isVerified) CosmicCyan else Color(0xFF3B2808)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isVerified) "✓ Verified" else "+ Verify",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isVerified) Color(0xFF070B1E) else Color(0xFFFFD54F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            // SECTION 2: STUDENT FREELANCERS DIRECTORY & VERIFICATION
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "2. STUDENT INNOVATOR ROSTER (${uiState.students.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.students.isEmpty()) {
                Text(
                    text = "No other student profiles registered in orbit.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                uiState.students.forEach { st ->
                val isVerified = st.badges.contains(VerificationType.STUDENT_VERIFIED)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(
                            listOf(
                                if (isVerified) ElectricViolet.copy(alpha = 0.5f) else CosmicBorderSubtle,
                                CosmicBorder
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        if (isNarrow) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = st.name,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StarWhite
                                    )
                                    if (isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified Student",
                                            tint = CosmicPurple,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${st.roleTitle} • 🎓 ${st.college}",
                                    fontSize = 10.5.sp,
                                    color = SoftLavender
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⭐ ${st.rating} • ${st.completedProjects} proj",
                                        fontSize = 10.sp,
                                        color = CosmicCyan
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditStudent(st) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Student",
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteStudent(st) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Student",
                                                tint = Color(0xFFF43F5E),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                        Button(
                                            onClick = { onAdminToggleVerifyStudent(st.id) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isVerified) CosmicPurple else Color(0xFF22173B)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isVerified) "✓ Verified" else "+ Verify",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isVerified) StarWhite else CosmicCyan
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = st.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                        if (isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Verified Student",
                                                tint = CosmicPurple,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${st.roleTitle} • 🎓 ${st.college}",
                                        fontSize = 10.sp,
                                        color = SoftLavender
                                    )
                                    Text(
                                        text = "Projects: ${st.completedProjects} • Rating: ⭐ ${st.rating}",
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Edit Button
                                    IconButton(
                                        onClick = { onEditStudent(st) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Student",
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Delete Button (Admin Only)
                                    IconButton(
                                        onClick = { onDeleteStudent(st) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Student",
                                            tint = Color(0xFFF43F5E),
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }

                                    // Verify Toggle Button
                                    Button(
                                        onClick = { onAdminToggleVerifyStudent(st.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isVerified) CosmicPurple else Color(0xFF22173B)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isVerified) "✓ Verified" else "+ Verify",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isVerified) StarWhite else CosmicCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            // SECTION 3: CAMPUS STUDENT & SELLER VERIFICATIONS QUEUE
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3. VERIFICATION QUEUE (${uiState.verificationRequests.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD54F),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "SheerID Students & Seller Govt IDs",
                    fontSize = 10.sp,
                    color = SoftLavender
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.verificationRequests.isEmpty()) {
                Text(
                    text = "No student or seller verification requests in queue.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            } else {
                uiState.verificationRequests.forEach { req ->
                    val isSellerReq = req.type == "SELLER_GOVT_ID"
                    val cardBorder = if (isSellerReq) Color(0xFFFFD54F).copy(alpha = 0.5f) else CosmicCyan.copy(alpha = 0.5f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Header badge & status
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSellerReq) Color(0xFFD97706).copy(alpha = 0.2f) else CosmicPurple.copy(alpha = 0.3f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isSellerReq) "🛍️ SELLER APPLICATION" else "🎓 SHEERID STUDENT ID",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSellerReq) Color(0xFFFFD54F) else CosmicCyan
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            when (req.status) {
                                                "APPROVED" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                "REJECTED" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                    Text(
                                        text = when (req.status) {
                                            "APPROVED" -> if (isSellerReq) "✓ Active Seller" else "✓ SheerID Active"
                                            "REJECTED" -> "Rejected"
                                            else -> "Awaiting Admin Review"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (req.status) {
                                            "APPROVED" -> Color(0xFF10B981)
                                            "REJECTED" -> Color(0xFFEF4444)
                                            else -> Color(0xFFF59E0B)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Applicant Name & College
                            Text(
                                text = req.studentName,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = StarWhite
                            )
                            Text(
                                text = "🎓 ${req.college} · ${req.departmentYear.ifBlank { "Campus Orbit" }}",
                                fontSize = 10.5.sp,
                                color = CosmicCyan
                            )

                            if (isSellerReq) {
                                // SELLER SPECIFIC DETAILS: Business Name, Govt ID, WhatsApp, Products
                                Spacer(modifier = Modifier.height(6.dp))
                                req.businessName?.let { bName ->
                                    if (bName.isNotBlank()) {
                                        Text(
                                            text = "🏪 Store / Brand: $bName",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFFD54F)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "🪪 ${req.governmentIdType ?: "Govt ID"}: ${req.governmentIdNumber ?: "Verified"}",
                                        fontSize = 10.5.sp,
                                        color = SoftLavender
                                    )
                                    req.whatsappNumber?.let { wa ->
                                        if (wa.isNotBlank()) {
                                            Text(
                                                text = "💬 WhatsApp: $wa",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF34D399)
                                            )
                                        }
                                    }
                                }

                                // Product Sample Images Preview
                                if (req.productImages.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Submitted Product Samples (${req.productImages.size}):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftLavender
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(req.productImages) { imgUrl ->
                                            Box(
                                                modifier = Modifier
                                                    .size(54.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            ) {
                                                AsyncImage(
                                                    model = imgUrl,
                                                    contentDescription = "Seller Product Sample",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // SHEERID STUDENT SPECIFIC DETAILS: Roll Number, Institutional Email, Validity Expiry
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Roll / Student ID: ${req.rollNumber}",
                                    fontSize = 11.sp,
                                    color = StarWhite
                                )
                                Text(
                                    text = "📧 Campus Institutional Email: ${req.collegeEmail}",
                                    fontSize = 10.5.sp,
                                    color = SoftLavender
                                )
                                req.sheerIdValidityExpiry?.let { expiry ->
                                    Text(
                                        text = "⏳ SheerID Term Validity: $expiry",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                Text(
                                    text = "🔗 Stored in Firestore · Linked to Auth Profile (${req.studentId})",
                                    fontSize = 9.5.sp,
                                    color = TextMuted
                                )
                            }

                            // Actions if PENDING
                            if (req.status == "PENDING") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onAdminApproveVerification(req.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isSellerReq) Color(0xFFD97706) else Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp).weight(1.3f),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isSellerReq) "Approve as Seller ✓" else "Approve SheerID ✓",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StarWhite
                                        )
                                    }

                                    Button(
                                        onClick = { onAdminRejectVerification(req.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp).weight(0.7f),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "Reject", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 4: CREW APPLICATIONS OVERVIEW
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "4. CREW RECRUITMENT DECISIONS (${uiState.crewApplications.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            uiState.crewApplications.forEach { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CosmicBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${app.applicantName} ➔ ${app.projectTitle}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                                Text(
                                    text = "Role: ${app.chosenSkill} · 🎓 ${app.applicantCollege}",
                                    fontSize = 11.sp,
                                    color = CosmicCyan
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(app.status.badgeColorHex).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = app.status.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(app.status.badgeColorHex)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"${app.pitchMessage}\"",
                            fontSize = 11.sp,
                            color = SoftLavender
                        )

                        if (app.status == ApplicationStatus.PENDING) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onUpdateApplicationStatus(app.id, ApplicationStatus.ACCEPTED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp).weight(1f),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Accept to Crew ✓", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                }

                                Button(
                                    onClick = { onUpdateApplicationStatus(app.id, ApplicationStatus.DECLINED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp).weight(1f),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Decline", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
