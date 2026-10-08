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

    // 1. 10 Columns Arabic Layout (Exact match for Screenshot IMG_20261008_162712_510.jpg / Gboard)
    val arabic10ColRow1 = listOf("ض", "ص", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج").map {
        KeyModel(KeyType.Character(it))
    }
    val arabic10ColRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك").map {
        KeyModel(KeyType.Character(it))
    }
    val arabic10ColRow3 = listOf(
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Character("ط")),
        KeyModel(KeyType.Character("ذ")),
        KeyModel(KeyType.Character("د")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("ث")),
        KeyModel(KeyType.Backspace, weight = 1.35f)
    )

    // 2. 11 Columns Arabic Layout (Standard Transboard / SwiftKey)
    val arabic11ColRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج").map {
        KeyModel(KeyType.Character(it))
    }
    val arabic11ColRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط").map {
        KeyModel(KeyType.Character(it))
    }
    val arabic11ColRow3 = listOf(
        KeyModel(KeyType.Character("ذ")),
        KeyModel(KeyType.Character("ء")),
        KeyModel(KeyType.Character("ؤ")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("ى")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Character("د")),
        KeyModel(KeyType.Backspace, weight = 1.35f)
    )

    // 3. 12 Columns Arabic Layout (Extended Samsung Layout)
    val arabic12ColRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج", "د").map {
        KeyModel(KeyType.Character(it))
    }
    val arabic12ColRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط").map {
        KeyModel(KeyType.Character(it))
    }
    val arabic12ColRow3 = listOf(
        KeyModel(KeyType.Character("ذ")),
        KeyModel(KeyType.Character("ئ")),
        KeyModel(KeyType.Character("ء")),
        KeyModel(KeyType.Character("ؤ")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("لا")),
        KeyModel(KeyType.Character("ى")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Backspace, weight = 1.45f)
    )

    // Samsung Arabic Layout
    val samsungRow1 = arabic12ColRow1
    val samsungRow2 = arabic12ColRow2
    val samsungRow3 = arabic12ColRow3

    // AOSP Arabic Layout
    val aospRow1 = arabic11ColRow1
    val aospRow2 = arabic11ColRow2
    val aospRow3 = arabic11ColRow3

    // SwiftKey Arabic Layout
    val swiftRow1 = arabic11ColRow1
    val swiftRow2 = arabic11ColRow2
    val swiftRow3 = arabic11ColRow3

    // Long press popup characters for Arabic & English (تشكيل، همزات، أرقام ورموز)
    val charPopupMap = mapOf(
        "ا" to listOf("أ", "إ", "آ", "ء", "ٱ", "1"),
        "و" to listOf("ؤ", "9"),
        "ي" to listOf("ئ", "ى", "8"),
        "ت" to listOf("ة", "4"),
        "ه" to listOf("ة", "6"),
        "لا" to listOf("لأ", "لإ", "لآ"),
        "ل" to listOf("لا", "لأ", "لإ"),
        "ب" to listOf("پ", "2"),
        "ج" to listOf("چ"),
        "ف" to listOf("ڤ"),
        "ك" to listOf("گ"),
        "ز" to listOf("ژ"),
        "س" to listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ّ", "ْ", "ـ"),
        "q" to listOf("1"), "w" to listOf("2"), "e" to listOf("3", "é", "è", "ê", "ë"),
        "r" to listOf("4"), "t" to listOf("5"), "y" to listOf("6"), "u" to listOf("7", "ú", "ù", "û", "ü"),
        "i" to listOf("8", "í", "ì", "î", "ï"), "o" to listOf("9", "ó", "ò", "ô", "ö", "õ"),
        "p" to listOf("0"), "a" to listOf("á", "à", "â", "ä", "ã", "å"), "s" to listOf("ß", "$"),
        "c" to listOf("ç"), "n" to listOf("ñ")
    )

    // Tashkeel / Diacritics
    val arabicTashkeelRow1 = listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ّ", "ْ", "ـ", "؛", "،").map {
        KeyModel(KeyType.Character(it))
    }

    val arabicTashkeelRow2 = listOf("؟", "!", "«", "»", "–", "—", "…", "٪", "×", "÷", "±").map {
        KeyModel(KeyType.Character(it))
    }

    // English Layout
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

    // Numbers Row
    val numbersRowAr = listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠").map {
        KeyModel(KeyType.Character(it))
    }

    val numbersRowEn = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map {
        KeyModel(KeyType.Character(it))
    }

    // Symbols Page 1
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

    // Symbols Page 2
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

    fun parseCustomRow(text: String): List<KeyModel> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return emptyList()
        val tokens = if (trimmed.contains(" ")) {
            trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        } else {
            trimmed.map { it.toString() }
        }
        return tokens.map { token ->
            if (token == "⌫" || token.equals("backspace", ignoreCase = true)) {
                KeyModel(KeyType.Backspace, weight = 1.35f)
            } else {
                KeyModel(KeyType.Character(token))
            }
        }
    }

    fun getRowsForLanguage(
        lang: com.example.data.WorldLanguage,
        isShifted: Boolean = false,
        isArabic: Boolean = false,
        layoutStyle: String = "10_columns",
        columnsCount: Int = 10,
        customRow1: String = "",
        customRow2: String = "",
        customRow3: String = ""
    ): Triple<List<KeyModel>, List<KeyModel>, List<KeyModel>> {
        if (lang.code == "ar" || isArabic || lang.isRtl) {
            // Check manual custom layout
            if (layoutStyle == "custom" && (customRow1.isNotBlank() || customRow2.isNotBlank() || customRow3.isNotBlank())) {
                val r1 = parseCustomRow(customRow1).ifEmpty { arabic10ColRow1 }
                val r2 = parseCustomRow(customRow2).ifEmpty { arabic10ColRow2 }
                val r3List = parseCustomRow(customRow3).toMutableList()
                if (r3List.isEmpty()) {
                    return Triple(r1, r2, arabic10ColRow3)
                }
                if (r3List.none { it.type is KeyType.Backspace }) {
                    r3List.add(KeyModel(KeyType.Backspace, weight = 1.35f))
                }
                return Triple(r1, r2, r3List)
            }

            return when {
                layoutStyle == "10_columns" || layoutStyle == "gboard" || columnsCount == 10 -> {
                    Triple(arabic10ColRow1, arabic10ColRow2, arabic10ColRow3)
                }
                layoutStyle == "11_columns" || layoutStyle == "swift" || layoutStyle == "aosp" || columnsCount == 11 -> {
                    Triple(arabic11ColRow1, arabic11ColRow2, arabic11ColRow3)
                }
                layoutStyle == "12_columns" || layoutStyle == "samsung" || columnsCount == 12 -> {
                    Triple(arabic12ColRow1, arabic12ColRow2, arabic12ColRow3)
                }
                else -> Triple(arabic10ColRow1, arabic10ColRow2, arabic10ColRow3)
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
