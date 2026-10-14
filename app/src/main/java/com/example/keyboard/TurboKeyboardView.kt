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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.zIndex
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

data class ImeActionInfo(
    val actionId: Int,
    val icon: ImageVector,
    val contentDescription: String,
    val isNewline: Boolean
)

fun resolveImeAction(editorInfo: EditorInfo?): ImeActionInfo {
    if (editorInfo == null) {
        return ImeActionInfo(
            actionId = EditorInfo.IME_ACTION_UNSPECIFIED,
            icon = Icons.AutoMirrored.Filled.KeyboardReturn,
            contentDescription = "سطر جديد",
            isNewline = true
        )
    }

    val imeOptions = editorInfo.imeOptions
    val action = imeOptions and EditorInfo.IME_MASK_ACTION
    val noEnterKey = (imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
    val isMultiLine = (editorInfo.inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0

    // Explicit Action takes precedence unless isMultiLine is set AND action is unspecified/none
    if (action != EditorInfo.IME_ACTION_UNSPECIFIED && action != EditorInfo.IME_ACTION_NONE) {
        return when (action) {
            EditorInfo.IME_ACTION_SEARCH -> ImeActionInfo(action, Icons.Default.Search, "بحث", false)
            EditorInfo.IME_ACTION_SEND -> ImeActionInfo(action, Icons.AutoMirrored.Filled.Send, "إرسال", false)
            EditorInfo.IME_ACTION_GO -> ImeActionInfo(action, Icons.AutoMirrored.Filled.ArrowForward, "انتقال", false)
            EditorInfo.IME_ACTION_DONE -> ImeActionInfo(action, Icons.Default.Check, "تم", false)
            EditorInfo.IME_ACTION_NEXT -> ImeActionInfo(action, Icons.AutoMirrored.Filled.ArrowForward, "التالي", false)
            EditorInfo.IME_ACTION_PREVIOUS -> ImeActionInfo(action, Icons.AutoMirrored.Filled.ArrowBack, "السابق", false)
            else -> ImeActionInfo(action, Icons.AutoMirrored.Filled.KeyboardReturn, "إدخال", false)
        }
    }

    // Single-line fields with actionId in actionId field
    if (editorInfo.actionId != 0 && editorInfo.actionId != EditorInfo.IME_ACTION_UNSPECIFIED && editorInfo.actionId != EditorInfo.IME_ACTION_NONE) {
        return when (editorInfo.actionId) {
            EditorInfo.IME_ACTION_SEARCH -> ImeActionInfo(editorInfo.actionId, Icons.Default.Search, "بحث", false)
            EditorInfo.IME_ACTION_SEND -> ImeActionInfo(editorInfo.actionId, Icons.AutoMirrored.Filled.Send, "إرسال", false)
            EditorInfo.IME_ACTION_GO -> ImeActionInfo(editorInfo.actionId, Icons.AutoMirrored.Filled.ArrowForward, "انتقال", false)
            EditorInfo.IME_ACTION_DONE -> ImeActionInfo(editorInfo.actionId, Icons.Default.Check, "تم", false)
            EditorInfo.IME_ACTION_NEXT -> ImeActionInfo(editorInfo.actionId, Icons.AutoMirrored.Filled.ArrowForward, "التالي", false)
            EditorInfo.IME_ACTION_PREVIOUS -> ImeActionInfo(editorInfo.actionId, Icons.AutoMirrored.Filled.ArrowBack, "السابق", false)
            else -> ImeActionInfo(editorInfo.actionId, Icons.AutoMirrored.Filled.KeyboardReturn, "إدخال", false)
        }
    }

    // Multiline fields without explicit action
    if (isMultiLine && !noEnterKey) {
        return ImeActionInfo(
            actionId = EditorInfo.IME_ACTION_UNSPECIFIED,
            icon = Icons.AutoMirrored.Filled.KeyboardReturn,
            contentDescription = "سطر جديد",
            isNewline = true
        )
    }

    // Default return
    return ImeActionInfo(
        actionId = EditorInfo.IME_ACTION_UNSPECIFIED,
        icon = Icons.AutoMirrored.Filled.KeyboardReturn,
        contentDescription = if (isMultiLine) "سطر جديد" else "إدخال",
        isNewline = isMultiLine
    )
}

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
    editorInfo: EditorInfo? = null,
    getCurrentEditorInfo: (() -> EditorInfo?)? = null,
    onServiceEnter: (() -> Unit)? = null,
    onServiceNewline: (() -> Unit)? = null,
    onServiceDelete: (() -> Unit)? = null,
    onServiceCommitText: ((String) -> Boolean)? = null,
    initialLanguageCode: String? = null,
    onLanguageChanged: ((String) -> Unit)? = null,
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

    var currentLangCode by remember(initialLanguageCode) {
        mutableStateOf(initialLanguageCode ?: InputLanguagesManager.getCurrentLanguageCode(context))
    }
    LaunchedEffect(initialLanguageCode) {
        if (initialLanguageCode != null && initialLanguageCode != currentLangCode) {
            currentLangCode = initialLanguageCode
        }
    }
    val currentWorldLang = remember(currentLangCode) {
        InputLanguagesManager.getLanguageByCode(currentLangCode)
    }
    val isArabic = currentWorldLang.code == "ar"

    var isShifted by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }
    var isMoreSymbolsMode by remember { mutableStateOf(false) }
    var activeSubView by remember { mutableStateOf(KeyboardSubView.NONE) }

    val currentImeAction = remember(editorInfo, getCurrentEditorInfo) {
        resolveImeAction(getCurrentEditorInfo?.invoke() ?: editorInfo)
    }

    LaunchedEffect(isArabic) {
        if (isArabic) {
            isShifted = false
        }
    }

    var customTopEmojis by remember { mutableStateOf(prefs.getCustomTopEmojis()) }
    var isCustomizeTopEmojisOpen by remember { mutableStateOf(false) }

    // Live Transboard features
    var activeDecorationStyle by remember { mutableStateOf(prefs.getActiveDecorationStyle()) }
    var isDecorationBarOpen by remember { mutableStateOf(false) }

    var isTranslationBarOpen by remember { mutableStateOf(false) }
    var translationSource by remember { mutableStateOf(currentSettings.translationSource) }
    var translationTarget by remember { mutableStateOf(currentSettings.translationTarget) }
    var isTranslatingLive by remember { mutableStateOf(false) }

    // Long-Press character variant popup state (Matching Screenshots 1, 2, 4)
    var longPressChar by remember { mutableStateOf<String?>(null) }
    var longPressVariants by remember { mutableStateOf<List<String>>(emptyList()) }

    // Live text tracking for prediction and special 4-sec translate
    var currentComposingText by remember { mutableStateOf("") }

    // 2-sec Enter hold to toggle Newline mode (السطر الثاني ↵)
    var isEnterInNewlineMode by remember { mutableStateOf(false) }
    var isEnterHolding by remember { mutableStateOf(false) }
    var enterHoldProgress by remember { mutableFloatStateOf(0f) }
    var enterHoldMessage by remember { mutableStateOf("") }
    var newlineSwitchNotification by remember { mutableStateOf<String?>(null) }

    // 3-sec Translate button hold state
    var isTranslateButtonHolding by remember { mutableStateOf(false) }
    var translateHoldProgress by remember { mutableFloatStateOf(0f) }
    var translateHoldMessage by remember { mutableStateOf("") }

    LaunchedEffect(newlineSwitchNotification) {
        if (newlineSwitchNotification != null) {
            delay(2200)
            newlineSwitchNotification = null
        }
    }

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

    fun dismissLongPressPopup() {
        if (longPressChar != null || longPressVariants.isNotEmpty()) {
            longPressChar = null
            longPressVariants = emptyList()
        }
    }

    fun sendText(text: String) {
        dismissLongPressPopup()
        val resolvedText = if (text == "-" || text == "_") {
            val prevChar = currentComposingText.lastOrNull()
            val isAfterArabic = prevChar != null && (prevChar in '\u0600'..'\u06FF' || prevChar == 'ـ')
            if (isAfterArabic || isArabic) {
                "ـ" // وصل الحروف مع بعض ومدها (Tatweel / Kashida)
            } else {
                text
            }
        } else {
            text
        }
        val textToInsert = if (activeDecorationStyle != "none" && resolvedText.length == 1) {
            TextDecorator.decorateChar(resolvedText, activeDecorationStyle)
        } else {
            resolvedText
        }
        currentComposingText = (currentComposingText + textToInsert).takeLast(80)

        var handled = false
        if (onServiceCommitText != null) {
            try {
                handled = onServiceCommitText.invoke(textToInsert)
            } catch (_: Exception) {}
        }
        if (!handled) {
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            if (ic != null) {
                try {
                    ic.finishComposingText()
                } catch (_: Exception) {}
                try {
                    val committed = ic.commitText(textToInsert, 1)
                    if (!committed) {
                        ic.beginBatchEdit()
                        ic.commitText(textToInsert, 1)
                        ic.endBatchEdit()
                    }
                } catch (e: Exception) {
                    try {
                        ic.commitText(textToInsert, 1)
                    } catch (_: Exception) {}
                }
            } else {
                onDirectInsertText?.invoke(textToInsert)
            }
        }
    }

    fun sendDelete() {
        dismissLongPressPopup()
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
                        val deleted = ic.deleteSurroundingText(1, 0)
                        if (!deleted) {
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

    fun executeNormalEnter() {
        if (onServiceEnter != null) {
            onServiceEnter.invoke()
        } else {
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            val info = getCurrentEditorInfo?.invoke() ?: editorInfo
            val actionInfo = resolveImeAction(info)

            if (ic != null) {
                try {
                    val imeOptions = info?.imeOptions ?: 0
                    val inputType = info?.inputType ?: 0
                    val isMultiLine = (inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0
                    val action = actionInfo.actionId

                    if (actionInfo.isNewline || (isMultiLine && (action == EditorInfo.IME_ACTION_NONE || action == EditorInfo.IME_ACTION_UNSPECIFIED))) {
                        val down = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                        val up = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                        if (!down || !up) {
                            ic.commitText("\n", 1)
                        }
                    } else if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                        val handled = ic.performEditorAction(action)
                        if (!handled) {
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                        }
                    } else {
                        val fallbackAction = imeOptions and EditorInfo.IME_MASK_ACTION
                        var handled = false
                        if (fallbackAction != EditorInfo.IME_ACTION_NONE && fallbackAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
                            handled = ic.performEditorAction(fallbackAction)
                        }
                        if (!handled) {
                            val down = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                            val up = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                            if (!down || !up) {
                                ic.commitText("\n", 1)
                            }
                        }
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
        }
        currentComposingText = ""
        // Crucial fix: Automatically return to letters keyboard on Send / Enter
        if (currentSettings.autoReturnToLettersOnSend) {
            activeSubView = KeyboardSubView.NONE
            isSymbolsMode = false
            isMoreSymbolsMode = false
        }
    }

    fun executeTranslateInPlace() {
        if (isTranslatingLive) return
        coroutineScope.launch {
            isTranslatingLive = true
            try {
                val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                val textBefore = ic?.getTextBeforeCursor(1000, 0)?.toString() ?: ""
                val selectedText = ic?.getSelectedText(0)?.toString() ?: ""
                val lastLine = textBefore.substringAfterLast('\n')
                val textToTranslate = if (selectedText.isNotBlank()) {
                    selectedText.trim()
                } else if (currentComposingText.isNotBlank()) {
                    currentComposingText.trim()
                } else if (lastLine.isNotBlank()) {
                    lastLine.trim()
                } else if (textBefore.isNotBlank()) {
                    textBefore.trim()
                } else {
                    ""
                }

                if (textToTranslate.isNotBlank()) {
                    val translated = com.example.data.TranslationEngine.translate(
                        text = textToTranslate,
                        sourceLang = translationSource,
                        targetLang = translationTarget
                    )
                    if (ic != null) {
                        ic.beginBatchEdit()
                        try {
                            if (selectedText.isNotBlank()) {
                                ic.commitText(translated, 1)
                            } else {
                                val deleteCount = if (currentComposingText.isNotBlank() && textBefore.endsWith(currentComposingText)) {
                                    currentComposingText.length
                                } else if (textBefore.endsWith(textToTranslate)) {
                                    textToTranslate.length
                                } else if (currentComposingText.isNotBlank()) {
                                    currentComposingText.length
                                } else {
                                    lastLine.length
                                }
                                ic.deleteSurroundingText(deleteCount, 0)
                                ic.commitText(translated, 1)
                            }
                        } finally {
                            ic.endBatchEdit()
                        }
                    } else {
                        onDirectClearAndReplaceText?.invoke(translated)
                    }
                    currentComposingText = ""
                    performFeedback(isSpecial = true)
                }
            } finally {
                isTranslatingLive = false
            }
        }
    }

    fun execute3SecTranslation() {
        if (isTranslatingLive) return
        coroutineScope.launch {
            isTranslatingLive = true
            try {
                val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                val textBefore = ic?.getTextBeforeCursor(2000, 0)?.toString() ?: ""
                val selectedText = ic?.getSelectedText(0)?.toString() ?: ""
                val lastLine = textBefore.substringAfterLast('\n')
                val textToTranslate = when {
                    selectedText.isNotBlank() -> selectedText.trim()
                    currentComposingText.isNotBlank() -> currentComposingText.trim()
                    lastLine.isNotBlank() -> lastLine.trim()
                    textBefore.isNotBlank() -> textBefore.trim()
                    getCurrentText?.invoke()?.isNotBlank() == true -> getCurrentText.invoke()!!.trim()
                    else -> ""
                }

                if (textToTranslate.isNotBlank()) {
                    val hasArabic = textToTranslate.any { it in '\u0600'..'\u06FF' }
                    val src = if (hasArabic) "ar" else "en"
                    val tgt = if (hasArabic) "en" else "ar"

                    val translated = com.example.data.TranslationEngine.translate(
                        text = textToTranslate,
                        sourceLang = src,
                        targetLang = tgt
                    )

                    if (ic != null) {
                        ic.beginBatchEdit()
                        try {
                            if (selectedText.isNotBlank()) {
                                ic.commitText(translated, 1)
                            } else {
                                val deleteCount = if (currentComposingText.isNotBlank() && textBefore.endsWith(currentComposingText)) {
                                    currentComposingText.length
                                } else if (textBefore.endsWith(textToTranslate)) {
                                    textToTranslate.length
                                } else if (currentComposingText.isNotBlank()) {
                                    currentComposingText.length
                                } else {
                                    lastLine.length
                                }
                                ic.deleteSurroundingText(deleteCount, 0)
                                ic.commitText(translated, 1)
                            }
                        } finally {
                            ic.endBatchEdit()
                        }
                    } else {
                        onDirectClearAndReplaceText?.invoke(translated)
                    }
                    currentComposingText = ""
                    performFeedback(isSpecial = true)
                    translateHoldMessage = "✓ تمت الترجمة بنجاح"
                } else {
                    translateHoldMessage = "اكتب نصاً لترجمته"
                }
            } catch (_: Exception) {
                translateHoldMessage = "تعذر الترجمة حالياً"
            } finally {
                isTranslatingLive = false
                delay(2000)
                translateHoldMessage = ""
            }
        }
    }

    fun sendEnter() {
        dismissLongPressPopup()
        performFeedback(isSpecial = true)

        // إذا كان زر الترجمة مفعلاً: ترجمة النص بعد كتابته والضغط على زر إنتر
        if (isTranslationBarOpen) {
            coroutineScope.launch {
                isTranslatingLive = true
                try {
                    val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                    val textBefore = ic?.getTextBeforeCursor(1000, 0)?.toString() ?: ""
                    val selectedText = ic?.getSelectedText(0)?.toString() ?: ""
                    val lastLine = textBefore.substringAfterLast('\n')
                    val textToTranslate = if (selectedText.isNotBlank()) {
                        selectedText.trim()
                    } else if (currentComposingText.isNotBlank()) {
                        currentComposingText.trim()
                    } else if (lastLine.isNotBlank()) {
                        lastLine.trim()
                    } else if (textBefore.isNotBlank()) {
                        textBefore.trim()
                    } else {
                        ""
                    }

                    if (textToTranslate.isNotBlank()) {
                        val translated = com.example.data.TranslationEngine.translate(
                            text = textToTranslate,
                            sourceLang = translationSource,
                            targetLang = translationTarget
                        )
                        if (ic != null) {
                            ic.beginBatchEdit()
                            try {
                                if (selectedText.isNotBlank()) {
                                    ic.commitText(translated, 1)
                                } else {
                                    val charsToDelete = if (currentComposingText.isNotBlank() && textBefore.endsWith(currentComposingText)) {
                                        currentComposingText.length
                                    } else if (textBefore.endsWith(textToTranslate)) {
                                        textToTranslate.length
                                    } else if (currentComposingText.isNotBlank()) {
                                        currentComposingText.length
                                    } else {
                                        lastLine.length
                                    }
                                    ic.deleteSurroundingText(charsToDelete, 0)
                                    ic.commitText(translated, 1)
                                }
                            } finally {
                                ic.endBatchEdit()
                            }
                        } else {
                            onDirectClearAndReplaceText?.invoke(translated)
                        }
                        currentComposingText = ""

                        if (onServiceEnter != null) {
                            onServiceEnter.invoke()
                        } else if (ic != null) {
                            val down = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                            val up = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                            if (!down || !up) {
                                ic.commitText("\n", 1)
                            }
                        }
                        if (currentSettings.autoReturnToLettersOnSend) {
                            activeSubView = KeyboardSubView.NONE
                            isSymbolsMode = false
                            isMoreSymbolsMode = false
                        }
                    } else {
                        executeNormalEnter()
                    }
                } finally {
                    isTranslatingLive = false
                }
            }
            return
        }

        executeNormalEnter()
    }

    fun replaceAllText(newText: String) {
        dismissLongPressPopup()
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
        // Top status/alert banner when Enter 2-sec newline switch or Translate 3-sec hold is active
        AnimatedVisibility(visible = newlineSwitchNotification != null || enterHoldProgress > 0.05f || enterHoldMessage.isNotEmpty() || translateHoldProgress > 0.03f || translateHoldMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        when {
                            translateHoldProgress > 0.03f || translateHoldMessage.isNotEmpty() -> Color(0xFF00E5FF).copy(alpha = 0.25f)
                            isEnterInNewlineMode -> Color(0xFF00E5FF).copy(alpha = 0.25f)
                            else -> Color(theme.accentColor).copy(alpha = 0.2f)
                        }
                    )
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (translateHoldProgress in 0.03f..0.99f) {
                        CircularProgressIndicator(
                            progress = { translateHoldProgress },
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF00E5FF),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val formattedSec = String.format(java.util.Locale.US, "%.1f", (translateHoldProgress * 3f).coerceAtMost(3f))
                        Text(
                            text = "استمر بالضغط 3 ثوانٍ لترجمة النص... (${formattedSec}ث/3ث)",
                            color = Color(0xFF00E5FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (translateHoldMessage.isNotEmpty()) {
                        Text(
                            text = translateHoldMessage,
                            color = Color(0xFF00E5FF),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (enterHoldProgress in 0.05f..0.99f) {
                        CircularProgressIndicator(
                            progress = { enterHoldProgress },
                            modifier = Modifier.size(16.dp),
                            color = if (isEnterInNewlineMode) Color(0xFF00E5FF) else Color(theme.accentColor),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val target = if (isEnterInNewlineMode) "زر الإدخال / الإرسال" else "زر السطر الثاني (↵ انتقال للسطر الثاني)"
                        val formattedSec = String.format(java.util.Locale.US, "%.1f", (enterHoldProgress * 2f).coerceAtMost(2f))
                        Text(
                            text = "استمر بالضغط 2 ثوانٍ للتبديل إلى $target... (${formattedSec}ث/2ث)",
                            color = if (isEnterInNewlineMode) Color(0xFF00E5FF) else Color(theme.accentColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (newlineSwitchNotification != null) {
                        Text(
                            text = newlineSwitchNotification ?: "",
                            color = if (isEnterInNewlineMode) Color(0xFF00E5FF) else Color(theme.accentColor),
                            fontSize = 12.sp,
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
            isForceEnglishNumbers = currentSettings.forceEnglishNumbers,
            onToggleForceEnglishNumbers = {
                val updated = currentSettings.copy(forceEnglishNumbers = !currentSettings.forceEnglishNumbers)
                currentSettings = updated
                prefs.saveSettings(updated)
                performFeedback(isSpecial = true)
            },
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
                    val updated = currentSettings.copy(translationSource = translationSource, translationTarget = translationTarget)
                    currentSettings = updated
                    prefs.saveSettings(updated)
                },
                onSelectSourceLang = {
                    translationSource = it
                    val updated = currentSettings.copy(translationSource = it)
                    currentSettings = updated
                    prefs.saveSettings(updated)
                },
                onSelectTargetLang = {
                    translationTarget = it
                    val updated = currentSettings.copy(translationTarget = it)
                    currentSettings = updated
                    prefs.saveSettings(updated)
                },
                onClose = { isTranslationBarOpen = false },
                onTranslateNow = { executeTranslateInPlace() },
                isTranslating = isTranslatingLive
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

        // Quick Shortcuts / Emojis Row (Customizable for user - إضافة وحذف الابتسامات)
        if (activeSubView == KeyboardSubView.NONE && settings.topQuickEmojiRowEnabled) {
            val config = androidx.compose.ui.platform.LocalConfiguration.current
            val isPort = config.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isPort) 30.dp else 26.dp)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                customTopEmojis.forEach { char ->
                    val quickKeyBg = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .pointerInput(char) {
                                detectTapGestures(
                                    onLongPress = {
                                        performFeedback(isSpecial = true)
                                        isCustomizeTopEmojisOpen = true
                                    },
                                    onTap = {
                                        performFeedback()
                                        sendText(char)
                                    }
                                )
                            },
                        shape = RoundedCornerShape(theme.cornerRadius.dp),
                        color = quickKeyBg,
                        border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                color = Color(theme.keyTextColor),
                                fontSize = if (char.length > 1 && !char.startsWith("\uD83D")) 9.sp else (13 * currentSettings.emojiSizeScale).sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Add / Customize button for the top emoji row (+)
                Surface(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            performFeedback(isSpecial = true)
                            isCustomizeTopEmojisOpen = true
                        },
                    shape = RoundedCornerShape(theme.cornerRadius.dp),
                    color = Color(theme.accentColor).copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, Color(theme.accentColor).copy(alpha = 0.6f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "تخصيص الابتسامات",
                            tint = Color(theme.accentColor),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // Floating Dialog to Customize Top Emojis Row
        if (isCustomizeTopEmojisOpen) {
            CustomizeTopEmojisDialog(
                theme = activeTheme,
                currentEmojis = customTopEmojis,
                onSave = { updatedList ->
                    customTopEmojis = updatedList
                    prefs.saveCustomTopEmojis(updatedList)
                    isCustomizeTopEmojisOpen = false
                },
                onDismiss = { isCustomizeTopEmojisOpen = false }
            )
        }

        // Main Keyboard Keys Section
        if (activeSubView == KeyboardSubView.NONE) {
            val (worldR1, worldR2, worldR3) = remember(
                currentWorldLang,
                isShifted,
                currentSettings.keyboardLayoutStyle,
                currentSettings.arabicColumnsCount,
                currentSettings.customArabicRow1,
                currentSettings.customArabicRow2,
                currentSettings.customArabicRow3
            ) {
                KeyLayouts.getRowsForLanguage(
                    lang = currentWorldLang,
                    isShifted = isShifted,
                    isArabic = isArabic,
                    layoutStyle = currentSettings.keyboardLayoutStyle,
                    columnsCount = currentSettings.arabicColumnsCount,
                    customRow1 = currentSettings.customArabicRow1,
                    customRow2 = currentSettings.customArabicRow2,
                    customRow3 = currentSettings.customArabicRow3
                )
            }

            val configuration = androidx.compose.ui.platform.LocalConfiguration.current
            val isPortrait = configuration.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val screenHeightDp = configuration.screenHeightDp

            val baseKeyHeight = remember(isPortrait, screenHeightDp) {
                if (isPortrait) {
                    when {
                        screenHeightDp < 640 -> 39.dp
                        screenHeightDp < 760 -> 42.dp
                        screenHeightDp < 900 -> 44.dp
                        else -> 46.dp
                    }
                } else {
                    when {
                        screenHeightDp < 360 -> 28.dp
                        screenHeightDp < 480 -> 32.dp
                        else -> 35.dp
                    }
                }
            }

            val calculatedKeyHeight = remember(baseKeyHeight, currentSettings.keyHeightFactor, currentSettings.keyButtonScale, isPortrait) {
                (baseKeyHeight * currentSettings.keyHeightFactor * currentSettings.keyButtonScale)
                    .coerceIn(
                        minimumValue = if (isPortrait) 32.dp else 24.dp,
                        maximumValue = if (isPortrait) 48.dp else 36.dp
                    )
            }

            val calculatedNumberKeyHeight = remember(calculatedKeyHeight) {
                (calculatedKeyHeight * 0.84f).coerceAtLeast(28.dp)
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.5.dp, vertical = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(if (isPortrait) 3.5.dp else 2.dp)
                    ) {
                        // Optional Number Row
                        if (currentSettings.numberRowEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                            ) {
                                val row = if (isArabic && !currentSettings.forceEnglishNumbers) KeyLayouts.numbersRowAr else KeyLayouts.numbersRowEn
                                row.forEach { key ->
                                    val digitChar = (key.type as KeyType.Character).primary
                                    key("num_$digitChar") {
                                        KeyButton(
                                            key = key,
                                            theme = activeTheme,
                                            isShifted = isShifted,
                                            showKeyPopup = currentSettings.keyPopupEnabled,
                                            keyHeight = calculatedNumberKeyHeight,
                                            keyFontSizeFactor = currentSettings.keyFontSizeFactor,
                                            keyButtonScale = currentSettings.keyButtonScale,
                                            longPressDelayMs = currentSettings.longPressDelayMs.toLong(),
                                            onPerformFeedback = {
                                                dismissLongPressPopup()
                                                performFeedback(it)
                                            },
                                            modifier = Modifier.weight(key.weight),
                                            onClick = {
                                                dismissLongPressPopup()
                                                sendText(digitChar)
                                            },
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
                        }

                        // Row 1 (With number hints matching Screenshots 3 & 4)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
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
                                key("${currentWorldLang.code}_r1_$char") {
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
                                        onPerformFeedback = {
                                            dismissLongPressPopup()
                                            performFeedback(it)
                                        },
                                        modifier = Modifier.weight(key.weight),
                                        onClick = {
                                            dismissLongPressPopup()
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
                        }

                        // Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                        ) {
                            val row = when {
                                isSymbolsMode && !isMoreSymbolsMode -> KeyLayouts.symbolsRow2
                                isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.symbolsMoreRow2
                                else -> worldR2
                            }
                            row.forEach { key ->
                                val char = (key.type as KeyType.Character).primary
                                key("${currentWorldLang.code}_r2_$char") {
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
                                        onPerformFeedback = {
                                            dismissLongPressPopup()
                                            performFeedback(it)
                                        },
                                        modifier = Modifier.weight(key.weight),
                                        onClick = {
                                            dismissLongPressPopup()
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
                        }

                        // Row 3
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val row = when {
                                isSymbolsMode && !isMoreSymbolsMode -> KeyLayouts.symbolsRow3
                                isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.symbolsMoreRow3
                                else -> worldR3
                            }

                            row.forEach { key ->
                                key("${currentWorldLang.code}_r3_${key.type}") {
                                    when (key.type) {
                                        is KeyType.Shift -> {
                                            SpecialKeyButton(
                                                icon = Icons.Default.ArrowUpward,
                                                theme = activeTheme,
                                                isActive = isShifted,
                                                keyHeight = calculatedKeyHeight,
                                                modifier = Modifier.weight(key.weight),
                                                onClick = {
                                                    dismissLongPressPopup()
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
                                                onDelete = {
                                                    dismissLongPressPopup()
                                                    sendDelete()
                                                },
                                                onDeleteWord = {
                                                    dismissLongPressPopup()
                                                    performFeedback(isDelete = true)
                                                    if (currentComposingText.isNotEmpty()) {
                                                        val words = currentComposingText.trimEnd().split(" ")
                                                        currentComposingText = if (words.size > 1) words.dropLast(1).joinToString(" ") + " " else ""
                                                    }
                                                    val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                    if (ic != null) {
                                                        ic.deleteSurroundingText(10, 0)
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
                                                        dismissLongPressPopup()
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
                                                        dismissLongPressPopup()
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
                                                    onPerformFeedback = {
                                                        dismissLongPressPopup()
                                                        performFeedback(it)
                                                    },
                                                    modifier = Modifier.weight(key.weight),
                                                    onClick = {
                                                        dismissLongPressPopup()
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
                                        else -> {}
                                    }
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
                                 .height(calculatedKeyHeight),
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
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

                            // 2. Translation Button (Replaces Language Switch; Hold for 3s to translate text)
                            var isTranslatePressed by remember { mutableStateOf(false) }
                            var translateJob by remember { mutableStateOf<Job?>(null) }
                            val translateBg by animateColorAsState(
                                targetValue = if (isTranslatePressed || translateHoldProgress > 0f) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isTranslatePressed) 20 else 90),
                                label = "translate_bg"
                            )
                            val translateScale by animateFloatAsState(
                                targetValue = if (isTranslatePressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "translate_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = translateScale; scaleY = translateScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(translateBg)
                                    .border(
                                        1.dp,
                                        if (translateHoldProgress > 0f || isTranslationBarOpen) Color(0xFF00E5FF) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)),
                                        RoundedCornerShape(activeTheme.cornerRadius.dp)
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isTranslatePressed = true
                                                performFeedback(isSpecial = true)
                                                translateJob = coroutineScope.launch {
                                                    isTranslateButtonHolding = true
                                                    translateHoldProgress = 0f
                                                    val totalTime = 3000L // Hold for 3 seconds to translate
                                                    val step = 40L
                                                    var elapsed = 0L
                                                    while (elapsed < totalTime) {
                                                        delay(step)
                                                        elapsed += step
                                                        translateHoldProgress = (elapsed.toFloat() / totalTime).coerceIn(0f, 1f)
                                                    }
                                                    // 3 seconds reached -> translate text
                                                    performFeedback(durationMs = 60, isSpecial = true)
                                                    execute3SecTranslation()
                                                    isTranslateButtonHolding = false
                                                    translateHoldProgress = 0f
                                                }
                                                try {
                                                    tryAwaitRelease()
                                                } finally {
                                                    translateJob?.cancel()
                                                    isTranslateButtonHolding = false
                                                    translateHoldProgress = 0f
                                                    isTranslatePressed = false
                                                }
                                            },
                                            onTap = {
                                                // Quick tap toggles inline translation bar
                                                performFeedback(isSpecial = true)
                                                isTranslationBarOpen = !isTranslationBarOpen
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (translateHoldProgress in 0.03f..0.99f) {
                                        CircularProgressIndicator(
                                            progress = { translateHoldProgress },
                                            modifier = Modifier.size(24.dp),
                                            color = Color(0xFF00E5FF),
                                            strokeWidth = 2.dp,
                                            trackColor = Color.Transparent
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "Translate (Hold 3s to translate)",
                                        tint = if (translateHoldProgress > 0f || isTranslationBarOpen) Color(0xFF00E5FF) else Color(activeTheme.accentColor),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            // 3. Emoji shortcut button or Clipboard button (Swappable!)
                            val isLeftSlotClipboard = currentSettings.swapClipboardAndEmoji
                            var isSlot3Pressed by remember { mutableStateOf(false) }
                            val slot3Bg by animateColorAsState(
                                targetValue = if (isSlot3Pressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isSlot3Pressed) 20 else 90),
                                label = "slot3_bg"
                            )
                            val slot3Scale by animateFloatAsState(
                                targetValue = if (isSlot3Pressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "slot3_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.95f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = slot3Scale; scaleY = slot3Scale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(slot3Bg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(isLeftSlotClipboard) {
                                        detectTapGestures(
                                            onPress = {
                                                isSlot3Pressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isSlot3Pressed = false
                                            },
                                            onTap = {
                                                if (isLeftSlotClipboard) {
                                                    activeSubView = if (activeSubView == KeyboardSubView.CLIPBOARD) KeyboardSubView.NONE else KeyboardSubView.CLIPBOARD
                                                } else {
                                                    activeSubView = if (activeSubView == KeyboardSubView.EMOJI) KeyboardSubView.NONE else KeyboardSubView.EMOJI
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLeftSlotClipboard) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = "Clipboard",
                                        tint = Color(activeTheme.accentColor),
                                        modifier = Modifier.size(19.dp)
                                    )
                                } else {
                                    Text(
                                        text = "😊",
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            // 4. Spacebar in the center with active world language name and swipe language switch
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
                            val currentSwipeEnabled by rememberUpdatedState(currentSettings.swipeSpaceSwitchLanguage)
                            val currentLangRef by rememberUpdatedState(currentLangCode)

                            Box(
                                modifier = Modifier
                                    .weight(3.1f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = spaceScale; scaleY = spaceScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(spaceBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(Unit) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            isSpacePressed = true
                                            var totalDrag = 0f
                                            var hasSwitchedLanguage = false
                                            var isDragGesture = false
                                            val swipeThreshold = 34.dp.toPx()

                                            try {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                                    if (!change.pressed) {
                                                        // Pointer lifted
                                                        change.consume()
                                                        if (!isDragGesture && !hasSwitchedLanguage) {
                                                            performFeedback(isSpace = true)
                                                            sendText(" ")
                                                        }
                                                        break
                                                    }

                                                    val deltaX = change.position.x - down.position.x
                                                    totalDrag = deltaX

                                                    if (kotlin.math.abs(totalDrag) > 12.dp.toPx()) {
                                                        isDragGesture = true
                                                    }

                                                    if (currentSwipeEnabled && !hasSwitchedLanguage && kotlin.math.abs(totalDrag) >= swipeThreshold) {
                                                        hasSwitchedLanguage = true
                                                        change.consume()
                                                        performFeedback(isSpecial = true)
                                                        val nextLang = if (totalDrag > 0) {
                                                            InputLanguagesManager.getNextActiveLanguage(context, currentLangRef)
                                                        } else {
                                                            InputLanguagesManager.getPreviousActiveLanguage(context, currentLangRef)
                                                        }
                                                        InputLanguagesManager.setCurrentLanguageCode(context, nextLang.code)
                                                        currentLangCode = nextLang.code
                                                        onLanguageChanged?.invoke(nextLang.code)

                                                        // Clear composing text and finish composing in the target app
                                                        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                        try {
                                                            ic?.finishComposingText()
                                                        } catch (_: Exception) {}
                                                        currentComposingText = ""
                                                    }
                                                }
                                            } finally {
                                                isSpacePressed = false
                                            }
                                        }
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

                            // 6. Right slot button (Emoji or Clipboard - Swappable!)
                            var isSlot6Pressed by remember { mutableStateOf(false) }
                            val slot6Bg by animateColorAsState(
                                targetValue = if (isSlot6Pressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isSlot6Pressed) 20 else 90),
                                label = "slot6_bg"
                            )
                            val slot6Scale by animateFloatAsState(
                                targetValue = if (isSlot6Pressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "slot6_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.85f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = slot6Scale; scaleY = slot6Scale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(slot6Bg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(isLeftSlotClipboard) {
                                        detectTapGestures(
                                            onPress = {
                                                isSlot6Pressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isSlot6Pressed = false
                                            },
                                            onTap = {
                                                if (isLeftSlotClipboard) {
                                                    activeSubView = if (activeSubView == KeyboardSubView.EMOJI) KeyboardSubView.NONE else KeyboardSubView.EMOJI
                                                } else {
                                                    activeSubView = if (activeSubView == KeyboardSubView.CLIPBOARD) KeyboardSubView.NONE else KeyboardSubView.CLIPBOARD
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLeftSlotClipboard) {
                                    Text(
                                        text = "😊",
                                        fontSize = 18.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = "Clipboard",
                                        tint = Color(activeTheme.accentColor),
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
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
                                        if (isEnterInNewlineMode) {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF00E5FF),
                                                    Color(0xFF0284C7).copy(alpha = if (isEnterPressed) 0.75f else 0.9f)
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(activeTheme.enterButtonColor),
                                                    Color(activeTheme.enterButtonColor).copy(alpha = if (isEnterPressed) 0.7f else 0.85f)
                                                )
                                            )
                                        }
                                    )
                                    .border(
                                        if (isEnterInNewlineMode) 1.5.dp else 1.dp,
                                        if (isEnterInNewlineMode) Color(0xFF00E5FF) else Color(activeTheme.accentColor).copy(alpha = 0.7f),
                                        RoundedCornerShape(activeTheme.cornerRadius.dp)
                                    )
                                    .pointerInput(isEnterInNewlineMode) {
                                        detectTapGestures(
                                            onPress = {
                                                isEnterPressed = true
                                                performFeedback(isSpecial = true)
                                                enterJob = coroutineScope.launch {
                                                    isEnterHolding = true
                                                    enterHoldProgress = 0f
                                                    val totalTime = 2000L // الضغط المطول لمدة 2 ثوان بالضبط للتبديل
                                                    val step = 40L
                                                    var elapsed = 0L
                                                    while (elapsed < totalTime) {
                                                        delay(step)
                                                        elapsed += step
                                                        enterHoldProgress = (elapsed.toFloat() / totalTime).coerceIn(0f, 1f)
                                                    }
                                                    // تم اكتمال ثانيتين كاملتين -> تبديل وضع زر انتر / السطر الثاني
                                                    performFeedback(durationMs = 60, isSpecial = true)
                                                    isEnterInNewlineMode = !isEnterInNewlineMode
                                                    newlineSwitchNotification = if (isEnterInNewlineMode) {
                                                        "↵ تم التبديل لزر السطر الثاني (New Line)"
                                                    } else {
                                                        "✓ تمت العودة لزر الإدخال / الإرسال الافتراضي"
                                                    }
                                                    isEnterHolding = false
                                                    enterHoldProgress = 0f
                                                }
                                                try {
                                                    tryAwaitRelease()
                                                } finally {
                                                    enterJob?.cancel()
                                                    isEnterHolding = false
                                                    enterHoldProgress = 0f
                                                    isEnterPressed = false
                                                }
                                            },
                                            onTap = {
                                                if (isEnterInNewlineMode && !isTranslationBarOpen) {
                                                    performFeedback(isSpecial = true)
                                                    if (onServiceNewline != null) {
                                                        onServiceNewline.invoke()
                                                    } else {
                                                        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                        if (ic != null) {
                                                            ic.commitText("\n", 1)
                                                        } else {
                                                            onDirectInsertText?.invoke("\n")
                                                        }
                                                    }
                                                } else {
                                                    sendEnter()
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isEnterInNewlineMode) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                            contentDescription = "انتقال للسطر الثاني",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "سطر ↵",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = currentImeAction.icon,
                                        contentDescription = currentImeAction.contentDescription,
                                        tint = ThemePresets.getEnterIconTint(activeTheme),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                if (isEnterHolding && enterHoldProgress in 0.05f..0.99f) {
                                    CircularProgressIndicator(
                                        progress = { enterHoldProgress },
                                        modifier = Modifier.size(34.dp),
                                        color = if (isEnterInNewlineMode) Color.White else Color(activeTheme.accentColor),
                                        strokeWidth = 2.5.dp
                                    )
                                }
                            }
                        }
                    }

                    // Floating Long-Press Character Popup Overlay (Automatically hides when any character is pressed)
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
                                                    val chosen = variant
                                                    dismissLongPressPopup()
                                                    sendText(chosen)
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
                                                dismissLongPressPopup()
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
    var isBubbleVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isTouching) {
        if (isTouching) {
            isBubbleVisible = true
        } else {
            delay(140) // Smooth, visible iOS-style preview bubble retention
            isBubbleVisible = false
        }
    }

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentFeedback by rememberUpdatedState(onPerformFeedback)

    fun triggerFeedback(durationMs: Long) {
        currentFeedback?.invoke(durationMs)
    }

    val normalKeyColor = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    val pressedKeyColor = ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    val targetKeyColor = if (isTouching) pressedKeyColor else normalKeyColor

    val animatedKeyColor by animateColorAsState(
        targetValue = targetKeyColor,
        animationSpec = tween(durationMillis = 20),
        label = "key_color"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isTouching) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 20),
        label = "key_scale"
    )

    val hintText = numberHint

    // Outer Box: Does not clip children so the preview bubble floats above the row seamlessly
    Box(
        modifier = modifier
            .height(keyHeight)
            .zIndex(if (isBubbleVisible) 100f else 0f),
        contentAlignment = Alignment.Center
    ) {
        // 1. Key Button Body (clipped & styled)
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                .pointerInput(displayChar, onLongClick != null, longPressDelayMs) {
                    detectTapGestures(
                        onPress = {
                            isTouching = true
                            triggerFeedback(0L)
                            try {
                                tryAwaitRelease()
                            } finally {
                                isTouching = false
                            }
                        },
                        onLongPress = if (onLongClick != null) {
                            {
                                triggerFeedback(30L)
                                currentOnLongClick?.invoke()
                            }
                        } else null,
                        onTap = {
                            currentOnClick()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (hintText != null) {
                Text(
                    text = hintText,
                    color = Color(theme.subtextColor).copy(alpha = 0.85f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 1.dp, start = 3.dp)
                )
            }

            val baseFontSize = if (hintText != null) 18.5.sp else 21.sp
            Text(
                text = displayChar,
                color = customTextColor ?: Color(theme.keyTextColor),
                fontSize = (baseFontSize.value * keyFontSizeFactor * keyButtonScale).sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 2. Magnificent Floating Key Preview Bubble (iOS 16 style, smoothly visible)
        AnimatedVisibility(
            visible = isBubbleVisible && showKeyPopup && displayChar.isNotBlank(),
            enter = fadeIn(animationSpec = tween(30)) + scaleIn(initialScale = 0.85f, animationSpec = tween(30)),
            exit = fadeOut(animationSpec = tween(90)) + scaleOut(targetScale = 0.90f, animationSpec = tween(90)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-52).dp)
        ) {
            KeyPreviewBubble(
                char = displayChar,
                numberHint = hintText,
                theme = theme
            )
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

@Composable
fun CustomizeTopEmojisDialog(
    theme: KeyboardTheme,
    currentEmojis: List<String>,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var workingList by remember { mutableStateOf(currentEmojis.toMutableList()) }
    var newEmojiText by remember { mutableStateOf("") }
    val popularSuggestions = listOf(
        "😂", "❤️", "🥺", "🔥", "👏", "🤍", "😍", "✨", "🤲", "🌹", "🌸", "👍",
        "👑", "💋", "💯", "🙈", "🖤", "🤩", "🕊️", "💎", "🍿", "🚀", "💡", "⚡"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(theme.backgroundColor),
            border = BorderStroke(1.2.dp, Color(theme.accentColor)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clickable(enabled = false) {}
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تخصيص شريط الابتسامات العلوي",
                        color = Color(theme.keyTextColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(theme.subtextColor),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "اضغط على أي ابتسامة لحذفها، أو اختر من القائمة لإضافتها",
                    color = Color(theme.subtextColor),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Current Emojis Chips (scrollable row)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(theme.toolbarColor), RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(workingList) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.5f)),
                            modifier = Modifier.clickable {
                                workingList = workingList.toMutableList().apply { remove(item) }
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = item, color = Color(theme.keyTextColor), fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Custom Text / Emoji Field + Backspace & Add
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BasicTextField(
                        value = newEmojiText,
                        onValueChange = { newEmojiText = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(theme.keyTextColor),
                            fontSize = 13.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(Color(theme.keyBackgroundColor), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        decorationBox = { innerTextField ->
                            if (newEmojiText.isEmpty()) {
                                Text(
                                    "اكتب أو اختر من الرموز أدناه...",
                                    color = Color(theme.subtextColor).copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    // Backspace button for deleting character
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(theme.keyBackgroundColor),
                        border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.5f)),
                        modifier = Modifier.clickable {
                            if (newEmojiText.isNotEmpty()) {
                                newEmojiText = newEmojiText.dropLast(1)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = Color(theme.subtextColor),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp).size(18.dp)
                        )
                    }

                    // Add button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(theme.accentColor),
                        modifier = Modifier.clickable {
                            val trimmed = newEmojiText.trim()
                            if (trimmed.isNotEmpty() && !workingList.contains(trimmed)) {
                                workingList = workingList.toMutableList().apply { add(trimmed) }
                                newEmojiText = ""
                            }
                        }
                    ) {
                        Text(
                            text = "إضافة",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Built-in Keyboard Tabs & Emoji Grid (إظهار الكيبورد والابتسامات المدمجة)
                var activeEmojiCat by remember { mutableStateOf(0) }
                val emojiCategories = listOf(
                    "😊 فيسات" to listOf("😂", "😍", "🥺", "😭", "🤣", "😇", "😋", "😎", "🥳", "🤔", "😴", "🙄", "😤", "🤐", "😷", "🤯", "🥶", "🥵", "😡", "🤡", "😏", "🤤", "🤭", "🤫"),
                    "❤️ قلوب" to listOf("❤️", "🤍", "🖤", "💖", "💔", "💕", "💓", "💗", "💘", "💙", "💚", "💛", "💜", "🤎", "🧡", "❣️", "💌", "💋", "💝", "💞"),
                    "🔥 تفاعلات" to listOf("🔥", "👏", "👍", "👎", "👑", "✨", "💯", "🤲", "🌹", "🌸", "⚡", "💎", "🕊️", "🍿", "🚀", "💡", "🌙", "☀️", "🎉", "💐"),
                    "🔣 رموز" to listOf("ـ", "؛", "،", "؟", "!", "@", "#", "$", "%", "&", "*", "(", ")", "-", "+", "=", "/", ":", "•", "✓")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    emojiCategories.indices.forEach { idx ->
                        val isCatSelected = activeEmojiCat == idx
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCatSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                            border = BorderStroke(1.dp, if (isCatSelected) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f).clickable { activeEmojiCat = idx }
                        ) {
                            Text(
                                text = emojiCategories[idx].first,
                                color = if (isCatSelected) Color(theme.accentColor) else Color(theme.keyTextColor),
                                fontSize = 10.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Grid of clickable emojis/symbols (انقر لإضافة الإيموجي مباشرة للشريط)
                val currentCategoryItems = emojiCategories[activeEmojiCat].second
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(theme.toolbarColor).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(currentCategoryItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(0.7.dp, Color(theme.borderColor).copy(alpha = 0.4f)),
                            modifier = Modifier.clickable {
                                if (!workingList.contains(item)) {
                                    workingList = workingList.toMutableList().apply { add(item) }
                                }
                                newEmojiText = (newEmojiText + item).take(10)
                            }
                        ) {
                            Text(
                                text = item,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Reset & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            workingList = mutableListOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
                        }
                    ) {
                        Text("استعادة الافتراضي", color = Color(theme.subtextColor), fontSize = 11.sp)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(theme.accentColor),
                        modifier = Modifier.clickable {
                            if (workingList.isEmpty()) {
                                workingList = mutableListOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
                            }
                            onSave(workingList)
                        }
                    ) {
                        Text(
                            text = "حفظ الابتسامات",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
