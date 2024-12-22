package com.vishnu.remindme.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.utils.Constants

class AlarmUtils {
    companion object {
        fun scheduleAlarm(context: Context, reminder: Reminder) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.dueDate,
                getPendingIntent(
                    context,
                    reminder
                )
            )
        }

        fun cancelAlarm(context: Context, reminder: Reminder) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(
                getPendingIntent(
                    context,
                    reminder
                )
            )
        }

        private fun getPendingIntent(context: Context, reminder: Reminder): PendingIntent {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(Constants.REMINDER_ITEM_KEY, reminder)
            }

            return PendingIntent.getBroadcast(
                context,
                reminder._id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}