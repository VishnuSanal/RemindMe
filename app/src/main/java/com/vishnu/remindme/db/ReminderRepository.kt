package com.vishnu.remindme.db

import com.vishnu.remindme.model.Reminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReminderRepository @Inject constructor(private val reminderDAO: ReminderDAO) {

    val allNotes: Flow<List<Reminder>> = reminderDAO.getAllEntries()

    suspend fun insert(reminder: Reminder): Long {
        return reminderDAO.insert(reminder)
    }

    suspend fun update(reminder: Reminder) {
        reminderDAO.update(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        reminderDAO.delete(reminder)
    }
}