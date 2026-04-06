package com.dailyrap.app.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrap.app.data.model.PlayerStats
import com.dailyrap.app.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(PlayerStats())
    val stats: StateFlow<PlayerStats> = _stats

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            _stats.value = statsRepository.getPlayerStats()
        }
    }
}
