package com.wtfu.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.wtfu.data.AlarmSettings

/**
 * BroadcastReceiver triggered by AlarmManager when alarm time is reached.
 * Starts the foreground AlarmService to handle playback.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm triggered!")

        val settings = intent.getParcelableExtra<AlarmSettings>("ALARM_SETTINGS")
        if (settings == null) {
            Log.e(TAG, "AlarmSettings not found in intent")
            return
        }

        // Start the foreground service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_SETTINGS", settings)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
