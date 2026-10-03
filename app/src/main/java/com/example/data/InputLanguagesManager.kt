package com.example.data

import android.content.Context
import com.example.keyboard.KeyModel
import com.example.keyboard.KeyType

data class WorldLanguage(
    val code: String,
    val nameAr: String,
    val nameNative: String,
    val nameEn: String,
    val flag: String,
    val size: String = "1.2 MB",
    val isRtl: Boolean = false,
    val layoutTag: String = "QWERTY",
    val row1: List<String>,
    val row2: List<String>,
    val row3: List<String>,
    val quickShortcuts: List<String> = listOf("❤️", "😂", "👍", "🔥", "✨", "🎉", "🤲", "🌙", "👑", "💋")
)

object InputLanguagesManager {

    // All World Input Languages with accurate key layouts
    val allWorldLanguages: List<WorldLanguage> = listOf(
        // 1. Arabic
        WorldLanguage(
            code = "ar",
            nameAr = "عربي اساسي",
            nameNative = "عربي اساسي",
            nameEn = "Arabic (Standard)",
            flag = "🇸🇦",
            size = "1.5 MB",
            isRtl = true,
            layoutTag = "عربي قياسي",
            row1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
            row2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك"),
            row3 = listOf("ظ", "ط", "ذ", "د", "ز", "ر", "و", "ة", "ى"),
            quickShortcuts = listOf("👑", "💋", "ة", "ؤ", "ء", "ئ", "ى", "لأ", "😂", "خاص")
        ),

        // 2. English (US/UK)
        WorldLanguage(
            code = "en",
            nameAr = "الإنجليزية",
            nameNative = "English",
            nameEn = "English (US)",
            flag = "🇺🇸",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m"),
            quickShortcuts = listOf("🔥", "haha", "⭐", "❤️", "👍", "🍋", "🍌", "🌿", "🤌", "✏️")
        ),

        // 3. French (Français - AZERTY)
        WorldLanguage(
            code = "fr",
            nameAr = "الفرنسية",
            nameNative = "Français",
            nameEn = "French",
            flag = "🇫🇷",
            size = "1.3 MB",
            isRtl = false,
            layoutTag = "AZERTY",
            row1 = listOf("a", "z", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("q", "s", "d", "f", "g", "h", "j", "k", "l", "m"),
            row3 = listOf("w", "x", "c", "v", "b", "n", "é", "è", "ç")
        ),

        // 4. Spanish (Español)
        WorldLanguage(
            code = "es",
            nameAr = "الإسبانية",
            nameNative = "Español",
            nameEn = "Spanish",
            flag = "🇪🇸",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ñ"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "á", "é")
        ),

        // 5. German (Deutsch - QWERTZ)
        WorldLanguage(
            code = "de",
            nameAr = "الألمانية",
            nameNative = "Deutsch",
            nameEn = "German",
            flag = "🇩🇪",
            size = "1.4 MB",
            isRtl = false,
            layoutTag = "QWERTZ",
            row1 = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ü"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä"),
            row3 = listOf("y", "x", "c", "v", "b", "n", "m", "ß")
        ),

        // 6. Turkish (Türkçe)
        WorldLanguage(
            code = "tr",
            nameAr = "التركية",
            nameNative = "Türkçe",
            nameEn = "Turkish",
            flag = "🇹🇷",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "Türkçe Q",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "ı", "o", "p", "ğ", "ü"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ş", "i"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ö", "ç")
        ),

        // 7. Russian (Русский - Cyrillic)
        WorldLanguage(
            code = "ru",
            nameAr = "الروسية",
            nameNative = "Русский",
            nameEn = "Russian",
            flag = "🇷🇺",
            size = "1.6 MB",
            isRtl = false,
            layoutTag = "ЙЦУКЕН",
            row1 = listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "ъ"),
            row2 = listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э"),
            row3 = listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю")
        ),

        // 8. Persian / Farsi (فارسی)
        WorldLanguage(
            code = "fa",
            nameAr = "الفارسية",
            nameNative = "فارسی",
            nameEn = "Persian",
            flag = "🇮🇷",
            size = "1.4 MB",
            isRtl = true,
            layoutTag = "فارسی",
            row1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ"),
            row2 = listOf("ش", "س", "ی", "ب", "ل", "ا", "ت", "ن", "م", "ک", "گ"),
            row3 = listOf("ظ", "ط", "ز", "ر", "ذ", "د", "پ", "و", "ژ")
        ),

        // 9. Urdu (اردو)
        WorldLanguage(
            code = "ur",
            nameAr = "الأردية",
            nameNative = "اردو",
            nameEn = "Urdu",
            flag = "🇵🇰",
            size = "1.5 MB",
            isRtl = true,
            layoutTag = "اردو",
            row1 = listOf("ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "چ"),
            row2 = listOf("ش", "س", "ی", "ب", "ل", "ا", "ت", "ٹ", "ن", "م", "ک", "گ"),
            row3 = listOf("ظ", "ط", "ز", "ژ", "ر", "ڑ", "ذ", "د", "ڈ", "و", "ے")
        ),

        // 10. Italian (Italiano)
        WorldLanguage(
            code = "it",
            nameAr = "الإيطالية",
            nameNative = "Italiano",
            nameEn = "Italian",
            flag = "🇮🇹",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "é"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ò", "à"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ù", "ì")
        ),

        // 11. Portuguese (Português)
        WorldLanguage(
            code = "pt",
            nameAr = "البرتغالية",
            nameNative = "Português",
            nameEn = "Portuguese",
            flag = "🇧🇷",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ç"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ã", "õ")
        ),

        // 12. Indonesian (Bahasa Indonesia)
        WorldLanguage(
            code = "id",
            nameAr = "الإندونيسية",
            nameNative = "Bahasa Indonesia",
            nameEn = "Indonesian",
            flag = "🇮🇩",
            size = "1.0 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 13. Hindi (हिन्दी)
        WorldLanguage(
            code = "hi",
            nameAr = "الهندية",
            nameNative = "हिन्दी",
            nameEn = "Hindi",
            flag = "🇮🇳",
            size = "1.6 MB",
            isRtl = false,
            layoutTag = "देवनागरी",
            row1 = listOf("क", "ख", "ग", "घ", "च", "छ", "ज", "झ", "ट", "ठ"),
            row2 = listOf("त", "थ", "द", "ध", "न", "प", "फ", "ब", "भ", "म"),
            row3 = listOf("य", "र", "ल", "व", "श", "ष", "स", "ह", "ा", "ी")
        ),

        // 14. Hebrew (עברית)
        WorldLanguage(
            code = "he",
            nameAr = "العبرية",
            nameNative = "עברית",
            nameEn = "Hebrew",
            flag = "🇮🇱",
            size = "1.1 MB",
            isRtl = true,
            layoutTag = "עברית",
            row1 = listOf("ק", "ר", "א", "ט", "ו", "ן", "ם", "פ"),
            row2 = listOf("ש", "ד", "ג", "כ", "ע", "י", "ח", "ל", "ך", "ף"),
            row3 = listOf("ז", "ס", "ב", "ה", "נ", "מ", "צ", "ת", "ץ")
        ),

        // 15. Greek (Ελληνικά)
        WorldLanguage(
            code = "el",
            nameAr = "اليونانية",
            nameNative = "Ελληνικά",
            nameEn = "Greek",
            flag = "🇬🇷",
            size = "1.3 MB",
            isRtl = false,
            layoutTag = "Ελληνικά",
            row1 = listOf("ε", "ρ", "τ", "υ", "θ", "ι", "ο", "π"),
            row2 = listOf("α", "σ", "δ", "φ", "γ", "η", "ξ", "κ", "λ"),
            row3 = listOf("ζ", "χ", "ψ", "ω", "β", "ν", "μ")
        ),

        // 16. Kurdish (کوردی)
        WorldLanguage(
            code = "ku",
            nameAr = "الكردية",
            nameNative = "کوردی",
            nameEn = "Kurdish",
            flag = "☀️",
            size = "1.3 MB",
            isRtl = true,
            layoutTag = "سۆرانی",
            row1 = listOf("ض", "ص", "ث", "ق", "ف", "ڤ", "غ", "ع", "ھ", "خ", "ح", "ج", "چ"),
            row2 = listOf("ش", "س", "ی", "ێ", "ب", "ل", "ڵ", "ا", "ت", "ن", "م", "ک", "گ"),
            row3 = listOf("ظ", "ط", "ز", "ژ", "ڕ", "ر", "ذ", "د", "پ", "و", "ۆ")
        ),

        // 17. Pashto (پښتو)
        WorldLanguage(
            code = "ps",
            nameAr = "البشتوية",
            nameNative = "پښتو",
            nameEn = "Pashto",
            flag = "🇦🇫",
            size = "1.3 MB",
            isRtl = true,
            layoutTag = "پښتو",
            row1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ځ", "ح", "ج", "چ"),
            row2 = listOf("ش", "ښ", "س", "ی", "ې", "ب", "ل", "ا", "ت", "ټ", "ن", "ڼ", "م", "ک", "ګ"),
            row3 = listOf("ظ", "ط", "ز", "ژ", "ږ", "ر", "ړ", "ذ", "د", "ډ", "پ", "و", "ۍ")
        ),

        // 18. Swedish (Svenska)
        WorldLanguage(
            code = "sv",
            nameAr = "السويدية",
            nameNative = "Svenska",
            nameEn = "Swedish",
            flag = "🇸🇪",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "å"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 19. Dutch (Nederlands)
        WorldLanguage(
            code = "nl",
            nameAr = "الهولندية",
            nameNative = "Nederlands",
            nameEn = "Dutch",
            flag = "🇳🇱",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 20. Polish (Polski)
        WorldLanguage(
            code = "pl",
            nameAr = "البولندية",
            nameNative = "Polski",
            nameEn = "Polish",
            flag = "🇵🇱",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "Programisty",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ł"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ś", "ć", "ż")
        ),

        // 21. Ukrainian (Українська)
        WorldLanguage(
            code = "uk",
            nameAr = "الأوكرانية",
            nameNative = "Українська",
            nameEn = "Ukrainian",
            flag = "🇺🇦",
            size = "1.5 MB",
            isRtl = false,
            layoutTag = "ЙЦУКЕН",
            row1 = listOf("й", "ц", "у", "к", "е", "н", "г", "ш", "щ", "з", "х", "ї"),
            row2 = listOf("ф", "і", "в", "а", "п", "р", "о", "л", "д", "ж", "є"),
            row3 = listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю", "ґ")
        ),

        // 22. Vietnamese (Tiếng Việt)
        WorldLanguage(
            code = "vi",
            nameAr = "الفيتنامية",
            nameNative = "Tiếng Việt",
            nameEn = "Vietnamese",
            flag = "🇻🇳",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "Telex",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "ơ", "ư"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "đ"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 23. Thai (ไทย)
        WorldLanguage(
            code = "th",
            nameAr = "التايلاندية",
            nameNative = "ไทย",
            nameEn = "Thai",
            flag = "🇹🇭",
            size = "1.5 MB",
            isRtl = false,
            layoutTag = "Kedmanee",
            row1 = listOf("ไ", "ำ", "พ", "ะ", "ั", "ี", "ร", "น", "ย", "บ", "ล"),
            row2 = listOf("ฟ", "ห", "ก", "ด", "เ", "้", "่า", "ส", "ว", "ง"),
            row3 = listOf("ผ", "ป", "แ", "อ", "ิ", "ื", "ท", "ม", "ใ", "ฝ")
        ),

        // 24. Tagalog / Filipino
        WorldLanguage(
            code = "tl",
            nameAr = "الفلبينية",
            nameNative = "Tagalog",
            nameEn = "Filipino",
            flag = "🇵🇭",
            size = "1.0 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ñ")
        ),

        // 25. Swahili (Kiswahili)
        WorldLanguage(
            code = "sw",
            nameAr = "السواحيلية",
            nameNative = "Kiswahili",
            nameEn = "Swahili",
            flag = "🇰🇪",
            size = "1.0 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 26. Romanian (Română)
        WorldLanguage(
            code = "ro",
            nameAr = "الرومانية",
            nameNative = "Română",
            nameEn = "Romanian",
            flag = "🇷🇴",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "ă", "î"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ș", "ț"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "â")
        ),

        // 27. Czech (Čeština - QWERTZ)
        WorldLanguage(
            code = "cs",
            nameAr = "التشيكية",
            nameNative = "Čeština",
            nameEn = "Czech",
            flag = "🇨🇿",
            size = "1.3 MB",
            isRtl = false,
            layoutTag = "QWERTZ",
            row1 = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ú"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ů", "§"),
            row3 = listOf("y", "x", "c", "v", "b", "n", "m", "ě", "š", "č")
        ),

        // 28. Hungarian (Magyar - QWERTZ)
        WorldLanguage(
            code = "hu",
            nameAr = "المجرية",
            nameNative = "Magyar",
            nameEn = "Hungarian",
            flag = "🇭🇺",
            size = "1.3 MB",
            isRtl = false,
            layoutTag = "QWERTZ",
            row1 = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ő", "ú"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "é", "á", "ű"),
            row3 = listOf("y", "x", "c", "v", "b", "n", "m", "ö", "ü", "ó")
        ),

        // 29. Malay (Bahasa Melayu)
        WorldLanguage(
            code = "ms",
            nameAr = "الماليزية",
            nameNative = "Bahasa Melayu",
            nameEn = "Malay",
            flag = "🇲🇾",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 30. Japanese (Romaji / 日本語)
        WorldLanguage(
            code = "ja",
            nameAr = "اليابانية",
            nameNative = "日本語",
            nameEn = "Japanese",
            flag = "🇯🇵",
            size = "1.8 MB",
            isRtl = false,
            layoutTag = "Romaji",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 31. Chinese (Pinyin / 中文)
        WorldLanguage(
            code = "zh",
            nameAr = "الصينية",
            nameNative = "中文",
            nameEn = "Chinese",
            flag = "🇨🇳",
            size = "1.8 MB",
            isRtl = false,
            layoutTag = "Pinyin",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 32. Korean (Hangul / 한국어)
        WorldLanguage(
            code = "ko",
            nameAr = "الكورية",
            nameNative = "한국어",
            nameEn = "Korean",
            flag = "🇰🇷",
            size = "1.6 MB",
            isRtl = false,
            layoutTag = "두벌식",
            row1 = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ"),
            row2 = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ"),
            row3 = listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")
        ),

        // 33. Bengali (বাংলা)
        WorldLanguage(
            code = "bn",
            nameAr = "البنغالية",
            nameNative = "বাংলা",
            nameEn = "Bengali",
            flag = "🇧🇩",
            size = "1.4 MB",
            isRtl = false,
            layoutTag = "জাতীয়",
            row1 = listOf("দ", "দ", "ঘ", "খ", "ঝ", "চ", "ট", "ঠ", "প", "ফ"),
            row2 = listOf("র", "ক", "ত", "থ", "গ", "হ", "জ", "ল", "স", "শ"),
            row3 = listOf("য", "শ", "ষ", "ম", "ন", "ব", "ভ")
        ),

        // 34. Somali (Soomaali)
        WorldLanguage(
            code = "so",
            nameAr = "الصومالية",
            nameNative = "Soomaali",
            nameEn = "Somali",
            flag = "🇸🇴",
            size = "1.0 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 35. Amharic (አማርኛ)
        WorldLanguage(
            code = "am",
            nameAr = "الأمهرية",
            nameNative = "አማርኛ",
            nameEn = "Amharic",
            flag = "🇪🇹",
            size = "1.6 MB",
            isRtl = false,
            layoutTag = "ግዕዝ",
            row1 = listOf("ቀ", "ወ", "ዐ", "ረ", "ተ", "የ", "ኡ", "ኢ", "ኦ", "ፕ"),
            row2 = listOf("አ", "ሰ", "ደ", "ፈ", "ገ", "ሀ", "ጀ", "ከ", "ለ"),
            row3 = listOf("ዘ", "ሸ", "ቸ", "ቨ", "በ", "ነ", "መ")
        ),

        // 36. Azerbaijani (Azərbaycanca)
        WorldLanguage(
            code = "az",
            nameAr = "الأذربيجانية",
            nameNative = "Azərbaycanca",
            nameEn = "Azerbaijani",
            flag = "🇦🇿",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "ü", "e", "r", "t", "y", "u", "i", "o", "p", "ö", "ğ"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ı", "ə"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ç", "ş")
        ),

        // 37. Kazakh (Қазақша)
        WorldLanguage(
            code = "kk",
            nameAr = "الكازاخستانية",
            nameNative = "Қазақша",
            nameEn = "Kazakh",
            flag = "🇰🇿",
            size = "1.5 MB",
            isRtl = false,
            layoutTag = "Кирилл",
            row1 = listOf("ә", "і", "ң", "ғ", "ү", "ұ", "қ", "ө", "һ", "ц", "у"),
            row2 = listOf("ф", "ы", "в", "а", "п", "р", "о", "л", "д", "ж", "э"),
            row3 = listOf("я", "ч", "с", "м", "и", "т", "ь", "б", "ю")
        ),

        // 38. Uzbek (Oʻzbekcha)
        WorldLanguage(
            code = "uz",
            nameAr = "الأوزبكية",
            nameNative = "Oʻzbekcha",
            nameEn = "Uzbek",
            flag = "🇺🇿",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "gʻ", "oʻ")
        ),

        // 39. Danish (Dansk)
        WorldLanguage(
            code = "da",
            nameAr = "الدانماركية",
            nameNative = "Dansk",
            nameEn = "Danish",
            flag = "🇩🇰",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "å"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "æ", "ø"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 40. Finnish (Suomi)
        WorldLanguage(
            code = "fi",
            nameAr = "الفنلندية",
            nameNative = "Suomi",
            nameEn = "Finnish",
            flag = "🇫🇮",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "å"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ö", "ä"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 41. Norwegian (Norsk)
        WorldLanguage(
            code = "no",
            nameAr = "النرويجية",
            nameNative = "Norsk",
            nameEn = "Norwegian",
            flag = "🇳🇴",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "å"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ø", "æ"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m")
        ),

        // 42. Bulgarian (Български)
        WorldLanguage(
            code = "bg",
            nameAr = "البلغارية",
            nameNative = "Български",
            nameEn = "Bulgarian",
            flag = "🇧🇬",
            size = "1.4 MB",
            isRtl = false,
            layoutTag = "БДС",
            row1 = listOf("я", "в", "е", "р", "т", "ъ", "у", "и", "о", "п", "ш", "щ"),
            row2 = listOf("а", "с", "д", "ф", "г", "х", "й", "к", "л", "з", "ж"),
            row3 = listOf("ь", "ю", "ч", "с", "м", "н", "б", "ц")
        ),

        // 43. Serbian (Српски)
        WorldLanguage(
            code = "sr",
            nameAr = "الصربية",
            nameNative = "Српски",
            nameEn = "Serbian",
            flag = "🇷🇸",
            size = "1.3 MB",
            isRtl = false,
            layoutTag = "Ћирилица",
            row1 = listOf("љ", "њ", "е", "р", "т", "з", "у", "и", "о", "п", "ш", "ђ"),
            row2 = listOf("а", "с", "д", "ф", "г", "х", "ј", "к", "л", "ч", "ћ"),
            row3 = listOf("џ", "ц", "в", "б", "н", "м", "ж")
        ),

        // 44. Croatian (Hrvatski)
        WorldLanguage(
            code = "hr",
            nameAr = "الكرواتية",
            nameNative = "Hrvatski",
            nameEn = "Croatian",
            flag = "🇭🇷",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "QWERTZ",
            row1 = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "š", "đ"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "č", "ć"),
            row3 = listOf("y", "x", "c", "v", "b", "n", "m", "ž")
        ),

        // 45. Slovak (Slovenčina)
        WorldLanguage(
            code = "sk",
            nameAr = "السلوفاكية",
            nameNative = "Slovenčina",
            nameEn = "Slovak",
            flag = "🇸🇰",
            size = "1.2 MB",
            isRtl = false,
            layoutTag = "QWERTZ",
            row1 = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "ú", "ä"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ô", "§"),
            row3 = listOf("y", "x", "c", "v", "b", "n", "m", "ď", "ť", "ň")
        ),

        // 46. Tamil (தமிழ்)
        WorldLanguage(
            code = "ta",
            nameAr = "التاميلية",
            nameNative = "தமிழ்",
            nameEn = "Tamil",
            flag = "🇮🇳",
            size = "1.5 MB",
            isRtl = false,
            layoutTag = "தமிழ்99",
            row1 = listOf("ஆ", "ஈ", "ஊ", "ஐ", "ஏ", "ள", "ற", "ன", "ட", "ண"),
            row2 = listOf("அ", "இ", "உ", "ஒ", "எ", "க", "ப", "ம", "த", "ந"),
            row3 = listOf("ங", "ஞ", "ய", "ர", "ல", "வ", "ழ")
        ),

        // 47. Telugu (తెలుగు)
        WorldLanguage(
            code = "te",
            nameAr = "التيلجو",
            nameNative = "తెలుగు",
            nameEn = "Telugu",
            flag = "🇮🇳",
            size = "1.5 MB",
            isRtl = false,
            layoutTag = "ఇన్స్క్రిప్ట్",
            row1 = listOf("ఔ", "ఐ", "ఆ", "ఈ", "ఊ", "బ", "హ", "గ", "ద", "జ", "డ"),
            row2 = listOf("ొ", "ో", "ా", "ీ", "ూ", "ర", "క", "త", "చ", "ట"),
            row3 = listOf("ం", "మ", "న", "వ", "ల", "స", "య")
        ),

        // 48. Malayalam (മലയാളം)
        WorldLanguage(
            code = "ml",
            nameAr = "الماليالامية",
            nameNative = "മലയാളം",
            nameEn = "Malayalam",
            flag = "🇮🇳",
            size = "1.5 MB",
            isRtl = false,
            layoutTag = "ഇൻസ്ക്രിപ്റ്റ്",
            row1 = listOf("ൌ", "ൈ", "ാ", "ീ", "ൂ", "ബ", "ഹ", "ഗ", "ദ", "ജ", "ഡ"),
            row2 = listOf("ൊ", "ോ", "്", "ി", "ു", "ര", "ക", "ത", "ച", "ട"),
            row3 = listOf("ം", "മ", "ന", "വ", "ല", "സ", "യ")
        ),

        // 49. Nepali (नेपाली)
        WorldLanguage(
            code = "ne",
            nameAr = "النيبالية",
            nameNative = "नेपाली",
            nameEn = "Nepali",
            flag = "🇳🇵",
            size = "1.4 MB",
            isRtl = false,
            layoutTag = "परम्परागत",
            row1 = listOf("ट", "ठ", "ड", "ढ", "ण", "त", "थ", "द", "ध", "न"),
            row2 = listOf("प", "फ", "ब", "भ", "म", "य", "र", "ल", "व"),
            row3 = listOf("श", "ष", "स", "ह", "क", "ख", "ग", "घ")
        ),

        // 50. Georgian (ქართული)
        WorldLanguage(
            code = "ka",
            nameAr = "الجورجية",
            nameNative = "ქართული",
            nameEn = "Georgian",
            flag = "🇬🇪",
            size = "1.3 MB",
            isRtl = false,
            layoutTag = "ქართული",
            row1 = listOf("ქ", "წ", "ე", "რ", "ტ", "ყ", "უ", "ი", "ო", "პ"),
            row2 = listOf("ა", "ს", "დ", "ფ", "გ", "ჰ", "ჯ", "კ", "ლ"),
            row3 = listOf("ზ", "ხ", "ც", "ვ", "ბ", "ნ", "მ")
        ),

        // 51. Armenian (Հայերեն)
        WorldLanguage(
            code = "hy",
            nameAr = "الأرمنية",
            nameNative = "Հայերեն",
            nameEn = "Armenian",
            flag = "🇦🇲",
            size = "1.3 MB",
            isRtl = false,
            layoutTag = "Հայերեն",
            row1 = listOf("ք", "ո", "ե", "ռ", "տ", "ը", "ւ", "ի", "օ", "պ", "խ", "ծ"),
            row2 = listOf("ա", "ս", "դ", "ֆ", "գ", "հ", "յ", "կ", "լ", "թ", "փ"),
            row3 = listOf("զ", "ղ", "ց", "վ", "բ", "ն", "մ", "շ", "չ")
        ),

        // 52. Albanian (Shqip)
        WorldLanguage(
            code = "sq",
            nameAr = "الألبانية",
            nameNative = "Shqip",
            nameEn = "Albanian",
            flag = "🇦🇱",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTY",
            row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ë"),
            row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ç")
        ),

        // 53. Bosnian (Bosanski)
        WorldLanguage(
            code = "bs",
            nameAr = "البوسنية",
            nameNative = "Bosanski",
            nameEn = "Bosnian",
            flag = "🇧🇦",
            size = "1.1 MB",
            isRtl = false,
            layoutTag = "QWERTZ",
            row1 = listOf("q", "w", "e", "r", "t", "z", "u", "i", "o", "p", "š", "đ"),
            row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "č", "ć"),
            row3 = listOf("y", "x", "c", "v", "b", "n", "m", "ž")
        )
    )

    fun getLanguageByCode(code: String): WorldLanguage {
        return allWorldLanguages.find { it.code == code } ?: allWorldLanguages[0]
    }

    // Default installed on first launch: Arabic and English
    fun getInstalledLanguages(context: Context): List<String> {
        val sp = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)
        val saved = sp.getString("installed_languages_list", "ar,en") ?: "ar,en"
        return saved.split(",").filter { it.isNotBlank() }
    }

    fun saveInstalledLanguages(context: Context, codes: List<String>) {
        val sp = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("installed_languages_list", codes.distinct().joinToString(",")).apply()
    }

    fun getActiveLanguages(context: Context): List<String> {
        val sp = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)
        val saved = sp.getString("active_languages_list", "ar,en") ?: "ar,en"
        val active = saved.split(",").filter { it.isNotBlank() }
        val installed = getInstalledLanguages(context)
        val valid = active.filter { it in installed }
        return if (valid.isNotEmpty()) valid else listOf("ar")
    }

    fun saveActiveLanguages(context: Context, codes: List<String>) {
        val sp = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("active_languages_list", codes.distinct().joinToString(",")).apply()
    }

    fun installLanguage(context: Context, code: String) {
        val installed = getInstalledLanguages(context).toMutableList()
        if (code !in installed) {
            installed.add(code)
            saveInstalledLanguages(context, installed)
        }
        val active = getActiveLanguages(context).toMutableList()
        if (code !in active) {
            active.add(code)
            saveActiveLanguages(context, active)
        }
    }

    fun uninstallLanguage(context: Context, code: String) {
        if (code == "ar" || code == "en") return // Keep basic defaults
        val installed = getInstalledLanguages(context).toMutableList()
        installed.remove(code)
        saveInstalledLanguages(context, installed)

        val active = getActiveLanguages(context).toMutableList()
        active.remove(code)
        saveActiveLanguages(context, active)
    }

    fun toggleLanguageActive(context: Context, code: String): Boolean {
        val active = getActiveLanguages(context).toMutableList()
        val willBeActive = if (code in active) {
            if (active.size > 1) {
                active.remove(code)
                false
            } else {
                true // Must have at least one active language
            }
        } else {
            active.add(code)
            true
        }
        saveActiveLanguages(context, active)
        return willBeActive
    }

    // Get current typing language
    fun getCurrentLanguageCode(context: Context): String {
        val sp = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)
        val current = sp.getString("current_typing_language_code", "ar") ?: "ar"
        val active = getActiveLanguages(context)
        return if (current in active) current else active.firstOrNull() ?: "ar"
    }

    fun setCurrentLanguageCode(context: Context, code: String) {
        val sp = context.getSharedPreferences("turbo_keyboard_prefs", Context.MODE_PRIVATE)
        sp.edit().putString("current_typing_language_code", code).apply()
    }

    fun getNextActiveLanguage(context: Context, currentCode: String): WorldLanguage {
        val active = getActiveLanguages(context)
        if (active.isEmpty()) return getLanguageByCode("ar")
        val currentIndex = active.indexOf(currentCode)
        val nextIndex = if (currentIndex == -1 || currentIndex >= active.size - 1) 0 else currentIndex + 1
        val nextCode = active[nextIndex]
        setCurrentLanguageCode(context, nextCode)
        return getLanguageByCode(nextCode)
    }
}
