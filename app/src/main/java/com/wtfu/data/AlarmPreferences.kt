package com.wtfu.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alarm_settings")

/**
 * DataStore repository for persisting alarm settings.
 */
class AlarmPreferences(private val context: Context) {

    companion object {
        private val TIME_IN_MILLIS = longPreferencesKey("time_in_millis")
        private val RING_DURATION_MINUTES = intPreferencesKey("ring_duration_minutes")
        private val VOLUME_PERCENT = intPreferencesKey("volume_percent")
        private val SOUND_RES_ID = intPreferencesKey("sound_res_id")
        private val AUTO_STOP_ENABLED = booleanPreferencesKey("auto_stop_enabled")
        private val RESCHEDULE_ON_BOOT = booleanPreferencesKey("reschedule_on_boot")
        private val IS_ACTIVE = booleanPreferencesKey("is_active")
    }

    val alarmSettingsFlow: Flow<AlarmSettings> = context.dataStore.data.map { preferences ->
        AlarmSettings(
            timeInMillis = preferences[TIME_IN_MILLIS] ?: 0L,
            ringDurationMinutes = preferences[RING_DURATION_MINUTES] ?: 5,
            volumePercent = preferences[VOLUME_PERCENT] ?: 100,
            soundResId = preferences[SOUND_RES_ID] ?: 0,
            autoStopEnabled = preferences[AUTO_STOP_ENABLED] ?: true,
            rescheduleOnBoot = preferences[RESCHEDULE_ON_BOOT] ?: false,
            isActive = preferences[IS_ACTIVE] ?: false
        )
    }

    suspend fun saveAlarmSettings(settings: AlarmSettings) {
        context.dataStore.edit { preferences ->
            preferences[TIME_IN_MILLIS] = settings.timeInMillis
            preferences[RING_DURATION_MINUTES] = settings.ringDurationMinutes
            preferences[VOLUME_PERCENT] = settings.volumePercent
            preferences[SOUND_RES_ID] = settings.soundResId
            preferences[AUTO_STOP_ENABLED] = settings.autoStopEnabled
            preferences[RESCHEDULE_ON_BOOT] = settings.rescheduleOnBoot
            preferences[IS_ACTIVE] = settings.isActive
        }
    }

    suspend fun setAlarmActive(isActive: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_ACTIVE] = isActive
        }
    }
}
