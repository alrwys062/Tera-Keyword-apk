package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import com.example.util.AppStrings
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
    initialAppLanguageCode: String = "auto",
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
    val flowLangCode by com.example.util.LocaleHelper.appLanguageFlow.collectAsState()
    var appLanguageCode by remember(flowLangCode, initialAppLanguageCode) {
        mutableStateOf(
            if (flowLangCode != "auto") flowLangCode
            else if (initialAppLanguageCode != "auto") initialAppLanguageCode
            else com.example.util.LocaleHelper.getAppLanguageCode(context)
        )
    }
    var appLanguageSearchQuery by remember { mutableStateOf("") }

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

    val currentLayoutDir = remember(appLanguageCode) {
        com.example.util.LocaleHelper.getLayoutDirectionForCode(context, appLanguageCode)
    }

    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides currentLayoutDir
    ) {
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
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_chat_simulator_screen_003"),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = AppStrings.trCode(context, appLanguageCode, "app_settings"),
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
            placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "search_placeholder"), color = Color(0xFF8E9BAE), fontSize = 12.sp) },
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
            // 0. لغة التطبيق والإعدادات (App UI Language)
            item {
                val currentLangName = remember(appLanguageCode) {
                    val found = com.example.util.LocaleHelper.supportedLanguages.find { it.code == appLanguageCode }
                    if (found != null) "${found.flag} ${found.nameNative} (${found.nameAr})" else "🌐 تلقائي (لغة الجوال)"
                }
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "app_language"),
                    subtitle = "${AppStrings.trCode(context, appLanguageCode, "app_language_desc")} ($currentLangName)",
                    icon = Icons.Outlined.Public,
                    onClick = { activeDialog = "APP_UI_LANGUAGE" }
                )
            }

            // 1. لغات الإدخال (Screenshots 1, 3, SettingsInputKeyboards)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "input_languages"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "input_languages_desc"),
                    icon = Icons.Outlined.Language,
                    onClick = { showLanguagesScreen = true }
                )
            }

            // 2. الترجمة (Screenshot 1 & 4)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "translation"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "translation_desc"),
                    icon = Icons.Outlined.Translate,
                    onClick = { activeDialog = "TRANSLATE" }
                )
            }

            // 3. الزخرفة (Screenshot 1 & 5)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "decoration"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "decoration_desc"),
                    icon = Icons.Outlined.AutoAwesome,
                    onClick = { activeDialog = "DECORATION" }
                )
            }

            // 4. المظهر (Screenshot 1 & 6)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "appearance"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "appearance_desc"),
                    icon = Icons.Outlined.Palette,
                    onClick = onNavigateToThemes
                )
            }

            // 5. تخطيط لوحة المفاتيح والأعمدة
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "layouts"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "layouts_desc"),
                    icon = Icons.Outlined.Keyboard,
                    onClick = { activeDialog = "LAYOUTS" }
                )
            }

            // 6. الإبتسامات (Screenshot 1)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "emojis"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "emojis_desc"),
                    icon = Icons.Outlined.Mood,
                    onClick = { activeDialog = "EMOJIS" }
                )
            }

            // 7. الحافظة (Screenshot 1 & User requirement: حفظ للأبد وإغلاق عند اللصق وتذكر الموضع)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "clipboard"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "clipboard_desc"),
                    icon = Icons.Outlined.ContentPaste,
                    onClick = { activeDialog = "CLIPBOARD" }
                )
            }

            // 8. الاختصارات (Screenshot 1 & 2)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "shortcuts"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "shortcuts_desc"),
                    icon = Icons.Outlined.ShortText,
                    onClick = { activeDialog = "SHORTCUTS" }
                )
            }

            // 9. الكتابة (Screenshot 2 & 10)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "typing"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "typing_desc"),
                    icon = Icons.Outlined.TextFields,
                    onClick = { activeDialog = "TYPING" }
                )
            }

            // 9.1 الذكاء الاصطناعي وتغيير النبرة
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "ai_tone"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "ai_tone_desc"),
                    icon = Icons.Outlined.Psychology,
                    onClick = { activeDialog = "AI_TONE" }
                )
            }

            // 9.2 قاموس تصحيح الأخطاء والكلمات الشخصية
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "dictionary"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "dictionary_desc"),
                    icon = Icons.Outlined.Spellcheck,
                    onClick = { activeDialog = "DICTIONARY" }
                )
            }

            // 10. الصف السفلي والعلوي وشريط الأدوات (Screenshot 2 & 11)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "rows_toolbar"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "rows_toolbar_desc"),
                    icon = Icons.Outlined.ViewStream,
                    onClick = { activeDialog = "ROWS_TOOLBAR" }
                )
            }

            // 10.1 تخصيص شريط الأدوات العلوي (تقليل الأيقونات وإزالة الزحمة)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "customize_toolbar"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "customize_toolbar_desc"),
                    icon = Icons.Outlined.Tune,
                    onClick = { activeDialog = "CUSTOMIZE_TOOLBAR" }
                )
            }

            // 10.2 إدارة وحذف شريط الأدوات فوق الأحرف
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "manage_quick_actions"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "manage_quick_actions_desc"),
                    icon = Icons.Outlined.FormatListBulleted,
                    onClick = { activeDialog = "MANAGE_KEYBOARD_MENUS" }
                )
            }

            // 11. الصوت والإهتزاز (Screenshot 2 & 12)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "sound_haptic"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "sound_haptic_desc"),
                    icon = Icons.Outlined.VolumeUp,
                    onClick = { activeDialog = "SOUND_HAPTIC" }
                )
            }

            // 11.1 سرعة الكتابة واستجابة الأزرار
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "typing_speed"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "typing_speed_desc"),
                    icon = Icons.Outlined.Speed,
                    onClick = { activeDialog = "TYPING_SPEED" }
                )
            }

            // 12. إرتفاع الكيبورد وحجم الأحرف (Screenshot 2 & 13)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "height_font"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "height_font_desc"),
                    icon = Icons.Outlined.Height,
                    onClick = { activeDialog = "HEIGHT_FONT" }
                )
            }

            // 12.1 قسم التخصيص والأبعاد المتقدمة
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "customization"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "customization_desc"),
                    icon = Icons.Outlined.Tune,
                    onClick = { activeDialog = "CUSTOMIZATION" }
                )
            }

            // 13. النسخ الإحتياطي (Screenshot 2)
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "backup"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "backup_desc"),
                    icon = Icons.Outlined.CloudUpload,
                    onClick = { activeDialog = "BACKUP" }
                )
            }

            // 13.1 استعادة الإعدادات الافتراضية
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "restore_defaults"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "restore_defaults_desc"),
                    icon = Icons.Outlined.RestartAlt,
                    onClick = { activeDialog = "RESET_DEFAULTS_CONFIRM" }
                )
            }

            // 14. قنوات التليجرام والتواصل
            item {
                TransboardSettingsRow(
                    title = AppStrings.trCode(context, appLanguageCode, "telegram_channels_title"),
                    subtitle = AppStrings.trCode(context, appLanguageCode, "telegram_channels_desc"),
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
            title = { Text(AppStrings.trCode(context, appLanguageCode, "input_langs_dialog_title"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(AppStrings.trCode(context, appLanguageCode, "arabic_basic"), color = Color.White, fontSize = 14.sp)
                        Checkbox(checked = true, onCheckedChange = null)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_001"), color = Color.White, fontSize = 14.sp)
                        Checkbox(checked = true, onCheckedChange = null)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppStrings.trCode(context, appLanguageCode, "swipe_space_switch"), color = Color.White, fontSize = 13.sp)
                            Text(AppStrings.trCode(context, appLanguageCode, "swipe_space_switch_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 0. APP UI LANGUAGE DIALOG (All World Languages + Auto System Detection)
    if (activeDialog == "APP_UI_LANGUAGE") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.Public, contentDescription = null, tint = Color(0xFF00E5FF))
                    Text(AppStrings.trCode(context, appLanguageCode, "app_language"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "app_ui_lang_dialog_desc"),
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = appLanguageSearchQuery,
                        onValueChange = { appLanguageSearchQuery = it },
                        placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "search_any_lang"), fontSize = 11.sp, color = Color(0xFF8E9BAE)) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF28364F),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(16.dp))
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val filteredLangs = remember(appLanguageSearchQuery) {
                        if (appLanguageSearchQuery.isBlank()) com.example.util.LocaleHelper.supportedLanguages
                        else {
                            val q = appLanguageSearchQuery.trim()
                            com.example.util.LocaleHelper.supportedLanguages.filter {
                                it.nameNative.contains(q, ignoreCase = true) ||
                                it.nameAr.contains(q, ignoreCase = true) ||
                                it.nameEn.contains(q, ignoreCase = true) ||
                                it.code.contains(q, ignoreCase = true)
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredLangs, key = { it.code }) { lang ->
                            val isSelected = lang.code == appLanguageCode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f) else Color(0xFF141A28),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF1E283A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        appLanguageCode = lang.code
                                        com.example.util.LocaleHelper.setAppLanguageCode(context, lang.code)
                                        com.example.util.LocaleHelper.applyLocale(context)
                                        activeDialog = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(lang.flag, fontSize = 20.sp)
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(lang.nameNative, color = Color.White, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                                if (lang.code == "auto") {
                                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF00E5FF).copy(alpha = 0.2f)) {
                                                        Text(AppStrings.trCode(context, appLanguageCode, "recommended"), color = Color(0xFF00E5FF), fontSize = 8.5.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (lang.code == "auto") "Auto-detect device system language" else "${lang.nameAr} (${lang.nameEn})",
                                                color = Color(0xFF8E9BAE),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "action_close"), color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF0D121D)
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
            title = { Text(AppStrings.trCode(context, appLanguageCode, "instant_translate_title"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(AppStrings.trCode(context, appLanguageCode, "default_translate_langs"), color = Color(0xFF8E9BAE), fontSize = 11.sp)
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
                                Text(AppStrings.trCode(context, appLanguageCode, "from_label"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
                                val isArabicLang = (appLanguageCode == "ar" || (appLanguageCode == "auto" && AppStrings.getLang(context) == "ar"))
                                val srcName = if (isArabicLang) srcLangObj.nameAr else srcLangObj.nameEn
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_002", srcLangObj.flag, srcName), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                Text(AppStrings.trCode(context, appLanguageCode, "to_label"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
                                val isArabicLang = (appLanguageCode == "ar" || (appLanguageCode == "auto" && AppStrings.getLang(context) == "ar"))
                                val tgtName = if (isArabicLang) tgtLangObj.nameAr else tgtLangObj.nameEn
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_003", tgtLangObj.flag, tgtName), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            title = {
                                Text(
                                    if (showSourcePicker) AppStrings.trCode(context, appLanguageCode, "select_source_lang")
                                    else AppStrings.trCode(context, appLanguageCode, "select_target_lang"),
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            },
                            text = {
                                Column(modifier = Modifier.height(300.dp)) {
                                    OutlinedTextField(
                                        value = langFilter,
                                        onValueChange = { langFilter = it },
                                        placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "search_languages_hint"), fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filteredList) { l ->
                                            val isArabicLang = (appLanguageCode == "ar" || (appLanguageCode == "auto" && AppStrings.getLang(context) == "ar"))
                                            val displayName = if (isArabicLang) l.nameAr else l.nameEn
                                            val subName = if (isArabicLang) l.nameEn else l.nameAr
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
                                                Text(displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("($subName)", color = Color(0xFF8E9BAE), fontSize = 11.sp)
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
                                    Text(AppStrings.trCode(context, appLanguageCode, "cancel"), color = Color(0xFF00E5FF))
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
                            Text(AppStrings.trCode(context, appLanguageCode, "auto_translate_copied"), color = Color.White, fontSize = 13.sp)
                            Text(AppStrings.trCode(context, appLanguageCode, "auto_translate_copied_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "translate_enter_hold"), color = Color.White, fontSize = 13.sp)
                            Text(AppStrings.trCode(context, appLanguageCode, "translate_enter_hold_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 3. DECORATION DIALOG (Screenshot 5 & 18)
    if (activeDialog == "DECORATION") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(AppStrings.trCode(context, appLanguageCode, "text_decoration_title"), color = Color.White, fontWeight = FontWeight.Bold) },
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
                    Text(AppStrings.trCode(context, appLanguageCode, "close"), color = Color(0xFF00E5FF))
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
                    AppStrings.trCode(context, appLanguageCode, "layouts"),
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
                        AppStrings.trCode(context, appLanguageCode, "layouts_dialog_title"),
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
                            AppStrings.trCode(context, appLanguageCode, "custom_layout_manual"),
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
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_004"), color = Color.White, fontSize = 12.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_005"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_006"), color = Color(0xFF00E5FF), fontSize = 10.sp)
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
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_007"), color = Color.White, fontSize = 12.sp)
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
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_008"), color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = settings.autoReturnToLettersOnSend,
                            onCheckedChange = { updateSettings(settings.copy(autoReturnToLettersOnSend = it)) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "save_done_btn"), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
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
                        Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_009"), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_010"), color = Color(0xFF94A3B8))
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
                    AppStrings.trCode(context, appLanguageCode, "custom_layout_manual"),
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
                        AppStrings.trCode(context, appLanguageCode, "layouts_desc"),
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_011"), color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_012"), color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_013"), color = Color(0xFF00E5FF), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_014"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_015"), fontSize = 9.5.sp, color = Color(0xFF00E5FF))
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_016"), fontSize = 9.5.sp, color = Color.White)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_017"), fontSize = 9.5.sp, color = Color.White)
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
                        Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_018"), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_019"), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDialog = "LAYOUTS" }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_020"), color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 6. EMOJIS DIALOG
    if (activeDialog == "EMOJIS") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(AppStrings.trCode(context, appLanguageCode, "emojis_dialog_title"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppStrings.trCode(context, appLanguageCode, "emojis_row_toggle"), color = Color.White, fontSize = 13.sp)
                            Text(AppStrings.trCode(context, appLanguageCode, "emojis_row_toggle_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 7. CLIPBOARD DIALOG (User's specific core requirements!)
    if (activeDialog == "CLIPBOARD") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(AppStrings.trCode(context, appLanguageCode, "clipboard"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    // Feature 1: حفظ للأبد
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppStrings.trCode(context, appLanguageCode, "clipboard_save_forever"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "clipboard_save_forever_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "clipboard_close_on_paste"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "clipboard_close_on_paste_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "clipboard_remember_scroll"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "clipboard_remember_scroll"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "clipboard_swap_emoji"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (settings.swapClipboardAndEmoji) "[📋] | [😊]" else "[😊] | [📋]",
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp
                            )
                        }
                        Switch(
                            checked = settings.swapClipboardAndEmoji,
                            onCheckedChange = { updateSettings(settings.copy(swapClipboardAndEmoji = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // زر إصلاح ومزامنة نصوص الحافظة الفورية (Fix & Sync Clipboard)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val count = prefs.forceSyncAndRepairClipboard(context)
                                Toast.makeText(context, AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_021", count), Toast.LENGTH_SHORT).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(24.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(AppStrings.trCode(context, appLanguageCode, "fix_and_sync_clipboard"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(AppStrings.trCode(context, appLanguageCode, "fix_and_sync_clipboard_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            prefs.clearClipboardHistory()
                            val isArabicLang = (appLanguageCode == "ar" || (appLanguageCode == "auto" && AppStrings.getLang(context) == "ar"))
                            Toast.makeText(context, if (isArabicLang) AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_002") else "Unpinned clips cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text(AppStrings.trCode(context, appLanguageCode, "clear_unpinned_only"))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF))
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
            title = { Text(AppStrings.trCode(context, appLanguageCode, "shortcuts_dialog_title"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp, max = 450.dp)) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_022"),
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
                            placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_023"), fontSize = 11.sp, color = Color(0xFF64748B)) },
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
                            placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_024"), fontSize = 11.sp, color = Color(0xFF64748B)) },
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
                                Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_025"), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_026"), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_027", shortcutsList.size), color = Color(0xFFCBD5E1), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_028"), color = Color(0xFF94A3B8), fontSize = 11.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 10. ROWS & TOOLBAR DIALOG (Screenshot 11)
    if (activeDialog == "ROWS_TOOLBAR") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(AppStrings.trCode(context, appLanguageCode, "rows_toolbar"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_029"), color = Color.White, fontSize = 13.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_005"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_030"), color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = settings.topQuickEmojiRowEnabled,
                            onCheckedChange = { updateSettings(settings.copy(topQuickEmojiRowEnabled = it)) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_031"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_032"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.toolbarEnabled,
                            onCheckedChange = { updateSettings(settings.copy(toolbarEnabled = it)) }
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
                                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_033"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF8E9BAE), modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // التحكم بحجم شريط الابتسامات (Emoji Bar Size)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E293B).copy(alpha = 0.7f))
                                .border(1.dp, Color(0xFF28364F), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_034"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_035", (settings.emojiBarHeightFactor * 100).toInt()), color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // خيارات جاهزة: صغير / متوسط / كبير
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "صغير" to 0.80f,
                                    "متوسط" to 1.0f,
                                    "كبير" to 1.30f
                                ).forEach { (label, factor) ->
                                    val isSelected = kotlin.math.abs(settings.emojiBarHeightFactor - factor) < 0.10f
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.25f) else Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF28364F)),
                                        modifier = Modifier.weight(1f).clickable {
                                            updateSettings(
                                                settings.copy(
                                                    emojiBarHeightFactor = factor,
                                                    emojiButtonSizeFactor = factor
                                                )
                                            )
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

                            // سحب دقيق بالحجم (Slider)
                            Slider(
                                value = settings.emojiBarHeightFactor,
                                onValueChange = { newFactor ->
                                    val clamped = newFactor.coerceIn(0.70f, 1.60f)
                                    updateSettings(
                                        settings.copy(
                                            emojiBarHeightFactor = clamped,
                                            emojiButtonSizeFactor = clamped
                                        )
                                    )
                                },
                                valueRange = 0.70f..1.60f
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // معاينة مباشرة لشريط الابتسامات
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_036"), color = Color(0xFF8E9BAE), fontSize = 10.5.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((30.dp * settings.emojiBarHeightFactor).coerceIn(22.dp, 48.dp))
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0B101B))
                                    .padding(horizontal = 4.dp, vertical = 1.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("😊", "❤️", "😂", "🔥", "👍", "✨", "🎉", "🤲").forEach { em ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = em, fontSize = (13 * settings.emojiButtonSizeFactor).coerceIn(9f, 22f).sp)
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFF28364F), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))

                    // التحكم في حجم الفيسات والإيموجي
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_037"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_038"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_039"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_040"), color = Color(0xFF00E5FF))
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
                    text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_041"),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_042"),
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
                            placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_043"), fontSize = 11.sp, color = Color(0xFF8E9BAE)) },
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_044"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_045"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_046"), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        topEmojisList = mutableListOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
                    }
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_047"), color = Color(0xFF8E9BAE))
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_048"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.heightIn(max = 420.dp)) {
                    Text(
                        AppStrings.trCode(context, appLanguageCode, "customize_toolbar_desc"),
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_049"), color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_050"), color = Color.White, fontSize = 11.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_051"), color = Color.White, fontSize = 11.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "save_done_btn"), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    // 9. TYPING DIALOG (الكتابة والتصحيح التلقائي)
    if (activeDialog == "TYPING") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = { Text(AppStrings.trCode(context, appLanguageCode, "typing_dialog_title"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_052"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_053"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_054"), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_055"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_056"), color = Color.White, fontSize = 13.sp)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_057"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_058"), color = Color.White, fontSize = 13.sp)
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_059"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF))
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_060"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                                text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_061"),
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
                                text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_062"),
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
                        label = { Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_063")) },
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
                                text = AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_064", stats.inputWordCount, stats.outputWordCount),
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_065"),
                                    color = Color(0xFF34D399),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_066"),
                            color = Color(0xFF8E9BAE),
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_067"), color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_068"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_069"), color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_019"), color = Color(0xFF00E5FF))
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
            title = { Text(AppStrings.trCode(context, appLanguageCode, "dictionary_dialog_title"), color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.height(340.dp)) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "dict_add_desc"),
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
                            placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "dict_input_hint"), fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newWordInput.isNotBlank()) {
                                    val added = userDict.addWord(newWordInput)
                                    if (added) {
                                        wordsList = userDict.getUserWords()
                                        Toast.makeText(context, "${AppStrings.trCode(context, appLanguageCode, "dict_word_added")} (${newWordInput.trim()})", Toast.LENGTH_SHORT).show()
                                        newWordInput = ""
                                    }
                                }
                            },
                            modifier = Modifier.height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "dict_add_btn"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("${AppStrings.trCode(context, appLanguageCode, "dict_saved_words")} (${wordsList.size}):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    if (wordsList.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(AppStrings.trCode(context, appLanguageCode, "dict_empty_msg"), color = Color(0xFF8E9BAE), fontSize = 11.sp)
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
                                            Icon(Icons.Default.Delete, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_015"), tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
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
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF))
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
                        text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_070"),
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
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_071"), color = dText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_072"), color = dSub, fontSize = 10.sp)
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
                                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_073"), color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
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
                                                        Icon(Icons.Default.VolumeUp, contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_074"), tint = dAccent, modifier = Modifier.size(16.dp))
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
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_075"), color = dText, fontSize = 11.sp)
                                        Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_076", (settings.soundVolume * 100).toInt()), color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_077"), color = dText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_078"), color = dSub, fontSize = 10.sp)
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
                                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_079"), color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
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
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_080"), color = dText, fontSize = 11.sp)
                                        Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_081", settings.vibrationDurationMs), color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_082"), color = dText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_083"), color = dSub, fontSize = 10.sp)
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
                                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_084"), color = if (isIos) dAccent else dText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_085"), color = dSub, fontSize = 9.sp)
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
                                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_086"), color = if (isFast) dAccent else dText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_087"), color = dSub, fontSize = 9.sp)
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
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_088"), color = dText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_089"), color = dSub, fontSize = 9.5.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "dim_save_done"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                        text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_090"),
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
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_091"), color = dText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_092"), color = dSub, fontSize = 10.sp)
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
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_093"), color = dText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Slider 1: سرعة تكرار الحذف
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_094"), color = dText, fontSize = 11.sp)
                                    Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_095", settings.keyRepeatSpeedMs), color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_096"), color = dText, fontSize = 11.sp)
                                    Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_097", settings.longPressDelayMs), color = dAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_098"), color = dText, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                // Toggle 1: بعد الإرسال
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_099"), color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_100"), color = dSub, fontSize = 9.5.sp)
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
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_101"), color = dText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_102"), color = dSub, fontSize = 9.5.sp)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_103"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            containerColor = dBg
        )
    }

    // 12. HEIGHT & FONT & CUSTOMIZATION DIALOG (Screenshot 13 & User Customization)
    if (activeDialog == "HEIGHT_FONT" || activeDialog == "CUSTOMIZATION") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Tune, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(AppStrings.trCode(context, appLanguageCode, "dim_title"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                val isRtlLang = com.example.util.LocaleHelper.isRtlLanguage(appLanguageCode)
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    // 1. Toolbar Height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${AppStrings.trCode(context, appLanguageCode, "dim_toolbar_height")} (📋🌐🧮✨):", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_104", settings.toolbarHeightDp), color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.toolbarHeightDp.toFloat(),
                        onValueChange = { updateSettings(settings.copy(toolbarHeightDp = it.toInt())) },
                        valueRange = 30f..80f,
                        steps = 49
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Keyboard Key Height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${AppStrings.trCode(context, appLanguageCode, "dim_keys_height")}:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_105", settings.keyHeightDp, (settings.keyHeightFactor * 100).toInt()), color = Color(0xFF00E5FF), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.keyHeightDp.toFloat(),
                        onValueChange = {
                            val intVal = it.toInt()
                            updateSettings(settings.copy(keyHeightDp = intVal, keyHeightFactor = (intVal.toFloat() / 40.0f).coerceIn(0.70f, 1.60f)))
                        },
                        valueRange = 30f..80f,
                        steps = 49
                    )
                    // 5 Clear Height Presets
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        listOf(
                            Triple(if (isRtlLang) "صغير جداً" else "Very Small", 34, 0.82f),
                            Triple(if (isRtlLang) "صغير" else "Small", 37, 0.90f),
                            Triple(if (isRtlLang) "متوسط 100%" else "Medium 100%", 40, 1.00f),
                            Triple(if (isRtlLang) "كبير" else "Large", 46, 1.15f),
                            Triple(if (isRtlLang) "كبير جداً" else "Very Large", 52, 1.30f)
                        ).forEach { (lbl, dpVal, factorVal) ->
                            val isSel = settings.keyHeightDp == dpVal || kotlin.math.abs(settings.keyHeightFactor - factorVal) < 0.04f
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) Color(0xFF00E5FF) else Color(0xFF1E2838),
                                modifier = Modifier.weight(1f).clickable {
                                    updateSettings(settings.copy(keyHeightDp = dpVal, keyHeightFactor = factorVal))
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                                    Text(lbl, color = if (isSel) Color.Black else Color.White, fontSize = 8.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Spacebar cursor control
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(AppStrings.trCode(context, appLanguageCode, "dim_space_cursor"), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(AppStrings.trCode(context, appLanguageCode, "dim_space_cursor_desc"), color = Color(0xFF8E9BAE), fontSize = 10.sp)
                        }
                        Switch(
                            checked = settings.spacebarCursorControl,
                            onCheckedChange = { updateSettings(settings.copy(spacebarCursorControl = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Key Font Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${AppStrings.trCode(context, appLanguageCode, "dim_font_size")}:", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_106", settings.keyFontSizeSp), color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.keyFontSizeSp.toFloat(),
                        onValueChange = {
                            val intVal = it.toInt()
                            updateSettings(settings.copy(keyFontSizeSp = intVal, keyFontSizeFactor = (intVal.toFloat() / 20.0f).coerceIn(0.7f, 1.6f)))
                        },
                        valueRange = 14f..32f,
                        steps = 17
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Floating Navigation Speed
                    val speedDesc = if (settings.floatingNavSpeed <= 3) (if (isRtlLang) "(بطيء)" else "(Slow)") else if (settings.floatingNavSpeed >= 8) (if (isRtlLang) "(سريع)" else "(Fast)") else (if (isRtlLang) "(متوسط)" else "(Medium)")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${AppStrings.trCode(context, appLanguageCode, "dim_floating_speed")}:", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${settings.floatingNavSpeed} $speedDesc",
                            color = Color(0xFF00E5FF),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        AppStrings.trCode(context, appLanguageCode, "dim_floating_speed_desc"),
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                    Slider(
                        value = settings.floatingNavSpeed.toFloat(),
                        onValueChange = { updateSettings(settings.copy(floatingNavSpeed = it.toInt())) },
                        valueRange = 1f..10f,
                        steps = 8
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Emoji Bar Height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${AppStrings.trCode(context, appLanguageCode, "dim_quick_emoji_size")}:", color = Color.White, fontSize = 12.5.sp)
                        Text(AppStrings.trfCode(context, appLanguageCode, "ui_settings_screen_035", (settings.emojiBarHeightFactor * 100).toInt()), color = Color(0xFF00E5FF), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = settings.emojiBarHeightFactor,
                        onValueChange = { updateSettings(settings.copy(emojiBarHeightFactor = it)) },
                        valueRange = 0.6f..1.8f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 6. Keyboard Scale
                    Text("${AppStrings.trCode(context, appLanguageCode, "dim_keyboard_scale")}: ${(settings.keyButtonScale * 100).toInt()}%", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = settings.keyButtonScale,
                        onValueChange = { updateSettings(settings.copy(keyButtonScale = it)) },
                        valueRange = 0.75f..1.6f
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            updateSettings(
                                settings.copy(
                                    toolbarHeightDp = 34,
                                    keyHeightDp = 40,
                                    keyHeightFactor = 1.0f,
                                    keyFontSizeSp = 19,
                                    keyFontSizeFactor = 1.0f,
                                    floatingNavSpeed = 6,
                                    emojiBarHeightFactor = 1.0f,
                                    keyButtonScale = 1.0f,
                                    backspaceKeyScale = 1.15f
                                )
                            )
                            Toast.makeText(context, if (isRtlLang) AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_107") else "Default keyboard dimensions restored ✓", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF00E5FF).copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("↺ " + AppStrings.trCode(context, appLanguageCode, "dim_restore_btn"), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "dim_save_done"), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        updateSettings(
                            settings.copy(
                                toolbarHeightDp = 34,
                                keyHeightDp = 40,
                                keyHeightFactor = 1.0f,
                                keyFontSizeSp = 19,
                                keyFontSizeFactor = 1.0f,
                                floatingNavSpeed = 6,
                                emojiBarHeightFactor = 1.0f,
                                keyButtonScale = 1.0f
                            )
                        )
                        Toast.makeText(context, if (com.example.util.LocaleHelper.isRtlLanguage(appLanguageCode)) AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_108") else "Default size restored ✓", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "dim_reset"), color = Color(0xFF94A3B8))
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
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_109"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_110"),
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
                                text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_111"),
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
                            text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_112"),
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
                                Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_113"), Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_114"), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_115"), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else {
                        Text(
                            text = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_116"),
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
                                        Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_117"), Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_118"), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_119"), color = Color(0xFF38BDF8), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = restoreCodeInput,
                            onValueChange = { restoreCodeInput = it },
                            placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_120"), color = Color(0xFF64748B), fontSize = 11.sp) },
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
                                    Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_121"), Toast.LENGTH_SHORT).show()
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
                                            Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_122"), Toast.LENGTH_LONG).show()
                                            activeDialog = null
                                        } else {
                                            Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_123"), Toast.LENGTH_LONG).show()
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
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_124"), color = Color.White, fontSize = 12.sp)
                            } else {
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_125"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_019"), color = Color.White)
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
                    Text(AppStrings.trCode(context, appLanguageCode, "telegram_channels_title"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "telegram_channels_desc"),
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
                                Text(AppStrings.trCode(context, appLanguageCode, "telegram_channel_1"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("t.me/Abu2Saleh", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0088CC)) {
                                Text(AppStrings.trCode(context, appLanguageCode, "telegram_join_btn"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
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
                                Text(AppStrings.trCode(context, appLanguageCode, "telegram_channel_2"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("t.me/+OWBdNfDNS8MzYzQ0", color = Color(0xFF38BDF8), fontSize = 11.sp)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0088CC)) {
                                Text(AppStrings.trCode(context, appLanguageCode, "telegram_join_btn"), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "action_close"), color = Color.White)
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    if (activeDialog == "RESET_DEFAULTS_CONFIRM") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(38.dp)
                )
            },
            title = {
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "reset_confirm_title"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "reset_confirm_msg"),
                    color = Color(0xFFCAD5E2),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (newSettings, _) = prefs.resetToFactoryDefaults()
                        updateSettings(newSettings)
                        activeDialog = null
                        Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "reset_confirm_success"), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "reset_confirm_btn"), color = Color(0xFF090D15), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { activeDialog = null },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Text(AppStrings.trCode(context, appLanguageCode, "cancel"), color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }

    if (activeDialog == "MANAGE_KEYBOARD_MENUS") {
        AlertDialog(
            onDismissRequest = { activeDialog = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.FormatListBulleted, contentDescription = null, tint = Color(0xFF00E5FF))
                    Text(AppStrings.trCode(context, appLanguageCode, "manage_menus_title"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "manage_menus_desc"),
                        color = Color(0xFF8E9BAE),
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Preview of the Quick Actions Row
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0D121D),
                        border = BorderStroke(1.dp, Color(0xFF1E283A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = AppStrings.trCode(context, appLanguageCode, "manage_menus_preview"),
                                color = Color(0xFF00E5FF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (settings.showQuickActionUndoRedo) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF10B981).copy(alpha = 0.2f), border = BorderStroke(0.8.dp, Color(0xFF10B981))) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_126"), color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (settings.showQuickActionPaste) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1C2230)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_127"), color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (settings.showQuickActionCopy) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1C2230)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_clipboard_screen_018"), color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (settings.showQuickActionCut) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1C2230)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_128"), color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (settings.showQuickActionSelectAll) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1C2230)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_129"), color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (settings.showQuickActionReadText) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1C2230)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_130"), color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (settings.showQuickActionClearAll) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1C2230)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_131"), color = Color(0xFFEF4444), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                if (settings.showQuickActionClipboard) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1C2230)) {
                                        Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_132"), color = Color.White, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Presets buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                updateSettings(settings.copy(
                                    showQuickActionsRow = true,
                                    showQuickActionClearAll = true,
                                    showQuickActionReadText = true,
                                    showQuickActionPaste = true,
                                    showQuickActionCopy = true,
                                    showQuickActionCut = true,
                                    showQuickActionSelectAll = true,
                                    showQuickActionUndoRedo = true,
                                    showQuickActionClipboard = true
                                ))
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283A)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_133"), color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                updateSettings(settings.copy(
                                    showQuickActionClearAll = false,
                                    showQuickActionReadText = false,
                                    showQuickActionPaste = false,
                                    showQuickActionCopy = false,
                                    showQuickActionCut = false,
                                    showQuickActionSelectAll = false,
                                    showQuickActionUndoRedo = false,
                                    showQuickActionClipboard = false
                                ))
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283A)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_134"), color = Color(0xFFEF4444), fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                updateSettings(settings.copy(
                                    showQuickActionsRow = true,
                                    showQuickActionClearAll = true,
                                    showQuickActionReadText = true,
                                    showQuickActionPaste = true,
                                    showQuickActionCopy = true,
                                    showQuickActionCut = true,
                                    showQuickActionSelectAll = true,
                                    showQuickActionUndoRedo = true,
                                    showQuickActionClipboard = true
                                ))
                            },
                            modifier = Modifier.weight(1f).height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283A)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_135"), color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_136"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_137"),
                                icon = Icons.Outlined.ViewStream,
                                isChecked = settings.showQuickActionsRow,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionsRow = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_138"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_139"),
                                icon = Icons.Default.DeleteSweep,
                                isChecked = settings.showQuickActionClearAll,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionClearAll = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_140"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_141"),
                                icon = Icons.Default.VolumeUp,
                                isChecked = settings.showQuickActionReadText,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionReadText = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_142"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_143"),
                                icon = Icons.Default.ContentPaste,
                                isChecked = settings.showQuickActionPaste,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionPaste = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_144"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_145"),
                                icon = Icons.Outlined.ContentCopy,
                                isChecked = settings.showQuickActionCopy,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionCopy = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_146"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_147"),
                                icon = Icons.Outlined.ContentCut,
                                isChecked = settings.showQuickActionCut,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionCut = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_148"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_149"),
                                icon = Icons.Outlined.SelectAll,
                                isChecked = settings.showQuickActionSelectAll,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionSelectAll = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_150"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_151"),
                                icon = Icons.Default.Undo,
                                isChecked = settings.showQuickActionUndoRedo,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionUndoRedo = it)) }
                            )
                        }
                        item {
                            MenuToggleItem(
                                title = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_152"),
                                subtitle = AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_153"),
                                icon = Icons.Outlined.ContentPasteGo,
                                isChecked = settings.showQuickActionClipboard,
                                onCheckedChange = { updateSettings(settings.copy(showQuickActionClipboard = it)) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeDialog = null }) {
                    Text(AppStrings.trCode(context, appLanguageCode, "done"), color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF141A28)
        )
    }
    }
}

@Composable
private fun MenuToggleItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isChecked) Color(0xFF1E283A) else Color(0xFF101520),
        border = BorderStroke(1.dp, if (isChecked) Color(0xFF00E5FF).copy(alpha = 0.35f) else Color(0xFF26334A).copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isChecked) Color(0xFF00E5FF) else Color(0xFF8E9BAE),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        color = if (isChecked) Color.White else Color(0xFF8E9BAE),
                        fontSize = 12.sp,
                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = subtitle,
                        color = Color(0xFF64748B),
                        fontSize = 9.5.sp
                    )
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
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
