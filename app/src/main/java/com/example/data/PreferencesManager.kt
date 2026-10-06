package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ClipboardItem
import com.example.model.KeyboardSettings
import com.example.model.KeyboardTheme
import com.example.model.TextShortcut
import com.example.model.ThemePresets
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)

    // Settings
    fun getSettings(): KeyboardSettings {
        return KeyboardSettings(
            vibrationEnabled = prefs.getBoolean("vibrationEnabled", true),
            vibrationDurationMs = prefs.getInt("vibrationDurationMs", 30),
            soundEnabled = prefs.getBoolean("soundEnabled", true),
            soundProfile = prefs.getString("soundProfile", "ios_16") ?: "ios_16",
            soundVolume = prefs.getFloat("soundVolume", 0.85f),
            keyPressTimingStyle = prefs.getString("keyPressTimingStyle", "ios_balanced") ?: "ios_balanced",
            typingSpeedMode = prefs.getString("typingSpeedMode", "medium") ?: "medium",
            typingSpeedMultiplier = prefs.getFloat("typingSpeedMultiplier", 1.0f),
            keyRepeatSpeedMs = prefs.getInt("keyRepeatSpeedMs", 45),
            longPressDelayMs = prefs.getInt("longPressDelayMs", 340),
            autoReturnToLettersOnSend = prefs.getBoolean("autoReturnToLettersOnSend", true),
            autoReturnToLettersOnShortcut = prefs.getBoolean("autoReturnToLettersOnShortcut", true),
            keyPopupEnabled = prefs.getBoolean("keyPopupEnabled", true),
            autoCapitalization = prefs.getBoolean("autoCapitalization", true),
            doubleSpacePeriod = prefs.getBoolean("doubleSpacePeriod", true),
            suggestionsEnabled = prefs.getBoolean("suggestionsEnabled", true),
            autoCorrection = prefs.getBoolean("autoCorrection", false),
            numberRowEnabled = prefs.getBoolean("numberRowEnabled", false),
            topQuickEmojiRowEnabled = prefs.getBoolean("topQuickEmojiRowEnabled", true),
            keyHeightFactor = prefs.getFloat("keyHeightFactor", 1.0f),
            keyFontSizeFactor = prefs.getFloat("keyFontSizeFactor", 1.0f),
            keyButtonScale = prefs.getFloat("keyButtonScale", 1.0f),
            backspaceKeyScale = prefs.getFloat("backspaceKeyScale", 1.15f),
            currentThemeId = prefs.getString("currentThemeId", "cyber_pro") ?: "cyber_pro",
            defaultLanguage = prefs.getString("defaultLanguage", "ar") ?: "ar",
            swipeSpaceSwitchLanguage = prefs.getBoolean("swipeSpaceSwitchLanguage", true),
            enterLongPressTranslateEnabled = prefs.getBoolean("enterLongPressTranslateEnabled", true),
            showDualHints = prefs.getBoolean("showDualHints", true),
            clipboardCloseOnPaste = prefs.getBoolean("clipboardCloseOnPaste", true),
            clipboardSaveForever = prefs.getBoolean("clipboardSaveForever", true),
            enterKeyOnLeft = prefs.getBoolean("enterKeyOnLeft", false),
            isNightModeEnabled = prefs.getBoolean("isNightModeEnabled", true),
            autoReturnAfterEmojiInsert = prefs.getBoolean("autoReturnAfterEmojiInsert", false),
            activeDecorationStyle = prefs.getString("activeDecorationStyle", "none") ?: "none",
            keyboardLayoutStyle = prefs.getString("keyboardLayoutStyle", "samsung") ?: "samsung",
            translationSource = prefs.getString("translationSource", "ar") ?: "ar",
            translationTarget = prefs.getString("translationTarget", "en") ?: "en",
            autoTranslateOnCopy = prefs.getBoolean("autoTranslateOnCopy", false),
            visibleToolbarTools = getVisibleToolbarTools()
        )
    }

    fun getVisibleToolbarTools(): List<String> {
        val raw = prefs.getString("visibleToolbarTools", null)
        return if (raw.isNullOrBlank()) {
            listOf("stickers", "translate", "clipboard", "decoration", "phrases", "calculator", "emoji", "voice", "ai", "photos", "gif", "settings")
        } else {
            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    fun saveVisibleToolbarTools(tools: List<String>) {
        prefs.edit().putString("visibleToolbarTools", tools.joinToString(",")).apply()
    }

    fun saveSettings(settings: KeyboardSettings) {
        saveVisibleToolbarTools(settings.visibleToolbarTools)
        prefs.edit()
            .putBoolean("vibrationEnabled", settings.vibrationEnabled)
            .putInt("vibrationDurationMs", settings.vibrationDurationMs)
            .putBoolean("soundEnabled", settings.soundEnabled)
            .putString("soundProfile", settings.soundProfile)
            .putFloat("soundVolume", settings.soundVolume)
            .putString("keyPressTimingStyle", settings.keyPressTimingStyle)
            .putString("typingSpeedMode", settings.typingSpeedMode)
            .putFloat("typingSpeedMultiplier", settings.typingSpeedMultiplier)
            .putInt("keyRepeatSpeedMs", settings.keyRepeatSpeedMs)
            .putInt("longPressDelayMs", settings.longPressDelayMs)
            .putBoolean("autoReturnToLettersOnSend", settings.autoReturnToLettersOnSend)
            .putBoolean("autoReturnToLettersOnShortcut", settings.autoReturnToLettersOnShortcut)
            .putBoolean("autoReturnAfterEmojiInsert", settings.autoReturnAfterEmojiInsert)
            .putBoolean("keyPopupEnabled", settings.keyPopupEnabled)
            .putBoolean("autoCapitalization", settings.autoCapitalization)
            .putBoolean("doubleSpacePeriod", settings.doubleSpacePeriod)
            .putBoolean("suggestionsEnabled", settings.suggestionsEnabled)
            .putBoolean("autoCorrection", settings.autoCorrection)
            .putBoolean("numberRowEnabled", settings.numberRowEnabled)
            .putBoolean("topQuickEmojiRowEnabled", settings.topQuickEmojiRowEnabled)
            .putFloat("keyHeightFactor", settings.keyHeightFactor)
            .putFloat("keyFontSizeFactor", settings.keyFontSizeFactor)
            .putFloat("keyButtonScale", settings.keyButtonScale)
            .putFloat("backspaceKeyScale", settings.backspaceKeyScale)
            .putString("currentThemeId", settings.currentThemeId)
            .putString("defaultLanguage", settings.defaultLanguage)
            .putBoolean("swipeSpaceSwitchLanguage", settings.swipeSpaceSwitchLanguage)
            .putBoolean("enterLongPressTranslateEnabled", settings.enterLongPressTranslateEnabled)
            .putBoolean("showDualHints", settings.showDualHints)
            .putBoolean("clipboardCloseOnPaste", settings.clipboardCloseOnPaste)
            .putBoolean("clipboardSaveForever", settings.clipboardSaveForever)
            .putBoolean("enterKeyOnLeft", settings.enterKeyOnLeft)
            .putBoolean("isNightModeEnabled", settings.isNightModeEnabled)
            .putString("activeDecorationStyle", settings.activeDecorationStyle)
            .putString("keyboardLayoutStyle", settings.keyboardLayoutStyle)
            .putString("translationSource", settings.translationSource)
            .putString("translationTarget", settings.translationTarget)
            .putBoolean("autoTranslateOnCopy", settings.autoTranslateOnCopy)
            .apply()
    }

    // Custom stickers persistence
    fun getCustomStickers(): List<String> {
        val raw = prefs.getString("custom_stickers_list", null)
        return if (raw.isNullOrBlank()) {
            listOf("صباح الورد والياسمين 🌸", "ألف مبروك التميز 🥳", "فديتك يا الغالي ❤️", "الله يسعدك ويحفظك 🤲")
        } else {
            raw.split(";;;").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }

    fun saveCustomSticker(stickerText: String) {
        val existing = getCustomStickers().toMutableList()
        if (!existing.contains(stickerText)) {
            existing.add(0, stickerText)
            prefs.edit().putString("custom_stickers_list", existing.joinToString(";;;")).apply()
        }
    }

    fun deleteCustomSticker(stickerText: String) {
        val existing = getCustomStickers().toMutableList()
        existing.remove(stickerText)
        prefs.edit().putString("custom_stickers_list", existing.joinToString(";;;")).apply()
    }

    fun setCurrentTheme(themeId: String) {
        prefs.edit().putString("currentThemeId", themeId).apply()
    }

    fun setActiveDecorationStyle(style: String) {
        prefs.edit().putString("activeDecorationStyle", style).apply()
    }

    fun getActiveDecorationStyle(): String {
        return prefs.getString("activeDecorationStyle", "none") ?: "none"
    }

    // Scroll position in Clipboard (Index and exact pixel Offset)
    fun getLastClipboardScrollIndex(): Int {
        return prefs.getInt("last_clipboard_scroll_idx", 0)
    }

    fun setLastClipboardScrollIndex(idx: Int) {
        prefs.edit().putInt("last_clipboard_scroll_idx", idx).apply()
    }

    fun getLastClipboardScrollOffset(): Int {
        return prefs.getInt("last_clipboard_scroll_offset", 0)
    }

    fun setLastClipboardScrollOffset(offset: Int) {
        prefs.edit().putInt("last_clipboard_scroll_offset", offset).apply()
    }

    // Custom Themes storage
    fun getCustomThemes(): List<KeyboardTheme> {
        val json = prefs.getString("custom_themes_list", "[]") ?: "[]"
        val list = mutableListOf<KeyboardTheme>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    KeyboardTheme(
                        id = obj.getString("id"),
                        nameAr = obj.getString("nameAr"),
                        nameEn = obj.getString("nameEn"),
                        isCustom = true,
                        backgroundColor = obj.getLong("backgroundColor"),
                        keyBackgroundColor = obj.getLong("keyBackgroundColor"),
                        keyPressedColor = obj.getLong("keyPressedColor"),
                        keyTextColor = obj.getLong("keyTextColor"),
                        subtextColor = obj.getLong("subtextColor"),
                        accentColor = obj.getLong("accentColor"),
                        enterButtonColor = obj.getLong("enterButtonColor"),
                        toolbarColor = obj.getLong("toolbarColor"),
                        borderColor = obj.getLong("borderColor"),
                        cornerRadius = obj.getDouble("cornerRadius").toFloat(),
                        borderAlpha = obj.getDouble("borderAlpha").toFloat(),
                        backgroundImageUri = if (obj.has("backgroundImageUri") && !obj.isNull("backgroundImageUri")) obj.getString("backgroundImageUri") else null,
                        keyStyle = obj.optString("keyStyle", "rounded"),
                        category = obj.optString("category", "modern")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveCustomTheme(theme: KeyboardTheme) {
        val current = getCustomThemes().toMutableList()
        current.removeAll { it.id == theme.id }
        current.add(0, theme)

        val arr = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("nameAr", t.nameAr)
                put("nameEn", t.nameEn)
                put("backgroundColor", t.backgroundColor)
                put("keyBackgroundColor", t.keyBackgroundColor)
                put("keyPressedColor", t.keyPressedColor)
                put("keyTextColor", t.keyTextColor)
                put("subtextColor", t.subtextColor)
                put("accentColor", t.accentColor)
                put("enterButtonColor", t.enterButtonColor)
                put("toolbarColor", t.toolbarColor)
                put("borderColor", t.borderColor)
                put("cornerRadius", t.cornerRadius.toDouble())
                put("borderAlpha", t.borderAlpha.toDouble())
                put("backgroundImageUri", t.backgroundImageUri)
                put("keyStyle", t.keyStyle)
                put("category", t.category)
            }
            arr.put(obj)
        }
        prefs.edit().putString("custom_themes_list", arr.toString()).apply()
    }

    fun deleteCustomTheme(themeId: String) {
        val current = getCustomThemes().toMutableList()
        current.removeAll { it.id == themeId }
        val arr = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("nameAr", t.nameAr)
                put("nameEn", t.nameEn)
                put("backgroundColor", t.backgroundColor)
                put("keyBackgroundColor", t.keyBackgroundColor)
                put("keyPressedColor", t.keyPressedColor)
                put("keyTextColor", t.keyTextColor)
                put("subtextColor", t.subtextColor)
                put("accentColor", t.accentColor)
                put("enterButtonColor", t.enterButtonColor)
                put("toolbarColor", t.toolbarColor)
                put("borderColor", t.borderColor)
                put("cornerRadius", t.cornerRadius.toDouble())
                put("borderAlpha", t.borderAlpha.toDouble())
                put("backgroundImageUri", t.backgroundImageUri)
                put("keyStyle", t.keyStyle)
                put("category", t.category)
            }
            arr.put(obj)
        }
        prefs.edit().putString("custom_themes_list", arr.toString()).apply()
        if (prefs.getString("currentThemeId", "") == themeId) {
            setCurrentTheme("cyber_pro")
        }
    }

    fun getActiveTheme(): KeyboardTheme {
        val id = prefs.getString("currentThemeId", "cyber_pro") ?: "cyber_pro"
        return getCustomThemes().find { it.id == id } ?: ThemePresets.getById(id)
    }

    fun saveActiveTheme(theme: KeyboardTheme) {
        setCurrentTheme(theme.id)
    }

    @Volatile
    private var cachedClipboardItems: List<ClipboardItem>? = null

    // Permanent Clipboard storage ("حفظ النصوص للأبد") with in-memory 0ms cache
    fun getClipboardItems(): List<ClipboardItem> {
        val cached = cachedClipboardItems
        if (cached != null) return cached

        val json = prefs.getString("clipboard_history", null)
        if (json == null) {
            val initial = listOf(
                ClipboardItem(text = "مرحبا! كيف حالك اليوم؟", isPinned = true),
                ClipboardItem(text = "السلام عليكم ورحمة الله وبركاته", isPinned = true),
                ClipboardItem(text = "جزاك الله خيراً وبارك فيك", isPinned = true),
                ClipboardItem(text = "صلى الله عليه وسلم", isPinned = true),
                ClipboardItem(text = "أنا بخير، شكراً لك! 😊", isPinned = false),
                ClipboardItem(text = "https://google.com", isPinned = false)
            )
            cachedClipboardItems = initial
            saveClipboardItems(initial)
            return initial
        }

        val list = mutableListOf<ClipboardItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ClipboardItem(
                        id = obj.getString("id"),
                        text = obj.getString("text"),
                        timestamp = obj.getLong("timestamp"),
                        isPinned = obj.optBoolean("isPinned", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedClipboardItems = list
        return list
    }

    fun saveClipboardItems(items: List<ClipboardItem>) {
        cachedClipboardItems = items
        val arr = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("text", item.text)
                put("timestamp", item.timestamp)
                put("isPinned", item.isPinned)
            }
            arr.put(obj)
        }
        prefs.edit().putString("clipboard_history", arr.toString()).apply()
    }

    fun addClipboardItem(text: String, isPinned: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val current = getClipboardItems().toMutableList()
        current.removeAll { it.text == trimmed }
        current.add(0, ClipboardItem(text = trimmed, isPinned = isPinned))
        // Stored forever without small limit - up to 10000 items
        if (current.size > 10000) {
            saveClipboardItems(current.take(10000))
        } else {
            saveClipboardItems(current)
        }
    }

    fun togglePinClipboard(id: String) {
        val current = getClipboardItems().map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveClipboardItems(current)
    }

    fun deleteClipboardItem(id: String) {
        val current = getClipboardItems().filterNot { it.id == id }
        saveClipboardItems(current)
    }

    fun clearClipboardHistory() {
        // Keep pinned only
        val current = getClipboardItems().filter { it.isPinned }
        saveClipboardItems(current)
    }

    // Text Shortcuts (الاختصارات)
    fun getShortcuts(): List<TextShortcut> {
        val json = prefs.getString("text_shortcuts_list", null)
        if (json == null) {
            val initial = listOf(
                TextShortcut(trigger = "سلام", expansion = "السلام عليكم ورحمة الله وبركاته"),
                TextShortcut(trigger = "ص", expansion = "صلى الله عليه وسلم"),
                TextShortcut(trigger = "جزاك", expansion = "جزاك الله خيراً ونفع بك"),
                TextShortcut(trigger = "إن شاء", expansion = "إن شاء الله تعالى"),
                TextShortcut(trigger = "شكرا", expansion = "شكراً جزيلاً لك وبارك الله فيك")
            )
            saveShortcuts(initial)
            return initial
        }
        val list = mutableListOf<TextShortcut>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    TextShortcut(
                        id = obj.getString("id"),
                        trigger = obj.getString("trigger"),
                        expansion = obj.getString("expansion")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveShortcuts(shortcuts: List<TextShortcut>) {
        val arr = JSONArray()
        for (s in shortcuts) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("trigger", s.trigger)
                put("expansion", s.expansion)
            }
            arr.put(obj)
        }
        prefs.edit().putString("text_shortcuts_list", arr.toString()).apply()
    }

    fun addShortcut(trigger: String, expansion: String) {
        if (trigger.isBlank() || expansion.isBlank()) return
        val current = getShortcuts().toMutableList()
        current.removeAll { it.trigger.equals(trigger.trim(), ignoreCase = true) }
        current.add(0, TextShortcut(trigger = trigger.trim(), expansion = expansion.trim()))
        saveShortcuts(current)
    }

    fun deleteShortcut(id: String) {
        val current = getShortcuts().filterNot { it.id == id }
        saveShortcuts(current)
    }

    // Recent Emojis
    fun getRecentEmojis(): List<String> {
        val saved = prefs.getString("recent_emojis", "😊,❤️,😂,👍,🔥,✨,🎉,👋,🤲,🌙,👑,💋") ?: "😊,❤️,😂,👍,🔥,✨,🎉,👋,🤲,🌙,👑,💋"
        return saved.split(",").filter { it.isNotBlank() }
    }

    fun addRecentEmoji(emoji: String) {
        val current = getRecentEmojis().toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        prefs.edit().putString("recent_emojis", current.take(32).joinToString(",")).apply()
    }

    // Backup & Restore
    fun exportBackupJson(): String {
        val root = JSONObject().apply {
            put("clipboard", JSONArray(prefs.getString("clipboard_history", "[]")))
            put("shortcuts", JSONArray(prefs.getString("text_shortcuts_list", "[]")))
            put("settings", JSONObject().apply {
                val s = getSettings()
                put("vibrationEnabled", s.vibrationEnabled)
                put("soundEnabled", s.soundEnabled)
                put("numberRowEnabled", s.numberRowEnabled)
                put("keyboardLayoutStyle", s.keyboardLayoutStyle)
            })
        }
        return root.toString()
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.has("clipboard")) {
                prefs.edit().putString("clipboard_history", root.getJSONArray("clipboard").toString()).apply()
            }
            if (root.has("shortcuts")) {
                prefs.edit().putString("text_shortcuts_list", root.getJSONArray("shortcuts").toString()).apply()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
