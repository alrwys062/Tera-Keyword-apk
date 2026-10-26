package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

class UserDictionaryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_user_dictionary", Context.MODE_PRIVATE)

    @Volatile
    private var cachedWords: List<String>? = null

    fun getUserWords(): List<String> {
        val cached = cachedWords
        if (cached != null) return cached

        val words = try {
            val set = prefs.getStringSet("custom_words", emptySet()) ?: emptySet()
            set.toList().sorted()
        } catch (_: Exception) {
            // Auto-heal if custom_words was stored as String or other type from bad backup restore
            try {
                val raw = prefs.all["custom_words"]
                val repaired = when (raw) {
                    is Set<*> -> raw.filterIsInstance<String>().toSet()
                    is String -> {
                        try {
                            val arr = JSONArray(raw)
                            val s = mutableSetOf<String>()
                            for (i in 0 until arr.length()) s.add(arr.getString(i))
                            s
                        } catch (_: Exception) {
                            raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                        }
                    }
                    else -> emptySet()
                }
                prefs.edit().remove("custom_words").putStringSet("custom_words", repaired).apply()
                repaired.toList().sorted()
            } catch (_: Exception) {
                emptyList()
            }
        }
        cachedWords = words
        return words
    }

    fun addWord(word: String): Boolean {
        val trimmed = word.trim()
        if (trimmed.length < 2) return false
        return try {
            val current = getUserWords().toMutableSet()
            current.add(trimmed)
            val updated = current.toList().sorted()
            cachedWords = updated
            prefs.edit().putStringSet("custom_words", current).apply()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun removeWord(word: String): Boolean {
        return try {
            val current = getUserWords().toMutableSet()
            val removed = current.remove(word.trim())
            if (removed) {
                val updated = current.toList().sorted()
                cachedWords = updated
                prefs.edit().putStringSet("custom_words", current).apply()
            }
            removed
        } catch (_: Exception) {
            false
        }
    }

    fun clearDictionary() {
        cachedWords = emptyList()
        try {
            prefs.edit().remove("custom_words").apply()
        } catch (_: Exception) {}
    }
}

