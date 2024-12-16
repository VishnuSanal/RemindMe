package com.vishnu.remindme.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

fun showNotification(context: Context, title: String, message: String) {
    val channelId = context.packageName + "reminder_channel"

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId, "Alarm Notifications", NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Build the notification
    val notification = NotificationCompat.Builder(context, channelId).setContentTitle(title)
        .setContentText(message).setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_HIGH).build()

    // Show the notification
    notificationManager.notify(2000, notification)
}
