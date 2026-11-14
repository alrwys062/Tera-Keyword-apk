package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TranslationEngine
import com.example.util.AppStrings
import kotlinx.coroutines.launch

@Composable
fun TranslationScreen(
    appLanguageCode: String = "auto",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val isArUi = remember(appLanguageCode) {
        val resolved = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context)
        else AppStrings.getNormalizedLangCode(appLanguageCode)
        resolved == "ar"
    }

    var sourceLang by remember(isArUi) { mutableStateOf(if (isArUi) "ar" else "en") }
    var targetLang by remember(isArUi) { mutableStateOf(if (isArUi) "en" else "ar") }
    var inputText by remember(isArUi) {
        mutableStateOf(if (isArUi) "مرحبا! كيف حالك اليوم؟" else "Hello! How are you today?")
    }
    var translatedResult by remember(isArUi) {
        mutableStateOf(if (isArUi) "Hello! How are you today?" else "مرحبا! كيف حالك اليوم؟")
    }
    var isTranslating by remember { mutableStateOf(false) }

    var showSourcePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }

    val allLangs = TranslationEngine.supportedLanguages
    val sourceItem = allLangs.find { it.code == sourceLang } ?: allLangs[0]
    val targetItem = allLangs.find { it.code == targetLang } ?: allLangs[1]

    fun doTranslation() {
        if (inputText.isBlank()) return
        isTranslating = true
        coroutineScope.launch {
            translatedResult = TranslationEngine.translate(inputText, sourceLang, targetLang)
            isTranslating = false
        }
    }

    if (showSourcePicker || showTargetPicker) {
        var query by remember { mutableStateOf("") }
        val filtered = allLangs.filter {
            it.nameAr.contains(query, ignoreCase = true) ||
            it.nameEn.contains(query, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = {
                showSourcePicker = false
                showTargetPicker = false
            },
            title = {
                Text(
                    text = if (showSourcePicker) {
                        AppStrings.trCode(context, appLanguageCode, "trans_source_picker_title")
                    } else {
                        AppStrings.trCode(context, appLanguageCode, "trans_target_picker_title")
                    },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.height(300.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text(AppStrings.trCode(context, appLanguageCode, "trans_search_hint"), fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered) { langItem ->
                            val primaryName = if (isArUi) langItem.nameAr else langItem.nameEn
                            val secondaryName = if (isArUi) langItem.nameEn else langItem.nameAr
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (showSourcePicker) {
                                            sourceLang = langItem.code
                                            showSourcePicker = false
                                        } else {
                                            targetLang = langItem.code
                                            showTargetPicker = false
                                        }
                                        doTranslation()
                                    }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(langItem.flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(primaryName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("($secondaryName)", color = Color(0xFF8E9BAE), fontSize = 11.sp)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C12))
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
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
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "ui_chat_simulator_screen_003"),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = AppStrings.trCode(context, appLanguageCode, "trans_screen_title"),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(38.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language selector & swap
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141926),
            border = BorderStroke(1.dp, Color(0xFF26334A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E283A),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { showSourcePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sourceItem.flag, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArUi) sourceItem.nameAr else sourceItem.nameEn,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val temp = sourceLang
                        sourceLang = targetLang
                        targetLang = temp
                        val tempText = inputText
                        inputText = translatedResult
                        translatedResult = tempText
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = AppStrings.trCode(context, appLanguageCode, "trans_btn"),
                        tint = Color(0xFF00E5FF)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E283A),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { showTargetPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(targetItem.flag, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArUi) targetItem.nameAr else targetItem.nameEn,
                            color = Color(0xFF00E5FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input text card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141926),
            border = BorderStroke(1.dp, Color(0xFF26334A))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "trans_original_label"),
                    color = Color(0xFF8E9BAE),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF26334A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { doTranslation() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(AppStrings.trCode(context, appLanguageCode, "trans_btn"), color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Result card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF141926),
            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "trans_result_label"),
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(translatedResult))
                            try {
                                com.example.data.PreferencesManager(context).addClipboardItem(translatedResult)
                            } catch (_: Exception) {}
                            Toast.makeText(context, AppStrings.trCode(context, appLanguageCode, "trans_copied_toast"), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = AppStrings.trCode(context, appLanguageCode, "copy_short"),
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = translatedResult,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
