package com.wtfu.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wtfu.data.AlarmSettings

/**
 * Handles scheduling and canceling alarms using AlarmManager.
 * Uses setExactAndAllowWhileIdle for reliable triggering even in Doze mode.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        private const val TAG = "AlarmScheduler"
        private const val ALARM_REQUEST_CODE = 1001
    }

    /**
     * Schedule an exact alarm at the specified time.
     * Requires SCHEDULE_EXACT_ALARM permission on Android 12+.
     */
    fun scheduleAlarm(settings: AlarmSettings): Boolean {
        // Check if we can schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e(TAG, "Cannot schedule exact alarms - permission not granted")
                return false
            }
        }

        try {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("ALARM_TIME", settings.timeInMillis)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use setExactAndAllowWhileIdle for reliable triggering
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                settings.timeInMillis,
                pendingIntent
            )

            Log.d(TAG, "Alarm scheduled for ${settings.timeInMillis}")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
            return false
        }
    }

    /**
     * Cancel the currently scheduled alarm.
     */
    fun cancelAlarm() {
        try {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()

            Log.d(TAG, "Alarm canceled")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling alarm", e)
        }
    }

    /**
     * Check if exact alarms can be scheduled (Android 12+).
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}
