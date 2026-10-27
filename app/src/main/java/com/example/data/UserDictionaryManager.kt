package com.example.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UserDictionaryManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("turbo_user_dictionary", Context.MODE_PRIVATE)
    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var cachedWords: List<String>? = null
    @Volatile
    private var cachedBigrams: MutableMap<String, MutableMap<String, Int>>? = null
    @Volatile
    private var cachedFrequencies: MutableMap<String, Int>? = null

    private fun loadFrequencies(): MutableMap<String, Int> {
        cachedFrequencies?.let { return it }
        val map = mutableMapOf<String, Int>()
        try {
            val json = prefs.getString("word_frequencies", "") ?: ""
            if (json.isNotBlank()) {
                val obj = org.json.JSONObject(json)
                obj.keys().forEach { k ->
                    map[k] = obj.optInt(k, 1)
                }
            }
        } catch (_: Exception) {}
        cachedFrequencies = map
        return map
    }

    private fun loadBigrams(): MutableMap<String, MutableMap<String, Int>> {
        cachedBigrams?.let { return it }
        val map = mutableMapOf<String, MutableMap<String, Int>>()
        try {
            val json = prefs.getString("ngram_bigrams", "") ?: ""
            if (json.isNotBlank()) {
                val root = org.json.JSONObject(json)
                root.keys().forEach { prev ->
                    val inner = root.optJSONObject(prev)
                    if (inner != null) {
                        val subMap = mutableMapOf<String, Int>()
                        inner.keys().forEach { next ->
                            subMap[next] = inner.optInt(next, 1)
                        }
                        map[prev] = subMap
                    }
                }
            }
        } catch (_: Exception) {}
        cachedBigrams = map
        return map
    }

    fun recordWord(word: String, prevWord: String? = null) {
        val clean = word.trim().replace(Regex("[،؛؟?!.,:;]+$"), "")
        if (clean.length < 2) return

        // 1. Add to user custom words
        addWord(clean)

        // 2. Increment frequency in memory
        val freqs = loadFrequencies()
        freqs[clean] = (freqs[clean] ?: 0) + 1

        // 3. Increment bigram in memory if prevWord is available
        val cleanPrev = prevWord?.trim()?.replace(Regex("[،؛؟?!.,:;]+$"), "")
        if (!cleanPrev.isNullOrBlank() && cleanPrev.length >= 2) {
            val bigrams = loadBigrams()
            val sub = bigrams.getOrPut(cleanPrev) { mutableMapOf() }
            sub[clean] = (sub[clean] ?: 0) + 1
        }

        // Persist on background IO thread to prevent UI micro-stutters
        ioScope.launch {
            try {
                val freqObj = org.json.JSONObject()
                freqs.entries.sortedByDescending { it.value }.take(300).forEach { (k, v) ->
                    freqObj.put(k, v)
                }

                val bigramObj = org.json.JSONObject()
                loadBigrams().entries.take(150).forEach { (prev, subMap) ->
                    val inner = org.json.JSONObject()
                    subMap.entries.sortedByDescending { it.value }.take(8).forEach { (next, cnt) ->
                        inner.put(next, cnt)
                    }
                    bigramObj.put(prev, inner)
                }

                prefs.edit()
                    .putString("word_frequencies", freqObj.toString())
                    .putString("ngram_bigrams", bigramObj.toString())
                    .apply()
            } catch (_: Exception) {}
        }
    }

    fun getLearnedNextWords(prevWord: String): List<String> {
        val cleanPrev = prevWord.trim().replace(Regex("[،؛؟?!.,:;]+$"), "")
        if (cleanPrev.isBlank()) return emptyList()
        val bigrams = loadBigrams()
        val sub = bigrams[cleanPrev] ?: return emptyList()
        return sub.entries.sortedByDescending { it.value }.map { it.key }
    }

    fun getWordsByFrequency(): List<String> {
        val freqs = loadFrequencies()
        val allWords = getUserWords().toSet()
        return allWords.sortedByDescending { freqs[it] ?: 0 }
    }

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
            val added = current.add(trimmed)
            if (added) {
                val updated = current.toList().sorted()
                cachedWords = updated
                prefs.edit().putStringSet("custom_words", current).apply()
            }
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
        cachedBigrams = mutableMapOf()
        cachedFrequencies = mutableMapOf()
        try {
            prefs.edit()
                .remove("custom_words")
                .remove("word_frequencies")
                .remove("ngram_bigrams")
                .apply()
        } catch (_: Exception) {}
    }
}

