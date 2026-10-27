package com.example.keyboard

/**
 * AutocorrectEngine
 * Highly optimized Trie and Hash dictionary for real-time Arabic and English suggestion & autocorrection.
 */
class AutocorrectEngine {

    class TrieNode {
        val children = HashMap<Char, TrieNode>()
        var isEndOfWord = false
        var frequency = 0
    }

    private val arabicRoot = TrieNode()
    private val englishRoot = TrieNode()

    init {
        // Populate standard Arabic vocabulary
        val arabicWords = listOf(
            "مرحبا", "السلام", "عليكم", "صباح", "الخير", "مساء", "النور",
            "شكرا", "جزيل", "أهلا", "سهلا", "كيف", "حالك", "الحمد", "لله",
            "إن", "شاء", "الله", "تمام", "أنا", "بخير", "في", "من", "على",
            "إلى", "عن", "مع", "هذا", "هذه", "ذلك", "الذي", "التي", "نعم",
            "لا", "شكراً", "حبيبي", "يا", "أخي", "صديقي", "متناسق", "ممتاز",
            "تطبيق", "لوحة", "مفاتيح", "رسالة", "كتابة", "إرسال", "اليوم",
            "غدا", "الآن", "سريع", "جميل", "حفظ", "جديد", "أخبار", "معلومات"
        )
        for (w in arabicWords) {
            insertWord(arabicRoot, w, 10)
        }

        // Populate standard English vocabulary
        val englishWords = listOf(
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
            "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
            "this", "but", "his", "by", "from", "they", "we", "say", "her",
            "she", "or", "an", "will", "my", "one", "all", "would", "there",
            "their", "what", "so", "up", "out", "if", "about", "who", "get",
            "which", "go", "me", "when", "make", "can", "like", "time", "no",
            "just", "him", "know", "take", "people", "into", "year", "your",
            "good", "some", "could", "them", "see", "other", "than", "then",
            "now", "look", "only", "come", "its", "over", "think", "also",
            "back", "after", "use", "two", "how", "our", "work", "first",
            "well", "way", "even", "new", "want", "because", "any", "these",
            "give", "day", "most", "us", "hello", "thanks", "keyboard"
        )
        for (w in englishWords) {
            insertWord(englishRoot, w, 10)
        }
    }

    fun addWord(word: String, isArabic: Boolean, frequency: Int = 1) {
        val root = if (isArabic) arabicRoot else englishRoot
        insertWord(root, word.trim(), frequency)
    }

    private fun insertWord(root: TrieNode, word: String, frequency: Int) {
        if (word.isBlank()) return
        var current = root
        for (ch in word.lowercase()) {
            current = current.children.getOrPut(ch) { TrieNode() }
        }
        current.isEndOfWord = true
        current.frequency += frequency
    }

    /**
     * Search top 3 suggestions matching prefix
     */
    fun getTopSuggestions(prefix: String, isArabic: Boolean, limit: Int = 3): List<String> {
        val cleanPrefix = prefix.trim().lowercase()
        if (cleanPrefix.isEmpty()) return emptyList()

        val root = if (isArabic) arabicRoot else englishRoot
        var current = root

        for (ch in cleanPrefix) {
            current = current.children[ch] ?: return emptyList()
        }

        val results = mutableListOf<Pair<String, Int>>()
        collectAllWords(current, StringBuilder(cleanPrefix), results)

        return results
            .sortedByDescending { it.second }
            .map { it.first }
            .take(limit)
    }

    private fun collectAllWords(node: TrieNode, currentWord: StringBuilder, results: MutableList<Pair<String, Int>>) {
        if (node.isEndOfWord) {
            results.add(Pair(currentWord.toString(), node.frequency))
        }
        for ((ch, child) in node.children) {
            currentWord.append(ch)
            collectAllWords(child, currentWord, results)
            currentWord.deleteCharAt(currentWord.length - 1)
        }
    }

    companion object {
        val instance by lazy { AutocorrectEngine() }
    }
}
