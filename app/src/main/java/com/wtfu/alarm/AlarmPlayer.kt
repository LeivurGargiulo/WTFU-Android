package com.wtfu.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

/**
 * Handles the playback of the alarm sound using MediaPlayer.
 */
class AlarmPlayer(
    private val context: Context,
    private val soundUri: Uri,
    private val volumePercent: Int
) {

    private var mediaPlayer: MediaPlayer? = null
    private val tag = "AlarmPlayer"

    /**
     * Creates, configures, and starts the MediaPlayer.
     */
    fun start() {
        Log.d(tag, "Starting alarm playback...")
        try {
            // Release any existing player
            stop()

            // Create and configure a new MediaPlayer instance
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, soundUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                isLooping = true // The alarm sound should loop
                prepareAsync() // Prepare the player asynchronously
                setOnPreparedListener {
                    Log.d(tag, "MediaPlayer prepared, starting playback.")
                    it.start()
                    setVolume(volumePercent) // Set volume after starting
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(tag, "MediaPlayer error - What: $what, Extra: $extra")
                    true // Return true to indicate the error has been handled
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to start MediaPlayer", e)
        }
    }

    /**
     * Stops and releases the MediaPlayer resources.
     */
    fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                Log.d(tag, "MediaPlayer stopped.")
            }
            it.release()
            Log.d(tag, "MediaPlayer released.")
        }
        mediaPlayer = null
    }

    /**
     * Sets the volume of the MediaPlayer.
     * @param percent The volume level, from 0 to 100.
     */
    private fun setVolume(percent: Int) {
        val volume = percent / 100f
        mediaPlayer?.setVolume(volume, volume)
        Log.d(tag, "Alarm volume set to $percent%")
    }
}