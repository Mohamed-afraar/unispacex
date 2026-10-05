package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatRepository
import com.example.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val activeChatRecipientId: String = "",
    val activeChatRecipientName: String = "",
    val activeChatRecipientRole: String = "",
    val activeChatRecipientCollege: String = "",
    val activeChatRecipientAvatar: String? = null,
    val chatConversations: Map<String, List<ChatMessage>> = emptyMap()
) {
    val currentChatMessages: List<ChatMessage>
        get() = chatConversations[activeChatRecipientId] ?: emptyList()
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepository.conversationsFlow.collect { convos ->
                _uiState.update { it.copy(chatConversations = convos) }
            }
        }
    }

    fun selectChatRecipient(
        recipientId: String,
        name: String,
        role: String,
        college: String,
        avatar: String? = null,
        initialMessage: String? = null
    ) {
        _uiState.update { state ->
            state.copy(
                activeChatRecipientId = recipientId,
                activeChatRecipientName = name,
                activeChatRecipientRole = role,
                activeChatRecipientCollege = college,
                activeChatRecipientAvatar = avatar
            )
        }
        if (!initialMessage.isNullOrBlank()) {
            sendMessage(initialMessage)
        }
    }

    fun sendMessage(text: String, quote: Int? = null, serviceRef: String? = null) {
        if (text.isBlank()) return
        val currentRecipientId = _uiState.value.activeChatRecipientId
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val senderId = authUser?.uid ?: authUser?.email?.substringBefore("@") ?: "student"
        val senderName = authUser?.displayName ?: authUser?.email?.substringBefore("@") ?: "Student"

        val newMsg = ChatMessage(
            id = "msg-${System.currentTimeMillis()}",
            senderId = senderId,
            senderName = senderName,
            senderRole = "Student Innovator",
            text = text,
            timestamp = "Just now",
            isFromMe = true,
            recipientId = currentRecipientId,
            quoteAmount = quote,
            serviceRef = serviceRef
        )
        chatRepository.addMessage(newMsg)
        viewModelScope.launch {
            chatRepository.syncMessageToFirestore(newMsg)
        }
    }
}
