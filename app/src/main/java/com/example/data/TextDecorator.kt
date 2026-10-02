package com.example.data

object TextDecorator {

    data class DecorationItem(
        val nameAr: String,
        val nameEn: String,
        val preview: String,
        val transform: (String) -> String
    )

    val arabicStyles = listOf(
        DecorationItem("زخرفة كشيدة ممدودة", "Arabic Elongated", "مــرحــبــاً") { text ->
            val sb = StringBuilder()
            for (ch in text) {
                sb.append(ch)
                if (isArabicLetter(ch) && ch != 'ا' && ch != 'و' && ch != 'د' && ch != 'ذ' && ch != 'ر' && ch != 'ز' && ch != 'ة') {
                    sb.append("ــ")
                }
            }
            sb.toString()
        },
        DecorationItem("زخرفة الملكية ꧁꧂", "Royal Ornament", "꧁ مرحباً ꧂") { text ->
            "꧁ $text ꧂"
        },
        DecorationItem("زخرفة ريشة ༺༻", "Feather Ornament", "༺ مرحباً ༻") { text ->
            "༺ $text ༻"
        },
        DecorationItem("زخرفة الأجنحة ༒༒", "Angel Wings", "༒ مرحباً ༒") { text ->
            "༒ $text ༒"
        },
        DecorationItem("تشكيل وتنوين خفيف", "Tashkeel Diacritics", "مَرْحَبَاً") { text ->
            tashkeelText(text)
        },
        DecorationItem("زخرفة إسلامية ۞۞", "Islamic Star", "۞ مرحباً ۞") { text ->
            "۞ $text ۞"
        },
        DecorationItem("زخرفة الفراشات ✿✿", "Flower Blossom", "✿ مرحباً ✿") { text ->
            "✿ $text ✿"
        },
        DecorationItem("زخرفة النجوم ★★★", "Star Border", "★ مرحباً ★") { text ->
            "★ $text ★"
        },
        DecorationItem("زخرفة الأقواس ⦅⦆", "Curved Brackets", "⦅ مرحباً ⦆") { text ->
            "⦅ $text ⦆"
        },
        DecorationItem("زخرفة الحروف المشكلة", "Arabic Ornate Letters", "مہرحہبہاً") { text ->
            ornateArabicLetters(text)
        }
    )

    val englishStyles = listOf(
        DecorationItem("𝒞𝓊𝓇𝓈𝒾𝓋ℯ 𝒮𝒸𝓇𝒾𝓅𝓉", "Cursive Script", "ℋℯ𝓁𝓁ℴ") { text ->
            transformToMap(text, SCRIPT_MAP)
        },
        DecorationItem("𝐁𝐨𝐥𝐝 𝐒𝐞𝐫𝐢𝐟", "Bold Serif", "𝐇𝐞𝐥𝐥𝐨") { text ->
            transformToMap(text, BOLD_SERIF_MAP)
        },
        DecorationItem("𝗕𝗼𝗹𝗱 𝗦𝗮𝗻𝘀", "Bold Sans", "𝗛𝗲𝗹𝗹𝗼") { text ->
            transformToMap(text, BOLD_SANS_MAP)
        },
        DecorationItem("𝔊𝔬𝔱𝔥𝔦𝔠 𝔉𝔯𝔞𝔨𝔱𝔲𝔯", "Gothic Fraktur", "𝔈𝔵𝔞𝔪𝔭𝔩𝔢") { text ->
            transformToMap(text, GOTHIC_MAP)
        },
        DecorationItem("Ⓑⓤⓑⓑⓛⓔ Ⓒⓘⓡⓒⓛⓔ", "Bubble Circles", "Ⓗⓔⓛⓛⓞ") { text ->
            transformToMap(text, CIRCLE_MAP)
        },
        DecorationItem("🅂🅀🅄🄰🅁🄴 🄱🄾🅇", "Square Boxes", "🄷🄴🄻🄻🄾") { text ->
            transformToMap(text, SQUARE_MAP)
        },
        DecorationItem("𝕎𝕚𝕕𝕖 𝔽𝕦𝕝𝕝𝕨𝕚𝕕𝕥𝕙", "Fullwidth", "Ｈｅｌｌｏ") { text ->
            transformToMap(text, FULLWIDTH_MAP)
        },
        DecorationItem("U̲n̲d̲e̲r̲l̲i̲n̲e̲d̲", "Underlined", "H̲e̲l̲l̲o̲") { text ->
            val sb = StringBuilder()
            for (ch in text) {
                sb.append(ch).append('\u0332')
            }
            sb.toString()
        },
        DecorationItem("S̶t̶r̶i̶k̶e̶t̶h̶r̶o̶u̶g̶h̶", "Strikethrough", "H̶e̶l̶l̶o̶") { text ->
            val sb = StringBuilder()
            for (ch in text) {
                sb.append(ch).append('\u0336')
            }
            sb.toString()
        },
        DecorationItem("★ N e o n ★", "Spaced Stars", "★ H e l l o ★") { text ->
            "★ " + text.toCharArray().joinToString(" ") + " ★"
        }
    )

    private fun isArabicLetter(c: Char): Boolean {
        return c in '\u0621'..'\u064A'
    }

    private fun tashkeelText(text: String): String {
        val diacritics = listOf("َ", "ِ", "ُ", "ْ", "ّ", "ً", "ٍ", "ٌ")
        val sb = StringBuilder()
        var i = 0
        for (c in text) {
            sb.append(c)
            if (isArabicLetter(c)) {
                sb.append(diacritics[i % diacritics.size])
                i++
            }
        }
        return sb.toString()
    }

    private fun ornateArabicLetters(text: String): String {
        val map = mapOf(
            'ا' to "أ", 'ب' to "بہ", 'ت' to "تہ", 'ث' to "ثہ",
            'ج' to "جہ", 'ح' to "حہ", 'خ' to "خہ", 'د' to "د",
            'ذ' to "ذ", 'ر' to "ر", 'ز' to "ز", 'س' to "سہ",
            'ش' to "شہ", 'ص' to "صہ", 'ض' to "ضه", 'ط' to "طہ",
            'ظ' to "ظ", 'ع' to "عہ", 'غ' to "غه", 'ف' to "فُـ",
            'ق' to "قہ", 'ك' to "كُـ", 'ل' to "لـ", 'م' to "مہ",
            'ن' to "نہ", 'ه' to "هـ", 'و' to "و", 'ي' to "يہ", 'ة' to "ة"
        )
        val sb = StringBuilder()
        for (c in text) {
            sb.append(map[c] ?: c.toString())
        }
        return sb.toString()
    }

    private fun transformToMap(text: String, map: Map<Char, String>): String {
        val sb = StringBuilder()
        for (c in text) {
            sb.append(map[c] ?: c.toString())
        }
        return sb.toString()
    }

    // Maps for Unicode transforms
    private val SCRIPT_MAP = mapOf(
        'A' to "𝒜", 'B' to "ℬ", 'C' to "𝒞", 'D' to "𝒟", 'E' to "ℰ", 'F' to "ℱ", 'G' to "𝒢", 'H' to "ℋ",
        'I' to "ℐ", 'J' to "𝒥", 'K' to "𝒦", 'L' to "ℒ", 'M' to "ℳ", 'N' to "𝒩", 'O' to "𝒪", 'P' to "𝒫",
        'Q' to "𝒬", 'R' to "ℛ", 'S' to "𝒮", 'T' to "𝒯", 'U' to "𝒰", 'V' to "𝒱", 'W' to "𝒲", 'X' to "𝒳",
        'Y' to "𝒴", 'Z' to "𝒵",
        'a' to "𝒶", 'b' to "𝒷", 'c' to "𝒸", 'd' to "𝒹", 'e' to "ℯ", 'f' to "𝒻", 'g' to "ℊ", 'h' to "𝒽",
        'i' to "𝒾", 'j' to "𝒿", 'k' to "𝓀", 'l' to "𝓁", 'm' to "𝓂", 'n' to "𝓃", 'o' to "ℴ", 'p' to "𝓅",
        'q' to "𝓆", 'r' to "𝓇", 's' to "𝓈", 't' to "𝓉", 'u' to "𝓊", 'v' to "𝓋", 'w' to "𝓌", 'x' to "𝓍",
        'y' to "𝓎", 'z' to "𝓏"
    )

    private val BOLD_SERIF_MAP = mapOf(
        'A' to "𝐀", 'B' to "𝐁", 'C' to "𝐂", 'D' to "𝐃", 'E' to "𝐄", 'F' to "𝐅", 'G' to "𝐆", 'H' to "𝐇",
        'I' to "𝐈", 'J' to "𝐉", 'K' to "𝐊", 'L' to "𝐋", 'M' to "𝐌", 'N' to "𝐍", 'O' to "𝐎", 'P' to "𝐏",
        'Q' to "𝐐", 'R' to "𝐑", 'S' to "𝐒", 'T' to "𝐓", 'U' to "𝐔", 'V' to "𝐕", 'W' to "𝐖", 'X' to "𝐗",
        'Y' to "𝐘", 'Z' to "𝐙",
        'a' to "𝐚", 'b' to "𝐛", 'c' to "𝐜", 'd' to "𝐝", 'e' to "𝐞", 'f' to "𝐟", 'g' to "𝐠", 'h' to "𝐡",
        'i' to "𝐢", 'j' to "𝐣", 'k' to "𝐤", 'l' to "𝐥", 'm' to "𝐦", 'n' to "𝐧", 'o' to "𝐨", 'p' to "𝐩",
        'q' to "𝐪", 'r' to "𝐫", 's' to "𝐬", 't' to "𝐭", 'u' to "𝐮", 'v' to "𝐯", 'w' to "𝐰", 'x' to "𝐱",
        'y' to "𝐲", 'z' to "𝐳",
        '0' to "𝟎", '1' to "𝟏", '2' to "𝟐", '3' to "𝟑", '4' to "𝟒", '5' to "𝟓", '6' to "𝟔", '7' to "𝟕",
        '8' to "𝟖", '9' to "𝟗"
    )

    private val BOLD_SANS_MAP = mapOf(
        'A' to "𝗔", 'B' to "𝗕", 'C' to "𝗖", 'D' to "𝗗", 'E' to "𝗘", 'F' to "𝗙", 'G' to "𝗚", 'H' to "𝗛",
        'I' to "𝗜", 'J' to "𝗝", 'K' to "𝗞", 'L' to "𝗟", 'M' to "𝗠", 'N' to "𝗡", 'O' to "𝗢", 'P' to "𝗣",
        'Q' to "𝗤", 'R' to "𝗥", 'S' to "𝗦", 'T' to "𝗧", 'U' to "𝗨", 'V' to "𝗩", 'W' to "𝗪", 'X' to "𝗫",
        'Y' to "𝗬", 'Z' to "𝗭",
        'a' to "𝗮", 'b' to "𝗯", 'c' to "𝗰", 'd' to "𝗱", 'e' to "𝗲", 'f' to "𝗳", 'g' to "𝗴", 'h' to "𝗵",
        'i' to "𝗶", 'j' to "𝗷", 'k' to "𝗸", 'l' to "𝗹", 'm' to "𝗺", 'n' to "𝗻", 'o' to "𝗼", 'p' to "𝗽",
        'q' to "𝗾", 'r' to "𝗿", 's' to "𝘀", 't' to "𝘁", 'u' to "𝘂", 'v' to "𝘃", 'w' to "𝘄", 'x' to "𝘅",
        'y' to "𝘆", 'z' to "𝘇"
    )

    private val GOTHIC_MAP = mapOf(
        'A' to "𝔄", 'B' to "𝔅", 'C' to "ℭ", 'D' to "𝔇", 'E' to "𝔈", 'F' to "𝔉", 'G' to "𝔊", 'H' to "ℌ",
        'I' to "ℑ", 'J' to "𝔍", 'K' to "𝔎", 'L' to "𝔏", 'M' to "𝔐", 'N' to "𝔑", 'O' to "𝔒", 'P' to "𝔓",
        'Q' to "𝔔", 'R' to "ℜ", 'S' to "𝔖", 'T' to "𝔗", 'U' to "𝔘", 'V' to "𝔙", 'W' to "𝔚", 'X' to "𝔛",
        'Y' to "𝔜", 'Z' to "ℨ",
        'a' to "𝔞", 'b' to "𝔟", 'c' to "𝔠", 'd' to "𝔡", 'e' to "𝔢", 'f' to "𝔣", 'g' to "𝔤", 'h' to "𝔥",
        'i' to "𝔦", 'j' to "𝔧", 'k' to "𝔨", 'l' to "𝔩", 'm' to "𝔪", 'n' to "𝔫", 'o' to "𝔬", 'p' to "𝔭",
        'q' to "𝔮", 'r' to "𝔯", 's' to "𝔰", 't' to "𝔱", 'u' to "𝔲", 'v' to "𝔳", 'w' to "𝔴", 'x' to "𝔵",
        'y' to "𝔶", 'z' to "𝔷"
    )

    private val CIRCLE_MAP = mapOf(
        'A' to "Ⓐ", 'B' to "Ⓑ", 'C' to "Ⓒ", 'D' to "Ⓓ", 'E' to "Ⓔ", 'F' to "Ⓕ", 'G' to "Ⓖ", 'H' to "Ⓗ",
        'I' to "Ⓘ", 'J' to "Ⓙ", 'K' to "Ⓚ", 'L' to "Ⓛ", 'M' to "Ⓜ", 'N' to "Ⓝ", 'O' to "Ⓞ", 'P' to "Ⓟ",
        'Q' to "Ⓠ", 'R' to "Ⓡ", 'S' to "Ⓢ", 'T' to "Ⓣ", 'U' to "Ⓤ", 'V' to "Ⓥ", 'W' to "Ⓦ", 'X' to "Ⓧ",
        'Y' to "Ⓨ", 'Z' to "Ⓩ",
        'a' to "ⓐ", 'b' to "ⓑ", 'c' to "ⓒ", 'd' to "ⓓ", 'e' to "ⓔ", 'f' to "ⓕ", 'g' to "ⓖ", 'h' to "ⓗ",
        'i' to "ⓘ", 'j' to "ⓙ", 'k' to "ⓚ", 'l' to "ⓛ", 'm' to "ⓜ", 'n' to "ⓝ", 'o' to "ⓞ", 'p' to "ⓟ",
        'q' to "ⓠ", 'r' to "ⓡ", 's' to "ⓢ", 't' to "ⓣ", 'u' to "ⓤ", 'v' to "ⓥ", 'w' to "ⓦ", 'x' to "ⓧ",
        'y' to "ⓨ", 'z' to "ⓩ",
        '1' to "①", '2' to "②", '3' to "③", '4' to "④", '5' to "⑤", '6' to "⑥", '7' to "⑦", '8' to "⑧", '9' to "⑨"
    )

    private val SQUARE_MAP = mapOf(
        'A' to "🄰", 'B' to "🄱", 'C' to "🄲", 'D' to "🄳", 'E' to "🄴", 'F' to "🄵", 'G' to "🄶", 'H' to "🄷",
        'I' to "🄸", 'J' to "🄹", 'K' to "🄺", 'L' to "🄻", 'M' to "🄼", 'N' to "🄽", 'O' to "🄾", 'P' to "🄿",
        'Q' to "🅀", 'R' to "🅁", 'S' to "🅂", 'T' to "🅃", 'U' to "🅄", 'V' to "🅅", 'W' to "🅆", 'X' to "🅇",
        'Y' to "🅈", 'Z' to "🅉",
        'a' to "🄰", 'b' to "🄱", 'c' to "🄲", 'd' to "🄳", 'e' to "🄴", 'f' to "🄵", 'g' to "🄶", 'h' to "🄷",
        'i' to "🄸", 'j' to "🄹", 'k' to "🄺", 'l' to "🄻", 'm' to "🄼", 'n' to "🄽", 'o' to "🄾", 'p' to "🄿",
        'q' to "🅀", 'r' to "🅁", 's' to "🅂", 't' to "🅃", 'u' to "🅄", 'v' to "🅅", 'w' to "🅆", 'x' to "🅇",
        'y' to "🅈", 'z' to "🅉"
    )

    private val FULLWIDTH_MAP = mapOf(
        'A' to "Ａ", 'B' to "Ｂ", 'C' to "Ｃ", 'D' to "Ｄ", 'E' to "Ｅ", 'F' to "Ｆ", 'G' to "Ｇ", 'H' to "Ｈ",
        'I' to "Ｉ", 'J' to "Ｊ", 'K' to "Ｋ", 'L' to "Ｌ", 'M' to "Ｍ", 'N' to "Ｎ", 'O' to "Ｏ", 'P' to "Ｐ",
        'Q' to "Ｑ", 'R' to "Ｒ", 'S' to "Ｓ", 'T' to "Ｔ", 'U' to "Ｕ", 'V' to "Ｖ", 'W' to "Ｗ", 'X' to "Ｘ",
        'Y' to "Ｙ", 'Z' to "Ｚ",
        'a' to "ａ", 'b' to "ｂ", 'c' to "ｃ", 'd' to "ｄ", 'e' to "ｅ", 'f' to "ｆ", 'g' to "ｇ", 'h' to "ｈ",
        'i' to "ｉ", 'j' to "ｊ", 'k' to "ｋ", 'l' to "ｌ", 'm' to "ｍ", 'n' to "ｎ", 'o' to "ｏ", 'p' to "ｐ",
        'q' to "ｑ", 'r' to "ｒ", 's' to "ｓ", 't' to "ｔ", 'u' to "ｕ", 'v' to "ｖ", 'w' to "ｗ", 'x' to "ｘ",
        'y' to "ｙ", 'z' to "ｚ"
    )
}
