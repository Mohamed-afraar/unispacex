package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.OrderProcess
import com.example.ui.theme.*
import com.example.viewmodel.UniSpaceUiState

data class SellerContact(
    val id: String,
    val name: String,
    val role: String,
    val college: String,
    val avatarUrl: String? = null,
    val iconBadge: String = "🛍️"
)

@Composable
fun MessagesScreen(
    uiState: UniSpaceUiState,
    onSendMessage: (String) -> Unit,
    onOpenOrder: (OrderProcess) -> Unit,
    onSelectSeller: (sellerId: String, name: String, role: String, college: String, avatar: String?) -> Unit = { _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var contactSearchQuery by remember { mutableStateOf("") }

    val allSellerContacts = remember(uiState.businesses, uiState.students) {
        val bizList = uiState.businesses.map { biz ->
            SellerContact(
                id = biz.id,
                name = biz.name,
                role = biz.tagline.ifBlank { "Campus Seller · ${biz.category}" },
                college = biz.college,
                avatarUrl = biz.avatarUrl,
                iconBadge = "🛍️"
            )
        }
        val studentList = uiState.students.filter { it.name != uiState.userProfile.name }.map { st ->
            SellerContact(
                id = st.id,
                name = st.name,
                role = st.roleTitle,
                college = st.college,
                avatarUrl = st.avatarUrl,
                iconBadge = "🎓"
            )
        }
        (bizList + studentList).distinctBy { it.id }
    }

    val filteredContacts = remember(allSellerContacts, contactSearchQuery) {
        if (contactSearchQuery.isBlank()) allSellerContacts
        else allSellerContacts.filter {
            it.name.contains(contactSearchQuery, ignoreCase = true) ||
            it.role.contains(contactSearchQuery, ignoreCase = true) ||
            it.college.contains(contactSearchQuery, ignoreCase = true)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("messages_screen")
    ) {
        val isWideScreen = maxWidth >= 720.dp

        if (isWideScreen) {
            // ==========================================
            // CANONICAL TABLET/EXPANDED 2-PANE LIST-DETAIL
            // ==========================================
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Pane: Contacts & Direct Transmission Directory (300.dp)
                Column(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                        .background(CosmicSurface)
                        .border(width = 0.8.dp, color = CosmicBorder)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CAMPUS TRANSMISSIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact Search
                    OutlinedTextField(
                        value = contactSearchQuery,
                        onValueChange = { contactSearchQuery = it },
                        placeholder = { Text("Search talent or seller...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CosmicSurfaceElevated,
                            unfocusedContainerColor = CosmicSurfaceElevated,
                            focusedBorderColor = CosmicCyan,
                            unfocusedBorderColor = CosmicBorderSubtle,
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        ),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredContacts) { contact ->
                            val isSelected = (contact.id == uiState.activeChatRecipientId)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) ElectricViolet.copy(alpha = 0.35f) else CosmicSurfaceCard)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.5.dp,
                                        color = if (isSelected) CosmicCyan else CosmicBorderSubtle,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onSelectSeller(contact.id, contact.name, contact.role, contact.college, contact.avatarUrl)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(CosmicPurple, Color(0xFF0E1424))))
                                        .border(1.dp, if (isSelected) CosmicCyan else CosmicBorderSubtle, CircleShape)
                                ) {
                                    if (!contact.avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = contact.avatarUrl,
                                            contentDescription = contact.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text(text = contact.name.take(1), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StarWhite)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = contact.name,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = StarWhite,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = contact.iconBadge, fontSize = 10.sp)
                                    }
                                    Text(
                                        text = contact.role,
                                        fontSize = 10.5.sp,
                                        color = if (isSelected) CosmicCyan else TextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Pane: Active Chat Conversation (weight 1f)
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // Header Bar
                    ConversationHeaderBar(
                        uiState = uiState,
                        onOpenOrder = onOpenOrder
                    )

                    // Messages Stream
                    ChatMessagesStream(
                        uiState = uiState,
                        onOpenOrder = onOpenOrder,
                        modifier = Modifier.weight(1f)
                    )

                    // Message Composer Bar (Compact bottom padding for tablet)
                    MessageComposerBar(
                        inputText = inputText,
                        recipientName = uiState.activeChatRecipientName,
                        onTextChange = { inputText = it },
                        onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        bottomPadding = 16.dp
                    )
                }
            }
        } else {
            // ==========================================
            // COMPACT PHONE SINGLE-COLUMN LAYOUT
            // ==========================================
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar with Horizontal Carousel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicSurface)
                        .border(0.8.dp, CosmicBorder, RoundedCornerShape(0.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Column {
                        ConversationHeaderBarContent(
                            uiState = uiState,
                            onOpenOrder = onOpenOrder
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Switch Seller / Contact Pills (Horizontal Carousel)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Sellers:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SoftLavender
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(allSellerContacts) { contact ->
                                    val isSelected = (contact.id == uiState.activeChatRecipientId)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isSelected) ElectricViolet else CosmicSurfaceCard
                                            )
                                            .border(
                                                width = if (isSelected) 1.2.dp else 0.6.dp,
                                                color = if (isSelected) CosmicCyan else CosmicBorder,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                onSelectSeller(
                                                    contact.id,
                                                    contact.name,
                                                    contact.role,
                                                    contact.college,
                                                    contact.avatarUrl
                                                )
                                            }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = contact.iconBadge, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = contact.name,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) StarWhite else SoftLavender
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Messages Stream
                ChatMessagesStream(
                    uiState = uiState,
                    onOpenOrder = onOpenOrder,
                    modifier = Modifier.weight(1f)
                )

                // Message Composer Bar (with 75dp space for phone bottom bar)
                MessageComposerBar(
                    inputText = inputText,
                    recipientName = uiState.activeChatRecipientName,
                    onTextChange = { inputText = it },
                    onSend = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    bottomPadding = 8.dp
                )
            }
        }
    }
}

@Composable
private fun ConversationHeaderBar(
    uiState: UniSpaceUiState,
    onOpenOrder: (OrderProcess) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurface)
            .border(0.8.dp, CosmicBorder, RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ConversationHeaderBarContent(uiState = uiState, onOpenOrder = onOpenOrder)
    }
}

@Composable
private fun ConversationHeaderBarContent(
    uiState: UniSpaceUiState,
    onOpenOrder: (OrderProcess) -> Unit
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
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(listOf(CosmicPurple, CometBlue)),
                        shape = CircleShape
                    )
                    .border(1.2.dp, CosmicCyan, CircleShape)
            ) {
                if (!uiState.activeChatRecipientAvatar.isNullOrBlank()) {
                    AsyncImage(
                        model = uiState.activeChatRecipientAvatar,
                        contentDescription = uiState.activeChatRecipientName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = uiState.activeChatRecipientName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = StarWhite
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.activeChatRecipientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(CosmicSuccess, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${uiState.activeChatRecipientRole} · ${uiState.activeChatRecipientCollege.split(" ").first()}",
                        fontSize = 10.sp,
                        color = CosmicCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Order tracker shortcut if exists
        val relatedOrder = uiState.orders.firstOrNull { it.providerName.contains(uiState.activeChatRecipientName, ignoreCase = true) || it.orderId == "ORD-7821" }
        if (relatedOrder != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricViolet.copy(alpha = 0.25f))
                    .border(0.8.dp, CometBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { onOpenOrder(relatedOrder) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "☄️", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = relatedOrder.orderId,
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
private fun ChatMessagesStream(
    uiState: UniSpaceUiState,
    onOpenOrder: (OrderProcess) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMessages = uiState.currentChatMessages
    if (currentMessages.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(CosmicSurface)
                    .border(1.dp, CosmicBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(text = "🛰️", fontSize = 36.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Direct Transmission with ${uiState.activeChatRecipientName}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = StarWhite
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Inquire about products, custom work, turnaround time, or quotes. Transmissions are encrypted in the campus galaxy.",
                    fontSize = 12.sp,
                    color = SoftLavender,
                    lineHeight = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentMessages) { message ->
                ChatMessageBubble(
                    message = message,
                    onAcceptQuote = {
                        val relatedOrder = uiState.orders.firstOrNull { it.orderId == "ORD-7821" }
                        if (relatedOrder != null) onOpenOrder(relatedOrder)
                    }
                )
            }
        }
    }
}

@Composable
private fun MessageComposerBar(
    inputText: String,
    recipientName: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CosmicSurfaceGlass)
            .border(0.8.dp, CosmicBorder, RoundedCornerShape(0.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .padding(bottom = bottomPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChange,
                placeholder = { Text("Message $recipientName...", color = TextMuted, fontSize = 13.sp) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CosmicSurface,
                    unfocusedContainerColor = CosmicSurface,
                    focusedBorderColor = CosmicCyan,
                    unfocusedBorderColor = CosmicBorder,
                    focusedTextColor = StarWhite,
                    unfocusedTextColor = StarWhite
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(42.dp)
                    .background(CosmicPurple, CircleShape)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = StarWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}


@Composable
private fun ChatMessageBubble(
    message: ChatMessage,
    onAcceptQuote: () -> Unit
) {
    val isMe = message.isFromMe

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isMe) CosmicCyan else ElectricViolet
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = message.timestamp,
                fontSize = 9.sp,
                color = TextMuted
            )
        }

        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isMe) 14.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 14.dp
                    )
                )
                .background(
                    if (isMe) {
                        Brush.linearGradient(listOf(ElectricViolet, CosmicPurple))
                    } else {
                        Brush.linearGradient(listOf(CosmicSurfaceCard, CosmicSurfaceElevated))
                    }
                )
                .border(
                    0.8.dp,
                    if (isMe) ElectricViolet.copy(alpha = 0.5f) else CosmicBorder,
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isMe) 14.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 14.dp
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                // Attached Service or Product Reference
                if (message.serviceRef != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1220))
                            .border(0.6.dp, CometBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "💼 Inquiring about: ${message.serviceRef}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = CosmicCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = StarWhite
                )

                // Quote Box
                if (message.quoteAmount != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0C1220))
                            .border(1.dp, CosmicCyan, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Official Cosmic Quote", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = "₹${message.quoteAmount}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicCyan
                                )
                            }

                            Button(
                                onClick = onAcceptQuote,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CosmicPurple,
                                    contentColor = StarWhite
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(text = "Accept Quote", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
