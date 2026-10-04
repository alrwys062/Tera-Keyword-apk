package com.example.data

object LongPressVariantsManager {

    // Complete Arabic letters variants (Diacritics, Tanween, Shaddah, Phonetic derivatives like پ, چ, گ, ڤ, etc.)
    val arabicVariants: Map<String, List<String>> = mapOf(
        "ب" to listOf("پ", "ـ", "ب", "بّ", "بَ", "بٍ", "بٌ", "ٺ", "ٹ", "ٻ", "ݕ"),
        "ت" to listOf("ت", "ة", "تّ", "تَ", "تِ", "تُ", "تً", "تٍ", "تٌ", "ٹ", "ٺ"),
        "ث" to listOf("ث", "ثّ", "ثَ", "ثِ", "ثُ", "ثً", "ثٍ", "ثٌ"),
        "ج" to listOf("چ", "ج", "جّ", "جَ", "جِ", "جُ", "ڃ", "ڄ"),
        "ح" to listOf("ح", "حّ", "حَ", "حِ", "حُ", "حً", "حٍ", "حٌ"),
        "خ" to listOf("خ", "خّ", "خَ", "خِ", "خُ", "خً", "خٍ", "خٌ"),
        "د" to listOf("د", "دّ", "دَ", "دِ", "دُ", "ڈ", "ڌ", "ڍ"),
        "ذ" to listOf("ذ", "ذّ", "ذَ", "ذِ", "ذُ"),
        "ر" to listOf("ر", "رّ", "رَ", "رِ", "رُ", "ڑ", "ڕ", "ړ"),
        "ز" to listOf("ز", "ژ", "زّ", "زَ", "زِ", "زُ", "ڙ"),
        "س" to listOf("س", "سّ", "سَ", "سِ", "سُ", "سً", "سٍ", "سٌ", "ښ"),
        "ش" to listOf("ش", "شّ", "شَ", "شِ", "شُ", "شً", "شٍ", "شٌ", "ڜ"),
        "ص" to listOf("ص", "صّ", "صَ", "صِ", "صُ", "صً", "صٍ", "صٌ"),
        "ض" to listOf("ض", "ضّ", "ضَ", "ضِ", "ضُ", "ضً", "ضٍ", "ضٌ"),
        "ط" to listOf("ط", "طّ", "طَ", "طِ", "طُ", "طً", "طٍ", "طٌ"),
        "ظ" to listOf("ظ", "ظّ", "ظَ", "ظِ", "ظُ"),
        "ع" to listOf("ع", "عّ", "عَ", "عِ", "عُ", "عً", "عٍ", "عٌ"),
        "غ" to listOf("غ", "غّ", "غَ", "غِ", "غُ", "ڠ"),
        "ف" to listOf("ف", "ڤ", "فّ", "فَ", "فِ", "فُ", "فً", "فٍ", "فٌ", "ڢ"),
        "ق" to listOf("ق", "ڨ", "قّ", "قَ", "قِ", "قُ", "ڧ"),
        "ك" to listOf("ك", "گ", "ک", "ڪ", "كّ", "كَ", "كِ", "كُ"),
        "ل" to listOf("ل", "لا", "لأ", "لإ", "لآ", "لّ", "لَ", "لِ", "لُ"),
        "م" to listOf("م", "مّ", "مَ", "مِ", "مُ", "مً", "مٍ", "مٌ", "۾", "ݥ"),
        "ن" to listOf("ن", "ڹ", "نّ", "نَ", "نِ", "نُ", "نً", "نٍ", "نٌ", "ں", "ݧ", "ݩ"),
        "ه" to listOf("ه", "هّ", "هَ", "هِ", "هُ", "ھ", "ہ", "ۂ", "ة"),
        "و" to listOf("و", "ؤ", "وّ", "وَ", "وِ", "وُ", "ۆ", "ۇ", "ۉ"),
        "ي" to listOf("ي", "ئ", "ى", "يـ", "يّة", "يَ", "يِ", "يُ", "ؠ"),
        "ا" to listOf("ا", "أ", "إ", "آ", "ٱ", "ء", "ـ"),
        "ء" to listOf("ء", "ئ", "ؤ", "أ", "إ", "آ"),
        "ى" to listOf("ى", "ي", "ئ", "ۍ"),
        "ة" to listOf("ة", "ت", "ه"),
        "ئ" to listOf("ئ", "ي", "ء", "ى"),
        "ؤ" to listOf("ؤ", "و", "ء")
    )

    // Complete English letters variants (Unicode styles: Bold, Italic Script, Fraktur, Circled, Accents)
    val englishVariants: Map<String, List<String>> = mapOf(
        "a" to listOf("a", "á", "à", "â", "ä", "ã", "å", "ā", "𝒂", "𝓪", "𝔞", "ⓐ"),
        "b" to listOf("b", "𝒃", "𝓫", "𝔟", "𝔅", "ⓑ", "β"),
        "c" to listOf("c", "ç", "ć", "č", "𝒄", "𝓬", "𝔠", "ⓒ", "©"),
        "d" to listOf("d", "ð", "ď", "𝒅", "𝓭", "𝔡", "ⓓ"),
        "e" to listOf("e", "é", "è", "ê", "ë", "ē", "ė", "ę", "𝒆", "𝓮", "𝔢", "ⓔ", "€"),
        "f" to listOf("f", "𝒇", "𝓯", "𝔣", "𝔉", "ⓕ"),
        "g" to listOf("g", "ğ", "ġ", "ģ", "𝒈", "𝓰", "𝔤", "ⓖ"),
        "h" to listOf("h", "ħ", "𝒉", "𝓱", "𝔥", "ⓗ"),
        "i" to listOf("i", "í", "ì", "î", "ï", "ī", "ı", "𝒊", "𝓲", "𝔦", "ⓘ"),
        "j" to listOf("j", "ʲ", "𝒋", "𝓳", "𝔧", "ⓙ"),
        "k" to listOf("k", "ķ", "𝒌", "𝓴", "𝔨", "ⓚ"),
        "l" to listOf("l", "ł", "ĺ", "ľ", "𝒍", "𝓵", "𝔩", "ⓛ", "£"),
        "m" to listOf("m", "𝒎", "𝓶", "𝔪", "ⓜ"),
        "n" to listOf("n", "ñ", "ń", "ň", "𝒏", "𝓷", "𝔫", "ⓝ"),
        "o" to listOf("o", "ó", "ò", "ô", "ö", "õ", "ō", "ø", "œ", "𝒐", "𝓸", "𝔬", "ⓞ"),
        "p" to listOf("p", "𝒑", "𝓹", "𝔭", "𝔓", "ⓟ"),
        "q" to listOf("q", "𝒒", "𝓺", "𝔮", "ⓠ"),
        "r" to listOf("r", "ŕ", "ř", "𝒓", "𝓻", "𝔯", "ⓡ", "®"),
        "s" to listOf("s", "ß", "ś", "š", "ş", "𝒔", "𝓼", "𝔰", "ⓢ", "$"),
        "t" to listOf("t", "ť", "ţ", "𝒕", "𝓽", "𝔱", "ⓣ", "™"),
        "u" to listOf("u", "ú", "ù", "û", "ü", "ū", "𝒖", "𝓾", "𝔲", "ⓤ"),
        "v" to listOf("v", "𝒗", "𝓿", "𝔳", "ⓥ"),
        "w" to listOf("w", "𝒘", "𝔀", "𝔴", "ⓦ"),
        "x" to listOf("x", "𝒙", "𝔵", "ⓧ", "×"),
        "y" to listOf("y", "ý", "ÿ", "𝒚", "𝔂", "𝔶", "ⓨ", "¥"),
        "z" to listOf("z", "ž", "ź", "ż", "𝒛", "𝔃", "𝔷", "ⓩ")
    )

    // Numbers variants (Arabic Eastern, Western, Superscript, Subscript, fractions)
    val numberVariants: Map<String, List<String>> = mapOf(
        "1" to listOf("1", "١", "¹", "₁", "½", "¼", "①"),
        "2" to listOf("2", "٢", "²", "₂", "⅔", "②"),
        "3" to listOf("3", "٣", "³", "₃", "¾", "③"),
        "4" to listOf("4", "٤", "⁴", "₄", "④"),
        "5" to listOf("5", "٥", "⁵", "₅", "⑤"),
        "6" to listOf("6", "٦", "⁶", "₆", "⑥"),
        "7" to listOf("7", "٧", "⁷", "₇", "⑦"),
        "8" to listOf("8", "٨", "⁸", "₈", "⑧"),
        "9" to listOf("9", "٩", "⁹", "₉", "⑨"),
        "0" to listOf("0", "٠", "⁰", "₀", "∅", "⓪"),
        "١" to listOf("١", "1", "¹", "½", "①"),
        "٢" to listOf("٢", "2", "²", "⅔", "②"),
        "٣" to listOf("٣", "3", "³", "¾", "③"),
        "٤" to listOf("٤", "4", "⁴", "④"),
        "٥" to listOf("٥", "5", "⁵", "⑤"),
        "٦" to listOf("٦", "6", "⁶", "⑥"),
        "٧" to listOf("٧", "7", "⁷", "⑦"),
        "٨" to listOf("٨", "8", "⁸", "⑧"),
        "٩" to listOf("٩", "9", "⁹", "⑨"),
        "٠" to listOf("٠", "0", "⁰", "∅", "⓪")
    )

    fun getVariants(char: String): List<String> {
        val lower = char.lowercase()
        return arabicVariants[char]
            ?: arabicVariants[lower]
            ?: englishVariants[lower]
            ?: numberVariants[char]
            ?: listOf(char)
    }

    // Number hint mapping for Row 1
    val arabicRow1Hints = mapOf(
        "ض" to "١", "ص" to "٢", "ث" to "٣", "ق" to "٤", "ف" to "٥",
        "غ" to "٦", "ع" to "٧", "ه" to "٨", "خ" to "٩", "ح" to "٠"
    )

    val englishRow1Hints = mapOf(
        "q" to "1", "w" to "2", "e" to "3", "r" to "4", "t" to "5",
        "y" to "6", "u" to "7", "i" to "8", "o" to "9", "p" to "0"
    )
}
