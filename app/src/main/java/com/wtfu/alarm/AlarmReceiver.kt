package com.wtfu.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

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

        // Start the foreground service
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("ALARM_TIME", intent.getLongExtra("ALARM_TIME", 0L))
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
