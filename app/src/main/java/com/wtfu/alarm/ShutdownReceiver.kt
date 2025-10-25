package com.wtfu.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receives ACTION_SHUTDOWN broadcast to gracefully stop alarm service
 * when device is powering off.
 */
class ShutdownReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ShutdownReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SHUTDOWN) {
            Log.d(TAG, "Device shutting down, stopping alarm service")
            
            val serviceIntent = Intent(context, AlarmService::class.java)
            context.stopService(serviceIntent)
        }
    }
}
