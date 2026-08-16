package com.dailyrap.app.ui.screen.literalnie

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.ui.screen.literalnie.components.ExtraAttemptDialog
import com.dailyrap.app.ui.screen.literalnie.components.Keyboard
import com.dailyrap.app.ui.screen.literalnie.components.LetterGrid
import com.dailyrap.app.ui.screen.literalnie.components.LiteralnieResultDialog
import com.dailyrap.app.ui.theme.AccentPurple
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileText
import kotlin.math.roundToInt

@Composable
fun LiteralnieScreen(
    onNavigateBack: () -> Unit,
    viewModel: LiteralnieViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResultDialog by remember { mutableStateOf(false) }
    var lastRevealedRow by remember { mutableIntStateOf(-1) }
    val context = LocalContext.current
    val activity = context as? Activity

    // If game was already finished (restored from progress), show all rows as revealed
    LaunchedEffect(uiState.alreadyFinished) {
        if (uiState.alreadyFinished && uiState.gameState != null) {
            lastRevealedRow = uiState.gameState!!.guesses.size - 1
        }
    }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LiteralnieEvent.TooShort,
                is LiteralnieEvent.NotInDictionary -> {
                    shakeOffset.animateTo(12f, tween(50))
                    shakeOffset.animateTo(-12f, tween(50))
                    shakeOffset.animateTo(8f, tween(50))
                    shakeOffset.animateTo(-8f, tween(50))
                    shakeOffset.animateTo(0f, tween(50))
                }
                is LiteralnieEvent.GuessAccepted -> {
                    lastRevealedRow = event.row
                }
                is LiteralnieEvent.GameWon, is LiteralnieEvent.GameLost -> {
                    showResultDialog = true
                }
                is LiteralnieEvent.LetterTyped -> {}
            }
        }
    }

    val gameState = uiState.gameState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ładowanie...", color = TextSecondary)
            }
        } else if (uiState.noWordToday || gameState == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Brak hasła na dziś", color = TileText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text("Wróć", color = AccentPurple)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Back button + title in one row
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            tint = TileText
                        )
                    }
                    Text(
                        text = "RapWord",
                        color = LetterCorrect,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Info + Hint in one row
                val totalGuesses = gameState.maxGuesses + gameState.extraAttemptsUsed
                Text(
                    text = "${gameState.dailyWord.variant} liter | $totalGuesses prób",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Kategoria: ${gameState.dailyWord.hint}",
                    color = AccentPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Letter grid with shake
                Box(
                    modifier = Modifier.offset {
                        IntOffset(shakeOffset.value.roundToInt(), 0)
                    }
                ) {
                    LetterGrid(
                        guesses = gameState.guesses,
                        currentGuess = gameState.currentGuess,
                        maxGuesses = totalGuesses,
                        wordLength = gameState.dailyWord.variant,
                        guessResults = uiState.guessResults,
                        lastRevealedRow = lastRevealedRow
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Remaining tries — above keyboard
                if (gameState.status == GameStatus.IN_PROGRESS) {
                    val remaining = totalGuesses - gameState.guesses.size
                    Text(
                        text = "Pozostało prób: $remaining",
                        color = if (remaining <= 2) LetterPresent else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Keyboard
                Keyboard(
                    keyboardState = uiState.keyboardState,
                    onKey = { viewModel.onKeyPress(it) },
                    onEnter = { viewModel.onEnter() },
                    onDelete = { viewModel.onDelete() },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Extra attempt dialog
        if (uiState.showExtraAttemptDialog && gameState != null) {
            ExtraAttemptDialog(
                extraAttemptsUsed = gameState.extraAttemptsUsed,
                onWatchAd = {
                    if (activity != null) {
                        viewModel.onWatchAdForExtraAttempt(activity)
                    }
                },
                onGiveUp = { viewModel.onGiveUp() }
            )
        }

        // Result dialog
        if (showResultDialog && gameState != null) {
            LiteralnieResultDialog(
                gameState = gameState,
                guessResults = uiState.guessResults,
                onDismiss = { showResultDialog = false },
                onGoHome = {
                    showResultDialog = false
                    onNavigateBack()
                }
            )
        }
    }
}
