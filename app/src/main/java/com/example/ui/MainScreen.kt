package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.PreferencesManager
import com.example.model.KeyboardTheme
import com.example.model.ThemePresets
import com.example.ui.screens.*
import com.example.utils.KeyboardStatus
import com.example.utils.KeyboardStatusHelper

enum class AppDestination {
    KEYBOARD_SETUP,
    TOOLS,
    THEMES,
    SETTINGS,
    CHAT_SIMULATOR,
    CUSTOM_THEME_BUILDER,
    CLIPBOARD_MANAGER,
    DECORATION_STUDIO,
    TRANSLATION_STUDIO
}

@Composable
fun MainScreen(initialTarget: AppDestination? = null) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val prefs = remember { PreferencesManager(context) }
    var currentTheme by remember { mutableStateOf(prefs.getActiveTheme()) }

    var keyboardStatus by remember {
        mutableStateOf(KeyboardStatusHelper.checkKeyboardStatus(context))
    }

    // Auto-select Setup on first launch if not enabled
    val initialDestination = remember(initialTarget) {
        if (initialTarget != null) initialTarget
        else if (keyboardStatus == KeyboardStatus.DISABLED) AppDestination.KEYBOARD_SETUP
        else AppDestination.TOOLS
    }

    var currentDestination by remember { mutableStateOf(initialDestination) }
    LaunchedEffect(initialTarget) {
        if (initialTarget != null) currentDestination = initialTarget
    }

    // Re-check status on resume
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

    // Back handling for sub-screens
    BackHandler(enabled = currentDestination != AppDestination.TOOLS && currentDestination != AppDestination.KEYBOARD_SETUP) {
        currentDestination = AppDestination.TOOLS
    }

    Scaffold(
        containerColor = Color(0xFF070A0F),
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (currentDestination != AppDestination.CHAT_SIMULATOR && currentDestination != AppDestination.CUSTOM_THEME_BUILDER) {
                FloatingActionButton(
                    onClick = { currentDestination = AppDestination.CHAT_SIMULATOR },
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(54.dp)
                        .border(2.dp, Color(0xFF38BDF8), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Test Keyboard",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (currentDestination in listOf(AppDestination.KEYBOARD_SETUP, AppDestination.TOOLS, AppDestination.THEMES, AppDestination.SETTINGS)) {
                BottomNavigationBar(
                    currentDestination = currentDestination,
                    keyboardStatus = keyboardStatus,
                    onDestinationSelected = { currentDestination = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.KEYBOARD_SETUP -> {
                    KeyboardSetupScreen(
                        prefs = prefs,
                        onNavigateToThemes = { currentDestination = AppDestination.THEMES },
                        onNavigateToSettings = { currentDestination = AppDestination.SETTINGS },
                        onNavigateToChatSimulator = { currentDestination = AppDestination.CHAT_SIMULATOR },
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
                AppDestination.TOOLS -> {
                    ToolsScreen(
                        onNavigateToEmoji = { currentDestination = AppDestination.CHAT_SIMULATOR },
                        onNavigateToGif = { currentDestination = AppDestination.CHAT_SIMULATOR },
                        onNavigateToTranslate = { currentDestination = AppDestination.TRANSLATION_STUDIO },
                        onNavigateToClipboard = { currentDestination = AppDestination.CLIPBOARD_MANAGER },
                        onNavigateToVoice = { currentDestination = AppDestination.CHAT_SIMULATOR },
                        onToggleNightMode = {
                            val next = if (currentTheme.id == "cyber_pro") ThemePresets.BLACK_GOLD else ThemePresets.CYBER_PRO
                            prefs.setCurrentTheme(next.id)
                            currentTheme = next
                        },
                        onNavigateToDecoration = { currentDestination = AppDestination.DECORATION_STUDIO },
                        onNavigateToSetup = { currentDestination = AppDestination.KEYBOARD_SETUP },
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
                AppDestination.THEMES -> {
                    ThemesScreen(
                        prefs = prefs,
                        currentThemeId = currentTheme.id,
                        onThemeApplied = { theme -> currentTheme = theme },
                        onOpenCustomBuilder = { currentDestination = AppDestination.CUSTOM_THEME_BUILDER },
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
                AppDestination.SETTINGS -> {
                    SettingsScreen(
                        prefs = prefs,
                        onNavigateToThemes = { currentDestination = AppDestination.THEMES },
                        onNavigateToClipboard = { currentDestination = AppDestination.CLIPBOARD_MANAGER },
                        onNavigateToSetup = { currentDestination = AppDestination.KEYBOARD_SETUP },
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
                AppDestination.CHAT_SIMULATOR -> {
                    ChatSimulatorScreen(
                        prefs = prefs,
                        currentTheme = currentTheme,
                        onNavigateToSettings = { currentDestination = AppDestination.SETTINGS },
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
                AppDestination.CUSTOM_THEME_BUILDER -> {
                    CustomThemeBuilderScreen(
                        prefs = prefs,
                        onThemeSavedAndApplied = { theme ->
                            currentTheme = theme
                            currentDestination = AppDestination.THEMES
                        },
                        onBack = { currentDestination = AppDestination.THEMES }
                    )
                }
                AppDestination.CLIPBOARD_MANAGER -> {
                    ClipboardScreen(
                        prefs = prefs,
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
                AppDestination.DECORATION_STUDIO -> {
                    DecorationScreen(
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
                AppDestination.TRANSLATION_STUDIO -> {
                    TranslationScreen(
                        onBack = { currentDestination = AppDestination.TOOLS }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentDestination: AppDestination,
    keyboardStatus: KeyboardStatus,
    onDestinationSelected: (AppDestination) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0D111A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E283A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Setup / Activation tab
            val setupBadgeColor = when (keyboardStatus) {
                KeyboardStatus.ACTIVE_DEFAULT -> Color(0xFF10B981)
                KeyboardStatus.ENABLED_NOT_DEFAULT -> Color(0xFF38BDF8)
                KeyboardStatus.DISABLED -> Color(0xFFEF4444)
            }

            BottomBarItem(
                title = "التفعيل",
                icon = if (keyboardStatus == KeyboardStatus.ACTIVE_DEFAULT) Icons.Default.CheckCircle else Icons.Outlined.PowerSettingsNew,
                isSelected = currentDestination == AppDestination.KEYBOARD_SETUP,
                badgeColor = setupBadgeColor,
                onClick = { onDestinationSelected(AppDestination.KEYBOARD_SETUP) }
            )

            // Tools tab
            BottomBarItem(
                title = "الأدوات",
                icon = Icons.Outlined.DashboardCustomize,
                isSelected = currentDestination == AppDestination.TOOLS,
                onClick = { onDestinationSelected(AppDestination.TOOLS) }
            )

            // Themes tab
            BottomBarItem(
                title = "السمات",
                icon = Icons.Outlined.Palette,
                isSelected = currentDestination == AppDestination.THEMES,
                onClick = { onDestinationSelected(AppDestination.THEMES) }
            )

            // Settings tab
            BottomBarItem(
                title = "الإعدادات",
                icon = Icons.Outlined.Settings,
                isSelected = currentDestination == AppDestination.SETTINGS,
                onClick = { onDestinationSelected(AppDestination.SETTINGS) }
            )
        }
    }
}
}

@Composable
fun BottomBarItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badgeColor: Color? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.15f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8E9BAE),
                    modifier = Modifier.size(22.dp)
                )
                if (badgeColor != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF8E9BAE),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
