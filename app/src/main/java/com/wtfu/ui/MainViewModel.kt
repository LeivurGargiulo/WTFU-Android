package com.wtfu.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wtfu.R
import com.wtfu.alarm.AlarmScheduler
import com.wtfu.data.AlarmPreferences
import com.wtfu.data.AlarmSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel for the main screen.
 * Manages alarm settings and scheduling.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val preferences = AlarmPreferences(context)
    private val scheduler = AlarmScheduler(context)

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferences.alarmSettingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    hour = getHourFromMillis(settings.timeInMillis),
                    minute = getMinuteFromMillis(settings.timeInMillis),
                    ringDurationMinutes = settings.ringDurationMinutes,
                    volumePercent = settings.volumePercent,
                    autoStopEnabled = settings.autoStopEnabled,
                    rescheduleOnBoot = settings.rescheduleOnBoot,
                    isAlarmActive = settings.isActive
                )
            }
        }
    }

    fun onHourChanged(hour: Int) {
        _uiState.value = _uiState.value.copy(hour = hour)
    }

    fun onMinuteChanged(minute: Int) {
        _uiState.value = _uiState.value.copy(minute = minute)
    }

    fun onRingDurationChanged(duration: String) {
        val durationInt = duration.toIntOrNull() ?: 5
        _uiState.value = _uiState.value.copy(ringDurationMinutes = durationInt.coerceIn(1, 60))
    }

    fun onVolumeChanged(volume: Float) {
        _uiState.value = _uiState.value.copy(volumePercent = volume.toInt())
    }

    fun onAutoStopToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoStopEnabled = enabled)
    }

    fun onRescheduleOnBootToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(rescheduleOnBoot = enabled)
    }

    fun setAlarm() {
        val state = _uiState.value
        
        // Calculate alarm time
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, state.hour)
            set(Calendar.MINUTE, state.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time is in the past, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val settings = AlarmSettings(
            timeInMillis = calendar.timeInMillis,
            ringDurationMinutes = state.ringDurationMinutes,
            volumePercent = state.volumePercent,
            soundResId = R.raw.alarm_sound,
            autoStopEnabled = state.autoStopEnabled,
            rescheduleOnBoot = state.rescheduleOnBoot,
            isActive = true
        )

        viewModelScope.launch {
            preferences.saveAlarmSettings(settings)
            val success = scheduler.scheduleAlarm(settings)
            _uiState.value = _uiState.value.copy(
                isAlarmActive = success,
                statusMessage = if (success) {
                    "Alarm set for ${formatTime(state.hour, state.minute)}"
                } else {
                    "Failed to set alarm. Check permissions."
                }
            )
        }
    }

    fun cancelAlarm() {
        scheduler.cancelAlarm()
        viewModelScope.launch {
            preferences.setAlarmActive(false)
            _uiState.value = _uiState.value.copy(
                isAlarmActive = false,
                statusMessage = "Alarm canceled"
            )
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return scheduler.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    private fun getHourFromMillis(millis: Long): Int {
        if (millis == 0L) return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.HOUR_OF_DAY)
    }

    private fun getMinuteFromMillis(millis: Long): Int {
        if (millis == 0L) return Calendar.getInstance().get(Calendar.MINUTE)
        return Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.MINUTE)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
    }
}

data class AlarmUiState(
    val hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
    val minute: Int = Calendar.getInstance().get(Calendar.MINUTE),
    val ringDurationMinutes: Int = 5,
    val volumePercent: Int = 100,
    val autoStopEnabled: Boolean = true,
    val rescheduleOnBoot: Boolean = false,
    val isAlarmActive: Boolean = false,
    val statusMessage: String = ""
)
