package com.example.keyboard

sealed class KeyType {
    data class Character(val primary: String, val secondary: String? = null) : KeyType()
    data object Shift : KeyType()
    data object Backspace : KeyType()
    data object Enter : KeyType()
    data object Space : KeyType()
    data object ModeChange : KeyType() // 123 / ABC
    data object Emoji : KeyType()
    data object LanguageSwitch : KeyType()
}

data class KeyModel(
    val type: KeyType,
    val weight: Float = 1.0f
)

object KeyLayouts {

    val arabicRow1 = listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج").map {
        KeyModel(KeyType.Character(it))
    }

    val arabicRow2 = listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ط").map {
        KeyModel(KeyType.Character(it))
    }

    val arabicRow3 = listOf(
        KeyModel(KeyType.Shift, weight = 1.25f),
        KeyModel(KeyType.Character("ئ")),
        KeyModel(KeyType.Character("ء")),
        KeyModel(KeyType.Character("ؤ")),
        KeyModel(KeyType.Character("ر")),
        KeyModel(KeyType.Character("ى")),
        KeyModel(KeyType.Character("ة")),
        KeyModel(KeyType.Character("و")),
        KeyModel(KeyType.Character("ز")),
        KeyModel(KeyType.Character("ظ")),
        KeyModel(KeyType.Backspace, weight = 1.25f)
    )

    // Tashkeel row when Shift is pressed on Arabic
    val arabicTashkeelRow1 = listOf("َ", "ً", "ُ", "ٌ", "ِ", "ٍ", "ّ", "ْ", "ـ", "؛", "،").map {
        KeyModel(KeyType.Character(it))
    }

    val arabicTashkeelRow2 = listOf("؟", "!", "«", "»", "–", "—", "…", "٪", "×", "÷", "±").map {
        KeyModel(KeyType.Character(it))
    }

    val englishRow1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").map {
        KeyModel(KeyType.Character(it))
    }

    val englishRow2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l").map {
        KeyModel(KeyType.Character(it))
    }

    val englishRow3 = listOf(
        KeyModel(KeyType.Shift, weight = 1.4f),
        KeyModel(KeyType.Character("z")),
        KeyModel(KeyType.Character("x")),
        KeyModel(KeyType.Character("c")),
        KeyModel(KeyType.Character("v")),
        KeyModel(KeyType.Character("b")),
        KeyModel(KeyType.Character("n")),
        KeyModel(KeyType.Character("m")),
        KeyModel(KeyType.Backspace, weight = 1.4f)
    )

    val numbersRowAr = listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠").map {
        KeyModel(KeyType.Character(it))
    }

    val numbersRowEn = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map {
        KeyModel(KeyType.Character(it))
    }

    val symbolsRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").map {
        KeyModel(KeyType.Character(it))
    }

    val symbolsRow2 = listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")").map {
        KeyModel(KeyType.Character(it))
    }

    val symbolsRow3 = listOf(
        KeyModel(KeyType.Shift, weight = 1.3f),
        KeyModel(KeyType.Character("!")),
        KeyModel(KeyType.Character("\"")),
        KeyModel(KeyType.Character("'")),
        KeyModel(KeyType.Character(":")),
        KeyModel(KeyType.Character(";")),
        KeyModel(KeyType.Character("/")),
        KeyModel(KeyType.Character("?")),
        KeyModel(KeyType.Character("~")),
        KeyModel(KeyType.Backspace, weight = 1.3f)
    )

    val moreSymbolsRow2 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆").map {
        KeyModel(KeyType.Character(it))
    }

    val moreSymbolsRow3 = listOf(
        KeyModel(KeyType.Shift, weight = 1.3f),
        KeyModel(KeyType.Character("£")),
        KeyModel(KeyType.Character("€")),
        KeyModel(KeyType.Character("¥")),
        KeyModel(KeyType.Character("^")),
        KeyModel(KeyType.Character("°")),
        KeyModel(KeyType.Character("=")),
        KeyModel(KeyType.Character("{")),
        KeyModel(KeyType.Character("}")),
        KeyModel(KeyType.Backspace, weight = 1.3f)
    )
}
