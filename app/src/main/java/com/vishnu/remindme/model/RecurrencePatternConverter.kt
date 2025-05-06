package com.vishnu.remindme.model

import androidx.room.TypeConverter

class RecurrencePatternConverter {
    @TypeConverter
    fun fromRecurrencePattern(pattern: RecurrencePattern?): String? {
        return pattern?.name
    }

    @TypeConverter
    fun toRecurrencePattern(value: String?): RecurrencePattern? {
        return value?.let { RecurrencePattern.valueOf(it) }
    }
}