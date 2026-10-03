package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TranslationEngine {

    data class Language(
        val code: String,
        val nameAr: String,
        val nameEn: String,
        val flag: String = "🌐"
    )

    // Complete list of world languages for instant translation
    val supportedLanguages = listOf(
        Language("ar", "العربية", "Arabic", "🇸🇦"),
        Language("en", "الإنجليزية", "English", "🇺🇸"),
        Language("fr", "الفرنسية", "French", "🇫🇷"),
        Language("es", "الإسبانية", "Spanish", "🇪🇸"),
        Language("tr", "التركية", "Turkish", "🇹🇷"),
        Language("de", "الألمانية", "German", "🇩🇪"),
        Language("ru", "الروسية", "Russian", "🇷🇺"),
        Language("fa", "الفارسية", "Persian", "🇮🇷"),
        Language("ur", "الأردية", "Urdu", "🇵🇰"),
        Language("it", "الإيطالية", "Italian", "🇮🇹"),
        Language("pt", "البرتغالية", "Portuguese", "🇧🇷"),
        Language("id", "الإندونيسية", "Indonesian", "🇮🇩"),
        Language("hi", "الهندية", "Hindi", "🇮🇳"),
        Language("zh-CN", "الصينية (المبسطة)", "Chinese (Simplified)", "🇨🇳"),
        Language("ja", "اليابانية", "Japanese", "🇯🇵"),
        Language("ko", "الكورية", "Korean", "🇰🇷"),
        Language("bn", "البنغالية", "Bengali", "🇧🇩"),
        Language("ku", "الكردية", "Kurdish", "☀️"),
        Language("ps", "البشتوية", "Pashto", "🇦🇫"),
        Language("he", "العبرية", "Hebrew", "🇮🇱"),
        Language("el", "اليونانية", "Greek", "🇬🇷"),
        Language("sv", "السويدية", "Swedish", "🇸🇪"),
        Language("nl", "الهولندية", "Dutch", "🇳🇱"),
        Language("pl", "البولندية", "Polish", "🇵🇱"),
        Language("uk", "الأوكرانية", "Ukrainian", "🇺🇦"),
        Language("vi", "الفيتنامية", "Vietnamese", "🇻🇳"),
        Language("th", "التايلاندية", "Thai", "🇹🇭"),
        Language("tl", "الفلبينية (تاغالوغ)", "Filipino (Tagalog)", "🇵🇭"),
        Language("sw", "السواحيلية", "Swahili", "🇰🇪"),
        Language("ro", "الرومانية", "Romanian", "🇷🇴"),
        Language("cs", "التشيكية", "Czech", "🇨🇿"),
        Language("hu", "المجرية", "Hungarian", "🇭🇺"),
        Language("ms", "الماليزية", "Malay", "🇲🇾"),
        Language("da", "الدانماركية", "Danish", "🇩🇰"),
        Language("fi", "الفنلندية", "Finnish", "🇫🇮"),
        Language("no", "النرويجية", "Norwegian", "🇳🇴"),
        Language("so", "الصومالية", "Somali", "🇸🇴"),
        Language("am", "الأمهرية", "Amharic", "🇪🇹"),
        Language("ta", "التاميلية", "Tamil", "🇮🇳"),
        Language("te", "التيلجو", "Telugu", "🇮🇳"),
        Language("mr", "الماراثية", "Marathi", "🇮🇳"),
        Language("pa", "البنجابية", "Punjabi", "🇮🇳"),
        Language("gu", "الغوجاراتية", "Gujarati", "🇮🇳"),
        Language("ml", "الماليالامية", "Malayalam", "🇮🇳"),
        Language("kn", "الكانادا", "Kannada", "🇮🇳"),
        Language("ne", "النيبالية", "Nepali", "🇳🇵"),
        Language("si", "السنهالية", "Sinhala", "🇱🇰"),
        Language("my", "البورمية", "Burmese", "🇲🇲"),
        Language("km", "الخميرية", "Khmer", "🇰🇭"),
        Language("lo", "اللاوية", "Lao", "🇱🇦"),
        Language("ka", "الجورجية", "Georgian", "🇬🇪"),
        Language("hy", "الأرمنية", "Armenian", "🇦🇲"),
        Language("az", "الأذربيجانية", "Azerbaijani", "🇦🇿"),
        Language("kk", "الكازاخستانية", "Kazakh", "🇰🇿"),
        Language("uz", "الأوزبكية", "Uzbek", "🇺🇿"),
        Language("bg", "البلغارية", "Bulgarian", "🇧🇬"),
        Language("sr", "الصربية", "Serbian", "🇷🇸"),
        Language("hr", "الكرواتية", "Croatian", "🇭🇷"),
        Language("sk", "السلوفاكية", "Slovak", "🇸🇰"),
        Language("lt", "الليتوانية", "Lithuanian", "🇱🇹"),
        Language("lv", "اللاتفية", "Latvian", "🇱🇻"),
        Language("et", "الإستونية", "Estonian", "🇪🇪"),
        Language("sq", "الألبانية", "Albanian", "🇦🇱"),
        Language("bs", "البوسنية", "Bosnian", "🇧🇦"),
        Language("mk", "المقدونية", "Macedonian", "🇲🇰"),
        Language("sl", "السلوفينية", "Slovenian", "🇸🇮"),
        Language("is", "الأيسلندية", "Icelandic", "🇮🇸"),
        Language("ga", "الأيرلندية", "Irish", "🇮🇪"),
        Language("cy", "الويلزية", "Welsh", "🇬🇧"),
        Language("eu", "الباسكية", "Basque", "🇪🇸"),
        Language("ca", "الكتالونية", "Catalan", "🇪🇸"),
        Language("gl", "الجاليكية", "Galician", "🇪🇸"),
        Language("mt", "المالطية", "Maltese", "🇲🇹"),
        Language("eo", "الإسبرانتو", "Esperanto", "🌐"),
        Language("la", "اللاتينية", "Latin", "🏛️"),
        Language("zu", "الزولو", "Zulu", "🇿🇦"),
        Language("af", "الأفريقانية", "Afrikaans", "🇿🇦"),
        Language("ha", "الهوسا", "Hausa", "🇳🇬"),
        Language("yo", "اليوروبا", "Yoruba", "🇳🇬"),
        Language("ig", "الإيغبو", "Igbo", "🇳🇬"),
        Language("mn", "المنغولية", "Mongolian", "🇲🇳"),
        Language("tt", "التترية", "Tatar", "🇷🇺"),
        Language("tg", "الطاجيكية", "Tajik", "🇹🇯"),
        Language("tk", "التركمانية", "Turkmen", "🇹🇲"),
        Language("ky", "القيرغيزية", "Kyrgyz", "🇰🇬"),
        Language("sd", "السندية", "Sindhi", "🇵🇰"),
        Language("ug", "الأويغورية", "Uyghur", "🇨🇳")
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

        // Check offline fast dictionary first if translating between Arabic and English
        if ((sourceLang == "ar" || sourceLang == "auto") && targetLang == "en") {
            offlinePhrases[trimmed]?.let { return it }
        }
        if ((sourceLang == "en" || sourceLang == "auto") && targetLang == "ar") {
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
        val lower = text.trim().lowercase()
        offlinePhrasesEnToAr[lower]?.let { return it }
        offlinePhrases[text.trim()]?.let { return it }

        val isArabic = text.any { it in '\u0600'..'\u06FF' }
        return if (isArabic && targetLang == "en") {
            "Translation: $text"
        } else if (!isArabic && targetLang == "ar") {
            "ترجمة: $text"
        } else {
            text
        }
    }
}
