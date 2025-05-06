package com.vishnu.remindme.model

import java.util.concurrent.TimeUnit

enum class RecurrencePattern(val displayName: String, val intervalMillis: Long) {
    NONE("Never", 0),
    DAILY("Daily", TimeUnit.DAYS.toMillis(1)),
    WEEKLY("Weekly", TimeUnit.DAYS.toMillis(7)),
    BIWEEKLY("Every 2 Weeks", TimeUnit.DAYS.toMillis(14)),
    MONTHLY("Monthly", TimeUnit.DAYS.toMillis(30)),
    YEARLY("Yearly", TimeUnit.DAYS.toMillis(365));
}

