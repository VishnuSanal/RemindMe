package com.vishnu.remindme.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.ui.AlarmActivity
import com.vishnu.remindme.utils.Constants


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminder =
            intent.getParcelableExtra<Reminder>(Constants.REMINDER_ITEM_KEY, Reminder::class.java)

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constants.REMINDER_ITEM_KEY, reminder)
        }

        context.startActivity(alarmIntent)
    }
}
