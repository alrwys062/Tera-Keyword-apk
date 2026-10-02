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

    fun getPredictions(prefix: String, isArabic: Boolean, userWords: List<String> = emptyList()): List<String> {
        val clean = prefix.trim().lowercase()
        if (clean.isEmpty()) {
            return if (isArabic) {
                listOf("اقتراحات", "اقتراح", "اقتراحات")
            } else {
                listOf("suggest", "suggestion", "suggestive")
            }
        }

        val dictionary = if (isArabic) commonArabicWords else commonEnglishWords
        val combined = userWords + dictionary
        val matches = combined.filter { it.lowercase().startsWith(clean) && it.lowercase() != clean }.distinct()

        return if (matches.isNotEmpty()) {
            val list = matches.take(3).toMutableList()
            while (list.size < 3) {
                list.add(if (isArabic) "اقتراح" else "suggest")
            }
            list
        } else {
            if (isArabic) {
                listOf("${clean}ة", clean, "${clean}ات")
            } else {
                listOf(clean, "${clean}s", "${clean}ing")
            }
        }
    }
}
