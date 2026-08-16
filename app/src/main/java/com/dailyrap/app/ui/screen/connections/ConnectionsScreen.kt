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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.ui.screen.connections.components.CategoryHintReveal
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ConnectionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConnectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResultDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = androidx.activity.compose.LocalActivity.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

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

                // Solved categories (full) + hinted categories (truncated), sorted by difficulty
                val solvedDifficulties = gameState.solvedCategories.map { it.difficulty }.toSet()
                val allBars = gameState.puzzle.categories
                    .filter { it.difficulty in solvedDifficulties || it.difficulty in gameState.hintedCategories }
                    .sortedBy { it.difficulty }

                allBars.forEach { category ->
                    if (category.difficulty in solvedDifficulties) {
                        CategoryReveal(category = category)
                    } else {
                        CategoryHintReveal(category = category)
                    }
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
                    val unhintedUnsolved = gameState.puzzle.categories.count {
                        it.difficulty !in solvedDifficulties && it.difficulty !in gameState.hintedCategories
                    }
                    val hintEnabled = gameState.hintsUsed < 2 && unhintedUnsolved > 1

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.requestHint() },
                            modifier = Modifier.weight(0.7f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = hintEnabled),
                            enabled = hintEnabled,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Podpowied\u017A",
                                tint = if (hintEnabled) Color(0xFFEAB308) else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

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

                // AdMob Banner
                Spacer(modifier = Modifier.height(16.dp))
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = "ca-app-pub-3940256099942544/6300978111"
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Hint dialog
        if (uiState.showHintDialog && gameState != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissHintDialog() },
                title = { Text("Podpowiedź", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Obejrzyj reklamę, aby poznać nazwę najłatwiejszej pozostałej kategorii.\n\nPozostało podpowiedzi: ${2 - gameState.hintsUsed}/2"
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (activity != null) {
                            viewModel.rewardedAdManager.showAd(
                                activity = activity,
                                onRewarded = { viewModel.onHintRewarded() },
                                onFailed = {
                                    viewModel.dismissHintDialog()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Nie udało się załadować reklamy")
                                    }
                                }
                            )
                        }
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Oglądam reklamę", color = AccentPurple)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissHintDialog() }) {
                        Text("Anuluj", color = TextSecondary)
                    }
                }
            )
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
