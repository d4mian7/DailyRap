package com.dailyrap.app.ui.screen.crossword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrap.app.data.model.CrosswordGameState
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.repository.GameProgressRepository
import com.dailyrap.app.domain.CrosswordEngine
import com.dailyrap.app.domain.CrosswordEngine.CheckResult
import com.dailyrap.app.domain.usecase.GetDailyCrosswordUseCase
import com.dailyrap.app.domain.usecase.SaveGameResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

sealed class CrosswordEvent {
    data class AnswerCorrect(val row: Int) : CrosswordEvent()
    data class AnswerWrong(val row: Int) : CrosswordEvent()
    data object SecretRevealed : CrosswordEvent()
    data object SecretWordWrong : CrosswordEvent()
    data object GameFinished : CrosswordEvent()
}

data class CrosswordUiState(
    val gameState: CrosswordGameState? = null,
    val isLoading: Boolean = true,
    val noCrosswordToday: Boolean = false,
    val revealedLetters: List<Char?> = emptyList(),
    val alreadyFinished: Boolean = false
)

@HiltViewModel
class CrosswordViewModel @Inject constructor(
    private val getDailyCrossword: GetDailyCrosswordUseCase,
    private val saveGameResult: SaveGameResultUseCase,
    private val gameProgressRepository: GameProgressRepository
) : ViewModel() {

    private val engine = CrosswordEngine()

    private val _uiState = MutableStateFlow(CrosswordUiState())
    val uiState: StateFlow<CrosswordUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CrosswordEvent>()
    val events: SharedFlow<CrosswordEvent> = _events.asSharedFlow()

    init {
        loadCrossword()
    }

    private fun loadCrossword() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val crossword = getDailyCrossword(today)
        if (crossword == null) {
            _uiState.value = CrosswordUiState(isLoading = false, noCrosswordToday = true)
            return
        }

        // Try to restore saved progress for today
        val savedState = gameProgressRepository.loadCrosswordProgress(todayStr)
        if (savedState != null && savedState.puzzle.id == crossword.id) {
            val alreadyFinished = savedState.status != GameStatus.IN_PROGRESS
            _uiState.value = CrosswordUiState(
                gameState = savedState,
                isLoading = false,
                revealedLetters = engine.getRevealedSecretLetters(savedState),
                alreadyFinished = alreadyFinished
            )
            if (alreadyFinished) {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(300)
                    _events.emit(CrosswordEvent.GameFinished)
                }
            }
        } else {
            val gameState = engine.createGame(crossword)
            _uiState.value = CrosswordUiState(
                gameState = gameState,
                isLoading = false,
                revealedLetters = engine.getRevealedSecretLetters(gameState)
            )
        }
    }

    private fun saveProgress() {
        val state = _uiState.value.gameState ?: return
        gameProgressRepository.saveCrosswordProgress(state)
    }

    fun updateAnswer(rowIndex: Int, answer: String) {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val newState = engine.updateAnswer(state, rowIndex, answer)
        _uiState.value = _uiState.value.copy(gameState = newState)
    }

    fun updateSecretWordAnswer(answer: String) {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val newState = engine.updateSecretWordAnswer(state, answer)
        _uiState.value = _uiState.value.copy(gameState = newState)
    }

    fun checkSecretWord() {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val (newState, result) = engine.checkSecretWord(state)

        val revealed = engine.getRevealedSecretLetters(newState)
        _uiState.value = _uiState.value.copy(
            gameState = newState,
            revealedLetters = revealed
        )

        // Save progress
        gameProgressRepository.saveCrosswordProgress(newState)

        viewModelScope.launch {
            when (result) {
                is CrosswordEngine.CheckResult.Correct -> {
                    _events.emit(CrosswordEvent.SecretRevealed)
                }
                is CrosswordEngine.CheckResult.Wrong -> {
                    _events.emit(CrosswordEvent.SecretWordWrong)
                }
                is CrosswordEngine.CheckResult.Empty -> {}
            }
            if (newState.status != GameStatus.IN_PROGRESS) {
                _events.emit(CrosswordEvent.GameFinished)
                saveGameResult.saveCrosswordResult(newState)
            }
        }
    }

    fun checkAnswer(rowIndex: Int) {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val (newState, result) = engine.checkAnswer(state, rowIndex)

        val revealed = engine.getRevealedSecretLetters(newState)
        _uiState.value = _uiState.value.copy(
            gameState = newState,
            revealedLetters = revealed
        )

        // Save progress
        gameProgressRepository.saveCrosswordProgress(newState)

        viewModelScope.launch {
            when (result) {
                is CheckResult.Correct -> {
                    _events.emit(CrosswordEvent.AnswerCorrect(rowIndex))
                    if (newState.secretWordRevealed && !state.secretWordRevealed) {
                        _events.emit(CrosswordEvent.SecretRevealed)
                    }
                }
                is CheckResult.Wrong -> {
                    _events.emit(CrosswordEvent.AnswerWrong(rowIndex))
                }
                is CheckResult.Empty -> {}
            }
            if (newState.status != GameStatus.IN_PROGRESS) {
                _events.emit(CrosswordEvent.GameFinished)
                saveGameResult.saveCrosswordResult(newState)
            }
        }
    }

    fun finishGame() {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val newState = engine.finishGame(state)
        val revealed = engine.getRevealedSecretLetters(newState)
        _uiState.value = _uiState.value.copy(
            gameState = newState,
            revealedLetters = revealed
        )
        // Save progress
        gameProgressRepository.saveCrosswordProgress(newState)

        viewModelScope.launch {
            _events.emit(CrosswordEvent.GameFinished)
            saveGameResult.saveCrosswordResult(newState)
        }
    }

    override fun onCleared() {
        // Save progress when leaving the screen
        saveProgress()
        super.onCleared()
    }
}
