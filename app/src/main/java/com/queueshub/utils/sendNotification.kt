package com.queueshub.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat

fun sendNotification(
    context: Context,
    channel_ID: String,
    title: String,
    msg: String,
    iconRes :Int,
    sound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
    destIntent: PendingIntent,
) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        && notificationManager.getNotificationChannel(channel_ID) == null
    ) {
        createChannel(sound, channel_ID, context)
    }


//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, channel_ID)
        .setSmallIcon(iconRes)
        .setContentTitle(title)
        .setContentText(msg)
        .setAutoCancel(true)
        .setLights(Color.GRAY, 500, 500)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setSound(sound)
        .setContentIntent(destIntent)
//        .setFullScreenIntent(fullScreenPendingIntent, true)
        .build()

    notificationManager.notify(getUniqueId(), notification)
}

private fun createChannel(
    sound: Uri,
    CHANNEL_ID: String,
    context: Context
) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance).apply {
                description = CHANNEL_ID
                lightColor = Color.GRAY
                enableLights(true)
                setShowBadge(true)
                setSound(sound, audioAttributes)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
    }
}

private fun getUniqueId() = ((System.currentTimeMillis() % 10000).toInt())