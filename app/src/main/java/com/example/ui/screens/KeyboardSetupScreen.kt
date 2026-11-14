package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.PreferencesManager
import com.example.keyboard.TurboKeyboardView
import com.example.utils.KeyboardStatus
import com.example.utils.KeyboardStatusHelper

@Composable
fun KeyboardSetupScreen(
    prefs: PreferencesManager,
    appLanguageCode: String = "auto",
    onNavigateToThemes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChatSimulator: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var keyboardStatus by remember {
        mutableStateOf(KeyboardStatusHelper.checkKeyboardStatus(context))
    }

    var testInputText by remember { mutableStateOf("") }

    // Automatic real-time detection whenever user returns from Android Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                keyboardStatus = KeyboardStatusHelper.checkKeyboardStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isEnabled = keyboardStatus != KeyboardStatus.DISABLED
    val isDefault = keyboardStatus == KeyboardStatus.ACTIVE_DEFAULT

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A0F))
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141926))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = com.example.util.AppStrings.trCode(context, appLanguageCode, "ui_chat_simulator_screen_003"),
                        tint = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(38.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_title"),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_subtitle"),
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = {
                    keyboardStatus = KeyboardStatusHelper.checkKeyboardStatus(context)
                    Toast.makeText(context, com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_active_title"), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF141926))
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Status",
                    tint = Color(0xFF00E5FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Icon Card displaying the Turbo Keyboard icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F1424))
                .border(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFD500F9))), RoundedCornerShape(20.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.turbo_keyboard_icon_1791119766162),
                    contentDescription = "Turbo Keyboard Icon",
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_hero_title"),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_hero_subtitle"),
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current status banner matching requirements 5 & 13
        AnimatedContent(targetState = keyboardStatus, label = "StatusBanner") { status ->
            when (status) {
                KeyboardStatus.DISABLED -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF261215),
                        border = BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_disabled_title"),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_disabled_desc"),
                                        color = Color(0xFFFCA5A5),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { KeyboardStatusHelper.openEnableKeyboardSettings(context) },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_btn_enable"), color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                KeyboardStatus.ENABLED_NOT_DEFAULT -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF131F33),
                        border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_enabled_title"),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_enabled_desc"),
                                        color = Color(0xFFBAE6FD),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { KeyboardStatusHelper.openSelectKeyboardPicker(context) },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF))
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_btn_select_default"), color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                KeyboardStatus.ACTIVE_DEFAULT -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0E271D),
                        border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.8f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_active_title"),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_hero_subtitle"),
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Step-by-Step Setup Guide
        Text(
            text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_steps_title"),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Step 1 Card: Enable
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131825),
            border = BorderStroke(1.dp, if (isEnabled) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF26334A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) Color(0xFF10B981) else Color(0xFF1E283A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isEnabled) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step1_title"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEnabled) com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_active_title") else com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step1_desc"),
                            color = if (isEnabled) Color(0xFF10B981) else Color(0xFF8E9BAE),
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { KeyboardStatusHelper.openEnableKeyboardSettings(context) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnabled) Color(0xFF1E283A) else Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isEnabled) com.example.util.AppStrings.trCode(context, appLanguageCode, "dim_reset") else com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step1_btn"),
                        color = if (isEnabled) Color.White else Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step 2 Card: Select Default
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131825),
            border = BorderStroke(1.dp, if (isDefault) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF26334A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDefault) Color(0xFF10B981) else Color(0xFF1E283A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDefault) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.Black, modifier = Modifier.size(20.dp))
                        } else {
                            Text("2", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step2_title"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isDefault) com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_active_title") else com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step2_desc"),
                            color = if (isDefault) Color(0xFF10B981) else Color(0xFF8E9BAE),
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = { KeyboardStatusHelper.openSelectKeyboardPicker(context) },
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDefault) Color(0xFF1E283A) else Color(0xFF00E5FF)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isDefault) com.example.util.AppStrings.trCode(context, appLanguageCode, "action_done") else com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step2_btn"),
                        color = if (isDefault) Color.White else Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Step 3 Card: Customize & Languages
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131825),
            border = BorderStroke(1.dp, Color(0xFF26334A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E283A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step3_title"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step3_desc"),
                            color = Color(0xFF8E9BAE),
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onNavigateToThemes,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E283A)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_step3_btn"), color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Live Test Field
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF131825),
            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_test_field_title"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (testInputText.isNotEmpty()) {
                        TextButton(onClick = { testInputText = "" }) {
                            Text(com.example.util.AppStrings.trCode(context, appLanguageCode, "action_clear_all"), color = Color(0xFFEF4444), fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = testInputText,
                    onValueChange = { testInputText = it },
                    placeholder = {
                        Text(
                            com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_test_field_hint"),
                            color = Color(0xFF8E9BAE),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF26334A),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                var showInteractiveKeyboardPreview by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showInteractiveKeyboardPreview = !showInteractiveKeyboardPreview },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showInteractiveKeyboardPreview) Color(0xFF00E5FF) else Color(0xFF1E283A)
                        )
                    ) {
                        Icon(
                            imageVector = if (showInteractiveKeyboardPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (showInteractiveKeyboardPreview) Color.Black else Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showInteractiveKeyboardPreview) {
                                com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_preview_hide")
                            } else {
                                com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_preview_show")
                            },
                            color = if (showInteractiveKeyboardPreview) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onNavigateToChatSimulator,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E5FF)),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(com.example.util.AppStrings.trCode(context, appLanguageCode, "chat_simulator"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { KeyboardStatusHelper.openKeyboardSystemSettings(context) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8E9BAE)),
                        border = BorderStroke(1.dp, Color(0xFF26334A))
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(com.example.util.AppStrings.trCode(context, appLanguageCode, "system_settings"), fontSize = 11.sp)
                    }
                }

                // Interactive live keyboard right inside the preview card
                val prefsVersion by PreferencesManager.prefsVersionFlow.collectAsState()
                val liveTheme = remember(prefsVersion) { prefs.getActiveTheme() }
                val liveSettings = remember(prefsVersion) { prefs.getSettings() }
                AnimatedVisibility(visible = showInteractiveKeyboardPreview) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = com.example.util.AppStrings.trCode(context, appLanguageCode, "interactive_preview_title"),
                                color = Color(0xFF00E5FF),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = com.example.util.AppStrings.trCode(context, appLanguageCode, "interactive_preview_desc"),
                                color = Color(0xFF8E9BAE),
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0A0E18),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f))
                        ) {
                            TurboKeyboardView(
                                theme = liveTheme,
                                settings = liveSettings,
                                onDirectInsertText = { text ->
                                    if (text == "\n") testInputText += "\n"
                                    else testInputText += text
                                },
                                onDirectDeleteLastChar = {
                                    if (testInputText.isNotEmpty()) {
                                        testInputText = testInputText.dropLast(1)
                                    }
                                },
                                onDirectClearAndReplaceText = { newText ->
                                    testInputText = newText
                                },
                                getCurrentText = { testInputText },
                                prefsManager = prefs
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Special guidance for Honor, Huawei, Xiaomi, and Samsung devices
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF101624),
            border = BorderStroke(1.dp, Color(0xFF233048))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_honor_huawei_title"),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = com.example.util.AppStrings.trCode(context, appLanguageCode, "setup_honor_huawei_desc"),
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
