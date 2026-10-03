package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InputLanguagesManager
import com.example.data.PreferencesManager
import com.example.data.WorldLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun InputLanguagesScreen(
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var installedCodes by remember { mutableStateOf(InputLanguagesManager.getInstalledLanguages(context)) }
    var activeCodes by remember { mutableStateOf(InputLanguagesManager.getActiveLanguages(context)) }
    var currentTypingCode by remember { mutableStateOf(InputLanguagesManager.getCurrentLanguageCode(context)) }

    var swipeSpaceSwitch by remember {
        mutableStateOf(prefs.getSettings().swipeSpaceSwitchLanguage)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = اللغات المفعلة والمثبتة, 1 = تحميل لغات العالم

    // Map of download states: language code -> progress float (0f to 1f)
    val downloadingProgress = remember { mutableStateMapOf<String, Float>() }

    fun refreshLists() {
        installedCodes = InputLanguagesManager.getInstalledLanguages(context)
        activeCodes = InputLanguagesManager.getActiveLanguages(context)
        currentTypingCode = InputLanguagesManager.getCurrentLanguageCode(context)
    }

    fun startDownloadLanguage(lang: WorldLanguage) {
        if (downloadingProgress.containsKey(lang.code)) return
        downloadingProgress[lang.code] = 0.05f

        coroutineScope.launch {
            // Smooth progress simulation
            for (step in 1..10) {
                delay(120)
                downloadingProgress[lang.code] = step * 0.1f
            }
            delay(150)
            InputLanguagesManager.installLanguage(context, lang.code)
            downloadingProgress.remove(lang.code)
            refreshLists()
            Toast.makeText(context, "تم تنزيل وتثبيت لغة ${lang.nameAr} بنجاح!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D15))
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top bar matching SettingsActivity & SettingsInputKeyboards
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141A28))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "لغات الإدخال ولوحات المفاتيح",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "جميع لغات العالم مع التنزيل والتطبيق",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(38.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Swipe space switch card (matching Transboard screenshot 3)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF131A26),
            border = BorderStroke(1.dp, Color(0xFF1E283A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "السحب على المسافة للتبديل",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مرر إصبعك على مفتاح المسافة لتبديل لغة الكتابة",
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = swipeSpaceSwitch,
                    onCheckedChange = {
                        swipeSpaceSwitch = it
                        val current = prefs.getSettings()
                        prefs.saveSettings(current.copy(swipeSpaceSwitchLanguage = it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00E5FF)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs: [ اللغات المفعلة (X) ]  [ تحميل لغات العالم (50+) ]
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF131A26),
            contentColor = Color(0xFF00E5FF),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "اللغات المثبتة والمفعلة (${installedCodes.size})",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "تحميل لغات العالم (${InputLanguagesManager.allWorldLanguages.size})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث عن أي لغة بالعالم (فرنسية، روسية، كورية، ألمانية...)", color = Color(0xFF8E9BAE), fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF26334A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Content
        if (selectedTab == 0) {
            // TAB 0: INSTALLED / ACTIVE LANGUAGES
            val installedLanguages = InputLanguagesManager.allWorldLanguages.filter { it.code in installedCodes }
            val filtered = installedLanguages.filter {
                it.nameAr.contains(searchQuery, ignoreCase = true) ||
                it.nameNative.contains(searchQuery, ignoreCase = true) ||
                it.nameEn.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "اللغات المفعلة يتم التبديل بينها عبر السحب على المسافة أو زر اللغة:",
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                items(filtered, key = { it.code }) { lang ->
                    val isActive = lang.code in activeCodes
                    val isCurrentlyTyping = lang.code == currentTypingCode

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCurrentlyTyping) Color(0xFF142436) else Color(0xFF131A26),
                        border = BorderStroke(1.dp, if (isCurrentlyTyping) Color(0xFF00E5FF) else Color(0xFF1E283A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = lang.flag,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.nameNative,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF1E293B)
                                        ) {
                                            Text(
                                                text = lang.layoutTag,
                                                color = Color(0xFF00E5FF),
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                        if (isCurrentlyTyping) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "مفعلة حالياً بالكيبورد",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${lang.nameAr} (${lang.nameEn})",
                                        color = Color(0xFF8E9BAE),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // "تطبيق الآن" (Apply now button)
                                if (!isCurrentlyTyping) {
                                    TextButton(
                                        onClick = {
                                            InputLanguagesManager.setCurrentLanguageCode(context, lang.code)
                                            if (lang.code !in activeCodes) {
                                                InputLanguagesManager.toggleLanguageActive(context, lang.code)
                                            }
                                            refreshLists()
                                            Toast.makeText(context, "تم تطبيق لغة ${lang.nameNative} على الكيبورد!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF00E5FF))
                                    ) {
                                        Text("تطبيق", fontSize = 11.sp)
                                    }
                                }

                                // Checkbox to include in spacebar cycling
                                Checkbox(
                                    checked = isActive,
                                    onCheckedChange = {
                                        InputLanguagesManager.toggleLanguageActive(context, lang.code)
                                        refreshLists()
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF00E5FF),
                                        checkmarkColor = Color.Black
                                    )
                                )

                                // Uninstall button if not ar/en
                                if (lang.code != "ar" && lang.code != "en") {
                                    IconButton(
                                        onClick = {
                                            InputLanguagesManager.uninstallLanguage(context, lang.code)
                                            refreshLists()
                                            Toast.makeText(context, "تم حذف لغة ${lang.nameAr}", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Uninstall",
                                            tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: DOWNLOAD ALL WORLD LANGUAGES
            val availableToDownload = InputLanguagesManager.allWorldLanguages.filter {
                it.nameAr.contains(searchQuery, ignoreCase = true) ||
                it.nameNative.contains(searchQuery, ignoreCase = true) ||
                it.nameEn.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Text(
                        text = "اضغط تحميل على أي لغة لإضافتها فوراً وتطبيقها على لوحة المفاتيح:",
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                items(availableToDownload, key = { it.code }) { lang ->
                    val isInstalled = lang.code in installedCodes
                    val progress = downloadingProgress[lang.code]

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF131A26),
                        border = BorderStroke(1.dp, if (isInstalled) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFF1E283A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = lang.flag,
                                        fontSize = 24.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = lang.nameNative,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF1E293B)
                                            ) {
                                                Text(
                                                    text = lang.layoutTag,
                                                    color = Color(0xFF00E5FF),
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${lang.nameAr} • الحجم: ${lang.size}",
                                            color = Color(0xFF8E9BAE),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isInstalled) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                            border = BorderStroke(1.dp, Color(0xFF10B981))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF10B981),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "مثبتة",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))
                                        TextButton(
                                            onClick = {
                                                InputLanguagesManager.setCurrentLanguageCode(context, lang.code)
                                                refreshLists()
                                                Toast.makeText(context, "تم تطبيق لغة ${lang.nameNative} على الكيبورد!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text("تطبيق", color = Color(0xFF00E5FF), fontSize = 11.sp)
                                        }
                                    }
                                } else if (progress != null) {
                                    // Downloading progress indicator
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.width(100.dp)
                                    ) {
                                        Text(
                                            text = "جاري التنزيل ${(progress * 100).toInt()}%",
                                            color = Color(0xFF00E5FF),
                                            fontSize = 10.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { progress },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                            color = Color(0xFF00E5FF),
                                            trackColor = Color(0xFF1E283A)
                                        )
                                    }
                                } else {
                                    // Download button
                                    Button(
                                        onClick = { startDownloadLanguage(lang) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00E5FF),
                                            contentColor = Color.Black
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "تحميل",
                                            fontSize = 11.sp,
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
    }
}
