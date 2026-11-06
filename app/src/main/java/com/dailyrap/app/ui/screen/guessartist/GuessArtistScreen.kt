package com.dailyrap.app.ui.screen.guessartist

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.data.model.GameStatus
import com.dailyrap.app.data.model.GuessArtistGameState
import com.dailyrap.app.ui.theme.AccentPurple
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText

val GuessArtistAccent = Color(0xFF38BDF8)

fun buildGuessArtistShareText(state: GuessArtistGameState): String {
    val total = state.puzzle.clues.size
    val won = state.status == GameStatus.WON
    val tiles = (1..total).joinToString("") { i ->
        when {
            won && i == state.revealedClues -> "🟩"
            i <= state.revealedClues -> "🟥"
            else -> "⬛"
        }
    }
    val result = when {
        !won -> "Nie udało się ($total/$total)"
        state.extraChanceUsed -> "Trop $total/$total + dodatkowa szansa"
        else -> "Trop ${state.revealedClues}/$total"
    }
    return "Kim jestem? 🎤 #${state.puzzle.id}\n$tiles\n$result"
}

@Composable
fun GuessArtistScreen(
    onNavigateBack: () -> Unit,
    viewModel: GuessArtistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val gameState = uiState.gameState
    val activity = LocalContext.current as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        if (uiState.isLoading) {
            Text("Ładowanie...", color = TextSecondary, modifier = Modifier.align(Alignment.Center))
        } else if (gameState == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Brak zestawu na dziś", color = TileText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onNavigateBack) {
                    Text("Wróć", color = AccentPurple)
                }
            }
        } else {
            GameContent(
                gameState = gameState,
                query = uiState.query,
                suggestions = uiState.suggestions,
                hasNextClue = gameState.revealedClues < gameState.puzzle.clues.size,
                onNavigateBack = onNavigateBack,
                onQueryChange = viewModel::onQueryChange,
                onGuess = viewModel::submitGuess,
                onNextClue = viewModel::nextClue,
                onShowResult = { viewModel.onGiveUp() }
            )

            if (uiState.showExtraChanceDialog) {
                ExtraChanceDialog(
                    onWatchAd = {
                        if (activity != null) viewModel.onWatchAd(activity) else viewModel.onGiveUp()
                    },
                    onGiveUp = viewModel::onGiveUp
                )
            }
            if (uiState.showResultDialog) {
                ResultDialog(
                    gameState = gameState,
                    onDismiss = viewModel::dismissResult,
                    onGoHome = {
                        viewModel.dismissResult()
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
private fun GameContent(
    gameState: GuessArtistGameState,
    query: String,
    suggestions: List<String>,
    hasNextClue: Boolean,
    onNavigateBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onGuess: (String) -> Unit,
    onNextClue: () -> Unit,
    onShowResult: () -> Unit
) {
    val total = gameState.puzzle.clues.size
    val active = gameState.status == GameStatus.IN_PROGRESS
    val scrollState = rememberScrollState()

    // Nowy trop ma być od razu widoczny
    LaunchedEffect(gameState.revealedClues) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                text = "Kim jestem?",
                color = GuessArtistAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Trop ${gameState.revealedClues}/$total",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Silhouette(revealedName = if (active) null else gameState.puzzle.artist)
            Spacer(modifier = Modifier.height(16.dp))

            gameState.puzzle.clues.take(gameState.revealedClues).forEachIndexed { index, clue ->
                Text(
                    text = "${index + 1}. $clue",
                    color = TileText,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TileDefault)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }

            if (gameState.guesses.isNotEmpty()) {
                Text(
                    text = "Twoje typy: " + gameState.guesses.joinToString(", "),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 8.dp)
                )
            }
        }

        if (active) {
            GuessInput(
                query = query,
                suggestions = suggestions,
                hasNextClue = hasNextClue,
                extraChanceUsed = gameState.extraChanceUsed,
                onQueryChange = onQueryChange,
                onGuess = onGuess,
                onNextClue = onNextClue
            )
        } else {
            Button(
                onClick = onShowResult,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("WYNIK", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun Silhouette(revealedName: String?) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .size(96.dp)
            .clip(CircleShape)
            .background(TileDefault),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier.size(84.dp)
        )
        if (revealedName == null) {
            Text("?", color = GuessArtistAccent, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }
    }
    if (revealedName != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(revealedName, color = GuessArtistAccent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GuessInput(
    query: String,
    suggestions: List<String>,
    hasNextClue: Boolean,
    extraChanceUsed: Boolean,
    onQueryChange: (String) -> Unit,
    onGuess: (String) -> Unit,
    onNextClue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 192.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TileDefault)
                    .verticalScroll(rememberScrollState())
            ) {
                suggestions.forEach { artist ->
                    Text(
                        text = artist,
                        color = TileText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGuess(artist) }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        } else if (query.isNotBlank()) {
            Text(
                text = "Nikogo takiego nie ma w grze",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TileDefault)
                .padding(horizontal = 12.dp),
            textStyle = TextStyle(color = TileText, fontSize = 16.sp, fontWeight = FontWeight.Medium),
            singleLine = true,
            cursorBrush = SolidColor(GuessArtistAccent),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text("Wpisz artystę lub skład", color = TextSecondary, fontSize = 15.sp)
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        when {
            hasNextClue -> TextButton(
                onClick = onNextClue,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("NASTĘPNY TROP", color = GuessArtistAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            else -> Text(
                text = if (extraChanceUsed) "Dodatkowa szansa: ostatnia próba" else "Ostatni trop: ostatnia próba",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun ExtraChanceDialog(onWatchAd: () -> Unit, onGiveUp: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* non-dismissable */ },
        containerColor = TileDefault,
        title = { Text("Nie udało się!", color = TileText, fontWeight = FontWeight.Bold) },
        text = { Text("Chcesz obejrzeć reklamę za jeszcze jedną próbę?", color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onWatchAd,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("OGLĄDAM", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onGiveUp) {
                Text("PODDAJĘ SIĘ", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ResultDialog(
    gameState: GuessArtistGameState,
    onDismiss: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val won = gameState.status == GameStatus.WON
    val total = gameState.puzzle.clues.size

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundDark, RoundedCornerShape(20.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (won) Icons.Default.Celebration else Icons.Default.SentimentDissatisfied,
                    contentDescription = null,
                    tint = if (won) LetterCorrect else Color(0xFFEF4444),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (won) "Brawo!" else "Przegrana",
                    color = if (won) LetterCorrect else Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }

            Text(
                text = gameState.puzzle.artist,
                color = GuessArtistAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (won) "Zgadnięte przy tropie ${gameState.revealedClues}/$total" else "Tym razem się nie udało",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Kim jestem?", buildGuessArtistShareText(gameState)))
                    Toast.makeText(context, "Skopiowano do schowka!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text("UDOSTĘPNIJ", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = TileText)
            }

            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text("WRÓĆ DO MENU", color = TileText, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}
