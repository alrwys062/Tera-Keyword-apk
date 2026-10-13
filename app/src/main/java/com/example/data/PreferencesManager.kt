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
            enterLongPressNewlineEnabled = prefs.getBoolean("enterLongPressNewlineEnabled", true),
            emojiSizeScale = prefs.getFloat("emojiSizeScale", 1.0f),
            showDualHints = prefs.getBoolean("showDualHints", true),
            clipboardCloseOnPaste = prefs.getBoolean("clipboardCloseOnPaste", true),
            clipboardSaveForever = prefs.getBoolean("clipboardSaveForever", true),
            swapClipboardAndEmoji = prefs.getBoolean("swapClipboardAndEmoji", true),
            enterKeyOnLeft = prefs.getBoolean("enterKeyOnLeft", false),
            isNightModeEnabled = prefs.getBoolean("isNightModeEnabled", true),
            autoReturnAfterEmojiInsert = prefs.getBoolean("autoReturnAfterEmojiInsert", false),
            activeDecorationStyle = prefs.getString("activeDecorationStyle", "none") ?: "none",
            keyboardLayoutStyle = prefs.getString("keyboardLayoutStyle", "10_columns") ?: "10_columns",
            arabicColumnsCount = prefs.getInt("arabicColumnsCount", 10),
            customArabicRow1 = prefs.getString("customArabicRow1", "") ?: "",
            customArabicRow2 = prefs.getString("customArabicRow2", "") ?: "",
            customArabicRow3 = prefs.getString("customArabicRow3", "") ?: "",
            forceEnglishNumbers = prefs.getBoolean("forceEnglishNumbers", true),
            translationSource = prefs.getString("translationSource", "ar") ?: "ar",
            translationTarget = prefs.getString("translationTarget", "en") ?: "en",
            autoTranslateOnCopy = prefs.getBoolean("autoTranslateOnCopy", false),
            visibleToolbarTools = getVisibleToolbarTools()
        )
    }

    fun getVisibleToolbarTools(): List<String> {
        val raw = prefs.getString("visibleToolbarTools", null)
        return if (raw.isNullOrBlank()) {
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
            .putBoolean("enterLongPressNewlineEnabled", settings.enterLongPressNewlineEnabled)
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

    // Permanent Clipboard storage ("حفظ النصوص للأبد") with reliable multi-process synchronization
    @Synchronized
    fun getClipboardItems(forceRefresh: Boolean = false): List<ClipboardItem> {
        if (!forceRefresh) {
            val cached = cachedClipboardItems
            if (cached != null) return cached
        }

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
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()).ifBlank { java.util.UUID.randomUUID().toString() },
                        text = obj.getString("text"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
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
        val set = prefs.getStringSet("recently_deleted_system_clips", null) ?: emptySet()
        return set.toMutableSet()
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
                if (v != null) allPrefsObj.put(k, v)
            }
            put("all_preferences", allPrefsObj)

            // 2. User personal dictionary
            try {
                val dictPrefs = context.getSharedPreferences("turbo_user_dictionary", Context.MODE_PRIVATE)
                val dictObj = JSONObject()
                dictPrefs.all.forEach { (k, v) -> if (v != null) dictObj.put(k, v) }
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
            val root = JSONObject(jsonString)
            val editor = prefs.edit()

            if (root.has("all_preferences")) {
                val allPrefs = root.getJSONObject("all_preferences")
                val keys = allPrefs.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    when (val value = allPrefs.get(key)) {
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Double -> editor.putFloat(key, value.toFloat())
                        is String -> editor.putString(key, value)
                        else -> editor.putString(key, value.toString())
                    }
                }
            } else {
                // Fallback for v1 backups
                if (root.has("clipboard")) {
                    editor.putString("clipboard_history", root.getJSONArray("clipboard").toString())
                }
                if (root.has("shortcuts")) {
                    editor.putString("text_shortcuts_list", root.getJSONArray("shortcuts").toString())
                }
            }
            editor.commit()

            // Restore user dictionary
            if (root.has("user_dictionary")) {
                try {
                    val dictPrefs = context.getSharedPreferences("turbo_user_dictionary", Context.MODE_PRIVATE)
                    val dictEditor = dictPrefs.edit()
                    val dictObj = root.getJSONObject("user_dictionary")
                    val keys = dictObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        dictEditor.putString(k, dictObj.optString(k, ""))
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
        val raw = prefs.getString("custom_top_emojis_list", null)
        if (!raw.isNullOrBlank()) {
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
        return prefs.getFloat("emojiSizeScale", 1.0f)
    }

    fun saveEmojiSizeScale(scale: Float) {
        prefs.edit().putFloat("emojiSizeScale", scale).apply()
    }
}

