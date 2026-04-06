package com.dailyrap.app.ui.screen.connections.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyrap.app.data.model.Category
import com.dailyrap.app.ui.theme.CategoryEasy
import com.dailyrap.app.ui.theme.CategoryHard
import com.dailyrap.app.ui.theme.CategoryMedium
import com.dailyrap.app.ui.theme.CategoryTricky

fun categoryColor(difficulty: Int): Color = when (difficulty) {
    0 -> CategoryEasy
    1 -> CategoryMedium
    2 -> CategoryHard
    3 -> CategoryTricky
    else -> CategoryEasy
}

@Composable
fun CategoryReveal(
    category: Category,
    modifier: Modifier = Modifier
) {
    val color = categoryColor(category.difficulty)

    AnimatedVisibility(
        visible = true,
        enter = expandVertically() + fadeIn()
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = category.name.uppercase(),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = category.words.joinToString(", "),
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
