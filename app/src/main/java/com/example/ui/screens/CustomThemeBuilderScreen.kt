package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import com.example.keyboard.TurboKeyboardView
import com.example.model.KeyboardTheme
import java.util.UUID

@Composable
fun CustomThemeBuilderScreen(
    prefs: PreferencesManager,
    onThemeSavedAndApplied: (KeyboardTheme) -> Unit,
    onBack: () -> Unit
) {
    var themeName by remember { mutableStateOf("ثيمي الخاص") }

    val colorPalette = listOf(
        0xFF0E1118, 0xFF121212, 0xFF1A1A2E, 0xFF0D1B2A, 0xFF14213D,
        0xFF1F2421, 0xFF2B2D42, 0xFF3D0C11, 0xFF1E1408, 0xFF2D1B4E,
        0xFF00E5FF, 0xFF38BDF8, 0xFF818CF8, 0xFFA855F7, 0xFFE879F9,
        0xFFFFD700, 0xFFF59E0B, 0xFF10B981, 0xFFEF4444, 0xFFFFFFFF
    )

    var bgColor by remember { mutableLongStateOf(0xFF0E1118) }
    var keyColor by remember { mutableLongStateOf(0xFF1E2638) }
    var keyTextColor by remember { mutableLongStateOf(0xFFFFFFFF) }
    var accentColor by remember { mutableLongStateOf(0xFF00E5FF) }
    var enterColor by remember { mutableLongStateOf(0xFF1E88E5) }
    var cornerRadius by remember { mutableFloatStateOf(10f) }

    val livePreviewTheme = remember(themeName, bgColor, keyColor, keyTextColor, accentColor, enterColor, cornerRadius) {
        KeyboardTheme(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            nameAr = themeName.ifBlank { "ثيم مخصص" },
            nameEn = "Custom Theme",
            isCustom = true,
            backgroundColor = bgColor,
            keyBackgroundColor = keyColor,
            keyPressedColor = (keyColor + 0x00151515).toLong(),
            keyTextColor = keyTextColor,
            subtextColor = 0xFF8E9BAE,
            accentColor = accentColor,
            enterButtonColor = enterColor,
            toolbarColor = (bgColor + 0x0006080A).toLong(),
            borderColor = accentColor,
            cornerRadius = cornerRadius,
            borderAlpha = 0.5f
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

        // Header
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

            Text(
                text = "صانع الثيمات المخصص",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    val finalTheme = livePreviewTheme.copy(id = "custom_" + System.currentTimeMillis())
                    prefs.saveCustomTheme(finalTheme)
                    prefs.setCurrentTheme(finalTheme.id)
                    onThemeSavedAndApplied(finalTheme)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("حفظ وتطبيق", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live interactive preview
        Text(
            text = "معاينة حية للكيبورد",
            color = Color(0xFF8E9BAE),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(accentColor).copy(alpha = 0.5f))
        ) {
            TurboKeyboardView(
                theme = livePreviewTheme,
                settings = prefs.getSettings(),
                prefsManager = prefs
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Theme name input
        OutlinedTextField(
            value = themeName,
            onValueChange = { themeName = it },
            label = { Text("اسم الثيم", color = Color(0xFF8E9BAE)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF26334A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Background Color Selector
        ColorPickerSection("لون الخلفية", bgColor, colorPalette) { bgColor = it }

        Spacer(modifier = Modifier.height(14.dp))

        // Key Background Color Selector
        ColorPickerSection("لون الأزرار", keyColor, colorPalette) { keyColor = it }

        Spacer(modifier = Modifier.height(14.dp))

        // Accent / Neon Color Selector
        ColorPickerSection("لون الإضاءة والنيون", accentColor, colorPalette) { accentColor = it }

        Spacer(modifier = Modifier.height(14.dp))

        // Enter Key Color Selector
        ColorPickerSection("لون زر الإدخال (Enter)", enterColor, colorPalette) { enterColor = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Corner Radius Slider
        Text(
            text = "استدارة حواف الأزرار (${cornerRadius.toInt()} dp)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = cornerRadius,
            onValueChange = { cornerRadius = it },
            valueRange = 2f..24f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00E5FF),
                activeTrackColor = Color(0xFF00E5FF)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ColorPickerSection(
    title: String,
    selectedColor: Long,
    palette: List<Long>,
    onColorSelected: (Long) -> Unit
) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(palette) { colorHex ->
                val isSelected = colorHex == selectedColor
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(colorHex))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color.White else Color(0xFF4A5568),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorHex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (colorHex == 0xFFFFFFFF) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
