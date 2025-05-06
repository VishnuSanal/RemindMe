package com.vishnu.remindme.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vishnu.remindme.db.ReminderDAO
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderDAO: ReminderDAO

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val reminders = reminderDAO.reminders()

                reminders.forEach {
                    if (it.dueDate >= System.currentTimeMillis())
                        AlarmUtils.scheduleAlarm(context, it)
                    else if (it.recurrencePattern != null)
                        AlarmUtils.rescheduleAlarm(context, it)
                }
            }
        }
    }
}
