package com.example.keyboard

import androidx.compose.animation.*
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.model.KeyboardTheme
import kotlinx.coroutines.launch

@Composable
fun EmojiPickerView(
    theme: KeyboardTheme,
    onEmojiSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val currentEmojis = remember(selectedCategoryIndex, searchQuery) {
        if (searchQuery.isNotBlank()) {
            EmojiData.categories.flatMap { it.emojis }
                .distinct()
                .filter { it.contains(searchQuery.trim()) }
        } else {
            EmojiData.categories[selectedCategoryIndex].emojis
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Top search & close row
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
                    .height(46.dp),
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

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
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

        Spacer(modifier = Modifier.height(6.dp))

        // Categories bar
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(EmojiData.categories.indices.toList()) { index ->
                val cat = EmojiData.categories[index]
                val isSelected = index == selectedCategoryIndex && searchQuery.isBlank()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f)
                            else Color(theme.keyBackgroundColor)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(theme.accentColor) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            searchQuery = ""
                            selectedCategoryIndex = index
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cat.icon, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 38.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            items(currentEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

@Composable
fun GifPickerView(
    theme: KeyboardTheme,
    onGifSelected: (String) -> Unit,
    onClose: () -> Unit
) {
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
            Text(
                text = "ملصقات و GIFs متحركة",
                color = Color(theme.keyTextColor),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(theme.keyBackgroundColor), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(theme.keyTextColor),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(EmojiData.sampleGifs) { gif ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(theme.keyBackgroundColor),
                                    Color(theme.borderColor)
                                )
                            )
                        )
                        .border(1.dp, Color(theme.accentColor).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            onGifSelected("${gif.title} ${gif.emojiFallback}")
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = gif.emojiFallback, fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gif.title,
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

@Composable
fun QuickTranslateView(
    theme: KeyboardTheme,
    currentText: String,
    onInsertTranslated: (String) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf(currentText) }
    var translatedResult by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var sourceLang by remember { mutableStateOf("ar") }
    var targetLang by remember { mutableStateOf("en") }

    fun doTranslate() {
        if (inputText.isBlank()) return
        isTranslating = true
        coroutineScope.launch {
            val res = TranslationEngine.translate(inputText, sourceLang, targetLang)
            translatedResult = res
            isTranslating = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(12.dp)
    ) {
        // Top row: language selector and swap
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor))
                ) {
                    Text(
                        text = if (sourceLang == "ar") "العربية" else "English",
                        color = Color(theme.accentColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val temp = sourceLang
                        sourceLang = targetLang
                        targetLang = temp
                        if (translatedResult.isNotBlank()) {
                            inputText = translatedResult
                            translatedResult = ""
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Swap Languages",
                        tint = Color(theme.accentColor)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor))
                ) {
                    Text(
                        text = if (targetLang == "en") "English" else "العربية",
                        color = Color(theme.accentColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(theme.keyBackgroundColor), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(theme.keyTextColor),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input text field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text("اكتب النص المراد ترجمته...", color = Color(theme.subtextColor), fontSize = 12.sp)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(theme.accentColor),
                    unfocusedBorderColor = Color(theme.borderColor),
                    focusedTextColor = Color(theme.keyTextColor),
                    unfocusedTextColor = Color(theme.keyTextColor)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = { doTranslate() },
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(theme.accentColor)
                )
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("ترجم", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Result box
        AnimatedVisibility(visible = translatedResult.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(theme.keyBackgroundColor),
                border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = translatedResult,
                        color = Color(theme.keyTextColor),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = { onInsertTranslated(translatedResult) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(theme.enterButtonColor)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("إدراج", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = Color(theme.accentColor),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "حافظة النصوص (${filteredItems.size})",
                    color = Color(theme.keyTextColor),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Row {
                TextButton(
                    onClick = {
                        prefs.clearClipboardHistory()
                        items = prefs.getClipboardItems()
                    }
                ) {
                    Text("مسح غير المثبت", color = Color(theme.subtextColor), fontSize = 11.sp)
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(30.dp)
                        .background(Color(theme.keyBackgroundColor), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(theme.keyTextColor),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "الحافظة فارغة حالياً",
                    color = Color(theme.subtextColor),
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredItems, key = { it.id }) { clip ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(theme.keyBackgroundColor),
                        border = BorderStroke(
                            1.dp,
                            if (clip.isPinned) Color(theme.accentColor) else Color(theme.borderColor)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemInserted(clip.text) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = clip.text,
                                color = Color(theme.keyTextColor),
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        prefs.togglePinClipboard(clip.id)
                                        items = prefs.getClipboardItems()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (clip.isPinned) Icons.Default.PushPin else Icons.Default.OutlinedFlag,
                                        contentDescription = "Pin",
                                        tint = if (clip.isPinned) Color(theme.accentColor) else Color(theme.subtextColor),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        prefs.deleteClipboardItem(clip.id)
                                        items = prefs.getClipboardItems()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = Color(theme.subtextColor),
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
}

@Composable
fun DecorationDrawer(
    theme: KeyboardTheme,
    sampleText: String,
    onApplyDecoratedText: (String) -> Unit,
    onClose: () -> Unit
) {
    var isArabicTab by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf(if (sampleText.isNotBlank()) sampleText else "كيبورد برو") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(theme.backgroundColor))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = { isArabicTab = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isArabicTab) Color(theme.accentColor) else Color(theme.keyBackgroundColor)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "زخرفة عربية",
                        color = if (isArabicTab) Color.Black else Color(theme.keyTextColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        isArabicTab = false
                        if (inputText == "كيبورد برو") inputText = "Turbo Keyboard"
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isArabicTab) Color(theme.accentColor) else Color(theme.keyBackgroundColor)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "English Styles",
                        color = if (!isArabicTab) Color.Black else Color(theme.keyTextColor),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(theme.keyBackgroundColor), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(theme.keyTextColor),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        val styles = if (isArabicTab) TextDecorator.arabicStyles else TextDecorator.englishStyles

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(styles) { style ->
                val transformed = style.transform(inputText)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(theme.keyBackgroundColor),
                    border = BorderStroke(1.dp, Color(theme.borderColor)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onApplyDecoratedText(transformed) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transformed,
                                color = Color(theme.keyTextColor),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = style.nameAr,
                                color = Color(theme.subtextColor),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { onApplyDecoratedText(transformed) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(theme.enterButtonColor)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("إدراج", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
