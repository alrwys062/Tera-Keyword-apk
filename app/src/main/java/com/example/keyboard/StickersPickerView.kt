package com.example.keyboard

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.model.KeyboardTheme

data class StickerPreset(
    val category: String,
    val text: String,
    val emoji: String,
    val style: String = "modern"
)

object BuiltinStickers {
    val items = listOf(
        // Islamic
        StickerPreset("إسلامية", "صباح الخير والبركة", "☀️"),
        StickerPreset("إسلامية", "جزاك الله خيراً وبورك فيك", "🤲"),
        StickerPreset("إسلامية", "ما شاء الله تبارك الرحمن", "🤍"),
        StickerPreset("إسلامية", "الحمد لله دائماً وأبداً", "✨"),
        StickerPreset("إسلامية", "رمضان كريم ومبارك", "🌙"),
        StickerPreset("إسلامية", "جمعة مباركة وطيبة", "🕌"),
        StickerPreset("إسلامية", "لا حول ولا قوة إلا بالله", "📿"),
        StickerPreset("إسلامية", "استغفر الله العظيم", "🌿"),

        // Greetings & Congrats
        StickerPreset("تهاني", "ألف ألف مبروك التميز", "🥳"),
        StickerPreset("تهاني", "كل عام وأنتم بألف خير", "🎂"),
        StickerPreset("تهاني", "منور يا بعد قلبي", "✨"),
        StickerPreset("تهاني", "تسلم يمناك ويعطيك العافية", "👏"),
        StickerPreset("تهاني", "بالبركة والخير والمسرات", "💐"),
        StickerPreset("تهاني", "تستاهل كل خير ونجاح", "🌟"),
        StickerPreset("تهاني", "مساء الورد والسعادة", "🌸"),

        // Memes & Funny
        StickerPreset("ردود وميمز", "ههههههههه أضحكتني والله", "😂"),
        StickerPreset("ردود وميمز", "قوية هذي ما توقعتها", "🤣"),
        StickerPreset("ردود وميمز", "يا ساتر يا رب سترك", "😱"),
        StickerPreset("ردود وميمز", "تم قصف الجبهة بنجاح", "💣"),
        StickerPreset("ردود وميمز", "لا تعليق.. الصمت حكمة", "🤐"),
        StickerPreset("ردود وميمز", "ألو العمليات؟ الحقونا", "🚨"),
        StickerPreset("ردود وميمز", "ما أسمعك الشبكة تقطع", "🙉"),

        // Love & Warmth
        StickerPreset("حب وود", "فديت قلبك وروحك الغالية", "❤️"),
        StickerPreset("حب وود", "يعطيك ألف صحة وعافية", "🌹"),
        StickerPreset("حب وود", "تسلم لي عيونك الحلوة", "👀"),
        StickerPreset("حب وود", "يسعد مساك وصباحك يا غالي", "💖"),
        StickerPreset("حب وود", "أحبك في الله يا أخي", "🤍"),
        StickerPreset("حب وود", "وجودك يسعد قلبي دوماً", "🥰")
    )
}

@Composable
fun StickersPickerView(
    theme: KeyboardTheme,
    prefs: PreferencesManager,
    onStickerSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("⭐ ملصقاتي") }
    var customStickers by remember { mutableStateOf(prefs.getCustomStickers()) }

    // Designer state
    var designerText by remember { mutableStateOf("صباح الورد والسرور 🌸") }
    var selectedDesignStyle by remember { mutableStateOf("نيون") }
    var designerEmoji by remember { mutableStateOf("✨") }

    val categories = listOf("⭐ ملصقاتي", "🎨 صمم ملصق", "🕌 إسلامية", "🎉 تهاني", "😂 ردود وميمز", "❤️ حب وود")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(Color(theme.backgroundColor))
    ) {
        // Top Bar with Categories and Big Circular X Close Button (كبيرة وسهلة اللمس كالمطلوب)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(theme.toolbarColor))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Close X Button (Harmonious compact size)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Stickers",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Categories horizontal scroll
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(theme.accentColor).copy(alpha = 0.25f)
                                else Color(theme.keyBackgroundColor).copy(alpha = 0.7f)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(theme.accentColor) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) Color(theme.accentColor) else Color(theme.keyTextColor),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Main Content Area
        Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            when (selectedCategory) {
                "🎨 صمم ملصق" -> {
                    // Interactive Sticker Designer Studio
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Live Sticker Preview Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when (selectedDesignStyle) {
                                        "نيون" -> Brush.horizontalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))
                                        "ذهبي" -> Brush.horizontalGradient(listOf(Color(0xFF2E2207), Color(0xFF4A380A), Color(0xFF141006)))
                                        "وردي" -> Brush.horizontalGradient(listOf(Color(0xFF4A0033), Color(0xFF8E0E00), Color(0xFF1F1C2C)))
                                        "زجاجي" -> Brush.horizontalGradient(listOf(Color(0x33FFFFFF), Color(0x11FFFFFF)))
                                        else -> Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                                    }
                                )
                                .border(
                                    2.dp,
                                    when (selectedDesignStyle) {
                                        "نيون" -> Color(0xFF00E5FF)
                                        "ذهبي" -> Color(0xFFFFD700)
                                        "وردي" -> Color(0xFFFF4081)
                                        else -> Color(theme.accentColor)
                                    },
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = designerEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = designerText.ifBlank { "اكتب نص الملصق..." },
                                    color = when (selectedDesignStyle) {
                                        "ذهبي" -> Color(0xFFFFE898)
                                        "نيون" -> Color(0xFF00F5FF)
                                        "وردي" -> Color(0xFFFF80AB)
                                        else -> Color.White
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Text input for sticker
                        OutlinedTextField(
                            value = designerText,
                            onValueChange = { designerText = it },
                            placeholder = { Text("اكتب عبارة أو جملة الملصق هنا...", fontSize = 11.sp, color = Color(0xFF8E9BAE)) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(theme.accentColor),
                                unfocusedBorderColor = Color(theme.borderColor).copy(alpha = 0.5f),
                                focusedTextColor = Color(theme.keyTextColor),
                                unfocusedTextColor = Color(theme.keyTextColor)
                            )
                        )

                        // Style choices and action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("نيون", "ذهبي", "وردي", "زجاجي").forEach { st ->
                                val isSel = st == selectedDesignStyle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(theme.accentColor).copy(alpha = 0.3f) else Color(theme.keyBackgroundColor),
                                    border = BorderStroke(1.dp, if (isSel) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.4f)),
                                    modifier = Modifier.weight(1f).clickable { selectedDesignStyle = st }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(st, color = if (isSel) Color(theme.accentColor) else Color(theme.keyTextColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Bottom Actions: Save to My Stickers + Send directly
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val finalSticker = "$designerEmoji $designerText"
                                    prefs.saveCustomSticker(finalSticker)
                                    customStickers = prefs.getCustomStickers()
                                    Toast.makeText(context, "تم حفظ الملصق في (ملصقاتي) ⭐", Toast.LENGTH_SHORT).show()
                                    selectedCategory = "⭐ ملصقاتي"
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(theme.keyBackgroundColor))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(theme.accentColor), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حفظ بملصقاتي", color = Color(theme.keyTextColor), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val finalSticker = "$designerEmoji $designerText"
                                    onStickerSelected(finalSticker)
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إرسال الآن", color = Color.Black, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "⭐ ملصقاتي" -> {
                    if (customStickers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("لم تقم بتصميم أي ملصقات بعد", color = Color(theme.subtextColor), fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { selectedCategory = "🎨 صمم ملصق" },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor))
                                ) {
                                    Text("🎨 اضغط هنا لتصميم ملصقك الأول", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(customStickers) { sticker ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(theme.keyBackgroundColor),
                                    border = BorderStroke(1.dp, Color(theme.borderColor).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { onStickerSelected(sticker) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = sticker,
                                            color = Color(theme.keyTextColor),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                prefs.deleteCustomSticker(sticker)
                                                customStickers = prefs.getCustomStickers()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Built-in categories
                    val list = BuiltinStickers.items.filter { it.category in selectedCategory }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(theme.keyBackgroundColor),
                                border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    onStickerSelected("${item.emoji} ${item.text}")
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(item.emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.text,
                                        color = Color(theme.keyTextColor),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
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
