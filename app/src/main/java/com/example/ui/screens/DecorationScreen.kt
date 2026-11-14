package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.TextDecorator
import com.example.util.AppStrings

@Composable
fun DecorationScreen(
    appLanguageCode: String = "auto",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isArUi = remember(appLanguageCode) {
        val resolved = if (appLanguageCode == "auto" || appLanguageCode.isBlank()) AppStrings.getLang(context)
        else AppStrings.getNormalizedLangCode(appLanguageCode)
        resolved == "ar"
    }
    var isArabicTab by remember(isArUi) { mutableStateOf(isArUi) }
    var userText by remember(isArUi) {
        mutableStateOf(if (isArUi) "كيبورد برو الرائع" else "Turbo Keyboard Pro")
    }

    val styles = if (isArabicTab) TextDecorator.arabicStyles else TextDecorator.englishStyles

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C12))
            .padding(14.dp)
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
                text = AppStrings.trCode(context, appLanguageCode, "decor_screen_title"),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(38.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab switch: Arabic / English
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isArabicTab = true
                    if (userText == "Turbo Keyboard Pro") userText = "كيبورد برو الرائع"
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isArabicTab) Color(0xFF00E5FF) else Color(0xFF141926)
                )
            ) {
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "decor_tab_ar"),
                    color = if (isArabicTab) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    isArabicTab = false
                    if (userText == "كيبورد برو الرائع") userText = "Turbo Keyboard Pro"
                },
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!isArabicTab) Color(0xFF00E5FF) else Color(0xFF141926)
                )
            ) {
                Text(
                    text = AppStrings.trCode(context, appLanguageCode, "decor_tab_en"),
                    color = if (!isArabicTab) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Input text field
        OutlinedTextField(
            value = userText,
            onValueChange = { userText = it },
            label = { Text(AppStrings.trCode(context, appLanguageCode, "decor_input_label"), color = Color(0xFF8E9BAE)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF26334A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Styles list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(styles) { style ->
                val transformed = style.transform(userText)
                val copiedMsg = AppStrings.trCode(context, appLanguageCode, "decor_copied_toast")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF141926),
                    border = BorderStroke(1.dp, Color(0xFF26334A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboardManager.setText(AnnotatedString(transformed))
                            try {
                                com.example.data.PreferencesManager(context).addClipboardItem(transformed)
                            } catch (_: Exception) {}
                            Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = transformed,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArUi) style.nameAr else style.nameEn,
                                color = Color(0xFF00E5FF),
                                fontSize = 11.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(transformed))
                                try {
                                    com.example.data.PreferencesManager(context).addClipboardItem(transformed)
                                } catch (_: Exception) {}
                                Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = AppStrings.trCode(context, appLanguageCode, "copy_short"),
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
