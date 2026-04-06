package com.dailyrap.app.ui.screen.help

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileText

@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTutorial: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
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
                text = "Pomoc",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TileText
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Wybierz tryb gry, aby zobaczyć jak grać:",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            HelpTile(
                icon = "\uD83C\uDFA4",
                title = "RapConnect",
                gradientColors = listOf(TileDefault, Color(0xFF352F50)),
                onClick = { onNavigateToTutorial("rapconnect") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            HelpTile(
                icon = "\uD83D\uDD24",
                title = "RapWord",
                gradientColors = listOf(TileDefault, Color(0xFF1F3D2E)),
                onClick = { onNavigateToTutorial("rapword") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            HelpTile(
                icon = "\uD83E\uDDE9",
                title = "RapCrossword",
                gradientColors = listOf(TileDefault, Color(0xFF3D2F1F)),
                onClick = { onNavigateToTutorial("rapcrossword") }
            )
        }
    }
}

@Composable
private fun HelpTile(
    icon: String,
    title: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),
        end = Offset(800f, 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$icon  $title",
            color = TileText,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 1.sp
        )
    }
}
