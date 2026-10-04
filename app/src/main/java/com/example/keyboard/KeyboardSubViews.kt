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
import kotlinx.coroutines.launch

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
            .height(44.dp)
            .background(Color(0xFF161C28))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Close button (Red circular X like Screenshot 20)
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444).copy(alpha = 0.85f))
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
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Source Language Selector
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF222B3D),
                border = BorderStroke(1.dp, Color(0xFF32415C)),
                modifier = Modifier.clickable { showSourcePickerModal = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sourceLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            // Swap icon ⇄
            IconButton(
                onClick = onSwapLanguages,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222B3D))
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "Swap Languages",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Target Language Selector
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF222B3D),
                border = BorderStroke(1.dp, Color(0xFF32415C)),
                modifier = Modifier.clickable { showTargetPickerModal = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(targetLabel, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
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
                .height(44.dp)
                .background(Color(0xFF161C28))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Close red X button (Screenshot 19)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.85f))
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
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF222B3D),
                border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.5f)),
                modifier = Modifier.clickable { showStylesMenu = !showStylesMenu }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activeItem.preview,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (showStylesMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(theme.accentColor),
                        modifier = Modifier.size(18.dp)
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
    var items by remember { mutableStateOf(prefs.getClipboardItems()) }
    var searchQuery by remember { mutableStateOf("") }

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

    // Continuously save scroll position as user scrolls so upon reopening it returns to exact position
    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        prefs.setLastClipboardScrollIndex(gridState.firstVisibleItemIndex)
        prefs.setLastClipboardScrollOffset(gridState.firstVisibleItemScrollOffset)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(0xFF0F141E))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Top Toolbar of Clipboard matching Screenshot 21:
        // [⚙️ Settings] [🗑️ Clear] [📋 Paste] [✂️ Cut] [❌ Close]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(Color(0xFF161E2E), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Settings
                IconButton(
                    onClick = { /* Settings handled in main */ },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }

                // Delete all non-pinned
                IconButton(
                    onClick = {
                        prefs.clearClipboardHistory()
                        items = prefs.getClipboardItems()
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Clear History", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                }

                // Copy all / current
                IconButton(
                    onClick = {
                        // Quick info / paste first
                        if (items.isNotEmpty()) {
                            onItemInserted(items[0].text)
                            onClose() // Auto-close upon paste
                        }
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Outlined.ContentPaste, contentDescription = "Paste Recent", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                }

                // Cut icon (Screenshot 21)
                IconButton(
                    onClick = {
                        if (items.isNotEmpty()) {
                            val first = items[0]
                            onItemInserted(first.text)
                            prefs.deleteClipboardItem(first.id)
                            items = prefs.getClipboardItems()
                            onClose() // Auto-close
                        }
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Outlined.ContentCut, contentDescription = "Cut/Pop", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }

            // Close Button ❌
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222B3D))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "الحافظة فارغة (يتم حفظ كل ما تنسخه تلقائياً للأبد)",
                    color = Color(0xFF8E9BAE),
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
                        color = Color(0xFF192233),
                        border = BorderStroke(
                            1.dp,
                            if (clip.isPinned) Color(0xFF00E5FF) else Color(0xFF28364F)
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
                                color = Color.White,
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
                                    tint = Color(0xFF00E5FF),
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
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Top search & style toggle & close row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("بحث في الإيموجي...", color = Color(theme.subtextColor), fontSize = 12.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(theme.accentColor),
                    unfocusedBorderColor = Color(theme.borderColor),
                    focusedTextColor = Color(theme.keyTextColor),
                    unfocusedTextColor = Color(theme.keyTextColor)
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(theme.subtextColor),
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.width(6.dp))

            // iOS / Android emoji style toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isIosStyle) Color(0xFF0A84FF).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, if (isIosStyle) Color(0xFF0A84FF) else Color(theme.borderColor)),
                modifier = Modifier.clickable { isIosStyle = !isIosStyle }
            ) {
                Text(
                    text = if (isIosStyle) "🍏 آيفون" else "🤖 أندرويد",
                    color = if (isIosStyle) Color(0xFF0A84FF) else Color(theme.keyTextColor),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(34.dp)
                    .background(Color(theme.keyBackgroundColor), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(theme.keyTextColor),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Categories Tab: 🕒 Recent first, then normal categories
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recent Emojis 🕒 Tab
            item {
                val isRecentSelected = selectedCategoryIndex == -1
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isRecentSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                    border = BorderStroke(
                        1.dp,
                        if (isRecentSelected) Color(theme.accentColor) else Color(theme.borderColor)
                    ),
                    modifier = Modifier.clickable { selectedCategoryIndex = -1 }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🕒", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "المستعملة",
                            color = if (isRecentSelected) Color(theme.accentColor) else Color(theme.keyTextColor),
                            fontSize = 10.sp,
                            fontWeight = if (isRecentSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            items(EmojiData.categories.indices.toList()) { index ->
                val cat = EmojiData.categories[index]
                val isSelected = index == selectedCategoryIndex
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f) else Color(theme.keyBackgroundColor),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(theme.accentColor) else Color(theme.borderColor)
                    ),
                    modifier = Modifier.clickable { selectedCategoryIndex = index }
                ) {
                    Text(
                        text = cat.icon,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Grid of emojis
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(currentEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            recentEmojiManager.addEmoji(emoji)
                            recentEmojisList = recentEmojiManager.getRecentEmojis()
                            onEmojiSelected(emoji)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
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
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
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
            IconButton(onClick = onClose, modifier = Modifier.size(30.dp)) {
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(theme.keyBackgroundColor),
                            border = BorderStroke(1.dp, Color(theme.borderColor)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clickable {
                                    onMediaSelected(sticker)
                                    onClose()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(4.dp)) {
                                Text(
                                    text = sticker,
                                    color = Color(theme.keyTextColor),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E283A))
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
            IconButton(onClick = {
                stopListening()
                onClose()
            }, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(theme.subtextColor),
                    modifier = Modifier.size(18.dp)
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
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 12.dp, vertical = 8.dp)
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
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "إعدادات الكيبورد السريعة",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(theme.subtextColor),
                    modifier = Modifier.size(18.dp)
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
            // 1. معاينة الحرف عند اللمس
            item {
                QuickSettingToggleRow(
                    title = "معاينة الحرف عند الضغط (Preview Bubble)",
                    subtitle = "ظهور فقاعة تكبير الحرف فوق الزر عند اللمس",
                    icon = Icons.Outlined.ZoomIn,
                    checked = settings.keyPopupEnabled,
                    accentColor = Color(theme.accentColor),
                    onCheckedChange = { onUpdateSettings(settings.copy(keyPopupEnabled = it)) }
                )
            }

            // 2. اهتزاز المفاتيح
            item {
                QuickSettingToggleRow(
                    title = "اهتزاز المفاتيح (Haptic Feedback)",
                    subtitle = "تفعيل الاهتزاز اللمسي الخفيف عند الضغط",
                    icon = Icons.Outlined.Vibration,
                    checked = settings.vibrationEnabled,
                    accentColor = Color(theme.accentColor),
                    onCheckedChange = { onUpdateSettings(settings.copy(vibrationEnabled = it)) }
                )
            }

            // 3. صف الأرقام العلوي
            item {
                QuickSettingToggleRow(
                    title = "صف الأرقام العلوي المباشر",
                    subtitle = "عرض شريط الأرقام بشكل دائم فوق الحروف",
                    icon = Icons.Outlined.Pin,
                    checked = settings.numberRowEnabled,
                    accentColor = Color(theme.accentColor),
                    onCheckedChange = { onUpdateSettings(settings.copy(numberRowEnabled = it)) }
                )
            }

            // 4. شريط الاقتراحات والإكمال الذكي
            item {
                QuickSettingToggleRow(
                    title = "شريط الاقتراحات الذكية",
                    subtitle = "توقع الكلمات التالية والتصحيح الإملائي",
                    icon = Icons.Outlined.AutoFixHigh,
                    checked = settings.suggestionsEnabled,
                    accentColor = Color(theme.accentColor),
                    onCheckedChange = { onUpdateSettings(settings.copy(suggestionsEnabled = it)) }
                )
            }

            // 5. صوت المفاتيح
            item {
                QuickSettingToggleRow(
                    title = "صوت النقر على المفاتيح",
                    subtitle = "إصدار نغمة نقر خفيفة عند الكتابة",
                    icon = Icons.Outlined.VolumeUp,
                    checked = settings.soundEnabled,
                    accentColor = Color(theme.accentColor),
                    onCheckedChange = { onUpdateSettings(settings.copy(soundEnabled = it)) }
                )
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
                containerColor = Color(theme.accentColor)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(38.dp)
        ) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "فتح إعدادات التطبيق الكاملة ⚙️",
                color = Color.Black,
                fontSize = 12.sp,
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
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141924))
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
                tint = if (checked) accentColor else Color(0xFF8E9BAE),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF8E9BAE),
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

