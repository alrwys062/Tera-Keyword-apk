package com.example.data

object PredictionEngine {

    private val commonArabicWords = listOf(
        "اقتراح", "اقتراحات", "شكراً", "شكرا", "مرحبا", "السلام", "عليكم", "ورحمة", "الله", "وبركاته",
        "كيف", "حالك", "اليوم", "أنا", "بخير", "الحمد", "لله", "تمام", "صباح", "الخير", "مساء",
        "إن", "شاء", "في", "على", "من", "إلى", "مع", "هذا", "هذه", "الذي", "التي", "نعم", "لا",
        "أريد", "يمكن", "جميل", "رائع", "ممتاز", "حسناً", "بالتأكيد", "أراك", "قريباً", "وداعاً",
        "كيبورد", "برو", "تطبيق", "هاتف", "رسالة", "جديدة", "كل", "عام", "وأنتم", "بخير"
    )

    private val commonEnglishWords = listOf(
        "suggestion", "suggest", "suggestive", "hello", "how", "are", "you", "good", "morning",
        "thank", "thanks", "welcome", "please", "okay", "alright", "keyboard", "turbo", "pro",
        "message", "chat", "love", "great", "nice", "awesome", "perfect", "friend", "happy",
        "today", "tomorrow", "tonight", "work", "time", "where", "there", "about", "could", "would"
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
        if (clean.isEmpty()) {
            return if (isArabic) {
                listOf(
                    PredictionResult("اقتراحات"),
                    PredictionResult("اقتراح"),
                    PredictionResult("اقتراحات")
                )
            } else {
                listOf(
                    PredictionResult("suggest"),
                    PredictionResult("suggestion"),
                    PredictionResult("suggestive")
                )
            }
        }

        val correction = SpellCheckDictionary.getCorrection(clean, isArabic)
        val dictionary = if (isArabic) commonArabicWords else commonEnglishWords
        val combined = (userWords + dictionary).distinct()
        val matches = combined.filter {
            it.lowercase().startsWith(clean.lowercase()) && it.lowercase() != clean.lowercase()
        }

        val results = mutableListOf<PredictionResult>()

        if (correction != null) {
            // First priority: corrected spelling!
            results.add(PredictionResult(word = correction, isCorrection = true))
        }

        for (m in matches) {
            if (results.size < 3 && results.none { it.word == m }) {
                results.add(PredictionResult(word = m, isCorrection = false))
            }
        }

        // If user typed a word not in dictionary and not corrected, offer to save to user dictionary
        if (clean.length >= 3 && !combined.contains(clean) && correction == null && results.size < 3) {
            results.add(PredictionResult(word = clean, isCustomCandidate = true))
        }

        // Fillers if needed
        while (results.size < 3) {
            val fallback = if (isArabic) "اقتراح" else "suggest"
            results.add(PredictionResult(word = fallback))
        }

        return results.take(3)
    }

    fun getPredictions(prefix: String, isArabic: Boolean, userWords: List<String> = emptyList()): List<String> {
        return getPredictionsWithCorrection(prefix, isArabic, userWords).map { it.word }
    }
}

