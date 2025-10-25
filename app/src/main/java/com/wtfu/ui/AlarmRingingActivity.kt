package com.wtfu.ui

import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wtfu.data.AlarmPreferences
import com.wtfu.ui.theme.WTFUTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-screen activity displayed when alarm is ringing.
 * Shows over lock screen and turns screen on.
 * NO dismiss button - only way to stop is device power off or auto-stop timeout.
 */
class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set flags to show over lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContent {
            WTFUTheme {
                AlarmRingingScreen()
            }
        }
    }

    /**
     * Block volume key events to prevent user from lowering alarm volume.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP,
            KeyEvent.KEYCODE_VOLUME_MUTE -> {
                // Block volume controls during alarm
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}

@Composable
fun AlarmRingingScreen() {
    val context = LocalContext.current
    val preferences = remember { AlarmPreferences(context) }
    var autoStopEnabled by remember { mutableStateOf(true) }
    var ringDurationMinutes by remember { mutableStateOf(5) }
    var currentTime by remember { mutableStateOf("") }

    // Load settings
    LaunchedEffect(Unit) {
        val settings = preferences.alarmSettingsFlow.first()
        autoStopEnabled = settings.autoStopEnabled
        ringDurationMinutes = settings.ringDurationMinutes
    }

    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            currentTime = formatter.format(Date())
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Current Time
            Text(
                text = currentTime,
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // WTFU Title
            Text(
                text = "WTFU",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.error,
                letterSpacing = 8.sp
            )

            // Alarm Ringing Message
            Text(
                text = "🚨 ALARM RINGING! 🚨",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Instructions
            Text(
                text = if (autoStopEnabled) {
                    "⚠️ To stop immediately:\nPower off your device\n\nOr wait $ringDurationMinutes minutes for auto-stop"
                } else {
                    "⚠️ To stop this alarm:\nPower off your device\n\n(Auto-stop is disabled)"
                },
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 28.sp
            )
        }
    }
}
