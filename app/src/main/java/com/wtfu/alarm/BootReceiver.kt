package com.wtfu.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wtfu.data.AlarmPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Receives BOOT_COMPLETED broadcast to re-schedule alarm after device reboot.
 * Only re-schedules if user has enabled "rescheduleOnBoot" option.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Boot completed, checking if alarm needs rescheduling")

            val preferences = AlarmPreferences(context)
            val scheduler = AlarmScheduler(context)

            // Use goAsync to allow coroutine work
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = preferences.alarmSettingsFlow.first()

                    if (settings.rescheduleOnBoot && settings.isActive && settings.timeInMillis > System.currentTimeMillis()) {
                        // Re-schedule the alarm
                        scheduler.scheduleAlarm(settings)
                        Log.d(TAG, "Alarm rescheduled after boot")
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
