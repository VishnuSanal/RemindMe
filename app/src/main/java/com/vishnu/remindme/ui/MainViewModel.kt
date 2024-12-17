package com.vishnu.emotiontracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.remindme.alarm.cancelAlarm
import com.vishnu.remindme.alarm.scheduleAlarm
import com.vishnu.remindme.db.ReminderRepository
import com.vishnu.remindme.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
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
            scheduleAlarm(context = application.applicationContext, reminder = reminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            scheduleAlarm(context = application.applicationContext, reminder = reminder)
            update(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            cancelAlarm(context = application.applicationContext, reminder = reminder)
            delete(reminder)
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
