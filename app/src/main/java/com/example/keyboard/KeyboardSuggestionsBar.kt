package com.example.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardTheme

@Composable
fun KeyboardSuggestionsBar(
    theme: KeyboardTheme,
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    if (suggestions.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val left = suggestions.getOrNull(0) ?: ""
        val center = suggestions.getOrNull(1) ?: (suggestions.getOrNull(0) ?: "")
        val right = suggestions.getOrNull(2) ?: ""

        // Left suggestion
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onSuggestionClick(left) }
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⟨ $left ⟩",
                color = Color(theme.subtextColor),
                fontSize = 13.sp
            )
        }

        // Center highlighted suggestion
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(theme.enterButtonColor).copy(alpha = 0.35f))
                .border(1.dp, Color(theme.accentColor).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable { onSuggestionClick(center) }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = center,
                color = Color(theme.keyTextColor),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Right suggestion
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onSuggestionClick(right) }
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⟨ $right ⟩",
                color = Color(theme.subtextColor),
                fontSize = 13.sp
            )
        }
    }
}
