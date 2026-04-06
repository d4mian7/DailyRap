package com.dailyrap.app.ui.screen.literalnie

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrap.app.ads.RewardedAdManager
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.LetterResult
import com.dailyrap.app.data.model.LiteralnieGameState
import com.dailyrap.app.data.repository.GameProgressRepository
import com.dailyrap.app.data.repository.WordRepository
import com.dailyrap.app.domain.LiteralnieEngine
import com.dailyrap.app.domain.LiteralnieEngine.SubmitResult
import com.dailyrap.app.domain.usecase.GetDailyWordUseCase
import com.dailyrap.app.domain.usecase.SaveGameResultUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

sealed class LiteralnieEvent {
    data object TooShort : LiteralnieEvent()
    data object NotInDictionary : LiteralnieEvent()
    data class GuessAccepted(val row: Int, val results: List<LetterResult>) : LiteralnieEvent()
    data object GameWon : LiteralnieEvent()
    data object GameLost : LiteralnieEvent()
    data object LetterTyped : LiteralnieEvent()
}

data class LiteralnieUiState(
    val gameState: LiteralnieGameState? = null,
    val isLoading: Boolean = true,
    val noWordToday: Boolean = false,
    val guessResults: List<List<LetterResult>> = emptyList(),
    val keyboardState: Map<Char, LetterResult> = emptyMap(),
    val alreadyFinished: Boolean = false,
    val showExtraAttemptDialog: Boolean = false
)

@HiltViewModel
class LiteralnieViewModel @Inject constructor(
    private val getDailyWord: GetDailyWordUseCase,
    private val saveGameResult: SaveGameResultUseCase,
    private val wordRepository: WordRepository,
    private val gameProgressRepository: GameProgressRepository,
    val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val engine = LiteralnieEngine()
    private var dictionary: Set<String> = emptySet()

    private val _uiState = MutableStateFlow(LiteralnieUiState())
    val uiState: StateFlow<LiteralnieUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LiteralnieEvent>()
    val events: SharedFlow<LiteralnieEvent> = _events.asSharedFlow()

    init {
        rewardedAdManager.loadAd()
        loadWord()
    }

    private fun loadWord() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val word = getDailyWord(today)
        if (word == null) {
            _uiState.value = LiteralnieUiState(isLoading = false, noWordToday = true)
            return
        }
        dictionary = wordRepository.getDictionary()

        // Try to restore saved progress for today
        val savedState = gameProgressRepository.loadLiteralnieProgress(todayStr)
        if (savedState != null && savedState.dailyWord.id == word.id) {
            val guessResults = savedState.guesses.map { guess ->
                engine.evaluateGuess(guess, savedState.dailyWord.word.uppercase())
            }
            val keyboardState = engine.getKeyboardState(savedState)
            val alreadyFinished = savedState.status != GameStatus.IN_PROGRESS
            _uiState.value = LiteralnieUiState(
                gameState = savedState,
                isLoading = false,
                guessResults = guessResults,
                keyboardState = keyboardState,
                alreadyFinished = alreadyFinished
            )
            if (alreadyFinished) {
                viewModelScope.launch {
                    delay(300)
                    if (savedState.status == GameStatus.WON) {
                        _events.emit(LiteralnieEvent.GameWon)
                    } else {
                        _events.emit(LiteralnieEvent.GameLost)
                    }
                }
            }
        } else {
            val gameState = engine.createGame(word)
            _uiState.value = LiteralnieUiState(
                gameState = gameState,
                isLoading = false
            )
        }
    }

    fun onKeyPress(letter: Char) {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val newState = engine.typeLetter(state, letter)
        if (newState !== state) {
            _uiState.value = _uiState.value.copy(gameState = newState)
            viewModelScope.launch { _events.emit(LiteralnieEvent.LetterTyped) }
        }
    }

    fun onDelete() {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        _uiState.value = _uiState.value.copy(gameState = engine.deleteLetter(state))
    }

    fun onEnter() {
        val state = _uiState.value.gameState ?: return
        if (state.status != GameStatus.IN_PROGRESS) return
        val (newState, result) = engine.submitGuess(state, dictionary)

        when (result) {
            is SubmitResult.TooShort -> {
                viewModelScope.launch { _events.emit(LiteralnieEvent.TooShort) }
            }
            is SubmitResult.NotInDictionary -> {
                viewModelScope.launch { _events.emit(LiteralnieEvent.NotInDictionary) }
            }
            is SubmitResult.Accepted -> {
                val newResults = _uiState.value.guessResults + listOf(result.letterResults)
                val newKeyboard = engine.getKeyboardState(newState)
                _uiState.value = _uiState.value.copy(
                    gameState = newState,
                    guessResults = newResults,
                    keyboardState = newKeyboard
                )
                gameProgressRepository.saveLiteralnieProgress(newState)

                viewModelScope.launch {
                    _events.emit(
                        LiteralnieEvent.GuessAccepted(
                            row = newState.guesses.size - 1,
                            results = result.letterResults
                        )
                    )
                    if (newState.status == GameStatus.WON) {
                        delay(1200)
                        _events.emit(LiteralnieEvent.GameWon)
                        saveGameResult.saveLiteralnieResult(newState)
                    } else if (newState.status == GameStatus.LOST) {
                        // Check if extra attempts are available
                        if (newState.extraAttemptsUsed < LiteralnieGameState.MAX_EXTRA_ATTEMPTS) {
                            delay(1200)
                            _uiState.value = _uiState.value.copy(showExtraAttemptDialog = true)
                        } else {
                            delay(1200)
                            _events.emit(LiteralnieEvent.GameLost)
                            saveGameResult.saveLiteralnieResult(newState)
                        }
                    }
                }
            }
            is SubmitResult.GameOver -> { /* ignore */ }
        }
    }

    fun onWatchAdForExtraAttempt(activity: Activity) {
        rewardedAdManager.showAd(
            activity = activity,
            onRewarded = {
                val state = _uiState.value.gameState ?: return@showAd
                val newExtraUsed = state.extraAttemptsUsed + 1
                val newState = state.copy(
                    status = GameStatus.IN_PROGRESS,
                    extraAttemptsUsed = newExtraUsed
                )
                val newKeyboard = engine.getKeyboardState(newState)
                _uiState.value = _uiState.value.copy(
                    gameState = newState,
                    keyboardState = newKeyboard,
                    showExtraAttemptDialog = false
                )
                gameProgressRepository.saveLiteralnieProgress(newState)
            },
            onFailed = {
                // No ad available — end the game
                val state = _uiState.value.gameState ?: return@showAd
                _uiState.value = _uiState.value.copy(showExtraAttemptDialog = false)
                viewModelScope.launch {
                    _events.emit(LiteralnieEvent.GameLost)
                    saveGameResult.saveLiteralnieResult(state)
                }
            }
        )
    }

    fun onGiveUp() {
        val state = _uiState.value.gameState ?: return
        _uiState.value = _uiState.value.copy(showExtraAttemptDialog = false)
        viewModelScope.launch {
            _events.emit(LiteralnieEvent.GameLost)
            saveGameResult.saveLiteralnieResult(state)
        }
    }
}
