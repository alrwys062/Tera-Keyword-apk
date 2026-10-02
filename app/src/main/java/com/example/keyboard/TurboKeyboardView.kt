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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PredictionEngine
import com.example.data.PreferencesManager
import com.example.data.TranslationEngine
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
    onThemeRequested: (() -> Unit)? = null,
    prefsManager: PreferencesManager? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember { prefsManager ?: PreferencesManager(context) }

    var currentLang by remember {
        mutableStateOf(if (settings.defaultLanguage == "en") KeyboardLanguage.ENGLISH else KeyboardLanguage.ARABIC)
    }
    var isShifted by remember { mutableStateOf(false) }
    var isSymbolsMode by remember { mutableStateOf(false) }
    var isMoreSymbolsMode by remember { mutableStateOf(false) }
    var activeSubView by remember { mutableStateOf(KeyboardSubView.NONE) }

    // Live text tracking for prediction and special 4-sec translate
    var currentComposingText by remember { mutableStateOf("") }
    var lastSpacePressTime by remember { mutableLongStateOf(0L) }

    // 4-sec Enter hold translation state
    var isEnterHolding by remember { mutableStateOf(false) }
    var enterHoldProgress by remember { mutableFloatStateOf(0f) }
    var enterHoldMessage by remember { mutableStateOf("") }

    val isArabic = currentLang == KeyboardLanguage.ARABIC

    val userDict = remember { com.example.data.UserDictionaryManager(context) }
    val userWords = remember { userDict.getUserWords() }

    // Prediction suggestions with user dictionary
    val suggestions = remember(currentComposingText, currentLang, userWords) {
        if (settings.suggestionsEnabled) {
            val lastWord = currentComposingText.trim().split(" ").lastOrNull() ?: ""
            PredictionEngine.getPredictions(lastWord, isArabic, userWords)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(20)
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Input actions
    fun sendText(text: String) {
        performFeedback()
        currentComposingText += text
        if (inputConnection != null) {
            inputConnection.commitText(text, 1)
        } else {
            onDirectInsertText?.invoke(text)
        }

        if (isShifted) {
            isShifted = false
        }
    }

    fun deleteChar() {
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

    fun deleteWord() {
        performFeedback()
        if (inputConnection != null) {
            val textBefore = inputConnection.getTextBeforeCursor(60, 0)?.toString() ?: ""
            val trimmed = textBefore.trimEnd()
            val lastWord = trimmed.split("\\s+".toRegex()).lastOrNull() ?: ""
            val deleteCount = (textBefore.length - trimmed.length) + lastWord.length
            if (deleteCount > 0) {
                inputConnection.deleteSurroundingText(deleteCount, 0)
            } else {
                inputConnection.deleteSurroundingText(1, 0)
            }
        } else {
            val words = currentComposingText.trimEnd().split(" ")
            currentComposingText = if (words.size > 1) words.dropLast(1).joinToString(" ") + " " else ""
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
            // Delete text in current line / field and insert new text
            inputConnection.deleteSurroundingText(100, 100)
            inputConnection.commitText(newText, 1)
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

        // Toolbar (Emoji, GIF, Translate, Clipboard, Voice, Night)
        KeyboardToolbar(
            theme = theme,
            activeSubView = activeSubView,
            onSubViewSelected = { activeSubView = it },
            onVoiceClick = { onVoiceRequested?.invoke() },
            onThemeToggleClick = { onThemeRequested?.invoke() },
            isArabic = isArabic
        )

        // Subviews drawer (if open)
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
                KeyboardSubView.TRANSLATE -> {
                    QuickTranslateView(
                        theme = theme,
                        currentText = getCurrentText?.invoke() ?: currentComposingText,
                        onInsertTranslated = {
                            sendText(it)
                            activeSubView = KeyboardSubView.NONE
                        },
                        onClose = { activeSubView = KeyboardSubView.NONE }
                    )
                }
                KeyboardSubView.CLIPBOARD -> {
                    ClipboardDrawer(
                        theme = theme,
                        prefs = prefs,
                        onItemInserted = {
                            sendText(it)
                            activeSubView = KeyboardSubView.NONE
                        },
                        onClose = { activeSubView = KeyboardSubView.NONE }
                    )
                }
                KeyboardSubView.DECORATION -> {
                    DecorationDrawer(
                        theme = theme,
                        sampleText = getCurrentText?.invoke()?.ifBlank { currentComposingText } ?: currentComposingText,
                        onApplyDecoratedText = {
                            replaceAllText(it)
                            activeSubView = KeyboardSubView.NONE
                        },
                        onClose = { activeSubView = KeyboardSubView.NONE }
                    )
                }
                else -> {}
            }
        }

        // Suggestions Bar (only when main keyboard is active)
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
                }
            )
        }

        // Main Keyboard Keys Section
        if (activeSubView == KeyboardSubView.NONE) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Optional Number Row
                if (settings.numberRowEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val row = if (isArabic) KeyLayouts.numbersRowAr else KeyLayouts.numbersRowEn
                        row.forEach { key ->
                            KeyButton(
                                key = key,
                                theme = theme,
                                isShifted = isShifted,
                                modifier = Modifier.weight(key.weight),
                                onClick = { sendText((key.type as KeyType.Character).primary) }
                            )
                        }
                    }
                }

                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val row = when {
                        isSymbolsMode && !isMoreSymbolsMode -> KeyLayouts.symbolsRow1
                        isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.symbolsRow1
                        isArabic && isShifted -> KeyLayouts.arabicTashkeelRow1
                        isArabic -> KeyLayouts.arabicRow1
                        else -> KeyLayouts.englishRow1
                    }
                    row.forEach { key ->
                        KeyButton(
                            key = key,
                            theme = theme,
                            isShifted = isShifted,
                            modifier = Modifier.weight(key.weight),
                            onClick = {
                                val char = (key.type as KeyType.Character).primary
                                sendText(if (isShifted && !isArabic && !isSymbolsMode) char.uppercase() else char)
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
                        isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.moreSymbolsRow2
                        isArabic && isShifted -> KeyLayouts.arabicTashkeelRow2
                        isArabic -> KeyLayouts.arabicRow2
                        else -> KeyLayouts.englishRow2
                    }

                    // On English keyboard, add subtle side spacers for standard layout
                    if (!isArabic && !isSymbolsMode) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }

                    row.forEach { key ->
                        KeyButton(
                            key = key,
                            theme = theme,
                            isShifted = isShifted,
                            modifier = Modifier.weight(key.weight),
                            onClick = {
                                val char = (key.type as KeyType.Character).primary
                                sendText(if (isShifted && !isArabic && !isSymbolsMode) char.uppercase() else char)
                            }
                        )
                    }

                    if (!isArabic && !isSymbolsMode) {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }

                // Row 3 (Shift, characters, Backspace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val row = when {
                        isSymbolsMode && !isMoreSymbolsMode -> KeyLayouts.symbolsRow3
                        isSymbolsMode && isMoreSymbolsMode -> KeyLayouts.moreSymbolsRow3
                        isArabic -> KeyLayouts.arabicRow3
                        else -> KeyLayouts.englishRow3
                    }

                    row.forEach { key ->
                        when (key.type) {
                            is KeyType.Shift -> {
                                SpecialKeyButton(
                                    icon = if (isSymbolsMode) {
                                        if (isMoreSymbolsMode) Icons.Default.LooksOne else Icons.Default.LooksTwo
                                    } else {
                                        Icons.Default.ArrowUpward
                                    },
                                    isActive = isShifted,
                                    theme = theme,
                                    modifier = Modifier.weight(key.weight),
                                    onClick = {
                                        performFeedback()
                                        if (isSymbolsMode) {
                                            isMoreSymbolsMode = !isMoreSymbolsMode
                                        } else {
                                            isShifted = !isShifted
                                        }
                                    }
                                )
                            }
                            is KeyType.Backspace -> {
                                SpecialKeyButton(
                                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                                    isActive = false,
                                    theme = theme,
                                    modifier = Modifier.weight(key.weight),
                                    onClick = { deleteChar() }
                                )
                            }
                            is KeyType.Character -> {
                                KeyButton(
                                    key = key,
                                    theme = theme,
                                    isShifted = isShifted,
                                    modifier = Modifier.weight(key.weight),
                                    onClick = {
                                        val char = key.type.primary
                                        sendText(if (isShifted && !isArabic && !isSymbolsMode) char.uppercase() else char)
                                    }
                                )
                            }
                            else -> {}
                        }
                    }
                }

                // Row 4 (ModeChange 123, Emoji, Spacebar with Language swipe, Enter with 4s translate)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mode change key (123 / ABC / العربية)
                    Box(
                        modifier = Modifier
                            .weight(1.3f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                isSymbolsMode = !isSymbolsMode
                                isMoreSymbolsMode = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSymbolsMode) (if (isArabic) "العربية" else "ABC") else "123",
                            color = Color(theme.keyTextColor),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Globe / Switch language key
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                currentLang = if (currentLang == KeyboardLanguage.ARABIC) KeyboardLanguage.ENGLISH else KeyboardLanguage.ARABIC
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Switch Language",
                            tint = Color(theme.keyTextColor),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Emoji quick key
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .clickable {
                                performFeedback()
                                activeSubView = KeyboardSubView.EMOJI
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Mood,
                            contentDescription = "Emoji",
                            tint = Color(theme.keyTextColor),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Spacebar with cursor control & SWIPE to switch language
                    var totalDragX by remember { mutableFloatStateOf(0f) }
                    var cursorStepAccumulator by remember { mutableFloatStateOf(0f) }

                    Box(
                        modifier = Modifier
                            .weight(4.2f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(theme.cornerRadius.dp))
                            .background(Color(theme.keyBackgroundColor))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        totalDragX = 0f
                                        cursorStepAccumulator = 0f
                                    },
                                    onDragEnd = {
                                        if (settings.swipeSpaceSwitchLanguage && kotlin.math.abs(totalDragX) > 80f) {
                                            performFeedback()
                                            currentLang = if (currentLang == KeyboardLanguage.ARABIC) KeyboardLanguage.ENGLISH else KeyboardLanguage.ARABIC
                                        }
                                        totalDragX = 0f
                                        cursorStepAccumulator = 0f
                                    },
                                    onDrag = { _, dragAmount ->
                                        totalDragX += dragAmount.x
                                        cursorStepAccumulator += dragAmount.x
                                        if (cursorStepAccumulator > 22f) {
                                            performFeedback()
                                            inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                                            inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
                                            cursorStepAccumulator = 0f
                                        } else if (cursorStepAccumulator < -22f) {
                                            performFeedback()
                                            inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                                            inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
                                            cursorStepAccumulator = 0f
                                        }
                                    }
                                )
                            }
                            .clickable {
                                performFeedback()
                                val now = System.currentTimeMillis()
                                if (settings.doubleSpacePeriod && now - lastSpacePressTime < 450) {
                                    // Replace preceding space with period and space
                                    if (currentComposingText.endsWith(" ")) {
                                        deleteChar()
                                        sendText(". ")
                                    } else {
                                        sendText(" ")
                                    }
                                    lastSpacePressTime = 0L
                                } else {
                                    sendText(" ")
                                    lastSpacePressTime = now
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = null,
                                tint = Color(theme.subtextColor).copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "العربية" else "English (US)",
                                color = Color(theme.keyTextColor).copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(theme.subtextColor).copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Enter Key with special 4-second Long Press Translation
                    // "الترجمة للنص بعد الضغط على زر انتر لمدة 4 ثواني"
                    var enterJob: Job? by remember { mutableStateOf(null) }

                    Box(
                        modifier = Modifier
                            .weight(1.6f)
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
                                            isEnterHolding = true
                                            enterHoldProgress = 0f
                                            enterJob = coroutineScope.launch {
                                                val startTime = System.currentTimeMillis()
                                                val totalDuration = 4000L
                                                while (isEnterHolding) {
                                                    val elapsed = System.currentTimeMillis() - startTime
                                                    enterHoldProgress = (elapsed / totalDuration.toFloat()).coerceIn(0f, 1f)
                                                    if (elapsed >= totalDuration) {
                                                        // 4 seconds completed!
                                                        performFeedback()
                                                        triggerInstantTranslate()
                                                        enterHoldProgress = 0f
                                                        isEnterHolding = false
                                                        break
                                                    }
                                                    delay(50)
                                                }
                                            }
                                        }
                                        val released = tryAwaitRelease()
                                        isEnterHolding = false
                                        enterJob?.cancel()
                                        enterHoldProgress = 0f
                                        if (released) {
                                            // Tapped normally
                                            sendEnter()
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                                contentDescription = "Enter / Translate (Hold 4s)",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val char = (key.type as? KeyType.Character)?.primary ?: ""
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val displayChar = if (isShifted) char.uppercase() else char
    val hint = if (theme.dualLanguageHints) {
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (hint != null) {
            Text(
                text = hint,
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
            color = Color(theme.keyTextColor),
            fontSize = if (hint != null) 17.sp else 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SpecialKeyButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(
                when {
                    isActive -> Color(theme.accentColor).copy(alpha = 0.35f)
                    isPressed -> Color(theme.keyPressedColor)
                    else -> Color(theme.keyBackgroundColor).copy(alpha = 0.85f)
                }
            )
            .border(
                width = 1.dp,
                color = if (isActive) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = theme.borderAlpha),
                shape = RoundedCornerShape(theme.cornerRadius.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) Color(theme.accentColor) else Color(theme.keyTextColor),
            modifier = Modifier.size(20.dp)
        )
    }
}
