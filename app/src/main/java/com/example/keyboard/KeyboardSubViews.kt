package com.example.keyboard

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
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
import com.example.data.EmojiData
import com.example.data.PreferencesManager
import com.example.data.TextDecorator
import com.example.data.TranslationEngine
import com.example.model.ClipboardItem
import com.example.model.KeyboardSettings
import com.example.model.KeyboardTheme
import com.example.sound.KeyboardSoundEngine
import kotlinx.coroutines.launch

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
    onSwapLanguages: () -> Unit,
    onSelectSourceLang: (String) -> Unit,
    onSelectTargetLang: (String) -> Unit,
    onClose: () -> Unit
) {
    var showSourcePickerModal by remember { mutableStateOf(false) }
    var showTargetPickerModal by remember { mutableStateOf(false) }

    val allLangs = com.example.data.TranslationEngine.supportedLanguages
    val sourceItem = allLangs.find { it.code == sourceLang } ?: allLangs[0]
    val targetItem = allLangs.find { it.code == targetLang } ?: allLangs[1]

    val sourceLabel = "${sourceItem.flag} ${sourceItem.nameAr}"
    val targetLabel = "${targetItem.flag} ${targetItem.nameAr}"

    if (showSourcePickerModal || showTargetPickerModal) {
        var query by remember { mutableStateOf("") }
        val filtered = allLangs.filter {
            it.nameAr.contains(query, ignoreCase = true) ||
            it.nameEn.contains(query, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = {
                showSourcePickerModal = false
                showTargetPickerModal = false
            },
            title = {
                Text(
                    text = if (showSourcePickerModal) "لغة النص الأصلي (من)" else "لغة الترجمة الفورية (إلى)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.height(280.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("ابحث في جميع لغات العالم...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { langItem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (showSourcePickerModal) {
                                            onSelectSourceLang(langItem.code)
                                            showSourcePickerModal = false
                                        } else {
                                            onSelectTargetLang(langItem.code)
                                            showTargetPickerModal = false
                                        }
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(langItem.flag, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(langItem.nameAr, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("(${langItem.nameEn})", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSourcePickerModal = false
                    showTargetPickerModal = false
                }) {
                    Text("إلغاء", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(Color(theme.toolbarColor))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Close button (Compact harmonious circular X)
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Translation",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // Language selectors and Swap (Screenshot 20: [Target ▼] ⇄ [Source ▼])
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Source Language Selector
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier.clickable { showSourcePickerModal = true }
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
                    contentDescription = "Swap Languages",
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(11.dp)
                )
            }

            // Target Language Selector
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier.clickable { showTargetPickerModal = true }
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

        Text(
            text = "ترجمة فورية",
            color = Color(0xFF00E5FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// -------------------------------------------------------------
// 2. INLINE TEXT DECORATION BAR & POPUP (Matching Screenshots 18 & 19)
// -------------------------------------------------------------
@Composable
fun InlineDecorationBar(
    theme: KeyboardTheme,
    activeStyleId: String,
    onSelectStyle: (String) -> Unit,
    onClose: () -> Unit
) {
    var showStylesMenu by remember { mutableStateOf(false) }

    val activeItem = TextDecorator.allStyles.find { it.id == activeStyleId } ?: TextDecorator.allStyles[0]

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .background(Color(theme.toolbarColor))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close red X button (Compact & sleek)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Decoration",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
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
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeItem.preview,
                        color = Color(theme.keyTextColor),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (showStylesMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(theme.accentColor),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Text(
                text = "زخرفة حية",
                color = Color(theme.accentColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Floating / Expandable styles list matching Screenshot 18
        AnimatedVisibility(visible = showStylesMenu) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
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
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = style.preview,
                                    color = if (isSelected) Color(theme.accentColor) else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = style.nameAr,
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
    onItemInserted: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var items by remember { mutableStateOf(prefs.getClipboardItems(forceRefresh = true)) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        prefs.syncWithSystemClipboard(context)
        items = prefs.getClipboardItems(forceRefresh = true)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Top Toolbar of Clipboard:
        // [⚙️ Settings] [🗑️ Clear] [📋 Paste] [✂️ Cut] [❌ Close]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color(theme.toolbarColor), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Settings
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(theme.keyPressedColor).copy(alpha = 0.5f))
                        .clickable { /* Settings handled in main */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color(theme.keyTextColor).copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
                }

                // Delete all non-pinned
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                        .clickable {
                            prefs.clearClipboardHistory()
                            items = prefs.getClipboardItems()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Clear History", tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                }

                // Copy all / current
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(theme.accentColor).copy(alpha = 0.15f))
                        .clickable {
                            if (items.isNotEmpty()) {
                                onItemInserted(items[0].text)
                                onClose() // Auto-close upon paste
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste Recent", tint = Color(theme.accentColor), modifier = Modifier.size(12.dp))
                }

                // Cut icon
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(theme.keyPressedColor).copy(alpha = 0.5f))
                        .clickable {
                            if (items.isNotEmpty()) {
                                val first = items[0]
                                onItemInserted(first.text)
                                prefs.deleteClipboardItem(first.id)
                                items = prefs.getClipboardItems()
                                onClose() // Auto-close
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ContentCut, contentDescription = "Cut/Pop", tint = Color(theme.keyTextColor).copy(alpha = 0.85f), modifier = Modifier.size(12.dp))
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
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "الحافظة فارغة (يتم حفظ كل ما تنسخه تلقائياً للأبد)",
                    color = Color(theme.subtextColor),
                    fontSize = 12.sp
                )
            }
        } else {
            // 2-Column Grid matching Screenshot 21!
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
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
                            .height(68.dp)
                            .clickable {
                                // 1. Paste text
                                onItemInserted(clip.text)
                                // 2. CLOSE CLIPBOARD IMMEDIATELY ("واغلاق الحافظة عند اللصق")
                                onClose()
                            }
                    ) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = clip.text,
                                color = Color(theme.keyTextColor),
                                fontSize = 11.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 15.sp,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Pin icon indicator
                            if (clip.isPinned) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = Color(theme.accentColor),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.BottomEnd)
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
// 4. EMOJI PICKER VIEW (مع قسم الإيموجي المستعملة 🕒 وإيموجي آيفون وأندرويد)
// -------------------------------------------------------------
@Composable
fun EmojiPickerView(
    theme: KeyboardTheme,
    onEmojiSelected: (String) -> Unit,
    onSpacePressed: () -> Unit = {},
    onDeletePressed: () -> Unit = {},
    onEnterPressed: () -> Unit = {},
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val recentEmojiManager = remember { com.example.data.RecentEmojiManager(context) }
    var recentEmojisList by remember { mutableStateOf(recentEmojiManager.getRecentEmojis()) }

    // -1 = Recent Emojis 🕒, 0..N = Standard Categories
    var selectedCategoryIndex by remember { mutableIntStateOf(-1) }
    var searchQuery by remember { mutableStateOf("") }
    var isIosStyle by remember { mutableStateOf(true) }

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
            .height(265.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        // Top search & style toggle & close row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean non-clipping Search Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(theme.keyBackgroundColor))
                    .border(1.dp, Color(theme.borderColor).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(theme.subtextColor),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "بحث في الإيموجي...",
                                color = Color(theme.subtextColor).copy(alpha = 0.7f),
                                fontSize = 11.5.sp
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
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
                            contentDescription = "Clear",
                            tint = Color(theme.subtextColor),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { searchQuery = "" }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // iOS / Android emoji style toggle
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isIosStyle) Color(0xFF0A84FF).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, if (isIosStyle) Color(0xFF0A84FF) else Color(theme.borderColor)),
                modifier = Modifier.clickable { isIosStyle = !isIosStyle }
            ) {
                Text(
                    text = if (isIosStyle) "🍏 آيفون" else "🤖 أندرويد",
                    color = if (isIosStyle) Color(0xFF0A84FF) else Color(theme.keyTextColor),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Emoji picker close button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Categories Tab: 🕒 Recent first, then normal categories
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
                            text = "المستعملة",
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

        // Grid of emojis
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            items(currentEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            recentEmojiManager.addEmoji(emoji)
                            recentEmojisList = recentEmojiManager.getRecentEmojis()
                            onEmojiSelected(emoji)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 21.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bottom Navigation Bar (Return to letters, Space, Delete, Send)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Return to Alphabet Keyboard button (⌨️ أحرف / ABC)
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
                        text = "⌨️ أحرف",
                        color = Color(theme.accentColor),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Spacebar
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier
                    .weight(2.7f)
                    .fillMaxHeight()
                    .clickable { onSpacePressed() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("مسافة", color = Color(theme.keyTextColor), fontSize = 11.sp)
                }
            }

            // 3. Delete button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha)),
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clickable { onDeletePressed() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Delete",
                        tint = Color(theme.keyTextColor),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 4. Send / Enter button (Sends and automatically returns to regular letters keyboard!)
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
                    Text("إرسال", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
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
    onGifSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val quickGifs = listOf(
        "(^_^)/", "(o_O)", "(•‿•)", "(╯°□°)╯", "¯\\_(ツ)_/¯",
        "(づ￣ ³￣)づ", "(♥_♥)", "(T_T)", "(¬_¬)", "ᕙ(⇀‸↼‶)ᕗ",
        "₍ᐢ•ﻌ•ᐢ₎", "(⁠◍⁠•⁠ᴗ⁠•⁠◍⁠)", "(⁠｡⁠♡⁠‿⁠♡⁠｡⁠)", "ʕ•ᴥ•ʔ", "(>_<)"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("رموز Kaomoji و GIF سريعة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
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
                        .height(48.dp)
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
// 6. MEDIA & PHOTOS PICKER VIEW (Requested: "مثل الصور والملصقات")
// -------------------------------------------------------------
@Composable
fun MediaPickerView(
    theme: KeyboardTheme,
    onMediaSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("🖼️ الملصقات والصور", "🎬 GIFs متحركة", "🎭 فيسات Kaomoji", "🎨 نصوص فنية")

    val stickers = listOf(
        "صباح الخير 🌸", "مساء الورد 🌹", "جمعة مباركة ✨", "ألف مبروك 🎉",
        "شكراً جزيلاً 🙏", "تحياتي يا بطل 👑", "ما شاء الله 💫", "بالتوفيق والنجاح 🌟",
        "تسلم إيدك 👍", "هههههههههه 😂", "أهلاً وسهلاً 👋", "نورتنا يا غالي 💖",
        "الله يسعدك 🤍", "كل عام وأنتم بخير 🌙", "عيدكم مبارك 🎈", "الحمد لله دائماً 🤲"
    )

    val gifMemes = listOf(
        "🤣 [GIF: ضحك هستيري]", "👏 [GIF: تصفيق حار]", "❤️ [GIF: قلوب تتطاير]",
        "🔥 [GIF: حماس ونار]", "😲 [GIF: صدمة مضحكة]", "👋 [GIF: تحية ملكية]",
        "😎 [GIF: نظارة كول]", "🕺 [GIF: رقصة فرح]", "💤 [GIF: نعاس وتعب]",
        "🥳 [GIF: احتفال صاخب]", "💪 [GIF: عضلات وقوة]", "🎯 [GIF: إصابة مباشرة]"
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
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
                text = "الصور والملصقات والوسائط",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
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
                        color = if (isSelected) Color(theme.accentColor) else Color(theme.keyTextColor),
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
                                color = Color.White,
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
                                    color = Color(theme.keyTextColor),
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
                                    color = Color(theme.keyTextColor),
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
// 7. AI TONE & ASSISTANT DRAWER (Requested: "زي كيبورد ذكاء اصطناعي وتغير نبرة الكتابة وتصحح الأخطاء")
// -------------------------------------------------------------
@Composable
fun AiToneDrawer(
    theme: KeyboardTheme,
    currentText: String,
    onReplaceText: (String) -> Unit,
    onAddWordToDictionary: (String) -> Unit,
    onClose: () -> Unit
) {
    var selectedToneId by remember { mutableStateOf("formal") }
    var generatedText by remember {
        mutableStateOf(
            if (currentText.isNotBlank()) com.example.data.AiToneEngine.transformTone(currentText, "formal")
            else "السلام عليكم ورحمة الله، أود التواصل معكم بخصوص هذا الموضوع، شاكرين ومقدرين حسن تعاونكم."
        )
    }
    var statusMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .background(Color(0xFF0F141F))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "مساعد الذكاء الاصطناعي وتغيير النبرة",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tone Selector Pills (Horizontal Scroll)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(com.example.data.AiToneEngine.allTones) { tone ->
                val isSelected = tone.id == selectedToneId
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF1A2233),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF00E5FF) else Color(0xFF28364F)
                    ),
                    modifier = Modifier.clickable {
                        selectedToneId = tone.id
                        generatedText = com.example.data.AiToneEngine.transformTone(
                            if (currentText.isNotBlank()) currentText else "",
                            tone.id
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tone.icon, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tone.nameAr,
                            color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick AI Action Chips (Spell check, Harakat, Rephrase)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. تدقيق إملائي فوري
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF152A28),
                border = BorderStroke(1.dp, Color(0xFF10B981)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val corrected = com.example.data.SpellCheckDictionary.autoCorrectSentence(
                            if (currentText.isNotBlank()) currentText else generatedText,
                            true
                        )
                        generatedText = corrected
                        statusMessage = "تم التدقيق والتصحيح الإملائي بنجاح ✓"
                    }
            ) {
                Box(modifier = Modifier.padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                    Text("✓ تدقيق وتصحيح", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 2. تشكيل الحركات
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF261D36),
                border = BorderStroke(1.dp, Color(0xFFA855F7)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val withTashkeel = com.example.data.AiToneEngine.addAutomaticTashkeel(
                            if (currentText.isNotBlank()) currentText else generatedText
                        )
                        generatedText = withTashkeel
                        statusMessage = "تم إضافة التشكيل التلقائي ✓"
                    }
            ) {
                Box(modifier = Modifier.padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                    Text("✨ تشكيل الحركات", color = Color(0xFFC084FC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // 3. إضافة للقاموس
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1D263B),
                border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val wordToAdd = currentText.trim().split(" ").lastOrNull() ?: currentText.trim()
                        if (wordToAdd.isNotBlank()) {
                            onAddWordToDictionary(wordToAdd)
                            statusMessage = "تم حفظ '$wordToAdd' في قاموسك الشخصي ✓"
                        }
                    }
            ) {
                Box(modifier = Modifier.padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
                    Text("➕ حفظ بالقاموس", color = Color(0xFF60A5FA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Result Card & Preview
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF161F2E),
            border = BorderStroke(1.dp, Color(0xFF2A3A54)),
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
                Text(
                    text = generatedText,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (statusMessage.isNotEmpty()) {
                        Text(
                            text = statusMessage,
                            color = Color(0xFF00E5FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Replace Button
                    Button(
                        onClick = {
                            onReplaceText(generatedText)
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "استبدال النص ✓",
                            color = Color.Black,
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
    onInsertText: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("اضغط على المايك للتحدث...") }
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
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isArabic) "ar-SA" else "en-US")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        statusText = "جاري الاستماع... تحدث الآن بصوت واضح"
                    }
                    override fun onBeginningOfSpeech() {
                        statusText = "جاري التقاط الصوت..."
                    }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                        statusText = "تم التقاط الصوت! اضغط إدراج"
                    }
                    override fun onError(error: Int) {
                        isListening = false
                        statusText = "اضغط على المايك أو اختر عبارة جاهزة"
                    }
                    override fun onResults(results: Bundle?) {
                        isListening = false
                        val matches = results?.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            recognizedText = matches[0]
                            statusText = "تم التعرف بنجاح ✓"
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
                statusText = "جاري فتح الميكروفون..."
            } else {
                statusText = "اختر من العبارات السريعة أدناه أو تحدث"
            }
        } catch (e: Exception) {
            isListening = false
            statusText = "تحدث أو اضغط على إحدى العبارات:"
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .background(Color(theme.backgroundColor))
            .padding(12.dp),
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الكتابة بالصوت (Voice Typing)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
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
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Animated Mic & Status
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size((56 * pulseScale).dp)
                    .clip(CircleShape)
                    .background(
                        if (isListening) Color(theme.accentColor).copy(alpha = 0.25f)
                        else Color(0xFF232B3B)
                    )
                    .clickable {
                        if (isListening) stopListening() else startListening()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Mic else Icons.Outlined.MicNone,
                    contentDescription = "Mic",
                    tint = if (isListening) Color(theme.accentColor) else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = statusText,
                color = if (isListening) Color(theme.accentColor) else Color(theme.subtextColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            if (recognizedText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$recognizedText\"",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Quick phrases chips + Actions
        Column(modifier = Modifier.fillMaxWidth()) {
            val samplePhrases = if (isArabic) {
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
                            .background(Color(0xFF1E2638))
                            .border(1.dp, Color(theme.borderColor).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                recognizedText = phrase
                                onInsertText(phrase + " ")
                                onClose()
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(text = phrase, color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        stopListening()
                        onClose()
                    },
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(theme.borderColor))
                ) {
                    Text("إلغاء", color = Color(theme.subtextColor), fontSize = 12.sp)
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
                    modifier = Modifier.weight(1.5f).height(38.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("إدراج النص ✓", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    onUpdateSettings: (KeyboardSettings) -> Unit,
    onOpenFullSettings: () -> Unit,
    onCustomizeToolbar: () -> Unit = {},
    onClose: () -> Unit
) {
    val isLight = remember(theme.backgroundColor) { isLightColor(theme.backgroundColor) }
    val textColor = if (isLight) Color(0xFF0F172A) else Color.White
    val subTextColor = if (isLight) Color(0xFF475569) else Color(0xFF8E9BAE)
    val rowBg = if (isLight) Color(theme.keyBackgroundColor) else Color(0xFF141924)
    val rowBorder = if (isLight) Color(theme.borderColor).copy(alpha = 0.6f) else Color(0xFF26334A).copy(alpha = 0.4f)
    val accentCol = Color(theme.accentColor)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(255.dp)
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
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إعدادات الكيبورد السريعة",
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
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
            // 1. صوت المفاتيح ونوع الصوت
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
                                Text("أصوات المفاتيح المخصصة", color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("نغمة واقعية عند لمس كل حرف", color = subTextColor, fontSize = 9.sp)
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
                                        text = prof.nameAr,
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

            // 1.1 سرعة الكتابة واستجابة الأزرار
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
                                Text("سرعة وثقل استجابة المفاتيح", color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    when (settings.typingSpeedMode) {
                                        "fast" -> "⚡ فائق السرعة واستجابة فورية"
                                        "smooth" -> "👌 خفيف وثقيل متزن (مثل كيبورد الفيديو)"
                                        "slow" -> "🧘 هادئ وثقيل لعدم الخطأ"
                                        else -> "🍏 متوسط متوازن ومريح"
                                    },
                                    color = accentCol,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Triple("⚡ سريع", "fast", Pair(25, 200)),
                            Triple("👌 خفيف/ثقيل", "smooth", Pair(35, 300)),
                            Triple("🍏 متوازن", "medium", Pair(45, 380)),
                            Triple("🧘 هادئ", "slow", Pair(70, 520))
                        ).forEach { (lbl, mode, timings) ->
                            val isSel = settings.typingSpeedMode == mode
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
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 8.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
            }

            // 2. اهتزاز المفاتيح
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
                                Text("الاهتزاز اللمسي (${settings.vibrationDurationMs}ms)", color = textColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("اهتزاز ملموس ومريح عند الضغط", color = subTextColor, fontSize = 9.sp)
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
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(Pair("خفيف", 15), Pair("متوسط آيفون 16", 30), Pair("قوي", 45)).forEach { (lbl, dur) ->
                                val isSel = settings.vibrationDurationMs == dur
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSel) accentCol else if (isLight) Color(0xFFF1F5F9) else Color(0xFF1E2838),
                                    modifier = Modifier.weight(1f).clickable {
                                        onUpdateSettings(settings.copy(vibrationDurationMs = dur))
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(lbl, color = if (isSel) Color.Black else textColor, fontSize = 9.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. معاينة الحرف عند اللمس
            item {
                QuickSettingToggleRow(
                    title = "معاينة الحرف العائم (Preview Bubble)",
                    subtitle = "ظهور فقاعة تكبير الحرف فوق الزر عند اللمس",
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

            // 4. صف الأرقام العلوي
            item {
                QuickSettingToggleRow(
                    title = "صف الأرقام العلوي المباشر",
                    subtitle = "عرض شريط الأرقام بشكل دائم فوق الحروف",
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

            // 5. شريط الاقتراحات والإكمال الذكي
            item {
                QuickSettingToggleRow(
                    title = "شريط الاقتراحات الذكية",
                    subtitle = "توقع الكلمات التالية والتصحيح الإملائي",
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

            // 6. تخصيص شريط الأدوات العلوي
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
                            Text("تخصيص شريط الأدوات العلوي ⚙️", color = textColor, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Text("تقليل الأيقونات وإخفاء الأدوات غير المستخدمة", color = subTextColor, fontSize = 9.sp)
                        }
                    }
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = accentCol, modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Full Settings Launcher Button
        Button(
            onClick = {
                onOpenFullSettings()
                onClose()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = accentCol
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "فتح إعدادات التطبيق الكاملة ⚙️",
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
    onPhraseSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val categories = com.example.data.DecoratedPhrasesData.categories
    var selectedCatId by remember { mutableStateOf(categories.first().id) }
    val currentCategory = categories.find { it.id == selectedCatId } ?: categories.first()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFF141926), RoundedCornerShape(8.dp))
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
                            text = "${cat.icon} ${cat.nameAr}",
                            color = if (isSelected) Color(theme.accentColor) else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Phrases Grid
        androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
            columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phrase,
                            color = Color(theme.keyTextColor),
                            fontSize = 12.sp,
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
    onInsertText: (String) -> Unit,
    onClose: () -> Unit
) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    fun evaluate(expr: String): String {
        return try {
            if (expr.isBlank()) return ""
            val cleanExpr = expr.replace("×", "*").replace("÷", "/")
            // Simple arithmetic evaluator
            val res = evaluateSimpleMath(cleanExpr)
            if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res).trimEnd('0').trimEnd('.')
        } catch (_: Exception) {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Screen display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFF0F1420), RoundedCornerShape(8.dp))
                .border(1.dp, Color(theme.borderColor).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = expression.ifEmpty { "0" },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                if (result.isNotEmpty()) {
                    Text(
                        text = "= $result",
                        color = Color(theme.accentColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (result.isNotEmpty()) {
                Button(
                    onClick = {
                        onInsertText(result)
                        onClose()
                    },
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor))
                ) {
                    Text("إدراج الناتج ($result)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        onInsertText("$expression = $result")
                        onClose()
                    },
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("إدراج المعادلة كاملة", color = Color(theme.accentColor), fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calc grid
        val buttons = listOf(
            listOf("C", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=", "⌫")
        )

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
                            isOp -> Color(0xFF1E2838)
                            isAction -> Color(0xFF2D1820)
                            else -> Color(theme.keyBackgroundColor)
                        },
                        border = BorderStroke(0.8.dp, Color(theme.borderColor).copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = btn,
                                color = when {
                                    btn == "=" -> Color.White
                                    isOp -> Color(theme.accentColor)
                                    isAction -> Color(0xFFFF5252)
                                    else -> Color(theme.keyTextColor)
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
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
    val description: String
)

val allKeyboardToolbarTools = listOf(
    ToolbarToolDefinition("stickers", "ملصقات وستيكرات", Icons.Outlined.AutoAwesomeMosaic, "تصميم وإرسال الملصقات العربية الجاهزة والمصممة"),
    ToolbarToolDefinition("translate", "ترجمة فورية", Icons.Default.Translate, "ترجمة فورية للنصوص بجميع لغات العالم"),
    ToolbarToolDefinition("clipboard", "حافظة النصوص", Icons.Outlined.ContentPaste, "حفظ النصوص المنسوخة والرجوع لها للأبد"),
    ToolbarToolDefinition("decoration", "زخرفة النصوص", Icons.Outlined.AutoAwesome, "زخرفة الكلمات والخطوط والعبارات الحية"),
    ToolbarToolDefinition("phrases", "كليشات وعبارات", Icons.Outlined.FavoriteBorder, "عبارات ترحيب وإسلامية ونصوص جاهزة"),
    ToolbarToolDefinition("calculator", "آلة حاسبة", Icons.Outlined.Calculate, "حاسبة رياضية فورية وكتابة النتيجة"),
    ToolbarToolDefinition("emoji", "لوحة الإيموجي", Icons.Outlined.Mood, "إيموجي آيفون وأندرويد وقسم المستعملة"),
    ToolbarToolDefinition("voice", "كتابة صوتية", Icons.Default.Mic, "تحويل الصوت المباشر إلى نصوص مكتوبة"),
    ToolbarToolDefinition("ai", "ذكاء واصطناع", Icons.Outlined.Psychology, "تغيير نبرة الكلام والمساعد الذكي"),
    ToolbarToolDefinition("photos", "صور وملصقات", Icons.Outlined.PhotoLibrary, "ملصقات وستيكرات وصور مجهزة"),
    ToolbarToolDefinition("gif", "Kaomoji و GIF", Icons.Outlined.Gif, "فيسات يابانية وصور متحركة سريعة"),
    ToolbarToolDefinition("settings", "إعدادات التطبيق", Icons.Outlined.Settings, "الوصول السريع لكافة تخصيصات الكيبورد")
)

@Composable
fun CustomizeToolbarDrawer(
    theme: KeyboardTheme,
    visibleTools: List<String>,
    onUpdateTools: (List<String>) -> Unit,
    onClose: () -> Unit
) {
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
            .height(265.dp)
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
                    text = "تخصيص شريط الأدوات العلوي",
                    color = primaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Quick presets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Preset 1: شريط مختصر
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
                    Text("⚡ مختصر (5)", color = Color(theme.accentColor), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Preset 2: كتابة وترجمة
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
                    Text("📝 كتابة (5)", color = primaryText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Preset 3: كامل الأدوات
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
                    Text("🌟 الكل (12)", color = primaryText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tools List with Checkboxes
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
                                    text = tool.nameAr,
                                    color = if (isChecked) primaryText else secondaryText,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = tool.description,
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

