package com.vishnu.remindme.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.remindme.alarm.AlarmUtils
import com.vishnu.remindme.db.ReminderRepository
import com.vishnu.remindme.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    // Undo functionality
    private val _pendingDeletion = MutableStateFlow<Reminder?>(null)
    val pendingDeletion: StateFlow<Reminder?> = _pendingDeletion.asStateFlow()
    
    private var deletionJob: Job? = null
    private val UNDO_TIMEOUT_MS = 5000L // 5 seconds

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

    fun deleteReminder(reminder: Reminder) {
        // Cancel any ongoing deletion
        deletionJob?.cancel()
        
        // Set pending deletion
        _pendingDeletion.value = reminder
        
        // Cancel alarm immediately for better user experience
        viewModelScope.launch {
            AlarmUtils.cancelAlarm(context = application.applicationContext, reminder = reminder)
        }
        
        // Start countdown for permanent deletion
        deletionJob = viewModelScope.launch {
            delay(UNDO_TIMEOUT_MS)
            // Perform actual deletion after timeout
            delete(reminder)
            _pendingDeletion.value = null
        }
    }
    
    fun undoDelete() {
        deletionJob?.cancel()
        val reminderToRestore = _pendingDeletion.value
        if (reminderToRestore != null) {
            // Reschedule the alarm
            viewModelScope.launch {
                AlarmUtils.scheduleAlarm(context = application.applicationContext, reminder = reminderToRestore)
            }
            _pendingDeletion.value = null
        }
    }
    
    fun confirmDelete() {
        val reminderToDelete = _pendingDeletion.value
        if (reminderToDelete != null) {
            deletionJob?.cancel()
            viewModelScope.launch {
                delete(reminderToDelete)
                _pendingDeletion.value = null
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
