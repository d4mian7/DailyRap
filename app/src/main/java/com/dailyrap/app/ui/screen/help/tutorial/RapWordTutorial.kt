package com.dailyrap.app.ui.screen.help.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText
import kotlinx.coroutines.launch

private val LetterGreen = Color(0xFF22C55E)
private val LetterYellow = Color(0xFFEAB308)
private val LetterGray = Color(0xFF3F3F46)

@Composable
fun RapWordTutorial(onClose: () -> Unit) {
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
                    0 -> WordStep1()
                    1 -> WordStep2()
                    2 -> WordStep3()
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
private fun WordStep1() {
    TutorialTitle("Odgadnij dzisiejsze rapowe słowo na RapWord!")
    Spacer(modifier = Modifier.height(8.dp))
    TutorialDescription("Wpisz dowolne słowo i naciśnij OK, by spróbować odgadnąć hasło.")
    Spacer(modifier = Modifier.height(8.dp))
    TutorialDescription("Po każdej próbie, litery zostaną odpowiednio zaznaczone:")

    Spacer(modifier = Modifier.height(24.dp))

    ExampleWordRow(
        letters = listOf("K", "O", "L", "E", "J"),
        colors = listOf(LetterGray, LetterGreen, LetterGray, LetterGray, LetterGray)
    )

    Spacer(modifier = Modifier.height(16.dp))
    TutorialDescription("Jeśli litera podświetlona jest na zielono, występuje ona w haśle w tym samym miejscu")
}

@Composable
private fun WordStep2() {
    ExampleWordRow(
        letters = listOf("R", "O", "W", "E", "R"),
        colors = listOf(LetterGray, LetterGray, LetterYellow, LetterGray, LetterGray)
    )

    Spacer(modifier = Modifier.height(16.dp))
    TutorialDescription("Jeśli litera podświetlona jest na żółto, występuje ona w haśle, lecz w innym miejscu")
}

@Composable
private fun WordStep3() {
    ExampleWordRow(
        letters = listOf("K", "R", "O", "W", "A"),
        colors = listOf(LetterGray, LetterGray, LetterGray, LetterGray, LetterGray)
    )

    Spacer(modifier = Modifier.height(16.dp))
    TutorialDescription("Jeśli litera nie jest podświetlona, nie występuje w haśle")

    Spacer(modifier = Modifier.height(20.dp))
    TutorialDescription("Codziennie dostępne jest nowe hasło. Masz od 5 do 7 prób w zależności od długości hasła. Powodzenia!")
}
