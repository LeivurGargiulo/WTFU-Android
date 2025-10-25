# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

WTFU (Wake The Fugg Up) is an Android alarm app designed to be extremely difficult to dismiss. The alarm cannot be stopped through the app interface - only by powering off the device or waiting for auto-stop. Built with Kotlin and Jetpack Compose.

**Key constraint**: The app intentionally provides NO dismiss functionality in the UI. This is core to the app's design philosophy.

## Build Commands

### Prerequisites
- **CRITICAL**: An alarm sound file MUST exist at `app/src/main/res/raw/alarm_sound.mp3` (or .ogg/.wav)
- Android SDK API 34
- JDK 8+

### Common Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore configuration)
./gradlew assembleRelease

# Install debug build on connected device/emulator
./gradlew installDebug

# Run tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean

# Print version
./gradlew printVersion

# Lint check
./gradlew lint
```

## Architecture & Key Components

### Data Flow
1. **UI Layer** (MainScreen) → User configures alarm settings
2. **ViewModel** (MainViewModel) → Manages UI state and coordinates operations
3. **Data Layer** (AlarmPreferences) → Persists settings via DataStore
4. **Scheduling Layer** (AlarmScheduler) → Schedules alarm via AlarmManager
5. **Trigger Flow**: AlarmManager → AlarmReceiver → AlarmService → AlarmRingingActivity + AlarmPlayer

### Critical Components

**AlarmScheduler** (`alarm/AlarmScheduler.kt`)
- Manages scheduling/canceling alarms using `AlarmManager.setExactAndAllowWhileIdle()`
- Uses `RTC_WAKEUP` to wake device from sleep
- Requires `SCHEDULE_EXACT_ALARM` permission on Android 12+

**AlarmReceiver** (`alarm/AlarmReceiver.kt`)
- BroadcastReceiver triggered at alarm time
- Acquires WakeLock to prevent CPU sleep during alarm startup
- Starts AlarmService as foreground service
- Launches AlarmRingingActivity as full-screen intent

**AlarmService** (`alarm/AlarmService.kt`)
- Foreground service with `FOREGROUND_SERVICE_MEDIA_PLAYBACK` type
- Manages alarm playback lifecycle
- Displays persistent notification
- Handles auto-stop timer if enabled
- Stops itself when alarm completes or times out

**AlarmPlayer** (`alarm/AlarmPlayer.kt`)
- MediaPlayer wrapper using `USAGE_ALARM` audio attributes
- Plays sound from `R.raw.alarm_sound`
- Uses `AudioManager.STREAM_ALARM` to bypass Do Not Disturb
- Loops playback until stopped

**AlarmRingingActivity** (`ui/AlarmRingingActivity.kt`)
- Full-screen activity shown over lock screen
- Uses `FLAG_SHOW_WHEN_LOCKED`, `FLAG_TURN_SCREEN_ON`, `FLAG_KEEP_SCREEN_ON`
- Displays alarm time and current time
- **Does NOT provide dismiss button** (by design)

**BootReceiver** (`alarm/BootReceiver.kt`)
- Re-schedules alarm after device reboot (if `rescheduleOnBoot` is enabled)
- Triggered by `BOOT_COMPLETED` broadcast

**ShutdownReceiver** (`alarm/ShutdownReceiver.kt`)
- Gracefully stops AlarmService on device shutdown
- Prevents wake locks from preventing shutdown

**AlarmPreferences** (`data/AlarmPreferences.kt`)
- DataStore-based persistence layer
- Stores: time, duration, volume, auto-stop flag, reschedule flag, active state

**AlarmSettings** (`data/AlarmSettings.kt`)
- Data class representing alarm configuration

### UI Architecture

- **Jetpack Compose** with Material 3 theming
- **Navigation**: Compose Navigation between MainScreen and ReliabilityGuideScreen
- **ViewModel**: MainViewModel manages alarm state and coordinates with AlarmScheduler/AlarmPreferences
- **Dark theme only** (no light mode implementation)

### Permission Requirements

Core permissions required for functionality:
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` (Android 12+)
- `WAKE_LOCK`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `POST_NOTIFICATIONS` (Android 13+)
- `RECEIVE_BOOT_COMPLETED`
- `USE_FULL_SCREEN_INTENT`
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`

## Development Guidelines

### Adding New Alarm Sounds
Place audio files in `app/src/main/res/raw/` and reference via `R.raw.alarm_sound`. Supported formats: MP3, OGG, WAV.

### Testing Alarms
Always test with short intervals (2-3 minutes) first. Lock the device and verify:
- Screen wakes and turns on
- Full-screen activity appears over lock screen
- Sound plays at configured volume
- Cannot be dismissed through UI

### Reliability Considerations
The app uses multiple strategies to ensure alarm reliability:
- `setExactAndAllowWhileIdle()` bypasses Doze mode
- Foreground service prevents system termination
- WakeLock ensures CPU stays active during trigger
- `USAGE_ALARM` audio attributes bypass DND
- Full-screen intent with screen-on flags

### Common Issues
1. **Alarm doesn't trigger**: Check exact alarm permission (Settings → Apps → WTFU → Alarms & reminders)
2. **App killed by system**: Disable battery optimization (Settings → Apps → WTFU → Battery → Unrestricted)
3. **No sound**: Verify `alarm_sound.mp3` exists in `app/src/main/res/raw/`
4. **Build fails**: Ensure alarm sound file is present

### Release Build Configuration
Signing configuration uses `keystore.properties` in project root:
```properties
storeFile=release-keystore.jks
storePassword=<password>
keyAlias=<alias>
keyPassword=<password>
```

Build gracefully falls back to debug signing if keystore is unavailable.

## Code Patterns

### Scheduling an Alarm
```kotlin
val settings = AlarmSettings(
    timeInMillis = targetTime,
    ringDurationMinutes = 5,
    volumePercent = 100,
    autoStopEnabled = true,
    rescheduleOnBoot = false
)
alarmScheduler.scheduleAlarm(settings)
alarmPreferences.saveAlarmSettings(settings.copy(isActive = true))
```

### Canceling an Alarm
```kotlin
alarmScheduler.cancelAlarm()
alarmPreferences.setAlarmActive(false)
```

### Persisting Settings
```kotlin
// Save settings
alarmPreferences.saveAlarmSettings(settings)

// Observe settings
alarmPreferences.alarmSettingsFlow.collect { settings ->
    // React to changes
}
```

## Project Constraints

- **Single alarm only**: No support for multiple alarms
- **No recurring alarms**: Each alarm is one-time only
- **No sound selection**: Uses hardcoded `R.raw.alarm_sound`
- **No dismiss button**: By design, alarm cannot be stopped via UI
- **No snooze**: Intentionally omitted
- **Local storage only**: No cloud sync or backup

## Version Information

- Min SDK: 26 (Android 8.0 Oreo)
- Target SDK: 34 (Android 14)
- Kotlin: 1.9.20
- Compose BOM: 2024.01.00
- Gradle: 8.10
- Android Gradle Plugin: 8.2.0
