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

    // Quick shortcuts row matching Transboard Screenshots 14 & 15
    val arabicQuickShortcutsRow = listOf("👑", "💋", "ة", "ؤ", "ء", "ئ", "ى", "لأ", "😂", "خاص").map {
        KeyModel(KeyType.Character(it))
    }

    val englishQuickShortcutsRow = listOf("🔥", "ههه", "⭐", "🔫", "❤️", "🍋", "🍌", "🌿", "🤌", "✏️").map {
        KeyModel(KeyType.Character(it))
    }

    // عربي اساسي (Transboard Default - Screenshots 7, 14)
    val arabicRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج").map {
        KeyModel(KeyType.Character(it))
    }

    val arabicRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك").map {
        KeyModel(KeyType.Character(it))
    }

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
        KeyModel(KeyType.Backspace, weight = 1.4f)
    )

    // Tashkeel / Diacritics
    val arabicTashkeelRow1 = listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ّ", "ْ", "ـ", "؛", "،").map {
        KeyModel(KeyType.Character(it))
    }

    val arabicTashkeelRow2 = listOf("؟", "!", "«", "»", "–", "—", "…", "٪", "×", "÷", "±").map {
        KeyModel(KeyType.Character(it))
    }

    // English Layout (Screenshot 15)
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

    // Symbols Page 1 (Screenshot 16)
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

    // Symbols Page 2 (Screenshot 17)
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
        layoutStyle: String = "basic_ar"
    ): Triple<List<KeyModel>, List<KeyModel>, List<KeyModel>> {
        if (lang.code == "ar" || isArabic) {
            val r1 = if (isShifted) arabicTashkeelRow1 else arabicRow1
            val r2 = if (isShifted) arabicTashkeelRow2 else arabicRow2
            val r3 = arabicRow3
            return Triple(r1, r2, r3)
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
