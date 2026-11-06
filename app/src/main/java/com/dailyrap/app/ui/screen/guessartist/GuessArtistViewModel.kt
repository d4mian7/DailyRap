package com.dailyrap.app.ui.screen.guessartist

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyrap.app.ads.RewardedAdManager
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.GuessArtistGameState
import com.dailyrap.app.data.repository.GameProgressRepository
import com.dailyrap.app.data.repository.GuessArtistRepository
import com.dailyrap.app.domain.GuessArtistEngine
import com.dailyrap.app.domain.usecase.GetDailyArtistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class GuessArtistUiState(
    val gameState: GuessArtistGameState? = null,
    val isLoading: Boolean = true,
    val query: String = "",
    val suggestions: List<String> = emptyList(),
    val showExtraChanceDialog: Boolean = false,
    val showResultDialog: Boolean = false
)

@HiltViewModel
class GuessArtistViewModel @Inject constructor(
    private val getDailyArtist: GetDailyArtistUseCase,
    private val repository: GuessArtistRepository,
    private val gameProgressRepository: GameProgressRepository,
    private val rewardedAdManager: RewardedAdManager
) : ViewModel() {

    private val engine = GuessArtistEngine()
    private val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    private val _uiState = MutableStateFlow(GuessArtistUiState())
    val uiState: StateFlow<GuessArtistUiState> = _uiState.asStateFlow()

    init {
        rewardedAdManager.loadAd()
        loadGame()
    }

    private fun loadGame() {
        val puzzle = getDailyArtist()
        if (puzzle == null) {
            _uiState.value = GuessArtistUiState(isLoading = false)
            return
        }
        val saved = gameProgressRepository.loadGuessArtistProgress(todayStr)
            ?.takeIf { it.puzzle.id == puzzle.id }
        val state = saved ?: engine.createGame(puzzle)
        _uiState.value = GuessArtistUiState(
            gameState = state,
            isLoading = false,
            showResultDialog = state.status != GameStatus.IN_PROGRESS
        )
    }

    fun onQueryChange(query: String) {
        val state = _uiState.value.gameState ?: return
        _uiState.value = _uiState.value.copy(
            query = query,
            suggestions = engine.suggestions(query, repository.load().pool, state.guesses)
        )
    }

    fun submitGuess(artist: String) {
        val state = _uiState.value.gameState ?: return
        val (newState, _) = engine.submitGuess(state, artist)
        update(newState)
        if (newState.status == GameStatus.IN_PROGRESS) return
        viewModelScope.launch {
            delay(500)
            _uiState.value = if (newState.status == GameStatus.LOST && !newState.extraChanceUsed) {
                _uiState.value.copy(showExtraChanceDialog = true)
            } else {
                _uiState.value.copy(showResultDialog = true)
            }
        }
    }

    fun nextClue() {
        val state = _uiState.value.gameState ?: return
        update(engine.nextClue(state))
    }

    fun onWatchAd(activity: Activity) {
        rewardedAdManager.showAd(
            activity = activity,
            onRewarded = {
                val state = _uiState.value.gameState ?: return@showAd
                update(engine.grantExtraChance(state))
                _uiState.value = _uiState.value.copy(showExtraChanceDialog = false)
            },
            onFailed = { onGiveUp() }
        )
    }

    fun onGiveUp() {
        _uiState.value = _uiState.value.copy(showExtraChanceDialog = false, showResultDialog = true)
    }

    fun dismissResult() {
        _uiState.value = _uiState.value.copy(showResultDialog = false)
    }

    private fun update(newState: GuessArtistGameState) {
        _uiState.value = _uiState.value.copy(gameState = newState, query = "", suggestions = emptyList())
        gameProgressRepository.saveGuessArtistProgress(newState, todayStr)
    }
}
