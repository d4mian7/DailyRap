package com.dailyrap.app.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.BorderSubtle
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileSelected
import com.dailyrap.app.ui.theme.TileText
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    onNavigateToConnections: () -> Unit,
    onNavigateToLiteralnie: () -> Unit,
    onNavigateToCrossword: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToHelp: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    var secondsLeft by remember { mutableLongStateOf(uiState.secondsUntilNextGame) }
    LaunchedEffect(uiState.secondsUntilNextGame) {
        secondsLeft = uiState.secondsUntilNextGame
        while (secondsLeft > 0) {
            delay(1000L)
            secondsLeft--
        }
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(initialAlpha = 0f)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mascot logo
                    Image(
                        painter = painterResource(id = R.drawable.mascot),
                        contentDescription = "DailyRap Mascot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentScale = ContentScale.Fit
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GameTile(
                        icon = Icons.Default.Headphones,
                        title = "RAPCONNECT",
                        subtitle = if (uiState.connectionsPlayed) "Zagrane!" else "Połącz 16 słów w 4 ukryte grupy",
                        streak = uiState.connectionsStreak,
                        played = uiState.connectionsPlayed,
                        gradientColors = listOf(TileDefault, Color(0xFF352F50)),
                        accentColor = TileSelected,
                        onClick = onNavigateToConnections
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GameTile(
                        icon = Icons.Default.Abc,
                        title = "RAPWORD",
                        subtitle = if (uiState.literalniePlayed) "Zagrane!" else "Odgadnij słowo w 6 próbach",
                        streak = uiState.literalnieStreak,
                        played = uiState.literalniePlayed,
                        gradientColors = listOf(TileDefault, Color(0xFF1F3D2E)),
                        accentColor = LetterCorrect,
                        onClick = onNavigateToLiteralnie
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GameTile(
                        icon = Icons.Default.Extension,
                        title = "RAPCROSSWORD",
                        subtitle = if (uiState.crosswordPlayed) "Zagrane!" else "Rozwiąż krzyżówkę i odkryj hasło",
                        streak = uiState.crosswordStreak,
                        played = uiState.crosswordPlayed,
                        gradientColors = listOf(TileDefault, Color(0xFF3D2F1F)),
                        accentColor = LetterPresent,
                        onClick = onNavigateToCrossword
                    )

                    // Countdown timer
                    if (secondsLeft > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val hours = secondsLeft / 3600
                        val minutes = (secondsLeft % 3600) / 60
                        val secs = secondsLeft % 60
                        Text(
                            text = "Następna runda za",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format("%02d:%02d:%02d", hours, minutes, secs),
                            color = Color(0xFF9CA3AF),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                // Bottom navigation bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A2E))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToHelp() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = TileText,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(text = "Pomoc", color = Color(0xFF9CA3AF), fontSize = 10.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToStats() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = TileText,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(text = "Statystyki", color = Color(0xFF9CA3AF), fontSize = 10.sp)
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToSettings() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = TileText,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(text = "Ustawienia", color = Color(0xFF9CA3AF), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    streak: Int,
    played: Boolean,
    gradientColors: List<Color>,
    accentColor: Color,
    onClick: () -> Unit
) {
    val tileGradient = Brush.linearGradient(
        colors = gradientColors,
        start = Offset(0f, 0f),
        end = Offset(800f, 0f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .background(tileGradient)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = accentColor,
                        letterSpacing = 1.sp
                    )
                }
                if (played) {
                    StatusBadge(text = "OK", color = LetterCorrect)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 14.sp
            )

            if (streak > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Streak: $streak",
                        color = accentColor.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
