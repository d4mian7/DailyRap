package com.dailyrap.app.ui.screen.crossword

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.ui.screen.crossword.components.CrosswordGrid
import com.dailyrap.app.ui.screen.crossword.components.CrosswordResultDialog
import com.dailyrap.app.ui.theme.AccentPurple
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText

@Composable
fun CrosswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: CrosswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResultDialog by remember { mutableStateOf(false) }
    var showSurrenderDialog by remember { mutableStateOf(false) }
    var selectedClue by remember { mutableIntStateOf(0) }
    var secretWordMode by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CrosswordEvent.GameFinished -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    kotlinx.coroutines.delay(1000)
                    showResultDialog = true
                }
                is CrosswordEvent.AnswerCorrect -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    val gs = uiState.gameState ?: return@collect
                    val next = gs.puzzle.clues.indices.firstOrNull { it !in gs.correctRows && it !in gs.checkedRows }
                        ?: gs.puzzle.clues.indices.firstOrNull { it !in gs.correctRows }
                    if (next != null) {
                        selectedClue = next
                    }
                }
                is CrosswordEvent.AnswerWrong -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                is CrosswordEvent.SecretRevealed -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                is CrosswordEvent.SecretWordWrong -> {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                else -> {}
            }
        }
    }

    val gameState = uiState.gameState

    // If game was already finished (restored from progress), show result dialog
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
            .imePadding()
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Ładowanie...", color = TextSecondary)
            }
        } else if (uiState.noCrosswordToday || gameState == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Brak krzyżówki na dziś", color = TileText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(onClick = onNavigateBack) {
                        Text("Wróć", color = AccentPurple)
                    }
                }
            }
        } else {
            Scaffold(
                containerColor = BackgroundDark,
                bottomBar = {
                    BottomInputBar(
                        gameState = gameState,
                        selectedClue = selectedClue,
                        secretWordMode = secretWordMode,
                        onSecretWordModeChange = { secretWordMode = it },
                        viewModel = viewModel
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    // Top bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Wróć",
                                tint = TileText
                            )
                        }
                        Text(
                            text = "RapCrossword",
                            color = LetterPresent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (gameState.status == GameStatus.IN_PROGRESS) {
                            TextButton(onClick = { showSurrenderDialog = true }) {
                                Text(
                                    text = "PODDAJ SIĘ",
                                    color = AccentPurple,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Grid — fills remaining space, auto-sizes cells to fit
                    CrosswordGrid(
                        gameState = gameState,
                        selectedClueIndex = selectedClue,
                        secretWordMode = secretWordMode,
                        onClueClick = { idx ->
                            selectedClue = idx
                            secretWordMode = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }
            }
        }

        // Result dialog
        if (showResultDialog && gameState != null) {
            CrosswordResultDialog(
                gameState = gameState,
                onDismiss = { showResultDialog = false },
                onGoHome = {
                    showResultDialog = false
                    onNavigateBack()
                }
            )
        }

        // Surrender confirmation dialog
        if (showSurrenderDialog) {
            AlertDialog(
                onDismissRequest = { showSurrenderDialog = false },
                containerColor = TileDefault,
                title = {
                    Text(
                        text = "Poddajesz się?",
                        color = TileText,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Czy na pewno chcesz się poddać? Wszystkie nieodgadnięte hasła zostaną oznaczone jako błędne.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSurrenderDialog = false
                            viewModel.finishGame()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Text("Poddaj się", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSurrenderDialog = false }) {
                        Text("Anuluj", color = AccentPurple)
                    }
                }
            )
        }
    }
}

@Composable
private fun BottomInputBar(
    gameState: com.dailyrap.app.data.model.CrosswordGameState,
    selectedClue: Int,
    secretWordMode: Boolean,
    onSecretWordModeChange: (Boolean) -> Unit,
    viewModel: CrosswordViewModel
) {
    val currentClue = gameState.puzzle.clues.getOrNull(selectedClue)
    val secretLen = gameState.puzzle.secretWord.length
    val isClueCorrect = selectedClue in gameState.correctRows
    val isClueChecked = selectedClue in gameState.checkedRows
    val isGameActive = gameState.status == GameStatus.IN_PROGRESS

    // Should we show input row?
    val showInput = isGameActive && (
            (secretWordMode && !gameState.secretWordCorrect) ||
                    (!secretWordMode && !isClueCorrect && currentClue != null)
            )

    // Clue text
    val clueText = if (secretWordMode) {
        if (gameState.secretWordCorrect)
            "Hasło główne: ${gameState.puzzle.secretWord.uppercase()}"
        else
            "Hasło główne ($secretLen liter)"
    } else {
        currentClue?.let {
            "${selectedClue + 1}. ${it.question}" +
                    if (isClueCorrect) " — ${it.answer.uppercase()}" else ""
        } ?: ""
    }

    // Clue background color
    val clueBgColor by animateColorAsState(
        targetValue = when {
            secretWordMode && gameState.secretWordCorrect -> LetterCorrect.copy(alpha = 0.15f)
            !secretWordMode && isClueCorrect -> LetterCorrect.copy(alpha = 0.15f)
            !secretWordMode && isClueChecked -> LetterPresent.copy(alpha = 0.1f)
            else -> TileDefault
        },
        animationSpec = tween(300),
        label = "clueBg"
    )

    val clueTextColor = when {
        secretWordMode && gameState.secretWordCorrect -> LetterCorrect
        !secretWordMode && isClueCorrect -> LetterCorrect
        else -> TileText
    }

    // Input text state — keyed on mode + clue so it resets appropriately
    val maxLen = if (secretWordMode) secretLen else (currentClue?.answer?.length ?: 0)
    val currentAnswer = if (secretWordMode) gameState.secretWordAnswer
    else gameState.answers[selectedClue] ?: ""

    var localText by remember(secretWordMode, selectedClue) {
        mutableStateOf(currentAnswer)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // 1. Clue — always visible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(clueBgColor)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = clueText,
                color = clueTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }

        if (showInput) {
            Spacer(modifier = Modifier.height(6.dp))

            // 2. Row: toggle button + TextField + SPRAWDŹ
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (secretWordMode) Color(0xFF8B5CF6)
                            else TileDefault
                        )
                        .clickable { onSecretWordModeChange(!secretWordMode) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = if (secretWordMode) "Przełącz na pionowe" else "Przełącz na hasło główne",
                        tint = if (secretWordMode) Color.White else TileText.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // TextField
                BasicTextField(
                    value = localText,
                    onValueChange = { newValue ->
                        val filtered = newValue
                            .uppercase()
                            .filter { it.isLetter() }
                            .take(maxLen)
                        localText = filtered
                        if (secretWordMode) {
                            viewModel.updateSecretWordAnswer(filtered)
                        } else {
                            viewModel.updateAnswer(selectedClue, filtered)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TileDefault)
                        .padding(horizontal = 12.dp),
                    textStyle = TextStyle(
                        color = TileText.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(if (secretWordMode) AccentPurple else LetterPresent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (secretWordMode) viewModel.checkSecretWord()
                            else viewModel.checkAnswer(selectedClue)
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (localText.isEmpty()) {
                                Text(
                                    text = if (secretWordMode)
                                        "Hasło główne ($secretLen)"
                                    else
                                        "Odpowiedź ($maxLen)",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Check button
                Button(
                    onClick = {
                        if (secretWordMode) viewModel.checkSecretWord()
                        else viewModel.checkAnswer(selectedClue)
                    },
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (secretWordMode) AccentPurple else LetterPresent
                    )
                ) {
                    Text(
                        text = "SPRAWDŹ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
