package com.example.data

object TextDecorator {

    data class DecorationItem(
        val id: String,
        val nameAr: String,
        val nameEn: String,
        val preview: String,
        val transform: (String) -> String
    )

    val arabicStyles = listOf(
        DecorationItem("arabic_ashraf_1", "زخرفة أشرف المعربة 1", "Ashraf Ornate 1", "م֠ــۢ͜ـرح֠ــۢ͜ـب֠ــۢ͜ـآ") { ashrafStyle1(it) },
        DecorationItem("arabic_ashraf_2", "زخرفة أشرف الشدات 2", "Ashraf Ornate 2", "مـْـْْـْرحـْـْْـْبـٌـٌٌـٌُآ") { ashrafStyle2(it) },
        DecorationItem("arabic_ashraf_3", "زخرفة أشرف المدات 3", "Ashraf Ornate 3", "مــہٰٰ۫ـــرحــہٰٰ۫ـــبــہٰٰ۫ـــا") { ashrafStyle3(it) },
        DecorationItem("arabic_ashraf_4", "زخرفة أشرف الأقواس 4", "Ashraf Ornate 4", "م̯͡ر̯͡ح̯͡ب̯͡آ") { ashrafStyle4(it) },
        DecorationItem("arabic_ashraf_hearts", "زخرفة أشرف القلوب ♥", "Ashraf Hearts", "مـ♥ـرحـ♥ـبـ♥ـآ") { ashrafSymbolStyle(it, "♥") },
        DecorationItem("arabic_ashraf_stars", "زخرفة أشرف النجوم ★", "Ashraf Stars", "مـ★ـرحـ★ـبـ★ـآ") { ashrafSymbolStyle(it, "★") },
        DecorationItem("arabic_ashraf_smiles", "زخرفة أشرف الابتسامات ☻", "Ashraf Smiles", "مـ☻ـرحـ☻ـبـ☻ـآ") { ashrafSymbolStyle(it, "☻") },
        DecorationItem("arabic_ashraf_lined", "زخرفة أشرف المسطرة ̲", "Ashraf Underlined", "م̲ر̲ح̲ب̲آ") { ashrafUnderlineStyle(it) },
        DecorationItem("arabic_ornate", "زخرفة عربية حروف", "Arabic Ornate", "مہرحہبہاً") { ornateArabicLetters(it) },
        DecorationItem("arabic_kashida", "زخرفة عربية ممدودة", "Arabic Elongated", "مــرحــبــاً") { elongateArabic(it) },
        DecorationItem("arabic_tashkeel", "تشكيل وتنوين عربي", "Arabic Tashkeel", "مَرْحَبَاً") { tashkeelText(it) },
        DecorationItem("arabic_stars", "زخرفة نجوم ★", "Arabic Stars", "★ مرحباً ★") { "★ $it ★" },
        DecorationItem("arabic_royal", "زخرفة ملكية ꧁꧂", "Royal Ornament", "꧁ مرحباً ꧂") { "꧁ $it ꧂" },
        DecorationItem("arabic_wings", "زخرفة أجنحة ༒", "Angel Wings", "༒ مرحباً ༒") { "༒ $it ༒" }
    )

    val englishStyles = listOf(
        DecorationItem("bold_serif", "عريض سيريف", "Bold Serif", "𝐓𝐞𝐱𝐭 𝐒𝐭𝐲𝐥𝐞") { transformToMap(it, BOLD_SERIF_MAP) },
        DecorationItem("bold_sans", "عريض سانس", "Bold Sans", "𝗧𝗲𝘅𝘁 𝗦𝘁𝘆𝗹𝗲") { transformToMap(it, BOLD_SANS_MAP) },
        DecorationItem("italic_sans", "مائل سانس", "Italic Sans", "𝘛𝘦𝘹𝘵 𝘚𝘵𝘺𝘭𝘦") { transformToMap(it, ITALIC_SANS_MAP) },
        DecorationItem("monospace", "أحادي المسافة", "Monospace", "𝚃𝚎𝚡𝚝 𝚂𝚝𝚢𝚕𝚎") { transformToMap(it, MONOSPACE_MAP) },
        DecorationItem("gothic", "قوطي كلاسيك", "Gothic Fraktur", "𝔗𝔢𝔵𝔱 𝔖𝔱𝔶𝔩𝔢") { transformToMap(it, GOTHIC_MAP) },
        DecorationItem("script", "مخطوطة يدوية", "Script Cursive", "𝓣𝓮𝔁𝓽 𝓢𝓽𝔂𝓵𝓮") { transformToMap(it, SCRIPT_MAP) },
        DecorationItem("square", "مربعات أنيقة", "Square Boxes", "🅃🄴🅇🅃 🅂🅃🅈🄻🄴") { transformToMap(it, SQUARE_MAP) },
        DecorationItem("circle", "دوائر فقاعية", "Bubble Circles", "Ⓣⓔⓧⓣ Ⓢⓣⓨⓛⓔ") { transformToMap(it, CIRCLE_MAP) }
    )

    val allStyles = listOf(
        DecorationItem("none", "بدون زخرفة", "Normal", "Text Style") { it }
    ) + englishStyles + arabicStyles

    fun decorateChar(charStr: String, styleId: String): String {
        if (styleId == "none") return charStr
        if (charStr.length != 1) return charStr
        val c = charStr[0]
        return when (styleId) {
            "bold_serif" -> BOLD_SERIF_MAP[c] ?: charStr
            "bold_sans" -> BOLD_SANS_MAP[c] ?: charStr
            "italic_sans" -> ITALIC_SANS_MAP[c] ?: charStr
            "monospace" -> MONOSPACE_MAP[c] ?: charStr
            "gothic" -> GOTHIC_MAP[c] ?: charStr
            "script" -> SCRIPT_MAP[c] ?: charStr
            "square" -> SQUARE_MAP[c] ?: charStr
            "circle" -> CIRCLE_MAP[c] ?: charStr
            "arabic_ornate" -> ARABIC_ORNATE_MAP[c] ?: charStr
            "arabic_kashida" -> if (isArabicLetter(c) && !isNonConnectingArabic(c)) "${c}\u0640" else charStr
            "arabic_tashkeel" -> if (isArabicLetter(c)) "${c}\u064E" else charStr
            "arabic_ashraf_1" -> if (isArabicLetter(c) && !isNonConnectingArabic(c)) "${c}֠ــۢ͜ـ" else charStr
            "arabic_ashraf_2" -> if (isArabicLetter(c) && !isNonConnectingArabic(c)) "${c}ـْـْْـْ" else charStr
            "arabic_ashraf_3" -> if (isArabicLetter(c) && !isNonConnectingArabic(c)) "${c}ــہٰٰ۫ـــ" else charStr
            "arabic_ashraf_4" -> if (isArabicLetter(c)) "${c}̯͡" else charStr
            "arabic_ashraf_hearts" -> if (isArabicLetter(c) && !isNonConnectingArabic(c)) "${c}ـ♥ـ" else charStr
            "arabic_ashraf_stars" -> if (isArabicLetter(c) && !isNonConnectingArabic(c)) "${c}ـ★ـ" else charStr
            "arabic_ashraf_smiles" -> if (isArabicLetter(c) && !isNonConnectingArabic(c)) "${c}ـ☻ـ" else charStr
            "arabic_ashraf_lined" -> if (isArabicLetter(c)) "${c}\u0332" else charStr
            else -> charStr
        }
    }

    fun decorateText(text: String, styleId: String): String {
        val item = allStyles.find { it.id == styleId } ?: return text
        return item.transform(text)
    }

    private fun isArabicLetter(c: Char): Boolean {
        return c in '\u0621'..'\u064A'
    }

    private fun isNonConnectingArabic(c: Char): Boolean {
        return c in listOf('ا', 'و', 'د', 'ذ', 'ر', 'ز', 'ة', 'ء', 'ى')
    }

    private fun ashrafStyle1(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (isArabicLetter(ch) && !isNonConnectingArabic(ch)) {
                sb.append("֠ــۢ͜ـ")
            }
        }
        return sb.toString()
    }

    private fun ashrafStyle2(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (isArabicLetter(ch) && !isNonConnectingArabic(ch)) {
                sb.append("ـْـْْـْ")
            }
        }
        return sb.toString()
    }

    private fun ashrafStyle3(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (isArabicLetter(ch) && !isNonConnectingArabic(ch)) {
                sb.append("ــہٰٰ۫ـــ")
            }
        }
        return sb.toString()
    }

    private fun ashrafStyle4(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (isArabicLetter(ch)) {
                sb.append("̯͡")
            }
        }
        return sb.toString()
    }

    private fun ashrafSymbolStyle(text: String, symbol: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (isArabicLetter(ch) && !isNonConnectingArabic(ch)) {
                sb.append("ـ${symbol}ـ")
            }
        }
        return sb.toString()
    }

    private fun ashrafUnderlineStyle(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (isArabicLetter(ch)) {
                sb.append("\u0332")
            }
        }
        return sb.toString()
    }

    private fun elongateArabic(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            sb.append(ch)
            if (isArabicLetter(ch) && !isNonConnectingArabic(ch)) {
                sb.append("\u0640")
            }
        }
        return sb.toString()
    }

    private fun tashkeelText(text: String): String {
        val diacritics = listOf("\u064E", "\u0650", "\u064F", "\u0652", "\u0651", "\u064B", "\u064D", "\u064C")
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

    private val ARABIC_ORNATE_MAP = mapOf(
        'ا' to "أ", 'ب' to "بہ", 'ت' to "تہ", 'ث' to "ثہ",
        'ج' to "جہ", 'ح' to "حہ", 'خ' to "خہ", 'د' to "د",
        'ذ' to "ذ", 'ر' to "ر", 'ز' to "ز", 'س' to "سہ",
        'ش' to "شہ", 'ص' to "صہ", 'ض' to "ضه", 'ط' to "طہ",
        'ظ' to "ظ", 'ع' to "عہ", 'غ' to "غه", 'ف' to "فُـ",
        'ق' to "قہ", 'ك' to "كُـ", 'ل' to "لـ", 'م' to "مہ",
        'ن' to "نہ", 'ه' to "هـ", 'و' to "و", 'ي' to "يہ", 'ة' to "ة"
    )

    private fun ornateArabicLetters(text: String): String {
        val sb = StringBuilder()
        for (c in text) {
            sb.append(ARABIC_ORNATE_MAP[c] ?: c.toString())
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

    private val ITALIC_SANS_MAP = mapOf(
        'A' to "𝘈", 'B' to "𝘉", 'C' to "𝘊", 'D' to "𝘋", 'E' to "𝘌", 'F' to "𝘍", 'G' to "𝘎", 'H' to "𝘏",
        'I' to "𝘐", 'J' to "𝘑", 'K' to "𝘒", 'L' to "𝘓", 'M' to "𝘔", 'N' to "𝘕", 'O' to "𝘖", 'P' to "𝘗",
        'Q' to "𝘘", 'R' to "𝘙", 'S' to "𝘚", 'T' to "𝘛", 'U' to "𝘜", 'V' to "𝘝", 'W' to "𝘞", 'X' to "𝘟",
        'Y' to "𝘠", 'Z' to "𝘡",
        'a' to "𝘢", 'b' to "𝘣", 'c' to "𝘤", 'd' to "𝘥", 'e' to "𝘦", 'f' to "𝘧", 'g' to "𝘨", 'h' to "𝘩",
        'i' to "𝘪", 'j' to "𝘫", 'k' to "𝘬", 'l' to "𝘭", 'm' to "𝘮", 'n' to "𝘯", 'o' to "𝘰", 'p' to "𝘱",
        'q' to "𝘲", 'r' to "𝘳", 's' to "𝓈", 't' to "𝘵", 'u' to "𝘶", 'v' to "𝘷", 'w' to "𝘸", 'x' to "𝘹",
        'y' to "𝘺", 'z' to "𝘻"
    )

    private val MONOSPACE_MAP = mapOf(
        'A' to "𝚃", 'B' to "𝙱", 'C' to "𝙲", 'D' to "𝙳", 'E' to "𝙴", 'F' to "𝙵", 'G' to "𝙶", 'H' to "𝙷",
        'I' to "𝙸", 'J' to "𝙹", 'K' to "𝙺", 'L' to "𝙻", 'M' to "𝙼", 'N' to "𝙽", 'O' to "𝙾", 'P' to "𝙿",
        'Q' to "𝚀", 'R' to "𝚁", 'S' to "𝚂", 'T' to "𝚃", 'U' to "𝚄", 'V' to "𝚅", 'W' to "𝚆", 'X' to "𝚇",
        'Y' to "𝚈", 'Z' to "𝚉",
        'a' to "𝚝", 'b' to "𝚋", 'c' to "𝚌", 'd' to "𝚍", 'e' to "𝚎", 'f' to "𝚏", 'g' to "𝚐", 'h' to "𝚑",
        'i' to "𝚒", 'j' to "𝚓", 'k' to "𝚔", 'l' to "𝚕", 'm' to "𝚖", 'n' to "𝚗", 'o' to "𝚘", 'p' to "𝚙",
        'q' to "𝚚", 'r' to "𝚛", 's' to "𝚜", 't' to "𝚝", 'u' to "𝚞", 'v' to "𝚟", 'w' to "𝚠", 'x' to "𝚡",
        'y' to "𝚢", 'z' to "𝚣"
    )

    private val GOTHIC_MAP = mapOf(
        'A' to "𝔄", 'B' to "𝔅", 'C' to "ℭ", 'D' to "𝔇", 'E' to "𝔈", 'F' to "𝔉", 'G' to "𝔊", 'H' to "ℌ",
        'I' to "ℑ", 'J' to "𝔍", 'K' to "𝔎", 'L' to "𝔏", 'M' to "𝔐", 'N' to "𝔑", 'O' to "𝔒", 'P' to "𝔓",
        'Q' to "𝔔", 'R' to "ℜ", 'S' to "𝔖", 'T' to "𝔗", 'U' to "𝔘", 'V' to "𝔙", 'W' to "𝔚", 'X' to "𝔛",
        'Y' to "𝔜", 'Z' to "ℨ",
        'a' to "𝔞", 'b' to "𝔟", 'c' to "𝔠", 'd' to "𝔡", 'e' to "𝔢", 'f' to "𝔣", 'g' to "𝔤", 'h' to "𝔥",
        'i' to "𝔦", 'j' to "𝔧", 'k' to "𝔨", 'l' to "ل", 'm' to "𝔪", 'n' to "𝔫", 'o' to "𝔬", 'p' to "𝔭",
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

    // زخرفة أسماء ألعاب وبرامج تواصل (ببجي، فري فاير، فيسبوك، انستا، تيك توك)
    fun generateGameAndSocialNicknames(name: String): List<String> {
        val base = if (name.isBlank()) "الـمـلـك" else name.trim()
        val ornate = ornateArabicLetters(base)
        val elongated = elongateArabic(base)

        return listOf(
            "꧁༺ $base ༻꧂",
            "★彡 $base 彡★",
            "亗『 $base 』亗",
            "⚔️ ⦅ $base ⦆ ⚔️",
            "♛ $base ♛",
            "★ᴾᴿᴼ★ $base",
            "乡 $base 乡",
            "🔥『 $ornate 』🔥",
            "『ツ』$base",
            "༺LeGeNd༻ $base",
            "亗 $base 亗",
            "〆 $base 〆",
            "۝ $base ۝",
            "⚡ $base ⚡",
            "👑 $ornate 👑",
            "【 $base 】",
            "⫷ $base ⫸",
            "•°¯`•• $base ••´¯°•",
            "««—(¯`v´¯)—»» $base",
            "░▒▓█ $base █▓▒░",
            "★ $elongated ★",
            "✧ $base ✧",
            "༺ $base ༻",
            "ッ $base",
            "𝒯𝓊𝓇𝒷ℴ | $base",
            "『V I P』$base",
            "★ $base ★",
            "ღ $base ღ"
        )
    }
}
