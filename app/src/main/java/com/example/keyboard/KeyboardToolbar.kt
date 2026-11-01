package com.example.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardSubView
import com.example.model.KeyboardTheme

import androidx.compose.ui.input.pointer.PointerEventPass
import kotlin.math.abs

fun Modifier.edgeSwipeToBack(
    edgeThreshold: Dp = 48.dp,
    dragThreshold: Dp = 20.dp,
    enabled: Boolean = true,
    onSwipeBack: () -> Unit
): Modifier = if (!enabled) this else this.pointerInput(onSwipeBack) {
    awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial)
        val startX = down.position.x
        val edgePx = edgeThreshold.toPx()
        val dragPx = dragThreshold.toPx()
        val isNearLeftEdge = startX <= edgePx
        val isNearRightEdge = startX >= (size.width - edgePx)
        var backTriggered = false

        while (true) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break

            val deltaX = change.position.x - startX
            val absX = abs(deltaX)

            if (!backTriggered) {
                // Only strict horizontal inward swipe from left or right edge
                val isLeftInward = isNearLeftEdge && deltaX > dragPx && absX > dragPx
                val isRightInward = isNearRightEdge && deltaX < -dragPx && absX > dragPx

                if (isLeftInward || isRightInward) {
                    backTriggered = true
                    change.consume()
                    onSwipeBack()
                    break
                }
            }
        }
    }
}

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
    isForceEnglishNumbers: Boolean = true,
    isCompact: Boolean = false,
    toolbarHeight: Dp? = null,
    onToggleForceEnglishNumbers: (() -> Unit)? = null,
    onSubViewSelected: (KeyboardSubView) -> Unit,
    onToggleDecorationBar: () -> Unit,
    onToggleTranslationBar: () -> Unit,
    onTranslateLongPressEnter: (() -> Unit)? = null,
    onToggleNightMode: () -> Unit,
    onVoiceClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onCustomizeToolbar: () -> Unit = {},
    onSwipeBack: (() -> Unit)? = null,
    onMyThemeClick: (() -> Unit)? = null,
    onMyThemeLongClick: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val toolsToShow = if (visibleTools.isEmpty()) {
        listOf("stickers", "clipboard", "decoration", "phrases", "translate", "calculator", "emoji", "voice", "ai", "photos", "gif", "settings")
    } else {
        visibleTools
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val actualHeight = toolbarHeight ?: if (isLandscape) 32.dp else if (isCompact) 48.dp else 50.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(actualHeight)
            .background(Color(theme.toolbarColor))
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp, vertical = if (isLandscape) 1.dp else 2.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 0.2 "ثيماتي" My Theme Quick Reset
        if (onMyThemeClick != null) {
            ToolbarFixedItem(
                icon = Icons.Outlined.Palette,
                label = "ثيماتي",
                isActive = activeSubView == KeyboardSubView.THEMES,
                theme = theme,
                onClick = onMyThemeClick,
                onLongClick = onMyThemeLongClick,
                longClickDelayMs = 600L
            )
        }
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

        // 0.1 ملصقاتي الخاصة (My Stickers)
        if ("my_stickers" in toolsToShow) {
            val isMyStickersActive = activeSubView == KeyboardSubView.MY_STICKERS
            ToolbarFixedItem(
                icon = Icons.Outlined.PhotoFilter,
                label = "ملصقاتي",
                isActive = isMyStickersActive,
                theme = theme,
                onClick = {
                    onSubViewSelected(if (isMyStickersActive) KeyboardSubView.NONE else KeyboardSubView.MY_STICKERS)
                }
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

        // 2.1 تثبيت الأرقام الإنجليزية (English Numbers 123)
        if ("english_numbers" in toolsToShow) {
            ToolbarFixedItem(
                icon = Icons.Outlined.Pin,
                label = if (isForceEnglishNumbers) "أرقام 123 ✓" else "أرقام ١٢٣",
                isActive = isForceEnglishNumbers,
                theme = theme,
                onClick = {
                    onToggleForceEnglishNumbers?.invoke()
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

        // 1. ترجمة (Translation) - ضغطة سريعة تفتح/تغلق الترجمة، وضغطة مطولة 1.5 ثانية تعمل كزر Enter
        if ("translate" in toolsToShow) {
            val isTranslateActive = isTranslationActive || activeSubView == KeyboardSubView.TRANSLATE
            ToolbarFixedItem(
                icon = Icons.Default.Translate,
                label = "ترجمة",
                isActive = isTranslateActive,
                theme = theme,
                onClick = onToggleTranslationBar,
                onLongClick = onTranslateLongPressEnter,
                longClickDelayMs = 1500L
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
                }
            )
        }

        // 8. AI Keyboard: صحّح وأعد الصياغة وترجم واكتب
        if ("ai" in toolsToShow) {
            val isAiActive = activeSubView == KeyboardSubView.AI_ASSISTANT
            ToolbarFixedItem(
                icon = Icons.Outlined.AutoFixHigh,
                label = "AI ذكي",
                isActive = isAiActive,
                theme = theme,
                onClick = {
                    onSubViewSelected(if (isAiActive) KeyboardSubView.NONE else KeyboardSubView.AI_ASSISTANT)
                }
            )
        }

        // 8.1 اقرأ النص بصوت الجوال (TTS Speech Reader)
        if ("tts" in toolsToShow) {
            val isTtsActive = activeSubView == KeyboardSubView.TTS_SPEECH
            ToolbarFixedItem(
                icon = Icons.Outlined.VolumeUp,
                label = "قراءة",
                isActive = isTtsActive,
                theme = theme,
                onClick = {
                    onSubViewSelected(if (isTtsActive) KeyboardSubView.NONE else KeyboardSubView.TTS_SPEECH)
                }
            )
        }

        // 8.2 Keyboard Pass: كلمات المرور والبطاقات والآيبان
        if ("pass" in toolsToShow) {
            val isPassActive = activeSubView == KeyboardSubView.PASSWORD_MANAGER
            ToolbarFixedItem(
                icon = Icons.Outlined.Password,
                label = "Pass",
                isActive = isPassActive,
                theme = theme,
                onClick = {
                    onSubViewSelected(if (isPassActive) KeyboardSubView.NONE else KeyboardSubView.PASSWORD_MANAGER)
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

        // 13. قنوات تليجرام للتواصل
        val isTelegramActive = activeSubView == KeyboardSubView.TELEGRAM_CHANNELS
        ToolbarFixedItem(
            icon = Icons.AutoMirrored.Filled.Send,
            label = "تليجرام",
            isActive = isTelegramActive,
            theme = theme,
            onClick = {
                onSubViewSelected(if (isTelegramActive) KeyboardSubView.NONE else KeyboardSubView.TELEGRAM_CHANNELS)
            }
        )

        // 14. تخصيص الأدوات (Customize Tools button always available at end)
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
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    longClickDelayMs: Long = 500L
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val isToolbarLight = remember(theme.toolbarColor) { isLightColor(theme.toolbarColor) }
    
    val effectiveActiveColor = remember(theme.accentColor, isToolbarLight) {
        if (isToolbarLight && isLightColor(theme.accentColor)) {
            Color(0xFF1E40AF)
        } else if (!isToolbarLight && !isLightColor(theme.accentColor)) {
            Color(0xFF38BDF8)
        } else {
            Color(theme.accentColor)
        }
    }
    
    val inactiveTextColor = remember(theme.toolbarColor, theme.keyTextColor, isToolbarLight) {
        if (isToolbarLight) {
            Color(0xFF0F172A)
        } else {
            Color(0xFFF8FAFC)
        }
    }

    val itemModifier = Modifier
        .widthIn(min = if (isLandscape) 38.dp else 46.dp, max = if (isLandscape) 48.dp else 56.dp)
        .height(if (isLandscape) 28.dp else 46.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(if (isActive) effectiveActiveColor.copy(alpha = if (isToolbarLight) 0.18f else 0.28f) else Color.Transparent)
        .border(
            width = if (isActive) 1.2.dp else 0.dp,
            color = if (isActive) effectiveActiveColor else Color.Transparent,
            shape = RoundedCornerShape(8.dp)
        )
        .then(
            if (onLongClick != null) {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onLongClick() },
                        onTap = { onClick() }
                    )
                }
            } else {
                Modifier.clickable(onClick = onClick)
            }
        )
        .padding(horizontal = 2.dp, vertical = if (isLandscape) 1.dp else 2.dp)

    Box(
        modifier = itemModifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) effectiveActiveColor else inactiveTextColor.copy(alpha = 0.92f),
                modifier = Modifier.size(if (isLandscape) 14.dp else 19.dp)
            )
            Spacer(modifier = Modifier.height(if (isLandscape) 0.dp else 1.dp))
            Text(
                text = label,
                color = if (isActive) effectiveActiveColor else inactiveTextColor,
                fontSize = if (isLandscape) 7.5.sp else 9.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
