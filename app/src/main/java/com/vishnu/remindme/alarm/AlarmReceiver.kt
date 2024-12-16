package com.vishnu.remindme.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.ui.AlarmActivity


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.e("vishnu", "onReceive() called with: context = $context, intent = $intent")

        val reminder = intent.getParcelableExtra<Reminder>("reminderItem", Reminder::class.java)

        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("reminderItem", reminder)
        }

        context.startActivity(alarmIntent)
    }
}
