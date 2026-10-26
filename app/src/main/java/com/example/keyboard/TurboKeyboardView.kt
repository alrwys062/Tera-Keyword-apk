package com.example.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import kotlinx.coroutines.isActive
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

fun isNumericInputType(editorInfo: EditorInfo?): Boolean {
    if (editorInfo == null) return false
    val inputType = editorInfo.inputType
    if (inputType == InputType.TYPE_NULL) return false
    val inputClass = inputType and InputType.TYPE_MASK_CLASS
    if (inputClass == InputType.TYPE_CLASS_PHONE ||
        inputClass == InputType.TYPE_CLASS_NUMBER ||
        inputClass == InputType.TYPE_CLASS_DATETIME) {
        return true
    }
    val variation = inputType and InputType.TYPE_MASK_VARIATION
    if (variation == InputType.TYPE_TEXT_VARIATION_PHONETIC) return true

    val hint = editorInfo.hintText?.toString()?.lowercase() ?: ""
    val label = editorInfo.label?.toString()?.lowercase() ?: ""
    val fieldName = editorInfo.fieldName?.lowercase() ?: ""
    val isOtpOrPin = listOf("otp", "pin", "رمز", "كود", "هاتف", "جوال", "phone", "mobile", "cvv", "numpad", "رقم", "verification").any {
        hint.contains(it) || label.contains(it) || fieldName.contains(it)
    }
    return isOtpOrPin && (inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE == 0)
}

fun isPhoneInputType(editorInfo: EditorInfo?): Boolean {
    if (editorInfo == null) return false
    return (editorInfo.inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_PHONE
}

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
    val isSearchType = (editorInfo.inputType and InputType.TYPE_MASK_VARIATION) == InputType.TYPE_TEXT_VARIATION_FILTER

    // الكشف الذكي عن حقول البحث بالكلمات المفتاحية والمعرفات
    val hintStr = editorInfo.hintText?.toString()?.lowercase() ?: ""
    val fieldNameStr = editorInfo.fieldName?.lowercase() ?: ""
    val labelStr = editorInfo.label?.toString()?.lowercase() ?: ""
    val isSearchKeyword = listOf("بحث", "ابحث", "search", "find", "query", "searchbox").any {
        hintStr.contains(it) || fieldNameStr.contains(it) || labelStr.contains(it)
    }

    // إذا كان حقل بحث: يظهر زر البحث دائماً
    if (action == EditorInfo.IME_ACTION_SEARCH || editorInfo.actionId == EditorInfo.IME_ACTION_SEARCH || isSearchType || isSearchKeyword) {
        return ImeActionInfo(EditorInfo.IME_ACTION_SEARCH, Icons.Default.Search, "بحث", false)
    }

    // إذا كان حقل نصوص أحادي السطر لنموذج (التالي / السابق)
    if (!isMultiLine) {
        if (action == EditorInfo.IME_ACTION_NEXT || editorInfo.actionId == EditorInfo.IME_ACTION_NEXT) {
            return ImeActionInfo(EditorInfo.IME_ACTION_NEXT, Icons.AutoMirrored.Filled.ArrowForward, "التالي", false)
        }
        if (action == EditorInfo.IME_ACTION_PREVIOUS || editorInfo.actionId == EditorInfo.IME_ACTION_PREVIOUS) {
            return ImeActionInfo(EditorInfo.IME_ACTION_PREVIOUS, Icons.AutoMirrored.Filled.ArrowBack, "السابق", false)
        }
    }

    // في جميع أماكن النصوص والمحادثات والرسائل: يظهر سهم السطر الجديد والانتقال المباشر للسطر الثاني بضغطة واحدة
    return ImeActionInfo(
        actionId = EditorInfo.IME_ACTION_UNSPECIFIED,
        icon = Icons.AutoMirrored.Filled.KeyboardReturn,
        contentDescription = "سطر جديد",
        isNewline = true
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
    inputSessionId: Long = 0L,
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
    var isCapsLockActive by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }
    var isMoreSymbolsMode by remember { mutableStateOf(false) }
    var isNumberPadMode by remember { mutableStateOf(false) }
    var activeSubView by remember { mutableStateOf(KeyboardSubView.NONE) }
    var activeTranslationPickerForSource by remember { mutableStateOf<Boolean?>(null) }

    val currentImeAction = remember(editorInfo, getCurrentEditorInfo) {
        resolveImeAction(getCurrentEditorInfo?.invoke() ?: editorInfo)
    }

    // Auto-detect number and phone input types (التعرف على إدخال الأرقام والهاتف تلقائياً)
    LaunchedEffect(inputSessionId, editorInfo) {
        val info = getCurrentEditorInfo?.invoke() ?: editorInfo
        if (isNumericInputType(info)) {
            isNumberPadMode = true
            isSymbolsMode = false
            isMoreSymbolsMode = false
        } else {
            isNumberPadMode = false
        }
    }

    LaunchedEffect(isArabic) {
        if (isArabic) {
            isShifted = false
        }
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
                minimumValue = if (isPortrait) 24.dp else 18.dp,
                maximumValue = if (isPortrait) 85.dp else 56.dp
            )
    }

    val calculatedNumberKeyHeight = remember(calculatedKeyHeight) {
        (calculatedKeyHeight * 0.84f).coerceAtLeast(24.dp)
    }

    val calculatedKeyboardHeight = remember(
        calculatedKeyHeight,
        currentSettings.numberRowEnabled,
        currentSettings.suggestionsEnabled,
        currentSettings.topQuickEmojiRowEnabled,
        currentSettings.emojiBarHeightFactor,
        isPortrait
    ) {
        val numRowH = if (currentSettings.numberRowEnabled) (calculatedKeyHeight * 0.88f + 4.dp) else 0.dp
        val emojiRowH = if (currentSettings.topQuickEmojiRowEnabled) (36.dp * currentSettings.emojiBarHeightFactor + 4.dp) else 0.dp
        val suggH = if (currentSettings.suggestionsEnabled) 36.dp else 0.dp
        val rowsH = (calculatedKeyHeight + 4.dp) * 5
        (numRowH + emojiRowH + suggH + rowsH).coerceAtLeast(if (isPortrait) 220.dp else 160.dp)
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
    var selectedVariantIndex by remember { mutableIntStateOf(0) }

    // Live text tracking for prediction and special 4-sec translate
    var currentComposingText by remember { mutableStateOf("") }

    // 3-sec Translate button hold state
    var isTranslateButtonHolding by remember { mutableStateOf(false) }
    var translateHoldProgress by remember { mutableFloatStateOf(0f) }
    var translateHoldMessage by remember { mutableStateOf("") }

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

    // Prediction suggestions + auto-correction + contextual next-word predictions
    val suggestions: List<PredictionEngine.PredictionResult> = remember(currentComposingText, currentWorldLang, userWords, shortcuts) {
        if (currentSettings.suggestionsEnabled) {
            val lastWord = currentComposingText.trim().split(" ").lastOrNull() ?: ""
            if (lastWord.isNotBlank()) {
                val shortcutMatch = shortcuts.find { it.trigger.equals(lastWord, ignoreCase = true) }
                val baseList = PredictionEngine.getPredictionsWithCorrection(lastWord, isArabic, userWords)
                if (shortcutMatch != null) {
                    listOf(PredictionEngine.PredictionResult(shortcutMatch.expansion)) + baseList.take(2)
                } else {
                    baseList
                }
            } else {
                if (isArabic) {
                    listOf(
                        PredictionEngine.PredictionResult("مرحبا"),
                        PredictionEngine.PredictionResult("السلام عليكم"),
                        PredictionEngine.PredictionResult("صباح الخير")
                    )
                } else {
                    listOf(
                        PredictionEngine.PredictionResult("Hello"),
                        PredictionEngine.PredictionResult("Thanks"),
                        PredictionEngine.PredictionResult("Good")
                    )
                }
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
            selectedVariantIndex = 0
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
        if (textToInsert == " " || textToInsert.contains(" ") || textToInsert in listOf("\n", "\r", ".", "!", "?", "،", "؛", "؟", ",", ";", ":")) {
            currentComposingText = ""
        } else {
            currentComposingText = (currentComposingText + textToInsert).takeLast(30)
        }

        // التحويل التلقائي للأحرف الصغيرة في اللغة الإنجليزية بعد كتابة أول حرف كبير
        if (!isArabic && isShifted && !isCapsLockActive && textToInsert.length == 1 && textToInsert[0].isUpperCase()) {
            isShifted = false
        }
        if (textToInsert in listOf(".", "!", "?", "\n", "\r")) {
            if (!isArabic && !isCapsLockActive && currentSettings.autoCapitalization) {
                isShifted = true
            }
        }


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

    // السحب في الأحرف العائمة: مسموح باتجاه اليمين فقط وممنوع لليسار نهائياً
    val onDragVariantHandler: (Float) -> Unit = { deltaX ->
        if (longPressVariants.isNotEmpty()) {
            if (deltaX > 0f) {
                val stepPx = 28f
                val step = (deltaX / stepPx).toInt()
                val targetIdx = step.coerceIn(0, longPressVariants.lastIndex)
                if (targetIdx != selectedVariantIndex) {
                    selectedVariantIndex = targetIdx
                    performFeedback(12L)
                }
            } else {
                if (selectedVariantIndex != 0) {
                    selectedVariantIndex = 0
                }
            }
        }
    }

    // الانتقال للسطر الثاني مباشرة
    fun insertNewlineDirectly() {
        dismissLongPressPopup()
        if (onServiceNewline != null) {
            onServiceNewline.invoke()
        } else {
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            if (ic != null) {
                val down = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                val up = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                if (!down || !up) {
                    ic.commitText("\n", 1)
                }
            } else {
                onDirectInsertText?.invoke("\n")
            }
        }
        currentComposingText = ""
    }

    val onReleaseVariantHandler: () -> Unit = {
        if (longPressVariants.isNotEmpty() && selectedVariantIndex in longPressVariants.indices) {
            val chosen = longPressVariants[selectedVariantIndex]
            dismissLongPressPopup()
            sendText(chosen)
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
        val info = getCurrentEditorInfo?.invoke() ?: editorInfo
        val actionInfo = resolveImeAction(info)

        // إذا كان حقل بحث: تنفيذ البحث بضغطة زر واحدة
        if (actionInfo.actionId == EditorInfo.IME_ACTION_SEARCH) {
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            if (ic != null) {
                val handled = ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
                if (!handled) {
                    val down = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    val up = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                    if (!down || !up) {
                        ic.commitText("\n", 1)
                    }
                }
            } else if (onServiceEnter != null) {
                onServiceEnter.invoke()
            }
            currentComposingText = ""
            return
        }

        // إذا كان حقل تنقل أحادي السطر (التالي أو السابق): تنفيذ أمر التنقل
        if (!actionInfo.isNewline && (actionInfo.actionId == EditorInfo.IME_ACTION_NEXT || actionInfo.actionId == EditorInfo.IME_ACTION_PREVIOUS)) {
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            if (ic != null) {
                ic.performEditorAction(actionInfo.actionId)
            } else {
                onServiceEnter?.invoke()
            }
            currentComposingText = ""
            return
        }

        // في جميع أماكن النصوص والمحادثات: انتقال مباشر للسطر الثاني بضغطة زر واحدة بسرعة وبدون أي انتظار
        insertNewlineDirectly()

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
                translateHoldMessage = "جاري الترجمة الفورية..."
                val source = if (isArabic) "ar" else "en"
                val target = if (isArabic) "en" else "ar"
                val result = TranslationEngine.translate(textToTranslate, source, target)
                replaceAllText(result)
                translateHoldMessage = "تمت الترجمة بنجاح ✓"
                delay(1200)
                translateHoldMessage = ""
            }
        }
    }

    val isGlassTheme = theme.isGlass || theme.id == "liquid_glass_blur"
    val containerShape = if (isGlassTheme) RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp) else RoundedCornerShape(0.dp)
    val containerBorder = if (isGlassTheme) BorderStroke(1.dp, Color(0x33FFFFFF)) else null

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
                .then(if (containerBorder != null) Modifier.border(containerBorder, containerShape) else Modifier)
                .clip(containerShape)
                .then(if (theme.backgroundImageUri.isNullOrBlank()) bgModifier else Modifier)
                .navigationBarsPadding()
        ) {
        // Top status/alert banner when Translate 3-sec hold is active
        AnimatedVisibility(visible = translateHoldProgress > 0.03f || translateHoldMessage.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00E5FF).copy(alpha = 0.25f))
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
                if (sub == KeyboardSubView.CLIPBOARD) {
                    try {
                        prefs.syncWithSystemClipboard(context)
                    } catch (_: Exception) {}
                }
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
                onOpenSourcePicker = {
                    activeTranslationPickerForSource = true
                    activeSubView = KeyboardSubView.NONE
                },
                onOpenTargetPicker = {
                    activeTranslationPickerForSource = false
                    activeSubView = KeyboardSubView.NONE
                },
                onClose = {
                    isTranslationBarOpen = false
                    activeTranslationPickerForSource = null
                },
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

        // Sub-views drawers (Language Picker, Stickers, Clipboard, Emoji, GIF, AI Assistant, Photos, Customize Toolbar)
        // Instant 0ms display: completely solves lag when opening clipboard or translation
        if (activeTranslationPickerForSource != null) {
            LanguagePickerDrawer(
                theme = activeTheme,
                isSource = activeTranslationPickerForSource == true,
                selectedCode = if (activeTranslationPickerForSource == true) translationSource else translationTarget,
                keyboardHeight = calculatedKeyboardHeight,
                onSelectLanguage = { selectedCode ->
                    if (activeTranslationPickerForSource == true) {
                        translationSource = selectedCode
                        val updated = currentSettings.copy(translationSource = selectedCode)
                        currentSettings = updated
                        prefs.saveSettings(updated)
                    } else {
                        translationTarget = selectedCode
                        val updated = currentSettings.copy(translationTarget = selectedCode)
                        currentSettings = updated
                        prefs.saveSettings(updated)
                    }
                    activeTranslationPickerForSource = null
                },
                onClose = {
                    activeTranslationPickerForSource = null
                }
            )
        } else if (activeSubView != KeyboardSubView.NONE) {
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
                            keyboardHeight = calculatedKeyboardHeight,
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
                        // 2-column grid, closes on paste, maintains scroll position, saves forever, maintains exact keyboard height
                        ClipboardDrawer(
                            theme = theme,
                            prefs = prefs,
                            keyboardHeight = calculatedKeyboardHeight,
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
                    KeyboardSubView.TELEGRAM_CHANNELS -> {
                        TelegramChannelsSubView(
                            theme = theme,
                            onClose = { activeSubView = KeyboardSubView.NONE }
                        )
                    }
                    else -> {}
                }
            }
        }

        // Suggestions Bar with spell correction & dictionary addition
        if (activeTranslationPickerForSource == null && activeSubView == KeyboardSubView.NONE && !isNumberPadMode && settings.suggestionsEnabled) {
            KeyboardSuggestionsBar(
                theme = theme,
                suggestions = suggestions,
                onSuggestionClick = { word ->
                    performFeedback()
                    val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                    if (ic != null) {
                        ic.beginBatchEdit()
                        try {
                            val textBefore = try {
                                ic.getTextBeforeCursor(60, 0)?.toString() ?: ""
                            } catch (_: Exception) { "" }
                            val lastTokenMatch = Regex("""[^\s\p{Punct}]+$""").find(textBefore)
                            val tokenLen = lastTokenMatch?.value?.length ?: currentComposingText.length
                            val toDelete = if (tokenLen > 0) tokenLen else currentComposingText.length
                            if (toDelete > 0) {
                                ic.deleteSurroundingText(toDelete, 0)
                            }
                            ic.commitText("$word ", 1)
                        } catch (_: Exception) {
                            ic.commitText("$word ", 1)
                        } finally {
                            ic.endBatchEdit()
                        }
                    } else {
                        sendText("$word ")
                    }
                    currentComposingText = ""
                },
                onAddWordToDictionary = { word ->
                    userDict.addWord(word)
                    userWords = userDict.getUserWords()
                    performFeedback()
                }
            )
        }

        // Quick Shortcuts / Emojis Row (Customizable for user - إضافة وحذف الابتسامات)
        if (activeTranslationPickerForSource == null && activeSubView == KeyboardSubView.NONE && !isNumberPadMode && settings.topQuickEmojiRowEnabled) {
            val config = androidx.compose.ui.platform.LocalConfiguration.current
            val isPort = config.orientation != android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val dynamicRowHeight = (if (isPort) 30.dp else 26.dp) * currentSettings.emojiBarHeightFactor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dynamicRowHeight)
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
                                fontSize = (13 * currentSettings.emojiButtonSizeFactor).sp,
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
                currentHeightFactor = currentSettings.emojiBarHeightFactor,
                currentButtonSizeFactor = currentSettings.emojiButtonSizeFactor,
                onSave = { updatedList, newHeightFactor, newButtonSizeFactor ->
                    customTopEmojis = updatedList
                    prefs.saveCustomTopEmojis(updatedList)
                    val updatedSettings = currentSettings.copy(
                        emojiBarHeightFactor = newHeightFactor,
                        emojiButtonSizeFactor = newButtonSizeFactor
                    )
                    currentSettings = updatedSettings
                    prefs.saveSettings(updatedSettings)
                    isCustomizeTopEmojisOpen = false
                },
                onDismiss = { isCustomizeTopEmojisOpen = false }
            )
        }

        // Main Keyboard Keys Section
        if (activeTranslationPickerForSource == null && activeSubView == KeyboardSubView.NONE) {
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

            if (isNumberPadMode) {
                val effectiveEi = getCurrentEditorInfo?.invoke() ?: editorInfo
                val isPhone = isPhoneInputType(effectiveEi)
                val numRowHeight = remember(calculatedKeyHeight, isPortrait) {
                    if (isPortrait) (calculatedKeyHeight * 1.15f).coerceIn(36.dp, 85.dp)
                    else 36.dp
                }
                KeyboardNumericPad(
                    theme = activeTheme,
                    isPhoneField = isPhone,
                    actionInfo = currentImeAction,
                    rowHeight = numRowHeight,
                    currentLanguageName = if (isArabic) "ع" else "En",
                    isArabicDefault = isArabic,
                    forceEnglishNumbers = currentSettings.forceEnglishNumbers,
                    onSendText = { text ->
                        sendText(text)
                    },
                    onDeleteChar = {
                        sendDelete()
                    },
                    onActionClick = {
                        executeNormalEnter()
                    },
                    onSwitchToAlphabet = {
                        isNumberPadMode = false
                    },
                    onFeedback = { dur, isSpec ->
                        performFeedback(dur, isSpec)
                    }
                )
            } else {
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
                                                if (variants.isNotEmpty()) {
                                                    longPressChar = digitChar
                                                    longPressVariants = variants
                                                    selectedVariantIndex = 0
                                                }
                                            },
                                            onDragVariant = onDragVariantHandler,
                                            onReleaseVariant = onReleaseVariantHandler
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
                                val hint = if (isArabic) {
                                    if (char == "ج" || char == "ك") null
                                    else {
                                        val raw = LongPressVariantsManager.arabicRow1Hints[char]
                                        if (raw != null && !currentSettings.forceEnglishNumbers) {
                                            when (raw) {
                                                "1" -> "١"; "2" -> "٢"; "3" -> "٣"; "4" -> "٤"; "5" -> "٥"
                                                "6" -> "٦"; "7" -> "٧"; "8" -> "٨"; "9" -> "٩"; "0" -> "٠"
                                                else -> raw
                                            }
                                        } else raw
                                    }
                                } else LongPressVariantsManager.englishRow1Hints[char.lowercase()]
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
                                            if (variants.isNotEmpty()) {
                                                longPressChar = char
                                                longPressVariants = variants
                                                selectedVariantIndex = 0
                                            }
                                        },
                                        onDragVariant = onDragVariantHandler,
                                        onReleaseVariant = onReleaseVariantHandler
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
                                            if (variants.isNotEmpty()) {
                                                longPressChar = char
                                                longPressVariants = variants
                                                selectedVariantIndex = 0
                                            }
                                        },
                                        onDragVariant = onDragVariantHandler,
                                        onReleaseVariant = onReleaseVariantHandler
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
                                                icon = if (isCapsLockActive) Icons.Default.Lock else Icons.Default.ArrowUpward,
                                                theme = activeTheme,
                                                isActive = isShifted || isCapsLockActive,
                                                keyHeight = calculatedKeyHeight,
                                                modifier = Modifier.weight(key.weight),
                                                onLongClick = {
                                                    dismissLongPressPopup()
                                                    performFeedback(isSpecial = true)
                                                    isCapsLockActive = !isCapsLockActive
                                                    isShifted = isCapsLockActive
                                                },
                                                onClick = {
                                                    dismissLongPressPopup()
                                                    performFeedback(isSpecial = true)
                                                    if (isCapsLockActive) {
                                                        isCapsLockActive = false
                                                        isShifted = false
                                                    } else {
                                                        isShifted = !isShifted
                                                    }
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
                                                        if (variants.isNotEmpty()) {
                                                            longPressChar = char
                                                            longPressVariants = variants
                                                            selectedVariantIndex = 0
                                                        }
                                                    },
                                                    onDragVariant = onDragVariantHandler,
                                                    onReleaseVariant = onReleaseVariantHandler
                                                )
                                            }
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        }


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
                                            onLongPress = {
                                                performFeedback(isSpecial = true)
                                                isNumberPadMode = true
                                                isSymbolsMode = false
                                                isMoreSymbolsMode = false
                                            },
                                            onTap = {
                                                val info = getCurrentEditorInfo?.invoke() ?: editorInfo
                                                if (isNumericInputType(info)) {
                                                    isNumberPadMode = true
                                                    isSymbolsMode = false
                                                    isMoreSymbolsMode = false
                                                } else {
                                                    isSymbolsMode = !isSymbolsMode
                                                    isMoreSymbolsMode = false
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val info = getCurrentEditorInfo?.invoke() ?: editorInfo
                                val isNumField = isNumericInputType(info)
                                Text(
                                    text = if (isNumField) "123" else if (isSymbolsMode) currentWorldLang.nameNative else "123!#()",
                                    color = Color(activeTheme.keyTextColor),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // 2. Dedicated Language Switcher Button [🌐 / 文A]
                            var isLangBtnPressed by remember { mutableStateOf(false) }
                            val langBtnBg by animateColorAsState(
                                targetValue = if (isLangBtnPressed) bottomKeyPressedBg else bottomKeyBg,
                                animationSpec = tween(durationMillis = if (isLangBtnPressed) 20 else 90),
                                label = "lang_btn_bg"
                            )
                            val langBtnScale by animateFloatAsState(
                                targetValue = if (isLangBtnPressed) 0.95f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                label = "lang_btn_scale"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = langBtnScale; scaleY = langBtnScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(langBtnBg)
                                    .border(
                                        1.dp,
                                        Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)),
                                        RoundedCornerShape(activeTheme.cornerRadius.dp)
                                    )
                                    .pointerInput(currentLangCode) {
                                        detectTapGestures(
                                            onPress = {
                                                isLangBtnPressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isLangBtnPressed = false
                                            },
                                            onTap = {
                                                val nextLang = InputLanguagesManager.getNextActiveLanguage(context, currentLangCode)
                                                InputLanguagesManager.setCurrentLanguageCode(context, nextLang.code)
                                                currentLangCode = nextLang.code
                                                onLanguageChanged?.invoke(nextLang.code)
                                                val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                try { ic?.finishComposingText() } catch (_: Exception) {}
                                                currentComposingText = ""
                                                performFeedback(isSpecial = true)
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "تبديل اللغة",
                                    tint = Color(activeTheme.accentColor),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // 3. Dedicated Clipboard button [📋]
                            var isSlot3Pressed by remember { mutableStateOf(false) }
                            val slot3Bg by animateColorAsState(
                                targetValue = if (isSlot3Pressed || activeSubView == KeyboardSubView.CLIPBOARD) bottomKeyPressedBg else bottomKeyBg,
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
                                    .weight(0.9f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = slot3Scale; scaleY = slot3Scale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(slot3Bg)
                                    .border(
                                        1.dp,
                                        if (activeSubView == KeyboardSubView.CLIPBOARD) Color(activeTheme.accentColor) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)),
                                        RoundedCornerShape(activeTheme.cornerRadius.dp)
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isSlot3Pressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isSlot3Pressed = false
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
                                    contentDescription = "الحافظة",
                                    tint = Color(activeTheme.accentColor),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 4. Spacebar in the center with active world language name and horizontal swipe language switch
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

                            Box(
                                modifier = Modifier
                                    .weight(3.2f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = spaceScale; scaleY = spaceScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(spaceBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(currentLangCode, currentSwipeEnabled) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            isSpacePressed = true
                                            val pressStartTime = System.currentTimeMillis()
                                            var totalDrag = 0f
                                            var hasSwitchedLanguage = false
                                            var isDragGesture = false
                                            val swipeThreshold = 18.dp.toPx()

                                            try {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                                    val downDuration = System.currentTimeMillis() - pressStartTime
                                                    if (!hasSwitchedLanguage && !isDragGesture && downDuration >= 420L) {
                                                        hasSwitchedLanguage = true
                                                        performFeedback(isSpecial = true)
                                                        val nextLang = InputLanguagesManager.getNextActiveLanguage(context, currentLangCode)
                                                        InputLanguagesManager.setCurrentLanguageCode(context, nextLang.code)
                                                        currentLangCode = nextLang.code
                                                        onLanguageChanged?.invoke(nextLang.code)
                                                        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                        try { ic?.finishComposingText() } catch (_: Exception) {}
                                                        currentComposingText = ""
                                                    }

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

                                                    if (kotlin.math.abs(totalDrag) > 8.dp.toPx()) {
                                                        isDragGesture = true
                                                    }

                                                    if (currentSwipeEnabled && !hasSwitchedLanguage && kotlin.math.abs(totalDrag) >= swipeThreshold) {
                                                        hasSwitchedLanguage = true
                                                        change.consume()
                                                        performFeedback(isSpecial = true)
                                                        val nextLang = if (totalDrag > 0) {
                                                            InputLanguagesManager.getNextActiveLanguage(context, currentLangCode)
                                                        } else {
                                                            InputLanguagesManager.getPreviousActiveLanguage(context, currentLangCode)
                                                        }
                                                        InputLanguagesManager.setCurrentLanguageCode(context, nextLang.code)
                                                        currentLangCode = nextLang.code
                                                        onLanguageChanged?.invoke(nextLang.code)

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
                                val spacebarLabel = if (currentWorldLang.code == "ar") "عربي أساسي" else if (currentWorldLang.code == "en") "English (US)" else currentWorldLang.nameNative
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "◀  $spacebarLabel  ▶",
                                        color = Color(activeTheme.keyTextColor).copy(alpha = 0.9f),
                                        fontSize = 12.sp,
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
                                    .border(
                                        1.dp,
                                        if (activeSubView == KeyboardSubView.EMOJI) Color(activeTheme.accentColor) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)),
                                        RoundedCornerShape(activeTheme.cornerRadius.dp)
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isSlot6Pressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isSlot6Pressed = false
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

                            // 7. Enter key with automatic detection (Search, Send, Newline)
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
                                        if (activeTheme.id == "liquid_glass_blur") {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF3B82F6),
                                                    Color(0xFF2563EB)
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
                                        1.dp,
                                        if (activeTheme.id == "liquid_glass_blur") Color(0xFF60A5FA).copy(alpha = 0.85f) else Color(activeTheme.accentColor).copy(alpha = 0.7f),
                                        RoundedCornerShape(activeTheme.cornerRadius.dp)
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isEnterPressed = true
                                                performFeedback(isSpecial = true)
                                                tryAwaitRelease()
                                                isEnterPressed = false
                                            },
                                            onTap = {
                                                sendEnter()
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentImeAction.icon,
                                    contentDescription = currentImeAction.contentDescription,
                                    tint = ThemePresets.getEnterIconTint(activeTheme),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Floating Long-Press Character Popup Overlay (Automatically hides when any character is pressed)
                    if (longPressChar != null && longPressVariants.isNotEmpty()) {
                        val isLight = ThemePresets.isLightColor(activeTheme.backgroundColor)
                        val popupTextCol = if (isLight) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                        val popupScrollState = rememberScrollState()
                        LaunchedEffect(selectedVariantIndex) {
                            if (longPressVariants.isNotEmpty()) {
                                popupScrollState.animateScrollTo((selectedVariantIndex * 48 * 2).coerceAtLeast(0))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = 2.dp)
                                .zIndex(9999f),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(activeTheme.backgroundColor),
                                border = BorderStroke(1.5.dp, Color(activeTheme.accentColor)),
                                shadowElevation = 16.dp
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    val isGrid = longPressVariants.size > 4
                                    val colCount = if (longPressVariants.size > 8) 4 else if (longPressVariants.size >= 6) 4 else 3

                                    if (isGrid) {
                                        // Grid Layout (2x4, 3x4, etc.) for > 4 items
                                        val rows = longPressVariants.chunked(colCount)
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(0.9f),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Dismiss X button
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFEF4444).copy(alpha = 0.95f))
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
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            rows.forEach { rowItems ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    rowItems.forEach { variant ->
                                                        val isCurrent = longPressVariants.indexOf(variant) == selectedVariantIndex
                                                        Box(
                                                            modifier = Modifier
                                                                .size(width = 46.dp, height = 48.dp)
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(
                                                                    if (isCurrent) Color(activeTheme.keyPressedColor)
                                                                    else Color(activeTheme.keyBackgroundColor)
                                                                )
                                                                .border(
                                                                    if (isCurrent) 2.dp else 1.dp,
                                                                    if (isCurrent) Color(activeTheme.accentColor) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha),
                                                                    RoundedCornerShape(4.dp)
                                                                )
                                                                .clickable {
                                                                    performFeedback()
                                                                    dismissLongPressPopup()
                                                                    sendText(variant)
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = variant,
                                                                color = if (isCurrent) Color(activeTheme.accentColor) else popupTextCol,
                                                                fontSize = if (isCurrent) 20.sp else 17.sp,
                                                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Clean single row for <= 4 items
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            longPressVariants.forEachIndexed { index, variant ->
                                                val isCurrent = index == selectedVariantIndex
                                                Box(
                                                    modifier = Modifier
                                                        .size(width = 46.dp, height = 48.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            if (isCurrent) Color(activeTheme.keyPressedColor)
                                                            else Color(activeTheme.keyBackgroundColor)
                                                        )
                                                        .border(
                                                            if (isCurrent) 2.dp else 1.dp,
                                                            if (isCurrent) Color(activeTheme.accentColor) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .clickable {
                                                            performFeedback()
                                                            dismissLongPressPopup()
                                                            sendText(variant)
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = variant,
                                                        color = if (isCurrent) Color(activeTheme.accentColor) else popupTextCol,
                                                        fontSize = if (isCurrent) 20.sp else 17.sp,
                                                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                                                    )
                                                }
                                            }
                                            // Dismiss button
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
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
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
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
    onLongClick: (() -> Unit)? = null,
    onDragVariant: ((Float) -> Unit)? = null,
    onReleaseVariant: (() -> Unit)? = null
) {
    val char = (key.type as? KeyType.Character)?.primary ?: ""
    val displayChar = if (isShifted) char.uppercase() else char
    val secondaryHint = if (isArabicLayout) {
        arabicSecondaryHintMap[char]
    } else {
        englishSecondaryHintMap[char.lowercase()]
    }

    val coroutineScope = rememberCoroutineScope()
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
    val currentOnDragVariant by rememberUpdatedState(onDragVariant)
    val currentOnReleaseVariant by rememberUpdatedState(onReleaseVariant)
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
                    width = if (theme.keyStyle == "neon") 1.2.dp else if (theme.keyStyle == "glass") 0.8.dp else 1.dp,
                    color = if (theme.keyStyle == "neon") Color(theme.accentColor).copy(alpha = 0.85f)
                    else if (theme.keyStyle == "glass") Color(0x35FFFFFF)
                    else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)),
                    shape = RoundedCornerShape(theme.cornerRadius.dp)
                )
                .pointerInput(displayChar, onLongClick != null, longPressDelayMs) {
                    if (onLongClick != null) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isTouching = true
                            triggerFeedback(0L)
                            val startX = down.position.x
                            var isLongPressed = false
                            var hasDragged = false
                            val job = coroutineScope.launch {
                                delay(longPressDelayMs)
                                if (isActive && isTouching) {
                                    isLongPressed = true
                                    triggerFeedback(30L)
                                    currentOnLongClick?.invoke()
                                }
                            }
                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    if (isLongPressed) {
                                        val deltaX = change.position.x - startX
                                        // السحب مسموح باتجاه اليمين فقط وممنوع لليسار نهائياً
                                        if (deltaX > 6.dp.toPx()) {
                                            hasDragged = true
                                            currentOnDragVariant?.invoke(deltaX)
                                        } else {
                                            currentOnDragVariant?.invoke(0f)
                                        }
                                        change.consume()
                                    }
                                }
                            } finally {
                                job.cancel()
                                isTouching = false
                                if (isLongPressed) {
                                    if (hasDragged) {
                                        currentOnReleaseVariant?.invoke()
                                    }
                                } else {
                                    currentOnClick()
                                }
                            }
                        }
                    } else {
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
                            onTap = {
                                currentOnClick()
                            }
                        )
                    }
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

        // 2. Square-shaped Floating Key Preview Bubble (Square-shaped with 4dp corner radius)
        AnimatedVisibility(
            visible = isTouching && showKeyPopup && displayChar.isNotBlank(),
            enter = fadeIn(animationSpec = tween(15)) + scaleIn(initialScale = 0.92f, animationSpec = tween(15)),
            exit = fadeOut(animationSpec = tween(40)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-48).dp)
                .zIndex(999f)
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

    // Square-shaped popup box with 4dp corner radius
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(previewBg)
            .border(
                1.5.dp,
                Color(theme.accentColor),
                RoundedCornerShape(4.dp)
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
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
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
                if (isActive) Color(theme.accentColor) else if (theme.keyStyle == "glass") Color(0x35FFFFFF) else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)),
                RoundedCornerShape(theme.cornerRadius.dp)
            )
            .pointerInput(onLongClick, onClick) {
                if (onLongClick != null) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        isPressed = true
                        var isLongPressHandled = false
                        val holdJob = coroutineScope.launch {
                            kotlinx.coroutines.delay(1000L) // Exactly 1-second hold for Shift CapsLock
                            isLongPressHandled = true
                            onLongClick()
                        }
                        val up = waitForUpOrCancellation()
                        holdJob.cancel()
                        isPressed = false
                        if (up != null && !isLongPressHandled) {
                            onClick()
                        }
                    }
                } else {
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
                }
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
    currentHeightFactor: Float = 1.0f,
    currentButtonSizeFactor: Float = 1.0f,
    onSave: (List<String>, Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var workingList by remember { mutableStateOf(currentEmojis.toMutableList()) }
    var heightFactor by remember { mutableFloatStateOf(currentHeightFactor) }
    var buttonSizeFactor by remember { mutableFloatStateOf(currentButtonSizeFactor) }
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

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "💡 انقر مباشرة على أي رمز أو إيموجي من الأقسام أدناه لإضافته فوراً:",
                    color = Color(theme.accentColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

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

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Live Preview of Top Emoji Row
                Text(
                    text = "معاينة الشريط المباشرة:",
                    color = Color(theme.keyTextColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp * heightFactor)
                        .background(Color(theme.toolbarColor), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    workingList.take(7).forEach { item ->
                        Box(
                            modifier = Modifier
                                .size(28.dp * buttonSizeFactor)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(theme.keyBackgroundColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item,
                                fontSize = (14 * buttonSizeFactor).sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Height Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "ارتفاع الشريط", color = Color(theme.keyTextColor), fontSize = 11.sp)
                    Text(text = "${(heightFactor * 100).toInt()}%", color = Color(theme.accentColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = heightFactor,
                    onValueChange = { heightFactor = it },
                    valueRange = 0.7f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(theme.accentColor),
                        activeTrackColor = Color(theme.accentColor)
                    ),
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                )

                // Button Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "حجم أيقونات الابتسامات", color = Color(theme.keyTextColor), fontSize = 11.sp)
                    Text(text = "${(buttonSizeFactor * 100).toInt()}%", color = Color(theme.accentColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = buttonSizeFactor,
                    onValueChange = { buttonSizeFactor = it },
                    valueRange = 0.7f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(theme.accentColor),
                        activeTrackColor = Color(theme.accentColor)
                    ),
                    modifier = Modifier.fillMaxWidth().height(28.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: Reset & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            workingList = mutableListOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
                            heightFactor = 1.0f
                            buttonSizeFactor = 1.0f
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
                            onSave(workingList, heightFactor, buttonSizeFactor)
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
