package com.vishnu.remindme.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.vishnu.remindme.BuildConfig
import com.vishnu.remindme.R
import com.vishnu.remindme.settings.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    bottomSheetState: SheetState,
    onDismiss: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val context = LocalContext.current
    val settings = remember { SettingsRepository(context) }

    var ringtoneUri by remember { mutableStateOf(settings.alarmRingtoneUri) }
    var vibrate by remember { mutableStateOf(settings.vibrateEnabled) }

    val pickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // A null picked-URI with RESULT_OK means the user chose "Silent".
            val picked: Uri? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    result.data?.getParcelableExtra(
                        RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java
                    )
                else
                    @Suppress("DEPRECATION")
                    result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            val newValue = picked?.toString() ?: ""
            ringtoneUri = newValue
            settings.alarmRingtoneUri = newValue
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
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grouped M3 card, matching the visual language of the About screen.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                // Alarm sound
                ListItem(
                    modifier = Modifier.clickable {
                        pickerLauncher.launch(buildRingtonePickerIntent(context, ringtoneUri))
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Unspecified),
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_alarm),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.alarm_sound),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    supportingContent = {
                        Text(
                            text = ringtoneTitle(ringtoneUri),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Vibrate
                ListItem(
                    modifier = Modifier.clickable {
                        vibrate = !vibrate
                        settings.vibrateEnabled = vibrate
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Unspecified),
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_vibrate),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.vibrate),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = vibrate,
                            onCheckedChange = {
                                vibrate = it
                                settings.vibrateEnabled = it
                            }
                        )
                    }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // About
                ListItem(
                    modifier = Modifier.clickable { onAboutClick() },
                    colors = ListItemDefaults.colors(containerColor = Color.Unspecified),
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_info),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    headlineContent = {
                        Text(
                            text = stringResource(R.string.about),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    supportingContent = {
                        Text(
                            text = BuildConfig.VERSION_NAME,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}

/** Human-readable name of the currently selected alarm sound. */
@Composable
private fun ringtoneTitle(uri: String?): String {
    val context = LocalContext.current
    return when (uri) {
        null -> stringResource(R.string.ringtone_default)
        "" -> stringResource(R.string.ringtone_silent)
        else -> runCatching {
            RingtoneManager.getRingtone(context, Uri.parse(uri))?.getTitle(context)
        }.getOrNull() ?: stringResource(R.string.ringtone_default)
    }
}

private fun buildRingtonePickerIntent(context: Context, currentUri: String?): Intent {
    val existing: Uri? = when (currentUri) {
        // Never set → the default alarm sound is the effective current selection.
        // Passing null here would make the picker preselect "Silent" instead.
        null -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        // Explicitly chosen "Silent".
        "" -> null
        else -> runCatching { currentUri.toUri() }.getOrNull()
    }
    return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
        putExtra(
            RingtoneManager.EXTRA_RINGTONE_TITLE,
            context.getString(R.string.alarm_sound_picker_title)
        )
        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        putExtra(
            RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        )
        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existing)
    }
}
