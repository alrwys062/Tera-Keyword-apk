package com.example.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardSubView
import com.example.model.KeyboardTheme

data class ToolbarButton(
    val id: KeyboardSubView,
    val titleAr: String,
    val titleEn: String,
    val icon: ImageVector,
    val isPrimaryGlow: Boolean = false
)

@Composable
fun KeyboardToolbar(
    theme: KeyboardTheme,
    activeSubView: KeyboardSubView,
    onSubViewSelected: (KeyboardSubView) -> Unit,
    onVoiceClick: () -> Unit,
    onThemeToggleClick: () -> Unit,
    isArabic: Boolean
) {
    val buttons = listOf(
        ToolbarButton(KeyboardSubView.EMOJI, "أيموجي", "Emoji", Icons.Outlined.Mood),
        ToolbarButton(KeyboardSubView.GIF, "GIF", "GIF", Icons.Outlined.Gif),
        ToolbarButton(KeyboardSubView.TRANSLATE, "ترجمة", "Translate", Icons.Default.Translate, isPrimaryGlow = true),
        ToolbarButton(KeyboardSubView.CLIPBOARD, "حافظة", "Clipboard", Icons.Outlined.ContentPaste),
        ToolbarButton(KeyboardSubView.DECORATION, "زخرفة", "Decorate", Icons.Outlined.AutoAwesome)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Color(theme.toolbarColor))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick tools
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            buttons.forEach { btn ->
                val isSelected = activeSubView == btn.id
                val isTranslate = btn.id == KeyboardSubView.TRANSLATE

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isSelected -> Color(theme.accentColor).copy(alpha = 0.25f)
                                isTranslate -> Color(theme.enterButtonColor).copy(alpha = 0.2f)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                isSelected -> Color(theme.accentColor)
                                isTranslate -> Color(theme.enterButtonColor).copy(alpha = 0.6f)
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            onSubViewSelected(if (isSelected) KeyboardSubView.NONE else btn.id)
                        }
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = btn.icon,
                        contentDescription = btn.titleAr,
                        tint = when {
                            isSelected -> Color(theme.accentColor)
                            isTranslate -> Color(theme.accentColor)
                            else -> Color(theme.subtextColor)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isArabic) btn.titleAr else btn.titleEn,
                        color = when {
                            isSelected -> Color(theme.accentColor)
                            isTranslate -> Color(theme.accentColor)
                            else -> Color(theme.subtextColor)
                        },
                        fontSize = 9.sp,
                        fontWeight = if (isTranslate || isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Voice typing button
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onVoiceClick() }
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Voice",
                    tint = Color(theme.subtextColor),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isArabic) "صوت" else "Voice",
                    color = Color(theme.subtextColor),
                    fontSize = 9.sp
                )
            }

            // Night / Quick Theme button
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onThemeToggleClick() }
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.NightlightRound,
                    contentDescription = "Theme",
                    tint = Color(theme.subtextColor),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isArabic) "ليلي" else "Theme",
                    color = Color(theme.subtextColor),
                    fontSize = 9.sp
                )
            }
        }
    }
}
