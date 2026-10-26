package com.example.data

object PredictionEngine {

    // Comprehensive Arabic vocabulary covering daily communication, greetings, common verbs, questions & adverbs
    private val commonArabicWords = listOf(
        "السلام", "عليكم", "ورحمة", "الله", "وبركاته",
        "صباح", "الخير", "النور", "الورد", "السعادة",
        "مساء", "كيف", "حالك", "صحتك", "أمورك", "أخبارك",
        "أنا", "بخير", "الحمد", "لله", "تمام", "شكراً", "شكرا",
        "جزيلاً", "جزيلا", "عفواً", "عفوا", "أهلاً", "وسهلاً",
        "مرحبا", "إن", "شاء", "ما", "تبارك", "جزاك", "خيراً",
        "بارك", "فيك", "لك", "يعطيك", "العافية", "الصحة",
        "تسلم", "يا", "غالي", "حبيبي", "صديقي", "أخي", "أختي",
        "في", "على", "من", "إلى", "عن", "مع", "هذا", "هذه",
        "هؤلاء", "ذلك", "تلك", "الذي", "التي", "الذين", "نعم", "لا",
        "أجل", "كلا", "أريد", "يمكن", "ممكن", "جميل", "رائع",
        "ممتاز", "حسناً", "حسنا", "طيب", "أكيد", "بالتأكيد", "طبعاً",
        "أراك", "قريباً", "لاحقاً", "غداً", "اليوم", "الآن", "وداعاً",
        "معك", "معي", "عندي", "عندك", "أين", "متى", "لماذا", "كم",
        "كل", "عام", "وأنتم", "بخير", "رمضان", "كريم", "عيد", "مبارك",
        "سعيد", "سنة", "جديدة", "مبروك", "ألف", "بالتوفيق", "إنشاء",
        "كيبورد", "برو", "تطبيق", "هاتف", "رسالة", "صوت", "فيديو",
        "صورة", "ملف", "رابط", "كلمة", "نص", "ترجمة", "نسخ", "لصق",
        "كتابة", "قراءة", "عمل", "شغل", "دوام", "جامعة", "مدرسة",
        "بيت", "منزل", "طريق", "سيارة", "سفر", "مطار", "فندق",
        "أحبك", "مشتاق", "وحشتني", "يسعد", "قلبك", "الله", "يحفظك",
        "يسلمك", "يوفقك", "يرعاك", "آمين", "يارب", "استغفر", "سبحان"
    )

    private val commonEnglishWords = listOf(
        "hello", "hi", "hey", "how", "are", "you", "good", "morning", "afternoon", "evening", "night",
        "thank", "thanks", "welcome", "please", "okay", "alright", "sure", "fine", "great", "nice",
        "awesome", "perfect", "friend", "happy", "love", "today", "tomorrow", "tonight", "work", "time",
        "where", "there", "here", "about", "could", "would", "should", "will", "can", "need", "want",
        "keyboard", "turbo", "pro", "message", "chat", "send", "receive", "call", "later", "soon",
        "brother", "sister", "family", "best", "wishes", "congratulations", "blessings", "peace"
    )

    // Contextual Next-Word Prediction Map for natural phrase flow
    private val arabicNextWordPredictions = mapOf(
        "السلام" to listOf("عليكم", "ورحمة", "لكم"),
        "عليكم" to listOf("ورحمة", "السلام", "جميعاً"),
        "ورحمة" to listOf("الله", "وبركاته", "تعالى"),
        "وبركاته" to listOf("تحياتي", "أهلاً", "وبعد"),
        "صباح" to listOf("الخير", "النور", "الورد"),
        "مساء" to listOf("الخير", "النور", "السعادة"),
        "الخير" to listOf("والبركة", "يا", "جميعاً"),
        "كيف" to listOf("حالك", "صحتك", "أمورك"),
        "حالك" to listOf("اليوم", "يا", "بخير"),
        "الحمد" to listOf("لله", "حمداً", "دائماً"),
        "شكراً" to listOf("جزيلاً", "لك", "أخي"),
        "شكرا" to listOf("جزيلا", "لك", "يا"),
        "إن" to listOf("شاء", "الله", "كنت"),
        "شاء" to listOf("الله", "تعالى", "الرحمن"),
        "ما" to listOf("شاء", "هو", "أخبارك"),
        "جزاك" to listOf("الله", "خيراً", "كل"),
        "بارك" to listOf("الله", "فيك", "لك"),
        "يعطيك" to listOf("العافية", "الصحة", "خير"),
        "أهلاً" to listOf("وسهلاً", "بك", "فيك"),
        "أهلا" to listOf("وسهلا", "بك", "فيك"),
        "كل" to listOf("عام", "يوم", "مرة"),
        "عام" to listOf("وأنتم", "بخير", "سعيد"),
        "وأنتم" to listOf("بخير", "بصحة", "وسعادة"),
        "ألف" to listOf("مبروك", "شكر", "عافية"),
        "الله" to listOf("يسعدك", "يحفظك", "يبارك"),
        "أنا" to listOf("بخير", "في", "أريد"),
        "من" to listOf("فضلك", "أجل", "هنا"),
        "على" to listOf("خير", "كل", "هذا"),
        "في" to listOf("البيت", "العمل", "الطريق"),
        "مع" to listOf("السلامة", "أطيب", "تحياتي"),
        "أراك" to listOf("قريباً", "لاحقاً", "غداً")
    )

    private val englishNextWordPredictions = mapOf(
        "how" to listOf("are", "is", "about"),
        "are" to listOf("you", "they", "we"),
        "you" to listOf("doing", "welcome", "good"),
        "good" to listOf("morning", "evening", "night"),
        "thank" to listOf("you", "so", "very"),
        "thanks" to listOf("a lot", "so much", "brother"),
        "nice" to listOf("to meet", "day", "one"),
        "see" to listOf("you", "it", "soon"),
        "have" to listOf("a good", "a great", "fun")
    )

    data class PredictionResult(
        val word: String,
        val isCorrection: Boolean = false,
        val isCustomCandidate: Boolean = false
    )

    fun getPredictionsWithCorrection(
        prefix: String,
        isArabic: Boolean,
        userWords: List<String> = emptyList()
    ): List<PredictionResult> {
        val clean = prefix.trim()
        val dictionary = if (isArabic) commonArabicWords else commonEnglishWords
        val nextWordMap = if (isArabic) arabicNextWordPredictions else englishNextWordPredictions

        // If prefix is empty (e.g. at start of line or after space):
        // Show polite, natural greetings and conversational starters
        if (clean.isEmpty()) {
            return if (isArabic) {
                listOf(
                    PredictionResult("السلام"),
                    PredictionResult("مرحبا"),
                    PredictionResult("صباح")
                )
            } else {
                listOf(
                    PredictionResult("Hello"),
                    PredictionResult("How"),
                    PredictionResult("Thanks")
                )
            }
        }

        val correction = SpellCheckDictionary.getCorrection(clean, isArabic)
        val results = mutableListOf<PredictionResult>()

        if (correction != null && !correction.equals(clean, ignoreCase = true)) {
            // First priority: corrected spelling!
            results.add(PredictionResult(word = correction, isCorrection = true))
        }

        // Check if clean matches a preceding word for next-word prediction only when no typo
        if (results.isEmpty()) {
            val nextWords = nextWordMap[clean]
            if (nextWords != null && nextWords.isNotEmpty()) {
                return nextWords.take(3).map { PredictionResult(word = it) }
            }
        }

        val combined = (userWords + dictionary).distinct()

        // Prefix matches
        val matches = combined.filter {
            it.startsWith(clean, ignoreCase = true)
        }.sortedBy { it.length }

        for (m in matches) {
            if (results.size < 3 && results.none { it.word.equals(m, ignoreCase = true) }) {
                results.add(PredictionResult(word = m, isCorrection = false))
            }
        }

        // If user typed a complete non-dictionary word, offer to save to personal dictionary
        if (clean.length >= 3 && !combined.any { it.equals(clean, ignoreCase = true) } && correction == null && results.size < 3) {
            results.add(PredictionResult(word = clean, isCustomCandidate = true))
        }

        // Fillers from dictionary if needed (never "اقتراح" or "اقتراحات"!)
        val fallbackWords = if (isArabic) listOf("شكراً", "تمام", "نعم", "ممتاز", "أكيد") else listOf("Okay", "Sure", "Great", "Yes", "Thanks")
        for (fb in fallbackWords) {
            if (results.size >= 3) break
            if (results.none { it.word.equals(fb, ignoreCase = true) }) {
                results.add(PredictionResult(word = fb))
            }
        }

        return results.take(3)
    }

    fun getNextWordPredictions(prevWord: String, isArabic: Boolean): List<PredictionResult> {
        val clean = prevWord.trim().replace(Regex("[،؛؟?!.,:;]+$"), "")
        if (clean.isEmpty()) {
            return getPredictionsWithCorrection("", isArabic)
        }
        val nextWordMap = if (isArabic) arabicNextWordPredictions else englishNextWordPredictions
        val predictions = nextWordMap[clean]
        if (!predictions.isNullOrEmpty()) {
            return predictions.take(3).map { PredictionResult(word = it) }
        }
        // Fallback starters
        return if (isArabic) {
            listOf(PredictionResult("شكراً"), PredictionResult("تمام"), PredictionResult("نعم"))
        } else {
            listOf(PredictionResult("Thanks"), PredictionResult("Okay"), PredictionResult("Sure"))
        }
    }

    fun getPredictions(prefix: String, isArabic: Boolean, userWords: List<String> = emptyList()): List<String> {
        return getPredictionsWithCorrection(prefix, isArabic, userWords).map { it.word }
    }
}
