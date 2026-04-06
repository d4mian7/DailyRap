package com.dailyrap.app.ui.screen.help.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText

private val LetterGreen = Color(0xFF22C55E)
private val LetterYellow = Color(0xFFEAB308)
private val LetterGray = Color(0xFF3F3F46)

@Composable
fun ExampleWordRow(
    letters: List<String>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        letters.forEachIndexed { index, letter ->
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.getOrElse(index) { LetterGray }),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = TileText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if (index < letters.lastIndex) Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
fun ExampleCategoryBar(
    name: String,
    words: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(vertical = 10.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name\n$words",
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun TutorialDescription(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TutorialTitle(text: String) {
    Text(
        text = text,
        color = TileText,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        textAlign = TextAlign.Center,
        lineHeight = 24.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (index == currentPage) LetterCorrect else TextSecondary.copy(alpha = 0.4f))
            )
            if (index < pageCount - 1) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}
