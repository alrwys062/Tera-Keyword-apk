package com.example.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

data class SupportedAppLanguage(
    val code: String,
    val nameAr: String,
    val nameNative: String,
    val nameEn: String,
    val flag: String,
    val isRtl: Boolean = false
)

object LocaleHelper {

    private const val PREFS_NAME = "turbo_keyboard_prefs"
    private const val KEY_APP_LANGUAGE = "app_ui_language"

    val supportedLanguages: List<SupportedAppLanguage> = listOf(
        SupportedAppLanguage("auto", "تلقائي (لغة الجوال)", "System Default", "System Default", "🌐", false),
        SupportedAppLanguage("ar", "العربية", "العربية", "Arabic", "🇸🇦", true),
        SupportedAppLanguage("en", "الإنجليزية", "English", "English", "🇺🇸", false),
        SupportedAppLanguage("pt", "البرتغالية (البرازيل)", "Português (Brasil)", "Portuguese (Brazil)", "🇧🇷", false),
        SupportedAppLanguage("pt_PT", "البرتغالية (البرتغال)", "Português (Portugal)", "Portuguese (Portugal)", "🇵🇹", false),
        SupportedAppLanguage("es", "الإسبانية", "Español", "Spanish", "🇪🇸", false),
        SupportedAppLanguage("fr", "الفرنسية", "Français", "French", "🇫🇷", false),
        SupportedAppLanguage("de", "الألمانية", "Deutsch", "German", "🇩🇪", false),
        SupportedAppLanguage("tr", "التركية", "Türkçe", "Turkish", "🇹🇷", false),
        SupportedAppLanguage("ru", "الروسية", "Русский", "Russian", "🇷🇺", false),
        SupportedAppLanguage("fa", "الفارسية", "فارسی", "Persian", "🇮🇷", true),
        SupportedAppLanguage("ur", "الأردية", "اردو", "Urdu", "🇵🇰", true),
        SupportedAppLanguage("it", "الإيطالية", "Italiano", "Italian", "🇮🇹", false),
        SupportedAppLanguage("id", "الإندونيسية", "Bahasa Indonesia", "Indonesian", "🇮🇩", false),
        SupportedAppLanguage("hi", "الهندية", "हिन्दी", "Hindi", "🇮🇳", false),
        SupportedAppLanguage("zh", "الصينية", "中文", "Chinese", "🇨🇳", false),
        SupportedAppLanguage("ja", "اليابانية", "日本語", "Japanese", "🇯🇵", false),
        SupportedAppLanguage("ko", "الكورية", "한국어", "Korean", "🇰🇷", false),
        SupportedAppLanguage("nl", "الهولندية", "Nederlands", "Dutch", "🇳🇱", false),
        SupportedAppLanguage("pl", "البولندية", "Polski", "Polish", "🇵🇱", false),
        SupportedAppLanguage("sv", "السويدية", "Svenska", "Swedish", "🇸🇪", false),
        SupportedAppLanguage("uk", "الأوكرانية", "Українська", "Ukrainian", "🇺🇦", false),
        SupportedAppLanguage("el", "اليونانية", "Ελληνικά", "Greek", "🇬🇷", false),
        SupportedAppLanguage("he", "العبرية", "עבריت", "Hebrew", "🇮🇱", true),
        SupportedAppLanguage("ro", "الرومانية", "Română", "Romanian", "🇷🇴", false),
        SupportedAppLanguage("cs", "التشيكية", "Čeština", "Czech", "🇨🇿", false),
        SupportedAppLanguage("hu", "المجرية", "Magyar", "Hungarian", "🇭🇺", false),
        SupportedAppLanguage("vi", "الفيتنامية", "Tiếng Việt", "Vietnamese", "🇻🇳", false),
        SupportedAppLanguage("th", "التايلاندية", "ไทย", "Thai", "🇹🇭", false),
        SupportedAppLanguage("ms", "الماليزية", "Bahasa Melayu", "Malay", "🇲🇾", false),
        SupportedAppLanguage("bn", "البنغالية", "বাংলা", "Bengali", "🇧🇩", false),
        SupportedAppLanguage("sw", "السواحلية", "Kiswahili", "Swahili", "🇰🇪", false),
        SupportedAppLanguage("tl", "الفلبينية", "Filipino", "Filipino", "🇵🇭", false),
        SupportedAppLanguage("az", "الأذربيجانية", "Azərbaycan", "Azerbaijani", "🇦🇿", false),
        SupportedAppLanguage("uz", "الأوزبكية", "Oʻzbekcha", "Uzbek", "🇺🇿", false),
        SupportedAppLanguage("kk", "الكازاخستانية", "Қазақша", "Kazakh", "🇰🇿", false),
        SupportedAppLanguage("ku", "الكردية", "Kurdî", "Kurdish", "🇮🇶", true),
        SupportedAppLanguage("am", "الأمهرية", "አማርኛ", "Amharic", "🇪🇹", false),
        SupportedAppLanguage("so", "الصومالية", "Soomaali", "Somali", "🇸🇴", false),
        SupportedAppLanguage("ps", "البشتوية", "پښتو", "Pashto", "🇦🇫", true),
        SupportedAppLanguage("tg", "الطاجيكية", "Тоҷикӣ", "Tajik", "🇹🇯", false),
        SupportedAppLanguage("my", "البورمية", "မြန်မာ", "Burmese", "🇲🇲", false),
        SupportedAppLanguage("km", "الخميرية", "ខ្មែរ", "Khmer", "🇰🇭", false),
        SupportedAppLanguage("ne", "النيبالية", "नेपाली", "Nepali", "🇳🇵", false),
        SupportedAppLanguage("si", "السنهالية", "සිංහල", "Sinhala", "🇱🇰", false),
        SupportedAppLanguage("ta", "التاميلية", "தமிழ்", "Tamil", "🇮🇳", false),
        SupportedAppLanguage("te", "التيلوغوية", "తెలుగు", "Telugu", "🇮🇳", false),
        SupportedAppLanguage("kn", "الكانادية", "ಕನ್ನಡ", "Kannada", "🇮🇳", false),
        SupportedAppLanguage("ml", "المالايالامية", "മലയാളം", "Malayalam", "🇮🇳", false),
        SupportedAppLanguage("mr", "الماراثية", "मराठी", "Marathi", "🇮🇳", false),
        SupportedAppLanguage("gu", "الغوجاراتية", "ગુજરાતી", "Gujarati", "🇮🇳", false),
        SupportedAppLanguage("pa", "البنجابية", "ਪੰਜਾਬੀ", "Punjabi", "🇮🇳", false),
        SupportedAppLanguage("hy", "الأرمنية", "Հայերեն", "Armenian", "🇦🇲", false),
        SupportedAppLanguage("ka", "الجورجية", "ქართული", "Georgian", "🇬🇪", false),
        SupportedAppLanguage("sq", "الألبانية", "Shqip", "Albanian", "🇦🇱", false),
        SupportedAppLanguage("mk", "المقدونية", "Македонски", "Macedonian", "🇲🇰", false),
        SupportedAppLanguage("sr", "الصربية", "Српски", "Serbian", "🇷🇸", false),
        SupportedAppLanguage("hr", "الكرواتية", "Hrvatski", "Croatian", "🇭🇷", false),
        SupportedAppLanguage("sk", "السلوفاكية", "Slovenčina", "Slovak", "🇸🇰", false),
        SupportedAppLanguage("sl", "السلوفينية", "Slovenščina", "Slovenian", "🇸🇮", false),
        SupportedAppLanguage("bg", "البلغارية", "Български", "Bulgarian", "🇧🇬", false),
        SupportedAppLanguage("lt", "الليتوانية", "Lietuvių", "Lithuanian", "🇱🇹", false),
        SupportedAppLanguage("lv", "اللاتفية", "Latviešu", "Latvian", "🇱🇻", false),
        SupportedAppLanguage("et", "الإستونية", "Eesti", "Estonian", "🇪🇪", false),
        SupportedAppLanguage("fi", "الفنلندية", "Suomi", "Finnish", "🇫🇮", false),
        SupportedAppLanguage("da", "الدنماركية", "Dansk", "Danish", "🇩🇰", false),
        SupportedAppLanguage("no", "النرويجية", "Norsk", "Norwegian", "🇳🇴", false),
        SupportedAppLanguage("is", "الآيسلندية", "Íslenska", "Icelandic", "🇮🇸", false),
        SupportedAppLanguage("af", "الأفريكانية", "Afrikaans", "Afrikaans", "🇿🇦", false),
        SupportedAppLanguage("ha", "الهوسية", "Hausa", "Hausa", "🇳🇬", false),
        SupportedAppLanguage("yo", "اليوروبية", "Yorùbá", "Yoruba", "🇳🇬", false),
        SupportedAppLanguage("ig", "الإيغبو", "Igbo", "Igbo", "🇳🇬", false),
        SupportedAppLanguage("zu", "الزولو", "isiZulu", "Zulu", "🇿🇦", false)
    )

    private val _appLanguageFlow = kotlinx.coroutines.flow.MutableStateFlow("auto")
    val appLanguageFlow: kotlinx.coroutines.flow.StateFlow<String> = _appLanguageFlow

    fun init(context: Context) {
        AppStrings.init(context)
        _appLanguageFlow.value = getAppLanguageCode(context)
    }

    fun getAppLanguageCode(context: Context): String {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = sp.getString(KEY_APP_LANGUAGE, "auto") ?: "auto"
        if (_appLanguageFlow.value != code) {
            _appLanguageFlow.value = code
        }
        return code
    }

    fun setAppLanguageCode(context: Context, code: String) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_APP_LANGUAGE, code).apply()
        _appLanguageFlow.value = code
    }

    fun getEffectiveLocale(context: Context): Locale {
        val code = getAppLanguageCode(context)
        return if (code == "auto") {
            Locale.getDefault()
        } else if (code.contains("_")) {
            val parts = code.split("_")
            Locale(parts[0], parts[1])
        } else {
            Locale(code)
        }
    }

    fun isRtl(context: Context): Boolean {
        val locale = getEffectiveLocale(context)
        val lang = locale.language.lowercase()
        return lang == "ar" || lang == "fa" || lang == "ur" || lang == "he" || lang == "ps" || lang == "ku"
    }

    fun isRtlLanguageCode(langCode: String): Boolean {
        val l = langCode.lowercase()
        return l == "ar" || l == "fa" || l == "ur" || l == "he" || l == "ps" || l == "ku"
    }

    fun isRtlLanguage(langCode: String): Boolean = isRtlLanguageCode(langCode)

    fun getLayoutDirection(context: Context): LayoutDirection {
        return if (isRtl(context)) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    fun getLayoutDirectionForCode(context: Context, langCode: String): LayoutDirection {
        return if (langCode == "auto") {
            getLayoutDirection(context)
        } else if (isRtlLanguageCode(langCode)) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
    }

    fun applyLocale(context: Context): Context {
        val locale = getEffectiveLocale(context)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = android.os.LocaleList(locale)
            android.os.LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        }

        @Suppress("DEPRECATION")
        try {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } catch (_: Exception) {}

        return try {
            context.createConfigurationContext(config)
        } catch (_: Exception) {
            context
        }
    }

    fun setLocale(context: Context): Context {
        return applyLocale(context)
    }

    fun setLocale(context: Context, languageCode: String): Context {
        setAppLanguageCode(context, languageCode)
        return applyLocale(context)
    }
}
