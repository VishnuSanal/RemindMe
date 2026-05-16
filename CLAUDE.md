# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Remind Me! is an Android app that sets **exact alarms** (not soft reminders) for a specific date/time in the future, with optional recurrence. It is GPLv3, ad-free, and distributed on F-Droid, IzzyOnDroid, and Google Play.

## Build & test

```sh
./gradlew assembleDebug                # build debug APK
./gradlew installDebug                 # build + install on connected device/emulator
./gradlew test                         # JVM unit tests (app/src/test)
./gradlew connectedAndroidTest         # instrumented tests (needs device/emulator)
./gradlew lint                         # Android lint
./gradlew assembleRelease              # minified + resource-shrunk release build
```

Run a single instrumented test class:
```sh
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.vishnu.remindme.ExampleInstrumentedTest
```

- `compileSdk`/`targetSdk` 35, `minSdk` 26, Java 11.
- Dependencies are managed through the version catalog at `gradle/libs.versions.toml` — add/update libraries there, not in `app/build.gradle.kts` directly.
- The debug build uses applicationId suffix `.debug`, so it installs alongside a release build.

## Architecture

Single-module (`:app`) Jetpack Compose app, package `com.vishnu.remindme`. Hilt for DI, Room for persistence.

**Alarm flow (the core of the app):**
1. `MainViewModel.addNewReminder()` inserts a `Reminder` into Room, then calls `AlarmUtils.scheduleAlarm()`.
2. `AlarmUtils` registers an exact alarm via `AlarmManager.setExactAndAllowWhileIdle(RTC_WAKEUP, ...)` with a `PendingIntent` targeting `AlarmReceiver`.
3. On fire, `AlarmReceiver` (a `BroadcastReceiver`) starts `AlarmForegroundService`.
4. `AlarmForegroundService` posts the notification, draws a `SYSTEM_ALERT_WINDOW` overlay, and launches `AlarmActivity` (the full-screen ringing UI). For recurring reminders it calls `AlarmUtils.rescheduleAlarm()` to set the next occurrence.
5. `BootReceiver` re-schedules all future/recurring alarms after `BOOT_COMPLETED`, since `AlarmManager` does not survive reboot.

**Recurrence:** `RecurrencePattern` is a fixed enum (`NONE`/`DAILY`/`WEEKLY`/`BIWEEKLY`/`MONTHLY`/`YEARLY`) carrying a literal `intervalMillis`. `rescheduleAlarm()` advances `dueDate` by `intervalMillis` repeatedly until it lands in the future — so MONTHLY/YEARLY are approximations (30/365 days), not calendar-aware.

**Persistence:** Room database `ReminderDatabase` (entity `Reminder` in table `reminder_items`). `RecurrencePattern` is stored via `RecurrencePatternConverter`. Schema is at **version 2** — any entity change needs a new `Migration` in `ReminderDatabase` alongside the existing `MIGRATION_1_2` (`exportSchema = false`).

**DI:** `RemindMe` is the `@HiltAndroidApp` Application. `hilt/AppModule.kt` provides the database/DAO/repository singletons. `BootReceiver` is `@AndroidEntryPoint` with field injection. `MainViewModel` is `@HiltViewModel`.

**UI:** `MainActivity` hosts Compose content; `ReminderScreen.kt` is the main screen; `AlarmActivity` is the standalone ringing screen (`singleInstancePerTask`, empty `taskAffinity`). State flows from `MainViewModel.reminderEntries` (a `StateFlow` backed by the repository's Room `Flow`).

**Cross-process key:** Intents pass the `Reminder` as a `Parcelable` extra under `Constants.REMINDER_ITEM_KEY`.

## Gotchas

- `MainViewModel.kt` declares package `com.vishnu.emotiontracker.ui` (leftover from a template) while every other file is `com.vishnu.remindme`. Leave it unless intentionally renaming — it compiles, but don't copy the pattern.
- The app depends on the `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM` and `SYSTEM_ALERT_WINDOW` permissions; exact-alarm behavior is central and must not be downgraded to inexact alarms.
- Release builds run R8/minification + resource shrinking — verify ProGuard rules in `app/proguard-rules.pro` when adding reflection-based libraries.
