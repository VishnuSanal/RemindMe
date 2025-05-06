package com.vishnu.remindme.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.utils.Constants
import java.util.Date

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

        /** reschedules an alarm for the nearest (dueDate + intervalMillis) in the future. only for recurring alarms. */
        fun rescheduleAlarm(context: Context, reminder: Reminder) {

            if (reminder.recurrencePattern == null)
                return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            var dueDate = reminder.dueDate

            while (Date(dueDate).before(Date()))
                dueDate = dueDate + reminder.recurrencePattern.intervalMillis

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                dueDate,
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