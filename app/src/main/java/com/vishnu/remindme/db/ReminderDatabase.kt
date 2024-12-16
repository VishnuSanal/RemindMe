package com.vishnu.remindme.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vishnu.remindme.model.Reminder

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDAO(): ReminderDAO
}