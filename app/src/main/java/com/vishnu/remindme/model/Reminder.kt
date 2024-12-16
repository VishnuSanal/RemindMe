package com.vishnu.remindme.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_items")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val _id: Int = 0,
    val title: String,
    val description: String?,
    val dueDate: Long,
)