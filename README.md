# WTFU (Wake The Fugg Up) - Android Alarm App

A minimal, no-nonsense Android alarm application designed to wake you up reliably. The alarm **cannot be dismissed** through the app interface - the only way to stop it immediately is by powering off your device.

## Features

- ✅ **Reliable Wake-up**: Uses `AlarmManager.setExactAndAllowWhileIdle()` to ensure the alarm triggers even in Doze mode
- ✅ **No Snooze, No Dismiss**: Intentionally provides no in-app way to stop the alarm
- ✅ **Customizable Settings**:
  - Set alarm time with hour/minute picker
  - Configure ring duration (1-60 minutes, default: 5)
  - Adjust volume (0-100%, default: 100%)
  - Toggle auto-stop after duration (default: ON)
  - Toggle re-schedule after device reboot
- ✅ **Full-Screen Lock Screen Display**: Alarm shows over lock screen and turns screen on
- ✅ **Loud Alarm Sound**: Plays using `USAGE_ALARM` audio attributes to bypass Do Not Disturb
- ✅ **Foreground Service**: Ensures alarm continues playing reliably
- ✅ **Dark Mode UI**: Clean Material 3 design with dark theme
- ✅ **Battery Optimization Guidance**: Built-in instructions for various OEM devices

## How to Stop the Alarm

There are only two ways to stop an active WTFU alarm:

1. **Power off your device** (hold power button → select "Power off")
2. **Wait for auto-stop** (if enabled, alarm stops after configured duration)

This is intentional - WTFU is designed for people who need a truly insistent alarm.

## Permissions Required

### Core Permissions
- **SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM**: Required to schedule alarms at precise times (Android 12+)
- **WAKE_LOCK**: Keeps CPU running to ensure alarm playback
- **FOREGROUND_SERVICE**: Allows alarm to run as a foreground service
- **FOREGROUND_SERVICE_MEDIA_PLAYBACK**: Specifies the service type for alarm audio

### Optional Permissions
- **POST_NOTIFICATIONS**: Shows alarm notification (Android 13+)
- **RECEIVE_BOOT_COMPLETED**: Re-schedules alarm after device reboot (if enabled)
- **MODIFY_AUDIO_SETTINGS**: Controls alarm volume
- **REQUEST_IGNORE_BATTERY_OPTIMIZATIONS**: Prevents system from killing the app
- **USE_FULL_SCREEN_INTENT**: Shows full-screen alarm over lock screen

## Setup Instructions

### 1. Grant Exact Alarm Permission (Android 12+)

On Android 12 and above, you must grant the "Alarms & reminders" permission:

1. Open WTFU app
2. Tap the info icon (ℹ️) in the top-right
3. Tap "Allow Exact Alarms"
4. Enable the permission in system settings

Alternatively:
- Go to **Settings → Apps → WTFU → Alarms & reminders** and enable it

### 2. Disable Battery Optimization

To ensure WTFU works reliably:

1. Open WTFU app
2. Tap "Disable Battery Optimization" button
3. Select "Allow" in the system dialog

Alternatively:
- Go to **Settings → Apps → WTFU → Battery** and select "Unrestricted"

### 3. OEM-Specific Settings

Some manufacturers have aggressive battery management. Follow these steps for your device:

#### Samsung
1. Settings → Apps → WTFU
2. Battery → Allow background activity: **ON**

#### Xiaomi / POCO / Redmi
1. Settings → Apps → Manage apps → WTFU
2. Battery saver → **No restrictions**
3. Autostart → **Enable**

#### Huawei / Honor
1. Settings → Apps → WTFU
2. Battery → App launch
3. Manage manually → Enable all options (Auto-launch, Secondary launch, Run in background)

#### OnePlus / Realme / Oppo
1. Settings → Battery → Battery optimization
2. Find WTFU → Select "Don't optimize"
3. Settings → Apps → WTFU → Battery → Battery optimization → Don't optimize

#### Google Pixel (Stock Android)
1. Settings → Apps → WTFU
2. Battery → Battery optimization → Not optimized
3. Select "All apps" → Find WTFU → Don't optimize

### 4. Test Your Alarm

Before relying on WTFU for important wake-ups:

1. Set a test alarm for 2-3 minutes from now
2. Lock your device and wait
3. Verify the alarm:
   - Wakes the screen
   - Shows full-screen activity
   - Plays sound at configured volume
   - Cannot be dismissed (except by power-off or waiting for auto-stop)

## Usage

### Setting an Alarm

1. Open WTFU
2. Set your desired wake-up time using the hour/minute fields
3. Configure ring duration (how long until auto-stop)
4. Adjust volume slider
5. Toggle settings:
   - **Auto-stop after duration**: If ON, alarm stops automatically after configured minutes
   - **Re-schedule after reboot**: If ON, alarm is re-scheduled after device restart
6. Tap "Set Alarm"
7. Confirm alarm is set (you'll see a confirmation message)

### Canceling an Alarm

1. Open WTFU
2. Tap "Cancel Alarm" button
3. Alarm will be removed from the schedule

**Note**: You can only cancel an alarm *before* it starts ringing. Once ringing, only power-off or auto-stop will work.

## Technical Details

### Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Storage**: DataStore for alarm preferences
- **Scheduling**: AlarmManager with `setExactAndAllowWhileIdle()`

### Key Components

1. **AlarmScheduler**: Manages scheduling/canceling alarms via AlarmManager
2. **AlarmReceiver**: BroadcastReceiver triggered at alarm time
3. **AlarmService**: Foreground service that manages playback and notification
4. **AlarmPlayer**: MediaPlayer wrapper using `USAGE_ALARM` attributes
5. **AlarmRingingActivity**: Full-screen activity displayed when alarm triggers
6. **BootReceiver**: Re-schedules alarm after device reboot
7. **ShutdownReceiver**: Gracefully stops service on device shutdown

### Alarm Reliability Features

- **WakeLock**: Partial wake lock ensures CPU stays on during alarm startup
- **Foreground Service**: Prevents system from killing the app
- **setExactAndAllowWhileIdle**: Bypasses Doze mode restrictions
- **USAGE_ALARM**: Plays at alarm volume regardless of ringer mode
- **Full-Screen Intent**: Shows over lock screen with screen-on
- **High Priority Notification**: Ensures notification persists

## Building the Project

### Prerequisites

- Android Studio Hedgehog or newer
- JDK 8 or higher
- Android SDK with API 34

### Build Steps

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd WTFU
   ```

2. Open in Android Studio:
   - File → Open → Select WTFU directory

3. Sync Gradle:
   - Android Studio will automatically sync Gradle files
   - Wait for dependencies to download

4. **Important**: Add an alarm sound file:
   - Place a sound file (MP3, OGG, or WAV) at: `app/src/main/res/raw/alarm_sound.mp3`
   - You can use any loud, attention-grabbing sound
   - Recommended: 5-10 seconds of looping-friendly audio
   - You can download free alarm sounds from sites like freesound.org or use your own

5. Build the APK:
   ```bash
   ./gradlew assembleDebug
   ```

6. Install on device:
   ```bash
   ./gradlew installDebug
   ```

## Project Structure

```
WTFU/
├── app/
│   ├── src/main/
│   │   ├── java/com/wtfu/
│   │   │   ├── MainActivity.kt
│   │   │   ├── WtfuApplication.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   ├── MainScreen.kt
│   │   │   │   ├── ReliabilityGuideScreen.kt
│   │   │   │   ├── AlarmRingingActivity.kt
│   │   │   │   └── MainViewModel.kt
│   │   │   ├── data/
│   │   │   │   ├── AlarmPreferences.kt
│   │   │   │   └── AlarmSettings.kt
│   │   │   └── alarm/
│   │   │       ├── AlarmScheduler.kt
│   │   │       ├── AlarmReceiver.kt
│   │   │       ├── BootReceiver.kt
│   │   │       ├── ShutdownReceiver.kt
│   │   │       ├── AlarmService.kt
│   │   │       └── AlarmPlayer.kt
│   │   ├── res/
│   │   │   ├── raw/alarm_sound.mp3 (YOU MUST ADD THIS)
│   │   │   ├── drawable/ic_alarm.xml
│   │   │   ├── values/strings.xml
│   │   │   └── xml/backup_rules.xml
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Known Limitations

1. **Hardware Power Button**: The user can always power off the device using hardware buttons - this is by design
2. **Battery Drain**: Keeping the screen on with an active foreground service will drain battery during alarm
3. **OEM Restrictions**: Some manufacturers may still kill the app despite all optimizations
4. **No Cloud Sync**: Alarm settings are stored locally only
5. **Single Alarm**: Currently supports only one alarm at a time
6. **Alarm Sound Required**: You must provide your own alarm sound file (not included in the repository)

## Troubleshooting

### Alarm doesn't ring on time

1. Verify exact alarm permission is granted (Android 12+)
2. Disable battery optimization for WTFU
3. Check OEM-specific battery settings
4. Enable "Re-schedule after reboot" if device restarts frequently
5. Keep WTFU in recent apps (don't swipe away)

### Alarm doesn't show on lock screen

1. Verify notification permission is granted (Android 13+)
2. Check that "Show on lock screen" is enabled in notification settings
3. Ensure lock screen notifications are not disabled system-wide

### Sound doesn't play

1. Check volume slider in WTFU is not at 0%
2. Verify device alarm volume is not muted
3. Ensure alarm sound file exists at `app/src/main/res/raw/alarm_sound.mp3`
4. Test with a different sound file

### App crashes on alarm trigger

1. Check LogCat for error messages
2. Verify all permissions are granted
3. Ensure alarm sound file is present and valid
4. Try reinstalling the app

## Contributing

This is a demonstration project. For production use, consider:

- Multiple alarm support
- Custom alarm sounds selection
- Recurring alarms (daily, weekdays, weekends)
- Gradual volume increase
- Flashlight/vibration support
- Mathematical puzzle to dismiss (optional)

## License

This project is provided as-is for educational purposes.

## Disclaimer

⚠️ **Important**: This app is designed to be difficult to dismiss. Use responsibly and always ensure you have a backup alarm method for critical wake-ups. The developers are not responsible for missed alarms or any consequences thereof.

## Support

For issues, please check:
1. This README thoroughly
2. Android version compatibility (Min SDK 26)
3. All permissions are granted
4. Battery optimization is disabled
5. Alarm sound file has been added to `app/src/main/res/raw/`

---

**Wake The Fugg Up** - Because sometimes you really need to wake up!
