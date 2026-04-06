package com.dailyrap.app.ui.screen.connections

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.ui.screen.connections.components.CategoryReveal
import com.dailyrap.app.ui.screen.connections.components.MistakesIndicator
import com.dailyrap.app.ui.screen.connections.components.ResultsDialog
import com.dailyrap.app.ui.screen.connections.components.WordGrid
import com.dailyrap.app.ui.theme.AccentPurple
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.BorderSubtle
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileSelected
import com.dailyrap.app.ui.theme.TileText
import kotlin.math.roundToInt

@Composable
fun ConnectionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConnectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResultDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ConnectionsEvent.Correct -> {}
                is ConnectionsEvent.AlmostCorrect -> {
                    toastMessage = "Prawie! Jedno słowo źle"
                    shakeOffset.animateTo(12f, tween(50))
                    shakeOffset.animateTo(-12f, tween(50))
                    shakeOffset.animateTo(8f, tween(50))
                    shakeOffset.animateTo(-8f, tween(50))
                    shakeOffset.animateTo(0f, tween(50))
                }
                is ConnectionsEvent.Wrong -> {
                    toastMessage = null
                    shakeOffset.animateTo(12f, tween(50))
                    shakeOffset.animateTo(-12f, tween(50))
                    shakeOffset.animateTo(8f, tween(50))
                    shakeOffset.animateTo(-8f, tween(50))
                    shakeOffset.animateTo(0f, tween(50))
                }
                is ConnectionsEvent.InvalidGuess -> {
                    toastMessage = "Już to sprawdzałeś!"
                }
                is ConnectionsEvent.GameWon, is ConnectionsEvent.GameLost -> {
                    showResultDialog = true
                }
            }
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            kotlinx.coroutines.delay(2000L)
            toastMessage = null
        }
    }

    val gameState = uiState.gameState
    val isGameOver = gameState?.status != GameStatus.IN_PROGRESS

    // If game was already finished (restored from progress), show result dialog immediately
    LaunchedEffect(uiState.alreadyFinished) {
        if (uiState.alreadyFinished) {
            showResultDialog = true
        }
    }

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
        } else if (uiState.noPuzzleToday) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Brak puzzle'a na dziś",
                        color = TileText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Sprawdź jutro!", color = TextSecondary)
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text("Wróć", color = AccentPurple)
                    }
                }
            }
        } else if (gameState != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć",
                            tint = TileText
                        )
                    }
                }

                // Title
                Text(
                    text = "RapConnect",
                    color = TileSelected,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "Znajdź 4 grupy po 4 słowa",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Solved categories
                gameState.solvedCategories.forEach { category ->
                    CategoryReveal(category = category)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Word grid with shake
                if (gameState.remainingWords.isNotEmpty()) {
                    Box(
                        modifier = Modifier.offset {
                            IntOffset(shakeOffset.value.roundToInt(), 0)
                        }
                    ) {
                        WordGrid(
                            words = gameState.remainingWords,
                            selectedWords = gameState.selectedWords,
                            onWordClick = { word ->
                                if (!isGameOver) viewModel.toggleWord(word)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mistakes indicator
                MistakesIndicator(mistakesLeft = gameState.mistakesLeft)

                Spacer(modifier = Modifier.height(16.dp))

                // Toast
                if (toastMessage != null) {
                    Text(
                        text = toastMessage!!,
                        color = AccentPurple,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Action buttons
                if (!isGameOver) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.shuffle() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "TASUJ",
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearSelection() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = gameState.selectedWords.isNotEmpty()),
                            enabled = gameState.selectedWords.isNotEmpty(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "WYCZYŚĆ",
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        Button(
                            onClick = { viewModel.submitGuess() },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TileSelected
                            ),
                            enabled = gameState.selectedWords.size == 4,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "SPRAWDŹ",
                                color = TileText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Results dialog
        if (showResultDialog && gameState != null) {
            ResultsDialog(
                gameState = gameState,
                elapsedSeconds = uiState.elapsedSeconds,
                onDismiss = { showResultDialog = false },
                onGoHome = {
                    showResultDialog = false
                    onNavigateBack()
                }
            )
        }
    }
}
