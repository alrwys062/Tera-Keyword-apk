package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
        0xFF0E1118, 0xFF121212, 0xFF000000, 0xFF1A1A2E, 0xFF0D1B2A, 0xFF14213D,
        0xFF1F2421, 0xFF2B2D42, 0xFF3D0C11, 0xFF1E1408, 0xFF2D1B4E,
        0xFF00E5FF, 0xFF38BDF8, 0xFF818CF8, 0xFFA855F7, 0xFFE879F9,
        0xFFFFD700, 0xFFF59E0B, 0xFF10B981, 0xFFEF4444, 0xFFFFFFFF
    )

    val textPalette = listOf(
        0xFFFFFFFF, 0xFF000000, 0xFFFFDF7A, 0xFF00E5FF, 0xFFA7F3D0,
        0xFFFBCFE8, 0xFFE2E8F0, 0xFFFDE047, 0xFF38BDF8, 0xFFF43F5E
    )

    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var backgroundDim by remember { mutableFloatStateOf(0.45f) }
    var keyOpacity by remember { mutableFloatStateOf(0.9f) }

    var bgColor by remember { mutableLongStateOf(0xFF0E1118) }
    var keyColor by remember { mutableLongStateOf(0xFF1E2638) }
    var keyTextColor by remember { mutableLongStateOf(0xFFFFFFFF) }
    var accentColor by remember { mutableLongStateOf(0xFF00E5FF) }
    var enterColor by remember { mutableLongStateOf(0xFF1E88E5) }
    var cornerRadius by remember { mutableFloatStateOf(10f) }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri.toString()
        }
    }

    val livePreviewTheme = remember(
        themeName, bgColor, keyColor, keyTextColor, accentColor, enterColor,
        cornerRadius, selectedImageUri, backgroundDim, keyOpacity
    ) {
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
            toolbarColor = if (selectedImageUri != null) 0xFF0B0E14 else (bgColor + 0x0006080A).toLong(),
            borderColor = accentColor,
            cornerRadius = cornerRadius,
            borderAlpha = 0.5f,
            backgroundImageUri = selectedImageUri,
            backgroundDim = backgroundDim,
            keyOpacity = keyOpacity
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
                text = "صانع الثيمات بالصور والألوان",
                color = Color.White,
                fontSize = 17.sp,
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

        Spacer(modifier = Modifier.height(18.dp))

        // Custom Photo Background Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131924)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF25334A))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "خلفية الكيبورد بصورة مخصصة",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedImageUri != null) {
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove photo",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedImageUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Background Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = backgroundDim))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "نسبة تعتيم الصورة لإبراز الحروف (${(backgroundDim * 100).toInt()}%)",
                        color = Color(0xFF8E9BAE),
                        fontSize = 12.sp
                    )
                    Slider(
                        value = backgroundDim,
                        onValueChange = { backgroundDim = it },
                        valueRange = 0.1f..0.9f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedImageUri == null) "اختيار صورة من الاستديو" else "تغيير صورة الخلفية",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Transparency / Opacity Slider
        Text(
            text = "شفافية الأزرار (${(keyOpacity * 100).toInt()}%)",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Slider(
            value = keyOpacity,
            onValueChange = { keyOpacity = it },
            valueRange = 0.2f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF00E5FF),
                activeTrackColor = Color(0xFF00E5FF)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Background Color Selector
        ColorPickerSection("لون الخلفية", bgColor, colorPalette) { bgColor = it }

        Spacer(modifier = Modifier.height(14.dp))

        // Key Background Color Selector
        ColorPickerSection("لون الأزرار والمربعات", keyColor, colorPalette) { keyColor = it }

        Spacer(modifier = Modifier.height(14.dp))

        // Key Text Color Selector
        ColorPickerSection("لون نص الحروف", keyTextColor, textPalette) { keyTextColor = it }

        Spacer(modifier = Modifier.height(14.dp))

        // Accent / Neon Color Selector
        ColorPickerSection("لون الإضاءة والنيون", accentColor, colorPalette) { accentColor = it }

        Spacer(modifier = Modifier.height(14.dp))

        // Enter Key Color Selector
        ColorPickerSection("لون زر الإدخال (Enter)", enterColor, colorPalette) { enterColor = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Corner Radius Slider
        Text(
            text = "استدارة حواف مربعات الأزرار (${cornerRadius.toInt()} dp)",
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
                            tint = if (colorHex == 0xFFFFFFFF || colorHex == 0xFFFFDF7A || colorHex == 0xFF00E5FF) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
