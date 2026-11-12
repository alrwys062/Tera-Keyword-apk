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
        SupportedAppLanguage("es", "الإسبانية", "Español", "Spanish", "🇪🇸", false),
        SupportedAppLanguage("fr", "الفرنسية", "Français", "French", "🇫🇷", false),
        SupportedAppLanguage("ru", "الروسية", "Русский", "Russian", "🇷🇺", false),
        SupportedAppLanguage("hi", "الهندية", "हिन्दी", "Hindi", "🇮🇳", false),
        SupportedAppLanguage("fa", "الفارسية", "فارسی", "Persian", "🇮🇷", true),
        SupportedAppLanguage("ku", "الكردية", "Kurdî", "Kurdish", "🇮🇶", true),
        SupportedAppLanguage("tr", "التركية", "Türkçe", "Turkish", "🇹🇷", false),
        SupportedAppLanguage("ur", "الأردية", "اردو", "Urdu", "🇵🇰", true),
        SupportedAppLanguage("id", "الإندونيسية", "Bahasa Indonesia", "Indonesian", "🇮🇩", false)
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
        sp.edit().putString(KEY_APP_LANGUAGE, code).commit()
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
