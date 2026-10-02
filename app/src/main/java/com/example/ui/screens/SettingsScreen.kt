package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.model.KeyboardSettings
import com.example.ui.components.SettingsItemRow

@Composable
fun SettingsScreen(
    prefs: PreferencesManager,
    onNavigateToThemes: () -> Unit,
    onNavigateToClipboard: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onBack: () -> Unit
) {
    var settings by remember { mutableStateOf(prefs.getSettings()) }
    var searchQuery by remember { mutableStateOf("") }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }

    fun updateSettings(newSettings: KeyboardSettings) {
        settings = newSettings
        prefs.saveSettings(newSettings)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C12))
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
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

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "الإعدادات",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(38.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search in settings
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في الإعدادات...", color = Color(0xFF8E9BAE), fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF26334A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF8E9BAE)
                )
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Settings items list matching Screenshot 4 & 5
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 0. Activation & Setup
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val status = remember { com.example.utils.KeyboardStatusHelper.checkKeyboardStatus(context) }
                val isDefault = status == com.example.utils.KeyboardStatus.ACTIVE_DEFAULT

                SettingsItemRow(
                    title = "تفعيل واختيار الكيبورد",
                    subtitle = if (isDefault) "كيبورد Turbo مفعل ويعمل كافتراضي ✓" else "اضغط هنا لتفعيل واختيار الكيبورد",
                    icon = if (isDefault) Icons.Default.CheckCircle else Icons.Outlined.PowerSettingsNew,
                    iconBgColor = if (isDefault) Color(0xFF059669) else Color(0xFF0284C7),
                    onClick = onNavigateToSetup,
                    trailing = {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00E5FF))
                    }
                )
            }

            // 1. Languages
            item {
                SettingsItemRow(
                    title = "اللغات",
                    subtitle = if (settings.defaultLanguage == "ar") "العربية، English (US)" else "English (US), العربية",
                    icon = Icons.Outlined.Language,
                    iconBgColor = Color(0xFF2563EB),
                    onClick = { showLanguageDialog = true },
                    trailing = {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8E9BAE))
                    }
                )
            }

            // 2. Layout
            item {
                SettingsItemRow(
                    title = "التخطيط",
                    subtitle = "أبجدي، QWERTY قياسي",
                    icon = Icons.Outlined.GridView,
                    iconBgColor = Color(0xFF0284C7),
                    onClick = {},
                    trailing = {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8E9BAE))
                    }
                )
            }

            // 3. Themes & Appearance
            item {
                SettingsItemRow(
                    title = "المظهر والثيمات",
                    subtitle = "الألوان، الخلفيات، الثيمات",
                    icon = Icons.Outlined.Palette,
                    iconBgColor = Color(0xFF4F46E5),
                    onClick = onNavigateToThemes,
                    trailing = {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF00E5FF))
                    }
                )
            }

            // 4. Number row
            item {
                SettingsItemRow(
                    title = "صف الأرقام",
                    subtitle = "إظهار صف الأرقام أعلى لوحة المفاتيح",
                    icon = Icons.Outlined.Numbers,
                    iconBgColor = Color(0xFF0D9488),
                    onClick = {
                        updateSettings(settings.copy(numberRowEnabled = !settings.numberRowEnabled))
                    },
                    trailing = {
                        Switch(
                            checked = settings.numberRowEnabled,
                            onCheckedChange = { updateSettings(settings.copy(numberRowEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }
                )
            }

            // 5. Auto correction
            item {
                SettingsItemRow(
                    title = "التصحيح التلقائي",
                    subtitle = "تصحيح الأخطاء الإملائية تلقائياً",
                    icon = Icons.Outlined.CheckCircleOutline,
                    iconBgColor = Color(0xFF059669),
                    onClick = {
                        updateSettings(settings.copy(autoCorrection = !settings.autoCorrection))
                    },
                    trailing = {
                        Switch(
                            checked = settings.autoCorrection,
                            onCheckedChange = { updateSettings(settings.copy(autoCorrection = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }
                )
            }

            // 6. Suggestions
            item {
                SettingsItemRow(
                    title = "الاقتراحات",
                    subtitle = "شريط اقتراحات ذكي أعلى الحروف",
                    icon = Icons.Outlined.Lightbulb,
                    iconBgColor = Color(0xFF7C3AED),
                    onClick = {
                        updateSettings(settings.copy(suggestionsEnabled = !settings.suggestionsEnabled))
                    },
                    trailing = {
                        Switch(
                            checked = settings.suggestionsEnabled,
                            onCheckedChange = { updateSettings(settings.copy(suggestionsEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }
                )
            }

            // 7. Sound & Vibration
            item {
                SettingsItemRow(
                    title = "الصوت والاهتزاز",
                    subtitle = if (settings.vibrationEnabled) "الاهتزاز مفعل" else "الاهتزاز متوقف",
                    icon = Icons.Outlined.VolumeUp,
                    iconBgColor = Color(0xFF9333EA),
                    onClick = {
                        updateSettings(settings.copy(vibrationEnabled = !settings.vibrationEnabled))
                    },
                    trailing = {
                        Switch(
                            checked = settings.vibrationEnabled,
                            onCheckedChange = { updateSettings(settings.copy(vibrationEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }
                )
            }

            // 8. Gestures: Swipe space to switch language
            item {
                SettingsItemRow(
                    title = "الإيماءات وتمرير المسطرة",
                    subtitle = "تبديل اللغة بسحب زر المسطرة يميناً ويساراً",
                    icon = Icons.Outlined.Swipe,
                    iconBgColor = Color(0xFF4338CA),
                    onClick = {
                        updateSettings(settings.copy(swipeSpaceSwitchLanguage = !settings.swipeSpaceSwitchLanguage))
                    },
                    trailing = {
                        Switch(
                            checked = settings.swipeSpaceSwitchLanguage,
                            onCheckedChange = { updateSettings(settings.copy(swipeSpaceSwitchLanguage = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }
                )
            }

            // 9. Enter Key 4-second translate
            item {
                SettingsItemRow(
                    title = "الترجمة بضغطة زر Enter لمدة 4 ثوانٍ",
                    subtitle = "الضغط المطول على Enter يترجم النص مباشرة",
                    icon = Icons.Default.Translate,
                    iconBgColor = Color(0xFF2563EB),
                    onClick = {
                        updateSettings(settings.copy(enterLongPressTranslateEnabled = !settings.enterLongPressTranslateEnabled))
                    },
                    trailing = {
                        Switch(
                            checked = settings.enterLongPressTranslateEnabled,
                            onCheckedChange = { updateSettings(settings.copy(enterLongPressTranslateEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF00E5FF),
                                checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.35f)
                            )
                        )
                    }
                )
            }

            // 10. Clipboard Manager
            item {
                SettingsItemRow(
                    title = "الحافظة",
                    subtitle = "محفظة النصوص والروابط السريعة",
                    icon = Icons.Outlined.ContentPaste,
                    iconBgColor = Color(0xFFC026D3),
                    onClick = onNavigateToClipboard,
                    trailing = {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8E9BAE))
                    }
                )
            }

            // 11. Privacy & Security
            item {
                SettingsItemRow(
                    title = "الخصوصية والأمان",
                    subtitle = "عدم جمع البيانات الشخصية أو كلمات المرور",
                    icon = Icons.Outlined.Security,
                    iconBgColor = Color(0xFF7E22CE),
                    onClick = { showPrivacyDialog = true },
                    trailing = {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8E9BAE))
                    }
                )
            }

            // 12. Backup & Restore
            item {
                SettingsItemRow(
                    title = "النسخ الاحتياطي والمزامنة",
                    subtitle = "حافظ على بياناتك وثيماتك ونصوصك",
                    icon = Icons.Outlined.CloudSync,
                    iconBgColor = Color(0xFF3B82F6),
                    onClick = { showBackupDialog = true },
                    trailing = {
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF8E9BAE))
                    }
                )
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("اللغة الافتراضية", color = Color.White) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                updateSettings(settings.copy(defaultLanguage = "ar"))
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.defaultLanguage == "ar",
                            onClick = {
                                updateSettings(settings.copy(defaultLanguage = "ar"))
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("العربية (Arabic)", color = Color.White)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                updateSettings(settings.copy(defaultLanguage = "en"))
                                showLanguageDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.defaultLanguage == "en",
                            onClick = {
                                updateSettings(settings.copy(defaultLanguage = "en"))
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("English (US)", color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("إغلاق", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF131825)
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("الخصوصية والأمان 🔒", color = Color.White) },
            text = {
                Column {
                    Text(
                        text = "كيبورد برو (Turbo Keyboard) مصمم باحترام تام لخصوصيتك:\n\n" +
                                "• لا نقوم أبداً بجمع أو تخزين كلمات المرور أو أرقام البطاقات الائتمانية.\n" +
                                "• الحافظة محفوظة محلياً على جهازك فقط ولا يتم إرسالها إلى أي خادم خارجي.\n" +
                                "• الترجمة تتم بصورة آمنة ومباشرة.\n" +
                                "• يمكنك مسح سجل الحافظة في أي وقت بنقرة واحدة.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("حسناً، فهمت", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF131825)
        )
    }

    // Backup Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("النسخ الاحتياطي", color = Color.White) },
            text = {
                Text(
                    text = "تم حفظ جميع إعداداتك والثيمات المخصصة وحافظة النصوص محلياً على جهازك بصورة آمنة.",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("تم", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF131825)
        )
    }
}
