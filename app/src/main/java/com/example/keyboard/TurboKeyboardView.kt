package com.example.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.data.InputLanguagesManager
import com.example.data.LongPressVariantsManager
import com.example.data.PredictionEngine
import com.example.data.PreferencesManager
import com.example.data.TextDecorator
import com.example.data.TranslationEngine
import com.example.data.WorldLanguage
import com.example.model.KeyboardLanguage
import com.example.model.KeyboardSettings
import com.example.model.KeyboardSubView
import com.example.model.KeyboardTheme
import com.example.model.ThemePresets
import com.example.sound.KeyboardSoundEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private fun isLightColor(colorLong: Long): Boolean {
    val r = ((colorLong shr 16) and 0xFF) / 255.0
    val g = ((colorLong shr 8) and 0xFF) / 255.0
    val b = (colorLong and 0xFF) / 255.0
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance > 0.5
}

@Composable
fun TurboKeyboardView(
    theme: KeyboardTheme = ThemePresets.CYBER_PRO,
    settings: KeyboardSettings = KeyboardSettings(),
    inputConnection: InputConnection? = null,
    getCurrentInputConnection: (() -> InputConnection?)? = null,
    onServiceDelete: (() -> Unit)? = null,
    onDirectInsertText: ((String) -> Unit)? = null,
    onDirectDeleteLastChar: (() -> Unit)? = null,
    onDirectClearAndReplaceText: ((String) -> Unit)? = null,
    getCurrentText: (() -> String)? = null,
    onVoiceRequested: (() -> Unit)? = null,
    onOpenSettingsRequested: (() -> Unit)? = null,
    prefsManager: PreferencesManager? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { prefsManager ?: PreferencesManager(context) }
    var currentSettings by remember { mutableStateOf(settings) }
    LaunchedEffect(settings) { currentSettings = settings }

    var currentLangCode by remember {
        mutableStateOf(InputLanguagesManager.getCurrentLanguageCode(context))
    }
    val currentWorldLang = remember(currentLangCode) {
        InputLanguagesManager.getLanguageByCode(currentLangCode)
    }
    val isArabic = currentWorldLang.code == "ar"

    var isShifted by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }
    var isMoreSymbolsMode by remember { mutableStateOf(false) }
    var activeSubView by remember { mutableStateOf(KeyboardSubView.NONE) }

    // Live Transboard features
    var activeDecorationStyle by remember { mutableStateOf(prefs.getActiveDecorationStyle()) }
    var isDecorationBarOpen by remember { mutableStateOf(false) }

    var isTranslationBarOpen by remember { mutableStateOf(false) }
    var translationSource by remember { mutableStateOf(currentSettings.translationSource) }
    var translationTarget by remember { mutableStateOf(currentSettings.translationTarget) }

    // Long-Press character variant popup state (Matching Screenshots 1, 2, 4)
    var longPressChar by remember { mutableStateOf<String?>(null) }
    var longPressVariants by remember { mutableStateOf<List<String>>(emptyList()) }

    // Live text tracking for prediction and special 4-sec translate
    var currentComposingText by remember { mutableStateOf("") }
    var totalDragX by remember { mutableFloatStateOf(0f) }

    // 4-sec Enter hold translation state
    var isEnterHolding by remember { mutableStateOf(false) }
    var enterHoldProgress by remember { mutableFloatStateOf(0f) }
    var enterHoldMessage by remember { mutableStateOf("") }

    val userDict = remember { com.example.data.UserDictionaryManager(context) }
    var userWords by remember { mutableStateOf(userDict.getUserWords()) }
    val shortcuts = remember { prefs.getShortcuts() }
    var activeTheme by remember(theme) {
        mutableStateOf(theme)
    }

    // Auto-return to letters keyboard on Send / Text Clear
    var prevComposingLength by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentComposingText) {
        if (prevComposingLength > 0 && currentComposingText.isEmpty()) {
            activeSubView = KeyboardSubView.NONE
            isSymbolsMode = false
            isMoreSymbolsMode = false
        }
        prevComposingLength = currentComposingText.length
    }

    LaunchedEffect(Unit) {
        TurboKeyboardService.resetToLettersSignal.collect { timestamp ->
            if (timestamp > 0) {
                activeSubView = KeyboardSubView.NONE
                isSymbolsMode = false
                isMoreSymbolsMode = false
                isDecorationBarOpen = false
                isTranslationBarOpen = false
            }
        }
    }

    // Prediction suggestions + auto-correction + shortcuts expansion
    val suggestions = remember(currentComposingText, currentWorldLang, userWords, shortcuts) {
        if (currentSettings.suggestionsEnabled) {
            val lastWord = currentComposingText.trim().split(" ").lastOrNull() ?: ""
            val shortcutMatch = shortcuts.find { it.trigger.equals(lastWord, ignoreCase = true) }
            val baseList = PredictionEngine.getPredictionsWithCorrection(lastWord, isArabic, userWords)
            if (shortcutMatch != null) {
                listOf(PredictionEngine.PredictionResult(shortcutMatch.expansion)) + baseList.take(2)
            } else {
                baseList
            }
        } else {
            emptyList()
        }
    }

    LaunchedEffect(Unit) {
        KeyboardSoundEngine.initialize(context)
    }

    // Cached vibrator reference to prevent heavy IPC getSystemService calls on every single keystroke
    val vibrator = remember {
        try {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } catch (_: Exception) {
            null
        }
    }

    // Instant tactile feedback and rich sound engine matching iOS 16 feel
    fun performFeedback(
        durationMs: Long = currentSettings.vibrationDurationMs.toLong(),
        isSpecial: Boolean = false,
        isSpace: Boolean = false,
        isDelete: Boolean = false
    ) {
        if (currentSettings.vibrationEnabled && vibrator != null && vibrator.hasVibrator()) {
            try {
                val duration = if (durationMs > 0) durationMs else currentSettings.vibrationDurationMs.toLong().coerceAtLeast(15L)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            } catch (_: Exception) {
                // Ignore
            }
        }
        if (currentSettings.soundEnabled) {
            KeyboardSoundEngine.playKeySound(
                profile = currentSettings.soundProfile,
                volume = currentSettings.soundVolume,
                isSpecial = isSpecial,
                isSpace = isSpace,
                isDelete = isDelete
            )
        }
    }

    fun sendText(text: String) {
        val textToInsert = if (activeDecorationStyle != "none" && text.length == 1) {
            TextDecorator.decorateChar(text, activeDecorationStyle)
        } else {
            text
        }
        currentComposingText = (currentComposingText + textToInsert).takeLast(80)
        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
        if (ic != null) {
            try {
                ic.commitText(textToInsert, 1)
            } catch (e: Exception) {
                try {
                    ic.commitText(textToInsert, 1)
                } catch (_: Exception) {}
            }
        } else {
            onDirectInsertText?.invoke(textToInsert)
        }
    }

    fun sendDelete() {
        performFeedback(isDelete = true)
        if (currentComposingText.isNotEmpty()) {
            currentComposingText = currentComposingText.dropLast(1)
        }
        if (onServiceDelete != null) {
            onServiceDelete.invoke()
        } else {
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            if (ic != null) {
                try {
                    val selected = ic.getSelectedText(0)
                    if (!selected.isNullOrEmpty()) {
                        ic.commitText("", 1)
                    } else {
                        val before = ic.getTextBeforeCursor(1, 0)
                        if (!before.isNullOrEmpty()) {
                            val deleted = ic.deleteSurroundingText(1, 0)
                            if (!deleted) {
                                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                            }
                        } else {
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                        }
                    }
                } catch (e: Exception) {
                    try {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                    } catch (_: Exception) {}
                }
            } else {
                onDirectDeleteLastChar?.invoke()
            }
        }
    }

    fun sendEnter() {
        performFeedback(isSpecial = true)
        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
        if (ic != null) {
            try {
                // First try performEditorAction with common IME actions for chat/messaging apps
                var handled = false
                try {
                    handled = ic.performEditorAction(EditorInfo.IME_ACTION_SEND) ||
                              ic.performEditorAction(EditorInfo.IME_ACTION_GO) ||
                              ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
                } catch (_: Exception) {}
                if (!handled) {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            } catch (e: Exception) {
                try {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                } catch (_: Exception) {
                    try {
                        ic.commitText("\n", 1)
                    } catch (_: Exception) {}
                }
            }
        } else {
            onDirectInsertText?.invoke("\n")
        }
        currentComposingText = ""
        // Crucial fix: Automatically return to letters keyboard on Send / Enter
        if (currentSettings.autoReturnToLettersOnSend) {
            activeSubView = KeyboardSubView.NONE
            isSymbolsMode = false
            isMoreSymbolsMode = false
        }
    }

    fun replaceAllText(newText: String) {
        currentComposingText = newText.takeLast(80)
        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
        if (ic != null) {
            try {
                ic.beginBatchEdit()
                val before = ic.getTextBeforeCursor(2000, 0)?.length ?: 0
                val after = ic.getTextAfterCursor(2000, 0)?.length ?: 0
                ic.deleteSurroundingText(before, after)
                ic.commitText(newText, 1)
                ic.endBatchEdit()
            } catch (e: Exception) {
                try {
                    ic.commitText(newText, 1)
                } catch (_: Exception) {}
            }
        } else {
            onDirectClearAndReplaceText?.invoke(newText)
        }
    }

    // Special 4-second hold translation function requested by user
    fun triggerInstantTranslate() {
        coroutineScope.launch {
            val textToTranslate = getCurrentText?.invoke()?.ifBlank { currentComposingText } ?: currentComposingText
            if (textToTranslate.isNotBlank()) {
                enterHoldMessage = "جاري الترجمة الفورية..."
                val source = if (isArabic) "ar" else "en"
                val target = if (isArabic) "en" else "ar"
                val result = TranslationEngine.translate(textToTranslate, source, target)
                replaceAllText(result)
                enterHoldMessage = "تمت الترجمة بنجاح ✓"
                delay(1200)
                enterHoldMessage = ""
            }
        }
    }

    val bgModifier = remember(theme) {
        if (!theme.backgroundGradient.isNullOrEmpty()) {
            Modifier.background(Brush.verticalGradient(theme.backgroundGradient.map { Color(it) }))
        } else {
            Modifier.background(Color(theme.backgroundColor))
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (!theme.backgroundImageUri.isNullOrBlank()) {
            AsyncImage(
                model = theme.backgroundImageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = theme.backgroundDim))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (theme.backgroundImageUri.isNullOrBlank()) bgModifier else Modifier)
                .navigationBarsPadding()
        ) {
        // Top status/alert banner when 4-sec translate is active
        AnimatedVisibility(visible = enterHoldMessage.isNotEmpty() || enterHoldProgress > 0.1f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(theme.accentColor).copy(alpha = 0.2f))
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (enterHoldProgress in 0.05f..0.99f) {
                        CircularProgressIndicator(
                            progress = { enterHoldProgress },
                            modifier = Modifier.size(16.dp),
                            color = Color(theme.accentColor),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "اضغط مع الاستمرار 4 ثوانٍ لترجمة النص... (${(enterHoldProgress * 4).toInt()}/4s)",
                            color = Color(theme.accentColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (enterHoldMessage.isNotEmpty()) {
                        Text(
                            text = enterHoldMessage,
                            color = Color(theme.accentColor),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 1. TOP TOOLBAR matching previous Transboard design
        KeyboardToolbar(
            theme = activeTheme,
            activeSubView = activeSubView,
            isDecorationActive = isDecorationBarOpen || activeDecorationStyle != "none",
            isTranslationActive = isTranslationBarOpen,
            isNightMode = true,
            visibleTools = currentSettings.visibleToolbarTools,
            onSubViewSelected = { sub ->
                if (activeSubView == sub) {
                    activeSubView = KeyboardSubView.NONE
                } else {
                    activeSubView = sub
                    isDecorationBarOpen = false
                    isTranslationBarOpen = false
                }
            },
            onToggleDecorationBar = {
                activeSubView = KeyboardSubView.NONE
                isTranslationBarOpen = false
                isDecorationBarOpen = !isDecorationBarOpen
            },
            onToggleTranslationBar = {
                activeSubView = KeyboardSubView.NONE
                isDecorationBarOpen = false
                isTranslationBarOpen = !isTranslationBarOpen
            },
            onToggleNightMode = {},
            onVoiceClick = { onVoiceRequested?.invoke() },
            onOpenSettingsClick = { onOpenSettingsRequested?.invoke() },
            onCustomizeToolbar = {
                activeSubView = if (activeSubView == KeyboardSubView.CUSTOMIZE_TOOLBAR) KeyboardSubView.NONE else KeyboardSubView.CUSTOMIZE_TOOLBAR
                isDecorationBarOpen = false
                isTranslationBarOpen = false
            }
        )

        // 2. INLINE TRANSLATION BAR (Instant display without jank)
        if (isTranslationBarOpen) {
            InlineTranslationBar(
                theme = activeTheme,
                sourceLang = translationSource,
                targetLang = translationTarget,
                onSwapLanguages = {
                    val tmp = translationSource
                    translationSource = translationTarget
                    translationTarget = tmp
                },
                onSelectSourceLang = { translationSource = it },
                onSelectTargetLang = { translationTarget = it },
                onClose = { isTranslationBarOpen = false }
            )
        }

        // 3. INLINE TEXT DECORATION BAR & DROPDOWN (Instant display)
        if (isDecorationBarOpen) {
            InlineDecorationBar(
                theme = activeTheme,
                activeStyleId = activeDecorationStyle,
                onSelectStyle = { styleId ->
                    activeDecorationStyle = styleId
                    prefs.setActiveDecorationStyle(styleId)
                },
                onClose = {
                    isDecorationBarOpen = false
                    activeDecorationStyle = "none"
                    prefs.setActiveDecorationStyle("none")
                }
            )
        }

        // Sub-views drawers (Stickers, Clipboard, Emoji, GIF, AI Assistant, Photos, Customize Toolbar)
        // Instant 0ms display: completely solves lag when opening clipboard or translation
        if (activeSubView != KeyboardSubView.NONE) {
            Box(modifier = Modifier.fillMaxWidth()) {
                when (activeSubView) {
                    KeyboardSubView.STICKERS -> {
                        StickersPickerView(
                            theme = activeTheme,
                            prefs = prefs,
                            onStickerSelected = { sticker ->
                                sendText(" $sticker ")
                                if (currentSettings.autoReturnToLettersOnShortcut) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.EMOJI -> {
                        EmojiPickerView(
                            theme = activeTheme,
                            onEmojiSelected = {
                                prefs.addRecentEmoji(it)
                                sendText(it)
                                if (currentSettings.autoReturnAfterEmojiInsert) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onSpacePressed = { sendText(" ") },
                            onDeletePressed = { sendDelete() },
                            onEnterPressed = {
                                sendEnter()
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.GIF -> {
                        GifPickerView(
                            theme = activeTheme,
                            onGifSelected = {
                                sendText(" $it ")
                                if (currentSettings.autoReturnToLettersOnShortcut) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.PHOTOS -> {
                        MediaPickerView(
                            theme = theme,
                            onMediaSelected = { mediaText ->
                                sendText(" $mediaText ")
                                if (currentSettings.autoReturnToLettersOnShortcut) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.CLIPBOARD -> {
                        // Clipboard matching Screenshot 21:
                        // 2-column grid, closes on paste, maintains scroll position, saves forever
                        ClipboardDrawer(
                            theme = theme,
                            prefs = prefs,
                            onItemInserted = { text ->
                                sendText(text)
                                if (currentSettings.autoReturnToLettersOnShortcut || currentSettings.clipboardCloseOnPaste) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.AI_ASSISTANT -> {
                        AiToneDrawer(
                            theme = theme,
                            currentText = getCurrentText?.invoke()?.ifBlank { currentComposingText } ?: currentComposingText,
                            onReplaceText = {
                                replaceAllText(it)
                                if (currentSettings.autoReturnToLettersOnShortcut) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onAddWordToDictionary = { word ->
                                userDict.addWord(word)
                                userWords = userDict.getUserWords()
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.VOICE_INPUT -> {
                        VoiceInputView(
                            theme = theme,
                            isArabic = isArabic,
                            onInsertText = {
                                sendText(it)
                                if (currentSettings.autoReturnToLettersOnShortcut) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.PHRASES -> {
                        DecoratedPhrasesView(
                            theme = theme,
                            onPhraseSelected = { phrase ->
                                sendText(phrase)
                                if (currentSettings.autoReturnToLettersOnShortcut) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.CALCULATOR -> {
                        CalculatorPadView(
                            theme = theme,
                            onInsertText = { mathText ->
                                sendText(mathText)
                                if (currentSettings.autoReturnToLettersOnShortcut) {
                                    activeSubView = KeyboardSubView.NONE
                                    isSymbolsMode = false
                                    isMoreSymbolsMode = false
                                }
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.SETTINGS -> {
                        QuickSettingsView(
                            theme = theme,
                            settings = currentSettings,
                            onUpdateSettings = { newSet ->
                                currentSettings = newSet
                                prefs.saveSettings(newSet)
                            },
                            onOpenFullSettings = {
                                activeSubView = KeyboardSubView.NONE
                                onOpenSettingsRequested?.invoke()
                            },
                            onCustomizeToolbar = {
                                activeSubView = KeyboardSubView.CUSTOMIZE_TOOLBAR
                            },
                            onClose = { activeSubView = KeyboardSubView.NONE }
                        )
                    }
                    KeyboardSubView.CUSTOMIZE_TOOLBAR -> {
                        CustomizeToolbarDrawer(
                            theme = theme,
                            visibleTools = currentSettings.visibleToolbarTools,
                            onUpdateTools = { newTools ->
                                val updated = currentSettings.copy(visibleToolbarTools = newTools)
                                currentSettings = updated
                                prefs.saveSettings(updated)
                            },
                            onClose = { activeSubView = KeyboardSubView.NONE }
                        )
                    }
                    else -> {}
                }
            }
        }

        // Suggestions Bar with spell correction & dictionary addition
        if (activeSubView == KeyboardSubView.NONE && settings.suggestionsEnabled) {
            KeyboardSuggestionsBar(
                theme = theme,
                suggestions = suggestions,
                onSuggestionClick = { word ->
                    performFeedback()
                    val words = currentComposingText.trim().split(" ")
                    if (words.isNotEmpty()) {
                        val base = words.dropLast(1).joinToString(" ")
                        val updated = if (base.isEmpty()) "$word " else "$base $word "
                        replaceAllText(updated)
                    } else {
                        sendText("$word ")
                    }
                },
                onAddWordToDictionary = { word ->
                    userDict.addWord(word)
                    userWords = userDict.getUserWords()
                    performFeedback()
                }
            )
        }

        // Quick Shortcuts / Emojis Row (Matching Screenshots 14 & 15: 👑 💋 ة ؤ ء ئ ى لأ 😂 خاص)
        if (activeSubView == KeyboardSubView.NONE && settings.topQuickEmojiRowEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val quickRow = currentWorldLang.quickShortcuts.map { KeyModel(KeyType.Character(it)) }
                quickRow.forEach { key ->
                    val char = (key.type as KeyType.Character).primary
                    val quickKeyBg = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                performFeedback()
                                sendText(char)
                            },
                        shape = RoundedCornerShape(theme.cornerRadius.dp),
                        color = quickKeyBg,
                        border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                color = Color(theme.keyTextColor),
                                fontSize = if (char.length > 1 && !char.startsWith("\uD83D")) 10.sp else 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Main Keyboard Keys Section
        if (activeSubView == KeyboardSubView.NONE) {
            val (worldR1, worldR2, worldR3) = remember(currentWorldLang, isShifted, settings.keyboardLayoutStyle) {
                KeyLayouts.getRowsForLanguage(currentWorldLang, isShifted, isArabic, settings.keyboardLayoutStyle)
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.5.dp, vertical = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val calculatedKeyHeight = (52.dp * currentSettings.keyHeightFactor * currentSettings.keyButtonScale)

                        // Optional Number Row
                        if (currentSettings.numberRowEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val row = if (isArabic) KeyLayouts.numbersRowAr else KeyLayouts.numbersRowEn
                                row.forEach { key ->
                                    val digitChar = (key.type as KeyType.Character).primary
                                    KeyButton(
                                        key = key,
                                        theme = activeTheme,
                                        isShifted = isShifted,
                                        showKeyPopup = currentSettings.keyPopupEnabled,
                                        keyHeight = calculatedKeyHeight,
                                        keyFontSizeFactor = currentSettings.keyFontSizeFactor,
                                        keyButtonScale = currentSettings.keyButtonScale,
                                        longPressDelayMs = currentSettings.longPressDelayMs.toLong(),
                                        onPerformFeedback = { performFeedback(it) },
                                        modifier = Modifier.weight(key.weight),
                                        onClick = { sendText(digitChar) },
                                        onLongClick = {
                                            performFeedback()
                                            val variants = LongPressVariantsManager.getVariants(digitChar)
                                            longPressChar = digitChar
                                            longPressVariants = variants
                                        }
                                    )
                                }
                            }
                        }

                        // Row 1 (With number hints matching Screenshots 3 & 4)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val row = when {
                                isSymbolsMode && !isMoreSymbolsMode -> KeyLayouts.symbolsRow1
                                isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.symbolsMoreRow1
                                else -> worldR1
                            }
                            row.forEach { key ->
                                val char = (key.type as KeyType.Character).primary
                                val hint = if (isArabic) LongPressVariantsManager.arabicRow1Hints[char]
                                           else LongPressVariantsManager.englishRow1Hints[char.lowercase()]
                                KeyButton(
                                    key = key,
                                    theme = activeTheme,
                                    isShifted = isShifted,
                                    numberHint = hint,
                                    isArabicLayout = isArabic,
                                    showKeyPopup = currentSettings.keyPopupEnabled,
                                    keyHeight = calculatedKeyHeight,
                                    keyFontSizeFactor = currentSettings.keyFontSizeFactor,
                                    keyButtonScale = currentSettings.keyButtonScale,
                                    longPressDelayMs = currentSettings.longPressDelayMs.toLong(),
                                    onPerformFeedback = { performFeedback(it) },
                                    modifier = Modifier.weight(key.weight),
                                    onClick = {
                                        sendText(if (isShifted && !currentWorldLang.isRtl && !isSymbolsMode) char.uppercase() else char)
                                    },
                                    onLongClick = {
                                        performFeedback(22L)
                                        val variants = LongPressVariantsManager.getVariants(char)
                                        longPressChar = char
                                        longPressVariants = variants
                                    }
                                )
                            }
                        }

                        // Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            val row = when {
                                isSymbolsMode && !isMoreSymbolsMode -> KeyLayouts.symbolsRow2
                                isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.symbolsMoreRow2
                                else -> worldR2
                            }
                            row.forEach { key ->
                                val char = (key.type as KeyType.Character).primary
                                KeyButton(
                                    key = key,
                                    theme = activeTheme,
                                    isShifted = isShifted,
                                    isArabicLayout = isArabic,
                                    showKeyPopup = currentSettings.keyPopupEnabled,
                                    keyHeight = calculatedKeyHeight,
                                    keyFontSizeFactor = currentSettings.keyFontSizeFactor,
                                    keyButtonScale = currentSettings.keyButtonScale,
                                    longPressDelayMs = currentSettings.longPressDelayMs.toLong(),
                                    onPerformFeedback = { performFeedback(it) },
                                    modifier = Modifier.weight(key.weight),
                                    onClick = {
                                        sendText(if (isShifted && !currentWorldLang.isRtl && !isSymbolsMode) char.uppercase() else char)
                                    },
                                    onLongClick = {
                                        performFeedback(22L)
                                        val variants = LongPressVariantsManager.getVariants(char)
                                        longPressChar = char
                                        longPressVariants = variants
                                    }
                                )
                            }
                        }

                        // Row 3
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val row = when {
                                isSymbolsMode && !isMoreSymbolsMode -> KeyLayouts.symbolsRow3
                                isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.symbolsMoreRow3
                                else -> worldR3
                            }

                            row.forEach { key ->
                                when (key.type) {
                                    is KeyType.Shift -> {
                                        SpecialKeyButton(
                                            icon = Icons.Default.ArrowUpward,
                                            theme = activeTheme,
                                            isActive = isShifted,
                                            keyHeight = calculatedKeyHeight,
                                            modifier = Modifier.weight(key.weight),
                                            onClick = {
                                                performFeedback(isSpecial = true)
                                                isShifted = !isShifted
                                            }
                                        )
                                    }
                                    is KeyType.Backspace -> {
                                        val bWeight = (key.weight * currentSettings.backspaceKeyScale).coerceIn(1.0f, 1.85f)
                                        BackspaceKeyButton(
                                            theme = activeTheme,
                                            modifier = Modifier.weight(bWeight),
                                            keyHeight = calculatedKeyHeight,
                                            backspaceScale = currentSettings.backspaceKeyScale,
                                            repeatSpeedMs = currentSettings.keyRepeatSpeedMs.toLong(),
                                            onDelete = { sendDelete() },
                                            onDeleteWord = {
                                                performFeedback(isDelete = true)
                                                if (currentComposingText.isNotEmpty()) {
                                                    val words = currentComposingText.trimEnd().split(" ")
                                                    currentComposingText = if (words.size > 1) words.dropLast(1).joinToString(" ") + " " else ""
                                                }
                                                if (inputConnection != null) {
                                                    inputConnection.deleteSurroundingText(10, 0)
                                                } else {
                                                    onDirectClearAndReplaceText?.invoke(currentComposingText)
                                                }
                                            }
                                        )
                                    }
                                    is KeyType.Character -> {
                                        if (key.type.primary == "=\\<") {
                                            SpecialKeyButton(
                                                text = "=\\<",
                                                theme = activeTheme,
                                                keyHeight = calculatedKeyHeight,
                                                modifier = Modifier.weight(key.weight),
                                                onClick = {
                                                    performFeedback(isSpecial = true)
                                                    isMoreSymbolsMode = true
                                                }
                                            )
                                        } else if (key.type.primary == "123") {
                                            SpecialKeyButton(
                                                text = "123",
                                                theme = activeTheme,
                                                keyHeight = calculatedKeyHeight,
                                                modifier = Modifier.weight(key.weight),
                                                onClick = {
                                                    performFeedback(isSpecial = true)
                                                    isMoreSymbolsMode = false
                                                }
                                            )
                                        } else {
                                            val char = key.type.primary
                                            KeyButton(
                                                key = key,
                                                theme = activeTheme,
                                                isShifted = isShifted,
                                                isArabicLayout = isArabic,
                                                showKeyPopup = currentSettings.keyPopupEnabled,
                                                keyHeight = calculatedKeyHeight,
                                                keyFontSizeFactor = currentSettings.keyFontSizeFactor,
                                                keyButtonScale = currentSettings.keyButtonScale,
                                                longPressDelayMs = currentSettings.longPressDelayMs.toLong(),
                                                onPerformFeedback = { performFeedback(it) },
                                                modifier = Modifier.weight(key.weight),
                                                onClick = {
                                                    sendText(if (isShifted && !isArabic && !isSymbolsMode) char.uppercase() else char)
                                                },
                                                onLongClick = {
                                                    performFeedback(22L)
                                                    val variants = LongPressVariantsManager.getVariants(char)
                                                    longPressChar = char
                                                    longPressVariants = variants
                                                }
                                            )
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }

                        // Row 4: Exact bottom row from Samsung layout with Emoji key at bottom:
                        // [ 123!#() ] [ 🌐 Language ] [ 😊 Emoji ] [   Spacebar   ] [ . ] [ 📋 Clipboard ] [ ✓ Enter ]
                        var enterJob: Job? by remember { mutableStateOf(null) }
                        val bottomKeyBg = ThemePresets.resolveKeyColor(activeTheme.keyBackgroundColor, activeTheme.keyOpacity)
                        val bottomKeyPressedBg = ThemePresets.resolveKeyColor(activeTheme.keyPressedColor, activeTheme.keyOpacity)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(53.dp * currentSettings.keyHeightFactor * currentSettings.keyButtonScale),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. ModeChange key 123!#() on FAR LEFT
                            var isModePressed by remember { mutableStateOf(false) }
                            val modeBg by animateColorAsState(
                                targetValue = if (isModePressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isModePressed) 20 else 90),
                                label = "mode_bg"
                            )
                            val modeScale by animateFloatAsState(
                                targetValue = if (isModePressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "mode_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1.15f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = modeScale; scaleY = modeScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(modeBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isModePressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isModePressed = false
                                            },
                                            onTap = {
                                                isSymbolsMode = !isSymbolsMode
                                                isMoreSymbolsMode = false
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isSymbolsMode) currentWorldLang.nameNative else "123!#()",
                                    color = Color(activeTheme.keyTextColor),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 2. Language Switch 🌐
                            var isLangPressed by remember { mutableStateOf(false) }
                            val langBg by animateColorAsState(
                                targetValue = if (isLangPressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isLangPressed) 20 else 90),
                                label = "lang_bg"
                            )
                            val langScale by animateFloatAsState(
                                targetValue = if (isLangPressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "lang_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = langScale; scaleY = langScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(langBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isLangPressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isLangPressed = false
                                            },
                                            onTap = {
                                                val nextLang = InputLanguagesManager.getNextActiveLanguage(context, currentLangCode)
                                                currentLangCode = nextLang.code
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = Color(activeTheme.accentColor),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 3. Emoji shortcut button 😊 directly on bottom row
                            var isEmojiPressed by remember { mutableStateOf(false) }
                            val emojiBg by animateColorAsState(
                                targetValue = if (isEmojiPressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isEmojiPressed) 20 else 90),
                                label = "emoji_bg"
                            )
                            val emojiScale by animateFloatAsState(
                                targetValue = if (isEmojiPressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "emoji_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.95f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = emojiScale; scaleY = emojiScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(emojiBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isEmojiPressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isEmojiPressed = false
                                            },
                                            onTap = {
                                                activeSubView = if (activeSubView == KeyboardSubView.EMOJI) KeyboardSubView.NONE else KeyboardSubView.EMOJI
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "😊",
                                    fontSize = 18.sp
                                )
                            }

                            // 4. Spacebar in the center with active world language name and gesture cursor control
                            var isSpacePressed by remember { mutableStateOf(false) }
                            val spaceBg by animateColorAsState(
                                targetValue = if (isSpacePressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isSpacePressed) 20 else 90),
                                label = "space_bg"
                            )
                            val spaceScale by animateFloatAsState(
                                targetValue = if (isSpacePressed) 0.97f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "space_scale"
                            )
                            var dragAccumulator by remember { mutableFloatStateOf(0f) }
                            Box(
                                modifier = Modifier
                                    .weight(3.1f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = spaceScale; scaleY = spaceScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(spaceBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragEnd = {
                                                if (kotlin.math.abs(totalDragX) > 75f && settings.swipeSpaceSwitchLanguage) {
                                                    performFeedback()
                                                    val nextLang = InputLanguagesManager.getNextActiveLanguage(context, currentLangCode)
                                                    currentLangCode = nextLang.code
                                                }
                                                totalDragX = 0f
                                                dragAccumulator = 0f
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            totalDragX += dragAmount.x
                                            dragAccumulator += dragAmount.x
                                            // Smooth cursor control by swiping spacebar (Samsung style)
                                            if (kotlin.math.abs(dragAccumulator) >= 18f) {
                                                val isLeft = dragAccumulator < 0
                                                val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                if (ic != null) {
                                                    val keycode = if (isLeft) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
                                                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keycode))
                                                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keycode))
                                                    performFeedback(durationMs = 12L)
                                                }
                                                dragAccumulator = 0f
                                            }
                                        }
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isSpacePressed = true
                                                performFeedback(isSpace = true)
                                                tryAwaitRelease()
                                                isSpacePressed = false
                                            },
                                            onTap = {
                                                sendText(" ")
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentWorldLang.nameNative,
                                        color = Color(activeTheme.keyTextColor).copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // 5. Dot . key
                            var isDotPressed by remember { mutableStateOf(false) }
                            val dotBg by animateColorAsState(
                                targetValue = if (isDotPressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isDotPressed) 20 else 90),
                                label = "dot_bg"
                            )
                            val dotScale by animateFloatAsState(
                                targetValue = if (isDotPressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "dot_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.7f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(dotBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isDotPressed = true
                                                performFeedback()
                                                tryAwaitRelease()
                                                isDotPressed = false
                                            },
                                            onTap = {
                                                sendText(".")
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(".", color = Color(activeTheme.keyTextColor), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            // 6. Clipboard shortcut button 📋
                            var isClipPressed by remember { mutableStateOf(false) }
                            val clipBg by animateColorAsState(
                                targetValue = if (isClipPressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isClipPressed) 20 else 90),
                                label = "clip_bg"
                            )
                            val clipScale by animateFloatAsState(
                                targetValue = if (isClipPressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "clip_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = clipScale; scaleY = clipScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(clipBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isClipPressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isClipPressed = false
                                            },
                                            onTap = {
                                                activeSubView = if (activeSubView == KeyboardSubView.CLIPBOARD) KeyboardSubView.NONE else KeyboardSubView.CLIPBOARD
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentPaste,
                                    contentDescription = "Clipboard",
                                    tint = Color(activeTheme.accentColor),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 7. Enter key on the FAR RIGHT with 4-sec translate!
                            var isEnterPressed by remember { mutableStateOf(false) }
                            val enterScale by animateFloatAsState(
                                targetValue = if (isEnterPressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "enter_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1.25f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = enterScale; scaleY = enterScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(activeTheme.enterButtonColor),
                                                Color(activeTheme.enterButtonColor).copy(alpha = if (isEnterPressed) 0.7f else 0.85f)
                                            )
                                        )
                                    )
                                    .border(1.dp, Color(activeTheme.accentColor).copy(alpha = 0.7f), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(settings.enterLongPressTranslateEnabled) {
                                        detectTapGestures(
                                            onPress = {
                                                isEnterPressed = true
                                                performFeedback(isSpecial = true)
                                                if (settings.enterLongPressTranslateEnabled) {
                                                    enterJob = coroutineScope.launch {
                                                        isEnterHolding = true
                                                        enterHoldProgress = 0f
                                                        val totalTime = 4000L
                                                        val step = 50L
                                                        var elapsed = 0L
                                                        while (elapsed < totalTime) {
                                                            delay(step)
                                                            elapsed += step
                                                            enterHoldProgress = elapsed.toFloat() / totalTime
                                                        }
                                                        triggerInstantTranslate()
                                                        isEnterHolding = false
                                                        enterHoldProgress = 0f
                                                    }
                                                }
                                                val released = tryAwaitRelease()
                                                enterJob?.cancel()
                                                isEnterHolding = false
                                                enterHoldProgress = 0f
                                                isEnterPressed = false
                                                if (released) {
                                                    sendEnter()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                    contentDescription = "Enter",
                                    tint = ThemePresets.getEnterIconTint(activeTheme),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Floating Long-Press Character Popup Overlay (Never pushes rows or resizes keyboard - FROZEN!)
                    if (longPressChar != null && longPressVariants.isNotEmpty()) {
                        val isLight = ThemePresets.isLightColor(activeTheme.backgroundColor)
                        val popupTextCol = if (isLight) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(activeTheme.backgroundColor),
                                border = BorderStroke(1.5.dp, Color(activeTheme.accentColor)),
                                shadowElevation = 14.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 6.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    longPressVariants.forEach { variant ->
                                        val isCurrent = variant == longPressChar
                                        Box(
                                            modifier = Modifier
                                                .size(width = 40.dp, height = 46.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isCurrent) Color(activeTheme.keyPressedColor)
                                                    else Color(activeTheme.keyBackgroundColor)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isCurrent) Color(activeTheme.accentColor) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    performFeedback()
                                                    sendText(variant)
                                                    longPressChar = null
                                                    longPressVariants = emptyList()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = variant,
                                                color = if (isCurrent) Color(activeTheme.accentColor) else popupTextCol,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // Dismiss X circular button (Neat & balanced size)
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                                            .border(1.dp, Color.White.copy(alpha = 0.85f), CircleShape)
                                            .clickable {
                                                performFeedback()
                                                longPressChar = null
                                                longPressVariants = emptyList()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
    }
}

private val arabicSecondaryHintMap = mapOf(
    "ض" to "1", "ص" to "2", "ث" to "3", "ق" to "4", "ف" to "5",
    "غ" to "6", "ع" to "7", "ه" to "8", "خ" to "9", "ح" to "0",
    "ج" to "ـ", "د" to "؛",
    "ش" to "َ", "س" to "ً", "ي" to "ُ", "ب" to "ٌ", "ل" to "ِ",
    "ا" to "ٍ", "ت" to "ّ", "ن" to "ْ", "م" to "«", "ك" to "»", "ط" to "؟",
    "ذ" to "!", "ئ" to "@", "ء" to "#", "ؤ" to "$", "ر" to "%",
    "لا" to "&", "ى" to "*", "ة" to "(", "و" to ")", "ز" to "-", "ظ" to "+"
)

private val englishSecondaryHintMap = mapOf(
    "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
    "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0",
    "a" to "@", "s" to "#", "d" to "$", "f" to "%", "g" to "&",
    "h" to "*", "j" to "-", "k" to "+", "l" to "=",
    "z" to "!", "x" to "\"", "c" to "'", "v" to ":", "b" to ";",
    "n" to "/", "m" to "?"
)

@Composable
fun KeyButton(
    key: KeyModel,
    theme: KeyboardTheme,
    isShifted: Boolean,
    numberHint: String? = null,
    isArabicLayout: Boolean = false,
    customTextColor: Color? = null,
    showKeyPopup: Boolean = true,
    keyHeight: Dp = 52.dp,
    keyFontSizeFactor: Float = 1.0f,
    keyButtonScale: Float = 1.0f,
    longPressDelayMs: Long = 340L,
    modifier: Modifier = Modifier,
    onPerformFeedback: ((Long) -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val char = (key.type as? KeyType.Character)?.primary ?: ""
    val displayChar = if (isShifted) char.uppercase() else char
    val secondaryHint = if (isArabicLayout) {
        arabicSecondaryHintMap[char]
    } else {
        englishSecondaryHintMap[char.lowercase()]
    }

    var isTouching by remember { mutableStateOf(false) }
    var isLongPressActive by remember { mutableStateOf(false) }
    var showPopupState by remember { mutableStateOf(false) }

    LaunchedEffect(isTouching) {
        if (isTouching) {
            showPopupState = true
        } else {
            delay(40) // Smooth natural release retention
            showPopupState = false
        }
    }

    fun triggerFeedback(durationMs: Long) {
        onPerformFeedback?.invoke(durationMs)
    }

    val normalKeyColor = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    val pressedKeyColor = ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    val targetKeyColor = if (isTouching) pressedKeyColor else normalKeyColor

    val animatedKeyColor by animateColorAsState(
        targetValue = targetKeyColor,
        animationSpec = tween(durationMillis = 25),
        label = "key_color"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isTouching) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 25),
        label = "key_scale"
    )

    Box(
        modifier = modifier
            .height(keyHeight)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(animatedKeyColor)
            .border(
                width = if (theme.keyStyle == "neon") 1.2.dp else 1.dp,
                color = if (theme.keyStyle == "neon") Color(theme.accentColor).copy(alpha = 0.85f)
                else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)),
                shape = RoundedCornerShape(theme.cornerRadius.dp)
            )
            .pointerInput(displayChar, onLongClick, longPressDelayMs) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isTouching = true
                    isLongPressActive = false
                    triggerFeedback(0L)

                    val actualTimeout = longPressDelayMs.coerceIn(180L, 600L)
                    if (onLongClick != null) {
                        var releasedBeforeTimeout = false
                        try {
                            withTimeout(actualTimeout) {
                                val up = waitForUpOrCancellation()
                                if (up != null) {
                                    releasedBeforeTimeout = true
                                }
                            }
                        } catch (_: Exception) {
                            // Long-press triggered
                            isLongPressActive = true
                            triggerFeedback(30L)
                            onLongClick()
                        }
                        if (releasedBeforeTimeout) {
                            onClick()
                        } else {
                            waitForUpOrCancellation()
                        }
                    } else {
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            onClick()
                        }
                    }
                    isTouching = false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val hintText = numberHint ?: secondaryHint
        if (hintText != null) {
            Text(
                text = hintText,
                color = Color(theme.subtextColor).copy(alpha = 0.85f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 1.dp, start = 3.dp)
            )
        }

        val baseFontSize = if (hintText != null) 19.5.sp else 21.5.sp
        Text(
            text = displayChar,
            color = customTextColor ?: Color(theme.keyTextColor),
            fontSize = (baseFontSize.value * keyFontSizeFactor * keyButtonScale).sp,
            fontWeight = FontWeight.Bold
        )

        // Magnificent Key Press Preview Bubble (iOS 16 style)
        if (showPopupState && showKeyPopup && displayChar.isNotBlank() && !isLongPressActive) {
            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -145),
                properties = PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                KeyPreviewBubble(
                    char = displayChar,
                    numberHint = hintText,
                    theme = theme
                )
            }
        }
    }
}

@Composable
fun KeyPreviewBubble(
    char: String,
    numberHint: String?,
    theme: KeyboardTheme
) {
    val previewBg = ThemePresets.resolveKeyColor(theme.keyPressedColor, 1.0f)
    val isLight = ThemePresets.isLightColor(theme.keyPressedColor)
    val previewTextColor = if (isLight) Color(0xFF0F172A) else Color(0xFFFFFFFF)

    Box(
        modifier = Modifier
            .width(50.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(previewBg)
            .border(
                1.5.dp,
                Color(theme.accentColor),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (numberHint != null) {
            Text(
                text = numberHint,
                color = Color(theme.accentColor),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 2.dp, start = 4.dp)
            )
        }
        Text(
            text = char,
            color = previewTextColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SpecialKeyButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    text: String? = null,
    theme: KeyboardTheme,
    isActive: Boolean = false,
    customIconColor: Color? = null,
    keyHeight: Dp = 52.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val normalColor = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    val pressedColor = ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    val targetBg = if (isActive) Color(theme.accentColor).copy(alpha = 0.35f) else if (isPressed) pressedColor else normalColor

    val animatedBg by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(durationMillis = if (isPressed) 20 else 90),
        label = "special_bg"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "special_scale"
    )

    Box(
        modifier = modifier
            .height(keyHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(animatedBg)
            .border(
                1.dp,
                if (isActive) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)),
                RoundedCornerShape(theme.cornerRadius.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = customIconColor ?: if (isActive) Color(theme.accentColor) else Color(theme.keyTextColor),
                modifier = Modifier.size(22.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                color = if (isActive) Color(theme.accentColor) else Color(theme.keyTextColor),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BackspaceKeyButton(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 52.dp,
    backspaceScale: Float = 1.35f,
    repeatSpeedMs: Long = 45L,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onDelete() // Delete 1 character immediately on initial touch
            delay(320) // Initial hold threshold
            var deleteCount = 0
            val baseSpeed = repeatSpeedMs.coerceIn(30L, 80L)
            while (isPressed) {
                onDelete()
                deleteCount++
                val interval = if (deleteCount > 15) (baseSpeed * 0.7).toLong().coerceAtLeast(22L)
                               else baseSpeed
                delay(interval)
            }
        }
    }

    val normalColor = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    val pressedColor = ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)

    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) pressedColor else normalColor,
        animationSpec = tween(durationMillis = 25),
        label = "backspace_bg"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 25),
        label = "backspace_scale"
    )

    Box(
        modifier = modifier
            .height(keyHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(animatedBg)
            .border(
                1.dp,
                if (isPressed) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)),
                RoundedCornerShape(theme.cornerRadius.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val iconSize = (19.dp * backspaceScale).coerceIn(16.dp, 28.dp)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = Color(theme.accentColor),
            modifier = Modifier.size(iconSize)
        )
    }
}
