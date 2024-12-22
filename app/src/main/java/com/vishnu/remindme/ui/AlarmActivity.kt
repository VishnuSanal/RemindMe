package com.vishnu.remindme.ui

import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishnu.remindme.model.Reminder
import com.vishnu.remindme.ui.theme.RemindMeTheme
import com.vishnu.remindme.utils.Constants


class AlarmActivity : ComponentActivity() {

    private lateinit var ringtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("vishnu", "onCreate() called with: savedInstanceState = $savedInstanceState")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        if (ringtoneUri == null) {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }
        }

        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        ringtone.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        ringtone.isLooping = true
        ringtone.play()

        val reminder =
            intent.getParcelableExtra<Reminder>(Constants.REMINDER_ITEM_KEY, Reminder::class.java)
                ?: Reminder(-1, "Pet the Cat", "Lorem ipsum", 0L)

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
                        }
                    )
                }
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ringtone.isPlaying)
            ringtone.stop()
    }
}

@Composable
fun AlarmScreen(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            AlarmIcon()

            Text(
                text = reminder.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )
            reminder.description?.let {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Dismiss",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

//                Spacer(modifier = Modifier.height(16.dp))

//                OutlinedButton(
//                    onClick = onSnooze,
//                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                ) {
//                    Text(text = "Snooze")
//                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AlarmIcon() {
    // Animation for pulsating effect
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Box(
        modifier = Modifier
            .padding(16.dp)
            .size(120.dp)
            .scale(scale)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Alarm Icon",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(60.dp)
        )
    }
}