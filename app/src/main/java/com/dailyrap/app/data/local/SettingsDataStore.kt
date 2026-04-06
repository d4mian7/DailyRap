package com.dailyrap.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_HOUR = intPreferencesKey("notification_hour")
        val NOTIFICATION_MINUTE = intPreferencesKey("notification_minute")
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.VIBRATION_ENABLED] ?: true }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.NOTIFICATIONS_ENABLED] ?: false }

    val notificationHour: Flow<Int> = context.dataStore.data
        .map { it[Keys.NOTIFICATION_HOUR] ?: 12 }

    val notificationMinute: Flow<Int> = context.dataStore.data
        .map { it[Keys.NOTIFICATION_MINUTE] ?: 0 }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[Keys.NOTIFICATION_HOUR] = hour
            it[Keys.NOTIFICATION_MINUTE] = minute
        }
    }
}
