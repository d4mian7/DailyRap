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
import com.dailyrap.app.ui.theme.CategoryEasy
import com.dailyrap.app.ui.theme.CategoryHard
import com.dailyrap.app.ui.theme.CategoryMedium
import com.dailyrap.app.ui.theme.CategoryTricky
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText
import kotlinx.coroutines.launch

@Composable
fun RapConnectTutorial(onClose: () -> Unit) {
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
                    0 -> ConnectStep1()
                    1 -> ConnectStep2()
                    2 -> ConnectStep3()
                }
            }
        }

        // Bottom: indicators + buttons
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
private fun ConnectStep1() {
    TutorialTitle("Widzisz 16 słów na ekranie")
    Spacer(modifier = Modifier.height(8.dp))
    TutorialDescription("Musisz pogrupować je w 4 ukryte kategorie po 4 słowa.")
    Spacer(modifier = Modifier.height(24.dp))

    // Example 4x4 grid
    val words = listOf("KOŁO", "KWADRAT", "TRAPEZ", "PROSTOKĄT", "JABŁKO", "GRUSZKA", "ŚLIWKA", "BANAN",
        "ZIEMNIAK", "OGÓREK", "CEBULA", "MARCHEWKA", "CZAPKA", "SPODNIE", "BUTY", "KOSZULKA")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0..3) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                for (col in 0..3) {
                    val word = words[row * 4 + col]
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TileDefault),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(word, color = TileText, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    TutorialDescription("Zaznacz 4 słowa i kliknij SPRAWDŹ")
}

@Composable
private fun ConnectStep2() {
    TutorialTitle("Po sprawdzeniu:")
    Spacer(modifier = Modifier.height(20.dp))

    ExampleCategoryBar(
        name = "FIGURY",
        words = "KOŁO, KWADRAT, TRAPEZ, PROSTOKĄT",
        color = CategoryEasy
    )

    Spacer(modifier = Modifier.height(12.dp))
    TutorialDescription("\u2705 Jeśli trafisz — kategoria się odsłania w kolorze")

    Spacer(modifier = Modifier.height(20.dp))
    TutorialDescription("\u274C Jeśli pudło — tracisz jedną z 4 prób")
}

@Composable
private fun ConnectStep3() {
    TutorialTitle("Masz 4 próby na błąd")
    Spacer(modifier = Modifier.height(8.dp))
    TutorialDescription("Odgadnij wszystkie 4 kategorie żeby wygrać!")
    Spacer(modifier = Modifier.height(20.dp))

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ExampleCategoryBar("FIGURY", "KOŁO, KWADRAT, TRAPEZ, PROSTOKĄT", CategoryEasy)
        ExampleCategoryBar("OWOCE", "JABŁKO, GRUSZKA, ŚLIWKA, BANAN", CategoryMedium)
        ExampleCategoryBar("WARZYWA", "ZIEMNIAK, OGÓREK, CEBULA, MARCHEWKA", CategoryHard)
        ExampleCategoryBar("CZĘŚCI GARDEROBY", "CZAPKA, SPODNIE, BUTY, KOSZULKA", CategoryTricky)
    }

    Spacer(modifier = Modifier.height(20.dp))
    TutorialDescription("Po grze możesz udostępnić wynik znajomym \uD83C\uDFA4")
}
