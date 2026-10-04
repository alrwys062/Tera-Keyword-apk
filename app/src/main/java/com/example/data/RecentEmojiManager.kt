package com.example.data

import android.content.Context
import android.content.SharedPreferences

class RecentEmojiManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_recent_emojis_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_RECENT_EMOJIS = "recent_emojis_list"
        private const val MAX_RECENT_COUNT = 35

        val DEFAULT_EMOJIS = listOf(
            "😊", "😂", "❤️", "🔥", "👍", "✨", "👑", "🌹", "🤲", "😍",
            "🎉", "💯", "🤍", "🥺", "🥰", "👌", "👏", "🤩", "😎", "🙌",
            "🌸", "💫", "🕊️", "🌙", "⭐", "💋", "🤝", "⚡", "💐", "☕"
        )
    }

    fun getRecentEmojis(): List<String> {
        val raw = prefs.getString(KEY_RECENT_EMOJIS, null)
        return if (!raw.isNullOrBlank()) {
            raw.split(",").filter { it.isNotBlank() }
        } else {
            DEFAULT_EMOJIS
        }
    }

    fun addEmoji(emoji: String) {
        if (emoji.isBlank()) return
        val current = getRecentEmojis().toMutableList()
        current.remove(emoji)
        current.add(0, emoji)
        val updated = current.take(MAX_RECENT_COUNT)
        prefs.edit().putString(KEY_RECENT_EMOJIS, updated.joinToString(",")).apply()
    }

    fun clearRecent() {
        prefs.edit().remove(KEY_RECENT_EMOJIS).apply()
    }
}
