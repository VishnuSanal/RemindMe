package com.vishnu.remindme.settings

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri

/**
 * App-wide alarm settings, backed by [android.content.SharedPreferences].
 *
 * Reads are deliberately synchronous: the alarm-firing path ([com.vishnu.remindme.ui.AlarmActivity])
 * reads these values on the main thread the moment an alarm goes off, so this must not depend
 * on coroutines/Flow.
 */
class SettingsRepository(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * The chosen alarm sound:
     * - `null` → never set; fall back to the system default alarm sound.
     * - `""`   → Silent; play no sound.
     * - else   → a content URI string for the chosen sound.
     */
    var alarmRingtoneUri: String?
        get() = prefs.getString(KEY_RINGTONE_URI, null)
        set(value) = prefs.edit { putString(KEY_RINGTONE_URI, value) }

    /** Whether the alarm should vibrate while ringing. Defaults to `true`. */
    var vibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(value) = prefs.edit { putBoolean(KEY_VIBRATE, value) }

    /**
     * Resolves the sound to actually play, or `null` when the alarm should be silent.
     * For the not-set case this reproduces the historical fallback chain
     * (alarm → notification → ringtone default).
     */
    fun resolveRingtoneUri(): Uri? {
        return when (val stored = alarmRingtoneUri) {
            null -> defaultAlarmUri()
            "" -> null
            else -> runCatching { stored.toUri() }.getOrNull() ?: defaultAlarmUri()
        }
    }

    private fun defaultAlarmUri(): Uri? =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

    companion object {
        private const val PREFS_NAME = "com_vishnu_remindme_settings"
        private const val KEY_RINGTONE_URI = "alarm_ringtone_uri"
        private const val KEY_VIBRATE = "vibrate_enabled"
    }
}
