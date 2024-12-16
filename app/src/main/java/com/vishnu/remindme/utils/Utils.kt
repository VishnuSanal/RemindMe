package com.vishnu.remindme.utils

import android.text.format.DateUtils

class Utils {
    companion object {
        fun getFormattedDateFromTimestamp(timestampInMilliSeconds: Long): String {
//            return SimpleDateFormat("HH:mm dd MMM").format(Date(timestampInMilliSeconds))
            return DateUtils.getRelativeTimeSpanString(timestampInMilliSeconds).toString()
        }
    }
}