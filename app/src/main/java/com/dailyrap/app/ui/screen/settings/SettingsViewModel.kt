package com.dailyrap.app.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dailyrap.app.data.local.SettingsDataStore
import com.dailyrap.app.data.repository.StatsRepository
import com.dailyrap.app.util.NotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val notificationHour: Int = 12,
    val notificationMinute: Int = 0,
    val statsReset: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val statsRepository: StatsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.value = SettingsUiState(
                vibrationEnabled = settingsDataStore.vibrationEnabled.first(),
                notificationsEnabled = settingsDataStore.notificationsEnabled.first(),
                notificationHour = settingsDataStore.notificationHour.first(),
                notificationMinute = settingsDataStore.notificationMinute.first()
            )
        }
    }

    fun setVibration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(vibrationEnabled = enabled)
        viewModelScope.launch { settingsDataStore.setVibrationEnabled(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        viewModelScope.launch {
            settingsDataStore.setNotificationsEnabled(enabled)
            if (enabled) {
                scheduleNotification()
            } else {
                cancelNotification()
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(notificationHour = hour, notificationMinute = minute)
        viewModelScope.launch {
            settingsDataStore.setNotificationTime(hour, minute)
            if (_uiState.value.notificationsEnabled) {
                scheduleNotification()
            }
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            statsRepository.resetAllStats()
            _uiState.value = _uiState.value.copy(statsReset = true)
        }
    }

    private fun scheduleNotification() {
        val state = _uiState.value
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, state.notificationHour)
            set(Calendar.MINUTE, state.notificationMinute)
            set(Calendar.SECOND, 0)
        }
        if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)
        val delay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private fun cancelNotification() {
        WorkManager.getInstance(context).cancelUniqueWork(NotificationWorker.WORK_NAME)
    }
}
