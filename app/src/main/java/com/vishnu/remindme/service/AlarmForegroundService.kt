package com.vishnu.remindme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.vishnu.remindme.alarm.AlarmUtils
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.ui.AlarmActivity
import com.vishnu.remindme.utils.Constants

class AlarmForegroundService : LifecycleService() {

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "com.vishnu.remindme.alarm_service_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val reminder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                intent.getParcelableExtra<Reminder>(
                    Constants.REMINDER_ITEM_KEY,
                    Reminder::class.java
                )
            else
                intent.getParcelableExtra<Reminder>(Constants.REMINDER_ITEM_KEY)

        if (reminder!!.recurrencePattern != null)
            AlarmUtils.rescheduleAlarm(this, reminder)

        if (Settings.canDrawOverlays(this)) {
            showOverlay(this) {
                val activityIntent = Intent(this, AlarmActivity::class.java)
                activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                activityIntent.putExtra(Constants.REMINDER_ITEM_KEY, reminder)
                startActivity(activityIntent)
            }
        }

        showNotification(reminder)
        stopSelf()

        return START_NOT_STICKY
    }

    /**
     * Displays a transparent overlay using the WindowManager.
     * Calls [onOverlayShown] once the overlay is displayed.
     */
    private fun showOverlay(context: Context, onOverlayShown: () -> Unit) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val overlayView = View(context).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, params)

        onOverlayShown()

        Handler(Looper.getMainLooper()).postDelayed({
            windowManager.removeView(overlayView)
        }, 500)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "RemindMe Reminder",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for RemindMe Reminder triggers"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification(reminder: Reminder) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("RemindMe Reminder")
            .setContentText(reminder.title)
            .setSubText(reminder.description)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }
}
