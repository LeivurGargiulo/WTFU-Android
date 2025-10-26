package com.wtfu.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing alarm configuration.
 */
@Parcelize
data class AlarmSettings(
    val timeInMillis: Long = 28800000L,
    val ringDurationMinutes: Int = 5,
    val volumePercent: Int = 100,
    val soundResId: Int = 0,
    val autoStopEnabled: Boolean = true,
    val rescheduleOnBoot: Boolean = false,
    val isActive: Boolean = false
) : Parcelable
