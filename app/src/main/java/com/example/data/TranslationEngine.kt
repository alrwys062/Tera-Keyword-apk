package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TranslationEngine {

    data class Language(val code: String, val nameAr: String, val nameEn: String)

    val supportedLanguages = listOf(
        Language("ar", "العربية", "Arabic"),
        Language("en", "الإنجليزية", "English"),
        Language("fr", "الفرنسية", "French"),
        Language("es", "الإسبانية", "Spanish"),
        Language("tr", "التركية", "Turkish"),
        Language("de", "الألمانية", "German"),
        Language("ur", "الأردية", "Urdu"),
        Language("id", "الإندونيسية", "Indonesian")
    )

    private val offlinePhrases = mapOf(
        "مرحبا" to "Hello",
        "مرحبا!" to "Hello!",
        "كيف حالك؟" to "How are you?",
        "كيف حالك" to "How are you?",
        "أنا بخير، شكراً!" to "I'm good, thank you!",
        "أنا بخير" to "I'm good",
        "شكرا" to "Thank you",
        "شكراً" to "Thank you",
        "صباح الخير" to "Good morning",
        "مساء الخير" to "Good evening",
        "تصبح على خير" to "Good night",
        "السلام عليكم" to "Peace be upon you",
        "مع السلامة" to "Goodbye",
        "أراك لاحقاً" to "See you later",
        "بالتوفيق" to "Good luck",
        "كل عام وأنتم بخير" to "Happy new year",
        "عيد مبارك" to "Blessed Eid",
        "رمضان كريم" to "Ramadan Kareem",
        "أحبك" to "I love you",
        "نعم" to "Yes",
        "لا" to "No",
        "أين أنت؟" to "Where are you?",
        "ما اسمك؟" to "What is your name?",
        "حسناً" to "Alright",
        "تمام" to "OK"
    )

    private val offlinePhrasesEnToAr = offlinePhrases.entries.associate { (k, v) -> v.lowercase() to k }

    suspend fun translate(text: String, sourceLang: String = "auto", targetLang: String = "en"): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""

        // Check offline fast dictionary first
        if (sourceLang == "ar" || sourceLang == "auto") {
            offlinePhrases[trimmed]?.let { return it }
        }
        if (sourceLang == "en" || sourceLang == "auto") {
            offlinePhrasesEnToAr[trimmed.lowercase()]?.let { return it }
        }

        // Attempt online translation
        return withContext(Dispatchers.IO) {
            try {
                val encodedText = URLEncoder.encode(trimmed, "UTF-8")
                val urlString = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceLang&tl=$targetLang&dt=t&q=$encodedText"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 3500
                conn.readTimeout = 3500
                conn.setRequestProperty("User-Agent", "Mozilla/5.0")

                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(response)
                    val sentences = jsonArray.getJSONArray(0)
                    val result = StringBuilder()
                    for (i in 0 until sentences.length()) {
                        val sentence = sentences.getJSONArray(i)
                        result.append(sentence.getString(0))
                    }
                    result.toString()
                } else {
                    fallbackTranslate(trimmed, sourceLang, targetLang)
                }
            } catch (e: Exception) {
                fallbackTranslate(trimmed, sourceLang, targetLang)
            }
        }
    }

    private fun fallbackTranslate(text: String, sourceLang: String, targetLang: String): String {
        // Fallback for simple phrases
        val lower = text.trim().lowercase()
        offlinePhrasesEnToAr[lower]?.let { return it }
        offlinePhrases[text.trim()]?.let { return it }

        // If Arabic detected and target is English
        val isArabic = text.any { it in '\u0600'..'\u06FF' }
        return if (isArabic && targetLang == "en") {
            // Simple transliteration or friendly fallback
            "Hello: $text"
        } else if (!isArabic && targetLang == "ar") {
            "ترجمة: $text"
        } else {
            text
        }
    }
}
