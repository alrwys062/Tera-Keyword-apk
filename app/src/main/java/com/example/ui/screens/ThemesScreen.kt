package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
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
        "✨ 20 ستايل فاتح",
        "⭐ 20 ثيم بريميوم",
        "Pro ستايلات الهواتف",
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
        (customThemes + presets).distinctBy { it.id }
    }

    val filteredThemes = remember(allThemes, searchQuery, selectedCategory) {
        allThemes.filter { theme ->
            val matchQuery = searchQuery.isBlank() ||
                    theme.nameAr.contains(searchQuery.trim(), ignoreCase = true) ||
                    theme.nameEn.contains(searchQuery.trim(), ignoreCase = true)

            val matchCategory = when (selectedCategory) {
                "الكل" -> true
                "✨ 20 ستايل فاتح" -> theme.category == "twenty_light" || theme.id in ThemePresets.twentyLightThemes.map { it.id }
                "⭐ 20 ثيم بريميوم" -> theme.category == "twenty_premium" || theme.id in ThemePresets.twentyPremiumThemes.map { it.id }
                "Pro ستايلات الهواتف" -> theme.category == "pro_styles"
                "أنظمة وهواتف" -> theme.category == "systems"
                "داكن وفاتح" -> theme.category == "dark_light"
                "نيون وسايبر" -> theme.category == "neon"
                "تدرجات لونية" -> theme.category == "gradient"
                "زجاجي شفاف" -> theme.category == "glass" || theme.isGlass
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

    // Selected theme for live hero preview (defaults to currently applied theme)
    var heroThemeId by remember(currentThemeId) { mutableStateOf(currentThemeId) }
    val heroTheme = remember(heroThemeId, allThemes) {
        allThemes.find { it.id == heroThemeId } ?: allThemes.firstOrNull() ?: ThemePresets.DARK_NEON
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
                    text = "${filteredThemes.size} ستايل فائق السرعة والخفة",
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

                // Add custom theme
                IconButton(
                    onClick = onOpenCustomBuilder,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00E5FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Custom Theme",
                        tint = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "ابحث بالاسم أو السمة (آيفون، نيون، جلاكسي، ذهبي...)",
                    color = Color(0xFF728096),
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF728096),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF10141E),
                unfocusedContainerColor = Color(0xFF10141E),
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF1E2638),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF121724),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) Color(0xFF00E5FF) else Color(0xFF222B3D)
                    ),
                    modifier = Modifier.clickable { selectedCategory = category }
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.Black else Color(0xFFC0CAD9),
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High-Performance Grid with Interactive Top Hero Preview
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Span 2: Interactive Hero Preview of the selected theme
            item(span = { GridItemSpan(2) }) {
                HeroLivePreviewCard(
                    theme = heroTheme,
                    isCurrentActive = heroTheme.id == currentThemeId,
                    isArabic = previewLanguageIsArabic,
                    onApply = {
                        prefs.setCurrentTheme(heroTheme.id)
                        onThemeApplied(heroTheme)
                    },
                    onDelete = if (heroTheme.isCustom) {
                        {
                            prefs.deleteCustomTheme(heroTheme.id)
                            customThemes = prefs.getCustomThemes()
                            heroThemeId = currentThemeId
                        }
                    } else null
                )
            }

            // Section Header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "اختر ستايل الكيبورد (${filteredThemes.size})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اضغط للمعاينة الفورية",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp
                    )
                }
            }

            // Lightweight, Ultra-Responsive Theme Cards
            items(filteredThemes, key = { it.id }) { theme ->
                val isSelectedForPreview = theme.id == heroTheme.id
                val isCurrentlyActive = theme.id == currentThemeId

                LightweightThemeCard(
                    theme = theme,
                    isSelected = isSelectedForPreview,
                    isActive = isCurrentlyActive,
                    onClick = {
                        heroThemeId = theme.id
                    },
                    onApplyDirect = {
                        heroThemeId = theme.id
                        prefs.setCurrentTheme(theme.id)
                        onThemeApplied(theme)
                    }
                )
            }
        }
    }
}

/**
 * Top interactive hero card rendering a realistic keyboard preview for the selected theme.
 * Rendered ONCE at the top, avoiding any frame drops or memory leaks.
 */
@Composable
fun HeroLivePreviewCard(
    theme: KeyboardTheme,
    isCurrentActive: Boolean,
    isArabic: Boolean,
    onApply: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF10141F),
        border = BorderStroke(
            width = if (isCurrentActive) 2.dp else 1.2.dp,
            color = if (isCurrentActive) Color(theme.accentColor) else Color(0xFF263248)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161C2A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(theme.accentColor))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = theme.nameAr,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = theme.nameEn,
                            color = Color(0xFF8E9BAE),
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (onDelete != null) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentActive) Color(theme.accentColor) else Color(0xFF1E283A)
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCurrentActive) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("المفعل حالياً", color = Color.Black, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("تطبيق الثيم", color = Color.White, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Keyboard Preview Bezel
            val bgModifier = remember(theme.id, theme.backgroundColor, theme.backgroundGradient) {
                if (!theme.backgroundGradient.isNullOrEmpty()) {
                    Modifier.background(Brush.verticalGradient(theme.backgroundGradient.map { Color(it) }))
                } else {
                    Modifier.background(Color(theme.backgroundColor))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(bgModifier)
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Mini Toolbar Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(theme.toolbarColor).copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconTint = Color(theme.accentColor)
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                        Icon(Icons.Outlined.TextFields, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                        Icon(Icons.Outlined.Translate, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = iconTint, modifier = Modifier.size(13.dp))
                    }

                    // Key Row 1
                    val keysRow1 = remember(isArabic) {
                        if (isArabic) listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج")
                        else listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P")
                    }
                    SampleKeyRow(keys = keysRow1, theme = theme)

                    // Key Row 2
                    val keysRow2 = remember(isArabic) {
                        if (isArabic) listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط")
                        else listOf("A", "S", "D", "F", "G", "H", "J", "K", "L")
                    }
                    SampleKeyRow(keys = keysRow2, theme = theme, sidePadding = !isArabic)

                    // Key Row 3
                    val keysRow3 = remember(isArabic) {
                        if (isArabic) listOf("ئ", "ء", "ؤ", "ر", "ى", "ة", "و", "ز", "ظ")
                        else listOf("Z", "X", "C", "V", "B", "N", "M")
                    }
                    SampleBottomCharRow(keys = keysRow3, theme = theme)

                    // Spacebar Row
                    SampleSpacebarRow(theme = theme, isArabic = isArabic)
                }
            }
        }
    }
}

/**
 * Ultra-fast, lightweight theme card for the 2-column grid.
 * Uses very low memory, fast layout, and no complex nesting.
 */
@Composable
fun LightweightThemeCard(
    theme: KeyboardTheme,
    isSelected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    onApplyDirect: () -> Unit
) {
    val cardBgModifier = remember(theme.id, theme.backgroundColor, theme.backgroundGradient) {
        if (!theme.backgroundGradient.isNullOrEmpty()) {
            Modifier.background(Brush.verticalGradient(theme.backgroundGradient.map { Color(it) }))
        } else {
            Modifier.background(Color(theme.backgroundColor))
        }
    }

    val keyBg = remember(theme.keyBackgroundColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    }
    val accentColor = remember(theme.accentColor) { Color(theme.accentColor) }
    val borderColor = remember(theme.borderColor, theme.borderAlpha) {
        Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF10141E),
        border = BorderStroke(
            width = if (isActive) 2.dp else if (isSelected) 1.5.dp else 1.dp,
            color = if (isActive) accentColor else if (isSelected) Color(0xFF00E5FF) else Color(0xFF1C2434)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Theme Visual Mockup Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .then(cardBgModifier)
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(theme.toolbarColor).copy(alpha = 0.8f))
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(accentColor))
                        Box(modifier = Modifier.width(18.dp).height(3.dp).clip(RoundedCornerShape(1.dp)).background(accentColor.copy(alpha = 0.5f)))
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(accentColor))
                    }

                    // Mini Keys Grid Simulation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.5.dp))
                                    .background(keyBg)
                                    .border(0.5.dp, borderColor, RoundedCornerShape(2.5.dp))
                            )
                        }
                    }

                    // Mini Spacebar + Enter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(keyBg)
                        )
                        Box(
                            modifier = Modifier
                                .weight(3f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(keyBg)
                                .border(0.5.dp, borderColor, RoundedCornerShape(2.5.dp))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.5.dp))
                                .background(Color(theme.enterButtonColor))
                        )
                    }
                }

                // Active badge overlay
                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.75f),
                        border = BorderStroke(1.dp, accentColor),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = "✓ مفعل",
                            color = accentColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            // Card Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131825))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = theme.nameAr,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = theme.nameEn,
                        color = Color(0xFF7F8FA4),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isActive) accentColor.copy(alpha = 0.2f) else Color(0xFF1E283A),
                    border = BorderStroke(0.8.dp, if (isActive) accentColor else Color(0xFF2C384E)),
                    modifier = Modifier.clickable { onApplyDirect() }
                ) {
                    Text(
                        text = if (isActive) "مفعل" else "تطبيق",
                        color = if (isActive) accentColor else Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleKeyRow(
    keys: List<String>,
    theme: KeyboardTheme,
    sidePadding: Boolean = false
) {
    val keyBg = remember(theme.keyBackgroundColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    }
    val keyText = remember(theme.keyTextColor) { Color(theme.keyTextColor) }
    val borderColor = remember(theme.borderColor, theme.borderAlpha) {
        Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (sidePadding) {
            Spacer(modifier = Modifier.weight(0.4f))
        }
        keys.forEach { char ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                    .background(keyBg)
                    .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    color = keyText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        if (sidePadding) {
            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}

@Composable
private fun SampleBottomCharRow(
    keys: List<String>,
    theme: KeyboardTheme
) {
    val keyBg = remember(theme.keyBackgroundColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    }
    val accentColor = remember(theme.accentColor) { Color(theme.accentColor) }
    val keyText = remember(theme.keyTextColor) { Color(theme.keyTextColor) }
    val borderColor = remember(theme.borderColor, theme.borderAlpha) {
        Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Shift Key
        Box(
            modifier = Modifier
                .weight(1.3f)
                .height(28.dp)
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                .background(keyBg)
                .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
        }

        keys.forEach { char ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                    .background(keyBg)
                    .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    color = keyText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Backspace Key
        Box(
            modifier = Modifier
                .weight(1.3f)
                .height(28.dp)
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                .background(keyBg)
                .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun SampleSpacebarRow(
    theme: KeyboardTheme,
    isArabic: Boolean
) {
    val keyBg = remember(theme.keyBackgroundColor, theme.keyOpacity) {
        ThemePresets.resolveKeyColor(theme.keyBackgroundColor, theme.keyOpacity)
    }
    val keyText = remember(theme.keyTextColor) { Color(theme.keyTextColor) }
    val borderColor = remember(theme.borderColor, theme.borderAlpha) {
        Color(theme.borderColor).copy(alpha = theme.borderAlpha.coerceAtLeast(0.35f))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                .background(keyBg)
                .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
            contentAlignment = Alignment.Center
        ) {
            Text("?123", color = keyText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                .background(keyBg)
                .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Language, contentDescription = null, tint = keyText, modifier = Modifier.size(12.dp))
        }

        Box(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                .background(keyBg)
                .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Mood, contentDescription = null, tint = keyText, modifier = Modifier.size(12.dp))
        }

        // Spacebar
        Box(
            modifier = Modifier
                .weight(3.6f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                .background(keyBg)
                .border(0.8.dp, borderColor, RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isArabic) "اللغة العربية" else "TURBO KEYBOARD",
                color = keyText.copy(alpha = 0.85f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Enter
        Box(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(theme.cornerRadius.dp.coerceAtMost(6.dp)))
                .background(Color(theme.enterButtonColor)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                contentDescription = null,
                tint = ThemePresets.getEnterIconTint(theme),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
