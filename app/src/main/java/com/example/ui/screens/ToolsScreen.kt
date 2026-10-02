package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeonToolGridCard

@Composable
fun ToolsScreen(
    onNavigateToEmoji: () -> Unit,
    onNavigateToGif: () -> Unit,
    onNavigateToTranslate: () -> Unit,
    onNavigateToClipboard: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onToggleNightMode: () -> Unit,
    onNavigateToDecoration: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val keyboardStatus = remember { com.example.utils.KeyboardStatusHelper.checkKeyboardStatus(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090C12))
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
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
                text = "أدوات الكيبورد",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Activation Status Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onNavigateToSetup),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131926),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (keyboardStatus == com.example.utils.KeyboardStatus.ACTIVE_DEFAULT) Color(0xFF10B981).copy(alpha = 0.6f)
                else Color(0xFF00E5FF).copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (keyboardStatus == com.example.utils.KeyboardStatus.ACTIVE_DEFAULT) Color(0xFF10B981).copy(alpha = 0.2f)
                                else Color(0xFF00E5FF).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (keyboardStatus == com.example.utils.KeyboardStatus.ACTIVE_DEFAULT) Icons.Default.CheckCircle else Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (keyboardStatus == com.example.utils.KeyboardStatus.ACTIVE_DEFAULT) Color(0xFF10B981) else Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (keyboardStatus == com.example.utils.KeyboardStatus.ACTIVE_DEFAULT) "الكيبورد مفعل ويعمل بنجاح" else "إعداد وتفعيل كيبورد Turbo",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (keyboardStatus == com.example.utils.KeyboardStatus.ACTIVE_DEFAULT) "Turbo Keyboard هو لوحة المفاتيح الافتراضية" else "اضغط هنا لتفعيل الكيبورد واختياره كافتراضي",
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid of 6 Neon tools matching Screenshot 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NeonToolGridCard(
                title = "إيموجي",
                icon = Icons.Outlined.Mood,
                neonColor = Color(0xFF00E5FF),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToEmoji
            )
            NeonToolGridCard(
                title = "GIF",
                icon = Icons.Outlined.Gif,
                neonColor = Color(0xFF818CF8),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToGif
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NeonToolGridCard(
                title = "ترجمة",
                icon = Icons.Default.Translate,
                neonColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToTranslate
            )
            NeonToolGridCard(
                title = "حافظة",
                icon = Icons.Outlined.ContentPaste,
                neonColor = Color(0xFFC084FC),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToClipboard
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NeonToolGridCard(
                title = "كتابة صوتية",
                icon = Icons.Outlined.Mic,
                neonColor = Color(0xFF60A5FA),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToVoice
            )
            NeonToolGridCard(
                title = "وضع ليلي",
                icon = Icons.Outlined.NightlightRound,
                neonColor = Color(0xFFA855F7),
                modifier = Modifier.weight(1f),
                onClick = onToggleNightMode
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Extra Decoration tool row
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onNavigateToDecoration),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF141926).copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.65f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "Decoration",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "زخرفة النصوص العربية والإنجليزية",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "عشرات الخطوط والزخارف الاحترافية ꧁ ༺ 𝕿𝖚𝖗𝖇𝖔",
                            color = Color(0xFF8E9BAE),
                            fontSize = 12.sp
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom customizable toolbar banner matching Screenshot 3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF131825))
                .border(1.dp, Color(0xFF26334A), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.DashboardCustomize,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "شريط أدوات قابل للتخصيص",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "بإمكانك تعديل الأدوات والترتيب في شريط الكيبورد حسب احتياجك",
                    color = Color(0xFF8E9BAE),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
