package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.data.TextDecorator
import com.example.keyboard.allKeyboardToolbarTools
import com.example.model.KeyboardSettings
import com.example.model.TextShortcut
import com.example.model.ThemePresets
import com.example.sound.KeyboardSoundEngine
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    onNavigateToThemes: () -> Unit,
    onNavigateToClipboard: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

    var customRow1Input by remember(settings) {
        mutableStateOf(settings.customArabicRow1.ifBlank { "ض ص ق ف غ ع ه خ ح ج" })
    }
    var customRow2Input by remember(settings) {
        mutableStateOf(settings.customArabicRow2.ifBlank { "ش س ي ب ل ا ت ن م ك" })
    }
    var customRow3Input by remember(settings) {
        mutableStateOf(settings.customArabicRow3.ifBlank { "ظ ط ذ د ز ر و ة ث" })
    }

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
                    title = "المظهر والسمات",
                    subtitle = "اختر مظهر لوحة المفاتيح أو أنشئ مظهر مخصص",
                    icon = Icons.Outlined.Palette,
                    onClick = onNavigateToThemes
                )
            }

            // 5. تخطيط لوحة المفاتيح والأعمدة
            item {
                TransboardSettingsRow(
                    title = "تخطيط لوحة المفاتيح والأعمدة",
                    subtitle = "10 أعمدة (مثل الصورة) أو 11 أو 12 عمود، تعديل ترتيب الأحرف يدوياً، وحجم الأحرف ومربعات الأزرار",
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
                    subtitle = "التحكم في أصوات المفاتيح ونغمات النقر والاهتزاز بالملي ثانية",
                    icon = Icons.Outlined.VolumeUp,
                    onClick = { activeDialog = "SOUND_HAPTIC" }
                )
            }

            // 11.1 سرعة الكتابة واستجابة الأزرار
            item {
                TransboardSettingsRow(
                    title = "سرعة الكتابة واستجابة الأزرار",
                    subtitle = "زيادة أو تقليل سرعة الكتابة، تكرار الحذف، وتوقيت الضغط مثل آيفون 16",
                    icon = Icons.Outlined.Speed,
                    onClick = { activeDialog = "TYPING_SPEED" }
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

            // 14. قنوات التليجرام والتواصل
            item {
                TransboardSettingsRow(
                    title = "قنوات التليجرام والتواصل المباشر",
                    subtitle = "القناة الأولى (@Abu2Saleh) والقناة الثانية الرسمية",
                    icon = Icons.Outlined.Send,
                    onClick = { activeDialog = "TELEGRAM_CHANNELS" }
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

    // 5. KEYBOARD LAYOUTS DIALOG
    if (activeDialog == "LAYOUTS") {
        val columnOptions = listOf(
            Triple(10, "10_columns", "10 أعمدة (نفس الصورة / كيبورد Gboard)\nض ص ق ف غ ع ه خ ح ج"),
            Triple(11, "11_columns", "11 عمود (القياسي / SwiftKey)\nض ص ث ق ف غ ع ه خ ح ج"),
            Triple(12, "12_columns", "12 عمود (الممتد / سامسونج)\nض ص ث ق ف غ ع ه خ ح ج د"),
            Triple(10, "custom", "✏️ ترتيب مخصص يدوي للأحرف")
        )
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Text(
                    "تخطيط لوحة المفاتيح والأعمدة",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "عدد الأعمدة وترتيب الأحرف العربي:",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    columnOptions.forEach { (cols, code, title) ->
                        val isSelected = settings.keyboardLayoutStyle == code ||
                                (settings.keyboardLayoutStyle !in listOf("10_columns", "11_columns", "12_columns", "custom") && settings.arabicColumnsCount == cols && code.startsWith("${cols}_"))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF1C2436),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF28364F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable {
                                    updateSettings(
                                        settings.copy(
                                            keyboardLayoutStyle = code,
                                            arabicColumnsCount = cols
                                        )
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    title,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Button to manually customize letters
                    OutlinedButton(
                        onClick = {
                            activeDialog = "CUSTOM_LAYOUT"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00E5FF)
                        ),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "تغيير وترتيب الأحرف يدوياً...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Key Font Size
                    Text(
                        "حجم خط الأحرف: ${(settings.keyFontSizeFactor * 100).toInt()}%",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = settings.keyFontSizeFactor,
                        onValueChange = { updateSettings(settings.copy(keyFontSizeFactor = it)) },
                        valueRange = 0.75f..1.50f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 2. Key Button Box Scale
                    Text(
                        "حجم مربعات أزرار الكيبورد: ${(settings.keyButtonScale * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = settings.keyButtonScale,
                        onValueChange = { updateSettings(settings.copy(keyButtonScale = it)) },
                        valueRange = 0.75f..1.50f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 3. Key Height Factor
                    Text(
                        "ارتفاع أزرار الكيبورد: ${(settings.keyHeightFactor * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = settings.keyHeightFactor,
                        onValueChange = { updateSettings(settings.copy(keyHeightFactor = it)) },
                        valueRange = 0.70f..1.60f
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair("صغير 85%", 0.85f),
                            Pair("افتراضي 100%", 1.0f),
                            Pair("كبير 120%", 1.20f),
                            Pair("عملاق 140%", 1.40f)
                        ).forEach { (lbl, factor) ->
                            val isSel = kotlin.math.abs(settings.keyHeightFactor - factor) < 0.04f
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF00E5FF) else Color(0xFF1E2838),
                                modifier = Modifier.weight(1f).clickable {
                                    updateSettings(settings.copy(keyHeightFactor = factor))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else Color.White, fontSize = 9.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Backspace Key Scale
                    Text(
                        "حجم زر الحذف (Backspace): ${(settings.backspaceKeyScale * 100).toInt()}%",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = settings.backspaceKeyScale,
                        onValueChange = { updateSettings(settings.copy(backspaceKeyScale = it)) },
                        valueRange = 0.75f..1.80f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Emoji Bar Height Factor
                    Text(
                        "ارتفاع شريط الابتسامات العلوي: ${(settings.emojiBarHeightFactor * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = settings.emojiBarHeightFactor,
                        onValueChange = { updateSettings(settings.copy(emojiBarHeightFactor = it)) },
                        valueRange = 0.70f..1.80f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Emoji Button Size Factor
                    Text(
                        "حجم أزرار الابتسامات: ${(settings.emojiButtonSizeFactor * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = settings.emojiButtonSizeFactor,
                        onValueChange = { updateSettings(settings.copy(emojiButtonSizeFactor = it)) },
                        valueRange = 0.70f..1.80f
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("صف الأرقام العلوي", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = settings.numberRowEnabled,
                            onCheckedChange = { updateSettings(settings.copy(numberRowEnabled = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تثبيت الأرقام الإنجليزية (123)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("كتابة 123 مع الأحرف العربية", color = Color(0xFF00E5FF), fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.forceEnglishNumbers,
                            onCheckedChange = { updateSettings(settings.copy(forceEnglishNumbers = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الرموز والأحرف البديلة", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = settings.showDualHints,
                            onCheckedChange = { updateSettings(settings.copy(showDualHints = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الرجوع التلقائي للأحرف بعد الإرسال", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = settings.autoReturnToLettersOnSend,
                            onCheckedChange = { updateSettings(settings.copy(autoReturnToLettersOnSend = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم وحفظ", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        updateSettings(
                            settings.copy(
                                keyboardLayoutStyle = "10_columns",
                                arabicColumnsCount = 10,
                                keyButtonScale = 1.0f,
                                keyHeightFactor = 1.0f,
                                keyFontSizeFactor = 1.0f,
                                backspaceKeyScale = 1.15f
                            )
                        )
                        Toast.makeText(context, "تمت استعادة الإعدادات الافتراضية (10 أعمدة) ✓", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("إعادة ضبط الأحجام", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 5.B MANUAL CUSTOM LAYOUT DIALOG
    if (activeDialog == "CUSTOM_LAYOUT") {
        AlertDialog(
            onDismissRequest = { activeDialog = "LAYOUTS" },
            title = {
                Text(
                    "تغيير وترتيب الأحرف يدوياً",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "اكتب أو رتّب الحروف التي تريدها في كل صف من صفوف الكيبورد (افصل بينها بمسافة أو بدون مسافة):",
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("الصف الأول (الأعلى):", color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = customRow1Input,
                        onValueChange = { customRow1Input = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF28364F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الصف الثاني (الأوسط):", color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = customRow2Input,
                        onValueChange = { customRow2Input = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF28364F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الصف الثالث (الأسفل قبل المسطرة):", color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = customRow3Input,
                        onValueChange = { customRow3Input = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF28364F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("قوالب سريعة:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                customRow1Input = "ض ص ق ف غ ع ه خ ح ج"
                                customRow2Input = "ش س ي ب ل ا ت ن م ك"
                                customRow3Input = "ظ ط ذ د ز ر و ة ث"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Text("10 أعمدة (الصورة)", fontSize = 9.5.sp, color = Color(0xFF00E5FF))
                        }

                        OutlinedButton(
                            onClick = {
                                customRow1Input = "ض ص ث ق ف غ ع ه خ ح ج"
                                customRow2Input = "ش س ي ب ل ا ت ن م ك ط"
                                customRow3Input = "ذ ء ؤ ر ى ة و ز ظ د"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, Color(0xFF28364F))
                        ) {
                            Text("11 عمود", fontSize = 9.5.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                customRow1Input = "ض ص ث ق ف غ ع ه خ ح ج د"
                                customRow2Input = "ش س ي ب ل ا ت ن م ك ط"
                                customRow3Input = "ذ ئ ء ؤ ر لا ى ة و ز ظ"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, Color(0xFF28364F))
                        ) {
                            Text("12 عمود", fontSize = 9.5.sp, color = Color.White)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        updateSettings(
                            settings.copy(
                                keyboardLayoutStyle = "custom",
                                customArabicRow1 = customRow1Input.trim(),
                                customArabicRow2 = customRow2Input.trim(),
                                customArabicRow3 = customRow3Input.trim()
                            )
                        )
                        activeDialog = "LAYOUTS"
                        Toast.makeText(context, "تم حفظ الترتيب المخصص للأحرف بنجاح ✓", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("حفظ الترتيب", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDialog = "LAYOUTS" }) {
                    Text("إلغاء", color = Color(0xFF94A3B8))
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

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    // Feature 4: تبديل مكان الحافظة مكان الإيموجي في الصف السفلي
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تبديل مكان الحافظة مكان الإيموجي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (settings.swapClipboardAndEmoji) "الحافظة على اليسار [📋] والإيموجي على اليمين [😊]" else "الإيموجي على اليسار [😊] والحافظة على اليمين [📋]",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = settings.swapClipboardAndEmoji,
                            onCheckedChange = { updateSettings(settings.copy(swapClipboardAndEmoji = it)) }
                        )
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
                Column(modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp, max = 450.dp)) {
                    Text(
                        text = "أضف اختصاراتك السريعة؛ عند كتابة رمز الاختصار (مثل: سلام) سيظهر النص الكامل فوراً في شريط الاقتراحات:",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTrigger,
                            onValueChange = { newTrigger = it },
                            placeholder = { Text("الرمز: سلام", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            modifier = Modifier.weight(1.1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF0F1522),
                                unfocusedContainerColor = Color(0xFF0F1522)
                            )
                        )
                        OutlinedTextField(
                            value = newExpansion,
                            onValueChange = { newExpansion = it },
                            placeholder = { Text("النص الكامل البديل", fontSize = 11.sp, color = Color(0xFF64748B)) },
                            modifier = Modifier.weight(1.8f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF334155),
                                focusedContainerColor = Color(0xFF0F1522),
                                unfocusedContainerColor = Color(0xFF0F1522)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newTrigger.isNotBlank() && newExpansion.isNotBlank()) {
                                prefs.addShortcut(newTrigger.trim(), newExpansion.trim())
                                shortcutsList = prefs.getShortcuts()
                                newTrigger = ""
                                newExpansion = ""
                                Toast.makeText(context, "تمت إضافة الاختصار بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة الاختصار الجديد ➕", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("قائمة الاختصارات المحفوظة (${shortcutsList.size}):", color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        items(shortcutsList) { item ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1B2335),
                                border = BorderStroke(1.dp, Color(0xFF28364F)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                                border = BorderStroke(1.dp, Color(0xFF00E5FF))
                                            ) {
                                                Text(
                                                    text = item.trigger,
                                                    color = Color(0xFF00E5FF),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.5.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("➔ يُستبدل بـ:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.expansion,
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 18.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            prefs.deleteShortcut(item.id)
                                            shortcutsList = prefs.getShortcuts()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تثبيت الأرقام الإنجليزية (123)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (settings.forceEnglishNumbers) "الأرقام تظهر وتُكتب إنجليزية (123) حتى مع العربي ✓" else "الأرقام هندية / مشرقية (١٢٣)",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = settings.forceEnglishNumbers,
                            onCheckedChange = { updateSettings(settings.copy(forceEnglishNumbers = it)) }
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

                    if (settings.topQuickEmojiRowEnabled) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeDialog = "CUSTOMIZE_TOP_EMOJIS" }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Mood, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تخصيص الابتسامات في الصف العلوي...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    // التحكم في حجم الفيسات والإيموجي
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text("التحكم في حجم الفيسات والإيموجي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "صغير" to 0.85f,
                                "متوسط" to 1.0f,
                                "كبير" to 1.25f,
                                "ضخم" to 1.5f
                            ).forEach { (label, scale) ->
                                val isSelected = kotlin.math.abs(settings.emojiSizeScale - scale) < 0.12f
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF28364F)),
                                    modifier = Modifier.weight(1f).clickable {
                                        updateSettings(settings.copy(emojiSizeScale = scale))
                                    }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color(0xFF00E5FF) else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F1522))
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("😊", "🔥", "❤️", "👍", "✨", "🚀", "👑").forEach { em ->
                                Text(text = em, fontSize = (22 * settings.emojiSizeScale).sp)
                            }
                        }
                    }

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    // تبديل مكان الحافظة ومكان الإيموجي في الصف السفلي
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تبديل مكان الحافظة مكان الإيموجي", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (settings.swapClipboardAndEmoji) "الحافظة على اليسار [📋] والإيموجي على اليمين [😊]" else "الإيموجي على اليسار [😊] والحافظة على اليمين [📋]",
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = settings.swapClipboardAndEmoji,
                            onCheckedChange = { updateSettings(settings.copy(swapClipboardAndEmoji = it)) }
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

    // CUSTOMIZE TOP EMOJIS DIALOG (تخصيص شريط الابتسامات العلوي)
    if (activeDialog == "CUSTOMIZE_TOP_EMOJIS") {
        var topEmojisList by remember { mutableStateOf(prefs.getCustomTopEmojis().toMutableList()) }
        var newCustomEmoji by remember { mutableStateOf("") }
        val sampleEmojis = listOf("😂", "❤️", "🥺", "🔥", "👏", "🤍", "😍", "✨", "🤲", "🌹", "🌸", "👍", "👑", "💋", "💯", "🙈", "🖤", "🤩", "🕊️", "💎", "🍿", "🚀")

        AlertDialog(
            onDismissRequest = { activeDialog = "ROWS_TOOLBAR" },
            containerColor = Color(0xFF141926),
            title = {
                Text(
                    text = "تخصيص شريط الابتسامات العلوي",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "اضغط على أي ابتسامة لحذفها، أو اختر من القائمة أدناه لإضافتها للشريط:",
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Current chips
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(topEmojisList) { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f)),
                                modifier = Modifier.clickable {
                                    topEmojisList = topEmojisList.toMutableList().apply { remove(item) }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newCustomEmoji,
                            onValueChange = { newCustomEmoji = it },
                            placeholder = { Text("اكتب إيموجي أو رمزاً...", fontSize = 11.sp, color = Color(0xFF8E9BAE)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF28364F)
                            )
                        )
                        Button(
                            onClick = {
                                val t = newCustomEmoji.trim()
                                if (t.isNotEmpty() && !topEmojisList.contains(t)) {
                                    topEmojisList = topEmojisList.toMutableList().apply { add(t) }
                                    newCustomEmoji = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text("إضافة", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("مقترحات سريعة:", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(sampleEmojis) { emo ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier.clickable {
                                    if (!topEmojisList.contains(emo)) {
                                        topEmojisList = topEmojisList.toMutableList().apply { add(emo) }
                                    }
                                }
                            ) {
                                Text(emo, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (topEmojisList.isEmpty()) {
                            topEmojisList = mutableListOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
                        }
                        prefs.saveCustomTopEmojis(topEmojisList)
                        activeDialog = "ROWS_TOOLBAR"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    Text("حفظ", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        topEmojisList = mutableListOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
                    }
                ) {
                    Text("استعادة الافتراضي", color = Color(0xFF8E9BAE))
                }
            }
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

    // 9.1 AI TONE & STRICT SPELL CHECK DIALOG
    if (activeDialog == "AI_TONE") {
        var testText by remember { mutableStateOf("هدا النص فى اخطاء كثيره شكرن جزيلا") }
        var isSpellCheckMode by remember { mutableStateOf(true) }
        var selectedTone by remember { mutableStateOf("formal") }
        val stats = remember(testText) {
            com.example.data.SpellCheckDictionary.getCorrectionStats(testText, isArabic = true)
        }
        val spellResult = remember(testText) {
            com.example.data.SpellCheckDictionary.autoCorrectSentence(testText, isArabic = true)
        }
        val toneResult = remember(testText, selectedTone) {
            com.example.data.AiToneEngine.transformTone(testText, selectedTone)
        }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Spellcheck, contentDescription = null, tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("التدقيق الإملائي الصارم والنبرات", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(modifier = Modifier.height(410.dp)) {
                    // Mode selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSpellCheckMode) Color(0xFF10B981) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSpellCheckMode = true }
                        ) {
                            Text(
                                text = "التصحيح الإملائي الصارم",
                                color = if (isSpellCheckMode) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (!isSpellCheckMode) Color(0xFF818CF8) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSpellCheckMode = false }
                        ) {
                            Text(
                                text = "تغيير النبرة",
                                color = if (!isSpellCheckMode) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("اكتب نصاً للفحص والتصحيح") },
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSpellCheckMode) {
                        // Strict Spell Check View
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF161F2E), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF24334A), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المدخل: ${stats.inputWordCount} ⟵ المخرج: ${stats.outputWordCount} كلمة",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "تطابق الحجم 100% ✓",
                                    color = Color(0xFF34D399),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "شروط صارمة: تصحيح حروف الكلمات فقط بدون إضافة كلمات أو إكمال جمل.",
                            color = Color(0xFF8E9BAE),
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("النتيجة المصححة إملائياً:", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF161F2E),
                            border = BorderStroke(1.dp, Color(0xFF2A3A54)),
                            modifier = Modifier.fillMaxWidth().weight(1f).padding(2.dp)
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Text(spellResult, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp)
                            }
                        }
                    } else {
                        // Tone selection
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
                                        .clickable { selectedTone = tone.id }
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

    // 11. SOUND & HAPTIC DIALOG (أصوات واهتزازات الكيبورد المخصصة)
    if (activeDialog == "SOUND_HAPTIC") {
        val currentTheme = remember(settings.currentThemeId) { prefs.getActiveTheme() }
        val isLight = remember(currentTheme) { ThemePresets.isLightColor(currentTheme.backgroundColor) }
        val dBg = if (isLight) Color(currentTheme.backgroundColor) else Color(0xFF141A28)
        val dText = if (isLight) Color(0xFF0F172A) else Color.White
        val dSub = if (isLight) Color(0xFF475569) else Color(0xFF8E9BAE)
        val dCard = if (isLight) Color.White else Color(0xFF1B2333)
        val dAccent = Color(currentTheme.accentColor)

        val vibrator = remember {
            try {
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } catch (_: Exception) { null }
        }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = dAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "أصوات واهتزازات الكيبورد",
                        color = dText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // --- 1. قسم الأصوات ---
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dCard,
                            border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF26334A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("أصوات النقر أثناء الكتابة", color = dText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("تشغيل نغمة صوتية واقعية عند لمس كل زر", color = dSub, fontSize = 10.sp)
                                    }
                                    Switch(
                                        checked = settings.soundEnabled,
                                        onCheckedChange = {
                                            updateSettings(settings.copy(soundEnabled = it))
                                            if (it) {
                                                KeyboardSoundEngine.initialize(context)
                                                KeyboardSoundEngine.playKeySound(settings.soundProfile, settings.soundVolume)
                                            }
                                        }
                                    )
                                }

                                if (settings.soundEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("اختر صوت الكيبورد المفضل لديك:", color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        KeyboardSoundEngine.AVAILABLE_PROFILES.forEach { prof ->
                                            val isSelected = settings.soundProfile == prof.id
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isSelected) dAccent.copy(alpha = 0.18f) else if (isLight) Color(0xFFF8FAFC) else Color(0xFF141924),
                                                border = BorderStroke(
                                                    width = if (isSelected) 1.5.dp else 0.8.dp,
                                                    color = if (isSelected) dAccent else if (isLight) Color(0xFFCBD5E1) else Color(0xFF26334A)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        updateSettings(settings.copy(soundProfile = prof.id))
                                                        KeyboardSoundEngine.initialize(context)
                                                        KeyboardSoundEngine.playKeySound(prof.id, settings.soundVolume)
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        RadioButton(
                                                            selected = isSelected,
                                                            onClick = {
                                                                updateSettings(settings.copy(soundProfile = prof.id))
                                                                KeyboardSoundEngine.initialize(context)
                                                                KeyboardSoundEngine.playKeySound(prof.id, settings.soundVolume)
                                                            },
                                                            colors = RadioButtonDefaults.colors(selectedColor = dAccent)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = prof.nameAr,
                                                            color = if (isSelected) dAccent else dText,
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    }

                                                    // استماع سريع
                                                    IconButton(
                                                        onClick = {
                                                            KeyboardSoundEngine.initialize(context)
                                                            KeyboardSoundEngine.playKeySound(prof.id, settings.soundVolume)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.VolumeUp, contentDescription = "تجربة", tint = dAccent, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("مستوى صوت المفاتيح", color = dText, fontSize = 11.sp)
                                        Text("${(settings.soundVolume * 100).toInt()}%", color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = settings.soundVolume,
                                        onValueChange = { updateSettings(settings.copy(soundVolume = it)) },
                                        onValueChangeFinished = {
                                            KeyboardSoundEngine.initialize(context)
                                            KeyboardSoundEngine.playKeySound(settings.soundProfile, settings.soundVolume)
                                        },
                                        valueRange = 0.1f..1.0f
                                    )
                                }
                            }
                        }
                    }

                    // --- 2. قسم الاهتزاز اللمسي ---
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dCard,
                            border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF26334A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("الاهتزاز اللمسي (Haptic Feedback)", color = dText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("اهتزاز ملموس ومريح عند الضغط على الأحرف", color = dSub, fontSize = 10.sp)
                                    }
                                    Switch(
                                        checked = settings.vibrationEnabled,
                                        onCheckedChange = {
                                            updateSettings(settings.copy(vibrationEnabled = it))
                                            if (it && vibrator != null && vibrator.hasVibrator()) {
                                                try {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        vibrator.vibrate(VibrationEffect.createOneShot(settings.vibrationDurationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        vibrator.vibrate(settings.vibrationDurationMs.toLong())
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                        }
                                    )
                                }

                                if (settings.vibrationEnabled) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("قوة الاهتزاز السريعة:", color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Quick Presets (خفيف، متوسط آيفون، قوي، قوي جداً)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        listOf(
                                            Pair("خفيف (15ms)", 15),
                                            Pair("متوسط (30ms)", 30),
                                            Pair("قوي (45ms)", 45),
                                            Pair("شديد (60ms)", 60)
                                        ).forEach { (label, ms) ->
                                            val isSel = settings.vibrationDurationMs == ms
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSel) dAccent else if (isLight) Color(0xFFF1F5F9) else Color(0xFF141924),
                                                border = BorderStroke(0.8.dp, if (isSel) dAccent else if (isLight) Color(0xFFCBD5E1) else Color(0xFF26334A)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        updateSettings(settings.copy(vibrationDurationMs = ms))
                                                        if (vibrator != null && vibrator.hasVibrator()) {
                                                            try {
                                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                    vibrator.vibrate(VibrationEffect.createOneShot(ms.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
                                                                } else {
                                                                    @Suppress("DEPRECATION")
                                                                    vibrator.vibrate(ms.toLong())
                                                                }
                                                            } catch (_: Exception) {}
                                                        }
                                                    }
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 6.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        color = if (isSel) Color.Black else dText,
                                                        fontSize = 9.sp,
                                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("تعديل دقيق بالمللي ثانية:", color = dText, fontSize = 11.sp)
                                        Text("${settings.vibrationDurationMs} ms", color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = settings.vibrationDurationMs.toFloat(),
                                        onValueChange = { updateSettings(settings.copy(vibrationDurationMs = it.toInt())) },
                                        onValueChangeFinished = {
                                            if (vibrator != null && vibrator.hasVibrator()) {
                                                try {
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        vibrator.vibrate(VibrationEffect.createOneShot(settings.vibrationDurationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        vibrator.vibrate(settings.vibrationDurationMs.toLong())
                                                    }
                                                } catch (_: Exception) {}
                                            }
                                        },
                                        valueRange = 5f..80f
                                    )
                                }
                            }
                        }
                    }

                    // --- 3. استجابة وتوقيت الضغط مثل كيبورد iOS 16 ---
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dCard,
                            border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF26334A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("إحساس وتوقيت الضغط على الأزرار:", color = dText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("اختر بين الاستجابة السلسة المتوازنة (مثل آيفون 16) أو الفائقة السرعة:", color = dSub, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // نمط آيفون المتوازن
                                    val isIos = settings.keyPressTimingStyle == "ios_balanced"
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isIos) dAccent.copy(alpha = 0.2f) else if (isLight) Color(0xFFF1F5F9) else Color(0xFF141924),
                                        border = BorderStroke(if (isIos) 1.5.dp else 0.8.dp, if (isIos) dAccent else if (isLight) Color(0xFFCBD5E1) else Color(0xFF26334A)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { updateSettings(settings.copy(keyPressTimingStyle = "ios_balanced")) }
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("🍏 آيفون iOS 16", color = if (isIos) dAccent else dText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("متوسط وسلس بدون ثقل، مريح جداً للكتابة", color = dSub, fontSize = 9.sp)
                                        }
                                    }

                                    // نمط فائق السرعة
                                    val isFast = settings.keyPressTimingStyle == "ultra_fast"
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isFast) dAccent.copy(alpha = 0.2f) else if (isLight) Color(0xFFF1F5F9) else Color(0xFF141924),
                                        border = BorderStroke(if (isFast) 1.5.dp else 0.8.dp, if (isFast) dAccent else if (isLight) Color(0xFFCBD5E1) else Color(0xFF26334A)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { updateSettings(settings.copy(keyPressTimingStyle = "ultra_fast")) }
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("⚡ فائق السرعة", color = if (isFast) dAccent else dText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("استجابة فورية 0ms للمحترفين", color = dSub, fontSize = 9.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("معاينة الحرف العائم (Popup Preview)", color = dText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("ظهور فقاعة الحرف بانسيابية فوق المفتاح عند اللمس", color = dSub, fontSize = 9.5.sp)
                                    }
                                    Switch(
                                        checked = settings.keyPopupEnabled,
                                        onCheckedChange = { updateSettings(settings.copy(keyPopupEnabled = it)) }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = dAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تم وحفظ الإعدادات ✓", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            containerColor = dBg
        )
    }

    // 11.1 TYPING SPEED & KEY TIMINGS DIALOG
    if (activeDialog == "TYPING_SPEED") {
        val currentTheme = remember(settings.currentThemeId) { prefs.getActiveTheme() }
        val isLight = remember(currentTheme) { ThemePresets.isLightColor(currentTheme.backgroundColor) }
        val dBg = if (isLight) Color(currentTheme.backgroundColor) else Color(0xFF141A28)
        val dText = if (isLight) Color(0xFF0F172A) else Color.White
        val dSub = if (isLight) Color(0xFF475569) else Color(0xFF8E9BAE)
        val dCard = if (isLight) Color.White else Color(0xFF1B2333)
        val dAccent = Color(currentTheme.accentColor)

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Speed,
                        contentDescription = null,
                        tint = dAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سرعة الكتابة واستجابة الأزرار",
                        color = dText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // أنماط السرعة الجاهزة
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dCard,
                            border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF26334A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("أنماط سرعة الاستجابة الجاهزة:", color = dText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Text("اختر النمط المناسب لأسلوب كتابتك:", color = dSub, fontSize = 10.sp)
                                Spacer(modifier = Modifier.height(8.dp))

                                val speedPresets = listOf(
                                    Triple("⚡ فائق السرعة", "fast", "استجابة فورية 0ms وسرعة حذف خارقة"),
                                    Triple("🍏 متوسط متوازن (مثل آيفون 16)", "medium", "سلس ومتوازن بدون ثقل أو خطأ (موصى به)"),
                                    Triple("🧘 هادئ وبطيء ودقيق", "slow", "كتابة متأنية لتقليل الأخطاء الإملائية")
                                )

                                speedPresets.forEach { (title, mode, desc) ->
                                    val isSelected = settings.typingSpeedMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) dAccent.copy(alpha = 0.2f) else if (isLight) Color(0xFFF8FAFC) else Color(0xFF141924),
                                        border = BorderStroke(if (isSelected) 1.5.dp else 0.8.dp, if (isSelected) dAccent else if (isLight) Color(0xFFCBD5E1) else Color(0xFF26334A)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clickable {
                                                val (rep, longD) = when (mode) {
                                                    "fast" -> Pair(25, 200)
                                                    "slow" -> Pair(80, 520)
                                                    else -> Pair(45, 340)
                                                }
                                                updateSettings(
                                                    settings.copy(
                                                        typingSpeedMode = mode,
                                                        keyRepeatSpeedMs = rep,
                                                        longPressDelayMs = longD,
                                                        keyPressTimingStyle = if (mode == "fast") "ultra_fast" else "ios_balanced"
                                                    )
                                                )
                                                KeyboardSoundEngine.initialize(context)
                                                KeyboardSoundEngine.playKeySound(settings.soundProfile, settings.soundVolume)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(title, color = if (isSelected) dAccent else dText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text(desc, color = dSub, fontSize = 9.5.sp)
                                            }
                                            if (isSelected) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = dAccent, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // التعديل اليدوي المتقدم للسرعة
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dCard,
                            border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF26334A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("تخصيص دقيق بالمللي ثانية:", color = dText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Slider 1: سرعة تكرار الحذف
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("سرعة تكرار الحذف (Backspace Repeat)", color = dText, fontSize = 11.sp)
                                    Text("${settings.keyRepeatSpeedMs} ms", color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = settings.keyRepeatSpeedMs.toFloat(),
                                    onValueChange = {
                                        updateSettings(settings.copy(keyRepeatSpeedMs = it.toInt(), typingSpeedMode = "custom"))
                                    },
                                    valueRange = 20f..100f
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Slider 2: تأخير الضغط المطول
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تأخير الضغط المطول للحركات والرموز", color = dText, fontSize = 11.sp)
                                    Text("${settings.longPressDelayMs} ms", color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = settings.longPressDelayMs.toFloat(),
                                    onValueChange = {
                                        updateSettings(settings.copy(longPressDelayMs = it.toInt(), typingSpeedMode = "custom"))
                                    },
                                    valueRange = 150f..600f
                                )
                            }
                        }
                    }

                    // الرجوع التلقائي للأحرف بعد الإرسال والاختصارات
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = dCard,
                            border = BorderStroke(1.dp, if (isLight) Color(0xFFE2E8F0) else Color(0xFF26334A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("الرجوع التلقائي للأحرف (Auto-Return to Letters):", color = dText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Toggle 1: بعد الإرسال
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("الرجوع للحروف فوراً عند الضغط على إرسال", color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text("العودة التلقائية للوحة الحروف بعد إرسال الرسائل والإيموجي", color = dSub, fontSize = 9.5.sp)
                                    }
                                    Switch(
                                        checked = settings.autoReturnToLettersOnSend,
                                        onCheckedChange = { updateSettings(settings.copy(autoReturnToLettersOnSend = it)) }
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = if (isLight) Color(0xFFE2E8F0) else Color(0xFF26334A))

                                // Toggle 2: بعد الاختصارات
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("الرجوع للحروف بعد إدراج نصوص الشريط والاختصارات", color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text("العودة للأحرف بعد إدراج الكليشات أو الحافظة أو النصوص السريعة", color = dSub, fontSize = 9.5.sp)
                                    }
                                    Switch(
                                        checked = settings.autoReturnToLettersOnShortcut,
                                        onCheckedChange = { updateSettings(settings.copy(autoReturnToLettersOnShortcut = it)) }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = dAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("تم وحفظ سرعة الكتابة ✓", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            containerColor = dBg
        )
    }

    // 12. HEIGHT & FONT DIALOG (Screenshot 13)
    if (activeDialog == "HEIGHT_FONT") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text("ارتفاع الكيبورد وحجم الأحرف", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("تكبير وتصغير أزرار الكيبورد: ${(settings.keyButtonScale * 100).toInt()}%", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = settings.keyButtonScale,
                        onValueChange = { updateSettings(settings.copy(keyButtonScale = it)) },
                        valueRange = 0.75f..1.6f
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("حجم زر الحذف (Backspace): ${(settings.backspaceKeyScale * 100).toInt()}%", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = settings.backspaceKeyScale,
                        onValueChange = { updateSettings(settings.copy(backspaceKeyScale = it)) },
                        valueRange = 0.75f..1.8f
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("ارتفاع المفاتيح (عمودي): ${(settings.keyHeightFactor * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = settings.keyHeightFactor,
                        onValueChange = { updateSettings(settings.copy(keyHeightFactor = it)) },
                        valueRange = 0.70f..1.60f
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair("صغير 85%", 0.85f),
                            Pair("طبيعي 100%", 1.0f),
                            Pair("كبير 120%", 1.20f),
                            Pair("عملاق 140%", 1.40f)
                        ).forEach { (lbl, factor) ->
                            val isSel = kotlin.math.abs(settings.keyHeightFactor - factor) < 0.04f
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF00E5FF) else Color(0xFF1E2838),
                                modifier = Modifier.weight(1f).clickable {
                                    updateSettings(settings.copy(keyHeightFactor = factor))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else Color.White, fontSize = 8.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("حجم الأحرف على المفاتيح: ${(settings.keyFontSizeFactor * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = settings.keyFontSizeFactor,
                        onValueChange = { updateSettings(settings.copy(keyFontSizeFactor = it)) },
                        valueRange = 0.8f..1.3f
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            updateSettings(
                                settings.copy(
                                    keyButtonScale = 1.0f,
                                    backspaceKeyScale = 1.0f,
                                    keyHeightFactor = 1.0f,
                                    keyFontSizeFactor = 1.0f
                                )
                            )
                            Toast.makeText(context, "تمت استعادة الحجم الافتراضي للكيبورد (100%) ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("↺ استعادة الحجم الافتراضي للكيبورد", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text("تم وحفظ ✓", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        updateSettings(
                            settings.copy(
                                keyButtonScale = 1.0f,
                                backspaceKeyScale = 1.0f,
                                keyHeightFactor = 1.0f,
                                keyFontSizeFactor = 1.0f
                            )
                        )
                        Toast.makeText(context, "تمت استعادة الحجم الافتراضي ✓", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("إعادة ضبط", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 13. FULL BACKUP & RESTORE DIALOG (النسخ الاحتياطي والاستعادة لبيانات واعدادات التطبيق بالكامل)
    if (activeDialog == "BACKUP") {
        var backupTab by remember { mutableStateOf("EXPORT") } // EXPORT or IMPORT
        var restoreCodeInput by remember { mutableStateOf("") }
        var isRestoring by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CloudUpload,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("النسخ الاحتياطي والاستعادة الكاملة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (backupTab == "EXPORT") Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF1E283A),
                            border = BorderStroke(1.dp, if (backupTab == "EXPORT") Color(0xFF00E5FF) else Color(0xFF28364F)),
                            modifier = Modifier.weight(1f).clickable { backupTab = "EXPORT" }
                        ) {
                            Text(
                                text = "📤 إنشاء نسخة احتياطية",
                                color = if (backupTab == "EXPORT") Color(0xFF00E5FF) else Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (backupTab == "IMPORT") Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF1E283A),
                            border = BorderStroke(1.dp, if (backupTab == "IMPORT") Color(0xFF00E5FF) else Color(0xFF28364F)),
                            modifier = Modifier.weight(1f).clickable { backupTab = "IMPORT" }
                        ) {
                            Text(
                                text = "📥 استعادة نسخة سابقة",
                                color = if (backupTab == "IMPORT") Color(0xFF00E5FF) else Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (backupTab == "EXPORT") {
                        Text(
                            text = "تشمل النسخة الاحتياطية كافة إعدادات التطبيق بالكامل:\n• الثيمات المخصصة وحجم الكيبورد\n• إعدادات الصوت والاهتزاز وحجم الفيسات\n• نصوص الحافظة والاختصارات المحفوظة\n• شريط الابتسامات المخصص وقاموس الكلمات",
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Button 1: Copy to clipboard
                        Button(
                            onClick = {
                                val backup = prefs.exportBackupJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("TurboKeyboardBackup", backup)
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "📋 تم نسخ كود النسخة الاحتياطية للحافظة بنجاح!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text("📋 نسخ كود النسخة الاحتياطية", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Button 2: Share via apps
                        OutlinedButton(
                            onClick = {
                                val backup = prefs.exportBackupJson()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "نسخة احتياطية - كيبورد برو")
                                    putExtra(Intent.EXTRA_TEXT, backup)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة النسخة الاحتياطية").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.7f))
                        ) {
                            Text("📤 مشاركة وحفظ النسخة الاحتياطية", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            text = "قم بلصق كود النسخة الاحتياطية في الحقل أدناه لاستعادة جميع البيانات والإعدادات فوراً:",
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Paste from clipboard button
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1E283A),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                    val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        restoreCodeInput = text
                                        Toast.makeText(context, "تم لصق كود النسخة!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "الحافظة فارغة", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text("📋 لصق من الحافظة", color = Color(0xFF38BDF8), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = restoreCodeInput,
                            onValueChange = { restoreCodeInput = it },
                            placeholder = { Text("الصق كود النسخة الاحتياطية هنا...", color = Color(0xFF64748B), fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF28364F),
                                focusedContainerColor = Color(0xFF0F1522),
                                unfocusedContainerColor = Color(0xFF0F1522)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (restoreCodeInput.isBlank()) {
                                    Toast.makeText(context, "يرجى لصق كود النسخة أولاً", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isRestoring = true
                                coroutineScope.launch(Dispatchers.IO) {
                                    val success = prefs.restoreBackupJson(restoreCodeInput.trim())
                                    withContext(Dispatchers.Main) {
                                        isRestoring = false
                                        if (success) {
                                            val reloadedSettings = prefs.getSettings()
                                            settings = reloadedSettings
                                            customRow1Input = reloadedSettings.customArabicRow1.ifBlank { "ض ص ق ف غ ع ه خ ح ج" }
                                            customRow2Input = reloadedSettings.customArabicRow2.ifBlank { "ش س ي ب ل ا ت ن م ك" }
                                            customRow3Input = reloadedSettings.customArabicRow3.ifBlank { "ظ ط ذ د ز ر و ة ث" }
                                            Toast.makeText(context, "✅ تم استعادة كافة الإعدادات والبيانات بنجاح!", Toast.LENGTH_LONG).show()
                                            activeDialog = null
                                        } else {
                                            Toast.makeText(context, "❌ كود النسخة غير صالح أو تالف", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            enabled = !isRestoring,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("جاري استعادة البيانات بأمان...", color = Color.White, fontSize = 12.sp)
                            } else {
                                Text("✅ استعادة كافة البيانات والإعدادات الآن", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
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

    // 14. TELEGRAM CHANNELS DIALOG (قنوات التليجرام والتواصل)
    if (activeDialog == "TELEGRAM_CHANNELS") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        tint = Color(0xFF0088CC),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("قنوات التليجرام الرسمية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "تابع آخر التحديثات والإصدارات والدعم الفني عبر قنواتنا الرسمية على تليجرام:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Channel 1
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/Abu2Saleh")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF182234),
                        border = BorderStroke(1.2.dp, Color(0xFF0088CC).copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📢 القناة الأولى: قناة أبو صالح الرسمية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("t.me/Abu2Saleh", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0088CC)) {
                                Text("انضمام", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Channel 2
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+OWBdNfDNS8MzYzQ0")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF182234),
                        border = BorderStroke(1.2.dp, Color(0xFF0088CC).copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("🚀 القناة الثانية: قناة المطور والدعم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("t.me/+OWBdNfDNS8MzYzQ0", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0088CC)) {
                                Text("انضمام", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
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
