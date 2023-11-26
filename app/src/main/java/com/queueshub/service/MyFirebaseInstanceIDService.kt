package com.queueshub.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.queueshub.R
import com.queueshub.ui.MainActivity
import com.queueshub.utils.sendNotification
import timber.log.Timber

class MyFirebaseInstanceIDService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        // Check if message contains a notification payload.
        message.notification?.let {

            val pendingIntent = PendingIntent.getActivity(
                this,
                2345,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            sendNotification(
                this,
                getString(R.string.default_notification_channel_id),
                it.title.orEmpty(),
                it.body.orEmpty(),
                R.drawable.logo_iso,
                destIntent = pendingIntent
            )
            Log.d("TAG", "Message Notification Body: ${it.body}")
        }
    }
}