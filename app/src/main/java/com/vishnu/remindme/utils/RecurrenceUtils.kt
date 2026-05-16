package com.vishnu.remindme.utils

import android.content.Context
import com.vishnu.remindme.R
import com.vishnu.remindme.model.RecurrencePattern
import com.vishnu.remindme.model.RecurrenceUnit
import com.vishnu.remindme.model.Reminder

/** Helpers for resolving and presenting a reminder's recurrence interval. */
object RecurrenceUtils {

    /**
     * The effective repeat interval for [reminder] in milliseconds, or null when the reminder
     * does not recur (or carries a [RecurrencePattern.CUSTOM] pattern with no valid interval).
     */
    fun resolveIntervalMillis(reminder: Reminder): Long? = when (reminder.recurrencePattern) {
        null, RecurrencePattern.NONE -> null
        RecurrencePattern.CUSTOM -> reminder.recurrenceIntervalMillis?.takeIf { it > 0 }
        else -> reminder.recurrencePattern.intervalMillis
    }

    /** Splits an interval into a whole count of the largest [RecurrenceUnit] that divides it evenly. */
    fun splitInterval(millis: Long): Pair<Long, RecurrenceUnit> {
        for (unit in listOf(RecurrenceUnit.DAYS, RecurrenceUnit.HOURS, RecurrenceUnit.MINUTES))
            if (millis % unit.millis == 0L)
                return (millis / unit.millis) to unit
        return (millis / RecurrenceUnit.MINUTES.millis).coerceAtLeast(1) to RecurrenceUnit.MINUTES
    }

    /** Human-readable label for a custom interval, e.g. "Every 50 minutes". */
    fun formatInterval(context: Context, millis: Long?): String {
        if (millis == null || millis <= 0) return context.getString(R.string.recurrence_custom)
        val (count, unit) = splitInterval(millis)
        val plural = when (unit) {
            RecurrenceUnit.MINUTES -> R.plurals.recurrence_every_minutes
            RecurrenceUnit.HOURS -> R.plurals.recurrence_every_hours
            RecurrenceUnit.DAYS -> R.plurals.recurrence_every_days
        }
        return context.resources.getQuantityString(plural, count.toInt(), count.toInt())
    }

    /** Human-readable label for any reminder's recurrence (preset name or custom interval). */
    fun formatRecurrence(context: Context, reminder: Reminder): String =
        when (reminder.recurrencePattern) {
            null, RecurrencePattern.NONE -> context.getString(R.string.recurrence_never)
            RecurrencePattern.CUSTOM -> formatInterval(context, reminder.recurrenceIntervalMillis)
            else -> context.getString(reminder.recurrencePattern.displayNameRes)
        }
}
