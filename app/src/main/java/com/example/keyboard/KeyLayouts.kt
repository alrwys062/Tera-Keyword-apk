package com.example.keyboard

sealed class KeyType {
    data class Character(val primary: String, val secondary: String? = null) : KeyType()
    data object Shift : KeyType()
    data object Backspace : KeyType()
    data object Enter : KeyType()
    data object Space : KeyType()
    data object ModeChange : KeyType() // 123!#() / ABC
    data object Emoji : KeyType()
    data object Clipboard : KeyType()
    data object Com : KeyType()
    data object LanguageSwitch : KeyType()
}

data class KeyModel(
    val type: KeyType,
    val weight: Float = 1.0f
)

object KeyLayouts {

    // Quick shortcuts row matching iOS / Transboard layout
    val arabicQuickShortcutsRow = listOf("👑", "💋", "ة", "ؤ", "ء", "ئ", "ى", "لأ", "😂", "خاص").map {
        KeyModel(KeyType.Character(it))
    }

    val englishQuickShortcutsRow = listOf("🔥", "ههه", "⭐", "🔫", "❤️", "🍋", "🍌", "🌿", "🤌", "✏️").map {
        KeyModel(KeyType.Character(it))
    }

    // 10-Column Arabic Layout (iOS 16 / Gboard 10-Column Standard)
    // Row 1: Exactly 10 keys (ض ص ث ق ف غ ع ه خ ح)
    val arabicRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح").map {
        KeyModel(KeyType.Character(it))
    }

    // Row 2: Exactly 10 keys (ش س ي ب ل ا ت ن م ك)
    val arabicRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك").map {
        KeyModel(KeyType.Character(it))
    }

    // Row 3: Exactly 10 columns (9 wide letters + Backspace = 10 columns)
    val arabicRow3 = listOf(
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Character("ط")),
        KeyModel(KeyType.Character("ذ")),
        KeyModel(KeyType.Character("د")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("ى")),
        KeyModel(KeyType.Backspace, weight = 1.25f)
    )

    // Samsung / Classic 10-Column Arabic Layout
    val samsungRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح").map {
        KeyModel(KeyType.Character(it))
    }
    val samsungRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك").map {
        KeyModel(KeyType.Character(it))
    }
    val samsungRow3 = listOf(
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Character("ط")),
        KeyModel(KeyType.Character("ذ")),
        KeyModel(KeyType.Character("د")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("ى")),
        KeyModel(KeyType.Backspace, weight = 1.25f)
    )

    // AOSP 10-Column Arabic Layout
    val aospRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح").map {
        KeyModel(KeyType.Character(it))
    }
    val aospRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك").map {
        KeyModel(KeyType.Character(it))
    }
    val aospRow3 = listOf(
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Character("ط")),
        KeyModel(KeyType.Character("ذ")),
        KeyModel(KeyType.Character("د")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("ى")),
        KeyModel(KeyType.Backspace, weight = 1.25f)
    )

    // SwiftKey 10-Column Arabic Layout
    val swiftRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح").map {
        KeyModel(KeyType.Character(it))
    }
    val swiftRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك").map {
        KeyModel(KeyType.Character(it))
    }
    val swiftRow3 = listOf(
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Character("ط")),
        KeyModel(KeyType.Character("ذ")),
        KeyModel(KeyType.Character("د")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("ى")),
        KeyModel(KeyType.Backspace, weight = 1.25f)
    )

    // Long press popup characters for Arabic & English (تشكيل، همزات، أرقام ورموز)
    val charPopupMap = mapOf(
        "ح" to listOf("ج", "خ", "0"),
        "خ" to listOf("ح", "ج", "9"),
        "د" to listOf("ذ", "4"),
        "ذ" to listOf("د", "3"),
        "ط" to listOf("ظ", "2"),
        "ظ" to listOf("ط", "1"),
        "ا" to listOf("أ", "إ", "آ", "ء", "ٱ", "1"),
        "و" to listOf("ؤ", "9"),
        "ي" to listOf("ئ", "ى", "8"),
        "ت" to listOf("ة", "4"),
        "ه" to listOf("ة", "6"),
        "لا" to listOf("لأ", "لإ", "لآ"),
        "ل" to listOf("لا", "لأ", "لإ"),
        "ب" to listOf("پ", "2"),
        "ج" to listOf("چ", "ح", "خ"),
        "ف" to listOf("ڤ", "5"),
        "ك" to listOf("گ", "7"),
        "ز" to listOf("ژ", "8"),
        "س" to listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ّ", "ْ", "ـ"),
        "q" to listOf("1"), "w" to listOf("2"), "e" to listOf("3", "é", "è", "ê", "ë"),
        "r" to listOf("4"), "t" to listOf("5"), "y" to listOf("6"), "u" to listOf("7", "ú", "ù", "û", "ü"),
        "i" to listOf("8", "í", "ì", "î", "ï"), "o" to listOf("9", "ó", "ò", "ô", "ö", "õ"),
        "p" to listOf("0"), "a" to listOf("á", "à", "â", "ä", "ã", "å"), "s" to listOf("ß", "$"),
        "c" to listOf("ç"), "n" to listOf("ñ")
    )

    // Tashkeel / Diacritics (10 columns)
    val arabicTashkeelRow1 = listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ّ", "ْ", "ـ", "؛").map {
        KeyModel(KeyType.Character(it))
    }

    val arabicTashkeelRow2 = listOf("؟", "!", "«", "»", "–", "—", "…", "٪", "×", "÷").map {
        KeyModel(KeyType.Character(it))
    }

    // English Layout (10 Columns)
    val englishRow1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map {
        KeyModel(KeyType.Character(it))
    }

    val englishRow2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map {
        KeyModel(KeyType.Character(it))
    }

    val englishRow3 = listOf(
        KeyModel(KeyType.Shift, weight = 1.3f),
        KeyModel(KeyType.Character("z")),
        KeyModel(KeyType.Character("x")),
        KeyModel(KeyType.Character("c")),
        KeyModel(KeyType.Character("v")),
        KeyModel(KeyType.Character("b")),
        KeyModel(KeyType.Character("n")),
        KeyModel(KeyType.Character("m")),
        KeyModel(KeyType.Backspace, weight = 1.3f)
    )

    // Numbers Row (10 columns)
    val numbersRowAr = listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠").map {
        KeyModel(KeyType.Character(it))
    }

    val numbersRowEn = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map {
        KeyModel(KeyType.Character(it))
    }

    // Symbols Page 1 (10 Columns)
    val symbolsRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map {
        KeyModel(KeyType.Character(it))
    }

    val symbolsRow2 = listOf("@", "#", "/", "+", "%", "_", ".", "-", "*", ":").map {
        KeyModel(KeyType.Character(it))
    }

    val symbolsRow3 = listOf(
        KeyModel(KeyType.Character("=\\<"), weight = 1.2f),
        KeyModel(KeyType.Character("!")),
        KeyModel(KeyType.Character("\"")),
        KeyModel(KeyType.Character("$")),
        KeyModel(KeyType.Character("&")),
        KeyModel(KeyType.Character("(")),
        KeyModel(KeyType.Character(")")),
        KeyModel(KeyType.Character(";")),
        KeyModel(KeyType.Character("?")),
        KeyModel(KeyType.Backspace, weight = 1.3f)
    )

    // Symbols Page 2 (10 Columns)
    val symbolsMoreRow1 = listOf("~", "\\", "|", "•", "√", "π", "÷", "×", "{", "}").map {
        KeyModel(KeyType.Character(it))
    }

    val symbolsMoreRow2 = listOf("⇥", "£", "¢", "€", "°", "^", "_", "=", "[", "]").map {
        KeyModel(KeyType.Character(it))
    }

    val symbolsMoreRow3 = listOf(
        KeyModel(KeyType.Character("123"), weight = 1.2f),
        KeyModel(KeyType.Character("™")),
        KeyModel(KeyType.Character("®")),
        KeyModel(KeyType.Character("©")),
        KeyModel(KeyType.Character("¶")),
        KeyModel(KeyType.Character("\\")),
        KeyModel(KeyType.Character("<")),
        KeyModel(KeyType.Character(">")),
        KeyModel(KeyType.Backspace, weight = 1.3f)
    )

    fun getRowsForLanguage(
        lang: com.example.data.WorldLanguage,
        isShifted: Boolean = false,
        isArabic: Boolean = false,
        layoutStyle: String = "samsung"
    ): Triple<List<KeyModel>, List<KeyModel>, List<KeyModel>> {
        if (lang.code == "ar" || isArabic) {
            if (isShifted) {
                return Triple(arabicTashkeelRow1, arabicTashkeelRow2, samsungRow3)
            }
            return when (layoutStyle) {
                "samsung" -> Triple(samsungRow1, samsungRow2, samsungRow3)
                "aosp" -> Triple(aospRow1, aospRow2, aospRow3)
                "swift" -> Triple(swiftRow1, swiftRow2, swiftRow3)
                else -> Triple(arabicRow1, arabicRow2, arabicRow3)
            }
        }

        val r1 = lang.row1.map { char ->
            val display = if (isShifted && !lang.isRtl) char.uppercase() else char
            KeyModel(KeyType.Character(display))
        }

        val r2 = lang.row2.map { char ->
            val display = if (isShifted && !lang.isRtl) char.uppercase() else char
            KeyModel(KeyType.Character(display))
        }

        val r3 = mutableListOf<KeyModel>()
        if (!lang.isRtl) {
            r3.add(KeyModel(KeyType.Shift, weight = 1.3f))
        }
        lang.row3.forEach { char ->
            val display = if (isShifted && !lang.isRtl) char.uppercase() else char
            r3.add(KeyModel(KeyType.Character(display)))
        }
        r3.add(KeyModel(KeyType.Backspace, weight = 1.3f))

        return Triple(r1, r2, r3)
    }
}
