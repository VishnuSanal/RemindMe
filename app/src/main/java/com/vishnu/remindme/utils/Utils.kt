package com.vishnu.remindme.utils

import android.content.Context
import android.text.format.DateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class Utils {
    companion object {
        fun parseMillisToDeviceTimeFormat(context: Context, millis: Long): String {
            val is24HourFormat = DateFormat.is24HourFormat(context)
            val pattern = if (is24HourFormat) "HH:mm, dd MMM yyyy" else "hh:mm a, dd MMM yyyy"
            val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
                .withZone(ZoneId.systemDefault())
            return formatter.format(Instant.ofEpochMilli(millis))
        }
    }
}