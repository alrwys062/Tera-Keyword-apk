package com.example.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TurboKeyboardView(
    theme: KeyboardTheme = ThemePresets.CYBER_PRO,
    settings: KeyboardSettings = KeyboardSettings(),
    inputConnection: InputConnection? = null,
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
    var translationSource by remember { mutableStateOf(settings.translationSource) }
    var translationTarget by remember { mutableStateOf(settings.translationTarget) }

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
    var isNightMode by remember { mutableStateOf(true) }

    // Prediction suggestions + auto-correction + shortcuts expansion
    val suggestions = remember(currentComposingText, currentWorldLang, userWords, shortcuts) {
        if (settings.suggestionsEnabled) {
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

    // Sound & Haptic triggers
    fun performFeedback() {
        if (settings.vibrationEnabled) {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    val duration = settings.vibrationDurationMs.toLong().coerceAtLeast(10L)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(duration)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun sendText(text: String) {
        performFeedback()
        val textToInsert = if (activeDecorationStyle != "none" && text.length == 1) {
            TextDecorator.decorateChar(text, activeDecorationStyle)
        } else {
            text
        }
        currentComposingText += textToInsert
        if (inputConnection != null) {
            inputConnection.commitText(textToInsert, 1)
        } else {
            onDirectInsertText?.invoke(textToInsert)
        }
    }

    fun sendDelete() {
        performFeedback()
        if (currentComposingText.isNotEmpty()) {
            currentComposingText = currentComposingText.dropLast(1)
        }
        if (inputConnection != null) {
            inputConnection.deleteSurroundingText(1, 0)
        } else {
            onDirectDeleteLastChar?.invoke()
        }
    }

    fun sendEnter() {
        performFeedback()
        if (inputConnection != null) {
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            inputConnection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        } else {
            onDirectInsertText?.invoke("\n")
        }
        currentComposingText = ""
    }

    fun replaceAllText(newText: String) {
        currentComposingText = newText
        if (inputConnection != null) {
            inputConnection.beginBatchEdit()
            val before = inputConnection.getTextBeforeCursor(2000, 0)?.length ?: 0
            val after = inputConnection.getTextAfterCursor(2000, 0)?.length ?: 0
            inputConnection.deleteSurroundingText(before, after)
            inputConnection.commitText(newText, 1)
            inputConnection.endBatchEdit()
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(bgModifier)
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
            theme = theme,
            activeSubView = activeSubView,
            isDecorationActive = isDecorationBarOpen || activeDecorationStyle != "none",
            isTranslationActive = isTranslationBarOpen,
            isNightMode = isNightMode,
            onSubViewSelected = { sub ->
                activeSubView = sub
                if (sub != KeyboardSubView.NONE) {
                    isDecorationBarOpen = false
                    isTranslationBarOpen = false
                }
            },
            onToggleDecorationBar = {
                isDecorationBarOpen = !isDecorationBarOpen
                if (isDecorationBarOpen) activeSubView = KeyboardSubView.NONE
            },
            onToggleTranslationBar = {
                isTranslationBarOpen = !isTranslationBarOpen
                if (isTranslationBarOpen) activeSubView = KeyboardSubView.NONE
            },
            onToggleNightMode = {
                isNightMode = !isNightMode
            },
            onVoiceClick = { onVoiceRequested?.invoke() },
            onOpenSettingsClick = { onOpenSettingsRequested?.invoke() }
        )

        // 2. INLINE TRANSLATION BAR (Screenshot 20)
        AnimatedVisibility(visible = isTranslationBarOpen) {
            InlineTranslationBar(
                theme = theme,
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

        // 3. INLINE TEXT DECORATION BAR & DROPDOWN (Screenshots 18 & 19)
        AnimatedVisibility(visible = isDecorationBarOpen) {
            InlineDecorationBar(
                theme = theme,
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

        // Sub-views drawers (Clipboard, Emoji, GIF, AI Assistant, Photos)
        AnimatedVisibility(
            visible = activeSubView != KeyboardSubView.NONE,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            when (activeSubView) {
                KeyboardSubView.EMOJI -> {
                    EmojiPickerView(
                        theme = theme,
                        onEmojiSelected = {
                            prefs.addRecentEmoji(it)
                            sendText(it)
                        },
                        onClose = { activeSubView = KeyboardSubView.NONE }
                    )
                }
                KeyboardSubView.GIF -> {
                    GifPickerView(
                        theme = theme,
                        onGifSelected = { sendText(" $it ") },
                        onClose = { activeSubView = KeyboardSubView.NONE }
                    )
                }
                KeyboardSubView.PHOTOS -> {
                    MediaPickerView(
                        theme = theme,
                        onMediaSelected = { mediaText ->
                            sendText(" $mediaText ")
                            activeSubView = KeyboardSubView.NONE
                        },
                        onClose = { activeSubView = KeyboardSubView.NONE }
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
                            // Auto-close on paste requested by user
                            activeSubView = KeyboardSubView.NONE
                        },
                        onClose = { activeSubView = KeyboardSubView.NONE }
                    )
                }
                KeyboardSubView.AI_ASSISTANT -> {
                    AiToneDrawer(
                        theme = theme,
                        currentText = getCurrentText?.invoke()?.ifBlank { currentComposingText } ?: currentComposingText,
                        onReplaceText = { replaceAllText(it) },
                        onAddWordToDictionary = { word ->
                            userDict.addWord(word)
                            userWords = userDict.getUserWords()
                        },
                        onClose = { activeSubView = KeyboardSubView.NONE }
                    )
                }
                else -> {}
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
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                performFeedback()
                                sendText(char)
                            },
                        shape = RoundedCornerShape(theme.cornerRadius.dp),
                        color = Color(theme.keyBackgroundColor).copy(alpha = 0.65f),
                        border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = 0.4f))
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                // Long-Press Character Popup Overlay (Matching Screenshots 1, 2, 4)
                AnimatedVisibility(
                    visible = longPressChar != null && longPressVariants.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF141A26),
                            border = BorderStroke(1.5.dp, Color(0xFF00B0FF)),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                longPressVariants.forEach { variant ->
                                    val isCurrent = variant == longPressChar
                                    Box(
                                        modifier = Modifier
                                            .size(width = 38.dp, height = 44.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (isCurrent) Color(0xFF007ACC)
                                                else Color(0xFF1F293B)
                                            )
                                            .border(
                                                1.dp,
                                                if (isCurrent) Color(0xFF00B0FF) else Color(0xFF2E3E58),
                                                RoundedCornerShape(6.dp)
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
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Dismiss X button
                                Box(
                                    modifier = Modifier
                                        .size(width = 32.dp, height = 44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF2D1E26))
                                        .border(1.dp, Color(0xFFFF5252).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .clickable {
                                            longPressChar = null
                                            longPressVariants = emptyList()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Optional Number Row
                if (settings.numberRowEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val row = if (isArabic) KeyLayouts.numbersRowAr else KeyLayouts.numbersRowEn
                        row.forEach { key ->
                            val digitChar = (key.type as KeyType.Character).primary
                            KeyButton(
                                key = key,
                                theme = theme,
                                isShifted = isShifted,
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            theme = theme,
                            isShifted = isShifted,
                            numberHint = hint,
                            modifier = Modifier.weight(key.weight),
                            onClick = {
                                sendText(if (isShifted && !currentWorldLang.isRtl && !isSymbolsMode) char.uppercase() else char)
                            },
                            onLongClick = {
                                performFeedback()
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                            theme = theme,
                            isShifted = isShifted,
                            modifier = Modifier.weight(key.weight),
                            onClick = {
                                sendText(if (isShifted && !currentWorldLang.isRtl && !isSymbolsMode) char.uppercase() else char)
                            },
                            onLongClick = {
                                performFeedback()
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                                    theme = theme,
                                    isActive = isShifted,
                                    modifier = Modifier.weight(key.weight),
                                    onClick = {
                                        performFeedback()
                                        isShifted = !isShifted
                                    }
                                )
                            }
                            is KeyType.Backspace -> {
                                BackspaceKeyButton(
                                    theme = theme,
                                    modifier = Modifier.weight(key.weight),
                                    onDelete = { sendDelete() },
                                    onDeleteWord = {
                                        performFeedback()
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
                                        theme = theme,
                                        modifier = Modifier.weight(key.weight),
                                        onClick = {
                                            performFeedback()
                                            isMoreSymbolsMode = true
                                        }
                                    )
                                } else if (key.type.primary == "123") {
                                    SpecialKeyButton(
                                        text = "123",
                                        theme = theme,
                                        modifier = Modifier.weight(key.weight),
                                        onClick = {
                                            performFeedback()
                                            isMoreSymbolsMode = false
                                        }
                                    )
                                } else {
                                    val char = key.type.primary
                                    KeyButton(
                                        key = key,
                                        theme = theme,
                                        isShifted = isShifted,
                                        modifier = Modifier.weight(key.weight),
                                        onClick = {
                                            sendText(if (isShifted && !isArabic && !isSymbolsMode) char.uppercase() else char)
                                        },
                                        onLongClick = {
                                            performFeedback()
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

                // Row 4: Exact bottom row from Screenshots 1, 2, 4, 5:
                // [ ١٢٣ / ?123 (cyan) ] [ 🌐 Language (cyan) ] [ 📋 Clipboard ] [   Spacebar   ] [ . ] [ 😊 ] [ ✓ Enter (cyan) ]
                var enterJob: Job? by remember { mutableStateOf(null) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. ModeChange key 123!#() on FAR LEFT
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.9f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                isSymbolsMode = !isSymbolsMode
                                isMoreSymbolsMode = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSymbolsMode) currentWorldLang.nameNative else "123!#()",
                            color = Color(theme.keyTextColor),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 2. Clipboard shortcut button 📋
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.9f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                activeSubView = if (activeSubView == KeyboardSubView.CLIPBOARD) KeyboardSubView.NONE else KeyboardSubView.CLIPBOARD
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentPaste,
                            contentDescription = "Clipboard",
                            tint = Color(theme.accentColor),
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // 3. .com button
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.9f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                sendText(".com")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ".com",
                            color = Color(theme.keyTextColor).copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // 4. Spacebar in the center with active world language name
                    Box(
                        modifier = Modifier
                            .weight(3.5f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.9f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = {
                                        if (kotlin.math.abs(totalDragX) > 60f && settings.swipeSpaceSwitchLanguage) {
                                            performFeedback()
                                            val nextLang = InputLanguagesManager.getNextActiveLanguage(context, currentLangCode)
                                            currentLangCode = nextLang.code
                                        }
                                        totalDragX = 0f
                                    }
                                ) { change, dragAmount ->
                                    change.consume()
                                    totalDragX += dragAmount.x
                                }
                            }
                            .clickable {
                                performFeedback()
                                sendText(" ")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = currentWorldLang.nameNative,
                                color = Color(theme.keyTextColor).copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 5. Slash / key
                    Box(
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.9f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                sendText("/")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("/", color = Color(theme.keyTextColor), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // 6. Dot . key
                    Box(
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.9f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                sendText(".")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(".", color = Color(theme.keyTextColor), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // 7. Enter key on the FAR RIGHT with 4-sec translate!
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(theme.enterButtonColor),
                                        Color(theme.enterButtonColor).copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .border(1.dp, Color(theme.accentColor).copy(alpha = 0.7f), RoundedCornerShape(theme.cornerRadius.dp))
                            .pointerInput(settings.enterLongPressTranslateEnabled) {
                                detectTapGestures(
                                    onPress = {
                                        performFeedback()
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
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}

private val enToArHintMap = mapOf(
    "q" to "ض", "w" to "ص", "e" to "ث", "r" to "ق", "t" to "ف",
    "y" to "غ", "u" to "ع", "i" to "ه", "o" to "خ", "p" to "ح",
    "a" to "ش", "s" to "س", "d" to "ي", "f" to "ب", "g" to "ل",
    "h" to "ا", "j" to "ت", "k" to "ن", "l" to "م",
    "z" to "ئ", "x" to "ء", "c" to "ؤ", "v" to "ر", "b" to "لا",
    "n" to "ى", "m" to "ة"
)

private val arToEnHintMap = enToArHintMap.entries.associate { (k, v) -> v to k }

@Composable
fun KeyButton(
    key: KeyModel,
    theme: KeyboardTheme,
    isShifted: Boolean,
    numberHint: String? = null,
    customTextColor: Color? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val char = (key.type as? KeyType.Character)?.primary ?: ""
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val displayChar = if (isShifted) char.uppercase() else char
    val dualHint = if (theme.dualLanguageHints) {
        enToArHintMap[char.lowercase()] ?: arToEnHintMap[char]
    } else null

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(
                if (isPressed) Color(theme.keyPressedColor)
                else Color(theme.keyBackgroundColor)
            )
            .border(
                width = if (theme.keyStyle == "neon") 1.2.dp else 1.dp,
                color = if (theme.keyStyle == "neon") Color(theme.accentColor).copy(alpha = 0.75f)
                else Color(theme.borderColor).copy(alpha = theme.borderAlpha),
                shape = RoundedCornerShape(theme.cornerRadius.dp)
            )
            .pointerInput(displayChar) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        if (onLongClick != null) onLongClick()
                        else onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (numberHint != null) {
            Text(
                text = numberHint,
                color = Color(0xFF00B0FF).copy(alpha = 0.85f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 1.dp, start = 3.dp)
            )
        } else if (dualHint != null) {
            Text(
                text = dualHint,
                color = Color(theme.subtextColor).copy(alpha = 0.75f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 4.dp)
            )
        }

        Text(
            text = displayChar,
            color = customTextColor ?: Color(theme.keyTextColor),
            fontSize = if (numberHint != null || dualHint != null) 17.sp else 18.sp,
            fontWeight = FontWeight.SemiBold
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(
                if (isActive) Color(theme.accentColor).copy(alpha = 0.35f)
                else Color(theme.keyBackgroundColor).copy(alpha = 0.9f)
            )
            .border(
                1.dp,
                if (isActive) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = theme.borderAlpha),
                RoundedCornerShape(theme.cornerRadius.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = customIconColor ?: if (isActive) Color(theme.accentColor) else Color(theme.keyTextColor),
                modifier = Modifier.size(20.dp)
            )
        } else if (text != null) {
            Text(
                text = text,
                color = if (isActive) Color(theme.accentColor) else Color(theme.keyTextColor),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BackspaceKeyButton(
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onDeleteWord: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.9f))
            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onDelete()
                    },
                    onLongPress = {
                        onDeleteWord()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Backspace",
            tint = Color(theme.keyTextColor),
            modifier = Modifier.size(20.dp)
        )
    }
}
