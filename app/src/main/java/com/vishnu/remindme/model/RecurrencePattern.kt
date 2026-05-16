package com.vishnu.remindme.model

import androidx.annotation.StringRes
import com.vishnu.remindme.R
import java.util.concurrent.TimeUnit

enum class RecurrencePattern(@StringRes val displayNameRes: Int, val intervalMillis: Long) {
    NONE(R.string.recurrence_never, 0),
    DAILY(R.string.recurrence_daily, TimeUnit.DAYS.toMillis(1)),
    WEEKLY(R.string.recurrence_weekly, TimeUnit.DAYS.toMillis(7)),
    BIWEEKLY(R.string.recurrence_biweekly, TimeUnit.DAYS.toMillis(14)),
    MONTHLY(R.string.recurrence_monthly, TimeUnit.DAYS.toMillis(30)),
    YEARLY(R.string.recurrence_yearly, TimeUnit.DAYS.toMillis(365)),

    /** User-defined interval; the actual value is stored in [com.vishnu.remindme.model.Reminder.recurrenceIntervalMillis]. */
    CUSTOM(R.string.recurrence_custom, 0);
}
