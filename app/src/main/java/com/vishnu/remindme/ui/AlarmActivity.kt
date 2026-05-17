package com.vishnu.remindme.ui

import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vishnu.remindme.R
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.settings.SettingsRepository
import com.vishnu.remindme.ui.theme.RemindMeTheme
import com.vishnu.remindme.utils.Constants
import com.vishnu.remindme.utils.Utils
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

class AlarmActivity : ComponentActivity() {

    private lateinit var ringtone: Ringtone
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

//        val reminder = Reminder(0, "Title", "Description", System.currentTimeMillis()) // debug
        val reminder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra<Reminder>(
                Constants.REMINDER_ITEM_KEY, Reminder::class.java
            )
        } else {
            intent.getParcelableExtra<Reminder>(Constants.REMINDER_ITEM_KEY)
        }

        if (reminder == null) {
            finish()
            return
        }

        val settings = SettingsRepository(applicationContext)

        // A null URI means the user picked "Silent" — play no sound at all.
        val ringtoneUri = settings.resolveRingtoneUri()
        if (ringtoneUri != null) {
            // getRingtone() returns null if the stored URI is no longer playable
            // (e.g. a custom sound that was since deleted); fall back to the default
            // alarm sound, and skip sound entirely if even that is unavailable.
            val resolved = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?.let { RingtoneManager.getRingtone(applicationContext, it) }
            if (resolved != null) {
                ringtone = resolved
                ringtone.setAudioAttributes(
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ringtone.isLooping = true
                }
                ringtone.play()
            }
        }

        if (settings.vibrateEnabled) {
            startVibration()
        }

        setContent {
            RemindMeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AlarmScreen(
                        modifier = Modifier.padding(innerPadding),
                        reminder = reminder,
                        onDismiss = {
                            stopAlarm()
                            finish()
                        },
                        onSnooze = {
                            stopAlarm()
                            finish()
                        })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }

    private fun startVibration() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        if (!vib.hasVibrator()) return
        vibrator = vib

        // Repeating wait/buzz/wait pattern, looping from index 0.
        val effect = VibrationEffect.createWaveform(longArrayOf(0, 800, 600), 0)

        // Tag the vibration as an alarm so it isn't suppressed by the device's
        // ringer mode (a plain vibrate() is treated like a notification buzz).
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        @Suppress("DEPRECATION")
        vib.vibrate(effect, attributes)
    }

    private fun stopAlarm() {
        if (::ringtone.isInitialized && ringtone.isPlaying) ringtone.stop()
        vibrator?.cancel()
    }
}

@Composable
fun AlarmScreen(
    reminder: Reminder, onDismiss: () -> Unit, onSnooze: () -> Unit, modifier: Modifier = Modifier
) {
    val dateTime = LocalDateTime
        .ofInstant(
            Instant.ofEpochMilli(reminder.dueDate),
            ZoneId.systemDefault()
        )
    val formattedTime = Utils.formatTime(LocalContext.current, dateTime.toLocalTime())
    val formattedDate = Utils.formatDate(LocalContext.current, dateTime.toLocalDate())

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
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
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.alarm_active_for, formatSeconds(seconds)),
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
                        text = stringResource(R.string.dismiss),
                        style = MaterialTheme.typography.titleMedium
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
            contentDescription = stringResource(R.string.alarm),
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