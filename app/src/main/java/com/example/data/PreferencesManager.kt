package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ClipboardItem
import com.example.model.KeyboardSettings
import com.example.model.KeyboardTheme
import com.example.model.ThemePresets
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)

    // Settings
    fun getSettings(): KeyboardSettings {
        return KeyboardSettings(
            vibrationEnabled = prefs.getBoolean("vibrationEnabled", true),
            soundEnabled = prefs.getBoolean("soundEnabled", false),
            keyPopupEnabled = prefs.getBoolean("keyPopupEnabled", true),
            autoCapitalization = prefs.getBoolean("autoCapitalization", true),
            doubleSpacePeriod = prefs.getBoolean("doubleSpacePeriod", true),
            suggestionsEnabled = prefs.getBoolean("suggestionsEnabled", true),
            autoCorrection = prefs.getBoolean("autoCorrection", false),
            numberRowEnabled = prefs.getBoolean("numberRowEnabled", false),
            keyHeightFactor = prefs.getFloat("keyHeightFactor", 1.0f),
            currentThemeId = prefs.getString("currentThemeId", "cyber_pro") ?: "cyber_pro",
            defaultLanguage = prefs.getString("defaultLanguage", "ar") ?: "ar",
            swipeSpaceSwitchLanguage = prefs.getBoolean("swipeSpaceSwitchLanguage", true),
            enterLongPressTranslateEnabled = prefs.getBoolean("enterLongPressTranslateEnabled", true)
        )
    }

    fun saveSettings(settings: KeyboardSettings) {
        prefs.edit()
            .putBoolean("vibrationEnabled", settings.vibrationEnabled)
            .putBoolean("soundEnabled", settings.soundEnabled)
            .putBoolean("keyPopupEnabled", settings.keyPopupEnabled)
            .putBoolean("autoCapitalization", settings.autoCapitalization)
            .putBoolean("doubleSpacePeriod", settings.doubleSpacePeriod)
            .putBoolean("suggestionsEnabled", settings.suggestionsEnabled)
            .putBoolean("autoCorrection", settings.autoCorrection)
            .putBoolean("numberRowEnabled", settings.numberRowEnabled)
            .putFloat("keyHeightFactor", settings.keyHeightFactor)
            .putString("currentThemeId", settings.currentThemeId)
            .putString("defaultLanguage", settings.defaultLanguage)
            .putBoolean("swipeSpaceSwitchLanguage", settings.swipeSpaceSwitchLanguage)
            .putBoolean("enterLongPressTranslateEnabled", settings.enterLongPressTranslateEnabled)
            .apply()
    }

    fun setCurrentTheme(themeId: String) {
        prefs.edit().putString("currentThemeId", themeId).apply()
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
        val current = getCustomThemes().filterNot { it.id == themeId }
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

    fun getActiveTheme(): KeyboardTheme {
        val id = prefs.getString("currentThemeId", "cyber_pro") ?: "cyber_pro"
        return getCustomThemes().find { it.id == id } ?: ThemePresets.getById(id)
    }

    // Clipboard storage
    fun getClipboardItems(): List<ClipboardItem> {
        val json = prefs.getString("clipboard_history", null)
        if (json == null) {
            // Seed initial helpful pinned clips
            val initial = listOf(
                ClipboardItem(text = "مرحبا! كيف حالك اليوم؟", isPinned = true),
                ClipboardItem(text = "أنا بخير، شكراً!", isPinned = true),
                ClipboardItem(text = "السلام عليكم ورحمة الله وبركاته", isPinned = true),
                ClipboardItem(text = "Hello! How are you?", isPinned = false),
                ClipboardItem(text = "I'm good, thank you! 😊", isPinned = false)
            )
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
        return list
    }

    fun saveClipboardItems(items: List<ClipboardItem>) {
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
        // Limit to 50 clips
        if (current.size > 50) {
            saveClipboardItems(current.take(50))
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

    // Recent Emojis
    fun getRecentEmojis(): List<String> {
        val saved = prefs.getString("recent_emojis", "😊,❤️,😂,👍,🔥,✨,🎉,👋,🤲,🌙") ?: "😊,❤️,😂,👍,🔥,✨,🎉,👋,🤲,🌙"
        return saved.split(",").filter { it.isNotBlank() }
    }

    fun addRecentEmoji(emoji: String) {
        val current = getRecentEmojis().toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        prefs.edit().putString("recent_emojis", current.take(24).joinToString(",")).apply()
    }
}
