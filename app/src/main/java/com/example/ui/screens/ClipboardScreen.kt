package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DeleteOutline
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PreferencesManager

@Composable
fun ClipboardScreen(
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var items by remember { mutableStateOf(prefs.getClipboardItems(forceRefresh = true)) }
    var searchQuery by remember { mutableStateOf("") }
    var newClipText by remember { mutableStateOf("") }
    var selectedClipForFloatingMenu by remember { mutableStateOf<com.example.model.ClipboardItem?>(null) }

    LaunchedEffect(Unit) {
        prefs.syncWithSystemClipboard(context)
        items = prefs.getClipboardItems(forceRefresh = true)
    }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.text.contains(searchQuery.trim(), ignoreCase = true) }
    }

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
                text = "حافظة النصوص المتقدمة",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = {
                    prefs.clearClipboardHistory()
                    items = prefs.getClipboardItems()
                    Toast.makeText(context, "تم مسح النصوص غير المثبتة", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("مسح", color = Color(0xFFEF4444), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add new clip quick bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newClipText,
                onValueChange = { newClipText = it },
                placeholder = { Text("أضف نصاً جديداً للحافظة...", color = Color(0xFF8E9BAE), fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF26334A),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newClipText.isNotBlank()) {
                        prefs.addClipboardItem(newClipText, isPinned = true)
                        items = prefs.getClipboardItems()
                        newClipText = ""
                        Toast.makeText(context, "تمت إضافة النص وتثبيته", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في الحافظة...", color = Color(0xFF8E9BAE), fontSize = 12.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF26334A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true,
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF8E9BAE))
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Clips list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredItems, key = { it.id }) { clip ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF141926),
                    border = BorderStroke(
                        1.dp,
                        if (clip.isPinned) Color(0xFF00E5FF) else Color(0xFF26334A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(clip.id) {
                            detectTapGestures(
                                onLongPress = {
                                    selectedClipForFloatingMenu = clip
                                },
                                onTap = {
                                    clipboardManager.setText(AnnotatedString(clip.text))
                                    Toast.makeText(context, "تم نسخ النص إلى الحافظة", Toast.LENGTH_SHORT).show()
                                }
                            )
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
                                text = clip.text,
                                color = Color.White,
                                fontSize = 14.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (clip.isPinned) "📌 مثبتة" else "الأخيرة",
                                color = if (clip.isPinned) Color(0xFF00E5FF) else Color(0xFF8E9BAE),
                                fontSize = 10.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    prefs.togglePinClipboard(clip.id)
                                    items = prefs.getClipboardItems()
                                }
                            ) {
                                Icon(
                                    imageVector = if (clip.isPinned) Icons.Default.PushPin else Icons.Default.OutlinedFlag,
                                    contentDescription = "Pin",
                                    tint = if (clip.isPinned) Color(0xFF00E5FF) else Color(0xFF8E9BAE)
                                )
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(clip.text))
                                    Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentPaste,
                                    contentDescription = "Copy",
                                    tint = Color(0xFF38BDF8)
                                )
                            }

                            IconButton(
                                onClick = {
                                    prefs.deleteClipboardItem(clip.id)
                                    items = prefs.getClipboardItems()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Dialog on Long Press (نافذة عائمة عند الضغط المطول)
        if (selectedClipForFloatingMenu != null) {
            val clip = selectedClipForFloatingMenu!!
            AlertDialog(
                onDismissRequest = { selectedClipForFloatingMenu = null },
                containerColor = Color(0xFF141926),
                title = {
                    Text(
                        text = "خيارات النص المنسوخ",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = clip.text,
                            color = Color(0xFF8E9BAE),
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        HorizontalDivider(color = Color(0xFF26334A))
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // 1. حذف
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        prefs.deleteClipboardItem(clip.id)
                                        items = prefs.getClipboardItems()
                                        selectedClipForFloatingMenu = null
                                        Toast.makeText(context, "تم حذف النص", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFEF4444))
                                Text("حذف", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            // 2. تثبيت
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        prefs.togglePinClipboard(clip.id)
                                        items = prefs.getClipboardItems()
                                        selectedClipForFloatingMenu = null
                                        Toast.makeText(context, if (!clip.isPinned) "تم التثبيت" else "تم إلغاء التثبيت", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Default.PushPin, contentDescription = "تثبيت", tint = Color(0xFF00E5FF))
                                Text(if (clip.isPinned) "إلغاء التثبيت" else "تثبيت", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            // 3. نسخ
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(clip.text))
                                        selectedClipForFloatingMenu = null
                                        Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Outlined.ContentPaste, contentDescription = "نسخ", tint = Color(0xFF38BDF8))
                                Text("نسخ", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedClipForFloatingMenu = null }) {
                        Text("إغلاق", color = Color(0xFF8E9BAE))
                    }
                }
            )
        }
    }
}
