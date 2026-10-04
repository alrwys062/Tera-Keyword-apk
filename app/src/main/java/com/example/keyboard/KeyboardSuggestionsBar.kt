package com.example.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PredictionEngine
import com.example.model.KeyboardTheme

@Composable
fun KeyboardSuggestionsBar(
    theme: KeyboardTheme,
    suggestions: List<PredictionEngine.PredictionResult>,
    onSuggestionClick: (String) -> Unit,
    onAddWordToDictionary: ((String) -> Unit)? = null
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
        val left = suggestions.getOrNull(0)
        val center = suggestions.getOrNull(1) ?: left
        val right = suggestions.getOrNull(2)

        // Left suggestion
        if (left != null) {
            SuggestionItem(
                result = left,
                theme = theme,
                isCenter = false,
                modifier = Modifier.weight(1f),
                onClick = { onSuggestionClick(left.word) },
                onAddWord = onAddWordToDictionary
            )
        }

        // Center highlighted suggestion / Correction
        if (center != null) {
            SuggestionItem(
                result = center,
                theme = theme,
                isCenter = true,
                modifier = Modifier.weight(1.35f),
                onClick = { onSuggestionClick(center.word) },
                onAddWord = onAddWordToDictionary
            )
        }

        // Right suggestion
        if (right != null) {
            SuggestionItem(
                result = right,
                theme = theme,
                isCenter = false,
                modifier = Modifier.weight(1f),
                onClick = { onSuggestionClick(right.word) },
                onAddWord = onAddWordToDictionary
            )
        }
    }
}

@Composable
private fun SuggestionItem(
    result: PredictionEngine.PredictionResult,
    theme: KeyboardTheme,
    isCenter: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onAddWord: ((String) -> Unit)?
) {
    if (result.isCustomCandidate && onAddWord != null) {
        // Quick add to dictionary button
        Box(
            modifier = modifier
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .clickable { onAddWord(result.word) }
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Word",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "حفظ: ${result.word}",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        val activeBg = Color(theme.accentColor).copy(alpha = if (isCenter) 0.25f else 0.1f)
        val activeBorder = if (isCenter) Color(theme.accentColor) else Color.Transparent

        Box(
            modifier = modifier
                .fillMaxHeight()
                .padding(horizontal = 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(activeBg)
                .border(1.dp, activeBorder, RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result.word,
                color = if (isCenter) Color(theme.accentColor) else Color(theme.keyTextColor),
                fontSize = if (isCenter) 14.sp else 13.sp,
                fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
