package com.example.keyboard

import com.example.util.AppStrings
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.example.data.EmojiData
import com.example.data.PreferencesManager
import com.example.data.TextDecorator
import com.example.data.TranslationEngine
import com.example.model.ClipboardItem
import com.example.model.KeyboardSettings
import com.example.model.KeyboardTheme
import com.example.model.ThemePresets
import com.example.sound.KeyboardSoundEngine
import com.example.util.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun isLightColor(colorLong: Long): Boolean {
    val r = ((colorLong shr 16) and 0xFF) / 255.0
    val g = ((colorLong shr 8) and 0xFF) / 255.0
    val b = (colorLong and 0xFF) / 255.0
    val luminance = 0.299 * r + 0.587 * g + 0.114 * b
    return luminance > 0.5
}

// -------------------------------------------------------------
// 1. INLINE TRANSLATION BAR (Matching Screenshot 20)
// -------------------------------------------------------------
@Composable
fun InlineTranslationBar(
    theme: KeyboardTheme,
    sourceLang: String,
    targetLang: String,
    appLanguageCode: String = "auto",
    onSwapLanguages: () -> Unit,
    onOpenSourcePicker: () -> Unit,
    onOpenTargetPicker: () -> Unit,
    onClose: () -> Unit,
    onTranslateNow: (() -> Unit)? = null,
    isTranslating: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    }
    val isAr = resolvedUiLang == "ar"
    val allLangs = com.example.data.TranslationEngine.supportedLanguages
    val sourceItem = allLangs.find { it.code == sourceLang } ?: allLangs[0]
    val targetItem = allLangs.find { it.code == targetLang } ?: allLangs[1]

    val sourceLabel = "${sourceItem.flag} ${if (isAr) sourceItem.nameAr else sourceItem.nameEn}"
    val targetLabel = "${targetItem.flag} ${if (isAr) targetItem.nameAr else targetItem.nameEn}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(31.dp)
            .background(Color(theme.toolbarColor))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Close button (Compact harmonious circular X)
        Box(
            modifier = Modifier
                .size(25.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_001"),
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        // Language selectors and Swap: [Source ▼] ⇄ [Target ▼]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Source Language Selector
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier.clickable { onOpenSourcePicker() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sourceLabel, color = Color(theme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(theme.keyTextColor).copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                }
            }

            // Swap icon ⇄ (Mini compact circular button)
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(theme.keyBackgroundColor))
                    .clickable(onClick = onSwapLanguages),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_002"),
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(11.dp)
                )
            }

            // Target Language Selector
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier.clickable { onOpenTargetPicker() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(targetLabel, color = Color(theme.keyTextColor), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(theme.keyTextColor).copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                }
            }
        }

        // Interactive Translate Button
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(theme.accentColor).copy(alpha = 0.22f),
            border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.85f)),
            modifier = Modifier.clickable { onTranslateNow?.invoke() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(11.dp),
                        color = Color(theme.accentColor),
                        strokeWidth = 1.5.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_003"),
                        color = Color(theme.accentColor),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_004"),
                        tint = Color(theme.accentColor),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_004"),
                        color = Color(theme.accentColor),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1b. LANGUAGE PICKER DRAWER (All 100+ World Languages)
// -------------------------------------------------------------
@Composable
fun LanguagePickerDrawer(
    theme: KeyboardTheme,
    isSource: Boolean,
    selectedCode: String,
    keyboardHeight: Dp = 220.dp,
    appLanguageCode: String = "auto",
    onSelectLanguage: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    }
    val isAr = resolvedUiLang == "ar"
    val allLangs = com.example.data.TranslationEngine.supportedLanguages
    var query by remember { mutableStateOf("") }

    val filteredLangs = remember(query) {
        if (query.isBlank()) {
            allLangs
        } else {
            val q = query.trim().lowercase()
            allLangs.filter {
                it.nameAr.contains(q, ignoreCase = true) ||
                it.nameEn.contains(q, ignoreCase = true) ||
                it.code.contains(q, ignoreCase = true)
            }
        }
    }

    val popularCodes = listOf("ar", "en", "fr", "es", "tr", "de", "ru", "fa", "ur", "it", "zh-CN", "ja", "ko", "hi", "id")
    val popularLangs = remember {
        popularCodes.mapNotNull { code -> allLangs.find { it.code == code } }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Header: Title & Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color(theme.keyBackgroundColor))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_005"),
                            tint = Color(theme.keyTextColor),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSource) AppStrings.trCode(context, appLanguageCode, "ui_translation_screen_014")
                               else AppStrings.trCode(context, appLanguageCode, "ui_translation_screen_015"),
                        color = Color(theme.keyTextColor),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(theme.accentColor).copy(alpha = 0.2f),
                    border = BorderStroke(0.8.dp, Color(theme.accentColor).copy(alpha = 0.6f))
                ) {
                    Text(
                        text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_006", allLangs.size),
                        color = Color(theme.accentColor),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Search Bar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_themes_screen_001"),
                        tint = Color(theme.subtextColor),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color(theme.keyTextColor),
                            fontSize = 12.sp
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { innerTextField ->
                            if (query.isEmpty()) {
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_007"),
                                    color = Color(theme.subtextColor).copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_input_languages_screen_007"),
                                tint = Color(theme.subtextColor),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Popular Language Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(popularLangs) { lang ->
                    val isSelected = lang.code == selectedCode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.35f) else Color(theme.keyBackgroundColor),
                        border = BorderStroke(
                            width = if (isSelected) 1.2.dp else 0.8.dp,
                            color = if (isSelected) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.clickable {
                            onSelectLanguage(lang.code)
                            onClose()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang.flag, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAr) lang.nameAr else lang.nameEn,
                                color = if (isSelected) Color.White else Color(theme.keyTextColor),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 100+ All Languages List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(filteredLangs, key = { it.code }) { lang ->
                    val isSelected = lang.code == selectedCode
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor).copy(alpha = 0.7f),
                        border = BorderStroke(
                            width = if (isSelected) 1.2.dp else 0.6.dp,
                            color = if (isSelected) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectLanguage(lang.code)
                                onClose()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flag, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (isAr) lang.nameAr else lang.nameEn,
                                        color = Color(theme.keyTextColor),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isAr) lang.nameEn else lang.nameAr,
                                        color = Color(theme.subtextColor),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(theme.backgroundColor).copy(alpha = 0.8f),
                                    border = BorderStroke(0.5.dp, Color(theme.borderColor).copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = lang.code.uppercase(),
                                        color = Color(theme.subtextColor),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_008"),
                                        tint = Color(theme.accentColor),
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

// -------------------------------------------------------------
// 2. INLINE TEXT DECORATION BAR & POPUP (Matching Screenshots 18 & 19)
// -------------------------------------------------------------
@Composable
fun InlineDecorationBar(
    theme: KeyboardTheme,
    activeStyleId: String,
    appLanguageCode: String = "auto",
    keyboardHeight: Dp = 200.dp,
    onSelectStyle: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    }
    val isAr = resolvedUiLang == "ar"
    var showStylesMenu by remember { mutableStateOf(false) }

    val activeItem = TextDecorator.allStyles.find { it.id == activeStyleId } ?: TextDecorator.allStyles[0]

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(33.dp)
                .background(Color(theme.toolbarColor))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close red X button (Compact & sleek)
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_009"),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Text Style button (Screenshot 19: "Text Style ▼")
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.accentColor).copy(alpha = 0.5f)),
                modifier = Modifier.clickable { showStylesMenu = !showStylesMenu }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeItem.preview,
                        color = Color(theme.keyTextColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showStylesMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(theme.accentColor),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_010"),
                color = Color(theme.accentColor),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Floating / Expandable styles list matching Screenshot 18
        AnimatedVisibility(visible = showStylesMenu) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((keyboardHeight * 0.85f).coerceIn(155.dp, 210.dp)),
                color = Color(0xFF181F2C),
                border = BorderStroke(1.dp, Color(0xFF29374F))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(TextDecorator.allStyles) { style ->
                        val isSelected = style.id == activeStyleId
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.2f) else Color(0xFF1F293D),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(theme.accentColor) else Color(0xFF2E3D59)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectStyle(style.id)
                                    showStylesMenu = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = style.preview,
                                    color = if (isSelected) Color(theme.accentColor) else Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isAr) style.nameAr else style.nameEn,
                                    color = Color(0xFF8E9BAE),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. CLIPBOARD DRAWER (Matching Screenshot 21)
// - Stores forever
// - 2-Column Grid
// - Auto closes on paste ("واغلاق الحافظة عند اللصق")
// - Preserves scroll position ("تكون بنفس المكان الي وصلت ابيه")
// -------------------------------------------------------------
@Composable
fun ClipboardDrawer(
    theme: KeyboardTheme,
    prefs: PreferencesManager,
    keyboardHeight: Dp = 220.dp,
    appLanguageCode: String = "auto",
    onItemInserted: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    }
    val isAr = resolvedUiLang == "ar"
    var items by remember { mutableStateOf(prefs.getClipboardItems(forceRefresh = true)) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedClipForMenu by remember { mutableStateOf<ClipboardItem?>(null) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var isSingleColumnMode by remember { mutableStateOf(false) }
    var previewFullTextItem by remember { mutableStateOf<ClipboardItem?>(null) }

    LaunchedEffect(Unit) {
        if (prefs.syncWithSystemClipboard(context)) {
            items = prefs.getClipboardItems(forceRefresh = true)
        } else {
            items = prefs.getClipboardItems(forceRefresh = true)
        }
        kotlinx.coroutines.delay(120)
        if (prefs.syncWithSystemClipboard(context)) {
            items = prefs.getClipboardItems(forceRefresh = true)
        }
        kotlinx.coroutines.delay(280)
        if (prefs.syncWithSystemClipboard(context)) {
            items = prefs.getClipboardItems(forceRefresh = true)
        }
    }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.text.contains(searchQuery.trim(), ignoreCase = true) }
    }

    // Remember the user's exact scroll position when opening clipboard!
    val initialIndex = remember { prefs.getLastClipboardScrollIndex().coerceIn(0, maxOf(0, items.size - 1)) }
    val initialOffset = remember { prefs.getLastClipboardScrollOffset() }
    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = initialIndex,
        initialFirstVisibleItemScrollOffset = initialOffset
    )

    // Save scroll position when user stops scrolling to eliminate frame drops and disk I/O lag
    LaunchedEffect(gridState.isScrollInProgress) {
        if (!gridState.isScrollInProgress) {
            prefs.setLastClipboardScrollIndex(gridState.firstVisibleItemIndex)
            prefs.setLastClipboardScrollOffset(gridState.firstVisibleItemScrollOffset)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            // Top Toolbar of Clipboard:
            // [🔄 إصلاح ومزامنة] [☷/☰] [🗑️ Clear] [📋 Paste] [✂️ Cut] [❌ Close]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(theme.toolbarColor), RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (showClearConfirmation) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_011"),
                            color = Color(theme.keyTextColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEF4444),
                                modifier = Modifier.clickable {
                                    prefs.clearClipboardHistory()
                                    items = prefs.getClipboardItems(forceRefresh = true)
                                    showClearConfirmation = false
                                }
                            ) {
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_012"),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(theme.keyPressedColor),
                                modifier = Modifier.clickable { showClearConfirmation = false }
                            ) {
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_020"),
                                    color = Color(theme.keyTextColor),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // 🔄 Dedicated "Sync & Repair" Button
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(theme.accentColor).copy(alpha = 0.25f),
                            border = BorderStroke(0.8.dp, Color(theme.accentColor)),
                            modifier = Modifier.clickable {
                                val count = prefs.forceSyncAndRepairClipboard(context)
                                items = prefs.getClipboardItems(forceRefresh = true)
                                Toast.makeText(context, AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_013", count), Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_014"), tint = Color(theme.accentColor), modifier = Modifier.size(13.dp))
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_014"),
                                    color = Color(theme.accentColor),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Toggle Grid / Single Wide Column for longer texts
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSingleColumnMode) Color(theme.accentColor).copy(alpha = 0.3f) else Color(theme.keyPressedColor).copy(alpha = 0.5f))
                                .clickable { isSingleColumnMode = !isSingleColumnMode },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSingleColumnMode) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_015"),
                                tint = if (isSingleColumnMode) Color(theme.accentColor) else Color(theme.keyTextColor).copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Delete all non-pinned with protection against accidental erase
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .clickable {
                                    showClearConfirmation = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_016"), tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                        }

                        // Paste recent
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(theme.accentColor).copy(alpha = 0.15f))
                                .clickable {
                                    if (items.isNotEmpty()) {
                                        onItemInserted(items[0].text)
                                        if (prefs.getSettings().clipboardCloseOnPaste) {
                                            onClose()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.ContentPaste, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_017"), tint = Color(theme.accentColor), modifier = Modifier.size(12.dp))
                        }

                        // Cut icon
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(theme.keyPressedColor).copy(alpha = 0.5f))
                                .clickable {
                                    if (items.isNotEmpty()) {
                                        val first = items[0]
                                        onItemInserted(first.text)
                                        prefs.deleteClipboardItem(first.id, first.text)
                                        items = prefs.getClipboardItems(forceRefresh = true)
                                        if (prefs.getSettings().clipboardCloseOnPaste) {
                                            onClose()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.ContentCut, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_018"), tint = Color(theme.keyTextColor).copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
                        }
                    }

                    // Close Button ❌
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                            .clickable(onClick = onClose),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_020"),
                            color = Color(theme.subtextColor),
                            fontSize = 12.sp
                        )
                        OutlinedButton(
                            onClick = {
                                val count = prefs.forceSyncAndRepairClipboard(context)
                                items = prefs.getClipboardItems(forceRefresh = true)
                                Toast.makeText(context, AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_021", count), Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(theme.accentColor)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_022"), color = Color(theme.accentColor), fontSize = 11.sp)
                        }
                    }
                }
            } else {
                // Adaptive Grid: 1 column (wide expanded preview) or 2 columns
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isSingleColumnMode) 1 else 2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredItems, key = { it.id }) { clip ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(
                                1.dp,
                                if (clip.isPinned) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = theme.borderAlpha)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isSingleColumnMode) 96.dp else 84.dp)
                                .pointerInput(clip.id) {
                                    detectTapGestures(
                                        onLongPress = {
                                            selectedClipForMenu = clip
                                        },
                                        onTap = {
                                            // Single tap pastes directly
                                            onItemInserted(clip.text)
                                            if (prefs.getSettings().clipboardCloseOnPaste) {
                                                onClose()
                                            }
                                        }
                                    )
                                }
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        text = clip.text,
                                        color = Color(theme.keyTextColor),
                                        fontSize = 11.5.sp,
                                        maxLines = if (isSingleColumnMode) 8 else 5,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 15.5.sp,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                // Icons bottom: Preview eye for large text, and Pin
                                Row(
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (clip.text.length > 60 || clip.text.contains("\n")) {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(theme.keyPressedColor).copy(alpha = 0.7f))
                                                .clickable { previewFullTextItem = clip },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_023"),
                                                tint = Color(theme.accentColor),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }

                                    if (clip.isPinned) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_024"),
                                            tint = Color(theme.accentColor),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Context Menu on Long-Press
        if (selectedClipForMenu != null) {
            val clip = selectedClipForMenu!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { selectedClipForMenu = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(theme.backgroundColor),
                    border = BorderStroke(1.2.dp, Color(theme.accentColor)),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = clip.text,
                            color = Color(theme.keyTextColor),
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )

                        HorizontalDivider(
                            color = Color(theme.borderColor).copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        prefs.deleteClipboardItem(clip.id, clip.text)
                                        items = prefs.getClipboardItems(forceRefresh = true)
                                        selectedClipForMenu = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_015"),
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_015"),
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        prefs.togglePinClipboard(clip.id)
                                        items = prefs.getClipboardItems(forceRefresh = true)
                                        selectedClipForMenu = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (clip.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_017"),
                                    tint = Color(theme.accentColor),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (clip.isPinned) {
                                        if (isAr) "إلغاء التثبيت" else "Unpin"
                                    } else {
                                        AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_017")
                                    },
                                    color = Color(theme.accentColor),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        cm?.setPrimaryClip(ClipData.newPlainText("text", clip.text))
                                        selectedClipForMenu = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_018"),
                                    tint = Color(theme.keyTextColor),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_018"),
                                    color = Color(theme.keyTextColor),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onItemInserted(clip.text)
                                        selectedClipForMenu = null
                                        if (prefs.getSettings().clipboardCloseOnPaste) {
                                            onClose()
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.ContentPaste,
                                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_127"),
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_127"),
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        previewFullTextItem = clip
                                        selectedClipForMenu = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_025"),
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_025"),
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full Text Viewer Modal for Large/Long Texts
        if (previewFullTextItem != null) {
            val clip = previewFullTextItem!!
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { previewFullTextItem = null },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(theme.backgroundColor),
                    border = BorderStroke(1.2.dp, Color(theme.accentColor)),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.92f)
                        .clickable(enabled = false) {}
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_026", clip.text.length),
                                color = Color(theme.accentColor),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { previewFullTextItem = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        HorizontalDivider(
                            color = Color(theme.borderColor).copy(alpha = 0.4f),
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color(theme.keyBackgroundColor), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = clip.text,
                                color = Color(theme.keyTextColor),
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(ClipData.newPlainText("text", clip.text))
                                    Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_008"), Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(theme.accentColor))
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, tint = Color(theme.accentColor), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_018"), color = Color(theme.accentColor), fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    onItemInserted(clip.text)
                                    previewFullTextItem = null
                                    if (prefs.getSettings().clipboardCloseOnPaste) {
                                        onClose()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Outlined.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_027"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. EMOJI PICKER VIEW (مع قسم الإيموجي المستعملة 🕒 وإيموجي آيفون وأندرويد)
// -------------------------------------------------------------
@Composable
fun EmojiPickerView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 220.dp,
    hideAbcKey: Boolean = false,
    backspaceScale: Float = 1.0f,
    searchQuery: String = "",
    appLanguageCode: String = "auto",
    onSearchQueryChange: (String) -> Unit = {},
    isCustomizeMode: Boolean = false,
    onEmojiSelected: (String) -> Unit,
    onEmojiLongPress: ((String) -> Unit)? = null,
    onSpacePressed: () -> Unit = {},
    onDeletePressed: () -> Unit = {},
    onEnterPressed: () -> Unit = {},
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    }
    val isAr = resolvedUiLang == "ar"
    val recentEmojiManager = remember { com.example.data.RecentEmojiManager(context) }
    var recentEmojisList by remember { mutableStateOf(recentEmojiManager.getRecentEmojis()) }

    // -1 = Recent Emojis 🕒, 0..N = Standard Categories
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }
    var isIosStyle by remember { mutableStateOf(true) }

    val preferencesManager = remember { com.example.data.PreferencesManager(context) }
    var emojiSizeScale by remember { mutableFloatStateOf(preferencesManager.getEmojiSizeScale()) }

    val currentEmojis = remember(selectedCategoryIndex, searchQuery, recentEmojisList) {
        if (searchQuery.isNotBlank()) {
            (recentEmojisList + EmojiData.categories.flatMap { it.emojis })
                .distinct()
                .filter { it.contains(searchQuery.trim()) }
        } else if (selectedCategoryIndex == -1) {
            recentEmojisList
        } else {
            EmojiData.categories.getOrNull(selectedCategoryIndex)?.emojis ?: emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Top search & style toggle & size control & close row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean non-clipping Search Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(theme.keyBackgroundColor))
                    .border(1.dp, Color(theme.borderColor).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_themes_screen_001"),
                        tint = Color(theme.subtextColor),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_028"),
                                color = Color(theme.subtextColor).copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(theme.keyTextColor),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(theme.accentColor)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_input_languages_screen_007"),
                            tint = Color(theme.subtextColor),
                            modifier = Modifier
                                .size(15.dp)
                                .clickable { onSearchQueryChange("") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Emoji Size Controls (التحكم في حجم الفيسات والإيموجي: A- / الحجم / A+)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.7f)),
                    modifier = Modifier.clickable {
                        val newScale = (emojiSizeScale - 0.2f).coerceAtLeast(0.75f)
                        emojiSizeScale = newScale
                        preferencesManager.saveEmojiSizeScale(newScale)
                    }
                ) {
                    Text(text = "A-", color = Color(theme.accentColor), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.7f)),
                    modifier = Modifier.clickable {
                        val nextScale = when {
                            emojiSizeScale < 0.9f -> 1.0f
                            emojiSizeScale < 1.15f -> 1.3f
                            emojiSizeScale < 1.4f -> 1.55f
                            else -> 0.8f
                        }
                        emojiSizeScale = nextScale
                        preferencesManager.saveEmojiSizeScale(nextScale)
                    }
                ) {
                    Text(
                        text = when {
                            emojiSizeScale >= 1.45f -> AppStrings.trCode(context, appLanguageCode, "key_height_preset_xl")
                            emojiSizeScale >= 1.25f -> if (isAr) "كبير" else "Large"
                            emojiSizeScale <= 0.85f -> AppStrings.trCode(context, appLanguageCode, "key_height_preset_compact")
                            else -> AppStrings.trCode(context, appLanguageCode, "key_height_preset_normal")
                        },
                        color = Color(theme.accentColor),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.7f)),
                    modifier = Modifier.clickable {
                        val newScale = (emojiSizeScale + 0.2f).coerceAtMost(1.6f)
                        emojiSizeScale = newScale
                        preferencesManager.saveEmojiSizeScale(newScale)
                    }
                ) {
                    Text(text = "A+", color = Color(theme.accentColor), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // iOS / Android emoji style toggle
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isIosStyle) Color(0xFF0A84FF).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, if (isIosStyle) Color(0xFF0A84FF) else Color(theme.borderColor)),
                modifier = Modifier.clickable { isIosStyle = !isIosStyle }
            ) {
                Text(
                    text = if (isIosStyle) {
                        if (isAr) "🍏 آيفون" else "🍏 iOS"
                    } else {
                        if (isAr) "🤖 أندرويد" else "🤖 Android"
                    },
                    color = if (isIosStyle) Color(0xFF0A84FF) else Color(theme.keyTextColor),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Emoji picker close button
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"),
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Categories Tab: 🕒 Recent first, then normal categories
        if (isCustomizeMode) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0084FF).copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color(0xFF0084FF).copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        tint = Color(0xFF0084FF),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_029"),
                        color = Color(0xFF0084FF),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recent Emojis 🕒 Tab
            item {
                val isRecentSelected = selectedCategoryIndex == -1
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isRecentSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                    border = BorderStroke(
                        1.dp,
                        if (isRecentSelected) Color(theme.accentColor) else Color(theme.borderColor)
                    ),
                    modifier = Modifier.clickable { selectedCategoryIndex = -1 }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🕒", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_030"),
                            color = if (isRecentSelected) Color(theme.accentColor) else Color(theme.keyTextColor),
                            fontSize = 9.5.sp,
                            fontWeight = if (isRecentSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            items(EmojiData.categories.indices.toList()) { index ->
                val cat = EmojiData.categories[index]
                val isSelected = index == selectedCategoryIndex
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(theme.accentColor) else Color(theme.borderColor)
                    ),
                    modifier = Modifier.clickable { selectedCategoryIndex = index }
                ) {
                    Text(
                        text = cat.icon,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val availableWidth = maxWidth
            val gridColumns = 8
            val itemWidth = (availableWidth - (3.dp * (gridColumns - 1))) / gridColumns
            val emojiFontSize = (itemWidth.value * 0.55f * emojiSizeScale).coerceIn(16f, 26f).sp

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(currentEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .height(itemWidth.coerceIn(32.dp, 42.dp))
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .pointerInput(emoji) {
                                detectTapGestures(
                                    onLongPress = {
                                        onEmojiLongPress?.invoke(emoji)
                                    },
                                    onTap = {
                                        recentEmojiManager.addEmoji(emoji)
                                        recentEmojisList = recentEmojiManager.getRecentEmojis()
                                        onEmojiSelected(emoji)
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = emojiFontSize)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom Navigation Bar (Return to letters, Space, Delete, Send)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(31.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Return to Alphabet Keyboard button (⌨️ ABC) - toggleable via hideAbcKey
            if (!hideAbcKey) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
                        .clickable(onClick = onClose)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_031"),
                            color = Color(theme.accentColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Spacebar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier
                    .weight(if (hideAbcKey) 3.5f else 2.7f)
                    .fillMaxHeight()
                    .clickable { onSpacePressed() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_032"), color = Color(theme.keyTextColor), fontSize = 11.sp)
                }
            }

            // 3. Scalable Delete button
            val bWeight = (1.2f * backspaceScale).coerceIn(1.0f, 2.2f)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier
                    .weight(bWeight)
                    .fillMaxHeight()
                    .clickable { onDeletePressed() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_012"),
                        tint = Color(theme.keyTextColor),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 4. Send / Enter button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.enterButtonColor),
                border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.8f)),
                modifier = Modifier
                    .weight(1.6f)
                    .fillMaxHeight()
                    .clickable {
                        onEnterPressed()
                        onClose()
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_033"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_chat_simulator_screen_012"),
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. GIF PICKER VIEW
// -------------------------------------------------------------
@Composable
fun GifPickerView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 220.dp,
    appLanguageCode: String = "auto",
    onGifSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val quickGifs = listOf(
        "(^_^)/", "(o_O)", "(•‿•)", "(╯°□°)╯", "¯\\_(ツ)_/¯",
        "(づ￣ ³￣)づ", "(♥_♥)", "(T_T)", "(¬_¬)", "ᕙ(⇀‸↼‶)ᕗ",
        "₍ᐢ•ﻌ•ᐢ₎", "(⁠◍⁠•⁠ᴗ⁠•⁠◍⁠)", "(⁠｡⁠♡⁠‿⁠♡⁠｡⁠)", "ʕ•ᴥ•ʔ", "(>_<)"
    )

    val isLight = remember(theme.backgroundColor, theme.toolbarColor) { isLightColor(theme.backgroundColor) || isLightColor(theme.toolbarColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_034"), color = textColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickGifs) { gif ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable { onGifSelected(gif) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(gif, color = Color(theme.keyTextColor), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. MEDIA & PHOTOS PICKER VIEW
// -------------------------------------------------------------
@Composable
fun MediaPickerView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 220.dp,
    appLanguageCode: String = "auto",
    onMediaSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    }
    val isAr = resolvedUiLang == "ar"
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = remember(isAr) {
        if (isAr) listOf("🖼️ الملصقات والصور", "🎬 GIFs متحركة", "🎭 فيسات Kaomoji", "🎨 نصوص فنية")
        else listOf("🖼️ Stickers & Photos", "🎬 Animated GIFs", "🎭 Kaomoji", "🎨 Text Art")
    }

    val stickers = listOf(
        "صباح الخير 🌸", "مساء الورد 🌹", "جمعة مباركة ✨", "ألف مبروك 🎉",
        "شكراً جزيلاً 🙏", "تحياتي يا بطل 👑", "ما شاء الله 💫", "بالتوفيق والنجاح 🌟",
        "تسلم إيدك 👍", "هههههههههه 😂", "أهلاً وسهلاً 👋", "نورتنا يا غالي 💖",
        "الله يسعدك 🤍", "كل عام وأنتم بخير 🌙", "عيدكم مبارك 🎈", "الحمد لله دائماً 🤲"
    )

    val gifMemes = listOf(
        "🤣 [GIF: LOL]", "👏 [GIF: Applause]", "❤️ [GIF: Hearts]",
        "🔥 [GIF: Fire]", "😲 [GIF: Shocked]", "👋 [GIF: Hello]",
        "😎 [GIF: Cool]", "🕺 [GIF: Dance]", "💤 [GIF: Sleepy]",
        "🥳 [GIF: Party]", "💪 [GIF: Strong]", "🎯 [GIF: Bullseye]"
    )

    val kaomojis = listOf(
        "(^_^)/", "(o_O)", "(•‿•)", "(╯°□°)╯", "¯\\_(ツ)_/¯",
        "(づ￣ ³￣)づ", "(♥_♥)", "(T_T)", "(¬_¬)", "ᕙ(⇀‸↼‶)ᕗ",
        "₍ᐢ•ﻌ•ᐢ₎", "(⁠◍⁠•⁠ᴗ⁠•⁠◍⁠)", "(⁠｡⁠♡⁠‿⁠♡⁠｡⁠)", "ʕ•ᴥ•ʔ", "(>_<)",
        "✧⁠◝⁠(⁠⁰⁠▿⁠⁰⁠)⁠◜⁠✧", "ヘ⁠(⁠￣⁠ω⁠￣⁠ヘ⁠)", "ƪ⁠(⁠˘⁠⌣⁠˘⁠)⁠ʃ", "ᕙ⁠(⁠ ⁠•⁠ ⁠‿⁠ ⁠•⁠ ⁠)⁠ᕗ"
    )

    val decorativeArt = listOf(
        "﷽",
        "۝ إِنَّ اللَّهَ مَعَ الصَّابِرِينَ ۝",
        "★彡 ( 𝒯𝓊𝓇𝒷ℴ 𝒦ℯ𝓎𝒷ℴ𝒶𝓇𝒹 ) 彡★",
        "━━━━━ 🌟 ━━━━━",
        "꧁༺ صباح السعادة ༻꧂",
        "•°¯`•• ( تحياتي وتقديري ) ••´¯°•",
        "««—(¯`v´¯)—»»",
        "░▒▓█ شكراً لك █▓▒░"
    )

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val isKeyLight = remember(theme.keyBackgroundColor) { isLightColor(theme.keyBackgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardTextColor = if (isKeyLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subTextColor = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_035"),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(tabs.indices.toList()) { index ->
                val isSelected = selectedTab == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, if (isSelected) Color(theme.accentColor) else Color(theme.borderColor)),
                    modifier = Modifier.clickable { selectedTab = index }
                ) {
                    Text(
                        text = tabs[index],
                        color = if (isSelected) Color(theme.accentColor) else (if (isKeyLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content Grid
        when (selectedTab) {
            0 -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(stickers) { sticker ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(Color(theme.keyBackgroundColor), Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                                    )
                                )
                                .border(
                                    1.dp,
                                    Color(theme.accentColor).copy(alpha = 0.45f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onMediaSelected(sticker)
                                    onClose()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sticker,
                                color = cardTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            1 -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(gifMemes) { meme ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(1.dp, Color(theme.borderColor)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable {
                                    onMediaSelected(meme)
                                    onClose()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                                Text(
                                    text = meme,
                                    color = Color(theme.accentColor),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(kaomojis) { kaomoji ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(1.dp, Color(theme.borderColor)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable {
                                    onMediaSelected(kaomoji)
                                    onClose()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = kaomoji,
                                    color = cardTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(decorativeArt) { art ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(1.dp, Color(theme.borderColor)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clickable {
                                    onMediaSelected(art)
                                    onClose()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = art,
                                    color = cardTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. AI KEYBOARD DRAWER (تدقيق، إعادة صياغة، ترجمة، كتابة ذكية بالذكاء الاصطناعي)
// -------------------------------------------------------------
@Composable
fun AiToneDrawer(
    theme: KeyboardTheme,
    currentText: String,
    keyboardHeight: Dp = 225.dp,
    appLanguageCode: String = "auto",
    onReplaceText: (String) -> Unit,
    onAddWordToDictionary: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    }
    val isAr = resolvedUiLang == "ar"
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf(currentText) }
    var selectedAiTab by remember { mutableIntStateOf(0) }
    var statusMessage by remember { mutableStateOf("") }
    var autoProofreadEnabled by remember { mutableStateOf(true) }

    val aiTabs = remember(isAr) {
        if (isAr) listOf("✅ تدقيق", "🔄 إعادة صياغة", "🌐 ترجمة AI", "✨ كتابة وتوليد")
        else listOf("✅ Proofread", "🔄 Rephrase", "🌐 AI Translate", "✨ Compose")
    }

    val isTextArabic = remember(inputText, isAr) {
        if (inputText.isBlank()) isAr else inputText.any { it in '\u0600'..'\u06FF' }
    }

    val stats = remember(inputText, isTextArabic) {
        com.example.data.SpellCheckDictionary.getCorrectionStats(inputText, isArabic = isTextArabic)
    }

    var resultText by remember(inputText, selectedAiTab, isTextArabic) {
        mutableStateOf(
            if (inputText.isBlank()) ""
            else com.example.data.SpellCheckDictionary.autoCorrectSentence(inputText, isArabic = isTextArabic)
        )
    }

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val isKeyLight = remember(theme.keyBackgroundColor) { isLightColor(theme.keyBackgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subTextColor = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)
    val cardBg = Color(theme.keyBackgroundColor)
    val cardBorder = Color(theme.borderColor).copy(alpha = if (isLight) 0.85f else 0.5f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AutoFixHigh,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_036"),
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_037"),
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_038"),
                        color = subTextColor,
                        fontSize = 8.5.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Auto-Proofread Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (autoProofreadEnabled) Color(theme.accentColor).copy(alpha = 0.2f) else cardBg,
                    border = BorderStroke(0.8.dp, if (autoProofreadEnabled) Color(theme.accentColor) else Color(theme.borderColor)),
                    modifier = Modifier.clickable {
                        autoProofreadEnabled = !autoProofreadEnabled
                        statusMessage = if (isAr) {
                            if (autoProofreadEnabled) "التدقيق التلقائي مفعّل" else "التدقيق التلقائي معطّل"
                        } else {
                            if (autoProofreadEnabled) "Auto-proofread ON" else "Auto-proofread OFF"
                        }
                    }
                ) {
                    Text(
                        text = if (isAr) {
                            if (autoProofreadEnabled) "تدقيق تلقائي: شغال" else "تدقيق تلقائي: متوقف"
                        } else {
                            if (autoProofreadEnabled) "Auto Check: ON" else "Auto Check: OFF"
                        },
                        color = if (autoProofreadEnabled) Color(theme.accentColor) else subTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"),
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tabs Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(aiTabs.indices.toList()) { idx ->
                val isSelected = selectedAiTab == idx
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else cardBg,
                    border = BorderStroke(1.dp, if (isSelected) Color(theme.accentColor) else cardBorder),
                    modifier = Modifier.clickable {
                        selectedAiTab = idx
                        statusMessage = ""
                        when (idx) {
                            0 -> resultText = if (inputText.isNotBlank()) com.example.data.SpellCheckDictionary.autoCorrectSentence(inputText, isArabic = isTextArabic) else ""
                            1 -> resultText = if (inputText.isNotBlank()) {
                                if (isTextArabic) "يسرّني إحاطتكم علماً بأن: $inputText، مع فائق التقدير."
                                else "Please allow me to share regarding: $inputText. Best regards."
                            } else ""
                            2 -> {
                                if (inputText.isNotBlank()) {
                                    coroutineScope.launch {
                                        val src = if (isTextArabic) "ar" else "en"
                                        val tgt = if (isTextArabic) "en" else "ar"
                                        resultText = com.example.data.TranslationEngine.translate(inputText, src, tgt)
                                    }
                                } else {
                                    resultText = ""
                                }
                            }
                            3 -> resultText = if (isAr) "أشكرك جزيل الشكر على التواصل والاهتمام، وأتمنى لك دوام التوفيق والنجاح!" else "Thank you very much for reaching out! Wishing you continued success."
                        }
                    }
                ) {
                    Text(
                        text = aiTabs[idx],
                        color = if (isSelected) Color(theme.accentColor) else textColor,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Tab Content / Options
        when (selectedAiTab) {
            0 -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBg, RoundedCornerShape(8.dp))
                        .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_039", stats.inputWordCount, stats.outputWordCount),
                        color = Color(theme.accentColor),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (stats.correctedWordCount > 0) {
                            if (isAr) "تم تصحيح ${stats.correctedWordCount} كلمة" else "Fixed ${stats.correctedWordCount} word(s)"
                        } else if (stats.inputWordCount > 0) {
                            if (isAr) "سليم إملائياً ✓" else "No errors ✓"
                        } else {
                            if (isAr) "بانتظار النص" else "Waiting for text"
                        },
                        color = if (stats.correctedWordCount > 0) (if (isLight) Color(0xFFD97706) else Color(0xFFFBBF24)) else subTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            1 -> {
                val styleChips = if (isAr) {
                    listOf("formal" to "رسمي", "friendly" to "ودود", "eloquent" to "فصيح", "concise" to "موجز")
                } else {
                    listOf("formal" to "Formal", "friendly" to "Friendly", "eloquent" to "Polished", "concise" to "Concise")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    styleChips.forEach { (id, label) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cardBg,
                            border = BorderStroke(0.8.dp, cardBorder),
                            modifier = Modifier.clickable {
                                if (inputText.isNotBlank()) {
                                    resultText = when (id) {
                                        "formal" -> if (isTextArabic) "نود إفادتكم بخصوص: $inputText، شاكرين لكم حسن تعاونكم." else "We would like to inform you regarding: $inputText. Thank you for your cooperation."
                                        "friendly" -> if (isTextArabic) "يا هلا والله! حبيت أشاركك بخصوص: $inputText ✨" else "Hey there! Just wanted to share: $inputText ✨"
                                        "eloquent" -> if (isTextArabic) "يطيب لي أن أسطر إليكم ما نصه: $inputText، سائلاً المولى لكم السداد." else "It is my pleasure to convey the following: $inputText, with sincere appreciation."
                                        else -> inputText.trim().replace("\\s+".toRegex(), " ")
                                    }
                                }
                            }
                        ) {
                            Text(label, color = Color(theme.accentColor), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }
            }
            2 -> {
                val langChips = if (isAr) {
                    listOf("الإنجليزية" to "en", "الفرنسية" to "fr", "التركية" to "tr", "الإسبانية" to "es")
                } else {
                    listOf("English" to "en", "Arabic" to "ar", "French" to "fr", "Spanish" to "es")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    langChips.forEach { (langName, code) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cardBg,
                            border = BorderStroke(0.8.dp, cardBorder),
                            modifier = Modifier.clickable {
                                if (inputText.isNotBlank()) {
                                    coroutineScope.launch {
                                        val src = if (isTextArabic) "ar" else "auto"
                                        val trans = com.example.data.TranslationEngine.translate(inputText, src, code)
                                        resultText = trans
                                    }
                                }
                            }
                        ) {
                            Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_040", langName), color = Color(theme.accentColor), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }
            }
            3 -> {
                val templates = if (isAr) {
                    listOf("thanks" to "شكر وتقدير", "apology" to "اعتذار رسمي", "congrats" to "مباركة وتهنئة", "confirm" to "موافقة وتأكيد")
                } else {
                    listOf("thanks" to "Thanks", "apology" to "Apology", "congrats" to "Congrats", "confirm" to "Confirm")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    templates.forEach { (id, label) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = cardBg,
                            border = BorderStroke(0.8.dp, cardBorder),
                            modifier = Modifier.clickable {
                                resultText = when (id) {
                                    "thanks" -> if (isAr) "أتقدم لكم بخالص الشكر والتقدير على جهودكم المتميزة وتعاونكم الراقي." else "Thank you sincerely for your outstanding efforts and support."
                                    "apology" -> if (isAr) "نعتذر منكم عن أي إزعاج أو تأخير غير مقصود، ونعمل حالياً على تدارك الأمر فوراً." else "We sincerely apologize for any inconvenience or delay and are resolving it right away."
                                    "congrats" -> if (isAr) "ألف مبروك ومنها للأعلى إن شاء الله! نسأل الله لك دوام التوفيق والنجاح." else "Congratulations! Wishing you continued success and happiness."
                                    else -> if (isAr) "تم الاطلاع والموافقة على ما جاء، وبإمكانكم اعتماد الخطوة القادمة شاكرين لكم." else "Reviewed and approved. Please proceed with the next step."
                                }
                            }
                        ) {
                            Text(label, color = Color(theme.accentColor), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Result Surface
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                if (resultText.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_041"),
                            color = subTextColor,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = resultText,
                            color = if (isKeyLight) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (selectedAiTab == 0 && stats.corrections.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(stats.corrections.take(6)) { (original, corrected) ->
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isKeyLight) Color(theme.backgroundColor) else Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_042", original, corrected),
                                            color = if (isKeyLight) Color(0xFFB45309) else Color(0xFFFBBF24),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (statusMessage.isNotEmpty()) {
                        Text(
                            text = statusMessage,
                            color = if (isLight) Color(0xFF059669) else Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isKeyLight) Color(theme.backgroundColor) else Color(0xFF1E283A),
                                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.7f)),
                                modifier = Modifier.clickable {
                                    if (resultText.isNotBlank()) {
                                        resultText = com.example.data.SpellCheckDictionary.addDiacriticsStrict(resultText)
                                        statusMessage = if (isAr) "تم التشكيل ✓" else "Done ✓"
                                    }
                                }
                            ) {
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_043"),
                                    color = if (isLight) Color(0xFF7C3AED) else Color(0xFFC084FC),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isKeyLight) Color(theme.backgroundColor) else Color(0xFF1E283A),
                                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.7f)),
                                modifier = Modifier.clickable {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(ClipData.newPlainText(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_044"), resultText))
                                    statusMessage = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_008")
                                }
                            ) {
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_045"),
                                    color = if (isLight) Color(0xFF1D4ED8) else Color(0xFF60A5FA),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Apply / Replace Button
                    Button(
                        onClick = {
                            if (resultText.isNotBlank()) {
                                onReplaceText(resultText)
                                onClose()
                            }
                        },
                        enabled = resultText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(theme.accentColor),
                            disabledContainerColor = Color(theme.accentColor).copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_046"),
                            color = if (isLightColor(theme.accentColor)) Color(0xFF0F172A) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. VOICE INPUT VIEW (تحويل الصوت إلى نص المباشر)
// -------------------------------------------------------------
@Composable
fun VoiceInputView(
    theme: KeyboardTheme,
    isArabic: Boolean = true,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onInsertText: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    val isArUi = resolvedUiLang == "ar"
    var voiceLangArabic by remember { mutableStateOf(isArabic) }
    var hasAudioPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var statusText by remember(isArUi, hasAudioPermission) {
        mutableStateOf(
            if (hasAudioPermission) {
                if (isArUi) "اضغط على المايك للتحدث بصوت واضح..." else "Tap mic to speak clearly..."
            } else {
                if (isArUi) "⚠️ يتطلب إذن الميكروفون - اضغط للتفعيل" else "⚠️ Microphone permission required - tap to enable"
            }
        )
    }
    var speechRecognizer by remember { mutableStateOf<android.speech.SpeechRecognizer?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.28f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    fun startListening() {
        hasAudioPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            statusText = if (isArUi) "جاري فتح الإدخال الصوتي للنظام..." else "Opening system voice input..."
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (voiceLangArabic) "ar-SA" else "en-US")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, if (isArUi) "تحدث الآن للإدخال الصوتي..." else "Speak now for voice typing...")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                statusText = if (isArUi) "⚠️ يرجى تفعيل إذن الميكروفون للكيبورد" else "⚠️ Please enable microphone permission"
            }
            return
        }

        try {
            if (speechRecognizer == null) {
                if (android.speech.SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer = android.speech.SpeechRecognizer.createSpeechRecognizer(context)
                }
            }
            val recognizer = speechRecognizer
            if (recognizer != null) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (voiceLangArabic) "ar-SA" else "en-US")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        statusText = if (isArUi) "جاري الاستماع... تحدث الآن" else "Listening... Speak now"
                    }
                    override fun onBeginningOfSpeech() {
                        statusText = if (isArUi) "جاري التقاط الصوت..." else "Capturing audio..."
                    }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                        statusText = if (isArUi) "تم التقاط الصوت! اضغط إدراج ✓" else "Speech captured! Tap insert ✓"
                    }
                    override fun onError(error: Int) {
                        isListening = false
                        statusText = when (error) {
                            android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> if (isArUi) "يرجى منح إذن الميكروفون" else "Please grant microphone permission"
                            android.speech.SpeechRecognizer.ERROR_NO_MATCH -> if (isArUi) "لم يتم فهم الصوت، تحدث مجدداً" else "Didn't catch that, try again"
                            android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> if (isArUi) "انتهت المهلة، اضغط للتحدث" else "Timed out, tap to speak"
                            android.speech.SpeechRecognizer.ERROR_NETWORK,
                            android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> if (isArUi) "تحقق من اتصال الإنترنت" else "Check internet connection"
                            else -> if (isArUi) "اضغط المايك أو اختر عبارة جاهزة" else "Tap mic or pick a quick phrase"
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            recognizedText = matches[0]
                            statusText = if (isArUi) "تم التعرف بنجاح ✓ اضغط إدراج" else "Recognized ✓ Tap insert"
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            recognizedText = matches[0]
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                recognizer.startListening(intent)
                isListening = true
                statusText = if (isArUi) "جاري فتح الميكروفون..." else "Starting microphone..."
            } else {
                statusText = if (isArUi) "خدمة التعرف غير متوفرة، اختر من العبارات" else "Speech service unavailable, pick a phrase"
            }
        } catch (e: Exception) {
            isListening = false
            statusText = if (isArUi) "اضغط على المايك للتحدث أو اختر عبارة" else "Tap mic to speak or pick a phrase"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        isListening = false
    }

    DisposableEffect(Unit) {
        startListening()
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val isKeyLight = remember(theme.keyBackgroundColor) { isLightColor(theme.keyBackgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subTextColor = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_047"),
                    color = textColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor)),
                    modifier = Modifier.clickable {
                        voiceLangArabic = !voiceLangArabic
                        stopListening()
                    }
                ) {
                    Text(
                        text = if (voiceLangArabic) (if (isArUi) "🇸🇦 عربي" else "🇸🇦 AR") else "🇺🇸 EN",
                        color = Color(theme.accentColor),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                        .clickable(onClick = {
                            stopListening()
                            onClose()
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"),
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Animated Mic & Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((46 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) Color(theme.accentColor).copy(alpha = 0.25f)
                        else if (isLight) Color(theme.keyBackgroundColor)
                        else Color(0xFF232B3B)
                    )
                    .border(
                        1.2.dp,
                        if (isListening) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.6f),
                        CircleShape
                    )
                    .clickable {
                        if (isListening) stopListening() else startListening()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Outlined.MicNone,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_048"),
                    tint = if (isListening) Color(theme.accentColor) else (if (isLight) Color(0xFF0F172A) else Color.White),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = statusText,
                color = if (isListening) Color(theme.accentColor) else subTextColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )

            if (!hasAudioPermission) {
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444),
                    modifier = Modifier.clickable {
                        try {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (voiceLangArabic) "ar-SA" else "en-US")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isArUi) "تحدث الآن للإدخال الصوتي..." else "Speak now...")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = android.net.Uri.fromParts("package", context.packageName, null)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_049"),
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            if (recognizedText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "\"$recognizedText\"",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Quick phrases chips + Actions
        Column(modifier = Modifier.fillMaxWidth()) {
            val samplePhrases = if (voiceLangArabic) {
                listOf("السلام عليكم", "مرحباً كيف حالك", "شكراً جزيلاً", "تمام الحمد لله", "أنا في الطريق")
            } else {
                listOf("Hello there", "How are you doing?", "Thank you very much", "I will be there soon", "Sounds good")
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(samplePhrases) { phrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isLight) Color(theme.keyBackgroundColor) else Color(0xFF1E2638))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                recognizedText = phrase
                                onInsertText(phrase + " ")
                                onClose()
                            }
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = phrase,
                            color = if (isKeyLight) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        stopListening()
                        onClose()
                    },
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(theme.borderColor)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_020"), color = subTextColor, fontSize = 11.5.sp)
                }

                Button(
                    onClick = {
                        stopListening()
                        if (recognizedText.isNotBlank()) {
                            onInsertText(recognizedText + " ")
                        }
                        onClose()
                    },
                    enabled = recognizedText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(theme.accentColor),
                        disabledContainerColor = Color(theme.accentColor).copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.weight(1.5f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_050"),
                        color = if (isLightColor(theme.accentColor)) Color(0xFF0F172A) else Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. QUICK IN-KEYBOARD SETTINGS VIEW (الإعدادات السريعة)
// -------------------------------------------------------------
@Composable
fun QuickSettingsView(
    theme: KeyboardTheme,
    settings: KeyboardSettings,
    keyboardHeight: Dp = 235.dp,
    appLanguageCode: String = "auto",
    onUpdateSettings: (KeyboardSettings) -> Unit,
    onOpenFullSettings: () -> Unit,
    onCustomizeToolbar: () -> Unit = {},
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    val isArUi = resolvedUiLang == "ar"
    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color.White
    val subTextColor = if (isLight) Color(0xFF475569) else Color(0xFF8E9BAE)
    val rowBg = if (isLight) Color(theme.keyBackgroundColor) else Color(0xFF141924)
    val rowBorder = if (isLight) Color(theme.borderColor).copy(alpha = 0.6f) else Color(0xFF26334A).copy(alpha = 0.4f)
    val accentCol = Color(theme.accentColor)

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val dialogHeight = if (isLandscape) (configuration.screenHeightDp * 0.72f).dp.coerceAtLeast(155.dp) else keyboardHeight.coerceAtLeast(215.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(dialogHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    tint = accentCol,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_051"),
                    color = textColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"),
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_034"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_035"),
                    icon = Icons.Outlined.Dashboard,
                    checked = settings.toolbarEnabled,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(toolbarEnabled = it)) }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.VolumeUp, contentDescription = null, tint = if (settings.soundEnabled) accentCol else subTextColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_052", (settings.soundVolume * 100).toInt()), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_053"), color = subTextColor, fontSize = 9.sp)
                            }
                        }
                        Switch(
                            checked = settings.soundEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = accentCol),
                            modifier = Modifier.height(26.dp)
                        )
                    }

                    if (settings.soundEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = settings.soundVolume,
                            onValueChange = { vol ->
                                onUpdateSettings(settings.copy(soundVolume = vol))
                            },
                            valueRange = 0.10f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                            modifier = Modifier.fillMaxWidth().height(24.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                Pair("30%", 0.30f),
                                Pair("60%", 0.60f),
                                Pair("85%", 0.85f),
                                Pair("100%", 1.0f)
                            ).forEach { (lbl, vol) ->
                                val isSel = kotlin.math.abs(settings.soundVolume - vol) < 0.08f
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                    border = BorderStroke(if (isSel) 1.2.dp else 0.8.dp, if (isSel) accentCol else rowBorder),
                                    modifier = Modifier.weight(1f).clickable {
                                        onUpdateSettings(settings.copy(soundVolume = vol))
                                        KeyboardSoundEngine.playKeySound(settings.soundProfile, vol)
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 8.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            KeyboardSoundEngine.AVAILABLE_PROFILES.forEach { prof ->
                                val isSel = settings.soundProfile == prof.id
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                    border = BorderStroke(if (isSel) 1.2.dp else 0.8.dp, if (isSel) accentCol else rowBorder),
                                    modifier = Modifier.clickable {
                                        onUpdateSettings(settings.copy(soundProfile = prof.id))
                                        KeyboardSoundEngine.playKeySound(prof.id, settings.soundVolume)
                                    }
                                ) {
                                    Text(
                                        text = if (isArUi) prof.nameAr else prof.nameEn,
                                        color = if (isSel) Color.Black else textColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Speed, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_054", settings.keyRepeatSpeedMs), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    when (settings.typingSpeedMode) {
                                        "fast" -> AppStrings.trCode(context, appLanguageCode, "preset_ultra_fast")
                                        "smooth" -> AppStrings.trCode(context, appLanguageCode, "preset_fast")
                                        "slow" -> AppStrings.trCode(context, appLanguageCode, "preset_calm")
                                        else -> AppStrings.trCode(context, appLanguageCode, "preset_medium")
                                    },
                                    color = accentCol,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = settings.keyRepeatSpeedMs.toFloat(),
                        onValueChange = { ms ->
                            val intMs = ms.toInt()
                            val mode = when {
                                intMs <= 20 -> "fast"
                                intMs <= 35 -> "smooth"
                                intMs <= 55 -> "medium"
                                else -> "slow"
                            }
                            onUpdateSettings(
                                settings.copy(
                                    keyRepeatSpeedMs = intMs,
                                    typingSpeedMode = mode,
                                    longPressDelayMs = (intMs * 7).coerceIn(180, 520)
                                )
                            )
                        },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Triple("${AppStrings.trCode(context, appLanguageCode, "preset_ultra_fast")} 15ms", "fast", Pair(15, 180)),
                            Triple("${AppStrings.trCode(context, appLanguageCode, "preset_fast")} 25ms", "fast", Pair(25, 220)),
                            Triple("${AppStrings.trCode(context, appLanguageCode, "preset_medium")} 45ms", "medium", Pair(45, 340)),
                            Triple("${AppStrings.trCode(context, appLanguageCode, "preset_calm")} 75ms", "slow", Pair(75, 480))
                        ).forEach { (lbl, mode, timings) ->
                            val isSel = kotlin.math.abs(settings.keyRepeatSpeedMs - timings.first) < 6
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                border = BorderStroke(if (isSel) 1.2.dp else 0.8.dp, if (isSel) accentCol else rowBorder),
                                modifier = Modifier.weight(1f).clickable {
                                    onUpdateSettings(
                                        settings.copy(
                                            typingSpeedMode = mode,
                                            keyRepeatSpeedMs = timings.first,
                                            longPressDelayMs = timings.second,
                                            keyPressTimingStyle = if (mode == "fast") "ultra_fast" else "ios_balanced"
                                        )
                                    )
                                    KeyboardSoundEngine.playKeySound(settings.soundProfile, settings.soundVolume)
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Vibration, contentDescription = null, tint = if (settings.vibrationEnabled) accentCol else subTextColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_055", settings.vibrationDurationMs), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_056"), color = subTextColor, fontSize = 9.sp)
                            }
                        }
                        Switch(
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { onUpdateSettings(settings.copy(vibrationEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = accentCol),
                            modifier = Modifier.height(26.dp)
                        )
                    }

                    if (settings.vibrationEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = settings.vibrationDurationMs.toFloat(),
                            onValueChange = { ms ->
                                onUpdateSettings(settings.copy(vibrationDurationMs = ms.toInt()))
                            },
                            valueRange = 5f..100f,
                            colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                            modifier = Modifier.fillMaxWidth().height(24.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                Pair("${AppStrings.trCode(context, appLanguageCode, "vib_light")} 15ms", 15),
                                Pair("${AppStrings.trCode(context, appLanguageCode, "vib_medium")} 30ms", 30),
                                Pair("${AppStrings.trCode(context, appLanguageCode, "vib_strong")} 55ms", 55),
                                Pair("85ms", 85)
                            ).forEach { (lbl, dur) ->
                                val isSel = kotlin.math.abs(settings.vibrationDurationMs - dur) < 6
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                    border = BorderStroke(if (isSel) 1.2.dp else 0.8.dp, if (isSel) accentCol else rowBorder),
                                    modifier = Modifier.weight(1f).clickable {
                                        onUpdateSettings(settings.copy(vibrationDurationMs = dur))
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 8.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Tune, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_057", (settings.keyHeightFactor * 100).toInt()), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_058"), color = subTextColor, fontSize = 9.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = settings.keyHeightFactor,
                        onValueChange = { onUpdateSettings(settings.copy(keyHeightFactor = it)) },
                        valueRange = 0.60f..1.70f,
                        colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair(AppStrings.trCode(context, appLanguageCode, "key_height_preset_compact"), 0.70f),
                            Pair("85%", 0.85f),
                            Pair("100%", 1.0f),
                            Pair("130%", 1.30f),
                            Pair(AppStrings.trCode(context, appLanguageCode, "key_height_preset_xl"), 1.60f)
                        ).forEach { (lbl, factor) ->
                            val isSel = kotlin.math.abs(settings.keyHeightFactor - factor) < 0.05f
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                modifier = Modifier.weight(1f).clickable {
                                    onUpdateSettings(settings.copy(keyHeightFactor = factor))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.FormatSize, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_059", (settings.keyFontSizeFactor * 100).toInt()), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_060"), color = subTextColor, fontSize = 9.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = settings.keyFontSizeFactor,
                        onValueChange = { onUpdateSettings(settings.copy(keyFontSizeFactor = it)) },
                        valueRange = 0.75f..1.35f,
                        colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair("85%", 0.85f),
                            Pair("100%", 1.0f),
                            Pair("115%", 1.15f),
                            Pair("130%", 1.30f)
                        ).forEach { (lbl, factor) ->
                            val isSel = kotlin.math.abs(settings.keyFontSizeFactor - factor) < 0.04f
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                border = BorderStroke(if (isSel) 1.2.dp else 0.8.dp, if (isSel) accentCol else rowBorder),
                                modifier = Modifier.weight(1f).clickable {
                                    onUpdateSettings(settings.copy(keyFontSizeFactor = factor))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 8.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.TouchApp, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_061", settings.floatingNavSpeed), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_062"), color = subTextColor, fontSize = 9.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Slider(
                        value = settings.floatingNavSpeed.toFloat(),
                        onValueChange = { spd ->
                            onUpdateSettings(settings.copy(floatingNavSpeed = spd.toInt()))
                        },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair("1 (${AppStrings.trCode(context, appLanguageCode, "preset_calm")})", 1),
                            Pair("4 (${AppStrings.trCode(context, appLanguageCode, "preset_comfortable")})", 4),
                            Pair("7 (${AppStrings.trCode(context, appLanguageCode, "preset_fast")})", 7),
                            Pair("10 (${AppStrings.trCode(context, appLanguageCode, "preset_ultra_fast")})", 10)
                        ).forEach { (lbl, spd) ->
                            val isSel = settings.floatingNavSpeed == spd
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                border = BorderStroke(if (isSel) 1.2.dp else 0.8.dp, if (isSel) accentCol else rowBorder),
                                modifier = Modifier.weight(1f).clickable {
                                    onUpdateSettings(settings.copy(floatingNavSpeed = spd))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_063"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_064"),
                    icon = Icons.Outlined.ZoomIn,
                    checked = settings.keyPopupEnabled,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(keyPopupEnabled = it)) }
                )
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_065"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_066"),
                    icon = Icons.Outlined.Pin,
                    checked = settings.numberRowEnabled,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(numberRowEnabled = it)) }
                )
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_028"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_067"),
                    icon = Icons.Outlined.Pin,
                    checked = settings.forceEnglishNumbers,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(forceEnglishNumbers = it)) }
                )
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_068"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_069"),
                    icon = Icons.Outlined.AutoFixHigh,
                    checked = settings.suggestionsEnabled,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(suggestionsEnabled = it)) }
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.AspectRatio, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_070", settings.sideMarginDp, settings.bottomMarginDp), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_071"), color = subTextColor, fontSize = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_072", settings.sideMarginDp), color = textColor, fontSize = 10.sp)
                    Slider(
                        value = settings.sideMarginDp.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(sideMarginDp = it.toInt())) },
                        valueRange = 0f..32f,
                        colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_073", settings.bottomMarginDp), color = textColor, fontSize = 10.sp)
                    Slider(
                        value = settings.bottomMarginDp.toFloat(),
                        onValueChange = { onUpdateSettings(settings.copy(bottomMarginDp = it.toInt())) },
                        valueRange = 0f..32f,
                        colors = SliderDefaults.colors(thumbColor = accentCol, activeTrackColor = accentCol),
                        modifier = Modifier.fillMaxWidth().height(24.dp)
                    )
                }
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_074"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_075"),
                    icon = Icons.Outlined.Layers,
                    checked = settings.samsungStyleHints,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(samsungStyleHints = it)) }
                )
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_076"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_077"),
                    icon = Icons.Outlined.EmojiEmotions,
                    checked = settings.replaceNumberRowWithEmojis,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(replaceNumberRowWithEmojis = it)) }
                )
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_078"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_079"),
                    icon = Icons.Outlined.ViewWeek,
                    checked = settings.isSplitKeyboard,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(isSplitKeyboard = it)) }
                )
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_080"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_081"),
                    icon = Icons.Outlined.PictureInPicture,
                    checked = settings.isFloatingKeyboard,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(isFloatingKeyboard = it)) }
                )
            }

            item {
                QuickSettingToggleRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_082"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_083"),
                    icon = Icons.Outlined.Calculate,
                    checked = settings.calculatorNumpadMode,
                    accentColor = accentCol,
                    textColor = textColor,
                    subTextColor = subTextColor,
                    cardBg = rowBg,
                    cardBorder = rowBorder,
                    onCheckedChange = { onUpdateSettings(settings.copy(calculatorNumpadMode = it)) }
                )
            }

            item {
                var syncFeedbackMessage by remember { mutableStateOf("") }
                val prefs = remember { PreferencesManager(context) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.CloudSync, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_084"), color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_085"), color = subTextColor, fontSize = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val json = prefs.exportShortcutsJson()
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("TurboKeyboardBackup", json))
                                syncFeedbackMessage = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_113")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentCol),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_086"), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = cm?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                if (clip.startsWith("{") || clip.startsWith("[")) {
                                    val success = prefs.importShortcutsJson(clip)
                                    syncFeedbackMessage = if (success) {
                                        AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_122")
                                    } else {
                                        AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_123")
                                    }
                                } else {
                                    syncFeedbackMessage = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_121")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isLight) Color(0xFFE2E8F0) else Color(0xFF1E2838)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_087"), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (syncFeedbackMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(syncFeedbackMessage, color = accentCol, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentCol.copy(alpha = 0.15f))
                        .clickable { onCustomizeToolbar() }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_088"), color = textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_089"), color = subTextColor, fontSize = 9.sp)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = accentCol, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                onOpenFullSettings()
                onClose()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = accentCol
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(34.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_090"),
                color = Color.Black,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickSettingToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    accentColor: Color,
    textColor: Color,
    subTextColor: Color,
    cardBg: Color,
    cardBorder: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) accentColor else subTextColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = subTextColor,
                    fontSize = 9.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF222B3B)
            ),
            modifier = Modifier.height(26.dp)
        )
    }
}

@Composable
fun DecoratedPhrasesView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onPhraseSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    val isArUi = resolvedUiLang == "ar"
    val categories = com.example.data.DecoratedPhrasesData.categories
    var selectedCatId by remember { mutableStateOf(categories.first().id) }
    val currentCategory = categories.find { it.id == selectedCatId } ?: categories.first()

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val isKeyLight = remember(theme.keyBackgroundColor) { isLightColor(theme.keyBackgroundColor) }
    val headerBg = if (isLight) Color(theme.keyBackgroundColor) else Color(0xFF141926)
    val unselectedTextColor = if (isLight) Color(0xFF475569) else Color.LightGray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(headerBg, RoundedCornerShape(8.dp))
                .border(1.dp, Color(theme.borderColor).copy(alpha = if (isLight) 0.8f else 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat.id == selectedCatId
                    Surface(
                        modifier = Modifier
                            .clickable { selectedCatId = cat.id },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color.Transparent,
                        border = if (isSelected) BorderStroke(1.dp, Color(theme.accentColor)) else null
                    ) {
                        Text(
                            text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_091", cat.icon, cat.getDisplayName(isArUi)),
                            color = if (isSelected) Color(theme.accentColor) else unselectedTextColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Phrases Grid
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(currentCategory.phrases.size) { idx ->
                val phrase = currentCategory.phrases[idx]
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPhraseSelected(phrase) },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = if (isLight) 0.8f else 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phrase,
                            color = if (isKeyLight) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorPadView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onInsertText: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val isKeyLight = remember(theme.keyBackgroundColor) { isLightColor(theme.keyBackgroundColor) }
    val screenBg = if (isLight) Color(theme.keyBackgroundColor) else Color(0xFF0F1420)
    val screenTextColor = if (isLight) Color(0xFF0F172A) else Color.White

    fun evaluate(expr: String): String {
        return try {
            if (expr.isBlank()) return ""
            val cleanExpr = expr.replace("×", "*").replace("÷", "/")
            val res = evaluateSimpleMath(cleanExpr)
            if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res).trimEnd('0').trimEnd('.')
        } catch (_: Exception) {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Screen display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(screenBg, RoundedCornerShape(8.dp))
                .border(1.dp, Color(theme.borderColor).copy(alpha = if (isLight) 0.8f else 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = expression.ifEmpty { "0" },
                    color = screenTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (result.isNotEmpty()) {
                    Text(
                        text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_092", result),
                        color = Color(theme.accentColor),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        if (result.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        onInsertText(result)
                        onClose()
                    },
                    modifier = Modifier.weight(1f).height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor)),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        AppStrings.trfCode(context, appLanguageCode, "calc_insert_result", result),
                        color = if (isLightColor(theme.accentColor)) Color(0xFF0F172A) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        onInsertText(AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_093", expression, result))
                        onClose()
                    },
                    modifier = Modifier.weight(1f).height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_094"), color = Color(theme.accentColor), fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
        }

        val buttons = listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=", "⌫")
        )

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { btn ->
                        val isOp = btn in listOf("÷", "×", "-", "+", "=")
                        val isAction = btn in listOf("C", "⌫")
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable {
                                    when (btn) {
                                        "C" -> {
                                            expression = ""
                                            result = ""
                                        }
                                        "⌫" -> {
                                            if (expression.isNotEmpty()) {
                                                expression = expression.dropLast(1)
                                                result = evaluate(expression)
                                            }
                                        }
                                        "=" -> {
                                            if (expression.isNotEmpty()) {
                                                result = evaluate(expression)
                                                if (result.isNotEmpty()) expression = result
                                            }
                                        }
                                        else -> {
                                            expression += btn
                                            result = evaluate(expression)
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(6.dp),
                            color = when {
                                btn == "=" -> Color(theme.enterButtonColor)
                                isOp -> if (isKeyLight) Color(theme.keyPressedColor) else Color(0xFF1E2838)
                                isAction -> if (isKeyLight) Color(0xFFFFEBEE) else Color(0xFF2D1820)
                                else -> Color(theme.keyBackgroundColor)
                            },
                            border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = btn,
                                    color = when {
                                        btn == "=" -> if (isLightColor(theme.enterButtonColor)) Color(0xFF0F172A) else Color.White
                                        isOp -> Color(theme.accentColor)
                                        isAction -> Color(0xFFFF5252)
                                        else -> if (isKeyLight) Color(0xFF0F172A) else Color(theme.keyTextColor)
                                    },
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun evaluateSimpleMath(expr: String): Double {
    val tokens = mutableListOf<String>()
    var numberBuffer = StringBuilder()

    for (ch in expr) {
        if (ch.isDigit() || ch == '.') {
            numberBuffer.append(ch)
        } else if (ch in "+-*/%") {
            if (numberBuffer.isNotEmpty()) {
                tokens.add(numberBuffer.toString())
                numberBuffer = StringBuilder()
            }
            tokens.add(ch.toString())
        }
    }
    if (numberBuffer.isNotEmpty()) {
        tokens.add(numberBuffer.toString())
    }

    if (tokens.isEmpty()) return 0.0

    // Multiply, divide, modulo
    val pass1 = mutableListOf<String>()
    var i = 0
    while (i < tokens.size) {
        val t = tokens[i]
        if (t == "*" || t == "/" || t == "%") {
            val left = pass1.removeAt(pass1.size - 1).toDoubleOrNull() ?: 0.0
            val right = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 1.0
            val res = when (t) {
                "*" -> left * right
                "/" -> if (right != 0.0) left / right else 0.0
                else -> left % right
            }
            pass1.add(res.toString())
            i += 2
        } else {
            pass1.add(t)
            i++
        }
    }

    // Add and subtract
    var total = pass1.firstOrNull()?.toDoubleOrNull() ?: 0.0
    var j = 1
    while (j < pass1.size) {
        val op = pass1[j]
        val num = pass1.getOrNull(j + 1)?.toDoubleOrNull() ?: 0.0
        if (op == "+") total += num
        else if (op == "-") total -= num
        j += 2
    }

    return total
}

// -------------------------------------------------------------
// 10. CUSTOMIZE TOOLBAR DRAWER (تخصيص وإخفاء أدوات شريط الكيبورد)
// -------------------------------------------------------------
data class ToolbarToolDefinition(
    val id: String,
    val nameAr: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val description: String,
    val nameEn: String = "",
    val descriptionEn: String = ""
) {
    fun getDisplayName(isAr: Boolean): String = if (isAr) nameAr else nameEn.ifBlank { nameAr }
    fun getDisplayDescription(isAr: Boolean): String = if (isAr) description else descriptionEn.ifBlank { description }
}

val allKeyboardToolbarTools = listOf(
    ToolbarToolDefinition("stickers", "ملصقات وستيكرات", Icons.Outlined.AutoAwesomeMosaic, "تصميم وإرسال الملصقات العربية الجاهزة والمصممة", "Stickers & Packs", "Design and send ready-made & custom stickers"),
    ToolbarToolDefinition("my_stickers", "ملصقاتي الخاصة", Icons.Outlined.PhotoFilter, "استعراض وتصميم ملصقاتك الخاصة وحفظها", "My Stickers", "Browse, create, and save custom stickers"),
    ToolbarToolDefinition("translate", "ترجمة فورية", Icons.Default.Translate, "ترجمة فورية للنصوص بجميع لغات العالم", "Instant Translate", "Translate text in real time across world languages"),
    ToolbarToolDefinition("clipboard", "حافظة النصوص", Icons.Outlined.ContentPaste, "حفظ النصوص المنسوخة والرجوع لها للأبد", "Clipboard", "Save copied clips forever and pin favorites"),
    ToolbarToolDefinition("english_numbers", "أرقام إنجليزية 123", Icons.Outlined.Pin, "تثبيت الأرقام الإنجليزية (123) حتى مع اللغة العربية", "Western Digits 123", "Always type Western 123 digits with Arabic layout"),
    ToolbarToolDefinition("decoration", "زخرفة النصوص", Icons.Outlined.AutoAwesome, "زخرفة الكلمات والخطوط والعبارات الحية", "Text Decoration", "Apply stylish fonts and live calligraphy"),
    ToolbarToolDefinition("phrases", "كليشات وعبارات", Icons.Outlined.FavoriteBorder, "عبارات ترحيب وإسلامية ونصوص جاهزة", "Quick Phrases", "Ready greetings, templates, and decorative phrases"),
    ToolbarToolDefinition("calculator", "آلة حاسبة", Icons.Outlined.Calculate, "حاسبة رياضية فورية وكتابة النتيجة", "Calculator", "Instant math calculator with direct result insert"),
    ToolbarToolDefinition("emoji", "لوحة الإيموجي", Icons.Outlined.Mood, "إيموجي آيفون وأندرويد وقسم المستعملة", "Emoji Panel", "Categorized emojis with search and recents"),
    ToolbarToolDefinition("voice", "كتابة صوتية", Icons.Default.Mic, "تحويل الصوت المباشر إلى نصوص مكتوبة", "Voice Typing", "Convert live speech directly into typed text"),
    ToolbarToolDefinition("tts", "قراءة النص بصوت", Icons.Outlined.VolumeUp, "اقرأ النص بصوت الجوال والناطق الصوتي", "Read Aloud (TTS)", "Speak typed text using system text-to-speech"),
    ToolbarToolDefinition("pass", "Keyboard Pass", Icons.Outlined.Password, "حفظ وتعبئة كلمات المرور والآيبان والبطاقات", "Keyboard Pass", "Save and autofill logins, IBANs, and cards"),
    ToolbarToolDefinition("ai", "AI Keyboard ذكاء اصطناعي", Icons.Outlined.AutoFixHigh, "صحّح وأعد الصياغة وترجم واكتب بالذكاء", "Smart AI Keyboard", "Proofread, rewrite tone, and polish text"),
    ToolbarToolDefinition("photos", "صور وملصقات", Icons.Outlined.PhotoLibrary, "ملصقات وستيكرات وصور مجهزة", "Photos & Badges", "Quick reaction cards and media stickers"),
    ToolbarToolDefinition("gif", "Kaomoji و GIF", Icons.Outlined.Gif, "فيسات يابانية وصور متحركة سريعة", "Kaomoji & GIFs", "Expressive kaomoji faces and text reactions"),
    ToolbarToolDefinition("quick_actions", "أدوات الأحرف (مسح، قراءة، لصق)", Icons.Outlined.FormatListBulleted, "إظهار أو حذف شريط الأدوات فوق الأحرف مباشرة", "Quick Actions Bar", "Toggle quick Clear, Read, Paste, Copy bar"),
    ToolbarToolDefinition("reset_settings", "استعادة الإعدادات الأصلية", Icons.Outlined.RestartAlt, "استعادة الإعدادات الافتراضية للكيبورد بضغطة واحدة", "Reset Defaults", "Restore factory keyboard settings in one tap"),
    ToolbarToolDefinition("settings", "إعدادات التطبيق", Icons.Outlined.Settings, "الوصول السريع لكافة تخصيصات الكيبورد", "App Settings", "Quick access to full Turbo Keyboard settings")
)

@Composable
fun CustomizeToolbarDrawer(
    theme: KeyboardTheme,
    visibleTools: List<String>,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onUpdateTools: (List<String>) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    val isArUi = resolvedUiLang == "ar"
    val currentSelected = remember(visibleTools) {
        if (visibleTools.isEmpty()) allKeyboardToolbarTools.map { it.id }.toSet()
        else visibleTools.toSet()
    }

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val primaryText = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val secondaryText = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_044"),
                    color = primaryText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = 0.6f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onUpdateTools(listOf("translate", "clipboard", "decoration", "emoji", "settings"))
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_095"), color = Color(theme.accentColor), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = 0.6f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onUpdateTools(listOf("translate", "clipboard", "decoration", "voice", "phrases"))
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_050"), color = primaryText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = 0.6f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onUpdateTools(allKeyboardToolbarTools.map { it.id })
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_051"), color = primaryText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(allKeyboardToolbarTools) { tool ->
                val isChecked = tool.id in currentSelected
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isChecked) Color(theme.keyBackgroundColor) else Color(theme.backgroundColor),
                    border = BorderStroke(0.8.dp, if (isChecked) Color(theme.accentColor).copy(alpha = 0.4f) else Color(theme.borderColor).copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val next = if (isChecked) {
                                if (currentSelected.size > 1) currentSelected - tool.id else currentSelected
                            } else {
                                currentSelected + tool.id
                            }
                            onUpdateTools(allKeyboardToolbarTools.map { it.id }.filter { it in next })
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = null,
                                tint = if (isChecked) Color(theme.accentColor) else secondaryText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tool.getDisplayName(isArUi),
                                    color = if (isChecked) primaryText else secondaryText,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = tool.getDisplayDescription(isArUi),
                                    color = secondaryText.copy(alpha = 0.8f),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Switch(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                val next = if (checked) {
                                    currentSelected + tool.id
                                } else {
                                    if (currentSelected.size > 1) currentSelected - tool.id else currentSelected
                                }
                                onUpdateTools(allKeyboardToolbarTools.map { it.id }.filter { it in next })
                            },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TelegramChannelsSubView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color.White

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color(0xFF0088CC),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_096"),
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"),
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/Abu2Saleh")).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF131F33),
            border = BorderStroke(1.2.dp, Color(0xFF0088CC).copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_tools_screen_008"),
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "t.me/Abu2Saleh",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.5.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0088CC)
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_097"),
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    try {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/+OWBdNfDNS8MzYzQ0")).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF131F33),
            border = BorderStroke(1.2.dp, Color(0xFF0088CC).copy(alpha = 0.8f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_098"),
                        color = Color.White,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "t.me/+OWBdNfDNS8MzYzQ0",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.5.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0088CC)
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_097"),
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ThemesPickerDrawer(
    theme: KeyboardTheme,
    prefs: PreferencesManager,
    keyboardHeight: Dp,
    allThemes: List<KeyboardTheme>,
    searchQuery: String = "",
    appLanguageCode: String = "auto",
    onSearchQueryChange: (String) -> Unit = {},
    onThemeSelected: (KeyboardTheme) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    val isArUi = resolvedUiLang == "ar"
    val coroutineScope = rememberCoroutineScope()
    val currentThemeId = theme.id

    val isLoading = allThemes.isEmpty()

    val filteredThemes = remember(allThemes, searchQuery) {
        if (searchQuery.isBlank()) allThemes
        else allThemes.filter {
            it.nameAr.contains(searchQuery.trim(), ignoreCase = true) ||
            it.nameEn.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_099", filteredThemes.size),
                    color = Color(theme.keyTextColor),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"),
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_100"), fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(theme.keyBackgroundColor),
                unfocusedContainerColor = Color(theme.keyBackgroundColor),
                focusedBorderColor = Color(theme.accentColor),
                unfocusedBorderColor = Color(theme.borderColor).copy(alpha = 0.5f),
                focusedTextColor = Color(theme.keyTextColor),
                unfocusedTextColor = Color(theme.keyTextColor)
            )
        )

        Spacer(modifier = Modifier.height(5.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = Color(theme.accentColor)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                items(filteredThemes, key = { it.id }) { itemTheme ->
                    val isCurrent = itemTheme.id == currentThemeId
                    val itemKeyBg = remember(itemTheme.keyBackgroundColor, itemTheme.keyOpacity) {
                        ThemePresets.resolveKeyColor(itemTheme.keyBackgroundColor, itemTheme.keyOpacity)
                    }
                    val itemAccent = Color(itemTheme.accentColor)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                coroutineScope.launch(Dispatchers.IO) {
                                    prefs.setCurrentTheme(itemTheme.id)
                                    withContext(Dispatchers.Main) {
                                        onThemeSelected(itemTheme)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(itemTheme.backgroundColor),
                        border = BorderStroke(
                            width = if (isCurrent) 1.8.dp else 0.8.dp,
                            color = if (isCurrent) itemAccent else Color(itemTheme.borderColor).copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArUi) itemTheme.nameAr else itemTheme.nameEn,
                                    color = Color(itemTheme.keyTextColor),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(itemAccent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(9.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                repeat(5) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(13.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(itemKeyBg)
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

// -------------------------------------------------------------
// 12. TTS SPEECH SUBVIEW (اقرأ النص بصوت الجوال)
// -------------------------------------------------------------
@Composable
fun TtsSpeechSubView(
    theme: KeyboardTheme,
    currentText: String,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    val isArUi = resolvedUiLang == "ar"
    val ttsEngine = remember { com.example.sound.KeyboardTtsEngine.getInstance(context) }
    val defaultTtsPrompt = if (isArUi) "أهلاً بك في كيبورد تيربو الذكي! يمكنك قراءة أي نص بصوت الجوال الواضح." else "Welcome to Turbo Keyboard! Read any typed text aloud clearly."
    var textToRead by remember(currentText, defaultTtsPrompt) { mutableStateOf(currentText.ifBlank { defaultTtsPrompt }) }
    var speedRate by remember { mutableFloatStateOf(1.0f) }
    var isArabicTts by remember { mutableStateOf(isArUi) }
    var isReading by remember { mutableStateOf(false) }

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    DisposableEffect(Unit) {
        onDispose {
            ttsEngine.stop()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_101"),
                    color = textColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = {
                        ttsEngine.stop()
                        onClose()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(theme.keyBackgroundColor),
            border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = textToRead,
                    color = textColor,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { rate ->
                    val isSelected = speedRate == rate
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                        border = BorderStroke(1.dp, if (isSelected) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.4f)),
                        modifier = Modifier.clickable { speedRate = rate }
                    ) {
                        Text(
                            text = "${rate}x",
                            color = if (isSelected) Color(theme.accentColor) else textColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.5f)),
                modifier = Modifier.clickable { isArabicTts = !isArabicTts }
            ) {
                Text(
                    text = if (isArabicTts) (if (isArUi) "🇸🇦 عربي" else "🇸🇦 AR") else "🇺🇸 EN",
                    color = Color(theme.accentColor),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                if (isReading) {
                    ttsEngine.stop()
                    isReading = false
                } else {
                    ttsEngine.speak(textToRead, isArabic = isArabicTts, speedRate = speedRate)
                    isReading = true
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(theme.accentColor)
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = if (isReading) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isLightColor(theme.accentColor)) Color(0xFF0F172A) else Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isReading) {
                    if (isArUi) "إيقاف الصوت ⏹" else "Stop Speaking ⏹"
                } else {
                    if (isArUi) "استمع للنص بصوت واضح ▶" else "Listen Aloud ▶"
                },
                color = if (isLightColor(theme.accentColor)) Color(0xFF0F172A) else Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun KeyboardPassSubView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onInsertText: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val resolvedUiLang = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context) else AppStrings.getNormalizedLangCode(appLanguageCode)
    val isArUi = resolvedUiLang == "ar"
    val passManager = remember { com.example.data.KeyboardPassManager(context) }
    var entries by remember { mutableStateOf(passManager.getAllEntries()) }
    var selectedCat by remember { mutableStateOf("all") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importCsvText by remember { mutableStateOf("") }
    var importResultMsg by remember { mutableStateOf("") }

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val subTextColor = if (isLight) Color(0xFF475569) else Color(0xFF94A3B8)

    val filtered = remember(entries, selectedCat) {
        if (selectedCat == "all") entries
        else entries.filter { it.category == selectedCat }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Outlined.Password,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_102"),
                        color = textColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_103"),
                        color = subTextColor,
                        fontSize = 8.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(theme.accentColor).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.6f)),
                    modifier = Modifier.clickable { showImportDialog = true }
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_104"),
                        color = Color(theme.accentColor),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val cats = listOf(
                "all" to AppStrings.trCode(context, appLanguageCode, "all_label"),
                "iban" to (if (isArUi) "🏦 آيبان" else "🏦 IBAN"),
                "card" to (if (isArUi) "💳 بطاقات" else "💳 Cards"),
                "login" to (if (isArUi) "🔐 دخول" else "🔐 Login")
            )
            cats.forEach { (key, label) ->
                val isSel = selectedCat == key
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSel) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, if (isSel) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.4f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedCat = key }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = label,
                            color = if (isSel) Color(theme.accentColor) else textColor,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (showImportDialog) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, Color(theme.accentColor)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_105"),
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = importCsvText,
                        onValueChange = { importCsvText = it },
                        placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_106"), fontSize = 9.5.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = TextStyle(fontSize = 10.sp, color = textColor)
                    )
                    if (importResultMsg.isNotEmpty()) {
                        Text(importResultMsg, color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                val count = passManager.importFromCsv(importCsvText)
                                if (count > 0) {
                                    entries = passManager.getAllEntries()
                                    importResultMsg = if (isArUi) "تم استيراد $count حساب بنجاح ✓" else "Imported $count entries ✓"
                                    showImportDialog = false
                                } else {
                                    importResultMsg = if (isArUi) "لم يتم العثور على أسطر صالحة" else "No valid rows found"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor)),
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_107"), color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showImportDialog = false },
                            modifier = Modifier.weight(1f).height(30.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_020"), fontSize = 10.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filtered) { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(theme.keyBackgroundColor),
                        border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = textColor,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (item.username.isNotEmpty()) {
                                    Text(
                                        text = item.username,
                                        color = subTextColor,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (item.username.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(theme.accentColor).copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            onInsertText(item.username)
                                            onClose()
                                        }
                                    ) {
                                        Text(
                                            text = if (item.category == "iban") (if (isArUi) "إدراج الآيبان" else "IBAN") else (if (isArUi) "اسم المستخدم" else "Username"),
                                            color = Color(theme.accentColor),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (item.password.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            onInsertText(item.password)
                                            onClose()
                                        }
                                    ) {
                                        Text(
                                            text = if (item.category == "card") (if (isArUi) "CVV/تاريخ" else "CVV/Exp") else (if (isArUi) "كلمة المرور" else "Password"),
                                            color = Color(0xFF10B981),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
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

@Composable
fun MyStickersSubView(
    theme: KeyboardTheme,
    keyboardHeight: Dp = 205.dp,
    appLanguageCode: String = "auto",
    onStickerSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var newStickerText by remember { mutableStateOf("") }
    val myStickers = remember {
        mutableStateListOf(
            "🌹 تحياتي وتقديري يا غالي",
            "❤️ يسعد مساكم جميعاً",
            "✨ جمعة مباركة وطيبة",
            "🔥 كفو والله ما قصرت",
            "👑 بطل والله العظيم",
            "🌸 صبحكم الله بالخير والسرور",
            "⚡ ع راسي والله",
            "🤝 تم بإذن الله، اعتمد"
        )
    }

    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.PhotoFilter,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.trfCode(context, appLanguageCode, "ui_keyboard_sub_views_108", myStickers.size),
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_019"), tint = Color.White, modifier = Modifier.size(15.dp))
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newStickerText,
                onValueChange = { newStickerText = it },
                placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_109"), fontSize = 10.5.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(fontSize = 11.sp, color = textColor)
            )

            Button(
                onClick = {
                    if (newStickerText.isNotBlank()) {
                        myStickers.add(0, newStickerText.trim())
                        newStickerText = ""
                    }
                },
                enabled = newStickerText.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor)),
                modifier = Modifier.height(38.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_110"), color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(myStickers) { sticker ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            onStickerSelected(sticker)
                            onClose()
                        }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp)) {
                        Text(
                            text = sticker,
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}



