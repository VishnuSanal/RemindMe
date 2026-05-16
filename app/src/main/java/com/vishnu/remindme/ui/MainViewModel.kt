package com.vishnu.emotiontracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.remindme.alarm.AlarmUtils
import com.vishnu.remindme.db.ReminderRepository
import com.vishnu.remindme.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val repository: ReminderRepository
) : AndroidViewModel(application) {

    private val _reminderEntries = MutableStateFlow<List<Reminder>>(emptyList())
    val reminderEntries: StateFlow<List<Reminder>> = _reminderEntries.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allNotes.collect { notes ->
                _reminderEntries.value = notes
            }
        }
    }

    fun addNewReminder(reminder: Reminder) {
        viewModelScope.launch {
            val _id = insert(reminder)
            reminder._id = _id
            AlarmUtils.scheduleAlarm(context = application.applicationContext, reminder = reminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            AlarmUtils.cancelAlarm(context = application.applicationContext, reminder = reminder)
            AlarmUtils.scheduleAlarm(context = application.applicationContext, reminder = reminder)
            update(reminder)
        }
    }

    /** The most recent delete coroutine, so [restoreReminder] can wait for it before re-inserting. */
    private var lastDeleteJob: Job? = null

    fun deleteReminder(reminder: Reminder) {
        lastDeleteJob = viewModelScope.launch {
            AlarmUtils.cancelAlarm(context = application.applicationContext, reminder = reminder)
            delete(reminder)
        }
    }

    /**
     * Re-inserts a previously deleted reminder (keeping its original id) and re-schedules its alarm.
     * Waits for the in-flight delete to commit first so the insert can't race ahead of it.
     *
     * Scheduling mirrors BootReceiver: a still-future reminder is scheduled as-is, a past-due
     * recurring reminder is advanced to its next future occurrence, and a past-due non-recurring
     * reminder is left unscheduled rather than fired immediately for an already-elapsed time.
     */
    fun restoreReminder(reminder: Reminder) {
        viewModelScope.launch {
            lastDeleteJob?.join()
            reminder._id = insert(reminder)
            val context = application.applicationContext
            when {
                reminder.dueDate >= System.currentTimeMillis() ->
                    AlarmUtils.scheduleAlarm(context = context, reminder = reminder)

                reminder.recurrencePattern != null ->
                    AlarmUtils.rescheduleAlarm(context = context, reminder = reminder)
            }
        }
    }

    private suspend fun insert(reminder: Reminder): Long {
        return repository.insert(reminder)
    }

    private suspend fun update(reminder: Reminder) {
        repository.update(reminder)
    }

    private suspend fun delete(reminder: Reminder) {
        repository.delete(reminder)
    }
}
