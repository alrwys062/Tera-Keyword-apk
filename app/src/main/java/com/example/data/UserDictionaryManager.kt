package com.example.data

import android.content.Context
import android.content.SharedPreferences

class UserDictionaryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_user_dictionary", Context.MODE_PRIVATE)

    fun getUserWords(): List<String> {
        val set = prefs.getStringSet("custom_words", emptySet()) ?: emptySet()
        return set.toList().sorted()
    }

    fun addWord(word: String): Boolean {
        val trimmed = word.trim()
        if (trimmed.length < 2) return false
        val current = getUserWords().toMutableSet()
        current.add(trimmed)
        prefs.edit().putStringSet("custom_words", current).apply()
        return true
    }

    fun removeWord(word: String): Boolean {
        val current = getUserWords().toMutableSet()
        val removed = current.remove(word.trim())
        if (removed) {
            prefs.edit().putStringSet("custom_words", current).apply()
        }
        return removed
    }

    fun clearDictionary() {
        prefs.edit().remove("custom_words").apply()
    }
}
