package com.dailyrap.app.ui.screen.help.tutorial

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText
import kotlinx.coroutines.launch

private val SecretPurple = Color(0xFF8B5CF6)
private val CorrectGreen = Color(0xFF22C55E)
private val CellGray = Color(0xFF3F3F46)

@Composable
fun RapCrosswordTutorial(onClose: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (page) {
                    0 -> CrosswordStep1()
                    1 -> CrosswordStep2()
                    2 -> CrosswordStep3()
                }
            }
        }

        PageIndicator(
            pageCount = 3,
            currentPage = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        if (pagerState.currentPage < 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onClose,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                ) {
                    Text("Pomiń", color = TextSecondary)
                }
                Button(
                    onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TileDefault)
                ) {
                    Text("Dalej", color = TileText, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TileDefault)
            ) {
                Text("Zamknij", color = TileText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CrosswordStep1() {
    TutorialTitle("Rozwiąż rapową krzyżówkę i odkryj ukryte hasło!")
    Spacer(modifier = Modifier.height(20.dp))

    // Mini crossword example — secret word horizontal with purple highlight
    val secretWord = listOf("P", "R", "A", "L", "K", "A")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        secretWord.forEach { letter ->
            MiniCell(letter = letter, color = SecretPurple)
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Show some vertical letters below
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Ł", "", "N", "", "U", "").forEachIndexed { _, l ->
            if (l.isNotEmpty()) MiniCell(letter = l, color = TileDefault)
            else Spacer(modifier = Modifier.size(32.dp))
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("O", "", "Y", "", "L", "").forEachIndexed { _, l ->
            if (l.isNotEmpty()) MiniCell(letter = l, color = TileDefault)
            else Spacer(modifier = Modifier.size(32.dp))
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("T", "", "Ż", "", "A", "").forEachIndexed { _, l ->
            if (l.isNotEmpty()) MiniCell(letter = l, color = TileDefault)
            else Spacer(modifier = Modifier.size(32.dp))
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    TutorialDescription("Hasło końcowe ukryte jest w fioletowym rzędzie")
}

@Composable
private fun CrosswordStep2() {
    TutorialTitle("Kliknij numer pytania, przeczytaj wskazówkę i wpisz odpowiedź")
    Spacer(modifier = Modifier.height(20.dp))

    // Example question
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TileDefault)
            .padding(12.dp)
    ) {
        Text(
            text = "1. Zatopiony lub rozbity statek",
            color = TileText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Example answer — green cells
    ExampleWordRow(
        letters = listOf("W", "R", "A", "K"),
        colors = listOf(CorrectGreen, CorrectGreen, CorrectGreen, CorrectGreen)
    )

    Spacer(modifier = Modifier.height(16.dp))
    TutorialDescription("\u2705 Poprawna odpowiedź — wiersz podświetla się na zielono i odsłania literę hasła")
}

@Composable
private fun CrosswordStep3() {
    TutorialTitle("Odpowiedz na wszystkie pytania i odkryj ukryte hasło!")
    Spacer(modifier = Modifier.height(20.dp))

    // Filled crossword
    val secretWord = listOf("P", "R", "A", "L", "K", "A")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        secretWord.forEach { letter ->
            MiniCell(letter = letter, color = SecretPurple)
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("Ł", "", "N", "", "U", "").forEach { l ->
            if (l.isNotEmpty()) MiniCell(letter = l, color = CorrectGreen)
            else Spacer(modifier = Modifier.size(32.dp))
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("O", "", "Y", "", "L", "").forEach { l ->
            if (l.isNotEmpty()) MiniCell(letter = l, color = CorrectGreen)
            else Spacer(modifier = Modifier.size(32.dp))
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("T", "", "Ż", "", "A", "").forEach { l ->
            if (l.isNotEmpty()) MiniCell(letter = l, color = CorrectGreen)
            else Spacer(modifier = Modifier.size(32.dp))
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    TutorialDescription("Twój wynik to liczba poprawnych odpowiedzi. Ukryte hasło to bonus! \uD83E\uDDE9")
}

@Composable
private fun MiniCell(letter: String, color: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = if (color == SecretPurple) Color.White else TileText,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
