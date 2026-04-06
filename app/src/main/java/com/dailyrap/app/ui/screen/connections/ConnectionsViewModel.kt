package com.dailyrap.app.ui.screen.connections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrap.app.data.model.Category
import com.dailyrap.app.data.model.ConnectionsGameState
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.Puzzle
import com.dailyrap.app.data.repository.GameProgressRepository
import com.dailyrap.app.domain.ConnectionsEngine
import com.dailyrap.app.domain.ConnectionsEngine.GuessResult
import com.dailyrap.app.domain.usecase.GetDailyPuzzleUseCase
import com.dailyrap.app.domain.usecase.SaveGameResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class ConnectionsEvent {
    data class Correct(val category: Category) : ConnectionsEvent()
    data object AlmostCorrect : ConnectionsEvent()
    data object Wrong : ConnectionsEvent()
    data object InvalidGuess : ConnectionsEvent()
    data object GameWon : ConnectionsEvent()
    data object GameLost : ConnectionsEvent()
}

data class ConnectionsUiState(
    val gameState: ConnectionsGameState? = null,
    val puzzle: Puzzle? = null,
    val isLoading: Boolean = true,
    val noPuzzleToday: Boolean = false,
    val elapsedSeconds: Int = 0,
    val todayDate: String = "",
    val alreadyFinished: Boolean = false
)

@HiltViewModel
class ConnectionsViewModel @Inject constructor(
    private val getDailyPuzzle: GetDailyPuzzleUseCase,
    private val saveGameResult: SaveGameResultUseCase,
    private val gameProgressRepository: GameProgressRepository
) : ViewModel() {

    private val engine = ConnectionsEngine()

    private val _uiState = MutableStateFlow(ConnectionsUiState())
    val uiState: StateFlow<ConnectionsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ConnectionsEvent>()
    val events: SharedFlow<ConnectionsEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null

    init {
        loadPuzzle()
    }

    private fun loadPuzzle() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val puzzle = getDailyPuzzle(today)

        if (puzzle == null) {
            _uiState.value = ConnectionsUiState(
                isLoading = false,
                noPuzzleToday = true,
                todayDate = todayStr
            )
            return
        }

        // Try to restore saved progress for today
        val saved = gameProgressRepository.loadConnectionsProgress(todayStr)
        if (saved != null && saved.first.puzzle.id == puzzle.id) {
            val (savedState, savedElapsed) = saved
            val alreadyFinished = savedState.status != GameStatus.IN_PROGRESS
            _uiState.value = ConnectionsUiState(
                gameState = savedState,
                puzzle = puzzle,
                isLoading = false,
                todayDate = todayStr,
                elapsedSeconds = savedElapsed,
                alreadyFinished = alreadyFinished
            )
            if (alreadyFinished) {
                viewModelScope.launch {
                    delay(300)
                    if (savedState.status == GameStatus.WON) {
                        _events.emit(ConnectionsEvent.GameWon)
                    } else {
                        _events.emit(ConnectionsEvent.GameLost)
                    }
                }
            } else {
                startTimer()
            }
        } else {
            val gameState = engine.createGame(puzzle)
            _uiState.value = ConnectionsUiState(
                gameState = gameState,
                puzzle = puzzle,
                isLoading = false,
                todayDate = todayStr
            )
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value
                if (current.gameState?.status == GameStatus.IN_PROGRESS) {
                    _uiState.value = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                }
            }
        }
    }

    private fun saveProgress() {
        val state = _uiState.value.gameState ?: return
        gameProgressRepository.saveConnectionsProgress(state, _uiState.value.elapsedSeconds)
    }

    fun toggleWord(word: String) {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        _uiState.value = _uiState.value.copy(gameState = engine.toggleWord(state, word))
    }

    fun clearSelection() {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        _uiState.value = _uiState.value.copy(gameState = engine.clearSelection(state))
    }

    fun shuffle() {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        _uiState.value = _uiState.value.copy(gameState = engine.shuffle(state))
    }

    fun submitGuess() {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val (newState, result) = engine.submitGuess(state)
        _uiState.value = _uiState.value.copy(gameState = newState)

        // Save progress after each guess
        gameProgressRepository.saveConnectionsProgress(newState, _uiState.value.elapsedSeconds)

        viewModelScope.launch {
            when (result) {
                is GuessResult.Correct -> {
                    _events.emit(ConnectionsEvent.Correct(result.category))
                    if (newState.status == GameStatus.WON) {
                        delay(600)
                        _events.emit(ConnectionsEvent.GameWon)
                        saveResult(newState)
                    }
                }
                is GuessResult.AlmostCorrect -> {
                    _events.emit(ConnectionsEvent.AlmostCorrect)
                    if (newState.status == GameStatus.LOST) {
                        delay(600)
                        _events.emit(ConnectionsEvent.GameLost)
                        saveResult(newState)
                    }
                }
                is GuessResult.Wrong -> {
                    _events.emit(ConnectionsEvent.Wrong)
                    if (newState.status == GameStatus.LOST) {
                        delay(600)
                        _events.emit(ConnectionsEvent.GameLost)
                        saveResult(newState)
                    }
                }
                is GuessResult.Invalid -> {
                    _events.emit(ConnectionsEvent.InvalidGuess)
                }
            }
        }
    }

    private fun saveResult(state: ConnectionsGameState) {
        viewModelScope.launch {
            saveGameResult.saveConnectionsResult(state, _uiState.value.elapsedSeconds)
        }
        timerJob?.cancel()
    }

    override fun onCleared() {
        timerJob?.cancel()
        // Save progress when leaving the screen
        saveProgress()
        super.onCleared()
    }
}
