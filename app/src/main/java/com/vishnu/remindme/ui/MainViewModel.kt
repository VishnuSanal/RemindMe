package com.vishnu.emotiontracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vishnu.remindme.db.ReminderRepository
import com.vishnu.remindme.model.Reminder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: ReminderRepository) : ViewModel() {

    private val _reminderEntries = MutableStateFlow<List<Reminder>>(emptyList())
    val reminderEntries: StateFlow<List<Reminder>> = _reminderEntries.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allNotes.collect { notes ->
                _reminderEntries.value = notes
            }
        }
    }

    fun insert(reminder: Reminder) {
        viewModelScope.launch {
            repository.insert(reminder)
        }
    }

    fun update(reminder: Reminder) {
        viewModelScope.launch {
            repository.update(reminder)
        }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            repository.delete(reminder)
        }
    }
}
