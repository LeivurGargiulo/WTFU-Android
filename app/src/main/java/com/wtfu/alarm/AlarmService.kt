package com.wtfu.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wtfu.R
import com.wtfu.data.AlarmPreferences
import com.wtfu.data.AlarmSettings
import com.wtfu.ui.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that manages alarm playback.
 * - Acquires WakeLock to keep CPU on
 * - Displays ongoing notification
 * - Launches full-screen activity
 * - Auto-stops after configured duration (if enabled)
 * - No dismiss actions available
 */
class AlarmService : Service() {

    private var alarmPlayer: AlarmPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val handler = Handler(Looper.getMainLooper())
    private var autoStopRunnable: Runnable? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "AlarmService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "wtfu_alarms"
        private const val CHANNEL_NAME = "WTFU Alarms"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AlarmService created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "AlarmService started")

        val settings = intent?.getParcelableExtra<AlarmSettings>("ALARM_SETTINGS")

        if (settings == null) {
            Log.e(TAG, "AlarmSettings not found in intent, stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        // Acquire WakeLock to keep CPU on
        acquireWakeLock()

        // Start foreground service with notification
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Launch full-screen activity
        launchFullScreenActivity()

        // Load settings and start alarm playback
        serviceScope.launch {
            // Start alarm playback
            val soundUri = RingtoneManager.getActualDefaultRingtoneUri(
                applicationContext,
                RingtoneManager.TYPE_ALARM
            )
            if (soundUri == null) {
                Log.e(TAG, "Could not find default alarm sound.")
                // As a fallback, you could try to use a notification sound or a raw resource if you add one
                // For now, we'''ll just log the error and the alarm will be silent.
            } else {
                val volume = settings.volumePercent
                alarmPlayer = AlarmPlayer(applicationContext, soundUri, volume)
                alarmPlayer?.start()
            }

            // Schedule auto-stop if enabled
            if (settings.autoStopEnabled && settings.ringDurationMinutes > 0) {
                scheduleAutoStop(settings.ringDurationMinutes)
            }

            // Mark alarm as inactive after triggering
            val preferences = AlarmPreferences(applicationContext)
            preferences.setAlarmActive(false)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "AlarmService destroyed")

        // Stop playback
        alarmPlayer?.stop()
        alarmPlayer = null

        // Cancel auto-stop
        autoStopRunnable?.let { handler.removeCallbacks(it) }

        // Release WakeLock
        releaseWakeLock()

        // Cancel coroutine scope
        serviceScope.cancel()
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "WTFU::AlarmWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // Max 10 minutes
            }
            Log.d(TAG, "WakeLock acquired")
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing WakeLock", e)
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for active alarms"
            setSound(null, null) // No sound for notification, only MediaPlayer
            enableVibration(false)
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, AlarmRingingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }


        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full-screen intent for lock screen
        val fullScreenIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WTFU - Alarm Ringing!")
            .setContentText("Power off device to stop")
            .setSmallIcon(R.drawable.ic_alarm)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true) // Cannot be dismissed
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .build()
    }

    private fun launchFullScreenActivity() {
        val intent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun scheduleAutoStop(durationMinutes: Int) {
        autoStopRunnable = Runnable {
            Log.d(TAG, "Auto-stop triggered after $durationMinutes minutes")
            stopSelf()
        }
        handler.postDelayed(autoStopRunnable!!, durationMinutes * 60 * 1000L)
        Log.d(TAG, "Auto-stop scheduled for $durationMinutes minutes")
    }
}
