package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.model.KeyboardTheme
import com.example.model.ThemePresets

@Composable
fun ThemesScreen(
    prefs: PreferencesManager,
    currentThemeId: String,
    onThemeApplied: (KeyboardTheme) -> Unit,
    onOpenCustomBuilder: () -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }
    var previewLanguageIsArabic by remember { mutableStateOf(false) }
    var customThemes by remember { mutableStateOf(prefs.getCustomThemes()) }

    val categories = listOf(
        "الكل",
        "أنظمة وهواتف",
        "داكن وفاتح",
        "نيون وسايبر",
        "فخم وراقي",
        "تدرجات لونية",
        "زجاجي شفاف",
        "ألعاب وجيمينج",
        "ألوان ومرح",
        "مينيمال",
        "مخصص"
    )

    val allThemes = remember(customThemes) {
        val presets = ThemePresets.allPresets
        customThemes + presets
    }

    val filteredThemes = remember(allThemes, searchQuery, selectedCategory) {
        allThemes.filter { theme ->
            val matchQuery = searchQuery.isBlank() ||
                    theme.nameAr.contains(searchQuery.trim(), ignoreCase = true) ||
                    theme.nameEn.contains(searchQuery.trim(), ignoreCase = true)

            val matchCategory = when (selectedCategory) {
                "الكل" -> true
                "أنظمة وهواتف" -> theme.category == "systems"
                "داكن وفاتح" -> theme.category == "dark_light"
                "نيون وسايبر" -> theme.category == "neon"
                "تدرجات لونية" -> theme.category == "gradient"
                "زجاجي شفاف" -> theme.category == "glass"
                "ألعاب وجيمينج" -> theme.category == "gaming"
                "فخم وراقي" -> theme.category == "elegant"
                "ألوان ومرح" -> theme.category == "colorful"
                "مينيمال" -> theme.category == "minimal"
                "مخصص" -> theme.isCustom
                else -> true
            }

            matchQuery && matchCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .padding(horizontal = 12.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141926))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "أشكال وثيمات الكيبورد",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredThemes.size} ستايل احترافي متكامل",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Toggle preview language
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF141926),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                    modifier = Modifier.clickable {
                        previewLanguageIsArabic = !previewLanguageIsArabic
                    }
                ) {
                    Text(
                        text = if (previewLanguageIsArabic) "العربية" else "EN Dual",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = onOpenCustomBuilder,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141926))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "New Theme",
                        tint = Color(0xFF00E5FF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f)
                            else Color(0xFF131825)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF00E5FF) else Color(0xFF26334A),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8E9BAE),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Realistic Bottom-Screen Keyboards Showcase List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            items(filteredThemes, key = { it.id }) { theme ->
                val isActive = theme.id == currentThemeId

                RealisticBottomScreenKeyboardCard(
                    theme = theme,
                    isActive = isActive,
                    isArabic = previewLanguageIsArabic,
                    onApply = {
                        prefs.setCurrentTheme(theme.id)
                        onThemeApplied(theme)
                    },
                    onDelete = if (theme.isCustom) {
                        {
                            prefs.deleteCustomTheme(theme.id)
                            customThemes = prefs.getCustomThemes()
                        }
                    } else null
                )
            }
        }
    }
}

/**
 * Renders a realistic keyboard preview that looks exactly like an actual
 * keyboard at the bottom of a phone screen (as requested in the prompt).
 */
@Composable
fun RealisticBottomScreenKeyboardCard(
    theme: KeyboardTheme,
    isActive: Boolean,
    isArabic: Boolean,
    onApply: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF11141C),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) Color(theme.accentColor) else Color(0xFF222B3D)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar with theme title and apply button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B26))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(theme.accentColor))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = theme.nameEn,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = theme.nameAr,
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color(theme.accentColor) else Color(0xFF1E283A)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("المفعل حالياً", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("تطبيق الثيم", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Realistic Bottom Phone Screen Bezel containing the Keyboard
            val bgModifier = if (!theme.backgroundGradient.isNullOrEmpty()) {
                Modifier.background(Brush.verticalGradient(theme.backgroundGradient.map { Color(it) }))
            } else {
                Modifier.background(Color(theme.backgroundColor))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(bgModifier)
                    .padding(top = 6.dp, bottom = 10.dp, start = 6.dp, end = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Realistic Toolbar (Search, Timer, Setting, Text Style, Voice, Mic, Text)
                    RealisticToolbarRow(theme = theme, isArabic = isArabic)

                    Spacer(modifier = Modifier.height(2.dp))

                    // Row 1: Q W E R T Y U I O P (or Arabic)
                    RealisticKeyRow(
                        keys = if (isArabic) {
                            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج")
                        } else {
                            listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
                        },
                        hints = if (isArabic) {
                            listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "")
                        } else {
                            listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح")
                        },
                        theme = theme
                    )

                    // Row 2: A S D F G H J K L (or Arabic)
                    RealisticKeyRow(
                        keys = if (isArabic) {
                            listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط")
                        } else {
                            listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
                        },
                        hints = if (isArabic) null else listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م"),
                        hasSidePadding = !isArabic,
                        theme = theme
                    )

                    // Row 3: Shift, Z X C V B N M, Backspace
                    RealisticBottomCharRow(
                        theme = theme,
                        isArabic = isArabic
                    )

                    // Row 4: ?123, Emoji, Spacebar ("TURBO KEYBOARD" / "العربية"), Enter
                    RealisticSpacebarRow(
                        theme = theme,
                        isArabic = isArabic
                    )

                    // Phone bottom home indicator line
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.CenterHorizontally)
                            .width(72.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(theme.keyTextColor).copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}

@Composable
fun RealisticToolbarRow(theme: KeyboardTheme, isArabic: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(theme.toolbarColor).copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconTint = Color(theme.accentColor)

        Icon(Icons.Outlined.Search, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Icon(Icons.Outlined.Settings, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Icon(Icons.Outlined.TextFields, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Icon(Icons.Outlined.Mic, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Icon(Icons.Outlined.Translate, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun RealisticKeyRow(
    keys: List<String>,
    hints: List<String>?,
    hasSidePadding: Boolean = false,
    theme: KeyboardTheme
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (hasSidePadding) {
            Spacer(modifier = Modifier.weight(0.4f))
        }

        keys.forEachIndexed { index, char ->
            val hint = hints?.getOrNull(index)
            RealisticKey(
                char = char,
                hint = hint,
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        if (hasSidePadding) {
            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}

@Composable
fun RealisticBottomCharRow(
    theme: KeyboardTheme,
    isArabic: Boolean
) {
    val charList = if (isArabic) {
        listOf("ئ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ")
    } else {
        listOf("Z", "X", "C", "V", "B", "N", "M")
    }
    val hintList = if (isArabic) null else listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "ة")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Shift Key
        Box(
            modifier = Modifier
                .weight(1.3f)
                .height(38.dp)
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
                .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                .border(
                    1.dp,
                    Color(theme.borderColor).copy(alpha = theme.borderAlpha),
                    RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "Shift",
                tint = Color(theme.accentColor),
                modifier = Modifier.size(16.dp)
            )
        }

        // Letter Keys
        charList.forEachIndexed { index, char ->
            RealisticKey(
                char = char,
                hint = hintList?.getOrNull(index),
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        // Backspace Key
        Box(
            modifier = Modifier
                .weight(1.3f)
                .height(38.dp)
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
                .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                .border(
                    1.dp,
                    Color(theme.borderColor).copy(alpha = theme.borderAlpha),
                    RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Backspace",
                tint = Color(theme.keyTextColor),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RealisticSpacebarRow(
    theme: KeyboardTheme,
    isArabic: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 123 / ?123
        Box(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
                .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?123",
                color = Color(theme.keyTextColor),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Globe / Language
        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
                .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = Color(theme.keyTextColor),
                modifier = Modifier.size(16.dp)
            )
        }

        // Emoji
        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
                .background(Color(theme.keyBackgroundColor).copy(alpha = 0.85f))
                .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Mood,
                contentDescription = null,
                tint = Color(theme.keyTextColor),
                modifier = Modifier.size(16.dp)
            )
        }

        // Spacebar labeled "TURBO KEYBOARD" / "العربية"
        Box(
            modifier = Modifier
                .weight(4f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
                .background(Color(theme.keyBackgroundColor))
                .border(1.dp, Color(theme.borderColor).copy(alpha = theme.borderAlpha), RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isArabic) "اللغة العربية" else "TURBO KEYBOARD",
                color = Color(theme.keyTextColor).copy(alpha = 0.75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Enter Key with accent glow
        Box(
            modifier = Modifier
                .weight(1.4f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
                .background(Color(theme.enterButtonColor))
                .border(1.dp, Color(theme.accentColor).copy(alpha = 0.7f), RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                contentDescription = "Enter",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun RealisticKey(
    char: String,
    hint: String?,
    theme: KeyboardTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp)))
            .background(Color(theme.keyBackgroundColor))
            .border(
                width = if (theme.keyStyle == "neon") 1.2.dp else 1.dp,
                color = if (theme.keyStyle == "neon") Color(theme.accentColor).copy(alpha = 0.8f)
                        else Color(theme.borderColor).copy(alpha = theme.borderAlpha),
                shape = RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(10.dp))
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!hint.isNullOrEmpty()) {
            Text(
                text = hint,
                color = Color(theme.subtextColor).copy(alpha = 0.7f),
                fontSize = 8.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 1.dp, end = 2.dp)
            )
        }

        Text(
            text = char,
            color = Color(theme.keyTextColor),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
