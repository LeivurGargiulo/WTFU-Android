package com.wtfu.data

/**
 * Data class representing alarm configuration.
 */
data class AlarmSettings(
    val timeInMillis: Long = 28800000L,
    val ringDurationMinutes: Int = 5,
    val volumePercent: Int = 100,
    val soundResId: Int = 0, // R.raw.alarm_sound
    val autoStopEnabled: Boolean = true,
    val rescheduleOnBoot: Boolean = false,
    val isActive: Boolean = false
)
