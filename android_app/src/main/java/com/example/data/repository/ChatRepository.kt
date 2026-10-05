package com.example.data.repository

import com.example.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val _conversations = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val conversationsFlow: Flow<Map<String, List<ChatMessage>>> = _conversations.asStateFlow()

    private var incomingListenerRegistration: ListenerRegistration? = null
    private var outgoingListenerRegistration: ListenerRegistration? = null

    init {
        // Start listening to realtime messages for current authenticated student
        startRealtimeListener()
    }

    fun startRealtimeListener(userId: String? = null) {
        val currentUid = userId 
            ?: FirebaseAuth.getInstance().currentUser?.uid 
            ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@")
            ?: ""

        if (currentUid.isBlank()) {
            Timber.w("ChatRepository: User not logged in, listening to public messages broadcast")
        }

        // Clean up any existing listeners before starting fresh
        incomingListenerRegistration?.remove()
        outgoingListenerRegistration?.remove()

        try {
            // Listen for all messages in the campus network ordered by creation time
            incomingListenerRegistration = firestore.collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Timber.w(error, "Error listening to realtime Firestore messages: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null && !snapshot.isEmpty) {
                        val activeUser = FirebaseAuth.getInstance().currentUser?.uid 
                            ?: FirebaseAuth.getInstance().currentUser?.email?.substringBefore("@") 
                            ?: ""

                        val grouped = _conversations.value.toMutableMap()

                        for (doc in snapshot.documents) {
                            val id = doc.getString("id") ?: doc.id
                            val senderId = doc.getString("senderId") ?: ""
                            val senderName = doc.getString("senderName") ?: "Campus Student"
                            val senderRole = doc.getString("senderRole") ?: "Student"
                            val text = doc.getString("text") ?: ""
                            val timestamp = doc.getString("timestamp") ?: "Recently"
                            val recipientId = doc.getString("recipientId") ?: ""
                            val productRef = doc.getString("productRef")
                            val serviceRef = doc.getString("serviceRef")
                            val quoteAmount = doc.getLong("quoteAmount")?.toInt()

                            val isMine = if (activeUser.isNotBlank()) {
                                senderId == activeUser || (senderId.isBlank() && doc.getBoolean("isFromMe") == true)
                            } else {
                                doc.getBoolean("isFromMe") ?: false
                            }

                            val partnerId = if (isMine) recipientId else (if (senderId.isNotBlank()) senderId else senderName)

                            if (partnerId.isNotBlank()) {
                                val currentList = grouped[partnerId]?.toMutableList() ?: mutableListOf()
                                if (currentList.none { it.id == id }) {
                                    currentList.add(
                                        ChatMessage(
                                            id = id,
                                            senderId = senderId,
                                            senderName = senderName,
                                            senderRole = senderRole,
                                            text = text,
                                            timestamp = timestamp,
                                            isFromMe = isMine,
                                            recipientId = recipientId,
                                            productRef = productRef,
                                            serviceRef = serviceRef,
                                            quoteAmount = quoteAmount
                                        )
                                    )
                                    grouped[partnerId] = currentList
                                }
                            }
                        }

                        _conversations.value = grouped
                    }
                }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Firestore message snapshot listener")
        }
    }

    fun getMessagesForRecipient(recipientId: String): Flow<List<ChatMessage>> =
        _conversations.map { it[recipientId] ?: emptyList() }

    fun addMessage(message: ChatMessage) {
        val current = _conversations.value.toMutableMap()
        val recipientId = message.recipientId
        val existing = (current[recipientId] ?: emptyList()) + message
        current[recipientId] = existing
        _conversations.value = current
    }

    suspend fun syncMessageToFirestore(message: ChatMessage) {
        try {
            val currentAuth = FirebaseAuth.getInstance().currentUser
            val effectiveSenderId = message.senderId.ifBlank {
                currentAuth?.uid ?: currentAuth?.email?.substringBefore("@") ?: "local_student"
            }
            val effectiveSenderName = message.senderName.ifBlank {
                currentAuth?.displayName ?: currentAuth?.email?.substringBefore("@") ?: "Student"
            }

            val messageMap = hashMapOf(
                "id" to message.id,
                "senderId" to effectiveSenderId,
                "senderName" to effectiveSenderName,
                "senderRole" to message.senderRole,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "isFromMe" to message.isFromMe,
                "recipientId" to message.recipientId,
                "productRef" to message.productRef,
                "serviceRef" to message.serviceRef,
                "quoteAmount" to message.quoteAmount,
                "createdAt" to System.currentTimeMillis()
            )
            firestore.collection("messages").document(message.id).set(messageMap).await()
            Timber.d("Message synced to Firestore: ${message.id}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync message to Firestore")
        }
    }
}
