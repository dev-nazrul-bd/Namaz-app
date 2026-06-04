package com.example.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URL

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val CHANNEL_ID = "namaz_parental_channel"
    private const val NOTIFICATION_ID = 2026

    private var databaseInitialized = false
    private var deviceId: String = "unknown_device"

    // Observed UI States
    private val _screenBlockState = MutableStateFlow(false) // Screen Block status
    val screenBlockState: StateFlow<Boolean> = _screenBlockState

    private val _parentalNoticeAction = MutableStateFlow<String?>(null) // Pending link
    val parentalNoticeAction: StateFlow<String?> = _parentalNoticeAction

    private val _adsEnabledState = MutableStateFlow(true) // Ads enabled status (default true)
    val adsEnabledState: StateFlow<Boolean> = _adsEnabledState

    private var lastProcessedTimestamp: Long = 0L

    /**
     * Initializes Firebase Realtime Database with offline persistence enabled.
     */
    fun initialize(context: Context) {
        if (databaseInitialized) return
        try {
            // Enable offline persistence for fully robust offline-first synchronization
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            databaseInitialized = true
            Log.d(TAG, "Firebase Persistence Enabled Successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable persistence (might be already initialized): ${e.message}")
        }

        // Generate persistent/unique device identifier sanitized for Firebase child keys
        val sharedPrefs = context.getSharedPreferences("namaz_firebase_prefs", Context.MODE_PRIVATE)
        var savedId = sharedPrefs.getString("device_unique_id", null)
        if (savedId.isNullOrEmpty()) {
            val manufacturer = Build.MANUFACTURER.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
            val model = Build.MODEL.replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
            val suffix = try {
                val rawId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                if (!rawId.isNullOrEmpty()) {
                    rawId.substring(Math.max(0, rawId.length - 6)).replace("[^a-zA-Z0-9]".toRegex(), "").lowercase()
                } else {
                    (100000..999999).random().toString()
                }
            } catch (e: Exception) {
                (100000..999999).random().toString()
            }
            savedId = "${manufacturer}_${model}_$suffix"
            sharedPrefs.edit().putString("device_unique_id", savedId).apply()
        }
        deviceId = savedId

        Log.d(TAG, "Device registered with identifier: $deviceId")
    }

    private fun buildManufacturerModel(): String {
        val rawName = "${Build.MANUFACTURER}-${Build.MODEL}"
        return rawName.replace("[^a-zA-Z0-9-]".toRegex(), "").lowercase()
    }

    /**
     * Starts listening to commands and uploads the required initial structure.
     */
    fun startParentalControlListener(context: Context) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val commandRef = rootRef.child("Namaz").child("Comand").child(deviceId)

        // Read first to see if config already exists. If yes, preserve. If no, initialize structure
        commandRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Upload the structure requested by the user
                    val structure = mapOf(
                        "deviceName" to "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})",
                        "screen_block" to "off",
                        "ads" to "on",
                        "notification" to mapOf(
                            "action" to "https://classroom.google.com",
                            "body" to "Please return to your books immediately and lock unnecessary screens.",
                            "photo" to "https://images.unsplash.com/photo-1497633762265-9d179a990aa6?w=600&auto=format&fit=crop",
                            "status" to "displayed",
                            "timestamp" to 1780573825229L,
                            "title" to "Study Time Announcement! 📚"
                        )
                    )
                    commandRef.setValue(structure)
                        .addOnSuccessListener { Log.d(TAG, "Uploaded default structure to Firebase successfully") }
                        .addOnFailureListener { e -> Log.e(TAG, "Failed uploading default structure: ${e.message}") }
                } else {
                    // Just update the identifying information to keep dynamic updates
                    commandRef.child("deviceName").setValue("${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed checking existence: ${error.message}")
            }
        })

        // Actively listen to changes at `/Namaz/Comand/<device_id>/`
        commandRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                // 1. Observe Screen Block Command
                val blockVal = snapshot.child("screen_block").getValue(String::class.java) ?: "off"
                _screenBlockState.value = blockVal.equals("on", ignoreCase = true)

                // 2. Observe Ads configuration (default "on")
                val adsVal = snapshot.child("ads").getValue(String::class.java) ?: "on"
                _adsEnabledState.value = !adsVal.equals("off", ignoreCase = true)

                // Cache action URL in case screen block is active to direct students
                val notifSnapshot = snapshot.child("notification")
                val actionUrl = notifSnapshot.child("action").getValue(String::class.java) ?: "https://classroom.google.com"
                _parentalNoticeAction.value = actionUrl

                // 3. Observe Parental Notification Structure Command
                val title = notifSnapshot.child("title").getValue(String::class.java) ?: "Announcement"
                val body = notifSnapshot.child("body").getValue(String::class.java) ?: ""
                val photo = notifSnapshot.child("photo").getValue(String::class.java) ?: ""
                val status = notifSnapshot.child("status").getValue(String::class.java) ?: "displayed"
                val timestamp = notifSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L

                Log.d(TAG, "Notice received: status=$status, ts=$timestamp, title=$title, block=$blockVal")

                // If status is "sent", or if timestamp is new, trigger notification
                if (status == "sent" || (timestamp > 0 && timestamp != lastProcessedTimestamp)) {
                    lastProcessedTimestamp = timestamp
                    
                    // Trigger Native Local Notification
                    triggerNativeNotification(context, title, body, actionUrl, photo)

                    // Immediately write back status update: displayed
                    commandRef.child("notification").child("status").setValue("displayed")
                        .addOnSuccessListener { Log.d(TAG, "Successfully reported 'displayed' back to online database") }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Observer cancelled: ${error.message}")
            }
        })
    }

    /**
     * Build and trigger standard Android high-priority notification with pending action redirect.
     */
    private fun triggerNativeNotification(
        context: Context,
        title: String,
        body: String,
        actionUrl: String,
        photoUrl: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel on Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Parental Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Strict notices and announcements regarding Study schedules."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action Pending Intent to open study class link in browser
        val openIntent = Intent(Intent.ACTION_VIEW, Uri.parse(actionUrl)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        CoroutineScope(Dispatchers.IO).launch {
            // Load Bitmap from URL for parental attachments (supports Unsplash images)
            val parentImage: Bitmap? = try {
                val connection = URL(photoUrl).openConnection()
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                BitmapFactory.decodeStream(connection.getInputStream())
            } catch (e: Exception) {
                Log.e(TAG, "Could not load bitmap online: ${e.message}")
                null
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setStyle(
                    if (parentImage != null) {
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(parentImage)
                            .setSummaryText(body)
                    } else {
                        NotificationCompat.BigTextStyle().bigText(body)
                    }
                )
                // Action shortcut key
                .addAction(
                    android.R.drawable.ic_menu_edit,
                    "Open Study Resource",
                    pendingIntent
                )

            notificationManager.notify(NOTIFICATION_ID, builder.build())
            Log.d(TAG, "Notification fired successfully on device")
        }
    }

    fun getDeviceId(): String {
        return deviceId
    }
}
