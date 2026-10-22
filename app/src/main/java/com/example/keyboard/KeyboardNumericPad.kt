package com.example.keyboard

import android.view.inputmethod.EditorInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardTheme
import com.example.model.ThemePresets
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Dedicated, highly ergonomic Numeric and Phone Keypad layout (وضع إدخال الأرقام والهاتف).
 * Automatically triggered when input field is TYPE_CLASS_PHONE, TYPE_CLASS_NUMBER, or TYPE_CLASS_DATETIME.
 * Features:
 * - 4x4 Numpad with large, comfortable buttons matching user's selected Theme.
 * - Arabic/Indic digits toggle (1234 <-> ١٢٣٤) with 1 tap.
 * - Quick switch back to Full Alphabet QWERTY (ع / En / ABC).
 * - Continuous long-press Backspace deletion.
 * - Long-press 0 for international '+' country code.
 * - Action button with dynamic IME action (Done, Next, Search, Send, Return).
 * - Quick symbols row on top for phone/math symbols.
 * - Secondary extended symbols mode (@?! / 123).
 */
@Composable
fun KeyboardNumericPad(
    theme: KeyboardTheme,
    isPhoneField: Boolean,
    actionInfo: ImeActionInfo,
    rowHeight: Dp = 48.dp,
    currentLanguageName: String = "ع",
    isArabicDefault: Boolean = true,
    forceEnglishNumbers: Boolean = false,
    onSendText: (String) -> Unit,
    onDeleteChar: () -> Unit,
    onActionClick: () -> Unit,
    onSwitchToAlphabet: () -> Unit,
    onFeedback: (durationMs: Long, isSpecial: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var isArabicDigits by remember(forceEnglishNumbers) {
        mutableStateOf(!forceEnglishNumbers && isArabicDefault)
    }
    var isSymbolsSubMode by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var backspaceJob by remember { mutableStateOf<Job?>(null) }

    val keyBg = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    val keyPressedBg = ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    val actionBg = Color(theme.accentColor)
    val borderCol = Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    val textColor = Color(theme.keyTextColor)
    val subtextColor = Color(theme.subtextColor).copy(alpha = 0.85f)
    val cornerRadius = theme.cornerRadius.dp

    // Phone vs General Number top quick symbols
    val quickSymbols = remember(isPhoneField) {
        if (isPhoneField) {
            listOf("+", "-", "*", "#", "(", ")", "/", ".", "N", "P")
        } else {
            listOf(".", ",", "-", "+", "*", "/", "=", "%", "(", ")")
        }
    }

    // Digit mapping
    val enDigits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val arDigits = listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠")
    val lettersHints = listOf("", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ", "+")

    // Extended symbols layout for @?! submode
    val symGrid = listOf(
        listOf("+", "-", "*"),
        listOf("/", "=", "%"),
        listOf("(", ")", "$")
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Quick Symbols Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            quickSymbols.forEach { sym ->
                Surface(
                    modifier = Modifier
                        .height(28.dp)
                        .widthIn(min = 34.dp)
                        .clip(RoundedCornerShape(cornerRadius))
                        .clickable {
                            onFeedback(0L, false)
                            onSendText(sym)
                        },
                    shape = RoundedCornerShape(cornerRadius),
                    color = keyBg.copy(alpha = 0.85f),
                    border = BorderStroke(0.8.dp, borderCol)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                        Text(
                            text = sym,
                            color = textColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Row 1: [ 1 ] [ 2 ] [ 3 ] [ ع / En / ABC ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!isSymbolsSubMode) {
                // 1
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[0] else enDigits[0],
                    subText = lettersHints[0],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[0] else enDigits[0])
                    }
                )
                // 2
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[1] else enDigits[1],
                    subText = lettersHints[1],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[1] else enDigits[1])
                    }
                )
                // 3
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[2] else enDigits[2],
                    subText = lettersHints[2],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[2] else enDigits[2])
                    }
                )
            } else {
                symGrid[0].forEach { sym ->
                    NumericKey(
                        primaryText = sym,
                        subText = "",
                        theme = theme,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onFeedback(0L, false)
                            onSendText(sym)
                        }
                    )
                }
            }

            // Function Key: Switch to Full Letters Keyboard (ع / En / ABC)
            NumericFunctionKey(
                text = currentLanguageName,
                theme = theme,
                modifier = Modifier.weight(1f),
                onClick = {
                    onFeedback(25L, true)
                    onSwitchToAlphabet()
                }
            )
        }

        // Row 2: [ 4 ] [ 5 ] [ 6 ] [ ١٢٣٤ / 1234 ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!isSymbolsSubMode) {
                // 4
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[3] else enDigits[3],
                    subText = lettersHints[3],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[3] else enDigits[3])
                    }
                )
                // 5
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[4] else enDigits[4],
                    subText = lettersHints[4],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[4] else enDigits[4])
                    }
                )
                // 6
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[5] else enDigits[5],
                    subText = lettersHints[5],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[5] else enDigits[5])
                    }
                )
            } else {
                symGrid[1].forEach { sym ->
                    NumericKey(
                        primaryText = sym,
                        subText = "",
                        theme = theme,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onFeedback(0L, false)
                            onSendText(sym)
                        }
                    )
                }
            }

            // Function Key: Toggle Arabic vs English digits (١٢٣٤ / 1234)
            NumericFunctionKey(
                text = if (isArabicDigits) "1234" else "١٢٣٤",
                theme = theme,
                modifier = Modifier.weight(1f),
                onClick = {
                    onFeedback(25L, true)
                    isArabicDigits = !isArabicDigits
                }
            )
        }

        // Row 3: [ 7 ] [ 8 ] [ 9 ] [ ⌫ Backspace ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!isSymbolsSubMode) {
                // 7
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[6] else enDigits[6],
                    subText = lettersHints[6],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[6] else enDigits[6])
                    }
                )
                // 8
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[7] else enDigits[7],
                    subText = lettersHints[7],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[7] else enDigits[7])
                    }
                )
                // 9
                NumericKey(
                    primaryText = if (isArabicDigits) arDigits[8] else enDigits[8],
                    subText = lettersHints[8],
                    theme = theme,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onFeedback(0L, false)
                        onSendText(if (isArabicDigits) arDigits[8] else enDigits[8])
                    }
                )
            } else {
                symGrid[2].forEach { sym ->
                    NumericKey(
                        primaryText = sym,
                        subText = "",
                        theme = theme,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onFeedback(0L, false)
                            onSendText(sym)
                        }
                    )
                }
            }

            // Function Key: Backspace with hold-to-repeat
            var isBkPressed by remember { mutableStateOf(false) }
            val bkBg by animateColorAsState(
                targetValue = if (isBkPressed) keyPressedBg else keyBg,
                animationSpec = tween(durationMillis = if (isBkPressed) 20 else 90),
                label = "bk_bg"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(bkBg)
                    .border(1.dp, borderCol, RoundedCornerShape(cornerRadius))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isBkPressed = true
                                onFeedback(15L, false)
                                onDeleteChar()
                                val job = coroutineScope.launch {
                                    delay(400)
                                    while (isBkPressed) {
                                        onFeedback(8L, false)
                                        onDeleteChar()
                                        delay(55)
                                    }
                                }
                                backspaceJob = job
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    isBkPressed = false
                                    job.cancel()
                                    backspaceJob = null
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "حذف",
                    tint = textColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Row 4: [ , / + ] [ 0 (hold for +) ] [ @?! / 123 ] [ Action / Enter ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(rowHeight),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Col 1: Comma / Plus / Period
            val col1Text = if (isPhoneField) "+" else ","
            val col1Sub = if (isPhoneField) "," else "."
            NumericKey(
                primaryText = col1Text,
                subText = col1Sub,
                theme = theme,
                modifier = Modifier.weight(1f),
                onClick = {
                    onFeedback(0L, false)
                    onSendText(col1Text)
                },
                onLongClick = {
                    onFeedback(25L, true)
                    onSendText(col1Sub)
                }
            )

            // Col 2: Digit 0 with '+' hold shortcut
            NumericKey(
                primaryText = if (isArabicDigits) arDigits[9] else enDigits[9],
                subText = if (isPhoneField) "+" else "",
                theme = theme,
                modifier = Modifier.weight(1f),
                onClick = {
                    onFeedback(0L, false)
                    onSendText(if (isArabicDigits) arDigits[9] else enDigits[9])
                },
                onLongClick = if (isPhoneField) {
                    {
                        onFeedback(25L, true)
                        onSendText("+")
                    }
                } else null
            )

            // Col 3: Mode toggle between digits and symbols (@?! / 123)
            NumericFunctionKey(
                text = if (isSymbolsSubMode) "123" else "@?!",
                theme = theme,
                modifier = Modifier.weight(1f),
                onClick = {
                    onFeedback(25L, true)
                    isSymbolsSubMode = !isSymbolsSubMode
                }
            )

            // Col 4: Action button (Done / Next / Search / Send / Enter)
            var isActPressed by remember { mutableStateOf(false) }
            val actScale by animateFloatAsState(
                targetValue = if (isActPressed) 0.94f else 1.0f,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
                label = "act_scale"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .graphicsLayer { scaleX = actScale; scaleY = actScale }
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(actionBg)
                    .border(1.dp, actionBg.copy(alpha = 0.9f), RoundedCornerShape(cornerRadius))
                    .pointerInput(actionInfo) {
                        detectTapGestures(
                            onPress = {
                                isActPressed = true
                                onFeedback(25L, true)
                                tryAwaitRelease()
                                isActPressed = false
                            },
                            onTap = {
                                onActionClick()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionInfo.icon,
                    contentDescription = actionInfo.contentDescription,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
    }
}

@Composable
private fun NumericKey(
    primaryText: String,
    subText: String,
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val keyBg = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    val keyPressedBg = ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) keyPressedBg else keyBg,
        animationSpec = tween(durationMillis = if (isPressed) 20 else 90),
        label = "num_key_bg"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "num_key_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(animatedBg)
            .border(
                width = 1.dp,
                color = Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)),
                shape = RoundedCornerShape(theme.cornerRadius.dp)
            )
            .pointerInput(primaryText, onLongClick) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onLongPress = if (onLongClick != null) {
                        { onLongClick.invoke() }
                    } else null,
                    onTap = { onClick.invoke() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = primaryText,
                color = Color(theme.keyTextColor),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (subText.isNotBlank()) {
                Text(
                    text = subText,
                    color = Color(theme.subtextColor).copy(alpha = 0.8f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NumericFunctionKey(
    text: String,
    theme: KeyboardTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val keyBg = ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    val keyPressedBg = ThemePresets.resolveKeyColor(theme.keyPressedColor, theme.keyOpacity)
    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) keyPressedBg else keyBg.copy(alpha = 0.9f),
        animationSpec = tween(durationMillis = if (isPressed) 20 else 90),
        label = "func_key_bg"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 600f),
        label = "func_key_scale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(theme.cornerRadius.dp))
            .background(animatedBg)
            .border(
                width = 1.dp,
                color = Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f)),
                shape = RoundedCornerShape(theme.cornerRadius.dp)
            )
            .pointerInput(text) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick.invoke() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(theme.keyTextColor),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
