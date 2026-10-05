package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CampusMeetupLocation
import com.example.model.CampusPaymentMethod
import com.example.model.CartItem
import com.example.model.CollaborationRequest
import com.example.model.OrderProcess
import com.example.model.SkillNode
import com.example.ui.theme.CelestialGold
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderGlow
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SolarGold
import com.example.ui.theme.RoyalSapphire
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted

// ==========================================
// 1. CAMPUS CART & MEETUP CHECKOUT MODAL
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartModalSheet(
    cartItems: List<CartItem>,
    subtotal: Int,
    meetupLocations: List<CampusMeetupLocation>,
    selectedLocation: CampusMeetupLocation,
    selectedPaymentMethod: CampusPaymentMethod,
    orderNote: String,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onSelectLocation: (CampusMeetupLocation) -> Unit,
    onSelectPaymentMethod: (CampusPaymentMethod) -> Unit,
    onOrderNoteChange: (String) -> Unit,
    onConfirmOrder: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CosmicPurple.copy(alpha = 0.3f))
                            .border(1.dp, CosmicCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Cart",
                            tint = CosmicCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Campus Bag & Meetup Checkout",
                            color = StarWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${cartItems.sumOf { it.quantity }} item(s) ready for campus handshake",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SoftLavender)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your campus bag is empty",
                            color = StarWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Discover physical merch, 3D prints, textbooks, or hardware from peers",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Cart Items List
                Text(
                    text = "ORDER ITEMS",
                    color = SoftLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                cartItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!item.product.imageUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.product.imageUrl,
                                    contentDescription = item.product.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CosmicSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "📦", fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.title,
                                    color = StarWhite,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.product.businessName} · ₹${item.product.price}",
                                    color = CosmicCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Quantity Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmicSurfaceElevated)
                                    .border(1.dp, CosmicBorderSubtle, RoundedCornerShape(8.dp))
                            ) {
                                IconButton(
                                    onClick = { onUpdateQuantity(item.product.id, -1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = if (item.quantity == 1) NeonPink else SoftLavender,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "${item.quantity}",
                                    color = StarWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { onUpdateQuantity(item.product.id, 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = SoftLavender,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meetup Landmark Selector
                Text(
                    text = "CAMPUS HANDOVER MEETUP POINT",
                    color = SoftLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                meetupLocations.forEach { loc ->
                    val isSelected = loc.id == selectedLocation.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onSelectLocation(loc) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CosmicPurple.copy(alpha = 0.25f) else CosmicSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CosmicCyan else CosmicBorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = loc.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = loc.name,
                                    color = if (isSelected) StarWhite else SoftLavender,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = loc.landmarkDesc,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = CosmicCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method
                Text(
                    text = "PAYMENT METHOD",
                    color = SoftLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                CampusPaymentMethod.values().forEach { method ->
                    val isSelected = method == selectedPaymentMethod
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onSelectPaymentMethod(method) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CometBlue.copy(alpha = 0.25f) else CosmicSurfaceCard
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) RoyalSapphire else CosmicBorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = method.icon, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = method.title,
                                    color = StarWhite,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = method.subtitle,
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = RoyalSapphire,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meetup instructions note
                OutlinedTextField(
                    value = orderNote,
                    onValueChange = onOrderNoteChange,
                    label = { Text("Meetup Details / What you're wearing (Optional)", color = TextMuted) },
                    placeholder = { Text("e.g. Near 2nd pillar wearing black hoodie", color = TextMuted.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        focusedBorderColor = CosmicCyan,
                        unfocusedBorderColor = CosmicBorder
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Items Subtotal", color = TextMuted, fontSize = 13.sp)
                            Text(text = "₹$subtotal", color = StarWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Campus Handshake Handover", color = TextMuted, fontSize = 13.sp)
                            Text(text = "FREE", color = CosmicCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(CosmicBorderSubtle)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total Payable at Meetup", color = StarWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "₹$subtotal",
                                color = CelestialGold,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm Button
                Button(
                    onClick = onConfirmOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_campus_order_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(listOf(RoyalSapphire, CosmicPurple)),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Confirm Campus Meetup Order · ₹$subtotal",
                            color = StarWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. CREW APPLICATION & PITCH MODAL
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyToCrewSheet(
    collab: CollaborationRequest,
    onSubmitApplication: (chosenSkill: String, pitchMessage: String, portfolioLink: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSkill by remember { mutableStateOf(collab.skillsNeeded.firstOrNull() ?: "General") }
    var pitchMessage by remember { mutableStateOf("") }
    var portfolioLink by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Apply to Join Crew 🚀",
                        color = StarWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${collab.title} · ${collab.organizer}",
                        color = CosmicCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SoftLavender)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Project Brief Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = collab.projectType,
                        color = CelestialGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = collab.description,
                        color = StarWhite.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bounty: ₹${collab.budget} · Timeline: ${collab.deadlineDays} days",
                        color = CosmicCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Choose Skill
            Text(
                text = "SELECT YOUR TARGET ROLE / SKILL",
                color = SoftLavender,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(collab.skillsNeeded) { skill ->
                    val isSelected = skill == selectedSkill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) ElectricViolet else CosmicSurfaceElevated
                            )
                            .border(
                                1.dp,
                                if (isSelected) CosmicCyan else CosmicBorderSubtle,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedSkill = skill }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = skill,
                            color = if (isSelected) StarWhite else SoftLavender,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pitch input
            OutlinedTextField(
                value = pitchMessage,
                onValueChange = { pitchMessage = it },
                label = { Text("Your Pitch / Why are you right for this crew?", color = TextMuted) },
                placeholder = { Text("Describe previous hackathons, tech stack, or relevant design projects...", color = TextMuted.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite,
                    focusedBorderColor = CosmicCyan,
                    unfocusedBorderColor = CosmicBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Portfolio link
            OutlinedTextField(
                value = portfolioLink,
                onValueChange = { portfolioLink = it },
                label = { Text("Portfolio / GitHub / Figma Link", color = TextMuted) },
                placeholder = { Text("https://github.com/... or https://behance.net/...", color = TextMuted.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite,
                    focusedBorderColor = CosmicCyan,
                    unfocusedBorderColor = CosmicBorder
                )
            )

            if (errorText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = errorText ?: "", color = NeonPink, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (pitchMessage.isBlank()) {
                        errorText = "Please write a short pitch for the project lead"
                    } else {
                        onSubmitApplication(selectedSkill, pitchMessage, portfolioLink)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_crew_application_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(CosmicPurple, RoyalSapphire)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Submit Application to $selectedSkill",
                        color = StarWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. PEER REVIEW & STAR RATING MODAL
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitReviewSheet(
    order: OrderProcess,
    onSubmitReview: (rating: Double, comment: String, tags: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val availableTags = listOf(
        "⚡ Fast Handover",
        "💎 Pristine Quality",
        "🤝 Great Communication",
        "🎯 Exact Specs",
        "🌟 Highly Recommended"
    )
    val selectedTags = remember { mutableStateListOf<String>("💎 Pristine Quality") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Leave Campus Review ⭐",
                        color = StarWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Order #${order.orderId} · ${order.providerName}",
                        color = CosmicCyan,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SoftLavender)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Star Rating Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "HOW WAS YOUR EXPERIENCE?",
                        color = SoftLavender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { rating = star },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = "$star Stars",
                                    tint = if (star <= rating) CelestialGold else TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = when (rating) {
                            5 -> "Exceptional! 5.0 Stars"
                            4 -> "Great Experience! 4.0 Stars"
                            3 -> "Average Service (3.0)"
                            else -> "Needs Improvement"
                        },
                        color = CelestialGold,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compliment Chips
            Text(
                text = "HIGHLIGHTS (SELECT ALL THAT APPLY)",
                color = SoftLavender,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(availableTags) { tag ->
                    val isSelected = selectedTags.contains(tag)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) CosmicPurple.copy(alpha = 0.4f) else CosmicSurfaceElevated)
                            .border(1.dp, if (isSelected) CosmicCyan else CosmicBorderSubtle, RoundedCornerShape(16.dp))
                            .clickable {
                                if (isSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            color = if (isSelected) StarWhite else SoftLavender,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Review comment
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Write your testimonial (Optional)", color = TextMuted) },
                placeholder = { Text("The creator was super helpful and delivered excellent work...", color = TextMuted.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite,
                    focusedBorderColor = CosmicCyan,
                    unfocusedBorderColor = CosmicBorder
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val finalComment = comment.ifBlank { "Smooth campus handover and high quality output." }
                    onSubmitReview(rating.toDouble(), finalComment, selectedTags.toList())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_rating_review_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(CelestialGold, ElectricViolet)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Submit Verified Review",
                        color = StarWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. STUDENT & VENTURE VERIFICATION MODAL
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentVerificationModalSheet(
    collegeName: String,
    initialTab: Int = 0,
    onSheerIdSubmit: (rollNumber: String, collegeEmail: String, departmentYear: String, graduationYear: String, policyAccepted: Boolean) -> Unit = { _, _, _, _, _ -> },
    onSellerSubmit: (businessName: String, govtIdType: String, govtIdNumber: String, whatsappNumber: String, productImages: List<String>, idProofUrl: String?) -> Unit = { _, _, _, _, _, _ -> },
    onSubmitRequest: (rollNumber: String, collegeEmail: String, departmentYear: String) -> Unit = { r, e, d -> onSheerIdSubmit(r, e, d, "2027", true) },
    onDismiss: () -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) } // 0 = SheerID Student, 1 = Seller Verification

    // SheerID Student Verification States
    var rollNumber by remember { mutableStateOf("") }
    var collegeEmail by remember { mutableStateOf("") }
    var departmentYear by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("2027") }
    var sheerIdTermsAccepted by remember { mutableStateOf(false) }

    // Seller Verification States (Govt ID, WhatsApp, Products)
    var businessName by remember { mutableStateOf("") }
    var selectedGovtIdType by remember { mutableStateOf("Aadhaar Card") }
    var govtIdNumber by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var sellerTermsAccepted by remember { mutableStateOf(false) }
    val productImages = remember { mutableStateListOf<String>() }
    var customProductImageUrl by remember { mutableStateOf("") }

    // Multiple photo picker for real seller catalog images
    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val str = uri.toString()
                if (!productImages.contains(str)) {
                    productImages.add(str)
                }
            }
        }
    }

    val sampleCatalogPresets = remember {
        listOf(
            "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=400&q=80",
            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&q=80",
            "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400&q=80",
            "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?w=400&q=80"
        )
    }

    val govtIdTypes = remember {
        listOf("Aadhaar Card", "Passport", "Driver's License", "Voter ID", "National ID")
    }

    var errorText by remember { mutableStateOf<String?>(null) }

    val isEmailSheerIdCompliant = collegeEmail.contains("@") && (
        collegeEmail.endsWith(".edu", ignoreCase = true) ||
        collegeEmail.endsWith(".ac.in", ignoreCase = true) ||
        collegeEmail.endsWith(".edu.in", ignoreCase = true) ||
        collegeEmail.endsWith(".ac.uk", ignoreCase = true) ||
        collegeEmail.contains(".edu.") ||
        collegeEmail.contains(".ac.") ||
        collegeEmail.contains("college", ignoreCase = true) ||
        collegeEmail.contains("univ", ignoreCase = true) ||
        collegeEmail.contains("student", ignoreCase = true) ||
        collegeEmail.contains("campus", ignoreCase = true)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Verify",
                        tint = CosmicCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTab == 0) "SheerID Student Verification 🎓" else "Seller Verification Application 🛍️",
                        color = StarWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SoftLavender)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two-Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicSurfaceCard)
                    .padding(3.dp)
            ) {
                // Tab 0: Student SheerID
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) CosmicPurple else Color.Transparent)
                        .clickable {
                            selectedTab = 0
                            errorText = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎓 Student (SheerID)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }
                }

                // Tab 1: Seller Verification
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) CosmicPurple else Color.Transparent)
                        .clickable {
                            selectedTab = 1
                            errorText = null
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛍️ Become Seller", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // ==========================================
                // 1. SHEERID STUDENT VERIFICATION TAB
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicCyan.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CosmicCyan.copy(alpha = 0.4f), ElectricViolet.copy(alpha = 0.4f))))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛡️ SheerID Instant Verification Engine", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Institutional email verification confirms active college enrollment. Your verified student badge activates immediately and remains valid through your graduation year.",
                            color = SoftLavender,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = collegeName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("College / University", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        focusedBorderColor = CosmicBorderSubtle,
                        unfocusedBorderColor = CosmicBorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = collegeEmail,
                    onValueChange = { collegeEmail = it },
                    label = { Text("Campus Email (.edu / .ac.in / college domain)", color = TextMuted) },
                    placeholder = { Text("student@campus.edu or name@vit.ac.in", color = TextMuted.copy(alpha = 0.5f)) },
                    supportingText = {
                        if (collegeEmail.isNotBlank()) {
                            Text(
                                text = if (isEmailSheerIdCompliant) "✓ Eligible campus domain detected" else "⚠️ Must be an institutional college email address",
                                color = if (isEmailSheerIdCompliant) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontSize = 10.sp
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        focusedBorderColor = if (isEmailSheerIdCompliant) Color(0xFF10B981) else CosmicCyan,
                        unfocusedBorderColor = CosmicBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = { Text("Roll Number / Student ID", color = TextMuted) },
                        placeholder = { Text("e.g. 21CS042", color = TextMuted.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, focusedBorderColor = CosmicCyan, unfocusedBorderColor = CosmicBorder)
                    )

                    OutlinedTextField(
                        value = graduationYear,
                        onValueChange = { graduationYear = it },
                        label = { Text("Graduation Year", color = TextMuted) },
                        placeholder = { Text("2027", color = TextMuted.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, focusedBorderColor = CosmicCyan, unfocusedBorderColor = CosmicBorder)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = departmentYear,
                    onValueChange = { departmentYear = it },
                    label = { Text("Department & Program", color = TextMuted) },
                    placeholder = { Text("e.g. B.Tech Computer Science / 3rd Year", color = TextMuted.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, focusedBorderColor = CosmicCyan, unfocusedBorderColor = CosmicBorder)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Validity Expiry Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CosmicSurfaceCard)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏳ Validity Term: ", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = "Valid until June ${graduationYear.ifBlank { "2027" }}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SheerID Policy & Terms Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sheerIdTermsAccepted = !sheerIdTermsAccepted },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sheerIdTermsAccepted,
                        onCheckedChange = { sheerIdTermsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = CosmicCyan, checkmarkColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I agree to the SheerID Student Verification Policy & Institutional Terms, confirming my enrolled student identity.",
                        fontSize = 10.5.sp,
                        color = SoftLavender,
                        lineHeight = 14.sp
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText ?: "", color = NeonPink, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (collegeEmail.isBlank() || rollNumber.isBlank()) {
                            errorText = "Please fill in your campus email and student roll number"
                        } else if (!isEmailSheerIdCompliant) {
                            errorText = "Campus email must be an institutional address (.edu / .ac.in / college domain)"
                        } else if (!sheerIdTermsAccepted) {
                            errorText = "You must accept the SheerID student terms & conditions"
                        } else {
                            onSheerIdSubmit(rollNumber, collegeEmail, departmentYear, graduationYear, sheerIdTermsAccepted)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_sheerid_verification_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(listOf(RoyalSapphire, ElectricViolet)),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Verify Student Status via SheerID 🛡️",
                            color = StarWhite,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            } else {
                // ==========================================
                // 2. SELLER VERIFICATION APPLICATION TAB
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1430)),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ElectricViolet.copy(alpha = 0.5f), SolarGold.copy(alpha = 0.5f))))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛍️ Campus Merchant & Seller Onboarding", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SolarGold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "To become an approved seller, submit any Government ID, your WhatsApp contact number, and sample photos of your products. Your account will become an active seller once our Admin approves.",
                            color = SoftLavender,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    label = { Text("Store / Brand / Venture Name", color = TextMuted) },
                    placeholder = { Text("e.g. Afraar Tech Lab, Campus Merch, Cookie Hub", color = TextMuted.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, focusedBorderColor = SolarGold, unfocusedBorderColor = CosmicBorder)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Government ID Document", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                Spacer(modifier = Modifier.height(4.dp))

                // Government ID Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(govtIdTypes) { idType ->
                        val isSelected = selectedGovtIdType == idType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SolarGold else CosmicSurfaceCard)
                                .clickable { selectedGovtIdType = idType }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = idType,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF1B1100) else SoftLavender
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = govtIdNumber,
                    onValueChange = { govtIdNumber = it },
                    label = { Text("$selectedGovtIdType Number", color = TextMuted) },
                    placeholder = { Text("e.g. 1234-5678-9012 or Passport / License ID", color = TextMuted.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, focusedBorderColor = SolarGold, unfocusedBorderColor = CosmicBorder)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text("Contact WhatsApp Number (with Country Code)", color = TextMuted) },
                    placeholder = { Text("e.g. +91 98765 43210", color = TextMuted.copy(alpha = 0.5f)) },
                    supportingText = {
                        Text("Used by campus customers & admin to coordinate order handovers and verify inventory.", fontSize = 10.sp, color = TextMuted)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, focusedBorderColor = SolarGold, unfocusedBorderColor = CosmicBorder)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Product Sample Images
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Product Images / Catalog Samples", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                        Text("Select sample product photos or upload from device:", fontSize = 10.sp, color = TextMuted)
                    }
                    Button(
                        onClick = {
                            multiplePhotoPicker.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("📸 Upload", color = SolarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Preset Images selector
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sampleCatalogPresets) { imgUrl ->
                        val isChosen = productImages.contains(imgUrl)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, if (isChosen) SolarGold else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChosen) productImages.remove(imgUrl) else productImages.add(imgUrl)
                                }
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Product Sample",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isChosen) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = SolarGold, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customProductImageUrl,
                        onValueChange = { customProductImageUrl = it },
                        placeholder = { Text("https://... product photo URL", fontSize = 11.sp, color = TextMuted.copy(alpha = 0.5f)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = StarWhite, unfocusedTextColor = StarWhite, focusedBorderColor = SolarGold, unfocusedBorderColor = CosmicBorder)
                    )
                    Button(
                        onClick = {
                            if (customProductImageUrl.isNotBlank()) {
                                productImages.add(customProductImageUrl.trim())
                                customProductImageUrl = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Add", color = SolarGold, fontSize = 11.sp)
                    }
                }

                if (productImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${productImages.size} product photos attached for admin review", fontSize = 10.sp, color = SolarGold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seller Agreement Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sellerTermsAccepted = !sellerTermsAccepted },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sellerTermsAccepted,
                        onCheckedChange = { sellerTermsAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = SolarGold, checkmarkColor = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I agree to the Campus Marketplace Seller Code of Conduct, WhatsApp order responsiveness, and fair student pricing.",
                        fontSize = 10.5.sp,
                        color = SoftLavender,
                        lineHeight = 14.sp
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = errorText ?: "", color = NeonPink, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (businessName.isBlank()) {
                            errorText = "Please enter your store or brand name"
                        } else if (govtIdNumber.isBlank()) {
                            errorText = "Please enter your $selectedGovtIdType number"
                        } else if (whatsappNumber.isBlank() || whatsappNumber.length < 8) {
                            errorText = "Please enter a valid WhatsApp contact number"
                        } else if (productImages.isEmpty()) {
                            errorText = "Please attach at least 1 sample photo of your products"
                        } else if (!sellerTermsAccepted) {
                            errorText = "You must agree to the seller code of conduct"
                        } else {
                            onSellerSubmit(
                                businessName.trim(),
                                selectedGovtIdType,
                                govtIdNumber.trim(),
                                whatsappNumber.trim(),
                                productImages.toList(),
                                null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit_seller_verification_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(listOf(SolarGold, Color(0xFFD97706))),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Submit Application for Admin Approval 🚀",
                            color = Color(0xFF1B1100),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. INTERACTIVE SKILL CONSTELLATION BUILDER
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSkillsSheet(
    skills: List<SkillNode>,
    onAddSkill: (name: String, category: String, level: String) -> Unit,
    onRemoveSkill: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var newSkillName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Hardware") }
    var selectedLevel by remember { mutableStateOf("Expert") }
    val categories = listOf("Hardware", "VLSI", "Software", "Embedded", "Design", "Creative", "Academic")
    val levels = listOf("Beginner", "Advanced", "Expert")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Skill Constellation Builder 🌌",
                        color = StarWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${skills.size} interconnected nodes in your galaxy",
                        color = CosmicCyan,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SoftLavender)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Current Nodes List
            Text(
                text = "CURRENT NODES",
                color = SoftLavender,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            skills.forEach { skill ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CosmicCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = skill.name, color = StarWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(
                                    text = "${skill.category} · ${skill.level} · ${skill.endorsements} endorsements",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { onRemoveSkill(skill.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = NeonPink, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Node Section
            Text(
                text = "ADD SKILL NODE",
                color = SoftLavender,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = newSkillName,
                onValueChange = { newSkillName = it },
                label = { Text("Skill / Technology Name", color = TextMuted) },
                placeholder = { Text("e.g. KiCad, Next.js, Blender, SolidWorks", color = TextMuted.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite,
                    focusedBorderColor = CosmicCyan,
                    unfocusedBorderColor = CosmicBorder
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Chips
            Text(text = "Category", color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) ElectricViolet else CosmicSurfaceElevated)
                            .border(1.dp, if (isSelected) CosmicCyan else CosmicBorderSubtle, RoundedCornerShape(14.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = cat, color = if (isSelected) StarWhite else SoftLavender, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level Chips
            Text(text = "Proficiency", color = TextMuted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                levels.forEach { lvl ->
                    val isSelected = lvl == selectedLevel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) RoyalSapphire else CosmicSurfaceElevated)
                            .border(1.dp, if (isSelected) CosmicCyan else CosmicBorderSubtle, RoundedCornerShape(14.dp))
                            .clickable { selectedLevel = lvl }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = lvl, color = if (isSelected) StarWhite else SoftLavender, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (newSkillName.isNotBlank()) {
                        onAddSkill(newSkillName.trim(), selectedCategory, selectedLevel)
                        newSkillName = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_skill_node_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(listOf(CosmicCyan, ElectricViolet)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Connect to Constellation",
                        color = StarWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
