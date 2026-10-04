package com.example.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardSubView
import com.example.model.KeyboardTheme

@Composable
fun KeyboardToolbar(
    theme: KeyboardTheme,
    activeSubView: KeyboardSubView,
    isDecorationActive: Boolean,
    isTranslationActive: Boolean,
    isNightMode: Boolean = true,
    onSubViewSelected: (KeyboardSubView) -> Unit,
    onToggleDecorationBar: () -> Unit,
    onToggleTranslationBar: () -> Unit,
    onToggleNightMode: () -> Unit,
    onVoiceClick: () -> Unit,
    onOpenSettingsClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .background(Color(theme.toolbarColor))
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. ترجمة (Translation)
        val isTranslateActive = activeSubView == KeyboardSubView.TRANSLATE || isTranslationActive
        ToolbarFixedItem(
            icon = Icons.Default.Translate,
            label = "ترجمة",
            isActive = isTranslateActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isTranslateActive) KeyboardSubView.NONE else KeyboardSubView.TRANSLATE)
                onToggleTranslationBar()
            }
        )

        // 2. حافظة (Clipboard)
        val isClipboardActive = activeSubView == KeyboardSubView.CLIPBOARD
        ToolbarFixedItem(
            icon = Icons.Outlined.ContentPaste,
            label = "حافظة",
            isActive = isClipboardActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isClipboardActive) KeyboardSubView.NONE else KeyboardSubView.CLIPBOARD)
            }
        )

        // 3. زخرفة (Decoration)
        val isDecorActive = activeSubView == KeyboardSubView.DECORATION || isDecorationActive
        ToolbarFixedItem(
            icon = Icons.Outlined.AutoAwesome,
            label = "زخرفة",
            isActive = isDecorActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isDecorActive) KeyboardSubView.NONE else KeyboardSubView.DECORATION)
                onToggleDecorationBar()
            }
        )

        // 4. كليشات وعبارات (Decorated Phrases)
        val isPhrasesActive = activeSubView == KeyboardSubView.PHRASES
        ToolbarFixedItem(
            icon = Icons.Outlined.FavoriteBorder,
            label = "كليشات",
            isActive = isPhrasesActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isPhrasesActive) KeyboardSubView.NONE else KeyboardSubView.PHRASES)
            }
        )

        // 5. حاسبة (Calculator)
        val isCalcActive = activeSubView == KeyboardSubView.CALCULATOR
        ToolbarFixedItem(
            icon = Icons.Outlined.Calculate,
            label = "حاسبة",
            isActive = isCalcActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isCalcActive) KeyboardSubView.NONE else KeyboardSubView.CALCULATOR)
            }
        )

        // 6. إيموجي (Emoji)
        val isEmojiActive = activeSubView == KeyboardSubView.EMOJI
        ToolbarFixedItem(
            icon = Icons.Outlined.Mood,
            label = "إيموجي",
            isActive = isEmojiActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isEmojiActive) KeyboardSubView.NONE else KeyboardSubView.EMOJI)
            }
        )

        // 5. صوت (Voice Input)
        val isVoiceActive = activeSubView == KeyboardSubView.VOICE_INPUT
        ToolbarFixedItem(
            icon = Icons.Default.Mic,
            label = "صوت",
            isActive = isVoiceActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isVoiceActive) KeyboardSubView.NONE else KeyboardSubView.VOICE_INPUT)
                onVoiceClick()
            }
        )

        // 6. ذكاء ونبرة (AI Tone & Smart Assistant)
        val isAiActive = activeSubView == KeyboardSubView.AI_ASSISTANT
        ToolbarFixedItem(
            icon = Icons.Outlined.Psychology,
            label = "ذكاء",
            isActive = isAiActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isAiActive) KeyboardSubView.NONE else KeyboardSubView.AI_ASSISTANT)
            }
        )

        // 7. صور والملصقات (Photos / Stickers)
        val isPhotosActive = activeSubView == KeyboardSubView.PHOTOS
        ToolbarFixedItem(
            icon = Icons.Outlined.PhotoLibrary,
            label = "صور",
            isActive = isPhotosActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isPhotosActive) KeyboardSubView.NONE else KeyboardSubView.PHOTOS)
            }
        )

        // 8. GIF
        val isGifActive = activeSubView == KeyboardSubView.GIF
        ToolbarFixedItem(
            icon = Icons.Outlined.Gif,
            label = "GIF",
            isActive = isGifActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isGifActive) KeyboardSubView.NONE else KeyboardSubView.GIF)
            }
        )

        // 9. ليلي (Night Mode toggle)
        ToolbarFixedItem(
            icon = Icons.Outlined.DarkMode,
            label = "ليلي",
            isActive = isNightMode,
            theme = theme,
            onClick = onToggleNightMode
        )

        // 10. إعدادات (Settings)
        val isSettingsActive = activeSubView == KeyboardSubView.SETTINGS
        ToolbarFixedItem(
            icon = Icons.Outlined.Settings,
            label = "إعدادات",
            isActive = isSettingsActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isSettingsActive) KeyboardSubView.NONE else KeyboardSubView.SETTINGS)
                onOpenSettingsClick()
            }
        )
    }
}

@Composable
private fun ToolbarFixedItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    theme: KeyboardTheme,
    onClick: () -> Unit
) {
    val activeColor = Color(theme.accentColor)
    val inactiveTextColor = Color(theme.keyTextColor).copy(alpha = 0.85f)

    Box(
        modifier = Modifier
            .width(54.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.22f) else Color.Transparent)
            .border(
                width = if (isActive) 1.2.dp else 0.dp,
                color = if (isActive) activeColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else inactiveTextColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isActive) activeColor else inactiveTextColor,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
