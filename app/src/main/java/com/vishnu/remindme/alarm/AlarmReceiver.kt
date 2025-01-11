package com.vishnu.remindme.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.service.AlarmForegroundService
import com.vishnu.remindme.utils.Constants

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                intent.getParcelableExtra<Reminder>(
                    Constants.REMINDER_ITEM_KEY,
                    Reminder::class.java
                )
            else
                intent.getParcelableExtra<Reminder>(Constants.REMINDER_ITEM_KEY)

        val alarmIntent = Intent(context, AlarmForegroundService::class.java)
        alarmIntent.putExtra(Constants.REMINDER_ITEM_KEY, reminder)

        context.startForegroundService(alarmIntent)
    }
}
