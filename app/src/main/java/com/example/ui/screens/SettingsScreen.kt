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
import com.example.data.PreferencesManager
import com.example.data.TextDecorator
import com.example.keyboard.allKeyboardToolbarTools
import com.example.model.KeyboardSettings
import com.example.model.TextShortcut

@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    onNavigateToThemes: () -> Unit,
    onNavigateToClipboard: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(prefs.getSettings()) }
    var searchQuery by remember { mutableStateOf("") }
    var showLanguagesScreen by remember { mutableStateOf(false) }

    if (showLanguagesScreen) {
        InputLanguagesScreen(
            prefs = prefs,
            onBack = {
                showLanguagesScreen = false
                settings = prefs.getSettings()
            }
        )
        return
    }

    // Dialog & sheet states matching Transboard screenshots
    var activeDialog by remember { mutableStateOf<String?>(null) }

    fun updateSettings(newSettings: KeyboardSettings) {
        settings = newSettings
        prefs.saveSettings(newSettings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D15))
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top bar matching Screenshots 1 & 2
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

            Text(
                text = "إعدادات لوحة المفاتيح",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(38.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في إعدادات لوحة المفاتيح...", color = Color(0xFF8E9BAE), fontSize = 12.sp) },
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
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Complete 13 Categories matching Transboard Screenshots 1 - 13
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. لغات الإدخال (Screenshots 1, 3, SettingsInputKeyboards)
            item {
                TransboardSettingsRow(
                    title = "لغات الإدخال",
                    subtitle = "تحميل وتطبيق جميع لغات العالم (مرر على المسافة للتبديل)",
                    icon = Icons.Outlined.Language,
                    onClick = { showLanguagesScreen = true }
                )
            }

            // 2. الترجمة (Screenshot 1 & 4)
            item {
                TransboardSettingsRow(
                    title = "الترجمة",
                    subtitle = "إعدادات الترجمة الفورية والترجمة عند النسخ",
                    icon = Icons.Outlined.Translate,
                    onClick = { activeDialog = "TRANSLATE" }
                )
            }

            // 3. الزخرفة (Screenshot 1 & 5)
            item {
                TransboardSettingsRow(
                    title = "الزخرفة",
                    subtitle = "إعدادات الزخرفة الحية وأنماط الخطوط",
                    icon = Icons.Outlined.AutoAwesome,
                    onClick = { activeDialog = "DECORATION" }
                )
            }

            // 4. المظهر (Screenshot 1 & 6)
            item {
                TransboardSettingsRow(
                    title = "المظهر",
                    subtitle = "اختر مظهر لوحة المفاتيح أو أنشئ مظهر مخصص",
                    icon = Icons.Outlined.Palette,
                    onClick = onNavigateToThemes
                )
            }

            // 5. تخطيط لوحة المفاتيح (Screenshot 1 & 7, 8, 9)
            item {
                TransboardSettingsRow(
                    title = "تخطيط لوحة المفاتيح",
                    subtitle = "ستايل لوحة المفاتيح وترتيب الأحرف (عربي اساسي، سامسونج، AOSP، سويفت)",
                    icon = Icons.Outlined.Keyboard,
                    onClick = { activeDialog = "LAYOUTS" }
                )
            }

            // 6. الإبتسامات (Screenshot 1)
            item {
                TransboardSettingsRow(
                    title = "الإبتسامات",
                    subtitle = "صف الإيموجي العلوي السريع ومظهر السمايلات",
                    icon = Icons.Outlined.Mood,
                    onClick = { activeDialog = "EMOJIS" }
                )
            }

            // 7. الحافظة (Screenshot 1 & User requirement: حفظ للأبد وإغلاق عند اللصق وتذكر الموضع)
            item {
                TransboardSettingsRow(
                    title = "الحافظة",
                    subtitle = "حفظ النصوص للأبد، إغلاق عند اللصق، وتذكر موضع التمرير",
                    icon = Icons.Outlined.ContentPaste,
                    onClick = { activeDialog = "CLIPBOARD" }
                )
            }

            // 8. الاختصارات (Screenshot 1 & 2)
            item {
                TransboardSettingsRow(
                    title = "الاختصارات",
                    subtitle = "التحكم في اختصارات النصوص السريعة (سلام، ص، جزاك...)",
                    icon = Icons.Outlined.ShortText,
                    onClick = { activeDialog = "SHORTCUTS" }
                )
            }

            // 9. الكتابة (Screenshot 2 & 10)
            item {
                TransboardSettingsRow(
                    title = "الكتابة والتصحيح التلقائي",
                    subtitle = "إعدادات الاقتراحات والتصحيح التلقائي والكتابة بالأحرف الكبيرة",
                    icon = Icons.Outlined.TextFields,
                    onClick = { activeDialog = "TYPING" }
                )
            }

            // 9.1 الذكاء الاصطناعي وتغيير النبرة
            item {
                TransboardSettingsRow(
                    title = "الذكاء الاصطناعي وتغيير نبرة الكتابة",
                    subtitle = "تغيير نبرة الكتابة (رسمي، ودي، مختصر، شاعري، فصيح...) وإعادة الصياغة",
                    icon = Icons.Outlined.Psychology,
                    onClick = { activeDialog = "AI_TONE" }
                )
            }

            // 9.2 قاموس تصحيح الأخطاء والكلمات الشخصية
            item {
                TransboardSettingsRow(
                    title = "قاموس تصحيح الأخطاء والكلمات",
                    subtitle = "إضافة كلمات جديدة للقاموس والتحكم في قاموس التدقيق الإملائي",
                    icon = Icons.Outlined.Spellcheck,
                    onClick = { activeDialog = "DICTIONARY" }
                )
            }

            // 10. الصف السفلي والعلوي وشريط الأدوات (Screenshot 2 & 11)
            item {
                TransboardSettingsRow(
                    title = "الصف السفلي والعلوي و شريط الأدوات",
                    subtitle = "إظهار صف الأرقام العلوي والتحكم بالصف السفلي وشريط الأدوات",
                    icon = Icons.Outlined.ViewStream,
                    onClick = { activeDialog = "ROWS_TOOLBAR" }
                )
            }

            // 10.1 تخصيص شريط الأدوات العلوي (تقليل الأيقونات وإزالة الزحمة)
            item {
                TransboardSettingsRow(
                    title = "تخصيص شريط الأدوات العلوي",
                    subtitle = "تقليل عدد الأيقونات، إخفاء الأدوات غير المستخدمة، واختيار الأدوات التي تظهر فقط",
                    icon = Icons.Outlined.Tune,
                    onClick = { activeDialog = "CUSTOMIZE_TOOLBAR" }
                )
            }

            // 11. الصوت والإهتزاز (Screenshot 2 & 12)
            item {
                TransboardSettingsRow(
                    title = "الصوت والإهتزاز",
                    subtitle = "التحكم في أصوات المفاتيح والاهتزاز بالملي ثانية",
                    icon = Icons.Outlined.VolumeUp,
                    onClick = { activeDialog = "SOUND_HAPTIC" }
                )
            }

            // 12. إرتفاع الكيبورد وحجم الأحرف (Screenshot 2 & 13)
            item {
                TransboardSettingsRow(
                    title = "إرتفاع الكيبورد وحجم الأحرف",
                    subtitle = "التحكم في ارتفاع الكيبورد وحجم الأحرف على المفاتيح",
                    icon = Icons.Outlined.Height,
                    onClick = { activeDialog = "HEIGHT_FONT" }
                )
            }

            // 13. النسخ الإحتياطي (Screenshot 2)
            item {
                TransboardSettingsRow(
                    title = "النسخ الإحتياطي",
                    subtitle = "النسخ الإحتياطي للحافظة والإختصارات واستعادتها",
                    icon = Icons.Outlined.CloudUpload,
                    onClick = { activeDialog = "BACKUP" }
                )
            }
        }
    }

    // -------------------------------------------------------------
    // MODALS & DIALOGS MATCHING TRANSBOARD
    // -------------------------------------------------------------

    // 1. LANGUAGES DIALOG (Screenshot 3)
    if (activeDialog == "LANGUAGES") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("لغات الإدخال", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عربي اساسي", color = Color.White, fontSize = 14.sp)
                        Checkbox(checked = true, onCheckedChange = null)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("English", color = Color.White, fontSize = 14.sp)
                        Checkbox(checked = true, onCheckedChange = null)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("السحب على المسافة للتبديل", color = Color.White, fontSize = 13.sp)
                            Text("مرر إصبعك على مفتاح المسافة لتبديل اللغة", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.swipeSpaceSwitchLanguage,
                            onCheckedChange = { updateSettings(settings.copy(swipeSpaceSwitchLanguage = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 2. TRANSLATION DIALOG (Screenshot 4 + All World Languages)
    if (activeDialog == "TRANSLATE") {
        var showSourcePicker by remember { mutableStateOf(false) }
        var showTargetPicker by remember { mutableStateOf(false) }
        val allLangs = com.example.data.TranslationEngine.supportedLanguages

        val srcLangObj = allLangs.find { it.code == settings.translationSource } ?: allLangs[0]
        val tgtLangObj = allLangs.find { it.code == settings.translationTarget } ?: allLangs[1]

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("إعدادات الترجمة الفورية", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("لغات الترجمة الافتراضية:", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Source
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1C2436),
                            border = BorderStroke(1.dp, Color(0xFF28364F)),
                            modifier = Modifier.weight(1f).clickable { showSourcePicker = true }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("من:", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                                Text("${srcLangObj.flag} ${srcLangObj.nameAr}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))

                        // Target
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1C2436),
                            border = BorderStroke(1.dp, Color(0xFF28364F)),
                            modifier = Modifier.weight(1f).clickable { showTargetPicker = true }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("إلى:", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                                Text("${tgtLangObj.flag} ${tgtLangObj.nameAr}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (showSourcePicker || showTargetPicker) {
                        var langFilter by remember { mutableStateOf("") }
                        val filteredList = allLangs.filter {
                            it.nameAr.contains(langFilter, ignoreCase = true) ||
                            it.nameEn.contains(langFilter, ignoreCase = true)
                        }

                        AlertDialog(
                            onDismissRequest = {
                                showSourcePicker = false
                                showTargetPicker = false
                            },
                            title = { Text(if (showSourcePicker) "اختر لغة الإدخال (من)" else "اختر لغة الترجمة (إلى)", color = Color.White, fontSize = 15.sp) },
                            text = {
                                Column(modifier = Modifier.height(300.dp)) {
                                    OutlinedTextField(
                                        value = langFilter,
                                        onValueChange = { langFilter = it },
                                        placeholder = { Text("بحث بين جميع لغات العالم...", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filteredList) { l ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        if (showSourcePicker) {
                                                            updateSettings(settings.copy(translationSource = l.code))
                                                            showSourcePicker = false
                                                        } else {
                                                            updateSettings(settings.copy(translationTarget = l.code))
                                                            showTargetPicker = false
                                                        }
                                                    }
                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(l.flag, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(l.nameAr, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("(${l.nameEn})", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    showSourcePicker = false
                                    showTargetPicker = false
                                }) {
                                    Text("إلغاء", color = Color(0xFF00E5FF))
                                }
                            },
                            containerColor = Color(0xFF141A28)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الترجمة التلقائية", color = Color.White, fontSize = 13.sp)
                            Text("ترجمة تلقائية للنص بعد النسخ", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = settings.autoTranslateOnCopy,
                            onCheckedChange = { updateSettings(settings.copy(autoTranslateOnCopy = it)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("الضغط 4 ثوانٍ على Enter للترجمة", color = Color.White, fontSize = 13.sp)
                            Text("ترجمة النص المكتوب كاملاً عند الضغط المطول", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = settings.enterLongPressTranslateEnabled,
                            onCheckedChange = { updateSettings(settings.copy(enterLongPressTranslateEnabled = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 3. DECORATION DIALOG (Screenshot 5 & 18)
    if (activeDialog == "DECORATION") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("زخارف ونمط الكتابة", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(260.dp)) {
                    items(TextDecorator.allStyles) { style ->
                        val isSelected = settings.activeDecorationStyle == style.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1C2436),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF28364F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    updateSettings(settings.copy(activeDecorationStyle = style.id))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(style.preview, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(style.nameAr, color = Color(0xFF8E9BAE), fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("إغلاق", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 5. KEYBOARD LAYOUTS DIALOG (Screenshots 7, 8, 9)
    if (activeDialog == "LAYOUTS") {
        val layouts = listOf(
            "basic_ar" to "عربي اساسي (Transboard القياسي)",
            "samsung" to "العربية / سامسونج",
            "aosp" to "العربية / AOSP",
            "linux" to "Arabic / Linux",
            "swift" to "العربية / Swift"
        )
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("تخطيط لوحة المفاتيح", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    layouts.forEach { (code, title) ->
                        val isSelected = settings.keyboardLayoutStyle == code
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1C2436),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF28364F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    updateSettings(settings.copy(keyboardLayoutStyle = code))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(title, color = if (isSelected) Color(0xFF00E5FF) else Color.White, fontSize = 12.sp)
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 6. EMOJIS DIALOG
    if (activeDialog == "EMOJIS") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("الإبتسامات والصف السريع", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("صف الإيموجي والاختصارات العلوي", color = Color.White, fontSize = 13.sp)
                            Text("إظهار شريط (👑 💋 ة ؤ ء ئ ى لأ 😂 خاص) أعلى الحروف", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.topQuickEmojiRowEnabled,
                            onCheckedChange = { updateSettings(settings.copy(topQuickEmojiRowEnabled = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 7. CLIPBOARD DIALOG (User's specific core requirements!)
    if (activeDialog == "CLIPBOARD") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("التحكم في إعدادات الحافظة", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Feature 1: حفظ للأبد
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("حفظ النصوص للأبد", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("الاحتفاظ بكافة النصوص المنسوخة دون حذف تلقائي", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = settings.clipboardSaveForever,
                            onCheckedChange = { updateSettings(settings.copy(clipboardSaveForever = it)) }
                        )
                    }
                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    // Feature 2: إغلاق عند اللصق
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إغلاق الحافظة عند اللصق", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("الرجوع إلى المفاتيح مباشرة عند اختيار أي نص", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = settings.clipboardCloseOnPaste,
                            onCheckedChange = { updateSettings(settings.copy(clipboardCloseOnPaste = it)) }
                        )
                    }
                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    // Feature 3: حفظ موضع التمرير
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تذكر موضع التمرير بالحافظة", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("عند فتح الحافظة تبقى عند نفس المكان الذي وصلت إليه", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            prefs.clearClipboardHistory()
                            Toast.makeText(context, "تم مسح النصوص غير المثبتة", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text("مسح النصوص غير المثبتة فقط")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 8. SHORTCUTS DIALOG (الاختصارات)
    if (activeDialog == "SHORTCUTS") {
        var shortcutsList by remember { mutableStateOf(prefs.getShortcuts()) }
        var newTrigger by remember { mutableStateOf("") }
        var newExpansion by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("إدارة الاختصارات", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(300.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newTrigger,
                            onValueChange = { newTrigger = it },
                            placeholder = { Text("الاختصار (سلام)", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newExpansion,
                            onValueChange = { newExpansion = it },
                            placeholder = { Text("النص الكامل", fontSize = 10.sp) },
                            modifier = Modifier.weight(1.5f).height(44.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newTrigger.isNotBlank() && newExpansion.isNotBlank()) {
                                    prefs.addShortcut(newTrigger, newExpansion)
                                    shortcutsList = prefs.getShortcuts()
                                    newTrigger = ""
                                    newExpansion = ""
                                }
                            },
                            modifier = Modifier.height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text("إضافة", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(shortcutsList) { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1B2335),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.trigger, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(item.expansion, color = Color.White, fontSize = 10.sp)
                                    }
                                    IconButton(
                                        onClick = {
                                            prefs.deleteShortcut(item.id)
                                            shortcutsList = prefs.getShortcuts()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 10. ROWS & TOOLBAR DIALOG (Screenshot 11)
    if (activeDialog == "ROWS_TOOLBAR") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("الصف السفلي والعلوي وشريط الأدوات", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إظهار صف الأرقام العلوي", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = settings.numberRowEnabled,
                            onCheckedChange = { updateSettings(settings.copy(numberRowEnabled = it)) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إظهار صف الإيموجي العلوي السريع", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = settings.topQuickEmojiRowEnabled,
                            onCheckedChange = { updateSettings(settings.copy(topQuickEmojiRowEnabled = it)) }
                        )
                    }

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    // موضع زر الإدخال
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("موضع زر الإدخال (Enter)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (settings.enterKeyOnLeft) "على اليسار (معكوس)" else "على اليمين (الافتراضي القياسي)",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = settings.enterKeyOnLeft,
                            onCheckedChange = { updateSettings(settings.copy(enterKeyOnLeft = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 10.1 CUSTOMIZE TOOLBAR DIALOG (تخصيص وتقليل أيقونات شريط الأدوات)
    if (activeDialog == "CUSTOMIZE_TOOLBAR") {
        val currentTools = settings.visibleToolbarTools.toSet()

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Tune, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تخصيص شريط الأدوات العلوي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp)) {
                    Text(
                        "اختر الأدوات التي تريد ظهورها في الشريط أعلى الكيبورد لتقليل الزحمة وجعل الكيبورد خفيفاً وسريعاً:",
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val next = listOf("translate", "clipboard", "decoration", "emoji", "settings")
                                updateSettings(settings.copy(visibleToolbarTools = next))
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.18f)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("⚡ خفيف (5)", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val next = listOf("translate", "clipboard", "decoration", "voice", "phrases")
                                updateSettings(settings.copy(visibleToolbarTools = next))
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26334A)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("📝 كتابة (5)", color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val next = allKeyboardToolbarTools.map { it.id }
                                updateSettings(settings.copy(visibleToolbarTools = next))
                            },
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26334A)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("🌟 الكل (12)", color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(allKeyboardToolbarTools) { tool ->
                            val isChecked = tool.id in currentTools
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChecked) Color(0xFF1E283A) else Color(0xFF101520),
                                border = BorderStroke(1.dp, if (isChecked) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color(0xFF26334A).copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val next = if (isChecked) {
                                            if (currentTools.size > 1) currentTools - tool.id else currentTools
                                        } else {
                                            currentTools + tool.id
                                        }
                                        updateSettings(settings.copy(visibleToolbarTools = allKeyboardToolbarTools.map { it.id }.filter { it in next }))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = tool.icon,
                                            contentDescription = null,
                                            tint = if (isChecked) Color(0xFF00E5FF) else Color(0xFF8E9BAE),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = tool.nameAr,
                                                color = if (isChecked) Color.White else Color(0xFF8E9BAE),
                                                fontSize = 12.sp,
                                                fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = tool.description,
                                                color = Color(0xFF64748B),
                                                fontSize = 9.5.sp
                                            )
                                        }
                                    }

                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val next = if (checked) {
                                                currentTools + tool.id
                                            } else {
                                                if (currentTools.size > 1) currentTools - tool.id else currentTools
                                            }
                                            updateSettings(settings.copy(visibleToolbarTools = allKeyboardToolbarTools.map { it.id }.filter { it in next }))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم وحفظ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 9. TYPING DIALOG (الكتابة والتصحيح التلقائي)
    if (activeDialog == "TYPING") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("إعدادات الكتابة والتصحيح", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إظهار الاقتراحات", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("شريط الاقتراحات الذكية أعلى لوحة المفاتيح", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.suggestionsEnabled,
                            onCheckedChange = { updateSettings(settings.copy(suggestionsEnabled = it)) }
                        )
                    }

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("التصحيح التلقائي الذكي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("تصحيح الأخطاء الإملائية والشائعة تلقائياً", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.autoCorrection,
                            onCheckedChange = { updateSettings(settings.copy(autoCorrection = it)) }
                        )
                    }

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("أحرف كبيرة تلقائياً", color = Color.White, fontSize = 13.sp)
                            Text("تكبير الحرف الأول بعد علامات الترقيم في الإنجليزية", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = settings.autoCapitalization,
                            onCheckedChange = { updateSettings(settings.copy(autoCapitalization = it)) }
                        )
                    }

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("نقطة عند النقر المزدوج على المسافة", color = Color.White, fontSize = 13.sp)
                            Text("إدراج نقطة ومسافة تلقائياً عند الضغط مرتين", color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Checkbox(
                            checked = settings.doubleSpacePeriod,
                            onCheckedChange = { updateSettings(settings.copy(doubleSpacePeriod = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 9.1 AI TONE DIALOG
    if (activeDialog == "AI_TONE") {
        var testText by remember { mutableStateOf("ابي اكلمك بموضوع مهم اذا فاضي") }
        var selectedTone by remember { mutableStateOf("formal") }
        var toneResult by remember { mutableStateOf(com.example.data.AiToneEngine.transformTone(testText, "formal")) }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("الذكاء الاصطناعي وتغيير النبرة", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(380.dp)) {
                    Text("جرب تغيير نبرة النص مباشرة:", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = testText,
                        onValueChange = {
                            testText = it
                            toneResult = com.example.data.AiToneEngine.transformTone(it, selectedTone)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("اختر النبرة المطلوبة:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(modifier = Modifier.height(130.dp)) {
                        items(com.example.data.AiToneEngine.allTones) { tone ->
                            val isSelected = tone.id == selectedTone
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1E283A),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF28364F)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        selectedTone = tone.id
                                        toneResult = com.example.data.AiToneEngine.transformTone(testText, tone.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(tone.icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(tone.nameAr, color = if (isSelected) Color(0xFF00E5FF) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(tone.description, color = Color(0xFF8E9BAE), fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("النتيجة بالنبرة المختارة:", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF161F2E),
                        border = BorderStroke(1.dp, Color(0xFF2A3A54)),
                        modifier = Modifier.fillMaxWidth().height(70.dp).padding(2.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Text(toneResult, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("إغلاق", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 9.2 USER DICTIONARY DIALOG (قاموس تصحيح الأخطاء وإضافة كلمات)
    if (activeDialog == "DICTIONARY") {
        val userDict = remember { com.example.data.UserDictionaryManager(context) }
        var wordsList by remember { mutableStateOf(userDict.getUserWords()) }
        var newWordInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("قاموس الكلمات الشخصية", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(340.dp)) {
                    Text(
                        text = "أضف كلماتك ومصطلحاتك الخاصة ليتعلمها الكيبورد ولا يصححها:",
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newWordInput,
                            onValueChange = { newWordInput = it },
                            placeholder = { Text("اكتب كلمة جديدة...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newWordInput.isNotBlank()) {
                                    val added = userDict.addWord(newWordInput)
                                    if (added) {
                                        wordsList = userDict.getUserWords()
                                        Toast.makeText(context, "تمت إضافة '${newWordInput.trim()}' للقاموس ✓", Toast.LENGTH_SHORT).show()
                                        newWordInput = ""
                                    }
                                }
                            },
                            modifier = Modifier.height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text("إضافة +", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("الكلمات المحفوظة (${wordsList.size}):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (wordsList.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("لا توجد كلمات شخصية مضافة بعد.\nيمكنك إضافة كلمات هنا أو مباشرة أثناء الكتابة!", color = Color(0xFF8E9BAE), fontSize = 11.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            items(wordsList) { word ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF1E283A),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(word, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        IconButton(
                                            onClick = {
                                                userDict.removeWord(word)
                                                wordsList = userDict.getUserWords()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 11. SOUND & HAPTIC DIALOG (Screenshot 12)
    if (activeDialog == "SOUND_HAPTIC") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("الصوت والإهتزاز", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("استخدم اهتزاز النظام", color = Color.White, fontSize = 13.sp)
                        Checkbox(
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { updateSettings(settings.copy(vibrationEnabled = it)) }
                        )
                    }

                    Text("اهتزاز عند النقر على مفتاح (${settings.vibrationDurationMs} مللي ثانية)", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = settings.vibrationDurationMs.toFloat(),
                        onValueChange = { updateSettings(settings.copy(vibrationDurationMs = it.toInt())) },
                        valueRange = 0f..100f
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("صوت عند النقر على المفاتيح", color = Color.White, fontSize = 13.sp)
                        Checkbox(
                            checked = settings.soundEnabled,
                            onCheckedChange = { updateSettings(settings.copy(soundEnabled = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("انبثاق عند الضغط على المفاتيح", color = Color.White, fontSize = 13.sp)
                        Checkbox(
                            checked = settings.keyPopupEnabled,
                            onCheckedChange = { updateSettings(settings.copy(keyPopupEnabled = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 12. HEIGHT & FONT DIALOG (Screenshot 13)
    if (activeDialog == "HEIGHT_FONT") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("ارتفاع الكيبورد وحجم الأحرف", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("ارتفاع المفاتيح (عمودي): ${(settings.keyHeightFactor * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = settings.keyHeightFactor,
                        onValueChange = { updateSettings(settings.copy(keyHeightFactor = it)) },
                        valueRange = 0.8f..1.4f
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("حجم الأحرف على المفاتيح: ${(settings.keyFontSizeFactor * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = settings.keyFontSizeFactor,
                        onValueChange = { updateSettings(settings.copy(keyFontSizeFactor = it)) },
                        valueRange = 0.8f..1.3f
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 13. BACKUP DIALOG
    if (activeDialog == "BACKUP") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("النسخ الاحتياطي والاستعادة", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("يمكنك حفظ نسخة احتياطية من كافة نصوص الحافظة المحفوظة والاختصارات المخصصة واستعادتها بأي وقت.", color = Color(0xFF8E9BAE), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val backup = prefs.exportBackupJson()
                            Toast.makeText(context, "تم حفظ النسخة الاحتياطية بنجاح (${backup.length} حرف)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Text("إنشاء نسخة احتياطية الآن", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("إغلاق", color = Color.White)
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }
}

@Composable
fun TransboardSettingsRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF131A26),
        border = BorderStroke(1.dp, Color(0xFF1E283A)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 2
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
