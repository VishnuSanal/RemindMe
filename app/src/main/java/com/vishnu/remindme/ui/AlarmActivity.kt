package com.vishnu.remindme.ui

import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.ui.theme.RemindMeTheme
import com.vishnu.remindme.utils.Constants
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class AlarmActivity : ComponentActivity() {

    private lateinit var ringtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val reminder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra<Reminder>(
                Constants.REMINDER_ITEM_KEY, Reminder::class.java
            )
        } else {
            intent.getParcelableExtra<Reminder>(Constants.REMINDER_ITEM_KEY)
        }

        if (reminder == null) {
//            reminder = Reminder(0, "Title", null, System.currentTimeMillis()) // debug
            finish()
            return
        }

        var ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        if (ringtoneUri == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        }

        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone.setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.isLooping = true
        }
        ringtone.play()

        setContent {
            RemindMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmScreen(
                        modifier = Modifier.padding(innerPadding),
                        reminder = reminder,
                        onDismiss = {
                            ringtone.stop()
                            finish()
                        },
                        onSnooze = {
                            ringtone.stop()
                            finish()
                        })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::ringtone.isInitialized && ringtone.isPlaying) ringtone.stop()
    }
}

@Composable
fun AlarmScreen(
    reminder: Reminder, onDismiss: () -> Unit, onSnooze: () -> Unit, modifier: Modifier = Modifier
) {
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(reminder.dueDate), ZoneId.systemDefault()
    )
    val formattedTime = dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
    val formattedDate = dateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000)
            seconds++
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AlarmIcon()

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    reminder.description?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Text(
                text = "Alarm active for ${formatSeconds(seconds)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Dismiss", style = MaterialTheme.typography.titleMedium
                    )
                }

//                FilledTonalButton(
//                    onClick = onSnooze,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(56.dp),
//                    colors = ButtonDefaults.filledTonalButtonColors(
//                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
//                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
//                    )
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.ExitToApp,
//                        contentDescription = "Snooze",
//                        modifier = Modifier.padding(end = 8.dp)
//                    )
//                    Text(
//                        text = "Snooze for 5 minutes", style = MaterialTheme.typography.titleMedium
//                    )
//                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AlarmIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "alarmPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.2f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
            ), contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Alarm",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(64.dp)
        )
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}