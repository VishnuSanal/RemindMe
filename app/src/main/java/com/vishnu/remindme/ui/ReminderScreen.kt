package com.vishnu.remindme.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var dialogReminderItem by remember { mutableStateOf<Reminder?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()

    Column(modifier) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = LocalContext.current.getString(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = {
                    dialogReminderItem = null
                    showBottomSheet = true
                },
                content = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add New",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

        ReminderList(
            onItemClick = {
                dialogReminderItem = it
                showBottomSheet = true
            },
            onItemDelete = { viewModel.deleteReminder(it) }
        )
    }

    if (showBottomSheet) {
        ReminderBottomSheet(
            bottomSheetState = bottomSheetState,
            reminder = dialogReminderItem,
            onDismiss = { showBottomSheet = false },
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

                showBottomSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderBottomSheet(
    bottomSheetState: SheetState,
    onDismiss: () -> Unit,
    onSetAlarm: (String, String?, Long) -> Unit,
    reminder: Reminder?,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf<String?>(null) }

    var dueDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    )

    val timePickerState = rememberTimePickerState(
        initialHour = dueDateTime.hour,
        initialMinute = dueDateTime.minute,
        is24Hour = true
    )

    val validInput by remember {
        derivedStateOf {
            dueDateTime > LocalDateTime.now() && title.isNotBlank()
        }
    }

    LaunchedEffect(reminder) {
        if (reminder != null) {
            title = reminder.title
            description = reminder.description

            dueDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(reminder.dueDate),
                ZoneId.systemDefault()
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (reminder == null) "Add New Reminder" else "Edit Reminder",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                value = description ?: "",
                onValueChange = { description = it },
                label = { Text("Description") },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = "Date: ${dueDateTime.toLocalDate()}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_alarm),
                    contentDescription = "Select Time",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = "Time: ${formatTime(dueDateTime.toLocalTime())}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        onSetAlarm(
                            title.trim(),
                            description?.takeIf { it.isNotBlank() },
                            dueDateTime
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli()
                        )
                    },
                    enabled = validInput
                ) {
                    Text("Save")
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            dueDateTime = LocalDateTime.of(newDate, dueDateTime.toLocalTime())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        dueDateTime = LocalDateTime.of(dueDateTime.toLocalDate(), newTime)
                        showTimePicker = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun formatTime(time: LocalTime): String {
    return String.format("%02d:%02d", time.hour, time.minute)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AnimatedVisibility(visible = !reminder.description.isNullOrBlank()) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = reminder.description ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Due Date",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = Utils.parseMillisToDeviceTimeFormat(
                            LocalContext.current,
                            reminder.dueDate
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            IconButton(
                onClick = { onDelete() }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}