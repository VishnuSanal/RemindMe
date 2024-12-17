package com.vishnu.remindme.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vishnu.remindme.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminder_items ORDER BY dueDate DESC")
    fun getAllEntries(): Flow<List<Reminder>>
}
