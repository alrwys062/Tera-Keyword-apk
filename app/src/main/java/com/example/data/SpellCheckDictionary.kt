package com.example.data

object SpellCheckDictionary {

    // Common Arabic spelling mistakes and typos mapping: typo -> correct word
    private val arabicTypoMap = mapOf(
        // Hamzas and Alifs
        "انشاء الله" to "إن شاء الله",
        "ان شاء الله" to "إن شاء الله",
        "انشالله" to "إن شاء الله",
        "لاكن" to "لكن",
        "لاكنن" to "لكن",
        "هدا" to "هذا",
        "هاذا" to "هذا",
        "هاذه" to "هذه",
        "هاذولا" to "هؤلاء",
        "اللذي" to "الذي",
        "اللتي" to "التي",
        "اللة" to "الله",
        "اللاه" to "الله",
        "شكراا" to "شكراً",
        "شكرن" to "شكراً",
        "عفون" to "عفواً",
        "مرسي" to "شكراً جزيلاً",
        "مساء لخير" to "مساء الخير",
        "مساءالخير" to "مساء الخير",
        "صباح لخير" to "صباح الخير",
        "صباحالخير" to "صباح الخير",
        "معليش" to "أعتذر",
        "اشوفك" to "أراك",
        "ابغي" to "أريد",
        "ابي" to "أريد",
        "ابغا" to "أريد",
        "كويس" to "جيد",
        "حلوو" to "رائع",
        "شلونك" to "كيف حالك",
        "كيفك" to "كيف حالك",
        "اخبارك" to "أخبارك",
        "شخبارك" to "كيف حالك",
        "وينك" to "أين أنت",
        "فينك" to "أين أنت",
        "تلقائى" to "تلقائي",
        "نهائى" to "نهائي",
        "مسؤلية" to "مسؤولية",
        "مسؤل" to "مسؤول",
        "قرائة" to "قراءة",
        "بدائة" to "بداية",
        "مائة" to "مئة",
        "رائيس" to "رئيس",
        "فائيدة" to "فائدة",
        "مهمةه" to "مهمة",
        "انت" to "أنت",
        "انتي" to "أنتِ",
        "اليك" to "إليك",
        "الي" to "إلى",
        "علي" to "على",
        "فى" to "في",
        "ان" to "إن",
        "اذا" to "إذا",
        "ايميل" to "بريد إلكتروني",
        "اوكي" to "حسناً",
        "اوك" to "حسناً",
        "طيب" to "حسناً",
        "يالله" to "يا الله",
        "يلا" to "هيا بنا",
        "هسة" to "الآن",
        "دحين" to "الآن",
        "الحين" to "الآن",
        "مشكور" to "شكراً جزيلاً",
        "تسلم" to "سلمك الله",
        "يعطيك العافية" to "جزاك الله خيراً",
        "عساك بخير" to "أتمنى لك دوام الصحة والعافية"
    )

    // Common English typos mapping
    private val englishTypoMap = mapOf(
        "teh" to "the",
        "thier" to "their",
        "recieve" to "receive",
        "seperate" to "separate",
        "alot" to "a lot",
        "untill" to "until",
        "occured" to "occurred",
        "definately" to "definitely",
        "truely" to "truly",
        "tommorrow" to "tomorrow",
        "goverment" to "government",
        "freind" to "friend",
        "beleive" to "believe",
        "neccessary" to "necessary",
        "wierd" to "weird",
        "becuase" to "because",
        "congradulations" to "congratulations",
        "calender" to "calendar",
        "accomodate" to "accommodate",
        "embarass" to "embarrass"
    )

    fun getCorrection(word: String, isArabic: Boolean): String? {
        val clean = word.trim().lowercase()
        if (clean.isBlank()) return null
        return if (isArabic) {
            arabicTypoMap[word.trim()] ?: arabicTypoMap[clean]
        } else {
            englishTypoMap[clean]
        }
    }

    fun hasCorrection(word: String, isArabic: Boolean): Boolean {
        return getCorrection(word, isArabic) != null
    }

    // Auto-correct an entire sentence intelligently
    fun autoCorrectSentence(sentence: String, isArabic: Boolean): String {
        if (sentence.isBlank()) return sentence
        var result = sentence

        if (isArabic) {
            arabicTypoMap.forEach { (typo, correction) ->
                val regex = Regex("(?<=^|\\s)${Regex.escape(typo)}(?=$|\\s|[,.!؟])", RegexOption.IGNORE_CASE)
                result = result.replace(regex, correction)
            }
            // General Arabic punctuation and space cleanup
            result = result.replace(Regex(" +"), " ")
                .replace(" ،", "،")
                .replace(" ؟", "؟")
                .replace(" .", ".")
        } else {
            englishTypoMap.forEach { (typo, correction) ->
                val regex = Regex("(?<=^|\\s)${Regex.escape(typo)}(?=$|\\s|[,.!?])", RegexOption.IGNORE_CASE)
                result = result.replace(regex, correction)
            }
        }
        return result
    }

    // Get common dictionary words to test spelling against
    fun getAllSampleCorrections(): List<Pair<String, String>> {
        return arabicTypoMap.entries.take(20).map { it.key to it.value }
    }
}
