package com.vishnu.remindme.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vishnu.remindme.model.RecurrencePatternConverter
import com.vishnu.remindme.model.Reminder

@Database(entities = [Reminder::class], version = 2, exportSchema = false)
@TypeConverters(RecurrencePatternConverter::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDAO(): ReminderDAO

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE reminder_items ADD COLUMN recurrencePattern TEXT DEFAULT NULL")
            }
        }
    }
}