package com.vishnu.remindme.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "reminder_items")
@Parcelize
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    var _id: Long = 0,
    val title: String,
    val description: String?,
    val dueDate: Long,
    val recurrencePattern: RecurrencePattern? = null
) : Parcelable

