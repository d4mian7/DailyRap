package com.dailyrap.app.ui.screen.stats

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyrap.app.data.model.PlayerStats
import com.dailyrap.app.ui.theme.AccentPurple
import com.dailyrap.app.ui.theme.BackgroundDark
import com.dailyrap.app.ui.theme.BorderSubtle
import com.dailyrap.app.ui.theme.LetterCorrect
import com.dailyrap.app.ui.theme.LetterPresent
import com.dailyrap.app.ui.theme.TextSecondary
import com.dailyrap.app.ui.theme.TileDefault
import com.dailyrap.app.ui.theme.TileSelected
import com.dailyrap.app.ui.theme.TileText

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

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
                text = "Statystyki",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TileText
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundDark,
            contentColor = TileText,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AccentPurple
                    )
                }
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "RapConnect",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) TileSelected else TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "RapWord",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) LetterCorrect else TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "Crossword",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 2) LetterPresent else TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .animateContentSize()
        ) {
            when (selectedTab) {
                0 -> ConnectionsStatsContent(stats)
                1 -> LiteralnieStatsContent(stats)
                2 -> CrosswordStatsContent(stats)
            }
        }
    }
}

@Composable
private fun ConnectionsStatsContent(stats: PlayerStats) {
    val winPercent = if (stats.connectionsPlayed > 0) {
        (stats.connectionsWon * 100) / stats.connectionsPlayed
    } else 0

    StatCardsRow(
        played = stats.connectionsPlayed,
        winPercent = winPercent,
        streak = stats.connectionsStreak,
        bestStreak = stats.connectionsBestStreak,
        accentColor = TileSelected
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Separator
    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Histogram błędów",
        color = TileText,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    val maxCount = stats.connectionsMistakeHistogram.values.maxOrNull() ?: 1
    for (mistakes in 0..3) {
        val count = stats.connectionsMistakeHistogram[mistakes] ?: 0
        val label = when (mistakes) {
            0 -> "Bezbłędnie"
            1 -> "1 błąd"
            2 -> "2 błędy"
            else -> "3 błędy"
        }
        // "Bezbłędnie" bar is green (success), others purple
        val barColor = if (mistakes == 0) LetterCorrect else TileSelected
        HistogramBar(
            label = label,
            count = count,
            maxCount = maxCount,
            accentColor = barColor
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LiteralnieStatsContent(stats: PlayerStats) {
    val winPercent = if (stats.literalniePlayed > 0) {
        (stats.literalnieWon * 100) / stats.literalniePlayed
    } else 0

    StatCardsRow(
        played = stats.literalniePlayed,
        winPercent = winPercent,
        streak = stats.literalnieStreak,
        bestStreak = stats.literalnieBestStreak,
        accentColor = LetterCorrect
    )

    Spacer(modifier = Modifier.height(20.dp))

    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Histogram prób",
        color = TileText,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    val maxCount = (1..9).maxOf { stats.literalnieGuessHistogram[it] ?: 0 }.coerceAtLeast(1)

    for (guesses in 1..9) {
        val count = stats.literalnieGuessHistogram[guesses] ?: 0
        HistogramBar(
            label = "$guesses",
            count = count,
            maxCount = maxCount,
            accentColor = LetterCorrect
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CrosswordStatsContent(stats: PlayerStats) {
    val winPercent = if (stats.crosswordPlayed > 0) {
        (stats.crosswordWon * 100) / stats.crosswordPlayed
    } else 0

    StatCardsRow(
        played = stats.crosswordPlayed,
        winPercent = winPercent,
        streak = stats.crosswordStreak,
        bestStreak = stats.crosswordBestStreak,
        accentColor = LetterPresent
    )

    Spacer(modifier = Modifier.height(20.dp))

    HorizontalDivider(thickness = 1.dp, color = BorderSubtle)

    Spacer(modifier = Modifier.height(20.dp))

    Text(
        text = "Histogram poprawnych odpowiedzi",
        color = TileText,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    )

    Spacer(modifier = Modifier.height(16.dp))

    val maxCount = stats.crosswordCorrectHistogram.values.maxOrNull() ?: 1
    for (correct in 0..7) {
        val count = stats.crosswordCorrectHistogram[correct] ?: 0
        // Crossword bars are golden
        HistogramBar(
            label = "$correct/7",
            count = count,
            maxCount = maxCount,
            accentColor = LetterPresent
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatCardsRow(
    played: Int,
    winPercent: Int,
    streak: Int,
    bestStreak: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(icon = Icons.Default.SportsEsports, iconTint = TextSecondary, value = "$played", label = "Zagrane", modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Default.EmojiEvents, iconTint = LetterPresent, value = "$winPercent%", label = "Wygrane", modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Default.LocalFireDepartment, iconTint = Color(0xFFEF4444), value = "$streak", label = "Streak", color = accentColor, modifier = Modifier.weight(1f))
        StatCard(icon = Icons.Default.Star, iconTint = LetterPresent, value = "$bestStreak", label = "Max", color = LetterPresent, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = TileText,
    iconTint: Color = TextSecondary
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .background(TileDefault)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistogramBar(
    label: String,
    count: Int,
    maxCount: Int,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(80.dp)
        )

        val fraction = if (maxCount > 0) count.toFloat() / maxCount else 0f
        val minFraction = if (count > 0) 0.08f else 0.06f

        Box(
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceAtLeast(minFraction))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (count > 0) accentColor else TileDefault),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "$count",
                    color = if (count > 0) TileText else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
