package com.example.data

import android.content.ClipData
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.example.model.ClipboardItem
import com.example.model.KeyboardSettings
import com.example.model.KeyboardTheme
import com.example.model.TextShortcut
import com.example.model.ThemePresets
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)

    companion object {
        val FLOAT_KEYS = setOf(
            "soundVolume", "typingSpeedMultiplier", "keyHeightFactor", "keyFontSizeFactor",
            "keyButtonScale", "backspaceKeyScale", "emojiSizeScale", "emojiBarHeightFactor", "emojiButtonSizeFactor"
        )
        val INT_KEYS = setOf(
            "vibrationDurationMs", "keyRepeatSpeedMs", "longPressDelayMs", "arabicColumnsCount",
            "last_clipboard_scroll_idx", "last_clipboard_scroll_offset"
        )
        val BOOLEAN_KEYS = setOf(
            "vibrationEnabled", "soundEnabled", "autoReturnToLettersOnSend", "autoReturnToLettersOnShortcut",
            "autoReturnAfterEmojiInsert", "keyPopupEnabled", "autoCapitalization", "doubleSpacePeriod",
            "suggestionsEnabled", "autoCorrection", "numberRowEnabled", "topQuickEmojiRowEnabled",
            "swipeSpaceSwitchLanguage", "enterLongPressTranslateEnabled",
            "showDualHints", "clipboardCloseOnPaste", "clipboardSaveForever", "swapClipboardAndEmoji",
            "enterKeyOnLeft", "isNightModeEnabled", "forceEnglishNumbers", "autoTranslateOnCopy"
        )
        val STRING_KEYS = setOf(
            "soundProfile", "keyPressTimingStyle", "typingSpeedMode", "currentThemeId",
            "defaultLanguage", "activeDecorationStyle", "keyboardLayoutStyle",
            "customArabicRow1", "customArabicRow2", "customArabicRow3",
            "translationSource", "translationTarget", "visibleToolbarTools",
            "custom_stickers_list", "custom_themes_list", "clipboard_history",
            "text_shortcuts_list", "recent_emojis", "custom_top_emojis_list"
        )
    }

    init {
        sanitizePrefs()
    }

    fun sanitizePrefs() {
        try {
            val all = prefs.all
            val editor = prefs.edit()
            var changed = false
            for (fk in FLOAT_KEYS) {
                val v = all[fk]
                if (v != null && v !is Float) {
                    val f = when (v) {
                        is Number -> v.toFloat()
                        is String -> v.toFloatOrNull() ?: 1.0f
                        else -> 1.0f
                    }
                    editor.remove(fk)
                    editor.putFloat(fk, f)
                    changed = true
                }
            }
            for (ik in INT_KEYS) {
                val v = all[ik]
                if (v != null && v !is Int) {
                    val i = when (v) {
                        is Number -> v.toInt()
                        is String -> v.toIntOrNull() ?: 30
                        else -> 30
                    }
                    editor.remove(ik)
                    editor.putInt(ik, i)
                    changed = true
                }
            }
            for (bk in BOOLEAN_KEYS) {
                val v = all[bk]
                if (v != null && v !is Boolean) {
                    val b = when (v) {
                        is Boolean -> v
                        is Number -> v.toInt() != 0
                        is String -> v.equals("true", ignoreCase = true) || v == "1"
                        else -> true
                    }
                    editor.remove(bk)
                    editor.putBoolean(bk, b)
                    changed = true
                }
            }
            for (sk in STRING_KEYS) {
                val v = all[sk]
                if (v != null && v !is String) {
                    editor.remove(sk)
                    editor.putString(sk, v.toString())
                    changed = true
                }
            }
            val clips = all["recently_deleted_system_clips"]
            if (clips != null && clips !is Set<*>) {
                editor.remove("recently_deleted_system_clips")
                val repairedClips = when (clips) {
                    is String -> {
                        try {
                            val a = JSONArray(clips)
                            val s = mutableSetOf<String>()
                            for (i in 0 until a.length()) s.add(a.getString(i))
                            s
                        } catch (_: Exception) {
                            clips.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                        }
                    }
                    else -> emptySet()
                }
                editor.putStringSet("recently_deleted_system_clips", repairedClips)
                changed = true
            }
            if (changed) {
                editor.commit()
            }
        } catch (_: Exception) {}
    }

    fun getBooleanSafe(key: String, def: Boolean): Boolean {
        return try {
            prefs.getBoolean(key, def)
        } catch (_: Exception) {
            try {
                val v = prefs.all[key]
                when (v) {
                    is Boolean -> v
                    is Number -> v.toInt() != 0
                    is String -> v.equals("true", ignoreCase = true) || v == "1"
                    else -> def
                }
            } catch (_: Exception) {
                def
            }
        }
    }

    fun getIntSafe(key: String, def: Int): Int {
        return try {
            prefs.getInt(key, def)
        } catch (_: Exception) {
            try {
                val v = prefs.all[key]
                when (v) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: def
                    is Boolean -> if (v) 1 else 0
                    else -> def
                }
            } catch (_: Exception) {
                def
            }
        }
    }

    fun getFloatSafe(key: String, def: Float): Float {
        return try {
            prefs.getFloat(key, def)
        } catch (_: Exception) {
            try {
                val v = prefs.all[key]
                when (v) {
                    is Number -> v.toFloat()
                    is String -> v.toFloatOrNull() ?: def
                    else -> def
                }
            } catch (_: Exception) {
                def
            }
        }
    }

    fun getStringSafe(key: String, def: String): String {
        return try {
            prefs.getString(key, def) ?: def
        } catch (_: Exception) {
            try {
                prefs.all[key]?.toString() ?: def
            } catch (_: Exception) {
                def
            }
        }
    }

    // Settings
    fun getSettings(): KeyboardSettings {
        return try {
            KeyboardSettings(
                vibrationEnabled = getBooleanSafe("vibrationEnabled", true),
                vibrationDurationMs = getIntSafe("vibrationDurationMs", 30),
                soundEnabled = getBooleanSafe("soundEnabled", true),
                soundProfile = getStringSafe("soundProfile", "ios_16"),
                soundVolume = getFloatSafe("soundVolume", 0.85f),
                keyPressTimingStyle = getStringSafe("keyPressTimingStyle", "ios_balanced"),
                typingSpeedMode = getStringSafe("typingSpeedMode", "medium"),
                typingSpeedMultiplier = getFloatSafe("typingSpeedMultiplier", 1.0f),
                keyRepeatSpeedMs = getIntSafe("keyRepeatSpeedMs", 45),
                longPressDelayMs = getIntSafe("longPressDelayMs", 340),
                autoReturnToLettersOnSend = getBooleanSafe("autoReturnToLettersOnSend", true),
                autoReturnToLettersOnShortcut = getBooleanSafe("autoReturnToLettersOnShortcut", true),
                keyPopupEnabled = getBooleanSafe("keyPopupEnabled", true),
                autoCapitalization = getBooleanSafe("autoCapitalization", true),
                doubleSpacePeriod = getBooleanSafe("doubleSpacePeriod", true),
                suggestionsEnabled = getBooleanSafe("suggestionsEnabled", true),
                autoCorrection = getBooleanSafe("autoCorrection", false),
                numberRowEnabled = getBooleanSafe("numberRowEnabled", false),
                topQuickEmojiRowEnabled = getBooleanSafe("topQuickEmojiRowEnabled", true),
                keyHeightFactor = getFloatSafe("keyHeightFactor", 1.0f),
                keyFontSizeFactor = getFloatSafe("keyFontSizeFactor", 1.0f),
                keyButtonScale = getFloatSafe("keyButtonScale", 1.0f),
                backspaceKeyScale = getFloatSafe("backspaceKeyScale", 1.15f),
                currentThemeId = getStringSafe("currentThemeId", "cyber_pro"),
                defaultLanguage = getStringSafe("defaultLanguage", "ar"),
                swipeSpaceSwitchLanguage = getBooleanSafe("swipeSpaceSwitchLanguage", true),
                enterLongPressTranslateEnabled = getBooleanSafe("enterLongPressTranslateEnabled", true),
                emojiBarHeightFactor = getFloatSafe("emojiBarHeightFactor", 1.0f),
                emojiButtonSizeFactor = getFloatSafe("emojiButtonSizeFactor", 1.0f),
                emojiSizeScale = getFloatSafe("emojiSizeScale", 1.0f),
                showDualHints = getBooleanSafe("showDualHints", true),
                clipboardCloseOnPaste = getBooleanSafe("clipboardCloseOnPaste", true),
                clipboardSaveForever = getBooleanSafe("clipboardSaveForever", true),
                swapClipboardAndEmoji = getBooleanSafe("swapClipboardAndEmoji", true),
                enterKeyOnLeft = getBooleanSafe("enterKeyOnLeft", false),
                isNightModeEnabled = getBooleanSafe("isNightModeEnabled", true),
                autoReturnAfterEmojiInsert = getBooleanSafe("autoReturnAfterEmojiInsert", false),
                activeDecorationStyle = getStringSafe("activeDecorationStyle", "none"),
                keyboardLayoutStyle = getStringSafe("keyboardLayoutStyle", "10_columns"),
                arabicColumnsCount = getIntSafe("arabicColumnsCount", 10),
                customArabicRow1 = getStringSafe("customArabicRow1", ""),
                customArabicRow2 = getStringSafe("customArabicRow2", ""),
                customArabicRow3 = getStringSafe("customArabicRow3", ""),
                forceEnglishNumbers = getBooleanSafe("forceEnglishNumbers", true),
                translationSource = getStringSafe("translationSource", "ar"),
                translationTarget = getStringSafe("translationTarget", "en"),
                autoTranslateOnCopy = getBooleanSafe("autoTranslateOnCopy", false),
                visibleToolbarTools = getVisibleToolbarTools()
            )
        } catch (e: Exception) {
            KeyboardSettings()
        }
    }

    fun getVisibleToolbarTools(): List<String> {
        val raw = getStringSafe("visibleToolbarTools", "")
        return if (raw.isBlank()) {
            listOf("stickers", "translate", "clipboard", "english_numbers", "decoration", "phrases", "calculator", "emoji", "voice", "ai", "photos", "gif", "settings")
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
            .putFloat("emojiBarHeightFactor", settings.emojiBarHeightFactor)
            .putFloat("emojiButtonSizeFactor", settings.emojiButtonSizeFactor)
            .putFloat("emojiSizeScale", settings.emojiSizeScale)
            .putBoolean("showDualHints", settings.showDualHints)
            .putBoolean("clipboardCloseOnPaste", settings.clipboardCloseOnPaste)
            .putBoolean("clipboardSaveForever", settings.clipboardSaveForever)
            .putBoolean("swapClipboardAndEmoji", settings.swapClipboardAndEmoji)
            .putBoolean("enterKeyOnLeft", settings.enterKeyOnLeft)
            .putBoolean("isNightModeEnabled", settings.isNightModeEnabled)
            .putString("activeDecorationStyle", settings.activeDecorationStyle)
            .putString("keyboardLayoutStyle", settings.keyboardLayoutStyle)
            .putInt("arabicColumnsCount", settings.arabicColumnsCount)
            .putString("customArabicRow1", settings.customArabicRow1)
            .putString("customArabicRow2", settings.customArabicRow2)
            .putString("customArabicRow3", settings.customArabicRow3)
            .putBoolean("forceEnglishNumbers", settings.forceEnglishNumbers)
            .putString("translationSource", settings.translationSource)
            .putString("translationTarget", settings.translationTarget)
            .putBoolean("autoTranslateOnCopy", settings.autoTranslateOnCopy)
            .apply()
    }

    // Custom stickers persistence
    fun getCustomStickers(): List<String> {
        val raw = getStringSafe("custom_stickers_list", "")
        return if (raw.isBlank()) {
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
        return getStringSafe("activeDecorationStyle", "none")
    }

    // Scroll position in Clipboard (Index and exact pixel Offset)
    fun getLastClipboardScrollIndex(): Int {
        return getIntSafe("last_clipboard_scroll_idx", 0)
    }

    fun setLastClipboardScrollIndex(idx: Int) {
        prefs.edit().putInt("last_clipboard_scroll_idx", idx).apply()
    }

    fun getLastClipboardScrollOffset(): Int {
        return getIntSafe("last_clipboard_scroll_offset", 0)
    }

    fun setLastClipboardScrollOffset(offset: Int) {
        prefs.edit().putInt("last_clipboard_scroll_offset", offset).apply()
    }

    // Custom Themes storage
    fun getCustomThemes(): List<KeyboardTheme> {
        val json = getStringSafe("custom_themes_list", "[]")
        val list = mutableListOf<KeyboardTheme>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    KeyboardTheme(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        nameAr = obj.optString("nameAr", "ثيم مخصص"),
                        nameEn = obj.optString("nameEn", "Custom Theme"),
                        isCustom = true,
                        backgroundColor = obj.optLong("backgroundColor", 0xFF0D1117L),
                        keyBackgroundColor = obj.optLong("keyBackgroundColor", 0xFF161B22L),
                        keyPressedColor = obj.optLong("keyPressedColor", 0xFF21262DL),
                        keyTextColor = obj.optLong("keyTextColor", 0xFFF0F6FCL),
                        subtextColor = obj.optLong("subtextColor", 0xFF8B949EL),
                        accentColor = obj.optLong("accentColor", 0xFF00E5FFL),
                        enterButtonColor = obj.optLong("enterButtonColor", 0xFF00E5FFL),
                        toolbarColor = obj.optLong("toolbarColor", 0xFF0D1117L),
                        borderColor = obj.optLong("borderColor", 0xFF30363DL),
                        cornerRadius = obj.optDouble("cornerRadius", 8.0).toFloat(),
                        borderAlpha = obj.optDouble("borderAlpha", 0.5).toFloat(),
                        backgroundImageUri = if (obj.has("backgroundImageUri") && !obj.isNull("backgroundImageUri")) obj.optString("backgroundImageUri") else null,
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
        if (getStringSafe("currentThemeId", "") == themeId) {
            setCurrentTheme("cyber_pro")
        }
    }

    fun getActiveTheme(): KeyboardTheme {
        return try {
            val id = getStringSafe("currentThemeId", "cyber_pro")
            getCustomThemes().find { it.id == id } ?: ThemePresets.getById(id)
        } catch (_: Exception) {
            ThemePresets.CYBER_PRO
        }
    }

    fun saveActiveTheme(theme: KeyboardTheme) {
        setCurrentTheme(theme.id)
    }

    @Volatile
    private var cachedClipboardItems: List<ClipboardItem>? = null

    // Permanent Clipboard storage ("حفظ النصوص للأبد") with reliable multi-process synchronization
    @Synchronized
    fun getClipboardItems(forceRefresh: Boolean = false): List<ClipboardItem> {
        if (!forceRefresh) {
            val cached = cachedClipboardItems
            if (cached != null) return cached
        }

        val json = getStringSafe("clipboard_history", "")
        if (json.isBlank()) {
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
                val txt = obj.optString("text", "")
                if (txt.isNotEmpty()) {
                    list.add(
                        ClipboardItem(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()).ifBlank { java.util.UUID.randomUUID().toString() },
                            text = txt,
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            isPinned = obj.optBoolean("isPinned", false)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedClipboardItems = list
        return list
    }

    @Synchronized
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
        prefs.edit().putString("clipboard_history", arr.toString()).commit()
    }

    private fun getDeletedSystemClips(): MutableSet<String> {
        return try {
            val set = prefs.getStringSet("recently_deleted_system_clips", null) ?: emptySet()
            set.toMutableSet()
        } catch (_: Exception) {
            try {
                val raw = prefs.all["recently_deleted_system_clips"]
                val repaired = when (raw) {
                    is Set<*> -> raw.filterIsInstance<String>().toSet()
                    is String -> {
                        try {
                            val a = JSONArray(raw)
                            val s = mutableSetOf<String>()
                            for (i in 0 until a.length()) s.add(a.getString(i))
                            s
                        } catch (_: Exception) {
                            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                        }
                    }
                    else -> emptySet()
                }
                prefs.edit().remove("recently_deleted_system_clips").putStringSet("recently_deleted_system_clips", repaired).apply()
                repaired.toMutableSet()
            } catch (_: Exception) {
                mutableSetOf()
            }
        }
    }

    private fun addDeletedSystemClip(text: String) {
        val set = getDeletedSystemClips()
        set.add(text.trim())
        val trimmedSet = if (set.size > 200) set.toList().takeLast(200).toSet() else set
        prefs.edit().putStringSet("recently_deleted_system_clips", trimmedSet).apply()
    }

    private fun isDeletedSystemClip(text: String): Boolean {
        return getDeletedSystemClips().contains(text.trim())
    }

    fun unblacklistSystemClip(text: String) {
        val set = getDeletedSystemClips()
        if (set.remove(text.trim())) {
            prefs.edit().putStringSet("recently_deleted_system_clips", set).apply()
        }
    }

    private fun clearFromSystemClipboardIfMatches(text: String) {
        try {
            val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager ?: return
            if (clipManager.hasPrimaryClip()) {
                val clip = clipManager.primaryClip
                val clipText = clip?.getItemAt(0)?.text?.toString()?.trim()
                if (clipText == text.trim()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        clipManager.clearPrimaryClip()
                    } else {
                        clipManager.setPrimaryClip(ClipData.newPlainText("", ""))
                    }
                }
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    fun addClipboardItem(text: String, isPinned: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        unblacklistSystemClip(trimmed)
        val current = getClipboardItems(forceRefresh = true).toMutableList()
        val existingIndex = current.indexOfFirst { it.text == trimmed }
        val finalPinned = if (existingIndex >= 0) (current[existingIndex].isPinned || isPinned) else isPinned
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        }
        current.add(0, ClipboardItem(text = trimmed, isPinned = finalPinned, timestamp = System.currentTimeMillis()))
        // Stored forever without small limit - up to 50000 items
        if (current.size > 50000) {
            saveClipboardItems(current.take(50000))
        } else {
            saveClipboardItems(current)
        }
    }

    @Synchronized
    fun syncWithSystemClipboard(context: Context) {
        try {
            val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager ?: return
            if (!clipManager.hasPrimaryClip()) return
            val clip = clipManager.primaryClip ?: return
            for (i in 0 until clip.itemCount) {
                val item = clip.getItemAt(i)
                val text = item?.text?.toString() ?: item?.coerceToText(context)?.toString()
                if (!text.isNullOrBlank()) {
                    val trimmed = text.trim()
                    if (!isDeletedSystemClip(trimmed)) {
                        addClipboardItem(trimmed)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    @Synchronized
    fun togglePinClipboard(id: String) {
        val current = getClipboardItems(forceRefresh = true).map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveClipboardItems(current)
    }

    @Synchronized
    fun deleteClipboardItem(id: String, textToDelete: String? = null) {
        val current = getClipboardItems(forceRefresh = true).toMutableList()
        val target = current.firstOrNull { 
            it.id == id || (textToDelete != null && it.text.trim() == textToDelete.trim())
        }
        val textToBlacklist = textToDelete?.trim() ?: target?.text?.trim()

        current.removeAll { 
            it.id == id || (textToBlacklist != null && it.text.trim() == textToBlacklist)
        }
        saveClipboardItems(current)

        if (!textToBlacklist.isNullOrBlank()) {
            addDeletedSystemClip(textToBlacklist)
            clearFromSystemClipboardIfMatches(textToBlacklist)
        }
    }

    @Synchronized
    fun clearClipboardHistory() {
        val all = getClipboardItems(forceRefresh = true)
        val unpinned = all.filterNot { it.isPinned }
        for (item in unpinned) {
            val t = item.text.trim()
            addDeletedSystemClip(t)
            clearFromSystemClipboardIfMatches(t)
        }
        val current = all.filter { it.isPinned }
        saveClipboardItems(current)
    }

    // Text Shortcuts (الاختصارات)
    fun getShortcuts(): List<TextShortcut> {
        val json = getStringSafe("text_shortcuts_list", "")
        if (json.isBlank()) {
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
                val tr = obj.optString("trigger", "").trim()
                val exp = obj.optString("expansion", "").trim()
                if (tr.isNotEmpty() && exp.isNotEmpty()) {
                    list.add(
                        TextShortcut(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            trigger = tr,
                            expansion = exp
                        )
                    )
                }
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
        val saved = getStringSafe("recent_emojis", "😊,❤️,😂,👍,🔥,✨,🎉,👋,🤲,🌙,👑,💋")
        return saved.split(",").filter { it.isNotBlank() }
    }

    fun addRecentEmoji(emoji: String) {
        val current = getRecentEmojis().toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        prefs.edit().putString("recent_emojis", current.take(32).joinToString(",")).apply()
    }

    // Full App Backup & Restore (النسخ الاحتياطي والاستعادة لبيانات واعدادات التطبيق بالكامل)
    fun exportBackupJson(): String {
        val root = JSONObject().apply {
            put("app_id", "com.example.keyboard")
            put("app_name", "كيبورد برو")
            put("backup_version", 2)
            put("timestamp", System.currentTimeMillis())

            // 1. All settings & preferences
            val allPrefsObj = JSONObject()
            prefs.all.forEach { (k, v) ->
                if (v is Set<*>) {
                    val arr = JSONArray()
                    v.forEach { item -> if (item != null) arr.put(item.toString()) }
                    allPrefsObj.put(k, arr)
                } else if (v != null) {
                    allPrefsObj.put(k, v)
                }
            }
            put("all_preferences", allPrefsObj)

            // 2. User personal dictionary
            try {
                val dictPrefs = context.getSharedPreferences("turbo_user_dictionary", Context.MODE_PRIVATE)
                val dictObj = JSONObject()
                dictPrefs.all.forEach { (k, v) ->
                    if (v is Set<*>) {
                        val arr = JSONArray()
                        v.forEach { item -> if (item != null) arr.put(item.toString()) }
                        dictObj.put(k, arr)
                    } else if (v != null) {
                        dictObj.put(k, v)
                    }
                }
                put("user_dictionary", dictObj)
            } catch (_: Exception) {}

            // 3. Recent emojis
            try {
                val emojiPrefs = context.getSharedPreferences("turbo_recent_emojis_prefs", Context.MODE_PRIVATE)
                val emojiObj = JSONObject()
                emojiPrefs.all.forEach { (k, v) -> if (v != null) emojiObj.put(k, v) }
                put("recent_emojis", emojiObj)
            } catch (_: Exception) {}
        }
        return root.toString(2)
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            var cleaned = jsonString.trim()
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.removePrefix("```json").removePrefix("```").trim()
                if (cleaned.endsWith("```")) {
                    cleaned = cleaned.removeSuffix("```").trim()
                }
            }
            val startIdx = cleaned.indexOf('{')
            val endIdx = cleaned.lastIndexOf('}')
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                cleaned = cleaned.substring(startIdx, endIdx + 1)
            }

            val root = JSONObject(cleaned)
            val editor = prefs.edit()

            if (root.has("all_preferences")) {
                val allPrefs = root.getJSONObject("all_preferences")
                val keys = allPrefs.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = allPrefs.get(key)
                    editor.remove(key)

                    if (key == "recently_deleted_system_clips") {
                        val s = mutableSetOf<String>()
                        when (value) {
                            is JSONArray -> for (idx in 0 until value.length()) s.add(value.getString(idx))
                            is String -> {
                                try {
                                    val a = JSONArray(value)
                                    for (idx in 0 until a.length()) s.add(a.getString(idx))
                                } catch (_: Exception) {
                                    s.addAll(value.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                }
                            }
                        }
                        editor.putStringSet(key, s)
                    } else if (key in FLOAT_KEYS) {
                        val f = when (value) {
                            is Number -> value.toFloat()
                            is String -> value.toFloatOrNull() ?: 1.0f
                            else -> 1.0f
                        }
                        editor.putFloat(key, f)
                    } else if (key in INT_KEYS || key == "last_clipboard_scroll_idx" || key == "last_clipboard_scroll_offset") {
                        val i = when (value) {
                            is Number -> value.toInt()
                            is String -> value.toIntOrNull() ?: 0
                            else -> 0
                        }
                        editor.putInt(key, i)
                    } else if (key in BOOLEAN_KEYS) {
                        val b = when (value) {
                            is Boolean -> value
                            is Number -> value.toInt() != 0
                            is String -> value.equals("true", ignoreCase = true) || value == "1"
                            else -> true
                        }
                        editor.putBoolean(key, b)
                    } else if (key in STRING_KEYS) {
                        val str = when (value) {
                            is JSONArray -> value.toString()
                            is JSONObject -> value.toString()
                            null -> ""
                            else -> value.toString()
                        }
                        editor.putString(key, str)
                    } else {
                        when (value) {
                            is Boolean -> editor.putBoolean(key, value)
                            is Int -> editor.putInt(key, value)
                            is Long -> editor.putLong(key, value)
                            is Double -> editor.putFloat(key, value.toFloat())
                            is Float -> editor.putFloat(key, value)
                            is String -> editor.putString(key, value)
                            is JSONArray -> editor.putString(key, value.toString())
                            is JSONObject -> editor.putString(key, value.toString())
                            else -> editor.putString(key, value.toString())
                        }
                    }
                }
            } else {
                // Fallback for v1 backups
                if (root.has("clipboard")) {
                    editor.remove("clipboard_history")
                    editor.putString("clipboard_history", root.getJSONArray("clipboard").toString())
                }
                if (root.has("shortcuts")) {
                    editor.remove("text_shortcuts_list")
                    editor.putString("text_shortcuts_list", root.getJSONArray("shortcuts").toString())
                }
            }
            editor.commit()
            sanitizePrefs()
            cachedClipboardItems = null

            // Restore user dictionary
            if (root.has("user_dictionary")) {
                try {
                    val dictPrefs = context.getSharedPreferences("turbo_user_dictionary", Context.MODE_PRIVATE)
                    val dictEditor = dictPrefs.edit()
                    val dictObj = root.getJSONObject("user_dictionary")
                    val keys = dictObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        dictEditor.remove(k)
                        if (k == "custom_words") {
                            val raw = dictObj.get(k)
                            val wordsSet = mutableSetOf<String>()
                            when (raw) {
                                is JSONArray -> for (idx in 0 until raw.length()) wordsSet.add(raw.getString(idx))
                                is String -> {
                                    try {
                                        val a = JSONArray(raw)
                                        for (idx in 0 until a.length()) wordsSet.add(a.getString(idx))
                                    } catch (_: Exception) {
                                        wordsSet.addAll(raw.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                                    }
                                }
                            }
                            dictEditor.putStringSet("custom_words", wordsSet)
                        } else {
                            dictEditor.putString(k, dictObj.optString(k, ""))
                        }
                    }
                    dictEditor.commit()
                } catch (_: Exception) {}
            }

            // Restore recent emojis
            if (root.has("recent_emojis")) {
                try {
                    val emojiPrefs = context.getSharedPreferences("turbo_recent_emojis_prefs", Context.MODE_PRIVATE)
                    val emEditor = emojiPrefs.edit()
                    val emObj = root.getJSONObject("recent_emojis")
                    val keys = emObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        emEditor.remove(k)
                        emEditor.putString(k, emObj.optString(k, ""))
                    }
                    emEditor.commit()
                } catch (_: Exception) {}
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    fun getCustomTopEmojis(): List<String> {
        val raw = getStringSafe("custom_top_emojis_list", "")
        if (raw.isNotBlank()) {
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val s = arr.getString(i).trim()
                    if (s.isNotEmpty()) list.add(s)
                }
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {}
        }
        return listOf("👑", "💋", "😂", "❤️", "🔥", "🥺", "✨", "🤍", "😍", "👍")
    }

    fun saveCustomTopEmojis(list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs.edit().putString("custom_top_emojis_list", arr.toString()).apply()
    }

    fun getEmojiSizeScale(): Float {
        return getFloatSafe("emojiSizeScale", 1.0f)
    }

    fun saveEmojiSizeScale(scale: Float) {
        prefs.edit().putFloat("emojiSizeScale", scale).apply()
    }
}

