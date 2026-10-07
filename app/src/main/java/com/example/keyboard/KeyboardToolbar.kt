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
import androidx.compose.runtime.remember
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

private fun isLightColor(colorLong: Long): Boolean {
    val r = ((colorLong shr 16) and 0xFF) / 255.0
    val g = ((colorLong shr 8) and 0xFF) / 255.0
    val b = (colorLong and 0xFF) / 255.0
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance > 0.5
}

@Composable
fun KeyboardToolbar(
    theme: KeyboardTheme,
    activeSubView: KeyboardSubView,
    isDecorationActive: Boolean,
    isTranslationActive: Boolean,
    isNightMode: Boolean = true,
    visibleTools: List<String> = emptyList(),
    onSubViewSelected: (KeyboardSubView) -> Unit,
    onToggleDecorationBar: () -> Unit,
    onToggleTranslationBar: () -> Unit,
    onToggleNightMode: () -> Unit,
    onVoiceClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onCustomizeToolbar: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val toolsToShow = if (visibleTools.isEmpty()) {
        listOf("stickers", "translate", "clipboard", "decoration", "phrases", "calculator", "emoji", "voice", "ai", "photos", "gif", "settings")
    } else {
        visibleTools
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color(theme.toolbarColor))
            .horizontalScroll(scrollState)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 0. ملصقات (Stickers & Designer)
        if ("stickers" in toolsToShow) {
            val isStickersActive = activeSubView == KeyboardSubView.STICKERS
            ToolbarFixedItem(
                icon = Icons.Outlined.AutoAwesomeMosaic,
                label = "ملصقات",
                isActive = isStickersActive,
                theme = theme,
                onClick = {
                    onSubViewSelected(if (isStickersActive) KeyboardSubView.NONE else KeyboardSubView.STICKERS)
                }
            )
        }

        // 1. ترجمة (Translation)
        if ("translate" in toolsToShow) {
            val isTranslateActive = isTranslationActive || activeSubView == KeyboardSubView.TRANSLATE
            ToolbarFixedItem(
                icon = Icons.Default.Translate,
                label = "ترجمة",
                isActive = isTranslateActive,
                theme = theme,
                onClick = onToggleTranslationBar
            )
        }

        // 2. حافظة (Clipboard)
        if ("clipboard" in toolsToShow) {
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
        }

        // 3. زخرفة (Decoration)
        if ("decoration" in toolsToShow) {
            val isDecorActive = isDecorationActive || activeSubView == KeyboardSubView.DECORATION
            ToolbarFixedItem(
                icon = Icons.Outlined.AutoAwesome,
                label = "زخرفة",
                isActive = isDecorActive,
                theme = theme,
                onClick = onToggleDecorationBar
            )
        }

        // 4. كليشات وعبارات (Decorated Phrases)
        if ("phrases" in toolsToShow) {
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
        }

        // 5. حاسبة (Calculator)
        if ("calculator" in toolsToShow) {
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
        }

        // 6. إيموجي (Emoji)
        if ("emoji" in toolsToShow) {
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
        }

        // 7. صوت (Voice Input)
        if ("voice" in toolsToShow) {
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
        }

        // 8. ذكاء ونبرة (AI Tone & Smart Assistant)
        if ("ai" in toolsToShow) {
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
        }

        // 9. صور والملصقات (Photos / Stickers)
        if ("photos" in toolsToShow) {
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
        }

        // 10. GIF
        if ("gif" in toolsToShow) {
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
        }

        // 12. إعدادات (Settings)
        if ("settings" in toolsToShow) {
            val isSettingsActive = activeSubView == KeyboardSubView.SETTINGS
            ToolbarFixedItem(
                icon = Icons.Outlined.Settings,
                label = "إعدادات",
                isActive = isSettingsActive,
                theme = theme,
                onClick = {
                    onSubViewSelected(if (isSettingsActive) KeyboardSubView.NONE else KeyboardSubView.SETTINGS)
                }
            )
        }

        // 13. تخصيص الأدوات (Customize Tools button always available at end)
        val isCustomizeActive = activeSubView == KeyboardSubView.CUSTOMIZE_TOOLBAR
        ToolbarFixedItem(
            icon = Icons.Outlined.Tune,
            label = "تخصيص",
            isActive = isCustomizeActive,
            theme = theme,
            onClick = onCustomizeToolbar
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
    val isLight = remember(theme.toolbarColor) { isLightColor(theme.toolbarColor) }
    val inactiveTextColor = remember(theme.toolbarColor, isLight) {
        if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    }

    Box(
        modifier = Modifier
            .widthIn(min = 48.dp, max = 56.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeColor.copy(alpha = 0.25f) else Color.Transparent)
            .border(
                width = if (isActive) 1.2.dp else 0.dp,
                color = if (isActive) activeColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else inactiveTextColor.copy(alpha = 0.90f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = label,
                color = if (isActive) activeColor else inactiveTextColor,
                fontSize = 9.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
