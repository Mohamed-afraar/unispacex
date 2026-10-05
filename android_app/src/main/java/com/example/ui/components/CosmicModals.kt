package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Business
import com.example.model.CampusPlanet
import com.example.model.CollaborationRequest
import com.example.model.FeedCategory
import com.example.model.Product
import com.example.model.Service
import com.example.model.Student
import com.example.ui.theme.CelestialGold
import com.example.ui.theme.CometBlue
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicBorderSubtle
import com.example.ui.theme.CosmicCyan
import com.example.ui.theme.CosmicPurple
import com.example.ui.theme.CosmicSecondary
import com.example.ui.theme.CosmicSurface
import com.example.ui.theme.CosmicSurfaceCard
import com.example.ui.theme.CosmicSurfaceElevated
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarWhite
import com.example.ui.theme.TextMuted
import com.example.viewmodel.CreateType
import com.example.viewmodel.UiNotification

/**
 * Centered responsive container frame for all Cosmic modal sheets.
 * Guarantees optimal ergonomic reading width and spacing on foldables and tablets.
 */
@Composable
fun CosmicModalContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 620.dp)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDetailSheet(
    business: Business,
    isFollowed: Boolean,
    sellerProducts: List<Product> = emptyList(),
    sellerServices: List<Service> = emptyList(),
    onSelectProduct: (Product) -> Unit = {},
    onSelectService: (Service) -> Unit = {},
    onToggleFollow: () -> Unit,
    onContact: () -> Unit,
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CosmicPurple.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .border(1.5.dp, CosmicCyan, RoundedCornerShape(14.dp))
                ) {
                    if (!business.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = business.avatarUrl,
                            contentDescription = business.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = business.name.take(2).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }
                }

                Button(
                    onClick = onToggleFollow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowed) CosmicSurfaceElevated else ElectricViolet
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = if (isFollowed) "Following Orbit" else "+ Follow Biz", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = business.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = StarWhite
            )

            Text(
                text = "Founder: ${business.ownerName} · 🎓 ${business.college}",
                fontSize = 12.sp,
                color = CosmicCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            CosmicBadgeRow(badges = business.badges)

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = business.tagline,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = StarWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = business.about,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = SoftLavender
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C1220))
                    .border(0.8.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${business.rating} ⭐", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    Text(text = "${business.reviewCount} Reviews", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${business.completedOrders}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                    Text(text = "Orders Delivered", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${business.responseRate}%", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CometBlue)
                    Text(text = "Response Rate", fontSize = 10.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 1: ALL PRODUCTS BY THIS SELLER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Products in Orbit (${sellerProducts.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "By ${business.name}",
                    fontSize = 11.sp,
                    color = CosmicCyan
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (sellerProducts.isEmpty()) {
                business.productsOffered.forEach { prodName ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CosmicSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(text = "🛍 $prodName", fontSize = 12.sp, color = StarWhite)
                    }
                }
            } else {
                sellerProducts.forEach { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { onSelectProduct(prod) },
                        colors = CardDefaults.cardColors(containerColor = CosmicSurfaceCard),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(CosmicBorder, CosmicCyan.copy(alpha = 0.3f)))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF090D18))
                                    .border(0.8.dp, CosmicBorder, RoundedCornerShape(10.dp))
                            ) {
                                if (!prod.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = prod.imageUrl,
                                        contentDescription = prod.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(text = "📦", fontSize = 24.sp)
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${prod.price}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CosmicCyan
                                    )
                                    if (prod.originalPrice != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "₹${prod.originalPrice}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "⭐ ${prod.rating}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFF59E0B)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CosmicPurple)
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(text = "View ➔", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 2: ALL SERVICES BY THIS SELLER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Services & Skills (${sellerServices.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (sellerServices.isEmpty()) {
                business.servicesOffered.forEach { servName ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10163C))
                            .padding(10.dp)
                    ) {
                        Text(text = "💼 $servName", fontSize = 12.sp, color = StarWhite)
                    }
                }
            } else {
                sellerServices.forEach { serv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectService(serv) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1538)),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(CosmicBorder, CometBlue.copy(alpha = 0.3f)))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = serv.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                                Text(
                                    text = "From ₹${serv.startingPrice} · ${serv.turnaroundDays} days delivery",
                                    fontSize = 10.5.sp,
                                    color = CometBlue
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricViolet.copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Request ➔", fontSize = 10.sp, color = CometBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onContact,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(text = "💬 Message Business on UniSpaceX", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailSheet(
    student: Student,
    onContact: () -> Unit,
    onEndorseSkill: ((Int) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(CosmicPurple, CometBlue)),
                            shape = CircleShape
                        )
                ) {
                    Text(text = student.name.take(1), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(text = student.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    Text(text = student.roleTitle, fontSize = 12.sp, color = CosmicCyan)
                    Text(text = "🎓 ${student.college}", fontSize = 11.sp, color = SoftLavender)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CosmicBadgeRow(badges = student.badges)

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = student.bio, fontSize = 12.sp, lineHeight = 18.sp, color = StarWhite.copy(alpha = 0.9f))

            Spacer(modifier = Modifier.height(16.dp))

            // Student Constellation Map
            SkillConstellation(
                skills = student.skills,
                title = "${student.name}'s Neural Constellation",
                subtitle = "Mapped skills and technical nodes"
            )

            // Skill Endorsement Chips
            if (student.skills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PEER SKILL ENDORSEMENTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftLavender,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(student.skills) { skill ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CosmicSurfaceCard)
                                .border(1.dp, CosmicBorderSubtle, RoundedCornerShape(12.dp))
                                .clickable { onEndorseSkill?.invoke(skill.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = skill.name,
                                    fontSize = 12.sp,
                                    color = StarWhite,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "👍 ${skill.endorsements}",
                                    fontSize = 11.sp,
                                    color = CosmicCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContact,
                colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(text = "Connect / Recruit for Project", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailSheet(
    product: Product,
    sellerBusiness: Business? = null,
    onViewSellerProfile: (Business) -> Unit = {},
    onAddToCart: ((Product) -> Unit)? = null,
    onBuyNow: ((Product) -> Unit)? = null,
    onContact: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeImagePreview by remember(product.id) {
        mutableStateOf(product.imageUrl ?: product.galleryImages.firstOrNull())
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            // Main Product Photo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CosmicSurfaceCard)
                    .border(1.2.dp, CosmicBorder, RoundedCornerShape(14.dp))
            ) {
                if (!activeImagePreview.isNullOrBlank()) {
                    AsyncImage(
                        model = activeImagePreview,
                        contentDescription = product.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(text = "🛍️", fontSize = 48.sp)
                }
            }

            // Multiple product images gallery thumbnails
            val allImages = remember(product) {
                val list = mutableListOf<String>()
                if (!product.imageUrl.isNullOrBlank()) list.add(product.imageUrl)
                list.addAll(product.galleryImages)
                list.distinct()
            }

            if (allImages.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allImages.forEach { imgUrl ->
                        val isSelected = (activeImagePreview == imgUrl)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0C1220))
                                .border(
                                    width = if (isSelected) 1.8.dp else 0.8.dp,
                                    color = if (isSelected) CosmicCyan else CosmicBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { activeImagePreview = imgUrl }
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Badges row: Condition & Semester Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CosmicSurfaceElevated)
                        .border(1.dp, CosmicBorderSubtle, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${product.condition.badge} ${product.condition.label}",
                        color = CosmicCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                product.semesterTag?.let { sem ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricViolet.copy(alpha = 0.2f))
                            .border(1.dp, ElectricViolet.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📚 $sem",
                            color = SoftLavender,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (product.isRental) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CelestialGold.copy(alpha = 0.2f))
                            .border(1.dp, CelestialGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "🔁 ${product.rentalDuration ?: "Rental Available"}",
                            color = CelestialGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = product.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarWhite)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "₹${product.price}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                if (product.originalPrice != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "₹${product.originalPrice}", fontSize = 14.sp, color = TextMuted)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "⭐ ${product.rating} (${product.reviewCount} reviews)", fontSize = 12.sp, color = Color(0xFFF59E0B))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = product.description, fontSize = 12.sp, lineHeight = 18.sp, color = SoftLavender)

            Spacer(modifier = Modifier.height(16.dp))

            // SELLER PROFILE CARD
            sellerBusiness?.let { seller ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CosmicSurfaceCard)
                        .border(1.dp, CometBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { onViewSellerProfile(seller) }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CosmicPurple.copy(alpha = 0.4f))
                                .border(1.2.dp, CosmicCyan, RoundedCornerShape(12.dp))
                        ) {
                            if (!seller.avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = seller.avatarUrl,
                                    contentDescription = seller.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = seller.name.take(2).uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = seller.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarWhite
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "🛡️", fontSize = 11.sp)
                            }
                            Text(
                                text = "Founder: ${seller.ownerName} · 🎓 ${seller.college}",
                                fontSize = 11.sp,
                                color = SoftLavender
                            )
                            Text(
                                text = "⭐ ${seller.rating} (${seller.reviewCount} reviews) · ${seller.completedOrders} orders",
                                fontSize = 10.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CometBlue.copy(alpha = 0.2f))
                                .border(0.6.dp, CometBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Seller Store ➔",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CosmicCyan
                            )
                        }
                    }
                }
            } ?: run {
                Text(text = "By ${product.businessName} · 🎓 ${product.college}", fontSize = 12.sp, color = CosmicCyan)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transaction buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAddToCart?.invoke(product) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CosmicCyan),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(text = "Add to Bag 🛍️", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                }

                Button(
                    onClick = {
                        onAddToCart?.invoke(product)
                        onBuyNow?.invoke(product)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text(text = "Meetup Buy ⚡", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onContact,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CosmicBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(text = "💬 Chat with Seller Before Buying", fontSize = 12.sp, color = SoftLavender)
            }

            Spacer(modifier = Modifier.height(8.dp))

            var showReportDialog by remember { mutableStateOf(false) }
            var selectedReportReason by remember { mutableStateOf(com.example.data.service.ReportReason.SPAM) }
            var reportSuccessMsg by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showReportDialog = true }
                ) {
                    Text(
                        text = "🚩 Report Listing or Seller",
                        color = Color(0xFFFDA4AF),
                        fontSize = 11.5.sp
                    )
                }

                if (reportSuccessMsg != null) {
                    Text(
                        text = "✓ Report Submitted",
                        color = Color(0xFF4ADE80),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showReportDialog) {
                AlertDialog(
                    onDismissRequest = { showReportDialog = false },
                    containerColor = CosmicSecondary,
                    title = {
                        Text(
                            text = "Report Campus Listing",
                            color = StarWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Help keep UNISpaceX safe. Select why you are reporting '${product.title}':",
                                color = SoftLavender,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            com.example.data.service.ReportReason.entries.forEach { reason ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedReportReason = reason }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedReportReason == reason,
                                        onClick = { selectedReportReason = reason },
                                        colors = RadioButtonDefaults.colors(selectedColor = CosmicCyan)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = reason.displayName,
                                        color = if (selectedReportReason == reason) CosmicCyan else StarWhite,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showReportDialog = false
                                coroutineScope.launch {
                                    com.example.data.service.ModerationService.getInstance().submitReport(
                                        targetType = "product",
                                        targetId = product.id,
                                        targetTitle = product.title,
                                        reason = selectedReportReason
                                    )
                                    reportSuccessMsg = "Report Submitted"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                        ) {
                            Text("Submit Report", color = StarWhite, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text("Cancel", color = SoftLavender)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailSheet(
    service: Service,
    onContact: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CosmicSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = service.category, fontSize = 11.sp, color = CometBlue, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(text = service.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarWhite)
            Text(text = "Provided by ${service.providerName} · 🎓 ${service.college}", fontSize = 12.sp, color = CosmicCyan)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CosmicSurfaceCard)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "₹${service.startingPrice}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                    Text(text = "Base Rate", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${service.turnaroundDays} Days", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CometBlue)
                    Text(text = "Turnaround", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${service.completedCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    Text(text = "Completed", fontSize = 10.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = service.description, fontSize = 12.sp, lineHeight = 18.sp, color = SoftLavender)

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onContact,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(text = "Request Official Quote & Milestone", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanetDetailSheet(
    planet: CampusPlanet,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color(planet.colorHex).copy(alpha = 0.4f), CircleShape)
                        .border(1.5.dp, Color(planet.accentHex), CircleShape)
                ) {
                    Text(text = "🪐", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(text = planet.shortName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    Text(text = planet.name, fontSize = 12.sp, color = SoftLavender)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(text = planet.description, fontSize = 12.sp, lineHeight = 18.sp, color = StarWhite.copy(alpha = 0.9f))

            Spacer(modifier = Modifier.height(14.dp))

            // Planet telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C1220))
                    .border(0.8.dp, CosmicBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${planet.studentCount}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    Text(text = "Students", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${planet.businessCount}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CosmicCyan)
                    Text(text = "Businesses", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${planet.serviceCount}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CometBlue)
                    Text(text = "Services", fontSize = 10.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${planet.productCount}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ElectricViolet)
                    Text(text = "Products", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModalSheet(
    activeType: CreateType?,
    isSellerVerified: Boolean = false,
    hasPendingSellerVerification: Boolean = false,
    onOpenSellerVerification: () -> Unit = {},
    onSelectType: (CreateType) -> Unit,
    onAddProduct: (String, Int, String, String, String?, List<String>) -> Unit,
    onAddService: (String, Int, String, Int, String) -> Unit,
    onCreateBusiness: (String, String, String, String) -> Unit,
    onCreateCollab: (String, String, Int, Int, List<String>, String) -> Unit,
    onCreatePost: (String, String, FeedCategory) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CosmicSecondary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        CosmicModalContainer {
            if (activeType == null) {
                Text(
                    text = "Launch into the Universe",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "What would you like to put into orbit on UniSpaceX?",
                    fontSize = 12.sp,
                    color = SoftLavender
                )
                Spacer(modifier = Modifier.height(16.dp))

                CreateType.values().forEach { type ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicSurface)
                            .border(1.dp, CosmicBorder, RoundedCornerShape(12.dp))
                            .clickable { onSelectType(type) }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = type.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(text = type.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                Text(text = type.subtitle, fontSize = 11.sp, color = SoftLavender)
                            }
                        }
                    }
                }
            } else {
                when (activeType) {
                    CreateType.PRODUCT -> {
                        CreateProductForm(
                            isSellerVerified = isSellerVerified,
                            hasPendingSellerVerification = hasPendingSellerVerification,
                            onOpenSellerVerification = onOpenSellerVerification,
                            onAddProduct = onAddProduct
                        )
                    }
                    CreateType.SERVICE -> {
                        CreateServiceForm(onAddService = onAddService)
                    }
                    CreateType.BUSINESS -> {
                        CreateBusinessForm(onCreateBusiness = onCreateBusiness)
                    }
                    CreateType.COLLAB -> {
                        CreateCollabForm(onCreateCollab = onCreateCollab)
                    }
                    CreateType.POST -> {
                        CreatePostForm(onCreatePost = onCreatePost)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateProductForm(
    isSellerVerified: Boolean = false,
    hasPendingSellerVerification: Boolean = false,
    onOpenSellerVerification: () -> Unit = {},
    onAddProduct: (String, Int, String, String, String?, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("399") }
    var category by remember { mutableStateOf("Tech") }
    var desc by remember { mutableStateOf("") }
    var primaryImageUrl by remember { mutableStateOf("") }
    var additionalImages by remember { mutableStateOf<List<String>>(emptyList()) }

    val singlePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            primaryImageUrl = uri.toString()
        }
    }

    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newUris = uris.map { it.toString() }
            if (primaryImageUrl.isBlank() && newUris.isNotEmpty()) {
                primaryImageUrl = newUris.first()
                additionalImages = (additionalImages + newUris.drop(1)).distinct()
            } else {
                additionalImages = (additionalImages + newUris).distinct()
            }
        }
    }

    Text(text = "Add Product to Orbit", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
    Text(text = "Upload product photos (multiple photos supported without limit) & set details", fontSize = 11.sp, color = SoftLavender)

    // Seller Verification Status Banner
    if (!isSellerVerified) {
        Spacer(modifier = Modifier.height(8.dp))
        if (hasPendingSellerVerification) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C1908)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⏳", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seller Verification Pending Admin Review", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        Text("Your Government ID, WhatsApp, and sample products are in the admin review queue. You can draft products now; they will become active once approved.", fontSize = 10.sp, color = SoftLavender)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1430)),
                border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛍️", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Seller Verification Required", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "To become an approved seller, submit any Government ID, product images, and your WhatsApp number. You will become a seller once our Admin approves.",
                        fontSize = 10.5.sp,
                        color = SoftLavender
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenSellerVerification,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Verify as Seller (Govt ID & WhatsApp) ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(12.dp))

    // Primary Product Image Preview / Upload Area
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CosmicSurfaceCard)
            .border(1.2.dp, if (primaryImageUrl.isNotBlank()) CosmicCyan else CosmicBorder, RoundedCornerShape(12.dp))
            .clickable {
                singlePhotoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
    ) {
        if (primaryImageUrl.isNotBlank()) {
            AsyncImage(
                model = primaryImageUrl,
                contentDescription = "Primary Product Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xDD090E1A))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = CosmicCyan, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Change Primary", fontSize = 10.sp, color = StarWhite, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Upload product photo",
                    tint = CosmicCyan,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to Select Primary Product Image",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Text(
                    text = "Select from camera or gallery",
                    fontSize = 10.sp,
                    color = SoftLavender
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Multiple Additional Images Row (Upload without limit)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Product Gallery (${additionalImages.size + if (primaryImageUrl.isNotBlank()) 1 else 0} photos)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = StarWhite
        )

        Button(
            onClick = {
                multiplePhotoPicker.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceElevated),
            border = ButtonDefaults.outlinedButtonBorder().copy(
                brush = Brush.horizontalGradient(listOf(CosmicBorder, CometBlue))
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp)
        ) {
            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(12.dp), tint = CosmicCyan)
            Spacer(modifier = Modifier.width(4.dp))
            Text("+ Add More Photos (Unlimited)", fontSize = 10.sp, color = CosmicCyan, fontWeight = FontWeight.Bold)
        }
    }

    if (additionalImages.isNotEmpty() || primaryImageUrl.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (primaryImageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, CosmicCyan, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = primaryImageUrl,
                        contentDescription = "Primary",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            additionalImages.forEachIndexed { index, imgUrl ->
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(0.8.dp, CosmicBorder, RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = "Additional $index",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Remove button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(Color(0xCC000000), CircleShape)
                            .clickable {
                                additionalImages = additionalImages.toMutableList().also { it.removeAt(index) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("×", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Preset Campus Product Image Picks
    Text(text = "Or quick-select category photo style:", fontSize = 10.sp, color = TextMuted)
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val samplePresets = listOf(
            "⚡ Hardware" to "https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop&q=80",
            "👕 Apparel" to "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&auto=format&fit=crop&q=80",
            "☕ Food/Treats" to "https://images.unsplash.com/photo-1579954115545-a95591f28bfc?w=500&auto=format&fit=crop&q=80",
            "🎨 Art/Sticker" to "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500&auto=format&fit=crop&q=80",
            "📚 Stationery" to "https://images.unsplash.com/photo-1507842229451-79b1be886a27?w=500&auto=format&fit=crop&q=80"
        )
        samplePresets.forEach { (label, url) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CosmicSurfaceElevated)
                    .border(0.6.dp, CosmicBorder, RoundedCornerShape(6.dp))
                    .clickable {
                        if (primaryImageUrl.isBlank()) {
                            primaryImageUrl = url
                        } else {
                            additionalImages = (additionalImages + url).distinct()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = SoftLavender
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
    CosmicTextField(value = title, onValueChange = { title = it }, label = "Product Name (e.g. Custom ESP32 Shield)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = price, onValueChange = { price = it }, label = "Price in ₹")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = category, onValueChange = { category = it }, label = "Category (Tech, Fashion, Food, Academic)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = desc, onValueChange = { desc = it }, label = "Description & Features", singleLine = false)
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = primaryImageUrl, onValueChange = { primaryImageUrl = it }, label = "Or Paste Product Image URL")
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (title.isNotBlank()) {
                val fullGallery = (listOf(primaryImageUrl) + additionalImages).filter { it.isNotBlank() }.distinct()
                onAddProduct(
                    title,
                    price.toIntOrNull() ?: 299,
                    category,
                    desc,
                    primaryImageUrl.ifBlank { fullGallery.firstOrNull() },
                    fullGallery
                )
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Text(text = "☄️ Put Product in Orbit", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CreateServiceForm(onAddService: (String, Int, String, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("500") }
    var category by remember { mutableStateOf("Design") }
    var days by remember { mutableStateOf("3") }
    var desc by remember { mutableStateOf("") }

    Text(text = "Offer Student Skill / Service", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
    Spacer(modifier = Modifier.height(10.dp))

    CosmicTextField(value = title, onValueChange = { title = it }, label = "Service Title (e.g. High-Speed PCB Layout)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = price, onValueChange = { price = it }, label = "Starting Price (₹)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = category, onValueChange = { category = it }, label = "Category (Tech, Video, Design, Tutoring)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = days, onValueChange = { days = it }, label = "Turnaround Time (Days)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = desc, onValueChange = { desc = it }, label = "Service Details & Deliverables", singleLine = false)
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (title.isNotBlank()) onAddService(title, price.toIntOrNull() ?: 500, category, days.toIntOrNull() ?: 3, desc)
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Text(text = "💼 Launch Service Gig", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CreateBusinessForm(onCreateBusiness: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Hardware & Design") }
    var tagline by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("") }

    Text(text = "Launch Student Business", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
    Spacer(modifier = Modifier.height(10.dp))

    CosmicTextField(value = name, onValueChange = { name = it }, label = "Business Name (e.g. Orbitron Studio)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = category, onValueChange = { category = it }, label = "Category")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = tagline, onValueChange = { tagline = it }, label = "Short Tagline")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = about, onValueChange = { about = it }, label = "About your student venture", singleLine = false)
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (name.isNotBlank()) onCreateBusiness(name, category, tagline, about)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Text(text = "🚀 Register Student Business", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CreateCollabForm(onCreateCollab: (String, String, Int, Int, List<String>, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var projectType by remember { mutableStateOf("Hackathon") }
    var budget by remember { mutableStateOf("1000") }
    var days by remember { mutableStateOf("5") }
    var skills by remember { mutableStateOf("Video Editing, Motion Graphics") }
    var desc by remember { mutableStateOf("") }

    Text(text = "Recruit Crew / Post Project Bounty", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
    Spacer(modifier = Modifier.height(10.dp))

    CosmicTextField(value = title, onValueChange = { title = it }, label = "Project Bounty Title")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = projectType, onValueChange = { projectType = it }, label = "Type (Hackathon, Startup, Media, Co-founder)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = budget, onValueChange = { budget = it }, label = "Budget (₹)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = days, onValueChange = { days = it }, label = "Deadline in Days")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = skills, onValueChange = { skills = it }, label = "Skills Needed (comma separated)")
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = desc, onValueChange = { desc = it }, label = "Role Details", singleLine = false)
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (title.isNotBlank()) {
                val list = skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                onCreateCollab(title, projectType, budget.toIntOrNull() ?: 1000, days.toIntOrNull() ?: 5, list, desc)
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Text(text = "🤝 Post Crew Bounty", fontWeight = FontWeight.Bold, color = StarWhite)
    }
}

@Composable
private fun CreatePostForm(onCreatePost: (String, String, FeedCategory) -> Unit) {
    var content by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("CampusMilestone") }
    var category by remember { mutableStateOf(FeedCategory.ACHIEVEMENT) }

    Text(text = "Broadcast to Campus Space", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StarWhite)
    Spacer(modifier = Modifier.height(10.dp))

    CosmicTextField(value = content, onValueChange = { content = it }, label = "What milestone, drop, or update are you sharing?", singleLine = false)
    Spacer(modifier = Modifier.height(8.dp))
    CosmicTextField(value = tag, onValueChange = { tag = it }, label = "Hashtag (e.g. VLSI, NewDrop, Hackathon)")
    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (content.isNotBlank()) onCreatePost(content, tag, category)
        },
        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Text(text = "📡 Broadcast Update", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CosmicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp, color = SoftLavender) },
        singleLine = singleLine,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CosmicSurface,
            unfocusedContainerColor = CosmicSurface,
            focusedBorderColor = CosmicCyan,
            unfocusedBorderColor = CosmicBorder,
            focusedTextColor = StarWhite,
            unfocusedTextColor = StarWhite
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<UiNotification>,
    onDismiss: () -> Unit,
    onMarkAllAsRead: () -> Unit = {}
) {
    val unreadCount = notifications.count { !it.isRead }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        onMarkAllAsRead()
    }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Cosmic Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                    Text(
                        text = if (unreadCount > 0) "$unreadCount unread transmission${if (unreadCount > 1) "s" else ""}" else "All notifications seen • Signal clear",
                        fontSize = 11.sp,
                        color = if (unreadCount > 0) CosmicCyan else SoftLavender
                    )
                }

                if (unreadCount > 0) {
                    Button(
                        onClick = onMarkAllAsRead,
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Mark Read", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            notifications.forEach { notif ->
                val isUnread = !notif.isRead
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isUnread) CosmicSurfaceElevated else CosmicSurface)
                        .border(
                            0.8.dp,
                            if (isUnread) CosmicCyan.copy(alpha = 0.5f) else CosmicBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        if (isUnread) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp, end = 8.dp)
                                    .size(8.dp)
                                    .background(Color(0xFFF43F5E), CircleShape)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = notif.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnread) CosmicCyan else StarWhite
                                )
                                Text(text = notif.time, fontSize = 10.sp, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = notif.description, fontSize = 11.sp, color = if (isUnread) StarWhite else SoftLavender)
                        }
                    }
                }
            }
        }
    }
}
