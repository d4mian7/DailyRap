package com.dailyrap.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrap.app.data.model.CrosswordPuzzle
import com.dailyrap.app.data.model.DailyWord
import com.dailyrap.app.data.model.Puzzle
import com.dailyrap.app.data.repository.StatsRepository
import com.dailyrap.app.domain.usecase.GetDailyCrosswordUseCase
import com.dailyrap.app.domain.usecase.GetDailyPuzzleUseCase
import com.dailyrap.app.domain.usecase.GetDailyWordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HomeUiState(
    val todayPuzzle: Puzzle? = null,
    val todayWord: DailyWord? = null,
    val todayCrossword: CrosswordPuzzle? = null,
    val connectionsPlayed: Boolean = false,
    val literalniePlayed: Boolean = false,
    val crosswordPlayed: Boolean = false,
    val connectionsStreak: Int = 0,
    val literalnieStreak: Int = 0,
    val crosswordStreak: Int = 0,
    val secondsUntilNextGame: Long = 0L,
    val todayDate: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDailyPuzzle: GetDailyPuzzleUseCase,
    private val getDailyWord: GetDailyWordUseCase,
    private val getDailyCrossword: GetDailyCrosswordUseCase,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val puzzle = getDailyPuzzle(today)
        val word = getDailyWord(today)
        val crossword = getDailyCrossword(today)

        val midnight = LocalDateTime.of(today.plusDays(1), LocalTime.MIDNIGHT)
        val secondsUntil = ChronoUnit.SECONDS.between(LocalDateTime.now(), midnight)

        viewModelScope.launch {
            val connectionsResult = statsRepository.getConnectionsResult(todayStr)
            val literalnieResult = statsRepository.getLiteralnieResult(todayStr)
            val crosswordResult = statsRepository.getCrosswordResult(todayStr)
            val stats = statsRepository.getPlayerStats()

            _uiState.value = HomeUiState(
                todayPuzzle = puzzle,
                todayWord = word,
                todayCrossword = crossword,
                connectionsPlayed = connectionsResult != null,
                literalniePlayed = literalnieResult != null,
                crosswordPlayed = crosswordResult != null,
                connectionsStreak = stats.connectionsStreak,
                literalnieStreak = stats.literalnieStreak,
                crosswordStreak = stats.crosswordStreak,
                secondsUntilNextGame = secondsUntil.coerceAtLeast(0),
                todayDate = todayStr
            )
        }
    }
}
