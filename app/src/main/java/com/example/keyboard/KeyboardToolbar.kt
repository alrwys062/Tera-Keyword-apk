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
            .height(52.dp)
            .background(Color(theme.toolbarColor))
            .horizontalScroll(scrollState)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. إيموجي (Emoji)
        val isEmojiActive = activeSubView == KeyboardSubView.EMOJI
        ToolbarIconItem(
            icon = Icons.Outlined.Mood,
            label = "إيموجي",
            isActive = isEmojiActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isEmojiActive) KeyboardSubView.NONE else KeyboardSubView.EMOJI)
            }
        )

        // 2. GIF
        val isGifActive = activeSubView == KeyboardSubView.GIF
        ToolbarIconItem(
            icon = Icons.Outlined.Gif,
            label = "GIF",
            isActive = isGifActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isGifActive) KeyboardSubView.NONE else KeyboardSubView.GIF)
            }
        )

        // 3. صور والملصقات (Photos / Stickers)
        val isPhotosActive = activeSubView == KeyboardSubView.PHOTOS
        ToolbarIconItem(
            icon = Icons.Outlined.PhotoLibrary,
            label = "صور",
            isActive = isPhotosActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isPhotosActive) KeyboardSubView.NONE else KeyboardSubView.PHOTOS)
            }
        )

        // 4. ترجمة (Translation - with exact cyan rounded border when active, matching Screenshot 1)
        ToolbarIconItem(
            icon = Icons.Default.Translate,
            label = "ترجمة",
            isActive = isTranslationActive,
            theme = theme,
            onClick = onToggleTranslationBar
        )

        // 5. حافظة (Clipboard)
        val isClipboardActive = activeSubView == KeyboardSubView.CLIPBOARD
        ToolbarIconItem(
            icon = Icons.Outlined.ContentPaste,
            label = "حافظة",
            isActive = isClipboardActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isClipboardActive) KeyboardSubView.NONE else KeyboardSubView.CLIPBOARD)
            }
        )

        // 6. زخرفة (Decoration)
        ToolbarIconItem(
            icon = Icons.Outlined.AutoAwesome,
            label = "زخرفة",
            isActive = isDecorationActive,
            theme = theme,
            onClick = onToggleDecorationBar
        )

        // 7. ذكاء ونبرة (AI Tone & Smart Assistant)
        val isAiActive = activeSubView == KeyboardSubView.AI_ASSISTANT
        ToolbarIconItem(
            icon = Icons.Outlined.Psychology,
            label = "ذكاء ونبرة",
            isActive = isAiActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isAiActive) KeyboardSubView.NONE else KeyboardSubView.AI_ASSISTANT)
            }
        )

        // 8. صوت (Voice)
        ToolbarIconItem(
            icon = Icons.Default.Mic,
            label = "صوت",
            isActive = false,
            theme = theme,
            onClick = onVoiceClick
        )

        // 9. ليلي (Night Mode toggle)
        ToolbarIconItem(
            icon = Icons.Outlined.DarkMode,
            label = "ليلي",
            isActive = isNightMode,
            theme = theme,
            onClick = onToggleNightMode
        )

        // 10. إعدادات (Settings)
        ToolbarIconItem(
            icon = Icons.Outlined.Settings,
            label = "إعدادات",
            isActive = false,
            theme = theme,
            onClick = onOpenSettingsClick
        )
    }
}

@Composable
private fun ToolbarIconItem(
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
            .padding(horizontal = 2.dp)
            .height(46.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) activeColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 2.dp),
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
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isActive) activeColor else inactiveTextColor,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
