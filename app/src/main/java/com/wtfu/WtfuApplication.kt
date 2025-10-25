package com.wtfu

import android.app.Application
import android.util.Log

/**
 * Application class for WTFU.
 */
class WtfuApplication : Application() {
    
    companion object {
        private const val TAG = "WtfuApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "WTFU Application created")
    }
}
