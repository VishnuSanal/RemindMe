package com.vishnu.remindme.model

import androidx.annotation.StringRes
import com.vishnu.remindme.R
import java.util.concurrent.TimeUnit

/** Time units offered when building a [RecurrencePattern.CUSTOM] interval. */
enum class RecurrenceUnit(@StringRes val displayNameRes: Int, val millis: Long) {
    MINUTES(R.string.unit_minutes, TimeUnit.MINUTES.toMillis(1)),
    HOURS(R.string.unit_hours, TimeUnit.HOURS.toMillis(1)),
    DAYS(R.string.unit_days, TimeUnit.DAYS.toMillis(1));
}
