package com.vishnu.remindme.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.emotiontracker.ui.MainViewModel
import com.vishnu.remindme.R
import com.vishnu.remindme.model.RecurrencePattern
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
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var dialogReminderItem by remember { mutableStateOf<Reminder?>(null) }
    val bottomSheetState = rememberModalBottomSheetState()
    val reminderEntries by viewModel.reminderEntries.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = context.getString(R.string.app_name),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogReminderItem = null
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Reminder",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { paddingValues ->
        if (reminderEntries.isEmpty()) {
            EmptyRemindersView(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Text(
                        text = "Your Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                items(reminderEntries) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onClick = {
                            dialogReminderItem = reminder
                            showBottomSheet = true
                        },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                }
            }
        }
    }

    if (showBottomSheet) {
        ReminderBottomSheet(
            bottomSheetState = bottomSheetState,
            reminder = dialogReminderItem,
            onDismiss = { showBottomSheet = false },
            onSetAlarm = { title, description, dueDate, recurrencePattern ->
                val reminder = Reminder(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    recurrencePattern = recurrencePattern
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

@Composable
fun EmptyRemindersView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Reminders Yet",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap the + button to create your first reminder",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderBottomSheet(
    bottomSheetState: SheetState,
    onDismiss: () -> Unit,
    onSetAlarm: (String, String?, Long, RecurrencePattern?) -> Unit,
    reminder: Reminder?,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf<String?>(null) }

    var dueDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Add recurrence pattern state
    var recurrencePattern by remember { mutableStateOf<RecurrencePattern?>(null) }
    var showRecurrenceMenu by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDateTime.toInstant(ZoneOffset.UTC).toEpochMilli(),
    )

    val timePickerState = rememberTimePickerState(
        initialHour = dueDateTime.hour,
        initialMinute = dueDateTime.minute
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
            recurrencePattern = reminder.recurrencePattern

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
                    text = "Date: ${
                        Utils.formatDate(
                            LocalContext.current,
                            dueDateTime.toLocalDate()
                        )
                    }",
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
                    text = "Time: ${
                        Utils.formatTime(
                            LocalContext.current,
                            dueDateTime.toLocalTime()
                        )
                    }",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Recurrence pattern row
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRecurrenceMenu = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = "Set Recurrence",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    Text(
                        text = "Repeat: ${recurrencePattern?.displayName ?: "Never"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                DropdownMenu(
                    expanded = showRecurrenceMenu,
                    onDismissRequest = { showRecurrenceMenu = false },
                    modifier = Modifier.width(240.dp)
                ) {
                    RecurrencePattern.entries.forEach { pattern ->
                        DropdownMenuItem(
                            text = { Text(pattern.displayName) },
                            onClick = {
                                recurrencePattern =
                                    if (pattern == RecurrencePattern.NONE) null else pattern
                                showRecurrenceMenu = false
                            }
                        )
                    }
                }
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
                                .toEpochMilli(),
                            recurrencePattern
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
                            val newDate = Instant
                                .ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                                .atStartOfDay()
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
fun ReminderCard(
    reminder: Reminder,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dueDateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(reminder.dueDate),
        ZoneId.systemDefault()
    )
    val isOverdue = dueDateTime.isBefore(LocalDateTime.now())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        isOverdue -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!reminder.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    color = when {
                        isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = "Due Date",
                            tint = when {
                                isOverdue -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = Utils.parseMillisToDeviceTimeFormat(context, reminder.dueDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    if (isOverdue) {
                        Text(
                            text = "Overdue",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (isOverdue) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Past due",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}