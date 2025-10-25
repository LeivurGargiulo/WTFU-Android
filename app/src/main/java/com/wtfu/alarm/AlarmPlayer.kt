package com.wtfu.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wrapper for MediaPlayer to handle alarm audio playback.
 * Uses USAGE_ALARM to ensure playback regardless of device ringer mode.
 * Also locks the volume to the configured level.
 */
class AlarmPlayer(
    private val context: Context,
    private val soundResId: Int,
    private val volumePercent: Int
) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalAlarmVolume: Int = -1

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var volumeJob: Job? = null

    companion object {
        private const val TAG = "AlarmPlayer"
    }

    /**
     * Start alarm playback with configured volume and lock the volume.
     */
    fun start() {
        try {
            // Store original alarm volume
            originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

            // Create MediaPlayer with alarm audio attributes
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            mediaPlayer = MediaPlayer.create(context, soundResId)?.apply {
                setAudioAttributes(audioAttributes)
                isLooping = true

                // Set initial volume
                val volume = volumePercent / 100f
                setVolume(volume, volume)

                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val targetVolume = (maxVolume * volumePercent / 100).coerceIn(0, maxVolume)
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    targetVolume,
                    0
                )

                // Start coroutine to lock volume
                volumeJob = scope.launch {
                    Log.d(TAG, "Volume lock coroutine started")
                    while (true) {
                        try {
                            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                            if (currentVolume != targetVolume) {
                                audioManager.setStreamVolume(
                                    AudioManager.STREAM_ALARM,
                                    targetVolume,
                                    0
                                )
                                Log.d(TAG, "Volume reset to $targetVolume (user tried to change it)")
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error in volume lock coroutine", e)
                        }
                        delay(250) // Check frequently
                    }
                }

                start()
                Log.d(TAG, "Alarm playback started at $volumePercent% volume")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting alarm playback", e)
        }
    }

    /**
     * Stop and release MediaPlayer resources, and restore original volume.
     */
    fun stop() {
        try {
            // Cancel volume lock coroutine
            volumeJob?.cancel()
            volumeJob = null
            Log.d(TAG, "Volume lock coroutine cancelled")

            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null

            // Restore original alarm volume
            if (originalAlarmVolume != -1) {
                audioManager.setStreamVolume(
                    AudioManager.STREAM_ALARM,
                    originalAlarmVolume,
                    0
                )
                Log.d(TAG, "Restored original alarm volume to $originalAlarmVolume")
            }

            Log.d(TAG, "Alarm playback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping alarm playback", e)
        } finally {
            scope.cancel() // Clean up the scope
        }
    }

    /**
     * Check if alarm is currently playing.
     */
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}
