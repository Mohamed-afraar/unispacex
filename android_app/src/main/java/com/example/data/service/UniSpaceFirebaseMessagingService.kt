package com.example.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Enterprise Firebase Cloud Messaging (FCM) Service for UNISpaceX.
 * Handles background push notifications for new peer chat messages, order updates,
 * and campus seller verification approvals.
 */
class UniSpaceFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "UniSpaceFCM"
        const val CHANNEL_ID_CHAT = "unispace_chat_channel"
        const val CHANNEL_NAME_CHAT = "Campus Chat & Messages"

        const val CHANNEL_ID_ORDERS = "unispace_orders_channel"
        const val CHANNEL_NAME_ORDERS = "Campus Orders & Handover"

        const val CHANNEL_ID_VERIFICATION = "unispace_verification_channel"
        const val CHANNEL_NAME_VERIFICATION = "Student Identity & Seller Approvals"

        /**
         * Ensures all Android notification channels are registered with the OS.
         */
        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val chatChannel = NotificationChannel(
                    CHANNEL_ID_CHAT,
                    CHANNEL_NAME_CHAT,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Instant notifications when students message you regarding products or gigs"
                    enableVibration(true)
                }

                val orderChannel = NotificationChannel(
                    CHANNEL_ID_ORDERS,
                    CHANNEL_NAME_ORDERS,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts for campus order status, handover meetups, and buyer requests"
                    enableVibration(true)
                }

                val verificationChannel = NotificationChannel(
                    CHANNEL_ID_VERIFICATION,
                    CHANNEL_NAME_VERIFICATION,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates on student identity and campus seller application status"
                }

                notificationManager.createNotificationChannels(listOf(chatChannel, orderChannel, verificationChannel))
                Log.d(TAG, "Notification channels registered successfully.")
            }
        }

        /**
         * Syncs the current FCM device token to Firestore linked to the authenticated student profile.
         */
        fun syncDeviceTokenToFirestore(token: String) {
            try {
                val auth = FirebaseAuth.getInstance()
                val uid = auth.currentUser?.uid ?: auth.currentUser?.email?.substringBefore("@")
                if (!uid.isNullOrBlank()) {
                    val tokenData = hashMapOf(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to System.currentTimeMillis()
                    )
                    FirebaseFirestore.getInstance().collection("user_profiles")
                        .document(uid)
                        .set(tokenData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Device FCM token registered in Firestore for user: $uid")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to register FCM token in Firestore: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error syncing FCM token: ${e.message}")
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM Device Registration Token generated: $token")
        syncDeviceTokenToFirestore(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = notification?.title ?: data["title"] ?: "UNISpaceX Alert"
        val body = notification?.body ?: data["body"] ?: "You have a new campus notification."
        val type = data["type"] ?: "general" // "chat", "order", "verification"

        showNotification(title, body, type, data)
    }

    private fun showNotification(title: String, body: String, type: String, data: Map<String, String>) {
        createNotificationChannels(this)

        val channelId = when (type.lowercase()) {
            "chat", "message" -> CHANNEL_ID_CHAT
            "order", "handover" -> CHANNEL_ID_ORDERS
            "verification", "seller" -> CHANNEL_ID_VERIFICATION
            else -> CHANNEL_ID_CHAT
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("notification_type", type)
            data.forEach { (k, v) -> putExtra(k, v) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
