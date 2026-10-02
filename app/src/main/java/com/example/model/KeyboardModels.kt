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
    val keyStyle: String = "rounded", // "rounded", "glass", "neon", "bubble", "carbon", "gold", "retro"
    val category: String = "modern", // "dark_light", "neon", "minimal", "gradient", "glass", "gaming", "elegant", "colorful", "anime"
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

data class KeyboardSettings(
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val keyPopupEnabled: Boolean = true,
    val autoCapitalization: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val suggestionsEnabled: Boolean = true,
    val autoCorrection: Boolean = false,
    val numberRowEnabled: Boolean = false,
    val keyHeightFactor: Float = 1.0f,
    val currentThemeId: String = "cyber_pro",
    val defaultLanguage: String = "ar",
    val swipeSpaceSwitchLanguage: Boolean = true,
    val enterLongPressTranslateEnabled: Boolean = true,
    val showDualHints: Boolean = true
)

enum class KeyboardLanguage(val code: String, val displayName: String, val nativeName: String) {
    ARABIC("ar", "Arabic", "العربية"),
    ENGLISH("en", "English (US)", "English (US)")
}

enum class KeyboardSubView {
    NONE,
    EMOJI,
    GIF,
    TRANSLATE,
    CLIPBOARD,
    DECORATION,
    TOOLS_MORE
}
