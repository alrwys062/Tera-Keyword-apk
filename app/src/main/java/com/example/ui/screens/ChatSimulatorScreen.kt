package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager
import com.example.keyboard.TurboKeyboardView
import com.example.model.KeyboardTheme
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isFromMe: Boolean
)

@Composable
fun ChatSimulatorScreen(
    prefs: PreferencesManager,
    currentTheme: KeyboardTheme,
    onNavigateToSettings: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isEnglishMockup by remember { mutableStateOf(false) }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(text = "مرحبا! كيف حالك اليوم؟", isFromMe = false),
                ChatMessage(text = "أنا بخير، شكراً! 😊", isFromMe = true)
            )
        )
    }

    var currentDraftText by remember { mutableStateOf("") }

    // Scroll to bottom when message added
    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F))
    ) {
        // Top App Bar matching Phone 1 & 2 in Screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B0E14))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141926))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isEnglishMockup) "New Message" else "محادثة جديدة",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تجربة كيبورد برو التفاعلية",
                    color = Color(0xFF00E5FF),
                    fontSize = 10.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch sample chat language
                TextButton(
                    onClick = {
                        isEnglishMockup = !isEnglishMockup
                        messages = if (isEnglishMockup) {
                            listOf(
                                ChatMessage(text = "Hello! How are you?", isFromMe = false),
                                ChatMessage(text = "I'm good, thank you! 😊", isFromMe = true)
                            )
                        } else {
                            listOf(
                                ChatMessage(text = "مرحبا! كيف حالك اليوم؟", isFromMe = false),
                                ChatMessage(text = "أنا بخير، شكراً! 😊", isFromMe = true)
                            )
                        }
                    }
                ) {
                    Text(
                        text = if (isEnglishMockup) "AR" else "EN",
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF192233)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Avatar",
                        tint = Color(0xFF8E9BAE),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick System Activation Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF101522),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF26334A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "لوحة مفاتيح نظام أندرويد:",
                    color = Color(0xFF8E9BAE),
                    fontSize = 11.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "افتح إعدادات لوحة المفاتيح لتفعيل Turbo Keyboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("1. تفعيل في النظام", fontSize = 10.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("2. اختيار كافتراضي", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Chat messages area (matches Screenshots 1 & 2)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.isFromMe
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    )
                                )
                                .background(
                                    if (isMe) Color(0xFF1C2230)
                                    else Color(0xFF283144)
                                )
                                .border(
                                    1.dp,
                                    if (isMe) Color(0xFF00E5FF).copy(alpha = 0.4f)
                                    else Color(0xFF3B4863),
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Live draft input bar (where keyboard outputs)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B0E14))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color(0xFF141A26),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF26334A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (currentDraftText.isEmpty()) "اكتب رسالة أو جرب إيماءات الكيبورد..." else currentDraftText,
                    color = if (currentDraftText.isEmpty()) Color(0xFF8E9BAE) else Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                if (currentDraftText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            messages = messages + ChatMessage(text = currentDraftText, isFromMe = true)
                            prefs.addClipboardItem(currentDraftText)
                            currentDraftText = ""
                            com.example.keyboard.TurboKeyboardService.resetToLettersSignal.value = System.currentTimeMillis()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E5FF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Embedded Turbo Keyboard matching screenshots!
        TurboKeyboardView(
            theme = currentTheme,
            settings = prefs.getSettings(),
            onDirectInsertText = { text ->
                if (text == "\n") {
                    if (currentDraftText.isNotBlank()) {
                        messages = messages + ChatMessage(text = currentDraftText, isFromMe = true)
                        prefs.addClipboardItem(currentDraftText)
                        currentDraftText = ""
                    }
                } else {
                    currentDraftText += text
                }
            },
            onDirectDeleteLastChar = {
                if (currentDraftText.isNotEmpty()) {
                    currentDraftText = currentDraftText.dropLast(1)
                }
            },
            onDirectClearAndReplaceText = { newText ->
                currentDraftText = newText
            },
            getCurrentText = { currentDraftText },
            onOpenSettingsRequested = {
                onNavigateToSettings?.invoke() ?: onBack()
            },
            prefsManager = prefs
        )
    }
}
