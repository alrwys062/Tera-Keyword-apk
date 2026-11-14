package com.example.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PredictionEngine
import com.example.model.KeyboardTheme
import com.example.util.AppStrings

private fun isLightColor(colorLong: Long): Boolean {
    val r = ((colorLong shr 16) and 0xFF) / 255.0
    val g = ((colorLong shr 8) and 0xFF) / 255.0
    val b = (colorLong and 0xFF) / 255.0
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance > 0.5
}

@Composable
fun KeyboardSuggestionsBar(
    theme: KeyboardTheme,
    suggestions: List<PredictionEngine.PredictionResult>,
    onSuggestionClick: (String) -> Unit,
    onAddWordToDictionary: ((String) -> Unit)? = null,
    onSwipeBack: (() -> Unit)? = null,
    detectedOtpCode: String? = null,
    onOtpClick: ((String) -> Unit)? = null,
    browserShortcuts: List<String> = emptyList(),
    onBrowserShortcutClick: ((String) -> Unit)? = null,
    appLanguageCode: String = "auto",
    typingLangCode: String = "ar"
) {
    val context = LocalContext.current
    val effectiveSuggestions = if (suggestions.isNotEmpty()) {
        suggestions
    } else {
        PredictionEngine.getDefaultSuggestionsForLang(
            isArabic = typingLangCode == "ar",
            langCode = typingLangCode
        )
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val barHeight = if (isLandscape) 23.dp else 29.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(barHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 6.dp, vertical = if (isLandscape) 1.dp else 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (!detectedOtpCode.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 1.dp)
                    .clickable { onOtpClick?.invoke(detectedOtpCode) },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF059669),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF10B981))
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = AppStrings.trfCode(context, appLanguageCode, "otp_paste_code", detectedOtpCode),
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = AppStrings.trfCode(context, appLanguageCode, "otp_paste_code", detectedOtpCode),
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = androidx.compose.ui.text.TextStyle(
                                platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
            }
        }

        val left = effectiveSuggestions.getOrNull(0)
        val center = effectiveSuggestions.getOrNull(1) ?: left
        val right = effectiveSuggestions.getOrNull(2)

        if (left != null) {
            SuggestionItem(
                result = left,
                theme = theme,
                isCenter = false,
                appLanguageCode = appLanguageCode,
                modifier = Modifier.weight(1f),
                onClick = { onSuggestionClick(left.word) },
                onAddWord = onAddWordToDictionary
            )
        }

        if (center != null) {
            SuggestionItem(
                result = center,
                theme = theme,
                isCenter = true,
                appLanguageCode = appLanguageCode,
                modifier = Modifier.weight(1.3f),
                onClick = { onSuggestionClick(center.word) },
                onAddWord = onAddWordToDictionary
            )
        }

        if (right != null) {
            SuggestionItem(
                result = right,
                theme = theme,
                isCenter = false,
                appLanguageCode = appLanguageCode,
                modifier = Modifier.weight(1f),
                onClick = { onSuggestionClick(right.word) },
                onAddWord = onAddWordToDictionary
            )
        }
    }
}

@Composable
private fun SuggestionItem(
    result: PredictionEngine.PredictionResult,
    theme: KeyboardTheme,
    isCenter: Boolean,
    appLanguageCode: String = "auto",
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onAddWord: ((String) -> Unit)?
) {
    val context = LocalContext.current
    val isBgLight = androidx.compose.runtime.remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val textColor = androidx.compose.runtime.remember(theme.backgroundColor, isBgLight) {
        if (isBgLight) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    }

    val itemShape = RoundedCornerShape(8.dp)

    if (result.isCustomCandidate && onAddWord != null) {
        // Quick add to dictionary button - matching identical height and shape
        androidx.compose.material3.Surface(
            modifier = modifier
                .fillMaxHeight()
                .clickable { onAddWord(result.word) },
            shape = itemShape,
            color = Color(0xFF10B981).copy(alpha = 0.16f),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.8f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = AppStrings.trfCode(context, appLanguageCode, "save_word_chip", result.word),
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = AppStrings.trfCode(context, appLanguageCode, "save_word_chip", result.word),
                        color = Color(0xFF10B981),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        val activeBg = if (isCenter) {
            Color(theme.accentColor).copy(alpha = 0.22f)
        } else {
            Color(theme.keyBackgroundColor)
        }
        val activeBorder = if (isCenter) {
            androidx.compose.foundation.BorderStroke(1.2.dp, Color(theme.accentColor))
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.4f)))
        }

        androidx.compose.material3.Surface(
            modifier = modifier
                .fillMaxHeight()
                .clickable(onClick = onClick),
            shape = itemShape,
            color = activeBg,
            border = activeBorder
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                Text(
                    text = result.word,
                    color = if (isCenter) Color(theme.accentColor) else textColor,
                    fontSize = if (isCenter) (if (isLandscape) 12.sp else 13.5.sp) else (if (isLandscape) 11.sp else 12.5.sp),
                    fontWeight = if (isCenter) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = androidx.compose.ui.text.TextStyle(
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }
        }
    }
}
