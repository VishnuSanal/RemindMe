package com.vishnu.remindme.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vishnu.remindme.model.Reminder

fun scheduleAlarm(context: Context, reminder: Reminder) {

    Log.e("vishnu", "scheduleAlarm() called with: context = $context, reminder = $reminder")

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("reminderItem", reminder)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        reminder.dueDate,
        pendingIntent
    )
}
