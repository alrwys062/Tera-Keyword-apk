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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.model.KeyboardTheme
import com.example.util.AppStrings
import com.example.util.LocaleHelper

data class StickerPreset(
    val category: String,
    val text: String,
    val emoji: String,
    val styleKey: String = "gold"
)

object BuiltinStickers {
    val items = listOf(
        // Islamic / إسلامية
        StickerPreset("إسلامية", "صباح الخير والبركة والسرور", "☀️", "emerald"),
        StickerPreset("إسلامية", "جزاك الله خيراً وبورك فيك", "🤲", "emerald"),
        StickerPreset("إسلامية", "ما شاء الله تبارك الرحمن", "🤍", "gold"),
        StickerPreset("إسلامية", "الحمد لله دائماً وأبداً", "✨", "gold"),
        StickerPreset("إسلامية", "رمضان كريم ومبارك عليكم", "🌙", "purple"),
        StickerPreset("إسلامية", "جمعة مباركة وطيبة", "🕌", "emerald"),
        StickerPreset("إسلامية", "لا حول ولا قوة إلا بالله", "📿", "neon"),
        StickerPreset("إسلامية", "استغفر الله العظيم وأتوب إليه", "🌿", "emerald"),
        StickerPreset("إسلامية", "اللهم صلِّ وسلم على نبينا محمد", "💚", "emerald"),
        StickerPreset("إسلامية", "في أمان الله وحفظه ورعايته", "🕊️", "neon"),

        // Greetings & Congrats / تهاني
        StickerPreset("تهاني", "ألف ألف مبروك النجاح والتميز", "🥳", "purple"),
        StickerPreset("تهاني", "كل عام وأنتم بألف صحة وسعادة", "🎂", "rose"),
        StickerPreset("تهاني", "عيدكم مبارك وكل عام وأنتم بخير", "🎈", "gold"),
        StickerPreset("تهاني", "منور يا بعد قلبي وروحي", "✨", "neon"),
        StickerPreset("تهاني", "تسلم يمناك ويعطيك ألف عافية", "👏", "gold"),
        StickerPreset("تهاني", "بالبركة والخير والمسرات يارب", "💐", "rose"),
        StickerPreset("تهاني", "تستاهل كل خير ومراتب عليا", "🌟", "gold"),
        StickerPreset("تهاني", "عساكم من عواده وتقبل الله طاعتكم", "🌙", "purple"),

        // Memes & Funny / ردود وميمز
        StickerPreset("ردود وميمز", "ههههههههههههه أضحكتني والله", "😂", "gold"),
        StickerPreset("ردود وميمز", "قوية هذي ما توقعتها منك أبداً", "🤣", "neon"),
        StickerPreset("ردود وميمز", "يا ساتر يا رب سترك ولطفك", "😱", "purple"),
        StickerPreset("ردود وميمز", "تم قصف الجبهة بنجاح ساحق", "💣", "rose"),
        StickerPreset("ردود وميمز", "لا تعليق.. الصمت حكمة وهيبة", "🤐", "glass"),
        StickerPreset("ردود وميمز", "ألو العمليات؟ الحقونا بضحكة", "🚨", "rose"),
        StickerPreset("ردود وميمز", "ما أسمعك الشبكة تقطع وترجع", "🙉", "neon"),
        StickerPreset("ردود وميمز", "كفووو والله قول وفعل يا بطل", "💪", "gold"),
        StickerPreset("ردود وميمز", "شكلك نسيت مين عمك يا صاحبي", "😉", "purple"),

        // Love & Warmth / حب وود
        StickerPreset("حب وود", "فديت قلبك وعيونك الغالية", "❤️", "rose"),
        StickerPreset("حب وود", "يعطيك ألف صحة وعافية يارب", "🌹", "rose"),
        StickerPreset("حب وود", "تسلم لي عيونك وطلتك الحلوة", "👀", "purple"),
        StickerPreset("حب وود", "يسعد مساك وصباحك يا أغلى الناس", "💖", "rose"),
        StickerPreset("حب وود", "أحبك في الله ودعواتي ترافقك", "🤍", "gold"),
        StickerPreset("حب وود", "وجودك في حياتي نعمة وسعادة", "🥰", "rose"),
        StickerPreset("حب وود", "يا بعد راسي وعيني ونبضي", "💕", "purple"),

        // Morning & Evening / صباح ومساء
        StickerPreset("صباح ومساء", "صباح الورد والياسمين والسرور", "🌸", "rose"),
        StickerPreset("صباح ومساء", "مساكم الله بالخير والرضا والنور", "🌆", "purple"),
        StickerPreset("صباح ومساء", "صباح النشاط والروقان والقهوة", "☕", "gold"),
        StickerPreset("صباح ومساء", "مساء الهدوء وراحة البال والسكينة", "🌙", "emerald"),
        StickerPreset("صباح ومساء", "صباح التفاؤل والأمل بالله", "☀️", "gold"),

        // Elite Quotes / عبارات راقية
        StickerPreset("عبارات راقية", "تفاءل بما تهوى يكن بإذن الله", "🌟", "neon"),
        StickerPreset("عبارات راقية", "الطيبون مثل بائع المسك طيبٌ أثره", "🌺", "rose"),
        StickerPreset("عبارات راقية", "راحة البال لا تُقدّر بأي ثمن", "🍃", "emerald"),
        StickerPreset("عبارات راقية", "كن جميلاً ترى الوجود جميلاً", "💎", "neon"),
        StickerPreset("عبارات راقية", "قل خيراً أو اصمت ففي الصمت نجاة", "📜", "gold")
    )
}

@Composable
fun StickersPickerView(
    theme: KeyboardTheme,
    prefs: PreferencesManager,
    onStickerSelected: (String) -> Unit,
    onClose: () -> Unit,
    keyboardHeight: Dp = 225.dp,
    appLanguageCode: String = "auto"
) {
    val context = LocalContext.current
    val resolvedLang = remember(appLanguageCode) {
        if (appLanguageCode == "auto") LocaleHelper.getAppLanguageCode(context) else appLanguageCode
    }
    val isRtl = remember(resolvedLang) { LocaleHelper.isRtlLanguage(resolvedLang) }
    var selectedCategoryId by remember { mutableStateOf("my_stickers") }
    var customStickers by remember { mutableStateOf(prefs.getCustomStickers()) }

    // Designer state
    var designerText by remember(isRtl) { mutableStateOf(if (isRtl) "صباح الورد والسرور 🌸" else "Have a lovely day 🌸") }
    var selectedDesignStyle by remember { mutableStateOf("ذهبي") }
    var designerEmoji by remember { mutableStateOf("✨") }

    val categories = remember(isRtl) {
        listOf(
            "my_stickers" to (if (isRtl) "⭐ ملصقاتي" else "⭐ My Stickers"),
            "designer" to (if (isRtl) "🎨 صمم ملصق" else "🎨 Designer"),
            "إسلامية" to (if (isRtl) "🕌 إسلامية" else "🕌 Blessings"),
            "تهاني" to (if (isRtl) "🎉 تهاني" else "🎉 Congrats"),
            "ردود وميمز" to (if (isRtl) "😂 ردود وميمز" else "😂 Memes"),
            "حب وود" to (if (isRtl) "❤️ حب وود" else "❤️ Love"),
            "صباح ومساء" to (if (isRtl) "☕ صباح ومساء" else "☕ Greetings"),
            "عبارات راقية" to (if (isRtl) "💫 عبارات راقية" else "💫 Quotes")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(keyboardHeight)
            .background(Color(theme.backgroundColor))
    ) {
        // Top Bar with Categories and Compact Harmonious Close X Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .background(Color(theme.toolbarColor))
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Circular Close X Button (Harmonious compact size 26dp)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.95f))
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "action_close"),
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
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
                categories.forEach { (catId, catLabel) ->
                    val isSelected = catId == selectedCategoryId
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
                            .clickable { selectedCategoryId = catId }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = catLabel,
                            color = if (isSelected) Color(theme.accentColor) else Color(theme.keyTextColor),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Dedicated text and sticker input bar
        var stickerComposeText by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp)
                .height(31.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(theme.keyBackgroundColor))
                .border(1.dp, Color(theme.accentColor).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (stickerComposeText.isEmpty()) {
                    Text(
                        text = if (isRtl) "اكتب نصاً لإدراجه مع الملصقات والرموز..." else "Type text to insert with stickers...",
                        color = Color(theme.subtextColor).copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
                BasicTextField(
                    value = stickerComposeText,
                    onValueChange = { stickerComposeText = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color(theme.keyTextColor),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(Color(theme.accentColor)),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (stickerComposeText.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = AppStrings.trCode(context, appLanguageCode, "clear_filter"),
                    tint = Color(theme.subtextColor),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { stickerComposeText = "" }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(theme.accentColor),
                    modifier = Modifier.clickable {
                        onStickerSelected(stickerComposeText)
                        stickerComposeText = ""
                    }
                ) {
                    Text(
                        text = AppStrings.trCode(context, appLanguageCode, "ui_keyboard_sub_views_110").replace("➕ ", ""),
                        color = Color.Black,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            when (selectedCategoryId) {
                "designer" -> {
                    // Scrollable Sticker Designer Studio so nothing is clipped
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Live Sticker Preview Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(85.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(getStickerBrush(selectedDesignStyle))
                                .border(
                                    1.5.dp,
                                    getStickerBorderColor(selectedDesignStyle, Color(theme.accentColor)),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = designerEmoji,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = designerText.ifBlank { if (isRtl) "اكتب نص الملصق هنا..." else "Type sticker text here..." },
                                    color = getStickerTextColor(selectedDesignStyle),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Quick Emojis Row for the Sticker
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("✨", "🌸", "👑", "🔥", "🤍", "🌹", "🥳", "🤲", "🌙", "🕌", "😂", "💖", "🚀", "⚡", "☕", "💎", "🕊️", "🎈").forEach { em ->
                                val isSelected = em == designerEmoji
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(theme.accentColor).copy(alpha = 0.3f)
                                            else Color(theme.keyBackgroundColor)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                        .clickable { designerEmoji = em },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = em, fontSize = 15.sp)
                                }
                            }
                        }

                        // Text input for sticker using clean BasicTextField to avoid any clipping
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(theme.keyBackgroundColor))
                                .border(
                                    1.dp,
                                    Color(theme.borderColor).copy(alpha = 0.6f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (designerText.isEmpty()) {
                                Text(
                                    text = if (isRtl) "اكتب عبارة أو جملة الملصق..." else "Enter sticker phrase...",
                                    color = Color(theme.subtextColor).copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                            BasicTextField(
                                value = designerText,
                                onValueChange = { designerText = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = Color(theme.keyTextColor),
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(Color(theme.accentColor)),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Style choices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val stylesList = listOf(
                                "ذهبي" to (if (isRtl) "ذهبي" else "Gold"),
                                "نيون" to (if (isRtl) "نيون" else "Neon"),
                                "وردي" to (if (isRtl) "وردي" else "Rose"),
                                "زمردي" to (if (isRtl) "زمردي" else "Emerald"),
                                "بنفسجي" to (if (isRtl) "بنفسجي" else "Purple"),
                                "زجاجي" to (if (isRtl) "زجاجي" else "Glass")
                            )
                            stylesList.forEach { (stKey, stLabel) ->
                                val isSel = stKey == selectedDesignStyle
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) Color(theme.accentColor).copy(alpha = 0.3f) else Color(theme.keyBackgroundColor),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSel) Color(theme.accentColor) else Color(theme.borderColor).copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDesignStyle = stKey }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = stLabel,
                                            color = if (isSel) Color(theme.accentColor) else Color(theme.keyTextColor),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Actions: Save to My Stickers + Send directly
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val finalSticker = "$designerEmoji ${designerText.ifBlank { if (isRtl) "ملصق جديد" else "New Sticker" }}"
                                    prefs.saveCustomSticker(finalSticker)
                                    customStickers = prefs.getCustomStickers()
                                    Toast.makeText(context, if (isRtl) "تم حفظ الملصق في (ملصقاتي) ⭐" else "Saved to My Stickers ⭐", Toast.LENGTH_SHORT).show()
                                    selectedCategoryId = "my_stickers"
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(theme.keyBackgroundColor)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(theme.accentColor), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_settings_screen_046"), color = Color(theme.keyTextColor), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val finalSticker = "$designerEmoji ${designerText.ifBlank { if (isRtl) "ملصق رائع" else "Awesome Sticker" }}"
                                    onStickerSelected(finalSticker)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor)),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(AppStrings.trCode(context, appLanguageCode, "ui_chat_simulator_screen_012"), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "my_stickers" -> {
                    if (customStickers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    if (isRtl) "لم تقم بتصميم أي ملصقات بعد" else "No custom stickers yet",
                                    color = Color(theme.subtextColor),
                                    fontSize = 12.5.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { selectedCategoryId = "designer" },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(theme.accentColor))
                                ) {
                                    Text(
                                        if (isRtl) "🎨 اضغط هنا لتصميم ملصقك الأول" else "🎨 Design Your First Sticker",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
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
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(theme.accentColor).copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { onStickerSelected(sticker) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = sticker,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                prefs.deleteCustomSticker(sticker)
                                                customStickers = prefs.getCustomStickers()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Rich Built-in categories
                    val list = BuiltinStickers.items.filter { it.category == selectedCategoryId }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(list) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(getStickerBrush(item.styleKey))
                                    .border(
                                        1.dp,
                                        getStickerBorderColor(item.styleKey, Color(theme.accentColor)),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        onStickerSelected("${item.emoji} ${item.text}")
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = item.emoji,
                                        fontSize = 20.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.text,
                                        color = getStickerTextColor(item.styleKey),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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

// Helpers for visual sticker styles
private fun getStickerBrush(styleKey: String): Brush {
    return when (styleKey) {
        "نيون" -> Brush.horizontalGradient(listOf(Color(0xFF071B26), Color(0xFF0F3A4A), Color(0xFF082230)))
        "ذهبي" -> Brush.horizontalGradient(listOf(Color(0xFF2E2207), Color(0xFF4A380A), Color(0xFF1F1703)))
        "وردي" -> Brush.horizontalGradient(listOf(Color(0xFF3F0B26), Color(0xFF6B113B), Color(0xFF2A0518)))
        "زمردي" -> Brush.horizontalGradient(listOf(Color(0xFF062B1E), Color(0xFF0F4D37), Color(0xFF041D14)))
        "بنفسجي" -> Brush.horizontalGradient(listOf(Color(0xFF240D3E), Color(0xFF451A75), Color(0xFF160628)))
        "زجاجي" -> Brush.horizontalGradient(listOf(Color(0x33334155), Color(0x1F1E293B)))
        "emerald" -> Brush.horizontalGradient(listOf(Color(0xFF062B1E), Color(0xFF0F4D37), Color(0xFF041D14)))
        "gold" -> Brush.horizontalGradient(listOf(Color(0xFF2E2207), Color(0xFF4A380A), Color(0xFF1F1703)))
        "rose" -> Brush.horizontalGradient(listOf(Color(0xFF3F0B26), Color(0xFF6B113B), Color(0xFF2A0518)))
        "purple" -> Brush.horizontalGradient(listOf(Color(0xFF240D3E), Color(0xFF451A75), Color(0xFF160628)))
        "neon" -> Brush.horizontalGradient(listOf(Color(0xFF071B26), Color(0xFF0F3A4A), Color(0xFF082230)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
    }
}

private fun getStickerBorderColor(styleKey: String, fallbackAccent: Color): Color {
    return when (styleKey) {
        "نيون", "neon" -> Color(0xFF00E5FF).copy(alpha = 0.8f)
        "ذهبي", "gold" -> Color(0xFFFFD700).copy(alpha = 0.8f)
        "وردي", "rose" -> Color(0xFFFF4081).copy(alpha = 0.8f)
        "زمردي", "emerald" -> Color(0xFF10B981).copy(alpha = 0.8f)
        "بنفسجي", "purple" -> Color(0xFFA855F7).copy(alpha = 0.8f)
        "زجاجي", "glass" -> Color.White.copy(alpha = 0.35f)
        else -> fallbackAccent.copy(alpha = 0.6f)
    }
}

private fun getStickerTextColor(styleKey: String): Color {
    return when (styleKey) {
        "ذهبي", "gold" -> Color(0xFFFFE898)
        "نيون", "neon" -> Color(0xFF00F5FF)
        "وردي", "rose" -> Color(0xFFFF80AB)
        "زمردي", "emerald" -> Color(0xFF6EE7B7)
        "بنفسجي", "purple" -> Color(0xFFD8B4FE)
        else -> Color.White
    }
}
