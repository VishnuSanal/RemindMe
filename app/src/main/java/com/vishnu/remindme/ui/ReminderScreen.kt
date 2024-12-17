package com.vishnu.remindme.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.emotiontracker.ui.MainViewModel
import com.vishnu.remindme.R
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.utils.Utils
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId


@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    var showDialog by remember { mutableStateOf(false) }
    var dialogReminderItem by remember { mutableStateOf<Reminder?>(null) }

    Column(modifier) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = LocalContext.current.getString(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    showDialog = true
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            )
        }

        ReminderList(
            onItemClick = {
                dialogReminderItem = it
                showDialog = true
            },
            onItemDelete = { viewModel.deleteReminder(it) }
        )
    }

    if (showDialog) {
        AlarmDialog(
            reminder = dialogReminderItem,
            onDismiss = { showDialog = false },
            onSetAlarm = { title, description, dueDate ->

                val reminder = Reminder(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                )

                if (dialogReminderItem == null)
                    viewModel.addNewReminder(reminder = reminder)
                else {
                    reminder._id = dialogReminderItem!!._id
                    viewModel.updateReminder(reminder)
                }

                showDialog = false
            }
        )
    }
}

@Composable
fun AlarmDialog(
    onDismiss: () -> Unit,
    onSetAlarm: (String, String?, Long) -> Unit,
    reminder: Reminder?,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

    var dueDate by remember { mutableStateOf(LocalDateTime.now()) }
    var validInput by remember { mutableStateOf(false) }

    LaunchedEffect(null) {
        if (reminder != null) {
            title = reminder.title
            description = reminder.description

            dueDate = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(reminder.dueDate),
                ZoneId.systemDefault()
            );
            selectedDate = dueDate.toLocalDate()
            selectedTime = dueDate.toLocalTime()
        }
    }

    validInput = dueDate > LocalDateTime.now() && title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Alarm")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    value = description ?: "",
                    onValueChange = { description = it },
                    label = { Text("Description") },
                )
                DatePickerField(selectedDate, onDateChange = {
                    selectedDate = it
                    dueDate = LocalDateTime.of(selectedDate, selectedTime)
                })
                TimePickerField(selectedTime, onTimeChange = {
                    selectedTime = it
                    dueDate = LocalDateTime.of(selectedDate, selectedTime)
                })
            }
        },
        confirmButton = @Composable {
            TextButton(
                onClick = {
                    onSetAlarm(
                        title.trim(),
                        description,
                        dueDate
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    )
                },
                enabled = validInput
            ) {
                Text("Set Alarm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DatePickerField(selectedDate: LocalDate, onDateChange: (LocalDate) -> Unit) {
    val context = LocalContext.current
    val datePicker = DatePickerDialog(context, { _, year, month, dayOfMonth ->
        onDateChange(LocalDate.of(year, month + 1, dayOfMonth))
    }, selectedDate.year, selectedDate.monthValue - 1, selectedDate.dayOfMonth)

    OutlinedButton(onClick = { datePicker.show() }, modifier = Modifier.fillMaxWidth()) {
        Text("Select Date: $selectedDate")
    }
}

@Composable
fun TimePickerField(selectedTime: LocalTime, onTimeChange: (LocalTime) -> Unit) {
    val context = LocalContext.current
    val timePicker = TimePickerDialog(context, { _, hourOfDay, minute ->
        onTimeChange(LocalTime.of(hourOfDay, minute))
    }, selectedTime.hour, selectedTime.minute, true)

    OutlinedButton(onClick = { timePicker.show() }, modifier = Modifier.fillMaxWidth()) {
        Text("Select Time: $selectedTime")
    }
}


@Composable
fun ReminderList(
    modifier: Modifier = Modifier,
    onItemClick: (reminder: Reminder) -> Unit,
    onItemDelete: (reminder: Reminder) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val reminderEntries by viewModel.reminderEntries.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(reminderEntries) {
            ReminderCard(
                reminder = it,
                onClick = { onItemClick(it) },
                onDelete = { onItemDelete(it) },
            )
        }
    }
}


@Composable
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column() {
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                reminder.description?.let {
                    Text(
                        modifier = Modifier
                            .padding(4.dp),
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Due Date",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = Utils.parseMillisToDeviceTimeFormat(
                            LocalContext.current,
                            reminder.dueDate
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        onDelete()
                    },
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}