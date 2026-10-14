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
        "أهلا" to "Welcome",
        "أهلاً" to "Welcome",
        "أهلا وسهلا" to "Welcome",
        "كيف حالك؟" to "How are you?",
        "كيف حالك" to "How are you?",
        "كيفك" to "How are you?",
        "شخبارك" to "How are you?",
        "أنا بخير، شكراً!" to "I'm good, thank you!",
        "أنا بخير" to "I'm good",
        "بخير الحمدلله" to "Fine, thank God",
        "شكرا" to "Thank you",
        "شكراً" to "Thank you",
        "شكرا جزيلا" to "Thank you very much",
        "شكراً جزيلاً" to "Thank you very much",
        "عفوا" to "You're welcome",
        "عفواً" to "You're welcome",
        "صباح الخير" to "Good morning",
        "صباح النور" to "Good morning",
        "مساء الخير" to "Good evening",
        "مساء النور" to "Good evening",
        "تصبح على خير" to "Good night",
        "السلام عليكم" to "Peace be upon you",
        "وعليكم السلام" to "Peace be upon you too",
        "مع السلامة" to "Goodbye",
        "وداعا" to "Goodbye",
        "وداعاً" to "Goodbye",
        "إلى اللقاء" to "See you later",
        "أراك لاحقاً" to "See you later",
        "أراك قريبا" to "See you soon",
        "بالتوفيق" to "Good luck",
        "مبروك" to "Congratulations",
        "ألف مبروك" to "Congratulations",
        "كل عام وأنتم بخير" to "Happy new year",
        "عيد مبارك" to "Blessed Eid",
        "رمضان كريم" to "Ramadan Kareem",
        "أحبك" to "I love you",
        "نعم" to "Yes",
        "أجل" to "Yes",
        "لا" to "No",
        "كلا" to "No",
        "أين أنت؟" to "Where are you?",
        "أين أنت" to "Where are you?",
        "وينك" to "Where are you?",
        "ما اسمك؟" to "What is your name?",
        "ما اسمك" to "What is your name?",
        "شو اسمك" to "What is your name?",
        "ماذا تفعل؟" to "What are you doing?",
        "ماذا تفعل" to "What are you doing?",
        "شو عم تعمل" to "What are you doing?",
        "حسناً" to "Alright",
        "حسنا" to "Alright",
        "تمام" to "OK",
        "أوك" to "OK",
        "أوكي" to "OK",
        "ان شاء الله" to "God willing",
        "إن شاء الله" to "God willing",
        "الحمد لله" to "Praise be to God",
        "الحمدلله" to "Praise be to God",
        "ما شاء الله" to "God bless",
        "ماشالله" to "God bless",
        "أنا آسف" to "I'm sorry",
        "اعتذر" to "I apologize",
        "أعتذر" to "I apologize",
        "لو سمحت" to "Please",
        "من فضلك" to "Please",
        "الله يعطيك العافية" to "May God give you health",
        "تسلم" to "Thank you",
        "مشكور" to "Thank you"
    )

    private val offlinePhrasesEnToAr = offlinePhrases.entries.associate { (k, v) -> v.lowercase() to k }

    suspend fun translate(text: String, sourceLang: String = "auto", targetLang: String = "en"): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""

        val effectiveSource = if (sourceLang.isBlank()) "auto" else sourceLang
        val effectiveTarget = if (targetLang.isBlank()) "en" else targetLang

        // If source and target are the same, auto detect language based on script
        val finalTarget = if (effectiveSource == effectiveTarget && effectiveSource != "auto") {
            val isArabic = trimmed.any { it in '\u0600'..'\u06FF' }
            if (isArabic) "en" else "ar"
        } else {
            effectiveTarget
        }

        // 1. Check offline phrases first for instant zero-latency match
        val offlineMatch = checkOffline(trimmed, effectiveSource, finalTarget)
        if (offlineMatch != null) {
            return offlineMatch
        }

        // 2. Perform online translation in background IO
        return withContext(Dispatchers.IO) {
            // Strategy 1: Google clients5 API (Fastest & most accurate, no captcha blocking)
            tryGoogleClients5(trimmed, effectiveSource, finalTarget)
                // Strategy 2: MyMemory Translated API (Reliable secondary fallback)
                ?: tryMyMemory(trimmed, effectiveSource, finalTarget)
                // Strategy 3: Smart fallback
                ?: fallbackTranslate(trimmed, effectiveSource, finalTarget)
        }
    }

    private fun checkOffline(text: String, sourceLang: String, targetLang: String): String? {
        val lower = text.lowercase()
        if (targetLang == "en" || targetLang == "auto") {
            offlinePhrases[text]?.let { return it }
        }
        if (targetLang == "ar" || targetLang == "auto") {
            offlinePhrasesEnToAr[lower]?.let { return it }
        }
        return null
    }

    private fun tryGoogleClients5(text: String, sourceLang: String, targetLang: String): String? {
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val sl = if (sourceLang == "auto") "auto" else sourceLang
            val urlString = "https://clients5.google.com/translate_a/t?client=dict-chrome-ex&sl=$sl&tl=$targetLang&q=$encoded"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                parseGoogleClients5Response(response)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseGoogleClients5Response(response: String): String? {
        return try {
            val jsonArray = JSONArray(response)
            val sb = StringBuilder()
            for (i in 0 until jsonArray.length()) {
                val elem = jsonArray.get(i)
                when (elem) {
                    is String -> sb.append(elem)
                    is JSONArray -> {
                        if (elem.length() > 0) {
                            val first = elem.get(0)
                            if (first is String) {
                                sb.append(first)
                            }
                        }
                    }
                }
            }
            val result = sb.toString().trim()
            if (result.isNotEmpty()) result else null
        } catch (_: Exception) {
            null
        }
    }

    private fun tryMyMemory(text: String, sourceLang: String, targetLang: String): String? {
        return try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val sl = if (sourceLang == "auto") "" else sourceLang
            val langpair = if (sl.isEmpty()) targetLang else "$sl|$targetLang"
            val urlString = "https://api.mymemory.translated.net/get?q=$encoded&langpair=$langpair"
            val url = URL(urlString)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val root = org.json.JSONObject(response)
                val responseData = root.optJSONObject("responseData")
                val translated = responseData?.optString("translatedText")?.trim()
                if (!translated.isNullOrEmpty() && !translated.startsWith("MYMEMORY WARNING")) {
                    cleanHtmlEntities(translated)
                } else {
                    null
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun cleanHtmlEntities(text: String): String {
        return text
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
    }

    private fun fallbackTranslate(text: String, sourceLang: String, targetLang: String): String {
        val lower = text.trim().lowercase()
        offlinePhrasesEnToAr[lower]?.let { return it }
        offlinePhrases[text.trim()]?.let { return it }

        // If no translation could be found and all networks failed, return the original text
        return text
    }
}
