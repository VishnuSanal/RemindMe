package com.vishnu.remindme.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vishnu.emotiontracker.ui.MainViewModel
import com.vishnu.remindme.R
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.ui.theme.RemindMeTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemindMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: MainViewModel = hiltViewModel(),
) {
    var showDialog by remember { mutableStateOf(false) }

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

        ReminderList()
    }

    if (showDialog) {
        AlarmDialog(
            onDismiss = { showDialog = false },
            onSetAlarm = { title, description, dueDate ->
                showDialog = false

                viewModel.insert(
                    Reminder(
                        title = title,
                        description = description,
                        dueDate = dueDate,
                    )
                )
            }
        )
    }
}

@Composable
fun AlarmDialog(
    onDismiss: () -> Unit,
    onSetAlarm: (String, String?, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }

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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                DatePickerField(selectedDate, onDateChange = { selectedDate = it })
                TimePickerField(selectedTime, onTimeChange = { selectedTime = it })
            }
        },
        confirmButton = @Composable {
            TextButton(onClick = {

                val dueDate: Long = LocalDateTime.of(selectedDate, selectedTime).atZone(
                    ZoneId.systemDefault()
                ).toInstant().toEpochMilli()

                onSetAlarm(title, null, dueDate)
            }) {
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
