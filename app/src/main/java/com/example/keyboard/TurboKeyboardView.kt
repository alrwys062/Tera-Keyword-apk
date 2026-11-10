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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.util.AppStrings
import com.example.util.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            contentDescription = "Return",
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
        contentDescription = "Return",
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
    onBackActionRegister: (((() -> Boolean) -> Unit))? = null,
    onHideKeyboardRequested: (() -> Unit)? = null,
    onThemeChanged: ((KeyboardTheme) -> Unit)? = null,
    prefsManager: PreferencesManager? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { prefsManager ?: PreferencesManager(context) }
    var currentSettings by remember { mutableStateOf(settings) }
    LaunchedEffect(settings) { currentSettings = settings }
    val appLangCode = remember { LocaleHelper.getAppLanguageCode(context) }

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
            // Landscape mode - AI auto-resize to prevent screen obstruction in games & landscape apps
            when {
                screenHeightDp < 360 -> 22.dp
                screenHeightDp < 440 -> 25.dp
                else -> 28.dp
            }
        }
    }

    val calculatedKeyHeight = remember(baseKeyHeight, currentSettings.keyHeightFactor, currentSettings.keyButtonScale, isPortrait) {
        (baseKeyHeight * currentSettings.keyHeightFactor * currentSettings.keyButtonScale)
            .coerceIn(
                minimumValue = if (isPortrait) 24.dp else 18.dp,
                maximumValue = if (isPortrait) 85.dp else 40.dp
            )
    }

    val calculatedNumberKeyHeight = remember(calculatedKeyHeight) {
        (calculatedKeyHeight * 0.84f).coerceAtLeast(20.dp)
    }

    val calculatedKeyboardHeight = remember(
        calculatedKeyHeight,
        currentSettings.numberRowEnabled,
        currentSettings.suggestionsEnabled,
        currentSettings.topQuickEmojiRowEnabled,
        currentSettings.emojiBarHeightFactor,
        isPortrait,
        screenHeightDp
    ) {
        val numRowH = if (currentSettings.numberRowEnabled) (calculatedKeyHeight * 0.88f + 3.dp) else 0.dp
        val emojiRowH = if (currentSettings.topQuickEmojiRowEnabled) ((if (isPortrait) 34.dp else 24.dp) * currentSettings.emojiBarHeightFactor + 3.dp) else 0.dp
        val suggH = if (currentSettings.suggestionsEnabled) (if (isPortrait) 34.dp else 24.dp) else 0.dp
        val rowsHValue = (calculatedKeyHeight + 3.dp) * 5
        val rawHeight = numRowH + emojiRowH + suggH + rowsHValue
        if (!isPortrait) {
            val maxLandscapeHeight = (screenHeightDp * 0.42f).dp.coerceAtLeast(130.dp)
            rawHeight.coerceIn(120.dp, maxLandscapeHeight)
        } else {
            rawHeight.coerceAtLeast(220.dp)
        }
    }

    val rowsH = remember(calculatedKeyHeight) { (calculatedKeyHeight + 3.dp) * 5 }

    var customTopEmojis by remember { mutableStateOf(prefs.getCustomTopEmojis()) }
    var isCustomizeTopEmojisOpen by remember { mutableStateOf(false) }
    var customizerInputText by remember { mutableStateOf("") }
    var isManageQuickActionsOpen by remember { mutableStateOf(false) }
    var quickActionItemForOptions by remember { mutableStateOf<String?>(null) }
    var lastSwipeDownTime by remember { mutableStateOf(0L) }
    var lastSwipeUpTime by remember { mutableStateOf(0L) }
    var pendingSwipeDownJob by remember { mutableStateOf<Job?>(null) }
    var pendingSwipeUpJob by remember { mutableStateOf<Job?>(null) }
    var spacePressCountForProactivePrediction by remember { mutableIntStateOf(0) }

    // Search queries for sub-drawers (lifted up for sendText redirection)
    var emojiSearchQuery by remember { mutableStateOf("") }
    var themesSearchQuery by remember { mutableStateOf("") }
    var clipboardSearchQuery by remember { mutableStateOf("") }

    // Live Transboard features
    var activeDecorationStyle by remember { mutableStateOf(prefs.getActiveDecorationStyle()) }
    var isDecorationBarOpen by remember { mutableStateOf(false) }

    var isTranslationBarOpen by remember { mutableStateOf(false) }
    var translationSource by remember { mutableStateOf(currentSettings.translationSource) }
    var translationTarget by remember { mutableStateOf(currentSettings.translationTarget) }
    var isTranslatingLive by remember { mutableStateOf(false) }

    // Long-Press character variant popup state with free multi-directional dragging
    var longPressChar by remember { mutableStateOf<String?>(null) }
    var longPressVariants by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedVariantIndex by remember { mutableIntStateOf(0) }
    var lastVariantStepTime by remember { mutableLongStateOf(0L) }
    var popupOffsetX by remember { mutableFloatStateOf(0f) }
    var popupOffsetY by remember { mutableFloatStateOf(0f) }

    // Live text tracking for prediction and special 4-sec translate
    var currentComposingText by remember { mutableStateOf("") }

    // 3-sec Translate button hold state
    var isTranslateButtonHolding by remember { mutableStateOf(false) }
    var translateHoldProgress by remember { mutableFloatStateOf(0f) }
    var translateHoldMessage by remember { mutableStateOf("") }
    var lastDeletedText by remember { mutableStateOf("") }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var resetSuccessToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(resetSuccessToast) {
        if (resetSuccessToast != null) {
            delay(2600)
            resetSuccessToast = null
        }
    }

    val userDict = remember { com.example.data.UserDictionaryManager(context) }
    var userWords by remember { mutableStateOf(userDict.getUserWords()) }
    val shortcuts = remember { prefs.getShortcuts() }
    var activeTheme by remember(theme) {
        mutableStateOf(theme)
    }

    var allThemes by remember {
        val reversedPresets = com.example.model.ThemePresets.allPresets.reversed()
        val custom = try { prefs.getCustomThemes() } catch (_: Exception) { emptyList() }
        mutableStateOf((custom + reversedPresets).distinctBy { it.id })
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val custom = prefs.getCustomThemes()
            val reversedPresets = com.example.model.ThemePresets.allPresets.reversed()
            val combined = (custom + reversedPresets).distinctBy { it.id }
            withContext(Dispatchers.Main) {
                allThemes = combined
            }
        }
    }

    // Auto-return to letters keyboard on Send / Text Clear
    var prevComposingLength by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentComposingText) {
        if (prevComposingLength > 0 && currentComposingText.isEmpty()) {
            if (activeSubView != KeyboardSubView.EMOJI) {
                activeSubView = KeyboardSubView.NONE
                isSymbolsMode = false
                isMoreSymbolsMode = false
            }
        }
        prevComposingLength = currentComposingText.length
    }

    LaunchedEffect(Unit) {
        TurboKeyboardService.resetToLettersSignal.collect { timestamp ->
            if (timestamp > 0) {
                if (activeSubView != KeyboardSubView.EMOJI) {
                    activeSubView = KeyboardSubView.NONE
                    isSymbolsMode = false
                    isMoreSymbolsMode = false
                    isDecorationBarOpen = false
                    isTranslationBarOpen = false
                }
            }
        }
    }

    // Prediction suggestions + auto-correction + contextual next-word predictions (computed on background thread for 0ms UI lag)
    var suggestions by remember {
        mutableStateOf<List<PredictionEngine.PredictionResult>>(
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
        )
    }

    LaunchedEffect(currentComposingText, currentWorldLang, userWords, shortcuts, currentSettings.suggestionsEnabled, spacePressCountForProactivePrediction) {
        if (!currentSettings.suggestionsEnabled) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        val computed = withContext(Dispatchers.Default) {
            val lastWord = currentComposingText.trim().split(" ").lastOrNull() ?: ""
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            val textBefore = try {
                ic?.getTextBeforeCursor(80, 0)?.toString() ?: ""
            } catch (_: Exception) { "" }
            val tokens = textBefore.trimEnd().split("\\s+".toRegex()).filter { it.isNotBlank() }

            if (lastWord.isNotBlank()) {
                val shortcutMatch = shortcuts.find { it.trigger.equals(lastWord, ignoreCase = true) }
                val prevToken = if (tokens.size >= 2) tokens[tokens.size - 2] else null
                val baseList = PredictionEngine.getPredictionsWithCorrection(
                    prefix = lastWord,
                    isArabic = isArabic,
                    userWords = userWords,
                    prevWord = prevToken,
                    userDict = userDict
                )
                if (shortcutMatch != null) {
                    listOf(PredictionEngine.PredictionResult(shortcutMatch.expansion)) + baseList.take(2)
                } else {
                    baseList
                }
            } else {
                // Proactive Next-Word Prediction: Immediately when Spacebar is pressed, predict next word based on preceding word!
                val prevToken = tokens.lastOrNull() ?: ""
                if (prevToken.isNotBlank()) {
                    PredictionEngine.getNextWordPredictions(
                        prevWord = prevToken,
                        isArabic = isArabic,
                        userDict = userDict,
                        langCode = currentLangCode
                    )
                } else {
                    if (isArabic) {
                        listOf(
                            PredictionEngine.PredictionResult("السلام عليكم"),
                            PredictionEngine.PredictionResult("مرحبا"),
                            PredictionEngine.PredictionResult("صباح الخير")
                        )
                    } else if (currentLangCode.startsWith("pt")) {
                        listOf(
                            PredictionEngine.PredictionResult("olá"),
                            PredictionEngine.PredictionResult("bom dia"),
                            PredictionEngine.PredictionResult("obrigado")
                        )
                    } else {
                        listOf(
                            PredictionEngine.PredictionResult("Hello"),
                            PredictionEngine.PredictionResult("Thanks"),
                            PredictionEngine.PredictionResult("Good")
                        )
                    }
                }
            }
        }
        suggestions = computed
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
            popupOffsetX = 0f
            popupOffsetY = 0f
        }
    }

    val handleBackNavigation: () -> Boolean = {
        var handled = false
        if (isCustomizeTopEmojisOpen) {
            isCustomizeTopEmojisOpen = false
            customizerInputText = ""
            performFeedback(isSpecial = true)
            handled = true
        } else if (activeTranslationPickerForSource != null) {
            activeTranslationPickerForSource = null
            performFeedback(isSpecial = true)
            handled = true
        } else if (isTranslationBarOpen) {
            isTranslationBarOpen = false
            performFeedback(isSpecial = true)
            handled = true
        } else if (isDecorationBarOpen) {
            isDecorationBarOpen = false
            performFeedback(isSpecial = true)
            handled = true
        } else if (activeSubView != KeyboardSubView.NONE) {
            activeSubView = KeyboardSubView.NONE
            performFeedback(isSpecial = true)
            handled = true
        } else if (longPressChar != null || longPressVariants.isNotEmpty()) {
            dismissLongPressPopup()
            handled = true
        } else if (isSymbolsMode || isMoreSymbolsMode) {
            isSymbolsMode = false
            isMoreSymbolsMode = false
            performFeedback(isSpecial = true)
            handled = true
        } else {
            onHideKeyboardRequested?.invoke()
            handled = false
        }
        handled
    }

    LaunchedEffect(
        activeSubView,
        isCustomizeTopEmojisOpen,
        isTranslationBarOpen,
        isDecorationBarOpen,
        activeTranslationPickerForSource,
        isSymbolsMode,
        isMoreSymbolsMode,
        longPressChar
    ) {
        onBackActionRegister?.invoke(handleBackNavigation)
    }

    fun commitEmojiDirectly(text: String) {
        dismissLongPressPopup()
        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
        if (ic != null) {
            try {
                ic.commitText(text, 1)
            } catch (_: Exception) {}
        } else {
            onDirectClearAndReplaceText?.invoke(text)
        }
        currentComposingText = (currentComposingText + text).takeLast(30)
    }

    fun sendText(text: String) {
        if (isCustomizeTopEmojisOpen) {
            customizerInputText = (customizerInputText + text).take(15)
            return
        }
        if (activeSubView == KeyboardSubView.EMOJI) {
            emojiSearchQuery = (emojiSearchQuery + text).take(30)
            return
        }
        if (activeSubView == KeyboardSubView.THEMES) {
            themesSearchQuery = (themesSearchQuery + text).take(30)
            return
        }
        dismissLongPressPopup()
        val resolvedText = text
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

    // السحب في الأحرف العائمة: مسموح بجميع الاتجاهات بتحكم هادئ وموزون ومضبوط الحساسية والتأخير
    val onDragVariantHandler: (Float, Float) -> Unit = { deltaX, deltaY ->
        if (longPressVariants.isNotEmpty()) {
            val isDotVariants = longPressChar == "."
            val colCount = if (isDotVariants) 1 else if (longPressVariants.size > 8) 4 else if (longPressVariants.size >= 6) 4 else 3
            val rowCount = (longPressVariants.size + colCount - 1) / colCount

            val speed = currentSettings.floatingNavSpeed.coerceIn(1, 10)
            // خطوة السحب: السرعة 1 تتطلب 65dp، والسرعة 10 تتطلب 25dp
            val stepPx = (65f - (speed * 4f)).coerceAtLeast(25f)
            val debounceMs = (110 - (speed * 6)).coerceIn(55, 110)

            val now = System.currentTimeMillis()
            val colOffset = (deltaX / stepPx).toInt()
            val rowOffset = (deltaY / stepPx).toInt()

            val targetCol = colOffset.coerceIn(0, colCount - 1)
            val targetRow = rowOffset.coerceIn(0, rowCount - 1)
            val targetIdx = (targetRow * colCount + targetCol).coerceIn(0, longPressVariants.lastIndex)

            if (targetIdx != selectedVariantIndex && (now - lastVariantStepTime >= debounceMs)) {
                selectedVariantIndex = targetIdx
                lastVariantStepTime = now
                performFeedback(12L)
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
        // Table stays open per user requirement: does NOT disappear on finger release, only closes on (X) or variant tap!
    }

    fun sendDelete() {
        if (isCustomizeTopEmojisOpen) {
            if (customizerInputText.isNotEmpty()) {
                customizerInputText = customizerInputText.dropLast(1)
            }
            return
        }
        if (activeSubView == KeyboardSubView.EMOJI) {
            if (emojiSearchQuery.isNotEmpty()) {
                emojiSearchQuery = emojiSearchQuery.dropLast(1)
                return
            }
        }
        if (activeSubView == KeyboardSubView.THEMES) {
            if (themesSearchQuery.isNotEmpty()) {
                themesSearchQuery = themesSearchQuery.dropLast(1)
                return
            }
        }
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
                        lastDeletedText = selected.toString()
                        ic.commitText("", 1)
                    } else {
                        val before = ic.getTextBeforeCursor(1, 0)
                        if (!before.isNullOrEmpty()) {
                            lastDeletedText = before.toString()
                        }
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

        if (isCustomizeTopEmojisOpen) {
            if (customizerInputText.isNotBlank()) {
                val trimmed = customizerInputText.trim()
                if (!customTopEmojis.contains(trimmed)) {
                    customTopEmojis = (customTopEmojis + trimmed)
                    prefs.saveCustomTopEmojis(customTopEmojis)
                }
                customizerInputText = ""
                isCustomizeTopEmojisOpen = false
            } else {
                isCustomizeTopEmojisOpen = false
            }
            return
        }

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

    // Special 1.5-second hold Enter translation from Arabic to English
    fun triggerInstantTranslate() {
        coroutineScope.launch {
            val ic = getCurrentInputConnection?.invoke() ?: inputConnection
            var textToTranslate = ""

            // 1. Try selected text first
            try {
                val sel = ic?.getSelectedText(0)?.toString()
                if (!sel.isNullOrBlank()) {
                    textToTranslate = sel.trim()
                }
            } catch (_: Exception) {}

            // 2. If nothing selected, get full field text before + after cursor
            if (textToTranslate.isBlank() && ic != null) {
                try {
                    val before = ic.getTextBeforeCursor(2000, 0)?.toString() ?: ""
                    val after = ic.getTextAfterCursor(2000, 0)?.toString() ?: ""
                    val combined = "$before$after".trim()
                    if (combined.isNotBlank()) {
                        textToTranslate = combined
                    }
                } catch (_: Exception) {}
            }

            // 3. Fallback to direct callbacks or composing text
            if (textToTranslate.isBlank()) {
                val direct = getCurrentText?.invoke()?.trim() ?: ""
                textToTranslate = if (direct.isNotBlank()) direct else currentComposingText.trim()
            }

            if (textToTranslate.isNotBlank()) {
                val hasArabic = textToTranslate.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
                val source = if (hasArabic) "ar" else "en"
                val target = if (hasArabic) "en" else "ar"
                val targetName = if (target == "en") "الإنجليزية" else "العربية"

                translateHoldMessage = "جاري الترجمة إلى $targetName..."
                performFeedback(durationMs = 45L, isSpecial = true)

                val result = TranslationEngine.translate(textToTranslate, source, target)

                if (result.isNotBlank()) {
                    replaceAllText(result)
                    translateHoldMessage = "تمت الترجمة إلى $targetName بنجاح ✓"
                    performFeedback(durationMs = 35L, isSpecial = true)
                } else {
                    translateHoldMessage = "تعذر الترجمة، تأكد من الاتصال بالإنترنت"
                }
                delay(1600)
                translateHoldMessage = ""
            } else {
                translateHoldMessage = "اكتب نصاً أولاً للترجمة!"
                performFeedback(durationMs = 25L, isSpecial = true)
                delay(1300)
                translateHoldMessage = ""
            }
        }
    }

    fun moveCursorByStep(steps: Int) {
        if (steps == 0) return
        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
        if (ic != null) {
            val keycode = if (steps > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
            val count = kotlin.math.abs(steps)
            for (i in 0 until count) {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keycode))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keycode))
            }
            performFeedback(15L, isSpecial = true)
        }
    }

    val effectiveEi = getCurrentEditorInfo?.invoke() ?: editorInfo
    val isUrlField = remember(effectiveEi) {
        if (effectiveEi == null) false
        else {
            val variation = effectiveEi.inputType and InputType.TYPE_MASK_VARIATION
            variation == InputType.TYPE_TEXT_VARIATION_URI || variation == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
        }
    }
    val browserShortcuts = remember(isUrlField) {
        if (isUrlField) prefs.getBrowserDomainShortcuts() else emptyList()
    }
    var detectedOtpCode by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(inputSessionId, activeSubView) {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
            val clipText = cm?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
            detectedOtpCode = prefs.extractOtpCode(clipText)
        } catch (_: Exception) {}
    }

    val isGlassTheme = activeTheme.isGlass || activeTheme.id == "liquid_glass_blur"
    val containerShape = if (isGlassTheme) RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp) else RoundedCornerShape(0.dp)
    val containerBorder = if (isGlassTheme) BorderStroke(1.dp, Color(0x33FFFFFF)) else null

    val bgModifier = remember(activeTheme) {
        val gradient = activeTheme.backgroundGradient
        if (!gradient.isNullOrEmpty()) {
            Modifier.background(Brush.verticalGradient(gradient.map { Color(it) }))
        } else {
            Modifier.background(Color(activeTheme.backgroundColor))
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        if (!activeTheme.backgroundImageUri.isNullOrBlank()) {
            AsyncImage(
                model = activeTheme.backgroundImageUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = activeTheme.backgroundDim))
            )
        }

        // Instant Translation Toast Notification Overlay
        AnimatedVisibility(
            visible = translateHoldMessage.isNotEmpty(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .zIndex(999999f)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.95f),
                border = BorderStroke(1.2.dp, Color(activeTheme.accentColor)),
                shadowElevation = 10.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = Color(activeTheme.accentColor),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = translateHoldMessage,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Reset Settings Confirmation Dialog Modal
        if (showResetSettingsDialog) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.78f))
                    .zIndex(999998f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {},
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF141923),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f)),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = AppStrings.tr("ui_turbo_keyboard_view_002"),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = AppStrings.tr("ui_turbo_keyboard_view_003"),
                            color = Color(0xFFCAD5E2),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showResetSettingsDialog = false },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text(AppStrings.tr("ui_settings_screen_020"), color = Color(0xFF94A3B8), fontSize = 13.sp)
                            }
                            Button(
                                onClick = {
                                    val (newSettings, newTheme) = prefs.resetToFactoryDefaults()
                                    currentSettings = newSettings
                                    activeTheme = newTheme
                                    onThemeChanged?.invoke(newTheme)
                                    showResetSettingsDialog = false
                                    resetSuccessToast = "تمت استعادة الإعدادات الأصلية بنجاح ✓"
                                    performFeedback(isSpecial = true)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Text(AppStrings.tr("ui_settings_screen_126"), color = Color(0xFF090D15), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Success Toast Notification Banner
        AnimatedVisibility(
            visible = resetSuccessToast != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(999999f)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF10B981),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = resetSuccessToast ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        val navInsets = if (isPortrait) {
            WindowInsets.navigationBars
        } else {
            WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
        }

        val isFloating = currentSettings.isFloatingKeyboard

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = currentSettings.sideMarginDp.dp)
                .padding(bottom = currentSettings.bottomMarginDp.dp)
                .then(
                    if (isFloating) Modifier.widthIn(max = 350.dp).align(Alignment.BottomCenter)
                    else Modifier
                )
                .then(if (containerBorder != null) Modifier.border(containerBorder, containerShape) else Modifier)
                .clip(containerShape)
                .then(if (activeTheme.backgroundImageUri.isNullOrBlank()) bgModifier else Modifier)
                .windowInsetsPadding(navInsets)
        ) {
        // 1. TOP SECTION: Toolbar or Themes Row
        val isThemesMode = activeSubView == KeyboardSubView.THEMES

        if (isThemesMode) {
            // Search Bar for Themes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Color(activeTheme.backgroundColor))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(activeTheme.accentColor), modifier = Modifier.size(18.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (themesSearchQuery.isEmpty()) {
                        Text(AppStrings.tr("ui_turbo_keyboard_view_004"), color = Color(activeTheme.subtextColor).copy(alpha = 0.7f), fontSize = 12.sp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = themesSearchQuery,
                                color = Color(activeTheme.keyTextColor),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = androidx.compose.ui.text.TextStyle(
                                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                            Text(
                                text = "|",
                                color = Color(activeTheme.accentColor),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (themesSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { themesSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(activeTheme.subtextColor), modifier = Modifier.size(16.dp))
                    }
                }
                TextButton(onClick = { activeSubView = KeyboardSubView.NONE }) {
                    Text(AppStrings.tr("ui_settings_screen_040"), color = Color(activeTheme.accentColor), fontWeight = FontWeight.Bold)
                }
            }

            // Horizontal Themes Row (All themes immediately accessible, matching settings order)
            val filteredThemes = remember(allThemes, themesSearchQuery) {
                if (themesSearchQuery.isBlank()) allThemes
                else {
                    val cleanQuery = themesSearchQuery.trim()
                        .replace("[إأآا]".toRegex(), "ا")
                        .replace("ى", "ي")
                        .replace("ة", "ه")
                        .replace("[\u064B-\u065F\u0670]".toRegex(), "")
                        .lowercase()
                    allThemes.filter { theme ->
                        val cleanAr = theme.nameAr
                            .replace("[إأآا]".toRegex(), "ا")
                            .replace("ى", "ي")
                            .replace("ة", "ه")
                            .replace("[\u064B-\u065F\u0670]".toRegex(), "")
                            .lowercase()
                        cleanAr.contains(cleanQuery) || theme.nameEn.contains(themesSearchQuery.trim(), ignoreCase = true)
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(activeTheme.toolbarColor))
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(filteredThemes, key = { it.id }) { themeItem ->
                    val isSelected = themeItem.id == activeTheme.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(themeItem.accentColor).copy(alpha = 0.2f) else Color.Transparent)
                            .border(if (isSelected) 1.5.dp else 0.dp, Color(themeItem.accentColor), RoundedCornerShape(8.dp))
                            .clickable {
                                activeTheme = themeItem
                                onThemeChanged?.invoke(themeItem)
                                prefs.saveActiveTheme(themeItem)
                                performFeedback(isSpecial = true)
                            }
                            .padding(4.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(3) {
                                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(2.dp)).background(Color(themeItem.keyBackgroundColor)))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = themeItem.nameAr,
                            color = if (isSelected) Color(themeItem.accentColor) else Color(activeTheme.keyTextColor),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            style = androidx.compose.ui.text.TextStyle(
                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
            }
        } else if (currentSettings.toolbarEnabled && !isCustomizeTopEmojisOpen) {
            KeyboardToolbar(
                theme = activeTheme,
                activeSubView = activeSubView,
                isDecorationActive = isDecorationBarOpen || activeDecorationStyle != "none",
                isTranslationActive = isTranslationBarOpen,
                isNightMode = true,
                visibleTools = currentSettings.visibleToolbarTools,
                isForceEnglishNumbers = currentSettings.forceEnglishNumbers,
                appLanguageCode = appLangCode,
                onMyThemeClick = {
                    val isThemesActive = activeSubView == KeyboardSubView.THEMES
                    if (!isThemesActive) {
                        themesSearchQuery = ""
                        activeSubView = KeyboardSubView.THEMES
                        isDecorationBarOpen = false
                        isTranslationBarOpen = false
                    } else {
                        activeSubView = KeyboardSubView.NONE
                    }
                    performFeedback(isSpecial = true)
                },
                onMyThemeLongClick = {
                    val defaultTheme = prefs.resetToMyTheme()
                    activeTheme = defaultTheme
                    onThemeChanged?.invoke(defaultTheme)
                    performFeedback(durationMs = 40L, isSpecial = true)
                },
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
                onTranslateLongPressEnter = {
                    performFeedback(durationMs = 40L, isSpecial = true)
                    insertNewlineDirectly()
                },
                onToggleNightMode = {},
                onVoiceClick = { onVoiceRequested?.invoke() },
                onOpenSettingsClick = { onOpenSettingsRequested?.invoke() },
                onCustomizeToolbar = {
                    activeSubView = if (activeSubView == KeyboardSubView.CUSTOMIZE_TOOLBAR) KeyboardSubView.NONE else KeyboardSubView.CUSTOMIZE_TOOLBAR
                    isDecorationBarOpen = false
                    isTranslationBarOpen = false
                },
                onSwipeBack = { handleBackNavigation() },
                onResetSettingsClick = {
                    showResetSettingsDialog = true
                    performFeedback(isSpecial = true)
                },
                isQuickActionsEnabled = currentSettings.showQuickActionsRow,
                onToggleQuickActions = {
                    val updated = currentSettings.copy(showQuickActionsRow = !currentSettings.showQuickActionsRow)
                    currentSettings = updated
                    prefs.saveSettings(updated)
                    resetSuccessToast = if (updated.showQuickActionsRow) "تم إظهار شريط الأدوات فوق الأحرف ✓" else "تم إخفاء وحذف شريط الأدوات فوق الأحرف ✓"
                    performFeedback(isSpecial = true)
                }
            )
        } else if (!currentSettings.toolbarEnabled && !isCustomizeTopEmojisOpen && activeSubView == KeyboardSubView.NONE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(activeTheme.keyBackgroundColor).copy(alpha = 0.85f),
                    border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        val updated = currentSettings.copy(toolbarEnabled = true)
                        currentSettings = updated
                        prefs.saveSettings(updated)
                        performFeedback(isSpecial = true)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = AppStrings.tr("ui_turbo_keyboard_view_005"),
                            tint = Color(activeTheme.accentColor),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = AppStrings.tr("ui_turbo_keyboard_view_005"),
                            color = Color(activeTheme.keyTextColor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 2. SECOND SECTION: Suggestions & Candidate Bar (Below Toolbar, Above Keys/Emojis)
        if (!isThemesMode && activeTranslationPickerForSource == null && activeSubView == KeyboardSubView.NONE && !isNumberPadMode && settings.suggestionsEnabled && !isCustomizeTopEmojisOpen) {
            KeyboardSuggestionsBar(
                theme = activeTheme,
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
                    val textBf = try { ic?.getTextBeforeCursor(60, 0)?.toString() ?: "" } catch (_: Exception) { "" }
                    val prevTokens = textBf.trimEnd().split("\\s+".toRegex()).filter { it.isNotBlank() }
                    val previousToken = if (prevTokens.size >= 2) prevTokens[prevTokens.size - 2] else null
                    userDict.recordWord(word, previousToken)
                    currentComposingText = ""
                    spacePressCountForProactivePrediction++
                },
                onAddWordToDictionary = { word ->
                    userDict.addWord(word)
                    userWords = userDict.getUserWords()
                    performFeedback()
                },
                onSwipeBack = { handleBackNavigation() },
                detectedOtpCode = detectedOtpCode,
                onOtpClick = { code ->
                    sendText(code)
                    detectedOtpCode = null
                },
                browserShortcuts = emptyList(),
                onBrowserShortcutClick = null
            )
        }

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

                    KeyboardSubView.EMOJI -> {
                        EmojiPickerView(
                            theme = activeTheme,
                            keyboardHeight = calculatedKeyboardHeight,
                            hideAbcKey = currentSettings.hideEmojiAbcKey,
                            backspaceScale = currentSettings.backspaceKeyScale,
                            searchQuery = emojiSearchQuery,
                            onSearchQueryChange = { emojiSearchQuery = it },
                            isCustomizeMode = isCustomizeTopEmojisOpen,
                            onEmojiSelected = { emoji ->
                                if (isCustomizeTopEmojisOpen) {
                                    val trimmed = emoji.trim()
                                    if (!customTopEmojis.contains(trimmed)) {
                                        customTopEmojis = (customTopEmojis + trimmed)
                                        prefs.saveCustomTopEmojis(customTopEmojis)
                                    }
                                    customizerInputText = trimmed
                                    performFeedback(isSpecial = true)
                                } else {
                                    prefs.addRecentEmoji(emoji)
                                    commitEmojiDirectly(emoji)
                                    performFeedback()
                                }
                            },
                            onEmojiLongPress = { emoji ->
                                val trimmed = emoji.trim()
                                if (!customTopEmojis.contains(trimmed)) {
                                    customTopEmojis = (customTopEmojis + trimmed)
                                    prefs.saveCustomTopEmojis(customTopEmojis)
                                    android.widget.Toast.makeText(context, AppStrings.trf(context, "ui_turbo_keyboard_view_007", trimmed), android.widget.Toast.LENGTH_SHORT).show()
                                    performFeedback(isSpecial = true)
                                }
                            },
                            onSpacePressed = {
                                val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                if (ic != null) {
                                    ic.commitText(" ", 1)
                                } else {
                                    onDirectInsertText?.invoke(" ")
                                }
                                performFeedback(isSpace = true)
                            },
                            onDeletePressed = {
                                if (isCustomizeTopEmojisOpen) {
                                    if (customizerInputText.isNotEmpty()) {
                                        customizerInputText = customizerInputText.dropLast(1)
                                    } else if (customTopEmojis.isNotEmpty()) {
                                        customTopEmojis = customTopEmojis.dropLast(1)
                                        prefs.saveCustomTopEmojis(customTopEmojis)
                                    }
                                    performFeedback(isDelete = true)
                                } else {
                                    val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                    if (ic != null) {
                                        try {
                                            val down = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                                            val up = ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                                            if (!down || !up) {
                                                ic.deleteSurroundingText(1, 0)
                                            }
                                        } catch (_: Exception) {
                                            try { ic.deleteSurroundingText(1, 0) } catch (_: Exception) {}
                                        }
                                    } else {
                                        onDirectDeleteLastChar?.invoke()
                                    }
                                    performFeedback(isDelete = true)
                                }
                            },
                            onEnterPressed = {
                                sendEnter()
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            },
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                emojiSearchQuery = ""
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.STICKERS -> {
                        StickersPickerView(
                            theme = activeTheme,
                            prefs = prefs,
                            onStickerSelected = { sticker ->
                                if (isCustomizeTopEmojisOpen) {
                                    val trimmed = sticker.trim()
                                    if (!customTopEmojis.contains(trimmed)) {
                                        customTopEmojis = (customTopEmojis + trimmed)
                                        prefs.saveCustomTopEmojis(customTopEmojis)
                                    }
                                    customizerInputText = trimmed
                                    android.widget.Toast.makeText(context, AppStrings.trf(context, "ui_turbo_keyboard_view_007", trimmed), android.widget.Toast.LENGTH_SHORT).show()
                                    performFeedback(isSpecial = true)
                                } else {
                                    sendText(" $sticker ")
                                    if (currentSettings.autoReturnToLettersOnShortcut) {
                                        activeSubView = KeyboardSubView.NONE
                                        isSymbolsMode = false
                                        isMoreSymbolsMode = false
                                    }
                                }
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
                            theme = activeTheme,
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
                            theme = activeTheme,
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
                            theme = activeTheme,
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
                            theme = activeTheme,
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
                            theme = activeTheme,
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
                            theme = activeTheme,
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
                            theme = activeTheme,
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
                            theme = activeTheme,
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
                            theme = activeTheme,
                            onClose = { activeSubView = KeyboardSubView.NONE }
                        )
                    }
                    KeyboardSubView.TTS_SPEECH -> {
                        TtsSpeechSubView(
                            theme = activeTheme,
                            currentText = getCurrentText?.invoke()?.ifBlank { currentComposingText } ?: currentComposingText,
                            onClose = {
                                activeSubView = KeyboardSubView.NONE
                                isSymbolsMode = false
                                isMoreSymbolsMode = false
                            }
                        )
                    }
                    KeyboardSubView.PASSWORD_MANAGER -> {
                        KeyboardPassSubView(
                            theme = activeTheme,
                            onInsertText = { textToInsert ->
                                sendText(textToInsert)
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
                    KeyboardSubView.MY_STICKERS -> {
                        MyStickersSubView(
                            theme = activeTheme,
                            onStickerSelected = { sticker ->
                                sendText(sticker)
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

                    else -> {}
                }
            }
        }



        // Quick Shortcuts / Emojis Row (Customizable for user - Hidden while customizer panel is active to avoid duplicate bars)
        if (activeTranslationPickerForSource == null && activeSubView == KeyboardSubView.NONE && !isNumberPadMode && settings.topQuickEmojiRowEnabled && !isCustomizeTopEmojisOpen) {
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
                    val quickKeyBg = ThemePresets.resolveKeyColor(activeTheme.keyBackgroundColor, activeTheme.keyOpacity)
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
                        shape = RoundedCornerShape(activeTheme.cornerRadius.dp),
                        color = quickKeyBg,
                        border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = char,
                                color = Color(activeTheme.keyTextColor),
                                fontSize = (13 * currentSettings.emojiButtonSizeFactor).sp,
                                fontWeight = FontWeight.Medium,
                                style = androidx.compose.ui.text.TextStyle(
                                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }

                // Add / Customize button for the top emoji row (+)
                val addKeyBg = ThemePresets.resolveKeyColor(activeTheme.keyBackgroundColor, activeTheme.keyOpacity)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                        .clickable {
                            performFeedback(isSpecial = true)
                            isCustomizeTopEmojisOpen = true
                        },
                    shape = RoundedCornerShape(activeTheme.cornerRadius.dp),
                    color = addKeyBg,
                    border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = AppStrings.tr("ui_turbo_keyboard_view_011"),
                            tint = Color(activeTheme.accentColor),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // Panel to Customize Top Emojis Row (docked above keys, keeping keyboard keys visible and active below)
        if (isCustomizeTopEmojisOpen) {
            CustomizeTopEmojisInlinePanel(
                theme = activeTheme,
                workingList = customTopEmojis,
                inputText = customizerInputText,
                onInputTextChange = { customizerInputText = it },
                onAddEmoji = { emoji ->
                    if (emoji.isNotBlank()) {
                        val trimmed = emoji.trim()
                        val itemsToAdd = if (trimmed.contains(" ")) {
                            trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }
                        } else {
                            val list = mutableListOf<String>()
                            var i = 0
                            while (i < trimmed.length) {
                                val cp = trimmed.codePointAt(i)
                                val charCount = Character.charCount(cp)
                                var end = i + charCount
                                while (end < trimmed.length) {
                                    val nextCp = trimmed.codePointAt(end)
                                    if (nextCp == 0xFE0F || nextCp == 0x200D || (nextCp in 0x1F3FB..0x1F3FF)) {
                                        end += Character.charCount(nextCp)
                                        if (nextCp == 0x200D && end < trimmed.length) {
                                            end += Character.charCount(trimmed.codePointAt(end))
                                        }
                                    } else {
                                        break
                                    }
                                }
                                list.add(trimmed.substring(i, end))
                                i = end
                            }
                            if (list.size > 1 && list.all { it.length <= 4 }) list else listOf(trimmed)
                        }
                        var updated = customTopEmojis
                        for (item in itemsToAdd) {
                            if (!updated.contains(item)) {
                                updated = updated + item
                            }
                        }
                        customTopEmojis = updated
                        prefs.saveCustomTopEmojis(updated)
                    }
                },
                onRemoveEmoji = { emoji ->
                    customTopEmojis = customTopEmojis.filter { it != emoji }
                    prefs.saveCustomTopEmojis(customTopEmojis)
                },
                heightFactor = currentSettings.emojiBarHeightFactor,
                onHeightFactorChange = { factor ->
                    val updated = currentSettings.copy(emojiBarHeightFactor = factor)
                    currentSettings = updated
                    prefs.saveSettings(updated)
                },
                buttonSizeFactor = currentSettings.emojiButtonSizeFactor,
                onButtonSizeFactorChange = { factor ->
                    val updated = currentSettings.copy(emojiButtonSizeFactor = factor)
                    currentSettings = updated
                    prefs.saveSettings(updated)
                },
                onResetDefaults = {
                    val defaults = listOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
                    customTopEmojis = defaults
                    prefs.saveCustomTopEmojis(defaults)
                    val updated = currentSettings.copy(emojiBarHeightFactor = 1.0f, emojiButtonSizeFactor = 1.0f)
                    currentSettings = updated
                    prefs.saveSettings(updated)
                },
                onSaveAndClose = {
                    isCustomizeTopEmojisOpen = false
                    customizerInputText = ""
                    prefs.saveCustomTopEmojis(customTopEmojis)
                },
                onDismiss = {
                    isCustomizeTopEmojisOpen = false
                    customizerInputText = ""
                },
                onOpenEmojiPicker = {
                    activeSubView = if (activeSubView == KeyboardSubView.EMOJI) KeyboardSubView.NONE else KeyboardSubView.EMOJI
                }
            )
        }

        // Panel to Manage & Delete Quick Actions Above Keys (مسح الكل، قراءة، لصق، الحافظة...)
        if (isManageQuickActionsOpen) {
            QuickActionsManagerInlinePanel(
                theme = activeTheme,
                settings = currentSettings,
                onUpdateSettings = { updated ->
                    currentSettings = updated
                    prefs.saveSettings(updated)
                },
                onDismiss = { isManageQuickActionsOpen = false }
            )
        }

        // Quick Options Popup for specific clicked tool
        if (quickActionItemForOptions != null) {
            val (actionTitle, actionIcon) = when (quickActionItemForOptions) {
                "clear_all" -> "مسح الكل" to Icons.Default.DeleteSweep
                "read_text" -> "قراءة النص" to Icons.Default.VolumeUp
                "paste" -> "لصق" to Icons.Default.ContentPaste
                "clipboard" -> "الحافظة" to Icons.Default.ContentPaste
                "copy" -> "نسخ" to Icons.Default.ContentCopy
                "cut" -> "قص" to Icons.Default.ContentCut
                "select_all" -> "تحديد الكل" to Icons.Default.SelectAll
                "undo_redo" -> "استعادة بعد الحذف" to Icons.Default.Undo
                else -> "أداة" to Icons.Default.Tune
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AlertDialog(
                    onDismissRequest = { quickActionItemForOptions = null },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(actionIcon, contentDescription = null, tint = Color(0xFF0084FF), modifier = Modifier.size(20.dp))
                            Text(AppStrings.trf("ui_turbo_keyboard_view_012", actionTitle), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(AppStrings.tr("ui_turbo_keyboard_view_013"), fontSize = 12.sp, color = Color.Gray)
                            
                            // 1. حذف هذا الزر فقط
                            Button(
                                onClick = {
                                    val updated = when (quickActionItemForOptions) {
                                        "clear_all" -> currentSettings.copy(showQuickActionClearAll = false)
                                        "read_text" -> currentSettings.copy(showQuickActionReadText = false)
                                        "paste" -> currentSettings.copy(showQuickActionPaste = false)
                                        "clipboard" -> currentSettings.copy(showQuickActionClipboard = false)
                                        "copy" -> currentSettings.copy(showQuickActionCopy = false)
                                        "cut" -> currentSettings.copy(showQuickActionCut = false)
                                        "select_all" -> currentSettings.copy(showQuickActionSelectAll = false)
                                        "undo_redo" -> currentSettings.copy(showQuickActionUndoRedo = false)
                                        else -> currentSettings
                                    }
                                    currentSettings = updated
                                    prefs.saveSettings(updated)
                                    resetSuccessToast = "تم حذف زر $actionTitle من الشريط ✓"
                                    quickActionItemForOptions = null
                                    performFeedback(isSpecial = true)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(AppStrings.trf("ui_turbo_keyboard_view_014", actionTitle), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            // 2. إخفاء وحذف الشريط بالكامل
                            OutlinedButton(
                                onClick = {
                                    val updated = currentSettings.copy(showQuickActionsRow = false)
                                    currentSettings = updated
                                    prefs.saveSettings(updated)
                                    resetSuccessToast = "تم إخفاء وحذف شريط الأدوات بالكامل ✓"
                                    quickActionItemForOptions = null
                                    performFeedback(isSpecial = true)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(AppStrings.tr("ui_turbo_keyboard_view_015"), color = Color(0xFFEF4444), fontSize = 12.sp)
                            }

                            // 3. إدارة وتخصيص كل الأدوات
                            TextButton(
                                onClick = {
                                    quickActionItemForOptions = null
                                    isManageQuickActionsOpen = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF0084FF), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(AppStrings.tr("ui_turbo_keyboard_view_016"), color = Color(0xFF0084FF), fontSize = 12.sp)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { quickActionItemForOptions = null }) {
                            Text(AppStrings.tr("ui_settings_screen_020"), color = Color.Gray)
                        }
                    }
                )
            }
        }

        // Main Keyboard Keys Section (Keys hidden when full-screen subview like EMOJI is active, matching keyboard height)
        val showKeys = activeTranslationPickerForSource == null && 
                (activeSubView == KeyboardSubView.NONE || activeSubView == KeyboardSubView.THEMES)
        
        if (showKeys) {
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
                        // 0. Quick Actions Row & Undo / Restore Chip
                        val hasUndo = lastDeletedText.isNotEmpty()
                        val hasActiveActions = currentSettings.showQuickActionsRow && (
                            currentSettings.showQuickActionClearAll ||
                            currentSettings.showQuickActionReadText ||
                            currentSettings.showQuickActionPaste ||
                            currentSettings.showQuickActionCopy ||
                            currentSettings.showQuickActionCut ||
                            currentSettings.showQuickActionSelectAll ||
                            currentSettings.showQuickActionClipboard
                        )
                        if ((hasUndo && currentSettings.showQuickActionUndoRedo) || hasActiveActions) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // زر إخفاء وحذف شريط الأدوات بالكامل بلمسة واحدة
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                    border = BorderStroke(0.8.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable {
                                        val updated = currentSettings.copy(showQuickActionsRow = false)
                                        currentSettings = updated
                                        prefs.saveSettings(updated)
                                        resetSuccessToast = "تم إخفاء شريط الأدوات فوق الأحرف ✓ (يمكنك إعادته من الإعدادات)"
                                        performFeedback(isSpecial = true)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = AppStrings.tr("ui_turbo_keyboard_view_017"), tint = Color(0xFFEF4444), modifier = Modifier.size(11.dp))
                                        Text(AppStrings.tr("ui_turbo_keyboard_view_017"), color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // زر تخصيص وحذف عناصر محددة من الشريط
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = Color(activeTheme.keyBackgroundColor),
                                    border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                    modifier = Modifier.clickable {
                                        isManageQuickActionsOpen = true
                                        performFeedback(isSpecial = true)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = AppStrings.tr("ui_turbo_keyboard_view_018"), tint = Color(activeTheme.accentColor), modifier = Modifier.size(11.dp))
                                        Text(AppStrings.tr("ui_turbo_keyboard_view_018"), color = Color(activeTheme.keyTextColor), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                // 1. استعادة بعد الحذف (Undo / Restore)
                                if (hasUndo && currentSettings.showQuickActionUndoRedo) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.22f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.8f)),
                                        modifier = Modifier.pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    quickActionItemForOptions = "undo_redo"
                                                    performFeedback(isSpecial = true)
                                                },
                                                onTap = {
                                                    sendText(lastDeletedText)
                                                    lastDeletedText = ""
                                                    performFeedback(isSpecial = true)
                                                }
                                            )
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Undo, contentDescription = AppStrings.tr("ui_settings_screen_126"), tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                                            Text(
                                                text = AppStrings.trf("ui_turbo_keyboard_view_019", lastDeletedText),
                                                color = Color(activeTheme.keyTextColor),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                if (currentSettings.showQuickActionsRow) {
                                    // 2. مسح الكل (Clear All)
                                    if (currentSettings.showQuickActionClearAll) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(activeTheme.keyBackgroundColor),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        quickActionItemForOptions = "clear_all"
                                                        performFeedback(isSpecial = true)
                                                    },
                                                    onTap = {
                                                        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                        val all = ic?.getTextBeforeCursor(2000, 0)?.toString() ?: ""
                                                        if (all.isNotBlank()) {
                                                            lastDeletedText = all
                                                            ic?.deleteSurroundingText(all.length, 0)
                                                            performFeedback(isDelete = true)
                                                        }
                                                    }
                                                )
                                            }
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.DeleteSweep, contentDescription = AppStrings.trCode(context, appLangCode, "clear_all"), tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(AppStrings.trCode(context, appLangCode, "clear_all"), color = Color(activeTheme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    // 3. قراءة النص (Read Text)
                                    if (currentSettings.showQuickActionReadText) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(activeTheme.keyBackgroundColor),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        quickActionItemForOptions = "read_text"
                                                        performFeedback(isSpecial = true)
                                                    },
                                                    onTap = {
                                                        val text = getCurrentText?.invoke()?.ifBlank { currentComposingText } ?: currentComposingText
                                                        if (text.isNotBlank()) {
                                                            com.example.sound.KeyboardTtsEngine.getInstance(context).speak(text)
                                                            performFeedback(isSpecial = true)
                                                        }
                                                    }
                                                )
                                            }
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.VolumeUp, contentDescription = AppStrings.trCode(context, appLangCode, "read_text"), tint = Color(activeTheme.accentColor), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(AppStrings.trCode(context, appLangCode, "read_text"), color = Color(activeTheme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    // 4. لصق (Paste)
                                    if (currentSettings.showQuickActionPaste) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(activeTheme.keyBackgroundColor),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        quickActionItemForOptions = "paste"
                                                        performFeedback(isSpecial = true)
                                                    },
                                                    onTap = {
                                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                        val clip = cm?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                                        if (clip.isNotBlank()) sendText(clip)
                                                        performFeedback(isSpecial = true)
                                                    }
                                                )
                                            }
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = AppStrings.trCode(context, appLangCode, "paste"), tint = Color(activeTheme.accentColor), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(AppStrings.trCode(context, appLangCode, "paste"), color = Color(activeTheme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    // 5. أيقونة الحافظة السريعة (Clipboard)
                                    if (currentSettings.showQuickActionClipboard) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(activeTheme.keyBackgroundColor),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        quickActionItemForOptions = "clipboard"
                                                        performFeedback(isSpecial = true)
                                                    },
                                                    onTap = {
                                                        activeSubView = if (activeSubView == KeyboardSubView.CLIPBOARD) KeyboardSubView.NONE else KeyboardSubView.CLIPBOARD
                                                        isDecorationBarOpen = false
                                                        isTranslationBarOpen = false
                                                        performFeedback(isSpecial = true)
                                                    }
                                                )
                                            }
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ContentPaste, contentDescription = AppStrings.trCode(context, appLangCode, "clipboard_label"), tint = Color(activeTheme.accentColor), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(AppStrings.trCode(context, appLangCode, "clipboard_label"), color = Color(activeTheme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    // 6. نسخ (Copy)
                                    if (currentSettings.showQuickActionCopy) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(activeTheme.keyBackgroundColor),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        quickActionItemForOptions = "copy"
                                                        performFeedback(isSpecial = true)
                                                    },
                                                    onTap = {
                                                        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                        val sel = ic?.getSelectedText(0)?.toString()
                                                        val textToCopy = if (!sel.isNullOrBlank()) sel else (ic?.getTextBeforeCursor(2000, 0)?.toString() ?: "")
                                                        if (textToCopy.isNotBlank()) {
                                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                            cm?.setPrimaryClip(android.content.ClipData.newPlainText("text", textToCopy))
                                                            resetSuccessToast = "تم نسخ النص ✓"
                                                            performFeedback(isSpecial = true)
                                                        }
                                                    }
                                                )
                                            }
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = AppStrings.tr("ui_clipboard_screen_018"), tint = Color(activeTheme.accentColor), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(AppStrings.tr("ui_clipboard_screen_018"), color = Color(activeTheme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    // 7. قص (Cut)
                                    if (currentSettings.showQuickActionCut) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(activeTheme.keyBackgroundColor),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        quickActionItemForOptions = "cut"
                                                        performFeedback(isSpecial = true)
                                                    },
                                                    onTap = {
                                                        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                        val sel = ic?.getSelectedText(0)?.toString()
                                                        if (!sel.isNullOrBlank()) {
                                                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                            cm?.setPrimaryClip(android.content.ClipData.newPlainText("text", sel))
                                                            lastDeletedText = sel
                                                            ic?.commitText("", 1)
                                                            resetSuccessToast = "تم قص النص ✓"
                                                            performFeedback(isDelete = true)
                                                        } else {
                                                            val before = ic?.getTextBeforeCursor(2000, 0)?.toString() ?: ""
                                                            if (before.isNotBlank()) {
                                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                                                cm?.setPrimaryClip(android.content.ClipData.newPlainText("text", before))
                                                                lastDeletedText = before
                                                                ic?.deleteSurroundingText(before.length, 0)
                                                                resetSuccessToast = "تم قص النص ✓"
                                                                performFeedback(isDelete = true)
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ContentCut, contentDescription = AppStrings.tr("ui_settings_screen_128"), tint = Color(activeTheme.accentColor), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(AppStrings.tr("ui_settings_screen_128"), color = Color(activeTheme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    // 8. تحديد الكل (Select All)
                                    if (currentSettings.showQuickActionSelectAll) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(activeTheme.keyBackgroundColor),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                            modifier = Modifier.pointerInput(Unit) {
                                                detectTapGestures(
                                                    onLongPress = {
                                                        quickActionItemForOptions = "select_all"
                                                        performFeedback(isSpecial = true)
                                                    },
                                                    onTap = {
                                                        val ic = getCurrentInputConnection?.invoke() ?: inputConnection
                                                        ic?.performContextMenuAction(android.R.id.selectAll)
                                                        performFeedback(isSpecial = true)
                                                    }
                                                )
                                            }
                                        ) {
                                            Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.SelectAll, contentDescription = AppStrings.tr("ui_settings_screen_129"), tint = Color(activeTheme.accentColor), modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(2.dp))
                                                Text(AppStrings.tr("ui_settings_screen_129"), color = Color(activeTheme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }

                                    // 9. زر تخصيص / حذف الأدوات (⚙️)
                                    Surface(
                                         shape = RoundedCornerShape(8.dp),
                                         color = Color(activeTheme.keyBackgroundColor),
                                         border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.45f)),
                                         modifier = Modifier.clickable {
                                             isManageQuickActionsOpen = true
                                             performFeedback(isSpecial = true)
                                         }
                                    ) {
                                         Box(
                                             modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                             contentAlignment = Alignment.Center
                                         ) {
                                             Icon(
                                                 Icons.Default.Tune,
                                                 contentDescription = AppStrings.tr("ui_turbo_keyboard_view_020"),
                                                 tint = Color(activeTheme.accentColor),
                                                 modifier = Modifier.size(12.dp)
                                             )
                                         }
                                    }

                                    // 10. زر حذف / إخفاء الشريط بالكامل (❌)
                                    Surface(
                                         shape = RoundedCornerShape(8.dp),
                                         color = Color(0xFFEF4444).copy(alpha = 0.15f),
                                         border = BorderStroke(0.8.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                                         modifier = Modifier.clickable {
                                             val updated = currentSettings.copy(showQuickActionsRow = false)
                                             currentSettings = updated
                                             prefs.saveSettings(updated)
                                             performFeedback(durationMs = 35L, isSpecial = true)
                                             resetSuccessToast = "تم إخفاء شريط الأدوات (يمكنك إعادته من الإعدادات) ✓"
                                         }
                                    ) {
                                         Box(
                                             modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                             contentAlignment = Alignment.Center
                                         ) {
                                             Icon(
                                                 Icons.Default.Close,
                                                 contentDescription = AppStrings.tr("ui_turbo_keyboard_view_021"),
                                                 tint = Color(0xFFEF4444),
                                                 modifier = Modifier.size(12.dp)
                                             )
                                         }
                                    }
                                }
                            }
                        }

                        // Recent Emojis Row Above Symbols
                        if (isSymbolsMode && currentSettings.recentEmojisAboveSymbols) {
                            val recentEmojis = remember { prefs.getRecentEmojis().take(8) }
                            if (recentEmojis.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((calculatedNumberKeyHeight * 0.9f).coerceIn(24.dp, 36.dp))
                                        .padding(horizontal = 2.dp, vertical = 1.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    recentEmojis.forEach { em ->
                                        Surface(
                                            modifier = Modifier.weight(1f).fillMaxHeight().clickable {
                                                performFeedback()
                                                sendText(em)
                                            },
                                            shape = RoundedCornerShape(activeTheme.cornerRadius.dp),
                                            color = ThemePresets.resolveKeyColor(activeTheme.keyBackgroundColor, activeTheme.keyOpacity),
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = 0.35f))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = em, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Optional Number Row or Custom Emojis Row
                        if (currentSettings.numberRowEnabled) {
                            if (currentSettings.replaceNumberRowWithEmojis) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(calculatedNumberKeyHeight),
                                    horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                                ) {
                                    val emojiRowList = customTopEmojis.take(10)
                                    emojiRowList.forEach { em ->
                                        val quickKeyBg = ThemePresets.resolveKeyColor(activeTheme.keyBackgroundColor, activeTheme.keyOpacity)
                                        Surface(
                                            modifier = Modifier.weight(1f).fillMaxHeight().clickable {
                                                performFeedback()
                                                sendText(em)
                                            },
                                            shape = RoundedCornerShape(activeTheme.cornerRadius.dp),
                                            color = quickKeyBg,
                                            border = BorderStroke(0.8.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)))
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = em, fontSize = 14.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                                ) {
                                    val row = if (isArabic && !currentSettings.forceEnglishNumbers) KeyLayouts.numbersRowAr else KeyLayouts.numbersRowEn
                                    row.forEachIndexed { idx, key ->
                                        if (currentSettings.isSplitKeyboard && idx == row.size / 2) {
                                            Spacer(modifier = Modifier.width(if (isPortrait) 20.dp else 36.dp))
                                        }
                                        val digitChar = (key.type as KeyType.Character).primary
                                        key("num_$digitChar") {
                                            KeyButton(
                                                key = key,
                                                theme = activeTheme,
                                                isShifted = isShifted,
                                                samsungStyleHints = currentSettings.samsungStyleHints,
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
                            row.forEachIndexed { idx, key ->
                                if (currentSettings.isSplitKeyboard && idx == row.size / 2) {
                                    Spacer(modifier = Modifier.width(if (isPortrait) 20.dp else 36.dp))
                                }
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
                                        samsungStyleHints = currentSettings.samsungStyleHints,
                                        isArabicLayout = isArabic,
                                        showKeyPopup = currentSettings.keyPopupEnabled,
                                        keyHeight = calculatedKeyHeight,
                                        keyFontSizeFactor = currentSettings.keyFontSizeFactor * (if (isArabic) currentSettings.arabicKeyTextScale else 1.0f),
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
                            row.forEachIndexed { idx, key ->
                                if (currentSettings.isSplitKeyboard && idx == row.size / 2) {
                                    Spacer(modifier = Modifier.width(if (isPortrait) 20.dp else 36.dp))
                                }
                                val char = (key.type as KeyType.Character).primary
                                key("${currentWorldLang.code}_r2_$char") {
                                    KeyButton(
                                        key = key,
                                        theme = activeTheme,
                                        isShifted = isShifted,
                                        samsungStyleHints = currentSettings.samsungStyleHints,
                                        isArabicLayout = isArabic,
                                        showKeyPopup = currentSettings.keyPopupEnabled,
                                        keyHeight = calculatedKeyHeight,
                                        keyFontSizeFactor = currentSettings.keyFontSizeFactor * (if (isArabic) currentSettings.arabicKeyTextScale else 1.0f),
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

                            row.forEachIndexed { idx, key ->
                                if (currentSettings.isSplitKeyboard && idx == row.size / 2) {
                                    Spacer(modifier = Modifier.width(if (isPortrait) 20.dp else 36.dp))
                                }
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
                                                    samsungStyleHints = currentSettings.samsungStyleHints,
                                                    isArabicLayout = isArabic,
                                                    showKeyPopup = currentSettings.keyPopupEnabled,
                                                    keyHeight = calculatedKeyHeight,
                                                    keyFontSizeFactor = currentSettings.keyFontSizeFactor * (if (isArabic) currentSettings.arabicKeyTextScale else 1.0f),
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


                        val bottomKeyBg = remember(activeTheme.keyBackgroundColor, activeTheme.keyOpacity) {
                            ThemePresets.resolveKeyColor(activeTheme.keyBackgroundColor, activeTheme.keyOpacity)
                        }
                        val bottomKeyPressedBg = remember(activeTheme.keyPressedColor, activeTheme.keyOpacity) {
                            ThemePresets.resolveKeyColor(activeTheme.keyPressedColor, activeTheme.keyOpacity)
                        }
                        val bottomKeyBorderColor = remember(activeTheme.borderColor, activeTheme.borderAlpha) {
                            Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f))
                        }

                        Row(
                            modifier = Modifier
                                 .fillMaxWidth()
                                 .height(calculatedKeyHeight),
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. ModeChange key 123!#() on FAR LEFT
                            var isModePressed by remember { mutableStateOf(false) }
                            val modeBg = if (isModePressed) bottomKeyPressedBg else bottomKeyBg
                            val modeScale = if (isModePressed) 0.95f else 1.0f
                            Box(
                                modifier = Modifier
                                    .weight(1.15f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = modeScale; scaleY = modeScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(modeBg)
                                    .border(1.dp, bottomKeyBorderColor, RoundedCornerShape(activeTheme.cornerRadius.dp))
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
                            val langBtnBg = if (isLangBtnPressed) bottomKeyPressedBg else bottomKeyBg
                            val langBtnScale = if (isLangBtnPressed) 0.95f else 1.0f
                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = langBtnScale; scaleY = langBtnScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(langBtnBg)
                                    .border(
                                        1.dp,
                                        bottomKeyBorderColor,
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
                                            },
                                            onLongPress = {
                                                performFeedback(durationMs = 45L, isSpecial = true)
                                                activeSubView = KeyboardSubView.SETTINGS
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = AppStrings.tr("ui_turbo_keyboard_view_022"),
                                    tint = Color(activeTheme.accentColor),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // 3. Dedicated Clipboard button [📋]
                            var isSlot3Pressed by remember { mutableStateOf(false) }
                            val slot3Bg = if (isSlot3Pressed || activeSubView == KeyboardSubView.CLIPBOARD) bottomKeyPressedBg else bottomKeyBg
                            val slot3Scale = if (isSlot3Pressed) 0.95f else 1.0f
                            Box(
                                modifier = Modifier
                                    .weight(0.9f)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = slot3Scale; scaleY = slot3Scale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(slot3Bg)
                                    .border(
                                        1.dp,
                                        if (activeSubView == KeyboardSubView.CLIPBOARD) Color(activeTheme.accentColor) else bottomKeyBorderColor,
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
                                    contentDescription = AppStrings.tr("ui_settings_screen_132"),
                                    tint = Color(activeTheme.accentColor),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // 4. Spacebar in the center with active world language name and horizontal swipe language switch & Trackpad cursor mode
                            var isSpacePressed by remember { mutableStateOf(false) }
                            var isCursorTrackpadActive by remember { mutableStateOf(false) }
                            val spaceBg = if (isSpacePressed) bottomKeyPressedBg else bottomKeyBg
                            val spaceScale = if (isSpacePressed) 0.97f else 1.0f
                            val currentSwipeEnabled by rememberUpdatedState(currentSettings.swipeSpaceSwitchLanguage)
                            val spacebarWeight = when (currentSettings.spacebarWidthMode) {
                                "compact" -> 2.2f
                                "wide" -> 4.2f
                                else -> 3.2f
                            }

                            Box(
                                modifier = Modifier
                                    .weight(spacebarWeight)
                                    .fillMaxHeight()
                                    .graphicsLayer { scaleX = spaceScale; scaleY = spaceScale }
                                    .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .background(spaceBg)
                                    .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                    .pointerInput(currentLangCode, currentSwipeEnabled, currentSettings.numberRowEnabled) {
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            isSpacePressed = true
                                            val pressStartTime = System.currentTimeMillis()
                                            var totalDragX = 0f
                                            var totalDragY = 0f
                                            var hasSwitchedLanguage = false
                                            var hasSwitchedNumberRow = false
                                            var isDragGesture = false
                                            var isTrackpadMode = false
                                            var accumulatedTrackpadX = 0f
                                            val swipeThresholdX = 20.dp.toPx()
                                            val swipeThresholdY = 22.dp.toPx()
                                            val trackpadStepPx = 13.dp.toPx()

                                            try {
                                                while (true) {
                                                    val event = awaitPointerEvent()
                                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                                    val deltaX = change.position.x - down.position.x
                                                    val deltaY = change.position.y - down.position.y
                                                    totalDragX = deltaX
                                                    totalDragY = deltaY

                                                    val downDuration = System.currentTimeMillis() - pressStartTime
                                                    if (!isTrackpadMode && !hasSwitchedLanguage && !hasSwitchedNumberRow && downDuration >= 360L && kotlin.math.abs(totalDragY) < 14.dp.toPx()) {
                                                        isTrackpadMode = true
                                                        isCursorTrackpadActive = true
                                                        performFeedback(durationMs = 25L, isSpecial = true)
                                                    }

                                                    if (!change.pressed) {
                                                        // Pointer lifted
                                                        change.consume()
                                                        if (!isDragGesture && !hasSwitchedLanguage && !hasSwitchedNumberRow && !isTrackpadMode) {
                                                            performFeedback(isSpace = true)
                                                            sendText(" ")
                                                            spacePressCountForProactivePrediction++
                                                        }
                                                        break
                                                    }

                                                    if (isTrackpadMode) {
                                                        val diff = deltaX - accumulatedTrackpadX
                                                        if (kotlin.math.abs(diff) >= trackpadStepPx) {
                                                            val steps = (diff / trackpadStepPx).toInt()
                                                            moveCursorByStep(steps)
                                                            accumulatedTrackpadX += steps * trackpadStepPx
                                                        }
                                                        change.consume()
                                                    } else {
                                                        if (kotlin.math.abs(totalDragX) > 8.dp.toPx() || kotlin.math.abs(totalDragY) > 8.dp.toPx()) {
                                                            isDragGesture = true
                                                        }

                                                        // 1. Strict Vertical Swipe on Spacebar state machine:
                                                        // - swipeState = 2: Full mode (Number row + Emoji row + letter keys)
                                                        // - swipeState = 1: Number row hidden, Emoji row + letters visible
                                                        // - swipeState = 0: Number row and Emoji row hidden, letters only
                                                        // Swipe DOWN decrements; Swipe UP increments. Fully reversible!
                                                        if (!hasSwitchedNumberRow && !hasSwitchedLanguage && kotlin.math.abs(totalDragY) >= swipeThresholdY && kotlin.math.abs(totalDragY) > kotlin.math.abs(totalDragX) * 1.15f) {
                                                            hasSwitchedNumberRow = true
                                                            change.consume()
                                                            var swipeState = when {
                                                                currentSettings.numberRowEnabled -> 2
                                                                currentSettings.topQuickEmojiRowEnabled -> 1
                                                                else -> 0
                                                            }
                                                            if (totalDragY > 0) {
                                                                // Swipe DOWN
                                                                if (swipeState > 0) swipeState--
                                                            } else {
                                                                // Swipe UP
                                                                if (swipeState < 2) swipeState++
                                                            }
                                                            val newNum = (swipeState == 2)
                                                            val newEmoji = (swipeState >= 1)
                                                            val updated = currentSettings.copy(
                                                                numberRowEnabled = newNum,
                                                                topQuickEmojiRowEnabled = newEmoji
                                                            )
                                                            currentSettings = updated
                                                            prefs.saveSettings(updated)
                                                            performFeedback(durationMs = 40L, isSpecial = true)
                                                            resetSuccessToast = when (swipeState) {
                                                                2 -> "الوضع الكامل: أرقام + ابتسامات ✓"
                                                                1 -> "إخفاء الأرقام • إظهار الابتسامات ✓"
                                                                else -> "الوضع المضغوط: إخفاء الأشرطة ✓"
                                                            }
                                                        }

                                                        // 2. Horizontal Swipe: Switches keyboard language
                                                        else if (currentSwipeEnabled && !hasSwitchedLanguage && !hasSwitchedNumberRow && kotlin.math.abs(totalDragX) >= swipeThresholdX && kotlin.math.abs(totalDragX) > kotlin.math.abs(totalDragY) * 1.15f) {
                                                            hasSwitchedLanguage = true
                                                            change.consume()
                                                            performFeedback(isSpecial = true)
                                                            val nextLang = if (totalDragX > 0) {
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
                                                }
                                            } finally {
                                                isSpacePressed = false
                                                isCursorTrackpadActive = false
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
                                    if (isCursorTrackpadActive) {
                                        Text(
                                            text = AppStrings.tr("ui_turbo_keyboard_view_023"),
                                            color = Color(activeTheme.accentColor),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = AppStrings.trf("ui_turbo_keyboard_view_024", spacebarLabel),
                                            color = Color(activeTheme.keyTextColor).copy(alpha = 0.9f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
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
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            isDotPressed = true
                                            performFeedback()
                                            var isLongPressed = false
                                            var hasDragged = false
                                            val dotVariants = listOf(".com", "www.", "https://", ".net", ".org", ".edu", "@gmail.com")
                                            val job = coroutineScope.launch {
                                                delay(currentSettings.longPressDelayMs.toLong())
                                                if (isActive && isDotPressed) {
                                                    isLongPressed = true
                                                    performFeedback(22L)
                                                    longPressChar = "."
                                                    longPressVariants = dotVariants
                                                    selectedVariantIndex = 0
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
                                                        val deltaX = change.position.x - down.position.x
                                                        val deltaY = change.position.y - down.position.y
                                                        if (kotlin.math.abs(deltaX) > 8.dp.toPx() || kotlin.math.abs(deltaY) > 8.dp.toPx()) {
                                                            hasDragged = true
                                                            onDragVariantHandler(deltaX, deltaY)
                                                        }
                                                        change.consume()
                                                    }
                                                }
                                            } finally {
                                                job.cancel()
                                                isDotPressed = false
                                                if (isLongPressed) {
                                                    // Floating table stays open until explicit (X) or item tap
                                                } else {
                                                    sendText(".")
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(".", color = Color(activeTheme.keyTextColor), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }

                            // 5b. Tashkeel / Harakat Key next to Dot
                            if (isArabic && currentSettings.showTashkeelKeyNextToDot) {
                                var isTashkeelPressed by remember { mutableStateOf(false) }
                                val tashkeelBg by animateColorAsState(
                                    targetValue = if (isTashkeelPressed) bottomKeyPressedBg else bottomKeyBg,
                                    animationSpec = tween(durationMillis = if (isTashkeelPressed) 20 else 90),
                                    label = "tashkeel_bg"
                                )
                                val tashkeelScale by animateFloatAsState(
                                    targetValue = if (isTashkeelPressed) 0.95f else 1.0f,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                                    label = "tashkeel_scale"
                                )
                                val harakatVariants = listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ّ", "ْ", "ـ", "ٰ")
                                Box(
                                    modifier = Modifier
                                        .weight(0.65f)
                                        .fillMaxHeight()
                                        .graphicsLayer { scaleX = tashkeelScale; scaleY = tashkeelScale }
                                        .clip(RoundedCornerShape(activeTheme.cornerRadius.dp))
                                        .background(tashkeelBg)
                                        .border(1.dp, Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha.coerceAtLeast(0.35f)), RoundedCornerShape(activeTheme.cornerRadius.dp))
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onPress = {
                                                    isTashkeelPressed = true
                                                    performFeedback()
                                                    tryAwaitRelease()
                                                    isTashkeelPressed = false
                                                },
                                                onTap = {
                                                    sendText("َ")
                                                },
                                                onLongPress = {
                                                    performFeedback(25L, isSpecial = true)
                                                    longPressChar = "َ"
                                                    longPressVariants = harakatVariants
                                                    selectedVariantIndex = 0
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(AppStrings.tr("ui_turbo_keyboard_view_025"), color = Color(activeTheme.accentColor), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
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
                                        awaitEachGesture {
                                            val down = awaitFirstDown(requireUnconsumed = false)
                                            isEnterPressed = true
                                            performFeedback(isSpecial = true)
                                            var isLongPressHandled = false
                                            val holdJob = coroutineScope.launch {
                                                delay(1500L) // Exactly 1.5 seconds long-press as requested
                                                if (isActive && isEnterPressed) {
                                                    isLongPressHandled = true
                                                    performFeedback(durationMs = 50L, isSpecial = true)
                                                    triggerInstantTranslate()
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
                                                }
                                            } finally {
                                                holdJob.cancel()
                                                isEnterPressed = false
                                                if (!isLongPressHandled) {
                                                    sendEnter()
                                                }
                                            }
                                        }
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

                    // Floating Long-Press Character Popup Overlay (Exact match to Image 2 with red (X), 4-col matrix, and bottom floating preview key)
                    if (longPressChar != null && longPressVariants.isNotEmpty()) {
                        val isLight = ThemePresets.isLightColor(activeTheme.backgroundColor)
                        val popupTextCol = if (isLight) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                        val isDotVariants = longPressChar == "."
                        val colCount = if (isDotVariants) 1 else 4

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp)
                                .zIndex(9999f)
                                .offset { IntOffset(popupOffsetX.toInt(), popupOffsetY.toInt()) },
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(activeTheme.backgroundColor).copy(alpha = 0.98f),
                                border = BorderStroke(1.5.dp, Color(activeTheme.accentColor)),
                                shadowElevation = 10.dp,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .clickable(enabled = false) {}
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .wrapContentSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Header matching Image 2: Red [X] on left, Title in center, Accent Badge on right
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 6.dp)
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val speedFactor = currentSettings.floatingNavSpeed / 5.0f
                                                    popupOffsetX += dragAmount.x * speedFactor
                                                    popupOffsetY += dragAmount.y * speedFactor
                                                }
                                            },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Dismiss (X) button
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFEF4444))
                                                .clickable {
                                                    performFeedback()
                                                    dismissLongPressPopup()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = AppStrings.tr("ui_keyboard_sub_views_019"),
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Title: الحروف العائمة (ي) =
                                        Text(
                                            text = AppStrings.trf("ui_turbo_keyboard_view_026", longPressChar ?: ""),
                                            color = Color(activeTheme.keyTextColor),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        // Selected preview badge
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(activeTheme.accentColor).copy(alpha = 0.22f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = longPressVariants.getOrElse(selectedVariantIndex) { longPressChar ?: "" },
                                                color = Color(activeTheme.accentColor),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        if (isDotVariants) {
                                            // Rectangular horizontal row for dot shortcuts
                                            LazyRow(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                items(longPressVariants) { variant ->
                                                    val isCurrent = longPressVariants.indexOf(variant) == selectedVariantIndex
                                                    Box(
                                                        modifier = Modifier
                                                            .height(38.dp)
                                                            .widthIn(min = 72.dp, max = 150.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(
                                                                if (isCurrent) Color(activeTheme.keyPressedColor)
                                                                else Color(activeTheme.keyBackgroundColor)
                                                            )
                                                            .border(
                                                                if (isCurrent) 2.dp else 1.dp,
                                                                if (isCurrent) Color(activeTheme.accentColor) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha),
                                                                RoundedCornerShape(8.dp)
                                                            )
                                                            .clickable {
                                                                performFeedback()
                                                                dismissLongPressPopup()
                                                                sendText(variant)
                                                            }
                                                            .padding(horizontal = 8.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = variant,
                                                            color = if (isCurrent) Color(activeTheme.accentColor) else popupTextCol,
                                                            fontSize = 12.sp,
                                                            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            // Exactly 4-column balanced grid matching Image 2
                                            val rows = longPressVariants.chunked(colCount)
                                            Column(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                rows.forEach { rowItems ->
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        rowItems.forEach { variant ->
                                                            val isCurrent = longPressVariants.indexOf(variant) == selectedVariantIndex
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(width = 48.dp, height = 44.dp)
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(
                                                                        if (isCurrent) Color(activeTheme.keyPressedColor)
                                                                        else Color(activeTheme.keyBackgroundColor)
                                                                    )
                                                                    .border(
                                                                        if (isCurrent) 2.dp else 1.dp,
                                                                        if (isCurrent) Color(activeTheme.accentColor) else Color(activeTheme.borderColor).copy(alpha = activeTheme.borderAlpha),
                                                                        RoundedCornerShape(8.dp)
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
                                                                    fontSize = 18.sp,
                                                                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Floating Target Preview Key centered under the grid matching Image 2
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(activeTheme.keyBackgroundColor),
                                        border = BorderStroke(1.5.dp, Color(activeTheme.accentColor)),
                                        shadowElevation = 6.dp,
                                        modifier = Modifier
                                            .size(width = 54.dp, height = 44.dp)
                                            .clickable {
                                                performFeedback()
                                                val chosen = longPressVariants.getOrElse(selectedVariantIndex) { longPressChar ?: "" }
                                                dismissLongPressPopup()
                                                sendText(chosen)
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = longPressVariants.getOrElse(selectedVariantIndex) { longPressChar ?: "" },
                                                color = Color(activeTheme.keyTextColor),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold
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
    Spacer(modifier = Modifier.height(4.dp))
}
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
    samsungStyleHints: Boolean = true,
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
    onDragVariant: ((Float, Float) -> Unit)? = null,
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

    val normalKeyColor = remember(theme.keyBackgroundColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    }
    val pressedKeyColor = remember(theme.keyPressedColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    }
    val targetKeyColor = if (isTouching) pressedKeyColor else normalKeyColor

    val keyBorderColor = remember(theme.keyStyle, theme.accentColor, theme.borderColor, theme.borderAlpha) {
        if (theme.keyStyle == "neon") Color(theme.accentColor).copy(alpha = 0.85f)
        else if (theme.keyStyle == "glass") Color(0x35FFFFFF)
        else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    }

    val hintText = numberHint ?: if (samsungStyleHints) secondaryHint else null

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
                    val s = if (isTouching) 0.96f else 1.0f
                    scaleX = s
                    scaleY = s
                }
                .clip(RoundedCornerShape(theme.cornerRadius.dp))
                .background(targetKeyColor)
                .border(
                    width = if (theme.keyStyle == "neon") 1.2.dp else if (theme.keyStyle == "glass") 0.8.dp else 1.dp,
                    color = keyBorderColor,
                    shape = RoundedCornerShape(theme.cornerRadius.dp)
                )
                .pointerInput(displayChar, onLongClick != null, longPressDelayMs) {
                    if (onLongClick != null) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            isTouching = true
                            triggerFeedback(0L)
                            val startX = down.position.x
                            val startY = down.position.y
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
                                        val deltaY = change.position.y - startY
                                        // السحب مسموح بجميع الاتجاهات (يمين، يسار، أعلى، أسفل) بحركة منضبطة
                                        if (kotlin.math.abs(deltaX) > 8.dp.toPx() || kotlin.math.abs(deltaY) > 8.dp.toPx()) {
                                            hasDragged = true
                                            currentOnDragVariant?.invoke(deltaX, deltaY)
                                        }
                                        change.consume()
                                    }
                                }
                            } finally {
                                job.cancel()
                                isTouching = false
                                if (isLongPressed) {
                                    // Floating table stays open until explicit (X) or variant tap
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
            val dynamicFontSize = remember(keyHeight, hintText, keyFontSizeFactor, keyButtonScale) {
                val h = keyHeight.value
                val rawBase = if (hintText != null) {
                    (h * 0.40f).coerceIn(10f, 18.5f)
                } else {
                    (h * 0.48f).coerceIn(11f, 21f)
                }
                (rawBase * keyFontSizeFactor * keyButtonScale).sp
            }
            val dynamicHintFontSize = remember(keyHeight) {
                (keyHeight.value * 0.24f).coerceIn(7f, 10.5f).sp
            }

            if (hintText != null) {
                Text(
                    text = hintText,
                    color = Color(theme.subtextColor).copy(alpha = 0.85f),
                    fontSize = dynamicHintFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 1.dp, start = 3.dp)
                )
            }

            Text(
                text = displayChar,
                color = customTextColor ?: Color(theme.keyTextColor),
                fontSize = dynamicFontSize,
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
    val previewBg = Color(0xFF2A2E39)
    val previewTextColor = Color(0xFFFFFFFF)

    Box(
        modifier = Modifier
            .width(52.dp)
            .height(60.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(previewBg)
            .border(
                1.5.dp,
                Color(theme.accentColor),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (numberHint != null) {
            Text(
                text = numberHint,
                color = Color(theme.accentColor),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 3.dp, start = 5.dp)
            )
        }
        Text(
            text = char,
            color = previewTextColor,
            fontSize = 24.sp,
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
    val normalColor = remember(theme.keyBackgroundColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    }
    val pressedColor = remember(theme.keyPressedColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    }
    val accentColor = remember(theme.accentColor) { Color(theme.accentColor) }
    val targetBg = if (isActive) accentColor.copy(alpha = 0.35f) else if (isPressed) pressedColor else normalColor

    val borderColor = remember(theme.keyStyle, theme.borderColor, theme.borderAlpha, isActive, accentColor) {
        if (isActive) accentColor
        else if (theme.keyStyle == "glass") Color(0x35FFFFFF)
        else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    }

    Box(
        modifier = modifier
            .height(keyHeight)
            .graphicsLayer {
                val s = if (isPressed) 0.95f else 1.0f
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(targetBg)
            .border(
                1.dp,
                borderColor,
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

    val normalColor = remember(theme.keyBackgroundColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    }
    val pressedColor = remember(theme.keyPressedColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    }
    val targetBg = if (isPressed) pressedColor else normalColor

    val borderColor = remember(theme.borderColor, theme.borderAlpha, theme.accentColor, isPressed) {
        if (isPressed) Color(theme.accentColor)
        else Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    }

    Box(
        modifier = modifier
            .height(keyHeight)
            .graphicsLayer {
                val s = if (isPressed) 0.96f else 1.0f
                scaleX = s
                scaleY = s
            }
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(targetBg)
            .border(
                1.dp,
                borderColor,
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
            contentDescription = AppStrings.tr("ui_turbo_keyboard_view_027"),
            tint = Color(theme.keyTextColor),
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun CustomizeTopEmojisInlinePanel(
    theme: KeyboardTheme,
    workingList: List<String>,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onAddEmoji: (String) -> Unit,
    onRemoveEmoji: (String) -> Unit,
    heightFactor: Float,
    onHeightFactorChange: (Float) -> Unit,
    buttonSizeFactor: Float,
    onButtonSizeFactorChange: (Float) -> Unit,
    onResetDefaults: () -> Unit,
    onSaveAndClose: () -> Unit,
    onDismiss: () -> Unit,
    onOpenEmojiPicker: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var showSliders by remember { mutableStateOf(false) }
    var activeEmojiCat by remember { mutableIntStateOf(0) }
    val emojiCategories = remember {
        listOf(
            "فيسات 😊" to listOf("😂", "😍", "🥺", "😭", "🤣", "😇", "😋", "😎", "🥳", "🤔", "😴", "🙄", "😤", "🤐", "😷", "🤯", "🥶", "🥵", "😡", "🤡", "😏", "🤤", "🤭", "🤫"),
            "قلوب ❤️" to listOf("❤️", "🤍", "🖤", "💖", "💔", "💕", "💓", "💗", "💘", "💙", "💚", "💛", "💜", "🤎", "🧡", "❣️", "💌", "💋", "💝", "💞"),
            "تفاعلات 🔥" to listOf("🔥", "👏", "👍", "👎", "👑", "✨", "💯", "🤲", "🌹", "🌸", "⚡", "💎", "🕊️", "🍿", "🚀", "💡", "🌙", "☀️", "🎉", "💐"),
            "رموز 🔣" to listOf("ـ", "؛", "،", "؟", "!", "@", "#", "$", "%", "&", "*", "(", ")", "-", "+", "=", "/", ":", "•", "✓", "«", "»", "±", "×", "÷"),
            "حروف 🔤" to listOf("أ", "ب", "ت", "ث", "ج", "ح", "خ", "د", "ذ", "ر", "ز", "س", "ش", "ص", "ض", "ط", "ظ", "ع", "غ", "ف", "ق", "ك", "ل", "م", "ن", "ه", "و", "ي"),
            "ملصقات 🎨" to listOf("صباح الخير 🌸", "مساء الورد 🌹", "شكراً جزيلاً ✨", "جزاك الله خيراً 🤲", "ما شاء الله 💫", "بالتوفيق يا رب 🤍", "الحمد لله 🤲", "كل عام وأنتم بخير 🎉", "ʕ•́ᴥ•̀ʔ", "(•‿•)", "¯\\_(ツ)_/¯", "(｡♥‿♥｡)")
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            color = Color(theme.backgroundColor),
            border = BorderStroke(1.2.dp, Color(0xFF0084FF).copy(alpha = 0.35f)),
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header: Close on Right, Title & Subtitle in Center, Tune/Sliders on Left (RTL matching screenshot)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Close button (X) in round gray background
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0).copy(alpha = 0.7f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.tr("ui_clipboard_screen_019"),
                            tint = Color(theme.keyTextColor),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Title & Subtitle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = AppStrings.tr("ui_settings_screen_041"),
                            color = Color(theme.keyTextColor),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppStrings.tr("ui_turbo_keyboard_view_028"),
                            color = Color(theme.subtextColor),
                            fontSize = 9.5.sp
                        )
                    }

                    // Tune / Sliders toggle button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if (showSliders) Color(0xFF0084FF).copy(alpha = 0.2f) else Color(0xFFE2E8F0).copy(alpha = 0.7f))
                            .clickable { showSliders = !showSliders },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = AppStrings.tr("ui_turbo_keyboard_view_029"),
                            tint = if (showSliders) Color(0xFF0084FF) else Color(theme.keyTextColor),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Current emojis chips with '×' on left and emoji on right (Matching Screenshot)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(theme.toolbarColor).copy(alpha = 0.6f), RoundedCornerShape(9.dp))
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(workingList) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1).copy(alpha = 0.8f)),
                            modifier = Modifier.clickable { onRemoveEmoji(item) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = AppStrings.tr("ui_clipboard_screen_015"),
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = item,
                                    color = Color(theme.keyTextColor),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Input Row: Add Button (Left in RTL) + Real Input Field with Paste & Clear (Right in RTL)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Add button (Solid primary blue)
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onAddEmoji(inputText)
                                onInputTextChange("")
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(AppStrings.tr("ui_settings_screen_044"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // 2. Input Box with Direct Paste & Clear
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(theme.keyBackgroundColor),
                        border = BorderStroke(1.dp, if (inputText.isNotEmpty()) Color(0xFF0084FF) else Color(theme.borderColor).copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Text input area (supports real typing & cursor & system copy/paste)
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = AppStrings.tr("ui_turbo_keyboard_view_030"),
                                        color = Color(theme.subtextColor).copy(alpha = 0.75f),
                                        fontSize = 10.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = onInputTextChange,
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color(theme.keyTextColor),
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFF0084FF)),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Paste button (زر لصق من الحافظة مباشرة)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0084FF).copy(alpha = 0.12f),
                                border = BorderStroke(0.8.dp, Color(0xFF0084FF).copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    val clip = clipboardManager.getText()?.text?.toString()?.trim()
                                    if (!clip.isNullOrEmpty()) {
                                        onInputTextChange(clip)
                                        onAddEmoji(clip)
                                        android.widget.Toast.makeText(context, AppStrings.trf(context, "ui_turbo_keyboard_view_031", clip), android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, AppStrings.tr(context, "ui_settings_screen_118"), android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentPaste,
                                        contentDescription = AppStrings.tr("ui_settings_screen_127"),
                                        tint = Color(0xFF0084FF),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(AppStrings.tr("ui_settings_screen_127"), color = Color(0xFF0084FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Clear button (X) when input is not empty
                            if (inputText.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(theme.borderColor).copy(alpha = 0.25f))
                                        .clickable { onInputTextChange("") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = AppStrings.tr("ui_clipboard_screen_003"), tint = Color(theme.subtextColor), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }

                // Quick emoji categories tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    emojiCategories.indices.forEach { idx ->
                        val isCatSelected = activeEmojiCat == idx
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = if (isCatSelected) Color(0xFF0084FF) else Color(theme.keyBackgroundColor),
                            border = BorderStroke(0.8.dp, if (isCatSelected) Color(0xFF0084FF) else Color(theme.borderColor).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeEmojiCat = idx }
                        ) {
                            Text(
                                text = emojiCategories[idx].first,
                                color = if (isCatSelected) Color.White else Color(theme.keyTextColor),
                                fontSize = 9.5.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                // Scrollable row of items in active category (click to add directly)
                val currentCategoryItems = emojiCategories[activeEmojiCat].second
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(theme.toolbarColor).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(currentCategoryItems) { item ->
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = 0.45f)),
                            modifier = Modifier.clickable {
                                onAddEmoji(item)
                            }
                        ) {
                            Text(
                                text = item,
                                fontSize = 15.5.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Extra shortcut item at the end of the row: Browse full emojis/stickers
                    if (onOpenEmojiPicker != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(7.dp),
                                color = Color(0xFF0084FF).copy(alpha = 0.15f),
                                border = BorderStroke(0.8.dp, Color(0xFF0084FF).copy(alpha = 0.4f)),
                                modifier = Modifier.clickable { onOpenEmojiPicker() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF0084FF), modifier = Modifier.size(13.dp))
                                    Text(AppStrings.tr("ui_turbo_keyboard_view_032"), color = Color(0xFF0084FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Expandable sliders for height and icon size
                if (showSliders) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(theme.toolbarColor), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(AppStrings.trf("ui_turbo_keyboard_view_033", (heightFactor * 100).toInt()), color = Color(theme.keyTextColor), fontSize = 10.sp)
                            Text(AppStrings.trf("ui_turbo_keyboard_view_034", (buttonSizeFactor * 100).toInt()), color = Color(0xFF0084FF), fontSize = 10.sp)
                        }
                        Slider(
                            value = heightFactor,
                            onValueChange = onHeightFactorChange,
                            valueRange = 0.7f..1.6f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0084FF), activeTrackColor = Color(0xFF0084FF)),
                            modifier = Modifier.fillMaxWidth().height(22.dp)
                        )
                        Slider(
                            value = buttonSizeFactor,
                            onValueChange = onButtonSizeFactorChange,
                            valueRange = 0.7f..1.6f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF0084FF), activeTrackColor = Color(0xFF0084FF)),
                            modifier = Modifier.fillMaxWidth().height(22.dp)
                        )
                    }
                }

                // Bottom Actions Row matching Screenshot: Save button on left, Restore Default on right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onResetDefaults() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(AppStrings.tr("ui_settings_screen_047"), color = Color(theme.subtextColor), fontSize = 11.sp)
                    }

                    if (onOpenEmojiPicker != null) {
                        TextButton(
                            onClick = { onOpenEmojiPicker() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Outlined.Mood, contentDescription = null, tint = Color(0xFF0084FF), modifier = Modifier.size(14.dp))
                                Text(AppStrings.tr("ui_turbo_keyboard_view_035"), color = Color(0xFF0084FF), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Button(
                        onClick = { onSaveAndClose() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(AppStrings.tr("ui_turbo_keyboard_view_036"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsManagerInlinePanel(
    theme: KeyboardTheme,
    settings: KeyboardSettings,
    onUpdateSettings: (KeyboardSettings) -> Unit,
    onDismiss: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(theme.backgroundColor),
            border = BorderStroke(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.45f)),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header: Close Button, Title & Subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0).copy(alpha = 0.7f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.tr("ui_clipboard_screen_019"),
                            tint = Color(theme.keyTextColor),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = AppStrings.tr("ui_turbo_keyboard_view_037"),
                            color = Color(theme.keyTextColor),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppStrings.tr("ui_turbo_keyboard_view_038"),
                            color = Color(theme.subtextColor),
                            fontSize = 9.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .clickable {
                                onUpdateSettings(settings.copy(showQuickActionsRow = !settings.showQuickActionsRow))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (settings.showQuickActionsRow) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Prominent Direct Action Button: Delete or Restore Entire Bar
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (settings.showQuickActionsRow) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, if (settings.showQuickActionsRow) Color(0xFFEF4444).copy(alpha = 0.6f) else Color(0xFF10B981).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUpdateSettings(settings.copy(showQuickActionsRow = !settings.showQuickActionsRow))
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (settings.showQuickActionsRow) Icons.Default.Delete else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (settings.showQuickActionsRow) Color(0xFFEF4444) else Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = if (settings.showQuickActionsRow) "حذف وإخفاء شريط الأدوات بالكامل" else "إظهار شريط الأدوات فوق الأحرف",
                                    color = if (settings.showQuickActionsRow) Color(0xFFEF4444) else Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    text = if (settings.showQuickActionsRow) "اضغط هنا لإزالة الشريط بالكامل فوراً" else "الشريط مخفي حالياً، اضغط لتفعيله",
                                    color = Color(theme.subtextColor),
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Switch(
                            checked = settings.showQuickActionsRow,
                            onCheckedChange = { onUpdateSettings(settings.copy(showQuickActionsRow = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981), checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f)),
                            modifier = Modifier.scale(0.75f)
                        )
                    }
                }

                // Grid / List of Individual Action Buttons for Deletion / Toggle
                val items = listOf(
                    Triple("مسح الكل", Icons.Default.DeleteSweep, settings.showQuickActionClearAll) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionClearAll = enabled))
                    },
                    Triple("قراءة النص", Icons.Default.VolumeUp, settings.showQuickActionReadText) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionReadText = enabled))
                    },
                    Triple("لصق", Icons.Default.ContentPaste, settings.showQuickActionPaste) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionPaste = enabled))
                    },
                    Triple("الحافظة", Icons.Default.ContentPaste, settings.showQuickActionClipboard) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionClipboard = enabled))
                    },
                    Triple("نسخ", Icons.Default.ContentCopy, settings.showQuickActionCopy) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionCopy = enabled))
                    },
                    Triple("قص", Icons.Default.ContentCut, settings.showQuickActionCut) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionCut = enabled))
                    },
                    Triple("تحديد الكل", Icons.Default.SelectAll, settings.showQuickActionSelectAll) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionSelectAll = enabled))
                    },
                    Triple("استعادة بعد الحذف", Icons.Default.Undo, settings.showQuickActionUndoRedo) to { enabled: Boolean ->
                        onUpdateSettings(settings.copy(showQuickActionUndoRedo = enabled))
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .background(Color(theme.toolbarColor).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    items(items) { (info, toggle) ->
                        val (name, icon, isEnabled) = info
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(0.7.dp, if (isEnabled) Color(theme.borderColor).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isEnabled) Color(theme.accentColor) else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = name,
                                        color = if (isEnabled) Color(theme.keyTextColor) else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = if (isEnabled) FontWeight.Medium else FontWeight.Normal
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isEnabled) {
                                        // Quick Delete Button (Red Trash Icon)
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                                .clickable { toggle(false) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = AppStrings.tr("ui_turbo_keyboard_view_039"),
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isEnabled,
                                        onCheckedChange = { toggle(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color(0xFF0084FF),
                                            checkedTrackColor = Color(0xFF0084FF).copy(alpha = 0.4f)
                                        ),
                                        modifier = Modifier.scale(0.65f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom row: Restore defaults & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onUpdateSettings(
                                settings.copy(
                                    showQuickActionsRow = true,
                                    showQuickActionClearAll = true,
                                    showQuickActionReadText = true,
                                    showQuickActionPaste = true,
                                    showQuickActionCopy = true,
                                    showQuickActionCut = true,
                                    showQuickActionSelectAll = true,
                                    showQuickActionUndoRedo = true,
                                    showQuickActionClipboard = true
                                )
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(AppStrings.tr("ui_settings_screen_047"), color = Color(theme.subtextColor), fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            Text(AppStrings.tr("ui_turbo_keyboard_view_040"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

