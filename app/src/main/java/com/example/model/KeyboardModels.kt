package com.example.model

data class KeyboardTheme(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val isCustom: Boolean = false,
    val backgroundColor: Long = 0xFF0E1118,
    val backgroundGradient: List<Long>? = null,
    val keyBackgroundColor: Long = 0xFF1C2230,
    val keyPressedColor: Long = 0xFF283247,
    val keyTextColor: Long = 0xFFFFFFFF,
    val subtextColor: Long = 0xFF8E9BAE,
    val accentColor: Long = 0xFF00D2FF,
    val enterButtonColor: Long = 0xFF1E88E5,
    val toolbarColor: Long = 0xFF141923,
    val borderColor: Long = 0xFF2A344A,
    val cornerRadius: Float = 10f,
    val borderAlpha: Float = 0.5f,
    val backgroundImageUri: String? = null,
    val backgroundDim: Float = 0.45f,
    val keyOpacity: Float = 1.0f,
    val keyStyle: String = "rounded", // "rounded", "glass", "neon", "bubble", "carbon", "gold", "retro"
    val category: String = "modern", // "dark_light", "neon", "minimal", "gradient", "glass", "gaming", "elegant", "colorful", "anime", "systems"
    val isGlass: Boolean = false,
    val dualLanguageHints: Boolean = true,
    val specialKeyColor: Long? = null
)

data class ClipboardItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

data class TextShortcut(
    val id: String = java.util.UUID.randomUUID().toString(),
    val trigger: String,
    val expansion: String
)

data class KeyboardSettings(
    val vibrationEnabled: Boolean = true,
    val vibrationDurationMs: Int = 30, // Solid medium-to-strong haptic as requested
    val soundEnabled: Boolean = true, // Enabled with custom sounds
    val soundProfile: String = "ios_16", // "ios_16", "mechanical", "modern_soft", "water_drop", "pop_bubble", "wood_block", "cyber_scifi", "classic_typewriter", "system_default"
    val soundVolume: Float = 0.85f,
    val keyPressTimingStyle: String = "ios_balanced", // "ios_balanced" (iOS 16 natural feel), "ultra_fast"
    val typingSpeedMode: String = "medium", // "fast", "medium" (iOS 16), "slow", "custom"
    val keyRepeatSpeedMs: Int = 45, // 25ms (fast), 45ms (medium), 80ms (slow)
    val longPressDelayMs: Int = 340, // 200ms (fast), 340ms (medium), 520ms (slow)
    val keyPopupEnabled: Boolean = true,
    val autoCapitalization: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val autoCorrection: Boolean = false,
    val numberRowEnabled: Boolean = false,
    val topQuickEmojiRowEnabled: Boolean = true,
    val keyHeightFactor: Float = 1.0f,
    val keyFontSizeFactor: Float = 1.0f,
    val currentThemeId: String = "cyber_pro",
    val defaultLanguage: String = "ar",
    val swipeSpaceSwitchLanguage: Boolean = true,
    val enterLongPressTranslateEnabled: Boolean = true,
    val showDualHints: Boolean = true,
    val clipboardCloseOnPaste: Boolean = true,
    val clipboardSaveForever: Boolean = true,
    val enterKeyOnLeft: Boolean = false,
    val activeDecorationStyle: String = "none",
    val keyboardLayoutStyle: String = "basic_ar", // "basic_ar", "samsung", "aosp", "linux", "swift"
    val translationSource: String = "ar",
    val translationTarget: String = "en",
    val autoTranslateOnCopy: Boolean = false,
    val visibleToolbarTools: List<String> = listOf(
        "translate", "clipboard", "decoration", "phrases", "calculator", "emoji", "voice", "ai", "photos", "gif", "night", "settings"
    )
)

enum class KeyboardLanguage(val code: String, val displayName: String, val nativeName: String) {
    ARABIC("ar", "Arabic", "عربي اساسي"),
    ENGLISH("en", "English", "English")
}

enum class KeyboardSubView {
    NONE,
    EMOJI,
    GIF,
    PHOTOS,
    TRANSLATE,
    CLIPBOARD,
    DECORATION,
    PHRASES,
    CALCULATOR,
    AI_ASSISTANT,
    TOOLS_MORE,
    VOICE_INPUT,
    SETTINGS,
    CUSTOMIZE_TOOLBAR
}
